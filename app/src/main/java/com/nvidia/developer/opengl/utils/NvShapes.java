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

import android.opengl.GLES20;
import android.opengl.GLES30;

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
		GLES30.glBindVertexArray(0);
		GLES30.glBindBuffer(GLES20.GL_ARRAY_BUFFER,0);
		GLES.glVertexAttribPointer(positionAttrib, 4,  false, 6 * 4, fullScreenQuadDataBuffer);
		GLES20.glEnableVertexAttribArray(positionAttrib);

		if(texcoordAttrib > 0){
			GLES.glVertexAttribPointer(texcoordAttrib, 2,  false, 6 * 4, fullScreenQuadDataTexBuffer);
			GLES20.glEnableVertexAttribArray(texcoordAttrib);
		}

		GLES.checkGLError();
		GLES20.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
		GLES.checkGLError();

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
