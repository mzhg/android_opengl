package jet.learning.opengl.common;

import android.opengl.GLES30;

public class UniformMaterial {

	int ambientOffset;
	int diffuseOffset;
	int specularOffset;
	int reflectOffset;
	
	public void init(int program){
		ambientOffset 			= GLES30.glGetUniformLocation(program, "gMaterial.Ambient");
		diffuseOffset 			= GLES30.glGetUniformLocation(program, "gMaterial.Diffuse");
		specularOffset 			= GLES30.glGetUniformLocation(program, "gMaterial.Specular");
		reflectOffset 			= GLES30.glGetUniformLocation(program, "gMaterial.Reflect");
	}
	
	public void apply(Material gMaterial){
		GLES30.glUniform4f(ambientOffset, gMaterial.ambient.x, gMaterial.ambient.y, gMaterial.ambient.z, gMaterial.ambient.w);
		GLES30.glUniform4f(diffuseOffset, gMaterial.diffuse.x, gMaterial.diffuse.y, gMaterial.diffuse.z, gMaterial.diffuse.w);
		GLES30.glUniform4f(specularOffset, gMaterial.specular.x, gMaterial.specular.y, gMaterial.specular.z, gMaterial.specular.w);
		GLES30.glUniform4f(reflectOffset, gMaterial.reflect.x, gMaterial.reflect.y, gMaterial.reflect.z, gMaterial.reflect.w);
	}
}
