////////////////////////////////////////////////////////////////////////////////
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

import org.lwjgl.util.vector.Vector2b;

public class Pixels2 extends Pixels{

	public Pixels2(int width, int height) {
		super(width, height, 2);
	}
	
	public Vector2b get(int w, int h){
		final int index = (h * width + w) * 2;
		Vector2b tmp = new Vector2b();
		tmp.x = buffer.get(index + 0);
		tmp.y = buffer.get(index + 1);
		return tmp;
	}
	
	public void set(int w, int h, Vector2b value){
		final int index = (h * width + w) * 2;
		buffer.put(index + 0, value.x);
		buffer.put(index + 1, value.y);
	}
	
	public void assgin(int dstW, int dstH, int srcW, int srcH){
		final int dstIndex = (dstH * width + dstW) * 2;
		final int srcIndex = (srcH * width + srcW) * 2;
		
		buffer.put(dstIndex + 0, buffer.get(srcIndex + 0));
		buffer.put(dstIndex + 1, buffer.get(srcIndex + 1));
	}
	
	public void swap(int w1, int h1, int w2, int h2){
		final int index1 = (h1 * width + w1) * 2;
		final int index2 = (h2 * width + w2) * 2;
		
		final byte v1x = buffer.get(index1 + 0);
		final byte v1y = buffer.get(index1 + 1);
		final byte v2x = buffer.get(index2 + 0);
		final byte v2y = buffer.get(index2 + 1);
		
		buffer.put(index1 + 0, v2x);
		buffer.put(index1 + 1, v2y);
		buffer.put(index2 + 0, v1x);
		buffer.put(index2 + 1, v1y);
	}
	
}
