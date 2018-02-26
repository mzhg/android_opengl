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

import java.util.Arrays;

/**
 * Revisited Radix Sort.Contains source code from the article "Radix Sort Revisited".<p>
 * This is my new radix routine:<ul>
 * <li>it uses indices and doesn't recopy the values anymore, hence wasting less ram
 * <li>it creates all the histograms in one run instead of four
 * <li>it sorts words faster than dwords and bytes faster than words
 * <li>it correctly sorts negative floating-point values by patching the offsets
 * <li>it automatically takes advantage of temporal coherence
 * <li>multiple keys support is a side effect of temporal coherence
 * <li>it may be worth recoding in asm... (mainly to use FCOMI, FCMOV, etc) [it's probably memory-bound anyway]
 * </ul>
 * History:<ul>
 * <li>08.15.98: very first version
 * <li>04.04.00: recoded for the radix article
 * <li>12.xx.00: code lifting
 * <li>09.18.01: faster CHECK_PASS_VALIDITY thanks to Mark D. Shattuck (who provided other tips, not included here)
 * <li>10.11.01: added local ram support
 * <li>01.20.02: bugfix! In very particular cases the last pass was skipped in the float code-path, leading to incorrect sorting......
 * </ul>
 * @author Pierre Terdiman April, 4, 2000<br> Nvidia 2014-9-24 12:14
 */
public class RadixSort {

	int[]       mHistogram;                 //!< Counters for each byte
	int[]       mOffset;                    //!< Offsets (nearly a cumulative distribution function)
	int			mCurrentSize;				//!< Current size of the indices list
	int			mPreviousSize;				//!< Size involved in previous call
	int[]	    mIndices;					//!< Two lists, swapped each pass
	int[]		mIndices2;
// Stats
	int			mTotalCalls;
	int			mNbHits;
	
	public RadixSort() {
		
		resetIndices();
	}
	
	/**
	 * Resizes the inner lists.
	 * @param nb new size (number of ints)
	 */
	public void resize(int nb){
		if(mIndices == null || mIndices.length < nb){
			mIndices     = new int[nb];
			mIndices2    = new int[nb];
			mCurrentSize = nb;
		}
		
		// Initialize indices so that the input buffer is read in sequential order
		resetIndices();
	}
	
	/** Access to results. mIndices is a list of indices in sorted order, i.e. in the order you may further process your data */
	public int[] getIndices(){ return mIndices;		}

	/** mIndices2 gets trashed on calling the sort routine, but otherwise you can recycle it the way you want.*/
	public int[] getRecyclable(){ return mIndices2;		}

	/** Resets the inner indices. After the call, mIndices is reset. */
	private void resetIndices(){
		for(int i=0;i<mCurrentSize;i++)	mIndices[i] = i;
	}
	
	/** Returns the total number of calls to the radix sorter.*/
	public int	getNbTotalCalls(){ return mTotalCalls;	}
	/** Returns the number of premature exits due to temporal coherence.*/
	public int	getNbHits()	{ return mNbHits;		}
	
	/**
	 * Gets the ram used.
	 * @return memory used in bytes
	 */
	public int getUsedRam(){
		int size = 16;
		size += mIndices.length * 4;
		size += mIndices2.length * 4;
		
		size += mHistogram == null ? 0 : mHistogram.length * 4;
		size += mOffset == null ? 0 : mOffset.length * 4;
		
		return size;
	}
	
	/**
	 * This one is for integer values. After the call, mIndices contains a list of indices in sorted order, i.e. in the order you may process your data.
	 * @param input a list of integer values to sort
	 * @param nb number of values to sort
	 * @return Self-Reference
	 */
	public RadixSort sort(int[] input, int nb){
		if(input == null || nb == 0)
			return this;
		
		// Stats
		mTotalCalls++;
		
		// Resize lists if needed.
		checkResize(nb);
		
		if(mHistogram == null) mHistogram = new int[256 * 4];
		if(mOffset == null)    mOffset    = new int[256];
		
		// Create histograms (counters). Counters for all passes are created in one run.
		// Pros:	read input buffer once instead of four times
		// Cons:	mHistogram is 4Kb instead of 1Kb
		// We must take care of signed/unsigned values for temporal coherence.... I just
		// have 2 code paths even if just a single opcode changes. Self-modifying code, someone?
		if(createHistograms(input, nb))
			return this;
		
		// Compute #negative values involved if needed
		int NbNegativeValues = 0;
		// An efficient way to compute the number of negatives values we'll have to deal with is simply to sum the 128
		// last values of the last histogram. Last histogram because that's the one for the Most Significant Byte,
		// responsible for the sign. 128 last values because the 128 first ones are related to positive numbers.
//		udword* h3= &mHistogram[768];
		for(int i=128;i<256;i++)	NbNegativeValues += mHistogram[768 + i];	// 768 for last histogram, 128 for negative part
		
		// Radix sort, j is the pass number (0=LSB, 3=MSB)
		for(int j=0;j<4;j++)
		{
//			CHECK_PASS_VALIDITY(j);
			int curCount = j << 8;
			boolean PerformPass = checkPassValidity(j, input, nb);

			// Sometimes the fourth (negative) pass is skipped because all numbers are negative and the MSB is 0xFF (for example). This is
			// not a problem, numbers are correctly sorted anyway.
			if(PerformPass)
			{
				// Should we care about negative values?
				if(j!=3 /*|| !signedvalues*/)
				{
					// Here we deal with positive values only

					// Create offsets
					mOffset[0] = 0;
					for(int i=1;i<256;i++)		mOffset[i] = mOffset[i-1] + mHistogram[curCount + i - 1] /*CurCount[i-1]*/;
				}
				else
				{
					// This is a special case to correctly handle negative integers. They're sorted in the right order but at the wrong place.

					// Create biased offsets, in order for negative numbers to be sorted as well
					mOffset[0] = NbNegativeValues;												// First positive number takes place after the negative ones
					for(int i=1;i<128;i++)		mOffset[i] = mOffset[i-1] + mHistogram[curCount + i - 1] /*CurCount[i-1]*/;	// 1 to 128 for positive numbers

					// Fixing the wrong place for negative values
					mOffset[128] = 0;
					for(int i=129;i<256;i++)	mOffset[i] = mOffset[i-1] + mHistogram[curCount + i - 1] /*CurCount[i-1]*/;
				}

				// Perform Radix Sort
//				ubyte* InputBytes	= (ubyte*)input;
//				udword* Indices		= mIndices;
//				udword* IndicesEnd	= &mIndices[nb];
//				InputBytes += j;
				
				int InputBytes = j;
				int Indices = 0;
				int IndicesEnd = nb;
				while(Indices!=IndicesEnd)
				{
//					udword id = *Indices++;
					int id = mIndices[Indices++];
//					mIndices2[mOffset[InputBytes[id<<2]]++] = id;
					mIndices2[mOffset[NvUtils.getByte(input, InputBytes + (id << 2))]++] = id;
				}

				// Swap pointers for next pass. Valid indices - the most recent ones - are in mIndices after the swap.
				int[] Tmp	= mIndices;	mIndices = mIndices2; mIndices2 = Tmp;
			}
		}
		
		return this;
	}
	
	/**
	 * This one is for floating-point values. After the call, mIndices contains a list of indices in sorted order, i.e. in the order you may process your data.
	 * @param input2 a list of floating-point values to sort
	 * @param nb number of values to sort
	 * @return Self-Reference
	 */
	public RadixSort sort(float[] input2, int nb){
		// Checkings
		if(input2 == null || nb == 0)	return this;

		// Stats
		mTotalCalls++;
		
		// Resize lists if needed
		checkResize(nb);
		
		if(mHistogram == null) mHistogram = new int[256 * 4];
		if(mOffset == null)    mOffset    = new int[256];
		
		// Create histograms (counters). Counters for all passes are created in one run.
		// Pros:	read input buffer once instead of four times
		// Cons:	mHistogram is 4Kb instead of 1Kb
		// Floating-point values are always supposed to be signed values, so there's only one code path there.
		// Please note the floating point comparison needed for temporal coherence! Although the resulting asm code
		// is dreadful, this is surprisingly not such a performance hit - well, I suppose that's a big one on first
		// generation Pentiums....We can't make comparison on integer representations because, as Chris said, it just
		// wouldn't work with mixed positive/negative values....
		if(createHistograms(input2, nb)){
			return this;
		}
		
		// Compute #negative values involved if needed
		int NbNegativeValues = 0;
		// An efficient way to compute the number of negatives values we'll have to deal with is simply to sum the 128
		// last values of the last histogram. Last histogram because that's the one for the Most Significant Byte,
		// responsible for the sign. 128 last values because the 128 first ones are related to positive numbers.
//		udword* h3= &mHistogram[768];
		for(int i=128;i<256;i++)	NbNegativeValues += mHistogram[768 + i];	// 768 for last histogram, 128 for negative part
		
		// Radix sort, j is the pass number (0=LSB, 3=MSB)
		for(int j=0;j<4;j++)
		{
			// Should we care about negative values?
			if(j != 3){
	//		    CHECK_PASS_VALIDITY(j);
				int curCount = j << 8;
				boolean PerformPass = checkPassValidity(j, input2, nb);
				
				if(PerformPass)
				{
					// Create offsets
					mOffset[0] = 0;
					for(int i=1;i<256;i++)		mOffset[i] = mOffset[i-1] + mHistogram[curCount + i - 1]; //CurCount[i-1];

					// Perform Radix Sort
//					ubyte* InputBytes	= (ubyte*)input;
//					udword* Indices		= mIndices;
//					udword* IndicesEnd	= &mIndices[nb];
//					InputBytes += j;
				    int InputBytes = j;
					int Indices = 0;
					int IndicesEnd = nb;
					while(Indices!=IndicesEnd)
					{
//						udword id = *Indices++;
						int id = mIndices[Indices ++];
//						mIndices2[mOffset[InputBytes[id<<2]]++] = id;
						mIndices2[mOffset[NvUtils.getByte(input2, InputBytes + (id << 2))]++] = id;
					}

					// Swap pointers for next pass. Valid indices - the most recent ones - are in mIndices after the swap.
					int[] Tmp	= mIndices;	mIndices = mIndices2; mIndices2 = Tmp;
				}
			}else{
				/* Shortcut to current counters */
				int curCount = j << 8;
				
				/* Reset flag. The sorting pass is supposed to be performed. (default) */
				boolean PerformPass = true;
				
				/* Check pass validity */									
				
				/* If all values have the same byte, sorting is useless. */
				/* It may happen when sorting bytes or words instead of dwords. */
				/* This routine actually sorts words faster than dwords, and bytes */
				/* faster than words. Standard running time (O(4*n))is reduced to O(2*n) */
				/* for words and O(n) for bytes. Running time for floats depends on actual values... */
				
				/* Get first byte */
				int UniqueVal = NvUtils.getByte(input2, j);
				
				/* Check that byte's counter */
//				if(CurCount[UniqueVal]==nb)	PerformPass=false;
				if(mHistogram[curCount + UniqueVal] == nb) PerformPass = false;
				
				if(PerformPass)
				{
					// Create biased offsets, in order for negative numbers to be sorted as well
					mOffset[0] = NbNegativeValues;												// First positive number takes place after the negative ones
					for(int i=1;i<128;i++)		mOffset[i] = mOffset[i-1] + mHistogram[curCount + i - 1]; //CurCount[i-1];	// 1 to 128 for positive numbers

					// We must reverse the sorting order for negative numbers!
					mOffset[255] = 0;
					for(int i=0;i<127;i++)		mOffset[254-i] = mOffset[255-i] + mHistogram[curCount + 255 - i]; //CurCount[255 - i];	// Fixing the wrong order for negative values
					for(int i=128;i<256;i++)	mOffset[i] += mHistogram[curCount + i];//CurCount[i];							// Fixing the wrong place for negative values

					// Perform Radix Sort
					for(int i=0;i<nb;i++)
					{
						int Radix = (Float.floatToIntBits(input2[mIndices[i]])>>24) & 0xFF;			// Radix byte, same as above. AND is useless here (udword).
						// ### cmp to be killed. Not good. Later.
						if(Radix<128)		mIndices2[mOffset[Radix]++] = mIndices[i];		// Number is positive, same as above
						else				mIndices2[--mOffset[Radix]] = mIndices[i];		// Number is negative, flip the sorting order
					}
					// Swap pointers for next pass. Valid indices - the most recent ones - are in mIndices after the swap.
					int[] Tmp	= mIndices;	mIndices = mIndices2; mIndices2 = Tmp;
				}
				else
				{
					// The pass is useless, yet we still have to reverse the order of current list if all values are negative.
					if(UniqueVal>=128)
					{
						for(int i=0;i<nb;i++)	mIndices2[i] = mIndices[nb-i-1];

						// Swap pointers for next pass. Valid indices - the most recent ones - are in mIndices after the swap.
						int[] Tmp	= mIndices;	mIndices = mIndices2; mIndices2 = Tmp;
					}
				}
			}
		}
		return this;
	}
	
	private final void checkResize(int n){
		if(n!=mPreviousSize)
		{
			if(n>mCurrentSize)	resize(n);
			else				resetIndices();
			mPreviousSize = n;
		}
	}
	
	private final boolean createHistograms(int[] buffer, int nb){
		/* Clear counters */
		Arrays.fill(mHistogram, 0);
		
		/* Prepare for temporal coherence */
		int PrevVal = buffer[mIndices[0]];
		boolean AlreadySorted = true;	/* Optimism... */
		int Indices = 0;
		
		/* Prepare to count */
		int p = 0;
		int pe = nb * 4;
		int h0 = 0;        /* Histogram for first pass (LSB)	*/
		int h1 = 256;      /* Histogram for second pass		    */
		int h2 = 512;      /* Histogram for third pass			*/  
		int h3 = 768;      /* Histogram for last pass (MSB)	    */
		while(p!=pe)
		{
			/* Read input buffer in previous sorted order */
			int Val = buffer[mIndices[Indices ++]];
			/* Check whether already sorted or not */
			if(Val < PrevVal) { AlreadySorted = false; break; } /* Early out */
			/* Update for next iteration */	
			PrevVal = Val;
			
			/* Create histograms */
			mHistogram[h0 + NvUtils.getByte(buffer, p++)]++;
			mHistogram[h1 + NvUtils.getByte(buffer, p++)]++;
			mHistogram[h2 + NvUtils.getByte(buffer, p++)]++;
			mHistogram[h3 + NvUtils.getByte(buffer, p++)]++;
		}
		
		/* If all input values are already sorted, we just have to return and leave the */
		/* previous list unchanged. That way the routine may take advantage of temporal */
		/* coherence, for example when used to sort transparent faces.					*/
		if(AlreadySorted)	{ mNbHits++; return true;}
		
		/* Else there has been an early out and we must finish computing the histograms */
		while(p!=pe)
		{
			/* Create histograms without the previous overhead */
			mHistogram[h0 + NvUtils.getByte(buffer, p++)]++;
			mHistogram[h1 + NvUtils.getByte(buffer, p++)]++;
			mHistogram[h2 + NvUtils.getByte(buffer, p++)]++;
			mHistogram[h3 + NvUtils.getByte(buffer, p++)]++;
		}
		
		return false;
	}
	
	private final boolean createHistograms(float[] buffer, int nb){
		/* Clear counters */
		Arrays.fill(mHistogram, 0);
		
		/* Prepare for temporal coherence */
		float PrevVal = buffer[mIndices[0]];
		boolean AlreadySorted = true;	/* Optimism... */
		int Indices = 0;
		
		/* Prepare to count */
		int p = 0;
		int pe = nb * 4;
		int h0 = 0;        /* Histogram for first pass (LSB)	*/
		int h1 = 256;      /* Histogram for second pass		    */
		int h2 = 512;      /* Histogram for third pass			*/  
		int h3 = 768;      /* Histogram for last pass (MSB)	    */
		while(p!=pe)
		{
			/* Read input buffer in previous sorted order */
			float Val = buffer[mIndices[Indices ++]];
			/* Check whether already sorted or not */
			if(Val < PrevVal) { AlreadySorted = false; break; } /* Early out */
			/* Update for next iteration */	
			PrevVal = Val;
			
			/* Create histograms */
			mHistogram[h0 + NvUtils.getByte(buffer, p++)]++;
			mHistogram[h1 + NvUtils.getByte(buffer, p++)]++;
			mHistogram[h2 + NvUtils.getByte(buffer, p++)]++;
			mHistogram[h3 + NvUtils.getByte(buffer, p++)]++;
		}
		
		/* If all input values are already sorted, we just have to return and leave the */
		/* previous list unchanged. That way the routine may take advantage of temporal */
		/* coherence, for example when used to sort transparent faces.					*/
		if(AlreadySorted)	{ mNbHits++; return true;}
		
		/* Else there has been an early out and we must finish computing the histograms */
		while(p!=pe)
		{
			/* Create histograms without the previous overhead */
			mHistogram[h0 + NvUtils.getByte(buffer, p++)]++;
			mHistogram[h1 + NvUtils.getByte(buffer, p++)]++;
			mHistogram[h2 + NvUtils.getByte(buffer, p++)]++;
			mHistogram[h3 + NvUtils.getByte(buffer, p++)]++;
		}
		
		return false;
	}
	
	private final boolean checkPassValidity(int pass, int[] input, int nb){
		/* Shortcut to current counters */
		int curCount = pass << 8;
		
		/* Reset flag. The sorting pass is supposed to be performed. (default) */
		boolean PerformPass = true;
		
		/* Check pass validity */									
		
		/* If all values have the same byte, sorting is useless. */
		/* It may happen when sorting bytes or words instead of dwords. */
		/* This routine actually sorts words faster than dwords, and bytes */
		/* faster than words. Standard running time (O(4*n))is reduced to O(2*n) */
		/* for words and O(n) for bytes. Running time for floats depends on actual values... */
		
		/* Get first byte */
		int UniqueVal = NvUtils.getByte(input, pass);
		
		/* Check that byte's counter */
//		if(CurCount[UniqueVal]==nb)	PerformPass=false;
		if(mHistogram[curCount + UniqueVal] == nb) PerformPass = false;
		
		return PerformPass;
	}
	
	private final boolean checkPassValidity(int pass, float[] input, int nb){
		/* Shortcut to current counters */
		int curCount = pass << 8;
		
		/* Reset flag. The sorting pass is supposed to be performed. (default) */
		boolean PerformPass = true;
		
		/* Check pass validity */									
		
		/* If all values have the same byte, sorting is useless. */
		/* It may happen when sorting bytes or words instead of dwords. */
		/* This routine actually sorts words faster than dwords, and bytes */
		/* faster than words. Standard running time (O(4*n))is reduced to O(2*n) */
		/* for words and O(n) for bytes. Running time for floats depends on actual values... */
		
		/* Get first byte */
		int UniqueVal = NvUtils.getByte(input, pass);
		
		/* Check that byte's counter */
//		if(CurCount[UniqueVal]==nb)	PerformPass=false;
		if(mHistogram[curCount + UniqueVal] == nb) PerformPass = false;
		
		return PerformPass;
	}
	
	public static void main(String[] args) {
		RadixSort sort = new RadixSort();
		
		float[] data = {4, 27, 61, 98, 63, 55, 95, 38, 47, 41};
		for(int i = 0; i < 10; i++)
			data[i] = (float) (Math.random() * 100);
		
		sort.sort(data, 10);
		
		int[] sortedIndices32 = sort.getIndices();
		System.out.println(Arrays.toString(data));
		for(int i = 0; i < 10; i++){
			System.out.println(data[sortedIndices32[i]]);
		}
	}
}
