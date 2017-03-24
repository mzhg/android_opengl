#version 300 es

precision highp float;

uniform sampler2D WaterHeightMap;

uniform float ODWHMR;
in vec2 a_texCoord;

out vec4 gl_FragColor;
void main()
{
	vec2 vh = texture(WaterHeightMap, a_texCoord).rg;

	float force = 0.0;

	force += 0.707107 * (texture(WaterHeightMap, a_texCoord - vec2(ODWHMR, ODWHMR)).g - vh.g);
	force += texture(WaterHeightMap, a_texCoord - vec2(0.0, ODWHMR)).g - vh.g;
	force += 0.707107 * (texture(WaterHeightMap, a_texCoord + vec2(ODWHMR, -ODWHMR)).g - vh.g);

	force += texture(WaterHeightMap, a_texCoord - vec2(ODWHMR, 0.0)).g - vh.g;
	force += texture(WaterHeightMap, a_texCoord + vec2(ODWHMR, 0.0)).g - vh.g;

	force += 0.707107 * (texture(WaterHeightMap, a_texCoord + vec2(-ODWHMR, ODWHMR)).g - vh.g);
	force += texture(WaterHeightMap, a_texCoord + vec2(0.0, ODWHMR)).g - vh.g;
	force += 0.707107 * (texture(WaterHeightMap, a_texCoord + vec2(ODWHMR, ODWHMR)).g - vh.g);

	force *= 0.125;

	vh.r += force;
	vh.g += vh.r;
	vh.g *= 0.99;

	gl_FragColor = vec4(vh, 0.0, 0.0);
}
