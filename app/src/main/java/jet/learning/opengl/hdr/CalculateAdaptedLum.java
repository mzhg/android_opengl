////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2014, NVIDIA CORPORATION. All rights reserved.
// Copyright (c) 2017 mzhg
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
package jet.learning.opengl.hdr;

import android.opengl.GLES20;

import com.nvidia.developer.opengl.utils.NvGLSLProgram;

import jet.learning.opengl.common.SimpleOpenGLProgram;

/**
 * Created by mazhen'gui on 2017/3/16.
 */

final class CalculateAdaptedLum extends SimpleOpenGLProgram {

    private int uElapsedTime;
    public void init(){
        NvGLSLProgram program = NvGLSLProgram.createFromFiles("hdr_shaders/quad_es3.vert", "hdr_shaders/CalculateAdaptedLum.frag");

        program.enable();
        program.setUniform1i("currentImage", 0);
        program.setUniform1i("image0", 1);
        program.disable();

        uElapsedTime = program.getUniformLocation("elapsedTime");
        programID = program.getProgram();
    }

    public void applyElapsedTime(float time){ GLES20.glUniform1f(uElapsedTime, time);}
}
