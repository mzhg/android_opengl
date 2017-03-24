//----------------------------------------------------------------------------------
// File:        NvShapes.java
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

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL11;

public final class NvShapes {

	static final FloatBuffer fullScreenQuadDataBuffer;
	static final FloatBuffer fullScreenQuadDataTexBuffer;
	static final FloatBuffer cubePositionBuffer;
	static final ByteBuffer cubeIndicesBuffer;
	static final FloatBuffer wireCubePositionBuffer;
	static final ByteBuffer wireCubeIndicesBuffer;
	
	static{
		final float fullScreenQuadData[] = 
			 {
			    -1.0f, -1.0f, -1.0f, 1.0f,  0.0f, 0.0f,
			     1.0f, -1.0f, -1.0f, 1.0f,  1.0f, 0.0f,
			    -1.0f,  1.0f, -1.0f, 1.0f, 0.0f, 1.0f,
			    1.0f,  1.0f, -1.0f, 1.0f, 1.0f, 1.0f
			 };
		fullScreenQuadDataBuffer = BufferUtils.createFloatBuffer(fullScreenQuadData.length);
		fullScreenQuadDataBuffer.put(fullScreenQuadData).flip();
		fullScreenQuadDataTexBuffer = fullScreenQuadDataBuffer.duplicate();
		fullScreenQuadDataTexBuffer.position(4);
		
		 float positions[] = {
		        -1.0f, -1.0f, 1.0f,
		        1.0f, -1.0f, 1.0f,
		        -1.0f, 1.0f, 1.0f,
		        1.0f, 1.0f, 1.0f,
		        -1.0f, -1.0f, -1.0f,
		        1.0f, -1.0f, -1.0f,
		        -1.0f, 1.0f, -1.0f,
		        1.0f, 1.0f, -1.0f,
		    };
		    
		    byte indices[] = {
		        0, 1, 2, 2, 1, 3,   // f
		        4, 6, 5, 5, 6, 7,   // b
		        4, 0, 6, 6, 0, 2,   // l
		        1, 5, 3, 3, 5, 7,   // r
		        2, 3, 6, 6, 3, 7,   // t
		        4, 5, 0, 0, 5, 1,   // b
		    };
		    
		    cubePositionBuffer = BufferUtils.createFloatBuffer(positions.length);
		    cubePositionBuffer.put(positions).flip();
		    
		    cubeIndicesBuffer = BufferUtils.createByteBuffer(indices.length);
		    cubeIndicesBuffer.put(indices).flip();
		    
		     positions = new float[]{
		            -1.0f, -1.0f, 1.0f,
		            1.0f, -1.0f, 1.0f,
		            1.0f, 1.0f, 1.0f,
		            -1.0f, 1.0f, 1.0f,
		            -1.0f, -1.0f, -1.0f,
		            1.0f, -1.0f, -1.0f,
		            1.0f, 1.0f, -1.0f,
		            -1.0f, 1.0f, -1.0f,
		        };
		        
		         indices = new byte[]{
		            0, 1, 1, 2, 2, 3, 3, 0,    // f
		            4, 5, 5, 6, 6, 7, 7, 4, // b
		            0, 4, 1, 5, 2, 6, 3, 7
		        };
		     
		   wireCubePositionBuffer = BufferUtils.createFloatBuffer(positions.length);
		   wireCubePositionBuffer.put(positions).flip();
		   
		   wireCubeIndicesBuffer = BufferUtils.createByteBuffer(indices.length);
		   wireCubeIndicesBuffer.put(indices).flip();
	}
	
	public static void drawQuad(int posIndex){
		FloatBuffer position = GLUtil.getCachedFloatBuffer(16);
		position.put(-1.0f).put(-1.0f);
		position.put(1.0f).put(-1.0f);
		position.put(-1.0f).put(1.0f);
		position.put(1.0f).put(1.0f);
		position.flip();
		
		GLES.glVertexAttribPointer(posIndex, 2, false, 2* 4, position);
		GLES20.glEnableVertexAttribArray(posIndex);

		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

		GLES20.glDisableVertexAttribArray(posIndex);
	}
	
	public static void drawQuad(int positionAttrib, int texcoordAttrib){
		GLES.glVertexAttribPointer(positionAttrib, 4,  false, 6 * 4, fullScreenQuadDataBuffer);
		GLES20.glEnableVertexAttribArray(positionAttrib);

		if(texcoordAttrib > 0){
			GLES.glVertexAttribPointer(texcoordAttrib, 2,  false, 6 * 4, fullScreenQuadDataTexBuffer);
			GLES20.glEnableVertexAttribArray(texcoordAttrib);
		}
		GLES20.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);

		GLES20.glDisableVertexAttribArray(positionAttrib);
		if(texcoordAttrib > 0) {
			GLES20.glDisableVertexAttribArray(texcoordAttrib);
		}
	}
	
	public static void drawCube(int posIndex){
		GLES.glVertexAttribPointer(posIndex, 3, false, 3 * 4, cubePositionBuffer);
		GLES20.glEnableVertexAttribArray(posIndex);

		GLES.glDrawElements(GL11.GL_TRIANGLES, cubeIndicesBuffer);

	    GLES20.glDisableVertexAttribArray(posIndex);
	}
	
	public static void drawWireCube(int posIndex){
		GLES.glVertexAttribPointer(posIndex, 3, false, 3 * 4, wireCubePositionBuffer);
		GLES20.glEnableVertexAttribArray(posIndex);

		GLES.glDrawElements(GL11.GL_LINES, wireCubeIndicesBuffer);

		GLES20.glDisableVertexAttribArray(posIndex);
	}
}
