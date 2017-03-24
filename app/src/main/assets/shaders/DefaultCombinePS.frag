#if GL_ES
precision highp float;
#if __VERSION__  >= 300
#define ENABLE_VERTEX_ID 1
#define ENABLE_IN_OUT_FEATURE 1
#endif

#if __VERSION__ >= 310
#define LAYOUT_LOC(x)  layout(location = x) out 
#else
#define LAYOUT_LOC(x)
#endif

#else

// The Desktop Platform, Almost all of the video drivers support the gl_VertexID, so just to enable it simply.
#define ENABLE_VERTEX_ID 1

#if __VERSION__ >= 130
#define ENABLE_IN_OUT_FEATURE 1
#endif

#if __VERSION__ >= 400
#define LAYOUT_LOC(x)  layout(location = x) 
#else
#define LAYOUT_LOC(x)
#endif

#endif

#ifdef __cplusplus
#define uniform
#define varying
#define texture(x, y) 
typedef int sampler2D;
struct float2
{
	float x, y;
};
struct float4
{
	float z, w;
	union
	{
		float2 xy;
		struct
		{
			float x;
		};
		struct
		{
			float y;
		};
	};
};

float4 _f4OutColor;
#define gl_FragColor _f4OutColor
typedef float4 vec4;
typedef float2 vec2;

extern float4 texture2D(sampler2D sampler, float2 location);
extern float4 operator+(const float4& a, const float4& b);
extern float4 operator*(const float4& a, const float& b);
#endif

#if ENABLE_IN_OUT_FEATURE
in vec4 ScreenSpaceUV;
LAYOUT_LOC(0) out vec4 f4FragColor;
#else
varying vec4 ScreenSpaceUV;
#define f4FragColor gl_FragColor
#define texture  texture2D
#endif

uniform sampler2D g_Texture0;
uniform sampler2D g_Texture1;

uniform vec2 g_f2Intensity;
void main()
{
	vec2 f2UV = ScreenSpaceUV.xy;

	vec4 f4Color0 = texture(g_Texture0, f2UV);
	vec4 f4Color1 = texture(g_Texture1, f2UV);

	f4FragColor = f4Color0 * g_f2Intensity.x + f4Color1 * g_f2Intensity.y;
}