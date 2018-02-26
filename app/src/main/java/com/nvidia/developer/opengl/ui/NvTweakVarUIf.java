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
 * A templated object that links NvTweakVar of some datatype with an appropriate
 * UI widget. This class connects a tweakable app variable, a specific
 * NvTweakVar template instance, with a UI widget appropriate for changing the
 * value of that variable, linked by an action code shared between the systems.
 * 
 * @author Nvidia 2014-9-13 20:50
 * 
 */
public class NvTweakVarUIf extends NvTweakVarUIProxyBase {

	/**
	 * The variable we hold a reference to in order to adjust its value based on
	 * the input/reaction from the user interacting with our proxied UI widget.
	 */
	protected NvTweakVarf m_tvar;

	public NvTweakVarUIf(NvTweakVarf tvar, NvUIElement el, int actionCode /* =0 */) {
		super(el);

		m_tvar = tvar;
		m_tvar.setActionCode(actionCode);
	}

	/**
	 * We override HandleReaction so that when there is an NvUIReaction passing
	 * through the system containing a value change for us or from our proxied
	 * UI, we can intercept and set the value of our NvTweakVar appropriately.
	 */
	@Override
	public int handleReaction(NvUIReaction react) {
		if (react.code != 0 && (react.code!=m_tvar.getActionCode()))
	        return nvuiEventNotHandled; // not a message for us.

	    if ((react.flags & NvReactFlag.FORCE_UPDATE) != 0)
	    {
	        NvUIReaction change = getReactionEdit(false); // false to not clear it!!!
	        change.fval = m_tvar.get(); // update to what's stored in the variable.
	    }

	    super.handleReaction(react);

	    int r = nvuiEventNotHandled;
	    if (   (react.uid==getUID()) // we always listen to our own sent messages.
	        || (react.code!= 0 && (react.code==m_tvar.getActionCode())) // we always listen to our action code
	        || (react.code == 0 && (react.flags & NvReactFlag.FORCE_UPDATE) != 0) ) // we listen to force-update only if NO action code
	    {
	        if (m_tvar.get() != react.fval)
	        {
	            m_tvar.set(react.fval); // float TweakVar stashed value in fval in HandleReaction
	            return nvuiEventHandled; // !!!!TBD TODO do we eat it here?
	        }
	    }

	    return r;
	}
}
