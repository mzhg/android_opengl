package jet.learning.opengl.upsampling;

import android.opengl.GLES20;
import android.opengl.GLES30;

import com.nvidia.developer.opengl.utils.GLUtil;
import com.nvidia.developer.opengl.utils.NvDisposeable;
import com.nvidia.developer.opengl.utils.NvGLModel;
import com.nvidia.developer.opengl.utils.NvSimpleFBO;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL11;

/**
 * Created by mazhen'gui on 2018/2/10.
 */

final class SceneRenderer implements NvDisposeable{
    final Params m_params = new Params();
    NvGLModel m_model;
    ParticleRenderer m_particles;
    Upsampler m_upsampler;
    final SceneInfo m_scene = new SceneInfo();

    int m_screenWidth;
    int m_screenHeight;
    SceneFBOs m_fbos;

    OpaqueColorProgram m_opaqueColorProg;
    OpaqueDepthProgram m_opaqueDepthProg;

    public SceneRenderer(boolean isGL) {
        initTimers();
        loadModel();

        m_opaqueColorProg = new OpaqueColorProgram();
        m_opaqueDepthProg = new OpaqueDepthProgram();

        m_fbos = new SceneFBOs();
        m_particles = new ParticleRenderer(isGL);
        m_upsampler = new Upsampler(m_fbos, isGL);

//	        memset(&m_scene, 0, sizeof(m_scene));
        m_scene.setLightVector(new Vector3f(-0.70710683f, 0.50000000f, 0.49999994f));
        m_scene.setLightDistance(6.f);
        m_scene.m_fbos = m_fbos;
    }

    @Override
    public void dispose() {
        m_fbos.dispose();
        m_opaqueColorProg.dispose();
        m_opaqueDepthProg.dispose();
        m_particles.dispose();
    }

    void initTimers(){}

    void loadModel(){
        m_model = new NvGLModel();
        m_model.loadModelFromFile("assets/models/cow.obj");
        m_model.rescaleModel(1.0f);
        m_model.initBuffers();
    }

    void drawModel(int positionAttrib, int normalAttrib)
    {
        if (!m_params.drawModel)
        {
            return;
        }

        if (normalAttrib >= 0)
        {
            m_model.drawElements(positionAttrib, normalAttrib);
        }
        else
        {
            m_model.drawElements(positionAttrib);
        }
    }

    void drawFloor(int positionAttrib, int normalAttrib)
    {
        float s =  4.f;
        float y = -1.f;

        // world-space positions and normals
        final float vertices[] =
                {
                        -s, y, -s, 	0.f, 1.f, 0.f,
                        s, y, -s,  0.f, 1.f, 0.f,
                        s, y, s,   0.f, 1.f, 0.f,
                        -s, y, s,   0.f, 1.f, 0.f,
                };

        final byte indices[] =
                {
                        2,1,0,
                        3,2,0
                };

        FloatBuffer vbuf = GLUtil.wrap(vertices);
        if (normalAttrib >= 0)
        {
            GLES20.glVertexAttribPointer(positionAttrib, 3, GLES20.GL_FLOAT, false, 6 * 4, vbuf);
            GLES20.glEnableVertexAttribArray(positionAttrib);
            vbuf.position(3);
            GLES20.glVertexAttribPointer(normalAttrib, 3,GLES20.GL_FLOAT, false, 6 * 4, vbuf);
            GLES20.glEnableVertexAttribArray(normalAttrib);

            GLES20.glDrawElements(GL11.GL_TRIANGLES, indices.length, GLES20.GL_UNSIGNED_BYTE, GLUtil.wrap(indices));

            GLES20.glDisableVertexAttribArray(positionAttrib);
            GLES20.glDisableVertexAttribArray(normalAttrib);
        }
        else
        {
            GLES20.glVertexAttribPointer(positionAttrib, 3, GLES20.GL_FLOAT,  false, 6 * 4, vbuf);
            GLES20.glEnableVertexAttribArray(positionAttrib);

            GLES20.glDrawElements(GL11.GL_TRIANGLES, indices.length, GLES20.GL_UNSIGNED_BYTE,GLUtil.wrap(indices));

            GLES20.glDisableVertexAttribArray(positionAttrib);
        }
    }

    void drawScene(int positionAttrib, int normalAttrib)
    {
        drawFloor(positionAttrib, normalAttrib);

        drawModel(positionAttrib, normalAttrib);
    }

    // render the opaque geometry to the depth buffer
    void renderSceneDepth(NvSimpleFBO depthFbo)
    {
        // bind the FBO and set the viewport to the FBO resolution
        depthFbo.bind();

        // depth-only pass, disable color writes
        GLES20.glColorMask(false, false, false, false);

        // enable depth testing and depth writes
        GLES20.glEnable(GL11.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GL11.GL_LESS);
        GLES20.glDepthMask(true);

        // clear depths to 1.0
        GLES20.glClear(GL11.GL_DEPTH_BUFFER_BIT);

        // draw the geometry with a dummy fragment shader
        m_opaqueDepthProg.enable();
        m_opaqueDepthProg.setUniforms(m_scene);
        drawScene(m_opaqueDepthProg.getPositionAttrib(), -1);

        // restore color writes
        GLES20.glColorMask(true, true, true, true);
    }

    void downsampleSceneDepth(NvSimpleFBO srcFbo, NvSimpleFBO dstFbo)
    {
        GLES20.glBindFramebuffer(GLES30.GL_READ_FRAMEBUFFER, srcFbo.fbo);
        GLES20.glBindFramebuffer(GLES30.GL_DRAW_FRAMEBUFFER, dstFbo.fbo);

        GLES30.glBlitFramebuffer(0, 0, srcFbo.width, srcFbo.height,
                0, 0, dstFbo.width, dstFbo.height,
                GL11.GL_DEPTH_BUFFER_BIT, GL11.GL_NEAREST);
    }

    // initialize the depth buffer to depth-test the low-res particles against
    void renderLowResSceneDepth()
    {
        // if the scene-resolution depth buffer can be used as a depth pre-pass
        // to speedup the forward shading passes, then it may make sense to
        // render the opaque geometry in full resolution first, and then
        // downsample the full-resolution depths to the particle resolution.

        if (m_params.useDepthPrepass)
        {
            renderSceneDepth(m_fbos.m_sceneFbo);

            downsampleSceneDepth(m_fbos.m_sceneFbo, m_fbos.m_particleFbo);
        }
        else
        {
            renderSceneDepth(m_fbos.m_particleFbo);
        }
    }

    // render the colors of the opaque geometry, receiving shadows from the particles
    void renderFullResSceneColor()
    {
        GLES20.glClearColor(m_params.backgroundColor.x, m_params.backgroundColor.y, m_params.backgroundColor.z, 0.0f);
        GLES20.glEnable(GL11.GL_DEPTH_TEST);

        m_fbos.m_sceneFbo.bind();

        m_opaqueColorProg.enable();
        m_opaqueColorProg.setUniforms(m_scene);

        if (m_params.useDepthPrepass)
        {
            // if we are using the depth pre-pass strategy, then re-use the full-resolution depth buffer
            // initialized earlier and perform a z-equal pass against it with depth writes disabled.

            GLES20.glDepthFunc(GL11.GL_EQUAL);
            GLES20.glDepthMask(false);

            GLES20.glClear(GL11.GL_COLOR_BUFFER_BIT);

            drawScene(m_opaqueColorProg.getPositionAttrib(), m_opaqueColorProg.getNormalAttrib());

            GLES20.glDepthFunc(GL11.GL_LESS);
            GLES20.glDepthMask(true);
        }
        else
        {
            GLES20.glDepthFunc(GL11.GL_LESS);
            GLES20.glDepthMask(true);

            GLES20.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

            drawScene(m_opaqueColorProg.getPositionAttrib(), m_opaqueColorProg.getNormalAttrib());
        }
    }

    void renderFrame()
    {
        {
            m_scene.calcVectors();
            m_particles.depthSort(m_scene);
        }

        {
            m_particles.updateEBO();
        }

        {
            // render scene depth to buffer for particle to be depth tested against
            renderLowResSceneDepth();
        }

        // clear light buffer
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, m_fbos.m_lightFbo.fbo);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GL11.GL_COLOR_BUFFER_BIT);

        // clear volume image
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, m_fbos.m_particleFbo.fbo);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GL11.GL_COLOR_BUFFER_BIT);

        {
            m_particles.renderParticles(m_scene);
        }

        {
            // the opaque colors need to be rendered after the particles
            // for the particles to cast shadows on the opaque scene
            renderFullResSceneColor();
        }

        {
            // upsample the particles & composite them on top of the opaque scene colors
            m_upsampler.upsampleParticleColors(m_scene);
        }

        {
            // final bilinear upsampling from scene resolution to backbuffer resolution
            m_upsampler.upsampleSceneColors(m_scene);
        }

        m_particles.swapBuffers();
    }

    void reshapeWindow(int w, int h)
    {
        m_scene.setScreenSize(w, h);
        createScreenBuffers();
    }

    void createScreenBuffers()
    {
        m_fbos.createScreenBuffers(m_scene.m_screenWidth, m_scene.m_screenHeight);
    }

    void createLightBuffer()
    {
        m_fbos.createLightBuffer();
    }

    void setEyeViewMatrix(Matrix4f viewMatrix)
    {
        m_scene.m_eyeView.load(viewMatrix);
    }

    ParticleRenderer.Params getParticleParams()
    {
        return m_particles.getParams();
    }

    Upsampler.Params getUpsamplingParams()
    {
        return m_upsampler.getParams();
    }

    SceneFBOs.Params getSceneFBOParams()
    {
        return m_fbos.m_params;
    }

    SceneRenderer.Params getSceneParams()
    {
        return m_params;
    }

    int getScreenWidth()
    {
        return m_scene.m_screenWidth;
    }

    int getScreenHeight()
    {
        return m_scene.m_screenHeight;
    }

    static final class Params{
        boolean drawModel = true;
        boolean useDepthPrepass;
        Vector3f backgroundColor = new Vector3f(0.5f, 0.8f, 1.0f);
    }
}
