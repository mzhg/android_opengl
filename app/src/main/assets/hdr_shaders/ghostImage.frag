#version 300 es
precision highp float;
uniform vec4 colorCoeff[4];

in vec2 TexCoord1;
in vec2 TexCoord2;
in vec2 TexCoord3;
in vec2 TexCoord4;

uniform sampler2D sampler1;
uniform sampler2D sampler2;
uniform sampler2D sampler3;
uniform sampler2D sampler4;

out vec4 gl_FragColor;
#define texture2D(x, y) texture(x, y)

void main()
{
	gl_FragColor = texture2D(sampler1, TexCoord1)*texture2D(sampler4, TexCoord1).g*colorCoeff[0] 
	             + texture2D(sampler1, TexCoord2)*texture2D(sampler4, TexCoord2).g*colorCoeff[1] 
	             + texture2D(sampler2, TexCoord3)*texture2D(sampler4, TexCoord3).g*colorCoeff[2] 
	             + texture2D(sampler3, TexCoord4)*texture2D(sampler4, TexCoord4).g*colorCoeff[3];

	gl_FragColor = min(vec4(256.0 * 256.0), gl_FragColor);
}