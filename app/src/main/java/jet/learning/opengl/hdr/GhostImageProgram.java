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

final class GhostImageProgram extends SimpleOpenGLProgram{
    private int attribPos;
    private int attribTexcoord;

    private int u_scalar;
    private int u_colorCoeff;

    public void init(){
        NvGLSLProgram program = NvGLSLProgram.createFromFiles("hdr_shaders/ghostImage.vert", "hdr_shaders/ghostImage.frag");

        program.enable();
        program.setUniform1i("sampler1", 0);
        program.setUniform1i("sampler2", 1);
        program.setUniform1i("sampler3", 2);
        program.setUniform1i("sampler4", 3);
        program.disable();

        attribPos = program.getAttribLocation("PosAttribute");
        attribTexcoord = program.getAttribLocation("TexAttribute");

        u_scalar   = program.getUniformLocation("scalar");
        u_colorCoeff = program.getUniformLocation("colorCoeff");
        programID = program.getProgram();
    }

    public final int getAttribPosition() { return attribPos;}
    public final int getAttribTexCoord() { return attribTexcoord;}
    public final int getSampler1Unit()   { return 0 + GLES20.GL_TEXTURE0;}
    public final int getSampler2Unit()   { return 1 + GLES20.GL_TEXTURE0;}
    public final int getSampler3Unit()   { return 2 + GLES20.GL_TEXTURE0;}
    public final int getSampler4Unit()   { return 3 + GLES20.GL_TEXTURE0;}

    public void applyScalar(float x, float y, float z, float w){
        GLES20.glUniform4f(u_scalar, x, y, z, w);
    }

    public void applyColorCoffs(float[] cof){
        GLES20.glUniform4fv(u_colorCoeff, 4, cof, 0);
    }
}
