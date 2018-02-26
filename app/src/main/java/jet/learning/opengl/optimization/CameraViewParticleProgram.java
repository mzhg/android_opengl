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

import com.nvidia.developer.opengl.utils.GLUtil;
import com.nvidia.developer.opengl.utils.NvGLSLProgram;

import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL11;

/**
 * Created by mazhen'gui on 2018/2/6.
 */

final class CameraViewParticleProgram extends NvGLSLProgram{
    private int m_positionAttrib = -1;
    private int m_depthTex = -1;
    private int m_pointScale = -1;
    private int m_alpha = -1;
    private int m_invViewport = -1;
    private int m_depthConstants = -1;
    private int m_depthDeltaScale = -1;
    private int m_modelViewMatrix = -1;
    private int m_projectionMatrix = -1;

    CameraViewParticleProgram(boolean isES2)
    {
        setSourceFromFiles("optimization/cameraViewParticle2.vert", "optimization/cameraViewParticle2.frag");
        m_positionAttrib     = getAttribLocation("g_positionAndColor");
        m_depthTex            = getUniformLocation("g_depthTex");
        m_pointScale         = getUniformLocation("g_pointScale");
        m_alpha             = getUniformLocation("g_alpha");
        m_modelViewMatrix     = getUniformLocation("g_modelViewMatrix");
        m_projectionMatrix     = getUniformLocation("g_projectionMatrix");

        // soft-particle constants
        m_invViewport        = getUniformLocation("g_invViewport");
        m_depthConstants    = getUniformLocation("g_depthConstants");
        m_depthDeltaScale    = getUniformLocation("g_depthDeltaScale");
    }

    void setUniforms(SceneInfo scene, ParticleRenderer.Params params)
    {
        GLES20.glUseProgram(m_program);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GL11.GL_TEXTURE_2D, scene.m_fbos.m_particleFbo.depthTexture);
        GLES20.glUniform1i(m_depthTex, 1);

        IntBuffer viewport = GLUtil.getCachedIntBuffer(16);
        GLES20.glGetIntegerv(GL11.GL_VIEWPORT, viewport);

        GLES20.glUniform2f(m_invViewport, 1.f / viewport.get(2), 1.f / viewport.get(3));
        GLES20.glUniform2f(m_depthConstants, -1.0f/OptimizationApp.EYE_ZFAR + 1.0f/OptimizationApp.EYE_ZNEAR, -1.0f/OptimizationApp.EYE_ZNEAR);
        GLES20.glUniform1f(m_depthDeltaScale, 1.f / params.softness);

        GLES20.glUniform1f(m_alpha, params.spriteAlpha * params.particleScale);
        GLES20.glUniform1f(m_pointScale, params.getPointScale(scene.m_eyeProj));
        GLES20.glUniformMatrix4fv(m_modelViewMatrix, 1, false, GLUtil.wrap(scene.m_eyeView));
        GLES20.glUniformMatrix4fv(m_projectionMatrix, 1, false, GLUtil.wrap(scene.m_eyeProj));
    }

    int getPositionAndColorAttrib() { return m_positionAttrib; }
}
