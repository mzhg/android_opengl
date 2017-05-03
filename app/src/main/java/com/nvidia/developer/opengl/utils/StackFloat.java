package com.nvidia.developer.opengl.utils;

import org.lwjgl.util.vector.Matrix4f;

import java.util.Arrays;

public class StackFloat {

	private float[] items;
	private transient int size;

	public StackFloat() {
		this(4);
	}

	public StackFloat(int capacity) {
		capacity = capacity < 1 ? 1 : capacity;
		items = new float[capacity];
	}

	public void set(int index, float value) {
		if (index >= size)
			throw new IndexOutOfBoundsException("index = " + index + ", size = " + size);

		items[index] = value;
	}
	
	public void addAll(StackFloat ints){
		for(int i = 0; i < ints.size;i++){
			push(ints.get(i));
		}
	}
	
	public void copyFrom(StackFloat ints){
		size = 0;
		addAll(ints);
	}

	public void plus(int index, float value) {
		if (index < 0 || index >= size)
			throw new IndexOutOfBoundsException();

		items[index] += value;
	}

	public void push(float item) {
		if (size == items.length)
			items = (float[]) NvUtils.resizeArray(items, items.length * 2);

		items[size++] = item;
	}
	
	public void push(Matrix4f mat){
		int newSize = size + 16;
		if(newSize > items.length){
			int capacity = items.length * 2;
			while(capacity < newSize)
				capacity *= 2;
			
			items = (float[]) NvUtils.resizeArray(items, capacity);
		}
		
		items[size++] = mat.m00;
		items[size++] = mat.m01;
		items[size++] = mat.m02;
		items[size++] = mat.m03;
		items[size++] = mat.m10;
		items[size++] = mat.m11;
		items[size++] = mat.m12;
		items[size++] = mat.m13;
		items[size++] = mat.m20;
		items[size++] = mat.m21;
		items[size++] = mat.m22;
		items[size++] = mat.m23;
		items[size++] = mat.m30;
		items[size++] = mat.m31;
		items[size++] = mat.m32;
		items[size++] = mat.m33;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public int size() {
		return size;
	}

	public void clear() {
		size = 0;
		if (items.length >= 256) {
			items = new float[8];
		}
	}

	public float pop() {
		if (isEmpty())
			throw new NullPointerException("stack is empty!");

		return items[--size];
	}

	public float[] getData() {
		return items;
	}

	public float[] toArray() {
		float[] tmp = new float[size];
		System.arraycopy(items, 0, tmp, 0, size);
		return tmp;
	}

	public float get(int index) {
		return items[index];
	}

	public float peer() {
		if (isEmpty())
			throw new NullPointerException("stack is empty!");

		return items[size - 1];
	}

	public void reserve(int cap) {
		if (cap > items.length) {
			items = (float[]) NvUtils.resizeArray(items, cap);
		}
	}

	public void resize(int size, float value) {
		if (size < 0)
			throw new IllegalArgumentException("size < 0");
		if (size >= items.length)
			items = new float[size];
		Arrays.fill(items, 0, size, value);

		this.size = size;
	}

	public void resize(int size) {
		if (size < 0)
			throw new IllegalArgumentException("size < 0");

		if (size >= items.length)
			items = Arrays.copyOf(items, size);

		this.size = size;
	}
	
	public StackFloat copy(){
		StackFloat sf = new StackFloat(size);
		System.arraycopy(items, 0, sf.items, 0, size);
		return sf;
	}
}
