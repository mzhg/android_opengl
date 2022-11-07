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
    float4 DepthBufferSizeAndInvSize;
    float4 BufferSizeAndInvSize;   // Viewport
    float4 GTAOParams[5];

    float4 Jitters[16];

    float4  WorldRadiusAdj_SinDeltaAngle_CosDeltaAngle_Thickness;
    float4  FadeRadiusMulAdd_FadeDistance_AttenFactor;
//    float4   ViewSizeAndInvSize;

    float2  DepthUnpackConsts;
    float   InvTanHalfFov;
    float   AmbientOcclusionFadeRadius;

    float3  ProjDia;
    float   AmbientOcclusionFadeDistance;

    float2  Power_Intensity_ScreenPixelsToSearch;
    int2   ViewRectMin;

    float  HBAO_WorldRadius;
    float  HBAO_NDotVBiase;
    float  HBAO_Multiplier;
    float  HBAO_NegInvRadiusSq;
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

const vec4 gJitters[16] = vec4[16](vec4(-0.115748,-0.993279,0.047800,0.0),vec4(-0.314060,-0.949403,0.576102,0.0),
                                   vec4(-0.248854,0.968541,-0.062941,0.0),vec4(0.678797,-0.734326,0.083099,0.0),
      vec4(-0.656423,0.754393,-0.322346,0.000000),vec4(0.993757,-0.111562,-0.266790,0.000000),
       vec4(0.807327,0.590104,0.563857,0.000000),vec4(-0.733109,-0.680111,-0.109045,0.000000),
       vec4(0.973889,-0.227023,0.674573,0.000000),vec4(0.831890,0.554941,0.928456,0.000000),
        vec4(-0.990372,0.138428,-0.617441,0.000000),vec4(0.649674,-0.760213,0.195499,0.000000),
        vec4(-0.006242,0.999981,0.325589,0.000000),vec4(0.976268,-0.216568,-0.488291,0.000000),
      vec4(-0.720332,-0.693629,0.736083,0.000000),vec4(-0.716976,0.697098,0.409543,0.000000));

float3 GetJitter(int arraySlice){
    return gJitters[arraySlice & 15].xyz;
}

float3 GetJitter(int2 PixelPos){
    int2 LocalPos = PixelPos & int2(3);
    int index = LocalPos.x * 4 + LocalPos.y;
    return gJitters[index].xyz;
}

/// ----------------------- HBAO Code ----------------------------
float Falloff(float DistanceSquare)
{
    // fade radius inverse.
    // 1 scalar mad instruction
    return DistanceSquare * HBAO_NegInvRadiusSq + 1.0;
}
//----------------------------------------------------------------------------------
// P = view-space position at the kernel center
// N = view-space normal at the kernel center
// S = view-space position of the current sample
//----------------------------------------------------------------------------------
float ComputeAO(float3 P, float3 N, float3 S)
{
    float3 V = S - P;
    float VdotV = dot(V, V);
    float NdotV = dot(N, V) * rsqrt(VdotV);
    float NDotVBias = HBAO_NDotVBiase;
    //    return clamp(NdotV - NDotVBias, 0.0, 1.0) * clamp(Falloff(VdotV), 0.0, 1.0);
    float AO = max(NdotV - NDotVBias, 0.0) * max(Falloff(VdotV), 0.0);
    return min(AO, 1.0);
}

float GetSearchingPixelRadiusHBAO(float ViewDepth, bool Interleaved, float MaxScreenRadius, int NumTaps)
{
    float DowmsampleFactor = GTAOParams[1].z;
    float ScreenHeight = BufferSizeAndInvSize.y;
    if(Interleaved)
        ScreenHeight /= 4.0;
    float FOVScale = ScreenHeight * InvTanHalfFov;
    // Get Radius in ScreenSpace (in pixels)
    float WorldRadiusAdj = HBAO_WorldRadius * FOVScale;
    float RadiusPixels = max(min(WorldRadiusAdj / abs(ViewDepth), MaxScreenRadius / DowmsampleFactor), float(NumTaps) / DowmsampleFactor);
    return RadiusPixels;
}