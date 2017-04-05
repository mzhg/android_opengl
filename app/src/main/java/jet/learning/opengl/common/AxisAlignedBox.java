package jet.learning.opengl.common;

import org.lwjgl.util.vector.Vector3f;

public class AxisAlignedBox {

	/** Center of the box. */
	public final Vector3f center = new Vector3f();
	/** Distance from the center to each side. */
	public final Vector3f extents = new Vector3f();
	
	public void set(AxisAlignedBox other){
		center.set(other.center);
		extents.set(other.extents);
	}
}
