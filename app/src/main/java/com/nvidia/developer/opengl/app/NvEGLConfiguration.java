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
package com.nvidia.developer.opengl.app;

import com.nvidia.developer.opengl.utils.NvGfxAPIVersion;

/** Cross-platform OpenGL Context APIs and information */
public class NvEGLConfiguration {

	/** API and version, defaults GLES2.0 */
	public NvGfxAPIVersion apiVer;
	/** red color channel depth in bits, defaults 8 */
	public int redBits;
	/** green color channel depth in bits, defaults 8 */
	public int greenBits;
	/** blue color channel depth in bits, defaults 8 */
	public int blueBits;
	/** alpha color channel depth in bits, defaults 8 */
	public int alphaBits;
	/** depth color channel depth in bits, defaults 24 */
	public int depthBits;
	/** stencil color channel depth in bits, defaults 0 */
	public int stencilBits;
	
	/**
	 * Inline all-elements constructor.
	 * @param _api the API and version information
	 * @param r the red color depth in bits
	 * @param g the green color depth in bits
	 * @param b the blue color depth in bits
	 * @param a the alpha color depth in bits
	 * @param d the depth buffer depth in bits
	 * @param s the stencil buffer depth in bits
	 */
	public NvEGLConfiguration(NvGfxAPIVersion _api, int r, int g, int b, int a, int d, int s) {
		apiVer = _api;
		redBits = r;
		greenBits = g;
		blueBits = b;
		alphaBits = a;
		depthBits = d;
		stencilBits = s;
	}
	
	public NvEGLConfiguration() {
		this(NvGfxAPIVersion.GLES2, 8, 8, 8, 8, 24, 0);
	}
	
	public NvEGLConfiguration(NvGfxAPIVersion _api) {
		this(_api, 8, 8, 8, 8, 24, 0);
	}
	
	public NvEGLConfiguration(NvGfxAPIVersion _api, int r, int g, int b, int a) {
		this(_api, r, g, b, a, 24, 0);
	}
}
