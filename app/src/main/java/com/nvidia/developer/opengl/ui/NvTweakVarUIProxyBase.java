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
 * An object that will help link an NvTweakVar with a proxied NvUIElement. This
 * class acts as the basis for NvTweakVarUI, connecting up a tweakable app
 * variable up with a particular NvUI widget class appropriate for interacting
 * with that variable -- such as buttons and sliders.
 * 
 * @author Nvidia 2014-9-13 16:51
 * 
 */
public class NvTweakVarUIProxyBase extends NvUIProxy {

	protected boolean m_readonly;

	/** Default constructor, takes the UI element we proxy to. */
	public NvTweakVarUIProxyBase(NvUIElement m_proxy) {
		super(m_proxy);
	}

	public void setReadOnly(boolean ro) {
		m_readonly = ro;
		m_canFocus = !ro;
	}

	// !!!TODO may want to override drawstate to only draw inactive state,
	// etc....

	/**
	 * We override HandleEvent to short-circuit any tweaks of read-only
	 * variables.
	 */
	public int handleEvent(NvGestureEvent ev, long timeUST,
			NvUIElement hasInteract) {
		if (m_readonly)
			return nvuiEventNotHandled;
		return super.handleEvent(ev, timeUST, hasInteract);
	}

	/**
	 * We override HandleEvent to short-circuit any tweaks of read-only
	 * variables.
	 */
	public int handleReaction(NvUIReaction react) {
		// if (m_readonly)
		// return nvuiEventNotHandled;
		return super.handleReaction(react);
	}
}
