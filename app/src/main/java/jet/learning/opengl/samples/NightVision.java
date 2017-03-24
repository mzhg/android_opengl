package jet.learning.opengl.samples;

import android.opengl.GLES20;
import android.opengl.GLES30;
import android.util.Log;

import com.nvidia.developer.opengl.app.NvSampleApp;
import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.NvGLSLProgram;
import com.nvidia.developer.opengl.utils.NvImage;
import com.nvidia.developer.opengl.utils.NvLogger;

import javax.microedition.khronos.opengles.GL11;

/**
 * Created by mazhen'gui on 2016/12/21.
 */

public class NightVision extends NvSampleApp {
    private int m_sourceTexture;
    private NvGLSLProgram m_Program;
    private float m_aspectRatio;
    private int m_DummyVAO;
    private boolean toonEnable = true;
    private float m_LastPointerX = 0.5f;

    private int texWidth, texHeight;

    @Override
    public void initUI() {
//        if(mTweakBar != null)
//            mTweakBar.addValue("Enable filter", new FieldControl(this, "toonEnable", FieldControl.CALL_FIELD), false, 0);
    }

    @Override
    protected void initRendering() {
        NvLogger.setLevel(NvLogger.INFO);
        m_Program = NvGLSLProgram.createFromFiles("shaders/Quad_VS.vert", "shaders/night_vision.frag");
        m_Program.enable();
        m_Program.setUniform1i("sceneBuffer", 0);
        m_Program.setUniform1f("luminanceThreshold", 0.2f);
        m_Program.setUniform1f("colorAmplification", 4.0f);
        m_Program.setUniform1f("effectCoverage", 0.5f);
        //load input texture
        NvImage m_sourceImage = NvImage.createFromDDSFile("textures/flower1024.dds");
        texWidth = m_sourceImage.getWidth();
        texHeight = m_sourceImage.getHeight();
        m_sourceTexture = m_sourceImage.updaloadTexture();
        GLES.checkGLError();

        GLES20.glBindTexture(GL11.GL_TEXTURE_2D, m_sourceTexture);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GLES20.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        m_DummyVAO = GLES.glGenVertexArray();

        setTitle("RadialBlur");

        Log.i("RadialBlur", "initRendering done!");
    }

    @Override
    protected void reshape(int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        m_aspectRatio = (float)height/(float)width;
        m_LastPointerX = width/2.0f;

        Log.i("RadialBlur", "reshape done!");
    }

    @Override
    protected void draw() {
//        Log.i("RadialBlur", "Start to draw");
        GLES30.glBindVertexArray(m_DummyVAO);
        m_Program.enable();
//        m_Program.setUniform1i("g_Texture", 0);
        m_Program.setUniform1f("elapsedTime", getTotalTime());
//        m_Program.setUniform1f("g_BlurWidth", 0.3f);
//        m_Program.setUniform2f("g_Center", 0.5f, 0.5f);
//        m_Program.setUniform1f("g_Intensity", 6f);
//        m_Program.setUniform1f("g_GlowGamma", 1.6f);

//        m_Program.setUniform3f("iResolution", getWidth(), getHeight(), 0);
//        m_Program.setUniform1f("iGlobalTime", getTotalTime());

//        m_Program.setUniform1i("toon_enable", toonEnable?1:0);
//        m_Program.setUniform1f("edge_thres", 0.2f);
//        m_Program.setUniform1f("edge_thres2", 5.0f);

        if(isTouchDown(0)){
            m_LastPointerX = getTouchX(0);
        }
//        m_Program.setUniform1f("mouse_x_offset", m_LastPointerX/ getWidth());
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GL11.GL_TEXTURE_2D, m_sourceTexture);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
        m_Program.disable();
        GLES30.glBindVertexArray(0);
//        Log.i("RadialBlur", "draw done!");
    }
}
