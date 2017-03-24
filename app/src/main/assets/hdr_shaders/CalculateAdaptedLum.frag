#version 300 es
precision highp float;

uniform float elapsedTime;
uniform sampler2D currentImage;
uniform sampler2D image0;

out vec4 f4FragColor;
void main()
{
	float currentLum = textureLod(currentImage, vec2(0), 0.0).r;
	float lastLum = textureLod(image0, vec2(0), 0.0).r;
	float newLum = lastLum + (currentLum - lastLum) * (1.0 - pow(0.98, 30.0 * elapsedTime));
//	imageStore(image1, ivec2(0, 0), vec4(newLum, newLum, newLum, newLum));

	f4FragColor = vec4(newLum);
}