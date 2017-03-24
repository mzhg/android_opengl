package jet.learning.opengl.samples;

import android.opengl.GLES20;
import android.opengl.GLES30;
import android.util.Log;

import com.nvidia.developer.opengl.app.NvSampleApp;
import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.NvImage;
import com.nvidia.developer.opengl.utils.NvLogger;

import javax.microedition.khronos.opengles.GL11;

import jet.learning.opengl.common.GaussionBlurProgram;

/**
 * Created by mazhen'gui on 2016/12/23.
 */

public class GaussionBlur extends NvSampleApp{
    private int m_sourceTexture;
    private GaussionBlurProgram m_Program;
    private float m_aspectRatio;
    private int m_DummyVAO;
    private boolean toonEnable = true;
    private float m_LastPointerX = 0.5f;

    private int texWidth, texHeight;
    private int m_Framebuffer;
    private int m_TempTexture;

    @Override
    protected void initRendering() {
        NvLogger.setLevel(NvLogger.INFO);
        m_Program = new GaussionBlurProgram();
        m_Program.init(11);
        //load input texture
        NvImage m_sourceImage = NvImage.createFromDDSFile("textures/flower1024.dds");
        texWidth = m_sourceImage.getWidth();
        texHeight = m_sourceImage.getHeight();
        m_sourceTexture = m_sourceImage.updaloadTexture();
        GLES.checkGLError();

        GLES20.glBindTexture(GL11.GL_TEXTURE_2D, m_sourceTexture);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP_TO_EDGE);
        GLES20.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        m_DummyVAO = GLES.glGenVertexArray();

        setTitle("GaussionBlur");

        Log.i("GaussionBlur", "initRendering done!");
    }

    @Override
    protected void reshape(int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        m_aspectRatio = (float) height / (float) width;
        m_LastPointerX = width / 2.0f;

        m_Framebuffer = GLES.glGenFramebuffers();
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, m_Framebuffer);
        {
            m_TempTexture = GLES.glGenTextures();
            GLES20.glBindTexture(GL11.GL_TEXTURE_2D, m_TempTexture);
            GLES30.glTexStorage2D(GL11.GL_TEXTURE_2D, 1, GLES30.GL_RGBA8, width, height);
            GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

            GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, m_TempTexture, 0);
            GLES.glDrawBuffer(GLES30.GL_COLOR_ATTACHMENT0);
        }
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
        GLES20.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        Log.i("GaussionBlur", "reshape done!");
    }

    @Override
    protected void draw() {
        Log.i("GaussionBlur", "Start to draw");
        GLES30.glBindVertexArray(m_DummyVAO);
        m_Program.enable();
        /*
        m_Program.setUniform1i("g_Texture", 0);
        m_Program.setUniform1fv("g_Offsets", gOffsets, 0, gOffsets.length);
        m_Program.setUniform1fv("g_Weights", gWeights, 0, gWeights.length);
        */

        {// first pass
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, m_Framebuffer);
            m_Program.setHalfPixelSize(1.0f/texWidth, 0);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GL11.GL_TEXTURE_2D, m_sourceTexture);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
        }

        {// second pass
            m_Program.setHalfPixelSize( 0, 1.0f/texHeight);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GL11.GL_TEXTURE_2D, m_TempTexture);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
        }


//        m_Program.setUniform1i("toon_enable", toonEnable?1:0);
//        m_Program.setUniform1f("edge_thres", 0.2f);
//        m_Program.setUniform1f("edge_thres2", 5.0f);

        if(isTouchDown(0)){
            m_LastPointerX = getTouchX(0);
        }
//        m_Program.setUniform1f("mouse_x_offset", m_LastPointerX/ getWidth());

        m_Program.disable();
        GLES30.glBindVertexArray(0);
        Log.i("RadialBlur", "draw done!");
    }
}
