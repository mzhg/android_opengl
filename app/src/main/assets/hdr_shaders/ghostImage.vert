#version 300 es
in vec3 PosAttribute;
in vec2 TexAttribute;
uniform vec4 scalar;

out vec2 TexCoord1;
out vec2 TexCoord2;
out vec2 TexCoord3;
out vec2 TexCoord4;
void main()
{
  TexCoord1 = (TexAttribute - 0.5) * scalar[0] + 0.5;
  TexCoord2 = (TexAttribute - 0.5) * scalar[1] + 0.5;
  TexCoord3 = (TexAttribute - 0.5) * scalar[2] + 0.5;
  TexCoord4 = (TexAttribute - 0.5) * scalar[3] + 0.5;
  gl_Position = vec4(PosAttribute, 1.0);
}