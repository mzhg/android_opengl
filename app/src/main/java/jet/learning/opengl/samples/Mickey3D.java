package jet.learning.opengl.samples;

import android.graphics.Bitmap;
import android.opengl.GLES11;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.antvr.antvrsdk.AntvrSDK;
import com.nvidia.developer.opengl.app.NvSampleApp;
import com.nvidia.developer.opengl.utils.AttribBinder;
import com.nvidia.developer.opengl.utils.AttribBindingTask;
import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.Glut;
import com.nvidia.developer.opengl.utils.NvGLModel;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import javax.microedition.khronos.opengles.GL11;

import jet.learning.opengl.common.SimpleTextureProgram;

/**
 * Created by mazhen'gui on 2017/11/10.
 */

public final class Mickey3D extends NvSampleApp {

    private static final float MICKEY_SCALE = 0.36f;
    private static final float EYE_DISTANCE = 0.125f;

    private NvGLModel m_Mickey;
    private int m_texture;
    private final Matrix4f m_view = new Matrix4f();
    private final Matrix4f m_proj = new Matrix4f();
    private final Matrix4f m_model = new Matrix4f();
    private final Matrix4f m_combined = new Matrix4f();
    private final Vector3f m_position = new Vector3f();
    private final Vector3f m_xais = new Vector3f();
    private final Vector3f m_yais = new Vector3f();
    private final Vector3f m_zais = new Vector3f();

    private SimpleTextureProgram m_program;
    private boolean m_left;
    private boolean m_pause = false;
    private float m_time;

    private AntvrSDK mAntvrSDK;//for gyro data

    @Override
    protected void initBeforeGL() {
        mAntvrSDK = AntvrSDK.getInstance(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mAntvrSDK == null){
//            Log.e(TAG,"mAntvrSDK is null");
        }
        mAntvrSDK.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(mAntvrSDK != null){
            mAntvrSDK.onPause();
        }
    }

    @Override
    protected void initRendering() {
        m_Mickey = new NvGLModel();
        m_Mickey.loadModelFromFile("models/Mickey_Mouse.obj");
        m_Mickey.initBuffers();

        m_program = new SimpleTextureProgram(new AttribBindingTask(
                new AttribBinder(SimpleTextureProgram.POSITION_ATTRIB_NAME, 0),
                new AttribBinder(SimpleTextureProgram.TEXTURE_ATTRIB_NAME, 1)));

        m_transformer.setTranslation(0,0, -1.4f);
        GLES.checkGLError();

        m_texture = GLES.glGenTextures();
        GLES11.glBindTexture(GLES11.GL_TEXTURE_2D, m_texture);
        Bitmap bitmap = Glut.loadBitmapFromAssets("textures/longmao.jpg");
        GLUtils.texImage2D(GLES11.GL_TEXTURE_2D, 0, bitmap, 0);
        GLES11.glTexParameteri(GLES11.GL_TEXTURE_2D, GLES11.GL_TEXTURE_MAG_FILTER, GLES11.GL_LINEAR);
        GLES11.glTexParameteri(GLES11.GL_TEXTURE_2D, GLES11.GL_TEXTURE_MIN_FILTER, GLES11.GL_LINEAR);
        GLES11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GLES11.GL_REPEAT);
        GLES11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GLES11.GL_REPEAT);
        bitmap.recycle();
    }

    @Override
    public void initUI() {
        mTweakBar.addValue("Animation", createControl("m_pause"));
    }

    @Override
    protected void draw() {
        GLES20.glClearDepthf(1.0f);
        GLES20.glClearColor(0.29f,0.29f,0.29f,0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT|GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES.checkGLError();

        Vector3f center = m_Mickey.m_center;
        if(!m_pause){
            m_time = getTotalTime();
        }

        float[] rotationMatrix = mAntvrSDK.getRotationMatrix();
        m_view.load(rotationMatrix, 0);

        m_model.setIdentity();
        m_model.scale(MICKEY_SCALE);
        m_model.translate(-center.x, -center.y, -center.z);

        Matrix4f.mul(m_view, m_model, m_model);

//        m_model.rotate(m_time * NvUtils.PI * 2.0f, Vector3f.Y_AXIS);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, m_texture);

        m_transformer.getModelViewMat(m_view);

        decodeViewMat(m_view);

        m_program.enable();GLES.checkGLError();
        m_program.setMatrix(m_left ? buildLeftEyeMvp() : buildRightEyeMvp());
        m_Mickey.drawElements(m_program.getAttribPosition(), m_program.getAttribTexCoord());
        GLES.checkGLError();

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

//        m_program.setMatrix(buildRightEyeMvp());
//        m_Mickey.drawElements(m_program.getAttribPosition());
        if(!m_pause)
            m_left=!m_left;
    }

    @Override
    protected void reshape(int width, int height) {
        Matrix4f.perspective(60, (float)width/height, 0.1f, 100.0f, m_proj);
    }

    private void decodeViewMat(Matrix4f view){
        Matrix4f.invert(view, m_combined);
        m_position.set(m_combined.m30, m_combined.m31, m_combined.m32);
        m_xais.set(m_combined.m00, m_combined.m01, m_combined.m02);
        m_yais.set(m_combined.m10, m_combined.m11, m_combined.m12);
        m_zais.set(m_combined.m20, m_combined.m21, m_combined.m22);
        m_zais.scale(-1);
    }

    private Matrix4f buildLeftEyeMvp(){
        Vector3f new_pos = Vector3f.linear(m_position, m_xais, -EYE_DISTANCE, null);
        Vector3f new_target = Vector3f.add(new_pos, m_zais, null);

        Matrix4f.lookAt(new_pos, new_target, m_yais, m_view);
        Matrix4f.mul(m_view, m_model, m_combined);
        Matrix4f.mul(m_proj, m_combined, m_combined);

        return m_combined;
    }

    private Matrix4f buildRightEyeMvp(){
        Vector3f new_pos = Vector3f.linear(m_position, m_xais, EYE_DISTANCE, null);
        Vector3f new_target = Vector3f.add(new_pos, m_zais, null);

        Matrix4f.lookAt(new_pos, new_target, m_yais, m_view);
        Matrix4f.mul(m_view, m_model, m_combined);
        Matrix4f.mul(m_proj, m_combined, m_combined);

        return m_combined;
    }
}
