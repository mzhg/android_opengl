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

final class AFontChar {
	public int m_idKey; // copy of our ascii/u8 index -- also the char lookup key
	public float m_x, m_y;
	public float m_width, m_height;
	public float m_xOff, m_yOff;
	public float m_xAdvance;
    public int m_pageID; // NVDHC: no plan to implement immediately
    public int m_channelIndex; // NVDHC: no plan to implement immediately
}
