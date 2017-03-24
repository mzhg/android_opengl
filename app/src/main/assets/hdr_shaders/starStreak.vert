attribute vec3 PosAttribute;
attribute vec2 TexAttribute;

uniform vec2 stepSize;
uniform float Stride;

varying vec2 TexCoord1;
varying vec2 TexCoord2;
varying vec2 TexCoord3;
varying vec2 TexCoord4;
void main()
{
  TexCoord1 = TexAttribute;
  TexCoord2 = TexAttribute + stepSize*Stride;
  TexCoord3 = TexAttribute + stepSize*2.0*Stride;
  TexCoord4 = TexAttribute + stepSize*3.0*Stride;
  gl_Position = vec4(PosAttribute, 1.0);
}