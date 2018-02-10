package jet.learning.opengl.upsampling;

import android.opengl.GLES20;

import com.nvidia.developer.opengl.utils.NvGLSLProgram;

import javax.microedition.khronos.opengles.GL11;

/**
 * Created by mazhen'gui on 2018/2/6.
 */

final class UpsampleBilinearProgram extends NvGLSLProgram {
    private int m_positionAttrib = -1;
    private int m_texCoordAttrib = -1;
    private int m_texture        = -1;

    UpsampleBilinearProgram()
    {
        setSourceFromFiles("optimization/upsampleBilinear.vert", "optimization/upsampleBilinear.frag");
        m_positionAttrib = getAttribLocation("g_position");
        m_texCoordAttrib = getAttribLocation("g_texCoords");
        m_texture        = getUniformLocation("g_texture");
    }

    void setTexture(int texture)
    {
        GLES20.glUseProgram(m_program);
        GLES20.glUniform1i(m_texture, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
    }

    int getPositionAttrib() {  return m_positionAttrib; }

    int getTexCoordAttrib() { return m_texCoordAttrib; }
}
