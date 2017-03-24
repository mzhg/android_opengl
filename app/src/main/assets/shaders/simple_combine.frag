#version 300 es

precision highp float;
in vec4 UVAndScreenPos;

uniform sampler2D g_Texture0;
uniform sampler2D g_Texture1;

uniform vec2 g_f2Intensity;

out vec4 gl_FragColor;

void main()
{
    vec2 f2UV = UVAndScreenPos.xy;

	vec4 f4Color0 = texture(g_Texture0, f2UV);
	vec4 f4Color1 = texture(g_Texture1, f2UV);

//    float lumiance = dot(vec3(1), f4Color0.rgb)/3.0;
//    lumiance
	gl_FragColor = f4Color0 * g_f2Intensity.x + f4Color1 * g_f2Intensity.y;
}