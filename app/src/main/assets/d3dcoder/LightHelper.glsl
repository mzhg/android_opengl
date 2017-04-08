//***************************************************************************************
// LightHelper.fx by Frank Luna (C) 2011 All Rights Reserved.
//
// Structures and functions for lighting calculations.
//***************************************************************************************

struct DirectionalLight
{
	vec4 Ambient;
	vec4 Diffuse;
	vec4 Specular;
	vec3 Direction;
	float pad;
};

struct PointLight
{ 
	vec4 Ambient;
	vec4 Diffuse;
	vec4 Specular;

	vec3 Position;
	float Range;

	vec3 Att;
	float pad;
};

struct SpotLight
{
	vec4 Ambient;
	vec4 Diffuse;
	vec4 Specular;

	vec3 Position;
	float Range;

	vec3 Direction;
	float Spot;

	vec3 Att;
	float pad;
};

struct Material
{
	vec4 Ambient;
	vec4 Diffuse;
	vec4 Specular; // w = SpecPower
	vec4 Reflect;
};

uniform cbPerFrame
{
	DirectionalLight gDirLights[3];
	vec3 gEyePosW;
	// this variable should declare as a uniform.
	int gLightCount;

	float  gFogStart;
	float  gFogRange;
	vec4 gFogColor;
};

uniform Material gMaterial;

//---------------------------------------------------------------------------------------
// Computes the ambient, diffuse, and specular terms in the lighting equation
// from a directional light.  We need to output the terms separately because
// later we will modify the individual terms.
//---------------------------------------------------------------------------------------
void ComputeDirectionalLight(//in Material mat, in DirectionalLight L,
                             int index,
                             in vec3 normal, in vec3 toEye,
					         out vec4 ambient,
						     out vec4 diffuse,
						     out vec4 spec)
{
	// Initialize outputs.
	ambient = vec4(0.0, 0.0, 0.0, 0.0);
	diffuse = vec4(0.0, 0.0, 0.0, 0.0);
	spec    = vec4(0.0, 0.0, 0.0, 0.0);

	// The light vector aims opposite the direction the light rays travel.
	vec3 lightVec = -gDirLights[index].Direction;

	// Add ambient term.
	ambient = gMaterial.Ambient * gDirLights[index].Ambient;

	// Add diffuse and specular term, provided the surface is in 
	// the line of site of the light.
	
	float diffuseFactor = dot(lightVec, normal);

	// Flatten to avoid dynamic branching.
	// [flatten]
	if( diffuseFactor > 0.0 )
	{
		vec3 v         = reflect(-lightVec, normal);
		float specFactor = pow(max(dot(v, toEye), 0.0f), gMaterial.Specular.w);
					
		diffuse = diffuseFactor * gMaterial.Diffuse * gDirLights[index].Diffuse;
		spec    = specFactor * gMaterial.Specular * gDirLights[index].Specular;
	}
}

//---------------------------------------------------------------------------------------
// Computes the ambient, diffuse, and specular terms in the lighting equation
// from a point light.  We need to output the terms separately because
// later we will modify the individual terms.
//---------------------------------------------------------------------------------------
void ComputePointLight(in Material mat, in PointLight L, in vec3 pos, in vec3 normal, in vec3 toEye,
				   out vec4 ambient, out vec4 diffuse, out vec4 spec)
{
	// Initialize outputs.
	ambient = vec4(0.0, 0.0, 0.0, 1.0);
	diffuse = vec4(0.0, 0.0, 0.0, 1.0);
	spec    = vec4(0.0, 0.0, 0.0, 1.0);

	// The vector from the surface to the light.
	vec3 lightVec = L.Position - pos;
		
	// The distance from surface to light.
	float d = length(lightVec);
	
	// Range test.
	if( d > L.Range )
		return;
		
	// Normalize the light vector.
	lightVec /= d; 
	
	// Ambient term.
	ambient = mat.Ambient * L.Ambient;	

	// Add diffuse and specular term, provided the surface is in 
	// the line of site of the light.

	float diffuseFactor = dot(lightVec, normal);

	// Flatten to avoid dynamic branching.
	// [flatten]
	if( diffuseFactor > 0.0 )
	{
		vec3 v         = reflect(-lightVec, normal);
		float specFactor = pow(max(dot(v, toEye), 0.0), mat.Specular.w);
					
		diffuse = diffuseFactor * mat.Diffuse * L.Diffuse;
		spec    = specFactor * mat.Specular * L.Specular;
	}

	// Attenuate
	float att = 1.0 / dot(L.Att, vec3(1.0, d, d*d));

	diffuse *= att;
	spec    *= att;
}

//---------------------------------------------------------------------------------------
// Computes the ambient, diffuse, and specular terms in the lighting equation
// from a spotlight.  We need to output the terms separately because
// later we will modify the individual terms.
//---------------------------------------------------------------------------------------
void ComputeSpotLight(in Material mat, in SpotLight L, in vec3 pos, in vec3 normal, in vec3 toEye,
				  out vec4 ambient, out vec4 diffuse, out vec4 spec)
{
	// Initialize outputs.
	ambient = vec4(0.0, 0.0, 0.0, 0.0);
	diffuse = vec4(0.0, 0.0, 0.0, 0.0);
	spec    = vec4(0.0, 0.0, 0.0, 0.0);

	// The vector from the surface to the light.
	vec3 lightVec = L.Position - pos;
		
	// The distance from surface to light.
	float d = length(lightVec);
	
	// Range test.
	if( d > L.Range )
		return;
		
	// Normalize the light vector.
	lightVec /= d; 
	
	// Ambient term.
	ambient = mat.Ambient * L.Ambient;	

	// Add diffuse and specular term, provided the surface is in 
	// the line of site of the light.

	float diffuseFactor = dot(lightVec, normal);

	// Flatten to avoid dynamic branching.
	// [flatten]
	if( diffuseFactor > 0.0 )
	{
		vec3 v         = reflect(-lightVec, normal);
		float specFactor = pow(max(dot(v, toEye), 0.0), mat.Specular.w);
					
		diffuse = diffuseFactor * mat.Diffuse * L.Diffuse;
		spec    = specFactor * mat.Specular * L.Specular;
	}
	
	// Scale by spotlight factor and attenuate.
	float spot = pow(max(dot(-lightVec, L.Direction), 0.0), L.Spot);

	// Scale by spotlight factor and attenuate.
	float att = spot / dot(L.Att, vec3(1.0, d, d*d));

	ambient *= spot;
	diffuse *= att;
	spec    *= att;
}

//---------------------------------------------------------------------------------------
// Transforms a normal map sample to world space.
//---------------------------------------------------------------------------------------
vec3 NormalSampleToWorldSpace(vec3 normalMapSample, vec3 unitNormalW, vec3 tangentW)
{
	// Uncompress each component from [0,1] to [-1,1].
	vec3 normalT = 2.0 * normalMapSample - 1.0;

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

//---------------------------------------------------------------------------------------
// Dumplicate method for compaciable
//---------------------------------------------------------------------------------------
vec3 NormalSampleToWorldSpace(vec3 normalMapSample, vec3 unitNormalW, vec4 tangentW)
{
	// Uncompress each component from [0,1] to [-1,1].
	vec3 normalT = 2.0 * normalMapSample - 1.0;

	// Build orthonormal basis.
	vec3 N = unitNormalW;
	vec3 T = normalize(tangentW.xyz - dot(tangentW.xyz, N)*N);
	vec3 B = tangentW.w * cross(N, T);

	mat3 TBN = mat3(T, B, N);

	// Transform from tangent space to world space.
	vec3 bumpedNormalW = TBN * normalT;

	return bumpedNormalW;
}


//---------------------------------------------------------------------------------------
// Performs shadowmap test to determine if a pixel is in shadow.
//---------------------------------------------------------------------------------------

const float SMAP_SIZE = 2048.0f;
const float SMAP_DX = 1.0f / SMAP_SIZE;

float CalcShadowFactor(sampler2D shadowMap, vec4 shadowPosH)
{
	// Complete projection by doing division by w.
	shadowPosH.xyz /= shadowPosH.w;
	
	// Depth in NDC space.
	//float depth = shadowPosH.z;

	// Texel size.
	const float dx = SMAP_DX;

	float percentLit = 0.0f;
	vec2 offsets[9];
	offsets[0] = vec2(-dx,  -dx);
	offsets[1] = vec2(0.0f,  -dx);
	offsets[2] = vec2(dx,  -dx);
	offsets[3] = vec2(-dx, 0.0f);
	offsets[4] = vec2(0.0f, 0.0f);
	offsets[5] = vec2(dx, 0.0f);
	offsets[6] = vec2(-dx,  +dx);
	offsets[7] = vec2(0.0f,  +dx);
	offsets[8] = vec2(dx,  +dx);

	// [unroll]
	for(int i = 0; i < 9; ++i)
	{
		percentLit += textureLod(shadowMap, shadowPosH.xy + offsets[i], 0.0);
	}

	return percentLit / 9.0f;
}
