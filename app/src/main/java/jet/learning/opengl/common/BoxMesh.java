/*
 * Copyright 2017 mzhg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jet.learning.opengl.common;

import android.opengl.GLES20;
import android.opengl.GLES30;

import com.nvidia.developer.opengl.utils.BufferUtils;
import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.GLUtil;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL11;

/**
 * Created by mazhen'gui on 2017/11/13.
 */
public class BoxMesh implements RenderMesh {
    int mBoxVB;
    int mBoxIB;
    int mBoxVBO;
    int mBoxIndexCount;
    int mBoxIndexOffset;

    @Override
    public void dispose() {
        GLES.glDeleteBuffers(mBoxIB);
        GLES.glDeleteBuffers(mBoxVB);
        GLES.glDeleteVertexArrays(mBoxVBO);
    }

    @Override
    public void initlize(MeshParams params) {
        MeshData box = new MeshData();

        GeometryGenerator geoGen = new GeometryGenerator();
        geoGen.createBox(1.0f, 1.0f, 1.0f, box);

        // Cache the vertex offsets to each object in the concatenated vertex buffer.
//		mBoxVertexOffset      = 0;

        // Cache the index count of each object.
        mBoxIndexCount      = box.indices.size();
        System.out.println("mBoxIndexCount = " + mBoxIndexCount);

        // Cache the starting index for each object in the concatenated index buffer.
        mBoxIndexOffset      = 0;

        int totalVertexCount = box.vertices.size();

        //
        // Extract the vertex elements we are interested in and pack the
        // vertices of all the meshes into one vertex buffer.
        //
        FloatBuffer vertices = BufferUtils.createFloatBuffer(totalVertexCount * 8);

        for(int i = 0; i < box.vertices.size(); ++i)
        {
            Vertex v = box.vertices.get(i);
            vertices.put(v.positionX).put(v.positionY).put(v.positionZ);  // Position
            vertices.put(v.normalX).put(v.normalY).put(v.normalZ);        // Normal
            vertices.put(v.texCX).put(v.texCY);                           // Texturecoord
        }
        vertices.flip();

        mBoxVB = GLES.glGenBuffers();
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mBoxVB);
        GLES.glBufferData(GLES20.GL_ARRAY_BUFFER, vertices, GLES20.GL_STATIC_DRAW);

        mBoxIB = GLES.glGenBuffers();
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mBoxIB);
        GLES.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, GLUtil.wrap(box.indices.getData(), 0, box.indices.size()), GLES20.GL_STATIC_DRAW);

        final int mfxPosition = params.posAttribLoc;
        final int mfxNormal = params.norAttribLoc;
        final int mfxTex = params.texAttribLoc;

        mBoxVBO = GLES.glGenVertexArrays();
        GLES30.glBindVertexArray(mBoxVBO);
        {
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mBoxVB);
            GLES30.glVertexAttribPointer(mfxPosition, 3, GL11.GL_FLOAT, false, 8 * 4, 0);
            GLES30.glVertexAttribPointer(mfxNormal, 3, GL11.GL_FLOAT, false, 8 * 4, 3 * 4);
            GLES30.glVertexAttribPointer(mfxTex, 2, GL11.GL_FLOAT, false, 8 * 4, 6 * 4);

            GLES30.glEnableVertexAttribArray(mfxPosition);
            GLES30.glEnableVertexAttribArray(mfxNormal);
            GLES30.glEnableVertexAttribArray(mfxTex);

            GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, mBoxIB);
        }
        GLES30.glBindVertexArray(0);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    @Override
    public void draw() {
        GLES30.glBindVertexArray(mBoxVBO);
        GLES30.glDrawElements(GL11.GL_TRIANGLES, mBoxIndexCount, GL11.GL_UNSIGNED_SHORT, mBoxIndexOffset);
        GLES30.glBindVertexArray(0);
    }
}
