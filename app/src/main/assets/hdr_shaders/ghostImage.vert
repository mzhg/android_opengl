attribute vec3 PosAttribute;
attribute vec2 TexAttribute;
uniform vec4 scalar;
varying vec2 TexCoord1;
varying vec2 TexCoord2;
varying vec2 TexCoord3;
varying vec2 TexCoord4;
void main()
{
  TexCoord1 = (TexAttribute - 0.5) * scalar[0] + 0.5;
  TexCoord2 = (TexAttribute - 0.5) * scalar[1] + 0.5;
  TexCoord3 = (TexAttribute - 0.5) * scalar[2] + 0.5;
  TexCoord4 = (TexAttribute - 0.5) * scalar[3] + 0.5;
  gl_Position = vec4(PosAttribute, 1.0);
}