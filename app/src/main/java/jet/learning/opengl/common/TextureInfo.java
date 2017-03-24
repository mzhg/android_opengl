package jet.learning.opengl.common;

import android.opengl.GLES30;

public class TextureInfo {

	/** The internal format of the texture. If the texture is depth texture, the value of the internalFormat must be one of the enums declared in {@link FrameBufferObject} */
	int internalFormat = GLES30.GL_RGBA8;
	boolean isRenderBuffer;
	
	int minFilter = GLES30.GL_LINEAR;
	int magFilter = GLES30.GL_LINEAR;

	int swrap = GLES30.GL_CLAMP_TO_EDGE;
	int twrap = GLES30.GL_CLAMP_TO_EDGE;
	/** The variable used for depth comparing. */
	boolean compareToRed = false;
	
	public int getInternalFormat() {	return internalFormat;}

	public int getMinFilter() {
		return minFilter;
	}

	public int getMagFilter() {
		return magFilter;
	}

	public int getSWrap() {
		return swrap;
	}

	public int getTWrap() {
		return twrap;
	}

	public TextureInfo setMinFilter(int minFilter) {
		this.minFilter = minFilter;
		return this;
	}

	public TextureInfo setMagFilter(int magFilter) {
		this.magFilter = magFilter;
		return this;
	}

	public TextureInfo setSWrap(int swrap) {
		this.swrap = swrap;
		return this;
	}

	public TextureInfo setTWrap(int twrap) {
		this.twrap = twrap;
		return this;
	}
	
	public TextureInfo setInternalFormat(int format) {
		this.internalFormat = format;
		return this;
	}
	
	public boolean isCompareToRed() { return compareToRed;}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + internalFormat;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
//		if (obj == null)
//			return false;
//		if (getClass() != obj.getClass())
//			return false;
		TextureInfo other = (TextureInfo) obj;
		if (internalFormat != other.internalFormat)
			return false;
		return true;
	}

	public boolean isRenderBuffer() {	return isRenderBuffer;}

	public TextureInfo setRenderBuffer(boolean isRenderBuffer) {
		this.isRenderBuffer = isRenderBuffer;
		return this;
	}
	
	public TextureInfo setCompareToRed(boolean flag) {
		this.compareToRed = flag;
		return this;
	}
}
