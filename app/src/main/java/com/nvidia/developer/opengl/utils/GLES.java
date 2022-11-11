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

import android.opengl.GLES10;
import android.opengl.GLES11;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLES32;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL11;

import jet.learning.opengl.common.Texture2D;
import jet.learning.opengl.common.Texture3D;

public class GLES {
	
	public static boolean useES2 = true;

	static final int[] integer = new int[1];
	static final boolean[] bool = new boolean[1];
	public static int glGenTextures(){
		if(useES2)
			GLES20.glGenTextures(1, integer, 0);
		else
			GLES10.glGenTextures(1, integer, 0);

		return integer[0];
	}

	public static int glGenSamplers(){
		GLES30.glGenSamplers(1, integer, 0);
		return integer[0];
	}

	public static int glGenQueries(){
		GLES30.glGenQueries(1, integer, 0);
		return integer[0];
	}

	public static int glGenTransformFeedbacks(){
		GLES30.glGenTransformFeedbacks(1, integer, 0);
		return integer[0];
	}

	public static int glGenRenderbuffers(){
		GLES20.glGenRenderbuffers(1, integer, 0);
		return integer[0];
	}

	public static int glGenVertexArray(){
		GLES30.glGenVertexArrays(1, integer, 0);
		return integer[0];
	}

	public static void glDeleteRenderbuffer(int renderbuffer){
		integer[0] = renderbuffer;
		GLES30.glDeleteRenderbuffers(1, integer, 0);
	}
	
	public static void glBindTexture(int target, int textureID){
		if(useES2)
			GLES20.glBindTexture(target, textureID);
		else
			GLES10.glBindTexture(target, textureID);
	}
	
	public static void glCompressedTexImage2D(int target, int level, int internalformat, int width, int height, int border, ByteBuffer data){
		if(useES2)
		    GLES20.glCompressedTexImage2D(target, level, internalformat, width, height, border, data.remaining(), data);
		else
			GLES10.glCompressedTexImage2D(target, level, internalformat, width, height, border, data.remaining(), data);
	}
	
	public static void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, ByteBuffer pixels){
		if(useES2)
			GLES20.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
		else
			GLES10.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
	}
	
	/**
	 * Throws OpenGLException if glGetError() returns anything else than GL_NO_ERROR
	 *
	 */
	public static void checkGLError() {
		int err;
		if(useES2){
			err = GLES20.glGetError();
		}else{
			err = GLES10.glGetError();
		}
		
		if ( err != GL11.GL_NO_ERROR ) {
			NvLogger.e("GL err:%s.\n", translateGLErrorString(err));
			throw new RuntimeException("GL err: " + translateGLErrorString(err));
		}
	}

	/**
	 * Throws OpenGLException if glGetError() returns anything else than GL_NO_ERROR
	 *
	 */
	public static void checkGLError(String msg) {
		int err;
		if(useES2){
			err = GLES20.glGetError();
		}else{
			err = GLES10.glGetError();
		}

		if ( err != GL11.GL_NO_ERROR ) {
			NvLogger.e("GL err occured at %s: %s.\n", msg, translateGLErrorString(err));
			throw new RuntimeException("GL err: " + translateGLErrorString(err));
		}
	}

	/**
	 * Translate a GL error code to a String describing the error
	 */
	public static String translateGLErrorString(int error_code) {
		switch (error_code) {
			case GL11.GL_NO_ERROR:
				return "No error";
			case GL11.GL_INVALID_ENUM:
				return "Invalid enum";
			case GL11.GL_INVALID_VALUE:
				return "Invalid value";
			case GL11.GL_INVALID_OPERATION:
				return "Invalid operation";
			case GL11.GL_STACK_OVERFLOW:
				return "Stack overflow";
			case GL11.GL_STACK_UNDERFLOW:
				return "Stack underflow";
			case GL11.GL_OUT_OF_MEMORY:
				return "Out of memory";
			case GLES20.GL_INVALID_FRAMEBUFFER_OPERATION:
				return "Invalid framebuffer operation";
			default:
				return null;
		}
	}
	
	public static int glGenFramebuffers(){
		if(useES2)
			GLES20.glGenFramebuffers(1, integer, 0);
		else
			GLES11Ext.glGenFramebuffersOES(1, integer, 0);
		
		return integer[0];
	}

	public static void glDrawbuffer(int drawbuffer){
		integer[0] = drawbuffer;
		GLES30.glDrawBuffers(1, integer, 0);
	}

	public static int glGetQueryObjectuiv(int query, int pname){
		GLES30.glGetQueryObjectuiv(query, pname, integer, 0);
		return integer[0];
	}
	
	public static int glGetInteger(int pname){
		if(useES2)
			GLES20.glGetIntegerv(pname, integer, 0);
		else
			GLES10.glGetIntegerv(pname, integer, 0);
		
		return integer[0];
	}

	public static int glGetInteger(int index, int pname){
		GLES30.glGetIntegeri_v(index, pname, integer,0);
		return integer[0];
	}
	
	public static void glBindFramebuffer(int framebuffer, int bufferID){
		if(useES2)
			GLES20.glBindFramebuffer(framebuffer, bufferID);
		else
			GLES11Ext.glBindFramebufferOES(framebuffer, bufferID);
	}
	
	public static void glFramebufferTexture2D(int target, int attachment, int textarget,
			int texture, int level){
		if(useES2)
			GLES20.glFramebufferTexture2D(target, attachment, textarget, texture, level);
		else
			GLES11Ext.glFramebufferTexture2DOES(target, attachment, textarget, texture, level);
	}
	
	public static int glCheckFramebufferStatus(int target){
		if(useES2)
			return GLES20.glCheckFramebufferStatus(target);
		else
			return GLES11Ext.glCheckFramebufferStatusOES(target);
		
	}
	
	/** You must bind a framebuffer object before call this function. */
	public static void checkFrameBufferStatus(){
		int status = GLES.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        switch (status)
        {
            case GLES20.GL_FRAMEBUFFER_COMPLETE:
                break;
            case GLES20.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT:
                NvLogger.e("Can't create FBO: GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT error");
                break;
            case GLES20.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:
            	NvLogger.e("Can't create FBO: GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT error");
                break;
            case GLES20.GL_FRAMEBUFFER_UNSUPPORTED:
            	NvLogger.e("Can't create FBO: GL_FRAMEBUFFER_UNSUPPORTED error");
                break;
            default:
            	NvLogger.e("Can't create FBO: unknown error");
                break;
        }
	}
	
	public static void glDeleteTextures(int textureID){
		integer[0] = textureID;
		if(useES2){
			GLES20.glDeleteTextures(1, integer, 0);
		}else{
			GLES10.glDeleteTextures(1, integer, 0);
		}
	}

	public static void glDeleteTransformFeedback(int transformFeedback){
		integer[0] = transformFeedback;
		GLES30.glDeleteTransformFeedbacks(1, integer,0);
	}

	public static void glDeleteQueries(int query){
		integer[0] = query;
		GLES30.glDeleteQueries(1, integer,0);
	}
	
	public static void glDeleteFramebuffers(int framebufferID){
		integer[0] = framebufferID;
		if(useES2){
			GLES20.glDeleteFramebuffers(1, integer, 0);
		}else{
			GLES11Ext.glDeleteFramebuffersOES(1, integer, 0);
		}
	}
	
	public static void glTexParameteri(int target, int pname, int value){
		if(useES2)
			GLES20.glTexParameteri(target, pname, value);
		else
			GLES10.glTexParameterx(target, pname, value);
	}
	
	public static void glTexParameterf(int target, int pname, float value){
		if(useES2)
			GLES20.glTexParameterf(target, pname, value);
		else
			GLES10.glTexParameterf(target, pname, value);
	}

	public static void glViewport(int x, int y, int width, int height) {
		if(useES2)
			GLES20.glViewport(x, y, width, height);
		else
			GLES10.glViewport(x, y, width, height);
	}
	
	public static int glGetShaderi(int shader, int pname){
		GLES20.glGetShaderiv(shader, pname, integer, 0);
		return integer[0];
	}
	
	public static int glGetProgrami(int program, int pname){
		GLES20.glGetProgramiv(program, pname, integer, 0);
		return integer[0];
	}
	
	public static void glActiveTexture(int texunit){
		if(useES2)
			GLES20.glActiveTexture(texunit);
		else
			GLES10.glActiveTexture(texunit);
	}

	public static void glDisable(int cab) {
		if(useES2)
			GLES20.glDisable(cab);
		else
			GLES10.glDisable(cab);
	}

	public static void glBindBuffer(int target, int buffer) {
		if(useES2)
			GLES20.glBindBuffer(target, buffer);
		else
			GLES11.glBindBuffer(target, buffer);
	}

	public static void glDeleteBuffers(int buffer) {
		integer[0] = buffer;
		if(useES2)
			GLES20.glDeleteBuffers(1, integer, 0);
		else
			GLES11.glDeleteBuffers(1, integer, 0);
	}

	public static int glGenBuffers() {
		if(useES2)
			GLES20.glGenBuffers(1, integer, 0);
		else
			GLES11.glGenBuffers(1, integer, 0);
		return integer[0];
	}

	public static void glBufferData(int target, FloatBuffer data, int type) {
		if(useES2)
			GLES20.glBufferData(target, data.remaining() << 2, data, type);
		else
			GLES11.glBufferData(target, data.remaining() << 2, data, type);
	}
	
	public static void glBufferData(int target, IntBuffer data, int type) {
		if(useES2)
			GLES20.glBufferData(target, data.remaining() << 2, data, type);
		else
			GLES11.glBufferData(target, data.remaining() << 2, data, type);
	}

	public static void glBufferData(int target, ShortBuffer data, int type) {
		if(useES2)
			GLES20.glBufferData(target, data.remaining() << 1, data, type);
		else
			GLES11.glBufferData(target, data.remaining() << 1, data, type);
	}
	
	public static void glBufferData(int target, ByteBuffer data, int type) {
		if(useES2)
			GLES20.glBufferData(target, data.remaining(), data, type);
		else
			GLES11.glBufferData(target, data.remaining(), data, type);
	}

	public static void glEnable(int cab) {
		if(useES2)
			GLES20.glEnable(cab);
		else
			GLES10.glEnable(cab);
	}

	public static void glBlendFuncSeparate(int sfactorRGB, int dfactorRGB, int sfactorAlpha, int dfactorAlpha) {
		if(useES2)
			GLES20.glBlendFuncSeparate(sfactorRGB, dfactorRGB, sfactorAlpha, dfactorAlpha);
		else // TODO this requires the "GL_OES_blend_func_separate" extension.
			GLES11Ext.glBlendFuncSeparateOES(sfactorRGB, dfactorRGB, sfactorAlpha, dfactorAlpha);
	}

	public static void glDrawElements(int mode, int count, int type, int offset) {
		if(useES2)
			GLES20.glDrawElements(mode, count, type, offset);
		else
			GLES11.glDrawElements(mode, count, type, offset);
	}

	public static void glDepthMask(boolean flag) {
		if(useES2)
			GLES20.glDepthMask(flag);
		else
			GLES10.glDepthMask(flag);
	}

	public static int glGetVertexAttribi(int index, int pname) {
		GLES20.glGetVertexAttribiv(index, pname, integer, 0);
		return integer[0];
	}

	public static boolean glGetBoolean(int caps) {
		if(useES2)
			GLES20.glGetBooleanv(caps, bool, 0);
		else
			GLES11.glGetBooleanv(caps, bool, 0);
		return bool[0];
	}

	public static boolean glIsEnabled(int caps) {
		return useES2 ? GLES20.glIsEnabled(caps) : GLES11.glIsEnabled(caps);
	}

	public static void glVertexAttribPointer(int index, int size, boolean normalized, int stride, FloatBuffer data) {
		GLES20.glVertexAttribPointer(index, size, GLES20.GL_FLOAT, normalized, stride, data);
	}

	public static void glDrawElements(int mode, ByteBuffer buffer) {
		if(useES2){
		   GLES20.glDrawElements(mode, buffer.remaining(), GLES20.GL_UNSIGNED_BYTE, buffer);
		}else{
		   GLES10.glDrawElements(mode, buffer.remaining(), GLES10.GL_UNSIGNED_BYTE, buffer);
		}
	}

	public static void glDrawBuffer(int buffer) {
		integer[0] = buffer;
		GLES30.glDrawBuffers(1, integer, 0);
	}


	public static int glGenVertexArrays() {
		GLES30.glGenVertexArrays(1, integer, 0);
		return integer[0];
	}

	public static int glGetActiveUniformBlocki(int program, int index, int pname) {
		GLES30.glGetActiveUniformBlockiv(program, index, pname, integer, 0);
		return integer[0];
	}

	public static int glGetActiveUniformsi(int program, int indice, int pname) {
		integer[0] = indice;
		GLES30.glGetActiveUniformsiv(program, 1, integer, 0, pname, integer, 0);  // TODO
		return integer[0];
	}

	public static void glDeleteVertexArrays(int vao) {
		integer[0] = vao;
		GLES30.glDeleteVertexArrays(1, integer, 0);
	}

	public static void glBindTextureUnit(int unit, Texture2D texture){
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + unit);

		int target = texture!=null ? texture.getTarget(): GLES20.GL_TEXTURE_2D;
		int textureID = texture != null ? texture.getTexture() : 0;
		GLES20.glBindTexture(target, textureID);
	}

	/*public static void glBindTextureUnit(int unit, Texture3D texture){
	GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + unit);

		int target = texture!=null ? texture.getTarget(): GLES30.GL_TEXTURE_3D;
		int textureID = texture != null ? texture.getTexture() : 0;
		GLES20.glBindTexture(target, textureID);
	}*/

	public static void drawFullscreenTriangle(){
		GLES30.glBindVertexArray(0);
		GLES30.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
		FloatBuffer dummyVertex = GLUtil.getCachedFloatBuffer(20);  // three vertex
		GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 9, dummyVertex);
		GLES30.glEnableVertexAttribArray(0);
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
		GLES20.glDisableVertexAttribArray(0);

		GLES.checkGLError();
	}
}
