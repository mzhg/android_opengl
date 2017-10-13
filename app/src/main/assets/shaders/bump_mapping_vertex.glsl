attribute vec4 position;
attribute mat3 tangentBasis;
attribute vec2 texcoord;

uniform vec3 light;
uniform vec3 halfAngle;
uniform mat4 modelViewI;
uniform mat4 modelViewProjection;

varying vec2 uv;
varying vec3 lightVec;
varying vec3 halfVec;
varying vec3 eyeVec;

void main()
{
    // output vertex position
    gl_Position = modelViewProjection * position;

    // output texture coordinates for decal and normal maps
    uv = texcoord;

    // transform light and half angle vectors by tangent basis
    lightVec = light * tangentBasis;
    halfVec = halfAngle * tangentBasis;
 
    eyeVec = modelViewI[3].xyz - position.xyz;
    eyeVec = eyeVec * tangentBasis;
}
