package jet.learning.opengl.gui_vr;

import android.opengl.GLES20;

import com.nvidia.developer.opengl.utils.BufferUtils;
import com.nvidia.developer.opengl.utils.NvGLSLProgram;

import org.lwjgl.util.vector.Matrix4f;

import java.nio.FloatBuffer;

import jet.learning.opengl.common.SimpleOpenGLProgram;

/**
 * Created by mazhen'gui on 2017/4/19.
 */

final class SpatialNormalProgram extends SimpleOpenGLProgram{

    private int mvpIndex;
    private int colorIndex;

    private final FloatBuffer mat_buffer = BufferUtils.createFloatBuffer(16);

    SpatialNormalProgram(){
        NvGLSLProgram program = NvGLSLProgram.createFromFiles("gui_vr/spatial_vs.vert", "gui_vr/spatial_ps.frag");
        programID = program.getProgram();

        mvpIndex = GLES20.glGetUniformLocation(programID, "g_MVP");
        colorIndex = GLES20.glGetUniformLocation(programID, "g_Color");

        if(mvpIndex < 0)
            throw  new IllegalArgumentException("mvpIndex = " + mvpIndex);
        if(colorIndex < 0)
            throw  new IllegalArgumentException("colorIndex = " + colorIndex);

        program.enable();
        program.setUniform1i("g_SourceTex", 0);
        program.setUniform4f(colorIndex, 1,1,1,1);
        program.disable();
    }

    void setMVP(Matrix4f mat){
        mat.store(mat_buffer);
        mat_buffer.flip();
        GLES20.glUniformMatrix4fv(mvpIndex, 1, false, mat_buffer);
    }

    void setColor(float r, float g, float b, float a){
        GLES20.glUniform4f(colorIndex,r,g,b,a);
    }
}
