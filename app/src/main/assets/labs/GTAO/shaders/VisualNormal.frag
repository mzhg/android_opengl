#version 300 es
precision highp float;

in vec4 UVAndScreenPos;

/*layout(binding = 0)*/ uniform usampler2D TextureNormal;

out vec4 OutColor;

vec3 DecodeNormal(uint v)
{
    vec2 Nxy = unpackSnorm2x16(v);
    float Nz = sqrt(1.0 - dot(Nxy, Nxy));

    return vec3(Nxy, Nz);
}

void main()
{
    uint z = textureLod(TextureNormal, UVAndScreenPos.xy, 0.0).r;
    OutColor = vec4(DecodeNormal(z) * 0.5 + 0.5, 0);
}