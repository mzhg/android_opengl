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

import java.nio.FloatBuffer;

/** This is a helper structure for rendering sets of 2D textured vertices. */
class NvTexturedVertex {

	/** 2d vertex coord */
	public float posX, posY;
	/** vertex texturing position */
	public float uvX, uvY;
	
	void store(FloatBuffer buf){
		buf.put(posX);
		buf.put(posY);
		buf.put(uvX);
		buf.put(uvY);
	}
}
