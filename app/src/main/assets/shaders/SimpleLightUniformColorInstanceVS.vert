#version 300 es
layout(location = 0) in vec4 In_Position;
layout(location = 1) in vec2 In_Texcoord;
layout(location = 2) in vec3 In_Normal;
layout(location = 3) in vec3 In_Tangent;

/*layout(binding = 0)*/ uniform FrameData
{
    mat4 g_ViewProj;
    mat4 g_Models[10];
    mat4 g_NormalMats[10];
    mat4 g_TexTransform;
    mat4 g_LightViewProj;
};

out vec3 m_PositionWS;
out vec3 m_NormalWS;
out vec3 m_TangentWS;
out vec2 m_Texcoord;
out vec4 m_ShadowPosH;

void main()
{
    int instanceID = gl_InstanceID;
    m_PositionWS = (g_Models[instanceID] * In_Position).xyz;
    m_NormalWS = mat3(g_NormalMats[instanceID]) * In_Normal;
    m_TangentWS = mat3(g_Models[instanceID]) * In_Tangent;

    gl_Position = g_ViewProj * vec4(m_PositionWS, 1);
    m_Texcoord = vec2(g_TexTransform * vec4(In_Texcoord, 0, 1));

    m_ShadowPosH = g_LightViewProj * vec4(m_PositionWS, 1);
//    m_ShadowPosH /= m_ShadowPosH.w;
//    m_ShadowPosH.xyz = m_ShadowPosH.xyz * 0.5 + 0.5;
}