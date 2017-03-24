#version 300 es

in vec3 PosAttribute;
in vec2 TexAttribute;

out vec3 a_texCoord;
uniform mat4 g_mvp;

void main()
{
    gl_Position = g_mvp * vec4(PosAttribute, 1.0);
    a_texCoord = vec3(PosAttribute.x, PosAttribute.yz);
}