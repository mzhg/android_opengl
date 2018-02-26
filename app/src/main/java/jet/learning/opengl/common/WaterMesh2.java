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
import android.opengl.GLES30;

import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.GLUtil;
import com.nvidia.developer.opengl.utils.NvGLSLProgram;
import com.nvidia.developer.opengl.utils.NvUtils;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import javax.microedition.khronos.opengles.GL11;

/**
 * Created by mazhen'gui on 2017/12/16.
 */
public class WaterMesh2 implements RenderMesh {
    //handles to the water's XZ VBO and Index Buffers (static and donot change)
    private int mWaterXZVBO, mWaterIBO;

    // programs
    private NvGLSLProgram mUpdateHeightMapProgram;
    private NvGLSLProgram mCalculateGradientProgram;

    // attirubte location
    private int mfxPosition;
    private int mfxNormal;
    private int mfxTex;

    private int m_vertCount;
    private final Vector4f mDisturbance = new Vector4f();

    // framebuffer and textures
    private int mFBO;
    private int[] mHeightMaps = new int[2];  // Two image for the ping-pong operation
    private int mGradientMap;                // for calculating the normal
    private int mCursor;

    private int mWidth, mHeight;   // the dimension of the water mesh

    private int mOldFBO;
    private int[] mOldViewport = new int[4];
    private int mRainFrame;

    public WaterMesh2(int width, int height){
        mWidth = width;
        mHeight = height;
    }

    @Override
    public void dispose() {
        if(mUpdateHeightMapProgram != null){
            mUpdateHeightMapProgram.dispose();
            mUpdateHeightMapProgram = null;
        }

        if(mCalculateGradientProgram != null){
            mCalculateGradientProgram.dispose();
            mCalculateGradientProgram = null;
        }

        if(mWaterXZVBO != 0){
            GLES.glDeleteBuffers(mWaterXZVBO);
            mWaterXZVBO = 0;
        }

        if(mWaterIBO != 0){
            GLES.glDeleteBuffers(mWaterIBO);
            mWaterIBO = 0;
        }

        if(mFBO != 0){
            GLES.glDeleteFramebuffers(mFBO);
            mFBO = 0;
        }

        if(mGradientMap !=0){
            GLES.glDeleteTextures(mGradientMap);
            mGradientMap = 0;
        }

        GLES20.glDeleteTextures(mHeightMaps.length, mHeightMaps, 0);
        mHeightMaps[0] = 0;
        mHeightMaps[1] = 0;
    }

    @Override
    public void initlize(MeshParams params) {
        if(mWidth <= 0 || mHeight <=0)
            throw new IllegalArgumentException("Invalid the dimension of the ");

        mUpdateHeightMapProgram = NvGLSLProgram.createFromFiles("shaders/Quad_VS.vert", "d3dcoder/WaterUpdateHeightMapPS.frag");
        mCalculateGradientProgram = NvGLSLProgram.createFromFiles("shaders/Quad_VS.vert", "d3dcoder/WaterCalculateGradientPS.frag");

        mfxPosition = params.posAttribLoc;
        mfxNormal = params.norAttribLoc;
        mfxTex = params.texAttribLoc;

        // initlize the framebuffer and textures
        GLES20.glGenTextures(2, mHeightMaps, 0);
        for(int i = 0; i < mHeightMaps.length; i++){
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mHeightMaps[i]);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RG16F, mWidth, mHeight, 0, GLES30.GL_RG, GLES30.GL_FLOAT, null);
            GLES.checkGLError();
        }

        mGradientMap = GLES.glGenTextures();
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mGradientMap);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RG16F, mWidth, mHeight, 0, GLES30.GL_RG, GLES30.GL_FLOAT, null);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
        GLES.checkGLError();

        mFBO = GLES.glGenFramebuffers();

        final int w = mWidth;
        final int h = mHeight;

        float[] surfacexz = new float[w*h*4];
        for(int i=0; i<h; i++)
        {
            for(int j=0; j<w; j++)
            {
                surfacexz[i*w*4 + j*4 + 0] = (float)i;
                surfacexz[i*w*4 + j*4 + 1] = (float)j;
                surfacexz[i*w*4 + j*4 + 2] = (float)j/w;
                surfacexz[i*w*4 + j*4 + 3] = (float)i/h;
            }
        }

        mWaterXZVBO = GLES.glGenBuffers();
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mWaterXZVBO);
        GLES.glBufferData(GLES20.GL_ARRAY_BUFFER, GLUtil.wrap(surfacexz), GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        //no of quads for a grid of wxh = (w-1)*(h-1);
        //no of triangles = no of quads * 2;
        //no of vertices = no of triangles * 3;
        //no of components per vertex = 2 [x,z];

        m_vertCount = (w-3)*(h-3) * 2 * 3;

        short[] indices = new short[m_vertCount];
        for(int i=0; i<w-3; i++)
        {
            for(int j=0; j<h-3; j++)
            {
                int k,l;
                if(i%2==0)
                {
                    k = i+1;
                    l = j+1;
                    //for each quad on even row
                    //triangle 1
                    //vertex 1
                    indices[i*(h-3)*6 + j*6 + 0] = (short) ((k)*h + (l));
                    //vertex 2
                    indices[i*(h-3)*6 + j*6 + 1] = (short) ((k)*h + (l+1));
                    //vertex 3
                    indices[i*(h-3)*6 + j*6 + 2] = (short) ((k+1)*h + (l));

                    //triangle 2
                    //vertex 1
                    indices[i*(h-3)*6 + j*6 + 3] = (short) ((k+1)*h + (l));
                    //vertex 2
                    indices[i*(h-3)*6 + j*6 + 4] = (short) ((k)*h + (l+1));
                    //vertex 3
                    indices[i*(h-3)*6 + j*6 + 5] = (short) ((k+1)*h + (l+1));
                }
                else
                {
                    k = 1 + (w - 4) - i;
                    l = 1 + (h - 4) - j;
                    //for each quad on odd row
                    //triangle 1
                    //vertex 1
                    indices[i*(h-3)*6 + j*6 + 0] = (short) ((k)*h + (l));
                    //vertex 2
                    indices[i*(h-3)*6 + j*6 + 1] = (short) ((k)*h + (l+1));
                    //vertex 3
                    indices[i*(h-3)*6 + j*6 + 2] = (short) ((k+1)*h + (l));

                    //triangle 2
                    //vertex 1
                    indices[i*(h-3)*6 + j*6 + 3] = (short) ((k+1)*h + (l));
                    //vertex 2
                    indices[i*(h-3)*6 + j*6 + 4] = (short) ((k)*h + (l+1));
                    //vertex 3
                    indices[i*(h-3)*6 + j*6 + 5] = (short) ((k+1)*h + (l+1));
                }
            }
        }

        mWaterIBO = GLES.glGenBuffers();
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mWaterIBO);
        GLES.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, GLUtil.wrap(indices), GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

        GLES.checkGLError();
    }

    // internal variables
    private final Vector4f disturbance = new Vector4f();
    private final Vector3f point = new Vector3f();
    private final Vector2f gridPos = new Vector2f();

    public void update(WaveSettings settings, float dt) {
        if(!settings.animation){
            return;
        }

        final int source = mCursor;
        final int destion = 1-mCursor;

        boolean addDisturbance = false;
        // add rain
        if (settings.addRain)
        {
            if (settings.frequency != 0 && mDisturbance.w <= 0.0f)
            {
                if (++mRainFrame >= settings.frequency)
                {
                    mRainFrame = 0;
                    mDisturbance.set(NvUtils.random(-1.0f, 1.0f), NvUtils.random(-1.0f, 1.0f),
                            settings.size, NvUtils.random(-settings.strength, settings.strength));

                    addDisturbance = true;
                }
            }
        }

        point.set(mDisturbance.x, 0.0f, mDisturbance.y);

        // compute disturbance for each wave
        if (addDisturbance && mapPointXZToGridPos(point, gridPos))
        {
            float mWaveScale = 1.0f;
            disturbance.set(gridPos.x, gridPos.y, settings.size * mWaveScale, settings.strength * mWaveScale);
        }

        GLES.checkGLError();
        saveStates();

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFBO);
        GLES20.glViewport(0,0,mWidth, mHeight);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        {  // update the height map
            mUpdateHeightMapProgram.enable();
            mUpdateHeightMapProgram.setUniform1i("g_WaterHeightMap", 0);
            mUpdateHeightMapProgram.setUniform1f("Damping", settings.damping);
            mUpdateHeightMapProgram.setUniform4f("Disturbance", disturbance.x, disturbance.y, disturbance.z, disturbance.w);
            mUpdateHeightMapProgram.setUniform1i("GridSize", mWidth);
            mUpdateHeightMapProgram.setUniform1f("DeltaTime", settings.speed);

            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, mHeightMaps[destion], 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mHeightMaps[source]);

            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
            GLES.checkGLError();
        }

        { // update the gradient map
            mCalculateGradientProgram.enable();
            mCalculateGradientProgram.setUniform1i("g_WaterHeightMap", 0);

            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, mGradientMap, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mHeightMaps[destion]);

            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
            GLES.checkGLError();
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        restoreStates();
        mDisturbance.set(0,0,0,0);
        disturbance.set(0,0,0,0);

        mCursor = 1- mCursor;
        GLES.checkGLError();
    }

    public int getHeightMap() { return mHeightMaps[mCursor];}
    public int getGradientMap() { return mGradientMap;}

    private boolean mapPointXZToGridPos(Vector3f point, Vector2f gridPos)
    {
        final float lowX = -1.1f;
        final float lowY = -1.1f;
        if(point.x> lowX && point.z> lowY)
        {
            float ox = (point.x - lowX)/2.0f;
            float oy = (point.z - lowY)/2.0f;
            if(ox<=1.0f && oy<=1.0f)
            {
                gridPos.x = oy * mWidth;
                gridPos.y = ox * mHeight;
                return true;
            }
            else return false;
        }
        else return false;
    }

    @Override
    public void draw() {
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mWaterXZVBO);
        GLES20.glVertexAttribPointer(mfxPosition, 2, GL11.GL_FLOAT, false, 16, 0);
        GLES20.glEnableVertexAttribArray(mfxPosition);
        GLES20.glVertexAttribPointer(mfxTex, 2, GL11.GL_FLOAT, false, 16, 8);
        GLES20.glEnableVertexAttribArray(mfxTex);

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mWaterIBO);
        GLES20.glDrawElements(GL11.GL_TRIANGLES, m_vertCount, GL11.GL_UNSIGNED_SHORT, 0);

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glDisableVertexAttribArray(mfxPosition);
        GLES20.glDisableVertexAttribArray(mfxTex);
    }

    private void saveStates(){
        mOldFBO = GLES.glGetInteger(GLES30.GL_FRAMEBUFFER_BINDING);
        GLES20.glGetIntegerv(GLES20.GL_VIEWPORT,mOldViewport,0);
    }

    private void restoreStates(){
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mOldFBO);
        GLES20.glViewport(mOldViewport[0], mOldViewport[1], mOldViewport[2], mOldViewport[3]);
    }

    public static class WaveSettings{
        public boolean      animation = true;
        public boolean		addRain = true;
        public int 		    frequency = 5;
        public float		strength = 5.0f;
        public float		size = 8.0f;
        public float		damping = 0.99f;
        public float        speed = 1.0f;

        public void set(WaveSettings settings){
            addRain = settings.addRain;
            frequency = settings.frequency;
            strength = settings.strength;
            size = settings.size;
            damping = settings.damping;
        }
    }
}
