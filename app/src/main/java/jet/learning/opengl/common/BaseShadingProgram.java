package jet.learning.opengl.common;

import android.opengl.GLES20;

import com.nvidia.developer.opengl.utils.NvGLSLProgram;

/**
 * Created by mazhen'gui on 2017/11/13.
 */

public class BaseShadingProgram extends SimpleLightProgram {
    private int m_alphaClip;
    private int m_reflectionEnabled;
    public BaseShadingProgram( NvGLSLProgram.LinkerTask task) {
        super(true, task);

        m_alphaClip = GLES20.glGetUniformLocation(getProgram(), "g_AlphaClip");
        m_reflectionEnabled = GLES20.glGetUniformLocation(getProgram(), "g_ReflectionEnabled");

        int reflectTex = GLES20.glGetUniformLocation(getProgram(), "g_ReflectTex");
        if (reflectTex >= 0)
            GLES20.glUniform1i(reflectTex, 1);
    }

    public void setAlphaClip(boolean flag){
        if(m_alphaClip >=0){
            GLES20.glUniform1i(m_alphaClip, flag?1:0);
        }
    }

    public void setReflection(boolean flag){
        if(m_reflectionEnabled >=0){
            GLES20.glUniform1i(m_reflectionEnabled, flag?1:0);
        }
    }

    @Override
    protected String getFragmentShaderFile(boolean uniform) {
        return "d3dcoder/BaseShading.frag";
    }

    @Override
    protected String getVertexShaderFile(boolean uniform) {
        if (uniform)
            return "shaders/SimpleLightUniformColorInstanceVS.vert";
        else
            throw new UnsupportedOperationException();
    }
}
