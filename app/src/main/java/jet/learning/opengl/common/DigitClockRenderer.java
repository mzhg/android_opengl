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

import android.icu.util.Calendar;
import android.opengl.GLES20;

import com.nvidia.developer.opengl.utils.BufferUtils;
import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.GLUtil;
import com.nvidia.developer.opengl.utils.NvDisposeable;
import com.nvidia.developer.opengl.utils.NvGLSLProgram;

import org.lwjgl.util.vector.Matrix4f;

import java.nio.FloatBuffer;

/**
 * Created by mazhen'gui on 2017/12/18.
 */

public class DigitClockRenderer implements NvDisposeable {
    public static final float RATIO_HW = 1.2f/3.5f;

    private NvGLSLProgram mDigitProgram;
    private int mQuadBuffer;
    private FloatBuffer mQuadData = BufferUtils.createFloatBuffer(16);
    private int[] viewport = new int[4];
    private static final Calendar calendar= Calendar.getInstance();

    private int mScreenWidth, mScreenHeight;
    private int mX, mY, mWidth, mHeight;   // blit region on the screen.
    private final Matrix4f mProj = new Matrix4f();

    private int posAttribLoc;
    private int texAttribLoc;

    public void initlizeGL(){
        mDigitProgram = NvGLSLProgram.createFromFiles("shaders/SimpleTextureVS.vert", "shaders/DigitClockPS.frag");
        posAttribLoc = mDigitProgram.getAttribLocation("In_Position");
        texAttribLoc = mDigitProgram.getAttribLocation("In_TexCoord");

        float[] quad = {
            -1, -1, 0,0,
            +1, -1, 3.5f,0,
            +1, +1, 3.5f,1.2f,
            -1, +1, 0,1.2f,
        };

        mQuadBuffer = GLES.glGenBuffers();
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mQuadBuffer);
        GLES.glBufferData(GLES20.GL_ARRAY_BUFFER, GLUtil.wrap(quad), GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

    public void draw(int x, int y, int width, int height){
//        GLES20.glGetIntegerv(GLES20.GL_VIEWPORT, viewport, 0);
        boolean depthTest = GLES20.glIsEnabled(GLES20.GL_DEPTH_TEST);
        boolean depthWrite = GLES.glGetBoolean(GLES20.GL_DEPTH_WRITEMASK);
        boolean stencilTest = GLES20.glIsEnabled(GLES20.GL_STENCIL_TEST);

//        GLES20.glViewport(x,y,width, height);
        if(depthTest) GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        if(stencilTest) GLES20.glDisable(GLES20.GL_STENCIL_TEST);
        if(depthWrite) GLES20.glDepthMask(false);

        updateBuffer(x,y,width, height);

        if(mScreenWidth== 0 || mScreenHeight == 0){
            throw new IllegalArgumentException();
        }

        mDigitProgram.enable();
        mDigitProgram.setUniformMatrix4("g_Mat", mProj);

        calendar.setTimeInMillis(System.currentTimeMillis());
        int hour=calendar.get(Calendar.HOUR_OF_DAY);
        int minute=calendar.get(Calendar.MINUTE);
        int second=calendar.get(Calendar.SECOND);

        mDigitProgram.setUniform1f("hour", hour);
        mDigitProgram.setUniform1f("minute", minute);
        mDigitProgram.setUniform1f("second", second);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mQuadBuffer);
        GLES20.glEnableVertexAttribArray(posAttribLoc);
        GLES20.glVertexAttribPointer(posAttribLoc, 2, GLES20.GL_FLOAT, false, 16, 0);
        GLES20.glEnableVertexAttribArray(texAttribLoc);
        GLES20.glVertexAttribPointer(texAttribLoc, 2, GLES20.GL_FLOAT, false, 16, 8);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glDisableVertexAttribArray(posAttribLoc);
        GLES20.glDisableVertexAttribArray(texAttribLoc);

//        GLES20.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
        if(depthTest) GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        if(stencilTest) GLES20.glEnable(GLES20.GL_STENCIL_TEST);
        if(depthWrite) GLES20.glDepthMask(depthWrite);
    }

    private void updateBuffer(int x, int y, int width, int height){
        if(width < 0 || height < 0)
            throw new IllegalArgumentException();

        if(mX != x || mY != y || mWidth != width || mHeight != height){
            mX = x;
            mY = y;
            mWidth = width;
            mHeight = height;

            mQuadData.put(x).put(y).put(0).put(0);
            mQuadData.put(x + width).put(y).put(3.5f).put(0);
            mQuadData.put(x + width).put(y + height).put(3.5f).put(1.2f);
            mQuadData.put(x).put(y + height).put(0.0f).put(1.2f);
            mQuadData.flip();

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mQuadBuffer);
            GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0,mQuadData.remaining() * 4, mQuadData);
        }
    }

    public void reshape(int width, int height){
        if(width < 0 || height < 0)
            throw new IllegalArgumentException();

        mScreenWidth = width;
        mScreenHeight = height;

        Matrix4f.ortho(0, width, 0, height, -1, 1, mProj);
    }

    @Override
    public void dispose() {
        if(mDigitProgram != null){
            mDigitProgram.dispose();
            mDigitProgram = null;
        }

        if(mQuadBuffer != 0){
            GLES.glDeleteBuffers(mQuadBuffer);
            mQuadBuffer = 0;
        }
    }
}
