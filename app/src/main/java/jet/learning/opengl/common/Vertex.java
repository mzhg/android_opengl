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

import java.nio.FloatBuffer;

public class Vertex {

	public static final int FLOAT_COUNT = 11;
	
	public float positionX, positionY, positionZ;
	public float normalX, normalY, normalZ;
	public float tangentUX, tangentUY, tangentUZ;
	public float texCX, texCY;
	
	public Vertex() {
	}
	
	public Vertex(float px, float py, float pz, 
			float nx, float ny, float nz, 
			float tx, float ty, float tz, 
			float u, float v) {
		this.positionX = px;
		this.positionY = py;
		this.positionZ = pz;
		this.normalX = nx;
		this.normalY = ny;
		this.normalZ = nz;
		this.tangentUX = tx;
		this.tangentUY = ty;
		this.tangentUZ = tz;
		this.texCX = u;
		this.texCY = v;
	}
	
	public void setPosition(float px, float py, float pz){
		this.positionX = px;
		this.positionY = py;
		this.positionZ = pz;
	}
	
	public void setNormal(float nx, float ny, float nz){
		this.normalX = nx;
		this.normalY = ny;
		this.normalZ = nz;
	}
	
	public void setTangentU(float tx, float ty, float tz){
		this.tangentUX = tx;
		this.tangentUY = ty;
		this.tangentUZ = tz;
	}
	
	public void setTexCoord(float u, float v){
		texCX = u;
		texCY = v;
	}
	
	public void store(FloatBuffer buf){
		buf.put(positionX).put(positionY).put(positionZ);
		buf.put(normalX).put(normalY).put(normalZ);
		buf.put(tangentUX).put(tangentUY).put(tangentUZ);
		buf.put(texCX).put(texCY);
	}
	
	public void load(FloatBuffer buf){
		this.positionX = buf.get();
		this.positionY = buf.get();
		this.positionZ = buf.get();
		this.normalX = buf.get();
		this.normalY = buf.get();
		this.normalZ = buf.get();
		this.tangentUX = buf.get();
		this.tangentUY = buf.get();
		this.tangentUZ = buf.get();
		this.texCX = buf.get();
		this.texCY = buf.get();
	}
	
}
