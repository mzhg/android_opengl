attribute vec3 PosAttribute;
attribute vec3 myNormal;
attribute vec2 uvTexCoord;

uniform mat4 viewProjMatrix;
uniform mat4 ModelMatrix;
uniform vec3 eyePos;
varying vec4 Position;
varying vec3 Normal;
varying vec3 IncidentVector;
varying vec2 texcoord;
void main()
{
   vec4 P = ModelMatrix * vec4(PosAttribute, 1.0);
   vec3 N = normalize(mat3(ModelMatrix) * myNormal);
   vec3 I = P.xyz - eyePos;
   Position = P;
   Normal = N;
   IncidentVector = I;
   texcoord = uvTexCoord;
   gl_Position = viewProjMatrix * P;
}