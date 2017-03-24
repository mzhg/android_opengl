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
