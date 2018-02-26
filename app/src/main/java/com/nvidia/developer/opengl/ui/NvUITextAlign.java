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
 * Basic text alignment.<p>
 * This enum is a clone of the alignments from BitFont's NvBftAlign enum, in order
 * to separate the two and not create a hard dependency between the system (as we
 * might decide to change the text rasterizer under the hood of NvUI, for example).
 * @author Nvidia 2014-9-8 : 22: 11
 *
 */
public interface NvUITextAlign {

	/** Align to left */
	static final int LEFT = 0;
	/** Align to right */
	static final int RIGHT = 1;
	/** Align to center */
	static final int CENTER = 2;
	/** Align to top */
	static final int TOP = 0;
	/** Align to bottom */
	static final int BOTTOM = 1;
}
