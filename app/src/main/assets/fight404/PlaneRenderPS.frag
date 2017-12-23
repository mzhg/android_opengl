#version 300 es
precision highp float;

in vec3 m_WorldPos;
in flat bool m_AbovePlane;

layout (location = 0) out vec4 fragColor;
uniform sampler2D sprite_texture;

void main()
{
	fragColor = texture(sprite_texture, gl_PointCoord);
}

