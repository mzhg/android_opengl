package jet.learning.opengl.optimization;

import android.opengl.GLES20;

import com.nvidia.developer.opengl.app.NvCameraMotionType;
import com.nvidia.developer.opengl.app.NvSampleApp;
import com.nvidia.developer.opengl.ui.NvUIFontFamily;
import com.nvidia.developer.opengl.ui.NvUIRect;
import com.nvidia.developer.opengl.ui.NvUIText;
import com.nvidia.developer.opengl.ui.NvUITextAlign;
import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.NvUtils;
import com.nvidia.developer.opengl.utils.VectorUtil;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import javax.microedition.khronos.opengles.GL11;

/**
 * Created by mazhen'gui on 2018/2/6.
 */

public final class OptimizationApp extends NvSampleApp {
    private static boolean HIGH_QUALITY = false;

    // shared static temporal matrix.
    static final Matrix4f tmp_mat0 = new Matrix4f();
    static final float EYE_FOVY_DEG = 45.0f;
    static final float EYE_ZNEAR    = 5.0f;
    static final float EYE_ZFAR     = 1000.0f;

    static final float LIGHT_FOVY_DEG   = 40.0f;
    static final float LIGHT_ZNEAR      = 1.0f;
    static final float LIGHT_ZFAR       = 100.0f;

    static int GRID_RESOLUTION;
    static float PARTICLE_SCALE;

    static{
        if(HIGH_QUALITY){
            GRID_RESOLUTION = 80;
            PARTICLE_SCALE  = 0.5f;
        }else{
            GRID_RESOLUTION = 40;
            PARTICLE_SCALE  = 1.f;
        }
    }

    SceneRenderer m_sceneRenderer;

    boolean m_pausedByPerfHUD;

    final Matrix4f m_projectionMatrix = new Matrix4f();
    final Matrix4f m_viewMatrix = new Matrix4f();

    final Matrix4f m_lightViewMatrix = new Matrix4f();

    final Vector4f m_lightDirection = new Vector4f();
    final Vector3f m_center = new Vector3f();

    NvUIText m_timingStats;

    int gFloatTypeEnum, gLumaTypeEnum;

    public OptimizationApp() {
        setTitle("Optimization Sample");

        m_transformer.setRotationVec(new Vector3f((float)Math.toRadians(11.405f), (float)Math.toRadians(231.978f), 0.0f));
        m_transformer.setTranslation(0.0f, -50.0f, 100.0f);
        m_transformer.setMaxTranslationVel(100.0f);
        m_transformer.setMotionMode(NvCameraMotionType.FIRST_PERSON);
    }

    @Override
    public void initUI() {
        // sample apps automatically have a tweakbar they can use.
        if (mTweakBar != null) { // create our tweak ui
            // Show the app title
            mTweakBar.addLabel("Optimizations Sample App", true);

            mTweakBar.addPadding();
            mTweakBar.addValue("Draw particles:", createControl(m_sceneRenderer.getParticleParams(), "render"));
            mTweakBar.addValue("Use depth pre-pass:", createControl(m_sceneRenderer.getSceneParams(),"useDepthPrepass"));
            mTweakBar.addValue("Render low res scene:", createControl(m_sceneRenderer.getSceneParams(),"renderLowResolution"));
            mTweakBar.addValue("Render low res particles:", createControl(m_sceneRenderer.getParticleParams(),"renderLowResolution"));
            mTweakBar.addValue("Use cross-bilateral upsampling:", createControl(m_sceneRenderer.getUpsamplingParams(),"useCrossBilateral"));
        }

        // UI elements for displaying triangle statistics
        if (mFPSText != null) {
            NvUIRect tr = mFPSText.getScreenRect(); // base off of fps element.
            m_timingStats = new NvUIText("Multi\nLine\nString", NvUIFontFamily.SANS, (mFPSText.getFontSize()*2)/3, NvUITextAlign.RIGHT);
            m_timingStats.setColor(NvUtils.makefourcc(0x30,0xD0,0xD0,0xB0));
            m_timingStats.setShadow();
            mUIWindow.add(m_timingStats, tr.left, tr.top+tr.height+8);
        }
    }

    @Override
    protected void initRendering() {
        GLES20.glClearColor(0.0f, 0.0f, 1.0f, 1.0f);

        gFloatTypeEnum = GL11.GL_FLOAT;
        gLumaTypeEnum = 0x1903; // GL_RED, not declared in ES

        m_sceneRenderer = new SceneRenderer(false);

        GLES20.glEnable(GL11.GL_DEPTH_TEST);
        GLES20.glEnable(GL11.GL_CULL_FACE);
        GLES20.glCullFace(GL11.GL_BACK);
    }

    @Override
    public void draw() {
        int prevFBO = 0;
        // Enum has MANY names based on extension/version
        // but they all map to 0x8CA6
        prevFBO = GLES.glGetInteger(0x8CA6);

        m_sceneRenderer.getSceneFBOParams().particleDownsample =
                m_sceneRenderer.getParticleParams().renderLowResolution ? 2.0f : 1.0f;
        m_sceneRenderer.getSceneFBOParams().sceneDownsample =
                m_sceneRenderer.getSceneParams().renderLowResolution ? 2.0f : 1.0f;

        m_sceneRenderer.updateFrame(getFrameDeltaTime());

        // To maintain correct rendering of the blur we have to detect when we've been paused by PerfHUD.
        // This logic ensures that the time-dependent blur remains when the frame debugger is activated.
        m_pausedByPerfHUD = (getFrameDeltaTime() == 0.0f);

        GLES20.glFlush();

        if (!m_pausedByPerfHUD)
            updateViewDependentParams();

        GLES20.glViewport(0, 0, getWidth(), getHeight());

        GLES20.glClearColor(0.2f, 0.2f, 0.2f, 1.0f);
        GLES20.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GLES20.glEnable(GL11.GL_DEPTH_TEST);

        m_sceneRenderer.renderFrame();

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, prevFBO);
        GLES20.glViewport(0, 0, m_sceneRenderer.getScreenWidth(), m_sceneRenderer.getScreenHeight());
        String buffer = null;
        if ((buffer =m_sceneRenderer.stats()) != null) {
            m_timingStats.setString(buffer);
        }
    }

    @Override
    protected void reshape(int width, int height) {
        VectorUtil.perspective(EYE_FOVY_DEG , (float) width / (float) height, EYE_ZNEAR, EYE_ZFAR, m_projectionMatrix);

        if(m_sceneRenderer == null)
            return;

        m_sceneRenderer.reshapeWindow(getWidth(), getHeight());
        m_sceneRenderer.setProjectionMatrix(m_projectionMatrix);
    }

    private void updateViewDependentParams(){
        Matrix4f.mul(m_transformer.getRotationMat(), m_transformer.getTranslationMat(), m_viewMatrix);
        m_sceneRenderer.setEyeViewMatrix(m_viewMatrix);

        // code for computing light direction and lightViewMatrix
        {
//            static nv::vec3f tempPoint = nv::normalize(nv::vec3f(-1, 1, -1));
            Vector3f tempPoint = new Vector3f(-1, 1, -1);
            tempPoint.normalise();

//            nv::vec3f front     = tempPoint - m_center;
//            nv::vec3f axisRight = front.cross(nv::vec3f(0.0f, 1.0f, 0.0f));
//            nv::vec3f up        = axisRight.cross(front);
            Vector3f front = Vector3f.sub(tempPoint, m_center, null);
            Vector3f tmpVec = new Vector3f(0, 1, 0);
            Vector3f axisRight = Vector3f.cross(front, tmpVec, tmpVec);
            Vector3f up = Vector3f.cross(axisRight, front, axisRight);

//            m_lightViewMatrix = nv::lookAt(m_lightViewMatrix, tempPoint, m_center, up);
            VectorUtil.lookAt(tempPoint, m_center, up, m_lightViewMatrix);
//            m_lightDirection  = nv::normalize<float>(m_viewMatrix * nv::vec4f(tempPoint.x, tempPoint.y, tempPoint.z, 0.0));
            m_lightDirection.set(tempPoint.x, tempPoint.y, tempPoint.z, 0.0f);
            Matrix4f.transform(m_viewMatrix, m_lightDirection, m_lightDirection).normalise();

//            const nv::vec3f tmpDir(m_lightDirection[0], m_lightDirection[1], m_lightDirection[2]);
            tmpVec.set(m_lightDirection);
            m_sceneRenderer.setLightDirection(tmpVec);
        }
    }
}
