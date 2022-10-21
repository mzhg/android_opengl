#include "GTAOCommon.glsl"

float2 TexturePosToBufferUV(int2 TexturePos)
{
    float2 BufferUV = (float2(TexturePos + ViewRectMin.xy) + float2(0.5f, 0.5f)) * BufferSizeAndInvSize.zw;

    return BufferUV;
}

float4 EncodeFloatRGBA(float v)
{
    float4 enc = float4(1.0, 255.0, 65025.0, 16581375.0) * v;
    float4 encValue = frac(enc);
    encValue -= encValue.yzww * float4(0.0039215686275f, 0.0039215686275f, 0.0039215686275f, 0.0f);
    return encValue;
}

float DecodeFloatRGBA(float4 rgba)
{
    return dot(rgba, float4(1.0, 0.0039215686275f, 1.53787e-5f, 6.03086294e-8f));
}

const int MAX_THREAD_GROUP_SIZE = THREADGROUP_SIZEX * THREADGROUP_SIZEY;
#define ARRAY_SIZE (MAX_THREAD_GROUP_SIZE * 2)  //THREADGROUP_SIZEX and THREADGROUP_SIZEY should be as larger as possible and at least 16

const int2 DEPTH_GROUP_THREAD_OFFSET = int2(1, 1);
const int DEPTH_THREADPOS_OFFSET = THREADGROUP_SIZEX + 2;
const int MAX_DEPTH_THREADS = (THREADGROUP_SIZEX + 2) * (THREADGROUP_SIZEY + 2);

#define HORIZONSEARCH_INTEGRAL_PIXEL_SHADER 0
#define SPATIALFILTER_PIXEL_SHADER          0
#define HORIZONSEARCH_INTEGRAL_SPATIALFILTER_COMPUTE_SHADER   0

shared float DeviceZArray[ARRAY_SIZE];

void SetZVal(float DeviceZ, int Index)
{
    DeviceZArray[Index] = DeviceZ;
}

float GetDeviceZFromSharedMemory(int2 ThreadPos)
{
    return DeviceZArray[ThreadPos.x + (ThreadPos.y * DEPTH_THREADPOS_OFFSET)];
}

float GetSceneDepthFromSharedMemory(int2 ThreadPos)
{
    return ScreenSpaceToViewSpaceDepth(GetDeviceZFromSharedMemory(ThreadPos));
}

const int2 AO_GROUP_THREAD_OFFSET = int2(2, 2);
const int AO_THREADPOS_OFFSET = THREADGROUP_SIZEX + 4;
const int MAX_AO_THREADS = (THREADGROUP_SIZEX + 4) * (THREADGROUP_SIZEY + 4);

shared float AOArray[ARRAY_SIZE];

void SetAOVal(float AO, int Index)
{
    AOArray[Index] = AO;
}

float GetAOValueFromSharedMemory(int2 ThreadPos)
{
    ThreadPos += AO_GROUP_THREAD_OFFSET;
    return AOArray[ThreadPos.x + (ThreadPos.y * AO_THREADPOS_OFFSET)];
}

//RWTexture2D<float4> OutTexture;
writeonly layout(rgba8, binding = 0) uniform image2D OutTexture;

#if SHADER_QUALITY == 0
// very low
#define GTAO_NUMTAPS 4
#define GTAO_BIASMIPLEVEL 2
#define GTAO_MAX_PIXEL_SCREEN_RADIUS 256.0f
#elif SHADER_QUALITY == 1
// low
#define GTAO_NUMTAPS 6
#define GTAO_BIASMIPLEVEL 1
#define GTAO_MAX_PIXEL_SCREEN_RADIUS 256.0f
#elif SHADER_QUALITY == 2
// medium
#define GTAO_NUMTAPS 8
#define GTAO_BIASMIPLEVEL 0
#define GTAO_MAX_PIXEL_SCREEN_RADIUS 256.0f
#elif SHADER_QUALITY == 3
// high
#define GTAO_NUMTAPS 12
#define GTAO_BIASMIPLEVEL 0
#define GTAO_MAX_PIXEL_SCREEN_RADIUS 256.0f
#else // SHADER_QUALITY == 4
// very high
#define GTAO_NUMTAPS 20
#define GTAO_BIASMIPLEVEL 0
#define GTAO_MAX_PIXEL_SCREEN_RADIUS 256.0f
#endif

const float PI_float = (PI*0.5);
const float LUTSize = 16.0;

layout(binding = 0) uniform sampler2D SceneDepthTexture;

float GetDeviceZFromAOInput(float2 TextureUV)
{
//    return Texture2DSample(SceneDepthTexture, SceneDepthSampler, TextureUV).r;
    return textureLod(SceneDepthTexture, TextureUV, 0.0).DEPTH_COMP;
}

float GetSceneDepthFromAOInput(float2 TextureUV)
{
    return ScreenSpaceToViewSpaceDepth(GetDeviceZFromAOInput(TextureUV));
}

float3 GetViewSpacePosFromAOInput(float2 UV)
{
    float SceneDepth = GetSceneDepthFromAOInput(UV);

    return ScreenToViewPos(UV, SceneDepth);
}

float TakeSmallerAbsDelta(float left, float mid, float right)
{
    float a = mid - left;
    float b = right - mid;

    return (abs(a) < abs(b)) ? a : b;
}

float3 GetNormal(float2 UV, int2 ThreadPos, float3 ViewSpacePosMid)
{
    float3 ViewSpaceNormal;

#if USE_NORMALBUFFER

    // Get the normal from the normal buffer
    float3 WorldNormal = Texture2DSample(NormalTexture, NormalSampler, UV).xyz;
    ViewSpaceNormal = normalize(mul(WorldNormal, (float3x3)ResolvedView.TranslatedWorldToView));

#else
    // Get the normal derived from the depth buffer
    float2 XOffset = float2(BufferSizeAndInvSize.z, 0.0f);
    float2 YOffset = float2(0.0f, BufferSizeAndInvSize.w);

    int2 iXOffset = int2(1, 0);
    int2 iYOffset = int2(0, 1);
    int2 ThreadOffsetPos = ThreadPos + DEPTH_GROUP_THREAD_OFFSET;
    float DeviceZ = GetDeviceZFromSharedMemory(ThreadOffsetPos);
    float DeviceZLeft = GetDeviceZFromSharedMemory(ThreadOffsetPos - iXOffset);
    float DeviceZTop = GetDeviceZFromSharedMemory(ThreadOffsetPos - iYOffset);
    float DeviceZRight = GetDeviceZFromSharedMemory(ThreadOffsetPos + iXOffset);
    float DeviceZBottom = GetDeviceZFromSharedMemory(ThreadOffsetPos + iYOffset);

    float DeviceZDdx = TakeSmallerAbsDelta(DeviceZLeft, DeviceZ, DeviceZRight);
    float DeviceZDdy = TakeSmallerAbsDelta(DeviceZTop, DeviceZ, DeviceZBottom);

    float ZRight = ScreenSpaceToViewSpaceDepth(DeviceZ + DeviceZDdx);
    float ZDown = ScreenSpaceToViewSpaceDepth(DeviceZ + DeviceZDdy);

    float3 Right = ScreenToViewPos(UV + XOffset, ZRight) - ViewSpacePosMid;
    float3 Down = ScreenToViewPos(UV + YOffset, ZDown) - ViewSpacePosMid;

    ViewSpaceNormal = normalize(cross(Right, Down));
#endif

    return ViewSpaceNormal;
}

// max absolute error 9.0x10^-3
// Eberly's polynomial degree 1 - respect bounds
// 4 VGPR, 12 FR (8 FR, 1 QR), 1 scalar
// input [-1, 1] and output [0, PI]
float acosFast(float inX)
{
    float x = abs(inX);
    float res = -0.156583 * x + (0.5 * PI);
    res *= sqrt(1.0 - x);
    return (inX >= 0.0) ? res : PI - res;
}
