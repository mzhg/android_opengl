package jet.learning.opengl.fight404;

import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLES31;

import com.nvidia.developer.opengl.utils.BufferUtils;
import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.NvDisposeable;

import java.nio.IntBuffer;
import java.util.Arrays;

/**
 * Created by mazhen'gui on 2017/12/21.
 */

final class BufferChain implements NvDisposeable{

    private int m_bufferFirstSlot;
    private int m_atomicFirstSlot;
    private int m_bufferResourceFirstSlot;
    private int m_CurrentMode;
    private final int[] stream_vaos;
    private final int[] stream_vbos;
    private final int[] strean_atomics;
    private final int[] buffer_sizes;

    private static final IntBuffer atomicinitData;
    static {
        atomicinitData = BufferUtils.createIntBuffer(4);
        atomicinitData.put(0).put(1).put(1).put(0).flip();
    }

    @Override
    public void dispose() {
        GLES30.glDeleteVertexArrays(stream_vaos.length, stream_vaos, 0);
        GLES30.glDeleteBuffers(stream_vbos.length, stream_vbos, 0);
        GLES30.glDeleteBuffers(strean_atomics.length, strean_atomics, 0);
    }

    public BufferChain(int bufferSize, Runnable vertex_binding){
        this(new int[]{bufferSize}, new Runnable[]{vertex_binding});
    }

    public BufferChain(int[] bufferSize, Runnable[] vertex_binding) {
        buffer_sizes = Arrays.copyOf(bufferSize, bufferSize.length);
        stream_vaos = new int[bufferSize.length];
        stream_vbos = new int[bufferSize.length];
        strean_atomics = new int[bufferSize.length];

        // make sure there is no VAO binding.
        GLES30.glBindVertexArray(0);
        for(int i = 0; i < bufferSize.length; i++){
            int vbo = GLES.glGenBuffers();
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, bufferSize[i], null, GLES20.GL_STREAM_DRAW);

            int vao = GLES.glGenVertexArray();
            GLES30.glBindVertexArray(vao);
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo);
            vertex_binding[i].run();
            GLES30.glBindVertexArray(0);
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

            int atomic = GLES.glGenBuffers();
            GLES20.glBindBuffer(GLES31.GL_ATOMIC_COUNTER_BUFFER, atomic);
            GLES.glBufferData(GLES31.GL_ATOMIC_COUNTER_BUFFER, atomicinitData, GLES20.GL_STREAM_DRAW);
            GLES20.glBindBuffer(GLES31.GL_ATOMIC_COUNTER_BUFFER, 0);

            stream_vbos[i] = vbo;
            stream_vaos[i] = vao;
            strean_atomics[i] = atomic;
        }
    }

    public void beginRecord(int primitveType, int buffer_slot, int atomic_slot){
        m_bufferFirstSlot = buffer_slot;
        m_atomicFirstSlot = atomic_slot;
        for(int i = 0; i < stream_vbos.length; i++){
            GLES30.glBindBufferBase(GLES31.GL_SHADER_STORAGE_BUFFER, buffer_slot + i, stream_vbos[i]);
            GLES30.glBindBufferBase(GLES31.GL_ATOMIC_COUNTER_BUFFER, atomic_slot + i, strean_atomics[i]);
            GLES20.glBufferSubData(GLES31.GL_ATOMIC_COUNTER_BUFFER,0, 16, atomicinitData);  // clear the atomic buffer
        }
        m_CurrentMode = primitveType;
    }

    public void bindResource(int slot){
        m_bufferResourceFirstSlot = slot;
        for(int i = 0; i < stream_vbos.length; i++){
            GLES30.glBindBufferBase(GLES31.GL_SHADER_STORAGE_BUFFER, slot + i, stream_vbos[i]);
        }
    }

    public void unbind(){
        for(int i = 0; i < stream_vbos.length; i++){
            GLES30.glBindBufferBase(GLES31.GL_SHADER_STORAGE_BUFFER, m_bufferResourceFirstSlot + i, 0);
        }
    }

    public int getBufferSize(int index){ return buffer_sizes[index];}

    public void endRecord(){
        /*for(int i = 0; i < stream_vbos.length; i++){
            GLES30.glBindBufferBase(GLES31.GL_SHADER_STORAGE_BUFFER, m_bufferFirstSlot + i, 0);
            GLES30.glBindBufferBase(GLES31.GL_ATOMIC_COUNTER_BUFFER, m_atomicFirstSlot + i, 0);
        }*/
    }

    public int getVBO(int index)  { return stream_vbos[index];}
    public int getAtomicBuffer(int index){
        return strean_atomics[index];
    }

    public void drawArrays(int index, int primive_count){
        GLES30.glBindVertexArray(stream_vaos[index]);
        GLES30.glDrawArrays(m_CurrentMode, 0, primive_count);
        GLES30.glBindVertexArray(0);
    }

    public void drawIndirectArrays(int index){
        GLES30.glBindVertexArray(stream_vaos[index]);
        GLES31.glDrawArraysIndirect(m_CurrentMode, 0);
        GLES30.glBindVertexArray(0);
    }
}
