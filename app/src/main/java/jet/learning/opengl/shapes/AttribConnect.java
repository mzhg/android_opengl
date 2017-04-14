package jet.learning.opengl.shapes;

import android.opengl.GLES11;
import android.opengl.GLES30;

import com.nvidia.developer.opengl.utils.NvLogger;

import javax.microedition.khronos.opengles.GL11;

interface AttribConnect {

	void enable(int index, int size, int type, int stride, int offset, int divisor);
	
	void disable(int index);
	
	static final AttribConnect VERTEX_ATTRIB = new AttribConnect() {
		public void enable(int index, int size, int type, int stride, int offset, int divisor) {
			GLES30.glVertexAttribPointer(index, size, type, false, stride, offset);
			GLES30.glEnableVertexAttribArray(index);
			GLES30.glVertexAttribDivisor(index, divisor);
		}
		
		@Override
		public void disable(int index) {
			GLES30.glDisableVertexAttribArray(index);
			GLES30.glVertexAttribDivisor(index, 0);
		}
	};
	
	static final AttribConnect VERTEX_POINTER = new AttribConnect() {
		public void enable(int index, int size, int type, int stride, int offset, int divisor) {
			GLES11.glVertexPointer(size, type, stride, offset);
			GLES11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
//			GLES11.glVertexAttribDivisor(index, divisor);
			if(divisor != 0){
				NvLogger.e("OpenGL ES 1.x doesn't support the instance attrib!");
			}
		}

		
		@Override
		public void disable(int index) {
			GLES11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
//			GL33.glVertexAttribDivisor(index, 0);
		}
	};
	
	static final AttribConnect NORMAL_POINTER = new AttribConnect() {
		public void enable(int index, int size, int type, int stride, int offset, int divisor) {
			if(size != 3)
				throw new IllegalArgumentException("The normal size must be 3.");
			GLES11.glNormalPointer(type, stride, offset);
			GLES11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
//			GL33.glVertexAttribDivisor(index, divisor);

			if(divisor != 0){
				NvLogger.e("OpenGL ES 1.x doesn't support the instance attrib!");
			}
		}

		@Override
		public void disable(int index) {
			GLES11.glDisableClientState(GL11.GL_NORMAL_ARRAY);
//			GL33.glVertexAttribDivisor(index, 0);
		}
	};
	
	static final AttribConnect COLOR_POINTER = new AttribConnect() {
		public void enable(int index, int size, int type, int stride, int offset, int divisor) {
			if(size != 3)
				throw new IllegalArgumentException("The normal size must be 3.");
			GLES11.glColorPointer(size, type, stride, offset);
			GLES11.glEnableClientState(GL11.GL_COLOR_ARRAY);
//			GL33.glVertexAttribDivisor(index, divisor);

			if(divisor != 0){
				NvLogger.e("OpenGL ES 1.x doesn't support the instance attrib!");
			}
		}

		@Override
		public void disable(int index) {
			GLES11.glDisableClientState(GL11.GL_COLOR_ARRAY);
//			GL33.glVertexAttribDivisor(index, 0);
		}
	};
	
	static final AttribConnect TEXTURE_POINTER = new AttribConnect() {
		
		@Override
		public void enable(int index, int size, int type, int stride, int offset, int divisor) {
			index -= Model.TYPE_TEXTURE0;
			if(index < 0)
				throw new IllegalArgumentException("Invalid index = "  + index);
			GLES11.glClientActiveTexture(GLES11.GL_TEXTURE0 + index);
			GLES11.glTexCoordPointer(size, type, stride, offset);
			GLES11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
//			GL33.glVertexAttribDivisor(index, divisor);

			if(divisor != 0){
				NvLogger.e("OpenGL ES 1.x doesn't support the instance attrib!");
			}
		}
		
		@Override
		public void disable(int index) {
			index -= Model.TYPE_TEXTURE0;
			if(index < 0)
				throw new IllegalArgumentException("Invalid index = "  + index);
//			GLES11.glClientActiveTexture(GLES11.GL_TEXTURE0 + index);
			GLES11.glDisableClientState(GLES11.GL_TEXTURE_COORD_ARRAY);
//			GL33.glVertexAttribDivisor(index, 0);
		}
	};
}
