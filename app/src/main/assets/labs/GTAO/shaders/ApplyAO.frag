#version 300 es
precision highp float;

in vec4 UVAndScreenPos;

layout(binding = 0) sampler2D TextureAO;

out vec4 OutColor;

void main()
{
    float AO = textureLod(TextureAO, UVAndScreenPos.xy, 0.0).x;
    OutColor = vec4(AO);
}