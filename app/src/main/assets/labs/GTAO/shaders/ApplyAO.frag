#version 300 es

precision highp float;

in vec4 UVAndScreenPos;

// TODO: Opengl ES 300 doesn't support the 'binding' qualifier
/*layout(binding = 0)*/ uniform sampler2D TextureAO;

layout(location = 0) out vec4 OutColor;

const float g_lightZFar = 100.0;
const float g_lightZNear = 0.1;
const float g_scalerFactor = 1.0;

float zClipToEye(float z)
{
    return g_lightZFar * g_lightZNear / (g_lightZFar - z * (g_lightZFar - g_lightZNear));
}

void main()
{
    vec4 color = textureLod(TextureAO, UVAndScreenPos.xy, 0.0);
//    float color = (zClipToEye(z) - g_lightZNear) / (g_lightZFar - g_lightZNear) * g_scalerFactor;
    OutColor = vec4(color.xyz, 1);
}