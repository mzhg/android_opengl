package com.nvidia.developer.opengl.utils;

import java.util.Arrays;

public class StackBool {

	private boolean[] items;
	private transient int size;

	public StackBool() {
		this(4);
	}

	public StackBool(int capacity) {
		capacity = capacity < 1 ? 1 : capacity;
		items = new boolean[capacity];
	}

	public void set(int index, boolean value) {
		if (index >= size)
			throw new IndexOutOfBoundsException("index = " + index + ", size = " + size);

		items[index] = value;
	}
	
	public void addAll(StackBool ints){
		for(int i = 0; i < ints.size;i++){
			push(ints.get(i));
		}
	}

	public void push(boolean item) {
		if (size == items.length)
			items = (boolean[]) NvUtils.resizeArray(items, items.length * 2);

		items[size++] = item;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public int size() {
		return size;
	}

	public void clear() {
		size = 0;
		if (items.length >= 16) {
			items = new boolean[4];
		}
	}

	public boolean pop() {
		if (isEmpty())
			throw new NullPointerException("stack is empty!");

		return items[--size];
	}

	public boolean[] getData() {
		return items;
	}

	public float[] toArray() {
		float[] tmp = new float[size];
		System.arraycopy(items, 0, tmp, 0, size);
		return tmp;
	}

	public boolean get(int index) {
		return items[index];
	}

	public boolean peer() {
		if (isEmpty())
			throw new NullPointerException("stack is empty!");

		return items[size - 1];
	}

	public void reserve(int cap) {
		if (cap > items.length) {
			items = (boolean[]) NvUtils.resizeArray(items, cap);
		}
	}

	public void resize(int size, boolean value) {
		if (size < 0)
			throw new IllegalArgumentException("size < 0");
		if (size >= items.length)
			items = new boolean[size];
		Arrays.fill(items, value);

		this.size = size;
	}

	public void resize(int size) {
		if (size < 0)
			throw new IllegalArgumentException("size < 0");

		if (size >= items.length)
			items = (boolean[]) NvUtils.resizeArray(items, size);

		this.size = size;
	}
}
