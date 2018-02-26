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
 * Default font families you may reference.<p>
 * This enum represents the families/typefaces for use in NvUI-based applications.
 * We currently use a default font that is sans-serif, generated from Roboto Condensed,
 * and a monospaced font generated from Courier New.  Both fonts have normal and bold
 * styles in their respective bitmap files, so have bolding supported.
 * @author Nvidia 2014-9-8 : 22: 05
 * @see NvBitFont
 * @see NvUIText
 *
 */
public interface NvUIFontFamily {

	/** Default font as initialized */
	static final int DEFAULT = 0;
	/** Sans serif style font */
	static final int SANS    = DEFAULT;
	/** Monospaced style font */
	static final int MONO    = 1;
	// should get set to right value...
	static final int COUNT   = 2;
}
