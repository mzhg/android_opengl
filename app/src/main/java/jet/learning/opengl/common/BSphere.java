/*
 * Copyright 2017 mzhg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
