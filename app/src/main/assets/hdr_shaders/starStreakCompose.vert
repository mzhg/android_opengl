attribute vec3 PosAttribute;
attribute vec2 TexAttribute;
varying vec2 TexCoord;
void main()
{
	gl_Position = vec4(PosAttribute, 1.0);
	TexCoord    = TexAttribute;
}