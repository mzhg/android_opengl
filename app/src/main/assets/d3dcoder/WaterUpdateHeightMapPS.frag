#version 300 es
precision highp float;

uniform sampler2D g_WaterHeightMap;

uniform vec4 Disturbance;
uniform float Damping;
uniform float DeltaTime;
uniform int GridSize;

layout(location = 0) out vec2 Out_Color;

float CalcDisturbance(vec2 coords)
{
	float dx = Disturbance.x - coords.x;
	float dy = Disturbance.y - coords.y;

	float d = sqrt(dx*dx + dy*dy);
	d = (d / Disturbance.z) * 3.0;
	return exp(-d*d) * Disturbance.w;
}

void main()
{

	ivec2 pos = ivec2(gl_FragCoord);
	vec2 vh = texelFetch(g_WaterHeightMap, pos, 0).rg;
	float h0 = texelFetchOffset(g_WaterHeightMap, pos, 0, ivec2(0, +1)).r;
	float h1 = texelFetchOffset(g_WaterHeightMap, pos, 0, ivec2(0, -1)).r;
	float h2 = texelFetchOffset(g_WaterHeightMap, pos, 0, ivec2(+1, 0)).r;
	float h3 = texelFetchOffset(g_WaterHeightMap, pos, 0, ivec2(-1, 0)).r;

	float d = ((h0 + h1 + h2 + h3) * 0.25 - vh.x);

    // update the velocity
	vh.y += d * DeltaTime;
	vh.y *= Damping;

    // update the height
    vh.x += vh.y * DeltaTime;
    if (Disturbance.w > 0.0)
    {
        vh.x += CalcDisturbance(gl_FragCoord);
    }

    Out_Color = vh;
}
