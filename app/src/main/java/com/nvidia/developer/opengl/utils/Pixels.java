package com.nvidia.developer.opengl.utils;

import java.nio.ByteBuffer;

import javax.microedition.khronos.opengles.GL11;

import android.opengl.GLES10;

public class Pixels {

	int width;           // the width of image in pixels
	int height;          // the height of image in pixels.
	int internalFormat;  // internalFormat
	int format;          // pixels data format
	int size;            // bit size of the per color component

	ByteBuffer buffer; // pixels data
	
	public void uploadTexture2D(boolean mipmap){
		uploadTexture2D(GLES10.GL_TEXTURE_2D, mipmap);
	}

	public void uploadTexture2D(int target, boolean mipmap) {
		GLES10.glTexImage2D(target, 0, internalFormat, width, height, 0, format, GL11.GL_UNSIGNED_BYTE, buffer);
	}
	
	public void uploadTexture2D(int target, int internalformat, int format, boolean mipmap){
		GLES10.glTexImage2D(target, 0, internalformat, width, height, 0, format, GL11.GL_UNSIGNED_BYTE, buffer);
	}

	public void uploadTexture2D() {
		uploadTexture2D(GL11.GL_TEXTURE_2D, false);
	}

	// public Pixels(String name) {
	// this.name = name;
	// }

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
	
	public int getSize(){
		return size;
	}

	
	public int getInternalFormat(){
		return internalFormat;
	}
	
	public void setInternalFormat(int internalFormat){
		this.internalFormat = internalFormat;
	}
	
	public int getFormat() {
		return format;
	}

	public ByteBuffer getBytesData(){
		return buffer;
	}
}
