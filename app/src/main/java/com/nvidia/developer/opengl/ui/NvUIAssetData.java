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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

final class NvUIAssetData {
	
	private static final Class<NvUIAssetData> clazz = NvUIAssetData.class;
	
	private static boolean inited = false;
	static final int staticAssetCount = 26;
    static final byte[][] staticAssetData = new byte[staticAssetCount][];
    static final String[] staticAssetNames = new String[staticAssetCount];
    static final int[]    staticAssetLen  = new int[staticAssetCount];
	
	static{
		init();
	}
	
	static synchronized void init(){
		if(inited)
			return;
		
		inited = true;
		String packName = clazz.getName();
		packName = packName.replace("NvUIAssetData", "");
		packName = packName.replace('.', '/');
		
		BufferedReader in = new BufferedReader(new InputStreamReader(clazz.getClassLoader().getResourceAsStream(packName + "NvUIAssetData.h")));
		byte[] data = null;
	    
		int cursor = 0;
		int i = 0;
		String line;
		
		try {
			while((line = in.readLine()) != null){
				line = line.trim();
				
				if(line.length() == 0 || line.startsWith("//") || line.startsWith("/*"))
					continue;
				
				if(line.startsWith("const")){
					if(line.lastIndexOf(';') != -1){ // const long int %s = %d;
						String[] strs = line.split(" ");
						staticAssetLen[cursor] = Integer.parseInt(strs[5].replace(";", ""));
						data = new byte[staticAssetLen[cursor]];
						staticAssetData[cursor] = data;
					}else{ // const unsigned char %s[%d] = {
						String[] strs = line.split(" ");
						staticAssetNames[cursor] = strs[3].substring(0, strs[3].indexOf('['));
					}
				}else{
					if(line.equals("};")){
						if(i != staticAssetLen[cursor]){
							System.err.println("missing some data, the " + cursor + "th data's size is " + i +", less than " + staticAssetLen[cursor]);
						}
						cursor ++;
						i = 0;
					}else{
						String[] strs = line.split(",");
						for(int j = 0; j < strs.length; j++){
							if(strs[j].length() == 0)
								continue;
							
							data[i++] = (byte) Integer.parseInt(strs[j]);
						}
					}
				}
			}
			
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static byte[] embeddedAssetLookup(String filename){
		for(int i = 0; i < NvUIAssetData.staticAssetCount; i++){
			if(filename.equals(NvUIAssetData.staticAssetFilenames[i])){
				return NvUIAssetData.staticAssetData[i];
			}
		}
		
		return null;
	}
	
	public static void main(String[] args) {
		NvUIAssetData.init();
	}
	
	static final String[] staticAssetFilenames = {
		"Courier-w-bold-24.dds",
	    "RobotoCond-w-bold-24.dds",
	    "arrow_blue.dds",
	    "arrow_blue_down.dds",
	    "arrow_blue_left.dds",
	    "arrow_pressed_down.dds",
	    "btn_box_blue.dds",
	    "btn_box_pressed_x.dds",
	    "btn_round_blue.dds",
	    "btn_round_pressed.dds",
	    "button_top_row.dds",
	    "button_top_row_locked.dds",
	    "button_top_row_pressed.dds",
	    "frame_thin.dds",
	    "frame_thin_dropshadow.dds",
	    "icon_button_highlight_small.dds",
	    "info_text_box_thin.dds",
	    "popup_frame.dds",
	    "rounding.dds",
	    "slider_empty.dds",
	    "slider_full.dds",
	    "slider_thumb.dds",
	    "Courier-24.fnt",
	    "Courier-Bold-24.fnt",
	    "RobotoCondensed-Bold-24.fnt",
	    "RobotoCondensed-Regular-24.fnt",
	};
	
	private NvUIAssetData(){}
}
