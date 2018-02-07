package jet.learning.opengl.optimization;

import android.opengl.GLES20;

import com.nvidia.developer.opengl.utils.BufferUtils;
import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.GLUtil;
import com.nvidia.developer.opengl.utils.NvDisposeable;
import com.nvidia.developer.opengl.utils.NvGLSLProgram;
import com.nvidia.developer.opengl.utils.NvWritableFB;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL11;

/**
 * Created by mazhen'gui on 2018/2/6.
 */

final class ParticleRenderer implements NvDisposeable {
    final Params m_params = new Params();
    ParticleSystem m_particleSystem;
    int m_batchSize;

    CameraViewParticleProgram m_cameraViewParticleProg;

    int[] m_vboArray;
    int m_frameId;
    int[] m_eboArray;
    int m_bufferCount = 2;

    private ShortBuffer mIndiceBuffer;
    private FloatBuffer mPositionBuffer;

    public ParticleRenderer(boolean isES2) {
        m_particleSystem = new ParticleSystem();

        mPositionBuffer = BufferUtils.createFloatBuffer(m_particleSystem.getPositions().length * 4);

        createShaders(isES2);
        createVBOs();
        createEBOs();
    }

    public void dispose() {
        m_particleSystem = null;

        deleteShaders();
        deleteVBOs();
        deleteEBOs();
    }

    void createShaders(boolean isES2)
    {
        m_cameraViewParticleProg = new CameraViewParticleProgram(isES2);
    }

    void deleteShaders()
    {
        if(m_cameraViewParticleProg != null){
            m_cameraViewParticleProg.dispose();
            m_cameraViewParticleProg = null;
        }
    }

    NvGLSLProgram getCameraViewProgram()
    {
        return m_cameraViewParticleProg;
    }

    void drawPointsSorted(int positionAttrib, int start, int count)
    {
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, m_vboArray[m_frameId]);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, m_eboArray[m_frameId]);

        GLES20.glVertexAttribPointer(positionAttrib, 4, GL11.GL_FLOAT, false, 0, 0);
        GLES20.glEnableVertexAttribArray(positionAttrib);

        // Hack for OpenGL-as-GLES; need to avoid this
//	   #ifndef ANDROID  // TODO
        /*GLES20.glEnable(GLES20.GL_POINT_SPRITE);
        GLES20.glEnable(GLES20.GL_PROGRAM_POINT_SIZE);*/
//	   #endif
        GLES20.glDrawElements(GL11.GL_POINTS, count, GL11.GL_UNSIGNED_SHORT, (2*start));

        GLES20.glDisableVertexAttribArray(positionAttrib);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    void drawSliceCameraView(SceneInfo scene, int i)
    {
        // back-to-front blending with pre-multiplied alpha
        GLES20.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);

        m_cameraViewParticleProg.enable();

        // these uniforms are slice-invariant, so only set them for the first slice
        if (i == 0)
            m_cameraViewParticleProg.setUniforms(scene, m_params);

        drawPointsSorted(m_cameraViewParticleProg.getPositionAndColorAttrib(), i*m_batchSize, m_batchSize);
    }

    void renderParticles(SceneInfo scene)
    {
        if (!m_params.render)
            return;

        // depth-test the particles against the low-res depth buffer
        GLES20.glEnable(GL11.GL_DEPTH_TEST);

        // Clear volume image only if we are rendering to a special off-screen buffer.
        targetFBO(scene).bind();
        if (m_params.renderLowResolution)
        {
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            GLES20.glClear(GL11.GL_COLOR_BUFFER_BIT);
        }
        else
        {
            // Otherwise we are blending on top of the main scene.  We still need to clear dest alpha only.
            GLES20.glColorMask(false, false, false, true);
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            GLES20.glClear(GL11.GL_COLOR_BUFFER_BIT);
            GLES20.glColorMask(true, true, true, true);
        }

        GLES20.glDepthMask(false);  // don't write depth
        GLES20.glEnable(GL11.GL_BLEND);

        // render slices
        for(int i = 0; i < m_params.numSlices; ++i)
        {
            // draw slice from camera view, sampling light buffer
            drawSliceCameraView(scene, i);
        }

        GLES20.glDepthMask(true);
        GLES20.glDisable(GL11.GL_BLEND);
    }

    void deleteVBOs()
    {
        if (m_vboArray != null)
        {
            GLES20.glDeleteBuffers(m_vboArray.length, m_vboArray, 0);
            m_vboArray = null;
        }
    }

    void createVBOs()
    {
        deleteVBOs();

        m_vboArray = new int[m_bufferCount];
        GLES20.glGenBuffers(m_vboArray.length, m_vboArray, 0);

        for (int i = 0; i < m_bufferCount; ++i)
        {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, m_vboArray[i]);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, 4 * 4 * getNumActive(), null,  GLES20.GL_STATIC_DRAW);
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        }
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
        m_eboArray = new int[m_bufferCount];
        GLES20.glGenBuffers(m_eboArray.length, m_eboArray, 0);

        for (int i = 0; i < m_bufferCount; ++i)
        {
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, m_eboArray[i]);
            GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, 2 * getNumActive(), null, GLES20.GL_DYNAMIC_DRAW);
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
//	           CHECK_GL_ERROR();
        }
    }

    void updateEBO()
    {
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, m_eboArray[m_frameId]);
        GLES.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, /*sizeof(GLushort) * getNumActive(), m_particleSystem.getSortedIndices(),*/ GLUtil.wrap(m_particleSystem.getSortedIndices(), 0, getNumActive()), GLES20.GL_DYNAMIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    void updateVBO()
    {

        mPositionBuffer.clear();
        Vector4f[] positions = m_particleSystem.getPositions();
        int numActives = m_particleSystem.getNumActive();
        for(int i = 0; i < numActives; i++){
            positions[i].store(mPositionBuffer);
        }
        mPositionBuffer.flip();

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, m_vboArray[m_frameId]);
        GLES.glBufferData(GLES20.GL_ARRAY_BUFFER, mPositionBuffer, GLES20.GL_DYNAMIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

    void updateBuffers()
    {
        updateVBO();
        updateEBO();
    }

    void simulate(SceneInfo scene, float frameElapsed)
    {
        m_particleSystem.simulate(frameElapsed, scene.m_viewVector, scene.m_eyePos);
        m_batchSize = getNumActive() / (int)m_params.numSlices;
    }

    NvWritableFB targetFBO(SceneInfo s)
    {
        if (m_params.renderLowResolution)
            return s.m_fbos.m_particleFbo;
        else
            return s.m_fbos.m_sceneFbo;
    }

    int getNumActive()
    {
        return m_particleSystem.getNumActive();
    }

    void swapBuffers()
    {
        m_frameId = (m_frameId + 1) % m_bufferCount;
    }

    Params getParams()
    {
        return m_params;
    }

    static final class Params{
        boolean render = true;
        boolean renderLowResolution;
        float numSlices = 16;
        float softness =6.0f;
        float particleScale = OptimizationApp.PARTICLE_SCALE;
        float pointRadius = 22.0f;
        float spriteAlpha = 0.55f;

        float getPointScale(Matrix4f projectionMatrix)
        {
            IntBuffer viewport = GLUtil.getCachedIntBuffer(16);
            GLES20.glGetIntegerv(GL11.GL_VIEWPORT, viewport);

            float worldSpacePointRadius = pointRadius * particleScale;
            float projectionScaleY = projectionMatrix.m11;
            float viewportHeight = (float)(viewport.get(3));

            return worldSpacePointRadius * projectionScaleY * viewportHeight;
        }
    }
}
