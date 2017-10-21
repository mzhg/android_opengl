uniform mat4 g_Mat;
attribute vec4  In_Position;
attribute vec2  In_TexCoord;
varying   vec2  vTextureCoord;

void main(){
    vTextureCoord = In_TexCoord;
    gl_Position = g_Mat*In_Position;
}