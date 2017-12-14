package jet.learning.opengl.common;

import android.opengl.GLES30;

import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.GLUtil;

import javax.microedition.khronos.opengles.GL11;

public class ShadowMap {

	int mWidth;
	int mHeight;
	
	int mFBO;
	int mDepthMapSRV;
	
	public void init(int width, int height){
		mWidth = width;
		mHeight = height;
		
		mDepthMapSRV = GLES.glGenTextures();
		GLES30.glBindTexture(GL11.GL_TEXTURE_2D, mDepthMapSRV);
		GLES30.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GLES30.GL_DEPTH_COMPONENT16, mWidth, mHeight, 0, GLES30.GL_DEPTH_COMPONENT,GL11.GL_UNSIGNED_SHORT, null);
		GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
		GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
		GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GLES30.GL_TEXTURE_COMPARE_MODE, GLES30.GL_COMPARE_REF_TO_TEXTURE);
		GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GLES30.GL_TEXTURE_COMPARE_FUNC, GL11.GL_LESS);
		GLES.checkGLError();

		// Assign the depth buffer texture to texture channel 0

		// Create and set up the FBO
		mFBO = GLES.glGenFramebuffers();
		GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, mFBO);
		GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D, mDepthMapSRV, 0);
		GLES.checkFrameBufferStatus();

		GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
		GLES30.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}
	
	public int depthMapSRV() { return mDepthMapSRV; }
	
	public void bindDsvAndSetNullRenderTarget(){
		GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, mFBO);
		GLES30.glClearBufferfv(GLES30.GL_DEPTH, 0, GLUtil.wrap(1.0f));
		GLES30.glViewport(0, 0, mWidth, mHeight);
		GLES.glDrawBuffer(GLES30.GL_NONE);
	}
}
