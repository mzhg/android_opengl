package jet.learning.opengl.common;

import com.nvidia.developer.opengl.utils.StackShort;

import java.nio.FloatBuffer;
import java.util.List;


public class MeshData {

	public List<Vertex> vertices;
	public StackShort indices;
	
	public int getVertexCount(){
		return vertices == null ? 0 : vertices.size();
	}
	
	public int getIndiceCount(){
		return indices == null ? 0 : indices.size();
	}
	
	public void store(FloatBuffer buf){
		for(Vertex v : vertices)
			v.store(buf);
	}
}
