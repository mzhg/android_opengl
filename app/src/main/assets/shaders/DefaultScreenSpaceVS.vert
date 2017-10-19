#if GL_ES

#if __VERSION__  >= 300
	#define ENABLE_VERTEX_ID 1
	#define ENABLE_IN_OUT_FEATURE 1
#endif

#else
	
// The Desktop Platform, Almost all of the video drivers support the gl_VertexID, so just to enable it simply.
 #define ENABLE_VERTEX_ID 1
 
 #if __VERSION__ >= 130
 #define ENABLE_IN_OUT_FEATURE 1
 #endif

#endif

#ifdef ENABLE_VERTEX_ID
const vec2 QuadVertices[4] = vec2[4]
(
    vec2(-1.0, -1.0),
    vec2( 1.0, -1.0),
    vec2(-1.0,  1.0),
    vec2( 1.0,  1.0)
);

const vec2 QuadTexCoordinates[4] = vec2[4]
(
    vec2(0.0, 1.0),
    vec2(1.0, 1.0),
    vec2(0.0, 0.0),
    vec2(1.0, 0.0)
);

#else
	attribute vec4 aPosition;
#endif

#ifdef ENABLE_IN_OUT_FEATURE
	out vec4 ScreenSpaceUV;
#else
	varying vec4 ScreenSpaceUV;
#endif

    
void main()
{
#ifdef ENABLE_VERTEX_ID
	gl_Position = vec4(QuadVertices[gl_VertexID], 0.0, 1.0);
#else
	gl_Position = aPosition;
#endif
	ScreenSpaceUV = vec4(0.5 * gl_Position.xy + 0.5, gl_Position.xy);
}

	

