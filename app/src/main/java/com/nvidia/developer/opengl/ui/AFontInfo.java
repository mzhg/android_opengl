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

import java.util.Arrays;

public class AFontInfo {

	public String m_name;
	public float m_size;
	public boolean m_isBold;
	public boolean m_isItalic;
	public boolean m_isUnicode;
	public String m_charset;
    public float m_stretchHeight;
    public final float[] m_padding = new float[4];
    public final float[] m_spacing = new float[2];
    public int m_charCount;
    
    public void set(AFontInfo o){
    	m_name = o.m_name;
    	m_size = o.m_size;
    	m_isBold = o.m_isBold;
    	m_isItalic = o.m_isItalic;
    	m_isUnicode = o.m_isUnicode;
    	m_charset = o.m_charset;
    	m_stretchHeight = o.m_stretchHeight;
    	m_charCount = o.m_charCount;
    	
    	System.arraycopy(o.m_padding, 0, m_padding, 0, 4);
    	System.arraycopy(o.m_spacing, 0, m_spacing, 0, 2);
    }

	@Override
	public String toString() {
		return "AFontInfo [m_name=" + m_name + ", m_size=" + m_size
				+ ", m_isBold=" + m_isBold + ", m_isItalic=" + m_isItalic
				+ ", m_isUnicode=" + m_isUnicode + ", m_charset=" + m_charset
				+ ", m_stretchHeight=" + m_stretchHeight + ", m_padding="
				+ Arrays.toString(m_padding) + ", m_spacing="
				+ Arrays.toString(m_spacing) + ", m_charCount=" + m_charCount
				+ "]";
	}
}
