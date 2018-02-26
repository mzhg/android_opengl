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

import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.example.oglsamples.R;
import com.google.vr.sdk.base.AndroidCompat;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;
import com.nvidia.developer.opengl.utils.Glut;
import com.nvidia.developer.opengl.utils.NvAssetLoader;
import com.nvidia.developer.opengl.utils.NvImage;
import com.nvidia.developer.opengl.utils.NvStopWatch;

import org.lwjgl.util.vector.Matrix4f;

import javax.microedition.khronos.egl.EGLConfig;

/**
 * Created by mazhen'gui on 2017/4/10.
 */

public abstract class GvrStereoSampleApp extends GvrActivity implements GvrView.StereoRenderer, NvInputCallbacks{

    protected NvFramerateCounter mFramerate;
    protected float mFrameDelta;
    protected final NvStopWatch mFrameTimer = new NvStopWatch();
    protected final NvInputTransformer m_transformer = new NvInputTransformer();

    private float totalTime;
    private final Matrix4f m_ViewMat = new Matrix4f();
    private final Matrix4f m_EyeMat = new Matrix4f();
    private final Matrix4f m_HeadViewMat = new Matrix4f();
    private NvInputHandler mInputHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        Display display = getWindow().getWindowManager().getDefaultDisplay();

        Glut.init(getAssets());

        NvAssetLoader.init(getAssets());
        setContentView(R.layout.gvr_main);

        GvrView gvrView = (GvrView) findViewById(R.id.gvr_view);
        gvrView.setEGLConfigChooser(8, 8, 8, 8, 16, 8);

        gvrView.setTransitionViewEnabled(true);
        gvrView.setEGLContextClientVersion(2);

        if (gvrView.setAsyncReprojectionEnabled(true)) {
            // Async reprojection decouples the app framerate from the display framerate,
            // allowing immersive interaction even at the throttled clockrates set by
            // sustained performance mode.
            AndroidCompat.setSustainedPerformanceMode(this, true);
        }

        gvrView.setRenderer(this);
        setGvrView(gvrView);
        mInputHandler = new NvInputHandler(gvrView);
        mInputHandler.setInputListener(this);
    }

    @Override
    public final void onSurfaceCreated(EGLConfig egl) {
        Log.e("OpenGL ES", "onSurfaceCreated");
        NvImage.setDXTExpansion(true);

        mFramerate = new NvFramerateCounter();
        mFrameTimer.start();
        initRendering();
    }

    /**
     * Initialize rendering.<p>
     * Called once the GLES context and surface have been created and bound
     * to the main thread.  Called again if the context is lost and must be
     * recreated.
     */
    protected abstract void initRendering();

    @Override
    public void onNewFrame(HeadTransform headTransform) {
        mFrameTimer.stop();
        final boolean mTestMode = false;
        final boolean isExiting = false;

        float[] mat = headTransform.getHeadView();
        m_HeadViewMat.load(mat, 0);
        m_transformer.getModelViewMat(m_ViewMat);
        Matrix4f.mul(m_HeadViewMat, m_ViewMat, m_HeadViewMat);

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

        mInputHandler.pollEvents();

        if(!isExiting){
            mFrameTimer.start();

//            draw();
//            drawFPS();
            if (mFramerate.nextFrame()) {
                // for now, disabling console output of fps as we have on-screen.
                // makes it easier to read USEFUL log output messages.
//                NvLogger.i("fps: %.2f", mFramerate.getMeanFramerate());
//                m_fpsGenerator.fpsText = Html.fromHtml(NvUtils.sprintf("<font color='red' size='20'>fps</font>: %.2f", mFramerate.getMeanFramerate()));
//                addUITask(m_fpsGenerator);
            }
        }
    }

    public Matrix4f getViewMatrix(Eye eye){
        m_transformer.getModelViewMat(m_ViewMat);
//        m_SensorMat.load(getRotationMatrix(), 0);
        m_EyeMat.load(eye.getEyeView(), 0);

        return Matrix4f.mul(m_EyeMat, m_ViewMat, m_ViewMat);
    }

    public Matrix4f getHeadViewMatrix() {return m_HeadViewMat;}

    @Override
    public abstract void onDrawEye(Eye eye);

    @Override
    public void onFinishFrame(Viewport viewport) {

    }

    @Override
    public void onSurfaceChanged(int w, int h) {
        m_transformer.setScreenSize(w, h);
        reshape(w, h);
    }

    protected abstract void reshape(int width, int height);

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
//        if (handleKeyInput(code, action))
//            return true;

        // give last shot to transformer.
        return m_transformer.processKey(code, action);
    }

    @Override
    public final boolean characterInput(char c) {
//        if (handleCharacterInput(c))
//            return true;
        return false;
    }

    @Override
    public boolean gamepadChanged(int changedPadFlags) {
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

//        if (handlePointerInput(device, action, modifiers, count, points))
//            return true;
//        else
        return m_transformer.processPointer(device, action, modifiers, count, points);
    }
}
