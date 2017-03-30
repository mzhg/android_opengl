#version 300 es

in vec3 PosAttribute;
in vec2 TexAttribute;

uniform vec2 stepSize;
uniform float Stride;

out vec2 TexCoord1;
out vec2 TexCoord2;
out vec2 TexCoord3;
out vec2 TexCoord4;

void main()
{
  TexCoord1 = TexAttribute;
  TexCoord2 = TexAttribute + stepSize*Stride;
  TexCoord3 = TexAttribute + stepSize*2.0*Stride;
  TexCoord4 = TexAttribute + stepSize*3.0*Stride;
  gl_Position = vec4(PosAttribute, 1.0);
}