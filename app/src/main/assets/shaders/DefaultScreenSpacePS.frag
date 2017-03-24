
#if GL_ES
precision highp float;
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

#if  ENABLE_IN_OUT_FEATURE
in vec4 ScreenSpaceUV;
out vec4 FragColor;
#else
varying vec4 ScreenSpaceUV;
#define FragColor gl_FragColor
#define texture  texture2D
#endif 

uniform sampler2D g_Texture;

void main()
{
	FragColor = texture(g_Texture, ScreenSpaceUV.xy);
}