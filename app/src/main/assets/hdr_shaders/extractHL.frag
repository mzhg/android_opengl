#version 300 es
precision highp float;
in vec2 a_texCoord;
uniform float	threshold;
uniform float	scalar;
uniform sampler2D sampler;

out vec4 gl_FragColor;
#define texture2D(x, y) texture(x, y)
void main()
{
    vec4 sceneColor = texture2D(sampler, a_texCoord);
    sceneColor = min(vec4(256.0 * 256.0), sceneColor);
	gl_FragColor = max((sceneColor - threshold)*scalar, vec4(0.0,0.0,0.0,0.0));

}