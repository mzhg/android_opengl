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

/** 32 bit color stored as RGBA */
public class Color32 {

	public int u;
	
	/**
	 * every value must be clamp to [0, 255].
	 * @param r
	 * @param g
	 * @param b
	 * @param a
	 */
	public void set(int r, int g, int b, int a){
		u = NvUtils.makefourcc(r, g, b, a);
	}
	
	/**
	 * Get the red component of the color32
	 * @return the red component, range from 0 to 255.
	 */
	public int getR(){
		return u & 0xFF;
	}
	
	/**
	 * Get the green component of the color32
	 * @return the green component, range from 0 to 255.
	 */
	public int getG(){
		return (u >> 8) & 0xFF;
	}
	
	/**
	 * Get the blue component of the color32
	 * @return the blue component, range from 0 to 255.
	 */
	public int getB(){
		return (u >> 16) & 0xFF;
	}
	
	/**
	 * Get the alpha component of the color32
	 * @return the alpha component, range from 0 to 255.
	 */
	public int getA(){
		return (u >> 24) & 0xFF;
	}
}
