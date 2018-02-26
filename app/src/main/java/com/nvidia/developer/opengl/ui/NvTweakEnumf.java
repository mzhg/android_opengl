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

public class NvTweakEnumf {

	public String m_name;
	public float m_value;
	
	public NvTweakEnumf() {
	}
	
	public NvTweakEnumf(String m_name, float m_value) {
		this.m_name = m_name;
		this.m_value = m_value;
	}

	public NvTweakEnumf(NvTweakEnumf o) {
		this.m_name = o.m_name;
		this.m_value = o.m_value;
	}

	public float get(){
		return m_value;
	}
	
	public void set(float v){
		m_value = v;
	}
	
	public String getName(){
		return m_name;
	}
	
	public void setName(String name){
		m_name = name;
	}
}
