package jet.learning.opengl.fight404;

import android.opengl.GLES20;
import android.opengl.GLES30;

import com.nvidia.developer.opengl.app.NvSampleApp;
import com.nvidia.developer.opengl.utils.BufferUtils;
import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.NvAssetLoader;
import com.nvidia.developer.opengl.utils.NvGLSLProgram;
import com.nvidia.developer.opengl.utils.NvUtils;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import java.nio.ByteBuffer;

import javax.microedition.khronos.opengles.GL11;

/**
 * Created by mazhen'gui on 2017/12/20.
 */
public final class Fireworks extends NvSampleApp {
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

    private int mBlockBuffer;
    final BlockData mBlockData = new BlockData();
    private final ByteBuffer mBlockMemory = BufferUtils.createByteBuffer(Math.max(BlockData.SIZE, RenderFrame.SIZE));

    private int mFrameBuffer;
    private final RenderFrame mRenderFrame = new RenderFrame();

    private int mRandomTex2D;

    @Override
    protected void initRendering() {
        float mTheta = PI/4;
        float mPhi = PI/3;
        float mRadius = (float)Math.sqrt(100 * 100 + 1500 * 1500);
        float x = (float) (mRadius*Math.sin(mPhi)*Math.cos(mTheta));
        float z = (float) (mRadius*Math.sin(mPhi)*Math.sin(mTheta));
        float y = (float) (mRadius*Math.cos(mPhi));

        initCamera(0, new Vector3f(-x,y,-z), Vector3f.ZERO);

        mBlockBuffer = GLES.glGenBuffers();
        GLES20.glBindBuffer(GLES30.GL_UNIFORM_BUFFER, mBlockBuffer);
        GLES20.glBufferData(GLES30.GL_UNIFORM_BUFFER, BlockData.SIZE, null, GLES30.GL_DYNAMIC_DRAW);
        GLES20.glBindBuffer(GLES30.GL_UNIFORM_BUFFER, 0);

        mFrameBuffer = GLES.glGenBuffers();
        GLES20.glBindBuffer(GLES30.GL_UNIFORM_BUFFER, mFrameBuffer);
        GLES20.glBufferData(GLES30.GL_UNIFORM_BUFFER, RenderFrame.SIZE, null, GLES30.GL_DYNAMIC_DRAW);
        GLES20.glBindBuffer(GLES30.GL_UNIFORM_BUFFER, 0);

        mRandomTex2D = createRandomTexture1D(256);
    }

    private void updateCamera(){
        m_transformer.getModelViewMat(mRenderFrame.view);
    }

    @Override
    protected void draw() {
        updateCamera();
    }

    @Override
    protected void reshape(int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        Matrix4f.perspective((float)Math.toDegrees(NvUtils.PI*2/3), (float)width/height, 0.1f, 1000f, mRenderFrame.projection);
    }

    void updateRenderFrame(int index){
        GLES30.glBindBufferBase(GLES30.GL_UNIFORM_BUFFER, index, mFrameBuffer);
        mBlockMemory.clear();
        mRenderFrame.store(mBlockMemory);
        mBlockMemory.flip();
        GLES20.glBufferSubData(GLES30.GL_UNIFORM_BUFFER, 0, mBlockMemory.remaining(), mBlockMemory);
    }

    void updateBlockdataAndBind(int index){
        GLES30.glBindBufferBase(GLES30.GL_UNIFORM_BUFFER, index, mBlockBuffer);
        mBlockMemory.clear();
        mBlockData.store(mBlockMemory);
        mBlockMemory.flip();
        GLES20.glBufferSubData(GLES30.GL_UNIFORM_BUFFER, 0, mBlockMemory.remaining(), mBlockMemory);
    }

    void bindRandomTex(){
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mRandomTex2D);
    }

    static NvGLSLProgram createShader(String filename, int shaderType){
        NvGLSLProgram program = new NvGLSLProgram();
        CharSequence source = NvAssetLoader.readText(filename);

        NvGLSLProgram.ShaderSourceItem item = new NvGLSLProgram.ShaderSourceItem(source, shaderType);
        program.setSourceFromStrings(item);

        return program;
    }

    static int createRandomTexture1D(int width){
        int internalFormat = GLES30.GL_RGB8;

        ByteBuffer buf = BufferUtils.createByteBuffer(width * 3);
        for(int i = 0; i < width; i++){
            for(int j = 0;j < 3; j++)
                buf.put((byte)(Math.random() * 255));
        }

        buf.flip();

        int texture = GLES.glGenTextures();
        GLES20.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        GLES20.glTexImage2D(GL11.GL_TEXTURE_2D, 0, internalFormat, width, 1,0, GLES20.GL_RGB, GL11.GL_UNSIGNED_BYTE, buf);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP_TO_EDGE);
        GLES20.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        return texture;
    }
}
