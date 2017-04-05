package jet.learning.opengl.common;

import org.lwjgl.util.vector.Vector3f;

/**
 * Created by mazhen'gui on 2017/4/5.<p>
 *
 * Wraps a 3D point and parametric time value.
 */

public class IntersectionPoint {
    private Vector3f intPt = new Vector3f();
    private float t;

    public Vector3f getIntersectionPoint() {
        return intPt;
    }

    public void setIntersectionPoint(Vector3f newPt) {
        intPt.set(newPt);
    }

    public float getT() {
        return t;
    }

    public void setT(float t) {
        this.t = t;
    }
}
