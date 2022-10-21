package jet.learning.opengl.gtao;

import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLES32;

import com.nvidia.developer.opengl.app.NvInputTransformer;
import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.GLUtil;
import com.nvidia.developer.opengl.utils.NvGLSLProgram;
import com.nvidia.developer.opengl.utils.NvUtils;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import jet.learning.opengl.common.Disposeable;
import jet.learning.opengl.common.Texture2D;
import jet.learning.opengl.common.Texture2DDesc;
import jet.learning.opengl.common.TextureUtils;


public class CubeScene {
    private static final int VERTEX_POS = 0;
    private static final int VERTEX_NORMAL = 1;
    private static final int VERTEX_COLOR = 2;
    private static final int INSTANCE_OFFSET = 3;
    private static final int INSTANCE_SCALE = 4;

    private static final int UBO_SCENE = 0;

    static final int        grid = 16;
    static final float      globalscale = 16.0f;

    private int m_FrameBuffer;
    private Texture2D m_SceneColorTex;
    private Texture2D m_SceneDepthTex;
    private Texture2D m_SceneDepthReadTex;
    private NvGLSLProgram m_Program;
    private NvGLSLProgram m_ApplyAO;
    private final Buffers m_Buffers = new Buffers();
    private final Projection m_Projection = new Projection();
    private final SceneData m_SceneUbo = new SceneData();

    private NvInputTransformer m_Transformer;
    private int m_SampleCount = 1; // MSAA count.
    private int m_SampleLastCount = 1; // MSAA count.

    public CubeScene(NvInputTransformer transformer){
        m_Transformer = transformer;
    }

    public void onCreate() {
        m_Transformer.setTranslationVec(new Vector3f(0.0f, 0.0f, -4.2f));
        m_Transformer.setRotationVec(new Vector3f(NvUtils.PI*0.35f, 0.0f, 0.0f));

        System.out.println("CubeScene::OnCreate");

        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        m_FrameBuffer = GLES.glGenFramebuffers();

        // init scene
        {
            String root = "Scenes/CubeScene/shaders/";
            m_Program = NvGLSLProgram.createFromFiles(root + "scene.vert", root + "scene.frag");
            GLES.checkGLError();
            initScene();
        }

        m_ApplyAO = NvGLSLProgram.createFromFiles("shaders/Quad_VS.vert", "labs/GTAO/shaders/ApplyAO.frag");
    }

    int cube_instance_count;

    void initScene(){
        final int LEVELS = 4;
        initCube();

//		cubeVAO = ModelGenerator.genCube(1, true, false, false).genVAO();
        //   color  | translate | scale
        //  R,G,B,A |   X,Y,Z   | X,Y,Z
        cube_instance_count = grid * grid * LEVELS;
        FloatBuffer buf = GLUtil.getCachedFloatBuffer(cube_instance_count * (4 + 3 + 3));

        final Vector4f color = new Vector4f();
        final Vector2f posxy = new Vector2f();
        final Vector3f pos = new Vector3f();
        final Vector3f size = new Vector3f();
        for (int i = 0; i < grid * grid; i++){
            color.x = NvUtils.random(0.75f, 1);
            color.y = NvUtils.random(0.75f, 1);
            color.z = NvUtils.random(0.75f, 1);
            color.w = 1;

            posxy.set(i % grid, i / grid);

            float depth = (float) (Math.sin(posxy.x*0.1f) * Math.cos(posxy.y*0.1f) * 2.0f);

            for (int l = 0; l < LEVELS; l++){
                pos.set(posxy.x, posxy.y, depth);

                float scale = globalscale * 0.5f/(grid);
                if (l != 0){
                    scale *= Math.pow(0.9f,l);
                    scale *= NvUtils.random(0.5f, 1.0f);
                }

//	          vec3 size = vec3(scale);
                size.set(scale, scale, scale);

                size.z *= NvUtils.random(0.3f, 3);  //frand()*1.0f+1.0f;
                if (l != 0){
                    size.z *= Math.pow(0.7f,l);
                }

                pos.x -= grid/2;
                pos.y -= grid/2;
                pos.scale(globalscale/grid);

                depth += size.z;

                pos.z = depth;

                color.store(buf);
                pos.store(buf);
                size.store(buf);

                depth += size.z;
            }
        }
        buf.flip();

        m_Buffers.instance_vbo = GLES.glGenBuffers();
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, m_Buffers.instance_vbo);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, buf.remaining(), buf, GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES.checkGLError();

        // build scene VAO
        m_Buffers.scene_vao = GLES.glGenVertexArray();
        GLES30.glBindVertexArray(m_Buffers.scene_vao);
        {
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, m_Buffers.scene_vbo);
            GLES30.glVertexAttribPointer(VERTEX_POS, 3, GLES30.GL_FLOAT, false, 0, 0);
            GLES30.glEnableVertexAttribArray(VERTEX_POS);

            GLES30.glVertexAttribPointer(VERTEX_NORMAL, 3, GLES30.GL_FLOAT, false, 0, 12 * 6 * 4);
            GLES30.glEnableVertexAttribArray(VERTEX_NORMAL);

            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, m_Buffers.instance_vbo);
            GLES30.glVertexAttribPointer(VERTEX_COLOR, 4, GLES30.GL_FLOAT, false, (4 + 3 + 3) * 4, 0);
            GLES30.glVertexAttribPointer(INSTANCE_OFFSET, 3, GLES30.GL_FLOAT, false, (4 + 3 + 3) * 4, 16);
            GLES30.glVertexAttribPointer(INSTANCE_SCALE, 3, GLES30.GL_FLOAT, false, (4 + 3 + 3) * 4, 28);

            GLES30.glEnableVertexAttribArray(VERTEX_COLOR);
            GLES30.glEnableVertexAttribArray(INSTANCE_OFFSET);
            GLES30.glEnableVertexAttribArray(INSTANCE_SCALE);

            GLES30.glVertexAttribDivisor(VERTEX_COLOR, 1);
            GLES30.glVertexAttribDivisor(INSTANCE_OFFSET, 1);
            GLES30.glVertexAttribDivisor(INSTANCE_SCALE, 1);

            GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, m_Buffers.scene_ibo);
        }
        GLES30.glBindVertexArray(0);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0);

        {  // Scene UBO
            m_Buffers.scene_ubo = GLES.glGenBuffers();
            GLES30.glBindBuffer(GLES30.GL_UNIFORM_BUFFER, m_Buffers.scene_ubo);
            GLES30.glBufferData(GLES30.GL_UNIFORM_BUFFER, SceneData.SIZE, null, GLES30.GL_DYNAMIC_DRAW);
            GLES30.glBindBuffer(GLES30.GL_UNIFORM_BUFFER, 0);
        }
        GLES.checkGLError();
    }

    int cube_element_count;
    void initCube(){
        float side2 = 2 * 0.5f;
        float[] v = {
                // Front
                -side2, -side2, side2,
                side2, -side2, side2,
                side2,  side2, side2,
                -side2,  side2, side2,
                // Right
                side2, -side2, side2,
                side2, -side2, -side2,
                side2,  side2, -side2,
                side2,  side2, side2,
                // Back
                -side2, -side2, -side2,
                -side2,  side2, -side2,
                side2,  side2, -side2,
                side2, -side2, -side2,
                // Left
                -side2, -side2, side2,
                -side2,  side2, side2,
                -side2,  side2, -side2,
                -side2, -side2, -side2,
                // Bottom
                -side2, -side2, side2,
                -side2, -side2, -side2,
                side2, -side2, -side2,
                side2, -side2, side2,
                // Top
                -side2,  side2, side2,
                side2,  side2, side2,
                side2,  side2, -side2,
                -side2,  side2, -side2
        };

        /** cube normals */
        final float[] cube_normal = {
                // Front
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,
                // Right
                1.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f,
                // Back
                0.0f, 0.0f, -1.0f,
                0.0f, 0.0f, -1.0f,
                0.0f, 0.0f, -1.0f,
                0.0f, 0.0f, -1.0f,
                // Left
                -1.0f, 0.0f, 0.0f,
                -1.0f, 0.0f, 0.0f,
                -1.0f, 0.0f, 0.0f,
                -1.0f, 0.0f, 0.0f,
                // Bottom
                0.0f, -1.0f, 0.0f,
                0.0f, -1.0f, 0.0f,
                0.0f, -1.0f, 0.0f,
                0.0f, -1.0f, 0.0f,
                // Top
                0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f
        };

        /** Cube indices*/
        final byte cube_indices[] = {
                0,1,2,0,2,3,
                4,5,6,4,6,7,
                8,9,10,8,10,11,
                12,13,14,12,14,15,
                16,17,18,16,18,19,
                20,21,22,20,22,23
        };

        FloatBuffer buf = GLUtil.getCachedFloatBuffer(v.length + cube_normal.length);
        buf.put(v).put(cube_normal).flip();

        m_Buffers.scene_vbo = GLES.glGenBuffers();
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, m_Buffers.scene_vbo);
        GLES.glBufferData(GLES30.GL_ARRAY_BUFFER, buf, GLES30.GL_STATIC_DRAW);

        m_Buffers.scene_ibo = GLES.glGenBuffers();
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, m_Buffers.scene_ibo);
        GLES.glBufferData(GLES30.GL_ARRAY_BUFFER, GLUtil.wrap(cube_indices), GLES30.GL_STATIC_DRAW);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
        GLES.checkGLError();
        cube_element_count = cube_indices.length;
    }

    public void onResize(int width, int height) {
        if(width == 0 || height == 0)
            return;

        GLES30.glViewport(0, 0, width, height);

        if(m_SceneColorTex == null || m_SceneColorTex.getWidth() != width || m_SceneColorTex.getHeight() != height){
            initFramebuffers(width, height, m_SampleCount);
        }
    }
    public boolean isMultiSample(){ return m_SceneColorTex.getSampleCount() > 1;}

    public int getWidth() { return m_SceneColorTex != null ? m_SceneColorTex.getWidth() : 0;}
    public int getHeight() { return m_SceneColorTex != null ? m_SceneColorTex.getHeight() : 0;}

    public void resoveMultisampleTexture(){
        GLES30.glBindFramebuffer(GLES30.GL_READ_FRAMEBUFFER, m_FrameBuffer);
        GLES32.glReadBuffer(GLES30.GL_COLOR_ATTACHMENT0);
        GLES30.glBindFramebuffer(GLES30.GL_DRAW_FRAMEBUFFER, 0);
        GLES30.glBlitFramebuffer(0,0,m_SceneColorTex.getWidth(),m_SceneColorTex.getHeight(),
                0,0,m_SceneColorTex.getWidth(),m_SceneColorTex.getHeight(),
                GLES30.GL_COLOR_BUFFER_BIT, GLES30.GL_NEAREST);
        GLES30.glBindFramebuffer(GLES30.GL_READ_FRAMEBUFFER, 0);
    }

    void initFramebuffers(int width, int height, int sampples){
        if(m_SceneColorTex != null){
            m_SceneColorTex.dispose();
            m_SceneDepthTex.dispose();
        }

        Texture2DDesc desc = new Texture2DDesc();
        desc.width = width;
        desc.height = height;
        desc.format = GLES30.GL_RGBA32F;
        desc.mipLevels = 1;
        desc.arraySize = 1;
        desc.sampleCount = sampples;

        m_SceneColorTex = TextureUtils.createTexture2D(desc, null);
        m_SceneColorTex.setName("SceneColor");

//        desc.format = GLES30.GL_R32F;
//        m_SceneDepthReadTex = TextureUtils.createTexture2D(desc, null);
//        m_SceneDepthReadTex.setName("SceneDepth");

        desc.format = GLES30.GL_DEPTH24_STENCIL8;
        m_SceneDepthTex = TextureUtils.createTexture2D(desc, null);
        m_SceneDepthTex.setName("SceneDepth");


        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, m_FrameBuffer);
        {
            GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, m_SceneColorTex.getTarget(),m_SceneColorTex.getTexture(), 0);
//            GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT1, m_SceneDepthReadTex.getTarget(),m_SceneDepthReadTex.getTexture(), 0);
            GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_STENCIL_ATTACHMENT, m_SceneDepthTex.getTarget(),m_SceneDepthTex.getTexture(), 0);

            int[] drawbuffers = {GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_COLOR_ATTACHMENT1};
//            GLES32.glDrawBuffers(2, drawbuffers,0);
        }
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
    }

    public void applyAO(Texture2D textureAO){
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER,m_FrameBuffer);
        int width = m_SceneColorTex.getWidth();
        int height = m_SceneColorTex.getHeight();
        {
            GLES30.glViewport(0, 0, width, height);
            GLES30.glDisable(GLES30.GL_DEPTH_TEST);

            GLES30.glEnable(GLES30.GL_BLEND);
            GLES30.glBlendEquation(GLES30.GL_FUNC_ADD);
            GLES30.glBlendFunc(GLES30.GL_DST_COLOR, GLES30.GL_ZERO);

            m_ApplyAO.enable();
            GLES.glBindTextureUnit(0, textureAO);

            GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3);
            GLES.glBindTextureUnit(0, null);
            GLES30.glDisable(GLES30.GL_BLEND);
        }

        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER,0);
    }

    public void blitTexToScreen(Texture2D texture){
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER,0);
        int width = m_SceneColorTex.getWidth();
        int height = m_SceneColorTex.getHeight();
        {
            GLES30.glViewport(0, 0, width, height);
            GLES30.glDisable(GLES30.GL_DEPTH_TEST);
            GLES30.glDisable(GLES30.GL_BLEND);

            m_ApplyAO.enable();
            GLES.glBindTextureUnit(0, texture);

            GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3);
            GLES.glBindTextureUnit(0, null);
        }
    }

    public void draw() {
        GLES.checkGLError();
        GLES30.glUseProgram(0);
        int width   = m_Transformer.getScreenWidth();
        int height  = m_Transformer.getScreenHeight();

        m_Projection.ortho       = /*m_control.m_sceneOrtho*/ false;
        m_Projection.orthoheight = /*m_control.m_sceneOrthoZoom*/1;
        m_Projection.update(width,height);

        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER,m_FrameBuffer);
        {
//	      NV_PROFILE_SECTION("Scene");
            GLES30.glViewport(0, 0, width, height);
            GLES30.glClearColor(0.2f, 0.2f, 0.2f, 0.0f);
            GLES30.glClearDepthf(1.0f);
            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

            GLES30.glEnable(GLES30.GL_DEPTH_TEST);
            GLES30.glColorMask(true, true, true, true);

//	      sceneUbo.viewport = uvec2(width,height);
            m_SceneUbo.viewportX = width;
            m_SceneUbo.viewportY = height;

//	      nv_math::mat4 view = m_control.m_viewMatrix;
            final Matrix4f modelView = m_SceneUbo.viewMatrix;
            m_Transformer.getModelViewMat(modelView);

//	      sceneUbo.viewProjMatrix = projection.matrix * view;
            Matrix4f.mul(m_Projection.matrix, modelView, m_SceneUbo.viewProjMatrix);
//	      sceneUbo.viewMatrix = view;
//	      sceneUbo.viewMatrixIT = nv_math::transpose(nv_math::invert(view));
            Matrix4f.invert(modelView, m_SceneUbo.viewMatrixIT);

//	      glUseProgram(progManager.get(programs.draw_scene));

            m_Program.enable();
//	      m_Program.setVSShader(m_SceneVS);
//	      m_Program.setPSShader(m_ScenePS);

            GLES30.glBindBufferBase(GLES30.GL_UNIFORM_BUFFER, UBO_SCENE, m_Buffers.scene_ubo);
            ByteBuffer buf = GLUtil.getCachedByteBuffer(SceneData.SIZE);
            m_SceneUbo.store(buf);
            buf.flip();
            GLES30.glBufferSubData(GLES30.GL_UNIFORM_BUFFER,0, buf.remaining(), buf);

            GLES30.glBindVertexArray(m_Buffers.scene_vao);
            GLES30.glDrawElementsInstanced(GLES30.GL_TRIANGLES,cube_element_count, GLES30.GL_UNSIGNED_BYTE,0,  cube_instance_count);
            GLES30.glBindVertexArray(0);
        }

        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER,0);
    }

    public void getViewProjMatrix(Matrix4f out){ Matrix4f.mul(m_Projection.matrix, m_SceneUbo.viewMatrix, out);}
    public Texture2D getSceneColor() {return m_SceneColorTex;}
    public Texture2D getSceneDepth() {return m_SceneColorTex;}
    public Matrix4f getProjMat()    { return m_Projection.matrix;}
    public Matrix4f getViewMat(){ return m_SceneUbo.viewMatrix;}
    public float getSceneNearPlane() { return m_Projection.nearplane;}
    public float getSceneFarPlane() { return m_Projection.farplane;}
    public float getFovInRadian()   { return (float)Math.toRadians(m_Projection.fov);}

    public void onDestroy() {
        if(m_FrameBuffer != 0){
            GLES.glDeleteFramebuffers(m_FrameBuffer);
            m_FrameBuffer = 0;
        }

        if(m_Program != null){
            m_Program.dispose();
            m_Program = null;
        }

        m_Buffers.dispose();
    }

    private final class Buffers implements Disposeable {
        int scene_vbo,
                scene_ibo,
                scene_ubo,
                scene_vao,
                hbao_ubo,
                instance_vbo;

        @Override
        public void dispose() {
            if(scene_vbo != 0){
                GLES.glDeleteBuffers(scene_ibo);
                GLES.glDeleteBuffers(scene_ibo);
                GLES.glDeleteBuffers(scene_ubo);
                GLES.glDeleteBuffers(hbao_ubo);
                GLES.glDeleteBuffers(instance_vbo);

                GLES.glDeleteVertexArrays(scene_vao);
            }

            scene_vbo = 0;
            scene_ibo = 0;
            scene_ubo = 0;
            hbao_ubo = 0;
            instance_vbo = 0;
            scene_vao = 0;
        }
    }

    private final class SceneData{
        static final int SIZE = (16 * 3 + 4) * 4;

        final Matrix4f viewProjMatrix = new Matrix4f();
        final Matrix4f viewMatrix     = new Matrix4f();
        final Matrix4f viewMatrixIT   = new Matrix4f();

        int viewportX;
        int viewportY;
        int x0,x1;  // pad

        void store(ByteBuffer buf){
            viewProjMatrix.store(buf);
            viewMatrix.store(buf);
            viewMatrixIT.store(buf);
            buf.putInt(viewportX);
            buf.putInt(viewportY);
            buf.putInt(x0);
            buf.putInt(x1);
        }
    }

    private final class Projection{
        float nearplane = 0.1f;
        float farplane = 100.0f;
        float fov = 45.f;
        float orthoheight = 1.0f;
        boolean  ortho = false;
        final Matrix4f matrix = new Matrix4f();

        void update(int width, int height){
            float aspect = (float)width/height;
            if(ortho){
                Matrix4f.ortho(-orthoheight*0.5f*aspect, orthoheight*0.5f*aspect, -orthoheight*0.5f, orthoheight*0.5f, nearplane, farplane, matrix);
            }else{
                Matrix4f.perspective(fov, aspect, nearplane, farplane, matrix);
            }
        }
    }
}
