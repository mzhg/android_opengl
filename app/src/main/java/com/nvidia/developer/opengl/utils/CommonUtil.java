package com.nvidia.developer.opengl.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.example.oglsamples.R;

public class CommonUtil {
	
	static Context context;
	
	public static void init(Context _context){
		context = _context;
	}
	/**
	 * 对传入的字符串进行非空判�?
	 * 
	 * @param str
	 */
	public static boolean isNotNull(String str) {
		return str != null && !str.equals("") ? true : false;
	}
	
	/** 判断字符串是否是空白字符 */
	public static boolean isNotBlank(CharSequence str){
		if(str == null) return false;
		
		for(int i = 0; i < str.length(); i++)
			if(!Character.isWhitespace(str.charAt(i)))
				return true;
		
		return false;
	}

	/**
	 * 根据手机的分辨率�? dp 的单�? 转成�? px(像素)
	 */
	public static int dip2px(float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	/**
	 * 根据手机的分辨率�? px(像素) 的单�? 转成�? dp
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	/**
	 * 判断密码强度
	 * 
	 * @param psd
	 * @return 1：弱�?2：中�?3：强
	 */
	public static int getPwdStrength(String psd) {
		int pwd_num = 0;// 数字�?
		int pwd_letter = 0;// 大写字母
		int pwd_special_char = 0;// 小写字母
		int len = psd.length();
		for (int i = 0; i < len; i++) {
			char elestr = psd.charAt(i);
			if (elestr >= 48 && elestr <= 57) { // 数字
				pwd_num++;
			} else if ((elestr >= 65 && elestr <= 90) || (elestr >= 97 && elestr <= 122)) { // 大写字母
				pwd_letter++;
			} else { // 特殊字符
				pwd_special_char++;
			}
		}
		if ((pwd_num > 0 && pwd_letter == 0 && pwd_special_char == 0) || (pwd_letter > 0 && pwd_num == 0 && pwd_special_char == 0) || (pwd_special_char > 0 && pwd_num == 0 && pwd_letter == 0)) {
			return 1;
		} else if (pwd_num > 0 && pwd_letter > 0 && pwd_special_char > 0) {
			return 3;
		} else if ((pwd_num > 0 && pwd_letter > 0 && pwd_special_char == 0) || (pwd_letter > 0 && pwd_num == 0 && pwd_special_char > 0) || (pwd_special_char > 0 && pwd_num > 0 && pwd_letter == 0)) {
			return 2;
		} else {
			return 0;
		}
	}

	/**
	 * 添加程序的快捷方�?
	 */
	public static void addShortCut(Context context) {

		Intent shortcut = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
		// 设置属�??
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, context.getResources().getString(R.string.app_name));

		// 是否允许重复创建
		shortcut.putExtra("duplicate", false);

		// 设置桌面快捷方式的图�?
		ShortcutIconResource icon = Intent.ShortcutIconResource.fromContext(context, R.drawable.ic_launcher);
//		Bitmap icon = ImageUtil.generatorContactCountIcon(context, ((BitmapDrawable) (context.getApplicationContext().getResources().getDrawable(R.drawable.app_icon))).getBitmap());
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON, icon);

		// 点击快捷方式的操�?
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
//		intent.setClass(context, SplashActivity.class); TODO
		// intent.setClass(this, this.getClass().getName());

		// 设置启动程序
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);

		// 广播通知桌面去创�?
		context.sendBroadcast(shortcut);
	}

	/**
	 * 删除程序的快捷方�?
	 */
	public static void delShortcut(Context context) {
		Intent shortcut = new Intent("com.android.launcher.action.UNINSTALL_SHORTCUT");

		// 快捷方式的名�?
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, context.getResources().getString(R.string.app_name));
//		String appClass = context.getPackageName() + "." + MainActivity.class;
//		ComponentName comp = new ComponentName(context.getPackageName(), appClass);
//		shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(Intent.ACTION_MAIN).setComponent(comp));
//
//		context.sendBroadcast(shortcut);

		/** 改成以下方式能够成功删除，估计是删除和创建需要对应才能找到快捷方式并成功删除 **/
//		Intent intent = new Intent();
		Intent intent = new Intent(Intent.ACTION_MAIN);
//		intent.setClass(context, SplashActivity.class); // TODO
//		intent.setAction("android.intent.action.MAIN");
//		intent.setAction(Intent.ACTION_MAIN);
//		intent.addCategory("android.intent.category.LAUNCHER");
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
		context.sendBroadcast(shortcut);
	}

	/**
	 * 获取版本�?
	 */
	public static String getVersion(Context context) {
		PackageManager pm = context.getPackageManager();

		PackageInfo pinfo = null;
		try {
			pinfo = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_CONFIGURATIONS);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		String versionName = pinfo.versionName;
		return versionName;
	}
	
	public static boolean isAppInstalled(Context context, String labelName, String packageName){
		PackageManager pm = context.getPackageManager();

		PackageInfo pinfo = null;
		try {
			pinfo = pm.getPackageInfo(packageName, PackageManager.GET_CONFIGURATIONS);
		} catch (NameNotFoundException e) {
			return false;
		}
		
		return pinfo != null;
		
//		if(appInfos == null || appInfos.get() == null){
//			PackageManager pm = context.getPackageManager();
//			// 查询�?有已经安装的应用程序
//			List<ApplicationInfo> listAppcations = pm
//					.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
//			Collections.sort(listAppcations,
//					new ApplicationInfo.DisplayNameComparator(pm));// 排序
//			List<AppInfo> _appInfos = new ArrayList<AppInfo>(); // 保存过滤查到的AppInfo
//			for (ApplicationInfo app : listAppcations) {
//				_appInfos.add(getAppInfo(app, pm));
//			}
//			
//			appInfos = new WeakReference<List<AppInfo>>(_appInfos);
//		}
//		
//		List<AppInfo> _appInfos = appInfos.get();
//		if(_appInfos == null){
//			Log.w("CommonUtil", "low momery!");
//			return false;
//		}
//		
//		for(AppInfo info : _appInfos){
//			if(info.getAppLabel().equals(labelName) && info.getPkgName().startsWith(packageName))
//				return true;
//		}
//		
//		return false;
	}
	
	
	/**
	 * get deviceid
	 * @param context
	 *            add  <uses-permission android:name="READ_PHONE_STATE" /> 
	 * @return
	 */
	public static String getDeviceID(Context context) {
	    if(context==null){
	        return "";
	    }
		if(checkPermissions(context, "android.permission.READ_PHONE_STATE")){
			String deviceId = "";
			if (checkPhoneState(context)) {
				TelephonyManager tm = (TelephonyManager) context
						.getSystemService(Context.TELEPHONY_SERVICE);
				deviceId = tm.getDeviceId();
			}
			if (deviceId != null) {
				
				return deviceId;
			} else {
				
				return "";
			}
		}else{
			
			return "";
		}
	}
	/**
	 * check phone _state is readied ;
	 * 
	 * @param context
	 * @return
	 */
	public static boolean checkPhoneState(Context context) {
		PackageManager packageManager = context.getPackageManager();
		if (packageManager.checkPermission("android.permission.READ_PHONE_STATE", context
				.getPackageName()) != 0) {
			return false;
		}
		return true;
	}
	/**
	 * get  OS number
	 * @param context
	 * @return
	 */
	public static String getOsVersion(Context context) {
		String osVersion = "";
		if (checkPhoneState(context)) {
			osVersion = android.os.Build.VERSION.RELEASE;
			
			return osVersion;
		} else {
			
			return null;
		}
	}
	/**
	 * get sdk number
	 * @param paramContext
	 * @return
	 */
	public static String getSdkVersion(Context paramContext) {
		String osVersion = "";
		if (checkPhoneState(paramContext)) {
			osVersion = android.os.Build.VERSION.RELEASE;
			return osVersion;
		} else {
			return null;
		}
	}
	/**
	 *get   APPKEY�?�?渠道�?
	 * 
	 * @param context
	 * @return  appkey
	 */
	public static String getAppKey(Context paramContext) {
	    if(paramContext==null){
	        return "";
	    }
		String gtAppkey;
		try {
			PackageManager localPackageManager = paramContext
					.getPackageManager();
			ApplicationInfo localApplicationInfo = localPackageManager
					.getApplicationInfo(paramContext.getPackageName(), 128);
			if (localApplicationInfo != null) {
				String str = localApplicationInfo.metaData
						.getString("GT_APPKEY");
				if (str != null) {
					gtAppkey = str;
					return gtAppkey.toString();
				}
			}
		} catch (Exception localException) {
				localException.printStackTrace();
		}
		return "";
	}
	/**
	 * 设置Selector
	 */
	public static StateListDrawable newSelector(Context context, int idNormal, int idPressed, int idFocused, int idUnable) {
		StateListDrawable bg = new StateListDrawable();
		Drawable normal = idNormal == -1 ? null : context.getResources().getDrawable(idNormal);
		Drawable pressed = idPressed == -1 ? null : context.getResources().getDrawable(idPressed);
		Drawable focused = idFocused == -1 ? null : context.getResources().getDrawable(idFocused);
		Drawable unable = idUnable == -1 ? null : context.getResources().getDrawable(idUnable);
		// View.PRESSED_ENABLED_STATE_SET
		bg.addState(new int[] { android.R.attr.state_pressed, android.R.attr.state_enabled }, pressed);
		// View.ENABLED_FOCUSED_STATE_SET
		bg.addState(new int[] { android.R.attr.state_enabled, android.R.attr.state_focused }, focused);
		// View.ENABLED_STATE_SET
		bg.addState(new int[] { android.R.attr.state_enabled }, normal);
		// View.FOCUSED_STATE_SET
		bg.addState(new int[] { android.R.attr.state_focused }, focused);
		// View.WINDOW_FOCUSED_STATE_SET
		bg.addState(new int[] { android.R.attr.state_window_focused }, unable);
		// View.EMPTY_STATE_SET
		bg.addState(new int[] {}, normal);
		return bg;
	}
	/**
	 * Determine the current networking is WIFI
	 * @param context
	 * @return
	 */
	public  static boolean currentNoteworkTypeIsWIFI(Context context){
		ConnectivityManager connectionManager = (ConnectivityManager)context.
                getSystemService(Context.CONNECTIVITY_SERVICE);   
		return	connectionManager.getActiveNetworkInfo().getType()==ConnectivityManager.TYPE_WIFI;
	}
	/**
	 * Judge wifi is available
	 * @param inContext
	 * @return
	 */
	    public static boolean isWiFiActive(Context inContext) { 
	    	if(checkPermissions(inContext, "android.permission.ACCESS_WIFI_STATE")){
	    		 Context context = inContext.getApplicationContext();  
	    	        ConnectivityManager connectivity = (ConnectivityManager) context  
	    	                .getSystemService(Context.CONNECTIVITY_SERVICE);  
	    	        if (connectivity != null) {  
	    	            NetworkInfo[] info = connectivity.getAllNetworkInfo();  
	    	            if (info != null) {  
	    	                for (int i = 0; i < info.length; i++) {  
	    	                    if (info[i].getTypeName().equals("WIFI") && info[i].isConnected()) {  
	    	                        return true;  
	    	                    }  
	    	                }  
	    	            }  
	    	        }  
	    	        return false;
	    	}else{
    			Log.e("lost permission", "lost--->android.permission.ACCESS_WIFI_STATE");
	    		
	    		return false;
	    	}
	    }  
		
		
		/**
		 * Testing equipment networking and networking WIFI
		 * 
		 * @param context
		 * @return true or false 
		 */
		public static boolean isNetworkAvailable(Context context) {
			if(checkPermissions(context, "android.permission.INTERNET")){
				ConnectivityManager cManager=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE); 
				NetworkInfo info = cManager.getActiveNetworkInfo(); 
					if (info != null && info.isAvailable()){     
				        return true; 
				  }else{ 
					  Log.e("error", "Network error");
				       
				        return false; 
				  } 
				
				
				
				  
			}else{
				Log.e(" lost  permission", "lost----> android.permission.INTERNET");
				
				return false;
			}
			

		}
		/**
		   * Determine the current network type  
		   * @param context
		   * @return
		   */
		public static boolean isNetworkTypeWifi(Context context) {
			

			if(checkPermissions(context, "android.permission.INTERNET")){
				ConnectivityManager cManager=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE); 
				NetworkInfo info = cManager.getActiveNetworkInfo(); 
				
					if (info != null && info.isAvailable()&&info.getTypeName().equals("WIFI")){ 
				        return true; 
				  }else{ 
					  Log.e("error", "Network not wifi");
				        return false; 
				  } 
			}else{
				Log.e(" lost  permission", "lost----> android.permission.INTERNET");
				return false;
			}
		}
		
		/**
		 * checkPermissions
		 * @param context
		 * @param permission  
		 * @return true or  false
		 */
		public static boolean checkPermissions(Context context, String permission) {
			PackageManager localPackageManager = context.getPackageManager();
			return localPackageManager.checkPermission(permission, context
					.getPackageName()) == PackageManager.PERMISSION_GRANTED;
		}
		
		/**
		 * Try to return the absolute file path from the given Uri
		 *
		 * @param context
		 * @param uri
		 * @return the file path or null
		 */
		public static String getRealFilePath( final Context context, final Uri uri ) {
		    if ( null == uri ) return null;
		    final String scheme = uri.getScheme();
		    String data = null;
		    if ( scheme == null )
		        data = uri.getPath();
		    else if ( ContentResolver.SCHEME_FILE.equals( scheme ) ) {
		        data = uri.getPath();
		    } else if ( ContentResolver.SCHEME_CONTENT.equals( scheme ) ) {
		        Cursor cursor = context.getContentResolver().query( uri, new String[] { ImageColumns.DATA }, null, null, null );
		        if ( null != cursor ) {
		            if ( cursor.moveToFirst() ) {
		                int index = cursor.getColumnIndex( ImageColumns.DATA );
		                if ( index > -1 ) {
		                    data = cursor.getString( index );
		                }
		            }
		            cursor.close();
		        }
		    }
		    return data;
		}
		
		/**
		 * A smoothed step function. A cubic function is used to smooth the step between two thresholds.
		 * @param a the lower threshold position
		 * @param b the upper threshold position
		 * @param x the input parameter
		 * @return the output value
		 */
		public static float smoothStep(float a, float b, float x) {
			if (x < a)
				return 0;
			if (x >= b)
				return 1;
			x = (x - a) / (b - a);
			return x*x * (3 - 2*x);
		}
		
		/**
		 * 从Uri中获取图片，该方法可能会导致内存溢出
		 * @param context
		 * @param uri
		 * @return
		 */
		public static Bitmap getBitmapFromUri(final Context context,Uri uri)
		{
		  try {
		   // 读取uri�?在的图片
		   Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
		   return bitmap;
		  }catch (Exception e){
		   Log.e("[Android]", e.getMessage());
		   Log.e("[Android]", "目录为：" + uri);
		   e.printStackTrace();
		   return null;
		  }
		}
		
		public static Bitmap getScaledBitmapFromUri(final Context context,Uri uri, double scaled){
			if(Math.abs(scaled - 1.0) < 0.001){
				return getBitmapFromUri(context, uri);
			}else{
				Bitmap source = getBitmapFromUri(context, uri);
				Bitmap dest = Bitmap.createBitmap((int)(source.getWidth() * scaled), (int)(source.getHeight() * scaled), Config.RGB_565);
				Canvas g = new Canvas(dest);
				Rect src = new Rect(0, 0, source.getWidth(), source.getHeight());
				Rect dst = new Rect(0, 0, dest.getWidth(), dest.getHeight());
				g.drawBitmap(source, src, dst, null);
				source.recycle();
				return dest;
			}
		}
		
		
		public static Bitmap getBitmapFromUri(final Activity context, Uri uri, int width, int height, boolean keepRatio){
			if(width <= 0 || height <= 0){
				return getBitmapFromUri(context, uri);
			}
			
			String path = null;
			Cursor cursor = null;
			try {
				String[] proj = {MediaStore.Images.Media.DATA};
				cursor = context.managedQuery(uri, proj, null, null, null);
				int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				cursor.moveToFirst();
				path = cursor.getString(column_index);
				cursor.close();
			} catch (Exception e) {
				Log.e("CommonUtil", "Error occured when load bitmap from Uri: " + uri.toString());
				return null;
			} finally{
				if(cursor != null && !cursor.isClosed()){
					cursor.close();
				}
			}
			
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(path, options);
			
//			float w = (float)width/options.outWidth;
//			float h = (float)height/options.outHeight;
			options.inJustDecodeBounds = false;
			
			int samplerW = (int)((float)options.outWidth/ width + 0.5f);
			int samplerH = (int)((float)options.outHeight/ height + 0.5f);
			options.inSampleSize = Math.max(samplerW, samplerH);
			Bitmap source = BitmapFactory.decodeFile(path, options);
			if(width != source.getWidth() || height != source.getHeight()){
				float widthRatio = (float)width/source.getWidth();
				float heightRatio = (float)height/source.getHeight();
				
				Rect src = new Rect(0, 0, source.getWidth(), source.getHeight());
				Rect dst = new Rect();
				
//				if((widthRatio > 1 && heightRatio > 1) || (widthRatio < 1 && heightRatio < 1)){
					if(widthRatio < heightRatio){
						float h = source.getHeight() * widthRatio;
						dst.left = 0; dst.right = width;
						dst.top = (int) ((height - h) * 0.5f);
						dst.bottom = height - dst.top;
					}else{
						float w = source.getWidth() * heightRatio;
						dst.top = 0; dst.bottom = height;
						dst.left = (int) ((width - w) * 0.5f);
						dst.right = width - dst.left;
					}
//				}else{
//					if(widthRatio > heightRatio){
//						float h = source.getHeight() * widthRatio;
//						dst.left = 0; dst.right = width;
//						dst.top = (int) ((height - h) * 0.5f);
//						dst.bottom = height - dst.top;
//					}else{
//						float w = source.getWidth() * heightRatio;
//						dst.top = 0; dst.bottom = height;
//						dst.left = (int) ((width - w) * 0.5f);
//						dst.right = width - dst.left;
//					}
//				}
				
				
				Bitmap dest = Bitmap.createBitmap(width, height, Config.RGB_565);
				Canvas g = new Canvas(dest);
				g.drawBitmap(source, src, dst, null);
				source.recycle();
				return dest;
			}else
				return source;
			
			
//			if(keepRatio){
//				if(Math.abs(w - h) < 0.001f){ // almost keep ratio.
//					int samplerSize = (int) (1.0f/w);
//					if(samplerSize > 1){ // the demision of the bitmap is more than the diser demesion.
//						double frac = 1.0f/w - samplerSize;
//						if(Math.abs(1.0 - frac) < 0.001){
//							options.inSampleSize = samplerSize;
//							return BitmapFactory.decodeFile(path, options);
//						}else{
//							return getScaledBitmapFromUri(context, uri, w);
//						}
//					}else{
//						return getScaledBitmapFromUri(context, uri, w);
//					}
//				}else{
//					int samplerSize;
//					
//					if(w > h){
//						options.inSampleSize = (int) (1.0f/w + 0.5);
//					}
//				}
//			}else{
//				
//			}
		}
}
