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
 * Pair of NvTweakCmd and NvTweakVarBase that we can look up as a bound result for some user input.
 * @author Nvidia 2014-9-13 19:13
 *
 */
public class NvTweakBind {

	/** No command specified */
	public static final int NONE = 0;
	/** Reset the variable to initial state */
	public static final int RESET = 1;
	/** Increment the variable */
	public static final int INCREMENT = 2;
	/** Decrement the variable */
	public static final int DECREMENT = 3;
	
	public int mCmd;
	public NvTweakVarBase mVar;
	
	public NvTweakBind() {
	}

	public NvTweakBind(int cmd, NvTweakVarBase var) {
		this.mCmd = cmd;
		this.mVar = var;
	}
}
