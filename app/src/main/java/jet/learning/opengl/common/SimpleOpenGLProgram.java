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

/**
 * Created by mazhen'gui on 2017/3/16.
 */

public class SimpleOpenGLProgram {
    protected int posLoc = 0;
    protected int texLoc = 1;

    protected int programID;

    public int getProgram() { return programID;}

    public void enable() { GLES20.glUseProgram(programID);}
    public void disable() {GLES20.glUseProgram(0);}

    public void dispose() {
        if(programID != 0){
            GLES20.glDeleteProgram(programID);
            programID = 0;
        }
    }

    protected void findAttrib(){
        if(programID == 0)
            throw new IllegalArgumentException("programID is 0.");
        posLoc = GLES20.glGetAttribLocation(programID, "PosAttribute");
        texLoc = GLES20.glGetAttribLocation(programID, "TexAttribute");
    }

    public int getAttribPosition() { return posLoc;}
    public int getAttribTexCoord() { return texLoc;}
}
