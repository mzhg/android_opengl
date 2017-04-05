package jet.learning.opengl.common;

import android.opengl.GLES30;

import com.nvidia.developer.opengl.utils.GLES;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.List;

import javax.microedition.khronos.opengles.GL11;

public class MeshGeometry {
	
	// vertex attributes location
	public static final int POSITION = 0;
	public static final int NORMAL = 1;
	public static final int TEXCOORD = 2;
	public static final int TANGENT = 3;
	public static final int WEIGHTS = 4;
	public static final int BONE_INDEX = 5;

	int mVB;
	int mIB;
	int mVAO;
	
	int mIndexBufferFormat = GL11.GL_UNSIGNED_SHORT;
	int mVertexStride;
	
    private boolean basic = true;
	
	List<Subset> mSubsetTable;
	
	public void setSkinVertices(FloatBuffer vertices){
		basic = false;
		if(mVB != 0) GLES.glDeleteBuffers(mVB);
		
		mVB = GLES.glGenBuffers();
		GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVB);
		GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vertices.remaining() * 4, vertices, GLES30.GL_STATIC_DRAW);
		GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
	}
	
	public void setVertices(FloatBuffer vertices){
		basic = true;
		if(mVB != 0) GLES.glDeleteBuffers(mVB);
		
		mVB = GLES.glGenBuffers();
		GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVB);
		GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vertices.remaining() * 4, vertices, GLES30.GL_STATIC_DRAW);
		GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
	}
	
	public void setIndices(ShortBuffer indices){
		if(mIB != 0) GLES.glDeleteBuffers(mIB);
		
		mIB = GLES.glGenBuffers();
		GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, mIB);
		GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER, indices.remaining() * 2, indices, GLES30.GL_STATIC_DRAW);
		GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0);
	}
	
	public void setSubsetTable(List<Subset> subsetTable){
		mSubsetTable = subsetTable;
	}
	
	public void draw(int subsetId){
		GLES30.glBindVertexArray(mVAO);
		GLES30.glDrawElements(GL11.GL_TRIANGLES, mSubsetTable.get(subsetId).faceCount * 3, GL11.GL_UNSIGNED_SHORT, mSubsetTable.get(subsetId).faceStart * 3 * 2);
		GLES30.glBindVertexArray(0);
	}
	
	public void buildVAO() {
		mVAO = GLES.glGenVertexArrays();
		GLES30.glBindVertexArray(mVAO);
		{
			final int stride = (basic ?Vertex.FLOAT_COUNT : PosNormalTexTanSkinned.FLOAT_COUNT) * 4 ;
			GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVB);

			GLES30.glVertexAttribPointer(POSITION, 3, GL11.GL_FLOAT, false, stride, 0);
			GLES30.glVertexAttribPointer(NORMAL, 3, GL11.GL_FLOAT, false, stride, 3 * 4);
			GLES30.glVertexAttribPointer(TEXCOORD, 2, GL11.GL_FLOAT, false, stride, (basic? 9 : 10) * 4);
			GLES30.glVertexAttribPointer(TANGENT, basic ? 3 : 4, GL11.GL_FLOAT, false, stride, 6 * 4);

			GLES30.glEnableVertexAttribArray(POSITION);
			GLES30.glEnableVertexAttribArray(NORMAL);
			GLES30.glEnableVertexAttribArray(TEXCOORD);
			GLES30.glEnableVertexAttribArray(TANGENT);
			
			if(!basic){
				GLES30.glVertexAttribPointer(WEIGHTS, 3, GL11.GL_FLOAT, false, stride, 12 * 4);
				GLES30.glVertexAttribPointer(BONE_INDEX, 4, GL11.GL_FLOAT, false, stride, 15 * 4);

				GLES30.glEnableVertexAttribArray(WEIGHTS);
				GLES30.glEnableVertexAttribArray(BONE_INDEX);
			}

			GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, mIB);
		}

		GLES30.glBindVertexArray(0);
	}
	
	public void dispose(){
		if(mVB != 0) GLES.glDeleteBuffers(mVB);
		if(mIB != 0) GLES.glDeleteBuffers(mIB);
	}
	
	public static final class Subset{
		public int id = -1;
		public int vertexStart;
		public int vertexCount;
		public int faceStart;
		public int faceCount;
	}
}
