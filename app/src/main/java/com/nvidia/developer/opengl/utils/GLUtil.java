////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2014, NVIDIA CORPORATION. All rights reserved.
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

import android.util.Log;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.ReadableVector3f;
import org.lwjgl.util.vector.ReadableVector4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public final class GLUtil {

	private static final int INIT_CAPACITY = 1024 * 1024;
	private static ByteBuffer nativeBuffer;
	
	private static IntBuffer intBuffer;
	private static ShortBuffer shortBuffer;
	private static FloatBuffer floatBuffer;
	private static DoubleBuffer doubleBuffer;

	private static Thread glThread;
	
	static{
		/* default to allocate 1MB memory. */
		remolloc(INIT_CAPACITY);

		Log.i("GLUtil", "Static inilized!!!");
	}

	private GLUtil(){
		throw new Error();
	}

	public static void markThread(Thread thread) {glThread = thread;}
	
	private static void remolloc(int size){
		if(glThread != null && Thread.currentThread() != glThread){
			throw new IllegalStateException("Can't use GLUtil on the non-gl-thread.");
		}

		if(nativeBuffer == null || nativeBuffer.capacity() < size){
			nativeBuffer = BufferUtils.createByteBuffer(size);
			
			intBuffer = nativeBuffer.asIntBuffer();
			shortBuffer = nativeBuffer.asShortBuffer();
			floatBuffer = nativeBuffer.asFloatBuffer();
			doubleBuffer = nativeBuffer.asDoubleBuffer();
		}
	}
	
	public static ByteBuffer getCachedByteBuffer(int size){
		if(glThread != null && Thread.currentThread() != glThread){
			throw new IllegalStateException("Can't use GLUtil on the non-gl-thread.");
		}
		if(size < 0)
			throw new IllegalArgumentException("size < 0, size = " + size);
		if(size > nativeBuffer.capacity())
			remolloc(size);
		
		nativeBuffer.position(0).limit(size);
		return nativeBuffer;
	}
	
	public static FloatBuffer getCachedFloatBuffer(int size){
		if(glThread != null && Thread.currentThread() != glThread){
			throw new IllegalStateException("Can't use GLUtil on the non-gl-thread.");
		}
		if(size < 0)
			throw new IllegalArgumentException("size < 0, size = " + size);
		if(size > floatBuffer.capacity())
			remolloc(size * 4);
		
		floatBuffer.position(0).limit(size);
		return floatBuffer;
	}
	
	public static IntBuffer getCachedIntBuffer(int size){
		if(glThread != null && Thread.currentThread() != glThread){
			throw new IllegalStateException("Can't use GLUtil on the non-gl-thread.");
		}
		if(size < 0)
			throw new IllegalArgumentException("size < 0, size = " + size);
		if(size > intBuffer.capacity())
			remolloc(size << 2);
		
		intBuffer.position(0).limit(size);
		return intBuffer;
	}
	
	public static ShortBuffer getCachedShortBuffer(int size){
		if(glThread != null && Thread.currentThread() != glThread){
			throw new IllegalStateException("Can't use GLUtil on the non-gl-thread.");
		}
		if(size < 0)
			throw new IllegalArgumentException("size < 0, size = " + size);
		if(size > shortBuffer.capacity())
			remolloc(size << 1);
		
		shortBuffer.position(0).limit(size);
		return shortBuffer;
	}
	
	public static DoubleBuffer getCachedDoubleBuffer(int size){
		if(glThread != null && Thread.currentThread() != glThread){
			throw new IllegalStateException("Can't use GLUtil on the non-gl-thread.");
		}
		if(size < 0)
			throw new IllegalArgumentException("size < 0, size = " + size);
		if(size > doubleBuffer.capacity())
			remolloc(size << 3);
		
		doubleBuffer.position(0).limit(size);
		return doubleBuffer;
	}
	
	public static IntBuffer wrap(int i){
		if(glThread != null && Thread.currentThread() != glThread){
			throw new IllegalStateException("Can't use GLUtil on the non-gl-thread.");
		}
		intBuffer.clear();
		intBuffer.put(i).flip();
		return intBuffer;
	}

	public static FloatBuffer wrap(float i){
		if(glThread != null && Thread.currentThread() != glThread){
			throw new IllegalStateException("Can't use GLUtil on the non-gl-thread.");
		}
		floatBuffer.clear();
		floatBuffer.put(i).flip();
		return floatBuffer;
	}
	
	public static FloatBuffer wrap(float[] data, int offset, int length){
		FloatBuffer buffer = getCachedFloatBuffer(length);
		buffer.put(data, offset, length).flip();
		return buffer;
	}

	public static FloatBuffer wrap(float x, float y, float z, float w){
		FloatBuffer buffer = getCachedFloatBuffer(4);
		buffer.put(x).put(y).put(z).put(w);
		buffer.flip();
		return buffer;
	}

	public static IntBuffer wrap(int x, int y, int z, int w){
		IntBuffer buffer = getCachedIntBuffer(4);
		buffer.put(x).put(y).put(z).put(w);
		buffer.flip();
		return buffer;
	}
	
	public static IntBuffer wrap(int[] data, int offset, int length){
		IntBuffer buffer = getCachedIntBuffer(length);
		buffer.put(data, offset, length).flip();
		return buffer;
	}

	public static FloatBuffer wrap(ReadableVector4f[] data){
		return wrap(data, 0, data.length);
	}
	
	public static FloatBuffer wrap(ReadableVector4f[] data, int offset, int length){
	   FloatBuffer buf = GLUtil.getCachedFloatBuffer(length * 4);
	   for(int i = 0; i < length; i++){
		   data[i + offset].store(buf);
	   }
	   buf.flip();
	   return buf;
	}

	public static FloatBuffer wrap(ReadableVector3f[] data){
		return wrap(data, 0, data.length);
	}

	public static FloatBuffer wrap(ReadableVector3f[] data, int offset, int length){
		FloatBuffer buf = GLUtil.getCachedFloatBuffer(length * 3);
		for(int i = 0; i < length; i++){
			data[i + offset].store(buf);
		}
		buf.flip();
		return buf;
	}
	
	public static FloatBuffer wrap(float[] data){
		FloatBuffer buffer = getCachedFloatBuffer(data.length);
		buffer.put(data, 0, data.length).flip();
		return buffer;
	}
	
	public static ShortBuffer wrap(short[] data, int offset, int length){
		ShortBuffer buffer = getCachedShortBuffer(length);
		buffer.put(data, offset, length).flip();
		return buffer;
	}
	
	public static IntBuffer wrap(int[] data){
		IntBuffer buffer = getCachedIntBuffer(data.length);
		buffer.put(data, 0, data.length).flip();
		return buffer;
	}
	
	public static FloatBuffer wrap(float[][] data){
		int totalSize = 0;
		for(int i = 0; i < data.length;i++)
			totalSize += data[i].length;
		
		FloatBuffer buffer = getCachedFloatBuffer(totalSize);
		for(int i = 0; i < data.length;i++)
		  buffer.put(data[i], 0, data[i].length);
		buffer.flip();
		return buffer;
	}
	
	public static ShortBuffer wrap(short[] data){
		ShortBuffer buffer = getCachedShortBuffer(data.length);
		buffer.put(data, 0, data.length).flip();
		return buffer;
	}
	
	public static ByteBuffer wrap(byte[] data, int offset, int length){
		ByteBuffer buffer = getCachedByteBuffer(length);
		buffer.put(data, offset, length).flip();
		return buffer;
	}
	
	public static ByteBuffer wrap(byte[] data){
		ByteBuffer buffer = getCachedByteBuffer(data.length);
		buffer.put(data, 0, data.length).flip();
		return buffer;
	}
	
	public static FloatBuffer wrap(Matrix4f mat){
		FloatBuffer buffer = getCachedFloatBuffer(16);
	    mat.store(buffer);
	    buffer.flip();
	    return buffer;
	}
}
