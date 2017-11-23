package jet.learning.opengl.samples;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.util.Log;

import com.nvidia.developer.opengl.app.NvSampleApp;
import com.nvidia.developer.opengl.ui.NvUIRect;
import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.Glut;
import com.nvidia.developer.opengl.utils.NvGLSLProgram;
import com.nvidia.developer.opengl.utils.NvLogger;

import org.lwjgl.util.vector.Vector4f;

import javax.microedition.khronos.opengles.GL11;

/**
 * Created by mazhen'gui on 2016/12/21.
 */

public final class Fisheye extends NvSampleApp {
    private int m_sourceTexture;
    private NvGLSLProgram m_Program;
    private float m_aspectRatio;
    private int m_DummyVAO;
    private boolean toonEnable = true;
    private float m_LastPointerX = 0.5f;

    private int texWidth, texHeight;
    private float m_Factor = 0.7f;

    private final Vector4f mKfactor = new Vector4f();
    private float mFocus = 1.0f;

    @Override
    public void initUI() {
//        mTweakBar.addValue("Factor", new FieldControl(this, "m_Factor", FieldControl.CALL_FIELD), 0.0f, 1.0f, 0.02f, 0);

        NvUIRect rect = mTweakBar.getScreenRect();
        mTweakTab.setOrigin(getWidth()/2, rect.top + 10);
        mTweakBar.setOrigin(getWidth()/2, mTweakBar.getScreenRect().top);
        mTweakBar.addValue("K1", createControl(mKfactor, "x"), -.7f, +.7f, 1.4f/100);
        mTweakBar.addValue("K2", createControl(mKfactor, "y"), -.7f, +.7f, 1.4f/100);
        mTweakBar.addValue("K3", createControl(mKfactor, "z"), -.7f, +.7f, 1.4f/100);
        mTweakBar.addValue("K4", createControl(mKfactor, "w"), -.7f, +.7f, 1.4f/100);
        mTweakBar.addPadding();
        mTweakBar.addValue("Focus", createControl( "mFocus"), 0.5f, 2.0f);
    }

    @Override
    protected void initRendering() {
        NvLogger.setLevel(NvLogger.INFO);
        m_Program = NvGLSLProgram.createFromFiles("shaders/Quad_VS.vert", "shaders/std_distort.frag");
        //load input texture
        /*NvImage m_sourceImage = NvImage.createFromDDSFile("textures/flower1024.dds");
        texWidth = m_sourceImage.getWidth();
        texHeight = m_sourceImage.getHeight();
        m_sourceTexture = m_sourceImage.updaloadTexture();*/

        Bitmap image = Glut.loadBitmapFromAssets("textures/grid_image.png");
        texWidth = image.getWidth();
        texHeight = image.getHeight();
        m_sourceTexture = GLES.glGenTextures();
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, m_sourceTexture);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, image, 0);
        GLES.checkGLError();
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP_TO_EDGE);
        GLES20.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        m_DummyVAO = GLES.glGenVertexArray();

        setTitle("Fisheye");

        Log.i("Fisheye", "initRendering done!");
    }

    @Override
    protected void reshape(int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        m_aspectRatio = (float)height/(float)width;
        m_LastPointerX = width/2.0f;

        Log.i("Fisheye", "reshape done!");
    }

    @Override
    protected void draw() {
        GLES30.glBindVertexArray(m_DummyVAO);

//        uniform sampler2D iChannel0;
//        uniform vec2 iResolution;
//        uniform float factor;
//        uniform vec2 iMouse;
        m_Program.enable();
        m_Program.setUniform1i("iChannel0", 0);
        m_Program.setUniform2f("iResolution", getWidth()/2, getHeight());
        m_Program.setUniform1f("factor", m_Factor);
        m_Program.setUniform1f("focuse", mFocus);
        m_Program.setUniform4f("Kfactor", mKfactor.x, mKfactor.y, mKfactor.z, mKfactor.w);

//        m_Program.setUniform1i("toon_enable", toonEnable?1:0);
//        m_Program.setUniform1f("edge_thres", 0.2f);
//        m_Program.setUniform1f("edge_thres2", 5.0f);

        if(isTouchDown(0)){
            m_LastPointerX = getTouchX(0);
        }
//        m_Program.setUniform1f("mouse_x_offset", m_LastPointerX/ getWidth());
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GL11.GL_TEXTURE_2D, m_sourceTexture);
        int screenWidth = getWidth()/ 2;
        GLES20.glViewport(0,0, screenWidth, getHeight());
        m_Program.setUniform4f("Viewport", 0,0, screenWidth, getHeight());
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);

        GLES20.glViewport(screenWidth,0, screenWidth, getHeight());
        m_Program.setUniform4f("Viewport", screenWidth,0, screenWidth, getHeight());
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
        m_Program.disable();
        GLES30.glBindVertexArray(0);
        GLES20.glViewport(0,0, getWidth(), getHeight());
    }
}
