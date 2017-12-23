#version 300 es

uniform float g_Level;
uniform mat4 g_ViewProj;
uniform mat4 g_ViewProjInv;

out vec3 m_WorldPos;
out flat bool m_AbovePlane;

void main()
{
    int id = gl_VertexID;

    vec2 UVAndScreenPos;
    UVAndScreenPos = vec2((id << 1) & 2, id & 2);
    vec4 projPos = vec4(UVAndScreenPos.xy * vec2(2,2) + vec2(-1,-1), 0, 1);

    vec4 worldPos = g_ViewProjInv * projPos;
    m_WorldPos = worldPos.xyz/worldPos.w;

    vec4 eyePos = g_ViewProjInv * vec4(0,0,-1,1);
    eyePos.xyz /= eyePos.w;

    m_AbovePlane = eyePos.y < g_Level;

    vec3 dirToWorldPos = normalize(m_WorldPos - eyePos.xyz);

//    vec3 dirToPlane = vec3(eyePos.x, g_Level, eyePos.z) - eyePos.xyz;
//    float H = length(dirToPlane);
    vec3 dirToPlane = vec3(0, m_AbovePlane ? -1:+1, 0);
    float H = abs(g_Level - eyePos.y);

    if(H > 0.0001)
    {
        float cosTheta = dot(dirToPlane, dirToWorldPos);
        float L = H/cosTheta;

        m_WorldPos = eyePos.xyz + dirToWorldPos * L;
        g_Position = g_ViewProj * vec4(m_WorldPos, 1);
    }
    else
    {
        g_Position = vec4(-1000, -1000, -1000, 1);  // discard the plane
    }

}