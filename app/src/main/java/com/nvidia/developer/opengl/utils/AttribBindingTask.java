////////////////////////////////////////////////////////////////////////////////
// Copyright 2017 mzhg
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
package com.nvidia.developer.opengl.utils;

import android.opengl.GLES20;

/**
 * Created by mazhen'gui on 2017/10/20.
 */

public class AttribBindingTask implements NvGLSLProgram.LinkerTask {
    private AttribBinder[] binders;

    public AttribBindingTask(AttribBinder...binders){
        this.binders = binders;
    }

    @Override
    public void invoke(int programID) {
        int length = binders != null ? binders.length : 0;
        for(int i = 0; i < length; i++){
            if(binders[i] != null){
                GLES20.glBindAttribLocation(programID, binders[i].getLocation(), binders[i].getName());
            }
        }
    }
}
