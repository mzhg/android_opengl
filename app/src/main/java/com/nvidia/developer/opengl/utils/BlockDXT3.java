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
package com.nvidia.developer.opengl.utils;

final class BlockDXT3 {

	public final AlphaBlockDXT3 alpha = new AlphaBlockDXT3();
	public final BlockDXT1 color = new BlockDXT1();
	
	public void decodeBlock(ColorBlock block){
		// Decode color.
	    color.decodeBlock(block);
	    
	    // Decode alpha.
	    alpha.decodeBlock(block);
	}

	public int load(byte[] buf, int offset) {
		offset = alpha.load(buf, offset);
		offset = color.load(buf, offset);
		return offset;
	}
}
