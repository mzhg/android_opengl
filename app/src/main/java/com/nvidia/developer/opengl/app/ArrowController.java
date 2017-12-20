package com.nvidia.developer.opengl.app;

import android.opengl.GLES11;
import android.opengl.GLES20;

import com.nvidia.developer.opengl.utils.BufferUtils;
import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.NvDisposeable;
import com.nvidia.developer.opengl.utils.NvUtils;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL11;

import jet.learning.opengl.common.SimpleTextureProgram;

/**
 * Created by mazhen'gui on 2017/12/19.
 */

public class ArrowController implements NvDisposeable{
    public static final int ARROW_LEFT = 0;
    public static final int ARROW_RIGHT = 1;
    public static final int ARROW_TOP = 2;
    public static final int ARROW_BOTTOM = 3;

    private static final int ARROW_LENGTH = 128;

    private ArrowOnTouchListener mListener;
    private boolean mVisible = true;
    private final boolean[] mArrowStates = new boolean[4];
    private final ArrowData[] mArrowData = new ArrowData[4];

    // OpenGL resources
    private int mArrowTex;
    private SimpleTextureProgram mArrowRenderProgram;

    // Internal variables
    private FloatBuffer mQuadBuffer;
    private final Matrix4f mProj = new Matrix4f();

    private int mScreenWidth, mScreenHeight;

    public ArrowController(){
        for(int i = 0; i < mArrowData.length; i++){
            mArrowData[i] = new ArrowData();
        }

        mQuadBuffer = BufferUtils.createFloatBuffer(16);
    }

    public void setVisible(boolean visible){
        mVisible = visible;
    }

    public static String getArrowName(int arrow){
        switch (arrow){
            case ARROW_LEFT: return  "LEFT";
            case ARROW_RIGHT: return  "RIGHT";
            case ARROW_TOP: return  "TOP";
            case ARROW_BOTTOM: return  "BOTTOM";
        }

        throw new IllegalArgumentException("invalid arrow: " + arrow);
    }

    public void initlizeGL(){
        final int h = ARROW_LENGTH;
        final int w = (int) (h * Math.cos(Math.PI/6) + 0.5);

        ByteBuffer pixels = BufferUtils.createByteBuffer(w * h * 4); // RGBA
        Vector2f low = new Vector2f();
        Vector2f top = new Vector2f();

        double low_bounds = Math.cos(Math.PI/3);
        double up_bounds = Math.cos(Math.PI/6);
        for(int j = 0; j < h; j++){
            for(int i = 0; i < w; i++){
                top.set(i, h-j);
                low.set(i, j);

                low.normalise();
                top.normalise();

                float lowAngle = Vector2f.dot(low, Vector2f.X_AXIS);
                float topAngle = Vector2f.dot(top, Vector2f.X_AXIS);

                boolean inside = true;

                if(lowAngle > up_bounds || topAngle > up_bounds || (lowAngle < low_bounds && topAngle < low_bounds)){
                    inside = false;
                }

                if(inside){
                    pixels.putInt(0xFFE7C375);
                }else{
                    pixels.putInt(0);
                }
            }
        }

        pixels.flip();

        mArrowTex = GLES.glGenTextures();
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mArrowTex);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, w,h, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, pixels);
        GLES11.glTexParameteri(GLES11.GL_TEXTURE_2D, GLES11.GL_TEXTURE_MAG_FILTER, GLES11.GL_LINEAR);
        GLES11.glTexParameteri(GLES11.GL_TEXTURE_2D, GLES11.GL_TEXTURE_MIN_FILTER, GLES11.GL_LINEAR);
        GLES11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GLES11.GL_CLAMP_TO_EDGE);
        GLES11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GLES11.GL_CLAMP_TO_EDGE);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        mArrowRenderProgram = new SimpleTextureProgram();
        GLES.checkGLError();
    }

    public boolean processPointer(int device, int action, int modifiers, int count, NvPointerEvent[] points) {
        if(mScreenWidth <= 0 || mScreenHeight <=0 || !mVisible)
            return false;

        boolean isDown = (action == NvPointerActionType.DOWN || action == NvPointerActionType.EXTRA_DOWN);
        boolean isUp = (action == NvPointerActionType.UP || action == NvPointerActionType.EXTRA_UP);
        boolean isMotion = (action == NvPointerActionType.MOTION);

        for(int i = 0; i < 4; i++){
            ArrowData data = mArrowData[i];

            for(int j = 0; j < count; j++){
                float x = points[j].m_x;
                float y = mScreenHeight - points[j].m_y;

                boolean touched = data.contain(x,y);
                if(touched){  // Only handle the event that had touched.
                    if(mArrowStates[i]){  // the button was pressed
                        if(isUp){
                            changeState(i, false);
                        }
                    }else{  // the button was relaxed
                        if(isDown || isMotion){  // Regard the motion event as the down event.
                            changeState(i, true);
                        }
                    }
                    return true;
                }
            }

        }

        return false;
    }

    private void changeState(int arrow, boolean newState){
        if(mListener != null) {
            mListener.onArrowTouch(arrow, newState);
        }

        mArrowStates[arrow] = newState;
    }

    public void draw(int screenWidth, int screenHeight){
        if(!mVisible)
            return;

        updateArrow(screenWidth, screenHeight);

        boolean depthTest = GLES20.glIsEnabled(GLES20.GL_DEPTH_TEST);
        boolean depthWrite = GLES.glGetBoolean(GLES20.GL_DEPTH_WRITEMASK);
        boolean stencilTest = GLES20.glIsEnabled(GLES20.GL_STENCIL_TEST);

//        GLES20.glViewport(x,y,width, height);
        if(depthTest) GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        if(stencilTest) GLES20.glDisable(GLES20.GL_STENCIL_TEST);
        if(depthWrite) GLES20.glDepthMask(false);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFuncSeparate(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA, GLES20.GL_ZERO, GLES20.GL_ONE);

        mArrowRenderProgram.enable();
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,mArrowTex);

        int posAttribLoc = mArrowRenderProgram.getAttribPosition();
        int texAttribLoc = mArrowRenderProgram.getAttribTexCoord();

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

        GLES20.glEnableVertexAttribArray(posAttribLoc);
        GLES20.glEnableVertexAttribArray(texAttribLoc);

        for(int i = 0; i < 4; i++){
            drawArrow(i);
        }

        GLES20.glDisableVertexAttribArray(posAttribLoc);
        GLES20.glDisableVertexAttribArray(texAttribLoc);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0);

        if(depthTest) GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        if(stencilTest) GLES20.glEnable(GLES20.GL_STENCIL_TEST);
        if(depthWrite) GLES20.glDepthMask(depthWrite);
        GLES20.glDisable(GLES20.GL_BLEND);

        GLES.checkGLError();
    }

    private void drawArrow(int arrow){
        ArrowData data = mArrowData[arrow];

        mQuadBuffer.position(0);
        mQuadBuffer.put(data.x).put(data.y).put(0.0f).put(0.0f);
        mQuadBuffer.put(data.x + data.width).put(data.y).put(1.0f).put(0.0f);
        mQuadBuffer.put(data.x + data.width).put(data.y + data.height).put(1.0f).put(1.0f);
        mQuadBuffer.put(data.x).put(data.y + data.height).put(0.0f).put(1.0f);
        mQuadBuffer.flip();

        mArrowRenderProgram.setMatrix(data.transform);

        int posAttribLoc = mArrowRenderProgram.getAttribPosition();
        int texAttribLoc = mArrowRenderProgram.getAttribTexCoord();

        GLES20.glVertexAttribPointer(posAttribLoc, 2, GLES20.GL_FLOAT, false, 16, mQuadBuffer);
        mQuadBuffer.position(2);
        GLES20.glVertexAttribPointer(texAttribLoc, 2, GLES20.GL_FLOAT, false, 16, mQuadBuffer);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

    }

    public final boolean isTouched(int arrow){ return mArrowStates[arrow];}

    public void setArrowOnTouchListener(ArrowOnTouchListener listener) { mListener = listener;}

    private void updateArrow(int screenWidth, int screenHeight){
        if(mScreenWidth != screenWidth || mScreenHeight != screenHeight){
            mScreenWidth = screenWidth;
            mScreenHeight = screenHeight;

            Matrix4f.ortho(0, screenWidth, 0, screenHeight, -1, 1, mProj);

            final int marginRigth = 20;
            final int marginBottom = 20;
            final int padding = 10;
            final float heightRatio = 0.11f; // the height of the image over the screenHeight

            final int height = (int) (screenHeight * heightRatio);
            final int width = (int) (height * Math.cos(Math.PI/6) + 0.5);

            final int height2 = height/2;
            final int width2 = width/2;

            {
                int centerX = screenWidth - marginRigth - width * 2 - padding * 2 - width2;
                int centerY = marginBottom + height + padding + height2;
                mArrowData[ARROW_LEFT].setRegion(centerX - width2, centerY - height2, width, height);
                mArrowData[ARROW_LEFT].setRotate(mProj, NvUtils.PI);
            }

            {
                int centerX = screenWidth - marginRigth - width2;
                int centerY = marginBottom + height + padding + height2;
                mArrowData[ARROW_RIGHT].setRegion(centerX - width2, centerY - height2, width, height);
                mArrowData[ARROW_RIGHT].setRotate(mProj, 0);
            }

            {
                int centerX = screenWidth - marginRigth -width - padding - width2;
                int centerY = marginBottom + height2;
                mArrowData[ARROW_BOTTOM].setRegion(centerX - width2, centerY - height2, width, height);
                mArrowData[ARROW_BOTTOM].setRotate(mProj, -NvUtils.PI/2);
            }

            {
                int centerX = screenWidth - marginRigth -width - padding - width2;
                int centerY = marginBottom + height *2 + padding * 2 + height2;
                mArrowData[ARROW_TOP].setRegion(centerX - width2, centerY - height2, width, height);
                mArrowData[ARROW_TOP].setRotate(mProj, NvUtils.PI/2);
            }
        }
    }

    @Override
    public void dispose() {
        if(mArrowTex != 0){
            GLES.glDeleteTextures(mArrowTex);
            mArrowTex = 0;
        }

        if(mArrowRenderProgram != null){
            mArrowRenderProgram.dispose();
            mArrowRenderProgram = null;
        }
    }

    public interface ArrowOnTouchListener{
        void onArrowTouch(int arrowFlags, boolean state);
    }

    private static final class ArrowData{
        int x,y;
        int width, height;  // region

        final Matrix4f transform = new Matrix4f();

        void setRegion(int x, int y, int width, int height){
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        boolean contain(float x, float y){
            float diffX = x - this.x;
            float diffY = y - this.y;

            return diffX >= 0 && diffX < width && diffY >=0 && diffY < height;
        }

        void setRotate(Matrix4f proj, float angle){
            float centerX = x + width/2;
            float centerY = y + height/2;

            transform.setIdentity();

            transform.translate(centerX, centerY, 0);
            transform.rotate(angle, Vector3f.Z_AXIS);
            transform.translate(-centerX, -centerY, 0);
            Matrix4f.mul(proj, transform, transform);
        }
    }
}
