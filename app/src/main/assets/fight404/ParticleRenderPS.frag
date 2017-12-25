#version 310 es
precision highp float;

in vec4 color;
layout(binding = 0) uniform sampler2D sprite_texture;
layout (location = 0) out vec4 fragColor;

void main( void)
{
	fragColor = texture(sprite_texture, gl_PointCoord) * color;
}