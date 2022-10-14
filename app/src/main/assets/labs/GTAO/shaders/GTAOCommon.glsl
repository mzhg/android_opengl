#version 310 es
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

uniform float4x4 Proj;
uniform float4  ProjInfo;
uniform float4 BufferSizeAndInvSize;
uniform float4 GTAOParams[5];

uniform float2  DepthUnpackConsts;
uniform float   InvTanHalfFov;
uniform float   AmbientOcclusionFadeRadius;
uniform float   AmbientOcclusionFadeDistance;
uniform float3  ProjDia;

const int SamplerPoint = 0;
const int SamplerLinear = 1;

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