package jet.learning.opengl.samples;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.util.Log;

import com.example.oglsamples.R;
import com.nvidia.developer.opengl.app.NvSampleApp;
import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.Glut;
import com.nvidia.developer.opengl.utils.NvGLSLProgram;
import com.nvidia.developer.opengl.utils.NvLogger;

import javax.microedition.khronos.opengles.GL11;

/**
 * Created by mazhen'gui on 2016/12/29.
 */

public class LightStreaker extends NvSampleApp {
    private int m_sourceTexture;
    private NvGLSLProgram m_DownsampleProgram;
    private NvGLSLProgram m_StreakerProgram;
    private NvGLSLProgram m_CombineProgram;
    private NvGLSLProgram m_ExtractHighLight;

    private float m_aspectRatio;
    private int m_DummyVAO;
    private boolean toonEnable = true;
    private float m_LastPointerX = 0.5f;

    private int texWidth, texHeight;
    private int m_Framebuffer;
    private int m_Downsample0;
    private int[] m_Downsample1 = new int[2];

    @Override
    public void initUI() {
//        if(mTweakBar != null)
//            mTweakBar.addValue("Enable filter", new FieldControl(this, "toonEnable", FieldControl.CALL_FIELD), false, 0);
    }

    @Override
    protected void initRendering() {
        Glut.init(this);
        NvLogger.setLevel(NvLogger.INFO);
        m_DownsampleProgram = NvGLSLProgram.createFromFiles("shaders/Quad_VS.vert", "shaders/downsample_normal.frag");
        m_StreakerProgram = NvGLSLProgram.createFromFiles("shaders/Quad_VS.vert", "shaders/light_streaker.frag");
        m_CombineProgram = NvGLSLProgram.createFromFiles("shaders/Quad_VS.vert", "shaders/simple_combine.frag");
        m_ExtractHighLight = NvGLSLProgram.createFromFiles("shaders/Quad_VS.vert", "shaders/extract_hight_light.frag");

        //load input texture
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.data2, options);
//        Pixels bitmap = Glut.loadImageFromResource(R.drawable.data2);

        m_sourceTexture = GLES.glGenTextures();
        GLES20.glBindTexture(GL11.GL_TEXTURE_2D, m_sourceTexture);
        GLUtils.texImage2D(GL11.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, bitmap, 0);
//        bitmap.uploadTexture2D();
        GLES.checkGLError();
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GLES20.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        texWidth = bitmap.getWidth();
        texHeight = bitmap.getHeight();
        bitmap.recycle();

        m_DummyVAO = GLES.glGenVertexArray();

        setTitle("LightStreaker");

        Log.i("LightStreaker", "initRendering done!");
    }

    private static int createTexture(int width, int height, int format) {
        int textureID = GLES.glGenTextures();
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, format, width, height, 0, format, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP_TO_EDGE);

        return textureID;
    }

    @Override
    protected void reshape(int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        m_aspectRatio = (float)height/(float)width;
        m_LastPointerX = width/2.0f;

        // create framebuffer and textures.
        {
            m_Downsample0 = createTexture(width/2, height/2, GLES20.GL_RGB);
            m_Downsample1[0] = createTexture(width/4, height/4, GLES20.GL_RGB);
            m_Downsample1[1] = createTexture(width/4, height/4, GLES20.GL_RGB);
            m_Framebuffer = GLES.glGenFramebuffers();
        }

        Log.i("LightStreaker", "reshape done!");
    }

    private void setRenderTarget(int textureID, int width, int height){
        GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, textureID, 0);
        GLES20.glViewport(0,0,width, height);
    }

    @Override
    protected void draw() {

        GLES20.glDisable(GLES20.GL_BLEND);
//        uniform vec2 g_TexelSize;
//        uniform sampler2D g_Texture;
//        Log.i("LightStreaker", "Start to draw");
        GLES30.glBindVertexArray(m_DummyVAO);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, m_Framebuffer);
        {// first pass: dowsample
            m_DownsampleProgram.enable();
            m_DownsampleProgram.setUniform1i("g_Texture", 0);
            m_DownsampleProgram.setUniform2f("g_TexelSize",1.0f/texWidth, 1.0f/texHeight);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, m_sourceTexture);
            setRenderTarget(m_Downsample0, getWidth()/2, getHeight()/2);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
        }

        {// second pass: dowsample
            m_DownsampleProgram.setUniform2f("g_TexelSize",2f/texWidth, 2f/texHeight);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, m_Downsample0);
            setRenderTarget(m_Downsample1[1], getWidth()/4, getHeight()/4);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
        }

        {// Extract high light
            m_ExtractHighLight.enable();
            m_ExtractHighLight.setUniform1i("g_Texture", 0);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, m_Downsample1[1]);
            setRenderTarget(m_Downsample1[0], getWidth()/4, getHeight()/4);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
        }

//        uniform vec4 g_TexSizeAndDir;
//        uniform float g_Attenuation;
//        uniform float g_Pass;
//        uniform sampler2D g_Texture;

        int ping_pong = 0;
        int pass = 0;
        m_StreakerProgram.enable();
        m_StreakerProgram.setUniform1i("g_Texture", 0);
        m_StreakerProgram.setUniform1f("g_Attenuation", 0.93f);
        double angle = Math.PI/2;
        {  // streak filter pass 0
            float dirX = (float)Math.cos(angle);
            float dirY = (float)Math.sin(angle);
            m_StreakerProgram.setUniform1f("g_Pass", 1);
            m_StreakerProgram.setUniform4f("g_TexSizeAndDir", 4f/texWidth, 4f/texHeight, dirX, dirY);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, m_Downsample1[ping_pong]);
            setRenderTarget(m_Downsample1[1-ping_pong], getWidth()/4, getHeight()/4);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
            ping_pong = 1-ping_pong;
        }

        {   // streak filter pass 1
//            angle += Math.PI/2;
            float dirX = (float)Math.cos(angle);
            float dirY = (float)Math.sin(angle);
            m_StreakerProgram.setUniform1f("g_Pass", 1);
            m_StreakerProgram.setUniform4f("g_TexSizeAndDir", 4f/texWidth, 4f/texHeight, -dirX, -dirY);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, m_Downsample1[ping_pong]);
            setRenderTarget(m_Downsample1[1-ping_pong], getWidth()/4, getHeight()/4);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
            ping_pong = 1-ping_pong;
        }

        {
            // streak filter pass 2
            angle = 0;
            float dirX = (float)Math.cos(angle);
            float dirY = (float)Math.sin(angle);
            m_StreakerProgram.setUniform1f("g_Pass", 1);
            m_StreakerProgram.setUniform4f("g_TexSizeAndDir", 4f/texWidth, 4f/texHeight, dirX, dirY);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, m_Downsample1[ping_pong]);
            setRenderTarget(m_Downsample1[1-ping_pong], getWidth()/4, getHeight()/4);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
            ping_pong = 1-ping_pong;
        }

        {
            // streak filter pass 3
//            angle += Math.PI/2;
            float dirX = (float)Math.cos(angle);
            float dirY = (float)Math.sin(angle);
            m_StreakerProgram.setUniform1f("g_Pass", 1);
            m_StreakerProgram.setUniform4f("g_TexSizeAndDir", 4f/texWidth, 4f/texHeight, -dirX, -dirY);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, m_Downsample1[ping_pong]);
            setRenderTarget(m_Downsample1[1-ping_pong], getWidth()/4, getHeight()/4);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
            ping_pong = 1-ping_pong;
        }

        GLES.checkGLError();

//        m_Program.setUniform1i("toon_enable", toonEnable?1:0);
//        m_Program.setUniform1f("edge_thres", 0.2f);
//        m_Program.setUniform1f("edge_thres2", 5.0f);

        if(isTouchDown(0)){
            m_LastPointerX = getTouchX(0);
        }
//        m_Program.setUniform1f("mouse_x_offset", m_LastPointerX/ getWidth());
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
//        Log.i("LightStreaker", "draw done!");

        GLES20.glViewport(0,0,getWidth(), getHeight());
        // render to defualt framebuffer
        {
            m_CombineProgram.enable();
            m_CombineProgram.setUniform1i("g_Texture0", 0);
            m_CombineProgram.setUniform1i("g_Texture1", 1);
            m_CombineProgram.setUniform2f("g_f2Intensity", 1f, 1.0f);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, m_Downsample1[ping_pong]);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, m_sourceTexture);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
            m_CombineProgram.disable();
        }
        GLES30.glBindVertexArray(0);
    }
}
