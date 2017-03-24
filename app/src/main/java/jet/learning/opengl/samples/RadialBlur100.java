package jet.learning.opengl.samples;

import android.opengl.GLES20;
import android.util.Log;

import com.nvidia.developer.opengl.app.NvSampleApp;
import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.NvGLSLProgram;
import com.nvidia.developer.opengl.utils.NvImage;
import com.nvidia.developer.opengl.utils.NvLogger;
import com.nvidia.developer.opengl.utils.NvShapes;

import javax.microedition.khronos.opengles.GL11;

/**
 * Created by mazhen'gui on 2016/12/21.
 */

public class RadialBlur100 extends NvSampleApp {
    private int m_sourceTexture;
    private NvGLSLProgram m_Program;
    private float m_aspectRatio;
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
        m_Program = NvGLSLProgram.createFromFiles("shaders/Quad_VS_100.vert", "shaders/radial_blur_100.frag");
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
        m_Program.enable();
        m_Program.setUniform1i("iChannel0", 0);
        m_Program.setUniform2f("iCenter", 0.5f, 0.5f);
        m_Program.setUniform1f("iGlobalTime", getTotalTime());

//        m_Program.setUniform1i("toon_enable", toonEnable?1:0);
//        m_Program.setUniform1f("edge_thres", 0.2f);
//        m_Program.setUniform1f("edge_thres2", 5.0f);

        if(isTouchDown(0)){
            m_LastPointerX = getTouchX(0);
        }
//        m_Program.setUniform1f("mouse_x_offset", m_LastPointerX/ getWidth());
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GL11.GL_TEXTURE_2D, m_sourceTexture);
        NvShapes.drawCube(m_Program.getAttribLocation("aPosition"));
        m_Program.disable();
//        Log.i("RadialBlur", "draw done!");
    }
}
