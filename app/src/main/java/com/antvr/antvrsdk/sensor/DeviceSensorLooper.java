package com.antvr.antvrsdk.sensor;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import java.util.ArrayList;

public class DeviceSensorLooper implements SensorEventProvider {
    private boolean mIsRunning;
    private SensorManager mSensorManager;
    private Looper mSensorLooper;
    private SensorEventListener mSensorEventListener;
    private final ArrayList<SensorEventListener> mRegisteredListeners;
    private static final int[] INPUT_SENSORS;
    
    public DeviceSensorLooper(final SensorManager sensorManager) {
        super();
        this.mRegisteredListeners = new ArrayList<SensorEventListener>();
        this.mSensorManager = sensorManager;
    }
    
    @Override
    public void start() {
        if (this.mIsRunning) {
            return;
        }
        this.mSensorEventListener = (SensorEventListener)new SensorEventListener() {
            public void onSensorChanged(final SensorEvent event) {
                for (final SensorEventListener listener : DeviceSensorLooper.this.mRegisteredListeners) {
                    synchronized (listener) {
                        listener.onSensorChanged(event);
                    }
                }
            }
            
            public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
                for (final SensorEventListener listener : DeviceSensorLooper.this.mRegisteredListeners) {
                    synchronized (listener) {
                        listener.onAccuracyChanged(sensor, accuracy);
                    }
                }
            }
        };
        final HandlerThread sensorThread = new HandlerThread("sensor") {
            protected void onLooperPrepared() {
                final Handler handler = new Handler(Looper.myLooper());
                for (final int sensorType : DeviceSensorLooper.INPUT_SENSORS) {
                    final Sensor sensor = DeviceSensorLooper.this.mSensorManager.getDefaultSensor(sensorType);
                    DeviceSensorLooper.this.mSensorManager.registerListener(DeviceSensorLooper.this.mSensorEventListener, sensor, 0, handler);
                }
            }
        };
        sensorThread.start();
        this.mSensorLooper = sensorThread.getLooper();
        this.mIsRunning = true;
    }
    
    @Override
    public void stop() {
        if (!this.mIsRunning) {
            return;
        }
        this.mSensorManager.unregisterListener(this.mSensorEventListener);
        this.mSensorEventListener = null;
        this.mSensorLooper.quit();
        this.mSensorLooper = null;
        this.mIsRunning = false;
    }
    
    @Override
    public void registerListener(final SensorEventListener listener) {
        synchronized (this.mRegisteredListeners) {
            this.mRegisteredListeners.add(listener);
        }
    }
    
    @Override
    public void unregisterListener(final SensorEventListener listener) {
        synchronized (this.mRegisteredListeners) {
            this.mRegisteredListeners.remove(listener);
        }
    }
    
    static {
        INPUT_SENSORS = new int[] { 1, 4 };
    }
}
