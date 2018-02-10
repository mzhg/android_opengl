package jet.learning.opengl.upsampling;

import android.opengl.GLES20;

import com.nvidia.developer.opengl.utils.NvGLSLProgram;

/**
 * Created by mazhen'gui on 2018/2/10.
 */

final class OpaqueDepthProgram extends NvGLSLProgram{
    private int m_positionAttrib;
    private int m_modelViewMatrix;
    private int m_projectionMatrix;

    OpaqueDepthProgram()
    {
        setSourceFromFiles("shaders/opaqueDepth.vert", "shaders/opaqueDepth.frag");
        m_positionAttrib 	= getAttribLocation("g_position");
        m_modelViewMatrix 	= getUniformLocation("g_modelViewMatrix");
        m_projectionMatrix	= getUniformLocation("g_projectionMatrix");
    }

    void setUniforms(SceneInfo scene)
    {
        GLES20.glUseProgram(m_program);

//        GLES20.glUniformMatrix4fv(m_modelViewMatrix,1, false, wrap(scene.m_eyeView));
//        GLES20.glUniformMatrix4fv(m_projectionMatrix, false, wrap(scene.m_eyeProj));

        setUniformMatrix4(m_modelViewMatrix, scene.m_eyeView, false);
        setUniformMatrix4(m_projectionMatrix, scene.m_eyeProj, false);
    }

    int getPositionAttrib()
    {
        return m_positionAttrib;
    }
}
