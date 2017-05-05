#version 300 es
precision highp float;
uniform vec4 colorCoeff[4];
in vec2 TexCoord1;
in vec2 TexCoord2;
in vec2 TexCoord3;
in vec2 TexCoord4;

uniform sampler2D sampler;

out vec4 FragColor;

void main()
{
	FragColor = texture(sampler, TexCoord1)*colorCoeff[0]
				 + texture(sampler, TexCoord2)*colorCoeff[1]
				 + texture(sampler, TexCoord3)*colorCoeff[2]
				 + texture(sampler, TexCoord4)*colorCoeff[3];
}