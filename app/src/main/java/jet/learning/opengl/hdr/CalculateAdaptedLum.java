package jet.learning.opengl.hdr;

import android.opengl.GLES20;

import com.nvidia.developer.opengl.utils.NvGLSLProgram;

import jet.learning.opengl.common.SimpleOpenGLProgram;

/**
 * Created by mazhen'gui on 2017/3/16.
 */

final class CalculateAdaptedLum extends SimpleOpenGLProgram {

    private int uElapsedTime;
    public void init(){
        NvGLSLProgram program = NvGLSLProgram.createFromFiles("shaders/Quad_VS.vert", "hdr_shaders/CalculateAdaptedLum.frag");

        program.enable();
        program.setUniform1i("currentImage", 0);
        program.setUniform1i("image0", 1);
        program.disable();

        uElapsedTime = program.getUniformLocation("elapsedTime");
        programID = program.getProgram();
    }

    public void applyElapsedTime(float time){ GLES20.glUniform1f(uElapsedTime, time);}
}
