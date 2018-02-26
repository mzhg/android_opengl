////////////////////////////////////////////////////////////////////////////////
// Copyright by Frank Luna (C) 2011 All Rights Reserved.
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

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import java.nio.FloatBuffer;

public class PosNormalTexTanSkinned {
	
	public static final int FLOAT_COUNT = 19;

	public final Vector3f pos = new Vector3f();
	public final Vector3f normal = new Vector3f();
	public final Vector2f tex = new Vector2f();
	public final Vector4f tangentU = new Vector4f();
	public final Vector3f weights = new Vector3f();
	public final byte[] boneIndices = new byte[4];
	
	public void store(FloatBuffer buf){
		pos.store(buf);
		normal.store(buf);
		tex.store(buf);
		tangentU.store(buf);
		weights.store(buf);
		
		buf.put(Float.intBitsToFloat(boneIndices[0]));
		buf.put(Float.intBitsToFloat(boneIndices[1]));
		buf.put(Float.intBitsToFloat(boneIndices[2]));
		buf.put(Float.intBitsToFloat(boneIndices[3]));
		
//		for(int i = 0; i < 4; i++)
//			buf.put(boneIndices[i]);
	}
}
