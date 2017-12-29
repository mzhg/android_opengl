package jet.learning.opengl.samples;

import android.opengl.GLES20;
import android.util.Log;

import com.nvidia.developer.opengl.app.NvSampleApp;
import com.nvidia.developer.opengl.utils.BufferUtils;
import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.Glut;
import com.nvidia.developer.opengl.utils.NvGLSLProgram;
import com.nvidia.developer.opengl.utils.NvUtils;

import java.nio.FloatBuffer;

/**
 * Created by mazhen'gui on 2017/12/28.
 */

public final class PointSprite extends NvSampleApp {
    private static final int MAX_POINT_COUNT = 32;

    private int mPointBuffer;
    private NvGLSLProgram mProgram;
    private int mSpriteTex;

    private int mPosition;
    private int mSize;

    @Override
    protected void initRendering() {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(MAX_POINT_COUNT * 3);
        for(int i = 0; i < MAX_POINT_COUNT; i++){
            float x = NvUtils.random(-1, 1);
            float y = NvUtils.random(-1, 1);
            float size = NvUtils.random(10.0f, 100.0f);

            buffer.put(x).put(y).put(size);
        }

        buffer.flip();
        mPointBuffer = GLES.glGenBuffers();
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mPointBuffer);
        GLES.glBufferData(GLES20.GL_ARRAY_BUFFER, buffer, GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        mProgram = NvGLSLProgram.createFromFiles("shaders/SpriteRenderVS.vert", "shaders/SpriteRenderPS.frag");
        mProgram.enable();
        mProgram.setUniform1i("sprite_texture", 0);
        mPosition = mProgram.getAttribLocation("aPosition");
        mSize = mProgram.getAttribLocation("aSize");

        Log.i("PointSprite", "mPosition = " + mPosition);
        Log.i("PointSprite", "mSize = " + mSize);

        mSpriteTex = Glut.loadTextureFromFile("textures/emitter.png", GLES20.GL_LINEAR, GLES20.GL_CLAMP_TO_EDGE);
        GLES.checkGLError();
    }

    @Override
    protected void draw() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT|GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mSpriteTex);

        mProgram.enable();

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mPointBuffer);
        GLES20.glVertexAttribPointer(mPosition, 2, GLES20.GL_FLOAT, false, 12, 0);
        GLES20.glVertexAttribPointer(mSize, 1, GLES20.GL_FLOAT, false, 12, 8);
        GLES20.glEnableVertexAttribArray(mPosition);
        GLES20.glEnableVertexAttribArray(mSize);

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, MAX_POINT_COUNT);

        GLES20.glDisableVertexAttribArray(mPosition);
        GLES20.glDisableVertexAttribArray(mSize);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES.checkGLError();
    }

    @Override
    protected void reshape(int width, int height) {
        GLES20.glViewport(0,0,width, height);
    }
}
