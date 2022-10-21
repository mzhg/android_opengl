#version 310 es
#extension GL_EXT_shader_io_blocks : enable
#extension GL_EXT_blend_func_extended : enable

precision highp float;
precision highp sampler2D;
precision highp sampler2DArray;
precision highp image2D;
precision highp image2DArray;

#include "../../../shader_libs/HLSLCompatiable.glsl"


#ifndef PI
#define PI 3.1415926
#endif

#define PI_HALF (PI*0.5)
#define USE_NORMALBUFFER 0

#if 0
uniform float4x4 Proj;
uniform float4  ProjInfo;
uniform float4 BufferSizeAndInvSize;
uniform float4 GTAOParams[5];

uniform float2  DepthUnpackConsts;
uniform float   InvTanHalfFov;
uniform float   AmbientOcclusionFadeRadius;
uniform float   AmbientOcclusionFadeDistance;
uniform float3  ProjDia;

#else

layout(std140,binding=0) uniform GTAOBuffer {
    float4x4 Proj;
    float4  ProjInfo;
    float4 BufferSizeAndInvSize;
    float4 GTAOParams[5];

    float4  WorldRadiusAdj_SinDeltaAngle_CosDeltaAngle_Thickness;
    float4  FadeRadiusMulAdd_FadeDistance_AttenFactor;
    float4   ViewSizeAndInvSize;

    float2  DepthUnpackConsts;
    float   InvTanHalfFov;
    float   AmbientOcclusionFadeRadius;

    float3  ProjDia;
    float   AmbientOcclusionFadeDistance;

    float2  Power_Intensity_ScreenPixelsToSearch;
    int2   ViewRectMin;
};

#endif

const int SamplerPoint = 0;
const int SamplerLinear = 1;

#ifndef DEPTH_COMP
#define DEPTH_COMP  a
#endif

#define Texture2DSampleLevel(T, S, UV, Lod) textureLod(T, UV, Lod)

// [0] - { cos(TemporalAngle), sin(TemporalAngle), TemporalOffset, FrameTemporalOffset}
// [1] - { FrameNumber, Thicknessblend, unused, unused}
// [2] - { TargetSizeX, TargetSizeY, 1.0/TargetSizeX, 1.0f/TargetSizeY}
// [3] - { FallOffStart, FallOffEnd, FalloffScale, FalloffBias}
// [4] - { Temporal Blend Weight, Angles, SinDeltaAngle, CosDeltaAngle}


float ScreenSpaceToViewSpaceDepth(float screenDepth)
{
    float mZFar =DepthUnpackConsts.x;
    float mZNear = DepthUnpackConsts.y;
    float fCamSpaceZ = mZFar*mZNear/(mZFar-screenDepth*(mZFar-mZNear));
    return fCamSpaceZ;
}

float3 ScreenToViewPos(float2 uv, float eye_z)
{
    float2 _uv = 2.0 * uv - 1.0;
    return float3((_uv + ProjInfo.xy) * ProjInfo.zw * eye_z, -eye_z);
//    return float3((uv * ProjInfo.xy + ProjInfo.zw) * eye_z, eye_z);
}

uint EncodeNormal(float3 Normal)
{
    return packSnorm2x16(Normal.xy);
}

float3 DecodeNormal(uint v)
{
    vec2 Nxy = unpackSnorm2x16(v);
    float Nz = sqrt(1.0 - dot(Nxy, Nxy));

    return float3(Nxy, Nz);
}

uint EncodeAOZ(float AO, float Z)
{
    return packHalf2x16(float2(AO, Z));
}

float2 DecodeAOZ(uint AOZ)
{
    return unpackHalf2x16(AOZ);
}

float InterleavedGradientNoise(float2 iPos)
{
    return frac(52.9829189f * frac((iPos.x * 0.06711056) + (iPos.y * 0.00583715)));
}

float2 GetRandomAngleOffset(uint2 iPos)
{
    iPos.y = 4096u - iPos.y;
    float Angle = InterleavedGradientNoise(float2(iPos));
    float Offset = (1.0 / 4.0) * float((iPos.y - iPos.x) & 3u);
    return float2(Angle, Offset);
}

float3 GetRandomVector(uint2 iPos)
{
    iPos.y = 16384u - iPos.y;

    float3 RandomVec = float3(0, 0, 0);
    float3 RandomTexVec = float3(0, 0, 0);
    float ScaleOffset;

    float TemporalCos = GTAOParams[0].x;
    float TemporalSin = GTAOParams[0].y;

    float GradientNoise = InterleavedGradientNoise(float2(iPos));

    RandomTexVec.x = cos((GradientNoise * PI));
    RandomTexVec.y = sin((GradientNoise * PI));

    ScaleOffset = (1.0 / 4.0) * float((iPos.y - iPos.x) & 3u);
    //	ScaleOffset = (1.0/5.0)  *  (( iPos.y - iPos.x) % 5);

    RandomVec.x = dot(RandomTexVec.xy, float2(TemporalCos, -TemporalSin));
    RandomVec.y = dot(RandomTexVec.xy, float2(TemporalSin, TemporalCos));
    RandomVec.z = frac(ScaleOffset + GTAOParams[0].z);

    return RandomVec;
}