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
 * BitFont supports specific color constants embedded in a string.<p>
 * You can directly use the following as character values for switching
 * color 'runs' in the text.  Note that these embedded color changes will
 * completely override the base color specified for a given string.<p>
 * There are also string literals for use directly in C quoted string composition.
 * @author Nvidia 2014-9-9 9:45
 *
 */
interface NvBftColorCode {

	/** Sets further text to be white. */
	static final int NvBF_COLORCODE_WHITE = 0;
	/** Sets further text to be gray. */
	static final int NvBF_COLORCODE_GRAY  = 1;
	/** Sets further text to be black. */
	static final int NvBF_COLORCODE_BLACK = 2;
	/** Sets further text to be red. */
	static final int NvBF_COLORCODE_RED   = 3; 
	/** Sets further text to be green. */
	static final int NvBF_COLORCODE_GREEN = 4;
	/** Sets further text to be blue. */
	static final int NvBF_COLORCODE_BLUE  = 5;
	/** Used for programmatic range checking of embedded codes. */
	static final int NvBF_COLORCODE_MAX   = 6; 
	
	/** Embedded string literal to change text coloring to white. */
	static final String NvBF_COLORSTR_WHITE  =   "\001";
	/** Embedded string literal to change text coloring to gray. */
	static final String NvBF_COLORSTR_GRAY   =   "\002";
	/** Embedded string literal to change text coloring to black. */
	static final String NvBF_COLORSTR_BLACK  =   "\003";
	/** Embedded string literal to change text coloring to red. */
	static final String NvBF_COLORSTR_RED    =   "\004";
	/** Embedded string literal to change text coloring to green. */
	static final String NvBF_COLORSTR_GREEN  =   "\005";
	/** Embedded string literal to change text coloring to blue. */
	static final String NvBF_COLORSTR_BLUE   =   "\006";
	/** Embedded string literal to restore text coloring to 'normal'. should be 'max' value. */
	static final String NvBF_COLORSTR_NORMAL =   "\007";
}
