package jet.learning.opengl.optimization;

import com.nvidia.developer.opengl.utils.ImprovedNoise;
import com.nvidia.developer.opengl.utils.RadixSort;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

/**
 * Created by mazhen'gui on 2018/2/6.
 */

final class ParticleSystem {
    ParticleInitializer m_pInit;

    public ParticleSystem() {
        m_pInit = new ParticleInitializer();
    }

    int getNumActive(){ return m_pInit.getNumActive(); }

    void simulate(float frameElapsed, Vector3f halfVector, Vector4f eyePos)
    {
        m_pInit.setTime(frameElapsed);
        m_pInit.simulate();

        m_pInit.depthSortEfficient(halfVector);
    }

    Vector4f[] getPositions(){  return m_pInit.m_pos;}

    short[] getSortedIndices(){ return m_pInit.m_sortedIndices16;}

    static class ParticleInitializer{
        final float m_width = 480;

        Vector4f[] m_pos; // could be replaced by the floating-point array.
        float[] m_zs;
        short[] m_sortedIndices16;
        int m_count;
        final RadixSort m_sorter = new RadixSort();

        int m_numActive;
        //		    r3::Condition m_timeCondition;
//		    r3::Condition m_updateGLCondition;
        final ImprovedNoise m_noise = new ImprovedNoise();
        float m_elapsedTime;

        public ParticleInitializer() {
            final int N = OptimizationApp.GRID_RESOLUTION;
            m_count = N * N;

            m_pos = new Vector4f[m_count];
            m_zs = new float [m_count];
            m_sortedIndices16 = new short [m_count];

            initGrid(OptimizationApp.GRID_RESOLUTION);
            addNoise(0.01f, 70.0f);
        }

        int getNumActive() {return m_numActive; }

        void setTime(float frameElapsed)
        {
            //m_timeCondition.Acquire();
            m_elapsedTime += frameElapsed;
            //m_timeCondition.Signal();
            //m_timeCondition.Release();
        }

        void simulate()
        {
            //m_timeCondition.Acquire();
            //m_timeCondition.Wait();
            final float dt = m_elapsedTime;
            //m_timeCondition.Release();

            Vector4f wind = new Vector4f(4.7f, 0.0f, 3.1f, 0.0f);
            wind.scale(dt);
            for (int i = 0; i < getNumActive(); i++)
            {
//		            m_pos[i] += dt * wind;
                Vector4f.add(m_pos[i], wind, m_pos[i]);
                wrapPos(m_width, m_pos[i]);
            }

            //m_timeCondition.Acquire();
//		        m_elapsedTime -= dt;
            m_elapsedTime = 0;
            //m_timeCondition.Release();
        }

        void depthSortEfficient(Vector3f halfVector)
        {
            // calculate eye-space z
            for (int i = 0; i < getNumActive(); ++i)
            {
                Vector4f pos = m_pos[i];
//		            float z = -dot(halfVector, truncate(m_pos[i]));  // project onto vector
                float z = -(halfVector.x * pos.x + halfVector.y * pos.y + halfVector.z * pos.z);
                m_zs[i] = z;
            }

            // sort
            m_sorter.sort(m_zs, getNumActive());

            int[] sortedIndices32 = m_sorter.getIndices();
            for (int i = 0; i < getNumActive(); ++i)
            {
                m_sortedIndices16[i] = (short) sortedIndices32[i];
            }
        }

        // initialize particles in regular grid.  Single threaded.
        void initGrid(int N)
        {
            int i = 0;
            Vector3f p = new Vector3f();
            Vector3f coords = new Vector3f();
            for (int z=0; z < N; z++)
            {
                for (int x=0; x < N; x++)
                {
//		                vec3f p = vec3f(float(x), 0, float(z)) / vec3f(float(N), float(N), float(N));
                    p.set((float)x/N, 0, (float)z/N);
//		                p = (p * 2.0f - 1.0f) * m_width;
                    p.x = (p.x * 2.0f - 1.0f) * m_width;
                    p.z = (p.z * 2.0f - 1.0f) * m_width;
                    p.y = -1.0f;        // -2

                    coords.set(p).scale(0.007f);
//		                const float noise = 0.7f + fabs(m_noise.fBm(coords * 0.007)) * 2;
                    final float noise = 0.7f + Math.abs(m_noise.fBm(coords)) * 2.0f;
                    m_pos[i] = new Vector4f(p.x, p.y, p.z, noise);
                    i++;
                }
            }

            m_numActive = i;
            assert(m_numActive == N * N);
        }

        // Single threaded.
        void addNoise(float freq, float scale)
        {
            Vector3f coords = new Vector3f();
            for (int i = 0; i < m_numActive; i++)
            {
//		            const vec3f coords(truncate(m_pos[i]));
                coords.set(m_pos[i]).scale(freq);
//		            const vec3f noise = m_noise.fBm3f(coords * freq) * scale;
                Vector3f noise = m_noise.fBm3f(coords);
                noise.scale(scale);
                m_pos[i].x += noise.x;
                m_pos[i].y += noise.y;
                m_pos[i].z += noise.z;
            }
        }
    }

    static void wrapPos(float width, Vector4f pos)
    {
        if (pos.x < -width)
            pos.x += 2*width;
        if (pos.x > width)
            pos.x -= 2*width;

        if (pos.z < -width)
            pos.z += 2*width;
        if (pos.z > width)
            pos.z -= 2*width;
    }
}
