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
package com.nvidia.developer.opengl.ui;

import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.NvDisposeable;
import com.nvidia.developer.opengl.utils.NvImage;
import com.nvidia.developer.opengl.utils.NvLogger;
import com.nvidia.developer.opengl.utils.NvUtils;

import java.util.Arrays;

import javax.microedition.khronos.opengles.GL11;

/**
 * Abstraction of a GL texture object, allowing for texture caching, lookup, and reuse/refcounting.
 * In addition, it stores a set of 'knowledge' about the texture data loaded in the GL object,
 * including things like the source filename and dimensions.
 * @author Nvidia 2014-9-4
 */
public class NvUITexture implements NvDisposeable{
	
	/** Some random prime num picked for hashing. */
	private static final int NV_UITEX_HASHMAX = 19;
	/** The texture hash table.  Using chaining for filled slots. */
	private static NvUITexture[] ms_texTable = new NvUITexture[NV_UITEX_HASHMAX];

	/** Internally linked list. */
	private NvUITexture m_llnext;
	/** The requested bitmap filename, used in hashing and debugging. */
	protected String m_filename;
	/** The GL texture ID allocated for our texture upload. */
	protected int m_glID;
	/** The loaded texture width or height. */
	protected int m_width, m_height;
	/** Flag for whether this texture is a null object or was successfully loaded. */
	protected boolean m_validTex;
	/** Flag for if this texture has data with an alpha channel. */
	protected boolean m_hasAlpha;
	/** Flag if we own the GL texture ID/object, or if someone else does. */
	protected boolean m_ownsID;
	/** Trivial internal refcount of a given texture object. */ // !!!!TBD TODO use a real ref system? */
	protected int m_refcount = 1;
	/** Whether or not the texture is cached in our master NvUITexture table. */
	protected boolean m_cached;
	
	/** Constructor for texture loaded from filename; may load from/to the texture cache. */
	public NvUITexture(String texname){
		this(texname, true);
	}
	/** Constructor for texture loaded from filename; may load from/to the texture cache. */
	public NvUITexture(String texname, boolean noMips){
		m_filename = texname;
		if(NvUtils.isEmpty(m_filename)){
			throw new NullPointerException("NvUITexture:: Passed an empty texture filename");
		}
		
		// TODO !!!!!TBD we shouldn't be loading in constructor, bad.  need to revise.
	    m_glID = 0;
	    NvImage image = null;
	    if (null == (image = loadEmbeddedTexture(texname)))
	        image = NvImage.createFromDDSFile(texname);
	    if (image != null)
	    {
	        m_width = image.getWidth();
	        m_height = image.getHeight();
	        m_hasAlpha = image.hasAlpha();

	        m_glID = image.updaloadTexture();
	        //TestPrintGLError("Error 0x%x after texture upload...\n");

	        // set up any tweaks to texture state here...
	        GLES.glActiveTexture(GL11.GL_TEXTURE0);
	        GLES.glBindTexture(GL11.GL_TEXTURE_2D, m_glID);
	        GLES.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
	        GLES.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
	        GLES.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP_TO_EDGE);
	        GLES.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP_TO_EDGE);
	        // then turn the bind back off!
	        GLES.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	    }

	    if (m_glID==0) // !!!!!TBD
	    {
	        throw new RuntimeException("NvUITexture: FAILED to load texture file: " + m_filename);
	    }
	    else
	    {
	        m_ownsID = true; // our call created the gl tex ID.
	        // !!!!TBD TODO this was set to FALSE??
	        m_validTex = true; // as if we got here, we loaded a texture...
	    }
	}
	
	/**
	 * Constructor for texture loaded from existing GL tex ID; texture cache is not involved.
	 */
	public NvUITexture(int texID, boolean alpha, int srcw, int srch, boolean ownsID, boolean isCubeMap){
		m_glID = texID;
		m_width = srcw;
		m_height = srch;
		m_validTex = true;
		m_hasAlpha = alpha;
		m_ownsID = ownsID;
		m_refcount = 1;
		m_cached = false;
	}
	
	static void staticCleanup(){
		Arrays.fill(ms_texTable, null);
	}
	
	/** Accessor for texture width. */
    public int getWidth() { return m_width; };
    /** Accessor for texture height. */
    public int getHeight() { return m_height; };
    /** Accessor for GL texture object ID. */
    public int getGLTex() { return m_glID; };
    /** Accessor for whether texture was flagged as having alpha channel. */
    public boolean getHasAlpha() { return m_hasAlpha; };
    /** Add reference to this texture. */
    public void addRef() {m_refcount++;}
    
    /** Subtract a reference from this texture/ */
    public void delRef(){
    	if (m_refcount == 0)
        {
            NvLogger.i("NvUITexture refcount is already 0!");
            return;
        }
        m_refcount--;

        if (m_refcount==0)
        {
            if (m_cached)
            {
            	NvLogger.e("Refcount reached 0 for a texture flagged as cached!!!");
            }
            
            dispose();
        }
        else if (m_cached && m_refcount == 1)
        {
            // cached textures got 1 more refcount so calling
            // DerefTexture when the counter reaches 1 is correct
            derefTexture();
        }
    }
    
    /** Handles internal dereferencing of texture objects, may include removal of entry from cache. */
    private boolean derefTexture() {
    	if (m_refcount==0) // we shouldn't be here
            return false;

        m_refcount--;
        if (m_refcount>0) // okay, we're still alive
            return true;

        // otherwise, we need to pull from list and REALLY delete. 
        
        // find existing texture in table
        if (NvUtils.isEmpty(m_filename)) // then something created by passing in a gl ID
        { // !!!!TBD is this the right thing to do?  need to look at use-case.
            dispose();
            return true;
        }
        else // created by loading from filename, should be in list.
        {
            // calculate some hash based on the filename string.
            int namehash = calculateNameHash(m_filename);
            // now we have hash, look in table slot, walk ll
            NvUITexture list = ms_texTable[namehash], follow = null;
            while (list != null)
            {
                // !!!!!TBD this isn't case insensitive, and SHOULD BE!!!
                // also possible we need full paths in order to ensure uniqueness.
                if (m_filename.equals(list.m_filename))
                {
                    // pull out of list.
                    if (follow==null) // head of slot
                        ms_texTable[namehash] = list.m_llnext;
                    else
                        follow.m_llnext = list.m_llnext;
                    // now we can safely, REALLY delete    
                    list.dispose();
                    return true; // all done.
                }
                follow = list;
                list = list.m_llnext;
            }
        }

        return false;
	}
	/** Static method to help calculate a hash-table value based on a texture name string. */
    private static int calculateNameHash(String texname){
    	// calculate some hash based on the filename string.
        int namehash = 0;
        int namelen = texname.length();
        for (int i=0; i<namelen; i++)
            namehash += texname.charAt(i);
        namehash = namehash % NV_UITEX_HASHMAX;
        return namehash;
    }
    
    /** Static method for loading textures via internal cache.
    First tries to find existing object in the cache.  If not found in the cache, tries to
    load from disk, and if successful then store in the cache for later load attempts.
    */
    public static NvUITexture cacheTexture(String texname, boolean noMips){
    	NvUITexture tex = null;
        
    	   if (NvUtils.isEmpty(texname))
    	        throw new NullPointerException("texname is empty");

    	    // find existing texture in table
    	    // calculate some hash based on the filename string.
    	    int namehash = calculateNameHash(texname);
    	    // now we have hash, look in table slot, walk ll
    	    NvUITexture list = ms_texTable[namehash];
    	    while (list != null)
    	    {
    	        // !!!!!TBD this isn't case insensitive, and SHOULD BE!!!
    	        // also possible we need full paths in order to ensure uniqueness.
    	        if (list.m_filename.equals(texname))
    	        {
    	            tex = list;
    	            break;
    	        }
    	        list = list.m_llnext;
    	    }
    	    
    	    // if not exist, create a new one
    	    if (tex==null)
    	    {
    	        tex = new NvUITexture(texname, noMips);
    	        if (!tex.m_filename.equals(texname))
    	        {
    	            tex.delRef();
    	            return null;
    	        }
    	            
    	        tex.m_cached = true;
    	        // then use calc'd hash info from above to insert into table.
    	        list = ms_texTable[namehash]; // get current head.
    	        tex.m_llnext = list; // move in front.
    	        ms_texTable[namehash] = tex; // take over head position.
    	    }

    	    tex.m_refcount++; // increment regardless of cached or allocated.
    	    return tex;
    }
    
    private static NvImage loadEmbeddedTexture(String filename){
    	byte[] data = NvUIAssetData.embeddedAssetLookup(filename);
    	
    	if(data != null){
    		NvImage image = new NvImage();
    		if(!image.loadImageFromFileData(data, 0, data.length, "dds")){
    			return null;
    		}
    		
    		return image;
    	}
    	
    	return null;
    }
    
    /** Dispose the GL texture associated to the NvUITexture. */
	@Override
	public void dispose() {
		// refcount owned outside of us...
	    if (0==m_refcount)
	    {
	        if (m_glID != 0 && m_ownsID)
	        { // if this is bound, we could be in bad place... !!!TBD
	        	GLES.glDeleteTextures(m_glID);
	        }
	        m_glID = 0;        
	    }
	    else
	    {
	        NvLogger.e("NvUITexture refcount is not 0!\n");
	    }
	}
}
