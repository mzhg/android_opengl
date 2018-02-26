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

import org.lwjgl.util.vector.ReadableVector3f;
import org.lwjgl.util.vector.Vector3f;

public final class NvPackedColor {

	/** A predefined constant for WHITE. */
	public static final int NV_PC_PREDEF_WHITE = 0xFFFFFFFF;
	/** A predefined constant for BLACK. */
	public static final int NV_PC_PREDEF_BLACK = 0xFF000000;

	public static final ReadableVector3f WHITE = new Vector3f(1, 1, 1);
	public static final ReadableVector3f BLACK = new Vector3f(0,0,0);
	public static final ReadableVector3f RED = new Vector3f(1, 0, 0);
	public static final ReadableVector3f GREEN = new Vector3f(0, 1, 0);
	public static final ReadableVector3f BLUE = new Vector3f(0, 0, 1);
	public static final ReadableVector3f YELLOW = new Vector3f(1, 1, 0);
	public static final ReadableVector3f CYAN = new Vector3f(0, 1, 1);
	public static final ReadableVector3f MAGENTA = new Vector3f(1, 0, 1);
	public static final ReadableVector3f SILVER = new Vector3f(0.75f, 0.75f, 0.75f);
	public static final ReadableVector3f LIGHT_STEEL_BLUE = new Vector3f(0.69f, 0.77f, 0.87f);
	
	/** Extracting the red value from an packed color which is in rgb(a) form. */
	public static final int getRedFromRGB(int rgb){
		return rgb & 0xFF;
	}
	
	/** Extracting the red value from an packed color which is in bgr(a) form. */
	public static final int getRedFromBGR(int bgr){
		return (bgr >> 16) & 0xFF;
	}
	
	/** Extracting the green value from an packed color no matter in rgb(a) or bgr(a) form. */
	public static final int getGreen(int rgb){
		return (rgb >> 8) & 0xFF;
	}
	
	/** Extracting the blue value from an packed color which is in rgb(a) form. */
	public static final int getBlueFromRGB(int rgb){
		return (rgb >> 16) & 0xFF;
	}
	
	/** Extracting the blue value from an packed color which is in bgr(a) form. */
	public static final int getBlueFromBGR(int bgr){
		return bgr & 0xFF;
	}
	
	/** Extracting the alpha value from an packed colorno matter in rgb(a) or bgr(a) form. */
	public static final int getAlpha(int rgba){
		return (rgba >> 24) & 0xFF;
	}
	
	/** Extracting the red value as a 0..1 float from an packed color which is in rgb(a) form. */
	public static final float getRedFromRGBf(int rgb){
		return (rgb & 0xFF)/255f;
	}
	
	/** Extracting the red value as a 0..1 float from an packed color which is in bgr(a) form. */
	public static final float getRedFromBGRf(int bgr){
		return ((bgr >> 16) & 0xFF)/255f;
	}
	
	/** Extracting the green value as a 0..1 float from an packed color no matter in rgb(a) or bgr(a) form. */
	public static final float getGreenf(int rgb){
		return ((rgb >> 8) & 0xFF)/255f;
	}
	
	/** Extracting the blue value as a 0..1 float from an packed color which is in rgb(a) form. */
	public static final float getBlueFromRGBf(int rgb){
		return ((rgb >> 16) & 0xFF)/255f;
	}
	
	/** Extracting the blue value as a 0..1 float from an packed color which is in bgr(a) form. */
	public static final float getBlueFromBGRf(int bgr){
		return (bgr & 0xFF)/255f;
	}
	
	/** Extracting the alpha value as a 0..1 float from an packed colorno matter in rgb(a) or bgr(a) form. */
	public static final float getAlphaf(int rgba){
		return ((rgba >> 24) & 0xFF)/255f;
	}
	
	/** Setting just the alpha value of the color, leaving the rest intact. */
	public static final int setAlpha(int c, int a){
		// this algorithm may be not right
		return ( ((c)&0xFFFFFF) | ((((a))&0xFF)<<24) );
	}
}
