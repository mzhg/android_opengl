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
import android.opengl.GLException;

import org.lwjgl.util.vector.Matrix4f;

import javax.microedition.khronos.opengles.GL11;

public class NvGLSLProgram implements NvDisposeable{

	protected static boolean ms_logAllMissing;
	
	protected boolean m_strict;
	protected int m_program;
	public interface LinkerTask{
		void invoke(int programID);
	}

	private LinkerTask m_linkerTask;
	
	/**
	 * Creates and returns a shader object from a pair of filenames/paths.<br>
	 * @param vertFilename the filename and partial path to the text file containing the vertex shader source
	 * @param fragFilename the filename and partial path to the text file containing the fragment shader source
	 * @param strict if set to true, then later calls to retrieve the locations of nonexistent uniforms and 
	 * vertex attributes will log a warning to the output
	 * @return a reference to an <code>NvGLSLProgram</code> on success and null on failure
	 */
	public static NvGLSLProgram createFromFiles(String vertFilename, String fragFilename, boolean strict){
		NvGLSLProgram prog = new NvGLSLProgram();
		
		if(prog.setSourceFromFiles(vertFilename, fragFilename, strict))
			return prog;
		else{
			prog.dispose();
			return null;
		}
	}
	
	/**
	 * Creates and returns a shader object from a pair of filenames/paths.<br>
	 * @param vertFilename the filename and partial path to the text file containing the vertex shader source
	 * @param fragFilename the filename and partial path to the text file containing the fragment shader source
	 * @return a reference to an <code>NvGLSLProgram</code> on success and null on failure
	 * @see {@link #createFromStrings(String, String, boolean)}
	 */
	public static NvGLSLProgram createFromFiles(String vertFilename, String fragFilename){
		return createFromFiles(vertFilename, fragFilename, false);
	}
	
	/**
	 * Creates and returns a shader object from a pair of source strings.
	 * @param vertSrc the string containing the vertex shader source
	 * @param fragSrc the string containing the fragment shader source
	 * @param strict if set to true, then later calls to retrieve the 
	 * locations of nonexistent uniforms and vertex attributes will 
	 * log a warning to the output
	 * @return a reference to an <code>NvGLSLProgram</code> on success and null on failure
	 */
	public static NvGLSLProgram createFromStrings(String vertSrc, String fragSrc, boolean strict){
		NvGLSLProgram prog = new NvGLSLProgram();
		
		if(prog.setSourceFromStrings(vertSrc, fragSrc, strict))
			return prog;
		else{
			prog.dispose();
			return null;
		}
	}
	
	/**
	 * Creates and returns a shader object from a pair of source strings.
	 * @param vertSrc the string containing the vertex shader source
	 * @param fragSrc the string containing the fragment shader source
	 * @return a reference to an <code>NvGLSLProgram</code> on success and null on failure
	 */
	public static NvGLSLProgram createFromStrings(CharSequence vertSrc, CharSequence fragSrc){
		NvGLSLProgram prog = new NvGLSLProgram();
		
		if(prog.setSourceFromStrings(vertSrc, fragSrc, false))
			return prog;
		else{
			prog.dispose();
			return null;
		}
	}
	
	/**
	 * Initializes an existing shader object from a pair of filenames/paths<br>
	 * Uses NvAssetLoader.readText() to load the files.
	 * @param vertFilename the filename and partial path to the text file containing the vertex shader source
	 * @param fragFilename the filename and partial path to the text file containing the fragment shader source
	 * @param strict if set to true, then later calls to retrieve the 
	 * locations of nonexistent uniforms and vertex attributes will 
	 * log a warning to the output
	 * @return true on success and false on failure
	 */
	public boolean setSourceFromFiles(String vertFilename, String fragFilename, boolean strict){
		StringBuilder vertSrc = NvAssetLoader.readText(vertFilename);
		StringBuilder fragSrc = NvAssetLoader.readText(fragFilename);
		
		if(vertSrc == null || fragSrc == null){
			return false;
		}
		
		return setSourceFromStrings(vertSrc, fragSrc, strict);
	}
	
	/**
	 * Initializes an existing shader object from a pair of filenames/paths<br>
	 * Uses NvAssetLoader.readText() to load the files.
	 * @param vertFilename the filename and partial path to the text file containing the vertex shader source
	 * @param fragFilename the filename and partial path to the text file containing the fragment shader source
	 * @return true on success and false on failure
	 * @see #setSourceFromFiles(String, String, boolean)
	 */
	public boolean setSourceFromFiles(String vertFilename, String fragFilename){
		return setSourceFromFiles(vertFilename, fragFilename, false);
	}
	
	/**
	 * Creates and returns a shader object from a pair of source strings.
	 * @param vertSrc the string containing the vertex shader source
	 * @param fragSrc the string containing the fragment shader source
	 * @param strict if set to true, then later calls to retrieve the 
	 * locations of nonexistent uniforms and vertex attributes will 
	 * log a warning to the output
	 * @return true on success and false on failure
	 */
	public boolean setSourceFromStrings(CharSequence vertSrc, CharSequence fragSrc, boolean strict){
		if(m_program != 0){
			GLES20.glDeleteProgram(m_program);
			m_program = 0;
		}
		
		 m_strict = strict;

		 m_program = compileProgram(vertSrc, fragSrc);

		 return m_program != 0;
	}
	
	/**
	 * Creates and returns a shader object from an array of #ShaderSourceItem source objects
	 * @param src an array of <code>ShaderSourceItem</code> objects containing the shaders sources to
	 * be loaded.  Unlike the vert/frag-only creation functions, this version can accept additional
	 * shader types such as geometry and tessellation shaders (if supported)
	 * @param count the number of elements in #src array
	 * @param strict if set to true, then later calls to retrieve the 
	 * locations of nonexistent uniforms and vertex attributes will 
	 * log a warning to the output
	 * @return true on success and false on failure
	 */
	@Deprecated
	public boolean setSourceFromStrings(ShaderSourceItem[] src, int count, boolean strict){
		if(m_program != 0){
			GLES20.glDeleteProgram(m_program);
			m_program = 0;
		}
		
		 m_strict = strict;

		 m_program = compileProgram(src, count);

		 return m_program != 0;
	}
	
	/**
	 * Creates and returns a shader object from an array of #ShaderSourceItem source objects
	 * @param src an array of <code>ShaderSourceItem</code> objects containing the shaders sources to
	 * be loaded.  Unlike the vert/frag-only creation functions, this version can accept additional
	 * shader types such as geometry and tessellation shaders (if supported)
	 * @param strict if set to true, then later calls to retrieve the 
	 * locations of nonexistent uniforms and vertex attributes will 
	 * log a warning to the output
	 * @return true on success and false on failure
	 */
	public boolean setSourceFromStrings(ShaderSourceItem[] src, boolean strict){
		if(m_program != 0){
			GLES20.glDeleteProgram(m_program);
			m_program = 0;
		}
		
		 m_strict = strict;

		 m_program = compileProgram(src, src.length);

		 return m_program != 0;
	}
	
	/**
	 * Creates and returns a shader object from an array of #ShaderSourceItem source objects
	 * @param src an array of <code>ShaderSourceItem</code> objects containing the shaders sources to
	 * be loaded.  Unlike the vert/frag-only creation functions, this version can accept additional
	 * shader types such as geometry and tessellation shaders (if supported)
	 * @param count the number of elements in #src array
	 * @return true on success and false on failure
	 */
	@Deprecated
	public boolean setSourceFromStrings(ShaderSourceItem[] src, int count){
		return setSourceFromStrings(src, count, false);
	}
	
	/**
	 * Creates and returns a shader object from an array of #ShaderSourceItem source objects
	 * @param src an array of <code>ShaderSourceItem</code> objects containing the shaders sources to
	 * be loaded.  Unlike the vert/frag-only creation functions, this version can accept additional
	 * shader types such as geometry and tessellation shaders (if supported)
	 * @return true on success and false on failure
	 */
	public boolean setSourceFromStrings(ShaderSourceItem... src){
		return setSourceFromStrings(src, src.length, false);
	}
	
	/** Binds the given shader program as current in the GL context */
	public void enable(){
		GLES20.glUseProgram(m_program);
		
		if(m_program == 0) throw new RuntimeException("program is 0");
		if(!GLES20.glIsProgram(m_program))throw new RuntimeException("program is not a valid program!!!");
		GLES.checkGLError();
	}
	
	/** Unbinds the given shader program from the GL context (binds shader 0) */
	public void disable(){
		GLES20.glUseProgram(0);
	}
	
	private final boolean checkCompileError(int shader, int target){
		int compiled = GLES.glGetShaderi(shader, GLES20.GL_COMPILE_STATUS);
		
		if(compiled == 0 || m_strict){
			if (compiled == 0) {
				String targetSrc = target == GLES20.GL_VERTEX_SHADER ? "Vertex " : "Fragment ";
	            NvLogger.e(targetSrc + "Error compiling shader");
	        }
	        int infoLen = GLES.glGetShaderi(shader, GLES20.GL_INFO_LOG_LENGTH);
	        if (infoLen > 0) {
                String buf = GLES20.glGetShaderInfoLog(shader/*, infoLen*/);
                NvLogger.ef("Shader log:\n%s\n", buf);
	        }
	        if (compiled == 0) {
	            GLES20.glDeleteShader(shader);
	            shader = 0;
	            return false;
	        }
		}
		
		return true;
	}
	
	public static boolean checkLinkError(int program){
		int success = GLES.glGetProgrami(program, GLES20.GL_LINK_STATUS);
	    if(success == 0){
	    	int bufLength = GLES.glGetProgrami(program, GLES20.GL_INFO_LOG_LENGTH);
	    	if(bufLength > 0){
	    		String buf = GLES20.glGetProgramInfoLog(program/*, bufLength*/);
				NvLogger.ef("compileProgram::Could not link program:\n%s\n", buf);
//	    		NvAppBase.throwExp();
				throw new  GLException(0, String.format("compileProgram::Could not link program:\n%s\n", buf));
	    	}
	    	
	    	return false;
	    }
	    
	    return true;
	}
	
	private int compileProgram(CharSequence vsource, CharSequence fsource){
		int vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
	    int fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);

	    GLES20.glShaderSource(vertexShader, vsource.toString());
	    GLES20.glShaderSource(fragmentShader, fsource.toString());

	    GLES20.glCompileShader(vertexShader);
	    if (!checkCompileError(vertexShader, GLES20.GL_VERTEX_SHADER))
	        return 0;

	    GLES20.glCompileShader(fragmentShader);
	    if (!checkCompileError(fragmentShader, GLES20.GL_FRAGMENT_SHADER))
	        return 0;

	    final int program = GLES20.glCreateProgram();
	    
	    GLES20.glAttachShader(program, vertexShader);
	    GLES20.glAttachShader(program, fragmentShader);

		if(m_linkerTask!=null){
			m_linkerTask.invoke(program);
		}

	    GLES20.glLinkProgram(program);
	    
	 // can be deleted since the program will keep a reference
	    GLES20.glDeleteShader(vertexShader);
	    GLES20.glDeleteShader(fragmentShader);
	    
	    if(checkLinkError(program)){
	    	return program;
	    }else{
	    	GLES20.glDeleteProgram(program);
	    	return 0;
	    }
	    
	}
	
	private int compileProgram(ShaderSourceItem[] src, int count){
		int program = GLES20.glCreateProgram();

	    int i;
	    for (i = 0; i < count; i++) {
	        int shader = GLES20.glCreateShader(src[i].type);
	        GLES20.glShaderSource(shader, src[i].src.toString());  // TODO Bad performance
	        GLES20.glCompileShader(shader);
	        if (!checkCompileError(shader, src[i].type))
	            return 0;

	        GLES20.glAttachShader(program, shader);

	        // can be deleted since the program will keep a reference
	        GLES20.glDeleteShader(shader);
	    }

		if(m_linkerTask!=null){
			m_linkerTask.invoke(program);
		}

	    GLES20.glLinkProgram(program);
	    
	    if(checkLinkError(program)){
	    	return program;
	    }else{
	    	GLES20.glDeleteProgram(program);
	    	return 0;
	    }
	}
	
	/** Relinks an existing shader program to update based on external changes */
	public boolean relink(){
		GLES20.glLinkProgram(m_program);
		return checkLinkError(m_program);
	}
	
	/**
	 * Returns the index containing the named vertex attribute
	 * @param attribute the string name of the attribute
	 * @param isOptional if true, the function logs an error if the attribute is not found
	 * @return the non-negative index of the attribute if found.  -1 if not found
	 */
	public int getAttribLocation(String attribute, boolean isOptional){
		int result = GLES20.glGetAttribLocation(m_program, attribute);

	    if (result == -1)
	    {
	        if((ms_logAllMissing || m_strict) && !isOptional) {
	            NvLogger.ef
	            (
	                "could not find attribute \"%s\" in program %d",
	                attribute,
	                m_program
	            );
	        }
	    }

	    return result;
	}
	
	/**
	 * Returns the index containing the named vertex attribute
	 * @param attribute the string name of the attribute
	 * @return the non-negative index of the attribute if found.  -1 if not found
	 */
	public int getAttribLocation(String attribute){
		return getAttribLocation(attribute, false);
	}
	
	/**
	 * Returns the index containing the named uniform
	 * @param uniform the string name of the uniform
	 * @return the non-negative index of the uniform if found.  -1 if not found
	 */
	public int getUniformLocation(String uniform){
		return getUniformLocation(uniform, false);
	}
	
	/**
	 * Returns the index containing the named uniform
	 * @param uniform the string name of the uniform
	 * @param isOptional if true, the function logs an error if the uniform is not found
	 * @return the non-negative index of the uniform if found.  -1 if not found
	 */
	public int getUniformLocation(String uniform, boolean isOptional)
	{
	    int result = GLES20.glGetUniformLocation(m_program, uniform);

	    if (result == -1)
	    {
	        if((ms_logAllMissing || m_strict) && !isOptional) {
	            NvLogger.ef
	            (
	                "could not find uniform \"%s\" in program %d",
	                uniform,
	                m_program
	            );
	        }
	    }

	    return result;
	}
	
	/**
	 * Returns the GL program object for the shader
	 * @return the GL shader object ID
	 */
	public int getProgram(){
		return m_program;
	}

	public void setLinkeTask(LinkerTask task) {m_linkerTask = task;}
	public LinkerTask getLinkerTask()         { return m_linkerTask;}
	
	/**
	 * Binds a 2D texture to a shader uniform by name.<p>
	 * Binds the given texture to the supplied texture unit and the unit to the given uniform.<br>
	 * Assumes that the given shader is bound via <code>enable</code>
	 * @param name the string name of the uniform
	 * @param unit the texture unit which will be used for the texture(count from 0)
	 * @param tex the texture to be bound
	 */
	public void bindTexture2D(String name, int unit, int tex)
	{
	    int loc = getUniformLocation(name, false);
	    if (loc >= 0) {
	    	GLES20.glUniform1i(loc, unit);
	        GLES.glActiveTexture(GL11.GL_TEXTURE0 + unit);
	        GLES.glBindTexture(GL11.GL_TEXTURE_2D, tex);
	    }
	}

	/**
	 * Binds a 2D texture to a shader uniform by index.<p>
	 * Binds the given texture to the supplied texture unit and the unit to the given uniform.<br>
	 * Assumes that the given shader is bound via <code>enable</code>
	 * @param index the index of the uniform
	 * @param unit the texture unit which will be used for the texture(count from 0)
	 * @param tex the texture to be bound
	 */
	public void bindTexture2D(int index, int unit, int tex)
	{
		GLES20.glUniform1i(index, unit);
		GLES.glActiveTexture(GL11.GL_TEXTURE0 + unit);
		GLES.glBindTexture(GL11.GL_TEXTURE_2D, tex);
	}

	/**
	 * Binds an array texture to a shader uniform by name.<p>
	 * Binds the given texture to the supplied texture unit and the unit to the given uniform.<br>
	 * Assumes that the given shader is bound via <code>enable</code>
	 * @param name the string name of the uniform
	 * @param unit the texture unit which will be used for the texture(count from 0)
	 * @param tex the texture to be bound
	 */
	public void bindTextureArray(String name, int unit, int tex)
	{
	    int loc = getUniformLocation(name, false);
	    if (loc >= 0) {
	    	GLES20.glUniform1i(loc, unit);
	    	GLES.glActiveTexture(GL11.GL_TEXTURE0 + unit);
	    	GLES.glBindTexture(0x8c1a, tex); // GL_TEXTURE_2D_ARRAY
	    }
	}

	/**
	 * Binds an array texture to a shader uniform by index.<p>
	 * Binds the given texture to the supplied texture unit and the unit to the given uniform.<br>
	 * Assumes that the given shader is bound via <code>enable</code>
	 * @param index the index of the uniform
	 * @param unit the texture unit which will be used for the texture(count from 0)
	 * @param tex the texture to be bound
	 */
	public void bindTextureArray(int index, int unit, int tex)
	{
		GLES20.glUniform1i(index, unit);
	    GLES20.glActiveTexture(GL11.GL_TEXTURE0 + unit);
	    GLES20.glBindTexture(0x8c1a, tex); // GL_TEXTURE_2D_ARRAY
	}

	/**
	 * Set scalar program uniform by string name.
	 * Assumes that the given shader is bound via {@link #enable}
	 * @param name the string with the name of the uniform
	 * @param value scalar value of the uniform
	 */
	public void setUniform1i(String name, int value)
	{
	    int loc = getUniformLocation(name, false);
	    if (loc >= 0) {
	        GLES20.glUniform1i(loc, value);
	    }
	}

	/**
	 * Set scalar program uniform by index.
	 * Assumes that the given shader is bound via {@link #enable}
	 * @param index the index of the uniform
	 * @param value scalar value of the uniform
	 */
	public void setUniform1i(int index, int value)
	{
	    if (index >= 0) {
	        GLES20.glUniform1i(index, value);
	    }
	}

	/**
	 * Set 2-vec program uniform by string name.
	 * Assumes that the given shader is bound via {@link #enable}
	 * @param name the string with the name of the uniform
	 * @param x first value of vector uniform
	 * @param y second value of vector uniform
	 */
	public void setUniform2i(String name, int x, int y)
	{
	    int loc = getUniformLocation(name, false);
	    if (loc >= 0) {
	    	GLES20.glUniform2i(loc, x, y);
	    }
	}

	/**
	 * Set 2-vec program uniform by index.
	 * Assumes that the given shader is bound via {@link #enable}
	 * @param index the index of the uniform
	 * @param x first value of vector uniform
	 * @param y second value of vector uniform
	 */
	public void setUniform2i(int index, int x, int y)
	{
	    if (index >= 0) {
	    	GLES20.glUniform2i(index, x, y);
	    }
	}

	/**
	 * Set 3-vec program uniform by string name.
	 * Assumes that the given shader is bound via {@link #enable}
	 * @param name the string with the name of the uniform
	 * @param x first value of vector uniform
	 * @param y second value of vector uniform
	 * @param z third value of vector uniform
	 */
	public void setUniform3i(String name, int x, int y, int z)
	{
	    int loc = getUniformLocation(name, false);
	    if (loc >= 0) {
	    	GLES20.glUniform3i(loc, x, y, z);
	    }
	}

	/**
	 * Set 3-vec program uniform by index.
	 * Assumes that the given shader is bound via {@link #enable}
	 * @param index the index of the uniform
	 * @param x first value of vector uniform
	 * @param y second value of vector uniform
	 * @param z third value of vector uniform
	 */
	public void setUniform3i(int index, int x, int y, int z)
	{
	    if (index >= 0) {
	    	GLES20.glUniform3i(index, x, y, z);
	    }
	}

	/**
	 * Set scalar program uniform by string name.
	 * Assumes that the given shader is bound via {@link #enable}
	 * @param name the string with the name of the uniform
	 * @param value scalar value of the uniform
	 */
	public void setUniform1f(String name, float value)
	{
	    int loc = getUniformLocation(name, false);
	    if (loc >= 0) {
	    	GLES20.glUniform1f(loc, value);
	    }
	}

	/**
	 * Set scalar program uniform by index.
	 * Assumes that the given shader is bound via {@link #enable}
	 * @param index the index of the uniform
	 * @param value scalar value of the uniform
	 */
	public void setUniform1f(int index, float value)
	{
	    if (index >= 0) {
	    	GLES20.glUniform1f(index, value);
	    }
	}

	/**
	 * Set 2-vec program uniform by string name.
	 * Assumes that the given shader is bound via {@link #enable}
	 * @param name the string with the name of the uniform
	 * @param x first value of vector uniform
	 * @param y second value of vector uniform
	 */
	public void setUniform2f(String name, float x, float y)
	{
	    int loc = getUniformLocation(name, false);
	    if (loc >= 0) {
	    	GLES20.glUniform2f(loc, x, y);
	    }
	}

	/**
	 * Set 2-vec program uniform by index.
	 * Assumes that the given shader is bound via {@link #enable}
	 * @param index the index of the uniform
	 * @param x first value of vector uniform
	 * @param y second value of vector uniform
	 */
	public void setUniform2f(int index, float x, float y)
	{
	    if (index >= 0) {
	    	GLES20.glUniform2f(index, x, y);
	    }
	}

	/**
	 * Set 3-vec program uniform by string name.
	 * Assumes that the given shader is bound via {@link #enable}
	 * @param name the string with the name of the uniform
	 * @param x first value of vector uniform
	 * @param y second value of vector uniform
	 * @param z third value of vector uniform
	 */
	public void setUniform3f(String name, float x, float y, float z)
	{
	    int loc = getUniformLocation(name, false);
	    if (loc >= 0) {
	    	GLES20.glUniform3f(loc, x, y, z);
	    }
	}

	/**
	 * Set 3-vec program uniform by index.
	 * Assumes that the given shader is bound via {@link #enable}
	 * @param index the index of the uniform
	 * @param x first value of vector uniform
	 * @param y second value of vector uniform
	 * @param z third value of vector uniform
	 */
	public void setUniform3f(int index, float x, float y, float z)
	{
	    if (index >= 0) {
	    	GLES20.glUniform3f(index, x, y, z);
	    }
	}

	/**
	 * Set 4-vec program uniform by string name.
	 * Assumes that the given shader is bound via {@link #enable}
	 * @param name the string with the name of the uniform
	 * @param x first value of vector uniform
	 * @param y second value of vector uniform
	 * @param z third value of vector uniform
	 * @param w fourth value of vector uniform
	 */
	public void setUniform4f(String name, float x, float y, float z, float w)
	{
	    int loc = getUniformLocation(name, false);
	    if (loc >= 0) {
	    	GLES20.glUniform4f(loc, x, y, z, w);
	    }
	}

	/**
	 * Set 4-vec program uniform by index.
	 * Assumes that the given shader is bound via {@link #enable}
	 * @param index the index of the uniform
	 * @param x first value of vector uniform
	 * @param y second value of vector uniform
	 * @param z third value of vector uniform
	 * @param w fourth value of vector uniform
	 */
	public void setUniform4f(int index, float x, float y, float z, float w)
	{
	    if (index >= 0) {
	    	GLES20.glUniform4f(index, x, y, z, w);
	    }
	}

	/**
	 * Set vector program uniform array by string name.
	 * Assumes that the given shader is bound via {@link #enable}
	 * @param name the string with the name of the uniform
	 * @param value array of values
	 * @param offset The offset within the array of the first float to be read; must be non-negative and no larger than array.length
	 * @param count The number of floats to be read from the given array; must be non-negative and no larger than array.length - offset
	 */
	public void setUniform1fv(String name, float[] value, int offset, int count)
	{
		int loc = getUniformLocation(name, false);
		if (loc >= 0) {
			GLES20.glUniform1fv(loc, count, value, offset);  // TODO
		}
	}

	/**
	 * Set vector program uniform array by string name.
	 * Assumes that the given shader is bound via {@link #enable}
	 * @param name the string with the name of the uniform
	 * @param value array of values
	 * @param offset The offset within the array of the first float to be read; must be non-negative and no larger than array.length
	 * @param count The number of floats to be read from the given array; must be non-negative and no larger than array.length - offset
	 */
	public void setUniform3fv(String name, float[] value, int offset, int count)
	{
	    int loc = getUniformLocation(name, false);
	    if (loc >= 0) {
//	        GLES20.glUniform3fv(loc, GLUtil.wrap(value, offset, count), 0);
	    	GLES20.glUniform3fv(loc, count, value, offset);  // TODO
	    }
	}

	/**
	 * Set vector program uniform array by index.
	 * Assumes that the given shader is bound via {@link #enable}
	 * @param index the index of the uniform
	 * @param value array of values
	 * @param offset The offset within the array of the first float to be read; must be non-negative and no larger than array.length
	 * @param count The number of floats to be read from the given array; must be non-negative and no larger than array.length - offset
	 */
	public void setUniform3fv(int index, float[] value, int offset, int count)
	{
		if (index >= 0) {
	        GLES20.glUniform3fv(index, count, value, offset); // TODO
	    }
	}
	/**
	 * Set vector program uniform array by string name.
	 * Assumes that the given shader is bound via {@link #enable}
	 * @param name the string with the name of the uniform
	 * @param value array of values
	 */
	public void setUniform3fv(String name, float[] value)
	{
		setUniform3fv(name, value, 0, value.length);
	}

	/**
	 * Set vector program uniform array by index.
	 * Assumes that the given shader is bound via {@link #enable}
	 * @param index the index of the uniform
	 * @param value array of values
	 */
	public void setUniform3fv(int index, float[] value)
	{
		setUniform3fv(index, value, 0, value.length);
	}

	/**
	 * Set vector program uniform array by string name.
	 * Assumes that the given shader is bound via {@link #enable}
	 * @param name the string with the name of the uniform
	 * @param value array of values
	 * @param offset The offset within the array of the first float to be read; must be non-negative and no larger than array.length
	 * @param count The number of floats to be read from the given array; must be non-negative and no larger than array.length - offset
	 */
	public void setUniform4fv(String name, float[] value, int offset, int count)
	{
	    int loc = getUniformLocation(name, false);
	    if (loc >= 0) {
	        GLES20.glUniform4fv(loc, count, value, offset); // TODO
	    }
	}

	/**
	 * Set vector program uniform array by index.
	 * Assumes that the given shader is bound via {@link #enable}
	 * @param index the index of the uniform
	 * @param value array of values
	 * @param offset The offset within the array of the first float to be read; must be non-negative and no larger than array.length
	 * @param count The number of floats to be read from the given array; must be non-negative and no larger than array.length - offset
	 */
	public void setUniform4fv(int index, float[] value, int offset, int count)
	{
		if (index >= 0) {
			GLES20.glUniform4fv(index, count, value, offset); // TODO
	    }
	}
	
	/**
	 * Set vector program uniform array by string name.
	 * Assumes that the given shader is bound via {@link #enable}
	 * @param name the string with the name of the uniform
	 * @param value array of values
	 */
	public void setUniform4fv(String name, float[] value)
	{
		setUniform4fv(name, value, 0, value.length);
	}

	/**
	 * Set vector program uniform array by index.
	 * Assumes that the given shader is bound via {@link #enable}
	 * @param index the index of the uniform
	 * @param value array of values
	 */
	public void setUniform4fv(int index, float[] value)
	{
		if (index >= 0) {
			GLES20.glUniform4fv(index, value.length, value, 0); // TODO
	    }
	}

	/**
	 * Set matrix array program uniform array by name.
	 * Assumes that the given shader is bound via {@link #enable}
	 * @param name the string with the name of the uniform
	 * @param m array of matrices
	 * @param offset The offset within the array of the first float to be read; must be non-negative and no larger than array.length
	 * @param count The number of floats to be read from the given array; must be non-negative and no larger than array.length - offset
	 * @param transpose if true, the matrices are transposed on input
	 */
	public void setUniformMatrix4fv(String name, float[] m, int offset, int count, boolean transpose)
	{
	    int loc = getUniformLocation(name, false);
	    if (loc >= 0) {
	    	GLES20.glUniformMatrix4fv(loc, count/16, transpose, m, offset);
	    }
	}
	
	/**
	 * Set matrix program uniform by name.
	 * Assumes that the given shader is bound via {@link #enable}
	 * @param name the string with the name of the uniform
	 * @param mat the matrix
	 * @param transpose if true, the matrices are transposed on input
	 */
	public void setUniformMatrix4(String name, Matrix4f mat, boolean transpose)
	{
	    int loc = getUniformLocation(name, false);
	    if (loc >= 0) {
	    	GLES20.glUniformMatrix4fv(loc, 1,transpose, GLUtil.wrap(mat));
	    }
	}
	
	/**
	 * Set matrix program uniform by name.
	 * Assumes that the given shader is bound via {@link #enable}
	 * @param name the string with the name of the uniform
	 * @param mat the matrix
	 */
	public void setUniformMatrix4(String name, Matrix4f mat)
	{
	    int loc = getUniformLocation(name, false);
	    if (loc >= 0) {
	    	GLES20.glUniformMatrix4fv(loc, 1, false, GLUtil.wrap(mat));
	    }
	}
	
	/**
	 * Set matrix program uniform by index.
	 * Assumes that the given shader is bound via {@link #enable}
	 * @param index the index of the uniform
	 * @param mat the matrix
	 * @param transpose if true, the matrices are transposed on input
	 */
	public void setUniformMatrix4(int index, Matrix4f mat, boolean transpose)
	{
    	GLES20.glUniformMatrix4fv(index, 1, transpose, GLUtil.wrap(mat));
	}

	/**
	 * Set matrix array program uniform array by index.
	 * Assumes that the given shader is bound via {@link #enable}
	 * @param index the index of the uniform
	 * @param m array of matrices
	 * @param offset The offset within the array of the first float to be read; must be non-negative and no larger than array.length
	 * @param count The number of floats to be read from the given array; must be non-negative and no larger than array.length - offset
	 * @param transpose if true, the matrices are transposed on input
	 */
	public void setUniformMatrix4fv(int index, float[] m, int offset, int count, boolean transpose)
	{
	    if (index >= 0) {
	    	GLES20.glUniformMatrix4fv(index, count/16, transpose, m, offset);
	    }
	}

	/**
	 * Set matrix array program uniform array by name.
	 * Assumes that the given shader is bound via {@link #enable}
	 * @param name the string with the name of the uniform
	 * @param m array of matrices
	 * @param transpose if true, the matrices are transposed on input
	 */
	public void setUniformMatrix4fv(String name, float[] m, boolean transpose)
	{
		setUniformMatrix4fv(name, m, 0, m.length, transpose);
	}

	/**
	 * Set matrix array program uniform array by index.
	 * Assumes that the given shader is bound via {@link #enable}
	 * @param index the index of the uniform
	 * @param m array of matrices
	 * @param transpose if true, the matrices are transposed on input
	 */
	public void setUniformMatrix4fv(int index, float[] m, boolean transpose)
	{
	    if (index >= 0) {
	    	GLES20.glUniformMatrix4fv(index, m.length/16, transpose, m, 0);
	    }
	}
	/**
	 * Represents a piece of shader source and the shader type.<p>
	 * Used with creation functions to pass in arrays of multiple shader source types.
	 */
	public static final class ShaderSourceItem{
		/** Shader source code */
		public CharSequence src;
		/** The GL_*_SHADER enum representing the shader type */
		public int type;
		
		public ShaderSourceItem() {
		}

		public ShaderSourceItem(CharSequence src, int type) {
			this.src = src;
			this.type = type;
		}
	}

	@Override
	public void dispose() {
		GLES20.glDeleteProgram(m_program);
	}
}
