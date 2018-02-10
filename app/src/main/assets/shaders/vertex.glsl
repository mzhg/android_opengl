// Distance map contour texturing according to Green (2007),
// implementation by Stefan Gustavson 2011.
// This code is in the public domain.

attribute vec3 In_Position;
attribute vec2 In_Texcoord;

uniform mat4 g_MVP;

varying vec2 st;

void main( void )
{
  // Get the texture coordinates
  st = vec2(In_Texcoord.x, 1.0 - In_Texcoord.y);
  gl_Position = g_MVP * vec4(In_Position,1);
}
