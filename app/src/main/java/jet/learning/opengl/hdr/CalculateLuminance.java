package jet.learning.opengl.hdr;

import com.nvidia.developer.opengl.utils.NvGLSLProgram;

import jet.learning.opengl.common.SimpleOpenGLProgram;

/**
 * Created by mazhen'gui on 2017/3/16.
 */

public class CalculateLuminance extends SimpleOpenGLProgram {

    public void init(){
        NvGLSLProgram program = NvGLSLProgram.createFromFiles("shaders/Quad_VS.vert", "hdr_shaders/CalculateLuminance.frag");

        program.enable();
        program.setUniform1i("inputImage", 0);
        program.disable();

        programID = program.getProgram();
    }
}
