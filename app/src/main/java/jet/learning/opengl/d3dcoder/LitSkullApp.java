package jet.learning.opengl.d3dcoder;


import android.opengl.GLES20;

import com.nvidia.developer.opengl.app.NvSampleApp;
import com.nvidia.developer.opengl.utils.AttribBinder;
import com.nvidia.developer.opengl.utils.AttribBindingTask;
import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.NvPackedColor;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.ReadableVector3f;
import org.lwjgl.util.vector.Vector3f;

import javax.microedition.khronos.opengles.GL11;

import jet.learning.opengl.common.RenderMesh;
import jet.learning.opengl.common.ShapeMesh;
import jet.learning.opengl.common.SimpleLightProgram;
import jet.learning.opengl.common.SkullMesh;

/**
 * Created by mazhen'gui on 2017/11/22.
 */

public final class LitSkullApp extends NvSampleApp {
    SimpleLightProgram mLightProgram;
    final SimpleLightProgram.LightParams mLightParams = new SimpleLightProgram.LightParams();

    final Matrix4f mSkullWorld = new Matrix4f();
    final Matrix4f mProj = new Matrix4f();
    final Matrix4f mView = new Matrix4f();
    final Matrix4f mProjView = new Matrix4f();

    ShapeMesh mShapeVBO;
    SkullMesh mSkullVBO;

    @Override
    protected void initRendering() {
        mLightParams.lightAmbient.set(0.2f, 0.2f, 0.2f);
        mLightParams.lightDiffuse.set(0.8f, 0.8f, 0.8f);
        mLightParams.lightSpecular .set(1.f, 1.f, 1.0f);
        mLightParams.lightPos .set(0.57735f, -0.57735f, 0.57735f, 0);
        mLightParams.lightPos.scale(-1);
        mLightParams.color.set(0.6f,0.6f,0.6f,1.0f);

        buildFX();
        buildGeometryBuffers();

        float mRadius = 15;
        float mTheta = 1.5f * PI;
        float mPhi   = 0.1f * PI;
        float x = (float) (mRadius*Math.sin(mPhi)*Math.cos(mTheta));
        float z = (float) (mRadius*Math.sin(mPhi)*Math.sin(mTheta));
        float y = (float) (mRadius*Math.cos(mPhi));
        m_transformer.setTranslation(0,-5,-15);
        m_transformer.setRotationVec(new Vector3f(0, PI, 0));

        GLES.checkGLError();
    }

    @Override
    protected void draw() {
        ReadableVector3f color = NvPackedColor.LIGHT_STEEL_BLUE;
        GLES20.glClearColor(color.getX(), color.getY(), color.getZ(), 1);
        GLES20.glClear(GL11.GL_COLOR_BUFFER_BIT| GL11.GL_DEPTH_BUFFER_BIT);
        GLES20.glEnable(GL11.GL_DEPTH_TEST);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        m_transformer.getModelViewMat(mView);

//        Matrix4f.lookAt(2.5f,0,0, 0,0,0, 0,1,0, mView);
        Matrix4f.mul(mProj, mView, mProjView);
        mView.invert();
        Matrix4f.transformVector(mView, Vector3f.ZERO, mLightParams.eyePos);
        mLightProgram.enable();

        mShapeVBO.bind();
        {
            // Draw the grid
            buildFrame(mShapeVBO.getGridWorld());
            setupGridMat();
            mLightProgram.setLightParams(mLightParams);
            mShapeVBO.drawGrid();
            GLES.checkGLError();
        }

        {//		 Draw the box
            buildFrame(mShapeVBO.getBoxWorld());
            setupBoxMat();
            mLightProgram.setLightParams(mLightParams);
            mShapeVBO.drawBox();
            GLES.checkGLError();
        }

        { //  Draw the cylinders.

            setupCylinderMat();
            for(int i = 0; i < 10; i++){
                buildFrame(mShapeVBO.getCylinderWorld(i));
                mLightProgram.setLightParams(mLightParams);
                mShapeVBO.drawCylinders();
            }
            GLES.checkGLError();
        }

        { // Draw the spheres.
            setupSphereMat();
            for(int i = 0; i < 10; i++){
                buildFrame(mShapeVBO.getSphereWorld(i));
                mLightProgram.setLightParams(mLightParams);
                mShapeVBO.drawSphere();
            }
            GLES.checkGLError();
        }

        mShapeVBO.unbind();

        { // Draw the skull
            buildFrame(mSkullWorld);
            setupSkullMat();
            mLightProgram.setLightParams(mLightParams);
            mSkullVBO.draw();
            GLES.checkGLError();
        }
    }

    @Override
    protected void reshape(int width, int height) {
        Matrix4f.perspective((float)Math.toDegrees(0.25 * PI), (float)width/height, 1.0f, 1000.0f, mProj);
        GLES20.glViewport(0,0, width, height);
    }

    private void buildFrame(Matrix4f world){
        mLightParams.model.load(world);
        Matrix4f.mul(mProjView, world, mLightParams.modelViewProj);
    }

    private void setupGridMat(){
        mLightParams.materialAmbient  .set(0.48f, 0.77f, 0.46f);
        mLightParams.materialDiffuse  .set(0.48f, 0.77f, 0.46f);
        mLightParams.materialSpecular .set(0.2f, 0.2f, 0.2f, 16.0f);
    }

    private void setupCylinderMat(){
        mLightParams.materialAmbient  .set(0.7f, 0.85f, 0.7f);
        mLightParams.materialDiffuse  .set(0.7f, 0.85f, 0.7f);
        mLightParams.materialSpecular .set(0.8f, 0.8f, 0.8f, 16.0f);
    }

    private void setupSphereMat(){
        mLightParams.materialAmbient  .set(0.1f, 0.2f, 0.3f);
        mLightParams.materialDiffuse  .set(0.2f, 0.4f, 0.6f);
        mLightParams.materialSpecular .set(0.9f, 0.9f, 0.9f, 16.0f);
    }

    private void setupBoxMat(){
        mLightParams.materialAmbient  .set(0.651f, 0.5f, 0.392f);
        mLightParams.materialDiffuse  .set(0.651f, 0.5f, 0.392f);
        mLightParams.materialSpecular .set(0.2f, 0.2f, 0.2f, 16.0f);
    }

    private void setupSkullMat(){
        mLightParams.materialAmbient  .set(0.8f, 0.8f, 0.8f);
        mLightParams.materialDiffuse  .set(0.8f, 0.8f, 0.8f);
        mLightParams.materialSpecular .set(0.8f, 0.8f, 0.8f, 16.0f);
    }

    void buildFX(){
        mLightProgram = new SimpleLightProgram(true, new AttribBindingTask(
                new AttribBinder(SimpleLightProgram.POSITION_ATTRIB_NAME, 0),
                new AttribBinder(SimpleLightProgram.TEXTURE_ATTRIB_NAME, 1),
                new AttribBinder(SimpleLightProgram.NORMAL_ATTRIB_NAME, 2)));
    }

    void buildGeometryBuffers(){
        RenderMesh.MeshParams params = new RenderMesh.MeshParams();
        params.posAttribLoc = mLightProgram.getAttribPosition();
        params.norAttribLoc = mLightProgram.getNormalAttribLoc();
        params.texAttribLoc = mLightProgram.getAttribTexCoord();

        mShapeVBO = new ShapeMesh();
        mShapeVBO.initlize(params);

        mSkullVBO = new SkullMesh();
        mSkullVBO.initlize(params);

        mSkullWorld.setIdentity();
        mSkullWorld.translate(0.0f, 1.0f, 0.0f);
        mSkullWorld.scale(0.5f, 0.5f, 0.5f);
    }
}
