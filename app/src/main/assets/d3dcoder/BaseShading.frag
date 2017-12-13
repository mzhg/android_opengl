#version 300 es
precision highp float;

in vec3 m_PositionWS;
in vec3 m_NormalWS;
in vec3 m_TangentWS;
in vec2 m_Texcoord;

uniform sampler2D g_InputTex;  // diffuse texture   slot 0
uniform samplerCube g_ReflectTex;  // reflect texture  slot 1
uniform sampler2D g_NormalMap;  // normal map slot 2
//uniform sampler2DShadow g_ShadowMap;

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

uniform bool g_AlphaClip;
uniform bool g_ReflectionEnabled;
uniform bool g_NormalMapEnabled;
//uniform bool g_UseShadowMap;

layout(location = 0) out vec4 Out_Color;

vec4 lit(float n_l, float r_v, vec4 C)
{
    vec3 color = g_LightAmbient * g_MaterialAmbient * C.rgb // ambient term
                +g_LightDiffuse * C.rgb * g_MaterialDiffuse * max(n_l, 0.0)
                +g_MaterialSpecular.rgb * g_LightSpecular * pow(max(r_v, 0.0), g_MaterialSpecular.a);

    return vec4(color, C.a);
}

//---------------------------------------------------------------------------------------
// Transforms a normal map sample to world space.
//---------------------------------------------------------------------------------------
vec3 NormalSampleToWorldSpace(vec3 normalMapSample, vec3 unitNormalW, vec3 tangentW)
{
	// Uncompress each component from [0,1] to [-1,1].
	vec3 normalT = normalize(2.0 * normalMapSample - 1.0);

	// Build orthonormal basis.
	vec3 N = unitNormalW;
	vec3 T = normalize(tangentW - dot(tangentW, N)*N);
	vec3 B = cross(N, T);

	mat3 TBN = mat3(T, B, N);
//	TBN = transpose(TBN); // Need this ?

	// Transform from tangent space to world space.
	vec3 bumpedNormalW = TBN * normalT;

	return bumpedNormalW;
}

void main()
{
    vec4 C;
    vec4 texColor = texture(g_InputTex, m_Texcoord);
    C.rgb = (g_Color.rgb + texColor.rgb);
    C.a = max(g_Color.a, texColor.a);

    if(g_AlphaClip)
    {
        // Discard pixel if texture alpha < 0.1.  Note that we do this
        // test as soon as possible so that we can potentially exit the shader
        // early, thereby skipping the rest of the shader code.
        if(C.a - 0.1 < 0.0)
            discard;
    }

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

    if(g_NormalMapEnabled)
    {
        const float bumpedSign = 1.0;
        vec3 normalMapSample = textureLod(g_NormalMap, m_Texcoord, 0.0).rgb;
        N = bumpedSign * NormalSampleToWorldSpace(normalMapSample, N, m_TangentWS);
    }

    vec3 R = reflect(-L, N);
    vec3 V = normalize(g_EyePos - m_PositionWS);

    float n_l = dot(N,  L);
    float r_v = dot(R,  V);

    Out_Color = lit(n_l, r_v, C);

    if( g_ReflectionEnabled )
    {
        R = reflect(-V, N);
        vec4 reflectionColor  = texture(g_ReflectTex, R);
        Out_Color.rgb += g_MaterialReflect * reflectionColor.rgb;
    }
}