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
 * Mouse button masks.The button indices are bitfields.  i.e., button-3 == middle == 1<<3.
 * @author Nvidia 2014-9-12 17:46
 */
public interface NvMouseButton {

	/** Left button */
	public static final int LEFT = 0x00000001;
	/** Right button */
	public static final int RIGHT = 0x00000002;
	/** Middle button */
	public static final int MIDDLE = 0x00000004;
}
