precision highp float;

varying vec4 m_Color0;
varying vec4 m_TexCoord0;
varying vec4 m_TexCoord1;

uniform sampler2D g_InputTex0;
uniform sampler2D g_InputTex1;

void main()
{
    vec4 color0 = texture2D(g_InputTex0, m_TexCoord0);
    vec4 color1 = texture2D(g_InputTex1, m_TexCoord0);

    gl_FragColor = vec4(color0.rgb * color1.rgb, color0.a);
}