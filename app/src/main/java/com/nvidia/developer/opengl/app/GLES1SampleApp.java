package com.nvidia.developer.opengl.app;

import android.util.Log;

import com.nvidia.developer.opengl.utils.NvImage;
import com.nvidia.developer.opengl.utils.NvLogger;
import com.nvidia.developer.opengl.utils.NvStopWatch;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by mazhen'gui on 2017/4/10.
 */

public class GLES1SampleApp extends NvAppBase{

    protected NvFramerateCounter mFramerate;
    protected float mFrameDelta;
    protected final NvStopWatch mFrameTimer = new NvStopWatch();

    protected final NvStopWatch mAutoRepeatTimer = new NvStopWatch();
    protected boolean mAutoRepeatButton;
    protected boolean mAutoRepeatTriggered;

    protected final NvInputTransformer m_transformer = new NvInputTransformer();

    private float totalTime;

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

        super.onSurfaceCreated(arg0, egl);
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

            if (mFramerate.nextFrame()) {
                // for now, disabling console output of fps as we have on-screen.
                // makes it easier to read USEFUL log output messages.
                NvLogger.i("fps: %.2f", mFramerate.getMeanFramerate());
            }
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
}
