package jet.learning.opengl.d3dcoder;

import android.opengl.GLES20;

import com.nvidia.developer.opengl.app.NvSampleApp;
import com.nvidia.developer.opengl.utils.AttribBinder;
import com.nvidia.developer.opengl.utils.AttribBindingTask;
import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.NvImage;
import com.nvidia.developer.opengl.utils.NvPackedColor;
import com.nvidia.developer.opengl.utils.NvUtils;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.ReadableVector3f;
import org.lwjgl.util.vector.Vector3f;

import javax.microedition.khronos.opengles.GL11;

import jet.learning.opengl.common.RenderMesh;
import jet.learning.opengl.common.SimpleLightProgram;
import jet.learning.opengl.common.SkullMesh;
import jet.learning.opengl.common.WaveMesh;
import jet.learning.opengl.common.Waves;

/**
 * Created by mazhen'gui on 2017/4/7.
 */

public final class CrateApp extends NvSampleApp {

    //	ID3D11ShaderResourceView* mDiffuseMapSRV;
    int mDiffuseMapSRV;

    SimpleLightProgram mLightProgram;

    final SimpleLightProgram.LightParams mBoxMat2 = new SimpleLightProgram.LightParams();

    final Matrix4f mBoxWorld = new Matrix4f();

    final Matrix4f mProj = new Matrix4f();
    final Matrix4f mView = new Matrix4f();
    final Matrix4f mProjView = new Matrix4f();
    private SkullMesh mBox;
    private WaveMesh mWave;

    @Override
    protected void initRendering() {
        /*int shaderBinaryCount = GLES.glGetInteger(GLES30.GL_NUM_SHADER_BINARY_FORMATS);
        Log.i("OPENGL", "shaderBinaryCount = " + shaderBinaryCount);
        for (int i = 0; i < shaderBinaryCount; i++){
            int shaderBinary = GLES.glGetInteger(i, GLES30.GL_SHADER_BINARY_FORMATS);
            Log.i("OPENGL", "binary format [" + i + "] = " + shaderBinary);
        }*/

        setTitle("Crate Demo");
        mBoxMat2.color.set(0,0,0,0);
        mBoxMat2.lightAmbient.set(0.3f, 0.3f, 0.3f);
        mBoxMat2.lightDiffuse.set(0.8f, 0.8f, 0.8f);
        mBoxMat2.lightSpecular.set(0.6f, 0.6f, 0.6f);
        mBoxMat2.lightPos.set(-0.707f, 0.0f, 0.707f);
        mBoxMat2.materialAmbient.set(0.5f, 0.5f, 0.5f);
        mBoxMat2.materialDiffuse.set(1.0f, 1.0f, 1.0f);
        mBoxMat2.materialSpecular.set(0.6f, 0.6f, 0.6f, 64);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        mDiffuseMapSRV = NvImage.uploadTextureFromDDSFile("textures/flower1024.dds");
        if(mDiffuseMapSRV == 0)
            throw new RuntimeException("Load data/WoodCrate01.dds failed!");
        GLES20.glBindTexture(GL11.GL_TEXTURE_2D, mDiffuseMapSRV);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
//        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, 4);
        GLES.checkGLError();
        buildFX();
        buildGeometryBuffers();

        m_transformer.setTranslation(0, 0, -2.5f);
    }

    void buildGeometryBuffers(){
        RenderMesh.MeshParams params = new RenderMesh.MeshParams();
        params.posAttribLoc = mLightProgram.getAttribPosition();
        params.norAttribLoc = mLightProgram.getNormalAttribLoc();
        params.texAttribLoc = mLightProgram.getAttribTexCoord();

        mBox = new SkullMesh();
        mBox.initlize(params);

        Waves mWaves = new Waves();
        mWaves.init(40, 40, 4.0f, 0.03f, 3.25f, 0.4f);
        mWave = new WaveMesh(mWaves);
        mWave.initlize(params);
    }

    @Override
    protected void reshape(int width, int height) {
        Matrix4f.perspective((float)Math.toDegrees(0.25 * NvUtils.PI), (float)width/height, 1.0f, 1000.0f, mProj);
    }

    @Override
    public void draw() {
        ReadableVector3f color = NvPackedColor.LIGHT_STEEL_BLUE;
        GLES20.glClearColor(color.getX(), color.getY(), color.getZ(), 1);
        GLES20.glClear(GL11.GL_COLOR_BUFFER_BIT| GL11.GL_DEPTH_BUFFER_BIT);
        GLES20.glEnable(GL11.GL_DEPTH_TEST);

        m_transformer.getModelViewMat(mView);

//        Matrix4f.lookAt(2.5f,0,0, 0,0,0, 0,1,0, mView);
        Matrix4f.mul(mProj, mView, mProjView);
        mView.invert();
        Matrix4f.transformVector(mView, Vector3f.ZERO, mBoxMat2.eyePos);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GL11.GL_TEXTURE_2D, mDiffuseMapSRV);

        mBoxMat2.model.load(mBoxWorld);
        Matrix4f.mul(mProjView, mBoxWorld, mBoxMat2.modelViewProj);
        mLightProgram.enable();
        mLightProgram.setLightParams(mBoxMat2);

        mBox.draw();

        mWave.update(getFrameDeltaTime());
        mWave.draw();
    }

    void buildFX(){
        mLightProgram = new SimpleLightProgram(true, new AttribBindingTask(
                new AttribBinder(SimpleLightProgram.POSITION_ATTRIB_NAME, 0),
                new AttribBinder(SimpleLightProgram.TEXTURE_ATTRIB_NAME, 1),
                new AttribBinder(SimpleLightProgram.NORMAL_ATTRIB_NAME, 2)));
    }
}
