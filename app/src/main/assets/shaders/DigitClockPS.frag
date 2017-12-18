precision highp float;

struct Plane
{
    vec2 normal;
    vec2 point;
};

struct Segment
{
    // four planes that define a segment in the LED
    Plane p0;
    Plane p1;
    Plane p2;
    Plane p3;
};

struct Digit
{
    vec4 bottom;
    vec4 top;
};

const Digit zero  = Digit(vec4(1.0, 1.0, 1.0, 0.0), vec4(0.0, 1.0, 1.0, 1.0));
const Digit one   = Digit(vec4(0.0, 0.0, 1.0, 0.0), vec4(0.0, 0.0, 1.0, 0.0));
const Digit two   = Digit(vec4(1.0, 1.0, 0.0, 1.0), vec4(1.0, 0.0, 1.0, 1.0));
const Digit three = Digit(vec4(1.0, 0.0, 1.0, 1.0), vec4(1.0, 0.0, 1.0, 1.0));
const Digit four  = Digit(vec4(0.0, 0.0, 1.0, 1.0), vec4(1.0, 1.0, 1.0, 0.0));
const Digit five  = Digit(vec4(1.0, 0.0, 1.0, 1.0), vec4(1.0, 1.0, 0.0, 1.0));
const Digit six   = Digit(vec4(1.0, 1.0, 1.0, 1.0), vec4(1.0, 1.0, 0.0, 1.0));
const Digit seven = Digit(vec4(0.0, 0.0, 1.0, 0.0), vec4(0.0, 0.0, 1.0, 1.0));
const Digit eight = Digit(vec4(1.0, 1.0, 1.0, 1.0), vec4(1.0, 1.0, 1.0, 1.0));
const Digit nine  = Digit(vec4(0.0, 0.0, 1.0, 1.0), vec4(1.0, 1.0, 1.0, 1.0));
const Digit off   = Digit(vec4(0.0, 0.0, 0.0, 0.0), vec4(0.0, 0.0, 0.0, 0.0));

const int ZeroToOne = 0;
const int ZeroToFive = 1;
const int ZeroToNine = 2;

// get the set of on/off states that represent a given digit
Digit getDigit(float num, const int range)
{
    Digit d;

    if (range == ZeroToOne)
    {
        if (num == 0.0) d = zero;
        if (num == 1.0) d = one;
        if (num == -1.0) d = off;   // turn all segments off
    }
    else if (range == ZeroToFive)
    {
        if (num == 0.0) d = zero;
        if (num == 1.0) d = one;
        if (num == 2.0) d = two;
        if (num == 3.0) d = three;
        if (num == 4.0) d = four;
        if (num == 5.0) d = five;
    }
    else if (range == ZeroToNine)
    {
        if (num == 0.0) d = zero;
        if (num == 1.0) d = one;
        if (num == 2.0) d = two;
        if (num == 3.0) d = three;
        if (num == 4.0) d = four;
        if (num == 5.0) d = five;
        if (num == 6.0) d = six;
        if (num == 7.0) d = seven;
        if (num == 8.0) d = eight;
        if (num == 9.0) d = nine;
    }

    return d;
}

const Segment ledA = Segment(Plane(vec2( 1.0,-1.0), vec2(0.1, 0.0)),
                       Plane(vec2( 0.0,-1.0), vec2(1.0, 0.9)),
                       Plane(vec2(-1.0,-1.0), vec2(5.0, 0.9)),
                       Plane(vec2( 0.0, 1.0), vec2(5.9, 0.0)));

const Segment ledB = Segment(Plane(vec2( 1.0, 0.0), vec2(0.0, 0.1)),
                       Plane(vec2(-1.0,-1.0), vec2(0.0, 5.9)),
                       Plane(vec2(-1.0, 0.0), vec2(0.9, 5.0)),
                       Plane(vec2(-1.0, 1.0), vec2(0.9, 1.0)));

const Segment ledC = Segment(Plane(vec2(-1.0, 0.0), vec2(6.0, 0.1)),
                       Plane(vec2( 1.0,-1.0), vec2(6.0, 5.9)),
                       Plane(vec2( 1.0, 0.0), vec2(5.1, 5.0)),
                       Plane(vec2( 1.0, 1.0), vec2(5.1, 1.0)));

const Segment ledD = Segment(Plane(vec2( 0.0,-1.0), vec2(0.1, 6.0)),
                       Plane(vec2(-1.0, 1.0), vec2(5.9, 6.0)),
                       Plane(vec2( 0.0, 1.0), vec2(5.0, 5.1)),
                       Plane(vec2( 1.0, 1.0), vec2(1.0, 5.1)));

const float scale = 10.0/120.0;

// returns true if any component of the two vec4's is non zero
bool any(vec4 a, vec4 b)
{
    float _a = clamp(dot(a, vec4(1.0)), 0.0, 1.0);
    float _b = clamp(dot(b, vec4(1.0)), 0.0, 1.0);

    return bool(max(_a, _b));
}

vec3 calcPlaneEq(Plane p, vec2 offset)
{
    return vec3(p.normal, dot(-p.normal, (p.point + offset)*scale));
}

float insideSegment(vec2 pos, Segment seg, vec2 offset)
{
    vec3 p0 = calcPlaneEq(seg.p0, offset);
    vec3 p1 = calcPlaneEq(seg.p1, offset);
    vec3 p2 = calcPlaneEq(seg.p2, offset);
    vec3 p3 = calcPlaneEq(seg.p3, offset);

    vec4 tmp = pos.x * vec4(p0.x, p1.x, p2.x, p3.x);
    tmp = (pos.y * vec4(p0.y, p1.y, p2.y, p3.y)) + tmp;
    tmp = clamp(tmp + vec4(p0.z, p1.z, p2.z, p3.z), 0.0, 1.0);

    tmp.xy = min(tmp.xy, tmp.zw);
    return min(tmp.x, tmp.y);
}

// returns true if point is inside any segment, and that segment is on
bool insideDigit(vec2 point, float xOffset, Digit d)
{
    vec4 bottom;
    bottom.x = insideSegment(point, ledA, vec2(xOffset, 1.0));
    bottom.y = insideSegment(point, ledB, vec2(xOffset, 1.0));
    bottom.z = insideSegment(point, ledC, vec2(xOffset, 1.0));
    bottom.w = insideSegment(point, ledD, vec2(xOffset, 1.0));

    vec4 top;
    top.x = insideSegment(point, ledA, vec2(xOffset, 7.0));
    top.y = insideSegment(point, ledB, vec2(xOffset, 7.0));
    top.z = insideSegment(point, ledC, vec2(xOffset, 7.0));
    top.w = insideSegment(point, ledD, vec2(xOffset, 7.0));

    return any(bottom*d.bottom, top*d.top);
}

// returns true if point is inside any segment, even if that segment is off
bool insideDigit(vec2 point, float xOffset)
{
    vec4 bottom;
    bottom.x = insideSegment(point, ledA, vec2(xOffset, 1.0));
    bottom.y = insideSegment(point, ledB, vec2(xOffset, 1.0));
    bottom.z = insideSegment(point, ledC, vec2(xOffset, 1.0));
    bottom.w = insideSegment(point, ledD, vec2(xOffset, 1.0));

    vec4 top;
    top.x = insideSegment(point, ledA, vec2(xOffset, 7.0));
    top.y = insideSegment(point, ledB, vec2(xOffset, 7.0));
    top.z = insideSegment(point, ledC, vec2(xOffset, 7.0));
    top.w = insideSegment(point, ledD, vec2(xOffset, 7.0));

    return any(bottom, top);
}

// returns true if point is inside the specified circle
bool insideCircle(vec2 curPos, vec2 circleCenter, float radius)
{
    return (distance(curPos, circleCenter * scale) <= radius*scale);
}

uniform float hour;
uniform float minute;
uniform float second;

varying   vec2  vTextureCoord;

void main()
{
    vec2 oPosition = vTextureCoord;
    // separate out the digits for hours/minutes/seconds
    float hourHigh = floor(hour/10.0);
    float hourLow = hour - (hourHigh*10.0);

    float minuteHigh = floor(minute/10.0);
    float minuteLow = minute - (minuteHigh*10.0);

    float secondHigh = floor(second/10.0);
    float secondLow = second - (secondHigh*10.0);

    // turn leading zero digit off for hours
    if (hourHigh == 0.0)
        hourHigh = -1.0;

    Digit hrHigh    = getDigit(hourHigh, ZeroToOne);
    Digit hrLow     = getDigit(hourLow, ZeroToNine);
    Digit minHigh   = getDigit(minuteHigh, ZeroToFive);
    Digit minLow    = getDigit(minuteLow, ZeroToNine);
    Digit secHigh   = getDigit(secondHigh, ZeroToFive);
    Digit secLow    = getDigit(secondLow, ZeroToNine);

    if (insideDigit(oPosition,  1.0, hrHigh) ||
        insideDigit(oPosition,  7.1, hrLow) ||
        insideDigit(oPosition, 15.0, minHigh) ||
        insideDigit(oPosition, 21.1, minLow) ||
        insideDigit(oPosition, 29.0, secHigh) ||
        insideDigit(oPosition, 35.1, secLow) ||

        // four dots for digit separators
        insideCircle(oPosition, vec2(14.0, 9.0), 0.7) ||
        insideCircle(oPosition, vec2(14.0, 5.0), 0.7) ||
        insideCircle(oPosition, vec2(28.0, 9.0), 0.7) ||
        insideCircle(oPosition, vec2(28.0, 5.0), 0.7))
    {
        // lit segments are bright green
        gl_FragColor = vec4(0.0, 1.0, 0.0, 1.0);
    }
    else if (insideDigit(oPosition, 1.0) ||
             insideDigit(oPosition, 7.1) ||
             insideDigit(oPosition, 15.0) ||
             insideDigit(oPosition, 21.1) ||
             insideDigit(oPosition, 29.0) ||
             insideDigit(oPosition, 35.1))
    {
        // unlit segments are dark green
        gl_FragColor = vec4(0.0, 0.2, 0.0, 1.0);
    }
    else
    {
        // fragments outside of any digit are black
        gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);
    }
}



