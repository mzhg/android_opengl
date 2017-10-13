package com.nvidia.developer.opengl.utils;

import android.opengl.GLES11;

import org.lwjgl.util.vector.Vector3b;

public class Pixels3 extends Pixels{

	public Pixels3(int width, int height) {
		super(width, height, 3);
		
		this.internalFormat = GLES11.GL_RGB;
		this.format = GLES11.GL_RGB;
	}
	
	public Vector3b get(int w, int h){
		final int index = (h * width + w) * 3;
		Vector3b tmp = new Vector3b();
		tmp.x = buffer.get(index + 0);
		tmp.y = buffer.get(index + 1);
		tmp.z = buffer.get(index + 2);
		return tmp;
	}
	
	public void set(int w, int h, Vector3b value){
		final int index = (h * width + w) * 3;
		buffer.put(index + 0, value.x);
		buffer.put(index + 1, value.y);
		buffer.put(index + 2, value.z);
	}
	
	public void assgin(int dstW, int dstH, int srcW, int srcH){
		final int dstIndex = (dstH * width + dstW) * 3;
		final int srcIndex = (srcH * width + srcW) * 3;
		
		buffer.put(dstIndex + 0, buffer.get(srcIndex + 0));
		buffer.put(dstIndex + 1, buffer.get(srcIndex + 1));
		buffer.put(dstIndex + 2, buffer.get(srcIndex + 2));
	}
	
	public void swap(int w1, int h1, int w2, int h2){
		final int index1 = (h1 * width + w1) * 3;
		final int index2 = (h2 * width + w2) * 3;
		
		final byte v1x = buffer.get(index1 + 0);
		final byte v1y = buffer.get(index1 + 1);
		final byte v1z = buffer.get(index1 + 2);
		final byte v2x = buffer.get(index2 + 0);
		final byte v2y = buffer.get(index2 + 1);
		final byte v2z = buffer.get(index2 + 2);
		
		buffer.put(index1 + 0, v2x);
		buffer.put(index1 + 1, v2y);
		buffer.put(index1 + 2, v2z);
		buffer.put(index2 + 0, v1x);
		buffer.put(index2 + 1, v1y);
		buffer.put(index2 + 2, v1z);
	}
	
}
