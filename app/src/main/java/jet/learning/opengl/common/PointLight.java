package jet.learning.opengl.common;

import java.nio.FloatBuffer;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class PointLight {

	public static final int FLOAT_SIZE = 20;
	public static final int BYTE_SIZE = 80;
	
	public final Vector4f ambient = new Vector4f();
	public final Vector4f diffuse = new Vector4f();
	public final Vector4f specular = new Vector4f();
	
	public final Vector3f position = new Vector3f();
	public float range;
	
	public final Vector3f att = new Vector3f();
	public float pad; // Pad the last float so we can set an array of lights if we wanted.
	
	public void store(FloatBuffer buf){
		ambient.store(buf);
		diffuse.store(buf);
		specular.store(buf);
		position.store(buf);
		buf.put(range);
		att.store(buf);
		buf.put(pad);
	}
}
