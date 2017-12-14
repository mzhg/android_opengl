package jet.learning.opengl.common;

import org.lwjgl.util.vector.Vector3f;

/**
 * Represents a bounding sphere.<p></p>
 * Created by mazhen'gui on 2017/12/14.
 */
public class BSphere {
    private Vector3f center = new Vector3f();
    private float radius;
    private float radSq;

    /**
     * Default constructor creates a sphere with center (0, 0, 0) and radius 0
     */
    public BSphere() {
        makeEmpty();
    }

    public BSphere(Vector3f center, float radius) {
        set(center, radius);
    }

    /** Re-initialize this sphere to center (0, 0, 0) and radius 0 */
    public void makeEmpty() {
        center.set(0, 0, 0);
        radius = radSq = 0;
    }

    public void setCenter(Vector3f center) {
        this.center.set(center);
    }

    public void setCenter(float x, float y, float z) {
        center.set(x, y, z);
    }

    public Vector3f getCenter() {
        return center;
    }

    public void setRadius(float radius) {
        this.radius = radius;
        radSq = radius * radius;
    }

    public float getRadius() {
        return radius;
    }

    public void set(Vector3f center, float radius) {
        setCenter(center);
        setRadius(radius);
    }

    /** Returns radius and mutates passed "center" vector */
    float get(Vector3f center) {
        center.set(this.center);
        return radius;
    }
}
