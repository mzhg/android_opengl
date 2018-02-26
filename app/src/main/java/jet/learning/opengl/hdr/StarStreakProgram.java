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

final class StarStreakProgram extends SimpleOpenGLProgram{
    private int attribPos;
    private int attribTexcoord;

    private int u_stepSize;
    private int u_stride;
    private int u_colorCoeff;

    public void init(){
        NvGLSLProgram program = NvGLSLProgram.createFromFiles("hdr_shaders/starStreak.vert", "hdr_shaders/starStreak.frag");

        program.enable();
        program.setUniform1i("sampler", 0);
        program.disable();

        attribPos = program.getAttribLocation("PosAttribute");
        attribTexcoord = program.getAttribLocation("TexAttribute");

        u_stepSize = program.getUniformLocation("stepSize");
        u_stride   = program.getUniformLocation("Stride");
        u_colorCoeff = program.getUniformLocation("colorCoeff");
        programID = program.getProgram();
    }

    public final int getAttribPosition() { return attribPos;}
    public final int getAttribTexCoord() { return attribTexcoord;}

    public void applyStepSize(float stepX, float stepY){
        GLES20.glUniform2f(u_stepSize, stepX, stepY);
    }

    public void applyStride(float stride){
        GLES20.glUniform1f(u_stride, stride);
    }

    public void applyColorCoffs(float[] cof){
        GLES20.glUniform4fv(u_colorCoeff, 4, cof, 0);
    }
}
