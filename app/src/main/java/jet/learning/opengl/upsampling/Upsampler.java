package jet.learning.opengl.upsampling;

import android.opengl.GLES20;
import android.opengl.GLES30;

import com.nvidia.developer.opengl.utils.NvDisposeable;
import com.nvidia.developer.opengl.utils.NvShapes;
import com.nvidia.developer.opengl.utils.NvSimpleFBO;

import javax.microedition.khronos.opengles.GL11;

/**
 * Created by mazhen'gui on 2018/2/10.
 */

final class Upsampler implements NvDisposeable{
    final Params m_params = new Params();
    SceneFBOs m_fbos;
    UpsampleBilinearProgram m_upsampleBilinearProg;
    UpsampleCrossBilateralProgram m_upsampleCrossBilateralProg;

    public Upsampler(SceneFBOs fbos, boolean isGL) {
        m_fbos = fbos;

        m_params.useBlit = isGL;

        m_upsampleBilinearProg 			= new UpsampleBilinearProgram();
        m_upsampleCrossBilateralProg 	= new UpsampleCrossBilateralProgram();
    }

    void drawQuad(int positionAttrib, int texcoordAttrib)
    {
        NvShapes.drawQuad(positionAttrib, texcoordAttrib);
    }

    void drawBilinearUpsampling(UpsampleBilinearProgram prog, int texture)
    {
        prog.enable();
        prog.setTexture(texture);

        drawQuad(prog.getPositionAttrib(), prog.getTexCoordAttrib());
    }

    void drawCrossBilateralUpsampling(UpsampleCrossBilateralProgram prog)
    {
        prog.enable();
        prog.setUniforms(m_fbos, m_params);

        drawQuad(prog.getPositionAttrib(), prog.getTexCoordAttrib());
    }

    void upsampleParticleColors(SceneInfo scene)
    {
        m_fbos.m_sceneFbo.bind();

        // composite the particle colors with the scene colors, preserving destination alpha
        // dst.rgb = src.rgb + (1.0 - src.a) * dst.rgb
        GLES20.glBlendFuncSeparate(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ZERO, GL11.GL_ONE);
        GLES20.glEnable(GL11.GL_BLEND);

        GLES20.glDisable(GL11.GL_DEPTH_TEST);

        if (m_params.useCrossBilateral)
        {
            drawCrossBilateralUpsampling(m_upsampleCrossBilateralProg);
        }
        else
        {
            drawBilinearUpsampling(m_upsampleBilinearProg, m_fbos.m_particleFbo.colorTexture);
        }

        GLES20.glDisable(GL11.GL_BLEND);
    }

    void upsampleSceneColors(SceneInfo scene)
    {
        if (m_params.useBlit)
        {
            final NvSimpleFBO srcFbo = m_fbos.m_sceneFbo;
            GLES20.glBindFramebuffer(GLES30.GL_READ_FRAMEBUFFER, srcFbo.fbo);
            GLES20.glBindFramebuffer(GLES30.GL_DRAW_FRAMEBUFFER, 0);

            GLES30.glBlitFramebuffer(0, 0, srcFbo.width, srcFbo.height,
                    0, 0, scene.m_screenWidth, scene.m_screenHeight,
                    GL11.GL_COLOR_BUFFER_BIT, GL11.GL_LINEAR);
        }
        else
        {
            GLES20.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
            GLES20.glViewport(0, 0, scene.m_screenWidth, scene.m_screenHeight);

            drawBilinearUpsampling(m_upsampleBilinearProg, m_fbos.m_sceneFbo.colorTexture);
        }
    }

    Params getParams()
    {
        return m_params;
    }

    @Override
    public void dispose() {
        m_upsampleBilinearProg.dispose();
        m_upsampleCrossBilateralProg.dispose();
    }

    static final class Params{
        float upsamplingDepthMult = 32.f;
        float upsamplingThreshold =.1f;
        boolean useCrossBilateral = true;
        boolean useBlit = true;
    }
}
