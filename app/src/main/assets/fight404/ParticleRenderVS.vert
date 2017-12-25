#version 310 es

layout (location = 0) in vec3 loc;        // position vector
layout (location = 1) in vec3 vel;        // velocity vector
layout (location = 2) in float radius;    // particles's size
layout (location = 3) in float age;       // current age of particle
layout (location = 4) in float lifeSpan;  // max allowed age of particle
layout (location = 5) in float gen;       // number of times particle has been involved in a SPLIT
layout (location = 6) in float bounceAge; // amount to age particle when it bounces off floor
layout (location = 7) in uint type;       // the type of particle

out vec4 color;

layout(binding = 0) uniform RenderFrame
{
    mat4 projection;
    mat4 modelView;

    float render_particle;
    float pointSize;
};

void draw_particle0()
{
	float agePer = 1.0 - age / lifeSpan;
	float diam = radius * agePer;
	color = vec4(agePer - .25, agePer * .25, 1.5 - agePer, 1.0);

	// transform to the camera coordinate.
	vec4 pos1 = modelView * vec4(loc, 1.0);
	vec4 pos2 = pos1;
	pos2.x += (diam * 0.5);

	vec4 ppos1 = projection * pos1;
	vec4 ppos2 = projection * pos2;

	float psize = distance(ppos1.xyz/ppos1.w, ppos2.xyz/ppos2.w) * 40.0;
	gl_PointSize = psize;
	gl_Position  = ppos1;

}

void draw_particle1()
{
    float agePer = 1.0 - age / lifeSpan;
    float diam = radius * agePer * 0.5;
    color = vec4(1, agePer * 0.75, agePer * 0.75, agePer);

    // transform to the camera coordinate.
    vec4 pos1 = modelView * vec4(loc, 1.0);
    vec4 pos2 = pos1;
    pos2.x = pos1.x + diam * 0.5;
    vec4 ppos1 = projection * pos1;
    vec4 ppos2 = projection * pos2;
    float psize = distance(ppos1.xyz/ppos1.w, ppos2.xyz/ppos2.w) * 40.0;
    gl_PointSize = psize * 0.5 /*distance(ppos1.xyz/ppos1.w, ppos2.xyz/ppos2.w) * 15.0*/;
    gl_Position  = ppos1;
}

void draw_nebula()
{
	color = unpackUnorm4x8(type);
    mat4 mvp = projection * modelView;
    vec4 ppos1 = mvp * vec4(loc, 1.0);
    vec4 ppos2 = mvp * vec4(vec3(loc.x + radius, loc.yz), 1.0);
    gl_Position = ppos1;

    gl_PointSize = distance(ppos1.xyz/ppos1.w, ppos2.xyz/ppos2.w) * 10.0;
}

void main()
{
    int particleType = int(render_particle);
	if(particleType == 0)
    {
        draw_particle0();
    }
	else if(particleType == 1)
	{
	    draw_particle1();
	}
	else
	{
	    draw_nebula();
	}
}