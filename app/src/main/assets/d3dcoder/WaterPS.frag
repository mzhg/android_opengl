#version 300 es
precision mediump float;

in vec3 m_PositionWS;
in vec3 m_NormalWS;
in vec2 m_Texcoord;
in vec4 m_ShadowPosH;

uniform vec4 g_LightPos;   // w==0, means light direction, must be normalized

uniform vec3 g_LightAmbient;   // Ia
uniform vec3 g_LightDiffuse;   // Il
uniform vec3 g_LightSpecular;  // rgb = Cs * Il,

uniform vec3 g_MaterialAmbient;   // ka
uniform vec3 g_MaterialDiffuse;   // kb
uniform vec4 g_MaterialSpecular;   // ks, w for power
uniform vec3 g_MaterialReflect;
uniform vec3 g_EyePos;
uniform vec4 g_Color;

uniform sampler2D g_InputTex;

layout(location = 0) out vec4 Out_Color;

vec4 lit(float n_l, float r_v, vec4 C)
{
    vec3 color = g_LightAmbient * g_MaterialAmbient * C.rgb // ambient term
                +g_LightDiffuse * C.rgb * g_MaterialDiffuse * max(n_l, 0.0)
                +g_MaterialSpecular.rgb * g_LightSpecular * pow(max(r_v, 0.0), g_MaterialSpecular.a);

    return vec4(color, C.a);
}

void main()
{
    vec4 C = texture(g_InputTex, m_Texcoord)*g_Color;

    vec3 L;  // light direction
    if(g_LightPos.w == 0.0)
    {
        L = g_LightPos.xyz;
    }
    else
    {
        L = normalize(g_LightPos.xyz-m_PositionWS);
    }

    vec3 N = normalize(m_NormalWS);
    vec3 R = reflect(-L, N);
    vec3 V = normalize(g_EyePos - m_PositionWS);

    float n_l = dot(N,  L);
    float r_v = dot(R,  V);

    Out_Color = lit(n_l, r_v, C);
}