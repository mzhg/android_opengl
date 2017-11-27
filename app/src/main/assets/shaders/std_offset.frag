#version 300 es
precision highp float;

uniform sampler2D iChannel0;
uniform vec4 Viewport;
uniform float EyeOffset;

out vec4 FragColor;

#define EPSILON 0.000011

void main()
{
    vec2 p = (gl_FragCoord.xy -Viewport.xy) / Viewport.zw;//normalized coords with some cheat
    	                                                         //(assume 1:1 prop)
    float prop = Viewport.z / Viewport.w;//screen proroption
    vec2 uv = p;
    uv.x += EyeOffset;

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