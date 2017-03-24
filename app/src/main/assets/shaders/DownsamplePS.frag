
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
#endif

#if ENABLE_IN_OUT_FEATURE
in vec4 ScreenSpaceUV;
LAYOUT_LOC(0) out vec4 f4FragColor;
#else
varying vec4 ScreenSpaceUV;
#define f4FragColor gl_FragColor
#define texture  texture2D
#endif

#define METHOD 1

#if 0
half4 PS_Downsample(DownsampleVertexOutput IN,
	uniform sampler2D OrigSampler,
	uniform float HighlightThreshold
	) : COLOR{
	half4 c;
#if 0
	// sub sampling
	c = tex2D(OrigSampler, IN.TexCoord[0]);
#else
	// box filter
	c = tex2D(OrigSampler, IN.TexCoord[0]) * 0.25;
	c += tex2D(OrigSampler, IN.TexCoord[1]) * 0.25;
	c += tex2D(OrigSampler, IN.TexCoord[2]) * 0.25;
	c += tex2D(OrigSampler, IN.TexCoord[3]) * 0.25;
#endif

	// store hilights in alpha
	c.a = highlights(c.rgb, HighlightThreshold);

	return c;
}
#endif

uniform vec2 g_TexelSize;
uniform sampler2D g_Texture;

// METHOD: 0, fastest; 1, normal, 2; combined_depth.
void main()
{
#if METHOD == 0
	f4FragColor = texture(g_Texture, ScreenSpaceUV.xy);
#elif METHOD == 1
	vec2 texelLocation = ScreenSpaceUV.xy;
	vec2 texelSampleLoc1 = texelLocation + vec2(-1, -1) * g_TexelSize;
	vec2 texelSampleLoc2 = texelLocation + vec2(+1, -1) * g_TexelSize;
	vec2 texelSampleLoc3 = texelLocation + vec2(-1, +1) * g_TexelSize;
	vec2 texelSampleLoc4 = texelLocation + vec2(+1, +1) * g_TexelSize;

	vec4 color1 = texture(g_Texture, texelSampleLoc1);
	vec4 color2 = texture(g_Texture, texelSampleLoc2);
	vec4 color3 = texture(g_Texture, texelSampleLoc3);
	vec4 color4 = texture(g_Texture, texelSampleLoc4);

	f4FragColor = (color1 + color2 + color3 + color4) * 0.25;
#elif METHOD == 2
	vec2 texelLocation = ScreenSpaceUV.xy;
	vec2 texelSampleLoc1 = texelLocation + vec2(-1, -1) * g_TexelSize;
	vec2 texelSampleLoc2 = texelLocation + vec2(+1, -1) * g_TexelSize;
	vec2 texelSampleLoc3 = texelLocation + vec2(-1, +1) * g_TexelSize;
	vec2 texelSampleLoc4 = texelLocation + vec2(+1, +1) * g_TexelSize;

	vec4 color1 = texture(g_Texture, texelSampleLoc1);
	vec4 color2 = texture(g_Texture, texelSampleLoc2);
	vec4 color3 = texture(g_Texture, texelSampleLoc3);
	vec4 color4 = texture(g_Texture, texelSampleLoc4);

	f4FragColor.rgb = (color1.xyz + color2.xyz + color3.xyz + color4.xyz) * 0.25;
	f4FragColor.a = texture(g_Texture, texture).a;
#endif
}