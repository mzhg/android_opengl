package jet.learning.opengl.common;

import android.opengl.GLES30;

import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.GLUtil;

import org.lwjgl.util.vector.Matrix4f;

public class UniformMatrix {

	public final Matrix4f gWorld = new Matrix4f();
	public final Matrix4f gWorldInvTranspose = new Matrix4f();
	public final Matrix4f gWorldViewProj = new Matrix4f();
	public final Material gMaterial = new Material();
	public final Matrix4f gTexTransform = new Matrix4f();
	public final Matrix4f gShadowTransform = new Matrix4f();

	int worldOffset;
	int worldInvTransposeOffset;
	int worldViewPorjOffset;
	int texTransformOffset;
	int shadowTransformOffset;
	int ambientOffset;
	int diffuseOffset;
	int specularOffset;
	int reflectOffset;

	public void init(int program) {
		worldOffset = GLES30.glGetUniformLocation(program, "gWorld");
		worldInvTransposeOffset = GLES30.glGetUniformLocation(program, "gWorldInvTranspose");
		worldViewPorjOffset = GLES30.glGetUniformLocation(program, "gWorldViewProj");
		texTransformOffset  = GLES30.glGetUniformLocation(program, "gTexTransform");
		shadowTransformOffset = GLES30.glGetUniformLocation(program, "gShadowTransform");
		
		ambientOffset = GLES30.glGetUniformLocation(program, "gMaterial.Ambient");
		diffuseOffset = GLES30.glGetUniformLocation(program, "gMaterial.Diffuse");
		specularOffset = GLES30.glGetUniformLocation(program, "gMaterial.Specular");
		reflectOffset = GLES30.glGetUniformLocation(program, "gMaterial.Reflect");
	}

	public void apply() {
		GLES30.glUniformMatrix4fv(worldOffset, 1, false, GLUtil.wrap(gWorld));  GLES.checkGLError();
		GLES30.glUniformMatrix4fv(worldInvTransposeOffset, 1, false, GLUtil.wrap(gWorldInvTranspose));  GLES.checkGLError();
		GLES30.glUniformMatrix4fv(worldViewPorjOffset, 1, false, GLUtil.wrap(gWorldViewProj));  GLES.checkGLError();
		GLES30.glUniformMatrix4fv(worldOffset, 1, false, GLUtil.wrap(gWorld));  GLES.checkGLError();
		GLES30.glUniformMatrix4fv(texTransformOffset, 1, false, GLUtil.wrap(gTexTransform));  GLES.checkGLError();
		GLES30.glUniformMatrix4fv(shadowTransformOffset, 1, false, GLUtil.wrap(gShadowTransform));  GLES.checkGLError();

		GLES30.glUniform4f(ambientOffset, gMaterial.ambient.x, gMaterial.ambient.y, gMaterial.ambient.z, gMaterial.ambient.w);  GLES.checkGLError();
		GLES30.glUniform4f(diffuseOffset, gMaterial.diffuse.x, gMaterial.diffuse.y, gMaterial.diffuse.z, gMaterial.diffuse.w);  GLES.checkGLError();
		GLES30.glUniform4f(specularOffset, gMaterial.specular.x, gMaterial.specular.y, gMaterial.specular.z, gMaterial.specular.w);  GLES.checkGLError();
		GLES30.glUniform4f(reflectOffset, gMaterial.reflect.x, gMaterial.reflect.y, gMaterial.reflect.z, gMaterial.reflect.w);  GLES.checkGLError();
	}
}
