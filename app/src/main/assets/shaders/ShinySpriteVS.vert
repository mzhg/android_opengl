attribute vec4 In_Position;
attribute vec4 In_Size;
attribute vec3 In_Normal;
attribute vec4 In_TexCoord0;

varying vec4 m_Color0;
varying vec4 m_TexCoord0;
varying vec4 m_TexCoord1;

uniform mat4 Projection;
uniform mat4 ModelView;
uniform mat4 ModelViewIT;
uniform vec3 Light;
uniform float Shininess;
uniform float Slices;
uniform vec2 CSTable[30];

vec4 lit(float NdotL, float NdotH, float m)
{
     float specular = (NdotL > 0.0) ? pow(max(0.0, NdotH), m) : 0.0;
     return vec4(1.0, max(0.0, NdotL), specular, 1.0);
}

void main()
{
    vec4 mvposition = ModelView * In_Position;
    vec4 mvnormal =   normalize(ModelViewIT * vec4(In_Normal, 0.0));

    vec3 eyedir = -normalize(mvposition.xyz);

    vec3 lightdir = normalize(Light-mvposition.xyz);

    // get the second point
    vec4 mvpos2;
    mvpos2.xyz = cross(lightdir, mvnormal.xyz) + mvposition.xyz;
    mvpos2.w = .0;

    // specular vector
    vec3 HalfV = normalize(lightdir + eyedir);

    //
    //=====> compute the crystal reflection
    //
    vec2 NLCosSin;
    int index;
    float NDotL = 0.1;
    float NDotH;
    /*NLCosSin.x =    dot(mvnormal.xyz, lightdir);
    NLCosSin.y =    sqrt(1.0-(NLCosSin.x * NLCosSin.x));
    index =         max(int(NLCosSin.x * Slices), 4);
    NLCosSin =      NLCosSin * CSTable[index];
    NDotL =         NLCosSin.x + NLCosSin.y;*/

    NLCosSin.x =    dot(mvnormal.xyz, HalfV);
    NLCosSin.y =    sqrt(1.0-(NLCosSin.x * NLCosSin.x));
    index =         max(int(NLCosSin.x * Slices), 4);
    NLCosSin =      NLCosSin * CSTable[index];
    NDotH =         NLCosSin.x + NLCosSin.y;

    vec4 lightcoefs = lit(NDotL, NDotH, Shininess);

    //
    //=====> transform the vertex
    //
    vec4 projpos1 = Projection * mvposition;
    vec4 projpos2 = Projection * mvpos2;
    vec2 pos2d1 = projpos1.xy / projpos1.ww;
    vec2 pos2d2 = projpos2.xy / projpos2.ww;
    vec2 dir2d  = normalize(pos2d2 - pos2d1);

    vec2 s;

    const vec2 ratio = vec2(1.0, 1920.0/1080.0);

    s = ratio * ((lightcoefs.z * In_Size.z) + In_Size.x);
    projpos1.xy = dir2d*s + projpos1.xy;

    s = ratio * ((lightcoefs.z * In_Size.w) + In_Size.y);
    projpos1.x = ( dir2d.y * s.x) + projpos1.x;
    projpos1.y = (-dir2d.x * s.y) + projpos1.y;


    gl_Position = projpos1;
    m_TexCoord0 = In_TexCoord0;
    m_TexCoord1.w = 1.0;
    m_TexCoord1.z = 0.0;
    m_TexCoord1.xy = (pos2d1.xy * 0.5) + 0.5;
    m_Color0 = vec4(1.0);
}