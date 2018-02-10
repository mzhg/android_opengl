package jet.learning.opengl.upsampling;

import com.nvidia.developer.opengl.utils.ImprovedNoise;
import com.nvidia.developer.opengl.utils.RadixSort;
import com.nvidia.developer.opengl.utils.VectorUtil;

import org.lwjgl.util.vector.Vector3f;

/**
 * Created by mazhen'gui on 2018/2/10.
 */

final class ParticleSystem {
    Vector3f[] m_pos;
    float[]    m_zs;
    short[]    m_sortedIndices16;

    int m_count;
    int m_numActive;

    final ImprovedNoise m_noise = new ImprovedNoise();
    final RadixSort m_sorter = new RadixSort();

    public ParticleSystem() {
        final int N = ParticleUpsampling.GRID_RESOLUTION;
        m_count = N * N * N;

        m_pos = new Vector3f[m_count];
        m_zs = new float [m_count];
        m_sortedIndices16 = new short[m_count];

        initGrid(N);
        addNoise(1.9f, 1.0f);
    }

    // initialize particles in regular grid
    void initGrid(int N)
    {
        float r = 1.f;
        int i = 0;
        for (int z = 0; z < N; z++)
        {
            for (int y=0; y < N; y++)
            {
                for (int x=0; x < N; x++)
                {
//		                nv::vec3f p = nv::vec3f((float)x, (float)y, (float)z) / nv::vec3f((float)N, (float)N, (float)N);
                    Vector3f p = new Vector3f((float)x/(float)N,(float)y/(float)N,(float)z/(float)N);
                    p.x = (p.x * 2.0f - 1.0f) * r;
                    p.y = (p.y * 2.0f - 1.0f) * r;
                    p.z = (p.z * 2.0f - 1.0f) * r;
//		                p = (p * 2.0f - 1.0f) * r;
                    if (i < m_count)
                    {
                        m_pos[i] = p;
                        i++;
                    }
                }
            }
        }
        m_numActive = i;
    }

    void addNoise(float freq, float scale)
    {
        Vector3f v = new Vector3f();
        for (int i = 0; i < m_count; i++)
        {
            v.set(m_pos[i]).scale(freq);
//		        m_pos[i] += m_noise.fBm3f(v) * scale;
            VectorUtil.linear(m_pos[i], m_noise.fBm3f(v), scale, m_pos[i]);
        }
    }

    void depthSort(Vector3f halfVector)
    {
        // calculate eye-space z
        for (int i = 0; i < m_numActive; ++i)
        {
            float z = -Vector3f.dot(halfVector, m_pos[i]);  // project onto vector
            m_zs[i] = z;
        }

        // sort
        m_sorter.sort(m_zs, m_numActive);

        int[] sortedIndices32 = m_sorter.getIndices();
        for (int i = 0; i < m_numActive; ++i)
        {
            m_sortedIndices16[i] = (short) sortedIndices32[i];
        }
    }

    Vector3f[] getPositions()
    {
        return m_pos;
    }

    short[] getSortedIndices()
    {
        return m_sortedIndices16;
    }

    int getNumActive()
    {
        return m_numActive;
    }
}
