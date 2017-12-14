package jet.learning.opengl.common;

import com.nvidia.developer.opengl.utils.NvGLSLProgram;

/**
 * Created by mazhen'gui on 2017/12/14.
 */

public class GenerateShadowMapProgram extends SimpleOpenGLProgram {

    public GenerateShadowMapProgram(NvGLSLProgram.LinkerTask task, boolean instanced){
        NvGLSLProgram program = new NvGLSLProgram();
        program.setLinkeTask(task);
        if(instanced){
            program.setSourceFromFiles("shaders/SimpleLightUniformColorInstanceVS.vert", "shaders/dummyPS3.frag");
        }else{
            program.setSourceFromFiles("shaders/SimpleLightUniformColorVS.vert", "shaders/dummyPS.frag");
        }

        programID = program.getProgram();
        findAttrib();
    }
}
