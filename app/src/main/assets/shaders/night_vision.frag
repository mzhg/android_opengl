#version 300 es
precision highp float;

uniform sampler2D sceneBuffer;
uniform sampler2D noiseTex;

uniform float elapsedTime; // seconds
uniform float luminanceThreshold; // 0.2
uniform float colorAmplification; // 4.0
uniform float effectCoverage; // 0.5

in vec4 UVAndScreenPos;
out vec4 FragColor;

void main ()
{
  vec4 finalColor;
  // Set effectCoverage to 1.0 for normal use.
  if (UVAndScreenPos.x < effectCoverage)
  {
    vec2 uv;
    uv.x = 0.4*sin(elapsedTime*50.0);
    uv.y = 0.4*cos(elapsedTime*50.0);
    float m = 1.0; //texture(maskTex, UVAndScreenPos.xy).r;
    vec3 n = vec3(0); //texture(noiseTex, (UVAndScreenPos.xy*3.5) + uv).rgb;
    vec3 c = texture(sceneBuffer, UVAndScreenPos.xy + (n.xy*0.005)).rgb;

    float lum = dot(vec3(0.30, 0.59, 0.11), c);
    if (lum < luminanceThreshold)
      c *= colorAmplification;

    vec3 visionColor = vec3(0.1, 0.95, 0.2);
    finalColor.rgb = (c + (n*0.2)) * visionColor * m;
   }
   else
   {
    finalColor = texture(sceneBuffer, UVAndScreenPos.xy);
   }
  FragColor.rgb = finalColor.rgb;
  FragColor.a = 1.0;
}