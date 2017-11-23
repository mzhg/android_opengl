package jet.learning.opengl.common;

import android.opengl.GLES20;
import android.opengl.GLES30;

import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.GLUtil;
import com.nvidia.developer.opengl.utils.Glut;
import com.nvidia.developer.opengl.utils.NvGLSLProgram;
import com.nvidia.developer.opengl.utils.NvUtils;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL11;

//***************************************************************************************
//Ssao.java by Frank Luna (C) 2011 All Rights Reserved.
//
//Class encapsulates data and methods to perform screen space ambient occlusion.
//***************************************************************************************
public class Ssao {

	private static final int FBO_DEPTH = 0;
	private static final int FBO_COLOR = 1;
	
	private static final int POSITION = 0;
	private static final int PLANE_INDEX = 1;
	private static final int TEXCOORD = 2;
	
	private static final Matrix4f NDC_TRANSFORM = new Matrix4f();
	
	static{
		NDC_TRANSFORM.m00 = 0.5f;
		NDC_TRANSFORM.m11 = -0.5f;
		NDC_TRANSFORM.m30 = 0.5f;
		NDC_TRANSFORM.m31 = 0.5f;
	}
	
	int mScreenQuadVB;
	int mScreenQuadIB;
	int mScreenQuadVAO;
	
	int mRandomVectorSRV;
	
	final int[] mFBOs = new int[2];
	int mDepthSRV;
	int mNormalDepthSRV;
	int mAmbientSRV0;
	int mAmbientSRV1;
	
	int mRenderTargetWidth;
	int mRenderTargetHeight;
	
	int mViewportWidth;
	int mViewportHeight;

	final Vector4f mFrustumFarCorner[] = new Vector4f[4];
	final Vector4f mOffsets[] = new Vector4f[14];
	final Matrix4f mWVP = new Matrix4f();
	
	final SsaoProgram ssaoFX = new SsaoProgram();
	final SsaoBlurProgram ssaoBlurFX = new SsaoBlurProgram();
	
	public Ssao(int width, int height, float fovy, float farZ) {
		for(int i = 0; i < mFrustumFarCorner.length; i++)
			mFrustumFarCorner[i] = new Vector4f();
		
		for(int i = 0; i < mOffsets.length; i++)
			mOffsets[i] = new Vector4f();
		
		ssaoFX.init();
		ssaoBlurFX.init();
		
		onSize(width, height, fovy, farZ);
		
		buildFullScreenQuad();
		buildOffsetVectors();
		buildRandomVectorTexture();
	}
	
	public int normalDepthSRV() {	return mNormalDepthSRV; }
	public int ambientSRV() {	return mAmbientSRV0; }
	public int depthSRV() { return mDepthSRV; }
	
    public void onSize(int width, int height, float fovy, float farZ)
	{
		mRenderTargetWidth = width;
		mRenderTargetHeight = height;

		// We render to ambient map at half the resolution.
//		mAmbientMapViewport.TopLeftX = 0.0f;
//		mAmbientMapViewport.TopLeftY = 0.0f;
//		mAmbientMapViewport.Width = width / 2.0f;
//		mAmbientMapViewport.Height = height / 2.0f;
//		mAmbientMapViewport.MinDepth = 0.0f;
//		mAmbientMapViewport.MaxDepth = 1.0f;
		mViewportWidth = width / 2;
		mViewportHeight = height / 2;
		
		buildFrustumFarCorners(fovy, farZ);
	    buildTextureViews();
	}
    
    public void setNormalDepthRenderTarget(){
    	GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, mFBOs[FBO_DEPTH]);
		GLES30.glViewport(0, 0, mRenderTargetWidth, mRenderTargetHeight);
		GLES.glDrawBuffer(GLES30.GL_COLOR_ATTACHMENT0);
    	
    	// Clear view space normal to (0,0,-1) and clear depth to be very far away.  
		GLES30.glClearBufferfv(GLES30.GL_COLOR, 0, GLUtil.wrap(0, 0, 1f, -1e4f));
		GLES30.glClearBufferfv(GLES30.GL_DEPTH, 0, GLUtil.wrap(1f));
    }
    
    public int getFBO() { return mFBOs[0]; }
    
    public void computeSsao(Matrix4f proj){
    	// Bind the ambient map as the render target.  Observe that this pass does not bind 
    	// a depth/stencil buffer--it does not need it, and without one, no depth test is
    	// performed, which is what we want.
		GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, mFBOs[FBO_COLOR]);
    	GLES.glDrawBuffer(GLES30.GL_COLOR_ATTACHMENT0);
		GLES30.glViewport(0, 0, mViewportWidth, mViewportHeight);
		GLES30.glClearBufferfv(GLES30.GL_COLOR, 0, GLUtil.wrap(0f,0,0,1));
		GLES30.glDisable(GL11.GL_DEPTH_TEST);
		GLES30.glDisable(GL11.GL_CULL_FACE);
    	
    	ssaoFX.enable();
    	ssaoFX.setOffsetVectors(mOffsets);
    	ssaoFX.setFrustumCorners(mFrustumFarCorner);
    	ssaoFX.setNormalDepthMap(mNormalDepthSRV);
    	ssaoFX.setRandomVecMap(mRandomVectorSRV);
    	ssaoFX.setViewToTexSpace(Matrix4f.mul(NDC_TRANSFORM, proj, mWVP));

		GLES30.glBindVertexArray(mScreenQuadVAO);
		GLES30.glDrawElements(GL11.GL_TRIANGLES, 6, GL11.GL_UNSIGNED_SHORT, 0);

		GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
		GLES30.glViewport(0, 0, mRenderTargetWidth, mRenderTargetHeight);
		GLES30.glEnable(GL11.GL_DEPTH_TEST);
    }
    
    public void blurAmbientMap(int count){
//    	GL11.glPushAttrib(GL11.GL_VIEWPORT_BIT);
		GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, mFBOs[FBO_COLOR]);
		GLES30.glViewport(0, 0, mViewportWidth, mViewportHeight);
    	
    	ssaoBlurFX.enable();
    	ssaoBlurFX.setTexelWidth(1.0f/mViewportWidth);
    	ssaoBlurFX.setTexelHeight(1.0f/mViewportHeight);
    	ssaoBlurFX.setNormalDepthMap(mNormalDepthSRV);
		GLES30.glDisable(GL11.GL_DEPTH_TEST);
		GLES30.glDepthMask(false);

		GLES30.glBindVertexArray(mScreenQuadVAO);
    	
    	for(int i = 0; i < count; i++){
    		// Ping-pong the two ambient map textures as we apply
    		// horizontal and vertical blur passes.
    		blurAmbientMap(mAmbientSRV0, 1, true);
    		blurAmbientMap(mAmbientSRV1, 0, false);
    	}

		GLES30.glBindVertexArray(0);
		GLES30.glUseProgram(0);
		GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
		GLES30.glEnable(GL11.GL_DEPTH_TEST);
		GLES30.glDepthMask(true);
//    	GL11.glPopAttrib();
    }
    
    private void blurAmbientMap(int inputSRV, int outputRTV, boolean horzBlur){
    	GLES.glDrawBuffer(GLES30.GL_COLOR_ATTACHMENT0 + outputRTV);
//    	GLES30.glClearBuffer(GL11.GL_COLOR, outputRTV, Framework.wrap(0, 0, 0, 1));
    	
    	ssaoBlurFX.setInputImage(inputSRV);
    	
    	if(horzBlur) {
//			ssaoBlurFX.useHorizontal();  TODO
		}else {
//			ssaoBlurFX.useVertical();  TODO
		}

		GLES30.glDrawElements(GL11.GL_TRIANGLES, 6, GL11.GL_UNSIGNED_SHORT, 0);
    	// Unbind the input SRV as it is going to be an output in the next blur.
    	ssaoBlurFX.setInputImage(0);
    }
    
    private void buildFrustumFarCorners(float fovy, float farZ){
    	float aspect = (float)mRenderTargetWidth / (float)mRenderTargetHeight;

    	float halfHeight = (float) (farZ * Math.tan( 0.5f*fovy ));
    	float halfWidth  = aspect * halfHeight;

    	mFrustumFarCorner[0].set(-halfWidth, -halfHeight, farZ, 0.0f);
    	mFrustumFarCorner[1].set(-halfWidth, +halfHeight, farZ, 0.0f);
    	mFrustumFarCorner[2].set(+halfWidth, +halfHeight, farZ, 0.0f);
    	mFrustumFarCorner[3].set(+halfWidth, -halfHeight, farZ, 0.0f);
    }
    
    private void buildFullScreenQuad(){
    	if(mScreenQuadVAO != 0)
    		return;
    	
    	MeshData quad = new MeshData();
		GeometryGenerator geoGen = new GeometryGenerator();
		geoGen.createFullscreenQuad(quad);
		
		//
		// Extract the vertex elements we are interested in and pack the
		// vertices of all the meshes into one vertex buffer.
		//
		
		FloatBuffer vertices = GLUtil.getCachedFloatBuffer(quad.vertices.size() * 8);
		for(int i = 0; i < quad.vertices.size(); i++){
			Vertex v = quad.vertices.get(i);
			
			vertices.put(v.positionX).put(v.positionY).put(v.positionZ);
			vertices.put(i).put(0).put(0);
			vertices.put(v.texCX).put(v.texCY);
		}
		vertices.flip();
		
		mScreenQuadVB = GLES.glGenBuffers();
		GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mScreenQuadVB);
		GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vertices.remaining() * 4, vertices, GLES30.GL_STATIC_DRAW);
		GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
		
		mScreenQuadIB = GLES.glGenBuffers();
		GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, mScreenQuadIB);
		GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER, quad.getIndiceCount()*2, GLUtil.wrap(quad.indices.getData(), 0, quad.getIndiceCount()), GLES30.GL_STATIC_DRAW);
		GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0);
		
		mScreenQuadVAO = GLES.glGenVertexArrays();
		GLES30.glBindVertexArray(mScreenQuadVAO);
		{
			GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mScreenQuadVB);
			GLES30.glVertexAttribPointer(POSITION, 3, GL11.GL_FLOAT, false, 8 * 4, 0);
			GLES30.glVertexAttribPointer(PLANE_INDEX, 3, GLES30.GL_UNSIGNED_INT, false, 8 * 4, 3 * 4);
			GLES30.glVertexAttribPointer(TEXCOORD, 2, GL11.GL_FLOAT, false, 8 * 4, 6 * 4);

			GLES30.glEnableVertexAttribArray(POSITION);
			GLES30.glEnableVertexAttribArray(PLANE_INDEX);
			GLES30.glEnableVertexAttribArray(TEXCOORD);

			GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, mScreenQuadIB);
		}
		GLES30.glBindVertexArray(0);
    }
    
    void buildTextureViews(){
    	if(mNormalDepthSRV != 0) GLES.glDeleteTextures(mNormalDepthSRV);
    	if(mAmbientSRV0 != 0) GLES.glDeleteTextures(mAmbientSRV0);
    	if(mAmbientSRV1 != 0) GLES.glDeleteTextures(mAmbientSRV1);
    	if(mDepthSRV != 0) GLES.glDeleteTextures(mDepthSRV);
    	
    	if(mFBOs[FBO_COLOR] != 0) GLES.glDeleteFramebuffers(mFBOs[FBO_COLOR]);
    	if(mFBOs[FBO_DEPTH] != 0) GLES.glDeleteFramebuffers(mFBOs[FBO_DEPTH]);
    	
    	mNormalDepthSRV = GLES.glGenTextures();
		GLES30.glBindTexture(GL11.GL_TEXTURE_2D, mNormalDepthSRV);
		GLES30.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GLES30.GL_RGBA32F, mRenderTargetWidth, mRenderTargetHeight, 0, GL11.GL_RGBA, GL11.GL_FLOAT, (FloatBuffer)null);
		GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
		GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
		
    	mAmbientSRV0 = GLES.glGenTextures();
		GLES30.glBindTexture(GL11.GL_TEXTURE_2D, mAmbientSRV0);
		GLES30.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GLES30.GL_RGBA32F, mRenderTargetWidth/2, mRenderTargetHeight/2, 0, GL11.GL_RGBA, GL11.GL_FLOAT, (FloatBuffer)null);
		GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
		GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
		
    	mAmbientSRV1 = GLES.glGenTextures();
		GLES30.glBindTexture(GL11.GL_TEXTURE_2D, mAmbientSRV1);
		GLES30.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GLES30.GL_RGBA32F, mRenderTargetWidth/2, mRenderTargetHeight/2, 0, GL11.GL_RGBA, GL11.GL_FLOAT, (FloatBuffer)null);
		GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
		GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
		
		int depthFormat;
		int depthBits = GLES.glGetInteger(GL11.GL_DEPTH_BITS);
		if(depthBits == 16){
			depthFormat = GLES30.GL_DEPTH_COMPONENT16;
		}else if(depthBits == 24){
			depthFormat = GLES30.GL_DEPTH_COMPONENT24;
		}else if(depthBits == 32){
			depthFormat = GLES30.GL_DEPTH_COMPONENT32F;
		}else{
			depthFormat = GLES30.GL_DEPTH_COMPONENT;
			System.out.println("unkown depth bit: " + depthBits);
		}
		System.out.println("Depth bit: " + depthBits);
//		mDepthSRV = GLES30.glGenRenderbuffers();
//		GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, mDepthSRV);
//		GLES30.glRenderbufferStorage(GLES30.GL_RENDERBUFFER, depthFormat, mRenderTargetWidth, mRenderTargetHeight);
		
		mDepthSRV = GLES.glGenTextures();
		GLES30.glBindTexture(GL11.GL_TEXTURE_2D, mDepthSRV);
		GLES30.glTexImage2D(GL11.GL_TEXTURE_2D, 0, depthFormat, mRenderTargetWidth, mRenderTargetHeight, 0, GLES30.GL_DEPTH_COMPONENT,GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
		GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
		GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
//		GLES30.glTexParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_BORDER_COLOR, Framework.wrap(0, 0, 0, 0));
		GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GLES30.GL_TEXTURE_COMPARE_MODE, GLES30.GL_COMPARE_REF_TO_TEXTURE);
		GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GLES30.GL_TEXTURE_COMPARE_FUNC, GL11.GL_LESS);

		GLES30.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    	
    	mFBOs[FBO_COLOR] = GLES.glGenFramebuffers();
    	GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, mFBOs[FBO_COLOR]);
    	GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, mAmbientSRV0, 0);
    	GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT1, GL11.GL_TEXTURE_2D, mAmbientSRV1, 0);
//    	GLES30.glFramebufferRenderbuffer(GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_ATTACHMENT, GLES30.GL_RENDERBUFFER, mDepthSRV);
    	
    	mFBOs[FBO_DEPTH] = GLES.glGenFramebuffers();
    	GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, mFBOs[FBO_DEPTH]);
    	GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, mNormalDepthSRV, 0);
    	GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D, mDepthSRV, 0);
    	
    	FrameBufferObject.checkFramebufferStatus(mFBOs[FBO_COLOR]);
		FrameBufferObject.checkFramebufferStatus(mFBOs[FBO_DEPTH]);
    }
    
    void buildRandomVectorTexture(){
    	if(mRandomVectorSRV != 0) GLES.glDeleteTextures(mRandomVectorSRV);
    	ByteBuffer color = GLUtil.getCachedByteBuffer(256 * 256 * 4);
    	for(int i = 0; i < 256; ++i)
    	{
    		for(int j = 0; j < 256; ++j)
    		{
//    			XMFLOAT3 v(MathHelper::RandF(), MathHelper::RandF(), MathHelper::RandF());

//    			color[i*256+j] = XMCOLOR(v.x, v.y, v.z, 0.0f);
//    			color.put((float)Math.random()).put((float)Math.random()).put((float)Math.random()).put(0);
    			color.put((byte)(256.0 * Math.random()));
    			color.put((byte)(256.0 * Math.random()));
    			color.put((byte)(256.0 * Math.random()));
    			color.put((byte)0);
    		}
    	}
    	color.flip();
    	
    	mRandomVectorSRV = GLES.glGenTextures();
		GLES30.glBindTexture(GL11.GL_TEXTURE_2D, mRandomVectorSRV);
		GLES30.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GLES30.GL_RGBA8, 256, 256, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, color);
		GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
		GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
		GLES30.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }
    
    void buildOffsetVectors(){
    	// Start with 14 uniformly distributed vectors.  We choose the 8 corners of the cube
    	// and the 6 center points along each cube face.  We always alternate the points on 
    	// opposites sides of the cubes.  This way we still get the vectors spread out even
    	// if we choose to use less than 14 samples.
    	
    	// 8 cube corners
    	mOffsets[0].set(+1.0f, +1.0f, +1.0f, 0.0f);
    	mOffsets[1].set(-1.0f, -1.0f, -1.0f, 0.0f);

    	mOffsets[2].set(-1.0f, +1.0f, +1.0f, 0.0f);
    	mOffsets[3].set(+1.0f, -1.0f, -1.0f, 0.0f);

    	mOffsets[4].set(+1.0f, +1.0f, -1.0f, 0.0f);
    	mOffsets[5].set(-1.0f, -1.0f, +1.0f, 0.0f);

    	mOffsets[6].set(-1.0f, +1.0f, -1.0f, 0.0f);
    	mOffsets[7].set(+1.0f, -1.0f, +1.0f, 0.0f);

    	// 6 centers of cube faces
    	mOffsets[8].set(-1.0f, 0.0f, 0.0f, 0.0f);
    	mOffsets[9].set(+1.0f, 0.0f, 0.0f, 0.0f);

    	mOffsets[10].set(0.0f, -1.0f, 0.0f, 0.0f);
    	mOffsets[11].set(0.0f, +1.0f, 0.0f, 0.0f);

    	mOffsets[12].set(0.0f, 0.0f, -1.0f, 0.0f);
    	mOffsets[13].set(0.0f, 0.0f, +1.0f, 0.0f);
    	
    	for(int i = 0; i < 14; ++i)
    	{
    		// Create random lengths in [0.25, 1.0].
    		float s = NvUtils.random(0.25f, 1.0f);
    		
//    		XMVECTOR v = s * XMVector4Normalize(XMLoadFloat4(&mOffsets[i]));
//    		XMStoreFloat4(&mOffsets[i], v);
			mOffsets[i].normalise();
    		mOffsets[i].scale(s);
    	}
    }
    
    private static final class SsaoProgram{
    	int mProgram;
    	
    	int mFrustumCorners;
    	int mViewToTexSpace;
    	int mOffsetVectors;
    	int mOcclusionRadius;
    	int mOcclusionFadeStart;
    	int mOcclusionFadeEnd;
    	int mSurfaceEpsilon;
    	
    	int mNormalDepthMap;
    	int mRandomVecMap;
    	
    	int mSampleCount;
    	
    	void init(){
			CharSequence vs_str = Glut.loadTextFromClassPath(CubeSky.class, "ssao.glvs");
			CharSequence fs_str = Glut.loadTextFromClassPath(CubeSky.class, "ssao.glfs");
			NvGLSLProgram program = NvGLSLProgram.createFromStrings(vs_str, fs_str);
//    		mProgram = Framework.linkProgramFromSource(Glut.loadTextFromClassPath(Sky.class, "ssao.glvs"), Glut.loadTextFromClassPath(Sky.class, "ssao.glfs"));
    		mProgram = program.getProgram();

    		mFrustumCorners = GLES20.glGetUniformLocation(mProgram, "gFrustumCorners");
    		mViewToTexSpace = GLES20.glGetUniformLocation(mProgram, "gViewToTexSpace");
    		mOffsetVectors = GLES20.glGetUniformLocation(mProgram, "gOffsetVectors");
    		mOcclusionRadius = GLES20.glGetUniformLocation(mProgram, "gOcclusionRadius");
    		mOcclusionFadeStart = GLES20.glGetUniformLocation(mProgram, "gOcclusionFadeStart");
    		mOcclusionFadeEnd = GLES20.glGetUniformLocation(mProgram, "gOcclusionFadeEnd");
    		mSurfaceEpsilon = GLES20.glGetUniformLocation(mProgram, "gSurfaceEpsilon");
    		
    		mNormalDepthMap = GLES20.glGetUniformLocation(mProgram, "gNormalDepthMap");
    		mRandomVecMap = GLES20.glGetUniformLocation(mProgram, "gRandomVecMap");
    		
    		mSampleCount = GLES20.glGetUniformLocation(mProgram, "gSampleCount");

			GLES20.glUseProgram(mProgram);
			GLES20.glUniform1i(mNormalDepthMap, 1);
			GLES20.glUniform1i(mRandomVecMap, 0);
			GLES20.glUniform1i(mSampleCount, 14);
			GLES20.glUseProgram(0);
    	}
    	
    	void setViewToTexSpace(Matrix4f m)      { GLES20.glUniformMatrix4fv(mViewToTexSpace, 1, false, GLUtil.wrap(m));}
    	void setOffsetVectors(Vector4f[] v)     { GLES20.glUniform4fv(mOffsetVectors, v.length, GLUtil.wrap(v)); }
    	void setFrustumCorners(Vector4f[] v)    { GLES20.glUniform4fv(mFrustumCorners,v.length, GLUtil.wrap(v)); }
    	void setOcclusionRadius(float f)        { GLES20.glUniform1f(mOcclusionRadius, f); }
    	void setOcclusionFadeStart(float f)     { GLES20.glUniform1f(mOcclusionFadeStart, f); }
    	void setOcclusionFadeEnd(float f)       { GLES20.glUniform1f(mOcclusionFadeEnd, f); }
    	void setSurfaceEpsilon(float f)         { GLES20.glUniform1f(mSurfaceEpsilon, f); }
    	
    	void enable() {GLES30.glUseProgram(mProgram);}
    	
    	void setNormalDepthMap(int srv){
			GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
			GLES30.glBindTexture(GL11.GL_TEXTURE_2D, srv);
			GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
			GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
//			GLES30.glTexParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_BORDER_COLOR, Framework.wrap(0.0f, 0.0f, 0.0f, 1e5f));
    	}
    	
    	void setRandomVecMap(int srv){
			GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
			GLES30.glBindTexture(GL11.GL_TEXTURE_2D, srv);
    	}
    }
    
    private static final class SsaoBlurProgram{
    	int mProgram;
    	
    	int mNormalDepthMap;
    	int mInputImage;
    	
    	int mTexelWidth;
    	int mTexelHeight;
    	
    	int mVertical;
    	int mHorizontal;
    	
    	void init(){
			CharSequence vs_str = Glut.loadTextFromClassPath(CubeSky.class, "ssao.glvs");
			CharSequence fs_str = Glut.loadTextFromClassPath(CubeSky.class, "ssao.glfs");
			NvGLSLProgram program = NvGLSLProgram.createFromStrings(vs_str, fs_str);
//    		mProgram = Framework.linkProgramFromSource(Glut.loadTextFromClassPath(Sky.class, "ssaoblur.glvs"), Glut.loadTextFromClassPath(Sky.class, "ssaoblur.glfs"));
    		mProgram = program.getProgram();

    		mNormalDepthMap = GLES30.glGetUniformLocation(mProgram, "gNormalDepthMap");
    		mInputImage     = GLES30.glGetUniformLocation(mProgram, "gInputImage");
    		
    		mTexelWidth = GLES30.glGetUniformLocation(mProgram, "gTexelWidth");
    		mTexelHeight = GLES30.glGetUniformLocation(mProgram, "gTexelHeight");

//    		mHorizontal = GLES30.glGetSubroutineIndex(mProgram, GL20.GL_FRAGMENT_SHADER, "horizontalBlur");  TODO ES doesn't support the subroutine
//    		mVertical = GLES30.glGetSubroutineIndex(mProgram, GL20.GL_FRAGMENT_SHADER, "verticalBlur");
			GLES30.glUseProgram(mProgram);
			GLES30.glUniform1i(mNormalDepthMap, 1);
			GLES30.glUniform1i(mInputImage, 0);
			GLES30.glUseProgram(0);
    	}
    	
    	void enable() {GLES30.glUseProgram(mProgram);}
    	
    	void setTexelWidth(float f)    { GLES30.glUniform1f(mTexelWidth, f); }
    	void setTexelHeight(float f)   { GLES30.glUniform1f(mTexelHeight, f); }
    	
//    	void useVertical() { GLES30.glUniformSubroutinesu(GLES30.GL_FRAGMENT_SHADER, GLUtil.wrap(mVertical));}
//    	void useHorizontal() { GLES30.glUniformSubroutinesu(GLES30.GL_FRAGMENT_SHADER, GLUtil.wrap(mHorizontal));}

    	void setNormalDepthMap(int texture)  {
			GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
			GLES30.glBindTexture(GL11.GL_TEXTURE_2D, texture);

			GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
			GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
    	}
    	
	    void setInputImage(int texture){
			GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
			GLES30.glBindTexture(GL11.GL_TEXTURE_2D, texture);
	    }
    }
}
