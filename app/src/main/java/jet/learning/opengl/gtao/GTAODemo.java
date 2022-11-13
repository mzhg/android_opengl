package jet.learning.opengl.gtao;

import android.opengl.GLES30;

import com.nvidia.developer.opengl.app.NvSampleApp;
import com.nvidia.developer.opengl.ui.NvTweakEnumi;
import com.nvidia.developer.opengl.ui.NvTweakVarBase;
import com.nvidia.developer.opengl.ui.NvUIReaction;
import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.NvGLSLProgram;

import java.util.ArrayList;
import java.util.List;

import jet.learning.opengl.common.GLSLUtil;
import jet.learning.opengl.common.Texture2D;
import jet.learning.opengl.common.TextureUtils;

public class GTAODemo extends NvSampleApp {

    private CubeScene m_Scene;
    private Texture2D m_TextureAO;
    private GTAO m_GTAO;
    private NvGLSLProgram m_BlitProg;
    private NvGLSLProgram m_VisualNormal;

    int m_TextureIndex = 0;
    int m_Downsample = 1;
    int m_GTAOMethod = 0;

    SSAOParameters parameters = new SSAOParameters();

    private static final int ACTION_AO_METHOD = 1;

    private final List<NvTweakVarBase> m_HBAOParameters = new ArrayList<>();

    @Override
    public void initUI() {
        NvTweakEnumi GTAOMethods[] =
        {
            new NvTweakEnumi( "Disbaled", 0 ),
            new NvTweakEnumi( "InterleaveGTAO", 1 ),
            new NvTweakEnumi( "InterleaveHBAO", 2 ),
            new NvTweakEnumi( "GTAO", 3 ),
            new NvTweakEnumi( "HBAO", 4 ),
        };

        mTweakBar.addMenu("AO Method:", createControl("m_GTAOMethod"), GTAOMethods, ACTION_AO_METHOD);

//        NvTweakEnumi objectIndex[] =
//        {
//                new NvTweakEnumi( "None", 0 ),
//                new NvTweakEnumi( "Normal", 1 ),
//                new NvTweakEnumi( "InterleaveDepth", 2 ),
//                new NvTweakEnumi( "InterleaveAO", 3 ),
//        };
//
//        mTweakBar.addEnum("Visual Texture:", NvSampleApp.createControl(this,"m_TextureIndex"), objectIndex, 0x55);
        mTweakBar.addValue("Downsample", createControl("m_Downsample"), 1, 3);
        mTweakBar.addValue("Quality", createControl(parameters,"GTAOQuality"), 0, 3);
        mTweakBar.addValue("Intensity", createControl(parameters,"AmbientOcclusionIntensity"), 0.1f, 3.0f, 0.05f);
        mTweakBar.addValue("Power", createControl(parameters,"AmbientOcclusionPower"), 0.01f, 2.0f, 0.01f);

        NvTweakVarBase hbaoParam;
        hbaoParam = mTweakBar.addValue("WorldRadius", createControl(m_GTAO.shaderParameters, "HBAO_WorldRadius"),0.5f, 5.0f, 0.1f);
        m_HBAOParameters.add(hbaoParam);
        hbaoParam = mTweakBar.addValue("NDotVBiase", createControl(m_GTAO.shaderParameters, "HBAO_NDotVBiase"),-0.7f, 0.7f, 0.05f);
        m_HBAOParameters.add(hbaoParam);
        hbaoParam = mTweakBar.addValue("Multiplier", createControl(m_GTAO.shaderParameters, "HBAO_Multiplier"),0.1f, 5.0f, 0.1f);
        m_HBAOParameters.add(hbaoParam);
    }

    @Override
    protected void initRendering() {
        NvGLSLProgram.ms_ThrowException = false;

        m_Scene = new CubeScene(m_transformer);
        m_Scene.onCreate();

//        GLES32.glDepthRangef(0.0f, 1);



        m_GTAO = new GTAO();
        m_GTAO.Create();

//        m_transformer.setMotionMode(NvCameraMotionType.FIRST_PERSON);
//        m_transformer.setTranslation(0, -4, 0);

//        m_BlitProg = NvGLSLProgram.createFromFiles("shaders/Quad_VS.vert", "labs/GTAO/shaders/VisualTex2DArray.frag");
//        m_VisualNormal = NvGLSLProgram.createFromFiles("shaders/Quad_VS.vert", "labs/GTAO/shaders/VisualNormal.frag");
//
//        m_BlitProg.enable();
//        GLSLUtil.setInt(m_BlitProg, "InputTexture", 0);
//        m_VisualNormal.enable();
//        GLSLUtil.setInt(m_VisualNormal, "TextureNormal", 0);
    }

    @Override
    protected int handleReaction(NvUIReaction react) {
        return super.handleReaction(react);
    }

    @Override
    protected void draw() {
//        if(m_TextureAO == null || )
        m_Scene.draw();

        if(isGTAOEnabled()){
            m_TextureAO = TextureUtils.resizeTexture2D(m_TextureAO, m_Scene.getWidth()/m_Downsample, m_Scene.getHeight()/m_Downsample, GLES30.GL_RGBA8);

            parameters.Projection.load(m_Scene.getProjMat());
            parameters.SceneDepth = m_Scene.getSceneDepth();
            parameters.ResultAO = m_TextureAO;
            parameters.SceneWidth = m_Scene.getSceneDepth().getWidth();
            parameters.SceneHeight = m_Scene.getSceneDepth().getHeight();
            parameters.CameraFar = m_Scene.getSceneFarPlane();
            parameters.CameraNear = m_Scene.getSceneNearPlane();
            parameters.DownscaleFactor = m_Downsample;

            m_GTAO.SetMethod(getAOMethod());
            m_GTAO.RenderAO(parameters);

            GLES.checkGLError();
            if(m_TextureIndex == 0){
                m_Scene.applyAO(m_TextureAO);
            }

            GLES.checkGLError();
        }

        m_Scene.resoveMultisampleTexture();

        /*if(isGTAOEnabled()){
            if(m_TextureIndex == 1){
                blitTexToScreen(m_GTAO.getMobileGTAO(), 0,false, 0);
//                blitTexToScreen(m_Scene.getSceneDepth(), 0,false, 0);
            }else if(m_TextureIndex == 2){
                blitTexToScreen(m_GTAO.getInterleaveDepth(), m_ArraySlice,false, m_Scene.getSceneFarPlane());
            }else  if(m_TextureIndex == 3){
                blitTexToScreen(m_GTAO.getInterleaveAO(), m_ArraySlice,false, 0);
            }
        }*/
    }

    private GTAOMethod getAOMethod(){
        return GTAOMethod.values()[m_GTAOMethod];
    }

    private boolean isGTAOEnabled(){
        return m_GTAOMethod != 0;
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
