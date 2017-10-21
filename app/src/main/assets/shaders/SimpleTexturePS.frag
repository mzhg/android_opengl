#ifdef GL_ES
precision mediump float;
#endif

varying vec2  vTextureCoord;
uniform sampler2D sparrow;

void main(){
	gl_FragColor = texture2D(sparrow, vTextureCoord);
}