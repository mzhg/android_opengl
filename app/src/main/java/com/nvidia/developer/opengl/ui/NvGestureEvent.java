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

import com.nvidia.developer.opengl.utils.NvUtils;

/**
 * Object packaging up all the data related to an input event by the user.
 * @author Nvidia 2014-9-4
 *
 */
public final class NvGestureEvent {

	static final int NV_GESTURE_UID_INVALID = 0xFFFFFFFF;
	static final int NV_GESTURE_UID_MAX = 0xFFFFEEEE;
	
	private static int lastID;
	
	private static int internalGetNextUID(){
		long lastid = NvUtils.unsignedInt(lastID);
		lastid++;
        if (lastid > NvUtils.unsignedInt(NV_GESTURE_UID_MAX)) // some arbitrary cap...
        	lastid = 1; // do NOT reset to 0, it's special...
        
        lastID = (int)lastid;
        return lastID;
	}
	
	/** A unique ID for this event -- generally an auto-incrementing counter. */
	public int uid;
	/** The kind of event that occurred. ref to NvGestureKind.*/
	public int kind;
	/** The input device that generated the event: mouse, finger, stylus, etc... ref to NvInputEventClass*/
	public int type;
	/**
	 * new fields, matching the user input struct, for button index and other flags<br>
	 * Storing mouse button, gamepad button, or key identifier for event.<p>
	 * 
	 * This is a unsigned char type.
	 */
	public short index;
	/** x, y position at the START of the gesture. */
	public float x, y;
	/**
	 * Delta x, y value for the gesture.<p>
	 * These are overloaded, different gestures will interpret as different values.<br>
	 * could be things like:<ul>
	 * <li> DRAG: delta position
	 * <li> FLICK: velocities
	 * <li> ZOOM: second finger
	 * </ul>
	 */
	public float dx, dy;
	
	/** General constructor.
	    @param intype The input device generating the event. This is enum value defined in the NvInputEventClass
	    @param inkind The kind of event. This is enum value defined in the NvGestureKind
	    @param inx The starting x position
	    @param inx The starting y position
	 */
	public NvGestureEvent(int intype, int inkind, float inx, float iny) {
		if (inkind == NvGestureKind.PRESS) // change UID only at press
            uid = internalGetNextUID();
        else // keep existing ID
            uid = (int) lastID;
        kind = inkind;
        type = intype;
        index = 0;
        //flags = 0;
        x = inx;
        y = iny;
        dx = 0;
        dy = 0;
	}
}
