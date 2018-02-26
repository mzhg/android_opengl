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

final class BlockDXT1 {

	public final Color16 co10 = new Color16();
	public final Color16 co11 = new Color16();
	public int indices;
	
	/** Return true if the block uses four color mode, false otherwise. */
	public boolean isFourColorMode(){
		return NvUtils.unsignedShort(co10.u) > NvUtils.unsignedShort(co11.u);
	}
	
	public int load(byte[] buf, int offset){
		co10.u = NvUtils.getShort(buf, offset);
		offset += 2;
		co11.u = NvUtils.getShort(buf, offset);
		offset += 2;
		
		indices = NvUtils.getInt(buf, offset);
		offset += 4;
		
		return offset;
	}
	
	public int evaluatePalette(Color32[] color_array){
		int sr = co10.getR();
		int sg = co10.getG();
		int sb = co10.getB();
		
		int b = (sb << 3) | (sb >> 2);
		int g = (sg << 2) | (sg >> 4);
		int r = (sr << 3) | (sr >> 2);
		int a = 255;
		
		color_array[0].set(r, g, b, a);
		
		sr = co11.getR();
		sg = co11.getG();
		sb = co11.getB();
		
		b = (sb << 3) | (sb >> 2);
		g = (sg << 2) | (sg >> 4);
		r = (sr << 3) | (sr >> 2);
		
		color_array[1].set(r, g, b, a);
		
		if(NvUtils.unsignedShort(co10.u) > NvUtils.unsignedShort(co11.u)){
			// Four-color block: derive the other two colors.
			r = (2 * color_array[0].getR() + color_array[1].getR()) / 3;
			g = (2 * color_array[0].getG() + color_array[1].getG()) / 3;
			b = (2 * color_array[0].getB() + color_array[1].getB()) / 3;
			
			color_array[2].set(r, g, b, a);
			
			r = (2 * color_array[1].getR() + color_array[0].getR()) / 3;
			g = (2 * color_array[1].getG() + color_array[0].getG()) / 3;
			b = (2 * color_array[1].getB() + color_array[0].getB()) / 3;
			
			color_array[3].set(r, g, b, a);
			
			return 4;
		}else{
			// Three-color block: derive the other color.
			r = (color_array[0].getR() + color_array[1].getR()) / 2;
			g = (color_array[0].getG() + color_array[1].getG()) / 2;
			b = (color_array[0].getB() + color_array[1].getB()) / 2;
			color_array[2].set(r, g, b, a);
			
			// Set all components to 0 to match DXT specs.
			r = g = b = a = 0x00;
			color_array[3].set(r, g, b, a);
			
			return 3;
		}
	}
	
	public int evaluatePalette(int[] color_array){
		int sr = co10.getR();
		int sg = co10.getG();
		int sb = co10.getB();
		
		int b = (sb << 3) | (sb >> 2);
		int g = (sg << 2) | (sg >> 4);
		int r = (sr << 3) | (sr >> 2);
		int a = 255;
		
		color_array[0] = NvUtils.makefourcc(r, g, b, a);
		
		sr = co11.getR();
		sg = co11.getG();
		sb = co11.getB();
		
		b = (sb << 3) | (sb >> 2);
		g = (sg << 2) | (sg >> 4);
		r = (sr << 3) | (sr >> 2);
		
		color_array[1] = NvUtils.makefourcc(r, g, b, a);
		
		if(NvUtils.unsignedShort(co10.u) > NvUtils.unsignedShort(co11.u)){
			// Four-color block: derive the other two colors.
			r = (2 * NvPackedColor.getRedFromRGB( color_array[0]) + NvPackedColor.getRedFromRGB( color_array[1])) / 3;
			g = (2 * NvPackedColor.getGreen(      color_array[0]) + NvPackedColor.getGreen(      color_array[1])) / 3;
			b = (2 * NvPackedColor.getBlueFromRGB(color_array[0]) + NvPackedColor.getBlueFromRGB(color_array[1])) / 3;
			color_array[2] = NvUtils.makefourcc(r, g, b, a);
			
			r = (2 * NvPackedColor.getRedFromRGB( color_array[1]) + NvPackedColor.getRedFromRGB( color_array[2])) / 3;
			g = (2 * NvPackedColor.getGreen(      color_array[1]) + NvPackedColor.getGreen(      color_array[2])) / 3;
			b = (2 * NvPackedColor.getBlueFromRGB(color_array[1]) + NvPackedColor.getBlueFromRGB(color_array[2])) / 3;
			color_array[3] = NvUtils.makefourcc(r, g, b, a);
			
			return 4;
		}else{
			// Three-color block: derive the other color.
			r = (NvPackedColor.getRedFromRGB( color_array[0]) + NvPackedColor.getRedFromRGB( color_array[1])) / 2;
			g = (NvPackedColor.getGreen(      color_array[0]) + NvPackedColor.getGreen(      color_array[1])) / 2;
			b = (NvPackedColor.getBlueFromRGB(color_array[0]) + NvPackedColor.getBlueFromRGB(color_array[1])) / 2;
			color_array[2] = NvUtils.makefourcc(r, g, b, a);
			
			// Set all components to 0 to match DXT specs.
			r = g = b = a = 0x00;
			color_array[3] = 0;
			
			return 3;
		}
	}
	
	/** Evaluate palette assuming 3 color block. */
	public void evaluatePalette3(int[] color_array){
		int sr = co10.getR();
		int sg = co10.getG();
		int sb = co10.getB();
		
		int b = (sb << 3) | (sb >> 2);
		int g = (sg << 2) | (sg >> 4);
		int r = (sr << 3) | (sr >> 2);
		int a = 255;
		
		color_array[0] = NvUtils.makefourcc(r, g, b, a);
		
		sr = co11.getR();
		sg = co11.getG();
		sb = co11.getB();
		
		b = (sb << 3) | (sb >> 2);
		g = (sg << 2) | (sg >> 4);
		r = (sr << 3) | (sr >> 2);
		
		color_array[1] = NvUtils.makefourcc(r, g, b, a);
		
		// Three-color block: derive the other color.
		r = (NvPackedColor.getRedFromRGB( color_array[0]) + NvPackedColor.getRedFromRGB( color_array[1])) / 2;
		g = (NvPackedColor.getGreen(      color_array[0]) + NvPackedColor.getGreen(      color_array[1])) / 2;
		b = (NvPackedColor.getBlueFromRGB(color_array[0]) + NvPackedColor.getBlueFromRGB(color_array[1])) / 2;
		color_array[2] = NvUtils.makefourcc(r, g, b, a);
		
		// Set all components to 0 to match DXT specs.
		r = g = b = a = 0x00;
		color_array[3] = 0;
	}
	
	/** Evaluate palette assuming 3 color block. */
	public void evaluatePalette3(Color32[] color_array){
		int sr = co10.getR();
		int sg = co10.getG();
		int sb = co10.getB();
		
		int b = (sb << 3) | (sb >> 2);
		int g = (sg << 2) | (sg >> 4);
		int r = (sr << 3) | (sr >> 2);
		int a = 255;
		
		color_array[0].set(r, g, b, a);
		
		sr = co11.getR();
		sg = co11.getG();
		sb = co11.getB();
		
		b = (sb << 3) | (sb >> 2);
		g = (sg << 2) | (sg >> 4);
		r = (sr << 3) | (sr >> 2);
		
		color_array[1].set(r, g, b, a);
		
		// Three-color block: derive the other color.
		r = (color_array[0].getR() + color_array[1].getR()) / 2;
		g = (color_array[0].getG() + color_array[1].getG()) / 2;
		b = (color_array[0].getB() + color_array[1].getB()) / 2;
		color_array[2].set(r, g, b, a);
		
		// Set all components to 0 to match DXT specs.
		r = g = b = a = 0x00;
		color_array[3].set(r, g, b, a);
	}
	
	/** Evaluate palette assuming 4 color block. */
	public void evaluatePalette4(int[] color_array){
		int sr = co10.getR();
		int sg = co10.getG();
		int sb = co10.getB();
		
		int b = (sb << 3) | (sb >> 2);
		int g = (sg << 2) | (sg >> 4);
		int r = (sr << 3) | (sr >> 2);
		int a = 255;
		
		color_array[0] = NvUtils.makefourcc(r, g, b, a);
		
		sr = co11.getR();
		sg = co11.getG();
		sb = co11.getB();
		
		b = (sb << 3) | (sb >> 2);
		g = (sg << 2) | (sg >> 4);
		r = (sr << 3) | (sr >> 2);
		
		color_array[1] = NvUtils.makefourcc(r, g, b, a);
		
		r = (2 * NvPackedColor.getRedFromRGB( color_array[0]) + NvPackedColor.getRedFromRGB( color_array[1])) / 3;
		g = (2 * NvPackedColor.getGreen(      color_array[0]) + NvPackedColor.getGreen(      color_array[1])) / 3;
		b = (2 * NvPackedColor.getBlueFromRGB(color_array[0]) + NvPackedColor.getBlueFromRGB(color_array[1])) / 3;
		color_array[2] = NvUtils.makefourcc(r, g, b, a);
		
		r = (2 * NvPackedColor.getRedFromRGB( color_array[1]) + NvPackedColor.getRedFromRGB( color_array[0])) / 3;
		g = (2 * NvPackedColor.getGreen(      color_array[1]) + NvPackedColor.getGreen(      color_array[0])) / 3;
		b = (2 * NvPackedColor.getBlueFromRGB(color_array[1]) + NvPackedColor.getBlueFromRGB(color_array[0])) / 3;
		color_array[3] = NvUtils.makefourcc(r, g, b, a);
	}
	
	/** Evaluate palette assuming 4 color block. */
	public void evaluatePalette4(Color32[] color_array){
		int sr = co10.getR();
		int sg = co10.getG();
		int sb = co10.getB();
		
		int b = (sb << 3) | (sb >> 2);
		int g = (sg << 2) | (sg >> 4);
		int r = (sr << 3) | (sr >> 2);
		int a = 255;
		
		color_array[0].set(r, g, b, a);
		
		sr = co11.getR();
		sg = co11.getG();
		sb = co11.getB();
		
		b = (sb << 3) | (sb >> 2);
		g = (sg << 2) | (sg >> 4);
		r = (sr << 3) | (sr >> 2);
		
		color_array[1].set(r, g, b, a);
		
		// Four-color block: derive the other two colors.
		r = (2 * color_array[0].getR() + color_array[1].getR()) / 3;
		g = (2 * color_array[0].getG() + color_array[1].getG()) / 3;
		b = (2 * color_array[0].getB() + color_array[1].getB()) / 3;
		
		color_array[2].set(r, g, b, a);
		
		r = (2 * color_array[1].getR() + color_array[0].getR()) / 3;
		g = (2 * color_array[1].getG() + color_array[0].getG()) / 3;
		b = (2 * color_array[1].getB() + color_array[0].getB()) / 3;
		
		color_array[3].set(r, g, b, a);
	}
	
	public void decodeBlock(ColorBlock block){
		int[] color_array = new int[4];
		evaluatePalette(color_array);
		
		// Write color block.
	    for( int j = 0; j < 4; j++ ) {
	        for( int i = 0; i < 4; i++ ) {
	        	int row = (indices >> j * 8) & 0xFF;
	            int idx = (row >> (2 * i)) & 3;
//	            block->color(i, j) = color_array[idx];
	            block.set(i, j, color_array[idx]);
	        }
	    }    
	}
	
	public void setIndices(int[] idx){
		indices = 0;
		for(int i = 0; i < 16; i++) {
	        indices |= (idx[i] & 3) << (2 * i);
	    }
	}
	
}
