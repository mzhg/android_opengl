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

public class StackShort {

	private short[] items;
	private transient int size;

	public StackShort() {
		this(4);
	}

	public StackShort(int capacity) {
		capacity = capacity < 1 ? 1 : capacity;
		items = new short[capacity];
	}

	public void push(short item) {
		if (size == items.length)
			items = (short[]) NvUtils.resizeArray(items, items.length * 2);

		items[size++] = item;
	}

	public void addAll(StackShort ints) {
		for (int i = 0; i < ints.size; i++) {
			push(ints.get(i));
		}
	}

	public void copyFrom(StackShort ints) {
		size = 0;
		addAll(ints);
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public short pop() {
		if (isEmpty())
			throw new NullPointerException("stack is empty!");

		return items[--size];
	}

	public int size() {
		return size;
	}

	public short[] getData() {
		return items;
	}

	public void set(int index, short value) {
		if (index >= size)
			throw new IndexOutOfBoundsException();

		items[index] = value;
	}

	public void clear() {
		size = 0;
		if (items.length >= 256) {
			items = new short[4];
		}
	}

	public short[] toArray() {
		short[] tmp = new short[size];
		System.arraycopy(items, 0, tmp, 0, size);
		return tmp;
	}

	public short get(int index) {
		return items[index];
	}

	public void resize(int newSize) {
		if (newSize < 0)
			throw new IllegalArgumentException("newSize < 0. newSize = "
					+ newSize);
		if (newSize <= items.length)
			size = newSize;
		else {
			items = (short[]) NvUtils.resizeArray(items, newSize);
			size = newSize;
		}
	}
	
	public void fill(int offset, int len, short value){
		if(len < 0)
			throw new IllegalArgumentException("len < 0. len = " + len);
		if(offset + len - 1 >= size)
			throw new IndexOutOfBoundsException();
		
		for(int i = offset; i < offset + len;i++){
			items[i] = value;
		}
	}

	public short peer() {
		if (isEmpty())
			throw new NullPointerException("stack is empty!");

		return items[size - 1];
	}

	public StackShort copy() {
		StackShort sf = new StackShort(size);
		System.arraycopy(items, 0, sf.items, 0, size);
		return sf;
	}
}
