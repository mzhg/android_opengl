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

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.util.Log;

import com.nvidia.developer.opengl.app.NvInputTransformer;
import com.nvidia.developer.opengl.utils.BufferUtils;
import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.Glut;
import com.nvidia.developer.opengl.utils.NvShapes;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.ReadableVector3f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL11;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES30.GL_RGB8;
import static com.nvidia.developer.opengl.utils.GLES.glGenBuffers;
import static com.nvidia.developer.opengl.utils.GLES.glGenFramebuffers;
import static com.nvidia.developer.opengl.utils.GLES.glGenTextures;

/**
 * Created by mazhen'gui on 2017/3/21.
 */

final class COpenGLRenderer {
    /** water mesh resolution */
    static final int WMR = 128;
    /** water height map resolution */
    static final int WHMR = 128;
    /** water normal map resolution */
    static final int WNMR = 128;

    int width, height;
    final Matrix4f viewMatrix = new Matrix4f();
    final Matrix4f projectionMatrix = new Matrix4f();
    final Matrix4f projectionBiasMatrixInverse = new Matrix4f();

    final Vector3f cameraPos = new Vector3f();

    int poolSkyCubeMap;
    final int[] waterHeightMaps = new int[2];
    int whmid, waterNormalMap, poolSkyVBO, waterVBO, waterIBO, fbo;
//    NvGLSLProgram waterAddDropProgram, waterHeightMapProgram, waterNormalMapProgram, poolSkyProgram, waterProgram;
    int quadsVerticesCount;
    WaterAddDropProgram waterAddDropProgram;
    WaterHeightmapProgram waterHeightMapProgram;
    WaterNormalmapProgram waterNormalMapProgram;
    PoolSkyProgram poolSkyProgram;
    WaterProgram waterProgram;

    boolean wireFrame, pause;
    float dropRadius;

    COpenGLRenderer(){
        whmid = 0;

        wireFrame = false;
        pause = false;

        dropRadius = 4.0f / 128.0f;
    }

    boolean Init(){
        String poolSkyCubeMapFileNames[] = {"water_resources/right.jpg", "water_resources/left.jpg", "water_resources/bottom.jpg",
                                            "water_resources/top.jpg", "water_resources/front.jpg", "water_resources/back.jpg"};
        poolSkyCubeMap = createCubemapTexture(poolSkyCubeMapFileNames);

        waterAddDropProgram = new WaterAddDropProgram();waterAddDropProgram.init();
        waterHeightMapProgram = new WaterHeightmapProgram(); waterHeightMapProgram.init();
        waterNormalMapProgram = new WaterNormalmapProgram(); waterNormalMapProgram.init();
        poolSkyProgram = new PoolSkyProgram(); poolSkyProgram.init();
        waterProgram = new WaterProgram();  waterProgram.init();

        float[] LightPosition = {0.0f, 5.5f, -9.5f};

        float[] CubeMapNormals = {
                -1.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f,
                0.0f, -1.0f, 0.0f,
                0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, -1.0f,
                0.0f, 0.0f, 1.0f,
        };

        // ------------------------------------------------------------------------------------------------------------------------
        waterProgram.enable();
        waterProgram.setCubemapNormals(CubeMapNormals);
        waterProgram.setLightPosition(0.0f, 5.5f, -9.5f);
        waterProgram.disable();
        GLES.checkGLError("COpenGLRenderer::Init shader done!");
        // ------------------------------------------------------------------------------------------------------------------------

        GLES30.glGenTextures(2, waterHeightMaps, 0);
        for(int i = 0; i < 2; i++)
        {
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, waterHeightMaps[i]);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
//            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, 4);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RG16F, WHMR, WHMR, 0, GLES30.GL_RG, GLES30.GL_FLOAT, null);
            GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
        }
        GLES.checkGLError("COpenGLRenderer::Init waterHeightMaps!");
        // ------------------------------------------------------------------------------------------------------------------------

        waterNormalMap = glGenTextures();

        ByteBuffer Normals = BufferUtils.createByteBuffer(WNMR * WNMR * 3);
        for(int i = 0; i < WNMR * WNMR; i++)
        {
//            Normals[i] = vec4(0.0f, 1.0f, 0.0f, 1.0f);
            Normals.put((byte)0);
            Normals.put((byte)255);
            Normals.put((byte)0);
        }
        Normals.flip();

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, waterNormalMap);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
//        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, 4);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GL_RGB8, WNMR, WNMR, 0, GLES30.GL_RGB, GLES30.GL_UNSIGNED_BYTE, Normals);
        GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
        GLES.checkGLError("COpenGLRenderer::Init waterNormalMap!");
        // ------------------------------------------------------------------------------------------------------------------------

        waterVBO = glGenBuffers();

        int WMRP1 = WMR + 1;

        FloatBuffer Vertices = BufferUtils.createFloatBuffer(WMRP1 * WMRP1 * 3);

        float WMSDWMR = 2.0f / (float)WMR;

        for(int y = 0; y < WMRP1; y++)
        {
            for(int x = 0; x < WMRP1; x++)
            {
                Vertices.put(x * WMSDWMR - 1.0f);
                Vertices.put(0.0f);
                Vertices.put(1.0f - y * WMSDWMR);
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

//                if(a < 0 || b < 0 || c < 0 || d <0)
//                    throw new RuntimeException();

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

        fbo = glGenFramebuffers();
        GLES.checkGLError("COpenGLRenderer::Init waterIBO!");

        return true;
    }

    private float lastDropTime;
    private float lastUpdateTime;
    private float currentTime;
    void Render(NvInputTransformer transformer, float FrameTime){
        // add drops --------------------------------------------------------------------------------------------------------------
        currentTime += FrameTime;
        if(!pause)
        {
            if(currentTime - lastDropTime > 1f)
            {
                lastDropTime = currentTime;

                AddDrop(2.0f * (float)Math.random() - 1.0f, 1.0f - 2.0f * (float)Math.random(), 4.0f / 128.0f * (float)Math.random());
            }
        }

//        transformer.getModelViewMat(viewMatrix);
//        cameraPos.set(transformer.getTranslationVec());
//        cameraPos.scale(-1);
        Matrix4f.lookAt(0.0f, 1.0f, 2.5f, 0.0f, -0.5f, 0.0f, 0, 1, 0, viewMatrix);
        cameraPos.set(0.0f, 1.0f, 2.5f);
        Matrix4f vp = Matrix4f.mul(projectionMatrix, viewMatrix, projectionBiasMatrixInverse);

        // update water surface ---------------------------------------------------------------------------------------------------
        if(currentTime - lastUpdateTime >= 16f/1000f)
        {
            lastUpdateTime = currentTime;

            // update water height map --------------------------------------------------------------------------------------------
            GLES30.glViewport(0, 0, WHMR, WHMR);

            int whmid = (this.whmid + 1) % 2;

            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fbo);
            GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, waterHeightMaps[whmid], 0);
            GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_ATTACHMENT, GLES30.GL_TEXTURE_2D, 0, 0);

            GLES30.glActiveTexture(GL_TEXTURE0);
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

            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fbo);
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

        // render pool sky mesh ---------------------------------------------------------------------------------------------------
        GLES30.glViewport(0, 0, width, height);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

        GLES30.glDisable(GLES30.GL_DEPTH_TEST);
        GLES30.glEnable(GLES30.GL_CULL_FACE);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE2);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_CUBE_MAP, poolSkyCubeMap);
        poolSkyProgram.enable();
        poolSkyProgram.setMVP(vp);
        NvShapes.drawCube(poolSkyProgram.getAttribPosition());
        glUseProgram(0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_CUBE_MAP, 0);

        // render water surface ---------------------------------------------------------------------------------------------------
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0); GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, waterHeightMaps[this.whmid]);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1); GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, waterNormalMap);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE2); GLES30.glBindTexture(GLES30.GL_TEXTURE_CUBE_MAP, poolSkyCubeMap);
        waterProgram.enable();
        waterProgram.setMVP(vp);
        waterProgram.setCameraPosition(cameraPos.x, cameraPos.y, cameraPos.z);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, waterVBO);
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, waterIBO);
        GLES30.glEnableVertexAttribArray(waterProgram.getAttribPosition());
        GLES20.glVertexAttribPointer(waterProgram.getAttribPosition(), 3, GL11.GL_FLOAT, false, 0, 0);
        GLES20.glDrawElements(GL11.GL_TRIANGLES, quadsVerticesCount, GLES20.GL_UNSIGNED_SHORT, 0);
        GLES20.glDisableVertexAttribArray(waterProgram.getAttribPosition());

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0);
        glUseProgram(0);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE2); GLES30.glBindTexture(GLES30.GL_TEXTURE_CUBE_MAP, 0);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1); GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0); GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);

        GLES30.glDisable(GLES30.GL_DEPTH_TEST);
        GLES.checkGLError("Rendering....");
    }
    void Resize(int Width, int Height){
        this.width = Width;
        this.height = Height;

        Matrix4f.perspective(45.0f, (float)Width / (float)Height, 0.125f, 512.0f, projectionMatrix);
    }

    void Destroy(){
        GLES.glDeleteTextures(poolSkyCubeMap);

        waterAddDropProgram.dispose();
        waterHeightMapProgram.dispose();
        waterNormalMapProgram.dispose();
        poolSkyProgram.dispose();
        waterProgram.dispose();

        GLES30.glDeleteTextures(2, waterHeightMaps, 0);
        GLES.glDeleteTextures(waterNormalMap);

        GLES.glDeleteBuffers(poolSkyVBO);
        GLES.glDeleteBuffers(waterVBO);
        GLES.glDeleteBuffers(waterIBO);

        GLES.glDeleteFramebuffers(fbo);
    }

    void AddDrop(float x, float y, float DropRadius){
        if(x >= -1.0f && x <= 1.0f && y >= -1.0f && y <= 1.0f)
        {
            GLES30.glViewport(0, 0, WMR, WMR);

            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fbo);
            GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, waterHeightMaps[(whmid + 1) % 2], 0);
            GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_ATTACHMENT, GLES30.GL_TEXTURE_2D, 0, 0);

            GLES30.glActiveTexture(GL_TEXTURE0);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, waterHeightMaps[whmid]);
            waterAddDropProgram.enable();
            waterAddDropProgram.setDropRadius(dropRadius);
            waterAddDropProgram.setPositon(x * 0.5f + 0.5f, 0.5f - y * 0.5f);
            NvShapes.drawQuad(waterAddDropProgram.getAttribPosition(), waterAddDropProgram.getAttribTexCoord());
            glUseProgram(0);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);

            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);

            ++whmid;
            whmid %= 2;
        }

    }

    Matrix4f biasMatrix;

    boolean AddDropByMouseClick(int x, int y){
        float s = (float)x / (float)(width - 1);
        float t = 1.0f - (float)y / (float)(height - 1);

        if(biasMatrix == null){
            biasMatrix = new Matrix4f();
            biasMatrix.m00 = biasMatrix.m11 = biasMatrix.m22 = 0.5f;
            biasMatrix.m30 = biasMatrix.m31 = biasMatrix.m32 = 0.5f;
        }

        Matrix4f vp = Matrix4f.mul(projectionMatrix, viewMatrix, projectionBiasMatrixInverse);
        vp = Matrix4f.mul(biasMatrix, vp, vp);
        Matrix4f vpInverse = Matrix4f.invert(vp, vp);

//        vec4 Position = ViewMatrixInverse * (ProjectionBiasMatrixInverse * vec4(s, t, 0.5f, 1.0f));
        Vector4f Position = new Vector4f(s,t,0.5f, 1.0f);
        Matrix4f.transform(vpInverse, Position, Position);
        Position.x /= Position.w;
        Position.y /= Position.w;
        Position.z /= Position.w;

//        vec3 Ray = normalize(*(vec3*)&Position - Camera.Position);
        Vector3f Ray = new Vector3f(Position.x - cameraPos.x, Position.y - cameraPos.y, Position.z - cameraPos.z);
//        vec3 Normal = vec3(0.0f, 1.0f, 0.0f);
        ReadableVector3f Normal = Vector3f.Y_AXIS;
        float D = 0;

        float NdotR = -Vector3f.dot(Normal, Ray);

        if(NdotR != 0.0f)
        {
            float Distance = (Vector3f.dot(Normal, cameraPos) + D) / NdotR;

            if(Distance > 0.0f)
            {
//                vec3 Position = Ray * Distance + Camera.Position;
                Vector3f position = Vector3f.linear(cameraPos, Ray, Distance, null);

                AddDrop(position.x, position.z, dropRadius);
                return true;
            }
        }

        return false;
    }

    static int createCubemapTexture(String[] filename){
        int tex = glGenTextures();
        GLES30.glBindTexture(GLES30.GL_TEXTURE_CUBE_MAP, tex);

        GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);

        for(int i = 0; i < 6; i++){
            Bitmap bitmap = Glut.loadBitmapFromAssets(filename[i]);
            if(bitmap == null){
                Log.e("Cubemape", "Couldn't load the file: " + filename[i]);
            }

            GLUtils.texImage2D(GLES30.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, bitmap, 0);
        }
        return tex;
    }
}
