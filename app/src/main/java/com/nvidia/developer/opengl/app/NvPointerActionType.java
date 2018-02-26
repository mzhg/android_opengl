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

/** Pointer input action values. 
 * @author Nvidia 2014-9-12 17:34
 */
public interface NvPointerActionType {

	/** touch or button release */
	public static final int UP = 0;
	/** touch or button press */
	public static final int DOWN = 1;
	/** touch or mouse pointer movement */
	public static final int MOTION = 2;
	/** multitouch additional touch press */
	public static final int EXTRA_DOWN = 4;
	/** multitouch additional touch release */
	public static final int EXTRA_UP = 8;
}
