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
package jet.learning.opengl.common;

import android.opengl.GLES20;

import com.nvidia.developer.opengl.utils.GLUtil;
import com.nvidia.developer.opengl.utils.NvGLSLProgram;

import org.lwjgl.util.vector.Matrix4f;

/**
 * Created by mazhen'gui on 2017/10/20.
 */

public class SimpleTextureProgram extends SimpleOpenGLProgram {
    public static final String POSITION_ATTRIB_NAME = "In_Position";
    public static final String TEXTURE_ATTRIB_NAME = "In_TexCoord";
    private int matIdx;

    public SimpleTextureProgram(){
        this(null);
    }

    public SimpleTextureProgram(NvGLSLProgram.LinkerTask task){
        NvGLSLProgram program = new NvGLSLProgram();
        program.setLinkeTask(task);
        program.setSourceFromFiles("shaders/SimpleTextureVS.vert", "shaders/SimpleTexturePS.frag");
        programID = program.getProgram();

        matIdx = program.getUniformLocation("g_Mat");

//        uniform sampler2D uSourceTex;
//        uniform int m_enableFilter;
        program.enable();
        program.setUniform1i("sparrow", 0);
        program.setUniformMatrix4(matIdx, Matrix4f.IDENTITY, false);
        disable();

        findAttrib();
    }

    protected void findAttrib(){
        if(programID == 0)
            throw new IllegalArgumentException("programID is 0.");
        posLoc = GLES20.glGetAttribLocation(programID, POSITION_ATTRIB_NAME);
        texLoc = GLES20.glGetAttribLocation(programID, TEXTURE_ATTRIB_NAME);
    }

    public void setMatrix(Matrix4f mat){
        GLES20.glUniformMatrix4fv(matIdx, 1, false, GLUtil.wrap(mat));
    }
}
