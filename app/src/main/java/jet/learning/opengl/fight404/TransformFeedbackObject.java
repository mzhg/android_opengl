package jet.learning.opengl.fight404;

import android.opengl.GLES20;
import android.opengl.GLES30;

import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.NvDisposeable;

/**
 * Created by mazhen'gui on 2017/7/3.
 */

public final class TransformFeedbackObject implements NvDisposeable{
    private int transformFeedback;
//    private int primitve_count;

    private int m_CurrentMode;
    private int stream_vaos;
    private int stream_vbos;
    private int stream_quey;
    private final int buffer_sizes;
    private int stream_count;

    @Override
    public void dispose() {
        GLES.glDeleteTransformFeedback(transformFeedback);
        GLES.glDeleteQueries(stream_quey);
        GLES.glDeleteVertexArrays(stream_vaos);
        GLES.glDeleteBuffers(stream_vbos);
    }

    public TransformFeedbackObject(int bufferSize, Runnable vertex_binding) {
        buffer_sizes = bufferSize;

        // make sure there is no VAO binding.
        GLES30.glBindVertexArray(0);
        int vbo = GLES.glGenBuffers();
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, bufferSize, null, GLES20.GL_STREAM_DRAW);

        int vao = GLES.glGenVertexArray();
        GLES30.glBindVertexArray(vao);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo);
        vertex_binding.run();
        GLES30.glBindVertexArray(0);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);

        stream_vbos = vbo;
        stream_vaos = vao;

        stream_quey = GLES.glGenQueries();

        transformFeedback = GLES.glGenTransformFeedbacks();
        GLES30.glBindTransformFeedback(GLES30.GL_TRANSFORM_FEEDBACK, transformFeedback);
        GLES30.glBindBufferBase(GLES30.GL_TRANSFORM_FEEDBACK_BUFFER, 0, stream_vbos);
        GLES30.glBindTransformFeedback(GLES30.GL_TRANSFORM_FEEDBACK, 0);
    }

    public void beginRecord(int primitveType){
        GLES20.glEnable(GLES30.GL_RASTERIZER_DISCARD);
        GLES30.glBindTransformFeedback(GLES30.GL_TRANSFORM_FEEDBACK, transformFeedback);
        GLES30.glBeginQuery(GLES30.GL_TRANSFORM_FEEDBACK_PRIMITIVES_WRITTEN, stream_quey);
        GLES30.glBeginTransformFeedback(primitveType);
        m_CurrentMode = primitveType;
        stream_count = -1;
    }

    public int getBufferSize(){ return buffer_sizes;}

    public void endRecord(){
        GLES30.glEndTransformFeedback();
        GLES30.glEndQuery(GLES30.GL_TRANSFORM_FEEDBACK_PRIMITIVES_WRITTEN);

        GLES30.glBindTransformFeedback(GLES30.GL_TRANSFORM_FEEDBACK, 0);
        GLES30.glDisable(GLES30.GL_RASTERIZER_DISCARD);

        stream_count = GLES.glGetQueryObjectuiv(stream_quey, GLES30.GL_QUERY_RESULT);
    }

    public int getPrimitiveCount(){
//        return gl.glGetQueryObjectuiv(stream_quey[index], GLenum.GL_QUERY_RESULT);
        return stream_count;
    }

//		public void bindVAO(){	GL30.glBindVertexArray(vao);}

    public void drawStream(){
        if(stream_count == -1){
            throw new IllegalStateException("invalid stream_count: -1");
        }

        GLES30.glBindVertexArray(stream_vaos);
//        GLES30.glDrawTransformFeedbackStream(m_CurrentMode, transformFeedback, index);
        GLES20.glDrawArrays(m_CurrentMode, 0, stream_count);
        GLES30.glBindVertexArray(0);
    }

    public void drawArrays( int primive_count){
        GLES30.glBindVertexArray(stream_vaos);
        GLES20.glDrawArrays(m_CurrentMode, 0, primive_count);
        GLES30.glBindVertexArray(0);
    }


}
