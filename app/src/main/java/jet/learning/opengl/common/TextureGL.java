package jet.learning.opengl.common;

import android.opengl.GLES20;

public abstract class TextureGL implements Disposeable {

	String name;  // Only for debugging.
	int textureID;
	int target;
	
	int format;
    int mipLevels;

    // If the texture created from the RenderTexturePool, the below variables will be used.
    int refCount = 0;
    boolean isCached = false;

    public final boolean isCached() { return isCached;}
	
    public TextureGL() {}
	public TextureGL(String name) {this.name = name;}
    
    public final int getTexture() { return textureID;}
    public abstract int getWidth();
    public int getHeight()  { return 1;}
    public int getDepth()   { return 1;}
    public final int getFormat()  { return format;}
    public int getMipLevels() { return mipLevels;}
    public final int getTarget()  { return target; }
    public int getSampleCount() { return 1;}
	public void setName(String name) {this.name = name;}
	public String getName() { return name;}

	public long computeMemorySize(){
//    	return textureID == 0 ? 0 : TextureUtils.getTextureMemorySize(target, textureID, 0, mipLevels);
		return 0;
    }

	@Override
	public void dispose() {
		if(textureID != 0){
			TextureUtils.glDeleteTextures(textureID);
			textureID = 0;
		}
	}
	
	public boolean isValid(){
		return textureID != 0 && GLES20.glIsTexture(textureID);
	}
	
	/**
     * Set the minification property for the texture.<P>
     * @param minFilter
     */
    public void setMinFilter(int minFilter){
		GLES20.glTexParameteri(target, GLES20.GL_TEXTURE_MIN_FILTER, minFilter);
    }
    
    /**
     * Set the magnification property for the texture.<P>
     * @param magFilter
     */
    public void setMagFilter(int magFilter){
		GLES20.glTexParameteri(target, GLES20.GL_TEXTURE_MAG_FILTER, magFilter);
    }
    
    /**
     * Sets the wrap parameter for texture coordinate s.<p>
     * @param mode
     */
    public void setWrapS(int mode){
		GLES20.glTexParameteri(target, GLES20.GL_TEXTURE_WRAP_S, mode);
    }
    
    /**
     * Sets the wrap parameter for texture coordinate t.<p>
     * @param mode
     */
    public void setWrapT(int mode){
		GLES20.glTexParameteri(target, GLES20.GL_TEXTURE_WRAP_T, mode);
    }
	
}
