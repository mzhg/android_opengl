package jet.learning.opengl.upsampling;

import com.nvidia.developer.opengl.utils.VectorUtil;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

/**
 * Created by mazhen'gui on 2018/2/10.
 */

final class SceneInfo {
    final Vector3f m_lightVector = new Vector3f();
    final Vector3f m_lightPos = new Vector3f();
    float m_lightDistance;
    final Matrix4f m_lightView = new Matrix4f();
    final Matrix4f m_lightProj = new Matrix4f();
    final Matrix4f m_shadowMatrix = new Matrix4f();
    final Matrix4f m_eyeView = new Matrix4f();
    final Matrix4f m_eyeViewInv = new Matrix4f();
    final Matrix4f m_eyeProj = new Matrix4f();
    final Vector3f m_viewVector = new Vector3f();
    final Vector3f m_halfVector = new Vector3f();
    boolean m_invertedView;
    final Vector4f m_eyePos = new Vector4f();
    final Vector4f m_halfVectorEye = new Vector4f();
    final Vector4f m_lightPosEye = new Vector4f();
    int m_screenWidth;
    int m_screenHeight;
    SceneFBOs m_fbos;

    void setLightVector(Vector3f v)
    {
//             m_lightVector = normalize(v);
        v.normalise(m_lightVector);
    }

    void setLightDistance(float d)
    {
        m_lightDistance = d;
    }

    void setScreenSize(int width, int height)
    {
        m_screenWidth = width;
        m_screenHeight = height;
//            nv::perspective(m_eyeProj, EYE_FOVY_DEG * (NV_PI / 180.0f), (float)width / (float)height, EYE_ZNEAR, EYE_ZFAR);
        VectorUtil.perspective(ParticleUpsampling.EYE_FOVY_DEG, (float)width / (float)height, ParticleUpsampling.EYE_ZNEAR, ParticleUpsampling.EYE_ZFAR, m_eyeProj);
    }

    void calcVectors()
    {
        // calculate half-angle vector between view and light
//            m_viewVector = -vec3f(m_eyeView.get_row(2));
        m_viewVector.x = -m_eyeView.m02;
        m_viewVector.y = -m_eyeView.m12;
        m_viewVector.z = -m_eyeView.m22;
        if (Vector3f.dot(m_viewVector, m_lightVector) > 0)
        {
//                m_halfVector = normalize(m_viewVector + m_lightVector);
            Vector3f.add(m_viewVector, m_lightVector, m_halfVector);
            m_halfVector.normalise();
            m_invertedView = false;
        }
        else
        {
//                m_halfVector = normalize(-m_viewVector + m_lightVector);
            Vector3f.sub(m_lightVector, m_viewVector, m_halfVector);
            m_halfVector.normalise();
            m_invertedView = true;
        }

        // build light matrices
//            m_lightPos = m_lightVector * m_lightDistance;
        m_lightPos.set(m_lightVector);
        m_lightPos.scale(m_lightDistance);

//            m_lightPosEye = m_eyeView * vec4f(m_lightPos, 1.0);
        m_lightPosEye.set(m_lightPos.x, m_lightPos.y, m_lightPos.z, 1.0f);
        Matrix4f.transform(m_eyeView, m_lightPosEye, m_lightPosEye);


//            nv::lookAt(m_lightView, nv::vec3f(m_lightPos[0], m_lightPos[1], m_lightPos[2]), nv::vec3f(0.0, 0.0, 0.0), nv::vec3f(0.0, 1.0, 0.0));
//            nv::perspective(m_lightProj, LIGHT_FOVY_DEG * (NV_PI / 180.0f), 1.0f, LIGHT_ZNEAR, LIGHT_ZFAR);
        VectorUtil.lookAt(m_lightPos, VectorUtil.ZERO3, VectorUtil.UNIT_Y, m_lightView);
        VectorUtil.perspective(ParticleUpsampling.LIGHT_FOVY_DEG, 1.0f, ParticleUpsampling.LIGHT_ZNEAR, ParticleUpsampling.LIGHT_ZFAR, m_lightProj);

        // construct shadow matrix
//            nv::matrix4f scale;
//            scale.set_scale(nv::vec3f(0.5, 0.5, 0.5));
//            nv::matrix4f translate;
//            translate.set_translate(nv::vec3f(0.5, 0.5, 0.5));

        Matrix4f.invert(m_eyeView, m_eyeViewInv);
//            m_shadowMatrix = translate * scale * m_lightProj * m_lightView * inverse(m_eyeView);
        m_shadowMatrix.setIdentity();
        m_shadowMatrix.translate(0.5f, 0.5f, 0.5f);
        m_shadowMatrix.scale(0.5f, 0.5f, 0.5f);
        Matrix4f.mul(m_shadowMatrix, m_lightProj, m_shadowMatrix);
        Matrix4f.mul(m_shadowMatrix, m_lightView, m_shadowMatrix);
        Matrix4f.mul(m_shadowMatrix, m_eyeViewInv, m_shadowMatrix);

        // calc object space eye position
//            m_eyePos = nv::inverse(m_eyeView) * nv::vec4f(0.0, 0.0, 0.0, 1.0);
        m_eyePos.set(0.0f, 0.0f, 0.0f, 1.0f);
        Matrix4f.transform(m_eyeViewInv, m_eyePos, m_eyePos);

        // calc half vector in eye space
//            m_halfVectorEye = m_eyeView * nv::vec4f(m_halfVector, 0.0);
        m_halfVectorEye.set(m_halfVector.x, m_halfVector.y, m_halfVector.z, 0);
        Matrix4f.transform(m_eyeView, m_halfVectorEye, m_halfVectorEye);
    }
}
