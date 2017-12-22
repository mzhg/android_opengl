#version 330

layout (location = 0) in vec3 a_pos;

/*uniform mat4 uMVP;
uniform float pointSize = 2.0f;*/

layout(binding = 0) uniform RenderFrame
{
    mat4 projection;
    mat4 modelView;

    float render_particle;
    float pointSize;
};

void main()
{
	gl_Position = projection * modelView * vec4(a_pos, 1.0);
	gl_PointSize = pointSize;
}