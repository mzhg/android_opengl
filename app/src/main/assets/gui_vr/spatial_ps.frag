precision highp float;

varying vec2 a_texCoord;
uniform sampler2D g_SourceTex;
uniform vec4      g_Color;

void main()
{
    gl_FragColor = texture2D(g_SourceTex, a_texCoord) * g_Color;
}