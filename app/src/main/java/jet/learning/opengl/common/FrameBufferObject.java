package jet.learning.opengl.common;

import android.opengl.GLES30;

import com.nvidia.developer.opengl.utils.GLES;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.List;

import javax.microedition.khronos.opengles.GL11;

public class FrameBufferObject {

	public static final int 
	FBO_DepthBufferType_NONE=0,
	FBO_DepthBufferType_RENDERTARGET=1,						//24bits depth buffer
	FBO_DepthBufferType_TEXTURE=2,							//24bits depth buffer
	FBO_DepthBufferType_TEXTURE_PACKED_DEPTH24_STENCIL8=3,	//24bits depth buffer + packed 8bits stencil (32bits per pixel)
	FBO_DepthBufferType_TEXTURE_DEPTH32F=4,					//32bits floating point depth buffer
	FBO_DepthBufferType_TEXTURE_PACKED_DEPTH32F_STENCIL8=5,	//32bits floating point depth buffer + packed 8bits stencil (64bits per pixel)
	FBO_DepthBufferType_TEXTURE_FROM_ANOTHER_FBO=6;			//link to this FBO the same dept buffer as another FBO passed in as a parameter
	
	/**
	 *	The OpenGL FBO ID
	 */
    int fbo;

	/**
	 *	the table containing all texture id of each render target
	 */
	int[] colorTextures;
	/**
	 *	the number of color attachement
	 */
	int nbColorAttachement;
	/**
	 *	The minification filtering for the color buffer textures
	 */
	int[] colorMinificationFiltering;

	/**
	 *	the depth texture/buffer id
	 */
	int depthID;
	/**
	 *	the depth type
	 */
	int depthType;
	/**
	 *	The minification filtering for the depth buffer texture
	 */
	int depthMinificationFiltering;

	/**
	 * the width in pixel
	 */
	int width;
	/**
	 * the height in pixel
	 */
	int height;

	/**
	 * indicate if the mipmap pyramid correspond to the color image (last render to texture were followed by a generateMipMap)
	 */
	boolean[] validColorMipMapPyramid;
	/**
	 * indicate if the mipmap pyramid correspond to the depth image (last render to texture were followed by a generateMipMap)
	 */
	boolean validDepthMipMapPyramid;
	
	public FrameBufferObject(FrameBufferBuilder builder) {
		List<TextureInfo> textureInfos = builder.getColorTextures();
		int width = builder.getWidth();
		int height= builder.getHeight();
		if(width == 0)
			throw new IllegalArgumentException("width can't be 0.");
		if(height == 0)
			throw new IllegalArgumentException("height can't be 0.");
		
		int nbColorBuffer = textureInfos.size();
		int[] colorBufferInternalFormat = null;
		int[] colorBufferSWRAP =null;
		int[] colorBufferTWRAP =null;
		int[] colorBufferMinFiltering = null;
		int[] colorBufferMagFiltering = null;
		
		if(nbColorBuffer > 0){
			colorBufferInternalFormat = new int[nbColorBuffer];
			colorBufferSWRAP = new int[nbColorBuffer];
			colorBufferTWRAP = new int[nbColorBuffer];
			colorBufferMinFiltering = new int[nbColorBuffer];
			colorBufferMagFiltering = new int[nbColorBuffer];
			
			for(int i = 0; i < nbColorBuffer; i++){
				TextureInfo color = textureInfos.get(i);
				colorBufferInternalFormat[i] = color.internalFormat;
				colorBufferSWRAP[i] = color.swrap;
				colorBufferTWRAP[i] = color.twrap;
				colorBufferMinFiltering[i] = color.minFilter;
				colorBufferMagFiltering[i] = color.magFilter;
			}
		}
		
		int depthBufferType = FBO_DepthBufferType_NONE;
		int depthBufferMinFiltering = 0;
		int depthBufferMagFiltering = 0;
		int depthBufferSWRAP = 0;
		int depthBufferTWRAP = 0;
		boolean depthTextureCompareToR = false;
		
		TextureInfo depth = builder.getDepthTexture();
		if(depth != null){
			depthBufferType = depth.internalFormat;
			depthBufferMinFiltering = depth.minFilter;
			depthBufferMagFiltering = depth.magFilter;
			depthBufferSWRAP = depth.swrap;
			depthBufferTWRAP = depth.twrap;
			depthTextureCompareToR = depth.compareToRed;
			
			if(depthBufferType < FBO_DepthBufferType_NONE || depthBufferType > FBO_DepthBufferType_TEXTURE_PACKED_DEPTH32F_STENCIL8){
				throw new IllegalArgumentException("Invalid depthBufferType: " + depthBufferType);
			}
		}
		
		init(width, height, nbColorBuffer, colorBufferInternalFormat, 
				colorBufferSWRAP, colorBufferTWRAP, colorBufferMinFiltering, 
				colorBufferMagFiltering, depthBufferType, depthBufferMinFiltering, 
				depthBufferMagFiltering, depthBufferSWRAP, depthBufferTWRAP, depthTextureCompareToR, null);
	}
	
	public FrameBufferObject( int width, int height, int nbColorBuffer, 
			int[] colorBufferInternalFormat, 
			int[] colorBufferSWRAP,
			int[] colorBufferTWRAP,
			int[] colorBufferMinFiltering,
			int[] colorBufferMagFiltering,
			int depthBufferType /*=FBO_DepthBufferType_NONE*/
//			int depthBufferMinFiltering/*=GL_LINEAR*/,
//			int depthBufferMagFiltering/*=GL_LINEAR*/,
//			int depthBufferSWRAP/*=GL_CLAMP*/,
//			int depthBufferTWRAP/*=GL_CLAMP*/,
//			boolean depthTextureCompareToR/*=false*/,
//			FrameBufferObject fboContainingDepthBuffer/*=NULL*/
			){
		this(width, height, nbColorBuffer, colorBufferInternalFormat, colorBufferSWRAP,colorBufferTWRAP,
				colorBufferMinFiltering, colorBufferMagFiltering, depthBufferType,
				GL11.GL_LINEAR, GL11.GL_LINEAR, GL11.GL_CLAMP_TO_EDGE, GL11.GL_CLAMP_TO_EDGE, false, null);
	}
	
	public FrameBufferObject( int width, int height, int nbColorBuffer, 
		int[] colorBufferInternalFormat, 
		int[] colorBufferSWRAP,
		int[] colorBufferTWRAP,
		int[] colorBufferMinFiltering,
		int[] colorBufferMagFiltering,
		int depthBufferType /*=FBO_DepthBufferType_NONE*/,
		int depthBufferMinFiltering/*=GL_LINEAR*/,
		int depthBufferMagFiltering/*=GL_LINEAR*/,
		int depthBufferSWRAP/*=GL_CLAMP*/,
		int depthBufferTWRAP/*=GL_CLAMP*/,
		boolean depthTextureCompareToR/*=false*/,
		FrameBufferObject fboContainingDepthBuffer/*=NULL*/){
		
		init(width, height, nbColorBuffer, colorBufferInternalFormat, colorBufferSWRAP, colorBufferTWRAP, colorBufferMinFiltering, colorBufferMagFiltering, depthBufferType, depthBufferMinFiltering, depthBufferMagFiltering, depthBufferSWRAP, depthBufferTWRAP, depthTextureCompareToR, fboContainingDepthBuffer);
	}
	
	private void init(int width, int height, int nbColorBuffer, 
		int[] colorBufferInternalFormat, 
		int[] colorBufferSWRAP,
		int[] colorBufferTWRAP,
		int[] colorBufferMinFiltering,
		int[] colorBufferMagFiltering,
		int depthBufferType /*=FBO_DepthBufferType_NONE*/,
		int depthBufferMinFiltering/*=GL_LINEAR*/,
		int depthBufferMagFiltering/*=GL_LINEAR*/,
		int depthBufferSWRAP/*=GL_CLAMP*/,
		int depthBufferTWRAP/*=GL_CLAMP*/,
		boolean depthTextureCompareToR/*=false*/,
		FrameBufferObject fboContainingDepthBuffer/*=NULL*/){
		
		this.width = width;
		this.height= height;
		this.nbColorAttachement = nbColorBuffer;
		if(this.nbColorAttachement>getMaxColorAttachments())
			this.nbColorAttachement = getMaxColorAttachments();

		/////////////////INITIALIZATION/////////////////

		//color render buffer
		if(this.nbColorAttachement>0)
		{
			this.colorTextures = new int[this.nbColorAttachement];
			this.colorMinificationFiltering = new int[this.nbColorAttachement];
			this.validColorMipMapPyramid = new boolean[this.nbColorAttachement];
			for(int i=0; i<this.nbColorAttachement; i++)
			{
				this.colorTextures[i]=0;
				colorTextures[i] = GLES.glGenTextures();
				GLES30.glBindTexture(GL11.GL_TEXTURE_2D, this.colorTextures[i]);
				GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, colorBufferMinFiltering[i]);
				this.colorMinificationFiltering[i] = colorBufferMinFiltering[i];
				GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, colorBufferMagFiltering[i]);
				GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, colorBufferSWRAP[i]);
				GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, colorBufferTWRAP[i]);

				GLES30.glTexImage2D(GL11.GL_TEXTURE_2D, 0, colorBufferInternalFormat[i], width, height, 0, measureFormat(colorBufferInternalFormat[i]), measureDataType(colorBufferInternalFormat[i]), (FloatBuffer)null);

				GLES.checkGLError("create color attachement" + i);
				this.validColorMipMapPyramid[i]=false;
				if(this.colorMinificationFiltering[i]==GL11.GL_NEAREST_MIPMAP_NEAREST
				|| this.colorMinificationFiltering[i]==GL11.GL_LINEAR_MIPMAP_NEAREST
				|| this.colorMinificationFiltering[i]==GL11.GL_NEAREST_MIPMAP_LINEAR
				|| this.colorMinificationFiltering[i]==GL11.GL_LINEAR_MIPMAP_LINEAR)
				{
					GLES30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
				}

				GLES.checkGLError("GenerateMipmap color attachement" + i);
			}
		}

		//depth render buffer
		this.depthType = depthBufferType;
		this.validDepthMipMapPyramid=false;
		if(this.depthType!=FBO_DepthBufferType_NONE)
		{
			this.depthID=0;
			switch(this.depthType)
			{
			case FBO_DepthBufferType_TEXTURE:
				depthID = GLES.glGenTextures();
				GLES30.glBindTexture(GL11.GL_TEXTURE_2D, this.depthID);
				this.depthMinificationFiltering = depthBufferMinFiltering;
				GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, depthBufferMinFiltering);
				GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, depthBufferMagFiltering);
				GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, depthBufferSWRAP);
				GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, depthBufferTWRAP);
				GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GLES30.GL_TEXTURE_COMPARE_MODE, GLES30.GL_COMPARE_REF_TO_TEXTURE);
				GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GLES30.GL_TEXTURE_COMPARE_FUNC, GL11.GL_LEQUAL);
				GLES30.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GLES30.GL_DEPTH_COMPONENT24, width, height, 0, GLES30.GL_DEPTH_COMPONENT, GLES30.GL_UNSIGNED_INT, (ByteBuffer)null);

				if(this.depthMinificationFiltering==GL11.GL_NEAREST_MIPMAP_NEAREST
				|| this.depthMinificationFiltering==GL11.GL_LINEAR_MIPMAP_NEAREST
				||this.depthMinificationFiltering==GL11.GL_NEAREST_MIPMAP_LINEAR
				|| this.depthMinificationFiltering==GL11.GL_LINEAR_MIPMAP_LINEAR)
				{
					GLES30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
				}
				break;
				
			case FBO_DepthBufferType_TEXTURE_DEPTH32F:
				depthID = GLES.glGenTextures();
				GLES30.glBindTexture(GL11.GL_TEXTURE_2D, this.depthID);
				if(this.depthMinificationFiltering==GL11.GL_NEAREST)	//FORCE MIPMAP DISABLED (is it supported??)
					GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
				else
				{
					this.depthMinificationFiltering = GL11.GL_LINEAR;
					GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
				}
				GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, depthBufferMagFiltering);
				GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, depthBufferSWRAP);
				GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, depthBufferTWRAP);
				if(depthTextureCompareToR)
					GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GLES30.GL_TEXTURE_COMPARE_MODE, GLES30.GL_COMPARE_REF_TO_TEXTURE);

				GLES30.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GLES30.GL_DEPTH_COMPONENT32F, width, height, 0, GLES30.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (FloatBuffer)null);
				break;
				
			case FBO_DepthBufferType_TEXTURE_PACKED_DEPTH24_STENCIL8:
				depthID = GLES.glGenTextures();
				GLES30.glBindTexture(GL11.GL_TEXTURE_2D, this.depthID);
				if(this.depthMinificationFiltering==GL11.GL_NEAREST)	//FORCE MIPMAP DISABLED
					GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
				else
				{
					this.depthMinificationFiltering = GL11.GL_LINEAR;
					GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
				}
				GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, depthBufferMagFiltering);
				GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, depthBufferSWRAP);
				GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, depthBufferTWRAP);
				if(depthTextureCompareToR)
					GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GLES30.GL_TEXTURE_COMPARE_MODE, GLES30.GL_COMPARE_REF_TO_TEXTURE);

				GLES30.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GLES30.GL_DEPTH24_STENCIL8, width, height, 0, GLES30.GL_DEPTH_STENCIL, GLES30.GL_UNSIGNED_INT_24_8, (ByteBuffer)null);
				break;
				
			case FBO_DepthBufferType_TEXTURE_PACKED_DEPTH32F_STENCIL8:
				depthID = GLES.glGenTextures();
				GLES.glBindTexture(GL11.GL_TEXTURE_2D, this.depthID);
				if(this.depthMinificationFiltering==GL11.GL_NEAREST)	//FORCE MIPMAP DISABLED
					GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
				else
				{
					this.depthMinificationFiltering = GL11.GL_LINEAR;
					GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
				}
				GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, depthBufferMagFiltering);
				GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, depthBufferSWRAP);
				GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, depthBufferTWRAP);
				if(depthTextureCompareToR)
					GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GLES30.GL_TEXTURE_COMPARE_MODE, GLES30.GL_COMPARE_REF_TO_TEXTURE);

				GLES30.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GLES30.GL_DEPTH32F_STENCIL8, width, height, 0, GLES30.GL_DEPTH_STENCIL, GLES30.GL_FLOAT_32_UNSIGNED_INT_24_8_REV, (FloatBuffer)null);
				break;

			case FBO_DepthBufferType_RENDERTARGET:
			default:
				depthID = GLES.glGenRenderbuffers();
				GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, this.depthID);
				GLES30.glRenderbufferStorage(GLES30.GL_RENDERBUFFER, GLES30.GL_DEPTH_COMPONENT24, width, height);
				break;

			case FBO_DepthBufferType_TEXTURE_FROM_ANOTHER_FBO:
				//nothing to create
				break;
			}
		}


		/////////////////ATTACHEMENT/////////////////
		fbo = GLES.glGenFramebuffers();
		GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER,this.fbo);

		//color render buffer attachement
		if(nbColorBuffer>0)
		{
			for(int i=0; i<this.nbColorAttachement; i++)
			{
				GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0+i, GL11.GL_TEXTURE_2D, this.colorTextures[i], 0 );
			}
			GLES30.glReadBuffer(GLES30.GL_COLOR_ATTACHMENT0);
			GLES.glDrawBuffer(GLES30.GL_COLOR_ATTACHMENT0);
		}
		else
		{
			GLES30.glReadBuffer(GLES30.GL_NONE);
			GLES.glDrawBuffer(GLES30.GL_NONE);
		}
		
		//depth render buffer attachement
		if(this.depthType!=FBO_DepthBufferType_NONE)
		{
			switch(this.depthType)
			{
			case FBO_DepthBufferType_RENDERTARGET:
			default:
				GLES30.glFramebufferRenderbuffer(GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_ATTACHMENT, GLES30.GL_RENDERBUFFER, this.depthID);
				break;

			case FBO_DepthBufferType_TEXTURE:
			case FBO_DepthBufferType_TEXTURE_DEPTH32F:
				GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D, this.depthID, 0);
				break;
				
			case FBO_DepthBufferType_TEXTURE_PACKED_DEPTH24_STENCIL8:
			case FBO_DepthBufferType_TEXTURE_PACKED_DEPTH32F_STENCIL8:
				GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D, this.depthID, 0);
				GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_STENCIL_ATTACHMENT, GL11.GL_TEXTURE_2D, this.depthID, 0);
				break;
				

			case FBO_DepthBufferType_TEXTURE_FROM_ANOTHER_FBO:
				switch(fboContainingDepthBuffer.depthType)
				{
				case FBO_DepthBufferType_RENDERTARGET:
					GLES30.glFramebufferRenderbuffer(GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_ATTACHMENT, GLES30.GL_RENDERBUFFER, fboContainingDepthBuffer.depthID);
					this.depthID=0;
					break;
				case FBO_DepthBufferType_TEXTURE:
				case FBO_DepthBufferType_TEXTURE_DEPTH32F:
					GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D, fboContainingDepthBuffer.depthID, 0);
					this.depthID=0;
					break;
				case FBO_DepthBufferType_TEXTURE_PACKED_DEPTH24_STENCIL8:
				case FBO_DepthBufferType_TEXTURE_PACKED_DEPTH32F_STENCIL8:
					GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D, fboContainingDepthBuffer.depthID, 0);
					GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_STENCIL_ATTACHMENT, GL11.GL_TEXTURE_2D, fboContainingDepthBuffer.depthID, 0);
					this.depthID=0;
					break;
				case FBO_DepthBufferType_NONE:
					break;
				}
				break;
			}
		}

		GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);

		checkFramebufferStatus(this.fbo);
	}
	
	/** Release the resources */
	public void dispose(){
		if(nbColorAttachement > 0){
			GLES30.glDeleteTextures(nbColorAttachement, colorTextures, 0);
		}
		
		switch(depthType)
		{
		case FBO_DepthBufferType_RENDERTARGET:
			GLES.glDeleteRenderbuffer(depthID);
			break;
		case FBO_DepthBufferType_NONE:
			break;
		default:
			GLES.glDeleteTextures(depthID);
			break;
		}
	}
	
	/** Return the texture that attachment at location specified by <i>index</i> */
	public int getColorTexture(int index) { return colorTextures[index];}
	
	/**
	 * @return the number of color attachment of this FBO
	 */
	public int getNumberOfColorAttachement(){ return nbColorAttachement; }
	/**
	 * @return the type of depth buffer of this FBO
	 */
	public int getDepthBufferType() { return depthType; }
	
	/** @return the texture that attachment to the depth buffer.  */
	public int getDepthTexture() { return depthID;}

	/**
	 * @return the width in pixel of this FBO
	 */
	public int getWidth() { return width; }
	/**
	 * @return the height in pixel of this FBO
	 */
	public int getHeight() { return height; }

	/**
	 *	@return the maximum number of color texture attachement
	 */
	public static int getMaxColorAttachments(){
		int maxAttach = GLES.glGetInteger(GLES30.GL_MAX_COLOR_ATTACHMENTS);
		return maxAttach;
	}
	/**
	 *	@return the maximum width and height allowed
	 */
	public static int getMaxBufferSize(){
		return GLES.glGetInteger(GLES30.GL_MAX_RENDERBUFFER_SIZE);
	}

	/**
	 *	Enable this FBO to render in ONE texture using the depth buffer if it exist.
	 * To disable rendering in the depth buffer and/or depth testing, use glDepthMask and glDisable(GL_DEPTH_TEST).
	 *
	 * @param colorBufferNum the number of the render texture (0 for GL_COLOR_ATTACHMENT0_EXT, etc)
	 */
	public void enableRenderToColorAndDepth(int colorBufferNum){
		GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fbo);

		if(nbColorAttachement>0)
		{
			validColorMipMapPyramid[colorBufferNum]=false;
			validDepthMipMapPyramid=false;
			int colorBuffer = GLES30.GL_COLOR_ATTACHMENT0 + colorBufferNum;
			if(colorBufferNum>nbColorAttachement)
				colorBuffer = GLES30.GL_COLOR_ATTACHMENT0;
			GLES.glDrawBuffer(colorBuffer);
		}
		else
		{
			GLES.glDrawBuffer(GLES30.GL_NONE);	//for example, in the case of rendering in a depth buffer
		}
	}
	/**
	 *	Enable this FBO to render in MULTIPLE texture using the depth buffer if it exist.
	 * To disable rendering in the depth buffer and/or depth testing, use glDepthMask and glDisable(GL_DEPTH_TEST).
	 *	Using this method, you must not use gl_fragColor in the fragment program but gl_fragData[i].
	 *
	 * @param numBuffers : the number of renger target we want
	 * @param drawbuffers : an array containing the color texture ID binded to the fragment program output gl_fragData[i]
	 */
	public void enableRenderToColorAndDepth_MRT(int numBuffers, int[] drawbuffers){
		GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fbo);
		
		//we mark invalid concerned mipmap pyramids
		for(int i=0; i<numBuffers; i++)
		{
			validColorMipMapPyramid[(drawbuffers[i]-GLES30.GL_COLOR_ATTACHMENT0)]=false;
		}
		validDepthMipMapPyramid=false;

		//HERE, THERE SHOULD HAVE A TEST OF THE DRAW BUFFERS ID AND NUMBER
		/*for(int i=0; i<numBuffers; i++)
		{
		}*/
		GLES30.glDrawBuffers(numBuffers, drawbuffers, 0);
	}
	/**
	 *	Disable render to texture. This method os static because it works the same way for all fbo instance.
	 * Thus, if you want to render to two render texture sequentially, you don't need to disable fbo rendering
	 * and it will improves performances.
	 */
	public static void disableRenderToColorDepth(){
		GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
	}

	private final int[] viewport = new int[4];
	/**
	 *	Save the current viewport and set up the viewport for this fbo. If the FBO is has same resolution as the current
	 * viewport, you don't need to call this method.
	 */
	public void saveAndSetViewPort(){
//		GL11.glPushAttrib(GL11.GL_VIEWPORT_BIT);
		GLES30.glGetIntegerv(GLES30.GL_VIEWPORT, viewport, 0);
		GLES30.glViewport(0, 0, width, height);
	}
	
	/** set up the viewport for this fbo. */
	public void setViewPort(){ GLES30.glViewport(0, 0, width, height);}
	
	/**
	 *	Restore previous viewport.
	 */
	public void restoreViewPort(){
		GLES30.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
	}
	
	/** @return the handler of the framebuffer object. */
	public int getFBO() { return fbo;}

	/**
	 *	Bind a color buffer in the current texture unit as a GL_TEXTURE_2D.
	 *
	 * @param colorBufferNum : the number of the color buffer texture to bind. If the number is invalid, texture 0 is binded
	 */
	public void bindColorTexture(int colorBufferNum){
		if(nbColorAttachement>0 && colorBufferNum<nbColorAttachement)
		{
			GLES30.glBindTexture(GL11.GL_TEXTURE_2D, colorTextures[colorBufferNum]);
		}
		else
		{
			System.err.println("the colorBufferNum out of the range.");
			GLES30.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		}
	}
	
	/**
	 *	Bind the depth buffer in the current texture unit as a GL_TEXTURE_2D.
	 */
	public void bindDepthTexture(){
		if(depthType>=FBO_DepthBufferType_TEXTURE)
		{
			GLES30.glBindTexture(GL11.GL_TEXTURE_2D, depthID);
		}
		else
			GLES30.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}

	/**
	 *	Generate the mipmap chain for a given color buffer.
	 * The mipmap generation will not work for texture-buffer you have specified with GL_NEAREST or GL_LINEAR at the creation.
	 *
	 * @param colorBufferNum : the number of the color buffer texture to bind. If the number is invalid, we return.
	 */
	public void generateColorBufferMipMap(int colorBufferNum){
		if(nbColorAttachement>0 && (int)colorBufferNum<nbColorAttachement)
		{
			if(colorMinificationFiltering[colorBufferNum]== GL11.GL_NEAREST
			|| colorMinificationFiltering[colorBufferNum]== GL11.GL_LINEAR)
				return;	//don't allow to generate mipmap chain for texture that don't support it at the creation

			GLES30.glBindTexture(GL11.GL_TEXTURE_2D, colorTextures[colorBufferNum]);
			GLES30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
			validColorMipMapPyramid[colorBufferNum]=true;
		}
	}
	/**
	 *	Generate the mipmap chain for the depth buffer. Some driver don't support it.
	 */
	public void generateDepthBufferMipMap(){
		if(depthType>=FBO_DepthBufferType_TEXTURE)
		{
			if(depthMinificationFiltering== GL11.GL_NEAREST
			|| depthMinificationFiltering== GL11.GL_LINEAR)
				return;	//don't allow to generate mipmap chain for texture that don't support it at the creation

			GLES30.glBindTexture(GL11.GL_TEXTURE_2D, depthID);
			GLES30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
			validDepthMipMapPyramid=true;
		}
	}
	/**
	 *
	 */
	public boolean isColorMipMapPyramidValid(int colorBufferNum){
		if(nbColorAttachement>0 && (int)colorBufferNum<nbColorAttachement)
		{
			return validColorMipMapPyramid[colorBufferNum];
		}
		else
			return false;
	}
	
	public boolean isDepthMipMapPyramidValid(){ return validDepthMipMapPyramid;}

	public boolean isFrameBufferComplete(){
		int status;
		boolean ret = false;

		GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fbo);

	    status = GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER);
	    if(status == GLES30.GL_FRAMEBUFFER_COMPLETE)
			ret = true;

		GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);

		return ret;
	}
	
	public static boolean checkFramebufferStatus(int fbo)
	{
	    int status;
		boolean ret = false;

		GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fbo);

	    status = GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER);
	    switch(status) {
	        case GLES30.GL_FRAMEBUFFER_COMPLETE:
//				System.err.print("Frame Buffer Complete\n");
				ret = true;
	            break;
	        case GLES30.GL_FRAMEBUFFER_UNSUPPORTED:
	        	System.err.print("Unsupported framebuffer format\n");
	            break;
	        case GLES30.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:
	        	System.err.print("Framebuffer incomplete, missing attachment\n");
	            break;
	        //case GL_FRAMEBUFFER_INCOMPLETE_DUPLICATE_ATTACHMENT_EXT:
	        //    printf("Framebuffer incomplete, duplicate attachment");
	        //   break;
	        case GLES30.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT:
	        	System.err.print("Framebuffer incomplete attachment\n");
	           break;
	        case GLES30.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS:
	        	System.err.print("Framebuffer incomplete, attached images must have same dimensions\n");
	            break;
			/*
	        case GLES30.GL_FRAMEBUFFER_INCOMPLETE_FORMATS:
	        	System.err.print("Framebuffer incomplete, attached images must have same format\n");
	            break;
	        case GLES30.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER:
	        	System.err.print("Framebuffer incomplete, missing draw buffer\n");
	            break;
	        case GLES30.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER:
	        	System.err.print("Framebuffer incomplete, missing read buffer\n");
	            break;
	            */
	        default:
	        	System.err.print("Framebuffer incomplete, UNKNOW ERROR\n");
	           // assert(0);
	    }

		GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);

		return ret;
	}

	private static final int RED = GLES30.GL_RED;
	private static final int RG  = GLES30.GL_RG;
	private static final int RGB = GL11.GL_RGB;
	private static final int RGBA= GL11.GL_RGBA;

	private static final int RED_INTEGER = GLES30.GL_RED_INTEGER;
	private static final int RG_INTEGER = GLES30.GL_RG_INTEGER;
	private static final int RGB_INTEGER = GLES30.GL_RGB_INTEGER;
	private static final int RGBA_INTEGER = GLES30.GL_RGBA_INTEGER;

	public static int measureFormat(int internalFormat){
		switch (internalFormat) {
			case GLES30.GL_R8:  				return RED;
			case GLES30.GL_R8_SNORM:		    return RED;
//			case GLES30.GL_R16: 				return RED;
//			case GLES30.GL_R16_SNORM : 		return RED;
			case GLES30.GL_RG8:				return RG;
			case GLES30.GL_RG8_SNORM:			return RG;
//			case GLES30.GL_RG16:				return RG;
//			case GLES30.GL_RG16_SNORM:		return RG;
//			case GLES30.GL_R3_G3_B2:			return RGB;
//			case GLES30.GL_RGB4:				return RGB;
//			case GLES30.GL_RGB5:				return RGB;
			case GLES30.GL_RGB8:				return RGB;
			case GLES30.GL_RGB8_SNORM:		return RGB;
//			case GLES30.GL_RGB10:				return RGB;
//			case GLES30.GL_RGB12:				return RGB;
//			case GLES30.GL_RGB16_SNORM:		return RGB;
//			case GLES30.GL_RGBA2:				return RGBA;  // TODO
			case GLES30.GL_RGBA4:				return RGBA;  // TODO
			case GLES30.GL_RGB5_A1:			return RGBA;  // TODO
			case GLES30.GL_RGBA8:				return RGBA;
			case GLES30.GL_RGBA8_SNORM:		return RGBA;
			case GLES30.GL_RGB10_A2:			return RGBA;
			case GLES30.GL_RGB10_A2UI:		return RGBA_INTEGER;
//			case GLES30.GL_RGBA12:			return RGBA;
//			case GLES30.GL_RGBA16:			return RGBA;
			case GLES30.GL_SRGB8:				return RGB;
			case GLES30.GL_SRGB8_ALPHA8:		return RGBA;
			case GLES30.GL_R16F:				return RED;
			case GLES30.GL_RG16F:				return RG;
			case GLES30.GL_RGB16F:			return RGB;
			case GLES30.GL_RGBA16F:			return RGBA;
			case GLES30.GL_R32F:				return RED;
			case GLES30.GL_RG32F:				return RG;
			case GLES30.GL_RGB32F:			return RGB;
			case GLES30.GL_RGBA32F:			return RGBA;
			case GLES30.GL_R11F_G11F_B10F:	return RGB;
			case GLES30.GL_RGB9_E5:			return RGB; // TODO ?
			case GLES30.GL_R8I:				return RED_INTEGER;
			case GLES30.GL_R8UI:				return RED_INTEGER;
			case GLES30.GL_R16I:				return RED_INTEGER;
			case GLES30.GL_R16UI:				return RED_INTEGER;
			case GLES30.GL_R32I:				return RED_INTEGER;
			case GLES30.GL_R32UI:				return RED_INTEGER;
			case GLES30.GL_RG8I:				return RG_INTEGER;
			case GLES30.GL_RG8UI:				return RG_INTEGER;
			case GLES30.GL_RG16I:				return RG_INTEGER;
			case GLES30.GL_RG16UI:			return RG_INTEGER;
			case GLES30.GL_RG32I:				return RG_INTEGER;
			case GLES30.GL_RG32UI:			return RG_INTEGER;
			case GLES30.GL_RGB8I:				return RGB_INTEGER;
			case GLES30.GL_RGB8UI:			return RGB_INTEGER;
			case GLES30.GL_RGB16I:			return RGB_INTEGER;
			case GLES30.GL_RGB16UI:			return RGB_INTEGER;
			case GLES30.GL_RGB32I:			return RGB_INTEGER;
			case GLES30.GL_RGB32UI:			return RGB_INTEGER;

			case GLES30.GL_RGBA8I:			return RGBA_INTEGER;
			case GLES30.GL_RGBA8UI:			return RGBA_INTEGER;
			case GLES30.GL_RGBA16I:			return RGBA_INTEGER;
			case GLES30.GL_RGBA16UI:			return RGBA_INTEGER;
			case GLES30.GL_RGBA32I:			return RGBA_INTEGER;
			case GLES30.GL_RGBA32UI:			return RGBA_INTEGER;
			case GLES30.GL_DEPTH_COMPONENT16:
			case GLES30.GL_DEPTH_COMPONENT24:
//			case GLES30.GL_DEPTH_COMPONENT32:
			case GLES30.GL_DEPTH_COMPONENT32F:
				return GLES30.GL_DEPTH_COMPONENT;
			case GLES30.GL_DEPTH24_STENCIL8:
			case GLES30.GL_DEPTH32F_STENCIL8:
				return GLES30.GL_DEPTH_STENCIL;
//			case EXTTextureCompressionS3TC.GL_COMPRESSED_RGBA_S3TC_DXT1_EXT:
//			case EXTTextureCompressionS3TC.GL_COMPRESSED_RGB_S3TC_DXT1_EXT:
//			case EXTTextureCompressionS3TC.GL_COMPRESSED_RGBA_S3TC_DXT3_EXT:
//			case EXTTextureCompressionS3TC.GL_COMPRESSED_RGBA_S3TC_DXT5_EXT:
//				return GLES30.GL_NONE;
			default:
				throw new IllegalArgumentException("Unkown internalFormat: " + internalFormat);
		}
	}

	public static int measureDataType(int internalFormat){
		switch (internalFormat) {
			case GLES30.GL_R8:  				return GL11.GL_UNSIGNED_BYTE;
			case GLES30.GL_R8_SNORM:		    return GL11.GL_BYTE;
//			case GLES30.GL_R16: 				return GL11.GL_UNSIGNED_SHORT;
//			case GLES30.GL_R16_SNORM : 		return GL11.GL_SHORT;
			case GLES30.GL_RG8:				return GL11.GL_UNSIGNED_BYTE;
			case GLES30.GL_RG8_SNORM:			return GL11.GL_BYTE;
//			case GLES30.GL_RG16:				return GL11.GL_UNSIGNED_SHORT;
//			case GLES30.GL_RG16_SNORM:		return GL11.GL_SHORT;
//			case GLES30.GL_R3_G3_B2:			return GL12.GL_UNSIGNED_BYTE_3_3_2;
//			case GLES30.GL_RGB4:				return GL11.GL_UNSIGNED_BYTE;  // TODO ?
//			case GLES30.GL_RGB5:				return GL11.GL_UNSIGNED_BYTE;  // TODO ?
			case GLES30.GL_RGB8:				return GL11.GL_UNSIGNED_BYTE;
			case GLES30.GL_RGB8_SNORM:		return GL11.GL_BYTE;
//			case GLES30.GL_RGB10:				return GL11.GL_UNSIGNED_BYTE;  // TODO
//			case GLES30.GL_RGB12:				return GL11.GL_UNSIGNED_BYTE;  // TODO
//			case GLES30.GL_RGB16_SNORM:		return GL11.GL_SHORT;
//			case GLES30.GL_RGBA2:				return GL11.GL_UNSIGNED_BYTE;  // TODO
			case GLES30.GL_RGBA4:				return GLES30.GL_UNSIGNED_SHORT_4_4_4_4;  // TODO
			case GLES30.GL_RGB5_A1:			return GLES30.GL_UNSIGNED_SHORT_5_5_5_1;  // TODO
			case GLES30.GL_RGBA8:				return GL11.GL_UNSIGNED_BYTE;
			case GLES30.GL_RGBA8_SNORM:		return GL11.GL_BYTE;
			case GLES30.GL_RGB10_A2:			return GLES30.GL_UNSIGNED_INT_2_10_10_10_REV;
			case GLES30.GL_RGB10_A2UI:		return GLES30.GL_UNSIGNED_INT_2_10_10_10_REV; // TODO
//			case GLES30.GL_RGBA12:			return GL11.GL_UNSIGNED_BYTE;
//			case GLES30.GL_RGBA16:			return GL11.GL_UNSIGNED_SHORT;
			case GLES30.GL_SRGB8:				return GL11.GL_BYTE;
			case GLES30.GL_SRGB8_ALPHA8:		return GL11.GL_BYTE;
			case GLES30.GL_R16F:				return GLES30.GL_HALF_FLOAT;
			case GLES30.GL_RG16F:				return GLES30.GL_HALF_FLOAT;
			case GLES30.GL_RGB16F:			return GLES30.GL_HALF_FLOAT;
			case GLES30.GL_RGBA16F:			return GLES30.GL_HALF_FLOAT;
			case GLES30.GL_R32F:				return GL11.GL_FLOAT;
			case GLES30.GL_RG32F:				return GL11.GL_FLOAT;
			case GLES30.GL_RGB32F:			return GL11.GL_FLOAT;
			case GLES30.GL_RGBA32F:			return GL11.GL_FLOAT;
			case GLES30.GL_R11F_G11F_B10F:	return GLES30.GL_UNSIGNED_INT_10F_11F_11F_REV;
			case GLES30.GL_RGB9_E5:			return GLES30.GL_UNSIGNED_INT_5_9_9_9_REV; // TODO ?
			case GLES30.GL_R8I:				return GL11.GL_BYTE;
			case GLES30.GL_R8UI:				return GL11.GL_UNSIGNED_BYTE;
			case GLES30.GL_R16I:				return GL11.GL_SHORT;
			case GLES30.GL_R16UI:				return GL11.GL_UNSIGNED_SHORT;
			case GLES30.GL_R32I:				return GLES30.GL_INT;
			case GLES30.GL_R32UI:				return GLES30.GL_UNSIGNED_INT;
			case GLES30.GL_RG8I:				return GL11.GL_BYTE;
			case GLES30.GL_RG8UI:				return GL11.GL_UNSIGNED_BYTE;
			case GLES30.GL_RG16I:				return GL11.GL_SHORT;
			case GLES30.GL_RG16UI:			return GL11.GL_UNSIGNED_SHORT;
			case GLES30.GL_RG32I:				return GLES30.GL_INT;
			case GLES30.GL_RG32UI:			return GLES30.GL_UNSIGNED_INT;
			case GLES30.GL_RGB8I:				return GL11.GL_BYTE;
			case GLES30.GL_RGB8UI:			return GL11.GL_UNSIGNED_BYTE;
			case GLES30.GL_RGB16I:			return GL11.GL_SHORT;
			case GLES30.GL_RGB16UI:			return GL11.GL_UNSIGNED_SHORT;
			case GLES30.GL_RGB32I:			return GLES30.GL_INT;
			case GLES30.GL_RGB32UI:			return GLES30.GL_UNSIGNED_INT;

			case GLES30.GL_RGBA8I:			return GL11.GL_BYTE;
			case GLES30.GL_RGBA8UI:			return GL11.GL_UNSIGNED_BYTE;
			case GLES30.GL_RGBA16I:			return GL11.GL_SHORT;
			case GLES30.GL_RGBA16UI:			return GL11.GL_UNSIGNED_SHORT;
			case GLES30.GL_RGBA32I:			return GLES30.GL_INT;
			case GLES30.GL_RGBA32UI:			return GLES30.GL_UNSIGNED_INT;
			case GLES30.GL_DEPTH_COMPONENT16: return GL11.GL_UNSIGNED_SHORT;
			case GLES30.GL_DEPTH_COMPONENT24:
			case GLES30.GL_DEPTH24_STENCIL8:  return GLES30.GL_UNSIGNED_INT_24_8;
//			case GLES30.GL_DEPTH_COMPONENT32: return GL11.GL_UNSIGNED_INT;
			case GLES30.GL_DEPTH_COMPONENT32F:return GL11.GL_FLOAT;

			default:
				throw new IllegalArgumentException("Unkown internalFormat: " + internalFormat);
		}
	}
}
