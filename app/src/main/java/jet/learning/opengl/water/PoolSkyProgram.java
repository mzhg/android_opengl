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

import org.lwjgl.util.vector.Matrix4f;

import jet.learning.opengl.common.SimpleOpenGLProgram;

/**
 * Created by mazhen'gui on 2017/3/21.
 */

public class PoolSkyProgram extends SimpleOpenGLProgram{
    private int attribPos;
    private int mvpIndex = -1;

    private final float[] mat_data = new float[16];

    public void init(){
        NvGLSLProgram program = NvGLSLProgram.createFromFiles("water_resources/poolsky.vert", "water_resources/poolsky.frag");

        program.enable();
        program.setUniform1i("PoolSkyCubeMap", 2);
        program.disable();

        attribPos = program.getAttribLocation("PosAttribute");
        mvpIndex = program.getUniformLocation("g_mvp");
        programID = program.getProgram();
    }

    public void setMVP(Matrix4f mvpMat){
        mvpMat.store(mat_data, 0);
        GLES20.glUniformMatrix4fv(mvpIndex, 1, false, mat_data, 0);
    }

    @Override
    public final int getAttribPosition() { return attribPos;}

    @Override
    public int getAttribTexCoord() {
        return -1;
    }
}
