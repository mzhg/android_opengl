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

import com.nvidia.developer.opengl.utils.GLES;

import javax.microedition.khronos.opengles.GL11;

/**
 * A single, top-level container that can manage all NvUIElements in the view.
 * <p>
 * Most importantly, the <code>NvUIWindow</code> class automatically takes care
 * of calling {@link #handleReaction} on its children after processing the
 * {@link #handleEvent(NvGestureEvent, long, NvUIElement)}.
 * 
 * @author Nvidia 2014-9-11 0:02
 * 
 */
public class NvUIWindow extends NvUIContainer {

	/**
	 * Default constructor, takes starting window/viewport width/height. Also
	 * responsible for initializing the NvUIText system with the same
	 * dimensions.
	 */
	public NvUIWindow(float width, float height) {
		super(width, height, null);
		
		NvUIText.staticInit(width, height);
		systemResChange((int)width, (int)height);
	}
	
	@Override
	public void dispose() {
		NvUIText.staticCleanup();
	}

	/**
	 * Overrides and calls normal @p HandleEvent, with pointer to @p this as
	 * element with focus.
	 */
	public int handleEvent(NvGestureEvent ev, long timeUST,
			NvUIElement hasInteract) {
		if (ev.kind > NvGestureKind.HOVER && hasFocus())
	        dropFocus();
	    // !!!!TBD note we ignore hasInteract, and just pass in the window as the top level focus holder.
	    int r = super.handleEvent(ev, timeUST, this);
	    return r;
	}

	/**
	 * Responsible for ensuring things get appropriate resized, in as much as
	 * that is possible given current NvUI system design constraints.
	 */
	public void handleReshape(float w, float h) {
		// !!!!!TBD TODO !!!!!TBD TODO
	    // resize container, notify contents
	    // update UIText/BitFont of view size change as needed.

	    super.handleReshape(w, h);

	    // most containers won't just resize, but we're THE WINDOW, we are the VIEW SIZE.
	    setDimensions(w, h);

	    // this changes our design w/h statics inside UIElement, so we MUST DO THIS LAST,
	    // so that children can compare current design w/h vs incoming new w/h...
	    // in the future with gravity positioning, and relative coord spaces, might not be necessary.
	    // !!!!TBD TODO for the moment, use SystemResChange to do what we want for text and similar systems.
	    systemResChange((int)w, (int)h);
	}

	/**
	 * We override to ensure we save and restore outside drawing state around
	 * the UI calls.
	 */
	public void draw(NvUIDrawState drawState) {
		if (!m_isVisible) return;

	    // !!!!TBD TODO we should have a separate helper utility
	    // for saving and restoring state.
	    // and another for caching filtered state so we don't
	    // redundantly set in all the UI rendering bits.

//        NvBFText.saveGLState();   // TODO may be have problem.
        
	    GLES.glDisable(GL11.GL_STENCIL_TEST);
	    GLES.glDisable(GL11.GL_CULL_FACE);
	    GLES.glDisable(GL11.GL_DEPTH_TEST);

	    super.draw(drawState);

//	    NvBFRestoreGLState();
//	    NvBFText.restoreGLState(); // TODO may be have problem.
	}
}
