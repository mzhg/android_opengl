#version 300 es
precision highp float;
precision highp sampler2DArray;
precision highp sampler3D;

in vec4 UVAndScreenPos;

#ifndef USE_TEXTURE_ARRAY
#define USE_TEXTURE_ARRAY 1
#endif

#if USE_TEXTURE_ARRAY
/*layout(binding = 0)*/ uniform sampler2DArray InputTexture;
#else
/*layout(binding = 0)*/ uniform sampler3D InputTexture;
#endif

// x: normalized ; y : normalizd data; z: array slice
uniform vec4 gArrayData;

out vec4 OutColor;

void main()
{
    bool normalized = bool(gArrayData.x);
    float value = gArrayData.y;

#if USE_TEXTURE_ARRAY
    vec3 arrayLoc = vec3(UVAndScreenPos.xy,gArrayData.z);
#else
    vec3 arrayLoc = vec3(UVAndScreenPos.xy,gArrayData.z / 16.0);
#endif

    vec4 AO = textureLod(InputTexture,arrayLoc, 0.0);
    if(normalized)
        AO /= value;

    OutColor = AO;
//    OutColor = vec4(1,0,0,1);
}