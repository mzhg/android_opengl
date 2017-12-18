#version 300 es
precision highp float;

uniform sampler2D g_WaterHeightMap;
layout(location = 0) out vec2 Out_Color;

void main()
{
    ivec2 pos = ivec2(gl_FragCoord);
	float h0 = texelFetchOffset(g_WaterHeightMap, pos, 0, ivec2(0, +1)).x;
	float h1 = texelFetchOffset(g_WaterHeightMap, pos, 0, ivec2(0, -1)).x;
	float h2 = texelFetchOffset(g_WaterHeightMap, pos, 0, ivec2(+1, 0)).x;
	float h3 = texelFetchOffset(g_WaterHeightMap, pos, 0, ivec2(-1, 0)).x;

	Out_Color = vec2(h0 - h1, h2 - h3);
//	Out_Color = vec2(h2 - h3, h0 - h1);
}