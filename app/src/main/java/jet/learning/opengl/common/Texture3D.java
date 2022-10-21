package jet.learning.opengl.common;

public class Texture3D extends TextureGL{
	
	int width;
	int height;
	int depth;

	public Texture3D() {
		super("Texture3D");
	}

	public Texture3D(String name) {
		super(name);
	}

	@Override
	public final int getWidth() {return width;}
	public final int getHeight() { return height;}
	public final int getDepth()  { return depth;}

	public Texture3DDesc getDesc() { return getDesc(null);}

	public Texture3DDesc getDesc(Texture3DDesc out){
		if(out == null)
			out = new Texture3DDesc();
		out.format = format;
		out.height = height;
		out.mipLevels = getMipLevels();
		out.width = width;
		out.depth = depth;

		return out;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(80);
		sb.append(name).append(' ').append('[');
		sb.append("textureID: ").append(textureID).append(',').append(' ');
		sb.append("target: ").append(TextureUtils.getTextureTargetName(target)).append(',').append(' ');
		sb.append("width = ").append(width).append(',').append(' ');
		sb.append("height = ").append(height).append(',').append(' ');
		sb.append("depth = ").append(depth).append(',').append(' ');
		sb.append("format = ").append(TextureUtils.getFormatName(format)).append(',').append(' ');
		sb.append("mipLevels = ").append(mipLevels).append(',').append(' ');

		return sb.toString();
	}
}
