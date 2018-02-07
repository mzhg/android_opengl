package jet.learning.opengl.optimization;

import org.lwjgl.util.vector.Matrix4f;

/**
 * Created by mazhen'gui on 2018/2/6.
 */

final class MatrixStorage {
    final Matrix4f m_model = new Matrix4f();
    final Matrix4f m_view = new Matrix4f();
    final Matrix4f m_projection = new Matrix4f();

    final Matrix4f m_modelView = new Matrix4f();
    final Matrix4f m_modelViewProjection = new Matrix4f();

    void multiply(){
        Matrix4f.mul(m_view, m_model, m_modelView);
        Matrix4f.mul(m_projection, m_modelView, m_modelViewProjection);
    }

    void set(MatrixStorage o){
        m_model.load(o.m_model);
        m_view.load(o.m_view);
        m_projection.load(o.m_projection);
        m_modelView.load(o.m_modelView);
        m_modelViewProjection.load(o.m_modelViewProjection);
    }

    void setIdentity(){
        m_model.setIdentity();
        m_view.setIdentity();
        m_projection.setIdentity();
        m_modelView.setIdentity();
        m_modelViewProjection.setIdentity();
    }
}
