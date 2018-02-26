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

import com.nvidia.developer.opengl.utils.GLUtil;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import jet.learning.opengl.common.SimpleOpenGLProgram;

/**
 * Created by mazhen'gui on 2017/3/16.
 */

abstract class BaseProgram extends SimpleOpenGLProgram{

    static final Matrix4f IDENTITY = new Matrix4f();
    int u_mvp;
    int u_modelView;
    int u_eyePos;

    void initVS() {
        u_mvp       = GLES20.glGetUniformLocation(programID, "viewProjMatrix");
        u_modelView = GLES20.glGetUniformLocation(programID, "ModelMatrix");
        u_eyePos    = GLES20.glGetUniformLocation(programID, "eyePos");
    }

    public void applyMVP(Matrix4f mat) {if(u_mvp >= 0) GLES20.glUniformMatrix4fv(u_mvp, 1, false, GLUtil.wrap(mat != null ? mat : IDENTITY));}
    public void applyModelView(Matrix4f mat) {if(u_modelView != -1) GLES20.glUniformMatrix4fv(u_modelView, 1, false, GLUtil.wrap(mat != null ? mat : IDENTITY));}
    public void applyEyePos(Vector3f eyePos){ if(u_eyePos >= 0) GLES20.glUniform3f(u_eyePos, eyePos.x, eyePos.y, eyePos.z);}

    public abstract void applyEmission(float r, float g, float b);
    public abstract void applyColor(float r, float g, float b, float a);
    public abstract int getAttribNormal();
}
