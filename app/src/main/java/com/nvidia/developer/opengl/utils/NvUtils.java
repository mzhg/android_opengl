//----------------------------------------------------------------------------------
// File:        NvUtils.java
// SDK Version: v1.2 
// Email:       gameworks@nvidia.com
// Site:        http://developer.nvidia.com/
//
// Copyright (c) 2014, NVIDIA CORPORATION. All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
//  * Redistributions of source code must retain the above copyright
//    notice, this list of conditions and the following disclaimer.
//  * Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
//  * Neither the name of NVIDIA CORPORATION nor the names of its
//    contributors may be used to endorse or promote products derived
//    from this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS ``AS IS'' AND ANY
// EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
// PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
// EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
// PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
// PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
// OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
//----------------------------------------------------------------------------------
package com.nvidia.developer.opengl.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Formatter;

public final class NvUtils {
	
	public static final float PI = (float) Math.PI;

	public static boolean DEBUG = true;
	public static boolean BTN_SUPPORTS_HOVER = false;
	public static boolean later = false;
	public static boolean SHOW_PRESSED_BUTTON = false;
	
	/** A big bold constant to make pure virtual methods more visible in code. */
	static final int NV_PURE_VIRTUAL = 0;
	
	private static final float[] QUTES = new float[16];
	static{
		float v = 1.0f;
		for(int i = 0; i < 16; i++){
			QUTES[i] = v;
			v *= 10.0f;
		}
	}
	/** Convert 64-bit usecs value to floating-point seconds. */
	static final double ust2secs(long t){
		return ((double)(((double)(t)) / 1.0e9));
	}
	
	/** Convert floating-point seconds to a 64-bit usecs value. */
	static final long secs2ust(float t){
		return ((long)(((double)(t)) * 1.0e9));
	}
	
	public static final int makefourcc(int c0, int c1, int c2, int c3){
		return c0 | (c1 << 8) | (c2 << 16) | (c3 << 24);
	}
	
	public static final int getInt(byte[] data, int position) {
		int a = data[position + 0] & 255;
		int b = data[position + 1] & 255;
		int c = data[position + 2] & 255;
		int d = data[position + 3] & 255;

		return makefourcc(a, b, c, d);
	}
	
	public static final long getLong(byte[] data, int position) {
		long a = data[position + 0] & 255;
		long b = data[position + 1] & 255;
		long c = data[position + 2] & 255;
		long d = data[position + 3] & 255;
		long e = data[position + 4] & 255;
		long f = data[position + 5] & 255;
		long g = data[position + 6] & 255;
		long h = data[position + 7] & 255;

		return a | (b << 8) | (c << 16) | (d << 24) | (e << 32) | (f << 40) | (g << 48) | (h << 56);
	}

	public static final short getShort(byte[] data, int position) {
		int a = data[position + 0] & 255;
		int b = data[position + 1] & 255;

		return (short) ((a << 0) | (b << 8));
	}
	
	public static int swap32(int rgb) {
		int a = (rgb >> 24) & 0xFF;
		int r = (rgb >> 16) & 0xff;
		int g = (rgb >> 8) & 0xff;
		int b = rgb & 0xff;

		return (b << 24) | (g << 16) | (r << 8) | a;
	}

	public static short swap16(short rgb) {
		int a = (rgb >> 8) & 0xFF;
		int r = rgb & 0xff;

		return (short) ((r << 8) | a);
	}
	
	public static void swap(byte[] data, int pos1, int pos2) {
		byte tmp = data[pos1];
		data[pos1] = data[pos2];
		data[pos2] = tmp;
	}

	public static void swap(short[] data, int pos1, int pos2) {
		short tmp = data[pos1];
		data[pos1] = data[pos2];
		data[pos2] = tmp;
	}
	
	public static void swap(int[] array, int i, int j) {
		if (i == j)
			return;

		int old = array[i];
		array[i] = array[j];
		array[j] = old;
	}
	
	public static byte[] toBytes(int x, byte[] out) {
		byte[] intBytes = out == null ? new byte[4] : out; 
		intBytes[3] = (byte) (x >> 24);
		intBytes[2] = (byte) (x >> 16);
		intBytes[1] = (byte) (x >> 8);
		intBytes[0] = (byte) (x >> 0);
		return intBytes;
	}
	
	public static void toBytes(int[] src, int srcOffset, byte[] dst, int dstOffset, int length){
		if(length < 0)
			throw new IllegalArgumentException("length must be >= 0, length = " + length);
		byte[] intBytes = null;
		
		for(int i = srcOffset; i < srcOffset + length; i++){
			intBytes = toBytes(src[i], intBytes);
			
			dst[dstOffset ++] = intBytes[0];
			dst[dstOffset ++] = intBytes[1];
			dst[dstOffset ++] = intBytes[2];
			dst[dstOffset ++] = intBytes[3];
		}
	}
	
	public static final boolean isPowerOfTwo(int x) {
        return x > 0 && (x & (x - 1)) == 0;
    }
	
	public final static int clampPower(int num) {

		int initialCapacity = 1;
		// Find the best power of two to hold elements.
		// Tests "<=" because arrays aren't kept full.
		if (num >= initialCapacity) {
			initialCapacity = num;
			initialCapacity |= (initialCapacity >>> 1);
			initialCapacity |= (initialCapacity >>> 2);
			initialCapacity |= (initialCapacity >>> 4);
			initialCapacity |= (initialCapacity >>> 8);
			initialCapacity |= (initialCapacity >>> 16);
			initialCapacity++;

			if (initialCapacity < 0) // Too many elements, must back off
				initialCapacity >>>= 1;// Good luck allocating 2 ^ 30 elements
		}

		return initialCapacity;
	}

	public static int unsignedByte(byte b) {
		return b & 0xFF;
	}

	public static int unsignedShort(short s) {
		return s & 0xFFFF;
	}

	public static int unsignedChar(char c) {
		return c & 0xFFFF;
	}

	public static long unsignedInt(int i) {
		long l = i;
		l = l & (0xFFFFFFFFL);
		return l;
	}
	
	public static final boolean isEmpty(String str){
		if(str == null || str.length() ==0)
			return true;
		
		for(int i = 0; i < str.length(); i++){
			char c = str.charAt(i);
			if(c != ' ' && c != '\t' && c != '\n')
				return false;
		}
		
		return true;
	}
	
	public static float toPrecisice(float v, int precision){
		if(precision >= 8)
			return v;
		if(precision < 0)
			throw new IllegalArgumentException("precision must be >= 0. precision = " + precision);
		
		double q = QUTES[precision];
		return (float) (Math.floor(v * q + 0.5)/q);
	}
	
	public static String formatPercisice(float v, int precision){
		if(precision < 0)
			throw new IllegalArgumentException("precision must be >= 0. precision = " + precision);
		
		String pattern = "%." + Integer.toString(precision) + 'f';
		return sprintf(pattern, v);
	}
	
	public static String formatPercisice(double v, int precision){
		if(precision < 0)
			throw new IllegalArgumentException("precision must be >= 0. precision = " + precision);
		
		String pattern = "%." + Integer.toString(precision) + 'f';
		return sprintf(pattern, v);
	}
	
	public static double toPrecisice(double v, int precision){
		if(precision > 15)
			return v;
		if(precision < 0)
			throw new IllegalArgumentException("precision must be >= 0. precision = " + precision);
		
		double q = QUTES[precision];
		return Math.floor(v * q + 0.5)/q;
	}
	
	public static int getByte(int[] src, int index){
		int mod = index & 3;
		index = index >> 2;
		
		return (src[index] >> (mod << 3)) & 0xFF;
	}
	
	public static int getByte(float[] src, int index){
		int mod = index & 3;
		index = index >> 2;
		
		int i = Float.floatToIntBits(src[index]);
		
		return (i >> (mod << 3)) & 0xFF;
	}
	
	/**
	 * 
	 * Constrains a value to not exceed a maximum and minimum value.
	 * 
	 * @param amt
	 *            the value to constrain
	 * @param low
	 *            minimum limit
	 * @param high
	 *            maximum limit
	 */

	static public final int clamp(int amt, int low, int high) {
		return (amt < low) ? low : ((amt > high) ? high : amt);
	}
	
	/**
	 * 
	 * Constrains a value to not exceed a maximum and minimum value.
	 * 
	 * @param amt
	 *            the value to constrain
	 * @param low
	 *            minimum limit
	 * @param high
	 *            maximum limit
	 */

	static public final float clamp(float amt, float low, float high) {
		return (amt < low) ? low : ((amt > high) ? high : amt);
	}
	
	private static final StringBuilder a = new StringBuilder();
	private static final Formatter formatter = new Formatter(a);
	/**
	 * Reallocates an array with a new size, and copies the contents of old
	 * array to the new array.
	 * 
	 * @param oldArray
	 *            The old array,to be reallocated.
	 * @param newSize
	 *            The new array size.
	 * @return A new Array with same contents.
	 */
	public static final Object resizeArray(Object oldArray, int newSize) {
		int oldSize = java.lang.reflect.Array.getLength(oldArray);
		Class<?> elementType = oldArray.getClass().getComponentType();
		Object newArray = java.lang.reflect.Array.newInstance(elementType,
				newSize);
		int length = Math.min(oldSize, newSize);
		if (length > 0)
			System.arraycopy(oldArray, 0, newArray, 0, length);
		return newArray;
	}
	
	/**
	 * Calculates a number between two numbers at a specific increment. The
	 * <b>amt</b> parameter is the amount to interpolate between the two values
	 * where 0.0 equal to the first point, 0.1 is very near the first point, 0.5
	 * is half-way in between, etc. The lerp function is convenient for creating
	 * motion along a straight path and for drawing dotted lines.
	 * 
	 * @param start
	 *            first value
	 * @param stop
	 *            second value
	 * @param amt
	 *            float between 0.0 and 1.0
	 */
	public static final float lerp(float start, float stop, float amt) {
		return start + (stop - start) * amt;
	}
	
	public static Method getMethod(Object obj, String name, Class<?>... args) {
		Method method = null;

		for (Class<?> clazz = obj.getClass(); clazz != Object.class; clazz = clazz
				.getSuperclass()) {
			try {
				method = clazz.getDeclaredMethod(name, args);

				if (method != null)
					return method;
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// don't do anything here.
			}
		}

		System.err.printf("No such method named '%s' in the class %s.\n", name, obj
				.getClass().getName());
		return null;
	}

	public static Field getField(Object obj, String filedName) {
		Class<? extends Object> objectType = obj.getClass();

		boolean found = false;

		while (objectType != null) {
			for (Field f : objectType.getDeclaredFields())
				if (f.getName().equals(filedName)) {
					found = true;
					break;
				}

			if (found)
				break;
			else
				objectType = objectType.getSuperclass();
		}

		if (found) {
			try {
				Field field = objectType.getDeclaredField(filedName);

				try {
					field.setAccessible(true);
					return field;
				} catch (java.security.AccessControlException e) {
					e.printStackTrace();
				}
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			}
		} else {
			System.err.println("No such field named '" + filedName
					+ "' in the class " + obj.getClass().getName());
		}
		return null;
	}

	public static String[] generateGetterAndSetterName(String fieldName) {
		char first = fieldName.charAt(0);

		String getter = "get" + Character.toUpperCase(first)
				+ fieldName.substring(1);
		String setter = "set" + Character.toUpperCase(first)
				+ fieldName.substring(1);

		return new String[] { getter, setter };
	}
	
	public static synchronized String sprintf(String format, Object...args){
		a.delete(0, a.length());
		return formatter.format(format, args).toString();
	}

	/**
	 * Return the exponent of a float number, removing the bias.
	 * <p>
	 * For float numbers of the form 2<sup>x</sup>, the unbiased
	 * exponent is exactly x.
	 * </p>
	 * @param f number from which exponent is requested
	 * @return exponent for d in IEEE754 representation, without bias
	 */
	public static int getExponent(final float f) {
		return ((Float.floatToIntBits(f) >>> 23) & 0xff) - 127;
	}

	/**
	 * Multiply a double number by a power of 2.
	 * @param d number to multiply
	 * @param n power of 2
	 * @return d &times; 2<sup>n</sup>
	 */
	public static double ldexp(final double d, final int n) {

		// first simple and fast handling when 2^n can be represented using normal numbers
		if ((n > -1023) && (n < 1024)) {
			return d * Double.longBitsToDouble(((long) (n + 1023)) << 52);
		}

		// handle special cases
		if (Double.isNaN(d) || Double.isInfinite(d) || (d == 0)) {
			return d;
		}
		if (n < -2098) {
			return (d > 0) ? 0.0 : -0.0;
		}
		if (n > 2097) {
			return (d > 0) ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
		}

		// decompose d
		final long bits = Double.doubleToLongBits(d);
		final long sign = bits & 0x8000000000000000L;
		int  exponent   = ((int) (bits >>> 52)) & 0x7ff;
		long mantissa   = bits & 0x000fffffffffffffL;

		// compute scaled exponent
		int scaledExponent = exponent + n;

		if (n < 0) {
			// we are really in the case n <= -1023
			if (scaledExponent > 0) {
				// both the input and the result are normal numbers, we only adjust the exponent
				return Double.longBitsToDouble(sign | (((long) scaledExponent) << 52) | mantissa);
			} else if (scaledExponent > -53) {
				// the input is a normal number and the result is a subnormal number

				// recover the hidden mantissa bit
				mantissa = mantissa | (1L << 52);

				// scales down complete mantissa, hence losing least significant bits
				final long mostSignificantLostBit = mantissa & (1L << (-scaledExponent));
				mantissa = mantissa >>> (1 - scaledExponent);
				if (mostSignificantLostBit != 0) {
					// we need to add 1 bit to round up the result
					mantissa++;
				}
				return Double.longBitsToDouble(sign | mantissa);

			} else {
				// no need to compute the mantissa, the number scales down to 0
				return (sign == 0L) ? 0.0 : -0.0;
			}
		} else {
			// we are really in the case n >= 1024
			if (exponent == 0) {

				// the input number is subnormal, normalize it
				while ((mantissa >>> 52) != 1) {
					mantissa = mantissa << 1;
					--scaledExponent;
				}
				++scaledExponent;
				mantissa = mantissa & 0x000fffffffffffffL;

				if (scaledExponent < 2047) {
					return Double.longBitsToDouble(sign | (((long) scaledExponent) << 52) | mantissa);
				} else {
					return (sign == 0L) ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
				}

			} else if (scaledExponent < 2047) {
				return Double.longBitsToDouble(sign | (((long) scaledExponent) << 52) | mantissa);
			} else {
				return (sign == 0L) ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
			}
		}

	}

	/**
	 * Multiply a float number by a power of 2.
	 * @param f number to multiply
	 * @param n power of 2
	 * @return f &times; 2<sup>n</sup>
	 */
	public static float ldexp(final float f, final int n) {

		// first simple and fast handling when 2^n can be represented using normal numbers
		if ((n > -127) && (n < 128)) {
			return f * Float.intBitsToFloat((n + 127) << 23);
		}

		// handle special cases
		if (Float.isNaN(f) || Float.isInfinite(f) || (f == 0f)) {
			return f;
		}
		if (n < -277) {
			return (f > 0) ? 0.0f : -0.0f;
		}
		if (n > 276) {
			return (f > 0) ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
		}

		// decompose f
		final int bits = Float.floatToIntBits(f);
		final int sign = bits & 0x80000000;
		int  exponent  = (bits >>> 23) & 0xff;
		int mantissa   = bits & 0x007fffff;

		// compute scaled exponent
		int scaledExponent = exponent + n;

		if (n < 0) {
			// we are really in the case n <= -127
			if (scaledExponent > 0) {
				// both the input and the result are normal numbers, we only adjust the exponent
				return Float.intBitsToFloat(sign | (scaledExponent << 23) | mantissa);
			} else if (scaledExponent > -24) {
				// the input is a normal number and the result is a subnormal number

				// recover the hidden mantissa bit
				mantissa = mantissa | (1 << 23);

				// scales down complete mantissa, hence losing least significant bits
				final int mostSignificantLostBit = mantissa & (1 << (-scaledExponent));
				mantissa = mantissa >>> (1 - scaledExponent);
				if (mostSignificantLostBit != 0) {
					// we need to add 1 bit to round up the result
					mantissa++;
				}
				return Float.intBitsToFloat(sign | mantissa);

			} else {
				// no need to compute the mantissa, the number scales down to 0
				return (sign == 0) ? 0.0f : -0.0f;
			}
		} else {
			// we are really in the case n >= 128
			if (exponent == 0) {

				// the input number is subnormal, normalize it
				while ((mantissa >>> 23) != 1) {
					mantissa = mantissa << 1;
					--scaledExponent;
				}
				++scaledExponent;
				mantissa = mantissa & 0x007fffff;

				if (scaledExponent < 255) {
					return Float.intBitsToFloat(sign | (scaledExponent << 23) | mantissa);
				} else {
					return (sign == 0) ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
				}

			} else if (scaledExponent < 255) {
				return Float.intBitsToFloat(sign | (scaledExponent << 23) | mantissa);
			} else {
				return (sign == 0) ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
			}
		}

	}
}
