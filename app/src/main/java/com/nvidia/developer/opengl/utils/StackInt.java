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

import java.util.Arrays;

public class StackInt {

	private int[] items;
	private transient int size;

	public StackInt() {
		this(4);
	}

	public StackInt(int capacity) {
		capacity = capacity < 1 ? 1 : capacity;
		items = new int[capacity];
	}

	public void push(int item) {
		if (size == items.length)
			items = (int[]) NvUtils.resizeArray(items, items.length * 2);

		items[size++] = item;
	}

	public void addAll(StackInt ints) {
		for (int i = 0; i < ints.size; i++) {
			push(ints.get(i));
		}
	}

	public void copyFrom(StackInt ints) {
		size = 0;
		addAll(ints);
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public int pop() {
		if (isEmpty())
			throw new NullPointerException("stack is empty!");

		return items[--size];
	}

	public int size() {
		return size;
	}

	public int[] getData() {
		return items;
	}

	public void set(int index, int value) {
		if (index >= size)
			throw new IndexOutOfBoundsException();

		items[index] = value;
	}

	public void clear() {
		size = 0;
		if (items.length >= 256) {
			items = new int[4];
		}
	}

	public int[] toArray() {
		int[] tmp = new int[size];
		System.arraycopy(items, 0, tmp, 0, size);
		return tmp;
	}

	public int get(int index) {
		return items[index];
	}

	public void resize(int newSize) {
		if (newSize < 0)
			throw new IllegalArgumentException("newSize < 0. newSize = "
					+ newSize);
		if (newSize <= items.length)
			size = newSize;
		else {
			items = (int[]) NvUtils.resizeArray(items, newSize);
			size = newSize;
		}
	}
	
	public void fill(int offset, int len, int value){
		if(len < 0)
			throw new IllegalArgumentException("len < 0. len = " + len);
		if(offset + len - 1 >= size)
			throw new IndexOutOfBoundsException();
		
		for(int i = offset; i < offset + len;i++){
			items[i] = value;
		}
	}

	public int peer() {
		if (isEmpty())
			throw new NullPointerException("stack is empty!");

		return items[size - 1];
	}

	public StackInt copy() {
		StackInt sf = new StackInt(size);
		System.arraycopy(items, 0, sf.items, 0, size);
		return sf;
	}

	public static void main(String[] args) {
		String[] strs = "3//2//11/6/78/89".split("//");
		System.out.println(Arrays.toString(strs));
	}
}
