precision highp float;

uniform sampler2D decalMap;
uniform sampler2D heightMap;
uniform sampler2D normalMap;

uniform bool parallaxMapping;

varying vec2 uv;
varying vec3 lightVec;
varying vec3 halfVec;
varying vec3 eyeVec;

const float diffuseCoeff = 0.7;
const float specularCoeff = 0.6;

void main()
{
    vec2 texUV;

    if (parallaxMapping)
    {
        float height = texture2D(heightMap, uv).r;
        height = height * 0.04 - 0.02;

        vec3 eye = normalize(eyeVec);
        texUV = uv + (eye.xy * height);
    }
    else
        texUV = uv;

    // fetch normal from normal map, expand to the [-1, 1] range, and normalize
    vec3 normal = 2.0 * (texture2D(normalMap, texUV).rgb - 0.5);
    normal = normalize(normal);

    // compute diffuse lighting
    float diffuse = max(dot(lightVec, normal), 0.0) * diffuseCoeff;

    // compute specular lighting
    float specular = max(dot(halfVec, normal), 0.0);
    specular = pow(specular, 128.0) * specularCoeff;

    vec3 decalColor = texture2D(decalMap, texUV).rgb;

    // output final color
    gl_FragColor = vec4(vec3(diffuse) * decalColor + vec3(specular), 1.0);
}