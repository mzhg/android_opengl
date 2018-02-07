package jet.learning.opengl.optimization;

import android.opengl.GLES20;

import com.nvidia.developer.opengl.utils.NvGLSLProgram;

import javax.microedition.khronos.opengles.GL11;

/**
 * Created by mazhen'gui on 2018/2/6.
 */

final class UpsampleCrossBilateralProgram extends NvGLSLProgram {
    private int m_positionAttrib = -1;
    private int m_texCoordAttrib = -1;

    private int m_fullResDepthTex = -1;
    private int m_lowResDepthTex = -1;
    private int m_lowResParticleColorTex = -1;

    private int m_depthConstants = -1;
    private int m_lowResTextureSize = -1;
    private int m_lowResTexelSize = -1;
    private int m_depthMult = -1;
    private int m_threshold = -1;

    UpsampleCrossBilateralProgram()
    {
        setSourceFromFiles("optimization/upsampleCrossBilateral.vert", "optimization/upsampleCrossBilateral.frag");
        m_positionAttrib             = getAttribLocation("g_position");
        m_texCoordAttrib             = getAttribLocation("g_texCoords");

        m_fullResDepthTex            = getUniformLocation("g_fullResDepthTex");
        m_lowResDepthTex             = getUniformLocation("g_lowResDepthTex");
        m_lowResParticleColorTex     = getUniformLocation("g_lowResParticleColorTex");

        m_depthConstants             = getUniformLocation("g_depthConstants");
        m_lowResTextureSize          = getUniformLocation("g_lowResTextureSize");
        m_lowResTexelSize            = getUniformLocation("g_lowResTexelSize");
        m_depthMult                  = getUniformLocation("g_depthMult");
        m_threshold                  = getUniformLocation("g_threshold");
    }

    void setUniforms(SceneFBOs fbos, Upsampler.Params params)
    {
        GLES20.glUseProgram(m_program);

        GLES20.glBindTexture(GL11.GL_TEXTURE_2D, fbos.m_particleFbo.colorTexture);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fbos.m_sceneFbo.depthTexture);
        GLES20.glUniform1i(m_fullResDepthTex, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GL11.GL_TEXTURE_2D, fbos.m_particleFbo.depthTexture);
        GLES20.glUniform1i(m_lowResDepthTex, 1);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GL11.GL_TEXTURE_2D, fbos.m_particleFbo.colorTexture);
        GLES20.glUniform1i(m_lowResParticleColorTex, 2);

//	        vec2f lowResolution((float)fbos.m_particleFbo.width, (float)fbos.m_particleFbo.height);
//	        vec2f lowResTexelSize(1.f/lowResolution.x, 1.f/lowResolution.y);
        float lowResolutionX = fbos.m_particleFbo.width;
        float lowResolutionY = fbos.m_particleFbo.height;
        float lowResTexelSizeX = 1.0f/lowResolutionX;
        float lowResTexelSizeY = 1.0f/lowResolutionY;

        GLES20.glUniform2f(m_depthConstants, -1.f/OptimizationApp.EYE_ZFAR + 1.f/OptimizationApp.EYE_ZNEAR, -1.f/OptimizationApp.EYE_ZNEAR);
        GLES20.glUniform2f(m_lowResTextureSize, lowResolutionX, lowResolutionY);
        GLES20.glUniform2f(m_lowResTexelSize, lowResTexelSizeX, lowResTexelSizeY);
        GLES20.glUniform1f(m_depthMult, params.upsamplingDepthMult);
        GLES20.glUniform1f(m_threshold, params.upsamplingThreshold);

        // unbind the textures.
        GLES20.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    int getPositionAttrib() {  return m_positionAttrib; }

    int getTexCoordAttrib() { return m_texCoordAttrib; }
}
