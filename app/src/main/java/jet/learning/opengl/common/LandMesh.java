package jet.learning.opengl.common;

import android.opengl.GLES30;

import com.nvidia.developer.opengl.utils.BufferUtils;
import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.GLUtil;

import org.lwjgl.util.vector.ReadableVector3f;
import org.lwjgl.util.vector.Vector3f;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL11;

/**
 * Created by mazhen'gui on 2017/11/13.
 */
public class LandMesh implements RenderMesh{

    int mLandVB;
    int mLandIB;
    int mLandVBO;

    int mLandIndexCount;

    final Vector3f min = new Vector3f();
    final Vector3f max = new Vector3f();
    @Override
    public void dispose() {
        GLES.glDeleteBuffers(mLandVB);
        GLES.glDeleteBuffers(mLandIB);
        GLES.glDeleteVertexArrays(mLandVBO);
    }

    @Override
    public void initlize(MeshParams params) {
        MeshData grid = new MeshData();
        Vector3f n = new Vector3f();

        GeometryGenerator geoGen = new GeometryGenerator();
        geoGen.createGrid(160.0f, 160.0f, 50, 50, grid);

        mLandIndexCount = grid.getIndiceCount();

        final float FLT_MAX = Float.MAX_VALUE;
        min.set(+FLT_MAX,+FLT_MAX,+FLT_MAX);
        max.set(-FLT_MAX,-FLT_MAX,-FLT_MAX);

        //
        // Extract the vertex elements we are interested and apply the height function to
        // each vertex.
        //
        FloatBuffer vertices = BufferUtils.createFloatBuffer(8 * grid.getVertexCount());
        for(int i = 0; i < grid.getVertexCount(); i++){
            Vertex p = grid.vertices.get(i);

            p.positionY = getHillHeight(p.positionX, p.positionZ);
            vertices.put(p.positionX).put(p.positionY).put(p.positionZ);

            getHillNormal(p.positionX, p.positionZ, n);
            vertices.put(n.x).put(n.y).put(n.z);

            vertices.put(p.texCX).put(p.texCY);

            min.set(Math.min(min.x, p.positionX), Math.min(min.y, p.positionY), Math.min(min.z, p.positionZ));
            max.set(Math.max(max.x, p.positionX), Math.max(max.y, p.positionY), Math.max(max.z, p.positionZ));
        }

        vertices.flip();

        mLandVB = GLES.glGenBuffers();
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mLandVB);
        GLES.glBufferData(GLES30.GL_ARRAY_BUFFER, vertices,  GLES30.GL_STATIC_DRAW);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);

        mLandIB = GLES.glGenBuffers();
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, mLandIB);
        GLES.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER, GLUtil.wrap(grid.indices.getData(), 0, grid.getIndiceCount()), GLES30.GL_STATIC_DRAW);
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0);

        final int mfxPosition = params.posAttribLoc;
        final int mfxNormal = params.norAttribLoc;
        final int mfxTex = params.texAttribLoc;
        mLandVBO = GLES.glGenVertexArrays();
        GLES30.glBindVertexArray(mLandVBO);
        {
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mLandVB);
            GLES30.glVertexAttribPointer(mfxPosition, 3, GL11.GL_FLOAT, false, 8 * 4, 0);
            GLES30.glVertexAttribPointer(mfxNormal, 3, GL11.GL_FLOAT, false, 8 * 4, 3 * 4);
            GLES30.glVertexAttribPointer(mfxTex, 2, GL11.GL_FLOAT, false, 8 * 4, 6 * 4);

            // Notice, must enable the attribute location in VBO creation block.
            GLES30.glEnableVertexAttribArray(mfxPosition);
            GLES30.glEnableVertexAttribArray(mfxNormal);
            GLES30.glEnableVertexAttribArray(mfxTex);

            GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, mLandIB);
        }
        GLES30.glBindVertexArray(0);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public ReadableVector3f getMin() { return min;}
    public ReadableVector3f getMax() { return max;}

    @Override
    public void draw() {
        GLES30.glBindVertexArray(mLandVBO);
        GLES30.glDrawElements(GL11.GL_TRIANGLES, mLandIndexCount, GL11.GL_UNSIGNED_SHORT, 0);
        GLES30.glBindVertexArray(0);
    }

    public void getHillNormal(float x, float z, Vector3f n){
        // n = (-df/dx, 1, -df/dz)
        n.set(
                -0.03f*z*(float)Math.cos(0.1f*x) - 0.3f*(float)Math.cos(0.1f*z),
                1.0f,
                -0.3f*(float)Math.sin(0.1f*x) + 0.03f*x*(float)Math.sin(0.1f*z));

        n.normalise();
    }

    public float getHillHeight(float x, float z){
        return 0.3f*( z* (float)Math.sin(0.1f*x) + x*(float)Math.cos(0.1f*z) );
    }
}
