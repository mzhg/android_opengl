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
