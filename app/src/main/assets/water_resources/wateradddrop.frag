#version 300 es

precision highp float;
uniform sampler2D WaterHeightMap;

uniform float DropRadius;
uniform vec2 Position;

in vec2 a_texCoord;

out vec4 gl_FragColor;

void main()
{
	vec2 vh = texture(WaterHeightMap, a_texCoord).rg;

	float d = distance(a_texCoord, Position);

	gl_FragColor = vec4(vh.r, vh.g - 4.0 * max(DropRadius - d, 0.0), 0.0, 0.0);
}