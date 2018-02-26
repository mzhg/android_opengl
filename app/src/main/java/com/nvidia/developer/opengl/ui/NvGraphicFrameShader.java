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

import android.opengl.GLES20;


/** This adds NvUIGraphicFrame features to the NvGraphicShader 'template'. */
public class NvGraphicFrameShader extends NvGraphicShader{

	public int m_borderIndex;
	public int m_thicknessIndex;
	public int m_texBorderIndex;
	
	/**
	 * Helper for compiling the given shader strings and then retrieving indicies.
	 * Overrides the version in NvGraphicShader, calls the inherited version first,
	 * then retrieves our additional indices.
	 */
	public void load(String vs, String fs){
		super.load(vs, fs);
		
		// inherited Load doesn't keep program enabled for 'safety', so we
	    // re-enable here so we can reference ourselves...
	    m_program.enable();

	    m_borderIndex = m_program.getAttribLocation("border", false);

	    GLES20.glUniform1i(m_program.getUniformLocation("sampler"), 0); // texunit index zero.

	    m_thicknessIndex = m_program.getUniformLocation("thickness");
	    m_texBorderIndex = m_program.getUniformLocation("texBorder");

	    m_program.disable();
	}
}
