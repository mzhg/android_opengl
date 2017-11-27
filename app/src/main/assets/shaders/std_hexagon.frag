#version 300 es
precision highp float;

uniform sampler2D iChannel0;
uniform vec2 iResolution;
uniform float factor;
uniform vec2 iMouse;
uniform vec4 HexagonData;  // x for the radius, y for inner radius, z for the triagnle offset

uniform float focuse;
uniform vec4 Kfactor;
uniform vec4 Viewport;

out vec4 FragColor;

#define EPSILON 0.000011

int MeasureHexagonRegion(vec2 dir, float length, out bool inRegion)
{
    float angle = acos(dir.x);
    if(dir.y < 0.0)
    {
        angle = 2.0 * 3.1415926 - angle;
    }

    const float intevelAngle = 2.0 * 3.1415926 / 6.0;
    int region = int(angle / intevelAngle);

    if(length < HexagonData.y)
    {
        inRegion = true;
    }
    else if(length > HexagonData.x)
    {
        inRegion = false;
    }
    else
    {
        float localAngle = angle - float(region) * intevelAngle;
        float t = tan(intevelAngle * 0.5);  // tan30
        float a = tan(localAngle);
        float a2 = a * a;
        float triLen = HexagonData.x /(1.0 + t * a) * sqrt(1.0 + a2);
        /*const float value = 1.0f/sqrt(3.0);
        inRegion = HexagonData.x - abs(dir.x) * length > abs(dir.y) * length * value;*/
        inRegion = (triLen > length);
    }

    return region;
}

vec2 distort(vec2 m, vec2 dir, float r)
{
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
    return uv;
}

vec2 distort2(vec2 m, vec2 dir, float r)
{
    float k1 = 0.1;
    float k2 = 0.1;
    float k3 = 0.2;
    float k4 = 0.3;

    float r2 = r * r;
    float r4 = r2 * r2;
    float r6 = r4 * r2;
    float r8 = r6 * r2;
    float rr = r * (1.0 + k1 * r2 + k2 * r4 + k3 * r6 + k4 * r8);

    vec2 uv = (m + rr * dir);
    return uv;
}

vec4 clampUV(vec2 uv)
{
    vec4 color;
    if(uv.x > 1.0 || uv.x < 0.0 || uv.y > 1.0 || uv.y < 0.0)
    {
        color = vec4(0);
    }
    else
    {
//        uv.y  = 1.0 - uv.y;
        color = texture(iChannel0, uv);
    }

    return color;
}

void main()
{
#if 1
    vec2 p = (gl_FragCoord.xy -Viewport.xy) / Viewport.z;//normalized coords with some cheat
    	                                                         //(assume 1:1 prop)
    float prop = Viewport.z / Viewport.w;//screen proroption
#else
    vec2 p = (gl_FragCoord.xy -Viewport.xy) / Viewport.zw;//normalized coords with some cheat
        	                                                         //(assume 1:1 prop)
    float prop = /*iResolution.x / iResolution.y*/1.0;//screen proroption
#endif
    vec2 m = vec2(0.5, 0.5 / prop);//center coords
    vec2 d = p - m;//vector from center to current fragment
    float r = length(d) / focuse; // distance of pixel from center
    vec2 dir = normalize(d);

    bool inRegion;
    int region = MeasureHexagonRegion(dir, r * focuse, inRegion);

    if(inRegion)
    {
        vec2 uv = distort(m, dir, r);
        uv.y *= prop;
        uv *= 0.5;
        vec2 newUV = m + d * 1.0/HexagonData.w;
        newUV.y *= prop;
        FragColor = clampUV(newUV);

    }
    else  // out of the region
    {
        /*if(region == 0)
        {
            FragColor = vec4(0);
        }
        else if(region == 1)
        {
            FragColor = vec4(1,0,0,1);
        }
        else if(region == 2)
        {
            FragColor = vec4(0,1,0,1);
        }
        else if(region == 3)
        {
            FragColor = vec4(0,0,1,1);
        }
        else if(region == 4)
        {
            FragColor = vec4(1,1,0,1);
        }
        else if(region == 5)
        {
            FragColor = vec4(1);
        }*/

        const float intevelAngle = 2.0 * 3.1415926 / 6.0;
        float middleAngle = float(region) * intevelAngle + intevelAngle * 0.5;
        vec2 offsetDir = vec2(cos(middleAngle), sin(middleAngle));
#if 1
        vec2 newLocation = p - offsetDir * HexagonData.z;
        r = length(newLocation - m) / focuse;
#else
        vec2 newCenter = m + offsetDir * HexagonData.z;
        r = length(p - newCenter) / focuse;
        m = newCenter;
#endif
        newLocation.y *= prop;
        FragColor = clampUV(newLocation);
    }



}