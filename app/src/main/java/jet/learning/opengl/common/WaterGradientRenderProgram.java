package jet.learning.opengl.common;

import android.opengl.GLES20;

/**
 * Created by mazhen'gui on 2017/12/18.
 */

public class WaterGradientRenderProgram extends SimpleLightProgram {

    public WaterGradientRenderProgram() {
        super(true, null);
        int gradientTex = GLES20.glGetUniformLocation(getProgram(), "g_WaterGradientMap");
        assert (gradientTex >= 0);
        if(gradientTex >= 0)
            GLES20.glUniform1i(gradientTex, 1);

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
            return "d3dcoder/WaterGradientVS.vert";
        else
            throw new UnsupportedOperationException();
    }
}
