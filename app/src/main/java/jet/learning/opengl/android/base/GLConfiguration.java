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
package jet.learning.opengl.android.base;

public class GLConfiguration {
	
	public static final int GLES1 = 0;
	public static final int GLES2 = 1;
	public static final int GLES3_0 = 2;
	public static final int GLES3_1 = 3;
	public static final int GLES3_2 = 4;

	public int redBits = 8;
	public int greenBits = 8;
	public int blueBits = 8;
	public int alphaBits = 0;
	
	public int depthBits = 24;
	public int stencilBits = 0;
	
	public int version = 2;
	
	public boolean checkGLError = true;
	public boolean logGLCallInfo = false;
	
	public boolean continueRender = true;
	
	public boolean isGLES2(){
		return version >= GLES2;
	}
	
	@Override
	public String toString() {
		return "redBits:" + redBits + "\n"
			  +"greenBits: " + greenBits +"\n"
			  +"blueBits: " + blueBits + "\n"
			  +"alphaBits: " + alphaBits + "\n"
			  +"depthBits: " + depthBits + "\n"
			  +"stencilBits: " + stencilBits;
 	}
}
