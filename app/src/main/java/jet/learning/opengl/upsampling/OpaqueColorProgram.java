package jet.learning.opengl.upsampling;

import android.opengl.GLES20;

import com.nvidia.developer.opengl.utils.GLUtil;
import com.nvidia.developer.opengl.utils.NvGLSLProgram;

import org.lwjgl.util.vector.Matrix4f;

import javax.microedition.khronos.opengles.GL11;

/**
 * Created by mazhen'gui on 2018/2/10.
 */

final class OpaqueColorProgram extends NvGLSLProgram{
    private int m_positionAttrib;
    private int m_normalAttrib;
    private int m_shadowTex;
    private int m_lightPosEye;
    private int m_modelViewMatrix;
    private int m_projectionMatrix;
    private int m_normalMatrix;
    private int m_shadowMatrix;

    OpaqueColorProgram()
    {
        setSourceFromFiles("shaders/opaqueColor.vert", "shaders/opaqueColor.frag");
        m_positionAttrib 	= getAttribLocation("g_position");
        m_normalAttrib   	= getAttribLocation("g_normal");
        m_shadowTex 		= getUniformLocation("g_shadowTex");
        m_lightPosEye 		= getUniformLocation("g_lightPosEye");
        m_modelViewMatrix 	= getUniformLocation("g_modelViewMatrix");
        m_projectionMatrix 	= getUniformLocation("g_projectionMatrix");
        m_normalMatrix		= getUniformLocation("g_normalMatrix");
        m_shadowMatrix		= getUniformLocation("g_shadowMatrix");
    }

    void setUniforms(SceneInfo scene)
    {
        GLES20.glUseProgram(m_program);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GL11.GL_TEXTURE_2D, scene.m_fbos.m_lightFbo.colorTexture);
        GLES20.glUniform1i(m_shadowTex, 0);

        GLES20.glUniform3f(m_lightPosEye, scene.m_lightPosEye.x, scene.m_lightPosEye.y, scene.m_lightPosEye.z);
        GLES20.glUniformMatrix4fv(m_modelViewMatrix, 1, false, GLUtil.wrap(scene.m_eyeView));
        GLES20.glUniformMatrix4fv(m_projectionMatrix, 1, false, GLUtil.wrap(scene.m_eyeProj));
//	        transpose(inverse(scene.m_eyeView)).get_value()
        Matrix4f.invert(scene.m_eyeView, scene.m_eyeViewInv).transpose();
        GLES20.glUniformMatrix4fv(m_normalMatrix, 1, false, GLUtil.wrap(scene.m_eyeViewInv));
        GLES20.glUniformMatrix4fv(m_shadowMatrix, 1, false, GLUtil.wrap(scene.m_shadowMatrix));
    }

    int getPositionAttrib()
    {
        return m_positionAttrib;
    }

    int getNormalAttrib()
    {
        return m_normalAttrib;
    }
}
