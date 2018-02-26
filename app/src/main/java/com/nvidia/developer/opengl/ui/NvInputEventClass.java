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
package com.nvidia.developer.opengl.ui;

/**
 * mouse/stylus (accurate) vs finger (inaccurate).
 *  these are used as array indices + max size!<p>
 *  These are types of input devices generating pointer events.
 *  We might use this information to adjust hit margins and such
 *  based on the average size of the pointer.
 * @author Nvidia 2014-9-2
 *
 */
public interface NvInputEventClass {
	/** No input type specified. */
	static final int NONE = 0;
	/** Mouse input */
	static final int MOUSE = 1;
	/** Touch input */
	static final int TOUCH = 2;
	/** Stylus input */
	static final int STYLUS = 3;
}
