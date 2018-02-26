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
public enum NvGfxAPIVersion implements Comparable<NvGfxAPIVersion>{
	GLES1(1, 0, true),
    GLES2(2,0, true), GLES3_0(3,0, true), GLES3_1(3,1, true),
	GLES3_2(3,2, true);

	/** The major version (X.0) */
	public final int majVersion;
	/** The minor version (0.Y) */
	public final int minVersion;
	/** True represent this version is OpenGL ES. */
	public final boolean isGLES;
	
	public static NvGfxAPIVersion queryVersion(boolean isES, int major, int minor){
		assert (isES);
		if(major == 1 && minor == 0){
			return GLES1;
		}else if(major == 2 && minor == 0){
			return GLES2;
		}else if(major == 3){
			if(minor == 0)
				return GLES3_0;
			else if(minor == 1)
				return GLES3_1;
		}
		
		throw null;
	}
	
	private NvGfxAPIVersion(int majVersion, int minVersion, boolean isGLES) {
		this.majVersion = majVersion;
		this.minVersion = minVersion;
		this.isGLES     = isGLES;
	}
	
}
