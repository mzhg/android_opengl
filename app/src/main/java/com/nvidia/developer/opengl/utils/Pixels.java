////////////////////////////////////////////////////////////////////////////////
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

import android.opengl.GLES10;

import java.nio.ByteBuffer;

import javax.microedition.khronos.opengles.GL11;

public abstract class Pixels {

	int width;           // the width of image in pixels
	int height;          // the height of image in pixels.
	int internalFormat;  // internalFormat
	int format;          // pixels data format
	int size;            // bit size of the per color component

	ByteBuffer buffer; // pixels data
	String name;

	int cmapEntries;
	int cmapFormat;
	ByteBuffer cmap;  // for colormap

	public static Pixels createPixels(int width, int height, int size){
		switch (size) {
			case 1:
				return new Pixels1(width, height);
			case 2:
				return new Pixels2(width, height);
			case 3:
				return new Pixels3(width, height);
			case 4:
				return new Pixels4(width, height);
			default:
				throw new IllegalArgumentException("Invalid size: " + size);
		}
	}

	protected Pixels(int width, int height, int size){
		this.size = size;
		this.width = width;
		this.height = height;

		buffer = BufferUtils.createByteBuffer(size * width * height);
	}
	
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
