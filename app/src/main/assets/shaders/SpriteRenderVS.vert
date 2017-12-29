attribute vec2 aPosition;
attribute float aSize;

void main()
{
    gl_Position = vec4(aPosition,0, 1);
    gl_PointSize = aSize;
}