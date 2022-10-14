package jet.learning.opengl.gtao;

import android.opengl.GLES30;
import android.opengl.GLES31;

import com.nvidia.developer.opengl.app.NvCameraMotionType;
import com.nvidia.developer.opengl.app.NvSampleApp;
import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.NvGLSLProgram;

import jet.learning.opengl.common.Texture2D;
import jet.learning.opengl.common.TextureUtils;

public class GTAODemo extends NvSampleApp {

    private CubeScene m_Scene;
    private Texture2D m_TextureAO;
    private GTAO m_GTAO;
    private NvGLSLProgram m_BlitProg;

    @Override
    protected void initRendering() {
        m_Scene = new CubeScene(m_transformer);
        m_Scene.onCreate();

        m_GTAO = new GTAO();
        m_GTAO.Create();

        m_transformer.setMotionMode(NvCameraMotionType.FIRST_PERSON);
        m_transformer.setTranslation(0, -4, 0);

//        m_BlitProg = NvGLSLProgram.createProgram("labs/GTAO/shaders/AOCombine.comp", null);
//        m_BlitProg = NvGLSLProgram.createFromFiles("shaders/Quad_VS.vert", "labs/GTAO/shaders/ApplyAO.frag");
    }


    @Override
    protected void draw() {
//        if(m_TextureAO == null || )
        m_Scene.draw();
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
        m_Scene.applyAO(m_TextureAO);

//        m_BlitProg.enable();
//        GLES.glBindTextureUnit(0, m_TextureAO);
        /*GLES31.glBindImageTexture(0, m_Scene.getSceneColor().getTexture(), 0, false, 0, GLES31.GL_READ_WRITE, m_Scene.getSceneColor().getFormat());
        GLES31.glDispatchCompute(m_TextureAO.getWidth() / 16, m_TextureAO.getHeight() / 16, 1);

        GLES.glBindTextureUnit(0, null);
        GLES31.glBindImageTexture(0, 0,0, false, 0, GLES31.GL_WRITE_ONLY, GLES31.GL_RGBA8);

        GLES31.glMemoryBarrier(GLES31.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);*/


        m_Scene.resoveMultisampleTexture();
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
