package jet.learning.opengl.samples;

import android.opengl.GLES20;
import android.util.Log;

import com.nvidia.developer.opengl.app.NvPointerEvent;
import com.nvidia.developer.opengl.app.NvSampleApp;
import com.nvidia.developer.opengl.utils.BufferUtils;
import com.nvidia.developer.opengl.utils.FieldControl;
import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.NvGLSLProgram;
import com.nvidia.developer.opengl.utils.NvImage;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL11;

public final class ComputeBasicGLSL extends NvSampleApp{

	private NvGLSLProgram m_blitProg;
	private NvImage m_sourceImage;
	private int m_sourceTexture;
	private int m_filterLocation;
	private boolean m_enableFilter = true;
	private float m_aspectRatio = 1.0f;
	
	private FloatBuffer vertexBuffer;
	private FloatBuffer texCoordBuffer;
	
	@Override
	public void initUI() {
		if(mTweakBar != null)
			mTweakBar.addValue("Enable filter", new FieldControl(this, "m_enableFilter", FieldControl.CALL_FIELD), false, 0);
		
		Log.i("OpenGL ES", "mUIWindow " + mUIWindow.getScreenRect());
		Log.i("OpenGL ES", "mFPSText " + mFPSText.getScreenRect());
	}
	
	@Override
	protected void initRendering() {
		// test shader
		NvGLSLProgram test_shader = NvGLSLProgram.createFromFiles("shaders/simple_v_t2.glvs", "shaders/simple_v_t2.glfs");
		test_shader.dispose();

		Log.i("OpenGL ES", "test_shader have no problems!");

		vertexBuffer = BufferUtils.createFloatBuffer(8);
		texCoordBuffer = BufferUtils.createFloatBuffer(8);
		//init shaders
	    m_blitProg = NvGLSLProgram.createFromFiles("shaders/plain.vert", "shaders/plain.frag");
	    m_filterLocation = m_blitProg.getUniformLocation("m_enableFilter");

//	    m_computeProg = new NvGLSLProgram();
//	    NvGLSLProgram.ShaderSourceItem[] sources = new NvGLSLProgram.ShaderSourceItem[1];
//	    sources[0] = new NvGLSLProgram.ShaderSourceItem();
//	    sources[0].type = GL43.GL_COMPUTE_SHADER;
//	    sources[0].src = NvAssetLoader.readText("shaders/invert.glsl");
//	    m_computeProg.setSourceFromStrings(sources, 1);

	    //load input texture 
	    m_sourceImage = NvImage.createFromDDSFile("textures/flower1024.dds");
	    m_sourceTexture = m_sourceImage.updaloadTexture();
	    GLES.checkGLError();
	    
	    GLES20.glBindTexture(GL11.GL_TEXTURE_2D, m_sourceTexture);
	    GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
	    GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
	    GLES20.glBindTexture(GL11.GL_TEXTURE_2D, 0);

	    //create output texture with same size and format as input 
//	    int w = m_sourceImage.getWidth();
//	    int h = m_sourceImage.getHeight();
//	    int intFormat = m_sourceImage.getInternalFormat();
//	    int format = m_sourceImage.getFormat();
//	    int type = m_sourceImage.getType();

//	    m_resultTexture = GLES.glGenTextures( );
//	    GLES20.glBindTexture(GL11.GL_TEXTURE_2D, m_resultTexture);
//	    GLES20.glTexImage2D(GL11.GL_TEXTURE_2D, 0, intFormat, w, h, 0, format, type, null);
//	    GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
//	    GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
	    
	    GLES20.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	    
	    setTitle("ComputeGLSL");
	}
	
	@Override
	protected void reshape(int width, int height) {
		GLES20.glViewport(0, 0, width, height);
		m_aspectRatio = (float)height/(float)width;
	}
	
//	private void runComputeFilter(int inputTex, int outputTex, int width, int height){
//		GL20.glUseProgram( m_computeProg.getProgram() );
//
//	    GL42.glBindImageTexture(0, inputTex, 0, false, 0, GL15.GL_READ_ONLY, GL11.GL_RGBA8); 
//	    GL42.glBindImageTexture(1, outputTex, 0, false, 0, GL15.GL_WRITE_ONLY, GL11.GL_RGBA8);
//
//	    GL43.glDispatchCompute(width/WORKGROUP_SIZE, height/WORKGROUP_SIZE, 1);
//
//	    GL42.glMemoryBarrier(GL42.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT); 
//	}
	
	private void drawImage(int texture){
		final float vertexPosition[] = { 
	        m_aspectRatio, -1.0f, 
	        -m_aspectRatio, -1.0f,
	        m_aspectRatio, 1.0f, 
	        -m_aspectRatio, 1.0f};

		final float textureCoord[] = { 
	        1.0f, 0.0f, 
	        0.0f, 0.0f, 
	        1.0f, 1.0f, 
	        0.0f, 1.0f };

//		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
//		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		GLES20.glClearColor(0.2f, 0.0f, 0.2f, 1.0f);
		GLES20.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		GLES20.glUseProgram(m_blitProg.getProgram());
		m_blitProg.setUniform1i(m_filterLocation, m_enableFilter ? 1 : 0);

		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GL11.GL_TEXTURE_2D, texture);

		GLES20.glUniform1i(m_blitProg.getUniformLocation("uSourceTex"), 0);
	    int aPosCoord = m_blitProg.getAttribLocation("aPosition");
	    int aTexCoord = m_blitProg.getAttribLocation("aTexCoord");

	    vertexBuffer.put(vertexPosition).flip();
	    texCoordBuffer.put(textureCoord).flip();
	    
	    GLES20.glVertexAttribPointer(aPosCoord, 2, GL11.GL_FLOAT,false, 0, vertexBuffer);
	    GLES20.glVertexAttribPointer(aTexCoord, 2, GL11.GL_FLOAT,false, 0, texCoordBuffer);
	    GLES20.glEnableVertexAttribArray(aPosCoord);
	    GLES20.glEnableVertexAttribArray(aTexCoord);

	    GLES20.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
	    
	    GLES20.glDisableVertexAttribArray(aPosCoord);
	    GLES20.glDisableVertexAttribArray(aTexCoord);
	}
	
	@Override
	public boolean handlePointerInput(int device, int action, int modifiers,
			int count, NvPointerEvent[] points) {
		if(count > 0){
			NvPointerEvent e = points[0];
			Log.i("OpenGL ES", "touch pointer: " + e.m_x + ", " + e.m_y);
		}
		
		return false;
	}
	
	@Override
	public void draw() {
//		if(m_enableFilter){
//	        runComputeFilter(m_sourceTexture, m_resultTexture, m_sourceImage.getWidth(), m_sourceImage.getHeight());
//	        drawImage(m_resultTexture);
//
//	    }else{
	        drawImage(m_sourceTexture);
//	    }
	}
}
