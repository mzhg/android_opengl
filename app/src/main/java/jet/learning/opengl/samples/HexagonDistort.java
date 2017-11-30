package jet.learning.opengl.samples;

import android.app.Activity;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.os.Build;
import android.util.Log;
import android.view.View;

import com.nvidia.developer.opengl.app.NvSampleApp;
import com.nvidia.developer.opengl.ui.NvTweakBar;
import com.nvidia.developer.opengl.ui.NvUIButton;
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

public final class HexagonDistort extends NvSampleApp {
    private int m_sourceTexture;
    private NvGLSLProgram m_splitProgram;
    private NvGLSLProgram m_distortProgram;
    private NvGLSLProgram m_offsetProgram;
    private float m_aspectRatio;
    private int m_DummyVAO;
    private boolean toonEnable = true;
    private float m_LastPointerX = 0.5f;

    private int texWidth, texHeight;
    private float mHexagonRadius = 0.35f;

    private final Vector4f mKfactor = new Vector4f();
    private float mFocus = 1.0f;
    private float mOffset = 0.1f;
    private float mCenterScale = 1/0.8f;
    private float mEyeOffset = 0.05f;
    private boolean mShowLine = true;

    private int m_framebuffer;
    private int m_texture;
    private int m_texture1;

    private NvTweakBar mLeftBar;
    private NvUIButton mLeftButton;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        navigationBarStatusBar(this, hasFocus);
    }

    public static void navigationBarStatusBar(Activity activity, boolean hasFocus){
        if (hasFocus && Build.VERSION.SDK_INT >= 19) {
            View decorView = activity.getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    public void initUI() {
        mTweakBar.addValue("HexagonRadius", createControl("mHexagonRadius"), 0.02f, 0.52f, 0.02f, 0);
        mTweakBar.addValue("Offset", createControl("mOffset"), 0.0f, 0.5f, 0.02f, 0);
        mTweakBar.addValue("CenterScale", createControl("mCenterScale"), 0.5f, 3.0f, (3 - 0.5f)/50);

        NvUIRect rect = mTweakBar.getScreenRect();
        mTweakTab.setOrigin(getWidth()/2, rect.top + 10);
        mTweakBar.setOrigin(getWidth()/2, mTweakBar.getScreenRect().top);
        mTweakBar.addValue("K1", createControl(mKfactor, "x"), -.7f, +.7f, 1.4f/100);
        mTweakBar.addValue("K2", createControl(mKfactor, "y"), -.7f, +.7f, 1.4f/100);
        mTweakBar.addValue("K3", createControl(mKfactor, "z"), -.7f, +.7f, 1.4f/100);

        mTweakBar.addValue("Focus", createControl( "mFocus"), 0.5f, 2.0f);

        if (mLeftBar==null) {
            mLeftBar = NvTweakBar.createTweakBar(mUIWindow); // adds to window internally.
            mLeftBar.setVisibility(false);

            // for now, app will own the tweakbar tab button
            float high = mLeftBar.getDefaultLineHeight();
            mUIWindow.add(mLeftButton, high*0.25f, mLeftBar.getStartOffY()+high*0.2f);

            mLeftBar.addPadding(10);
            mLeftBar.addValue("EyeOffset", createControl( "mEyeOffset"), 0.0f, +.5f, 0.5f/100, 11111);
            mLeftBar.addValue("ShowLine", createControl( "mShowLine"), false, 11112);
        }
    }

    @Override
    protected void initRendering() {
        NvLogger.setLevel(NvLogger.INFO);
        m_splitProgram = NvGLSLProgram.createFromFiles("shaders/Quad_VS.vert", "shaders/std_hexagon_blur.frag");
        m_distortProgram = NvGLSLProgram.createFromFiles("shaders/Quad_VS.vert", "shaders/std_distort.frag");
        m_offsetProgram = NvGLSLProgram.createFromFiles("shaders/Quad_VS.vert", "shaders/std_offset.frag");

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

        m_texture = GLES.glGenTextures();
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, m_texture);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, texWidth, texHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP_TO_EDGE);
        GLES20.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        m_texture1 = GLES.glGenTextures();
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, m_texture1);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, texWidth, texHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP_TO_EDGE);
        GLES20.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        m_framebuffer = GLES.glGenFramebuffers();
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, m_framebuffer);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, m_texture, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);


        setTitle("HexagonDistort");
        Log.i("HexagonDistort", "initRendering done!");
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
        split();
        disrot();

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GL11.GL_TEXTURE_2D, m_texture1);

        // offset
        m_offsetProgram.enable();
        m_offsetProgram.setUniform1i("iChannel0", 0);
        m_offsetProgram.setUniform2f("iResolution", getWidth()/2, getHeight());
        m_offsetProgram.setUniform1f("focuse", mFocus);
        m_offsetProgram.setUniform4f("Kfactor", mKfactor.x, mKfactor.y, mKfactor.z, mKfactor.w);

        int screenWidth = getWidth()/ 2;
        GLES20.glViewport(0,0, screenWidth, getHeight());
        m_offsetProgram.setUniform4f("Viewport", 0,0, screenWidth, getHeight());
        m_offsetProgram.setUniform1f("EyeOffset", /*+mEyeOffset*/0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);

        GLES20.glViewport(screenWidth,0, screenWidth, getHeight());
        m_offsetProgram.setUniform4f("Viewport", screenWidth,0, screenWidth, getHeight());
        m_offsetProgram.setUniform1f("EyeOffset", /*-mEyeOffset*/0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
        m_offsetProgram.disable();
        GLES30.glBindVertexArray(0);
        GLES20.glViewport(0,0, getWidth(), getHeight());

        GLES.checkGLError();
    }

    private void split(){
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, m_framebuffer);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, m_texture, 0);
        GLES20.glViewport(0,0,texWidth, texHeight);

        m_splitProgram.enable();
        m_splitProgram.setUniform1i("iChannel0", 0);
        m_splitProgram.setUniform2f("iResolution", getWidth()/2, getHeight());
        m_splitProgram.setUniform1f("focuse", mFocus);
        m_splitProgram.setUniform4f("Kfactor", mKfactor.x, mKfactor.y, mKfactor.z, mKfactor.w);
        m_splitProgram.setUniform1f("blurThreshold", mEyeOffset);
        m_splitProgram.setUniform1i("showLine", mShowLine ? 1: 0);
        m_splitProgram.setUniform2f("TexelSize", 1.0f/texWidth, 1.0f/texHeight);

        float innerRadius = (float) (mHexagonRadius * Math.cos(Math.PI/3));
        m_splitProgram.setUniform4f("HexagonData", mHexagonRadius, innerRadius, mOffset, mCenterScale);
        m_splitProgram.setUniform4f("Viewport", 0,0, texWidth, texHeight);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GL11.GL_TEXTURE_2D, m_sourceTexture);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
        m_splitProgram.disable();
    }

    private void disrot(){
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, m_framebuffer);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, m_texture1, 0);
        GLES20.glViewport(0,0,texWidth, texHeight);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GL11.GL_TEXTURE_2D, m_texture);

        m_distortProgram.enable();
        m_distortProgram.setUniform1i("iChannel0", 0);
        m_distortProgram.setUniform2f("iResolution", getWidth()/2, getHeight());
        m_distortProgram.setUniform1f("focuse", mFocus);
        m_distortProgram.setUniform4f("Kfactor", mKfactor.x, mKfactor.y, mKfactor.z, mKfactor.w);
        m_distortProgram.setUniform4f("Viewport", 0,0,texWidth, texHeight);
        m_distortProgram.setUniform1f("EyeOffset",0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
        m_distortProgram.disable();
    }
}
