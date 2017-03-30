#version 300 es
precision highp float;
uniform sampler2D inputImage;
const vec3 LUMINANCE_VECTOR = vec3(0.2125f, 0.7154f, 0.0721f);

out vec4 f4FragColor;

void main()
{
	float logLumSum = 0.0f;
	int x, y;
//	ivec2 size = textureSize(inputImage, 0);
	for (y = 0; y<16; y++) {
		for (x = 0; x<16; x++) {
		    vec2 texcoord = vec2(x, y)/vec2(16);
			logLumSum += (dot(textureLod(inputImage, texcoord, 0.0).rgb, LUMINANCE_VECTOR) + 0.00001);
		}
	}
	logLumSum /= 256.0;
	float val = (logLumSum + 0.00001);
//	imageStore(outputImage, ivec2(0, 0), vec4(val, val, val, val));
	f4FragColor = vec4(val);
}