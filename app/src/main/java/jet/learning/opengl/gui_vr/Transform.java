package jet.learning.opengl.gui_vr;

import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;

/**
 * Created by mazhen'gui on 2017/4/15.
 */

public class Transform {
    public final Vector3f position = new Vector3f();
    public final Quaternion rotation = new Quaternion();
    public final Vector3f scale = new Vector3f();

    public void set(Transform t){
        position.set(t.position);
        rotation.set(t.rotation);
        scale.set(t.scale);
    }
}
