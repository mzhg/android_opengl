#version 300 es
layout(location = 0) in vec4 In_Position;
layout(location = 1) in vec2 In_Texcoord;
layout(location = 2) in vec3 In_Normal;

/*layout(binding = 0)*/ uniform FrameData
{
    mat4 g_ViewProj;
    mat4 g_Models[10];
    mat4 g_NormalMats[10];
};

out vec3 m_PositionWS;
out vec3 m_NormalWS;
out vec2 m_Texcoord;

void main()
{
    int instanceID = gl_InstanceID;
    m_PositionWS = (g_Models[instanceID] * In_Position).xyz;
    m_NormalWS = mat3(g_NormalMats[instanceID]) * In_Normal;
    gl_Position = g_ViewProj * vec4(m_PositionWS, 1);
    m_Texcoord = In_Texcoord;
}