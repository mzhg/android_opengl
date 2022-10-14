#version 310 es

#extension GL_EXT_shader_io_blocks : enable
#extension GL_EXT_blend_func_extended : enable

precision mediump float;

#define VERTEX_POS    0
#define VERTEX_NORMAL 1
#define VERTEX_COLOR  2
#define INSTANCE_OFFSET 3
#define INSTANCE_SCALE 4

#define UBO_SCENE     0

struct SceneData {
  mat4  viewProjMatrix;
  mat4  viewMatrix;
  mat4  viewMatrixIT;

  vec2 viewport;
  vec2 _pad;
};

layout(std140,binding=UBO_SCENE) uniform sceneBuffer {
  SceneData   scene;
};

in Interpolants {
  vec3 pos;
  vec3 normal;
  flat vec4 color;
} IN;

layout(location=0,index=0) out vec4 out_Color;

void main()
{
  vec3  light = normalize(vec3(-1,2,1));
  float intensity = dot(normalize(IN.normal),light) * 0.5 + 0.5;
  vec4  color = IN.color * mix(vec4(0,0.25,0.75,0),vec4(1,1,1,0),intensity);
  
  out_Color = color;
}