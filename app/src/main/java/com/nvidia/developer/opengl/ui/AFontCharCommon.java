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

final class AFontCharCommon {

	public float m_lineHeight;
	public float m_baseline;
	public float m_pageWidth;
	public float m_pageHeight;
	public String m_filename;
    public int m_pageID;
    
    public void set(AFontCharCommon o){
    	m_lineHeight = o.m_lineHeight;
    	m_baseline = o.m_baseline;
    	m_pageWidth = o.m_pageWidth;
    	m_pageHeight = o.m_pageHeight;
    	m_filename = o.m_filename;
    	m_pageID = o.m_pageID;
    }
}
