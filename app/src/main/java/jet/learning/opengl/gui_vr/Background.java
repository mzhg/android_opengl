package jet.learning.opengl.gui_vr;

import android.graphics.Bitmap;
import android.opengl.GLES10;
import android.opengl.GLES11;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.Glut;
import com.nvidia.developer.opengl.utils.NvGLModel;

/**
 * Created by mazhen'gui on 2017/4/19.
 */

final class Background {

    private NvGLModel m_model;
    private int m_textureID;
    private UIRenderer m_Renderer;

    Background(UIRenderer renderer){
        m_Renderer = renderer;

        m_model = new NvGLModel();
        m_model.loadModelFromFile("gui_vr/cinema.obj");
        m_model.initBuffers(false);

        m_textureID = GLES.glGenTextures();
        GLES11.glBindTexture(GLES10.GL_TEXTURE_2D, m_textureID);
        Bitmap bitmap = Glut.loadBitmapFromAssets("gui_vr/cinema_light_on.png");
        GLUtils.texImage2D(GLES11.GL_TEXTURE_2D, 0, bitmap, 0);
        GLES11.glTexParameteri(GLES11.GL_TEXTURE_2D, GLES11.GL_TEXTURE_MAG_FILTER, GLES11.GL_LINEAR);
        GLES11.glTexParameteri(GLES11.GL_TEXTURE_2D, GLES11.GL_TEXTURE_MIN_FILTER, GLES11.GL_LINEAR);
        GLES11.glTexParameteri(GLES11.GL_TEXTURE_2D, GLES11.GL_TEXTURE_WRAP_S, GLES11.GL_CLAMP_TO_EDGE);
        GLES11.glTexParameteri(GLES11.GL_TEXTURE_2D, GLES11.GL_TEXTURE_WRAP_T, GLES11.GL_CLAMP_TO_EDGE);
    }

    void draw(){
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES11.glBindTexture(GLES10.GL_TEXTURE_2D, m_textureID);
        SpatialNormalProgram program = m_Renderer.getSpatialNormalProgram();
        program.enable();

//        Matrix4f model = getModelMatrix();
//        Matrix4f.mul(m_Renderer.getMVP(), model, model);
        program.setMVP(m_Renderer.getMVP());
        program.setColor(1,1,1,1);
        m_model.drawElements(program.getAttribPosition(), -1, program.getAttribTexCoord());
    }
}
