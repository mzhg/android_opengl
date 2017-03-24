#version 300 es

precision highp float;
in vec3 a_texCoord;

uniform samplerCube PoolSkyCubeMap;

out vec4 gl_FragColor;

void main()
{
	gl_FragColor = max(vec4(0.0), texture(PoolSkyCubeMap, a_texCoord));
}