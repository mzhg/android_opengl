precision highp float;

uniform sampler2D sprite_texture;

void main( void)
{
	gl_FragColor = texture2D(sprite_texture, gl_PointCoord);
//	gl_FragColor = vec4(1);
}