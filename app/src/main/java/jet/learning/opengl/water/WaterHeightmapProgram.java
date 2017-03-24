package jet.learning.opengl.water;

import com.nvidia.developer.opengl.utils.NvGLSLProgram;
import jet.learning.opengl.common.SimpleOpenGLProgram;

/**
 * Created by mazhen'gui on 2017/3/21.
 */

final class WaterHeightmapProgram extends SimpleOpenGLProgram {
    public void init(){
        NvGLSLProgram program = NvGLSLProgram.createFromFiles("hdr_shaders/quad_es3.vert", "water_resources/waterheightmap.frag");

        program.enable();
        program.setUniform1i("WaterHeightMap", 0);
        program.setUniform1f("ODWHMR", 1.0f / (float)COpenGLRenderer.WHMR);
        program.disable();

        programID = program.getProgram();
        findAttrib();
    }
}
