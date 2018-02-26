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

final class AlphaBlockDXT3 {

	public long row;
	
	public int load(byte[] buf, int offset){
		row = NvUtils.getLong(buf, offset);
		return offset + 8;
	}
	
	public int getAlpha(int index){
		return (int)(row >> (4 * index)) & 0xF;
	}
	
	public void decodeBlock(ColorBlock block){
		final int alpha0 = getAlpha(0x0);
		final int alpha1 = getAlpha(0x1);
		final int alpha2 = getAlpha(0x2);
		final int alpha3 = getAlpha(0x3);
		final int alpha4 = getAlpha(0x4);
		final int alpha5 = getAlpha(0x5);
		final int alpha6 = getAlpha(0x6);
		final int alpha7 = getAlpha(0x7);
		final int alpha8 = getAlpha(0x8);
		final int alpha9 = getAlpha(0x9);
		final int alphaA = getAlpha(0xA);
		final int alphaB = getAlpha(0xB);
		final int alphaC = getAlpha(0xC);
		final int alphaD = getAlpha(0xD);
		final int alphaE = getAlpha(0xE);
		final int alphaF = getAlpha(0xF);
		
		block.setAlpha(0x0, (alpha0 << 4) | alpha0);
		block.setAlpha(0x1, (alpha1 << 4) | alpha1);
		block.setAlpha(0x2, (alpha2 << 4) | alpha2);
		block.setAlpha(0x3, (alpha3 << 4) | alpha3);
		block.setAlpha(0x4, (alpha4 << 4) | alpha4);
		block.setAlpha(0x5, (alpha5 << 4) | alpha5);
		block.setAlpha(0x6, (alpha6 << 4) | alpha6);
		block.setAlpha(0x7, (alpha7 << 4) | alpha7);
		block.setAlpha(0x8, (alpha8 << 4) | alpha8);
		block.setAlpha(0x9, (alpha9 << 4) | alpha9);
		block.setAlpha(0xA, (alphaA << 4) | alphaA);
		block.setAlpha(0xB, (alphaB << 4) | alphaB);
		block.setAlpha(0xC, (alphaC << 4) | alphaC);
		block.setAlpha(0xD, (alphaD << 4) | alphaD);
		block.setAlpha(0xE, (alphaE << 4) | alphaE);
		block.setAlpha(0xF, (alphaF << 4) | alphaF);
	}
	
	public static void main(String[] args) {
		for(int i = 0; i < 16; i++){
			String format = "final int alpha%X = getAlpha(0x%X);\n";
			System.out.printf(format, i, i);
		}
		
		for(int i = 0; i < 16; i++){
			String format = "block.setAlpha(0x%X, (alpha%X << 4) | alpha%X);\n";
			System.out.printf(format, i, i, i);
		}
	}
}
