////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2017 mzhg
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
import org.lwjgl.util.vector.Vector4f;

import java.nio.FloatBuffer;

public class SpotLight {
	public static final int FLOAT_SIZE = 24;
	public static final int BYTE_SIZE = 96;
	
	public final Vector4f ambient = new Vector4f();
	public final Vector4f diffuse = new Vector4f();
	public final Vector4f specular = new Vector4f();
	
	public final Vector3f position = new Vector3f();
	public float range;
	
	public final Vector3f direction = new Vector3f();
	public float spot;
	
	public final Vector3f att = new Vector3f();
	public float pad; // Pad the last float so we can set an array of lights if we wanted.
	
	public void store(FloatBuffer buf){
		ambient.store(buf);
		diffuse.store(buf);
		specular.store(buf);
		position.store(buf);
		buf.put(range);
		direction.store(buf);
		buf.put(spot);
		att.store(buf);
		buf.put(pad);
	}
}
