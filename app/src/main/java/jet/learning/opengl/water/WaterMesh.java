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
import android.opengl.GLES30;

import com.nvidia.developer.opengl.utils.BufferUtils;
import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.NvShapes;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL11;
import jet.learning.opengl.common.RenderMesh;

/**
 * Created by mazhen'gui on 2017/11/14.
 */
public class WaterMesh implements RenderMesh {
    /** water mesh resolution */
    static final int WMR = 128;
    /** water height map resolution */
    static final int WHMR = 128;
    /** water normal map resolution */
    static final int WNMR = 128;

    private WaterAddDropProgram waterAddDropProgram;
    private WaterHeightmapProgram waterHeightMapProgram;
    private WaterNormalmapProgram waterNormalMapProgram;

    private float lastDropTime;
    private float lastUpdateTime;
    private float currentTime;

    private boolean pause;
    private float dropRadius;

    // Texture Resources
    final int[] waterHeightMaps = new int[2];
    int whmid, waterNormalMap, waterVBO, waterIBO;

    private int mFBO;

    private int mfxPosition;
    private int mfxNormal;
    private int mfxTex;

    private int quadsVerticesCount;

    public WaterMesh(){

        pause = false;

        dropRadius = 4.0f / 128.0f;
    }

    @Override
    public void initlize(MeshParams params) {
        mfxPosition = params.posAttribLoc;
        mfxNormal = params.norAttribLoc;
        mfxTex = params.texAttribLoc;

        waterAddDropProgram = new WaterAddDropProgram();waterAddDropProgram.init();
        waterHeightMapProgram = new WaterHeightmapProgram(); waterHeightMapProgram.init();
        waterNormalMapProgram = new WaterNormalmapProgram(); waterNormalMapProgram.init();

        GLES30.glGenTextures(2, waterHeightMaps, 0);

        for(int i = 0; i < 2; i++)
        {
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, waterHeightMaps[i]);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RG16F, WHMR, WHMR, 0, GLES30.GL_RG, GLES30.GL_FLOAT, null);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
        }
        GLES.checkGLError("COpenGLRenderer::Init waterHeightMaps!");

        // ------------------------------------------------------------------------------------------------------------------------
        waterNormalMap = GLES.glGenTextures();

        ByteBuffer Normals = BufferUtils.createByteBuffer(WNMR * WNMR * 3);
        for(int i = 0; i < WNMR * WNMR; i++)
        {
            Normals.put((byte)0);
            Normals.put((byte)255);
            Normals.put((byte)0);
        }
        Normals.flip();

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, waterNormalMap);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGB8, WNMR, WNMR, 0, GLES30.GL_RGB, GLES30.GL_UNSIGNED_BYTE, Normals);
        GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
        GLES.checkGLError("COpenGLRenderer::Init waterNormalMap!");

        waterVBO = GLES.glGenBuffers();
        int WMRP1 = WMR + 1;
        FloatBuffer Vertices = BufferUtils.createFloatBuffer(WMRP1 * WMRP1 * 5);

        float WMSDWMR = 2.0f / WMR;

        for(int y = 0; y < WMRP1; y++) {
            for(int x = 0; x < WMRP1; x++) {
                Vertices.put(x * WMSDWMR - 1.0f);
                Vertices.put(0.0f);
                Vertices.put(1.0f - y * WMSDWMR);

                float u = (float)x / WMR;
                float v = (float)y / WMR;
                Vertices.put(u).put(v);   // texture coords
            }
        }

        Vertices.flip();

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, waterVBO);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, Vertices.limit() * 4, Vertices, GLES30.GL_STATIC_DRAW);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
        GLES.checkGLError("COpenGLRenderer::Init waterVBO!");
        ShortBuffer indices = BufferUtils.createShortBuffer(WMR * WMR * 6);
        for(int y = 0; y < WMR; y++)
        {
            int yp1 = y + 1;

            for(int x = 0; x < WMR; x++)
            {
                int xp1 = x + 1;

                short a = (short)(WMRP1 * y + x);
                short b = (short)(WMRP1 * y + xp1);
                short c = (short)(WMRP1 * yp1 + xp1);
                short d = (short)(WMRP1 * yp1 + x);
                indices.put(a).put(b).put(c);
                indices.put(a).put(c).put(d);
            }
        }
        indices.flip();
        quadsVerticesCount = WMR * WMR * 6;
        waterIBO = GLES.glGenBuffers();
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, waterIBO);
        GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER, indices.limit() * 2, indices, GLES30.GL_STATIC_DRAW);
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0);

        // ------------------------------------------------------------------------------------------------------------------------
        GLES.checkGLError("COpenGLRenderer::Init waterIBO!");
        mFBO = GLES.glGenFramebuffers();
    }

    public void update(float deltaTime){
        // add drops --------------------------------------------------------------------------------------------------------------
        currentTime += deltaTime;
        if(!pause)
        {
            if(currentTime - lastDropTime > 1f)
            {
                lastDropTime = currentTime;

                addDrop(2.0f * (float)Math.random() - 1.0f, 1.0f - 2.0f * (float)Math.random(), 4.0f / 128.0f * (float)Math.random());
            }
        }

        // update water surface ---------------------------------------------------------------------------------------------------
        if(currentTime - lastUpdateTime >= 16f/1000f)
        {
            lastUpdateTime = currentTime;

            // update water height map --------------------------------------------------------------------------------------------
            GLES30.glViewport(0, 0, WHMR, WHMR);

            int whmid = (this.whmid + 1) % 2;

            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, mFBO);
            GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, waterHeightMaps[whmid], 0);
            GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_ATTACHMENT, GLES30.GL_TEXTURE_2D, 0, 0);

            GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, waterHeightMaps[this.whmid]);
            waterHeightMapProgram.enable();

            NvShapes.drawQuad(waterHeightMapProgram.getAttribPosition(), waterHeightMapProgram.getAttribTexCoord());
            waterHeightMapProgram.disable();
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);

            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);

            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, waterHeightMaps[whmid]);
            GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);

            ++this.whmid;
            this.whmid %= 2;

            // update water normal map --------------------------------------------------------------------------------------------
            GLES30.glViewport(0, 0, WNMR, WNMR);
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, mFBO);
            GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, waterNormalMap, 0);
            GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_ATTACHMENT, GLES30.GL_TEXTURE_2D, 0, 0);

            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, waterHeightMaps[this.whmid]);
            waterNormalMapProgram.enable();

            NvShapes.drawQuad(waterNormalMapProgram.getAttribPosition(), waterNormalMapProgram.getAttribTexCoord());
            waterNormalMapProgram.disable();
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);

            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);

            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, waterNormalMap);
            GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
        }
    }

    public int getWaterHeightMap() { return waterHeightMaps[this.whmid];}
    public int getWaterNormalMap() { return waterNormalMap;}

    @Override
    public void draw() {
        final int stride = 5 * 4;
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, waterVBO);
        GLES30.glEnableVertexAttribArray(mfxPosition);
        GLES20.glVertexAttribPointer(mfxPosition, 3, GL11.GL_FLOAT, false, stride, 0);

        GLES30.glEnableVertexAttribArray(mfxTex);
        GLES20.glVertexAttribPointer(mfxTex, 2, GL11.GL_FLOAT, false, stride, 3 * 4);

        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, waterIBO);
        GLES20.glDrawElements(GL11.GL_TRIANGLES, quadsVerticesCount, GLES20.GL_UNSIGNED_SHORT, 0);
        GLES20.glDisableVertexAttribArray(mfxPosition);
        GLES20.glDisableVertexAttribArray(mfxTex);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public void addDrop(float x, float y, float dropRadius){
        if(x >= -1.0f && x <= 1.0f && y >= -1.0f && y <= 1.0f) {
            GLES30.glViewport(0, 0, WMR, WMR);

            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, mFBO);
            GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, waterHeightMaps[(whmid + 1) % 2], 0);
            GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_ATTACHMENT, GLES30.GL_TEXTURE_2D, 0, 0);

            GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, waterHeightMaps[whmid]);
            waterAddDropProgram.enable();
            waterAddDropProgram.setDropRadius(dropRadius);
            waterAddDropProgram.setPositon(x * 0.5f + 0.5f, 0.5f - y * 0.5f);
            NvShapes.drawQuad(waterAddDropProgram.getAttribPosition(), waterAddDropProgram.getAttribTexCoord());
            GLES30.glUseProgram(0);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);

            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);

            ++whmid;
            whmid %= 2;
        }
    }

    @Override
    public void dispose() {
        waterAddDropProgram.dispose();
        waterHeightMapProgram.dispose();
        waterNormalMapProgram.dispose();

        GLES.glDeleteTextures(waterNormalMap);
        GLES20.glDeleteTextures(2, waterHeightMaps, 0);
        GLES.glDeleteBuffers(waterVBO);
        GLES.glDeleteBuffers(waterIBO);
        GLES.glDeleteFramebuffers(mFBO);
    }
}
