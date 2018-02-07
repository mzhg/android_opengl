package jet.learning.opengl.optimization;

import com.nvidia.developer.opengl.utils.NvGLModel;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;

/**
 * Created by mazhen'gui on 2018/2/6.
 */

final class MeshObj implements Comparable<MeshObj>{
    final Matrix4f m_modelMatrix = new Matrix4f();
    final Vector4f m_color = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);

    float        m_specularValue = 1.0f;
    int       m_texId;
    boolean         m_cullFacing = true;
    boolean           m_alphaTest;

    NvGLModel m_pModelData;

    @Override
    public int compareTo(MeshObj o) {
        int alphaTest1 = m_alphaTest ? 1 : 0;
        int alphaTest2 = o.m_alphaTest ? 1 : 0;
        return alphaTest1 - alphaTest2;
    }
}
