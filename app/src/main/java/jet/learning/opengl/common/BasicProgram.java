package jet.learning.opengl.common;

import android.opengl.GLES20;

import com.nvidia.developer.opengl.utils.Glut;
import com.nvidia.developer.opengl.utils.NvGLSLProgram;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.ReadableVector3f;

import javax.microedition.khronos.opengles.GL11;

public class BasicProgram {

	private static final Matrix4f SHADWN_MAT = new Matrix4f();
	
	static{
		SHADWN_MAT.m00 = 0.5f;
		SHADWN_MAT.m11 = 0.5f;
		SHADWN_MAT.m22 = 0.5f;
		SHADWN_MAT.m30 = 0.5f;
		SHADWN_MAT.m31 = 0.5f;
		SHADWN_MAT.m32 = 0.5f;
	}
	
	int mProgram;
	
	int mfxUseTex;
	int mfxUseCubeMap;
	int mfxDiffTex;
	int mfxCubeMap;
	int mfxAlphaCliped;
	
	int mfxUseShadowMap;
	int mfxShadowMap;
	int mfxWorldViewProjTex;
	
	public UniformLights mLights = new UniformLights();
	public UniformMatrix mMatrix = new UniformMatrix();
	
	public void init(){
		boolean use_uniform_block = false;
		String lightHelpString = Glut.loadTextFromClassPath(BasicProgram.class, "LightHelper.glsl").toString();
		StringBuilder vertexString = (StringBuilder) Glut.loadTextFromClassPath(DirectionalLight.class, "basic.glvs");
		StringBuilder fragmentString = (StringBuilder) Glut.loadTextFromClassPath(DirectionalLight.class, "basic.glfs");
		
		String include = "#include \"LightHelper.glsl\"";
		int index = vertexString.indexOf(include);
		if(index != -1)
			vertexString.replace(index, index + include.length(), lightHelpString);
		
		index = fragmentString.indexOf(include);
		fragmentString.replace(index, index + include.length(), lightHelpString);
		
		if(!use_uniform_block){
			String define = "#define USE_UNIFORM_BUFFER";
			
			index = vertexString.indexOf(define);
			if(index != -1){
				vertexString.delete(index, index + define.length());
			}
			
			index = fragmentString.indexOf(define);
			if(index != -1){
				fragmentString.delete(index, index + define.length());
			}
		}

		NvGLSLProgram program = NvGLSLProgram.createFromStrings(vertexString, fragmentString);
		mProgram = program.getProgram();
		
		mfxUseTex    = GLES20.glGetUniformLocation(mProgram, "gUseTexure");
		mfxUseCubeMap= GLES20.glGetUniformLocation(mProgram, "gReflectionEnabled");
		mfxDiffTex   = GLES20.glGetUniformLocation(mProgram, "gDiffuseMap");
		mfxCubeMap   = GLES20.glGetUniformLocation(mProgram, "gCubeMap");
		mfxAlphaCliped = GLES20.glGetUniformLocation(mProgram, "gAlphaClip");
		
		mfxUseShadowMap = GLES20.glGetUniformLocation(mProgram, "gUseShadowMap");
		mfxShadowMap = GLES20.glGetUniformLocation(mProgram, "gShadowMap");
		mfxWorldViewProjTex = GLES20.glGetUniformLocation(mProgram, "gWorldViewProjTex");
		
		mMatrix.init(mProgram);
		mLights.init(mProgram);

		GLES20.glUseProgram(mProgram);
		GLES20.glUniform1i(mfxDiffTex, 0);
		GLES20.glUniform1i(mfxCubeMap, 1);
		GLES20.glUniform1i(mfxShadowMap, 2);
		GLES20.glUseProgram(0);
	}
	
	public void setMatrix(Matrix4f viewProj, Matrix4f world, Matrix4f texTrans){
		if(world != null){
			mMatrix.gWorld.load(world);
			Matrix4f.invert(world, mMatrix.gWorldInvTranspose).transpose();
		}else
			world = Matrix4f.IDENTITY;
		
		if(viewProj != null){
			Matrix4f.mul(viewProj, world, mMatrix.gWorldViewProj);
			Matrix4f.mul(SHADWN_MAT, mMatrix.gWorldViewProj, mMatrix.gShadowTransform);
		}
		
		if(texTrans != null)
			mMatrix.gTexTransform.load(texTrans);
	}
	
	public void setMaterial(Material m){
		mMatrix.gMaterial.set(m);
	}
	
	public void applyMat(){
		mMatrix.apply();
	}
	
	public void setDiffuseTex(int texture){
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GL11.GL_TEXTURE_2D, texture);
		GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
		GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
//		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, 4);
	}
	
	public void setCubemap(int texture){
		GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, texture);
	}
	
	public void setShadowMap(int texture){
		GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
		GLES20.glBindTexture(GL11.GL_TEXTURE_2D, texture);
	}
	
	public void applyLights(){
		mLights.apply();
	}
	
	public void enable(){ GLES20.glUseProgram(mProgram);}
	
	public void enableAliphaClip() {GLES20.glUniform1i(mfxAlphaCliped, 1);}
	public void disableAliphaClip() {GLES20.glUniform1i(mfxAlphaCliped, 0);}
	
	public void set(boolean useTex, boolean useReflect, boolean useShadowMap){
		GLES20.glUniform1i(mfxUseTex, useTex ? 1 : 0);
		GLES20.glUniform1i(mfxUseCubeMap, useReflect ? 1 : 0);
		GLES20.glUniform1i(mfxUseShadowMap, useShadowMap ? 1 : 0);
	}
	
	public void setEyePosW(ReadableVector3f eyePos){
		mLights.gEyePosW.set(eyePos);
	}
}
