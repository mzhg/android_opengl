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
 * Camera transform mode.
 * @author Nvidia 2014-9-13 12:42
 */
public interface NvCameraXformType {

	/** Default transform */
	public static final int MAIN = 0;
	/** Secondary transform */
	public static final int SECONDARY = 1;
	/** Number of transforms */
	public static final int COUNT = 2;
}
