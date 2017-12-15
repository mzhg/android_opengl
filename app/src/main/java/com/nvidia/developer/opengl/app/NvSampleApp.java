package com.nvidia.developer.opengl.app;

import android.app.ActivityManager;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.util.SparseArray;

import com.nvidia.developer.opengl.ui.NvFocusEvent;
import com.nvidia.developer.opengl.ui.NvGestureEvent;
import com.nvidia.developer.opengl.ui.NvGestureKind;
import com.nvidia.developer.opengl.ui.NvInputEventClass;
import com.nvidia.developer.opengl.ui.NvReactFlag;
import com.nvidia.developer.opengl.ui.NvTweakBar;
import com.nvidia.developer.opengl.ui.NvTweakBind;
import com.nvidia.developer.opengl.ui.NvTweakVarBase;
import com.nvidia.developer.opengl.ui.NvUIButton;
import com.nvidia.developer.opengl.ui.NvUIButtonType;
import com.nvidia.developer.opengl.ui.NvUIDrawState;
import com.nvidia.developer.opengl.ui.NvUIElement;
import com.nvidia.developer.opengl.ui.NvUIEventResponse;
import com.nvidia.developer.opengl.ui.NvUIFontFamily;
import com.nvidia.developer.opengl.ui.NvUIGraphic;
import com.nvidia.developer.opengl.ui.NvUIReaction;
import com.nvidia.developer.opengl.ui.NvUIText;
import com.nvidia.developer.opengl.ui.NvUITextAlign;
import com.nvidia.developer.opengl.ui.NvUIValueText;
import com.nvidia.developer.opengl.ui.NvUIWindow;
import com.nvidia.developer.opengl.utils.Dimension;
import com.nvidia.developer.opengl.utils.FieldControl;
import com.nvidia.developer.opengl.utils.NvGfxAPIVersion;
import com.nvidia.developer.opengl.utils.NvImage;
import com.nvidia.developer.opengl.utils.NvLogger;
import com.nvidia.developer.opengl.utils.NvStopWatch;
import com.nvidia.developer.opengl.utils.NvUtils;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.ReadableVector3f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class NvSampleApp extends NvAppBase implements GLSurfaceView.Renderer{

	protected NvFramerateCounter mFramerate;
	protected float mFrameDelta;
	protected final NvStopWatch mFrameTimer = new NvStopWatch();

	protected final NvStopWatch mAutoRepeatTimer = new NvStopWatch();
	protected boolean mAutoRepeatButton;
	protected boolean mAutoRepeatTriggered;

	protected NvUIWindow mUIWindow;
	protected NvUIValueText mFPSText;
	protected NvTweakBar mTweakBar;
	protected NvUIButton mTweakTab;

	protected final NvInputTransformer m_transformer = new NvInputTransformer();

	protected final SparseArray<NvTweakBind> mKeyBinds = new SparseArray<>(0);
	protected final SparseArray<NvTweakBind> mButtonBinds = new SparseArray<>(0);

	private float totalTime;
	private volatile boolean mPaused;
	private boolean mShutDownCalled;
	private GLSurfaceView m_surfaceView;

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
	    
	    NvUIText.staticCleanup();
	    mUIWindow = null;
	    
	    mFramerate = new NvFramerateCounter();
	    mFrameTimer.start();
	    
		super.onSurfaceCreated(egl);
	    baseInitUI();
	}

	@Override
	protected GLSurfaceView createRenderView(NvEGLConfiguration configuration){
		GLSurfaceView view = new GLSurfaceView(this);
		view.setEGLConfigChooser(configuration.redBits, configuration.greenBits, configuration.blueBits, configuration.alphaBits,
				configuration.depthBits, configuration.stencilBits);
		if(configuration.apiVer == NvGfxAPIVersion.GLES1){
			view.setEGLContextClientVersion(1);
		}else{
			ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
			ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
			int major = (configurationInfo.reqGlEsVersion >> 16) & 0xFFFF;
			view.setEGLContextClientVersion(major);
		}
		view.setRenderer(this);
		m_surfaceView = view;
		return view;
	}

	@Override
	protected void onPause() {
		super.onPause();
		m_surfaceView.onPause();
		mPaused = true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		m_surfaceView.onResume();
		mPaused = false;
	}

	@Override
	public final void onDrawFrame(GL10 arg0) {
		if(!mPaused){
			render();
		}else if(isFinishing()){
			if(!mShutDownCalled) {
				shutdownRendering();
				mShutDownCalled = true;
			}
		}
	}

	private void render(){
		mFrameTimer.stop();
		boolean mTestMode = false;
		boolean isExiting = false;

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
					gamepadButtonChanged(1, true);
				}
			}

			draw();
			if (!mTestMode) {
				baseDrawUI();
			}

			if (mFramerate.nextFrame()) {
				// for now, disabling console output of fps as we have on-screen.
				// makes it easier to read USEFUL log output messages.
				NvLogger.i("fps: %.2f", mFramerate.getMeanFramerate());
			}
		}
	}

	/**
	 * Rendering callback.<p>
	 * Called to request the app render a frame at regular intervals when
	 * the app is focused or when force by outside events like a resize
	 */
	protected void draw() { }
	
	private void baseInitUI(){
	    if (mUIWindow == null){
	        final int w = getWidth(), h = getHeight();
	        mUIWindow = new NvUIWindow((float)w, (float)h);
	        mFPSText = new NvUIValueText("", NvUIFontFamily.SANS, w/40.0f * 1.5f, NvUITextAlign.RIGHT, 0.0f, 1, NvUITextAlign.RIGHT, 0);
	        mFPSText.setColor(NvUtils.makefourcc(0x30,0xD0,0xD0,0xB0));
	        mFPSText.setShadow();
	        mUIWindow.add(mFPSText, (float)w-8, 0);

	        if (mTweakBar==null) {
	            mTweakBar = NvTweakBar.createTweakBar(mUIWindow); // adds to window internally.
	            mTweakBar.setVisibility(false);
	            
	            String mAppTitle = getTitle().toString();
	            if (!NvUtils.isEmpty(mAppTitle)) {
	                mTweakBar.addLabel(mAppTitle, true);
	                mTweakBar.addPadding(0.5f);
	            }

	            // for now, app will own the tweakbar tab button
	            float high = mTweakBar.getDefaultLineHeight();
	            NvUIElement[] els = new NvUIElement[3];
	            els[0] = new NvUIGraphic("arrow_blue.dds", 0, 0);
	            els[0].setDimensions(high * 0.8f, high * 0.8f);
	            els[1] = new NvUIGraphic("arrow_blue_left.dds", 0 ,0 );
	            els[1].setDimensions(high * 0.8f, high * 0.8f);
	            els[2] = null;

	            mTweakTab = new NvUIButton(NvUIButtonType.CHECK, NvTweakBar.TWEAKBAR_ACTIONCODE_BASE, els);
	            mTweakTab.setHitMargin(high/2, high/2);
	            mUIWindow.add(mTweakTab, high*0.25f, mTweakBar.getStartOffY()+high*0.2f);
	        }

	    }

	    initUI();
	}
	
	/**
	 * UI init callback.
	 * <p>
	 * Called after rendering is initialized, to allow preparation of overlaid
	 * UI elements
	 */
	public void initUI() {
	}
	
	@Override
	public void onSurfaceChanged(GL10 arg0, int w, int h) {
		mUIWindow.handleReshape(w, h);
		m_transformer.setScreenSize(w, h);
		reshape(w, h);

		width = w;
		height = h;
	}

	public void initCamera(int index, ReadableVector3f eye, ReadableVector3f at) {
		// Construct the look matrix
//	    	    Matrix4f look;
//	    	    lookAt(look, eye, at, nv.vec3f(0.0f, 1.0f, 0.0f));
		Matrix4f look = Matrix4f.lookAt(eye, at, Vector3f.Y_AXIS, null);

		// Decompose the look matrix to get the yaw and pitch.
		float pitch = (float) Math.atan2(-look.m21, /*_32*/ look.m22/*_33*/);
		float yaw = (float) Math.atan2(look.m20/*_31*/, Vector2f.length(-look.m21/*_32*/, look.m22/*_33*/));

		// Initialize the camera view.
		NvInputTransformer m_camera = getInputTransformer();
		m_camera.setRotationVec(new Vector3f(pitch, yaw, 0.0f), index);
		m_camera.setTranslationVec(new Vector3f(look.m30/*_41*/, look.m31/*_42*/, look.m32/*_43*/), index);
		m_camera.update(0.0f);
	}
	
	private void baseDrawUI(){
		if (mUIWindow != null && mUIWindow.getVisibility()) {
	        if (mFPSText != null) {
	            mFPSText.setValue(mFramerate.getMeanFramerate());
	        }
	        long time = 0;
	        NvUIDrawState ds = new NvUIDrawState(time, getWidth(), getHeight());
	        mUIWindow.draw(ds);
	    }

	    drawUI();
	}
	
	private void baseHandleReaction(){
		int r;
		NvUIReaction react = NvUIElement.getReaction();
		// we let the UI handle any reaction first, in case there
	    // are interesting side-effects such as updating variables...
	    r = mUIWindow.handleReaction(react);
	    // then the app is always given a look, even if already handled...
	    //if (r==nvuiEventNotHandled)
	    r = handleReaction(react);
	}

	/**
	 * App-specific UI drawing callback.
	 * <p>
	 * Called to request the app render any UI elements over the frame.
	 */
	public void drawUI() {
	}
	
	/**
	 * Get UI window.
	 * <p>
	 * 
	 * @return a pointer to the UI window
	 */
	public NvUIWindow getUIWindow() {
		return mUIWindow;
	}

	public NvTweakBar getTweakBar() { return mTweakBar;}

	public NvInputTransformer getInputTransformer() {return m_transformer;}

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

	/**
	 * Key binding. Adds a key binding.
	 * 
	 * @param var
	 *            the tweak variable to be bound
	 * @param incKey
	 *            the key to be bound to increment the tweak variable
	 * @param decKey
	 *            the key to be bound to decrement the tweak variable
	 */
	public void addTweakKeyBind(NvTweakVarBase var, int incKey, int decKey/* =0 */) {
//		mKeyBinds[incKey] = NvTweakBind(NvTweakCmd.INCREMENT, var);
		mKeyBinds.put(incKey, new NvTweakBind(NvTweakBind.INCREMENT, var));
	    if (decKey != 0)
//	        mKeyBinds[decKey] = NvTweakBind(NvTweakCmd.DECREMENT, var);
	    	mKeyBinds.put(decKey, new NvTweakBind(NvTweakBind.DECREMENT, var));
	}
	
	/**
	 * Key binding. Adds a key binding.
	 * 
	 * @param var
	 *            the tweak variable to be bound
	 * @param incKey
	 *            the key to be bound to increment the tweak variable
	 * @see #addTweakKeyBind(NvTweakVarBase, int, int)
	 */
	public void addTweakKeyBind(NvTweakVarBase var, int incKey) {
		mKeyBinds.put(incKey, new NvTweakBind(NvTweakBind.INCREMENT, var));
	}
	
	public final boolean keyInput(int code, int action){
		// only do down and repeat for now.
	    if (NvKeyActionType.UP!=action) {
	        NvTweakBind bind = mKeyBinds.get(code);
	        if (bind != null) {
	            // we have a binding.  do something with it.
	            NvTweakVarBase var = bind.mVar;
	            if (var != null) {
	                switch (bind.mCmd) {
	                    case NvTweakBind.RESET:
	                        var.reset();
	                        break;
	                    case NvTweakBind.INCREMENT:
	                        var.increment();
	                        break;
	                    case NvTweakBind.DECREMENT:
	                        var.decrement();
	                        break;
	                    default:
	                        return false;
	                }

	                syncValue(var);
	                // we're done.
	                return true;
	            }
	        }
	    }

	    if (mTweakBar != null && NvKeyActionType.UP!=action) // handle down+repeat as needed
	    {
	        // would be nice if this was some pluggable class we could add/remove more easily like inputtransformer.
	        int r = NvUIEventResponse.nvuiEventNotHandled;
	        switch(code)
	        {
	            case NvKey.K_TAB: {
	                if (NvKeyActionType.DOWN!=action) break; // we don't want autorepeat...
	                NvUIReaction react = NvUIElement.getReactionEdit(true);
	                react.code = NvTweakBar.TWEAKBAR_ACTIONCODE_BASE;
	                react.state = mTweakBar.getVisibility() ? 0 : 1;
	                r = NvUIEventResponse.nvuiEventHandledReaction;
	                break;
	            }
	            case NvKey.K_ARROW_DOWN: {
	                if (NvKeyActionType.DOWN!=action) break; // we don't want autorepeat...
	                r = mUIWindow.handleFocusEvent(NvFocusEvent.MOVE_DOWN);
	                break;
	            }
	            case NvKey.K_ARROW_UP: {
	                if (NvKeyActionType.DOWN!=action) break; // we don't want autorepeat...
	                r = mUIWindow.handleFocusEvent(NvFocusEvent.MOVE_UP);
	                break;
	            }
	            case NvKey.K_ENTER: {
	                if (NvKeyActionType.DOWN!=action) break; // we don't want autorepeat...
	                r = mUIWindow.handleFocusEvent(NvFocusEvent.ACT_PRESS);
	                break;
	            }
	            case NvKey.K_BACKSPACE: {
	                if (NvKeyActionType.DOWN!=action) break; // we don't want autorepeat...
	                r = mUIWindow.handleFocusEvent(NvFocusEvent.FOCUS_CLEAR);
	                break;
	            }
	            case NvKey.K_ARROW_LEFT: {
	                r = mUIWindow.handleFocusEvent(NvFocusEvent.ACT_DEC);
	                break;
	            }
	            case NvKey.K_ARROW_RIGHT: {
	                r = mUIWindow.handleFocusEvent(NvFocusEvent.ACT_INC);
	                break;
	            }
	            default:
	                break;
	        }

	        if ((r&NvUIEventResponse.nvuiEventHandled) != 0)
	        {
	            if ((r&NvUIEventResponse.nvuiEventHadReaction)!=0)
	                baseHandleReaction();
	            return true;
	        }
	    }
	        
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

	/**
	 * Gamepad Button binding. Adds a button binding.
	 * 
	 * @param var
	 *            the tweak variable to be bound
	 * @param incBtn
	 *            the button to be bound to increment the tweak variable
	 * @param decBtn
	 *            the button to be bound to decrement the tweak variable
	 */
	public void addTweakButtonBind(NvTweakVarBase var, int incBtn, int decBtn/*=0*/) {
		mButtonBinds.put(incBtn, new NvTweakBind(NvTweakBind.INCREMENT, var));
		if(decBtn != 0)
			mButtonBinds.put(decBtn, new NvTweakBind(NvTweakBind.DECREMENT, var));
	}
	
	@Override
	public boolean gamepadChanged(int changedPadFlags) {
		return super.gamepadChanged(changedPadFlags);
	}
	
	protected boolean gamepadButtonChanged(int button, boolean down){
		// unimplemented
		
		return false;
	}

	/**
	 * Window size request.
	 * <p>
	 * Allows the app to change the default window size.
	 * <p>
	 * While an app can override this, it is NOT recommended, as the base class
	 * parses the command line arguments to set the window size. Applications
	 * wishing to override this should call the base class version and return
	 * without changing the values if the base class returns true.
	 * <p>
	 * Application must return true if it changes the width or height passed in
	 * Not all platforms can support setting the window size. These platforms
	 * will not call this function
	 * <p>
	 * Most apps should be resolution-agnostic and be able to run at a given
	 * resolution
	 * 
	 * @param size
	 *            the default size is passed in. If the application wishes to
	 *            reuqest it be changed, it should change the value before
	 *            returning true
	 * @return whether the value has been changed. true if changed, false if not
	 */
	public boolean getRequestedWindowSize(Dimension size) {
		return false;
	}

	protected int handleReaction(NvUIReaction react) {
		return NvUIEventResponse.nvuiEventNotHandled;
	}

	/**
	 * Request to update any UI related to a given NvTweakVarBase Allows the
	 * framework to abstract the process by which we call HandleReaction to
	 * notify all the UI elements that a particular variable being tracked has
	 * had some kind of update.
	 * 
	 * @param var
	 *            the variable that changed
	 */
	public void syncValue(NvTweakVarBase var) {
		NvUIReaction react = NvUIElement.getReactionEdit(true);
	    react.code = var.getActionCode();
	    react.flags = NvReactFlag.FORCE_UPDATE;
	    baseHandleReaction();
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

		if (mUIWindow!= null) {
			int giclass = NvInputEventClass.MOUSE; // default to mouse
			int gikind;
			// override for non-mouse device.
			if (device==NvInputDeviceType.STYLUS)
				giclass = NvInputEventClass.STYLUS;
			else if (device==NvInputDeviceType.TOUCH)
				giclass = NvInputEventClass.TOUCH;
			// since not using a heavyweight gesture detection system,
			// determine reasonable kind/state to pass along here.
			if (isButtonEvent)
				gikind = (isDown ? NvGestureKind.PRESS : NvGestureKind.RELEASE);
			else
				gikind = (isDown ? NvGestureKind.DRAG : NvGestureKind.HOVER);
			float x=0, y=0;
			if (count != 0)
			{
				x = points[0].m_x;
				y = points[0].m_y;
			}
			NvGestureEvent gesture = new NvGestureEvent(giclass, gikind, x, y);
			if (isButtonEvent)
			{
				if (isDown)
				{
					startX = x;
					startY = y;
				}
			}
			else if (isDown)
			{
				gesture.x = startX;
				gesture.y = startY;
				gesture.dx = x - startX;
				gesture.dy = y - startY;
			}
			int r = mUIWindow.handleEvent(gesture, time, null);
			if ((r&NvUIEventResponse.nvuiEventHandled) != 0)
			{
				if ((r&NvUIEventResponse.nvuiEventHadReaction) != 0)
					baseHandleReaction();
				return true;
			}
		}

		if (handlePointerInput(device, action, modifiers, count, points))
			return true;
		else
			return m_transformer.processPointer(device, action, modifiers, count, points);
	}

	/** Convience method used to create <code>FieldControl</code>*/
	protected final FieldControl createControl(String varname){
		return new FieldControl(this, varname);
	}

	/** Convience method used to create <code>FieldControl</code>*/
	public final static FieldControl createControl(Object obj, String varname){
		return new FieldControl(obj, varname);
	}

}
