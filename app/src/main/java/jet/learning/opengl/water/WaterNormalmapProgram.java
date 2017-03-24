package jet.learning.opengl.water;

import com.nvidia.developer.opengl.utils.NvGLSLProgram;

import jet.learning.opengl.common.SimpleOpenGLProgram;

/**
 * Created by mazhen'gui on 2017/3/21.
 */

final class WaterNormalmapProgram extends SimpleOpenGLProgram {
    public void init(){
        NvGLSLProgram program = NvGLSLProgram.createFromFiles("hdr_shaders/quad_es3.vert", "water_resources/waternormalmap.frag");

        program.enable();
        program.setUniform1i("WaterHeightMap", 0);
        program.setUniform1f("ODWNMR", 1.0f / (float)COpenGLRenderer.WHMR);
        program.setUniform1f("WMSDWNMRM2", 4.0f / (float)COpenGLRenderer.WHMR);
        program.disable();

        programID = program.getProgram();
        findAttrib();
    }
}
