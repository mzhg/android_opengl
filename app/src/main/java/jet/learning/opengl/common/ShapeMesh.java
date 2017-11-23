package jet.learning.opengl.common;

import android.opengl.GLES20;

import com.nvidia.developer.opengl.utils.BufferUtils;
import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.NvUtils;

import org.lwjgl.util.vector.Matrix4f;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL11;

/**
 * Created by mazhen'gui on 2017/11/22.
 */

public class ShapeMesh implements RenderMesh {
    // Define transformations from local spaces to world space.
    final Matrix4f[] mSphereWorld = new Matrix4f[10];
    final Matrix4f[] mCylWorld = new Matrix4f[10];
    final Matrix4f mBoxWorld = new Matrix4f();
    final Matrix4f mGridWorld = new Matrix4f();
    final Matrix4f mCenterSphereWorld = new Matrix4f();

    int mBoxVertexOffset;
    int mGridVertexOffset;
    int mSphereVertexOffset;
    int mCylinderVertexOffset;

    int mBoxIndexOffset;
    int mGridIndexOffset;
    int mSphereIndexOffset;
    int mCylinderIndexOffset;

    int mBoxIndexCount;
    int mGridIndexCount;
    int mSphereIndexCount;
    int mCylinderIndexCount;

    int mShapesVB;
    int mShapesIB;

    int mfxPosition;
    int mfxNormal;
    int mfxTex;

    int mIndiceType;

    @Override
    public void dispose() {
        GLES.glDeleteBuffers(mShapesVB);
        GLES.glDeleteBuffers(mShapesIB);
    }

    @Override
    public void initlize(MeshParams params) {
        mfxPosition = params.posAttribLoc;
        mfxNormal = params.norAttribLoc;
        mfxTex = params.texAttribLoc;

        mBoxWorld.translate(0.0f, 0.5f, 0.0f);
        mBoxWorld.scale(3.0f, 1.0f, 3.0f);

        mCenterSphereWorld.translate(0.0f, 2.0f, 0.0f);
        mCenterSphereWorld.scale(2.0f);

//		mSkullWorld.translate(vec3(0.0f, 1.0f, 0.0f));
//		mSkullWorld.scale(vec3(0.5f));

        for(int i = 0; i < 5; i++){
            mCylWorld[i * 2 + 0] = new Matrix4f();
            mCylWorld[i * 2 + 0].translate(-5.0f, 1.5f, -10.0f + i*5.0f);
            mCylWorld[i * 2 + 1] = new Matrix4f();
            mCylWorld[i * 2 + 1].translate(+5.0f, 1.5f, -10.0f + i*5.0f);


            mSphereWorld[i * 2 + 0] = new Matrix4f();
            mSphereWorld[i * 2 + 0].translate(-5.0f, 3.5f, -10.0f + i*5.0f);
            mSphereWorld[i * 2 + 1] = new Matrix4f();
            mSphereWorld[i * 2 + 1].translate(+5.0f, 3.5f, -10.0f + i*5.0f);
        }

        MeshData box = new MeshData();
        MeshData grid = new MeshData();
        MeshData sphere = new MeshData();
        MeshData cylinder = new MeshData();

        GeometryGenerator geoGen = new GeometryGenerator();
        geoGen.createBox(1.0f, 1.0f, 1.0f, box);
        geoGen.createGrid(20.0f, 30.0f, 60, 40, grid);
        geoGen.createSphere(0.5f, 20, 20, sphere);
        geoGen.createCylinder(0.5f, 0.3f, 3.0f, 20, 20, cylinder);

        // Cache the vertex offsets to each object in the concatenated vertex buffer.
        mBoxVertexOffset      = 0;
        mGridVertexOffset     = box.vertices.size();
        mSphereVertexOffset   = mGridVertexOffset + grid.vertices.size();
        mCylinderVertexOffset = mSphereVertexOffset + sphere.vertices.size();

        // Cache the index count of each object.
        mBoxIndexCount      = box.indices.size();
        mGridIndexCount     = grid.indices.size();
        mSphereIndexCount   = sphere.indices.size();
        mCylinderIndexCount = cylinder.indices.size();

        // Cache the starting index for each object in the concatenated index buffer.
        mBoxIndexOffset      = 0;
        mGridIndexOffset     = mBoxIndexCount;
        mSphereIndexOffset   = mGridIndexOffset + mGridIndexCount;
        mCylinderIndexOffset = mSphereIndexOffset + mSphereIndexCount;

        int totalVertexCount =
                box.vertices.size() +
                        grid.vertices.size() +
                        sphere.vertices.size() +
                        cylinder.vertices.size();

        int totalIndexCount =
                mBoxIndexCount +
                        mGridIndexCount +
                        mSphereIndexCount +
                        mCylinderIndexCount;

        //
        // Extract the vertex elements we are interested in and pack the
        // vertices of all the meshes into one vertex buffer.
        //

//		std::vector<Vertex> vertices(totalVertexCount);
        FloatBuffer vertices = BufferUtils.createFloatBuffer(totalVertexCount * 8 /* 3-tuple position + 3-tuple color */);

//		XMFLOAT4 black(0.0f, 0.0f, 0.0f, 1.0f);

        for(int i = 0; i < box.vertices.size(); ++i)
        {
            Vertex v = box.vertices.get(i);
            vertices.put(v.positionX).put(v.positionY).put(v.positionZ);
            vertices.put(v.normalX).put(v.normalY).put(v.normalZ);
            vertices.put(v.texCX).put(v.texCY);
        }

        for(int i = 0; i < grid.vertices.size(); ++i)
        {
            Vertex v = grid.vertices.get(i);
            vertices.put(v.positionX).put(v.positionY).put(v.positionZ);
            vertices.put(v.normalX).put(v.normalY).put(v.normalZ);
            vertices.put(v.texCX).put(v.texCY);
        }

        for(int i = 0; i < sphere.vertices.size(); ++i)
        {
            Vertex v = sphere.vertices.get(i);
            vertices.put(v.positionX).put(v.positionY).put(v.positionZ);
            vertices.put(v.normalX).put(v.normalY).put(v.normalZ);
            vertices.put(v.texCX).put(v.texCY);
        }

        for(int i = 0; i < cylinder.vertices.size(); ++i)
        {
            Vertex v = cylinder.vertices.get(i);
            vertices.put(v.positionX).put(v.positionY).put(v.positionZ);
            vertices.put(v.normalX).put(v.normalY).put(v.normalZ);
            vertices.put(v.texCX).put(v.texCY);
        }
        vertices.flip();

        mShapesVB = GLES.glGenBuffers();
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mShapesVB);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertices.remaining() << 2, vertices, GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        //
        // Pack the indices of all the meshes into one index buffer.
        //

        /*ShortBuffer indices = GLUtil.getCachedShortBuffer(totalIndexCount);
        indices.put(box.indices.getData(), 0, box.indices.size());
        indices.put(grid.indices.getData(), 0, grid.indices.size());
        indices.put(sphere.indices.getData(), 0, sphere.indices.size());
        indices.put(cylinder.indices.getData(), 0, cylinder.indices.size());
        indices.flip();*/
        int totalIndiceCount = box.indices.size() + grid.indices.size() + sphere.indices.size() + cylinder.indices.size();
        ByteBuffer indicesBuffer;
        if(totalIndexCount > NvUtils.unsignedShort((short)-1)){
            mIndiceType = GLES20.GL_UNSIGNED_INT;
            indicesBuffer = BufferUtils.createByteBuffer(totalIndiceCount * 4);

            {
                short[] data = box.indices.getData();
                int count = box.indices.size();
                for(int i = 0; i < count; i++){
                    indicesBuffer.putInt(data[i]);
                }
            }

            {
                short[] data = grid.indices.getData();
                int count = grid.indices.size();
                for(int i = 0; i < count; i++){
                    indicesBuffer.putInt(data[i] + mGridVertexOffset);
                }
            }

            {
                short[] data = sphere.indices.getData();
                int count = sphere.indices.size();
                for(int i = 0; i < count; i++){
                    indicesBuffer.putInt(data[i] + mSphereVertexOffset);
                }
            }

            {
                short[] data = cylinder.indices.getData();
                int count = cylinder.indices.size();
                for(int i = 0; i < count; i++){
                    indicesBuffer.putInt(data[i] + mCylinderVertexOffset);
                }
            }

        }else{
            mIndiceType = GLES20.GL_UNSIGNED_SHORT;
            indicesBuffer = BufferUtils.createByteBuffer(totalIndiceCount * 2);

            {
                short[] data = box.indices.getData();
                int count = box.indices.size();
                for(int i = 0; i < count; i++){
                    indicesBuffer.putShort(data[i]);
                }
            }

            {
                short[] data = grid.indices.getData();
                int count = grid.indices.size();
                for(int i = 0; i < count; i++){
                    indicesBuffer.putShort((short) (data[i] + mGridVertexOffset));
                }
            }

            {
                short[] data = sphere.indices.getData();
                int count = sphere.indices.size();
                for(int i = 0; i < count; i++){
                    indicesBuffer.putShort((short) (data[i] + mSphereVertexOffset));
                }
            }

            {
                short[] data = cylinder.indices.getData();
                int count = cylinder.indices.size();
                for(int i = 0; i < count; i++){
                    indicesBuffer.putShort((short) (data[i] + mCylinderVertexOffset));
                }
            }
        }

        indicesBuffer.flip();
        mShapesIB = GLES.glGenBuffers();
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mShapesIB);
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer.remaining(), indicesBuffer, GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public Matrix4f getGridWorld() { return mGridWorld;}
    public void drawGrid(){
        int offset = mGridIndexOffset * ((mIndiceType == GLES20.GL_UNSIGNED_SHORT) ? 2 : 4);
//        glDrawElementsBaseVertex(GL11.GL_TRIANGLES, mGridIndexCount, GL11.GL_UNSIGNED_SHORT, mGridIndexOffset * 2, mGridVertexOffset);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mGridIndexCount, mIndiceType, offset);
    }

    public Matrix4f getBoxWorld() { return mBoxWorld;}
    public void drawBox(){
//        GL32.glDrawElementsBaseVertex(GL11.GL_TRIANGLES, mBoxIndexCount, GL11.GL_UNSIGNED_SHORT, mBoxIndexOffset * 2, mBoxVertexOffset);
        int offset = mBoxIndexOffset * ((mIndiceType == GLES20.GL_UNSIGNED_SHORT) ? 2 : 4);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mBoxIndexCount, mIndiceType, offset);
    }

    public Matrix4f getCylinderWorld(int index) { return mCylWorld[index];}

    public void drawCylinders(){
//        GL32.glDrawElementsBaseVertex(GL11.GL_TRIANGLES, mCylinderIndexCount, GL11.GL_UNSIGNED_SHORT, mCylinderIndexOffset * 2, mCylinderVertexOffset);
        int offset = mCylinderIndexOffset * ((mIndiceType == GLES20.GL_UNSIGNED_SHORT) ? 2 : 4);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mCylinderIndexCount, mIndiceType, offset);
    }

    public Matrix4f getCenterSphereWorld(){ return mCenterSphereWorld;}
    public void drawSphere(){
//        GL32.glDrawElementsBaseVertex(GL11.GL_TRIANGLES, mSphereIndexCount, GL11.GL_UNSIGNED_SHORT, mSphereIndexOffset * 2, mSphereVertexOffset);
        int offset = mSphereIndexOffset * ((mIndiceType == GLES20.GL_UNSIGNED_SHORT) ? 2 : 4);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mSphereIndexCount, mIndiceType, offset);
    }

    public Matrix4f getSphereWorld(int index){ return mSphereWorld[index];}

    public void bind(){
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mShapesVB);
        GLES20.glVertexAttribPointer(mfxPosition, 3, GL11.GL_FLOAT, false, 8 * 4, 0);
        GLES20.glVertexAttribPointer(mfxNormal, 3, GL11.GL_FLOAT, false, 8 * 4, 3 * 4);
        GLES20.glVertexAttribPointer(mfxTex, 2, GL11.GL_FLOAT, false, 8 * 4, 6 * 4);
        GLES20.glEnableVertexAttribArray(mfxPosition);
        GLES20.glEnableVertexAttribArray(mfxNormal);
        GLES20.glEnableVertexAttribArray(mfxTex);

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mShapesIB);
    }

    public void unbind(){
        GLES20.glDisableVertexAttribArray(mfxPosition);
        GLES20.glDisableVertexAttribArray(mfxNormal);
        GLES20.glDisableVertexAttribArray(mfxTex);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    @Override
    public void draw() {
        throw new UnsupportedOperationException();
    }
}
