#version 300 es

precision highp float;

uniform sampler2D WaterHeightMap;

uniform float ODWNMR, WMSDWNMRM2;

in vec2 a_texCoord;

out vec4 gl_FragColor;
void main()
{
	float y[4];

	y[0] = texture(WaterHeightMap, a_texCoord + vec2(ODWNMR, 0.0)).g;
	y[1] = texture(WaterHeightMap, a_texCoord + vec2(0.0, ODWNMR)).g;
	y[2] = texture(WaterHeightMap, a_texCoord - vec2(ODWNMR, 0.0)).g;
	y[3] = texture(WaterHeightMap, a_texCoord - vec2(0.0, ODWNMR)).g;

	vec3 Normal = normalize(vec3(y[2] - y[0], WMSDWNMRM2, y[1] - y[3]));

    Normal = 0.5 * Normal + 0.5;
	gl_FragColor = vec4(Normal, 1.0);
}
