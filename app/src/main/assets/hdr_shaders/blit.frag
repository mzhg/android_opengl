#version 300 es

precision highp float;

uniform sampler2D uSourceTex;

in vec2 a_texCoord;

out vec4 FragColor;
void main(void)
{
    FragColor = texture(uSourceTex, a_texCoord);
}
