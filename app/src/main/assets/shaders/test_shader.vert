uniform mat4 g_Mat;
attribute vec4  g_Position;
attribute vec2  g_TextureCoord;
varying   vec2  vTextureCoord;

void main(){
    vTextureCoord = g_TextureCoord;
    gl_Position = g_Mat*g_Position;
}