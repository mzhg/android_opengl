package jet.learning.opengl.common;

import android.opengl.GLES20;

import com.nvidia.developer.opengl.utils.NvGLSLProgram;

/**
 * Created by mazhen'gui on 2017/11/13.
 */

public class BaseShadingProgram extends SimpleLightProgram {
    private int m_alphaClip;
    public BaseShadingProgram( NvGLSLProgram.LinkerTask task) {
        super(true, task);

        m_alphaClip = GLES20.glGetUniformLocation(getProgram(), "g_AlphaClip");
    }

    public void setAlphaClip(boolean flag){
        if(m_alphaClip >=0){
            GLES20.glUniform1i(m_alphaClip, flag?1:0);
        }
    }

    @Override
    protected String getFragmentShaderFile(boolean uniform) {
        return "d3dcoder/BaseShading.frag";
    }
}
