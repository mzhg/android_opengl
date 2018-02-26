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

import java.nio.ByteBuffer;

import javax.microedition.khronos.opengles.GL11;

public class NvSimpleFBO extends NvWritableFB{

	public int colorTexture;
	public int depthTexture;
	
	public NvSimpleFBO(Desc desc) {
		super(desc.width, desc.height);
		
		int prevFBO = 0;
		// Enum has MANY names based on extension/version
        // but they all map to 0x8CA6
		prevFBO = GLES.glGetInteger(0x8CA6);
		
		fbo = GLES.glGenFramebuffers();
		
		if (desc.color.format != 0)
        {
            colorTexture = createColorTexture2D(desc);

            GLES.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbo);
            GLES.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, colorTexture, 0);
        }

        if (desc.depth.format != 0)
        {
            depthTexture = createDepthTexture2D(desc);

            GLES.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbo);
            GLES.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D, depthTexture, 0);
        }

        GLES.checkFrameBufferStatus();

        width = desc.width;
        height = desc.height;

        // restore FBO
        GLES.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, prevFBO);
        GLES.checkGLError();
	}
	
	
	@Override
	public void dispose() {
		if (colorTexture != 0)
        {
            GLES.glDeleteTextures(colorTexture);
           colorTexture = 0;
        }
       if (depthTexture != 0)
       {
    	   GLES.glDeleteTextures(depthTexture);
           depthTexture = 0;
       }
        if (fbo != 0)
        {
        	GLES.glDeleteFramebuffers(fbo);
           fbo = 0;
        }
	}
	
	protected int createColorTexture2D(Desc desc){
		int texture = GLES.glGenTextures();
		GLES.glBindTexture(GL11.GL_TEXTURE_2D, texture);
		GLES.glTexImage2D(GL11.GL_TEXTURE_2D, 0, desc.color.internalFormat, desc.width, desc.height, 0, desc.color.format, desc.color.type, (ByteBuffer)null);

		GLES.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, desc.color.wrap);
		GLES.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, desc.color.wrap);
		GLES.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, desc.color.filter);
		GLES.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, desc.color.filter);

        return texture;
	}
	
	public int createDepthTexture2D(Desc desc){
		int texture = GLES.glGenTextures();
		GLES.glBindTexture(GL11.GL_TEXTURE_2D, texture);
		GLES.glTexImage2D(GL11.GL_TEXTURE_2D, 0, desc.depth.internalFormat, desc.width, desc.height, 0, desc.depth.format, desc.depth.type, null);

		GLES.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, desc.depth.wrap);
		GLES.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, desc.depth.wrap);
		GLES.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, desc.depth.filter);
		GLES.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, desc.depth.filter);

        return texture;
	}

	/**
	 * Texture format information class.
	 * Used for creating the textures upon which the FBOs are based
	 */
	public static final class TextureDesc{
		/** GL internal format value (e.g. GL_RGBA8). Default value is 0 */
		public int internalFormat;
		/** GL format value (e.g. GL_RGBA). Default value is 0 */
		public int format;
		/** GL type value (e.g. GL_UNSIGNED_BYTE). Default value is 0 */
		public int type;
		/** GL texture coordinate mapping. Default value is GL_CLAMP_TO_EDGE. */
		public int wrap = GL11.GL_CLAMP_TO_EDGE;
		/** GL texture filtering method, applied to min and mag. Default value is GL_NEAREST) */
		public int filter = GL11.GL_NEAREST;
	}
	
	/**
	 * Framebuffer description class.
	 * Used to represent the color and depth textures as well as the size
	 */
	public static final class Desc{
		/** width in pixels */
		public int width;
		/** height in pixels */
		public int height;
		/** color texture descriptor (format should be 0 if no color texture desired) */
		public final TextureDesc color = new TextureDesc();
		/** depth texture descriptor (format should be 0 if no depth texture desired) */
		public final TextureDesc depth = new TextureDesc();
	}
}
