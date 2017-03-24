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

#if 0  // The UE4 Shader Code.
// x:BloomThreshold, yz:unused, w:ExposureScale (useful if eyeadaptation is locked)
float4 BloomThreshold;
// -----------------------------
// bloom threshold
void MainPS(
	noperspective float4 UVAndScreenPos : TEXCOORD0,
	nointerpolation float InExposureScale : TEXCOORD1,
	out float4 OutColor : SV_Target0)
{
	float2 UV = UVAndScreenPos.xy;

	half4 SceneColor = Texture2DSample(PostprocessInput0, PostprocessInput0Sampler, UV);

	// clamp to avoid artifacts from exceeding fp16 through framebuffer blending of multiple very bright lights
	SceneColor.rgb = min(float3(256 * 256, 256 * 256, 256 * 256), SceneColor.rgb);

	half3 LinearColor = SceneColor.rgb;

	float ExposureScale = InExposureScale;

#if NO_EYEADAPTATION_EXPOSURE_FIX 
	ExposureScale = BloomThreshold.w;
#endif

	// todo: make this adjustable (e.g. LUT)
	half TotalLuminance = Luminance(LinearColor) * ExposureScale;
	half BloomLuminance = TotalLuminance - BloomThreshold.x;
	// mask 0..1
	half BloomAmount = saturate(BloomLuminance / 2.0f);

	OutColor = float4(BloomAmount * LinearColor, 0);
}
#endif

// x:BloomThreshold, y:ExposureScale (useful if eyeadaptation is locked)
uniform vec2 f2BloomThreshold;
uniform sampler2D g_Texture;
void main()
{
	vec2 f2UV = ScreenSpaceUV.xy;

	vec4 f4SceneColor = texture(g_Texture, f2UV);

	// clamp to avoid artifacts from exceeding fp16 through framebuffer blending of multiple very bright lights
	f4SceneColor.rgb = min(vec3(256 * 256, 256 * 256, 256 * 256), f4SceneColor.rgb);

	vec3 f3LinearColor = f4SceneColor.rgb;

	float fExposureScale = f2BloomThreshold.y;

	// todo: make this adjustable (e.g. LUT)
	float fTotalLuminance = dot(f3LinearColor, vec3(0.3, 0.59, 0.11)) * fExposureScale;
	float fBloomLuminance = fTotalLuminance - f2BloomThreshold.x;
	// mask 0..1
	float fBloomAmount = clamp(fBloomLuminance / 2.0, 0.0, 1.0);

	f4FragColor = vec4(fBloomAmount * f3LinearColor, f4SceneColor.a);
}