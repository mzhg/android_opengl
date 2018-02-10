package jet.learning.opengl.upsampling;

import android.opengl.GLES20;

import com.nvidia.developer.opengl.utils.GLUtil;
import com.nvidia.developer.opengl.utils.NvGLSLProgram;

import org.lwjgl.util.vector.Matrix4f;

/**
 * Created by mazhen'gui on 2018/2/10.
 */
final class LightViewParticleProgram extends NvGLSLProgram {
    private int m_positionAttrib;
    private int m_pointScale;
    private int m_alpha;
    private int m_mvpMatrix;

    public LightViewParticleProgram() {
        setSourceFromFiles("shaders/lightViewParticle.vert", "shaders/lightViewParticle.frag");
        m_positionAttrib 	= getAttribLocation("g_position");
        m_pointScale 		= getUniformLocation("g_pointScale");
        m_alpha 			= getUniformLocation("g_alpha");
        m_mvpMatrix 		= getUniformLocation("g_modelViewProjectionMatrix");
    }

    void setUniforms(SceneInfo scene, ParticleRenderer.Params params)
    {
        GLES20.glUseProgram(m_program);

        GLES20.glUniform1f(m_alpha, params.shadowAlpha * params.particleScale);
        GLES20.glUniform1f(m_pointScale, params.getPointScale(scene.m_lightProj));

//	        const matrix4f mvp = scene.m_lightProj * scene.m_lightView;
        Matrix4f mvp = Matrix4f.mul(scene.m_lightProj, scene.m_lightView, ParticleUpsampling.tmp_mat);
        GLES20.glUniformMatrix4fv(m_mvpMatrix,1, false, GLUtil.wrap(mvp));
    }

    int getPositionAttrib()
    {
        return m_positionAttrib;
    }
}
