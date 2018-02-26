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
