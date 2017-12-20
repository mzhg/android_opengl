#version 300 es

/*layout (location = 0) in vec3 loc;        // position vector
layout (location = 1) in vec3 vel;        // velocity vector
layout (location = 2) in float radius;    // particles's size
layout (location = 3) in float age;       // current age of particle
layout (location = 4) in float lifeSpan;  // max allowed age of particle
layout (location = 5) in float gen;       // number of times particle has been involved in a SPLIT
layout (location = 6) in float bounceAge; // amount to age particle when it bounces off floor
layout (location = 7) in uint type;       // the type of particle
layout (location = 8) in vec3 tail0;
layout (location = 9) in vec3 tail1;
layout (location = 10) in vec3 tail2;
layout (location = 11) in vec3 tail3;*/

layout(location = 0) in vec3 seed;

out vec3 loc;
out vec3 vel;
out float radius;
out float age;
out float lifeSpan;
out float gen;
out float bounceAge;
out uint type;
out vec3 tail0;
out vec3 tail1;
out vec3 tail2;
out vec3 tail3;

#define TYPE_BORN 0      // particles born
#define TYPE_UPDATE 1    // update the particles
#define TYPE_NEBULA 2    // update the nebulas
#define TYPE_NEBORN 3    // born the emitter nebulas

uniform vec3 position;
uniform sampler2D random_texture;

/** Return a random value from 0.0 to 1.0 */
vec3 random(float seed)
{
	return texture(random_texture, vec2(seed, 0)).xyz;
}

vec3 random(float seed, float low, float high)
{
	return random(seed) * (high - low) + low;
}

void main()
{
//    float seed = loc.x;
    gen = 1.0;
    radius = random(seed, 10.0 - gen, 50.0 - (gen - 1.0) * 10.0).x;
    loc = position + random(seed);

    float angle = random(seed, 0.0, 3.1415927 * 2.0).x;
    vel.z = cos(angle) * 0.7;
    vel.x = sin(angle) * 0.7;
    vel.y = -sqrt(1.0 - vel.x * vel.x - vel.y * vel.y);
    vel *= random(seed, 10.0, 20.0);

    age = 0.0;
    bounceAge = 2.0;
    lifeSpan = radius;
    type = uint(1);

    tail0 = loc;
    tail1 = loc;
    tail2 = loc;
    tail3 = loc;
}