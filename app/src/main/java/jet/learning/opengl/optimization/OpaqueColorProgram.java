package jet.learning.opengl.optimization;

import android.opengl.GLES20;

import com.nvidia.developer.opengl.utils.NvGLSLProgram;

import org.lwjgl.util.vector.Vector3f;

/**
 * Created by mazhen'gui on 2018/2/6.
 */

final class OpaqueColorProgram extends NvGLSLProgram{
    int m_positionAttrib = -1;
    int m_normalAttrib   = -1;

    OpaqueColorProgram(boolean isES2)
    {
        setSourceFromFiles("optimization/base.vert", "optimization/base.frag");

        m_positionAttrib     = getAttribLocation("g_Position");
        m_normalAttrib       = getAttribLocation("g_Normal");
    }

    void setUniforms(SceneInfo scene)
    {
        GLES20.glUseProgram(m_program);

        Vector3f v = scene.m_lightVector;
        setUniform3f("g_lightDirection", v.x, v.y, v.z);
    }

    int getPositionAttrib() { return m_positionAttrib;  }

    int getNormalAttrib() { return m_normalAttrib;  }
}
