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
 * This enum defines bit flags for extra/other info about a particular
 * NvUIReaction.
 */
public interface NvReactFlag {

	/** No additional flag data. */
	static final int NONE = 0;
	/**
	 * Flag to notify any UI elements linked to an outside data source
	 * (NvTweakVar or otherwise) that they should update themselves.
	 */
	static final int FORCE_UPDATE = 0x01;
	/**
	 * Flag that UI elements that match this reaction should clear their drawing state (to 'inactive').
	 */
	static final int CLEAR_STATE = 0x02;
}
