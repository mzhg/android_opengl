package jet.learning.opengl.optimization;

import android.opengl.GLES20;

import com.nvidia.developer.opengl.utils.NvGLSLProgram;

/**
 * Created by mazhen'gui on 2018/2/6.
 */

final class OpaqueDepthProgram extends NvGLSLProgram {
    int m_positionAttrib   = -1;
    int m_modelViewMatrix  = -1;
    int m_projectionMatrix = -1;

    OpaqueDepthProgram(String fraggy)
    {
        setSourceFromFiles("optimization/unshaded.vert", fraggy);
        m_positionAttrib     = getAttribLocation("g_Position");
    }

    void setUniforms(SceneInfo scene)
    {
        GLES20.glUseProgram(m_program);
    }

    int getPositionAttrib() {  return m_positionAttrib; }
}
