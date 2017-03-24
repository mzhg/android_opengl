#version 300 es
precision highp float;

in vec4 UVAndScreenPos;
// xy: texel size; zw: dir
uniform vec4 g_TexSizeAndDir;
uniform float g_Attenuation;
uniform float g_Pass;
uniform sampler2D g_Texture;

out vec4 FragColor;

#define NUM_SAMPLES 7.0

void main()
{
    vec3 cOut = vec3(0);

    // sample weight = a^(b*s)
    // a = attenuation
    // b = 4^(pass -1)
    // s = sample number

    float b = pow(NUM_SAMPLES, g_Pass);
    for(float s = 0.0; s < NUM_SAMPLES; s+= 1.0)
    {
        float weight = pow(g_Attenuation, b * s);
        vec2 sampleCoord = UVAndScreenPos.xy + (g_TexSizeAndDir.zw * b * vec2(s) * g_TexSizeAndDir.xy);
        cOut += clamp(weight, 0.0, 1.0) * texture(g_Texture, sampleCoord);
    }

    FragColor = vec4(cOut, 1.0);
}