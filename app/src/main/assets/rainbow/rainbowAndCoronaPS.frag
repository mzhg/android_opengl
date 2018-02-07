#version 300 es

out vec4 Out_Color;

in VS_OUTPUT {
    vec4 vTexCoord;// quadgl_ texture coordinates
    vec3 vEyeVec;// eye vector
    vec3 vLightVec;// light vector
}IN;

uniform sampler2D LookupMap;
uniform sampler2D MoistureMap;
uniform sampler2D CoronaLookupMap;
uniform float     dropletRadius;
uniform float     rainbowIntensity;

void CalculateRainbowColor(/*VS_OUTPUT IN,*/ out float d, out vec4 scattered, out vec4 moisture )
{
/*
  notes about rainbows

  -the lookuptexture should be blurred by the suns angular size 0.5 degrees.
   this should be baked into the texture

  -rainbow light blends additively to existing light in the scene.
    aka current scene color + rainbow color
    aka alpha blend, one, one

  -horizontal thickness of moisture,
  	a thin sheet of rain will produce less bright rainbows than a thick sheet
  	aka rainbow color  * water ammount, where water ammount ranges from 0 to 1

  -rainbow light can be scattered and absorbed by other atmospheric particles.
    aka simplified..rainbow color * light color

*/

	d =  dot(
				IN.vLightVec,			//this can be normalized per vertex
	          	normalize(IN.vEyeVec ) 	//this must be normalized per pixel to prevent banding
	         );

	//d will be clamped between 0 and 1 by the texture sampler
	// this gives up the dot product result in the range of [0 to 1]
	// that is to say, an angle of 0 to 90 degrees
	 scattered = texture(LookupMap, float2( dropletRadius, d));
	 moisture = texture(MoistureMap,IN.vTexCoord.xy);

}

void main()
{
    /*
        Same as rainbow shader, but adds corona arround sun.
    */

    float d; //note: I use a float for d here, since a half corrupts the corona
    vec4 scattered;
    vec4 moisture;

    CalculateRainbowColor(/*IN,*/ d, scattered, moisture );

    //(1 + d) will be clamped between 0 and 1 by the texture sampler
    // this gives up the dot product result in the range of [-1 to 0]
    // that is to say, an angle of 90 to 180 degrees
    vec4 coronaDiffracted = texture(CoronaLookupMap, float2(dropletRadius, 1 + d));

    Out_Color = (coronaDiffracted + scattered)*rainbowIntensity*moisture.x;
}