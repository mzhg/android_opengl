package jet.learning.opengl.hdr;

import android.opengl.GLES30;

import com.nvidia.developer.opengl.utils.NvAssetLoader;
import com.nvidia.developer.opengl.utils.NvGLSLProgram;

/**
 * Created by mazhen'gui on 2017/3/16.
 */

final class ReflectProgram extends  BaseProgram{
    private int u_emission;
    private int u_color;

    private int attribPos;
    private int attribNormal;

    public ReflectProgram() {
        CharSequence vert = NvAssetLoader.readText("hdr_shaders/matteObject.vert");
        CharSequence frag = NvAssetLoader.readText("hdr_shaders/reflectObject.frag");

        NvGLSLProgram program = new NvGLSLProgram();
        program.setSourceFromStrings(vert, frag, true);
        programID = program.getProgram();
        initVS();

        u_emission = program.getUniformLocation("emission");
        u_color    = program.getUniformLocation("color");

        int envMap      = program.getUniformLocation("envMap");
        int envMapRough = program.getUniformLocation("envMapRough");

        attribPos = program.getAttribLocation("PosAttribute");
        attribNormal = program.getAttribLocation("myNormal");

        GLES30.glUseProgram(programID);
        GLES30.glUniform1i(envMap, 0);
        GLES30.glUniform1i(envMapRough, 1);
        GLES30.glUseProgram(0);
    }

    public void applyEmission(float r, float g, float b){	GLES30.glUniform3f(u_emission, r, g, b);}
    public void applyColor(float r, float g, float b, float a){	GLES30.glUniform4f(u_color, r, g, b, a);}

    @Override
    public int getAttribPosition() { return attribPos; }
    @Override
    public int getAttribNormal() {return attribNormal;}
}
