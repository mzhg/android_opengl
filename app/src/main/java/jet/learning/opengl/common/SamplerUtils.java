package jet.learning.opengl.common;

import android.opengl.GLES30;
import android.opengl.GLES32;
import android.util.Log;

import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.NvUtils;

import java.nio.FloatBuffer;
import java.util.HashMap;

public final class SamplerUtils {

	private static final HashMap<SamplerDesc, Integer> sampler_caches = new HashMap<SamplerDesc, Integer>();

	private static final SamplerDesc g_DefaultSampler = new SamplerDesc();
	private static final SamplerDesc g_DepthComparisonSampler = new SamplerDesc();

	static {
		g_DepthComparisonSampler.borderColor = -1;
		g_DepthComparisonSampler.wrapR = GLES32.GL_CLAMP_TO_BORDER;
		g_DepthComparisonSampler.wrapS = GLES32.GL_CLAMP_TO_BORDER;
		g_DepthComparisonSampler.wrapT = GLES32.GL_CLAMP_TO_BORDER;
		g_DepthComparisonSampler.minFilter = GLES32.GL_NEAREST;
		g_DepthComparisonSampler.compareFunc = GLES32.GL_LESS;
		g_DepthComparisonSampler.compareMode = GLES32.GL_COMPARE_REF_TO_TEXTURE;
	}

	private static final int UNKOWN = 0;
	private static final int ENABLE = 1;
	private static final int DISABLE = 2;

	private static int g_SampleState = UNKOWN;

	public static boolean isSamplerSupport(){
		if(g_SampleState == UNKOWN){
			g_SampleState = ENABLE;
		}

		return (g_SampleState == ENABLE);
	}
	
	public static void releaseCaches(){
//		GLFuncProvider gl = GLFuncProviderFactory.getGLFuncProvider();
//		GLES32.glDeleteSamplers(sampler_caches.values());
		
		sampler_caches.clear();
	}

	public static int getDefaultSampler(){
		return createSampler(g_DefaultSampler);
	}

	public static int getDepthComparisonSampler(){
		return createSampler(g_DepthComparisonSampler);
	}
	
	public static int createSampler(SamplerDesc sampler){
		if(!isSamplerSupport()){
//			LogUtil.i(LogUtil.LogType.DEFAULT, "Unsupport the sampler object");

			Log.w("SamplerUtils", "Unsupport the sampler object");
			return 0;
		}
		
		Integer s = sampler_caches.get(sampler);
		if(s != null)
			return s;
		else{
			s = _createSampler(sampler);
			sampler_caches.put(new SamplerDesc(sampler), s);
			return s;
		}
	}
	
	private static int _createSampler(SamplerDesc sampler){
		if(!isSamplerSupport()){
//			LogUtil.i(LogUtil.LogType.DEFAULT, "Unsupport the sampler object");
			Log.w("SamplerUtils", "Unsupport the sampler object");
			return 0;
		}


		int obj = GLES.glGenSamplers();
		GLES30.glSamplerParameteri(obj, GLES32.GL_TEXTURE_MIN_FILTER, sampler.minFilter);
		GLES30.glSamplerParameteri(obj, GLES32.GL_TEXTURE_MAG_FILTER, sampler.magFilter);
		GLES30.glSamplerParameteri(obj, GLES32.GL_TEXTURE_WRAP_S, sampler.wrapS);
		GLES30.glSamplerParameteri(obj, GLES32.GL_TEXTURE_WRAP_T, sampler.wrapT);
		if(sampler.wrapR != 0)
			GLES30.glSamplerParameteri(obj, GLES32.GL_TEXTURE_WRAP_R, sampler.wrapR);

		if(sampler.borderColor != 0){
			float r = NvUtils.getRedFromRGBf(sampler.borderColor);
			float g = NvUtils.getGreenf(sampler.borderColor);
			float b = NvUtils.getBlueFromRGBf(sampler.borderColor);
			float a = NvUtils.getAlphaf(sampler.borderColor);
			
			float[] colorBuf = {r,g,b,a};
			GLES30.glSamplerParameterfv(obj, GLES32.GL_TEXTURE_BORDER_COLOR, colorBuf, 0);
//			LogUtil.i(LogUtil.LogType.DEFAULT, "Border Color =(" + r + ", " + g + ", " + b + ", " + a + ")");
		}
		
		/*if(sampler.anisotropic > 0 && !gl.getGLAPIVersion().ES){
			int largest = gl.glGetInteger(GLES32.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT);
			gl.glSamplerParameteri(obj, GLES32.GL_TEXTURE_MAX_ANISOTROPY_EXT, Math.min(sampler.anisotropic, largest));
		}*/
		
		if(sampler.compareFunc != 0){
			GLES30.glSamplerParameteri(obj, GLES32.GL_TEXTURE_COMPARE_FUNC, sampler.compareFunc);
		}
		
		if(sampler.compareMode != 0){
			GLES30.glSamplerParameteri(obj, GLES32.GL_TEXTURE_COMPARE_MODE, sampler.compareMode);
		}
		
		return obj;
	}

	/**
	 * Apply the defualt sampler to a cube map texture which binding.
	 * @param mipmap Indicate whether use mipmap on the min filter.
     */
	public static void applyCubemapSampler(boolean mipmap){
//		final GLFuncProvider gl = GLFuncProviderFactory.getGLFuncProvider();

		int target = GLES32.GL_TEXTURE_CUBE_MAP;
		int minFilter = mipmap ? GLES32.GL_LINEAR_MIPMAP_LINEAR : GLES32.GL_LINEAR;
		int magFilter = GLES32.GL_LINEAR;
		int wrap = GLES32.GL_CLAMP_TO_EDGE;

		GLES30.glTexParameteri(target, GLES32.GL_TEXTURE_MIN_FILTER, minFilter);
		GLES30.glTexParameteri(target, GLES32.GL_TEXTURE_MAG_FILTER, magFilter);
		GLES30.glTexParameteri(target, GLES32.GL_TEXTURE_WRAP_S, wrap);
		GLES30.glTexParameteri(target, GLES32.GL_TEXTURE_WRAP_T, wrap);
		GLES30.glTexParameteri(target, GLES32.GL_TEXTURE_WRAP_R, wrap);
	}

	// The default setting apply to a texture2D image.
	public static void applyTexture2DLinearClampSampler(boolean mipmap){
		applyTexture2DSampler(GLES32.GL_TEXTURE_2D, mipmap ? GLES32.GL_LINEAR_MIPMAP_LINEAR : GLES32.GL_LINEAR, GLES32.GL_LINEAR, GLES32.GL_CLAMP_TO_EDGE, GLES32.GL_CLAMP_TO_EDGE);
	}
	
	public static void applyTexture2DSampler(int target, int minFilter, int magFilter, int wrapS, int wrapT){
		GLES30.glTexParameteri(target, GLES32.GL_TEXTURE_MIN_FILTER, minFilter);
		GLES30.glTexParameteri(target, GLES32.GL_TEXTURE_MAG_FILTER, magFilter);
		GLES30.glTexParameteri(target, GLES32.GL_TEXTURE_WRAP_S, wrapS);
		GLES30.glTexParameteri(target, GLES32.GL_TEXTURE_WRAP_T, wrapT);
	}

	/**
	 * Apply the given sampler to the current binding texture.
	 * @param target The texture target. e.g: GL_TEXTURE2D, GL_TEXTURE2D_ARRAY...
	 * @param desc  The sampler which will be apllying to.
     */
	public static void applySampler(int target, SamplerDesc desc){

		GLES30.glTexParameteri(target, GLES32.GL_TEXTURE_MIN_FILTER, desc.minFilter);
		GLES30.glTexParameteri(target, GLES32.GL_TEXTURE_MAG_FILTER, desc.magFilter);
		GLES30.glTexParameteri(target, GLES32.GL_TEXTURE_WRAP_S, desc.wrapS);
		GLES30.glTexParameteri(target, GLES32.GL_TEXTURE_WRAP_T, desc.wrapT);
		if((target == GLES32.GL_TEXTURE_3D || target == GLES32.GL_TEXTURE_CUBE_MAP) && desc.wrapR != 0)
			GLES30.glTexParameteri(target, GLES32.GL_TEXTURE_WRAP_R, desc.wrapR);
		
		float r = NvUtils.getRedFromRGBf(desc.borderColor);
		float g = NvUtils.getGreenf(desc.borderColor);
		float b = NvUtils.getBlueFromRGBf(desc.borderColor);
		float a = NvUtils.getAlphaf(desc.borderColor);

		float[] colorBuf = {r,g,b,a};
		GLES30.glTexParameterfv(target, GLES32.GL_TEXTURE_BORDER_COLOR, colorBuf,0);
		
//		int largest = GLES.glGetInteger(GLES32.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT);
//		GLES30.glTexParameteri(target, GLES32.GL_TEXTURE_MAX_ANISOTROPY_EXT, Math.min(Math.max(0, desc.anisotropic), largest));

		GLES30.glTexParameteri(target, GLES32.GL_TEXTURE_COMPARE_FUNC, desc.compareFunc);
		GLES30.glTexParameteri(target, GLES32.GL_TEXTURE_COMPARE_MODE, desc.compareMode);
	}

	/**
	 * Apply the depth comparsion filter to the current binding texture.
	 * @param mipmap
     */
	public static void applyDepthTexture2DSampler(boolean mipmap){
		applyTexture2DSampler(GLES32.GL_TEXTURE_2D, mipmap ? GLES32.GL_NEAREST_MIPMAP_LINEAR : GLES32.GL_NEAREST, GLES32.GL_LINEAR, GLES32.GL_CLAMP_TO_EDGE, GLES32.GL_CLAMP_TO_EDGE);
		GLES30.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_COMPARE_FUNC, GLES32.GL_LESS);
		GLES30.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_COMPARE_MODE, GLES32.GL_COMPARE_REF_TO_TEXTURE);
	}
}
