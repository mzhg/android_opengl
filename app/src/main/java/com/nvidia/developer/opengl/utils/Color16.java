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

/** 16 bit 565 BGR color. */
final class Color16 {

	public static final int RED_MASK = Integer.parseInt("1111100000000000", 2);
	public static final int GREEN_MASK = Integer.parseInt("11111100000", 2);
	public static final int BLUE_MASK = Integer.parseInt( "11111", 2);
	public short u;
	
	public Color16() {
	}

	public Color16(short u) {
		this.u = u;
	}
	
	public Color16(Color16 c){
		u = c.u;
	}
	
	public void set(short u) {
		this.u = u;
	}
	
	public void set(Color16 c){
		u = c.u;
	}
	
	public int getB(){
		return u & BLUE_MASK ;
	}
	
	public int getG(){
		return (u & GREEN_MASK) >> 5;
	}
	
	public int getR(){
		return (u & RED_MASK) >> 11;
	}
	
	public int getColor(){
		return u;
	}
}
