#version 300 es
precision highp float;
in vec2 TexCoord;
uniform vec4 coeff;
uniform sampler2D sampler1;
uniform sampler2D sampler2;
uniform sampler2D sampler3;
uniform sampler2D sampler4;

out vec4 FragColor;

void main()
{
  vec4 color1 = max(texture(sampler1, TexCoord), texture(sampler2, TexCoord));
  vec4 color2 = max(texture(sampler3, TexCoord), texture(sampler4, TexCoord));
  FragColor = max(color1, color2);
}