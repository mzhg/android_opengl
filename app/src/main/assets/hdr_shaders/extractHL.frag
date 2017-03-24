precision highp float;
varying vec2 a_texCoord;
uniform float	threshold;
uniform float	scalar;
uniform sampler2D sampler;
void main()
{
    vec4 sceneColor = texture2D(sampler, a_texCoord);
    sceneColor = min(vec4(256.0 * 256.0), sceneColor);
	gl_FragColor = max((sceneColor - threshold)*scalar, vec4(0.0,0.0,0.0,0.0));

}