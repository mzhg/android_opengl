package jet.learning.opengl.shapes;

import android.opengl.GLES11;

import com.nvidia.developer.opengl.utils.GLES;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

/** This class represent a OpenGL buffer object. */
public class GLBuffer {

	int index;  // used for GLVAO.
	int target;
	int usage;
	
	int bufferID;
	long bufferSize;
	
	private ByteBuffer bufferData;
	
	public GLBuffer(int target, int usage) {
		this.target = target;
		this.usage = usage;
	}
	
	public void bind(){
		GLES11.glBindBuffer(target, bufferID);
	}
	
	public void unbind() { GLES11.glBindBuffer(target, 0);}
	
	public void load(Buffer buffer){
		boolean binded = false;
		if(bufferID == 0){
			bufferID = GLES.glGenBuffers();
			GLES11.glBindBuffer(target, bufferID);
			binded = true;
		}

		if(buffer instanceof ByteBuffer){
			ByteBuffer buf = (ByteBuffer)buffer;
			bufferSize = buf.remaining();
		}else if(buffer instanceof FloatBuffer){
			FloatBuffer buf = (FloatBuffer)buffer;
			bufferSize = (buf.remaining() << 2);
		}else if(buffer instanceof IntBuffer){
			IntBuffer buf = (IntBuffer)buffer;
			bufferSize = (buf.remaining() << 2);
		}else if(buffer instanceof ShortBuffer){
			ShortBuffer buf = (ShortBuffer)buffer;
			bufferSize = (buf.remaining() << 1);
		}else if(buffer instanceof LongBuffer){
			LongBuffer buf = (LongBuffer)buffer;
			bufferSize = (buf.remaining() << 3);
		}else if(buffer instanceof DoubleBuffer){
			DoubleBuffer buf = (DoubleBuffer)buffer;
			bufferSize = (buf.remaining() << 3);
		}else {
			throw new IllegalArgumentException("Unkown Supported buffer type: " + buffer.getClass().getName());
		}

		GLES11.glBufferData(target, (int)bufferSize, buffer, usage);

		if(binded)
			GLES11.glBindBuffer(target, 0);  // unbind the buffer.
		
		GLES.checkGLError();
	}
	
	public void load(long size){
		if(bufferID == 0){
			bufferID = GLES.glGenBuffers();
			GLES11.glBindBuffer(target, bufferID);
		}

		bufferSize = size;
		GLES11.glBufferData(target, (int )size, null, usage);
		GLES.checkGLError();
	}

	public void update(int offset, int bufferSize, Buffer data){
		GLES11.glBufferSubData(target, offset, bufferSize, data);
	}
	
	/* Create the mapping buffer for preparing update */
//	public void beginUpdate(int access){
//		bufferData = (ByteBuffer) GLES31.glMapBufferRange(target, 0, (int)bufferSize, access);
//	}
	
	public ByteBuffer getMappingBuffer() { return bufferData;}
	
//	public void finishUpdate(){
//		GLES31.glUnmapBuffer(target);
//	}
	
	public void dispose(){
		GLES.glDeleteBuffers(bufferID);
	}
	
	public long getBufferSize() { return bufferSize;}
}
