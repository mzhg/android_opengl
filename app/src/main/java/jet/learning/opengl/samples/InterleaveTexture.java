package jet.learning.opengl.samples;

import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLES31;
import android.opengl.GLES32;

import com.nvidia.developer.opengl.app.NvSampleApp;
import com.nvidia.developer.opengl.ui.NvTweakEnumi;
import com.nvidia.developer.opengl.utils.CommonUtil;
import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.Macro;
import com.nvidia.developer.opengl.utils.NvGLSLProgram;
import com.nvidia.developer.opengl.utils.NvImage;
import com.nvidia.developer.opengl.utils.NvLogger;
import com.nvidia.developer.opengl.utils.NvUtils;

import javax.microedition.khronos.opengles.GL11;

import jet.learning.opengl.common.GLSLUtil;
import jet.learning.opengl.common.Texture2D;
import jet.learning.opengl.common.Texture2DDesc;
import jet.learning.opengl.common.Texture3D;
import jet.learning.opengl.common.Texture3DDesc;
import jet.learning.opengl.common.TextureUtils;

public class InterleaveTexture extends NvSampleApp {
    private int m_sourceTexture;
    private NvGLSLProgram[][] m_InterleaveProg = new NvGLSLProgram[2][2];
    private NvGLSLProgram[] m_VisualProg = new NvGLSLProgram[3];
    private float m_aspectRatio;
    private int m_DummyVAO;

    private int m_Framebuffer;
    private Texture3D m_InterleaveTex3D;
    private Texture2D m_InterleaveTex2DArray;

    private static final int USE_TEXTURE_ARRAY = 0;
//    private Texture2D m_sourceImage;

    private boolean mUseTextureArray = true;
    private int mVisualInterleave;
    private int m_ArraySlice;
    private int texWidth, texHeight;

    @Override
    public void initUI() {
        mTweakBar.addValue("Use TextureArray", createControl("mUseTextureArray"));

        NvTweakEnumi objectIndex[] =
                {
                        new NvTweakEnumi( "Defualt", 0 ),
                        new NvTweakEnumi( "InterleaveCS", 1 ),
                        new NvTweakEnumi( "Deinterleave", 2 ),
                };

        mTweakBar.addEnum("Visual Type:", NvSampleApp.createControl(this,"mVisualInterleave"), objectIndex, 0x55);
        mTweakBar.addValue("Array Slice", createControl("m_ArraySlice"), 0, 15);
    }

    private Macro[] getInterleaveMacros(boolean useArray, boolean interleaved){
        return new Macro[]{
                new Macro("USE_TEXTURE_ARRAY", useArray ? 1 : 0),
                new Macro("InterleaveCS", interleaved ? 1 : 0),
                new Macro("DeinterleavePS", interleaved ? 0 : 1),
        };
    }

    @Override
    protected void initRendering() {
        NvLogger.setLevel(NvLogger.INFO);
        m_InterleaveProg[0][0] = NvGLSLProgram.createProgram("labs/GTAO/shaders/InterleaveTest.comp", getInterleaveMacros(true, true));
        m_InterleaveProg[1][0] = NvGLSLProgram.createProgram("labs/GTAO/shaders/InterleaveTest.comp", getInterleaveMacros(false, true));
        m_InterleaveProg[0][1] = NvGLSLProgram.createProgram("shaders/Quad_VS.vert", "labs/GTAO/shaders/InterleaveTest.comp", getInterleaveMacros(true, false));
        m_InterleaveProg[1][1] = NvGLSLProgram.createProgram("shaders/Quad_VS.vert","labs/GTAO/shaders/InterleaveTest.comp", getInterleaveMacros(false, false));
        m_VisualProg[0] = NvGLSLProgram.createProgram("shaders/Quad_VS.vert", "labs/GTAO/shaders/VisualTex2DArray.frag", CommonUtil.toArray(new Macro("USE_TEXTURE_ARRAY", 1)));
        m_VisualProg[1] = NvGLSLProgram.createProgram("shaders/Quad_VS.vert", "labs/GTAO/shaders/VisualTex2DArray.frag", CommonUtil.toArray(new Macro("USE_TEXTURE_ARRAY", 0)));
        m_VisualProg[2] = NvGLSLProgram.createProgram("shaders/Quad_VS.vert", "labs/GTAO/shaders/ApplyAO.frag", null);

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

        GLES.checkGLError();
        Texture3DDesc desc = new Texture3DDesc();
        desc.width = texWidth / 4;
        desc.height = texHeight / 4;
        desc.depth = 16;
        desc.format = GLES30.GL_RGBA8;
        desc.mipLevels = 1;
        m_InterleaveTex3D = TextureUtils.createTexture3D(desc, null);

        Texture2DDesc desc2D = new Texture2DDesc();
        desc2D.width = texWidth / 4;
        desc2D.height = texHeight / 4;
        desc2D.arraySize = 16;
        desc2D.format = GLES30.GL_RGBA8;
        desc2D.mipLevels = 1;
        m_InterleaveTex2DArray = TextureUtils.createTexture2D(desc2D, null);

        setTitle("InterleaveTexture");
    }

    @Override
    protected void reshape(int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        m_aspectRatio = (float) height / (float) width;
    }

    @Override
    protected void draw() {
        int programIdx = mUseTextureArray ? 0 : 1;

        //  generate interleave texture
        m_InterleaveProg[programIdx][0].enable();
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, m_sourceTexture);
        GLES.checkGLError();
        if(mUseTextureArray){
            GLES31.glBindImageTexture(0, m_InterleaveTex2DArray.getTexture(),0, true, 0, GLES32.GL_WRITE_ONLY, m_InterleaveTex2DArray.getFormat());
        }else{
            GLES31.glBindImageTexture(0, m_InterleaveTex3D.getTexture(),0, true, 0, GLES32.GL_WRITE_ONLY, m_InterleaveTex3D.getFormat());
        }
        GLES.checkGLError();
        GLES31.glDispatchCompute(NvUtils.divideAndRoundUp(texWidth, 16), NvUtils.divideAndRoundUp(texHeight, 16), 1);
        GLES.checkGLError();
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES31.glBindImageTexture(0, 0,0, false, 0, GLES32.GL_WRITE_ONLY, m_InterleaveTex3D.getFormat());

        GLES.checkGLError();
        // visual interleave results
        GLES30.glBindVertexArray(m_DummyVAO);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER,0);

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        if(mVisualInterleave == 0){  // visual interleave results
            blitTexToScreen(m_sourceTexture);
        }else if(mVisualInterleave == 1){
            blitTexToScreen(mUseTextureArray ?m_InterleaveTex2DArray :  m_InterleaveTex3D, m_ArraySlice);
        }else if(mVisualInterleave == 2){
            blitTexToScreen(mUseTextureArray ?m_InterleaveTex2DArray :  m_InterleaveTex3D);
        }
        GLES30.glBindVertexArray(0);
    }

    private void blitTexToScreen(Object texture, int arraySlice){
        if(texture == null)
            return;

        final int programIdx = (texture instanceof Texture2D) ? 0 : 1;
        int width =  getWidth();
        int height = getHeight();
        {
            GLES30.glViewport(width - texWidth, height - texHeight, texWidth, texHeight);
            GLES30.glDisable(GLES30.GL_DEPTH_TEST);
            GLES30.glDisable(GLES30.GL_BLEND);

            m_VisualProg[programIdx].enable();
            if(programIdx == 0){
                GLES.glBindTextureUnit(0, (Texture2D)texture);
            }else{
                Texture3D tex3D = (Texture3D)texture;
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(tex3D.getTarget(), tex3D.getTexture());
            }

            GLES30.glBindSampler(0, 0);
            GLSLUtil.setFloat4(m_VisualProg[programIdx], "gArrayData", 0.0f, 0.0f, arraySlice, 0);

            GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3);
            GLES.glBindTextureUnit(0, null);

            // reset viewport
            GLES30.glViewport(0,0,width, height);
        }
    }

    private void blitTexToScreen(Object texture){
        if(texture == null)
            return;

        final int programIdx = (texture instanceof Texture2D) ? 0 : 1;
        int width =  getWidth();
        int height = getHeight();
        {
            GLES30.glViewport(0,0, texWidth, texHeight);
            GLES30.glDisable(GLES30.GL_DEPTH_TEST);
            GLES30.glDisable(GLES30.GL_BLEND);

            m_InterleaveProg[programIdx][1].enable();
            if(programIdx == 0){
                GLES.glBindTextureUnit(0, (Texture2D)texture);
            }else{
                Texture3D tex3D = (Texture3D)texture;
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(tex3D.getTarget(), tex3D.getTexture());
            }
            GLES30.glBindSampler(0, 0);

            GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3);
            GLES.glBindTextureUnit(0, null);
            GLES30.glViewport(0,0,width, height);
        }
    }

    private void blitTexToScreen(int texture){
        int width =  getWidth();
        int height = getHeight();
        {
            GLES30.glViewport(0,0,width, height);
            GLES30.glDisable(GLES30.GL_DEPTH_TEST);
            GLES30.glDisable(GLES30.GL_BLEND);

            m_VisualProg[2].enable();
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
            GLES30.glBindSampler(0, 0);

            GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3);
            GLES.glBindTextureUnit(0, null);
        }
    }
}
