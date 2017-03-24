//----------------------------------------------------------------------------------
// File:        NvPackedColor.java
// SDK Version: v1.2 
// Email:       gameworks@nvidia.com
// Site:        http://developer.nvidia.com/
//
// Copyright (c) 2014, NVIDIA CORPORATION. All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
//  * Redistributions of source code must retain the above copyright
//    notice, this list of conditions and the following disclaimer.
//  * Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
//  * Neither the name of NVIDIA CORPORATION nor the names of its
//    contributors may be used to endorse or promote products derived
//    from this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS ``AS IS'' AND ANY
// EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
// PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
// EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
// PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
// PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
// OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
//----------------------------------------------------------------------------------
package com.nvidia.developer.opengl.utils;

public final class NvPackedColor {

	/** A predefined constant for WHITE. */
	public static final int NV_PC_PREDEF_WHITE = 0xFFFFFFFF;
	/** A predefined constant for BLACK. */
	public static final int NV_PC_PREDEF_BLACK = 0xFF000000;
	
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
