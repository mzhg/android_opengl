package jet.learning.opengl.upsampling;

import android.opengl.GLES20;

import com.nvidia.developer.opengl.utils.GLUtil;
import com.nvidia.developer.opengl.utils.NvGLSLProgram;

import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL11;

/**
 * Created by mazhen'gui on 2018/2/10.
 */

final class CameraViewParticleProgram extends NvGLSLProgram{
    int m_positionAttrib;
    int m_shadowTex;
    int m_depthTex;
    int m_pointScale;
    int m_alpha;
    int m_invViewport;
    int m_depthConstants;
    int m_depthDeltaScale;
    int m_modelViewMatrix;
    int m_projectionMatrix;
    int m_shadowMatrix;

    CameraViewParticleProgram()
    {
        setSourceFromFiles("shaders/cameraViewParticle.vert", "shaders/cameraViewParticle.frag");
        m_positionAttrib 	= getAttribLocation("g_position");
        m_depthTex			= getUniformLocation("g_depthTex");
        m_pointScale 		= getUniformLocation("g_pointScale");
        m_alpha 			= getUniformLocation("g_alpha");
        m_modelViewMatrix 	= getUniformLocation("g_modelViewMatrix");
        m_projectionMatrix 	= getUniformLocation("g_projectionMatrix");

        // shadowing constants
        m_shadowTex			= getUniformLocation("g_shadowTex");
        m_shadowMatrix 		= getUniformLocation("g_shadowMatrix");

        // soft-particle constants
        m_invViewport		= getUniformLocation("g_invViewport");
        m_depthConstants	= getUniformLocation("g_depthConstants");
        m_depthDeltaScale	= getUniformLocation("g_depthDeltaScale");
    }

    void setUniforms(SceneInfo scene, ParticleRenderer.Params params)
    {
        GLES20.glUseProgram(m_program);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GL11.GL_TEXTURE_2D, scene.m_fbos.m_lightFbo.colorTexture);
        GLES20.glUniform1i(m_shadowTex, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GL11.GL_TEXTURE_2D, scene.m_fbos.m_particleFbo.depthTexture);
        GLES20.glUniform1i(m_depthTex, 1);

        IntBuffer viewport = GLUtil.getCachedIntBuffer(16);
        GLES20.glGetIntegerv(GL11.GL_VIEWPORT, viewport);

        GLES20.glUniform2f(m_invViewport, 1.f / viewport.get(2), 1.f / viewport.get(3));
        GLES20.glUniform2f(m_depthConstants, -1.0f/ParticleUpsampling.EYE_ZFAR + 1.0f/ParticleUpsampling.EYE_ZNEAR, -1.0f/ParticleUpsampling.EYE_ZNEAR);
        GLES20.glUniform1f(m_depthDeltaScale, 1.f / params.softness);

        GLES20.glUniform1f(m_alpha, params.spriteAlpha * params.particleScale);
        GLES20.glUniform1f(m_pointScale, params.getPointScale(scene.m_eyeProj));
        GLES20.glUniformMatrix4fv(m_modelViewMatrix, 1, false, GLUtil.wrap(scene.m_eyeView));
        GLES20.glUniformMatrix4fv(m_projectionMatrix, 1, false, GLUtil.wrap(scene.m_eyeProj));
        GLES20.glUniformMatrix4fv(m_shadowMatrix, 1, false, GLUtil.wrap(scene.m_shadowMatrix));
    }

    int getPositionAttrib()
    {
        return m_positionAttrib;
    }
}
