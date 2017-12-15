#version 300 es
layout(location = 0) in vec4 In_Position;
layout(location = 1) in vec2 In_Texcoord;

uniform FrameData
{
    mat4 g_ViewProj;
    mat4 g_Models[10];
    mat4 g_NormalMats[10];
    mat4 g_TexTransform;
    mat4 g_LightViewProj;
};

out vec3 m_PositionWS;
out vec2 m_Texcoord0;
out vec2 m_Texcoord1;
out vec4 m_ShadowPosH;

uniform sampler2D g_WaterHeightMap;

void main()
{
    int instanceID = gl_InstanceID;
    m_PositionWS = (g_Models[instanceID] * In_Position).xyz;
    m_PositionWS.y += texture(g_WaterHeightMap, In_Texcoord).g;

    gl_Position = g_ViewProj * vec4(m_PositionWS, 1);
    m_Texcoord0 = vec2(g_TexTransform * vec4(In_Texcoord, 0, 1));
    m_Texcoord1 = In_Texcoord;

    m_ShadowPosH = g_LightViewProj * vec4(m_PositionWS, 1);
}