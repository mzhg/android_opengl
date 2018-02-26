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
final class GlareComposeProgram extends SimpleOpenGLProgram{
    private int attribPos;
    private int attribTexcoord;

    private int u_mixCoeff;

    public void init(){
        NvGLSLProgram program = NvGLSLProgram.createFromFiles("hdr_shaders/quad_es3.vert", "hdr_shaders/glareCompose.frag");

        program.enable();
        program.setUniform1i("sampler1", 0);
        program.setUniform1i("sampler2", 1);
        program.setUniform1i("sampler3", 2);
        program.setUniform1i("sampler4", 3);
        program.disable();

        attribPos = program.getAttribLocation("PosAttribute");
        attribTexcoord = program.getAttribLocation("TexAttribute");

        u_mixCoeff   = program.getUniformLocation("mixCoeff");
        programID = program.getProgram();
    }

    public final int getAttribPosition() { return attribPos;}
    public final int getAttribTexCoord() { return attribTexcoord;}
    public final int getSampler1Unit()   { return 0+GLES20.GL_TEXTURE0;}
    public final int getSampler2Unit()   { return 1+GLES20.GL_TEXTURE0;}
    public final int getSampler3Unit()   { return 2+GLES20.GL_TEXTURE0;}
    public final int getSampler4Unit()   { return 3+GLES20.GL_TEXTURE0;}

    public void applyMixCoeff(float x, float y, float z, float w){
        GLES20.glUniform4f(u_mixCoeff, x, y, z, w);
    }
}
