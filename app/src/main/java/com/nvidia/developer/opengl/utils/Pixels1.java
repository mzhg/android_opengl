package com.nvidia.developer.opengl.utils;

public class Pixels1 extends Pixels{

	public Pixels1(int width, int height) {
		super(width, height, 1);
	}
	
	public byte get(int w, int h){
		return buffer.get(h * width + w);
	}
	
	public byte set(int w, int h, byte value){
		byte old = buffer.get(h * width + w);
		buffer.put(h * width + w, value);
		return old;
	}
	
	public byte assgin(int dstW, int dstH, int srcW, int srcH){
		final int dstIndex = dstH * width + dstW;
		byte old = buffer.get(dstIndex);
		buffer.put(dstIndex, buffer.get(srcH * width + srcW));
		return old;
	}
	
	public void swap(int w1, int h1, int w2, int h2){
		final int index1 = h1 * width + w1;
		final int index2 = h2 * width + w2;
		
		final byte v1 = buffer.get(index1);
		final byte v2 = buffer.get(index2);
		
		buffer.put(index1, v2);
		buffer.put(index2, v1);
	}
	
}
