package jet.learning.opengl.common;

import android.opengl.GLES20;
import android.opengl.GLES30;

import com.nvidia.developer.opengl.utils.BufferUtils;
import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.GLUtil;
import com.nvidia.developer.opengl.utils.NvUtils;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL11;

/**
 * Created by mazhen'gui on 2017/11/13.
 */

public class WaveMesh implements RenderMesh {
    Waves mWaves;

    int mWavesVB;
    int mWavesIB;
    int mWavesVBO;

    float m_totalTime;
    float t_base;

    FloatBuffer mMapBuffer;
    final Vector2f mWaterTexOffset = new Vector2f();
    final Matrix4f mWaterTexTransform = new Matrix4f();

    public WaveMesh(Waves waves){
        mWaves = waves;
    }

    @Override
    public void dispose() {
        GLES.glDeleteBuffers(mWavesVB);
        GLES.glDeleteBuffers(mWavesIB);
        GLES.glDeleteVertexArrays(mWavesVBO);
    }

    @Override
    public void initlize(MeshParams params) {
// Create the vertex buffer.  Note that we allocate space only, as
        // we will be updating the data every time step of the simulation.
        mWavesVB = GLES.glGenBuffers();
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mWavesVB);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, 8 * mWaves.vertexCount() * 4, null, GLES20.GL_DYNAMIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        mMapBuffer = BufferUtils.createFloatBuffer(8 * mWaves.vertexCount());

        // Create the index buffer.  The index buffer is fixed, so we only
        // need to create and set once.
        int[] indices = new int[3 * mWaves.triangleCount()];

        // Iterate over each quad.
        int m = mWaves.rowCount();
        int n = mWaves.columnCount();
        int k = 0;
        for(int i = 0; i < m-1; ++i)
        {
            for(int j = 0; j < n-1; ++j)
            {
                indices[k]   = (i*n+j);
                indices[k+1] = (i*n+j+1);
                indices[k+2] = ((i+1)*n+j);

                indices[k+3] = ((i+1)*n+j);
                indices[k+4] = (i*n+j+1);
                indices[k+5] = ((i+1)*n+j+1);

                k += 6; // next quad
            }
        }

        mWavesIB = GLES.glGenBuffers();
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mWavesIB);
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indices.length * 4, GLUtil.wrap(indices), GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

        final int mfxPosition = params.posAttribLoc;
        final int mfxNormal = params.norAttribLoc;
        final int mfxTex = params.texAttribLoc;

        mWavesVBO = GLES.glGenVertexArrays();
        GLES30.glBindVertexArray(mWavesVBO);
        {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mWavesVB);
            GLES20.glVertexAttribPointer(mfxPosition, 3, GL11.GL_FLOAT, false, 8 * 4, 0);
            GLES20.glVertexAttribPointer(mfxNormal, 3, GL11.GL_FLOAT, false, 8 * 4, 3 * 4);
            GLES20.glVertexAttribPointer(mfxTex, 2, GL11.GL_FLOAT, false, 8 * 4, 6 * 4);

            // Notice, must enable the attribute location in VBO creation block.
            GLES20.glEnableVertexAttribArray(mfxPosition);
            GLES20.glEnableVertexAttribArray(mfxNormal);
            GLES20.glEnableVertexAttribArray(mfxTex);

            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mWavesIB);
        }
        GLES30.glBindVertexArray(0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    @Override
    public void update(float dt) {
        //
        // Every quarter second, generate a random wave.
        //
        if( (m_totalTime - t_base) >= 0.25f )
        {
            t_base += 0.25f;

            int i = 5 + NvUtils.rand() % (mWaves.rowCount()-10);
            int j = 5 + NvUtils.rand() % (mWaves.columnCount()-10);

            float r = NvUtils.random(1.0f, 2.0f);

            mWaves.disturb(i, j, r);
        }

        mWaves.update(dt);


//        mMapBuffer = (ByteBuffer) GLES30.glMapBufferRange(GLES20.GL_ARRAY_BUFFER, 0, 8 * mWaves.vertexCount() * 4, GLES30.GL_MAP_WRITE_BIT|GLES30.GL_MAP_INVALIDATE_BUFFER_BIT);
//		System.out.println("mapbuffer remain = " + mMapBuffer.remaining() + ",  position = " + mMapBuffer.position());
        mMapBuffer.clear();
        Vector3f pos;
        for(int i = 0; i < mWaves.vertexCount(); ++i)
        {
            pos = mWaves.get(i);
            Vector3f nor = mWaves.normal(i);
            mMapBuffer.put(pos.x).put(pos.y).put(pos.z);
            mMapBuffer.put(nor.x).put(nor.y).put(nor.z);

            // Derive tex-coords in [0,1] from position.
            mMapBuffer.put(0.5f + pos.x / mWaves.width());
            mMapBuffer.put(0.5f - pos.z / mWaves.depth());
        }
//        GLES30.glUnmapBuffer(GLES20.GL_ARRAY_BUFFER);
        mMapBuffer.flip();
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mWavesVB);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, mMapBuffer.remaining() * 4, mMapBuffer);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        // Translate texture over time.
        mWaterTexOffset.y += 0.05f*dt;
        mWaterTexOffset.x += 0.1f*dt;
        mWaterTexTransform.setIdentity();
        mWaterTexTransform.translate(mWaterTexOffset);
        mWaterTexTransform.scale(5.0f, 5.0f, 1.0f);

        m_totalTime+=dt;
    }

    @Override
    public void draw() {
        GLES30.glBindVertexArray(mWavesVBO);
        GLES20.glDrawElements(GL11.GL_TRIANGLES, 3*mWaves.triangleCount(), GLES20.GL_UNSIGNED_INT, 0);
        GLES30.glBindVertexArray(0);
    }
}
