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

import android.opengl.GLES30;

import com.nvidia.developer.opengl.utils.NvAssetLoader;
import com.nvidia.developer.opengl.utils.NvGLSLProgram;

/**
 * Created by mazhen'gui on 2017/3/16.
 */

final class ReflectProgram extends  BaseProgram{
    private int u_emission;
    private int u_color;

    private int attribPos;
    private int attribNormal;

    public ReflectProgram() {
        CharSequence vert = NvAssetLoader.readText("hdr_shaders/matteObject.vert");
        CharSequence frag = NvAssetLoader.readText("hdr_shaders/reflectObject.frag");

        NvGLSLProgram program = new NvGLSLProgram();
        program.setSourceFromStrings(vert, frag, true);
        programID = program.getProgram();
        initVS();

        u_emission = program.getUniformLocation("emission");
        u_color    = program.getUniformLocation("color");

        int envMap      = program.getUniformLocation("envMap");
        int envMapRough = program.getUniformLocation("envMapRough");

        attribPos = program.getAttribLocation("PosAttribute");
        attribNormal = program.getAttribLocation("myNormal");

        GLES30.glUseProgram(programID);
        GLES30.glUniform1i(envMap, 0);
        GLES30.glUniform1i(envMapRough, 1);
        GLES30.glUseProgram(0);
    }

    public void applyEmission(float r, float g, float b){	GLES30.glUniform3f(u_emission, r, g, b);}
    public void applyColor(float r, float g, float b, float a){	GLES30.glUniform4f(u_color, r, g, b, a);}

    @Override
    public int getAttribPosition() { return attribPos; }
    @Override
    public int getAttribNormal() {return attribNormal;}
}
