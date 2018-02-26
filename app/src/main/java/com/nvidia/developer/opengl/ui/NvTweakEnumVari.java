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

public class NvTweakEnumVari extends NvTweakVari{

	public NvTweakEnumi[] m_enumVals;
	public int m_enumIndex;
	
	protected NvTweakEnumVari(NvTweakEnumi[] enumVals, FieldControl refVal, String name, int minVal,
			int maxVal, int step, String description) {
		super(refVal, name, minVal, maxVal, step, description);
		
		int v = (Integer) refVal.getValue();
		m_enumVals = new NvTweakEnumi[enumVals.length];
		for(int i = 0; i < enumVals.length;i++){
			m_enumVals[i] = new NvTweakEnumi(enumVals[i]);
			if(m_enumVals[i].get() == v)
				m_enumIndex = i;
		}
	}
	
	public int get(int index){
		if (index>=m_enumVals.length)
            return m_enumVals[0].m_value;  // OOB !!!!TBD TODO log error to cout/stderr?
        return m_enumVals[index].m_value;
	}
	
	/** Specific implementation of increment for the floating-point datatype. */
	public void increment(){
		if (m_enumIndex==m_enumVals.length-1)
            m_enumIndex = 0;
        else
            m_enumIndex++;
//        this->mValRef = m_enumVals[m_enumIndex];
		mValRef.setValue(m_enumVals[m_enumIndex].get());
	}
	
	/** Specific implementation of decrement for the templated datatype. */
	@Override
	public void decrement() {
		if (m_enumIndex==0)
            m_enumIndex = m_enumVals.length - 1;
        else
            m_enumIndex--;
		
		mValRef.setValue(m_enumVals[m_enumIndex].get());
	}

}
