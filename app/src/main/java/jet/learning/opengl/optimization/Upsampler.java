////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2014, NVIDIA CORPORATION. All rights reserved.
// Copyright (c) 2018 mzhg
//
// Licensed under the Apache License, Version 2.0 (the "License"); you may not
// use this file except in compliance with the License.  You may obtain a copy
// of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
// License for the specific language governing permissions and limitations
// under the License.
////////////////////////////////////////////////////////////////////////////////
package jet.learning.opengl.optimization;

import android.opengl.GLES20;
import android.opengl.GLES30;

import com.nvidia.developer.opengl.utils.NvDisposeable;
import com.nvidia.developer.opengl.utils.NvShapes;
import com.nvidia.developer.opengl.utils.NvSimpleFBO;
import com.nvidia.developer.opengl.utils.NvWritableFB;

import javax.microedition.khronos.opengles.GL11;

/**
 * Created by mazhen'gui on 2018/2/6.
 */

final class Upsampler implements NvDisposeable {
    final Params m_params = new Params();
    SceneFBOs m_fbos;
    UpsampleBilinearProgram m_upsampleBilinearProg;
    UpsampleCrossBilateralProgram m_upsampleCrossBilateralProg;

    public Upsampler(SceneFBOs fbos) {
        m_fbos = fbos;

        m_upsampleBilinearProg             = new UpsampleBilinearProgram();
        m_upsampleCrossBilateralProg     = new UpsampleCrossBilateralProgram();
    }

    @Override
    public void dispose() {
        m_upsampleBilinearProg.dispose();
        m_upsampleCrossBilateralProg.dispose();
    }

    void drawBilinearUpsampling(UpsampleBilinearProgram prog, int texture)
    {
        prog.enable();
        prog.setTexture(texture);

        NvShapes.drawQuad(prog.getPositionAttrib(), prog.getTexCoordAttrib());
    }

    void drawCrossBilateralUpsampling(UpsampleCrossBilateralProgram prog)
    {
        prog.enable();
        prog.setUniforms(m_fbos, m_params);

        NvShapes.drawQuad(prog.getPositionAttrib(), prog.getTexCoordAttrib());
    }

    void upsampleParticleColors(NvWritableFB target)
    {
        target.bind();

        // composite the particle colors with the scene colors, preserving destination alpha
        // dst.rgb = src.rgb + (1.0 - src.a) * dst.rgb
        GLES20.glBlendFuncSeparate(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ZERO, GL11.GL_ONE);
        GLES20.glEnable(GL11.GL_BLEND);

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

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

    void upsampleSceneColors(NvWritableFB target)
    {
        if (m_params.useBlit /*&& glBlitFramebufferFunc*/)
        {
            final NvSimpleFBO srcFbo = m_fbos.m_sceneFbo;
            GLES20.glBindFramebuffer(GLES30.GL_READ_FRAMEBUFFER, srcFbo.fbo);
            GLES20.glBindFramebuffer(GLES30.GL_DRAW_FRAMEBUFFER, target.fbo);

            GLES30.glBlitFramebuffer(0, 0, srcFbo.width, srcFbo.height,
                    0, 0, target.width, target.height,
                    GL11.GL_COLOR_BUFFER_BIT, GL11.GL_LINEAR);
        }
        else
        {
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, target.fbo);
            GLES20.glViewport(0, 0, target.width, target.height);

            drawBilinearUpsampling(m_upsampleBilinearProg, m_fbos.m_sceneFbo.colorTexture);
        }
    }

    static final class Params{
        float upsamplingDepthMult = 32.0f;
        float upsamplingThreshold = 0.1f;
        boolean useCrossBilateral = true;
        boolean useBlit = true;
    }
}
