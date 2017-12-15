package jet.learning.opengl.d3dcoder;

import android.opengl.GLES20;
import android.opengl.GLES30;

import com.nvidia.developer.opengl.app.NvSampleApp;
import com.nvidia.developer.opengl.utils.BufferUtils;
import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.NvImage;
import com.nvidia.developer.opengl.utils.NvPackedColor;
import com.nvidia.developer.opengl.utils.NvUtils;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.ReadableVector3f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL11;

import jet.learning.opengl.common.BaseShadingProgram;
import jet.learning.opengl.common.BoxMesh;
import jet.learning.opengl.common.FrameData;
import jet.learning.opengl.common.GenerateShadowMapProgram;
import jet.learning.opengl.common.LandMesh;
import jet.learning.opengl.common.RenderMesh;
import jet.learning.opengl.common.WaterRenderProgram;
import jet.learning.opengl.water.WaterMesh;

/**
 * Created by mazhen'gui on 2017/12/14.
 */
public final class TreeBillboardApp extends NvSampleApp {
    private static final int TREE_COUNT = 16;

    // Meshes
    private BoxMesh mBoxMesh;
    private LandMesh mLandMesh;
    private WaterMesh mWaterMesh;

    // Programs
    private final BaseShadingProgram.ShadingParams mLightParams = new BaseShadingProgram.ShadingParams();
    private final FrameData mFrameData = new FrameData(1);
    private BaseShadingProgram mLightInstanceProgram;
    private GenerateShadowMapProgram mShadowmapProgram;
    private WaterRenderProgram mWaterRenderProgram;

    // Uniform buffers
    private int mFrameBuffer;
    private ByteBuffer mFrameMemory;

    private int mTreeSpritesVB;
    private int mTreeSpritesVBO;

    // Textures
    private int mGrassMapSRV;
    private int mWavesMapSRV;
    private int mBoxMapSRV;
    private int mTreeTextureMapArraySRV;

    // Internal variables
    private final Matrix4f mProj = new Matrix4f();
    private final Matrix4f mView = new Matrix4f();
    private final Matrix4f mProjView = new Matrix4f();

    private final Matrix4f mGrassTexTransform = new Matrix4f();
    private final Matrix4f mWaterTexTransform = new Matrix4f();
    private final Matrix4f mWavesWorld = new Matrix4f();
    private final Matrix4f mBoxWorld = new Matrix4f();
    private final Vector2f mWaterTexOffset = new Vector2f();

    @Override
    protected void initRendering() {
        mLightParams.lightAmbient.set(0.4f, 0.4f, 0.4f);
        mLightParams.lightDiffuse.set(0.8f, 0.8f, 0.7f);
        mLightParams.lightSpecular .set(0.8f, 0.8f, 0.7f);
        mLightParams.lightPos .set(0.57735f, 0.57735f, -0.57735f, 0);
        mLightParams.lightPos.normalise();
        mLightParams.color.set(0.f,0.f,0.f,0.0f);

        mLightParams.enableShadowMap = false;
        mLightParams.enableReflection = false;
        mLightParams.enableNormalMap = false;

        mBoxWorld.translate(8.0f, 5.0f, -15.0f);
        mBoxWorld.scale(15);

        mGrassTexTransform.scale(5.0f, 5.0f, 0.0f);

        buildFX();
        buildGeometryBuffers();
        loadTextures();

        ReadableVector3f color = NvPackedColor.SILVER;
        GLES20.glClearColor(color.getX(), color.getY(), color.getZ(), 1);
        GLES20.glClearDepthf(1.0f);

        float mTheta = 1.3f * PI;
        float mPhi = 0.4f * PI;
        float mRadius = 80.0f;
        float x = (float) (mRadius*Math.sin(mPhi)*Math.cos(mTheta));
        float z = (float) (mRadius*Math.sin(mPhi)*Math.sin(mTheta));
        float y = (float) (mRadius*Math.cos(mPhi));

        initCamera(0, new Vector3f(-x,y,-z), Vector3f.ZERO);
    }

    @Override
    protected void draw() {
        update(getFrameDeltaTime());

        m_transformer.getModelViewMat(mView);
        Matrix4f.mul(mProj, mView, mProjView);
        mView.invert();
        Matrix4f.transformVector(mView, Vector3f.ZERO, mLightParams.eyePos);

        drawScene();
    }

    private void update(float dt){
        // Translate texture over time.
        mWaterTexOffset.y += 0.05f*dt;
        mWaterTexOffset.x += 0.1f*dt;
        mWaterTexTransform.setIdentity();
        mWaterTexTransform.translate(mWaterTexOffset);
        mWaterTexTransform.scale(5.0f, 5.0f, 1.0f);

        mWaterMesh.update(dt);
    }

    private void drawScene(){
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glViewport(0,0,getWidth(), getHeight());
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthMask(true);

        mLightInstanceProgram.enable();
        mFrameData.viewProj.load(mProjView);

        { // Draw the box.
            mLightParams.enableNormalMap = false;
            mLightParams.enableShadowMap= false;
            mLightParams.enableReflection = false;
            mLightParams.alphaClip = true;

            GLES20.glBindTexture(GL11.GL_TEXTURE_2D, mBoxMapSRV);

            mFrameData.texMat.setIdentity();
            mFrameData.setInstanceCount(1);
            buildFrameInstance(0, mBoxWorld);
            setupBoxMat();
            updateAndBindFramebuffer();
            mLightInstanceProgram.setShadingParams(mLightParams);
            mBoxMesh.draw();

            GLES.checkGLError();
        }

        { // Draw the land
            mLightParams.enableNormalMap = false;
            mLightParams.enableShadowMap= false;
            mLightParams.enableReflection = false;
            mLightParams.alphaClip = false;

            GLES20.glBindTexture(GL11.GL_TEXTURE_2D, mGrassMapSRV);

            mFrameData.texMat.load(mGrassTexTransform);
            mFrameData.setInstanceCount(1);
            buildFrameInstance(0, Matrix4f.IDENTITY);
            setupLandMat();
            updateAndBindFramebuffer();
            mLightInstanceProgram.setShadingParams(mLightParams);
            mLandMesh.draw();

            GLES.checkGLError();
        }

        {// Draw the waves
            mLightParams.enableNormalMap = false;
            mLightParams.enableShadowMap= false;
            mLightParams.enableReflection = false;
            mLightParams.alphaClip = false;

            GLES20.glBindTexture(GL11.GL_TEXTURE_2D, mWavesMapSRV);
            GLES20.glEnable(GL11.GL_BLEND);
            GLES20.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mWaterMesh.getWaterNormalMap());
            GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mWaterMesh.getWaterHeightMap());

            mFrameData.texMat.load(mWaterTexTransform);
            mFrameData.setInstanceCount(1);
            buildFrameInstance(0, mWavesWorld);
            setupWaveMat();
            updateAndBindFramebuffer();
            mWaterRenderProgram.enable();
            mWaterRenderProgram.setLightParams(mLightParams);
            mWaterMesh.draw();

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
            GLES.checkGLError();
        }

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    @Override
    protected void reshape(int width, int height) {
        Matrix4f.perspective((float)Math.toDegrees(0.25 * PI), (float)width/height, 1.0f, 1000.0f, mProj);
        GLES20.glViewport(0,0, width, height);
    }

    private void buildFrameInstance(int idx, Matrix4f world){
        mFrameData.models[idx].load(world);
        Matrix4f normal = mFrameData.models[idx];
        normal.load(world);
    }

    private void updateAndBindFramebuffer(){
        GLES30.glBindBufferBase(GLES30.GL_UNIFORM_BUFFER, 0, mFrameBuffer);
        mFrameData.store(mFrameMemory).flip();
        GLES20.glBufferSubData(GLES30.GL_UNIFORM_BUFFER, 0, mFrameMemory.remaining(), mFrameMemory);
    }

    private void setupBoxMat(){
        mLightParams.materialAmbient  .set(0.5f, 0.5f, 0.5f);
        mLightParams.materialDiffuse  .set(1.0f, 1.0f, 1.0f);
        mLightParams.materialSpecular .set(0.4f, 0.4f, 0.4f, 16.0f);
        mLightParams.color            .set(0,0,0,0);
    }

    private void setupLandMat(){
        mLightParams.materialAmbient  .set(0.5f, 0.5f, 0.5f);
        mLightParams.materialDiffuse  .set(1.0f, 1.0f, 1.0f);
        mLightParams.materialSpecular .set(0.2f, 0.2f, 0.2f, 16.0f);
        mLightParams.color            .set(0,0,0,0);
    }

    private void setupWaveMat(){
        mLightParams.materialAmbient  .set(0.5f, 0.5f, 0.5f);
        mLightParams.materialDiffuse  .set(1.0f, 1.0f, 1.0f);
        mLightParams.materialSpecular .set(0.8f, 0.8f, 0.8f, 32.0f);
        mLightParams.color            .set(1,1,1,0.5f);
    }

    private void setupTreeMat(){
        mLightParams.materialAmbient  .set(0.5f, 0.5f, 0.5f);
        mLightParams.materialDiffuse  .set(1.0f, 1.0f, 1.0f);
        mLightParams.materialSpecular .set(0.2f, 0.2f, 0.2f, 16.0f);
        mLightParams.color            .set(0,0,0,0);
    }

    private void buildGeometryBuffers(){
        RenderMesh.MeshParams params = new RenderMesh.MeshParams();
        params.posAttribLoc = 0;
        params.norAttribLoc = 2;
        params.texAttribLoc = 1;

        mBoxMesh = new BoxMesh();
        mBoxMesh.initlize(params);

        mLandMesh = new LandMesh();
        mLandMesh.initlize(params);

        mWaterMesh = new WaterMesh();
        mWaterMesh.initlize(params);

        // Create the uniform buffer
        mFrameBuffer = GLES.glGenBuffers();
        GLES20.glBindBuffer(GLES30.GL_UNIFORM_BUFFER, mFrameBuffer);
        GLES20.glBufferData(GLES30.GL_UNIFORM_BUFFER, FrameData.SIZE, null, GLES30.GL_DYNAMIC_DRAW);
        GLES20.glBindBuffer(GLES30.GL_UNIFORM_BUFFER, 0);

        if(mFrameMemory == null)
            mFrameMemory = BufferUtils.createByteBuffer(FrameData.SIZE);

        ReadableVector3f max = mLandMesh.getMax();
        mWavesWorld.scale(max.getX(), 1.0f, max.getZ());
    }

    private void buildTreeSpritesBuffer(){
        final int mtPosition = 0;
        final int mtSize = 1;
        FloatBuffer v = BufferUtils.createFloatBuffer(TREE_COUNT * 5);
        for(int i = 0; i < TREE_COUNT; ++i)
        {
            float x = NvUtils.random(-35.0f, 35.0f);
            float z = NvUtils.random(-35.0f, 35.0f);
            float y = mLandMesh.getHillHeight(x,z);

            // Move tree slightly above land height.
            y += 10.0f;

//			v[i].Pos  = XMFLOAT3(x,y,z);
//			v[i].Size = XMFLOAT2(24.0f, 24.0f);
            v.put(x).put(y).put(z);  // position
            v.put(24.0f).put(24.0f);  // size
        }
        v.flip();

        mTreeSpritesVB = GLES.glGenBuffers();
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mTreeSpritesVB);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER,v.remaining() << 2,  v, GLES20.GL_STATIC_DRAW);

        mTreeSpritesVBO = GLES.glGenVertexArrays();
        GLES30.glBindVertexArray(mTreeSpritesVBO);
        {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mTreeSpritesVB);
            GLES20.glVertexAttribPointer(mtPosition, 3, GL11.GL_FLOAT, false, 5 * 4, 0);
            GLES20.glVertexAttribPointer(mtSize, 2, GL11.GL_FLOAT, false, 5 * 4, 3 * 4);

            GLES20.glEnableVertexAttribArray(mtPosition);
            GLES20.glEnableVertexAttribArray(mtSize);
        }
        GLES30.glBindVertexArray(0);
    }

    private void buildFX(){
        mLightInstanceProgram = new BaseShadingProgram(null);
        int index = GLES30.glGetUniformBlockIndex(mLightInstanceProgram.getProgram(), "FrameData");
        GLES30.glUniformBlockBinding(mLightInstanceProgram.getProgram(), index, 0);

        mShadowmapProgram = new GenerateShadowMapProgram(null, true);
        index = GLES30.glGetUniformBlockIndex(mShadowmapProgram.getProgram(), "FrameData");
        GLES30.glUniformBlockBinding(mShadowmapProgram.getProgram(), index, 0);

        mWaterRenderProgram = new WaterRenderProgram(null);
        index = GLES30.glGetUniformBlockIndex(mWaterRenderProgram.getProgram(), "FrameData");
        GLES30.glUniformBlockBinding(mWaterRenderProgram.getProgram(), index, 0);
    }

    private void loadTextures(){
        mGrassMapSRV = NvImage.uploadTextureFromDDSFile("textures/grass.dds");
        makeTextureProperties(false);
        mWavesMapSRV = NvImage.uploadTextureFromDDSFile("textures/water2.dds");
        makeTextureProperties(false);
        mBoxMapSRV = NvImage.uploadTextureFromDDSFile("textures/WireFence.dds");
        makeTextureProperties(false);

        /*mTreeTextureMapArraySRV = GLES.glGenTextures();
        GLES20.glBindTexture(GLES30.GL_TEXTURE_2D_ARRAY, mTreeTextureMapArraySRV);
        GLES30.glTexStorage3D(GLES30.GL_TEXTURE_2D_ARRAY, 10, GLES30.GL_RGBA8, 512, 512, 4);
        NvImage.setDXTExpansion(true);
        NvImage.upperLeftOrigin(false);
        for(int i = 0; i < 4; i++){
            NvImage image = new NvImage();
            image.loadImageFromFile(String.format("textures/tree%d.dds", i));

            int w = image.getWidth();
            int h = image.getHeight();
            for (int l = 0; l < image.getMipLevels(); l++) {
                if (image.isCompressed()) {
                    ByteBuffer buffer = GLUtil.wrap(image.getLevel(l));
                    GLES30.glCompressedTexSubImage3D(GLES30.GL_TEXTURE_2D_ARRAY, l, 0, 0, i, w, h, 1, image.getFormat(), buffer.remaining(), buffer);
                } else {
                    GLES30.glTexSubImage3D(GLES30.GL_TEXTURE_2D_ARRAY, l, 0, 0, i, w, h, 1, image.getFormat(), GL11.GL_UNSIGNED_BYTE, GLUtil.wrap(image.getLevel(l)));
                }

                w >>= 1;
                h >>= 1;
                w = (w != 0) ? w : 1;
                h = (h != 0) ? h : 1;
            }
        }

        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);*/
    }

    private void makeTextureProperties( boolean mipmap){
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, mipmap? GLES20.GL_LINEAR_MIPMAP_LINEAR: GL11.GL_LINEAR);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
    }
}
