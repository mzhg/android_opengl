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

import com.nvidia.developer.opengl.utils.NvGLSLProgram;

/**
 * Created by mazhen'gui on 2018/2/6.
 */

final class OpaqueDepthProgram extends NvGLSLProgram {
    int m_positionAttrib   = -1;
    int m_modelViewMatrix  = -1;
    int m_projectionMatrix = -1;

    OpaqueDepthProgram(String fraggy)
    {
        setSourceFromFiles("optimization/unshaded.vert", fraggy);
        m_positionAttrib     = getAttribLocation("g_Position");
    }

    void setUniforms(SceneInfo scene)
    {
        GLES20.glUseProgram(m_program);
    }

    int getPositionAttrib() {  return m_positionAttrib; }
}
