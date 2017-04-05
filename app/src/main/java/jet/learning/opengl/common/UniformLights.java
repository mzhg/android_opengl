package jet.learning.opengl.common;

import android.opengl.GLES30;

import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.GLUtil;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class UniformLights {

	public final DirectionalLight[] gDirLights = new DirectionalLight[3];
	public final Vector3f gEyePosW = new Vector3f();
	public int gLightCount = 3;

	public float  gFogStart;
	public float  gFogRange;
	public final Vector4f gFogColor = new Vector4f();
	
	int byteSize;
	int uniformIndex;
	int bufferID;
	
	
	int dirLightsOffset;
	int eyePosWOffset;
	int lightCountOffset;
	int fogStartOffset;
	int fogRangeOffset;
	int fogColorOffset;
	
	public UniformLights() {
		for(int i = 0;i < gDirLights.length; i++)
			gDirLights[i] = new DirectionalLight();
	}
	
	public void init(int program){
		uniformIndex = GLES30.glGetUniformBlockIndex(program, "cbPerFrame");
		byteSize = GLES.glGetActiveUniformBlocki(program, uniformIndex, GLES30.GL_UNIFORM_BLOCK_DATA_SIZE);
		
	    String[] names = {"gDirLights", "gEyePosW", "gLightCount", "gFogStart", "gFogRange", "gUseTexure", "gFogColor"};
	    int[] indices = new int[names.length];
	    IntBuffer _indices = GLUtil.getCachedIntBuffer(indices.length);
		GLES30.glGetUniformIndices(program, names, _indices);
	    _indices.get(indices);
	    
//	    dirLightsOffset 	= GL31.glGetActiveUniformsi(program, indices[0], GL31.GL_UNIFORM_OFFSET);
	    eyePosWOffset 		= GLES.glGetActiveUniformsi(program, indices[1], GLES30.GL_UNIFORM_OFFSET) >> 2;
	    lightCountOffset 	= GLES.glGetActiveUniformsi(program, indices[2], GLES30.GL_UNIFORM_OFFSET) >> 2;
	    fogStartOffset 		= GLES.glGetActiveUniformsi(program, indices[3], GLES30.GL_UNIFORM_OFFSET) >> 2;
	    fogRangeOffset 		= GLES.glGetActiveUniformsi(program, indices[4], GLES30.GL_UNIFORM_OFFSET) >> 2;
	    fogColorOffset 		= GLES.glGetActiveUniformsi(program, indices[6], GLES30.GL_UNIFORM_OFFSET) >> 2;
	    
	    bufferID = GLES.glGenBuffers();
		GLES30.glBindBuffer(GLES30.GL_UNIFORM_BUFFER, bufferID);
		GLES30.glBufferData(GLES30.GL_UNIFORM_BUFFER, byteSize, null, GLES30.GL_DYNAMIC_DRAW);
	}
	
	public void setDirectionLights(DirectionalLight[] lights){
		int len = Math.min(gDirLights.length, lights.length);
		for(int i = 0; i < len; i++)
			gDirLights[i].load(lights[i]);
	}
	
	public void apply(){
		FloatBuffer buffer = GLUtil.getCachedFloatBuffer(byteSize/4);
		
		buffer.position(dirLightsOffset);
		for(int i = 0; i < gDirLights.length; i++){
			gDirLights[i].store(buffer);
		}
		
		buffer.position(eyePosWOffset);  	gEyePosW.store(buffer);
		buffer.position(lightCountOffset);  buffer.put(Float.intBitsToFloat(gLightCount));
		buffer.position(fogStartOffset);  	buffer.put(gFogStart);
		buffer.position(fogRangeOffset);  	buffer.put(gFogRange);
		buffer.position(fogColorOffset);  	gFogColor.store(buffer);
		buffer.flip();

		GLES30.glBindBuffer(GLES30.GL_UNIFORM_BUFFER, bufferID);
		GLES30.glBufferSubData(GLES30.GL_UNIFORM_BUFFER, 0, byteSize, buffer);
		GLES30.glBindBufferBase(GLES30.GL_UNIFORM_BUFFER, uniformIndex, bufferID);
	}
}
