////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2014, NVIDIA CORPORATION. All rights reserved.
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
package com.nvidia.developer.opengl.utils;


public class NvStopWatch {

	protected boolean m_running;
	
	private long start_time;
	private long end_time;
	
	/** Time difference between the last start and stop */
	private float diff_time;
	/** tick frequency */
	private double freq;
	
	/** Test construct */
	public NvStopWatch(){
		freq = 1000;
	}
	
	/** Starts time measurement */
	public void start(){
		start_time = _getTime();
		m_running = true;
	}
	
	/** Stop time measurement */
	public void stop(){
		end_time = _getTime();
		diff_time = (float)(((double) end_time - (double) start_time) / freq);
	}
	
	/** Reset time counters to zero */
	public void reset(){
		diff_time = 0;
		if(m_running)
			start_time = _getTime();
	}
	
	private final long _getTime(){
		return System.currentTimeMillis();
	}
	
	/**
	 * Get elapsed time<p>
	 * Time in seconds after start. If the stop watch is still running (i.e. there
     * was no call to #stop()) then the elapsed time is returned, otherwise the
     * summed time between all {@link #start()} and {@link #stop()} calls is returned
	 * @return The elapsed time in seconds
	 */
	public float getTime(){
		if(m_running)
			return getDiffTime();
		else
			return diff_time;
	}
	
	/** Get difference between start time and current time */
	private float getDiffTime(){
		long temp = _getTime();
		return (float)  (((double) temp - (double) start_time) / freq);
	}
	
	/**
	 * Test whether the timer is running
	 * @return true if the timer is running (between {@link #start()} and {@link #stop()} calls) and false if not
	 */
	public boolean isRunning(){return m_running;};
}
