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

import android.util.Log;

public final class NvLogger {

	public static final int INFO = 1;
	public static final int ERROR = 0;
	static int level = 0;
	
	public static void setLevel(int lv){
		level =lv;
	}
	
	public static void i(String msg){
		if(level >= INFO){
			int index = msg.indexOf("::");
			
			if(index > 0){
				String tag = msg.substring(0, index);
				String info = msg.substring(index + 2);
				Log.i(tag, info);
			}else{
				Log.i("NvLogger", msg);
			}
		}
	}
	
	public static void i(String msg, Object...args){
		if(level >= INFO){
			int index = msg.indexOf("::");
			if(index > 0){
				String tag = msg.substring(0, index);
				String info = msg.substring(index + 2);
				Log.i(tag, NvUtils.sprintf(info, args));
			}else{
				Log.i("NvLogger", NvUtils.sprintf(msg, args));
			}
		}
	}
	
	public static void e(String msg){
		if(level >= ERROR){
			int index = msg.indexOf("::");
			if(index > 0){
				String tag = msg.substring(0, index);
				String info = msg.substring(index + 2);
				Log.e(tag, info);
			}else{
				Log.e("NvLogger", msg);
			}
		}
	}
	
	@Deprecated
	public static void ef(String msg, Object...args){
		if(level >= ERROR){
			int index = msg.indexOf("::");
			if(index > 0){
				String tag = msg.substring(0, index);
				String info = msg.substring(index + 2);
				Log.e(tag, NvUtils.sprintf(info, args));
			}else{
				Log.e("NvLogger", NvUtils.sprintf(msg, args));
			}
		}
	}
	
	public static void e(String msg, Object...args){
		if(level >= ERROR){
			int index = msg.indexOf("::");
			if(index > 0){
				String tag = msg.substring(0, index);
				String info = msg.substring(index + 2);
				Log.e(tag, NvUtils.sprintf(info, args));
			}else{
				Log.e("NvLogger", NvUtils.sprintf(msg, args));
			}
		}
	}
}
