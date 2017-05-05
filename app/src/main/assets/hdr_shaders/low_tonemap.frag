#version 300 es
precision highp float;

in vec2 a_texCoord;
uniform sampler2D   sceneTex;
uniform sampler2D   blurTex;
uniform sampler2D   lumTex;

uniform float blurAmount;
uniform float exposure;
uniform float gamma;

const float A = 0.15;
const float B = 0.50;
const float C = 0.10;
const float D = 0.20;
const float E = 0.02;
const float F = 0.30;
const float W = 11.2;

out vec4 FragColor;

vec3 filmicTonemapping(vec3 x)
{
  return ((x*(A*x+C*B)+D*E)/(x*(A*x+B)+D*F))-E/F;
}

float vignette(vec2 pos, float inner, float outer)
{
  float r = length(pos);
  r = 1.0 - smoothstep(inner, outer, r);
  return r;
}

void main()
{
    vec4 scene = texture(sceneTex, a_texCoord);
    vec4 blurred = texture(blurTex, a_texCoord);
    float lum = texture(lumTex, vec2(0.0,0.0)).r;
    vec3 c = scene.rgb + blurred.rgb * blurAmount;
    c = c * exposure/lum;
    c = c * vignette(a_texCoord*2.0-1.0, 0.55, 1.5);
    float ExposureBias = 1.0;
    c = filmicTonemapping(ExposureBias*c);
    vec3 whiteScale = 1.0/filmicTonemapping(vec3(W,W,W));
    c = c*whiteScale;
    c.r = pow(c.r, gamma);
    c.g = pow(c.g, gamma);
    c.b = pow(c.b, gamma);
    FragColor = vec4(c, 1.0);
    FragColor = min(vec4(256.0 * 256.0), FragColor);
}