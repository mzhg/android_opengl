package com.nvidia.developer.opengl.utils;

/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.opengl.GLES10;
import android.opengl.GLES20;

/** An ImmediateModeRenderer allows you to perform immediate mode rendering as you were accustomed to in your desktop OpenGL
 * environment. In order to draw something you first have to call {@link ImmediateModeRenderer10#begin(int)} with the primitive
 * type you want to render. Next you specify as many vertices as you want by first defining the vertex color, normal and texture
 * coordinates followed by the vertex position which finalizes the definition of a single vertex. When you are done specifying the
 * geometry you have to call {@link ImmediateModeRenderer10#end()} to make the renderer render the geometry. Internally the
 * renderer uses vertex arrays to render the provided geometry. This is not the best performing way to do this so use this class
 * only for non performance critical low vertex count geometries while debugging.
 * 
 * Note that this class of course only works with OpenGL ES 1.x.
 * 
 * @author mzechner */
public class ImmediateRenderer {
	
	/* GL profile */
	public static final int GLES1 = 0;
	public static final int GLES2 = 1;
	
	public static final int NORMAL = 1 << 0;
	public static final int COLOR = 1 << 1;
	public static final int INDICE = 1 << 2;
	public static final int TEXTURE = 1 << 3;
	public static final int TEXTURE0 = 1 << 3;
	public static final int TEXTURE1 = 1 << 4;
	public static final int TEXTURE2 = 1 << 5;
	public static final int TEXTURE3 = 1 << 6;
	
	public static int MAX_TEXTURE_UNITS = 1;
	
	private static GL10 gl10;
	
	public static void initGL(Object gl){
		if(gl != null && (gl instanceof GL10)){
			gl10 = (GL10)gl;
		}
		
		MAX_TEXTURE_UNITS = GLES.glGetInteger(GLES20.GL_MAX_TEXTURE_IMAGE_UNITS);
	}
	
	/** the primitive type **/
	private int primitiveType;
	/** The component of the vertices */
	private int components;

	/** the vertex position array and buffer **/
	private FloatBuffer positionsBuffer;

	/** the vertex color array and buffer **/
	private FloatBuffer colorsBuffer;

	/** the vertex normal array and buffer **/
	private FloatBuffer normalsBuffer;

	/** the texture coordinate array and buffer **/
	private FloatBuffer[] texCoordsBuffer = new FloatBuffer[MAX_TEXTURE_UNITS];
	
	/** the indices  */
	private ShortBuffer indicesBuffer;

	private final int maxVertices;
	private int numVertices;
	
	public int positionLoc = -1;
	public int colorLoc = -1;
	public int normalLoc = -1;
	
	public int positionSize = 3;
	public int colorSize = 4;
	
	public final int texcoordsLoc[];
	public final int texcoordsSize[];
	
	private int profile = GLES2;

	/** Constructs a new ImmediateModeRenderer */
	public ImmediateRenderer () {
		this(0, 128);
	}
	
	public ImmediateRenderer (int profile) {
		this(profile, 128);
	}

	/** Constructs a new ImmediateModeRenderer */
	public ImmediateRenderer (int profile, int maxVertices) {
		this.profile = profile;
		
		initGL(null);
		
		if(maxVertices < 1)
			throw new IllegalArgumentException("maxVertices < 1, maxVertices = " + maxVertices);
		this.maxVertices = maxVertices;
		
		texcoordsLoc = new int[MAX_TEXTURE_UNITS];
		Arrays.fill(texcoordsLoc, -1);
		
		texcoordsSize = new int[MAX_TEXTURE_UNITS];
		Arrays.fill(texcoordsSize, 2);
	}
	
	private static FloatBuffer createFloatBuffer(int size){
		ByteBuffer buffer = ByteBuffer.allocateDirect(size << 2).order(ByteOrder.nativeOrder());
		
		return buffer.asFloatBuffer();
	}
	
	private static ShortBuffer createShortBuffer(int size){
		ByteBuffer buffer = ByteBuffer.allocateDirect(size << 1).order(ByteOrder.nativeOrder());
		
		return buffer.asShortBuffer();
	}

	/** Starts a new list of primitives. The primitiveType specifies which primitives to draw. Can be any of GL10.GL_TRIANGLES,
	 * GL10.GL_LINES and so on. A maximum of 6000 vertices can be drawn at once.
	 * 
	 * @param primitiveType the primitive type. */
	public void begin (int primitiveType, int...comps) {
		this.primitiveType = primitiveType;
		components = 0;
		
		for(int i = 0; comps != null && i < comps.length; i++)
			components |= comps[i];
		
		
		numVertices = 0;
		
		prepareBuffers();
	}

	private void prepareBuffers() {
		boolean hasColor = hasColor();
		boolean hasNormal = hasNormal();
		boolean hasIndice = hasIndices();
		
		boolean hasTex0 = hasTexCoord0();
		boolean hasTex1 = hasTexCoord1();
		boolean hasTex2 = hasTexCoord2();
		boolean hasTex3 = hasTexCoord3();
		
		if(positionsBuffer == null)
		   positionsBuffer = createFloatBuffer(3 * maxVertices);
		positionsBuffer.clear();
		
		if(hasColor && colorsBuffer == null)
		   colorsBuffer = createFloatBuffer(4 * maxVertices);
		if(hasColor) colorsBuffer.clear();
		
	    if(hasNormal && normalsBuffer == null)normalsBuffer = createFloatBuffer(3 * maxVertices);
	    if(hasNormal) normalsBuffer.clear();
	    
	    if(hasIndice && indicesBuffer == null) indicesBuffer = createShortBuffer(3 * maxVertices);
	    if(hasIndice) indicesBuffer.clear();
	    
	    if(hasTex0){
	    	if(texCoordsBuffer[0] == null) texCoordsBuffer[0] = createFloatBuffer(4 * maxVertices);
	    	texCoordsBuffer[0].clear();
	    }
	    
	    if(hasTex1){
	    	if(MAX_TEXTURE_UNITS <= 1){
	    		System.err.println("The MAX_TEXTURE_UNITS is " + MAX_TEXTURE_UNITS);
	    		return;
	    	}
	    	if(texCoordsBuffer[1] == null) texCoordsBuffer[1] = createFloatBuffer(4 * maxVertices);
	    	texCoordsBuffer[1].clear();
	    }
	    
	    if(hasTex2){
	    	if(MAX_TEXTURE_UNITS <= 2){
	    		System.err.println("The MAX_TEXTURE_UNITS is " + MAX_TEXTURE_UNITS);
	    		return;
	    	}
	    	if(texCoordsBuffer[2] == null) texCoordsBuffer[2] = createFloatBuffer(4 * maxVertices);
	    	texCoordsBuffer[2].clear();
	    }
	    
	    if(hasTex3){
	    	if(MAX_TEXTURE_UNITS <= 3){
	    		System.err.println("The MAX_TEXTURE_UNITS is " + MAX_TEXTURE_UNITS);
	    		return;
	    	}
	    	if(texCoordsBuffer[3] == null) texCoordsBuffer[3] = createFloatBuffer(4 * maxVertices);
	    	texCoordsBuffer[3].clear();
	    }
	}
	
	private void flipBuffers(){
		boolean hasColor = hasColor();
		boolean hasNormal = hasNormal();
		boolean hasIndice = hasIndices();
		
		boolean hasTex0 = hasTexCoord0();
		boolean hasTex1 = hasTexCoord1() && MAX_TEXTURE_UNITS > 1;
		boolean hasTex2 = hasTexCoord2() && MAX_TEXTURE_UNITS > 2;
		boolean hasTex3 = hasTexCoord3() && MAX_TEXTURE_UNITS > 3;
		
		positionsBuffer.flip();
		if(hasNormal) normalsBuffer.flip();
		if(hasColor) colorsBuffer.flip();
		if(hasIndice) indicesBuffer.flip();
		if(hasTex0) texCoordsBuffer[0].flip();
		if(hasTex1) texCoordsBuffer[1].flip();
		if(hasTex2) texCoordsBuffer[2].flip();
		if(hasTex3) texCoordsBuffer[3].flip();
	}

	/** Specifies the color of the current vertex
	 * @param r the red component
	 * @param g the green component
	 * @param b the blue component
	 * @param a the alpha component */
	public void color (float r, float g, float b, float a) {
		colorsBuffer.put(r).put(g).put(b);
		
		if(colorSize == 4)
			colorsBuffer.put(a);
	}
	
	/** Specifies the color of the current vertex
	 * @param r the red component
	 * @param g the green component
	 * @param b the blue component
	 */
	public void color (float r, float g, float b) {
		colorsBuffer.put(r).put(g).put(b);
		
		if(colorSize == 4)
			colorsBuffer.put(1.0f);
	}

	/** Specifies the normal of the current vertex
	 * @param x the x component
	 * @param y the y component
	 * @param z the z component */
	public void normal (float x, float y, float z) {
		normalsBuffer.put(x).put(y).put(z);
	}

	/** Specifies the texture coordinates of the current vertex
	 * @param u the u coordinate
	 * @param v the v coordinate */
	public void texCoord (float u, float v) {
		texCoordsBuffer[0].put(u).put(v);
		if(texcoordsSize[0] >= 3) texCoordsBuffer[0].put(0.0f);
                 if(texcoordsSize[0] == 4) texCoordsBuffer[0].put(1.0f);
	}
	
	/** Specifies the texture coordinates of the current vertex
	 * @param u the u coordinate
	 * @param v the v coordinate */
	public void texCoord (int uint, float u, float v) {
		texCoordsBuffer[uint].put(u).put(v);
		if(texcoordsSize[uint] >= 3) texCoordsBuffer[uint].put(0.0f);
                 if(texcoordsSize[uint] == 4) texCoordsBuffer[uint].put(1.0f);
	}
	
	/** Specifies the texture coordinates of the current vertex
	 * @param u the u coordinate
	 * @param v the v coordinate */
	public void texCoord (float u, float v, float s) {
		texCoordsBuffer[0].put(u).put(v);
		if(texcoordsSize[0] >= 3) texCoordsBuffer[0].put(s);
		if(texcoordsSize[0] == 4) texCoordsBuffer[0].put(1.0f);
	}
	
	/** Specifies the texture coordinates of the current vertex
	 * @param u the u coordinate
	 * @param v the v coordinate */
	public void texCoord (float u, float v, float s, float t) {
		texCoordsBuffer[0].put(u).put(v);
		if(texcoordsSize[0] >= 3) texCoordsBuffer[0].put(s);
		if(texcoordsSize[0] == 4) texCoordsBuffer[0].put(t);
	}
	
	/** Specifies the texture coordinates of the current vertex
	 * @param u the u coordinate
	 * @param v the v coordinate */
	public void texCoord (int uint, float u, float v, float s) {
		texCoordsBuffer[uint].put(u).put(v);
		if(texcoordsSize[uint] >= 3) texCoordsBuffer[uint].put(s);
		if(texcoordsSize[uint] == 4) texCoordsBuffer[uint].put(1.0f);
	}
	
	/** Specifies the texture coordinates of the current vertex
	 * @param u the u coordinate
	 * @param v the v coordinate */
	public void texCoord (int uint, float u, float v, float s, float t) {
		texCoordsBuffer[uint].put(u).put(v);
		if(texcoordsSize[uint] >= 3) texCoordsBuffer[uint].put(s);
		if(texcoordsSize[uint] == 4) texCoordsBuffer[uint].put(t);
	}

	/** Specifies the position of the current vertex and finalizes it. After a call to this method you will effectively define a new
	 * vertex afterwards.
	 * 
	 * @param x the x component
	 * @param y the y component
	 * @param z the z component */
	public void vertex (float x, float y, float z) {
		positionsBuffer.put(x).put(y);
		if(positionSize == 3)
			positionsBuffer.put(z);
		numVertices++;
	}
	
	/** Specifies the position of the current vertex and finalizes it. After a call to this method you will effectively define a new
	 * vertex afterwards.
	 * 
	 * @param x the x component
	 * @param y the y component
	 * @param z the z component */
	public void vertex (float x, float y) {
		positionsBuffer.put(x).put(y);
		if(positionSize == 3)
			positionsBuffer.put(1.0f);
		numVertices++;
	}
	
	public void indice(short i){
		indicesBuffer.put(i);
	}

	public int getNumVertices () {
		return numVertices;
	}

	public int getMaxVertices () {
		return maxVertices;
	}

	/** Renders the primitives just defined. */
	public void end () {
		boolean hasColor = hasColor();
		boolean hasNormal = hasNormal();
		boolean hasIndice = hasIndices();
		
		boolean hasTex0 = hasTexCoord0();
		boolean hasTex1 = hasTexCoord1() && MAX_TEXTURE_UNITS > 1;
		boolean hasTex2 = hasTexCoord2() && MAX_TEXTURE_UNITS > 2;
		boolean hasTex3 = hasTexCoord3() && MAX_TEXTURE_UNITS > 3;
		
		if (numVertices == 0) return;
		
		flipBuffers();
		
		if((profile & GLES2) == 0){
			GLES10.glEnableClientState(GL11.GL_VERTEX_ARRAY);
			GLES10.glVertexPointer(positionSize, GL11.GL_FLOAT, 0, positionsBuffer);
	
			if (hasColor) {
				GLES10.glEnableClientState(GL11.GL_COLOR_ARRAY);
				GLES10.glColorPointer(colorSize, GL11.GL_FLOAT, 0, colorsBuffer);
			}
	
			if (hasNormal) {
				GLES10.glEnableClientState(GL11.GL_NORMAL_ARRAY);
				GLES10.glNormalPointer(GL11.GL_FLOAT, 0, normalsBuffer);
			}
	
			if (hasTex0) {
				int stride = texcoordsSize[0] == 1 ? 8/* 2 * sizeof(float) */ : 0;
				GLES10.glClientActiveTexture(GLES10.GL_TEXTURE0);
				GLES10.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
				GLES10.glTexCoordPointer(texcoordsSize[0], GL11.GL_FLOAT,stride, texCoordsBuffer[0]);
			}
			
			if (hasTex1) {
				int stride = texcoordsSize[1] == 1 ? 8/* 2 * sizeof(float) */ : 0;
				GLES10.glClientActiveTexture(GLES10.GL_TEXTURE1);
				GLES10.glEnableClientState(GLES10.GL_TEXTURE_COORD_ARRAY);
				GLES10.glTexCoordPointer(texcoordsSize[1], GL11.GL_FLOAT,stride, texCoordsBuffer[1]);
			}
			
			if (hasTex2) {
				int stride = texcoordsSize[2] == 1 ? 8/* 2 * sizeof(float) */ : 0;
				GLES10.glClientActiveTexture(GLES10.GL_TEXTURE2);
				GLES10.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
				GLES10.glTexCoordPointer(texcoordsSize[2], GL11.GL_FLOAT,stride, texCoordsBuffer[2]);
			}
			
			if (hasTex3) {
				int stride = texcoordsSize[3] == 1 ? 8/* 2 * sizeof(float) */ : 0;
				GLES10.glClientActiveTexture(GLES10.GL_TEXTURE3);
				GLES10.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
				GLES10.glTexCoordPointer(texcoordsSize[3], GL11.GL_FLOAT,stride, texCoordsBuffer[3]);
			}
	
			if(!hasIndice)
				GLES10.glDrawArrays(primitiveType, 0, numVertices);
			else
				GLES10.glDrawElements(primitiveType, indicesBuffer.remaining(), GLES10.GL_UNSIGNED_SHORT, indicesBuffer);
	
			if (hasColor) GLES10.glDisableClientState(GLES10.GL_COLOR_ARRAY);
			if (hasNormal) GLES10.glDisableClientState(GLES10.GL_NORMAL_ARRAY);
			
			if (hasTex3) {
				GLES10.glClientActiveTexture(GLES10.GL_TEXTURE3);
				GLES10.glDisableClientState(GLES10.GL_TEXTURE_COORD_ARRAY);
			}
			
			if (hasTex2) {
				GLES10.glClientActiveTexture(GLES10.GL_TEXTURE2);
				GLES10.glDisableClientState(GLES10.GL_TEXTURE_COORD_ARRAY);
			}
			
			if (hasTex1) {
				GLES10.glClientActiveTexture(GLES10.GL_TEXTURE1);
				GLES10.glDisableClientState(GLES10.GL_TEXTURE_COORD_ARRAY);
			}
			
			if (hasTex0) {
				GLES10.glClientActiveTexture(GLES10.GL_TEXTURE0);
				GLES10.glDisableClientState(GLES10.GL_TEXTURE_COORD_ARRAY);
			}
		}else{
			GLES20.glEnableVertexAttribArray(positionLoc);
			GLES20.glVertexAttribPointer(positionLoc, positionSize, GL11.GL_FLOAT, false, 0, positionsBuffer);
			
			if(hasColor){
				GLES20.glEnableVertexAttribArray(colorLoc);
				GLES20.glVertexAttribPointer(colorLoc, colorSize, GL11.GL_FLOAT, false, 0, colorsBuffer);
			}
			
			if(hasNormal){
				GLES20.glEnableVertexAttribArray(normalLoc);
				GLES20.glVertexAttribPointer(normalLoc, 3, GL11.GL_FLOAT, false, 0, normalsBuffer);
			}
			
			if(hasTex0){
				int stride = texcoordsSize[0] == 1 ? 8/* 2 * sizeof(float) */ : 0;
				GLES20.glEnableVertexAttribArray(texcoordsLoc[0]);
				GLES20.glVertexAttribPointer(texcoordsLoc[0], texcoordsSize[0], GL11.GL_FLOAT, false, stride, texCoordsBuffer[0]);
			}
			
			if(hasTex1){
				int stride = texcoordsSize[1] == 1 ? 8/* 2 * sizeof(float) */ : 0;
				GLES20.glEnableVertexAttribArray(texcoordsLoc[1]);
				GLES20.glVertexAttribPointer(texcoordsLoc[1], texcoordsSize[1], GL11.GL_FLOAT, false, stride, texCoordsBuffer[1]);
			}
			
			if(hasTex2){
				int stride = texcoordsSize[2] == 1 ? 8/* 2 * sizeof(float) */ : 0;
				GLES20.glEnableVertexAttribArray(texcoordsLoc[2]);
				GLES20.glVertexAttribPointer(texcoordsLoc[2], texcoordsSize[2], GL11.GL_FLOAT, false, stride, texCoordsBuffer[2]);
			}
			
			if(hasTex3){
				int stride = texcoordsSize[3] == 1 ? 8/* 2 * sizeof(float) */ : 0;
				GLES20.glEnableVertexAttribArray(texcoordsLoc[3]);
				GLES20.glVertexAttribPointer(texcoordsLoc[3], texcoordsSize[3], GL11.GL_FLOAT, false, stride, texCoordsBuffer[3]);
			}
			
			if(!hasIndice)
				GLES20.glDrawArrays(primitiveType, 0, numVertices);
			else
				GLES20.glDrawElements(primitiveType, indicesBuffer.remaining(), GL11.GL_UNSIGNED_SHORT, indicesBuffer);
			
			GLES20.glDisableVertexAttribArray(positionLoc);
			if(hasColor) GLES20.glDisableVertexAttribArray(colorLoc);
			if(hasNormal)GLES20.glDisableVertexAttribArray(normalLoc);
			if(hasTex0)GLES20.glDisableVertexAttribArray(texcoordsLoc[0]);
			if(hasTex1)GLES20.glDisableVertexAttribArray(texcoordsLoc[1]);
			if(hasTex2)GLES20.glDisableVertexAttribArray(texcoordsLoc[2]);
			if(hasTex3)GLES20.glDisableVertexAttribArray(texcoordsLoc[3]);
		}
	}
	
	public final boolean hasNormal(){
		return (components & NORMAL) != 0;
	}
	
	public final boolean hasColor(){
		return (components & COLOR) != 0;
	}
	
	public final boolean hasIndices(){
		return (components & INDICE) != 0;
	}
	
	public final boolean hasTexCoord0(){
		return (components & TEXTURE0) != 0;
	}
	
	public final boolean hasTexCoord1(){
		return (components & TEXTURE1) != 0;
	}
	
	public final boolean hasTexCoord2(){
		return (components & TEXTURE2) != 0;
	}
	
	public final boolean hasTexCoord3(){
		return (components & TEXTURE3) != 0;
	}
	
	public final boolean hasTexCoord(){
		return hasTexCoord0() || hasTexCoord1() || hasTexCoord2() || hasTexCoord3();
	}

	public int getProfile() {
		return profile;
	}

	public void setProfile(int profile) {
		this.profile = profile;
	}
	
}

