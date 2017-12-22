#version 310 es
precision highp float;

layout (location = 0) out vec4 fragColor;
uniform sampler2D sprite_texture;

void main()
{
	fragColor = texture(sprite_texture, gl_PointCoord);
}