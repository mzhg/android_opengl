package jet.learning.opengl.fight404;

import android.opengl.GLES20;

import com.nvidia.developer.opengl.app.NvSampleApp;
import com.nvidia.developer.opengl.utils.NvUtils;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

/**
 * Created by mazhen'gui on 2017/12/20.
 */

public final class Fireworks extends NvSampleApp {
    static final int MAX_PARTICLE_COUNT = 100_000;
    static final int MAX_EMITTER_COUNT = 16;
    static final int MAX_PARTICLE_TAIL_COUNT = 4;

    private static final int TYPE_BORN = 0;      // particles born
    private static final int TYPE_UPDATE = 1;    // update the particles
    private static final int TYPE_NEBULA = 2;    // update the nebulas
    private static final int TYPE_NEBORN = 3;    // born the emitter nebulas

    private static final int PARTICLE_SIZE = 48;

    private static final int PAR_LOC_OFFSET = 0;
    private static final int PAR_VEL_OFFSET = 12;
    private static final int PAR_RADIUS_OFFSET = 24;
    private static final int PAR_AGE_OFFSET = 28;
    private static final int PAR_LIFE_SPAN_OFFSET = 32;
    private static final int PAR_GEN_OFFSET = 36;
    private static final int PAR_BOUNCE_AGE_OFFSET = 40;
    private static final int PAR_TYPE_OFFSET = 44;

    private Textures textures;

    private final Vector3f gravity = new Vector3f();
    private float floorLevel;

    private int counter;
    private int saveCount;

    private boolean allowNebula;
    private boolean allowGravity = true;
    private boolean allowPerlin;
    private boolean allowTrails;
    private boolean allowFloor = true;
    private boolean addParticles = true;

    private float minNoise = 0.499f;
    private float maxNoise = 0.501f;

    private final Vector3f cursor = new Vector3f();

    private final Matrix4f mProj = new Matrix4f();
    private final Matrix4f mView = new Matrix4f();

    @Override
    protected void initRendering() {
        float mTheta = PI/4;
        float mPhi = PI/3;
        float mRadius = (float)Math.sqrt(100 * 100 + 1500 * 1500);
        float x = (float) (mRadius*Math.sin(mPhi)*Math.cos(mTheta));
        float z = (float) (mRadius*Math.sin(mPhi)*Math.sin(mTheta));
        float y = (float) (mRadius*Math.cos(mPhi));

        initCamera(0, new Vector3f(-x,y,-z), Vector3f.ZERO);

    }

    @Override
    protected void reshape(int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        Matrix4f.perspective((float)Math.toDegrees(NvUtils.PI*2/3), (float)width/height, 0.1f, 1000f, mProj);
    }
}
