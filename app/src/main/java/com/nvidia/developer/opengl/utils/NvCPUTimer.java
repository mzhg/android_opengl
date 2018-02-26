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

public class NvCPUTimer {

	protected NvStopWatch m_stopwatch;
	protected float m_elapsedTime;
	
	/** Initializes the timer. */
	public void init(){
		m_stopwatch = new NvStopWatch();
	}
	
	public void reset(){
		m_elapsedTime = 0.0f;
	}
	
	public void start(){
		m_stopwatch.start();
	}
	
	public void stop(){
		m_stopwatch.stop();
		m_elapsedTime += m_stopwatch.getTime();
	}
	
	public float getScaledCycles(){
		return m_elapsedTime;
	}
}
