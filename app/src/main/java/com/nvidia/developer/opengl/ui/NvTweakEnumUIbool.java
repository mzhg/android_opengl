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
 * A templated subclass of NvTweakVarUI specifically for handling one entry of
 * an NvTweakEnum list/array passed into the tweakbar -- i.e., one button in a
 * radio button group, or one menu item in a popup menu. Each NvTweakVarUI
 * element is responsible for setting its stored enum value on the linked
 * NvTweakVar when the UI element is selected.
 * 
 * @author Nvidia 2014-9-13 21:00
 * 
 */
public class NvTweakEnumUIbool extends NvTweakVarUIbool {

	/** A reference to the enum var that we represent one entry from. */
	protected NvTweakEnumVarbool m_tevar;
	/** The array index of the enum entry that our UI item represents. */
	protected int m_teindex;
	/**
	 * A cached value passed in via an NvTweakEnum entry.
	 * <p>
	 * Will be used to set our linked NvTweakVar to a specific value during an
	 * appropriate handleReaction.
	 */
	protected boolean m_enumval;

	/**
	 * Normal constructor. Takes the referenced NvTweakEnumVar, the enumerant
	 * index we represent, the individual UI element that acts as our proxy for
	 * user interaction in a radio group or popup menu, and an optional override
	 * of the action code for the variable and UI.
	 */
	public NvTweakEnumUIbool(NvTweakEnumVarbool tever, int index, NvUIElement el,
			int actionCode /* =0 */) {
		super(tever, el, actionCode);
		m_tevar = tever;
		m_teindex = index;
		m_enumval = tever.get(index); // cache the value at the given index for
										// easier access later.
	}

	/**
	 * We override handleEvent so that if there is a reaction by the proxied UI
	 * widget, we can replace the value inside the NvUIReaction with our cached
	 * value, and allow the rest of the reaction process to occur as normal.
	 */
	@Override
	public int handleEvent(NvGestureEvent ev, long timeUST,
			NvUIElement hasInteract) {
		int r =  super.handleEvent(ev, timeUST, hasInteract);
		if ((r&nvuiEventHadReaction) != 0)
	    {
	        NvUIReaction react = getReactionEdit(false); // false to not clear it!!!
	     // copy to state AND ival, as enums might need in ival.
	        react.state = m_enumval ? 1 : 0;
	        react.ival = react.state;
	    }
	    return r;
	}

	/**
	 * We override handleReaction so that when there is an NvUIReaction passing
	 * through the system containing a real value, we can set our state to be 0
	 * or 1 (active or pressed) based on whether our cached value matches the
	 * value of the NvTweakVar we're bound to. This is so radio buttons and menu
	 * items update their visual state to match outside changes to the
	 * NvTweakVar's value (such as from key input).
	 */
	@Override
	public int handleReaction(NvUIReaction react) {
		// TODO Auto-generated method stub
		return super.handleReaction(react);
	}

	/**
	 * We override handleFocusEvent so that if there is a reaction by the
	 * proxied UI widget, we can replace the value inside the NvUIReaction with
	 * our cached value, and allow the rest of the reaction process to occur as
	 * normal.
	 */
	@Override
	public int handleFocusEvent(int evt) {
		int r = super.handleFocusEvent(evt);
		if ((r&nvuiEventHadReaction) != 0)
	    {
	        NvUIReaction react = getReactionEdit(false); // false to not clear it!!!
	        // copy to state AND ival, as enums might need in ival.
	        react.state = m_enumval ? 1 : 0;
	        react.ival = react.state;
	    }
	    return r;
	}
}
