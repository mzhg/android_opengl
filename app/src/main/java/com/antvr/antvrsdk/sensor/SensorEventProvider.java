package com.antvr.antvrsdk.sensor;

import android.hardware.*;

public interface SensorEventProvider {
    void start();
    
    void stop();
    
    void registerListener(SensorEventListener p0);
    
    void unregisterListener(SensorEventListener p0);
}
