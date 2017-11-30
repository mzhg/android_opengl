package com.antvr.antvrsdk.sensor;

public class SystemClock implements Clock {
    @Override
    public long nanoTime() {
        return System.nanoTime();
    }
}
