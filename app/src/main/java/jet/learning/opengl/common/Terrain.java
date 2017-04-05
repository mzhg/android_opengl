package jet.learning.opengl.common;

import android.opengl.GLES30;
import android.opengl.GLES32;

import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.GLUtil;
import com.nvidia.developer.opengl.utils.Glut;
import com.nvidia.developer.opengl.utils.NvGLSLProgram;
import com.nvidia.developer.opengl.utils.NvImage;
import com.nvidia.developer.opengl.utils.NvPackedColor;
import com.nvidia.developer.opengl.utils.NvUtils;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.ReadableVector3f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL11;


public class Terrain {

	static final int TER_POSITION = 0;
	static final int TER_TEXCOORD = 1;
	static final int TER_BOUNDSY = 2;
	// Divide heightmap into patches such that each patch has CellsPerPatch cells
	// and CellsPerPatch+1 vertices.  Use 64 so that if we tessellate all the way 
	// to 64, we use all the data from the heightmap.  
	static final int CELLS_PER_PATCH = 64;
	
	int mQuadPatchVB;
	int mQuadPatchIB;
	int mQuadPatchVBO;

	int mLayerMapArraySRV;
	int mBlendMapSRV;
	int mHeightMapSRV;

	final InitInfo mInfo = new InitInfo();

	int mNumPatchVertices;
	int mNumPatchQuadFaces;

	int mNumPatchVertRows;
	int mNumPatchVertCols;

	final Matrix4f mWorld = new Matrix4f();

	final Material mMat = new Material();
	final TerrainProgram mProgram = new TerrainProgram();
	
	final Vector4f[] mPlanes = new Vector4f[6];

	Vector2f[] mPatchBoundsY;
	float[] mHeightmap;
	float[] mSmooth;
	
	public Terrain() {
		mMat.ambient  .set(1.0f, 1.0f, 1.0f, 1.0f);
		mMat.diffuse  .set(1.0f, 1.0f, 1.0f, 1.0f);
		mMat.specular .set(0.0f, 0.0f, 0.0f, 64.0f);
		mMat.reflect  .set(0.0f, 0.0f, 0.0f, 1.0f);
	}
	
	public int getProgramHanlder() { return mProgram.program;}
	public void dispose(){
		GLES.glDeleteVertexArrays(mQuadPatchVBO);
		GLES.glDeleteBuffers(mQuadPatchIB);
		GLES.glDeleteBuffers(mQuadPatchVB);

		GLES.glDeleteTextures(mLayerMapArraySRV);
		GLES.glDeleteTextures(mBlendMapSRV);
		GLES.glDeleteTextures(mHeightMapSRV);
	}
	
	public void draw(Matrix4f mvp, ReadableVector3f cameraPos,  UniformLights lights){
//		VectorUtil.extractFrustumPlanes(cam.viewProj(), mPlanes);
		Matrix4f.extractFrustumPlanes(mvp, mPlanes);

		mProgram.enable();
		ReadableVector3f c = NvPackedColor.SILVER;
		lights.gEyePosW.set(cameraPos);
		lights.gFogColor.set(c.getX(), c.getY(), c.getZ(), 1.0f);
		lights.gFogStart = 15.0f;
		lights.gFogRange = 175.0f;
		lights.apply();
		
		// Set per frame constants.
		mProgram.setViewProj(mvp);
		mProgram.setEyePos(cameraPos);
//		mProgram.setDirLights(lights);
//		mProgram.setFogColor(Colors::Silver);
//		mProgram.setFogStart(15.0f);
//		mProgram.setFogRange(175.0f);
		mProgram.setMinDist(20.0f);
		mProgram.setMaxDist(500.0f);
		mProgram.setMinTess(0.0f);
		mProgram.setMaxTess(6.0f);
		mProgram.setTexelCellSpaceU(1.0f / mInfo.heightmapWidth);
		mProgram.setTexelCellSpaceV(1.0f / mInfo.heightmapHeight);
		mProgram.setWorldCellSpace(mInfo.cellSpacing);
		mProgram.setWorldFrustumPlanes(mPlanes);
		
		GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
		GLES30.glBindTexture(GLES30.GL_TEXTURE_2D_ARRAY, mLayerMapArraySRV);
		GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
		GLES30.glBindTexture(GL11.GL_TEXTURE_2D, mBlendMapSRV);
		GLES30.glActiveTexture(GLES30.GL_TEXTURE2);
		GLES30.glBindTexture(GL11.GL_TEXTURE_2D, mHeightMapSRV);
		mProgram.setLayerMapArray(0);
		mProgram.setBlendMap(1);
		mProgram.setHeightMap(2);

		mProgram.setMaterial(mMat);

		GLES32.glPatchParameteri(GLES32.GL_PATCH_VERTICES, 4);
		GLES30.glBindVertexArray(mQuadPatchVBO);
		GLES32.glDrawElements(GLES32.GL_PATCHES, mNumPatchQuadFaces*4, GL11.GL_UNSIGNED_SHORT, 0);
	}
	
	float getWidth()
	{
		// Total terrain width.
		return (mInfo.heightmapWidth-1)*mInfo.cellSpacing;
	}

	float getDepth()
	{
		// Total terrain depth.
		return (mInfo.heightmapHeight-1)*mInfo.cellSpacing;
	}
	
	float _getHeight(int row, int col){
		row = Math.abs(row % mInfo.heightmapHeight);
		col = Math.abs(col % mInfo.heightmapHeight);
		
	    return mHeightmap[row * mInfo.heightmapWidth + col];
	}
	
	public float getHeight(float x, float z)
	{
		// Transform from terrain local space to "cell" space.
		float c = (x + 0.5f*getWidth()) /  mInfo.cellSpacing;
		float d = (z - 0.5f*getDepth()) / -mInfo.cellSpacing;

		// Get the row and column we are in.
		int row = (int)Math.floor(d);
		int col = (int)Math.floor(c);

		// Grab the heights of the cell we are in.
		// A*--*B
		//  | /|
		//  |/ |
		// C*--*D
//		float A = mHeightmap[row*mInfo.heightmapWidth + col];
//		float B = mHeightmap[row*mInfo.heightmapWidth + col + 1];
//		float C = mHeightmap[(row+1)*mInfo.heightmapWidth + col];
//		float D = mHeightmap[(row+1)*mInfo.heightmapWidth + col + 1];
		float A = _getHeight(row, col);
		float B = _getHeight(row, col + 1);
		float C = _getHeight(row + 1, col);
		float D = _getHeight(row + 1, col + 1);

		// Where we are relative to the cell.
		float s = c - (float)col;
		float t = d - (float)row;

		// If upper triangle ABC.
		if( s + t <= 1.0f)
		{
			float uy = B - A;
			float vy = C - A;
			return A + s*uy + t*vy;
		}
		else // lower triangle DCB.
		{
			float uy = C - D;
			float vy = B - D;
			return D + (1.0f-s)*uy + (1.0f-t)*vy;
		}
	}
	
	Matrix4f getWorld() { return mWorld; }

	void setWorld(Matrix4f m){ mWorld.load(m); }
	
	public void init(InitInfo initInfo)
	{
		mInfo.load(initInfo);

		// Divide heightmap into patches such that each patch has CellsPerPatch.
		mNumPatchVertRows = ((mInfo.heightmapHeight-1) / CELLS_PER_PATCH) + 1;
		mNumPatchVertCols = ((mInfo.heightmapWidth-1) / CELLS_PER_PATCH) + 1;

		mNumPatchVertices  = mNumPatchVertRows*mNumPatchVertCols;
		mNumPatchQuadFaces = (mNumPatchVertRows-1)*(mNumPatchVertCols-1);

		loadHeightmap();
		smooth();
		calcAllPatchBoundsY();

		buildQuadPatchVB();
		buildQuadPatchIB();
		buildQuadPatchVBO();
		buildHeightmapSRV();

		String[] layerFilenames = new String[5];
		layerFilenames[0] = (mInfo.layerMapFilename0);
		layerFilenames[1] = (mInfo.layerMapFilename1);
		layerFilenames[2] = (mInfo.layerMapFilename2);
		layerFilenames[3] = (mInfo.layerMapFilename3);
		layerFilenames[4] = (mInfo.layerMapFilename4);
//		mLayerMapArraySRV = d3dHelper::CreateTexture2DArraySRV(device, dc, layerFilenames);
		mLayerMapArraySRV = Glut.nvImageLoadTextureArrayFromDDSData(layerFilenames, 5);
		GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D_ARRAY);
		GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
		GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
		GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
		GLES30.glBindTexture(GLES30.GL_TEXTURE_2D_ARRAY, 0);
        
//		HR(D3DX11CreateShaderResourceViewFromFile(device, 
//			mInfo.BlendMapFilename.c_str(), 0, 0, &mBlendMapSRV, 0));
		
        mBlendMapSRV = NvImage.uploadTextureFromDDSFile(mInfo.blendMapFilename);
		GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
		GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
		GLES30.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        
        mProgram.init();
	}
	
	void loadHeightmap()
	{
		// A height for each vertex
//		std::vector<unsigned char> in( mInfo.HeightmapWidth * mInfo.HeightmapHeight );
		byte[] in = new byte[mInfo.heightmapWidth * mInfo.heightmapHeight];

		// Open the file.
//		std::ifstream inFile;
//		inFile.open(mInfo.HeightMapFilename.c_str(), std::ios_base::binary);
//
//		if(inFile)
//		{
//			// Read the RAW bytes.
//			inFile.read((char*)&in[0], (std::streamsize)in.size());
//
//			// Done with file.
//			inFile.close();
//		}
		
		try {
			InputStream inFile = Glut.readFileStream(mInfo.heightMapFilename);
			inFile.read(in, 0, in.length);
			inFile.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Copy the array data into a float array and scale it.
		mHeightmap = new float[mInfo.heightmapHeight * mInfo.heightmapWidth];
		for(int i = 0; i < mInfo.heightmapHeight * mInfo.heightmapWidth; ++i)
		{
			mHeightmap[i] = ((in[i] & 0xFF) / 255.0f)*mInfo.heightScale;
		}
	}

	void smooth()
	{
//		std::vector<float> dest( mHeightmap.size() );
		if(mSmooth == null) mSmooth = new float[mHeightmap.length];
		
		float[] dest = mSmooth;

		for(int i = 0; i < mInfo.heightmapHeight; ++i)
		{
			for(int j = 0; j < mInfo.heightmapWidth; ++j)
			{
				dest[i*mInfo.heightmapWidth+j] = average(i,j);
			}
		}

		// Replace the old heightmap with the filtered one.
		float[] t = mHeightmap;
		mHeightmap = dest;
		dest = t;
	}

	boolean inBounds(int i, int j)
	{
		// True if ij are valid indices; false otherwise.
		return 
			i >= 0 && i < (int)mInfo.heightmapHeight && 
			j >= 0 && j < (int)mInfo.heightmapWidth;
	}

	float average(int i, int j)
	{
		// Function computes the average height of the ij element.
		// It averages itself with its eight neighbor pixels.  Note
		// that if a pixel is missing neighbor, we just don't include it
		// in the average--that is, edge pixels don't have a neighbor pixel.
		//
		// ----------
		// | 1| 2| 3|
		// ----------
		// |4 |ij| 6|
		// ----------
		// | 7| 8| 9|
		// ----------

		float avg = 0.0f;
		float num = 0.0f;

		// Use int to allow negatives.  If we use int, @ i=0, m=i-1=int_MAX
		// and no iterations of the outer for loop occur.
		for(int m = i-1; m <= i+1; ++m)
		{
			for(int n = j-1; n <= j+1; ++n)
			{
				if( inBounds(m,n) )
				{
					avg += mHeightmap[m*mInfo.heightmapWidth + n];
					num += 1.0f;
				}
			}
		}

		return avg / num;
	}

	void calcAllPatchBoundsY()
	{
//		mPatchBoundsY.resize(mNumPatchQuadFaces);
		if(mPatchBoundsY == null) mPatchBoundsY = new Vector2f[mNumPatchQuadFaces];
		else
		mPatchBoundsY = (Vector2f[]) NvUtils.resizeArray(mPatchBoundsY, mNumPatchQuadFaces);

		// For each patch
		for(int i = 0; i < mNumPatchVertRows-1; ++i)
		{
			for(int j = 0; j < mNumPatchVertCols-1; ++j)
			{
				calcPatchBoundsY(i, j);
			}
		}
	}

	void calcPatchBoundsY(int i, int j)
	{
		// Scan the heightmap values this patch covers and compute the min/max height.

		int x0 = j*CELLS_PER_PATCH;
		int x1 = (j+1)*CELLS_PER_PATCH;

		int y0 = i*CELLS_PER_PATCH;
		int y1 = (i+1)*CELLS_PER_PATCH;

		float minY = Float.POSITIVE_INFINITY;
		float maxY = Float.NEGATIVE_INFINITY;
		for(int y = y0; y <= y1; ++y)
		{
			for(int x = x0; x <= x1; ++x)
			{
				int k = y*mInfo.heightmapWidth + x;
				minY = Math.min(minY, mHeightmap[k]);
				maxY = Math.max(maxY, mHeightmap[k]);
			}
		}

		int patchID = i*(mNumPatchVertCols-1)+j;
		if(mPatchBoundsY[patchID] == null) 
			mPatchBoundsY[patchID] = new Vector2f();
		mPatchBoundsY[patchID].set(minY, maxY);
	}
	
	void buildQuadPatchVB()
	{
//		std::vector<Vertex::Terrain> patchVertices(mNumPatchVertRows*mNumPatchVertCols);
		TerrainData[] patchVertices = new TerrainData[mNumPatchVertRows*mNumPatchVertCols];
		for(int i = 0; i < patchVertices.length; i++)
			patchVertices[i] = new TerrainData();

		float halfWidth = 0.5f*getWidth();
		float halfDepth = 0.5f*getDepth();

		float patchWidth = getWidth() / (mNumPatchVertCols-1);
		float patchDepth = getDepth() / (mNumPatchVertRows-1);
		float du = 1.0f / (mNumPatchVertCols-1);
		float dv = 1.0f / (mNumPatchVertRows-1);

		for(int i = 0; i < mNumPatchVertRows; ++i)
		{
			float z = halfDepth - i*patchDepth;
			for(int j = 0; j < mNumPatchVertCols; ++j)
			{
				float x = -halfWidth + j*patchWidth;

				patchVertices[i*mNumPatchVertCols+j].pos.set(x, 0.0f, z);

				// Stretch texture over grid.
				patchVertices[i*mNumPatchVertCols+j].tex.x = j*du;
				patchVertices[i*mNumPatchVertCols+j].tex.y = i*dv;
			}
		}

		// Store axis-aligned bounding box y-bounds in upper-left patch corner.
		for(int i = 0; i < mNumPatchVertRows-1; ++i)
		{
			for(int j = 0; j < mNumPatchVertCols-1; ++j)
			{
				int patchID = i*(mNumPatchVertCols-1)+j;
				patchVertices[i*mNumPatchVertCols+j].boundsY.set(mPatchBoundsY[patchID]);
			}
		}

//	    D3D11_BUFFER_DESC vbd;
//	    vbd.Usage = D3D11_USAGE_IMMUTABLE;
//		vbd.ByteWidth = sizeof(Vertex::Terrain) * patchVertices.size();
//	    vbd.BindFlags = D3D11_BIND_VERTEX_BUFFER;
//	    vbd.CPUAccessFlags = 0;
//	    vbd.MiscFlags = 0;
//		vbd.StructureByteStride = 0;
//
//		D3D11_SUBRESOURCE_DATA vinitData;
//	    vinitData.pSysMem = &patchVertices[0];
//	    HR(device->CreateBuffer(&vbd, &vinitData, &mQuadPatchVB));
		
		mQuadPatchVB = GLES.glGenBuffers();
		GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mQuadPatchVB);
		FloatBuffer patchData = wrap(patchVertices);
		GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, patchData.remaining() * 4, patchData, GLES30.GL_STATIC_DRAW);
		GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
	}

	void buildQuadPatchIB(/*ID3D11Device* device*/)
	{
//		std::vector<USHORT> indices(mNumPatchQuadFaces*4); // 4 indices per quad face
		short[] indices = new short[mNumPatchQuadFaces*4];

		// Iterate over each quad and compute indices.
		int k = 0;
		for(int i = 0; i < mNumPatchVertRows-1; ++i)
		{
			for(int j = 0; j < mNumPatchVertCols-1; ++j)
			{
				// Top row of 2x2 quad patch
				indices[k]   = (short) (i*mNumPatchVertCols+j);
				indices[k+1] = (short) (i*mNumPatchVertCols+j+1);

				// Bottom row of 2x2 quad patch
				indices[k+2] = (short) ((i+1)*mNumPatchVertCols+j);
				indices[k+3] = (short) ((i+1)*mNumPatchVertCols+j+1);

				k += 4; // next quad
			}
		}

//		D3D11_BUFFER_DESC ibd;
//	    ibd.Usage = D3D11_USAGE_IMMUTABLE;
//		ibd.ByteWidth = sizeof(USHORT) * indices.size();
//	    ibd.BindFlags = D3D11_BIND_INDEX_BUFFER;
//	    ibd.CPUAccessFlags = 0;
//	    ibd.MiscFlags = 0;
//		ibd.StructureByteStride = 0;
//
//	    D3D11_SUBRESOURCE_DATA iinitData;
//	    iinitData.pSysMem = &indices[0];
//	    HR(device->CreateBuffer(&ibd, &iinitData, &mQuadPatchIB));
		mQuadPatchIB = GLES.glGenBuffers();
		GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, mQuadPatchIB);
		GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER, indices.length * 2, GLUtil.wrap(indices), GLES30.GL_STATIC_DRAW);
		GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0);
	}
	
	void buildQuadPatchVBO(){
		mQuadPatchVBO = GLES.glGenVertexArrays();
		GLES30.glBindVertexArray(mQuadPatchVBO);
		{
			GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mQuadPatchVB);

			GLES30.glVertexAttribPointer(TER_POSITION, 3, GL11.GL_FLOAT, false, 7 * 4, 0);
			GLES30.glVertexAttribPointer(TER_TEXCOORD, 2, GL11.GL_FLOAT, false, 7 * 4, 3 * 4);
			GLES30.glVertexAttribPointer(TER_BOUNDSY, 2, GL11.GL_FLOAT, false, 7 * 4, 5 * 4);

			GLES30.glEnableVertexAttribArray(TER_POSITION);
			GLES30.glEnableVertexAttribArray(TER_TEXCOORD);
			GLES30.glEnableVertexAttribArray(TER_BOUNDSY);

			GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, mQuadPatchIB);
		}
		GLES30.glBindVertexArray(0);
	}
	
	void buildHeightmapSRV(){
		// Convert 32-bit floating-point to 16-bit floating-point.
//		System.out.println(GLContext.getCapabilities().GL_ARB_half_float_pixel ? "Half Float Supported" : "Half Float didn't Supported");
		short[] hmap = new short[mHeightmap.length];
		
		HDRImage.fp32toFp16(mHeightmap, 0, hmap, 0, hmap.length);
		
//		public static void fp32toFp16(float[] pt, int pt_offset, short[] out, int out_offset, int length)
//		{
//			for (int i=0;i<length;i++) {
//				out[i] = convertFloatToHFloat(pt[i]);
//			}
//		}
		
//		GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 2);
		mHeightMapSRV = GLES.glGenTextures();
		GLES30.glBindTexture(GL11.GL_TEXTURE_2D, mHeightMapSRV);
		GLES30.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GLES30.GL_R32F, mInfo.heightmapWidth, mInfo.heightmapHeight, 0, GLES30.GL_RED, GL11.GL_FLOAT,GLUtil.wrap(mHeightmap));
		GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GLES30.glBindTexture(GL11.GL_TEXTURE_2D, 0);
//		GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 4);
	}
	
	public static final class InitInfo{
		public String heightMapFilename;
		public String layerMapFilename0;
		public String layerMapFilename1;
		public String layerMapFilename2;
		public String layerMapFilename3;
		public String layerMapFilename4;
		public String blendMapFilename;
		public float heightScale;
		public int heightmapWidth;
		public int heightmapHeight;
		public float cellSpacing;
		
		void load(InitInfo info){
			heightMapFilename = info.heightMapFilename;
			layerMapFilename0 = info.layerMapFilename0;
			layerMapFilename1 = info.layerMapFilename1;
			layerMapFilename2 = info.layerMapFilename2;
			layerMapFilename3 = info.layerMapFilename3;
			layerMapFilename4 = info.layerMapFilename4;
			blendMapFilename = info.blendMapFilename;
			heightScale = info.heightScale;
			heightmapWidth = info.heightmapWidth;
			heightmapHeight = info.heightmapHeight;
			cellSpacing = info.cellSpacing;
		}
	}
	
	private static final class TerrainProgram{
		int program;
		
		int fogEnabled = -1;
		int viewProj = -1;
		
		int minDist = -1;
		int maxDist = -1;
		int minTess = -1;
		int maxTess = -1;
		int eyePos  = -1;
		int texelCellSpaceU = -1;
		int texelCellSpaceV = -1;
		int worldCellSpace = -1;
		int worldFrustumPlanes = -1;
		
		int layerMapArray = -1;
		int blendMap = -1;
		int heightMap = -1;
		
		final UniformMaterial mat = new UniformMaterial(); 
		
		public void init() {
			Class<?> clazz = DirectionalLight.class;
			String lightHelpString = Glut.loadTextFromClassPath(clazz, "LightHelper.glsl").toString();
			CharSequence vertexString = Glut.loadTextFromClassPath(clazz, "terrain.glvs");
			CharSequence tessControl = Glut.loadTextFromClassPath(clazz, "terrain.gltc");
			CharSequence tessEval = Glut.loadTextFromClassPath(clazz, "terrain.glte");
			StringBuilder fragmentString = (StringBuilder) Glut.loadTextFromClassPath(clazz, "terrain.glfs");
			
			String include = "#include \"LightHelper.glsl\"";
			
			int index = fragmentString.indexOf(include);
			fragmentString.replace(index, index + include.length(), lightHelpString);
			
//			GLProgram program = new GLProgram();
//			program.compileShaderFromString(vertexString, GLShaderType.VERTEX);
//			program.compileShaderFromString(tessControl, GLShaderType.TESS_CONTROL);
//			program.compileShaderFromString(tessEval, GLShaderType.TESS_EVALUATION);
//			program.compileShaderFromString(fragmentString, GLShaderType.FRAGMENT);
//
//			program.link();
			NvGLSLProgram.ShaderSourceItem vs_item = new NvGLSLProgram.ShaderSourceItem(vertexString, GLES32.GL_VERTEX_SHADER);
			NvGLSLProgram.ShaderSourceItem tc_item = new NvGLSLProgram.ShaderSourceItem(tessControl, GLES32.GL_TESS_CONTROL_SHADER);
			NvGLSLProgram.ShaderSourceItem te_item = new NvGLSLProgram.ShaderSourceItem(tessEval, GLES32.GL_TESS_EVALUATION_SHADER);
			NvGLSLProgram.ShaderSourceItem fs_item = new NvGLSLProgram.ShaderSourceItem(fragmentString, GLES32.GL_FRAGMENT_SHADER);
			NvGLSLProgram program = new NvGLSLProgram();
			program.setSourceFromStrings(vs_item, tc_item, te_item, fs_item);
			this.program = program.getProgram();
			
			fogEnabled = GLES32.glGetUniformLocation(this.program, "gFogEnabled");
			viewProj = GLES32.glGetUniformLocation(this.program, "gViewProj");
			
			minDist = GLES32.glGetUniformLocation(this.program, "gMinDist");
			maxDist = GLES32.glGetUniformLocation(this.program, "gMaxDist");
			minTess = GLES32.glGetUniformLocation(this.program, "gMinTess");
			maxTess = GLES32.glGetUniformLocation(this.program, "gMaxTess");
			texelCellSpaceU = GLES32.glGetUniformLocation(this.program, "gTexelCellSpaceU");
			texelCellSpaceV = GLES32.glGetUniformLocation(this.program, "gTexelCellSpaceV");
			worldCellSpace = GLES32.glGetUniformLocation(this.program, "gWorldCellSpace");
			worldFrustumPlanes = GLES32.glGetUniformLocation(this.program, "gWorldFrustumPlanes");
			layerMapArray = GLES32.glGetUniformLocation(this.program, "gLayerMapArray");
			blendMap = GLES32.glGetUniformLocation(this.program, "gBlendMap");
			heightMap = GLES32.glGetUniformLocation(this.program, "gHeightMap");
			eyePos = GLES32.glGetUniformLocation(this.program, "uEyePosW");
			
			mat.init(this.program);
		}
		
		void enable() {GLES32.glUseProgram(program);}
		
		void setViewProj(Matrix4f m)                       { GLES32.glUniformMatrix4fv(viewProj, 1, false, GLUtil.wrap(m)); }
		void setEyePos(ReadableVector3f pos)               { GLES32.glUniform3f(eyePos, pos.getX(), pos.getY(), pos.getZ()); }

		void setMaterial(Material m) {mat.apply(m);}
		void setMinDist(float f)                            { GLES32.glUniform1f(minDist, f); }
		void setMaxDist(float f)                            { GLES32.glUniform1f(maxDist, f); }
		void setMinTess(float f)                            { GLES32.glUniform1f(minTess, f); }
		void setMaxTess(float f)                            { GLES32.glUniform1f(maxTess, f); }
		void setTexelCellSpaceU(float f)                    { GLES32.glUniform1f(texelCellSpaceU, f); }
		void setTexelCellSpaceV(float f)                    { GLES32.glUniform1f(texelCellSpaceV, f);}
		void setWorldCellSpace(float f)                     { GLES32.glUniform1f(worldCellSpace, f); }
		void setWorldFrustumPlanes(Vector4f[] planes)      { GLES32.glUniform4fv(worldFrustumPlanes, planes.length, GLUtil.wrap(planes)); }

		void setLayerMapArray(int unit)   { GLES32.glUniform1i(layerMapArray, unit); }
		void setBlendMap(int unit)        { GLES32.glUniform1i(blendMap, unit); }
		void setHeightMap(int unit)       { GLES32.glUniform1i(heightMap, unit); }
	}
	
	private static final class TerrainData{
		final Vector3f pos = new Vector3f();
		final Vector2f tex = new Vector2f();
		final Vector2f boundsY = new Vector2f();
		
		void store(FloatBuffer buf){
			pos.store(buf);
			tex.store(buf);
			boundsY.store(buf);
		}
	}
	
	static FloatBuffer wrap(TerrainData[] data){
		FloatBuffer buf = GLUtil.getCachedFloatBuffer(data.length * 7);
		for(int i = 0; i < data.length; i++)
			data[i].store(buf);
		buf.flip();
		return buf;
	}
}
