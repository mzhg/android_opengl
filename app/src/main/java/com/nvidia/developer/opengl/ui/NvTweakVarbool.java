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

import com.nvidia.developer.opengl.utils.FieldControl;

public class NvTweakVarbool extends NvTweakVarBase{

	/** Reference to variable being tracked/tweaked. */
	protected FieldControl mValRef;
	/** Initial value, useful for 'reset' to starting point. */
	protected boolean mValInitial;
	/** A member of our datatype, for self-referencing NvTweakVar to point into itself. */
	protected boolean mValSelf;
	
	/** Specialized unclamped constructor, generally used for bool variable. */
	protected NvTweakVarbool(FieldControl refVal, String name, String description) {
		super(name, description);
		
		mValRef = refVal;
	}
	
	/** Specialized unclamped constructor, generally used for self-referential bool variable. */
	protected NvTweakVarbool(String name, String description) {
		super(name, description);
		
//		mValRef = new FieldControl(this, "mValSelf", FieldControl.CALL_FIELD);
	}
	
	/** Accessor for getting at the current value when we hold onto a typed subclass. */
    public boolean get() { return mValRef != null ? (Boolean) mValRef.getValue() : mValSelf; }

    /** Assignment operator to set (once) the internal variable we will point to. */
    public NvTweakVarbool set( boolean val) { 
    	if(mValRef != null)
    		mValRef.setValue(val); 
    	else
    		mValSelf = val;
    	return this; 
	}
    
    /** Specific implementation of increment for the templated datatype. */
    public void increment(){
    	set(!get());
    }
    /** Specific implementation of decrement for the templated datatype. */
    public void decrement(){
    	set(!get());
    }

    /** Reset the managed variable to its initial value. */
    public void reset() {
    	if(mValRef != null)
    		mValRef.setValue(mValInitial);
    	else
    		mValSelf = mValInitial;
    }

    /** Specific implementation of equals that each templated type must override appropriately. */
    public boolean equals(boolean val){
    	return get() == val;
    }
    /** Specific implementation of equals that each templated type must override appropriately. */
    public boolean equals(float val){
    	return false;
    }
    /** Specific implementation of equals that each templated type must override appropriately. */
    public boolean equals(int val){
    	return false;
    }

}
