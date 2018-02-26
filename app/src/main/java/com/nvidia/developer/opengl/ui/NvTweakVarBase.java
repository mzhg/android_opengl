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
 * This is an abstract base class for indirectly referencing app variables.
 * @author Nvidia 2014-9-13 16:40
 */
public abstract class NvTweakVarBase {

	/** A human-readable name/title of the variable. */
	protected String mName;
	/** An informative 'help' string for the variable. */
	protected String mDesc;
	/** A unique value for signalling changes across systems. */
	protected int mActionCode;
	
	/**
	 * Base constructor.<p>
	 * Note that the base constructor defaults mActionCode to 0, expecting
     * subclass constructors to set a meaningful, unique value afterward.
	 * @param name A title for this variable.
	 * @param description description An OPTIONAL help string.
	 */
	protected NvTweakVarBase(String name, String description) {
		mName = name;
		mDesc = description;
	}
	
	public abstract void increment();
    public abstract void decrement();
    public abstract void reset();
    
    public abstract boolean equals(boolean val);
    public abstract boolean equals(float val);
    public abstract boolean equals(int val);
    
    /** Accessor to retrieve pointer to the name string. */
    public String getName() { return mName; }
    /** Accessor to retrieve pointer to the description string. */
    public String getDesc() { return mDesc; }

    /** Set the action code value for this variable. */
    public void setActionCode(int code) { mActionCode = code; }
    /** Accessor to retrieve the action code value. */
    public int getActionCode() { return mActionCode; }
}
