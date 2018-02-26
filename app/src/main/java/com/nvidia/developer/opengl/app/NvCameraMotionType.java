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

/**
 * Automated input-to-camera motion mapping<p>
 * Camera motion mode.
 * @author Nvidia 2014-9-13 12:36
 */
public interface NvCameraMotionType {

	/** Camera orbits the world origin */
	public static final int ORBITAL = 0;
	/** Camera moves as in a 3D, first-person shooter */
	public static final int FIRST_PERSON = 1;
	/** Camera pans and zooms in 2D */
	public static final int PAN_ZOOM = 2;
	/** Two independent orbital transforms */
	public static final int DUAL_ORBITAL = 3;
}
