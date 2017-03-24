precision highp float;
varying vec4 Position;
varying vec3 Normal;
varying vec3 IncidentVector;

uniform vec3 emission;
uniform vec4 color;
uniform samplerCube envMap;
uniform samplerCube envMapRough;

float my_fresnel(vec3 I, vec3 N, float power,  float scale,  float bias)
{
    return bias + (pow(clamp(1.0 - dot(I, N), 0.0, 1.0), power) * scale);
}

void main()
{
    vec3 I = normalize(IncidentVector);
    vec3 N = normalize(Normal);
    vec3 R = reflect(I, N);
    float fresnel = my_fresnel(-I, N, 5.0, 1.0, 0.1);
    vec3 Creflect = textureCube(envMap, R).rgb;
	vec3 CreflectRough = textureCube(envMapRough, R).rgb;
    CreflectRough *= color.rgb;
	Creflect *= color.rgb;
	gl_FragColor = vec4(mix(mix(CreflectRough,Creflect,fresnel),mix(Creflect,CreflectRough,fresnel),color.a)+emission, 1.0);
}