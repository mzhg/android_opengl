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
out vec3 m_NormalWS;
out vec2 m_Texcoord;
out vec4 m_ShadowPosH;

uniform sampler2D g_WaterHeightMap;
uniform sampler2D g_WaterGradientMap;

void main()
{
    int instanceID = gl_InstanceID;
    m_PositionWS = (g_Models[instanceID] * In_Position).xyz;
    m_PositionWS.y += texture(g_WaterHeightMap, In_Texcoord).g;

    gl_Position = g_ViewProj * vec4(m_PositionWS, 1);
    m_Texcoord = vec2(g_TexTransform * vec4(In_Texcoord, 0, 1));

    vec2 vGradient = texture(g_WaterGradientMap, In_Texcoord).xy;
    vec3 N = normalize(vec3(vGradient.x,1, vGradient.y));
    m_NormalWS = mat3(g_NormalMats[instanceID]) * N;

    m_ShadowPosH = g_LightViewProj * vec4(m_PositionWS, 1);
}