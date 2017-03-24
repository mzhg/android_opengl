#version 300 es

precision highp float;

in vec2 a_texCoord;

uniform sampler2D WaterNormalMap;
uniform samplerCube PoolSkyCubeMap;

uniform vec3 LightPosition;
uniform vec3 CubeMapNormals[6];
uniform vec3 CameraPosition;

in vec3 Position;

out vec4 gl_FragColor;

vec3 IntersectCubeMap(vec3 Position, vec3 Direction)
{
	vec3 Point;

	for(int i = 0; i < 6; i++)
	{
		float NdotR = -dot(CubeMapNormals[i], Direction);

		if(NdotR > 0.0)
		{
			float Distance = (dot(CubeMapNormals[i], Position) + 1.0) / NdotR;

			if(Distance > -0.03)
			{
				Point = Direction * Distance + Position;

				if(Point.x > -1.001 && Point.x < 1.001 && Point.y > -1.001 && Point.y < 1.001 && Point.z > -1.001 && Point.z < 1.001)
				{
					break;
				}
			}
		}
	}

	return vec3(Point.x, -Point.yz);
}

void main()
{
	vec3 Normal = texture(WaterNormalMap, a_texCoord).rgb;
	vec3 Direction = normalize(Position - CameraPosition);

	Normal = normalize(2.0 * Normal - 1.0);

	if(CameraPosition.y > 0.0)
	{
		vec3 ReflectedColor = texture(PoolSkyCubeMap, IntersectCubeMap(Position, reflect(Direction, Normal))).rgb;
		vec3 RefractedColor = texture(PoolSkyCubeMap, IntersectCubeMap(Position, refract(Direction, Normal, 0.750395))).rgb;

		vec3 LightDirectionReflected = reflect(normalize(Position -  LightPosition), Normal);

		float Specular = pow(max(-dot(Direction, LightDirectionReflected), 0.0), 128.0);

		gl_FragColor.rgb = mix(ReflectedColor, RefractedColor, -dot(Normal, Direction)) + Specular;
	}
	else
	{
		Normal = -Normal;

		vec3 ReflectedColor = texture(PoolSkyCubeMap, IntersectCubeMap(Position, reflect(Direction, Normal))).rgb;
		vec3 DirectionRefracted = refract(Direction, Normal, 1.332631);

		if(DirectionRefracted.x == 0.0 && DirectionRefracted.y == 0.0 && DirectionRefracted.z == 0.0)
		{
			gl_FragColor.rgb = ReflectedColor;
		}
		else
		{
			vec3 RefractedColor = texture(PoolSkyCubeMap, IntersectCubeMap(Position, DirectionRefracted)).rgb;
			gl_FragColor.rgb = mix(ReflectedColor, RefractedColor, -dot(Normal, Direction));
		}
	}

//	gl_FragColor = max(vec4(1,0,0,1), gl_FragColor);
}
