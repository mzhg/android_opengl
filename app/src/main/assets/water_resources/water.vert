#version 300 es

in vec3 PosAttribute;
in vec2 TexAttribute;

out vec2 a_texCoord;

uniform sampler2D WaterHeightMap;
uniform mat4 g_mvp;

out vec3 Position;

void main()
{
	a_texCoord = vec2(PosAttribute.x * 0.5 + 0.5, 0.5 - PosAttribute.z * 0.5);
	Position = PosAttribute;
	Position.y += texture(WaterHeightMap, a_texCoord).g;
	gl_Position = g_mvp * vec4(Position, 1.0);
}