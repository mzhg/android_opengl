#version 300 es
precision highp float;

uniform sampler2D iChannel0;
uniform vec2 iResolution;
uniform float factor;
uniform vec2 iMouse;

uniform float focuse;
uniform vec4 Kfactor;
uniform vec4 Viewport;
uniform float EyeOffset;

out vec4 FragColor;

#define EPSILON 0.000011

void main()
{
    vec2 p = (gl_FragCoord.xy -Viewport.xy) / Viewport.z;//normalized coords with some cheat
    	                                                         //(assume 1:1 prop)
    p.x += EyeOffset;
    float prop = Viewport.z / Viewport.w;//screen proroption
    vec2 m = vec2(0.5+EyeOffset, 0.5 / prop);//center coords
    vec2 d = p - m;//vector from center to current fragment
    float r = sqrt(dot(d, d)) / focuse; // distance of pixel from center
    vec2 dir = normalize(d);


    float k1 = Kfactor.x;
    float k2 = Kfactor.y;
    float k3 = Kfactor.z;
    float k4 = Kfactor.w;

    float r2 = r * r;
    float r4 = r2 * r2;
    float r6 = r4 * r2;
    float r8 = r6 * r2;
    float rr = r * (1.0 + k1 * r2 + k2 * r4 + k3 * r6 + k4 * r8);

    vec2 uv = (m + rr * dir);
    uv.y *= prop;
//    uv.x += EyeOffset;

    if(uv.x > 1.0 || uv.x < 0.0 || uv.y > 1.0 || uv.y < 0.0)
    {
        FragColor = vec4(0);
    }
    else
    {
        uv.y  = 1.0 - uv.y;
        FragColor = texture(iChannel0, uv);
    }

}