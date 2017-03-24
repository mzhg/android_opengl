package jet.learning.opengl.hdr;

import com.nvidia.developer.opengl.utils.NvGLSLProgram;

import jet.learning.opengl.common.SimpleOpenGLProgram;

/**
 * Created by mazhen'gui on 2017/3/16.
 */

public class TextureBlitProgram extends SimpleOpenGLProgram {

    private int attribPos;
    private int attribTexcoord;

    public  void init(){
        NvGLSLProgram program = NvGLSLProgram.createFromFiles("hdr_shaders/quad.vert", "hdr_shaders/blit.frag");

        program.enable();
        program.setUniform1i("uSourceTex", 0);
        program.disable();

        attribPos = program.getAttribLocation("PosAttribute");
        attribTexcoord = program.getAttribLocation("TexAttribute");

        programID = program.getProgram();
    }

    @Override
    public final int getAttribPosition() { return attribPos;}
    @Override
    public final int getAttribTexCoord() { return attribTexcoord;}
}
