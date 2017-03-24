
precision highp float;
varying vec2 TexCoord1;
varying vec2 TexCoord2;
varying vec2 TexCoord3;
varying vec2 TexCoord4;
uniform sampler2D sampler;
void main()
{
		gl_FragColor = (texture2D(sampler, TexCoord1) + 
            				texture2D(sampler, TexCoord2) +
            				texture2D(sampler, TexCoord3) +
            				texture2D(sampler, TexCoord4))*0.25;
}