attribute vec3 PosAttribute;
attribute vec2 TexAttribute;
uniform vec2 twoTexelSize;
varying vec2 TexCoord1;
varying vec2 TexCoord2;
varying vec2 TexCoord3;
varying vec2 TexCoord4;
void main()
{
  TexCoord1 = TexAttribute;
  TexCoord2 = TexAttribute + vec2(twoTexelSize.x, 0);
  TexCoord3 = TexAttribute + vec2(twoTexelSize.x, twoTexelSize.y);
  TexCoord4 = TexAttribute + vec2(0, twoTexelSize.y);
  gl_Position = vec4(PosAttribute, 1.0);
}