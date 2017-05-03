package jet.learning.opengl.gui_vr;

import android.graphics.Bitmap;
import android.opengl.GLES10;
import android.opengl.GLES11;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.Glut;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

/**
 * Created by mazhen'gui on 2017/4/14.
 */

public class SpatialUI {
    final Transform m_transform = new Transform();
    private boolean m_dirty;
    private final String textureName;
    private QuadPlaneBoudingBox m_BoundingBox;
    private UIRenderer m_Renderer;
    private final Matrix4f m_ModelMat = new Matrix4f();
    private UIState m_State = UIState.NORMAL;

    // GL resources.
    int m_textureID;

    public SpatialUI(String textureName, UIRenderer renderer){
        this.textureName = textureName;
        m_Renderer = renderer;

        m_BoundingBox = new QuadPlaneBoudingBox();
        m_BoundingBox.setUserData(this);

        m_textureID = GLES.glGenTextures();
        GLES11.glBindTexture(GLES10.GL_TEXTURE_2D, m_textureID);
        Bitmap bitmap = Glut.loadBitmapFromAssets("gui_vr/" + textureName);
        GLUtils.texImage2D(GLES11.GL_TEXTURE_2D, 0, bitmap, 0);
        GLES11.glTexParameteri(GLES11.GL_TEXTURE_2D, GLES11.GL_TEXTURE_MAG_FILTER, GLES11.GL_LINEAR);
        GLES11.glTexParameteri(GLES11.GL_TEXTURE_2D, GLES11.GL_TEXTURE_MIN_FILTER, GLES11.GL_LINEAR);
        GLES11.glTexParameteri(GLES11.GL_TEXTURE_2D, GLES11.GL_TEXTURE_WRAP_S, GLES11.GL_CLAMP_TO_EDGE);
        GLES11.glTexParameteri(GLES11.GL_TEXTURE_2D, GLES11.GL_TEXTURE_WRAP_T, GLES11.GL_CLAMP_TO_EDGE);
    }

    public void draw(){
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES11.glBindTexture(GLES10.GL_TEXTURE_2D, m_textureID);
        SpatialNormalProgram program = m_Renderer.getSpatialNormalProgram();
        program.enable();

        Matrix4f model = getModelMatrix();
        Matrix4f.mul(m_Renderer.getMVP(), model, model);
        program.setMVP(model);
        if(m_State != UIState.NORMAL){
            program.setColor(0.25f, 0.25f, 0.25f, 1);
        }else {
            program.setColor(1, 1, 1, 1);
        }
        m_Renderer.bindQuadVBO(program);
        GLES20.glDrawArrays(GLES11.GL_TRIANGLE_STRIP, 0, 4);
        m_Renderer.unbindQuadVBO(program);
    }

    public String getTextureName(){
        return textureName;
    }

    public void setTransform(Transform transform){
        m_transform.set(transform);
        m_dirty = true;
    }

    public void setState(UIState state){
        m_State = state;
    }

    public void update(float dt){
        updateBoundingBox();

    }

    private void updateBoundingBox(){
        if(!m_dirty)
            return;

        m_dirty = false;
        m_BoundingBox.m_QuadPos[0].set(-1, -1, 0);
        m_BoundingBox.m_QuadPos[1].set(+1, -1, 0);
        m_BoundingBox.m_QuadPos[2].set(+1, +1, 0);
        m_BoundingBox.m_QuadPos[3].set(-1, +1, 0);

        getModelMatrix();
        applyTransform(m_BoundingBox.m_QuadPos[0]);
        applyTransform(m_BoundingBox.m_QuadPos[1]);
        applyTransform(m_BoundingBox.m_QuadPos[2]);
        applyTransform(m_BoundingBox.m_QuadPos[3]);
    }

    private void applyTransform(Vector3f v){
        // Apply scale first
//        v.x *= m_transform.scale.x;
//        v.y *= m_transform.scale.y;
//        v.z *= m_transform.scale.z;
//
//        // Then apply rotation
//        Quaternion.transform(m_transform.rotation, v, v);
//
//        // final apply trnaslation
//        v.x += m_transform.position.x;
//        v.y += m_transform.position.y;
//        v.z += m_transform.position.z;

        Matrix4f.transformVector(m_ModelMat, v, v);
    }

    public Matrix4f getModelMatrix(){
        m_transform.rotation.toMatrix(m_ModelMat);
        m_ModelMat.m30 = m_transform.position.x;
        m_ModelMat.m31 = m_transform.position.y;
        m_ModelMat.m32 = m_transform.position.z;
        m_ModelMat.m03 = 0;
        m_ModelMat.m13 = 0;
        m_ModelMat.m23 = 0;
        m_ModelMat.m33 = 1;

        m_ModelMat.scale(m_transform.scale);
        return m_ModelMat;
    }

    public BoundingBox getBoundingBox(){
        updateBoundingBox();
        return m_BoundingBox;
    }

    public Matrix4f getModelMatrix(Matrix4f out){
        if(out == null)
            out = new Matrix4f();

        m_transform.rotation.toMatrix(out);
        out.m30 = m_transform.position.x;
        out.m31 = m_transform.position.y;
        out.m32 = m_transform.position.z;

        out.scale(m_transform.scale);
        return out;
    }
}
