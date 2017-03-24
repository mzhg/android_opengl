package jet.learning.opengl.hdr;

import android.opengl.GLES20;

import com.nvidia.developer.opengl.utils.NvGLSLProgram;

import jet.learning.opengl.common.SimpleOpenGLProgram;

/**
 * Created by mazhen'gui on 2017/3/16.
 */

final class GlareComposeProgram extends SimpleOpenGLProgram{
    private int attribPos;
    private int attribTexcoord;

    private int u_mixCoeff;

    public void init(){
        NvGLSLProgram program = NvGLSLProgram.createFromFiles("hdr_shaders/quad.vert", "hdr_shaders/glareCompose.frag");

        program.enable();
        program.setUniform1i("sampler1", 0);
        program.setUniform1i("sampler2", 1);
        program.setUniform1i("sampler3", 2);
        program.setUniform1i("sampler4", 3);
        program.disable();

        attribPos = program.getAttribLocation("PosAttribute");
        attribTexcoord = program.getAttribLocation("TexAttribute");

        u_mixCoeff   = program.getUniformLocation("mixCoeff");
        programID = program.getProgram();
    }

    public final int getAttribPosition() { return attribPos;}
    public final int getAttribTexCoord() { return attribTexcoord;}
    public final int getSampler1Unit()   { return 0+GLES20.GL_TEXTURE0;}
    public final int getSampler2Unit()   { return 1+GLES20.GL_TEXTURE0;}
    public final int getSampler3Unit()   { return 2+GLES20.GL_TEXTURE0;}
    public final int getSampler4Unit()   { return 3+GLES20.GL_TEXTURE0;}

    public void applyMixCoeff(float x, float y, float z, float w){
        GLES20.glUniform4f(u_mixCoeff, x, y, z, w);
    }
}
