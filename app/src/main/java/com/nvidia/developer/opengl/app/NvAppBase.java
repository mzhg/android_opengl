package com.nvidia.developer.opengl.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.pm.ConfigurationInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLES20;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Pair;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.Glut;
import com.nvidia.developer.opengl.utils.NvAssetLoader;
import com.nvidia.developer.opengl.utils.NvGfxAPIVersion;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public abstract class NvAppBase extends Activity implements NvInputCallbacks, SensorEventListener {

	protected static final float PI = (float)Math.PI;
	
	private View m_surfaceView;
    private int width, height;
    
    private NvEGLConfiguration glConfig;
	private NvInputHandler mInputHandler;
	
    private Thread uiTread;

	private SensorManager mSensorManager;
	private Sensor mRotVectSensor;
	private final float[] mRotationMatrix = new float[16];
	private final float[] orientationVals = new float[16];
	private boolean mSensorEnabled;

	private static final int MSG_EXCEPTION = 0;
	private static final int UI_TASK = 1;
	private static final int TYPE_ROTATION_VR = Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR;

	private static boolean g_HaveNewTask = false;
	private static final Object g_Lock = new Object();
	private static final List<Pair<Integer, Object>> g_UITaskResults = new ArrayList<>();
	private static final List<Pair<Integer, Object>> g_UITaskPoster = new ArrayList<>();

	private static Handler g_Hanlder;

	static {
		g_Hanlder = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case MSG_EXCEPTION:
						throw (RuntimeException) msg.obj;
					case UI_TASK:
					{
						synchronized (g_Lock){
							UIThreadTask task = (UIThreadTask)msg.obj;
							Pair<Integer, Object> result = task.doUIThreadTask();
							if(result != null){
								g_UITaskResults.add(result);
								g_HaveNewTask = true;
							}
						}
					}
						break;
				}
			}
		};
	}

	// This class is not smart, later I'll complete it.
	public interface UIThreadTask/*<UIObj, GLObj>*/{

		Pair<Integer, Object> doUIThreadTask();

//		GLObj doOGLThreadTask(Pair<Integer, UIObj> uiThreadResult);
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

		ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();

		glConfig = new NvEGLConfiguration(NvGfxAPIVersion.GLES2);
		configurationCallback(glConfig);

		if(configurationInfo.reqGlEsVersion >= 0x20000 && isRequreOpenGLES2()){
			GLES.useES2 = true;
		}else{
			glConfig.apiVer = NvGfxAPIVersion.GLES1;
			GLES.useES2 = false;
		}

		m_surfaceView = createRenderView(glConfig);
		m_surfaceView.setFocusableInTouchMode(true);

		mInputHandler = new NvInputHandler(m_surfaceView);
		mInputHandler.setInputListener(this);

		setContentView(m_surfaceView);
		m_surfaceView.requestFocus();

		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mRotVectSensor =
				mSensorManager.getDefaultSensor(TYPE_ROTATION_VR);
		android.opengl.Matrix.setIdentityM(mRotationMatrix, 0);
	}

	protected abstract View createRenderView(NvEGLConfiguration configuration);

	public void enableSensor(){
		mSensorEnabled = true;
		mSensorManager.registerListener(this, mRotVectSensor, SensorManager.SENSOR_DELAY_GAME);
	}

	public void disableSensor(){
		mSensorEnabled = false;
		mSensorManager.unregisterListener(this);
		android.opengl.Matrix.setIdentityM(mRotationMatrix, 0);
	}

	//
	public final void addUITask(UIThreadTask task) {
		Message msg = g_Hanlder.obtainMessage(UI_TASK, task);
		g_Hanlder.sendMessage(msg);
	}

	protected boolean isRequreOpenGLES2() { return true;}
	
	public final int getWidth(){
		return width;
	}
	
	public int getHeight(){
		return height;
	}
	
	protected void initBeforeGL(){
	}
	
	public boolean isExtensionSupported(String ext){
		return false;
	}



	/**
	 * Pull all of the events, sub-class must be call this method every frame.
	 */
	public void pollEvents(){
		mInputHandler.pollEvents();

		// To avoid the unnecessary synchronize.
		if(!g_HaveNewTask){
			return;
		}

		g_HaveNewTask = false;
		synchronized (g_Lock){
			g_UITaskPoster.addAll(g_UITaskResults);
			g_UITaskResults.clear();
		}

		for(Pair<Integer, Object> result : g_UITaskPoster){
			onUITaskResult(result.first, result.second);
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
	
	protected void onUITaskResult(int action, Object result){

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
	
	public void onSurfaceCreated(EGLConfig config) {
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

	public boolean isKeyPressd(int keyCode){
		return mInputHandler.isKeyPressd(keyCode);
	}

	public boolean isTouchDown(int pointer){
		return mInputHandler.isTouchDown(pointer);
	}

	public int getTouchX(int pointer){
		return mInputHandler.getTouchX(pointer);
	}

	public int getTouchY(int pointer){
		return mInputHandler.getTouchY(pointer);
	}
	
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

	@Override
	protected void onResume() {
		super.onResume();

		if(mSensorEnabled){
			mSensorManager.registerListener(this, mRotVectSensor, SensorManager.SENSOR_DELAY_GAME);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if(mSensorEnabled){
			mSensorManager.unregisterListener(this);
		}
	}

	public float[] getRotationMatrix(){return mRotationMatrix;}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// It is good practice to check that we received the proper sensor event
		if (event.sensor.getType() == TYPE_ROTATION_VR)
		{
			// Convert the rotation-vector to a 4x4 matrix.
			SensorManager.getRotationMatrixFromVector(mRotationMatrix,
					event.values);
//			SensorManager
//					.remapCoordinateSystem(mRotationMatrix,
//							SensorManager.AXIS_Y, SensorManager.AXIS_Z,
//							mRotationMatrix);
			SensorManager.getOrientation(mRotationMatrix, orientationVals);

			// Optionally convert the result from radians to degrees TODO randians is ok.
//            orientationVals[0] = (float) Math.toDegrees(orientationVals[0]);
//            orientationVals[1] = (float) Math.toDegrees(orientationVals[1]);
//            orientationVals[2] = (float) Math.toDegrees(orientationVals[2]);
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
