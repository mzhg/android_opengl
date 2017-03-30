#version 300 es
in vec3 PosAttribute;
in vec2 TexAttribute;
uniform vec2 twoTexelSize;

out vec2 TexCoord1;
out vec2 TexCoord2;
out vec2 TexCoord3;
out vec2 TexCoord4;
void main()
{
  TexCoord1 = TexAttribute;
  TexCoord2 = TexAttribute + vec2(twoTexelSize.x, 0);
  TexCoord3 = TexAttribute + vec2(twoTexelSize.x, twoTexelSize.y);
  TexCoord4 = TexAttribute + vec2(0, twoTexelSize.y);
  gl_Position = vec4(PosAttribute, 1.0);
}