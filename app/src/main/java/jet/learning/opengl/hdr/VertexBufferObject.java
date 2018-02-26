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
import android.opengl.GLES30;

import com.nvidia.developer.opengl.utils.GLES;

import java.nio.Buffer;

/**
 * Created by mazhen'gui on 2017/3/17.
 */

final class VertexBufferObject {
    private int m_vboId;
    private int m_iboId;

    void genVertexData(Buffer data, int length, boolean stream){
        m_vboId = GLES.glGenBuffers();
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, m_vboId);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, length, data, stream?GLES30.GL_STREAM_DRAW:GLES30.GL_STATIC_DRAW);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
    }

    void genIndexData(Buffer data, int length, boolean stream){
        m_iboId = GLES.glGenBuffers();
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, m_iboId);
        GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER, length, data, stream?GLES30.GL_STREAM_DRAW:GLES30.GL_STATIC_DRAW);
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    void addAttribute(int attribute, int count, int type, boolean noramlize, int stride, int offset){
        GLES30.glEnableVertexAttribArray(attribute);
        GLES30.glVertexAttribPointer(attribute, count, type, noramlize, stride, offset);
    }

    void draw(int type, int count, int data_type, int offset){
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, m_vboId);
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, m_iboId);
        GLES30.glDrawElements(type, count, data_type, offset);
    }

    int getVBO() { return m_vboId; }
    int getIBO() { return m_iboId; }
    void dispose(){
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        GLES.glDeleteBuffers(m_vboId);
        GLES.glDeleteBuffers(m_iboId);
    }
}
