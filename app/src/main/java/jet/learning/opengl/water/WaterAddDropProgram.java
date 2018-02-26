////////////////////////////////////////////////////////////////////////////////
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
package jet.learning.opengl.water;

import android.opengl.GLES20;

import com.nvidia.developer.opengl.utils.NvGLSLProgram;

import jet.learning.opengl.common.SimpleOpenGLProgram;

/**
 * Created by mazhen'gui on 2017/3/21.
 */

public class WaterAddDropProgram extends SimpleOpenGLProgram{
    private int attribPos;
    private int attribTexcoord;

    private int dropRadiusLoc = -1;
    private int positionLoc = -1;

    public void init(){
        NvGLSLProgram program = NvGLSLProgram.createFromFiles("hdr_shaders/quad_es3.vert", "water_resources/wateradddrop.frag");

        program.enable();
        program.setUniform1i("WaterHeightMap", 0);
        program.disable();

        attribPos = program.getAttribLocation("PosAttribute");
        attribTexcoord = program.getAttribLocation("TexAttribute");
        programID = program.getProgram();

        dropRadiusLoc = program.getUniformLocation("DropRadius");
        positionLoc = program.getUniformLocation("Position");
    }

    public void setDropRadius(float radius){
        GLES20.glUniform1f(dropRadiusLoc, radius);
    }

    public void setPositon(float x, float y){
        GLES20.glUniform2f(positionLoc, x, y);
    }

    @Override
    public final int getAttribPosition() { return attribPos;}
    @Override
    public final int getAttribTexCoord() { return attribTexcoord;}
}
