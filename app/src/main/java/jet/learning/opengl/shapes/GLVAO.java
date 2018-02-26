////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2017 mzhg
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
package jet.learning.opengl.shapes;

import android.opengl.GLES11;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLES31;

import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.GLUtil;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

/** OpenGL Vertex Array Object. */
public class GLVAO {

//	final Model parent;
	int vaoID;
	GLBuffer[] glbuffers;
	AttribInfo[] attribInfos;
	byte[] validIds;
	int stride;
	int vertexCount;
	
	GLBuffer element;
	int elementCount;
	int elementType;
	int elementID;
	int elementModified;
	
	boolean programed;
	boolean prepared;
	
	/*public */GLVAO(Model parent, boolean programed) {
//		this.parent = parent;
		this.programed = programed;
		
		// Generate the GL-Buffers
		List<AttribArray> attribs = parent.attribs;
		List<AttribInfo>  infos   = parent.attribInfos;
		
//		if(parent.isProgramFlag()){
//			connects = new AttribConnect[1];
//			connects[0] = AttribConnect.VERTEX_ATTRIB;
//		}else{
//			connects = new AttribConnect[]
//		}
		
		attribInfos = new AttribInfo[infos.size()];
		for(int i = 0; i < attribInfos.length; i++)
			attribInfos[i] = new AttribInfo();
		
		if(parent.dymatic || parent.sperate_buffer){
			// Generate the separate buffer.
			glbuffers = new GLBuffer[attribs.size()];
			for(int i = 0; i < glbuffers.length; i++){
				AttribArray attrib = attribs.get(i);
				AttribInfo  info   = infos.get(i);
				attribInfos[i].index = info.index;
				attribInfos[i].size = info.size;
				attribInfos[i].type = info.type;
				attribInfos[i].offset = 0;
				attribInfos[i].modified = attrib.modified;  // record the 
				attribInfos[i].divisor = attrib.divisor;
				
				if(attrib != null){
					GLBuffer buffer = glbuffers[i] = new GLBuffer(GLES20.GL_ARRAY_BUFFER, GLES20.GL_DYNAMIC_DRAW);
					int size = attrib.getByteSize();
					ByteBuffer buf = GLUtil.getCachedByteBuffer(size);
					attrib.store(buf);
					buf.flip();
					buffer.load(buf);
					if(attrib.divisor == 0)
						vertexCount = attrib.getSize();
				}
			}
			
		}else{
			// Generate the combined gl-buffer-object.
			glbuffers = new GLBuffer[1];
			GLBuffer buffer = glbuffers[0] = new GLBuffer(GLES20.GL_ARRAY_BUFFER, GLES20.GL_STATIC_DRAW);
			validIds = new byte[parent.size()];
			
			// measure the attribute informations.
			int bufferSize = 0;
			stride = 0;
			int validIndex = 0;
			for(int i = 0; i < attribs.size(); i++){
				AttribArray attrib = attribs.get(i);
				AttribInfo  info   = infos.get(i);
				if(attrib != null){
					bufferSize += attrib.getByteSize();
					attribInfos[i].index = info.index;
					attribInfos[i].offset = stride;
					attribInfos[i].size = info.size;
					attribInfos[i].type = info.type;
					attribInfos[i].modified = attrib.modified;
					attribInfos[i].divisor = attrib.divisor;
					
					int cmpSize = attrib.getByteSize()/attrib.getSize();
					stride += cmpSize;
					
					if(attrib.divisor == 0){
						vertexCount = attrib.getSize();
					}
					validIds[validIndex ++] = (byte)i;
				}
			}
			
			// fill the data
			ByteBuffer buf = GLUtil.getCachedByteBuffer(bufferSize);
			for(int j = 0; j < vertexCount; j++){
				for(int i = 0; i < attribs.size(); i++){
					AttribArray attrib = attribs.get(i);
					if(attrib != null){
						attrib.store(j, buf);
					}
				}
			}
			buf.flip();
			
			// generate the gl buffer
			buffer.bind();
			buffer.load(buf);
			buffer.unbind();
		}

		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
		
		if(parent.element != null){
			createElementBuffer(parent);
		}
		
		// Generate the VAO if the video card supported.
		if(programed){ // TODO Need precise check
			vaoID = GLES.glGenVertexArrays();
			GLES30.glBindVertexArray(vaoID);
			_bind();
			GLES30.glBindVertexArray(0);
			_unbind();
		}
	}
	
	// Unimplemented
//	void prepareUpdate(){
//		for(int i = 0; i < glbuffers.length; i++){
//			GLBuffer buffer = glbuffers[i];
//			if(buffer != null){
//				
//			}
//		}
//	}
	
	void update(Model parent, boolean isProgram){
		List<AttribArray> attribs = parent.attribs;
		List<AttribInfo>  infos   = parent.attribInfos;
		
		boolean needReBind = false;
		// update the attribute informations
		if(attribInfos.length != infos.size()){
			int old_length = attribInfos.length;
			attribInfos = Arrays.copyOf(attribInfos, infos.size());
			for(int i = old_length; i < attribInfos.length; i++)
				attribInfos[i] = new AttribInfo();
			
			needReBind = true;
		}
		
//		boolean isProgram = parent.flag == Model.FLAG_WITH_PIPELINE;
		if(programed != isProgram){
			programed = isProgram;
			needReBind = true;
		}
		
		// update the buffer content
		if(parent.dymatic){
			// update the separate buffer.
			if(glbuffers.length != attribs.size()){
				glbuffers =  Arrays.copyOf(glbuffers, attribs.size());
			}
			
			int newVertexCount = -1;
			for(int i = 0; i < glbuffers.length; i++){
				AttribArray attrib = attribs.get(i);
				AttribInfo  info   = infos.get(i);
				attribInfos[i].index = info.index;
				attribInfos[i].type = info.type;
				attribInfos[i].offset = 0;
				GLBuffer buffer = glbuffers[i];
				
				if(attrib != null){
					if(buffer == null){ // create a new one.
						buffer = glbuffers[i] = new GLBuffer(GLES20.GL_ARRAY_BUFFER, GLES20.GL_DYNAMIC_DRAW);
						needReBind = true;
						ByteBuffer buf = wrap(attrib);
						buffer.load(buf);
						newVertexCount = attrib.getSize();
					}else{
						// check weather the content of the new attribute has changed.
						if(attribInfos[i].modified != attrib.modified){
							buffer.bind();
							if(vertexCount != attrib.getSize()){
								// The buffer length has changed. reload the buffer data.
								ByteBuffer buf = wrap(attrib);
								buffer.load(buf);
								newVertexCount = attrib.getSize();
							}else{
								ByteBuffer buf = GLUtil.getCachedByteBuffer(attrib.getSize());
								attrib.store(buf);
								buf.flip();
								buffer.update(0, attrib.getSize(), buf);
							}
							attribInfos[i].modified = attrib.modified;
						}else{
							// nothing need todo.
						}
					}
				}else{ // attribute is null, maybe removed.
					if(buffer != null){
						// remove the buffer
						buffer.unbind();
						buffer.dispose();
						glbuffers[i] = null;
					}
				}
			}
			
			if(newVertexCount != -1)
				vertexCount = newVertexCount;
		}else{
			// Generate the combined gl-buffer-object.
//			GLBuffer buffer = glbuffers[0] = new GLBuffer(GL15.GL_ARRAY_BUFFER, GL15.GL_STATIC_DRAW);
//			validIds = new byte[parent.size()];
			boolean needUpdate = false;
			GLBuffer buffer = glbuffers[0];
			if(validIds.length != parent.size()){
				validIds = new byte[parent.size()];
				needUpdate = true;
			}
			
			// measure the attribute informations.
			int bufferSize = 0;
			int stride = 0;
			int validIndex = 0;
			int[] tmpOffsets = new int[attribs.size()];
			int[] elemByteSizes = new int[attribs.size()];
			for(int i = 0; i < attribs.size(); i++){
				AttribArray attrib = attribs.get(i);
				AttribInfo  info   = infos.get(i);
				
				if(attrib != null){
					bufferSize += attrib.getByteSize();
					attribInfos[i].index = info.index;
//					attribInfos[i].offset = stride;
					tmpOffsets[i] = stride;
//					attribInfos[i].size = info.size;
//					attribInfos[i].type = info.type;
					int cmpSize = attrib.getByteSize()/attrib.getSize();
					elemByteSizes[i] = cmpSize;
					stride += cmpSize;
					vertexCount = attrib.getSize();
					validIds[validIndex ++] = (byte)i;
				}
			}
			
			ByteBuffer buf;
			if(bufferSize != buffer.getBufferSize()){
				// fill the data
				buf = GLUtil.getCachedByteBuffer(bufferSize);
				for(int j = 0; j < vertexCount; j++){
					for(int i = 0; i < attribs.size(); i++){
						AttribArray attrib = attribs.get(i);
						if(attrib != null){
							attrib.store(j, buf);
						}
					}
				}
				
				for(int i = 0; i < tmpOffsets.length; i++){
					AttribArray attrib = attribs.get(i);
					if(attrib != null){
						attribInfos[i].offset = tmpOffsets[i];
						attribInfos[i].modified = attrib.modified;
					}
				}
				
				buf.flip();
				// generate the gl buffer
				buffer.bind();
				buffer.load(buf);
			}else{
				buffer.bind();
//				buffer.prepareUpdate(GLES31.GL_WRITE_ONLY);
//				buf = buffer.getMappingBuffer();
				buf = GLUtil.getCachedByteBuffer((int)buffer.getBufferSize());
				for(int j = 0; j < vertexCount; j++){
					for(int i = 0; i < attribs.size(); i++){
						AttribArray attrib = attribs.get(i);
						if(attrib != null){
							if(attrib.modified != attribInfos[i].modified || tmpOffsets[i] != attribInfos[i].offset){
								attrib.store(j, buf);
								attribInfos[i].modified = attrib.modified;
								attribInfos[i].offset = tmpOffsets[i];
							}else{
								buf.position(buf.position() + elemByteSizes[i]);  // unmodifier the data.
							}
						}
					}
				}
				buf.flip();
				buffer.update(0, (int)buffer.getBufferSize(), buf);
			}
		}
		
		GLES11.glBindBuffer(GLES11.GL_ARRAY_BUFFER, 0);
		
		//update the element buffer
		if(parent.element != null){
			if(element == null){
				createElementBuffer(parent);
			}else{
				if(elementID != parent.element.getUniqueID() || elementModified != parent.element.modified){
					int bufferSize = parent.element.getByteSize();
					element.bind();
					ByteBuffer buf;
					if(bufferSize != element.getBufferSize()){
						buf = GLUtil.getCachedByteBuffer(bufferSize);
						parent.element.store(buf);
						buf.flip();
						element.load(buf);
					}else{
//						element.beginUpdate(GLES31.GL_WRITE_ONLY);
//						buf = element.getMappingBuffer();
//						parent.element.store(buf);
//						element.finishUpdate();

						buf = GLUtil.getCachedByteBuffer((int)element.getBufferSize());
						parent.element.store(buf);
						buf.flip();
						element.update(0, buf.remaining(), buf);
					}
					element.unbind();
					
					elementCount = parent.element.getSize();
					elementType = fixElementType(parent.element.getType());
					elementID = parent.element.getUniqueID();
					elementModified = parent.element.modified;
				}
			}
		}else{
			if(element != null){
				element.dispose();
				element = null;
			}
		}
		
		// Generate the VAO if the video card supported.
		if(needReBind &&  programed/*GL.getCapabilities().GL_ARB_vertex_array_object*/){
			GLES.glDeleteVertexArrays(vaoID);  // necessary do this?
			vaoID = GLES.glGenVertexArrays();
			GLES30.glBindVertexArray(vaoID);
			_bind();
			GLES30.glBindVertexArray(0);
			_unbind();
		}
	}
	
	void createElementBuffer(Model parent){
		element = new GLBuffer(GLES11.GL_ELEMENT_ARRAY_BUFFER, parent.dymatic ? GLES11.GL_DYNAMIC_DRAW : GLES11.GL_STATIC_DRAW);
		ByteBuffer buf = GLUtil.getCachedByteBuffer(parent.element.getByteSize());
		parent.element.store(buf);
		buf.flip();
		element.load(buf);
		element.unbind();
		elementCount = parent.element.getSize();
		elementType = fixElementType(parent.element.getType());
		elementID = parent.element.getUniqueID();
		elementModified = parent.element.modified;
	}
	
	private static ByteBuffer wrap(AttribArray attrib){
		int size = attrib.getByteSize();
		ByteBuffer buf = GLUtil.getCachedByteBuffer(size);
		attrib.store(buf);
		buf.flip();
		return buf;
	}
	
	private static int fixElementType(int type){
		switch (type) {
		case GLES11.GL_UNSIGNED_BYTE:
		case GLES11.GL_BYTE:
			return GLES11.GL_UNSIGNED_BYTE;

		case GLES11.GL_UNSIGNED_SHORT:
		case GLES11.GL_SHORT:
			return GLES11.GL_UNSIGNED_SHORT;
		case GLES20.GL_UNSIGNED_INT:
		case GLES20.GL_INT:
			return GLES20.GL_UNSIGNED_INT;
		default:
			throw new IllegalArgumentException("Unsupport Element type: " + type);
		}
	}
	
	public void draw(int mode){
		this.draw(mode, 1);
	}
	
	public void draw(int mode, int instanceCount){
		if(element != null){
			if(instanceCount > 1){
				GLES31.glDrawElementsInstanced(mode, elementCount, elementType, 0, instanceCount);
			}else{
				GLES11.glDrawElements(mode, elementCount, elementType, 0);
			}
		}else{
			if(instanceCount > 1){
				GLES31.glDrawArraysInstanced(mode, 0, vertexCount, instanceCount);
			}else{
				GLES11.glDrawArrays(mode, 0, vertexCount);
			}
		}
	}
	
	/** Bind the current buffer. */
	public void bind(){
		if(vaoID != 0){
			GLES30.glBindVertexArray(vaoID);
		}else{
			_bind();  // bind the buffer directly.
		}
	}
	
	public void unbind(){
		if(vaoID != 0){
			GLES30.glBindVertexArray(0);
		}else{
			_unbind();  // unbind the buffer directly.
		}
	}
	
	// bind the buffer
	private void _bind(){
		if(glbuffers.length == attribInfos.length){ // Separate buffer
			for(int i = 0; i < glbuffers.length; i++){
				GLBuffer buffer = glbuffers[i];
				AttribInfo info = attribInfos[i];
				if(buffer != null){
					buffer.bind();
					AttribConnect connect = getCorrespondConnect(info.index);
					connect.enable(info.index, info.size, info.type, 0, info.offset, info.divisor);
				}
			}
		}else{  // combined buffer
			for(int i = 0; i < glbuffers.length; i++){
				GLBuffer buffer = glbuffers[i];
				buffer.bind();
				for(int j = 0; j < validIds.length; j++){
					AttribInfo info = attribInfos[validIds[j]];
					AttribConnect connect = getCorrespondConnect(info.index);
					connect.enable(info.index, info.size, info.type, stride, info.offset, info.divisor);
//					System.out.println(info);
				}
			}
		}
		
		// bind element buffer if it exits.
		if(element != null){
			element.bind();
		}
	}
	
	// unbind the buffer
	private void _unbind(){
		if(glbuffers.length == attribInfos.length){ // Separate buffer
			for(int i = 0; i < glbuffers.length; i++){
				GLBuffer buffer = glbuffers[i];
				AttribInfo info = attribInfos[i];
				if(buffer != null){
					buffer.unbind();
					AttribConnect connect = getCorrespondConnect(info.index);
					connect.disable(info.index);
				}
			}
		}else{  // combined buffer
			for(int i = 0; i < glbuffers.length; i++){
				GLBuffer buffer = glbuffers[i];
				buffer.unbind();
				for(int j = 0; j < validIds.length; j++){
					AttribInfo info = attribInfos[validIds[j]];
					AttribConnect connect = getCorrespondConnect(info.index);
					connect.disable(info.index);
				}
			}
		}
		
		if(element != null){
			element.unbind();
		}
	}
	
	public void dispose(){
		if(vaoID != 0){
			GLES.glDeleteVertexArrays(vaoID);
			vaoID = 0;
		}
		
		for(int i = 0; i < glbuffers.length; i++){
			GLBuffer buffer = glbuffers[i];
			if(buffer != null){
				buffer.dispose();
				glbuffers[i] = null;
			}
		}
		
		if(element != null){
			element.dispose();
			element = null;
		}
	}
	
	private AttribConnect getCorrespondConnect(int index){
		if(programed)
			return AttribConnect.VERTEX_ATTRIB;
		else{
			switch (index) {
			case Model.TYPE_VERTEX:
//				AttribConnect.VERTEX_POINTER.enable(info.index, info.size, info.type, 0, info.offset);
				return AttribConnect.VERTEX_POINTER;
			case Model.TYPE_NORMAL:
//				AttribConnect.NORMAL_POINTER.enable(info.index, info.size, info.type, 0, info.offset);
				return AttribConnect.NORMAL_POINTER;
			case Model.TYPE_COLOR:
//				AttribConnect.COLOR_POINTER.enable(info.index, info.size, info.type, 0, info.offset);
//				break;
				return AttribConnect.COLOR_POINTER;
			default:
				int diff = index - Model.TYPE_TEXTURE0;
				if(diff >=0)
					return AttribConnect.TEXTURE_POINTER;
				else
					throw new IllegalArgumentException("Invalid index: " + index);
			}
		}
	}
}
