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

import org.lwjgl.util.vector.Vector4f;

import java.nio.FloatBuffer;

public class Material {

	public static final int FLOAT_SIZE = 16;
	public static final int BYTE_SIZE = 64;
	
	public final Vector4f ambient = new Vector4f();
	public final Vector4f diffuse = new Vector4f();
	public final Vector4f specular = new Vector4f();
	public final Vector4f reflect = new Vector4f();
	
	public void set(Material m){
		ambient.set(m.ambient);
		diffuse.set(m.diffuse);
		specular.set(m.specular);
		reflect.set(m.reflect);
	}
	
	public void store(FloatBuffer buf){
		ambient.store(buf);
		diffuse.store(buf);
		specular.store(buf);
		reflect.store(buf);
	}
}
