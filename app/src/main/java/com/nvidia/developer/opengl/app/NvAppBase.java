package com.nvidia.developer.opengl.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;

import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.Glut;
import com.nvidia.developer.opengl.utils.NvAssetLoader;
import com.nvidia.developer.opengl.utils.NvGfxAPIVersion;
import com.nvidia.developer.opengl.utils.Pool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class NvAppBase extends Activity implements GLSurfaceView.Renderer, NvInputCallbacks{

	protected static final float PI = (float)Math.PI;
	
	private GLSurfaceView m_surfaceView;
    private int width, height;
    
    private boolean[] pressedKeys = new boolean[128];
    private _OnKeyListener keyListener;
    
    private final int[] touchX = new int[20];
    private final int[] touchY = new int[20];
    private final boolean[] isTouched = new boolean[20];
    private final NvPointerEvent[] p = new NvPointerEvent[20];
    private final NvPointerEvent[][] specialEvents = new NvPointerEvent[6][12];
    private int mainCursor;
    private final int[] subCursor = new int[6];
    private final int[] eventType = new int[6];
    private _OnTouchListener touchListener;
	
    private NvEGLConfiguration glConfig;
	
    private Thread uiTread;

	private static final int MSG_EXCEPTION = 0;

	private static Handler g_Hanlder;

	static {
		g_Hanlder = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case MSG_EXCEPTION:
						throw (RuntimeException) msg.obj;
				}
			}
		};
	}

	public static void throwExp(RuntimeException throwable){
		Message msg = Message.obtain();
		msg.what = MSG_EXCEPTION;
		msg.obj = throwable;
		g_Hanlder.sendMessage(msg);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		Display display = getWindow().getWindowManager().getDefaultDisplay();
		width = display.getWidth();
		height = display.getHeight();

		Glut.init(getAssets());
		initBeforeGL();
		
		NvAssetLoader.init(getAssets());
		uiTread = Thread.currentThread();
		keyListener = new _OnKeyListener();
		touchListener = new _OnTouchListener();
		
		m_surfaceView = new GLSurfaceView(this);
		m_surfaceView.setOnKeyListener(keyListener);
		m_surfaceView.setOnTouchListener(touchListener);
		m_surfaceView.setFocusableInTouchMode(true);
		
		ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
		
		if(configurationInfo.reqGlEsVersion >= 0x20000){
			int major = (configurationInfo.reqGlEsVersion >> 16) & 0xFFFF;
			m_surfaceView.setEGLContextClientVersion(major);
			glConfig = new NvEGLConfiguration(NvGfxAPIVersion.GLES2);
			
			configurationCallback(glConfig);
			
			m_surfaceView.setEGLConfigChooser(glConfig.redBits, glConfig.greenBits, glConfig.blueBits, glConfig.alphaBits, glConfig.depthBits, glConfig.stencilBits);
		}else{
			showDialog("OpenGL锟斤拷锟斤拷", "锟斤拷锟斤拷只锟街э拷锟絆penGL ES 2.0!!!", true);
		}
		
		m_surfaceView.setRenderer(this);
		setContentView(m_surfaceView);
		m_surfaceView.requestFocus();
	}
	
	public final int getWidth(){
		return width;
	}
	
	public boolean isKeyPressd(int keyCode){
		return (keyCode < 0 || keyCode > 127) ? false : pressedKeys[keyCode];
	}
	
	public boolean isTouchDown(int pointer){
		return (pointer < 0 || pointer >=20) ? false :isTouched[pointer];
	}
	
	public int getTouchX(int pointer){
		return (pointer < 0 || pointer >=20) ? 0 :touchX[pointer];
	}
	
	public int getTouchY(int pointer){
		return (pointer < 0 || pointer >=20) ? 0 :touchX[pointer];
	}
	
	public int getHeight(){
		return height;
	}
	
	protected void initBeforeGL(){
	}
	
	@Override
	public void onDrawFrame(GL10 arg0) {
		draw();
	}
	
	public boolean isExtensionSupported(String ext){
		return false;
	}
	
	public void pollEvents(){
	   List<_KeyEvent> events = keyListener.getKeyEvents();
	   for(int i = 0; i < events.size(); i++){
		   boolean handled = false;
		   _KeyEvent e = events.get(i);
	       int code = e.keyCode;
	       boolean down = e.down;
	       
	       handled = keyInput(code, down ? NvKeyActionType.DOWN : NvKeyActionType.UP);
	       if(!handled && down){
	    	   char c = e.keyChar;
	    	   if(c != 0)
	    		   characterInput(c);
	       }
	   }
	   String actionName;
//		
//		if(action == NvPointerActionType.DOWN)
//			actionName = "DOWN";
//		else if(action == NvPointerActionType.UP)
//			actionName = "UP";
//		else if(action == NvPointerActionType.MOTION)
//			actionName = "MOTION";
//		else if(action == NvPointerActionType.EXTRA_DOWN)
//			actionName = "EXTRA_DOWN";
//		else if(action == NvPointerActionType.EXTRA_UP)
//			actionName = "EXTRA_UP";
//		else
//			actionName = "UNKOWN";
//		
//		Log.e("processPointer", "action: " + act
	   List<NvPointerEvent> pEvents = touchListener.getTouchEvents();
	   if(pEvents.size() > 0){
		   int pointerCount = pEvents.size();
		   NvPointerEvent p = pEvents.get(0);
		   
//		   pointerInput(NvInputDeviceType.TOUCH, pact, 0, pointerCount, pEvents.toArray(this.p));
		   splitEvents(pEvents);
		   
		   for(int i = 0; i <= mainCursor; i++){
			   pointerInput(NvInputDeviceType.TOUCH, eventType[i], 0, subCursor[i], specialEvents[i]);
		   }
	   }
	}
	
	private final void splitEvents(List<NvPointerEvent> pEvents){
		mainCursor = -1;
		Arrays.fill(subCursor, 0);

		int size = pEvents.size();
		int lastType = -1;
		for(int i = 0; i < size; i++){
			NvPointerEvent event = pEvents.get(i);
			
			if(event.type !=lastType){
				lastType = event.type;
				mainCursor ++;
				
//				Log.e("splitEvents", "mainCursour = " + mainCursor);
				int pact = 0;
				   switch (event.type) {
				case MotionEvent.ACTION_CANCEL:
					pact = NvPointerActionType.UP;
					break;
				case MotionEvent.ACTION_MOVE:
					pact = NvPointerActionType.MOTION;
					break;
				case MotionEvent.ACTION_DOWN:
					pact = NvPointerActionType.DOWN;
					break;
				case MotionEvent.ACTION_POINTER_DOWN:
					pact = NvPointerActionType.EXTRA_DOWN;
				    break;
				case MotionEvent.ACTION_UP:
					pact = NvPointerActionType.UP;
					break;
				case MotionEvent.ACTION_POINTER_UP:
					pact = NvPointerActionType.EXTRA_UP;
					break;
				default:
					break;
				}
				
//				   Log.e("splitEvents", "pack = " + pact);
				   eventType[mainCursor] = pact;
			}
			
			specialEvents[mainCursor][subCursor[mainCursor] ++] = event;
		}
	}
	
	/**
	 * Initialize rendering.<p>
	 * Called once the GLES context and surface have been created and bound
     * to the main thread.  Called again if the context is lost and must be
     * recreated.
	 */
	protected void initRendering(){
	}
	
	/**
	 * Shutdown rendering.<p>
	 * Called when the GLES context has just been shut down; it indicates that
     * all GL resources in the app's context have been deleted and are invalid, 
     * and will need to be recreated on the next call to initRendering.  This 
     * function should also be used to shut down any secondary threads that 
     * generate GL calls such as buffer mappings.<p>
     *
     * Because the sequence of shutdownRendering/initRendering may be called 
     * without the app being completely shut down (e.g. lost context), the app
     * needs to use this to delete non-GL resources (e.g. system memory related 
     * to 3D resources) and indicate that it needs to reload any GL resources on
     * initRendering
	 */
	protected void shutdownRendering(){
		
	}
	
	/**
	 * Rendering callback.<p>
	 * Called to request the app render a frame at regular intervals when
     * the app is focused or when force by outside events like a resize
	 */
	protected void draw() {
		
	}
	
	/**
	 * Resize callback.<p>
	 * Called when the main rendering window changes dimensions and before
	 * @param width the new window width in pixels
	 * @param height the new window height in pixels
	 */
	protected void reshape(int width, int height){
		
	}
	
	/**
	 * Shows a dialog and, if desired, exits the app on dialog close
	 * @param title a string with the title for the dialog
	 * @param contents a string with the text to be shown in the dialog
	 * @param exitApp if true, the app will exit when the dialog is closed. Useful for error dialogs
	 * @return true on success, false on failure
	 */
	protected boolean showDialog(String title, String contents, boolean exitApp){
		
		return false;
	}
	
	public boolean handlePointerInput(int device, int action, int modifiers,
			int count, NvPointerEvent[] points) {
		return false;
	}

	public boolean handleKeyInput(int code, int action) {
		return false;
	}

	public boolean handleCharacterInput(char c) {
		return false;
	}
	
	/**
	 * GL configuration request callback.<p>
	 * This function passes in the default set of GL configuration requests.<br>
	 * The app can override this function and change the defaults before
     * returning.  These are still requests, and may not be met.  If the
     * platform supports requesting GL options, this function will be called
     * before initGL.  Optional.
	 * @param config the default config to be used is passed in.  If the application
	 *  wishes anything different in the GL configuration, it should change those values before
	 *  returning from the function.  These are merely requests.
	 */
	protected void configurationCallback(NvEGLConfiguration config){}
	
	@Override
	public void onSurfaceChanged(GL10 arg0, int width, int height) {
		reshape(width, height);
	}
	
	@Override
	public void onSurfaceCreated(GL10 arg0, EGLConfig config) {
		glConfig.redBits = GLES.glGetInteger(GL10.GL_RED_BITS);
		glConfig.greenBits = GLES.glGetInteger(GL10.GL_GREEN_BITS);
		glConfig.blueBits = GLES.glGetInteger(GL10.GL_BLUE_BITS);
		glConfig.alphaBits = GLES.glGetInteger(GL10.GL_ALPHA_BITS);
		glConfig.depthBits = GLES.glGetInteger(GL10.GL_DEPTH_BITS);
		glConfig.stencilBits = GLES.glGetInteger(GL10.GL_STENCIL_BITS);
		
		String version = GLES20.glGetString(GLES20.GL_VERSION);
		String vendor = GLES20.glGetString(GLES20.GL_VENDOR);
		String ext = GLES20.glGetString(GLES20.GL_EXTENSIONS);
		
		Log.e("OpenGL ES", "Real Version: " + version);
		Log.e("OpenGL ES", "Vendor: " + vendor);
		Log.e("OpenGL ES", "Extensions: " + ext);
		
		initRendering();
	}
	
	/// Linker hack.
    /// An empty function that ensures the linker does not strip the framework
    // Function must be called in the concrete app subclass constructor to avoid link issues
    protected void forceLinkHack(){}

	@Override
	public boolean pointerInput(int device, int action, int modifiers,
			int count, NvPointerEvent[] points) {
		return false;
	}

	@Override
	public boolean keyInput(int code, int action) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean characterInput(char c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean gamepadChanged(int changedPadFlags) {
		// TODO Auto-generated method stub
		return false;
	}
	
	private static final class _KeyEvent{
		boolean down;
		int keyCode;
		char keyChar;
	}
	
	private final class _OnTouchListener implements OnTouchListener{
		Pool<NvPointerEvent> touchEventPool;
		List<NvPointerEvent> touchEvents = new ArrayList<NvPointerEvent>();
		List<NvPointerEvent> touchEventsBuffer = new ArrayList<NvPointerEvent>();
		
		public _OnTouchListener() {
			Pool.PoolObjectFactory<NvPointerEvent> factory = new Pool.PoolObjectFactory<NvPointerEvent>() {
				@Override
				public NvPointerEvent createObject() {
					return new NvPointerEvent();
				}
			};
			
			touchEventPool = new Pool<NvPointerEvent>(factory, 100);
		}
		
		@Override
		public boolean onTouch(View arg0, MotionEvent event) {
			synchronized (this) {
				int action = event.getAction() & MotionEvent.ACTION_MASK;
				int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_ID_MASK)
						>> MotionEvent.ACTION_POINTER_ID_SHIFT;
				int pointerId = event.getPointerId(pointerIndex);
				
				NvPointerEvent touchEvent;
				switch(action){
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_POINTER_DOWN:
					touchEvent = touchEventPool.newObject();
					touchEvent.type = action;
					touchEvent.m_id = pointerId;
					touchEvent.m_x = touchX[pointerId] = (int) (event.getX(pointerIndex) + 0.5f);
					touchEvent.m_y = touchY[pointerId] = (int) (event.getY(pointerIndex) + 0.5f);
					isTouched[pointerId] = true;
					touchEventsBuffer.add(touchEvent);
					break;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_POINTER_UP:
					touchEvent = touchEventPool.newObject();
					touchEvent.type = action;
					touchEvent.m_id = pointerId;
					touchEvent.m_x = touchX[pointerId] = (int) (event.getX(pointerIndex) + 0.5f);
					touchEvent.m_y = touchY[pointerId] = (int) (event.getY(pointerIndex) + 0.5f);
					isTouched[pointerId] = false;
					touchEventsBuffer.add(touchEvent);
					break;
				case MotionEvent.ACTION_MOVE:
					int pointerCount = event.getPointerCount();
					for(int i = 0; i < pointerCount; i++){
						pointerIndex = i;
						pointerId = event.getPointerId(pointerIndex);
						
						touchEvent = touchEventPool.newObject();
						touchEvent.type = action;
						touchEvent.m_id = pointerId;
						touchEvent.m_x = touchX[pointerId] = (int) (event.getX(pointerIndex) + 0.5f);
						touchEvent.m_y = touchY[pointerId] = (int) (event.getY(pointerIndex) + 0.5f);
						touchEventsBuffer.add(touchEvent);
					}
					break;
				}
				return true;
			}
		}
		
		List<NvPointerEvent> getTouchEvents(){
			synchronized (this) {
				int len = touchEvents.size();
				for(int i = 0; i < len; i++)
					touchEventPool.freeObject(touchEvents.get(i));
				
				touchEvents.clear();
				touchEvents.addAll(touchEventsBuffer);
				touchEventsBuffer.clear();
				return touchEvents;
			}
		}
	};
	
	private final class _OnKeyListener implements OnKeyListener{
		
		Pool<_KeyEvent> keyEventPool;
		List<_KeyEvent> keyEventsBuffer = new ArrayList<NvAppBase._KeyEvent>();
		List<_KeyEvent> keyEvents = new ArrayList<NvAppBase._KeyEvent>();
		
		public _OnKeyListener() {
			Pool.PoolObjectFactory<_KeyEvent> factory = new Pool.PoolObjectFactory<NvAppBase._KeyEvent>() {
				public _KeyEvent createObject() {
					return new _KeyEvent();
				}
			};
			
			keyEventPool = new Pool<NvAppBase._KeyEvent>(factory, 100);
		}
		
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if(event.getAction() == KeyEvent.ACTION_MULTIPLE)
				return false;
			
			synchronized (this) {
				_KeyEvent keyEvent = keyEventPool.newObject();
				keyEvent.keyCode = keyCode;
				keyEvent.keyChar = (char)event.getUnicodeChar();
				keyEvent.down = event.getAction() == KeyEvent.ACTION_DOWN;
				
				if(keyCode > 0 && keyCode <127)
					pressedKeys[keyCode] = keyEvent.down;
				
				keyEventsBuffer.add(keyEvent);
			}
			
			return false;
		}
		
		List<_KeyEvent> getKeyEvents(){
			synchronized (this) {
				int len = keyEvents.size();
				for(int i = 0; i < len; i++)
					keyEventPool.freeObject(keyEvents.get(i));
				
				keyEvents.clear();
				keyEvents.addAll(keyEventsBuffer);
				keyEventsBuffer.clear();
				return keyEvents;
			}
		}
	};
	
}
