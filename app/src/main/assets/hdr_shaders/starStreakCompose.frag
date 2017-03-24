precision highp float;
varying vec2 TexCoord;
uniform vec4 coeff;
uniform sampler2D sampler1;
uniform sampler2D sampler2;
uniform sampler2D sampler3;
uniform sampler2D sampler4;
void main()
{
  vec4 color1 = max(texture2D(sampler1, TexCoord), texture2D(sampler2, TexCoord));
  vec4 color2 = max(texture2D(sampler3, TexCoord), texture2D(sampler4, TexCoord));
  gl_FragColor = max(color1, color2);
  gl_FragColor = min(vec4(256.0 * 256.0), gl_FragColor);
}