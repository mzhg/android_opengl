////////////////////////////////////////////////////////////////////////////////
// Copyright by mzhg (c) 2017
//
// Licensed under the Apache License, Version 2.0 (the "License"); you may not
// use this file except in compliance with the License.  You may obtain a copy
// of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
// License for the specific language governing permissions and limitations
// under the License.
////////////////////////////////////////////////////////////////////////////////
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
