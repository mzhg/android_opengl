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

import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.NvDisposeable;
import com.nvidia.developer.opengl.utils.NvSimpleFBO;
import com.nvidia.developer.opengl.utils.NvWritableFB;

import javax.microedition.khronos.opengles.GL11;

/**
 * Created by mazhen'gui on 2018/2/6.
 */

final class SceneFBOs implements NvDisposeable{
    final Params m_params = new Params();
    int m_windowResX;
    int m_windowResY;

    NvSimpleFBO m_particleFbo;
    NvSimpleFBO m_sceneFbo;
    NvWritableFB m_backBufferFbo;

    void setWindowSize(int w, int h)
    {
        m_windowResX = w;
        m_windowResY = h;

        updateParticleBuffer();
        updateSceneBuffer();

        if(m_backBufferFbo != null)
            m_backBufferFbo.dispose();
        m_backBufferFbo = new NvWritableFB(w,h);
    }

    void updateBuffers() {
        updateParticleBuffer();
        updateSceneBuffer();
    }

    void updateParticleBuffer()
    {
        int width  = (int) ((float) m_windowResX
                / (m_params.particleDownsample * m_params.sceneDownsample) + 0.5f);
        int height = (int) ((float) m_windowResY
                / (m_params.particleDownsample * m_params.sceneDownsample) + 0.5f);

        if (m_particleFbo == null || (m_particleFbo.width != width)
                || (m_particleFbo.height != height)) {
            NvSimpleFBO.Desc desc = new NvSimpleFBO.Desc();

            desc.width = width;
            desc.height = height;
            desc.color.format = GL11.GL_RGBA;
            desc.color.type = GL11.GL_UNSIGNED_BYTE;
            desc.color.filter = GL11.GL_NEAREST;
            desc.color.internalFormat = GLES30.GL_RGBA8;

            desc.depth.format = GLES20.GL_DEPTH_COMPONENT;
            desc.depth.type = GLES20.GL_UNSIGNED_SHORT;
            desc.depth.filter = GL11.GL_NEAREST;
            desc.depth.internalFormat = GLES30.GL_DEPTH_COMPONENT16;

            GLES.checkGLError();
            if(m_particleFbo != null)
                m_particleFbo.dispose();
            m_particleFbo = new NvSimpleFBO(desc);
        }
    }

    void updateSceneBuffer()
    {
        int width  = (int) ((float) m_windowResX / m_params.sceneDownsample + 0.5f);
        int height  = (int) ((float) m_windowResY / m_params.sceneDownsample + 0.5f);

        if (m_sceneFbo == null || (m_sceneFbo.width != width)
                || (m_sceneFbo.height != height)) {
            NvSimpleFBO.Desc desc = new NvSimpleFBO.Desc();

            desc.width = width;
            desc.height = height;
            desc.color.format = GL11.GL_RGBA;
            desc.color.type = GL11.GL_UNSIGNED_BYTE;
            desc.color.filter = GL11.GL_NEAREST;
            desc.color.internalFormat = GLES30.GL_RGBA8;

            desc.depth.format = GLES20.GL_DEPTH_COMPONENT;
            desc.depth.type = GLES20.GL_UNSIGNED_SHORT;
            desc.depth.filter = GL11.GL_NEAREST;
            desc.depth.internalFormat = GLES30.GL_DEPTH_COMPONENT16;

            if(m_sceneFbo != null)
                m_sceneFbo.dispose();

            m_sceneFbo = new NvSimpleFBO(desc);
        }
    }

    @Override
    public void dispose() {
        if(m_particleFbo != null)
            m_particleFbo.dispose();
        m_particleFbo = null;

        if(m_sceneFbo != null)
            m_sceneFbo.dispose();
        m_sceneFbo = null;

        if(m_backBufferFbo != null)
            m_backBufferFbo.dispose();
        m_backBufferFbo = null;
    }

    static final class Params{
        float particleDownsample = 2.0f;
        float sceneDownsample = 2.0f;
    }
}
