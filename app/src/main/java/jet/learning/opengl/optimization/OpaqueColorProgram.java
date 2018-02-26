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

import org.lwjgl.util.vector.Vector3f;

/**
 * Created by mazhen'gui on 2018/2/6.
 */

final class OpaqueColorProgram extends NvGLSLProgram{
    int m_positionAttrib = -1;
    int m_normalAttrib   = -1;

    OpaqueColorProgram(boolean isES2)
    {
        setSourceFromFiles("optimization/base.vert", "optimization/base.frag");

        m_positionAttrib     = getAttribLocation("g_Position");
        m_normalAttrib       = getAttribLocation("g_Normal");
    }

    void setUniforms(SceneInfo scene)
    {
        GLES20.glUseProgram(m_program);

        Vector3f v = scene.m_lightVector;
        setUniform3f("g_lightDirection", v.x, v.y, v.z);
    }

    int getPositionAttrib() { return m_positionAttrib;  }

    int getNormalAttrib() { return m_normalAttrib;  }
}
