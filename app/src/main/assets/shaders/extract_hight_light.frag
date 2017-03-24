#version 300 es

precision highp float;
in vec4 UVAndScreenPos;

uniform sampler2D g_Texture;

out vec4 gl_FragColor;

void main(){
    vec4 f4Color = texture(g_Texture, UVAndScreenPos.xy);
    f4Color.rgb = min(vec3(256.0 * 256.0, 256.0 * 256.0, 256.0 * 256.0), f4Color.rgb);
    float lumiance = dot(vec3(1), f4Color.rgb)/3.0;
    if(lumiance > 0.99)
    {
    gl_FragColor = f4Color;
    }
    else
    {
    gl_FragColor = vec4(0);
    }
}