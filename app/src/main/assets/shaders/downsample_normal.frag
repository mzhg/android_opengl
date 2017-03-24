#version 300 es

precision highp float;
in vec4 UVAndScreenPos;

uniform vec2 g_TexelSize;
uniform sampler2D g_Texture;

out vec4 gl_FragColor;

void main()
{
    vec2 texelLocation = UVAndScreenPos.xy;
    vec2 texelSampleLoc1 = texelLocation + vec2(-1, -1) * g_TexelSize;
    vec2 texelSampleLoc2 = texelLocation + vec2(+1, -1) * g_TexelSize;
    vec2 texelSampleLoc3 = texelLocation + vec2(-1, +1) * g_TexelSize;
    vec2 texelSampleLoc4 = texelLocation + vec2(+1, +1) * g_TexelSize;

    vec4 color1 = texture(g_Texture, texelSampleLoc1);
    vec4 color2 = texture(g_Texture, texelSampleLoc2);
    vec4 color3 = texture(g_Texture, texelSampleLoc3);
    vec4 color4 = texture(g_Texture, texelSampleLoc4);

    gl_FragColor = (color1 + color2 + color3 + color4) * 0.25;
}