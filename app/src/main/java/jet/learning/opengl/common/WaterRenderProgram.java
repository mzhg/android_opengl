package jet.learning.opengl.common;

import android.opengl.GLES20;

import com.nvidia.developer.opengl.utils.NvGLSLProgram;

/**
 * Created by mazhen'gui on 2017/12/15.
 */
public class WaterRenderProgram extends SimpleLightProgram {
    public WaterRenderProgram(NvGLSLProgram.LinkerTask task) {
        super(true, task);

        int normalTex = GLES20.glGetUniformLocation(getProgram(), "g_WaterNormalMap");
        assert (normalTex >=0);
        GLES20.glUniform1i(normalTex, 1);

        int heightMapTex = GLES20.glGetUniformLocation(getProgram(), "g_WaterHeightMap");
        assert (heightMapTex >= 0);
        GLES20.glUniform1i(heightMapTex, 2);
    }

    @Override
    protected String getFragmentShaderFile(boolean uniform) {
        return "d3dcoder/WaterPS.frag";
    }

    @Override
    protected String getVertexShaderFile(boolean uniform) {
        if (uniform)
            return "d3dcoder/WaterVS.vert";
        else
            throw new UnsupportedOperationException();
    }
}
