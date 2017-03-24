package jet.learning.opengl.hdr;

import android.opengl.GLES20;

import com.nvidia.developer.opengl.utils.GLUtil;
import com.nvidia.developer.opengl.utils.NvAssetLoader;
import com.nvidia.developer.opengl.utils.NvGLSLProgram;

import org.lwjgl.util.vector.Matrix4f;

import jet.learning.opengl.common.SimpleOpenGLProgram;

/**
 * Created by mazhen'gui on 2017/3/16.
 */

final class SkyProgram extends SimpleOpenGLProgram{
    private int viewMatIndex;
    private int projMatIndex;
    private int attribPos;

    public SkyProgram() {
        CharSequence vert = NvAssetLoader.readText("hdr_shaders/skyBox.vert");
        CharSequence frag = NvAssetLoader.readText("hdr_shaders/skyBox.frag");

        NvGLSLProgram program = new NvGLSLProgram();
        program.setSourceFromStrings(vert, frag, true);
        programID = program.getProgram();

        int envMap      = program.getUniformLocation("envMap");
        viewMatIndex    = program.getUniformLocation("viewMatrix");
        projMatIndex    = program.getUniformLocation("ProjMatrix");
        attribPos = program.getAttribLocation("PosAttribute");

        GLES20.glUseProgram(programID);
        GLES20.glUniform1i(envMap, 0);
        GLES20.glUseProgram(0);
    }

    public void applyViewMat(Matrix4f mat){GLES20.glUniformMatrix4fv(viewMatIndex, 1, false, GLUtil.wrap(mat)); }
    public void applyProjMat(Matrix4f mat){GLES20.glUniformMatrix4fv(projMatIndex, 1, false, GLUtil.wrap(mat)); }

    @Override
    public int getAttribPosition() {
        return attribPos;
    }
}
