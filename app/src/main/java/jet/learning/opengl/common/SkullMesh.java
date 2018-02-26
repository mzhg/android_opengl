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

import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.GLUtil;
import com.nvidia.developer.opengl.utils.Glut;
import com.nvidia.developer.opengl.utils.StackFloat;
import com.nvidia.developer.opengl.utils.StackShort;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.microedition.khronos.opengles.GL11;

/**
 * Created by mazhen'gui on 2017/11/22.
 */

public class SkullMesh implements RenderMesh {
    int mSkullVB;
    int mSkullIB;

    int mSkullIndexCount;

    int mfxPosition;
    int mfxNormal;
    int mfxTex;

    @Override
    public void dispose() {
        GLES.glDeleteBuffers(mSkullVB);  mSkullVB = 0;
        GLES.glDeleteBuffers(mSkullIB);  mSkullIB = 0;
    }

    @Override
    public void initlize(MeshParams params) {
        mfxPosition = params.posAttribLoc;
        mfxNormal = params.norAttribLoc;
        mfxTex = params.texAttribLoc;

        StackFloat vertices = null;
        StackShort indices = null;
        try {
            BufferedReader fin =new BufferedReader(new InputStreamReader(Glut.readFileStream("models/skull.txt")));
            String readLine;
            float[] tmp = new float[6];
            short[] s_tmp = new short[3];

            int vcount = 0;
            int tcount = 0;

            vcount = extractCount(fin.readLine());
            tcount = extractCount(fin.readLine());

            fin.readLine();  // skip
            fin.readLine(); // skip

            vertices = new StackFloat(vcount * 6);
            for(int i = 0; i < vcount; i++){
                readLine = fin.readLine();

                readVertices(readLine, tmp);
                vertices.push(tmp[0]);  // positionX
                vertices.push(tmp[1]);  // positionY
                vertices.push(tmp[2]);  // positionZ

                vertices.push(tmp[3]);  // normalX
                vertices.push(tmp[4]);  // normalY
                vertices.push(tmp[5]);	// normalZ
            }

            fin.readLine();   // skip '}'
            fin.readLine();   // skip 'TriangleList'
            fin.readLine();   // skip '{'
            mSkullIndexCount = 3 * tcount;
            indices = new StackShort(mSkullIndexCount);
            for(int i = 0; i < tcount; i++){
                readLine = fin.readLine();
                readIndices(readLine, s_tmp);
                indices.push(s_tmp[0]);
                indices.push(s_tmp[1]);
                indices.push(s_tmp[2]);
            }

            fin.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mSkullVB = GLES.glGenBuffers();
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mSkullVB);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertices.size() << 2, GLUtil.wrap(vertices.getData(), 0, vertices.size()), GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        mSkullIB = GLES.glGenBuffers();
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mSkullIB);
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indices.size() << 1, GLUtil.wrap(indices.getData(), 0, indices.size()), GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    @Override
    public void draw() {
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mSkullVB);
        GLES20.glVertexAttribPointer(mfxPosition, 3, GL11.GL_FLOAT, false, 6 * 4, 0);
        GLES20.glVertexAttribPointer(mfxNormal, 3, GL11.GL_FLOAT, false, 6 * 4, 3 * 4);
        GLES20.glEnableVertexAttribArray(mfxPosition);
        GLES20.glEnableVertexAttribArray(mfxNormal);

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mSkullIB);

        GLES20.glDrawElements(GL11.GL_TRIANGLES, mSkullIndexCount, GL11.GL_UNSIGNED_SHORT, 0);

        GLES20.glDisableVertexAttribArray(mfxPosition);
        GLES20.glDisableVertexAttribArray(mfxNormal);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    private int extractCount(String line){
        String[] str = line.split(":");
        return Integer.parseInt(str[1].trim());
    }

    private void readVertices(String line, float[] data){
        String[] strs = line.split(" ");
        int len = Math.min(6, data.length);

        for(int i =0; i < len; i++)
            data[i] = Float.parseFloat(strs[i].trim());
    }

    private void readIndices(String line, short[] data){
        String[] strs = line.split(" ");
        int len = Math.min(3, data.length);

        for(int i =0; i < len; i++)
            data[i] = (short) Integer.parseInt(strs[i].trim());
    }
}
