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

final class TonemapProgram extends SimpleOpenGLProgram{
    private int attribPos;
    private int attribTexcoord;

    private int u_blurAmout;
    private int u_exposure;
    private int u_gamma;

    public void init(){
        init(false);
    }

    public void init(boolean low_hdr){
        NvGLSLProgram program = NvGLSLProgram.createFromFiles("hdr_shaders/quad_es3.vert",
                low_hdr ? "hdr_shaders/low_tonemap.frag":"hdr_shaders/tonemapping.frag");

        program.enable();
        program.setUniform1i("sceneTex", 0);
        program.setUniform1i("blurTex", 1);
        program.setUniform1i("lumTex", 2);
        program.disable();

        u_blurAmout  = program.getUniformLocation("blurAmount");
        u_exposure   = program.getUniformLocation("exposure");
        u_gamma      = program.getUniformLocation("gamma");

        attribPos = program.getAttribLocation("PosAttribute");
        attribTexcoord = program.getAttribLocation("TexAttribute");

        programID = program.getProgram();
    }

    public void applyExposure(float exposure){
        if(u_exposure >= 0) GLES20.glUniform1f(u_exposure, exposure);
    }

    public void applyGamma(float gamma){
        if(u_gamma >= 0) GLES20.glUniform1f(u_gamma, gamma);
    }

    public void applyBlurAmout(float blurAmout){
        GLES20.glUniform1f(u_blurAmout, blurAmout);
    }

    public final int getAttribPosition() { return attribPos;}
    public final int getAttribTexCoord() { return attribTexcoord;}
}
