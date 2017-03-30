#version 300 es
precision highp float;
in vec3 TexCoord;
uniform samplerCube envMap;

out vec4 gl_FragColor;
#define textureCube(x, y) texture(x, y)

void main()
{
	gl_FragColor = texture(envMap, TexCoord);
	gl_FragColor.a = gl_FragCoord.z;
}