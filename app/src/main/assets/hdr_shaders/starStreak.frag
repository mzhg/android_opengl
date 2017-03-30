#version 300 es
precision highp float;
uniform vec4 colorCoeff[4];
in vec2 TexCoord1;
in vec2 TexCoord2;
in vec2 TexCoord3;
in vec2 TexCoord4;

uniform sampler2D sampler;

out vec4 gl_FragColor;
#define texture2D(x, y) texture(x, y)

void main()
{
	gl_FragColor = texture2D(sampler, TexCoord1)*colorCoeff[0] 
				 + texture2D(sampler, TexCoord2)*colorCoeff[1] 
				 + texture2D(sampler, TexCoord3)*colorCoeff[2] 
				 + texture2D(sampler, TexCoord4)*colorCoeff[3];

    gl_FragColor = min(vec4(256.0 * 256.0), gl_FragColor);
}