package jet.learning.opengl.common;

import android.opengl.GLES20;

/**
 * Created by mazhen'gui on 2017/12/15.
 */
public class WaterRenderProgram extends SimpleLightProgram {
    private boolean mUseGraident;
    public WaterRenderProgram(boolean useGradient) {
        super(true, null);

        mUseGraident = useGradient;

        int normalTex = GLES20.glGetUniformLocation(getProgram(), "g_WaterNormalMap");
        if(normalTex >= 0)
            GLES20.glUniform1i(normalTex, 1);


        int gradientTex = GLES20.glGetUniformLocation(getProgram(), "g_WaterGradientMap");
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
            return mUseGraident ? "d3dcoder/WaterGradientVS.vert" : "d3dcoder/WaterVS.vert";
        else
            throw new UnsupportedOperationException();
    }
}
