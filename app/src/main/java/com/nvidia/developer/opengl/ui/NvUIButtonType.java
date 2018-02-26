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
 * Basic button types.<p>
 * This enum defines the types of buttons we understand in the system,
 * including basic 'push' button, radio button, and checkbox style interactions.
 * @author Nvidia 2014-9-9 23:03
 */
public interface NvUIButtonType {

	/** Button where state comes up automatically after click. */
	public static final int PUSH = 0;
	/** Button where state sticks 'in' when clicked, others in same group release. */
	public static final int RADIO = 1;
	/** Button where state toggles on/off on each click. */
	public static final int CHECK = 2;
}
