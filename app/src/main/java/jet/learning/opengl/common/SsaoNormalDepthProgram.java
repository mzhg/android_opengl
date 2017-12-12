package jet.learning.opengl.common;

import android.opengl.GLES20;
import android.opengl.GLES30;

import com.nvidia.developer.opengl.utils.GLUtil;
import com.nvidia.developer.opengl.utils.Glut;
import com.nvidia.developer.opengl.utils.NvGLSLProgram;

import org.lwjgl.util.vector.Matrix4f;

import javax.microedition.khronos.opengles.GL11;

public class SsaoNormalDepthProgram {

	int mProgram;
	
	int mWorldView;
	int mWorldInvTransposeView;
	int mWorldViewProj;
	int mTexTransform;

	int mDiffuseMap;
	
	final Matrix4f tmp = new Matrix4f();
	public void init(){
		CharSequence vs_str = Glut.loadTextFromClassPath(SkyRenderer.class, "ssaoNormalDepth.glvs");
		CharSequence fs_str = Glut.loadTextFromClassPath(SkyRenderer.class, "ssaoNormalDepth.glfs");
		NvGLSLProgram program = NvGLSLProgram.createFromStrings(vs_str, fs_str);

//		mProgram = Framework.linkProgramFromSource(Glut.loadTextFromClassPath(Sky.class, "ssaoNormalDepth.glvs"), Glut.loadTextFromClassPath(Sky.class, "ssaoNormalDepth.glfs"));
		mProgram = program.getProgram();

		mWorldView = GLES20.glGetUniformLocation(mProgram, "gWorldView");
		mWorldInvTransposeView = GLES20.glGetUniformLocation(mProgram, "gWorldInvTransposeView");
		mWorldViewProj = GLES20.glGetUniformLocation(mProgram, "gWorldViewProj");
		mTexTransform = GLES20.glGetUniformLocation(mProgram, "gTexTransform");
		
		mDiffuseMap = GLES20.glGetUniformLocation(mProgram, "gDiffuseMap");

		GLES20.glUseProgram(mProgram);
		GLES20.glUniform1i(mDiffuseMap, 0);
//		int index = GL40.glGetSubroutineIndex(mProgram, GL20.GL_FRAGMENT_SHADER, "NormalDepth");   TODO subroutineIndex
//		GLES20.glUniformSubroutinesu(GL20.GL_FRAGMENT_SHADER, GLUtil.wrap(index));
		GLES20.glUseProgram(0);
	}
	
	public void apply(Matrix4f world, Matrix4f view, Matrix4f viewProj, Matrix4f texTrans){
		Matrix4f.invert(world, tmp);
		tmp.transpose();
		
		Matrix4f worldInvTransposeView = Matrix4f.mul(view, tmp, tmp);
		GLES20.glUniformMatrix4fv(mWorldInvTransposeView, 1, false, GLUtil.wrap(worldInvTransposeView));
		
		Matrix4f worldView = Matrix4f.mul(view, world, tmp);
		GLES20.glUniformMatrix4fv(mWorldView, 1, false, GLUtil.wrap(worldView));
		
		Matrix4f worldViewProj = Matrix4f.mul(viewProj, world, tmp);
		GLES20.glUniformMatrix4fv(mWorldViewProj, 1, false, GLUtil.wrap(worldViewProj));
		
		if(texTrans == null){
			GLES20.glUniformMatrix4fv(mTexTransform, 1, false, GLUtil.wrap(Matrix4f.IDENTITY));
		}else{
			GLES20.glUniformMatrix4fv(mTexTransform, 1, false, GLUtil.wrap(texTrans));
		}
	}
	
	public void setDiffuseMap(int tex){
		GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
		GLES30.glBindTexture(GL11.GL_TEXTURE_2D, tex);
	}
	
	public void enable(){ GLES30.glUseProgram(mProgram); }
}
