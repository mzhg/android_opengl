package jet.learning.opengl.d3dcoder;


import android.opengl.GLES20;
import android.opengl.GLES30;

import com.nvidia.developer.opengl.app.NvSampleApp;
import com.nvidia.developer.opengl.utils.BufferUtils;
import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.NvImage;
import com.nvidia.developer.opengl.utils.NvPackedColor;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.ReadableVector3f;
import org.lwjgl.util.vector.Vector3f;

import java.nio.ByteBuffer;

import javax.microedition.khronos.opengles.GL11;

import jet.learning.opengl.common.BaseShadingProgram;
import jet.learning.opengl.common.FrameData;
import jet.learning.opengl.common.RenderMesh;
import jet.learning.opengl.common.ShapeMesh;
import jet.learning.opengl.common.SkullMesh;
import jet.learning.opengl.common.SkyRenderer;

/**
 * 2017-12-12 Update: Add the instance rendering and reflection.
 *
 * Created by mazhen'gui on 2017/11/22.
 */

public final class NormalMap extends NvSampleApp {
    BaseShadingProgram mLightInstanceProgram;

    final BaseShadingProgram.ShadingParams mLightParams = new BaseShadingProgram.ShadingParams();
    final FrameData mFrameData = new FrameData(1);

    final Matrix4f mSkullWorld = new Matrix4f();
    final Matrix4f mProj = new Matrix4f();
    final Matrix4f mView = new Matrix4f();
    final Matrix4f mProjView = new Matrix4f();

    ShapeMesh mShapeVBO;
    SkullMesh mSkullVBO;
    SkyRenderer mSky;

    int mFrameBuffer;
    ByteBuffer mFrameMemory;

    int mBrickTexSRV;       // texture 2d
    int mStoneTexSRV;       // texture 2d

    int mStoneNormalTexSRV;
    int mBrickNormalTexSRV;

    @Override
    public void initUI() {
//        mTweakBar.addValue("Enable Instance", createControl("mUseInstance"));
    }

    @Override
    protected void initRendering() {
        mLightParams.lightAmbient.set(0.4f, 0.4f, 0.4f);
        mLightParams.lightDiffuse.set(0.8f, 0.8f, 0.7f);
        mLightParams.lightSpecular .set(0.8f, 0.8f, 0.7f);
        mLightParams.lightPos .set(-0.707f, 0.0f, -0.707f, 0);
        mLightParams.color.set(0.f,0.f,0.f,0.0f);

        buildFX();
        buildGeometryBuffers();
        loadTextures();

        m_transformer.setTranslation(0,-5,-15);
        m_transformer.setRotationVec(new Vector3f(0, PI, 0));

        mSky = new SkyRenderer("textures/snowcube1024.dds", 100.0f);

        GLES.checkGLError();
    }

    @Override
    protected void draw() {
        ReadableVector3f color = NvPackedColor.LIGHT_STEEL_BLUE;
        GLES20.glClearColor(color.getX(), color.getY(), color.getZ(), 1);
        GLES20.glClear(GL11.GL_COLOR_BUFFER_BIT| GL11.GL_DEPTH_BUFFER_BIT);


        // Draw the sky box first
        {
            GLES20.glDisable(GLES20.GL_DEPTH_TEST);
            GLES20.glDepthMask(false);

            Matrix4f rotate = m_transformer.getRotationMat();
            Matrix4f.mul(mProj, rotate, mProjView);
            mSky.draw(mProjView);

            GLES20.glEnable(GL11.GL_DEPTH_TEST);
            GLES20.glDepthMask(true);
        }

        m_transformer.getModelViewMat(mView);

//        Matrix4f.lookAt(2.5f,0,0, 0,0,0, 0,1,0, mView);
        Matrix4f.mul(mProj, mView, mProjView);
        mView.invert();
        Matrix4f.transformVector(mView, Vector3f.ZERO, mLightParams.eyePos);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        drawSceneWithInstances();
    }

    private void drawSceneWithInstances(){
        mLightInstanceProgram.enable();

        mShapeVBO.bind();
        mFrameData.viewProj.load(mProjView);

        {
            // Draw the grid
            mFrameData.setInstanceCount(1);
            mFrameData.texMat.m00 = 8;
            mFrameData.texMat.m11 = 10;
            mFrameData.texMat.m22 = 1;
            mLightParams.enableNormalMap = true;
            buildFrameInstance(0, mShapeVBO.getGridWorld());
            setupGridMat();
            updateAndBindFramebuffer();
            mLightInstanceProgram.setShadingParams(mLightParams);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mStoneTexSRV);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mStoneNormalTexSRV);

            mShapeVBO.drawGrid();
            GLES.checkGLError();
        }

        {//		 Draw the box
            mFrameData.setInstanceCount(1);
            mFrameData.texMat.m00 = 2;
            mFrameData.texMat.m11 = 1;
            mFrameData.texMat.m22 = 1;
            buildFrameInstance(0, mShapeVBO.getBoxWorld());
            setupBoxMat();
            updateAndBindFramebuffer();
            mLightInstanceProgram.setShadingParams(mLightParams);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GL11.GL_TEXTURE_2D, mBrickTexSRV);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mBrickNormalTexSRV);

            mShapeVBO.drawBox();
            GLES.checkGLError();
        }



        { //  Draw the cylinders.
            mFrameData.setInstanceCount(10);
            mFrameData.texMat.m00 = 1;
            mFrameData.texMat.m11 = 2;
            mFrameData.texMat.m22 = 1;
            setupCylinderMat();
            for(int i = 0; i < 10; i++){
                buildFrameInstance(i, mShapeVBO.getCylinderWorld(i));
            }
            updateAndBindFramebuffer();
            mLightInstanceProgram.setShadingParams(mLightParams);
            mShapeVBO.drawCylinders(10);
            GLES.checkGLError();

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            mLightParams.enableNormalMap = false;
        }

        { // Draw the spheres.
            mFrameData.setInstanceCount(10);
            mFrameData.texMat.m00 = 1;
            mFrameData.texMat.m11 = 1;
            mFrameData.texMat.m22 = 1;
            setupSphereMat();
            for(int i = 0; i < 10; i++){
                buildFrameInstance(i, mShapeVBO.getSphereWorld(i));
            }

            GLES20.glBindTexture(GL11.GL_TEXTURE_2D, 0);
            mLightParams.enableReflection = true;
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, mSky.cubeMapSRV());

            updateAndBindFramebuffer();
            mLightInstanceProgram.setShadingParams(mLightParams);
            mShapeVBO.drawSphere(10);
            GLES.checkGLError();
        }

        mShapeVBO.unbind();

        { // Draw the skull
            mFrameData.setInstanceCount(1);
            buildFrameInstance(0, mSkullWorld);
            setupSkullMat();
            updateAndBindFramebuffer();
            mLightInstanceProgram.setShadingParams(mLightParams);
            mSkullVBO.draw();

            GLES.checkGLError();
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        mLightParams.enableReflection = false;

        GLES30.glBindBufferBase(GLES30.GL_UNIFORM_BUFFER, 0, 0);
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
//        normal.invert().transpose();
    }

    private void updateAndBindFramebuffer(){
        GLES30.glBindBufferBase(GLES30.GL_UNIFORM_BUFFER, 0, mFrameBuffer);
        mFrameData.store(mFrameMemory).flip();
        GLES20.glBufferSubData(GLES30.GL_UNIFORM_BUFFER, 0, mFrameMemory.remaining(), mFrameMemory);
    }

    private void setupGridMat(){
        mLightParams.materialAmbient  .set(0.8f, 0.8f, 0.8f);
        mLightParams.materialDiffuse  .set(1.0f, 1.0f, 1.0f);
        mLightParams.materialSpecular .set(0.4f, 0.4f, 0.4f, 16.0f);
        mLightParams.materialReflect  .set(0,0,0);
    }

    private void setupCylinderMat(){
        mLightParams.materialAmbient  .set(1.0f, 1.0f, 1.0f);
        mLightParams.materialDiffuse  .set(1.0f, 1.0f, 1.0f);
        mLightParams.materialSpecular .set(1.0f, 1.0f, 1.0f, 32.0f);
        mLightParams.materialReflect  .set(0,0,0);
    }

    private void setupSphereMat(){
        mLightParams.materialAmbient  .set(0.2f, 0.3f, 0.4f);
        mLightParams.materialDiffuse  .set(0.2f, 0.3f, 0.4f);
        mLightParams.materialSpecular .set(0.9f, 0.9f, 0.9f, 16.0f);
        mLightParams.materialReflect  .set(0.4f, 0.4f, 0.4f);
    }

    private void setupBoxMat(){
        mLightParams.materialAmbient  .set(1.0f, 1.0f, 1.0f);
        mLightParams.materialDiffuse  .set(1.0f, 1.0f, 1.0f);
        mLightParams.materialSpecular .set(0.8f, 0.8f, 0.8f, 16.0f);
        mLightParams.materialReflect  .set(0.0f, 0.0f, 0.0f);
    }

    private void setupSkullMat(){
        mLightParams.materialAmbient  .set(0.2f, 0.2f, 0.2f);
        mLightParams.materialDiffuse  .set(0.2f, 0.2f, 0.2f);
        mLightParams.materialSpecular .set(0.8f, 0.8f, 0.8f, 16.0f);
        mLightParams.materialReflect  .set(0.5f, 0.5f, 0.5f);
    }

    void buildFX(){
        mLightInstanceProgram = new BaseShadingProgram(null);
        int index = GLES30.glGetUniformBlockIndex(mLightInstanceProgram.getProgram(), "FrameData");
        GLES30.glUniformBlockBinding(mLightInstanceProgram.getProgram(), index, 0);
    }

    void buildGeometryBuffers(){
        RenderMesh.MeshParams params = new RenderMesh.MeshParams();
        params.posAttribLoc = 0;
        params.norAttribLoc = 2;
        params.texAttribLoc = 1;
        params.tanAttribLoc = 3;

        mShapeVBO = new ShapeMesh();
        mShapeVBO.initlize(params);

        mSkullVBO = new SkullMesh();
        mSkullVBO.initlize(params);

        mSkullWorld.setIdentity();
        mSkullWorld.translate(0.0f, 1.0f, 0.0f);
        mSkullWorld.scale(0.5f, 0.5f, 0.5f);

        // Create the uniform buffer
        mFrameBuffer = GLES.glGenBuffers();
        GLES20.glBindBuffer(GLES30.GL_UNIFORM_BUFFER, mFrameBuffer);
        GLES20.glBufferData(GLES30.GL_UNIFORM_BUFFER, FrameData.SIZE, null, GLES30.GL_DYNAMIC_DRAW);
        GLES20.glBindBuffer(GLES30.GL_UNIFORM_BUFFER, 0);

        if(mFrameMemory == null)
            mFrameMemory = BufferUtils.createByteBuffer(FrameData.SIZE);
    }

    void loadTextures(){
        mBrickTexSRV = NvImage.uploadTextureFromDDSFile("textures/bricks.dds");
        makeTextureProperties(false);
        mStoneTexSRV = NvImage.uploadTextureFromDDSFile("textures/floor.dds");
        makeTextureProperties(false);

        mStoneNormalTexSRV = NvImage.uploadTextureFromDDSFile("textures/floor_nmap.dds");
        makeTextureProperties(true);
        mBrickNormalTexSRV = NvImage.uploadTextureFromDDSFile("textures/bricks_nmap.dds");
        makeTextureProperties(true);
    }

    private void makeTextureProperties( boolean mipmap){
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, mipmap? GLES20.GL_LINEAR_MIPMAP_LINEAR: GL11.GL_LINEAR);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
    }
}
