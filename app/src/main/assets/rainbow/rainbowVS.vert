#version 300 es

in vec3 In_Position;
in vec3 In_TexCoord;

out VS_OUTPUT {
    vec4 vTexCoord;// quadgl_ texture coordinates
    vec3 vEyeVec;// eye vector
    vec3 vLightVec;// light vector
}OUT;

uniform mat4 ProjInv;
uniform mat4 View;
uniform vec3 LightVec;

void main()
{
    OUT.vTexCoord = In_TexCoord;
    // our input is a full screen quad in homogeneous-clip space
    gl_Position = vec4(In_Position,1.0);

    //we need to unproject the position, this moves it back into camera space
    vec4 tempPos = ProjInv * gl_Position;

    //while in camera space, the eye is at 0,0,0
    //vector from vertex to eye, no need to normalize here since we
    //will be normalizing in the pixel shader
    OUT.vEyeVec =  0.0 - tempPos;

    //transform light into eyespace
    vec4 tempLightDir = vec4(-LightVec , 0.0);
    OUT.vLightVec = normalize((View * tempLightDir ).xyz);
}