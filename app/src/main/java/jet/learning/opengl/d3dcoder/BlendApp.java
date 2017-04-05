package jet.learning.opengl.d3dcoder;

import android.opengl.GLES20;
import android.opengl.GLES30;

import com.nvidia.developer.opengl.app.NvSampleApp;
import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.GLUtil;
import com.nvidia.developer.opengl.utils.Glut;
import com.nvidia.developer.opengl.utils.NvGLSLProgram;
import com.nvidia.developer.opengl.utils.NvImage;
import com.nvidia.developer.opengl.utils.NvPackedColor;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.ReadableVector3f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL11;

import jet.learning.opengl.common.DirectionalLight;
import jet.learning.opengl.common.GeometryGenerator;
import jet.learning.opengl.common.Material;
import jet.learning.opengl.common.MathUtil;
import jet.learning.opengl.common.MeshData;
import jet.learning.opengl.common.UniformLights;
import jet.learning.opengl.common.UniformMatrix;
import jet.learning.opengl.common.Vertex;
import jet.learning.opengl.common.Waves;

/**
 * Created by mazhen'gui on 2017/4/5.
 */

public class BlendApp extends NvSampleApp {

    static final int LIGHTING = 0;
    static final int TEXTURES = 1;
    static final int TEXTURES_AND_FOG = 2;

    int mLandVB;
    int mLandIB;
    int mLandVBO;

    int mWavesVB;
    int mWavesIB;
    int mWavesVBO;

    int mBoxVB;
    int mBoxIB;
    int mBoxVBO;

    int mProgram;
    int mfxPosition;
    int mfxNormal;
    int mfxTex;
    int mfxAlphaClip;
    int mfxFogEnabled;
    int mfxUseTexture;

    int mGrassMapSRV;
    int mWavesMapSRV;
    int mBoxMapSRV;

    final Waves mWaves = new Waves();

    final UniformMatrix mMatrix = new UniformMatrix();
    final UniformLights mLights = new UniformLights();
    final DirectionalLight[] mDirLights = mLights.gDirLights;

    final Material mLandMat = new Material();
    final Material mWavesMat = new Material();
    final Material mBoxMat = new Material();

    final Matrix4f mGrassTexTransform = new Matrix4f();
    final Matrix4f mWaterTexTransform = new Matrix4f();
    final Matrix4f mBoxTexTransform = new Matrix4f();
    final Matrix4f mLandWorld = new Matrix4f();
    final Matrix4f mWavesWorld = new Matrix4f();
    final Matrix4f mBoxWorld = new Matrix4f();

    final Matrix4f mView = new Matrix4f();
    final Matrix4f mProj = new Matrix4f();
    final Matrix4f mProjView = new Matrix4f();

    int mLandIndexCount;

    final Vector2f mWaterTexOffset = new Vector2f();

    int mRenderOptions = 1;
    final Vector3f mEyePosW = mLights.gEyePosW;
    private float mRunningTime;

    float mTheta = 1.3f * PI;
    float mPhi = 0.4f * PI;
    float mRadius = 80.0f;

    float t_base;

    ByteBuffer mMapBuffer;

    @Override
    protected void initRendering() {
        setTitle("Blend Demo");

        m_transformer.setTranslation(0,2,2);
        mBoxWorld.translate(8.0f, 5.0f, -15.0f);
        mBoxWorld.scale(15.0f);

        mGrassTexTransform.scale(5.0f, 5.0f, 0.0f);

        mDirLights[0].ambient  .set(0.2f, 0.2f, 0.2f, 1.0f);
        mDirLights[0].diffuse  .set(0.5f, 0.5f, 0.5f, 1.0f);
        mDirLights[0].specular .set(0.5f, 0.5f, 0.5f, 1.0f);
        mDirLights[0].direction .set(0.57735f, -0.57735f, 0.57735f);

        mDirLights[1].ambient  .set(0.0f, 0.0f, 0.0f, 1.0f);
        mDirLights[1].diffuse  .set(0.20f, 0.20f, 0.20f, 1.0f);
        mDirLights[1].specular .set(0.25f, 0.25f, 0.25f, 1.0f);
        mDirLights[1].direction .set(-0.57735f, -0.57735f, 0.57735f);

        mDirLights[2].ambient  .set(0.0f, 0.0f, 0.0f, 1.0f);
        mDirLights[2].diffuse  .set(0.2f, 0.2f, 0.2f, 1.0f);
        mDirLights[2].specular .set(0.0f, 0.0f, 0.0f, 1.0f);
        mDirLights[2].direction .set(0.0f, -0.707f, -0.707f);

        mLandMat.ambient  .set(0.5f, 0.5f, 0.5f, 1.0f);
        mLandMat.diffuse  .set(1.0f, 1.0f, 1.0f, 1.0f);
        mLandMat.specular .set(0.2f, 0.2f, 0.2f, 16.0f);

        mWavesMat.ambient  .set(0.5f, 0.5f, 0.5f, 1.0f);
        mWavesMat.diffuse  .set(1.0f, 1.0f, 1.0f, 0.5f);
        mWavesMat.specular .set(0.8f, 0.8f, 0.8f, 32.0f);

        mBoxMat.ambient  .set(0.5f, 0.5f, 0.5f, 1.0f);
        mBoxMat.diffuse  .set(1.0f, 1.0f, 1.0f, 1.0f);
        mBoxMat.specular .set(0.4f, 0.4f, 0.4f, 16.0f);

        mWaves.init(160, 160, 1.0f, 0.03f, 3.25f, 0.4f);

        mGrassMapSRV = NvImage.uploadTextureFromDDSFile("data/grass.dds");
        makeTextureProperties();
        mWavesMapSRV = NvImage.uploadTextureFromDDSFile("data/water2.dds");
        makeTextureProperties();
        mBoxMapSRV = NvImage.uploadTextureFromDDSFile("data/WireFence.dds");
        makeTextureProperties();

        buildProgram();
        buildLandGeometryBuffers();
        buildWaveGeometryBuffers();
        buildCrateGeometryBuffers();
    }

    @Override
    protected void reshape(int width, int height) {
        Matrix4f.perspective((float)Math.toDegrees(0.25 * PI), (float)width/height, 1.0f, 1000.0f, mProj);
    }

    @Override
    protected void draw() {
        updateScene(getFrameDeltaTime());
        drawScene();
    }

    private void drawScene(){
        ReadableVector3f color = NvPackedColor.LIGHT_STEEL_BLUE;
        GLES20.glClearColor(color.getX(), color.getY(), color.getZ(), 1);
        GLES20.glClear(GL11.GL_COLOR_BUFFER_BIT| GL11.GL_DEPTH_BUFFER_BIT);
        GLES20.glEnable(GL11.GL_DEPTH_TEST);

        mLights.apply();

        Matrix4f.mul(mProj, mView, mProjView);

        // Draw the box with alpha clipping.
        GLES20.glDisable(GL11.GL_CULL_FACE);
        switch (mRenderOptions) {
            case LIGHTING:
                GLES20.glUniform1i(mfxUseTexture, 0);
                GLES20.glUniform1i(mfxAlphaClip, 0);
                GLES20.glUniform1i(mfxFogEnabled, 0);
                break;
            case TEXTURES:
                GLES20.glUniform1i(mfxUseTexture, 1);
                GLES20.glUniform1i(mfxAlphaClip, 1);
                GLES20.glUniform1i(mfxFogEnabled, 0);
                break;
            case TEXTURES_AND_FOG:
                GLES20.glUniform1i(mfxUseTexture, 1);
                GLES20.glUniform1i(mfxAlphaClip, 1);
                GLES20.glUniform1i(mfxFogEnabled, 1);
                break;
        }

        Matrix4f world = mBoxWorld;
        mMatrix.gWorld.load(world);
        mMatrix.gTexTransform.load(mBoxTexTransform);
        Matrix4f.invert(world, mMatrix.gWorldInvTranspose).transpose();
        Matrix4f.mul(mProjView, world, mMatrix.gWorldViewProj);
        mMatrix.gMaterial.set(mBoxMat);
        mMatrix.apply();

        GLES30.glBindTexture(GL11.GL_TEXTURE_2D, mBoxMapSRV);

        GLES30.glBindVertexArray(mBoxVBO);
        GLES30.glDrawElements(GL11.GL_TRIANGLES, 36, GL11.GL_UNSIGNED_SHORT, 0);

        // Draw the hill
        GLES30.glEnable(GL11.GL_CULL_FACE);
        switch (mRenderOptions) {
            case LIGHTING:
                break;
            case TEXTURES:
                GLES30.glUniform1i(mfxAlphaClip, 0);
                break;
            case TEXTURES_AND_FOG:
                GLES30.glUniform1i(mfxAlphaClip, 0);
                break;
        }
        world = mLandWorld;
        mMatrix.gWorld.load(world);
        mMatrix.gTexTransform.load(mGrassTexTransform);
        Matrix4f.invert(world, mMatrix.gWorldInvTranspose).transpose();
        Matrix4f.mul(mProjView, world, mMatrix.gWorldViewProj);
        mMatrix.gMaterial.set(mLandMat);
        mMatrix.apply();

        GLES30.glBindTexture(GL11.GL_TEXTURE_2D, mGrassMapSRV);

        GLES30.glBindVertexArray(mLandVBO);
        GLES30.glDrawElements(GL11.GL_TRIANGLES, mLandIndexCount, GL11.GL_UNSIGNED_SHORT, 0);


        // Draw the waves
        world = mWavesWorld;
        mMatrix.gWorld.load(world);
        mMatrix.gTexTransform.load(mWaterTexTransform);
        Matrix4f.invert(world, mMatrix.gWorldInvTranspose).transpose();
        Matrix4f.mul(mProjView, world, mMatrix.gWorldViewProj);
        mMatrix.gMaterial.set(mWavesMat);
        mMatrix.apply();

        GLES30.glBindTexture(GL11.GL_TEXTURE_2D, mWavesMapSRV);
        GLES30.glEnable(GL11.GL_BLEND);
        GLES30.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

        GLES30.glBindVertexArray(mWavesVBO);
        GLES30.glDrawElements(GL11.GL_TRIANGLES, 3*mWaves.triangleCount(), GLES30.GL_UNSIGNED_INT, 0);
        GLES30.glDisable(GL11.GL_BLEND);
    }

    private void updateScene(float dt) {
        // Convert Spherical to Cartesian coordinates.
        /*
        float x = mRadius*Math.sin(mPhi)*Math.cos(mTheta);
        float z = mRadius*Math.sin(mPhi)*Math.sin(mTheta);
        float y = mRadius*Math.cos(mPhi);

        mEyePosW.set(x, y, z);

        // Build the view matrix.
        Vector3f pos    = vec3(x, y, z, 1.0f);
        Vector3f target = VectorUtil.ZERO3;
        Vector3f up     = VectorUtil.UNIT_Y;

//		XMMATRIX V = XMMatrixLookAtLH(pos, target, up);
//		XMStoreFloat4x4(&mView, V);
        VectorUtil.lookAt(pos, target, up, mView);
        */
        m_transformer.getModelViewMat(mView);

        //
        // Every quarter second, generate a random wave.
        //
        if( (mRunningTime - t_base) >= 0.25f )
        {
            t_base += 0.25f;

            int i = 5 + MathUtil.randomInt() % (mWaves.rowCount()-10);
            int j = 5 + MathUtil.randomInt() % (mWaves.columnCount()-10);

            float r = MathUtil.random(1.0f, 2.0f);

            mWaves.disturb(i, j, r);
        }

        mWaves.update(dt);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mWavesVB);
        mMapBuffer = (ByteBuffer)GLES30.glMapBufferRange(GLES20.GL_ARRAY_BUFFER, 0, 8 * mWaves.vertexCount() * 4, GLES30.GL_MAP_WRITE_BIT|GLES30.GL_MAP_INVALIDATE_BUFFER_BIT);

//		System.out.println("mapbuffer remain = " + mMapBuffer.remaining() + ",  position = " + mMapBuffer.position());
        mMapBuffer.clear();
        for(int i = 0; i < mWaves.vertexCount(); ++i)
        {
            Vector3f pos = mWaves.get(i);
            Vector3f nor = mWaves.normal(i);
            mMapBuffer.putFloat(pos.x).putFloat(pos.y).putFloat(pos.z);
            mMapBuffer.putFloat(nor.x).putFloat(nor.y).putFloat(nor.z);

            // Derive tex-coords in [0,1] from position.
            mMapBuffer.putFloat(0.5f + pos.x / mWaves.width());
            mMapBuffer.putFloat(0.5f - pos.z / mWaves.depth());
        }
        GLES30.glUnmapBuffer(GLES20.GL_ARRAY_BUFFER);

        // Translate texture over time.
        mWaterTexOffset.y += 0.05f*dt;
        mWaterTexOffset.x += 0.1f*dt;
        mWaterTexTransform.setIdentity();
        mWaterTexTransform.translate(mWaterTexOffset);
        mWaterTexTransform.scale(5.0f, 5.0f, 0.0f);
    }

    void buildProgram(){
        String lightHelpString = Glut.loadTextFromClassPath(DirectionalLight.class, "LightHelper.glsl").toString();
        StringBuilder vertexString = Glut.loadTextFromClassPath(DirectionalLight.class, "basic.glvs");
        StringBuilder fragmentString = Glut.loadTextFromClassPath(DirectionalLight.class, "basic.glfs");

        String include = "#include \"LightHelper.glsl\"";
        int index = vertexString.indexOf(include);
        if(index != -1)
            vertexString.replace(index, index + include.length(), lightHelpString);

        index = fragmentString.indexOf(include);
        fragmentString.replace(index, index + include.length(), lightHelpString);

        boolean use_uniform_block = false;
        if(!use_uniform_block){
            String define = "#define USE_UNIFORM_BUFFER";

            index = vertexString.indexOf(define);
            if(index != -1){
                vertexString.delete(index, index + define.length());
            }

            index = fragmentString.indexOf(define);
            if(index != -1){
                fragmentString.delete(index, index + define.length());
            }
        }

        NvGLSLProgram program = NvGLSLProgram.createFromStrings(vertexString, fragmentString);
        mProgram = program.getProgram();

        mfxPosition = GLES20.glGetAttribLocation(mProgram, "PosL");
        mfxNormal   = GLES20.glGetAttribLocation(mProgram, "NormalL");
        mfxTex      = GLES20.glGetAttribLocation(mProgram, "Tex");

        mfxAlphaClip  = GLES20.glGetUniformLocation(mProgram, "gAlphaClip");
        mfxFogEnabled = GLES20.glGetUniformLocation(mProgram, "gFogEnabled");
        mfxUseTexture = GLES20.glGetUniformLocation(mProgram, "gUseTexure");
        int texSamp   = GLES20.glGetUniformLocation(mProgram, "gDiffuseMap");

        mMatrix.init(mProgram);
        mLights.init(mProgram);

        GLES20.glUseProgram(mProgram);
        GLES20.glUniform1i(texSamp, 0);
    }

    void buildWaveGeometryBuffers(){
        // Create the vertex buffer.  Note that we allocate space only, as
        // we will be updating the data every time step of the simulation.
        mWavesVB = GLES.glGenBuffers();
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mWavesVB);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, 8 * mWaves.vertexCount() * 4, (Buffer)null, GLES20.GL_DYNAMIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        // Create the index buffer.  The index buffer is fixed, so we only
        // need to create and set once.
        int[] indices = new int[3 * mWaves.triangleCount()];

        // Iterate over each quad.
        int m = mWaves.rowCount();
        int n = mWaves.columnCount();
        int k = 0;
        for(int i = 0; i < m-1; ++i)
        {
            for(int j = 0; j < n-1; ++j)
            {
                indices[k]   = (i*n+j);
                indices[k+1] = (i*n+j+1);
                indices[k+2] = ((i+1)*n+j);

                indices[k+3] = ((i+1)*n+j);
                indices[k+4] = (i*n+j+1);
                indices[k+5] = ((i+1)*n+j+1);

                k += 6; // next quad
            }
        }

        mWavesIB = GLES.glGenBuffers();
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mWavesIB);
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indices.length * 4, GLUtil.wrap(indices), GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

        mWavesVBO = GLES.glGenVertexArrays();
        GLES30.glBindVertexArray(mWavesVBO);
        {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mWavesVB);
            GLES20.glVertexAttribPointer(mfxPosition, 3, GL11.GL_FLOAT, false, 8 * 4, 0);
            GLES20.glVertexAttribPointer(mfxNormal, 3, GL11.GL_FLOAT, false, 8 * 4, 3 * 4);
            GLES20.glVertexAttribPointer(mfxTex, 2, GL11.GL_FLOAT, false, 8 * 4, 6 * 4);

            // Notice, must enable the attribute location in VBO creation block.
            GLES20.glEnableVertexAttribArray(mfxPosition);
            GLES20.glEnableVertexAttribArray(mfxNormal);
            GLES20.glEnableVertexAttribArray(mfxTex);

            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mWavesIB);
        }
        GLES30.glBindVertexArray(0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    float getHillHeight(float x, float z){
        return 0.3f*( z* (float)Math.sin(0.1f*x) + x*(float)Math.cos(0.1f*z) );
    }

    void getHillNormal(float x, float z, Vector3f n){
        // n = (-df/dx, 1, -df/dz)
        n.set(
                -0.03f*z*(float)Math.cos(0.1f*x) - 0.3f*(float)Math.cos(0.1f*z),
                1.0f,
                -0.3f*(float)Math.sin(0.1f*x) + 0.03f*x*(float)Math.sin(0.1f*z));

        n.normalise();
    }

    private void buildLandGeometryBuffers(){
        MeshData grid = new MeshData();
        Vector3f n = new Vector3f();

        GeometryGenerator geoGen = new GeometryGenerator();
        geoGen.createGrid(160.0f, 160.0f, 50, 50, grid);

        mLandIndexCount = grid.getIndiceCount();

        //
        // Extract the vertex elements we are interested and apply the height function to
        // each vertex.
        //
        FloatBuffer vertices = GLUtil.getCachedFloatBuffer(8 * grid.getVertexCount());
        for(int i = 0; i < grid.getVertexCount(); i++){
            Vertex p = grid.vertices.get(i);

            p.positionY = getHillHeight(p.positionX, p.positionZ);
            vertices.put(p.positionX).put(p.positionY).put(p.positionZ);

            getHillNormal(p.positionX, p.positionZ, n);
            vertices.put(n.x).put(n.y).put(n.z);

            vertices.put(p.texCX).put(p.texCY);
        }

        vertices.flip();

        mLandVB = GLES.glGenBuffers();
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mLandVB);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, 4 * 8 * grid.getVertexCount(), vertices,  GLES30.GL_STATIC_DRAW);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);

        mLandIB = GLES.glGenBuffers();
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, mLandIB);
        GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER, 4 * grid.getIndiceCount(), GLUtil.wrap(grid.indices.getData(), 0, grid.getIndiceCount()), GLES30.GL_STATIC_DRAW);
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0);

        mLandVBO = GLES.glGenVertexArrays();
        GLES30.glBindVertexArray(mLandVBO);
        {
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mLandVB);
            GLES30.glVertexAttribPointer(mfxPosition, 3, GL11.GL_FLOAT, false, 8 * 4, 0);
            GLES30.glVertexAttribPointer(mfxNormal, 3, GL11.GL_FLOAT, false, 8 * 4, 3 * 4);
            GLES30.glVertexAttribPointer(mfxTex, 2, GL11.GL_FLOAT, false, 8 * 4, 6 * 4);

            // Notice, must enable the attribute location in VBO creation block.
            GLES30.glEnableVertexAttribArray(mfxPosition);
            GLES30.glEnableVertexAttribArray(mfxNormal);
            GLES30.glEnableVertexAttribArray(mfxTex);

            GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, mLandIB);
        }
        GLES30.glBindVertexArray(0);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    void buildCrateGeometryBuffers(){
        MeshData box = new MeshData();

        GeometryGenerator geoGen = new GeometryGenerator();
        geoGen.createBox(1.0f, 1.0f, 1.0f, box);

        int totalVertexCount = box.vertices.size();

        FloatBuffer vertices = GLUtil.getCachedFloatBuffer(totalVertexCount * 8);

        for(int i = 0; i < box.vertices.size(); ++i)
        {
            Vertex v = box.vertices.get(i);
            vertices.put(v.positionX).put(v.positionY).put(v.positionZ);
            vertices.put(v.normalX).put(v.normalY).put(v.normalZ);
            vertices.put(v.texCX).put(v.texCY);
        }
        vertices.flip();

        mBoxVB = GLES.glGenBuffers();
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mBoxVB);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, totalVertexCount * 8 * 4, vertices, GLES30.GL_STATIC_DRAW);

        //
        // Pack the indices of all the meshes into one index buffer.
        //
        mBoxIB = GLES.glGenBuffers();
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, mBoxIB);
        GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER, 4 * box.indices.size(), GLUtil.wrap(box.indices.getData(), 0, box.indices.size()), GLES30.GL_STATIC_DRAW);

        mBoxVBO = GLES.glGenVertexArrays();
        GLES30.glBindVertexArray(mBoxVBO);
        {
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mBoxVB);
            GLES30.glVertexAttribPointer(mfxPosition, 3, GL11.GL_FLOAT, false, 8 * 4, 0);
            GLES30.glVertexAttribPointer(mfxNormal, 3, GL11.GL_FLOAT, false, 8 * 4, 3 * 4);
            GLES30.glVertexAttribPointer(mfxTex, 2, GL11.GL_FLOAT, false, 8 * 4, 6 * 4);

            // Notice, must enable the attribute location in VBO creation block.
            GLES30.glEnableVertexAttribArray(mfxPosition);
            GLES30.glEnableVertexAttribArray(mfxNormal);
            GLES30.glEnableVertexAttribArray(mfxTex);

            GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, mBoxIB);
        }
        GLES30.glBindVertexArray(0);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    private void makeTextureProperties(){
        GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        // Some driver doesn't support anisotropy filter
//        GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, 4);
    }
}
