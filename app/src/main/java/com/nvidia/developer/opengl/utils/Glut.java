package com.nvidia.developer.opengl.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;

public final class Glut {

	private static Context context;
	private static AssetManager assetManager;
	
	public static void init(Object obj){
		if(obj == null) return;
		
		if(obj instanceof Context){
			context = (Context)obj;
		}else if(obj instanceof AssetManager){
			assetManager = (AssetManager)obj;
		}
		
		if(assetManager == null && context != null)
			assetManager = context.getAssets();
		
	}
	
	public static Pixels loadImageFromResource(int imgID){
		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), imgID);
		
		return convertBitmapToPixels(bitmap);
	}
	
	public static Pixels loadImageFromAssets(String filename){
		Bitmap bitmap = loadBitmapFromAssets(filename);
		return convertBitmapToPixels(bitmap);
	}
	
	private static Pixels convertBitmapToPixels(Bitmap bitmap){
		Pixels pixels = new Pixels();
		int sizePerPixel;
		int internalFormat;
		int format;
		
		pixels.width = bitmap.getWidth();
		pixels.height = bitmap.getHeight();
		
		Config config = bitmap.getConfig();
		
		switch (config) {
		case ALPHA_8:
			sizePerPixel = 1;
			internalFormat = GL10.GL_ALPHA;
			format = GL10.GL_ALPHA;
			break;
		case ARGB_4444:
		case ARGB_8888:
			sizePerPixel = 4;
			internalFormat = GL10.GL_RGBA;
			format = GL10.GL_RGBA;
			break;
		case RGB_565:
			sizePerPixel = 3;
			internalFormat = GL10.GL_RGB;
			format = GL10.GL_RGB;
			break;
		default:
		    throw new RuntimeException();
		}
		
		ByteBuffer buffer = pixels.buffer = ByteBuffer.allocateDirect(pixels.width * pixels.height * sizePerPixel).order(ByteOrder.nativeOrder());
		buffer.clear();

		for(int i = 0; i < pixels.height; i++){
			for(int j = 0; j < pixels.width; j++){
				int color = bitmap.getPixel(j, i);
				byte r = (byte) Color.red(color);
				byte g = (byte) Color.green(color);
				byte b = (byte) Color.blue(color);
				byte a = (byte) Color.alpha(color);
				
				if(sizePerPixel == 1){
					buffer.put(a);
				}else if(sizePerPixel == 3){
					buffer.put(r).put(g).put(b);
				}else if(sizePerPixel == 4){
					buffer.put(r).put(g).put(b).put(a);
				}
			}
		}
		
		buffer.flip();
		bitmap.recycle();
		
		pixels.format = format;
		pixels.internalFormat = internalFormat;
		pixels.size = sizePerPixel;
		
		return pixels;
	}
	
	public static Bitmap loadBitmapFromAssets(String filename){
		if(assetManager == null)
			throw new NullPointerException("assetManager is null");
		
		try {
			InputStream in = assetManager.open(filename);
			Bitmap bitmap = BitmapFactory.decodeStream(in);
			in.close();
			
			return bitmap;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
