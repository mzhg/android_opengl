#version 310 es

#extension GL_EXT_shader_io_blocks : enable

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

in layout(location=VERTEX_POS)    vec3 pos;
in layout(location=VERTEX_NORMAL) vec3 normal;
in layout(location=VERTEX_COLOR)  vec4 color;
in layout(location=INSTANCE_OFFSET)  vec3 offset;
in layout(location=INSTANCE_SCALE)  vec3 scale;

out Interpolants {
  vec3 pos;
  vec3 normal;
  flat vec4 color;
} OUT;

out gl_PerVertex
{
	vec4 gl_Position;
};

void main()
{
  vec3 new_pos = pos * scale + offset;
  gl_Position = scene.viewProjMatrix * vec4(new_pos,1);
  OUT.pos = new_pos;
  OUT.normal = normal;
  OUT.color = color;
}