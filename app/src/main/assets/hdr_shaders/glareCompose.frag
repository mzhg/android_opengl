#version 300 es
precision highp float;

in vec2 a_texCoord;
uniform vec4 mixCoeff;

uniform sampler2D sampler1;
uniform sampler2D sampler2;
uniform sampler2D sampler3;
uniform sampler2D sampler4;

out vec4 gl_FragColor;
#define texture2D(x, y) texture(x, y)

void main()
{
	gl_FragColor = texture2D(sampler1, a_texCoord)*mixCoeff.x 
				 + texture2D(sampler2, a_texCoord)*mixCoeff.y 
				 + texture2D(sampler3, a_texCoord)*mixCoeff.z
				 + texture2D(sampler4, a_texCoord)*mixCoeff.w;

    gl_FragColor = min(vec4(256.0 * 256.0), gl_FragColor);
}