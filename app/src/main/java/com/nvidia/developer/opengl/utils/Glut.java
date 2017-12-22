package com.nvidia.developer.opengl.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;

import javax.microedition.khronos.opengles.GL10;

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
		int sizePerPixel;
		int internalFormat;
		int format;
		
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

		Pixels pixels = Pixels.createPixels(bitmap.getWidth(), bitmap.getHeight(), sizePerPixel);
		ByteBuffer buffer = pixels.buffer;
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

	public static int loadTextureFromFile(String filename, int filter, int wrap){
		int textureID = GLES.glGenTextures();
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID);
		Bitmap image = loadBitmapFromAssets(filename);
		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, image, 0);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, filter);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, filter);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, wrap);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, wrap);

		return textureID;
	}

	public static final StringBuilder loadTextFromClassPath(Class<?> clazz, String filename){
		String packName = clazz.getName();
		packName = packName.replace(clazz.getSimpleName(), "");
		packName = packName.replace('.', '/');
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		packName+= filename;
		InputStream input = ClassLoader.getSystemResourceAsStream(packName);
		if(input == null)
			throw  new NullPointerException();

		BufferedReader in = new BufferedReader(new InputStreamReader(input));
		StringBuilder sb = new StringBuilder();
		String s;

		try {
			while ((s = in.readLine()) != null)
				sb.append(s).append('\n');
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb;
	}

	public static int nvImageLoadTextureArrayFromDDSData(String[] ddsFile, int count) {
		int texID = GLES.glGenTextures();
		GLES30.glBindTexture(GLES30.GL_TEXTURE_2D_ARRAY, texID);

		for (int i = 0; i < count; i++) {
			NvImage image = NvImage.createFromDDSFile(ddsFile[i]);

			if (image != null) {
				int w = image.getWidth();
				int h = image.getHeight();
				if (i == 0) {
					if (image.isCompressed())
					{
//                        glCompressedTexImage3DOES(GL_TEXTURE_2D_ARRAY_EXT, 0, image.getInternalFormat(),
//                            w, h, count, 0, image.getImageSize(0)*count, NULL);
						GLES30.glCompressedTexImage3D(GLES30.GL_TEXTURE_2D_ARRAY, 0, image.getInternalFormat(), w, h, count, 0, image.getImageSize(0)*count, (ByteBuffer)null);
					}
					else
					{
//                           glTexImage3DOES(GL_TEXTURE_2D_ARRAY_EXT, 0, image.getFormat(), w, h, count, 0,
//                            image.getFormat(), image.getType(), NULL);
						GLES30.glTexImage3D(GLES30.GL_TEXTURE_2D_ARRAY, 0, image.getInternalFormat(), w, h, count, 0, image.getFormat(), image.getType(), (ByteBuffer)null);
					}
				}
				if (image.isCompressed())
				{
//                    glCompressedTexSubImage3DOES(GL_TEXTURE_2D_ARRAY_EXT, 0, 0, 0, i, w, h, 1, image.getInternalFormat(),
//                        image.getImageSize(0), image.getLevel(0));
					GLES30.glCompressedTexSubImage3D(GLES30.GL_TEXTURE_2D_ARRAY, 0, 0, 0, i, w, h, 1, image.getInternalFormat(),
							image.getImageSize(0), GLUtil.wrap(image.getLevel(0)));
				}
				else
				{
//                    glTexSubImage3DOES(GL_TEXTURE_2D_ARRAY_EXT, 0, 0, 0, i, w, h, 1, image.getFormat(),
//                        image.getType(), image.getLevel(0));
					GLES30.glTexSubImage3D(GLES30.GL_TEXTURE_2D_ARRAY, 0, 0, 0, i, w, h, 1, image.getFormat(), image.getType(), GLUtil.wrap(image.getLevel(0)));
				}
			}
		}

		return texID;
	}

	public static InputStream readFileStream(String filename) {
		try {
			return assetManager.open(filename);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
}
