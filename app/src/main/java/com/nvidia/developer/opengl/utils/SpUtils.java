package com.nvidia.developer.opengl.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * SharedPreference数据存取工具�?
 * 
 * @author hesen1
 * 
 */
public class SpUtils {

	private static SharedPreferences sp;

	private static void init(Context context) {
		if (sp == null) {
			sp = context.getSharedPreferences("goutong", Context.MODE_MULTI_PROCESS);
		}
	}

	public static void putValueToSp(Context context, String key, Object value) {
		if (sp == null) {
			init(context);
		}
		Editor edit = sp.edit();
		if (value instanceof Boolean) {
			edit.putBoolean(key, (Boolean) value);
		}
		if (value instanceof String) {
			edit.putString(key, (String) value);
		}
		if (value instanceof Integer) {
			edit.putInt(key, (Integer) value);
		}
		if (value instanceof Long) {
			edit.putLong(key, (Long) value);
		}
		if (value instanceof Float) {
			edit.putFloat(key, (Float) value);
		}
		edit.commit();
	}

	/** 从sp中取String取不到返回空字符�? **/
	public static String getStringToSp(Context context, String key) {
		if (sp == null) {
			init(context);
		}
		return sp.getString(key, "");
	}

	/** 从sp中取int取不到返�?0 **/
	public static int getIntToSp(Context context, String key) {
		if (sp == null) {
			init(context);
		}
		return sp.getInt(key, 0);
	}

	/** 从sp中取float取不到返�?0 **/
	public static float getFloatToSp(Context context, String key) {
		if (sp == null) {
			init(context);
		}
		return sp.getFloat(key, 0.0f);
	}

	/** 从sp中取long取不到返�?0 **/
	public static long getLongToSp(Context context, String key) {
		if (sp == null) {
			init(context);
		}
		return sp.getLong(key, 0);
	}

	/** 从sp中取boolean取不到返回false **/
	public static boolean getBooleanToSp(Context context, String key, boolean defaultvalue) {
		if (sp == null) {
			init(context);
		}
		return sp.getBoolean(key, defaultvalue);
	}

	public static void remove(Context context, String key) {
		if (sp == null) {
			init(context);
		}
		sp.edit().remove(key).commit();
	}

	public static void removeAll(Context context) {
		if (sp == null) {
			init(context);
		}
		sp.edit().clear().commit();
	}
}
