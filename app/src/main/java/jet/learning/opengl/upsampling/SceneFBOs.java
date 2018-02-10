package jet.learning.opengl.upsampling;

import android.opengl.GLES20;
import android.opengl.GLES30;

import com.nvidia.developer.opengl.utils.NvDisposeable;
import com.nvidia.developer.opengl.utils.NvSimpleFBO;

import javax.microedition.khronos.opengles.GL11;

/**
 * Created by mazhen'gui on 2018/2/10.
 */

final class SceneFBOs implements NvDisposeable{
    final Params m_params = new Params();
    int m_sceneResX;
    int m_sceneResY;

    NvSimpleFBO m_lightFbo;
    NvSimpleFBO m_particleFbo;
    NvSimpleFBO m_sceneFbo;

    public SceneFBOs() {
        createLightBuffer();
    }

    void createScreenBuffers(int w, int h)
    {
        m_sceneResX  = w / m_params.sceneDownsample;
        m_sceneResY = h / m_params.sceneDownsample;
        createParticleBuffer();
        createSceneBuffer();
    }

    void createLightBuffer()
    {
        NvSimpleFBO.Desc desc = new NvSimpleFBO.Desc();
        desc.width = m_params.lightBufferSize;
        desc.height = m_params.lightBufferSize;
        desc.color.internalFormat = GLES30.GL_R8;
        desc.color.format = GLES30.GL_RED;
        desc.color.type = GL11.GL_UNSIGNED_BYTE;
        desc.color.filter = GL11.GL_LINEAR;
        desc.color.wrap = GL11.GL_CLAMP_TO_EDGE;

        if(m_lightFbo != null)
            m_lightFbo.dispose();
        m_lightFbo = new NvSimpleFBO(desc);
    }

    void createParticleBuffer()
    {
        NvSimpleFBO.Desc desc = new NvSimpleFBO.Desc();
        desc.width = m_sceneResX   / m_params.particleDownsample;
        desc.height = m_sceneResY / m_params.particleDownsample;

        desc.color.internalFormat = GLES30.GL_RGBA8;
        desc.color.format = GL11.GL_RGBA;
        desc.color.type = GL11.GL_UNSIGNED_BYTE;
        desc.color.filter = GL11.GL_NEAREST;

        desc.depth.internalFormat = GLES30.GL_DEPTH_COMPONENT16;
        desc.depth.format = GLES20.GL_DEPTH_COMPONENT;
        desc.depth.type = GLES20.GL_UNSIGNED_SHORT;
        desc.depth.filter = GL11.GL_NEAREST;

        if(m_particleFbo != null)
            m_particleFbo.dispose();
        m_particleFbo = new NvSimpleFBO(desc);
    }

    void createSceneBuffer()
    {
        NvSimpleFBO.Desc desc = new NvSimpleFBO.Desc();
        desc.width = m_sceneResX;
        desc.height = m_sceneResY;

        desc.color.internalFormat = GLES30.GL_RGBA8;
        desc.color.format = GL11.GL_RGBA;
        desc.color.type = GL11.GL_UNSIGNED_BYTE;
        desc.color.filter = GL11.GL_NEAREST;

        desc.depth.internalFormat = GLES30.GL_DEPTH_COMPONENT16;
        desc.depth.format = GLES20.GL_DEPTH_COMPONENT;
        desc.depth.type = GL11.GL_UNSIGNED_SHORT;
        desc.depth.filter = GL11.GL_NEAREST;

        if(m_sceneFbo != null)
            m_sceneFbo.dispose();
        m_sceneFbo = new NvSimpleFBO(desc);
    }

    @Override
    public void dispose() {
        if(m_lightFbo != null)
            m_lightFbo.dispose();

        if(m_particleFbo != null)
            m_particleFbo.dispose();

        if(m_sceneFbo != null)
            m_sceneFbo.dispose();
    }

    static final class Params{
        int particleDownsample = 4;
        int sceneDownsample    = 1;
        int lightBufferSize    = 64;
    }
}
