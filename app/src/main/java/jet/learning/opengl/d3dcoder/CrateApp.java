package jet.learning.opengl.d3dcoder;

import android.opengl.GLES20;
import android.opengl.GLES30;

import com.nvidia.developer.opengl.app.NvSampleApp;
import com.nvidia.developer.opengl.utils.BufferUtils;
import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.GLUtil;
import com.nvidia.developer.opengl.utils.NvAssetLoader;
import com.nvidia.developer.opengl.utils.NvGLSLProgram;
import com.nvidia.developer.opengl.utils.NvImage;
import com.nvidia.developer.opengl.utils.NvPackedColor;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.ReadableVector3f;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL11;

import jet.learning.opengl.common.DirectionalLight;
import jet.learning.opengl.common.GeometryGenerator;
import jet.learning.opengl.common.Material;
import jet.learning.opengl.common.MeshData;
import jet.learning.opengl.common.UniformLights;
import jet.learning.opengl.common.UniformMatrix;
import jet.learning.opengl.common.Vertex;

/**
 * Created by mazhen'gui on 2017/4/7.
 */

public class CrateApp extends NvSampleApp {
    final UniformMatrix mMatrix = new UniformMatrix();
    final UniformLights mLights = new UniformLights();

    int mBoxVB;
    int mBoxIB;
    int mBoxVBO;

    //	ID3D11ShaderResourceView* mDiffuseMapSRV;
    int mDiffuseMapSRV;

    int mProgram;
    int mfxPosition = -1;
    int mfxNormal = -1;
    int mfxTex = -1;

    final DirectionalLight[] mDirLights = mLights.gDirLights;
    final Material mBoxMat = new Material();

    final Matrix4f mTexTransform = new Matrix4f();
    final Matrix4f mBoxWorld = new Matrix4f();

    final Matrix4f mView = new Matrix4f();
    final Matrix4f mProj = new Matrix4f();
    final Matrix4f mProjView = new Matrix4f();


    int mBoxVertexOffset;
    int mBoxIndexOffset;
    int mBoxIndexCount;

    @Override
    protected void initRendering() {
        setTitle("Crate Demo");

        mDirLights[0].ambient  .set(0.3f, 0.3f, 0.3f, 1.0f);
        mDirLights[0].diffuse  .set(0.8f, 0.8f, 0.8f, 1.0f);
        mDirLights[0].specular .set(0.6f, 0.6f, 0.6f, 16.0f);
        mDirLights[0].direction .set(0.707f, -0.707f, 0.0f);

        mDirLights[1].ambient  .set(0.2f, 0.2f, 0.2f, 1.0f);
        mDirLights[1].diffuse  .set(1.4f, 1.4f, 1.4f, 1.0f);
        mDirLights[1].specular .set(0.3f, 0.3f, 0.3f, 16.0f);
        mDirLights[1].direction .set(-0.707f, 0.0f, 0.707f);

        mBoxMat.ambient  .set(0.5f, 0.5f, 0.5f, 1.0f);
        mBoxMat.diffuse  .set(1.0f, 1.0f, 1.0f, 1.0f);
        mBoxMat.specular .set(0.6f, 0.6f, 0.6f, 16.0f);

        mLights.gLightCount = 0;

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
        GLES.checkGLError();

        m_transformer.setTranslation(0, 0, -2.5f);
    }

    void buildGeometryBuffers(){
        MeshData box = new MeshData();

        GeometryGenerator geoGen = new GeometryGenerator();
        geoGen.createBox(1.0f, 1.0f, 1.0f, box);

        // Cache the vertex offsets to each object in the concatenated vertex buffer.
//		mBoxVertexOffset      = 0;

        // Cache the index count of each object.
        mBoxIndexCount      = box.indices.size();
        System.out.println("mBoxIndexCount = " + mBoxIndexCount);

        // Cache the starting index for each object in the concatenated index buffer.
        mBoxIndexOffset      = 0;

        int totalVertexCount = box.vertices.size();

//		int totalIndexCount = mBoxIndexCount;

        //
        // Extract the vertex elements we are interested in and pack the
        // vertices of all the meshes into one vertex buffer.
        //

//		std::vector<Vertex::Basic32> vertices(totalVertexCount);
        FloatBuffer vertices = BufferUtils.createFloatBuffer(totalVertexCount * 8);

//		UINT k = 0;
        for(int i = 0; i < box.vertices.size(); ++i)
        {
//			vertices[k].Pos    = box.Vertices[i].Position;
//			vertices[k].Normal = box.Vertices[i].Normal;
//			vertices[k].Tex    = box.Vertices[i].TexC;

            Vertex v = box.vertices.get(i);
            vertices.put(v.positionX).put(v.positionY).put(v.positionZ);  // Position
            vertices.put(v.normalX).put(v.normalY).put(v.normalZ);        // Normal
            vertices.put(v.texCX).put(v.texCY);                           // Texturecoord
        }
        vertices.flip();


        mBoxVB = GLES.glGenBuffers();
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mBoxVB);
        GLES.glBufferData(GLES20.GL_ARRAY_BUFFER, vertices, GLES20.GL_STATIC_DRAW);

        mBoxIB = GLES.glGenBuffers();
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mBoxIB);
        GLES.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, GLUtil.wrap(box.indices.getData(), 0, box.indices.size()), GLES20.GL_STATIC_DRAW);

        mBoxVBO = GLES.glGenVertexArrays();
        GLES30.glBindVertexArray(mBoxVBO);
        {
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mBoxVB);
            GLES30.glVertexAttribPointer(mfxPosition, 3, GL11.GL_FLOAT, false, 8 * 4, 0);
            GLES30.glVertexAttribPointer(mfxNormal, 3, GL11.GL_FLOAT, false, 8 * 4, 3 * 4);
            GLES30.glVertexAttribPointer(mfxTex, 2, GL11.GL_FLOAT, false, 8 * 4, 6 * 4);

            GLES30.glEnableVertexAttribArray(mfxPosition);
            GLES30.glEnableVertexAttribArray(mfxNormal);
            GLES30.glEnableVertexAttribArray(mfxTex);

            GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, mBoxIB);
        }
        GLES30.glBindVertexArray(0);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    @Override
    protected void reshape(int width, int height) {
        Matrix4f.perspective((float)Math.toDegrees(0.25 * PI), (float)width/height, 1.0f, 1000.0f, mProj);
    }

    @Override
    protected void draw() {
        ReadableVector3f color = NvPackedColor.LIGHT_STEEL_BLUE;
        GLES20.glClearColor(color.getX(), color.getY(), color.getZ(), 1);
        GLES20.glClear(GL11.GL_COLOR_BUFFER_BIT| GL11.GL_DEPTH_BUFFER_BIT);
        GLES20.glEnable(GL11.GL_DEPTH_TEST);

        mLights.gEyePosW.set(m_transformer.getTranslationVec());
        mLights.gEyePosW.scale(-1);
        GLES20.glUseProgram(mProgram);
        mLights.apply();
        m_transformer.getModelViewMat(mView);
//        Matrix4f.lookAt(2.5f,0,0, 0,0,0, 0,1,0, mView);
        Matrix4f.mul(mProj, mView, mProjView);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GL11.GL_TEXTURE_2D, mDiffuseMapSRV);

        // Draw the grid
        Matrix4f world = mBoxWorld;
        mMatrix.gWorld.load(world);
        mMatrix.gTexTransform.load(mTexTransform);
        Matrix4f.invert(world, mMatrix.gWorldInvTranspose).transpose();
        Matrix4f.mul(mProjView, world, mMatrix.gWorldViewProj);
        mMatrix.gMaterial.set(mBoxMat);
        mMatrix.apply();
        GLES.checkGLError();
        GLES30.glBindVertexArray(mBoxVBO);
//
        GLES.checkGLError();
        GLES30.glDrawElements(GL11.GL_TRIANGLES, mBoxIndexCount, GL11.GL_UNSIGNED_SHORT, mBoxIndexOffset);
        GLES.checkGLError();
        GLES30.glBindVertexArray(0);
    }

    void buildFX(){
        StringBuilder vertexString = NvAssetLoader.readText("d3dcoder/basic.glvs");
        StringBuilder fragmentString = NvAssetLoader.readText("d3dcoder/basic.glfs");
        String lightHelpString = NvAssetLoader.readText("d3dcoder/LightHelper.glsl").toString();

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

        int useTex  = GLES20.glGetUniformLocation(mProgram, "gUseTexure");
        int alpha   = GLES20.glGetUniformLocation(mProgram, "gAlphaClip");
        int usefog  = GLES20.glGetUniformLocation(mProgram, "gFogEnabled");
        int useRef  = GLES20.glGetUniformLocation(mProgram, "gReflectionEnabled");
        int useSM   = GLES20.glGetUniformLocation(mProgram, "gUseShadowMap");

        int texSamp = GLES20.glGetUniformLocation(mProgram, "gDiffuseMap");
        int cubeSam = GLES20.glGetUniformLocation(mProgram, "gCubeMap");
        int shadow  = GLES20.glGetUniformLocation(mProgram, "gShadowMap");

        mMatrix.init(mProgram);
        mLights.init(mProgram);

        GLES20.glUseProgram(mProgram);
        GLES20.glUniform1i(texSamp, 0);
        GLES20.glUniform1i(cubeSam, 1);
        GLES20.glUniform1i(shadow, 2);

        GLES20.glUniform1i(useTex, 1);
        GLES20.glUniform1i(alpha, 0);
        GLES20.glUniform1i(usefog, 0);
        GLES20.glUniform1i(useRef, 0);
        GLES20.glUniform1i(useSM, 0);
    }
}
