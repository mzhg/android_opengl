////////////////////////////////////////////////////////////////////////////////
// Copyright 2017 mzhg
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
package com.nvidia.developer.opengl.app;

import android.app.ActivityManager;
import android.content.pm.ConfigurationInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Typeface;
import android.opengl.GLES10;
import android.opengl.GLES11;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.text.Html;
import android.text.TextPaint;
import android.util.Log;
import android.util.Pair;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.GLUtil;
import com.nvidia.developer.opengl.utils.NvGfxAPIVersion;
import com.nvidia.developer.opengl.utils.NvImage;
import com.nvidia.developer.opengl.utils.NvLogger;
import com.nvidia.developer.opengl.utils.NvStopWatch;
import com.nvidia.developer.opengl.utils.NvUtils;

import org.lwjgl.util.vector.Matrix4f;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by mazhen'gui on 2017/4/10.
 */

public class GLES1SampleApp extends NvAppBase implements GLSurfaceView.Renderer{

    protected NvFramerateCounter mFramerate;
    protected float mFrameDelta;
    protected final NvStopWatch mFrameTimer = new NvStopWatch();

    protected final NvStopWatch mAutoRepeatTimer = new NvStopWatch();
    protected boolean mAutoRepeatButton;
    protected boolean mAutoRepeatTriggered;

    protected final NvInputTransformer m_transformer = new NvInputTransformer();

    private float totalTime;
    private final FPSGenerator2 m_fpsGenerator = new FPSGenerator2();
    public static final int ACTION_UPDATE_FPS = 10085;
    protected int m_FPSTexture;

    private static final int FPS_TEX_WIDTH = 200;
    private static final int FPS_TEX_HEIGHT = 50;
    private final Matrix4f m_ViewMat = new Matrix4f();
    private final Matrix4f m_SensorMat = new Matrix4f();
    @Override
    public final void onSurfaceCreated(GL10 arg0, EGLConfig egl) {
        Log.e("OpenGL ES", "onSurfaceCreated");
        // check extensions and enable DXT expansion if needed
        boolean hasDXT = isExtensionSupported("GL_EXT_texture_compression_s3tc") ||
                isExtensionSupported("GL_EXT_texture_compression_dxt1");
        if (!hasDXT) {
            NvLogger.i("Device has no DXT texture support - enabling DXT expansion");
            NvImage.setDXTExpansion(true);
        }

        mFramerate = new NvFramerateCounter();
        mFrameTimer.start();

        super.onSurfaceCreated(egl);
    }

    @Override
    protected GLSurfaceView createRenderView(NvEGLConfiguration configuration){
        GLSurfaceView view = new GLSurfaceView(this);
        view.setEGLConfigChooser(configuration.redBits, configuration.greenBits, configuration.blueBits, configuration.alphaBits, configuration.depthBits, configuration.stencilBits);
        if(configuration.apiVer == NvGfxAPIVersion.GLES1){
            view.setEGLContextClientVersion(1);
        }else{
            ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
            int major = (configurationInfo.reqGlEsVersion >> 16) & 0xFFFF;
            view.setEGLContextClientVersion(major);
        }
        view.setRenderer(this);
        return view;
    }

    @Override
    public final void onDrawFrame(GL10 arg0) {
        mFrameTimer.stop();
        final boolean mTestMode = false;
        final boolean isExiting = false;

        if (mTestMode) {
            // Simulate 60fps
            mFrameDelta = 1.0f / 60.0f;

            // just an estimate
            totalTime += mFrameTimer.getTime();
        } else {
            mFrameDelta = mFrameTimer.getTime();
            // just an estimate
            totalTime += mFrameDelta;
        }
        m_transformer.update(mFrameDelta);
        mFrameTimer.reset();

        pollEvents();

        if(!isExiting){
            mFrameTimer.start();

            if (mAutoRepeatButton) {
                final float elapsed = mAutoRepeatTimer.getTime();
                if ( (!mAutoRepeatTriggered && elapsed >= 0.5f) ||
                        (mAutoRepeatTriggered && elapsed >= 0.04f) ) { // 25hz repeat
                    mAutoRepeatTriggered = true;
                }
            }

            draw();
            drawFPS();
            if (mFramerate.nextFrame()) {
                // for now, disabling console output of fps as we have on-screen.
                // makes it easier to read USEFUL log output messages.
//                NvLogger.i("fps: %.2f", mFramerate.getMeanFramerate());
                m_fpsGenerator.fpsText = Html.fromHtml(NvUtils.sprintf("<font color='red' size='20'>fps</font>: %.2f", mFramerate.getMeanFramerate()));
                addUITask(m_fpsGenerator);
            }
        }
    }

    /**
     * Rendering callback.<p>
     * Called to request the app render a frame at regular intervals when
     * the app is focused or when force by outside events like a resize
     */
    protected void draw() { }

    public Matrix4f getViewMatrix(){
        m_transformer.getModelViewMat(m_ViewMat);
        /*m_SensorMat.load(getRotationMatrix(), 0);
        return Matrix4f.mul(m_SensorMat, m_ViewMat, m_ViewMat);*/
        return m_ViewMat;
    }

    private int m_QuadPosTex;

    private void drawFPS(){
        if(m_FPSTexture == 0)
            return;

        boolean isTex2DEnabled = GLES11.glIsEnabled(GLES11.GL_TEXTURE_2D);
        boolean isLightEnabled = GLES11.glIsEnabled(GLES11.GL_LIGHTING);
        boolean isBlendEnabled = GLES11.glIsEnabled(GLES11.GL_BLEND);

        int vertexBinding = GLES.glGetInteger(GLES11.GL_VERTEX_ARRAY_BUFFER_BINDING);
        int elmentBinding = GLES.glGetInteger(GLES11.GL_ELEMENT_ARRAY_BUFFER_BINDING);

        if(!isTex2DEnabled)
            GLES11.glEnable(GLES11.GL_TEXTURE_2D);

        if(isLightEnabled)
            GLES11.glDisable(GLES11.GL_LIGHTING);

        if(!isBlendEnabled)
            GLES11.glEnable(GLES11.GL_BLEND);

        GLES11.glMatrixMode(GLES11.GL_PROJECTION);
        GLES11.glPushMatrix();
        GLES11.glLoadIdentity();
        GLES11.glOrthof(0, getWidth(), 0, getHeight(), -1, 1);
        GLES11.glMatrixMode(GLES11.GL_MODELVIEW);
        GLES11.glPushMatrix();
        GLES11.glLoadIdentity();

        boolean isBufferCreated = m_QuadPosTex != 0;
        if(!isBufferCreated)
            m_QuadPosTex = GLES.glGenBuffers();
        GLES11.glBindBuffer(GLES11.GL_ARRAY_BUFFER, m_QuadPosTex);

        if(!isBufferCreated){
            final float padding = 10;
            FloatBuffer buffer = GLUtil.getCachedFloatBuffer(16);
            buffer.put(getWidth() - FPS_TEX_WIDTH - padding).put(getHeight() - FPS_TEX_HEIGHT - padding).put(0).put(1);
            buffer.put(getWidth()- padding).put(getHeight() - FPS_TEX_HEIGHT- padding).put(1).put(1);
            buffer.put(getWidth() - padding).put(getHeight()- padding).put(1).put(0);
            buffer.put(getWidth() - FPS_TEX_WIDTH - padding).put(getHeight()- padding).put(0).put(0);
            buffer.flip();
            GLES11.glBufferData(GLES11.GL_ARRAY_BUFFER,  4* 16,buffer, GLES11.GL_STATIC_DRAW);
        }

        GLES11.glVertexPointer(2, GLES11.GL_FLOAT, 4 * 4, 0);
        GLES11.glEnableClientState(GLES11.GL_VERTEX_ARRAY);
        GLES11.glTexCoordPointer(2, GLES11.GL_FLOAT, 4*4, 8);
        GLES11.glEnableClientState(GLES11.GL_TEXTURE_COORD_ARRAY);

        if(elmentBinding != 0){
            GLES11.glBindBuffer(GLES11.GL_ELEMENT_ARRAY_BUFFER, 0);
        }
        GLES11.glBindTexture(GLES10.GL_TEXTURE_2D, m_FPSTexture);
        if(!isBlendEnabled){
            GLES11.glEnable(GLES10.GL_BLEND);
        }
//
        GLES11.glBlendFunc(GLES11.GL_SRC_ALPHA, GLES11.GL_ONE_MINUS_SRC_ALPHA);
        GLES.checkGLError();
        GLES11.glDrawArrays(GLES11.GL_TRIANGLE_FAN, 0, 4);

        GLES11.glDisableClientState(GLES11.GL_VERTEX_ARRAY);
        GLES11.glDisableClientState(GLES11.GL_COLOR_ARRAY);

        // reset the ogl states
        if(elmentBinding != 0){
            GLES11.glBindBuffer(GLES11.GL_ELEMENT_ARRAY_BUFFER, elmentBinding);
        }
        GLES11.glBindBuffer(GLES11.GL_ARRAY_BUFFER, vertexBinding);

        GLES11.glMatrixMode(GLES11.GL_PROJECTION);
        GLES11.glPopMatrix();

        GLES11.glMatrixMode(GLES11.GL_MODELVIEW);
        GLES11.glPopMatrix();

        if(!isTex2DEnabled){
            GLES11.glDisable(GLES11.GL_TEXTURE_2D);
        }

        if(isLightEnabled){
            GLES11.glEnable(GLES11.GL_LIGHTING);
        }

        if(!isBlendEnabled){
            GLES11.glDisable(GLES11.GL_BLEND);
        }
        GLES11.glBindTexture(GLES10.GL_TEXTURE_2D,0);
        GLES.checkGLError();
    }

    @Override
    protected void onUITaskResult(int action, Object result) {
        if(action == ACTION_UPDATE_FPS){
            boolean texCreated = m_FPSTexture != 0;
            if(!texCreated)
                m_FPSTexture = GLES.glGenTextures();
            GLES11.glBindTexture(GLES11.GL_TEXTURE_2D, m_FPSTexture);
            if(!texCreated) {
                GLUtils.texImage2D(GLES11.GL_TEXTURE_2D, 0, (Bitmap) result, 0);
                GLES11.glTexParameteri(GLES11.GL_TEXTURE_2D, GLES11.GL_TEXTURE_MAG_FILTER, GLES11.GL_LINEAR);
                GLES11.glTexParameteri(GLES11.GL_TEXTURE_2D, GLES11.GL_TEXTURE_MIN_FILTER, GLES11.GL_LINEAR);
            }else
                GLUtils.texSubImage2D(GLES11.GL_TEXTURE_2D, 0, 0, 0, (Bitmap)result);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 arg0, int w, int h) {
        m_transformer.setScreenSize(w, h);
        reshape(w, h);
    }

    /**
     * Get the framerate counter.
     * <p>
     * The NvSampleApp initializes and updates an NvFramerateCounter in its
     * mainloop implementation. It also draws it to the screen. The application
     * may gain access if it wishes to get the data for its own use.
     *
     * @return a pointer to the framerate counter object
     */
    public NvFramerateCounter getFramerate() {
        return mFramerate;
    }

    /**
     * Frame delta time.
     * <p>
     *
     * @return the time since the last frame in seconds
     */
    public float getFrameDeltaTime() {
        return mFrameDelta;
    }

    /**
     * Total time since the opengl context init..
     * <p>
     *
     * @return the total time.
     */
    public float getTotalTime() { return totalTime;}

    public final boolean keyInput(int code, int action){
        if (handleKeyInput(code, action))
            return true;

        // give last shot to transformer.
        return m_transformer.processKey(code, action);
    }

    @Override
    public final boolean characterInput(char c) {
        if (handleCharacterInput(c))
            return true;
        return false;
    }

    private boolean isDown = false;
    private float startX = 0, startY = 0;

    public final boolean pointerInput(int device, int action, int modifiers, int count, NvPointerEvent[] points) {
        long time = 0;
//		    static bool isDown = false;
//		    static float startX = 0, startY = 0;
        boolean isButtonEvent = (action==NvPointerActionType.DOWN)||(action==NvPointerActionType.UP);
        if (isButtonEvent)
            isDown = (action==NvPointerActionType.DOWN);

        if (handlePointerInput(device, action, modifiers, count, points))
            return true;
        else
            return m_transformer.processPointer(device, action, modifiers, count, points);
    }

    @Override
    protected final boolean isRequreOpenGLES2() {
        return false;
    }

    private final class FPSGenerator implements UIThreadTask{
        private Bitmap mBitmap;
        private Canvas mCanvas;
        private Paint mPaint;
        CharSequence fpsText;
        private PorterDuffXfermode mClearMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
        private PorterDuffXfermode mSrcMode = new PorterDuffXfermode(PorterDuff.Mode.SRC);

        @Override
        public Pair<Integer, Object> doUIThreadTask() {
            if(mBitmap == null){
//                LinearLayout layout = new LinearLayout(GLES1SampleApp.this);
//                layout.addView(mTextView);
                mBitmap = Bitmap.createBitmap(FPS_TEX_WIDTH, FPS_TEX_HEIGHT, Bitmap.Config.ARGB_8888);
                mCanvas = new Canvas(mBitmap);

                TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);
                textPaint.setTextSize(35);
                textPaint.setTypeface(Typeface.DEFAULT);
                textPaint.setColor(Color.WHITE);

                mPaint = textPaint;
            }
            mPaint.setXfermode(mClearMode);
            mCanvas.drawPaint(mPaint);
            mPaint.setXfermode(mSrcMode);
            mCanvas.drawText(fpsText,0,fpsText.length(), 00, FPS_TEX_HEIGHT - 10, mPaint);

            return Pair.create(ACTION_UPDATE_FPS, (Object) mBitmap);
        }
    }

    private final class FPSGenerator2 implements UIThreadTask{
        private Bitmap mBitmap;
        private TextView mTextView;
        private Canvas mCanvas;
        private Paint mPaint;

        CharSequence fpsText;

        @Override
        public Pair<Integer, Object> doUIThreadTask() {
            if(mTextView == null){
                mTextView = new TextView(GLES1SampleApp.this);
                mTextView.setTextColor(Color.WHITE);
                mTextView.setLayoutParams(new ViewGroup.LayoutParams(FPS_TEX_WIDTH, FPS_TEX_HEIGHT));
                mTextView.setMaxWidth(FPS_TEX_WIDTH);
                mTextView.setMaxHeight(FPS_TEX_HEIGHT);

                mPaint = new Paint();
                mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

                Log.i("FPSGenerator2", "Create TextView");
            }
            mTextView.setText(fpsText);

            if (mTextView.getMeasuredHeight() <= 0) {
                mTextView.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                mBitmap = Bitmap.createBitmap(mTextView.getMeasuredWidth(), mTextView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
                mCanvas = new Canvas(mBitmap);
                mTextView.layout(0, 0, mTextView.getMeasuredWidth(), mTextView.getMeasuredHeight());

                Log.i("FPSGenerator2", "Create Bitmap");
            }
            mCanvas.drawPaint(mPaint);
            mTextView.draw(mCanvas);
            return Pair.create(ACTION_UPDATE_FPS, (Object) mBitmap);
        }
    }
}
