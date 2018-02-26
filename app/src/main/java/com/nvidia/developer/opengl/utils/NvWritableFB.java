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

import android.opengl.GLES20;

public class NvWritableFB implements NvDisposeable{

	/** The GL framebuffer object handle */
	public int fbo;
	/** the width in pixels */
	public int width;
	/** the height in pixels */
	public int height;
	
	/**
	 * Creates an object that (by default) represents the main framebuffer (FBO 0)
	 * @param w the width in pixels
	 * @param h the height in pixels
	 */
	public NvWritableFB(int w, int h) {
		width = w;
		height = h;
	}
	
	/** Dispose the frame buffer object. */
	@Override
	public void dispose() {
		if(fbo != 0){
			GLES.glDeleteFramebuffers(fbo);
			fbo = 0;
		}
	}
	
	/**
	 * Binds the calling framebuffer and sets the viewport based
	 * on the sizes cached by the object
	 */
	public void bind(){
		GLES.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbo);
		GLES.glViewport(0, 0, width, height);
	}
}
