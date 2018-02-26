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

import org.lwjgl.util.vector.Vector3f;

import javax.microedition.khronos.opengles.GL11;

/**
 * Drawable geometric model using GL VBOs. Supports loading from OBJ file data.
 * Contains positions as well as optional normals, UVs, and tangent vectors
 * @author Nvidia 2014-9-1
 *
 */
public class NvGLModel {

	private NvModel model;
	private int model_vboID, model_iboID;
	private final Vector3f m_minExtent = new Vector3f();
	private final Vector3f m_maxExtent = new Vector3f();
	private final Vector3f m_radius = new Vector3f();
	
	public final Vector3f m_center = new Vector3f();
	
	/**
	 * Initialize internal model with passed in ptr. You should call this method in OpenGL context.
	 * @param model pointer to an NvModel to use for mesh data.
	 */
	public NvGLModel(NvModel model) {
		this.model = model;
		
		model_vboID = GLES.glGenBuffers();
		model_iboID = GLES.glGenBuffers();
		
		if(model == null){
			this.model = new NvModel();
		}
	}
	/**
	 * Initialize internal model. You should call this method in OpenGL context.
	 */
	public NvGLModel() {
		this(null);
	}
	
	public void dispose(){
		GLES.glDeleteBuffers(model_vboID);
		GLES.glDeleteBuffers(model_iboID);
	}
	
	/**
	 * Loads a model from OBJ-formatted file.
	 * @param filename
	 */
	public void loadModelFromFile(String filename){
		model.loadModelFromFile(filename);
		
		computeCenter();
	}
	
	public void computeCenter(){
		model.computeBoundingBox(m_minExtent, m_maxExtent);
		((Vector3f)Vector3f.sub(m_maxExtent, m_minExtent, m_radius)).scale(0.5f);
		Vector3f.add(m_minExtent, m_radius, m_center);
	}
	
	/**
	 * Rescales the model geometry and centers it around the origin.  Does NOT update 
	 * the vertex buffers.  Applications should update the VBOs via #initBuffers
	 * @param radius the desired new radius.  The model geometry will be rescaled to
	 *  fit this radius
	 */
	public void rescaleModel(float radius){
		model.rescaleToOrigin(radius);
	}
	
	/** Initialize or update the model geometry VBOs
	 * @see #initBuffers(boolean) */
	public void initBuffers(){
		initBuffers(false);
	}
	
	/**
	 * Initialize or update the model geometry VBOs
	 * @param computeTangents if set to true, then tangent vectors will be computed
     * to be in the S texture coordinate direction.  This may require vertices to be
     * duplicated in order to allow multiple tangents at a point.  This can cause model
     * size explosion, and should be done only if required.
	 */
	public void initBuffers(boolean computeTangents){
		model.computeNormals();
		
		if(computeTangents)
			model.computeTangents();
		
		model.compileModel(NvModel.TRIANGLES);
		
		GLES.glBindBuffer(GL11.GL_ARRAY_BUFFER, model_vboID);
		GLES.glBufferData(GL11.GL_ARRAY_BUFFER, GLUtil.wrap(model.getCompiledVertices(), 0, model.getCompiledVertexCount() * model.getCompiledVertexSize()), GL11.GL_STATIC_DRAW);
		GLES.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);

		GLES.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, model_iboID);
		GLES.glBufferData(GL11.GL_ELEMENT_ARRAY_BUFFER, GLUtil.wrap(model.getCompiledIndices(NvModel.TRIANGLES), 0, model.getCompiledIndexCount(NvModel.TRIANGLES)), GL11.GL_STATIC_DRAW);
		GLES.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 0);
	}
	
	private void bindBuffers(){
		GLES.glBindBuffer(GL11.GL_ARRAY_BUFFER, model_vboID);
		GLES.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, model_iboID);
	}
	
	private void unbindBuffers()
	{
		GLES.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
		GLES.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 0);
	}

	/**
	 * Draw the model using the current shader (positions)<p>
	 * Binds the vertex position array to the given attribute array index and draws the
	 * model with the currently bound shader.
	 * @param positionHandle the vertex attribute array index that represents position in the current shader
	 */
	public void drawElements(int positionHandle)
	{
	    bindBuffers();
	    GLES20.glVertexAttribPointer(positionHandle, model.getPositionSize(), GL11.GL_FLOAT, false, model.getCompiledVertexSize() * 4, 0);
	    GLES20.glEnableVertexAttribArray(positionHandle);
	    GLES20.glDrawElements(GL11.GL_TRIANGLES, model.getCompiledIndexCount(NvModel.TRIANGLES), GLES20.GL_UNSIGNED_INT, 0);
	    GLES20.glDisableVertexAttribArray(positionHandle);
	    unbindBuffers();
	}

	/**
	 * Draw the model using the current shader (positions and normals)<p>
	 * Binds the vertex position and normal arrays to the given attribute array indices and draws the
	 * model with the currently bound shader.
	 * @param positionHandle the vertex attribute array index that represents position in the current shader
	 * @param normalHandle the vertex attribute array index that represents normals in the current shader
	 */
	public void drawElements(int positionHandle, int normalHandle)
	{
	    bindBuffers();
	    GLES20.glVertexAttribPointer(positionHandle, model.getPositionSize(), GL11.GL_FLOAT, false, model.getCompiledVertexSize() * 4, 0);
	    GLES20.glEnableVertexAttribArray(positionHandle);
	    
	    if (normalHandle >= 0) {
	    	GLES20.glVertexAttribPointer(normalHandle, model.getNormalSize(), GL11.GL_FLOAT, false, model.getCompiledVertexSize() * 4, (model.getCompiledNormalOffset()*4));
	    	GLES20.glEnableVertexAttribArray(normalHandle);
	    }
	    
	    GLES20.glDrawElements(GL11.GL_TRIANGLES, model.getCompiledIndexCount(NvModel.TRIANGLES), GLES20.GL_UNSIGNED_INT, 0);

	    GLES20.glDisableVertexAttribArray(positionHandle);
	    if (normalHandle >= 0)
	    	GLES20.glDisableVertexAttribArray(normalHandle);
	    unbindBuffers();
	}

	/**
	 * Draw the model using the current shader (positions, UVs and normals)<p>
	 * Binds the vertex position, UV and normal arrays to the given attribute array indices and draws the
	 * model with the currently bound shader.
	 * @param positionHandle the vertex attribute array index that represents position in the current shader
	 * @param normalHandle the vertex attribute array index that represents normals in the current shader
	 * @param texcoordHandle the vertex attribute array index that represents UVs in the current shader
	 */
	public void drawElements(int positionHandle, int normalHandle, int texcoordHandle)
	{
	    bindBuffers();
	    GLES20.glVertexAttribPointer(positionHandle, model.getPositionSize(), GL11.GL_FLOAT, false, model.getCompiledVertexSize() * 4, 0);
	    GLES20.glEnableVertexAttribArray(positionHandle);

	    if (normalHandle >= 0) {
	    	GLES20.glVertexAttribPointer(normalHandle, model.getNormalSize(), GL11.GL_FLOAT, false, model.getCompiledVertexSize() * 4, (model.getCompiledNormalOffset()*4));
	    	GLES20.glEnableVertexAttribArray(normalHandle);
	    }

	    if (texcoordHandle >= 0 && model.getTexCoordSize() > 0) {
	    	GLES20.glVertexAttribPointer(texcoordHandle, model.getTexCoordSize(), GL11.GL_FLOAT, false, model.getCompiledVertexSize() * 4, (model.getCompiledTexCoordOffset()*4));
	    	GLES20.glEnableVertexAttribArray(texcoordHandle);
	    }
	    
	    GLES20.glDrawElements(GL11.GL_TRIANGLES, model.getCompiledIndexCount(NvModel.TRIANGLES), GLES20.GL_UNSIGNED_INT, 0);
	    
	    GLES20.glDisableVertexAttribArray(positionHandle);
	    if (normalHandle >= 0)
	    	GLES20.glDisableVertexAttribArray(normalHandle);
	    if (texcoordHandle >= 0)
	    	GLES20.glDisableVertexAttribArray(texcoordHandle);
	    unbindBuffers();
	}

	/**
	 * Draw the model using the current shader (positions, UVs, normals and tangents)<p>
	 * Binds the vertex position, UV, normal and tangent arrays to the given attribute array indices and draws the
	 * model with the currently bound shader.
	 * @param positionHandle the vertex attribute array index that represents position in the current shader
	 * @param normalHandle the vertex attribute array index that represents normals in the current shader
	 * @param texcoordHandle the vertex attribute array index that represents UVs in the current shader
	 * @param tangentHandle the vertex attribute array index that represents tangents in the current shader
	 */
	public void drawElements(int positionHandle, int normalHandle, int texcoordHandle, int tangentHandle)
	{
	    bindBuffers();
	    GLES20.glVertexAttribPointer(positionHandle, model.getPositionSize(), GL11.GL_FLOAT, false, model.getCompiledVertexSize() * 4, 0);
	    GLES20.glEnableVertexAttribArray(positionHandle);

	    if (normalHandle >= 0) {
	    	GLES20.glVertexAttribPointer(normalHandle, model.getNormalSize(), GL11.GL_FLOAT, false, model.getCompiledVertexSize() * 4, (model.getCompiledNormalOffset()*4));
	    	GLES20.glEnableVertexAttribArray(normalHandle);
	    }

	    if (texcoordHandle >= 0) {
	    	GLES20.glVertexAttribPointer(texcoordHandle, model.getTexCoordSize(), GL11.GL_FLOAT, false, model.getCompiledVertexSize() * 4, (model.getCompiledTexCoordOffset()*4));
	    	GLES20.glEnableVertexAttribArray(texcoordHandle);
	    }

	    if (tangentHandle >= 0) {
	    	GLES20.glVertexAttribPointer(tangentHandle,  model.getTangentSize(), GL11.GL_FLOAT, false, model.getCompiledVertexSize() * 4, (model.getCompiledTangentOffset()*4));
	    	GLES20.glEnableVertexAttribArray(tangentHandle);
	    }
	    GLES20.glDrawElements(GL11.GL_TRIANGLES, model.getCompiledIndexCount(NvModel.TRIANGLES), GLES20.GL_UNSIGNED_INT, 0);

	    GLES20.glDisableVertexAttribArray(positionHandle);
	    if (normalHandle >= 0)
	    	GLES20.glDisableVertexAttribArray(normalHandle);
	    if (texcoordHandle >= 0)
	    	GLES20.glDisableVertexAttribArray(texcoordHandle);
	    if (tangentHandle >= 0)
	    	GLES20.glDisableVertexAttribArray(tangentHandle);
	    unbindBuffers();
	}

	/**
	 * Get the low-level geometry data.
	 * @return the underlying geometry model data instance
	 */
	public NvModel getModel()
	{
	    return model;
	}
	
	/** return a reference to the minExtent */
	public Vector3f getMinExt(){
		return m_minExtent;
	}
	
	/** return a reference to the maxExtent */
	public Vector3f getMaxExt(){
		return m_maxExtent;
	}
}
