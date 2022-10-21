package jet.learning.opengl.gtao;

import android.opengl.GLES30;
import android.opengl.GLES32;

import com.nvidia.developer.opengl.app.NvSampleApp;
import com.nvidia.developer.opengl.ui.NvTweakEnumi;
import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.NvGLSLProgram;

import jet.learning.opengl.common.GLSLUtil;
import jet.learning.opengl.common.Texture2D;
import jet.learning.opengl.common.TextureUtils;

public class GTAODemo extends NvSampleApp {

    private CubeScene m_Scene;
    private Texture2D m_TextureAO;
    private GTAO m_GTAO;
    private NvGLSLProgram m_BlitProg;
    private NvGLSLProgram m_VisualNormal;

    boolean m_EnableGTAO = true;
    int m_TextureIndex = 0;
    int m_ArraySlice = 0;

    @Override
    public void initUI() {
        mTweakBar.addValue("Enable GTAO", createControl("m_EnableGTAO"));

        NvTweakEnumi objectIndex[] =
        {
                new NvTweakEnumi( "None", 0 ),
                new NvTweakEnumi( "Normal", 1 ),
                new NvTweakEnumi( "InterleaveDepth", 2 ),
                new NvTweakEnumi( "InterleaveAO", 3 ),
        };

        mTweakBar.addEnum("Visual Texture:", NvSampleApp.createControl(this,"m_TextureIndex"), objectIndex, 0x55);
        mTweakBar.addValue("Array Slice", createControl("m_ArraySlice"), 0, 15);
    }

    @Override
    protected void initRendering() {
        m_Scene = new CubeScene(m_transformer);
        m_Scene.onCreate();

        GLES32.glDepthRangef(0.0f, 1);

        m_GTAO = new GTAO();
        m_GTAO.Create();

//        m_transformer.setMotionMode(NvCameraMotionType.FIRST_PERSON);
//        m_transformer.setTranslation(0, -4, 0);

        m_BlitProg = NvGLSLProgram.createFromFiles("shaders/Quad_VS.vert", "labs/GTAO/shaders/VisualTex2DArray.frag");
        m_VisualNormal = NvGLSLProgram.createFromFiles("shaders/Quad_VS.vert", "labs/GTAO/shaders/VisualNormal.frag");
    }


    @Override
    protected void draw() {
//        if(m_TextureAO == null || )
        m_Scene.draw();

        if(m_EnableGTAO){
            m_TextureAO = TextureUtils.resizeTexture2D(m_TextureAO, m_Scene.getWidth(), m_Scene.getHeight(), GLES30.GL_RGBA8);

            SSAOParameters parameters = new SSAOParameters();
            parameters.Projection.load(m_Scene.getProjMat());
            parameters.SceneDepth = m_Scene.getSceneDepth();
            parameters.ResultAO = m_TextureAO;
            parameters.SceneWidth = m_Scene.getSceneDepth().getWidth();
            parameters.SceneHeight = m_Scene.getSceneDepth().getHeight();
            parameters.CameraFar = m_Scene.getSceneFarPlane();
            parameters.CameraNear = m_Scene.getSceneNearPlane();

            m_GTAO.RenderAO(parameters);

            if(m_TextureIndex == 0){
                m_Scene.applyAO(m_TextureAO);
            }
        }


//        m_BlitProg.enable();
//        GLES.glBindTextureUnit(0, m_TextureAO);
        /*GLES31.glBindImageTexture(0, m_Scene.getSceneColor().getTexture(), 0, false, 0, GLES31.GL_READ_WRITE, m_Scene.getSceneColor().getFormat());
        GLES31.glDispatchCompute(m_TextureAO.getWidth() / 16, m_TextureAO.getHeight() / 16, 1);

        GLES.glBindTextureUnit(0, null);
        GLES31.glBindImageTexture(0, 0,0, false, 0, GLES31.GL_WRITE_ONLY, GLES31.GL_RGBA8);

        GLES31.glMemoryBarrier(GLES31.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);*/


        m_Scene.resoveMultisampleTexture();

        if(m_EnableGTAO){
            if(m_TextureIndex == 1){
                blitTexToScreen(m_GTAO.getMobileGTAO(), 0,false, 0);
//                blitTexToScreen(m_Scene.getSceneDepth(), 0,false, 0);
            }else if(m_TextureIndex == 2){
                blitTexToScreen(m_GTAO.getInterleaveDepth(), m_ArraySlice,false, m_Scene.getSceneFarPlane());
            }else  if(m_TextureIndex == 3){
                blitTexToScreen(m_GTAO.getInterleaveAO(), m_ArraySlice,false, 0);
            }
        }
    }

    private void blitTexToScreen(Texture2D texture, int arraySlice, boolean normalize, float value){
        if(texture == null)
            return;

        if(texture.getArraySize() == 1) {  // normal texture
            m_Scene.blitTexToScreen(texture);
            /*GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER,0);

            int width =  getWidth();
            int height = getHeight();
            {
                GLES30.glViewport(0,0,width, height);
                GLES30.glDisable(GLES30.GL_DEPTH_TEST);
                GLES30.glDisable(GLES30.GL_BLEND);

                m_VisualNormal.enable();
                GLES.glBindTextureUnit(0, texture);
                GLES30.glBindSampler(0, 0);

                GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3);
                GLES.glBindTextureUnit(0, null);
            }*/
        }else /*if(texture.getDepth() > 1)*/{  // texture array
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER,0);

            int width =  getWidth();
            int height = getHeight();
            {
                GLES30.glViewport(2 * width / 3, 2 * height / 3, width / 3, height / 3);
                GLES30.glDisable(GLES30.GL_DEPTH_TEST);
                GLES30.glDisable(GLES30.GL_BLEND);

                m_BlitProg.enable();
                GLES.glBindTextureUnit(0, texture);
                GLES30.glBindSampler(0, 0);
                GLSLUtil.setFloat4(m_BlitProg, "gArrayData", normalize ? 1.f : 0.0f, value, arraySlice, 0);

                GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3);
                GLES.glBindTextureUnit(0, null);

                // reset viewport
                GLES30.glViewport(0,0,width, height);
            }
        }
    }

    @Override
    protected void reshape(int width, int height) {
        m_Scene.onResize(width, height);
    }

    /*@Override
    public void onDestroy() {
        m_Scene.onDestroy();
    }*/
}
