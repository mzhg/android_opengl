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
package com.nvidia.developer.opengl.utils;

/** Uncompressed 4x4 color block. */
public class ColorBlock {

	private final int[] m_color = new int[4 * 4];
	
	public ColorBlock() {
	}
	
	public ColorBlock(ColorBlock block) {
		System.arraycopy(block.m_color, 0, m_color, 0, 16);
	}
	
	public int[] colors(){
		return m_color;
	}
	
	public int get(int i){
		return m_color[i];
	}
	
	public void set(int i, int c){
		m_color[i] = c;
	}
	
	public int get(int x, int y){
		return m_color[y * 4 + x];
	}
	
	public void set(int x, int y, int c){
		m_color[y * 4 + x] = c;
	}
	
	public void setAlpha(int x, int y, int alpha){
		m_color[y * 4 + x] &= 0x00FFFFFF; // first clear the alpha
		m_color[y * 4 + x] |= (alpha << 24);
	}
	
	public void setAlpha(int i, int alpha){
		m_color[i] &= 0x00FFFFFF; // first clear the alpha
		m_color[i] |= (alpha << 24);
	}
}
