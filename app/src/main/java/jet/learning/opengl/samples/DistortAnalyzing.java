package jet.learning.opengl.samples;

import android.opengl.GLES20;
import android.opengl.GLES30;

import com.nvidia.developer.opengl.app.NvSampleApp;
import com.nvidia.developer.opengl.ui.NvUIFontFamily;
import com.nvidia.developer.opengl.ui.NvUIRect;
import com.nvidia.developer.opengl.ui.NvUIText;
import com.nvidia.developer.opengl.ui.NvUITextAlign;
import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.NvCPUTimer;
import com.nvidia.developer.opengl.utils.NvGLSLProgram;
import com.nvidia.developer.opengl.utils.NvImage;
import com.nvidia.developer.opengl.utils.NvShapes;
import com.nvidia.developer.opengl.utils.NvUtils;

import javax.microedition.khronos.opengles.GL11;

/**
 * 用OpenGL 3.0或许能大幅提升性能。<p></p>
 * Created by mazhen'gui on 2017/10/14.
 */
public final class DistortAnalyzing extends NvSampleApp {
    private static final int DISTORT = 0;
    private static final int STICH = 1;
    private static final int TOTAL = 2;
    private static final int COUNT = 3;
    private static final int STATS_FRAMES = 60;
    private static final String pattern = "Distort Timing: %f\n Stich Timing: %f\n Total Timing: %f";

    private NvGLSLProgram m_distortProgram;
    private NvGLSLProgram m_stichProgram;
    private final NvCPUTimer[] m_CPUTimers = new NvCPUTimer[COUNT];
    private int m_sourceTexture;
    private int m_framebuffer;
    private int m_tempTex;

    private NvUIText m_timingStats;
    private int m_statsCountdown = STATS_FRAMES;

    @Override
    protected void initRendering() {
        m_distortProgram = NvGLSLProgram.createFromFiles("shaders/DefaultScreenSpaceVS.vert","shaders/antvr_distort.frag");
        m_stichProgram = NvGLSLProgram.createFromFiles("shaders/DefaultScreenSpaceVS.vert","shaders/antvr_stich.frag");

        for(int i = 0; i < COUNT; i++){
            m_CPUTimers[i] = new NvCPUTimer();
            m_CPUTimers[i].init();
        }

        //load input texture
        NvImage sourceImage = NvImage.createFromDDSFile("textures/flower1024.dds");
        m_sourceTexture = sourceImage.updaloadTexture();
        GLES.checkGLError();

        GLES20.glBindTexture(GL11.GL_TEXTURE_2D, m_sourceTexture);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GLES20.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    @Override
    public void initUI() {
        // statistics
        if (mFPSText != null) {
            NvUIRect tr = mFPSText.getScreenRect();
            System.out.println(tr);
            m_timingStats = new NvUIText("Multi\nLine\nString", NvUIFontFamily.SANS, (mFPSText.getFontSize()*2)/3, NvUITextAlign.RIGHT);
            m_timingStats.setColor(NvUtils.makefourcc(0x30, 0xD0, 0xD0, 0xB0));
            m_timingStats.setShadow();
            mUIWindow.add(m_timingStats, tr.left, tr.top+tr.height+8);
        }
    }

    @Override
    protected void draw() {
        GLES20.glViewport(0,0,getWidth(), getHeight());
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        m_CPUTimers[TOTAL].start();
        // first pass: distort
        {
            m_CPUTimers[DISTORT].start();  // record the current time
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, m_framebuffer);
            m_distortProgram.enable();
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, m_sourceTexture);
            NvShapes.drawQuad(m_distortProgram.getAttribLocation("aPosition"));
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,0);
            GLES.checkGLError();
            waitGPUTaskFinished();

            m_CPUTimers[DISTORT].stop();
        }

        // second pass: stich
        {
            m_CPUTimers[STICH].start();  // record the current time
            m_stichProgram.enable();
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, m_tempTex);
            NvShapes.drawQuad(m_stichProgram.getAttribLocation("aPosition"));
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
            GLES.checkGLError();
            waitGPUTaskFinished();

            m_CPUTimers[STICH].stop();
        }
        m_CPUTimers[TOTAL].stop();


        {// update the status
            if(m_statsCountdown == 0) {
                m_statsCountdown = STATS_FRAMES;

                String statusValue = NvUtils.sprintf(pattern,
                        m_CPUTimers[DISTORT].getScaledCycles() * 1000/60,
                        m_CPUTimers[STICH].getScaledCycles() * 1000/60,
                        m_CPUTimers[TOTAL].getScaledCycles() * 1000/60);
                m_timingStats.setString(statusValue);

                for (int i = 0; i < COUNT; i++) {
                    m_CPUTimers[i].reset();
                }
            }else{
                m_statsCountdown --;
            }
        }

    }

    private void waitGPUTaskFinished(){
        long fenc = GLES30.glFenceSync(GLES30.GL_SYNC_GPU_COMMANDS_COMPLETE, 0);
        int result;
        do{
            result = GLES30.glClientWaitSync(fenc, GLES30.GL_SYNC_FLUSH_COMMANDS_BIT, 1000_000);
            if(result == GLES30.GL_WAIT_FAILED){
                throw new RuntimeException();
            }else if(result == GLES30.GL_ALREADY_SIGNALED){
                break;
            }
        }while (result != GLES30.GL_CONDITION_SATISFIED);
    }

    @Override
    protected void reshape(int width, int height) {
        if(m_framebuffer != 0){
            GLES.glDeleteFramebuffers(m_framebuffer);
            GLES.glDeleteTextures(m_tempTex);
            GLES.checkGLError();
        }

        m_tempTex = GLES.glGenTextures();
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, m_tempTex);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP_TO_EDGE);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        m_framebuffer = GLES.glGenFramebuffers();
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, m_framebuffer);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, m_tempTex, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }
}
