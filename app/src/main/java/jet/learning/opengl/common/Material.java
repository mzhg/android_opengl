package jet.learning.opengl.common;

import java.nio.FloatBuffer;

import org.lwjgl.util.vector.Vector4f;

public class Material {

	public static final int FLOAT_SIZE = 16;
	public static final int BYTE_SIZE = 64;
	
	public final Vector4f ambient = new Vector4f();
	public final Vector4f diffuse = new Vector4f();
	public final Vector4f specular = new Vector4f();
	public final Vector4f reflect = new Vector4f();
	
	public void set(Material m){
		ambient.set(m.ambient);
		diffuse.set(m.diffuse);
		specular.set(m.specular);
		reflect.set(m.reflect);
	}
	
	public void store(FloatBuffer buf){
		ambient.store(buf);
		diffuse.store(buf);
		specular.store(buf);
		reflect.store(buf);
	}
}
