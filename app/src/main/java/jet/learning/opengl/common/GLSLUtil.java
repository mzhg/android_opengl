package jet.learning.opengl.common;

import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLES31;

import com.nvidia.developer.opengl.utils.GLUtil;
import com.nvidia.developer.opengl.utils.NvGLSLProgram;

import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.ReadableVector3f;
import org.lwjgl.util.vector.ReadableVector4f;
import org.lwjgl.util.vector.Vector2i;
import org.lwjgl.util.vector.Vector4i;

public final  class GLSLUtil {

    public static void setBool(NvGLSLProgram prog, String name, boolean v){
        int index = prog.getUniformLocation(name, true);
        if(index >=0)
            GLES20.glUniform1i(index, v?1:0);
    }

    public static void setFloat(NvGLSLProgram prog, String name, float v){
        int index = prog.getUniformLocation(name, true);
        if(index >=0)
            GLES20.glUniform1f(index, v);
    }

    public static void setFloat2(NvGLSLProgram prog, String name, float x, float y){
        int index = prog.getUniformLocation(name, true);
        if(index >=0)
            GLES20.glUniform2f(index, x, y);
    }

    public static void setFloat3(NvGLSLProgram prog, String name, float x, float y, float z){
        int index = prog.getUniformLocation(name, true);
        if(index >=0)
            GLES20.glUniform3f(index, x,y,z);
    }

    public static void setFloat4(NvGLSLProgram prog, String name, float x, float y, float z, float w){
        int index = prog.getUniformLocation(name, true);
        if(index >=0)
            GLES20.glUniform4f(index, x,y,z, w);
    }

    public static void setMat4(NvGLSLProgram prog, String name, Matrix4f v){
        int index = prog.getUniformLocation(name, true);
        if(index >=0)
            GLES20.glUniformMatrix4fv(index, 1, false, GLUtil.wrap(v));
    }

    public static void setFloat2(NvGLSLProgram prog, String name, ReadableVector2f v){
        int index = prog.getUniformLocation(name, true);
        if(index >=0)
            GLES20.glUniform2f(index, v.getX(), v.getY());
    }

    public static void setFloat3(NvGLSLProgram prog, String name, ReadableVector3f v){
        int index = prog.getUniformLocation(name, true);
        if(index >=0)
            GLES20.glUniform3f(index, v.getX(), v.getY(), v.getZ());
    }

    public static void setFloat4(NvGLSLProgram prog, String name, ReadableVector4f v){
        int index = prog.getUniformLocation(name, true);
        if(index >=0)
            GLES20.glUniform4f(index, v.getX(),v.getY(),v.getZ(), v.getW());
    }

    public static void setFloat4(NvGLSLProgram prog, String name, ReadableVector4f[] v){
        int index = prog.getUniformLocation(name, true);
        if(index >=0){
            GLES20.glUniform4fv(index, v.length, GLUtil.wrap(v));
        }
    }

    public static void setInt(NvGLSLProgram prog, String name, int v){
        int index = prog.getUniformLocation(name, true);
        if(index >=0)
            GLES20.glUniform1i(index, v);
    }

    public static void setInt2(NvGLSLProgram prog, String name, int x, int y){
        int index = prog.getUniformLocation(name, true);
        if(index >=0)
            GLES20.glUniform2i(index, x, y);
    }

    public static void setInt2(NvGLSLProgram prog, String name, Vector2i v){
        int index = prog.getUniformLocation(name, true);
        if(index >=0)
            GLES20.glUniform2i(index, v.x, v.y);
    }

    public static void setInt4(NvGLSLProgram prog, String name, int x, int y, int z, int w){
        int index = prog.getUniformLocation(name, true);
        if(index >=0)
            GLES20.glUniform4i(index, x,y,z,w);
    }

    public static void setInt4(NvGLSLProgram prog, String name, Vector4i iv){
        int index = prog.getUniformLocation(name, true);
        if(index >=0)
            GLES20.glUniform4i(index, iv.x,iv.y,iv.z,iv.w);
    }

    public static void setUInt(NvGLSLProgram prog, String name, int v){
        int index = prog.getUniformLocation(name, true);
        if(index >=0)
            GLES30.glUniform1ui(index, v);
    }

    public static String getShaderTypeName(int shader){
        switch (shader){
            case GLES30.GL_VERTEX_SHADER: return "Vertex";
            case GLES30.GL_FRAGMENT_SHADER: return "Fragment";
            case GLES31.GL_COMPUTE_SHADER: return "Compute";
//            case GLES32.GL_TESS_EVALUATION_SHADER: return "Tess_Evaluation";
//            case GLES32.GL_TESS_CONTROL_SHADER: return "Tess_Control";
//            case GLES32.GL_GEOMETRY_SHADER: return "Geometry";
        }

        throw new IllegalArgumentException("Invalid shader type: " + Integer.toHexString(shader));
    }
}
