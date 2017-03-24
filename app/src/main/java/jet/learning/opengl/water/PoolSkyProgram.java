package jet.learning.opengl.water;

import android.opengl.GLES20;

import com.nvidia.developer.opengl.utils.NvGLSLProgram;

import org.lwjgl.util.vector.Matrix4f;

import jet.learning.opengl.common.SimpleOpenGLProgram;

/**
 * Created by mazhen'gui on 2017/3/21.
 */

public class PoolSkyProgram extends SimpleOpenGLProgram{
    private int attribPos;
    private int mvpIndex = -1;

    private final float[] mat_data = new float[16];

    public void init(){
        NvGLSLProgram program = NvGLSLProgram.createFromFiles("water_resources/poolsky.vert", "water_resources/poolsky.frag");

        program.enable();
        program.setUniform1i("PoolSkyCubeMap", 2);
        program.disable();

        attribPos = program.getAttribLocation("PosAttribute");
        mvpIndex = program.getUniformLocation("g_mvp");
        programID = program.getProgram();
    }

    public void setMVP(Matrix4f mvpMat){
        mvpMat.store(mat_data, 0);
        GLES20.glUniformMatrix4fv(mvpIndex, 1, false, mat_data, 0);
    }

    @Override
    public final int getAttribPosition() { return attribPos;}

    @Override
    public int getAttribTexCoord() {
        return -1;
    }
}
