#version 300 es
precision highp float;

in vec2 a_texCoord;
uniform vec4 mixCoeff;

uniform sampler2D sampler1;
uniform sampler2D sampler2;
uniform sampler2D sampler3;
uniform sampler2D sampler4;

out vec4 FragColor;

void main()
{
	FragColor = texture(sampler1, a_texCoord)*mixCoeff.x
				 + texture(sampler2, a_texCoord)*mixCoeff.y
				 + texture(sampler3, a_texCoord)*mixCoeff.z
				 + texture(sampler4, a_texCoord)*mixCoeff.w;

    FragColor = min(vec4(256.0 * 256.0), FragColor);
}