#version 300 es

out vec4 UVAndScreenPos;

void main()
{	
	int id = gl_VertexID;
	
	UVAndScreenPos.xy = vec2((id << 1) & 2, id & 2);
	gl_Position = vec4(UVAndScreenPos.xy * vec2(2,2) + vec2(-1,-1), 0, 1);
	UVAndScreenPos.zw = gl_Position.xy;
	
//	m_fInstID = gl_InstanceID;
}