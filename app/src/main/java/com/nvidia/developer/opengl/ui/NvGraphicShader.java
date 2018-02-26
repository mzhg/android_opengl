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

import com.nvidia.developer.opengl.utils.NvGLSLProgram;

/** A helper object for managing a shader program for NvUIGraphic and subclasses. */
public class NvGraphicShader {
	/** A pointer to an allocated NvGLSLProgram object.  We're not a subclass as we could easily have some other method for loading/storing what shader program we are using (previously, this was just a GLint for the compiled program). */
	public NvGLSLProgram m_program;
	/** Index for position attribute */
	public int m_positionIndex;
	/** Index for uv attribute */
	public int m_uvIndex;
	/** Index for matrix uniform */
	public int m_matrixIndex;
	/** Index for alpha uniform */
	public int m_alphaIndex;
	/** Index for color uniform */
	public int m_colorIndex;
	
	public void load(String vs, String fs){
		NvGLSLProgram prog = NvGLSLProgram.createFromStrings(vs, fs, false);
		
		m_program = prog;
		prog.enable();
		
		m_positionIndex = prog.getAttribLocation("position", false);
	    m_uvIndex = prog.getAttribLocation("tex", false);

	    prog.setUniform1i(prog.getUniformLocation("sampler", false), 0); // texunit index zero.

	    m_matrixIndex = prog.getUniformLocation("pixelToClipMat", false);
	    m_alphaIndex = prog.getUniformLocation("alpha", false);
	    m_colorIndex = prog.getUniformLocation("color", false);

	    prog.disable();
	}
}
