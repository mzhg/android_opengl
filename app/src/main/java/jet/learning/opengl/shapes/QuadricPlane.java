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
package jet.learning.opengl.shapes;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class QuadricPlane implements QuadricGenerator{

	private float xsize, zsize;
	public QuadricPlane(){
		this(1,1);
	}
	
	public QuadricPlane(float xsize, float zsize){
		this.xsize = xsize;
		this.zsize = zsize;
	}
	
	@Override
	public void genVertex(float x, float y, Vector3f position, Vector3f normal, Vector2f texCoord, Vector4f color) {
		position.set(x * xsize, 0, y * zsize);
		if(normal != null)
			normal.set(0, 1, 0);
		if(texCoord != null)
			texCoord.set(x, y);
	}

}
