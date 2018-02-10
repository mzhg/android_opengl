package jet.learning.opengl.upsampling;

import android.opengl.GLES20;

import com.nvidia.developer.opengl.utils.BufferUtils;
import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.GLUtil;
import com.nvidia.developer.opengl.utils.NvDisposeable;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL11;

/**
 * Created by mazhen'gui on 2018/2/10.
 */

final class ParticleRenderer implements NvDisposeable{
    final Params m_params = new Params();
    ParticleSystem m_particleSystem;
    int m_batchSize;

    LightViewParticleProgram m_lightViewParticleProg;
    CameraViewParticleProgram m_cameraViewParticleProg;

    int m_vbo;
    int m_frameId;
    int[] m_eboArray;
    int m_eboCount = 2;
    boolean m_isGL;

    private FloatBuffer mParitcileBuffer;

    public ParticleRenderer(boolean isGL) {
        m_isGL = isGL;

        m_particleSystem = new ParticleSystem();
        mParitcileBuffer = BufferUtils.createFloatBuffer(m_particleSystem.getPositions().length * 3);

        createShaders();
        createVBO();
        createEBOs();
    }

    @Override
    public void dispose() {
        m_particleSystem = null;

        deleteShaders();
        deleteVBO();
        deleteEBOs();
    }

    void createShaders()
    {
        m_lightViewParticleProg 		= new LightViewParticleProgram();
        m_cameraViewParticleProg 	= new CameraViewParticleProgram();
    }

    void deleteShaders()
    {
        m_lightViewParticleProg.dispose();
        m_cameraViewParticleProg.dispose();
    }

    void drawPointsSorted(int positionAttrib, int start, int count)
    {
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, m_vbo);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, m_eboArray[m_frameId]);

        GLES20.glVertexAttribPointer(positionAttrib, 3, GL11.GL_FLOAT, false, 0, 0);
        GLES20.glEnableVertexAttribArray(positionAttrib);

        if (m_isGL) {
            /*GLES20.glEnable(GLES20.GL_POINT_SPRITE);
            GLES20.glEnable(GLES20.GL_PROGRAM_POINT_SIZE);*/
        }
        GLES20.glDrawElements(GL11.GL_POINTS, count, GL11.GL_UNSIGNED_SHORT, /*(void*)(sizeof(GLushort)*start)*/ 2 * start);

        GLES20.glDisableVertexAttribArray(positionAttrib);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    void drawSliceCameraView(SceneInfo scene, int i)
    {
        scene.m_fbos.m_particleFbo.bind();

        if (scene.m_invertedView)
        {
            // front-to-back blending
            GLES20.glBlendFunc(GL11.GL_ONE_MINUS_DST_ALPHA, GL11.GL_ONE);
        }
        else
        {
            // back-to-front blending with pre-multiplied alpha
            GLES20.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        }

        m_cameraViewParticleProg.enable();

        // these uniforms are slice-invariant, so only set them for the first slice
        if (i == 0) m_cameraViewParticleProg.setUniforms(scene, m_params);

        drawPointsSorted(m_cameraViewParticleProg.getPositionAttrib(), i*m_batchSize, m_batchSize);
    }

    void drawSliceLightView(SceneInfo scene, int i)
    {
        scene.m_fbos.m_lightFbo.bind();

        // back-to-front alpha blending
        GLES20.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        m_lightViewParticleProg.enable();

        // these uniforms are slice-invariant, so only set them for the first slice
        if (i == 0) m_lightViewParticleProg.setUniforms(scene, m_params);

        drawPointsSorted(m_lightViewParticleProg.getPositionAttrib(), i*m_batchSize, m_batchSize);
    }

    void renderParticles(SceneInfo scene)
    {
        // depth-test the particles against the low-res depth buffer
        GLES20.glEnable(GL11.GL_DEPTH_TEST);

        GLES20.glDepthMask(false);  // don't write depth
        GLES20.glEnable(GL11.GL_BLEND);

        // render slices
        for(int i = 0; i < m_params.numSlices; ++i)
        {
            // draw slice from camera view, sampling light buffer
            drawSliceCameraView(scene, i);

            if (m_params.renderShadows)
            {
                // draw slice from light view to light buffer, accumulating shadows
                drawSliceLightView(scene, i);
            }
        }

        GLES20.glDepthMask(true);
        GLES20.glDisable(GL11.GL_BLEND);
    }

    void deleteVBO()
    {
        if (m_vbo != 0)
        {
            GLES.glDeleteBuffers(m_vbo);
            m_vbo = 0;
        }
    }

    void createVBO()
    {
        deleteVBO();

        final int numActive = getNumActive();
        FloatBuffer buffer = GLUtil.getCachedFloatBuffer(3 * numActive);
        Vector3f[] positions = m_particleSystem.getPositions();
        for(int i = 0; i < numActive; i++)
            positions[i].store(buffer);
        buffer.flip();

        m_vbo = GLES.glGenBuffers();
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, m_vbo);
        GLES.glBufferData(GLES20.GL_ARRAY_BUFFER, buffer, GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

    void deleteEBOs()
    {
        if (m_eboArray != null)
        {
            GLES20.glDeleteBuffers(m_eboArray.length, m_eboArray, 0);
            m_eboArray = null;
        }
    }

    void createEBOs()
    {
        deleteEBOs();

        // to avoid CPU<.GPU sync points when updating DYNAMIC buffers,
        // use an array of buffers and use the least-recently-used one each frame.
        m_eboArray = new int[m_eboCount];
        GLES20.glGenBuffers(m_eboCount, m_eboArray, 0);

        for (int i = 0; i < m_eboCount; ++i)
        {
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, m_eboArray[i]);
            GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, 2 * getNumActive(),null,  GLES20.GL_DYNAMIC_DRAW);
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        }
    }

    void updateEBO()
    {
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, m_eboArray[m_frameId]);
        GLES.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, GLUtil.wrap(m_particleSystem.getSortedIndices(), 0, getNumActive()), GLES20.GL_DYNAMIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    void depthSort(SceneInfo scene)
    {
        m_particleSystem.depthSort(scene.m_halfVector);

        m_batchSize = getNumActive() / (int)m_params.numSlices;
    }

    int getNumActive()
    {
        return m_particleSystem.getNumActive();
    }

    void swapBuffers()
    {
        m_frameId = (m_frameId + 1) % m_eboCount;
    }

    Params getParams()
    {
        return m_params;
    }

    static final class Params{
        boolean renderShadows = true;
        int   numSlices = 16;
        float softness = 0.4f;
        float particleScale = ParticleUpsampling.PARTICLE_SCALE;
        float pointRadius = 0.2f;
        float shadowAlpha = 0.04f;
        float spriteAlpha = 0.2f;

        float getPointScale(Matrix4f projectionMatrix)
        {
//	            int viewport[4];
//	            glGetIntegerv(GL_VIEWPORT, viewport);
            IntBuffer viewport = GLUtil.getCachedIntBuffer(16);
            GLES20.glGetIntegerv(GL11.GL_VIEWPORT, viewport);

            float worldSpacePointRadius = pointRadius * particleScale;
            float projectionScaleY = projectionMatrix.m11;
            float viewportHeight = viewport.get(3);

            return worldSpacePointRadius * projectionScaleY * viewportHeight;
        }
    }
}
