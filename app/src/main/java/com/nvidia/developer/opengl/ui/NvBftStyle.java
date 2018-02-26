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
 * BitFont supports 'styles' via embedded character codes and string literals.<p>
 * You can directly use the following as character values for switching
 * between the top and bottom 'halves' of a 'split' font files.  This is
 * currently used as 'normal' and 'bold' styles, but the two halves could
 * actually be set up as different typeface families.  There are also matching
 * string literals for equivalent use directly in C quoted string composition.
 * @author Nvidia 2014-9-8 22:22
 *
 */
public interface NvBftStyle {

	/** Sets further text to normal style. */
	static final int NORMAL = 0x10;
	/** Sets further text to bold style, for fonts loaded with bold support. */
	static final int BOLD = 0x11;
	/** Used for programmatic range checking of embedded codes. */
	static final int MAX = 0x12;
	
	/** BitFont string literal for style reset to 'normal'. */
	static final String NVBF_STYLESTR_NORMAL = "\020";
	/** BitFont string literal to style further characters 'bold'. */
	static final String NVBF_STYLESTR_BOLD   = "\021";
}
