package jet.learning.opengl.samples;

import android.opengl.GLES20;

import com.nvidia.developer.opengl.app.NvSampleApp;
import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.GLUtil;
import com.nvidia.developer.opengl.utils.Glut;
import com.nvidia.developer.opengl.utils.NvGLSLProgram;

import org.lwjgl.util.vector.Matrix4f;

/**
 * Created by mazhen'gui on 2018/2/10.
 */
public final class ContourDemo extends NvSampleApp {
    // Shader-related variables
    private int textureID;
    private int quadVBO;

    NvGLSLProgram programObj;
    NvGLSLProgram[] programObjs = new NvGLSLProgram[4];

    private final Matrix4f mProj = new Matrix4f();
    private final Matrix4f mView = new Matrix4f();


    @Override
    protected void initRendering() {
        // Load textures
        // The special shader used to render this texture performs its own minification
        // and magnification. Specify nearest neighbor sampling to avoid trampling
        // over the distance values split over two channels as 8.8 fixed-point data.
        textureID = Glut.loadTextureFromFile("textures/disttex.png", GLES20.GL_NEAREST, GLES20.GL_CLAMP_TO_EDGE);
        int texw = 512;
        int texh = 512;

        // Create, load and compile the shader programs
        programObjs[0] = NvGLSLProgram.createFromFiles("shaders/vertex.glsl", "shaders/fragment1.glsl");
        programObjs[1] = NvGLSLProgram.createFromFiles("shaders/vertex.glsl", "shaders/fragment2.glsl");
        programObjs[2] = NvGLSLProgram.createFromFiles("shaders/vertex.glsl", "shaders/fragment3.glsl");
        programObjs[3] = NvGLSLProgram.createFromFiles("shaders/vertex.glsl", "shaders/fragment4.glsl");

        for(int i = 0; i < programObjs.length; i++){
            programObjs[i].enable();
            programObjs[i].setUniform1i("disttex", 0);
            programObjs[i].setUniform1f("oneu", 1.f/texw);
            programObjs[i].setUniform1f("onev", 1.f/texh);
            programObjs[i].setUniform1f("texw", texw);
            programObjs[i].setUniform1f("texh", texh);
        }
        programObj = programObjs[0];

        float size = 5;
        float[] verts = {
             -size, -size, 0, 0,0,
             +size, -size, 0, 1,0,
             -size, +size, 0, 0,1,
             +size, +size, 0, 1,1,
        };
        quadVBO = GLES.glGenBuffers();
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, quadVBO);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, verts.length * 4, GLUtil.wrap(verts), GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        m_transformer.setTranslation(0,0, -10);
    }

    @Override
    protected void draw() {
        m_transformer.getModelViewMat(mView);

        GLES20.glClearColor(0,0,0,0);
        GLES20.glClearDepthf(1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT|GLES20.GL_DEPTH_BUFFER_BIT);

        Matrix4f.mul(mProj, mView, mView);

        programObj.enable();
        programObj.setUniformMatrix4("g_MVP", mView);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, quadVBO);
        int position = programObj.getAttribLocation("In_Position");
        int texcoord = programObj.getAttribLocation("In_Texcoord");

        GLES20.glEnableVertexAttribArray(position);
        GLES20.glVertexAttribPointer(position, 3, GLES20.GL_FLOAT, false, 20, 0 );
        GLES20.glEnableVertexAttribArray(texcoord);
        GLES20.glVertexAttribPointer(texcoord, 2, GLES20.GL_FLOAT, false, 20, 12 );

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(position);
        GLES20.glDisableVertexAttribArray(texcoord);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

    @Override
    protected void reshape(int width, int height) {
        Matrix4f.perspective(60, (float)width/height, 0.1f, 1000.0f, mProj);
    }


}
