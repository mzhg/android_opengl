#version 100

attribute vec4 aPosition;
varying vec4 UVAndScreenPos;

void main()
{	
	gl_Position = aPosition;
	UVAndScreenPos = vec4(0.5 * gl_Position.xy + 0.5, gl_Position.xy);
}