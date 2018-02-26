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
 * Basic button states.<p>
 * This enum encapsulates the base three states of a button visual: inactive,
 * active, and selected/pressed.
 * @author Nvidia 2014-9-9 22:56
 *
 */
public interface NvUIButtonState {

	/** Button is active, can be interacted with. */
	static final int ACTIVE = 0;
	/** Button is selected, user is actively interacting with it. */
	static final int SELECTED = 1;
	/** Button is inactive, not able to interact with it. */
	static final int INACTIVE = 2;
	static final int MAX = 3;
}
