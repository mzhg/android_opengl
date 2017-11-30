package com.antvr.antvrsdk;

import android.content.Context;

import com.antvr.antvrsdk.sensor.HeadTracker;
import com.antvr.antvrsdk.sensor.HeadTransform;

/**
 * ANTVR SDK
 * @author ANTVR-D24
 *
 */
public class AntvrSDK{
	HeadTransform mHeadTransform;
	HeadTracker mHeadTracker;
	public float[] mEulerAngles = new float[3];
	public float[] mQuaternion = new float[4];
	public float[] mRotationMatrix = new float[16];
	
	private Context ctx;
	private static AntvrSDK mAntvrSDK = null;
	
	float[] pre_Value = new float[16];
	float[] after_Value = new float[16];
	
	public static synchronized AntvrSDK getInstance(Context context) {
            mAntvrSDK = new AntvrSDK(context);
	    return mAntvrSDK;
	}
	public static synchronized AntvrSDK getInstance(){
		if(mAntvrSDK==null){
			mAntvrSDK = new AntvrSDK();
		}
		return mAntvrSDK;
	}
	
	public static synchronized void destroyInstance() {
		if (mAntvrSDK != null) mAntvrSDK = null;
	}
	
	public AntvrSDK(Context context){
		this.ctx = context;
	}
	public AntvrSDK(){}
	
	public void setContext(Context context){
		this.ctx = context;
	}
	
	public void onResume(){
    	mHeadTracker = HeadTracker.createFromContext(ctx);
		mHeadTracker.startTracking();
		mHeadTransform = new HeadTransform();
	}
	
	public void onStart(){
    	mHeadTracker = HeadTracker.createFromContext(ctx);
		mHeadTracker.startTracking();
		mHeadTransform = new HeadTransform();
	}
	
	public void onPause(){
    	mHeadTracker.stopTracking();
	}
	
	public void onStop(){
    	mHeadTracker.stopTracking();
	}
	
	public void onDestroy() {
		mHeadTracker.stopTracking();
	}
	/**
	 * return EulerAngles
	 * @return mEulerAngles
	 */
	public float[] getEulerAngles(){
		mHeadTracker.getLastHeadView(mHeadTransform.getHeadView(), 0);
		mHeadTransform.getEulerAngles(mEulerAngles, 0);
		return mEulerAngles;
	}
	
	/**
	 * return RotationMatrix
	 * @return mHeadTransform.getHeadView()
	 */
	public float[] getRotationMatrix(){
		//Log.e("panjieJ","im from antvr sdk getRotationMatrix");
		mHeadTracker.getLastHeadView(mHeadTransform.getHeadView(), 0);
		/*
		float[] result = mHeadTransform.getHeadView();
		// filter the sensor_fusion value
		for (int i = 0; i < result.length; i++) {
			after_Value[i] = filterValue(pre_Value[i], result[0]);
			pre_Value[i] = after_Value[i];
		}
		*/
		return mHeadTransform.getHeadView();
		//return after_Value;
	}
	
	/**
	 * return Quaternion
	 * @return mQuaternion
	 */
	public float[] getQuaternion(){
		mHeadTracker.getLastHeadView(mHeadTransform.getHeadView(), 0);
		mHeadTransform.getQuaternion(mQuaternion, 0);
		return mQuaternion;
	}
	
	/**
	 * Filter the value from sensor fusion
	 * @param pre_Value
	 * @param current_Value
	 * @return
	 */
	private float filterValue(float pre_Value, float current_Value){
		float after_Value = 0;
		float threshold = 80f;
		float e = (float) Math.exp(-(Math.abs(after_Value-current_Value))/threshold);
		after_Value = pre_Value * e + current_Value * (1-e);
		return after_Value;
	}
}
