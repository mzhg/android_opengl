attribute vec3 PosAttribute;
attribute vec2 TexAttribute;

varying vec2 a_texCoord;

uniform mat4 g_MVP;

void main()
{
    gl_Position = g_MVP * vec4(PosAttribute, 1.0);
    a_texCoord = TexAttribute;
}