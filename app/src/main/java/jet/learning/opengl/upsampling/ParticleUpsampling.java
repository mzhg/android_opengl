package jet.learning.opengl.upsampling;

import android.opengl.GLES20;

import com.nvidia.developer.opengl.app.NvSampleApp;
import com.nvidia.developer.opengl.ui.NvTweakEnumi;
import com.nvidia.developer.opengl.ui.NvUIEventResponse;
import com.nvidia.developer.opengl.ui.NvUIReaction;
import com.nvidia.developer.opengl.utils.VectorUtil;

import org.lwjgl.util.vector.Matrix4f;

import javax.microedition.khronos.opengles.GL11;

/**
 * Created by mazhen'gui on 2018/2/10.
 */

public class ParticleUpsampling extends NvSampleApp {
    private static boolean HIGH_QUALITY = false;
    static int GRID_RESOLUTION;
    static float PARTICLE_SCALE;

    static final float EYE_FOVY_DEG = 40.0f;
    static final float EYE_ZNEAR    = 1.0f;
    static final float EYE_ZFAR     = 100.0f;

    static final float LIGHT_FOVY_DEG   = 40.0f;
    static final float LIGHT_ZNEAR      = 1.0f;
    static final float LIGHT_ZFAR       = 100.0f;

    static final int REACT_UPDATE_SCREEN_BUFFERS = 1;
    static final int REACT_UPDATE_LIGHT_BUFFERS  = 2;

    static final Matrix4f tmp_mat = new Matrix4f();
    static{
        if(HIGH_QUALITY){
            GRID_RESOLUTION = 32;
            PARTICLE_SCALE = 0.5f;
        }else{
            GRID_RESOLUTION = 16;
            PARTICLE_SCALE = 1.0f;
        }
    }

    private SceneRenderer m_sceneRenderer;

    @Override
    protected void initRendering() {
        setTitle("Particle Upsampling Sample");

        m_sceneRenderer = new SceneRenderer(true);
        m_sceneRenderer.reshapeWindow(getWidth(), getHeight());
    }

    @Override
    public void draw() {
        GLES20.glEnable(GL11.GL_DEPTH_TEST);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

//	    matrix4f rotationMatrix, translationMatrix;
//	    nv::rotationY(rotationMatrix, m_transformer->getRotationVec().y);
//	    nv::translation(translationMatrix, 0.f, 0.f, -5.f);
//	    translationMatrix.set_scale(vec3f(-1.0, 1.0, -1.0));
        tmp_mat.setIdentity();
        tmp_mat.m32 = -5.f;  // make the translate
        tmp_mat.m00 = -1.0f; // make x scale
        tmp_mat.m11 = 1.0f;  // make y scale
        tmp_mat.m22 = -1.0f; // make z scale
        tmp_mat.rotate(m_transformer.getRotationVec().y, VectorUtil.UNIT_Y); // rotate Y axis.

        m_sceneRenderer.setEyeViewMatrix(tmp_mat/*translationMatrix * rotationMatrix*/);
        m_sceneRenderer.renderFrame();

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glViewport(0, 0, getWidth(), getHeight());
    }

    @Override
    public void initUI() {
        if (mTweakBar != null)
        {
            mTweakBar.addPadding();
            mTweakBar.addValue("renderShadows", createControl(m_sceneRenderer.getParticleParams(),"renderShadows"));
            mTweakBar.addValue("drawModel", createControl(m_sceneRenderer.getSceneParams(),"drawModel"));
            mTweakBar.addValue("useDepthPrepass", createControl(m_sceneRenderer.getSceneParams(),"useDepthPrepass"));

            mTweakBar.addPadding();
            NvTweakEnumi shadowSliceModes[] = {
                    new NvTweakEnumi("16",  16),
                    new NvTweakEnumi("32",  32),
                    new NvTweakEnumi("64",  64)
            };
            mTweakBar.addEnum("shadowSlices", createControl(m_sceneRenderer.getParticleParams(),"numSlices"), shadowSliceModes, 0);

            mTweakBar.addPadding();
            NvTweakEnumi particleDownsampleModes[] = {
                    new NvTweakEnumi("Full-Res",    1),
                    new NvTweakEnumi("Half-Res",    2),
                    new NvTweakEnumi("Quarter-Res", 4)
            };
            mTweakBar.addEnum("particleDownsample", createControl(m_sceneRenderer.getSceneFBOParams(),"particleDownsample"), particleDownsampleModes, REACT_UPDATE_SCREEN_BUFFERS);

            NvTweakEnumi lightBufferSizeModes[] = {
                    new NvTweakEnumi("64x64",   64),
                    new NvTweakEnumi("128x128",  128),
                    new NvTweakEnumi("256x256",  256)
            };
            mTweakBar.addEnum("lightBufferSize", createControl(m_sceneRenderer.getSceneFBOParams(),"lightBufferSize"), lightBufferSizeModes, REACT_UPDATE_LIGHT_BUFFERS);
        }
    }

    @Override
    public int handleReaction(NvUIReaction react) {
        switch(react.code)
        {
            case REACT_UPDATE_SCREEN_BUFFERS:
                m_sceneRenderer.createScreenBuffers();
                return NvUIEventResponse.nvuiEventHandled;
            case REACT_UPDATE_LIGHT_BUFFERS:
                m_sceneRenderer.createLightBuffer();
                return NvUIEventResponse.nvuiEventHandled;
            default:
                break;
        }
        return NvUIEventResponse.nvuiEventNotHandled;
    }

    @Override
    protected void reshape(int width, int height) {
        if(m_sceneRenderer != null)
            m_sceneRenderer.reshapeWindow(width, height);
    }

}
