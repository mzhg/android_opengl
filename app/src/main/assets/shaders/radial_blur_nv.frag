#version 300 es
precision highp float;

in vec4 UVAndScreenPos;
out vec4 fragColor;

uniform sampler2D g_Texture;
uniform float g_BlurStart; // (min, max, init) = (0,1,1)
uniform float g_BlurWidth; // (min, max, init) = (-1,1,-0.3)
uniform vec2 g_Center;     // (min, max, init) = (-1,2,0.5)
uniform float g_Intensity; // (min, max, init) = (0, 8, 6)
uniform float g_GlowGamma; // (min, max, init) = (0.5, 2.0, 1.6)

void main()
{
    const int nsamples = 24;
    vec4 blurred = vec4(0);
    vec2 ctrPt = g_Center;

    // this loop will be unrolled by compiler and the constants precalculated:
    for(int i=0; i<nsamples; i++) {
        float scale = g_BlurStart + g_BlurWidth*(float(i)/float(nsamples-1));
        blurred += texture(g_Texture, UVAndScreenPos.zw*scale + ctrPt );
    }
    blurred /= float(nsamples);
    blurred.rgb = pow(blurred.rgb,vec3(g_GlowGamma));
    blurred.rgb *= g_Intensity;
    blurred.rgb = clamp(blurred.rgb, vec3(0), vec3(1));
    vec4 origTex = texture(g_Texture, UVAndScreenPos.zw + ctrPt );
    origTex.a = 0.5;
    vec3 newC = origTex.rgb + (1.0-origTex.a)* blurred.rgb;
    float newA = max(origTex.a,blurred.a);
    fragColor = vec4(newC.rgb,newA);
}