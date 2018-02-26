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

/** This enum defines values for moving focus around with keyboard/DPAD when allowed and supported. */
public interface NvFocusEvent {

	/** Clear current focus chain. */
	static final int FOCUS_CLEAR     =   0x0000;
	/** Movement events. */
	static final int FLAG_MOVE       =   0x10;
	/** Action events. */
	static final int FLAG_ACT        =   0x20;
	/** Move focus to first element on screen. */
	static final int MOVE_FIRST      =   0x01+FLAG_MOVE; 
	/** Move focus up. */
	static final int MOVE_UP         =   0x02+FLAG_MOVE; 
	/** Move focus down. */
	static final int MOVE_DOWN       =   0x03+FLAG_MOVE; 
	/** Move focus left. */
	static final int MOVE_LEFT       =   0x04+FLAG_MOVE;
	/** Move focus right. */
	static final int MOVE_RIGHT      =   0x05+FLAG_MOVE;
	/** Press or toggle current value/button where appropriate. */
	static final int ACT_PRESS       =   0x01+FLAG_ACT;
	/** Increase current value/button where appropriate. */
	static final int ACT_INC         =   0x02+FLAG_ACT;
	/** Decrease current value/button where appropriate. */
	static final int ACT_DEC         =   0x03+FLAG_ACT; 
}
