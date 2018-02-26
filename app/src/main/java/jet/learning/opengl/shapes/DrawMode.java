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

import javax.microedition.khronos.opengles.GL11;

public enum DrawMode {

	FILL(GL11.GL_TRIANGLES),
	
	LINE(GL11.GL_LINES),
	
	POINT(GL11.GL_POINTS);
	
	final int drawMode;
	
	private DrawMode(int drawMode) {
		this.drawMode = drawMode;
	}
	
	/** Get the correspond OpenGL draw command.*/
	public int getGLMode() { return drawMode;}
}
