package jet.learning.opengl.common;

import java.util.ArrayList;
import java.util.List;

public class FrameBufferBuilder {

	/** The width of the framebuffer */
	private int width;
	/** The height of the framebuffer. */
	private int height;
	
	private final List<TextureInfo> colors = new ArrayList<TextureInfo>();
	private TextureInfo depth;  // the depth texture.
	
	public FrameBufferBuilder() {
	}
	
	public FrameBufferBuilder setWidth(int width){
		this.width = width;
		return this;
	}
	
	public FrameBufferBuilder setHeight(int height){
		this.height = height;
		return this;
	}
	
	public FrameBufferBuilder setDimension(int width, int height){
		this.width = width;
		this.height = height;
		
		return this;
	}
	
	public boolean equals(int width, int height){
		return this.width == width && this.height == height;
	}
	
	public int getWidth()  { return width;}
	public int getHeight() { return height;}
	
	public TextureInfo createColorTexture(){
		TextureInfo texture = new TextureInfo();
		colors.add(texture);
		return texture;
	}
	
	public List<TextureInfo> getColorTextures() {
		return colors;
	}
	
	public TextureInfo getOrCreateDepthTexture(){
		if(depth == null)
			depth = new TextureInfo();
		
		return depth;
	}
	
	public TextureInfo getDepthTexture(){
		return depth;
	}
	
	public void clearDepthTexture(){
		depth = null;
	}
}
