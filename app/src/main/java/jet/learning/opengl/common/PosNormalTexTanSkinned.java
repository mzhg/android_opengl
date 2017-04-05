package jet.learning.opengl.common;

import java.nio.FloatBuffer;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class PosNormalTexTanSkinned {
	
	public static final int FLOAT_COUNT = 19;

	public final Vector3f pos = new Vector3f();
	public final Vector3f normal = new Vector3f();
	public final Vector2f tex = new Vector2f();
	public final Vector4f tangentU = new Vector4f();
	public final Vector3f weights = new Vector3f();
	public final byte[] boneIndices = new byte[4];
	
	public void store(FloatBuffer buf){
		pos.store(buf);
		normal.store(buf);
		tex.store(buf);
		tangentU.store(buf);
		weights.store(buf);
		
		buf.put(Float.intBitsToFloat(boneIndices[0]));
		buf.put(Float.intBitsToFloat(boneIndices[1]));
		buf.put(Float.intBitsToFloat(boneIndices[2]));
		buf.put(Float.intBitsToFloat(boneIndices[3]));
		
//		for(int i = 0; i < 4; i++)
//			buf.put(boneIndices[i]);
	}
}
