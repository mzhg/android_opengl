package jet.learning.opengl.gui_vr;

import org.lwjgl.util.vector.Vector3f;

/**
 * Created by mazhen'gui on 2017/4/15.
 */

public abstract class BoundingBox {
    protected final Vector3f m_center = new Vector3f();
    private Object m_userData;

    public abstract boolean testWithRay(Ray ray, float[] t, int offset);

    public Object getUserData() {
        return m_userData;
    }

    public void setUserData(Object userData) {
        this.m_userData = userData;
    }
}
