package jet.learning.opengl.samples;

import android.graphics.Bitmap;
import android.opengl.GLES11;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.nvidia.developer.opengl.app.NvSampleApp;
import com.nvidia.developer.opengl.utils.AttribBinder;
import com.nvidia.developer.opengl.utils.AttribBindingTask;
import com.nvidia.developer.opengl.utils.BufferUtils;
import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.GLUtil;
import com.nvidia.developer.opengl.utils.Glut;
import com.nvidia.developer.opengl.utils.NvGLSLProgram;
import com.nvidia.developer.opengl.utils.NvShapes;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.ReadableVector3f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL11;

import jet.learning.opengl.common.FrameBufferBuilder;
import jet.learning.opengl.common.FrameBufferObject;
import jet.learning.opengl.common.SimpleLightProgram;
import jet.learning.opengl.common.SimpleTextureProgram;
import jet.learning.opengl.common.TextureInfo;
import jet.learning.opengl.shapes.DrawMode;
import jet.learning.opengl.shapes.GLVAO;
import jet.learning.opengl.shapes.QuadricBuilder;
import jet.learning.opengl.shapes.QuadricMesh;
import jet.learning.opengl.shapes.QuadricSphere;

/**
 * Created by mazhen'gui on 2017/10/19.
 */

public class ShinySprites extends NvSampleApp {
    private static final int POSITION_LOC = 0;
    private static final int NORMAL_LOC = 1;
    private static final int TEXTURE_LOC = 2;

    private static final int TEX_SZ = 256;
    private static final int NUMSHINES = 400;
    private SimpleTextureProgram mQuadProgram;
    private SimpleLightProgram mLightProgram;

    float m_fTestConstant1 = 0;
    float m_fTestConstant2 = 3.7f;
    float fsize = 0.21f;
    float alpha = 0, beta = 0;

    CSSprite sprites;

    private final Vector4f light_pos = new Vector4f(5,5,10,1);
    private final Vector3f lightDiff = new Vector3f(1,1,1);
    private final Vector3f lightSpec = new Vector3f(0.9f,0.9f,0.9f);
    private final float shinefact = 50;

    private final Vector3f[] shines = new Vector3f[NUMSHINES];
    private final Matrix4f proj = new Matrix4f();
    private final Matrix4f view = new Matrix4f();
    private final Matrix4f model = new Matrix4f();
    private final Matrix4f modelView = new Matrix4f();
    private final Matrix4f modelViewProj = new Matrix4f();
    private final Matrix4f normalMat = new Matrix4f();
    private final Vector3f tempV3 = new Vector3f();
    private final SimpleLightProgram.LightParams lightParams = new SimpleLightProgram.LightParams();
    private GLVAO m_sphere;

    private boolean m_debug = false;
    private boolean m_s = true;
    private boolean m_o = true;
    private boolean m_q = false;
    private boolean m__ = true;
    private boolean m_w = false;

    @Override
    protected void initRendering() {
        for(int i = 0; i < shines.length; i++){
            shines[i] = new Vector3f();
        }

        setTitle("Shiny Sprites Demo");

        /*glClearColor(.25f, .25f, .25f, 1);
        glEnable(GL_LIGHTING);
        glEnable(GL_LIGHT0);
        glLightfv(GL_LIGHT0, GL_POSITION, light_pos);
        glLightfv(GL_LIGHT0, GL_DIFFUSE, lightDiff);
        glLightfv(GL_LIGHT0, GL_SPECULAR, lightSpec);
        glMaterialfv(GL_FRONT_AND_BACK, GL_SPECULAR, lightSpec);
        glMaterialf(GL_FRONT_AND_BACK, GL_SHININESS, shinefact);
        glColorMaterial(GL_FRONT_AND_BACK, GL_DIFFUSE);
        glEnable(GL_COLOR_MATERIAL);*/
        lightParams.lightPos.set(light_pos);
        lightParams.lightDiffuse.set(lightDiff);
        lightParams.lightSpecular.set(lightSpec);
        lightParams.materialSpecular.set(lightSpec);
        lightParams.materialSpecular.w = shinefact;
        lightParams.materialDiffuse.set(lightDiff);
        lightParams.eyePos.set(0,0,0);


//        cgSetErrorCallback(cgErrorCallback);

        /*bInitialized = sprites.init();
        if (!bInitialized)
        {
            printf("Initialisation failed: %s\n", sprites.get_error_msg());
            cleanExit(0);
        }*/
        sprites = new CSSprite(false);
        sprites.init();

	    final double RADIUS = 1.01;
        for(int i=0; i<NUMSHINES; i++)
        {
            float be = PI*(float)Math.random();
            float al = 2*PI*(float)Math.random();
            shines[i].set(  (float)(RADIUS*Math.cos(al)*Math.sin(be)),
                            (float)(RADIUS*Math.sin(al)),
                            (float)(RADIUS*Math.cos(al)*Math.cos(be)));
        }

        mQuadProgram = new SimpleTextureProgram(new AttribBindingTask(
                new AttribBinder(SimpleTextureProgram.POSITION_ATTRIB_NAME, POSITION_LOC),
                new AttribBinder(SimpleTextureProgram.TEXTURE_ATTRIB_NAME, TEXTURE_LOC)));

        mLightProgram = new SimpleLightProgram(true, new AttribBindingTask(
                new AttribBinder(SimpleLightProgram.POSITION_ATTRIB_NAME, POSITION_LOC),
                new AttribBinder(SimpleLightProgram.TEXTURE_ATTRIB_NAME, TEXTURE_LOC),
                new AttribBinder(SimpleLightProgram.NORMAL_ATTRIB_NAME, NORMAL_LOC)));

        QuadricBuilder builder = new QuadricBuilder();
        builder.setXSteps(30).setYSteps(30);
        builder.setDrawMode(DrawMode.FILL);
        builder.setCenterToOrigin(true);
        builder.setPostionLocation(POSITION_LOC);
        builder.setNormalLocation(NORMAL_LOC);
        builder.setTexCoordLocation(TEXTURE_LOC);
        builder.setFlag(true);  // For OpenGL ES 2.0 above

        m_sphere = new QuadricMesh(builder, new QuadricSphere(1)).getModel().genVAO(true);
        GLES.checkGLError();
    }

    private void updateTransform(){
        m_transformer.getModelViewMat(view);
        Matrix4f.mul(view, model, modelView);
        Matrix4f.mul(proj, modelView, modelViewProj);
    }

    @Override
    protected void draw() {
        model.setIdentity();
        model.m32 = -3;
        model.rotate(alpha, 0,1,0);
        model.rotate(beta, 1,0,0);

        updateTransform();
        Matrix4f.decompseRigidMatrix(view, lightParams.eyePos,null,null);

        //
        // First : occlusion pass
        //
        sprites.begin_occlusion(m_debug);
        {
            if(m_s){
                mQuadProgram.enable();
                mQuadProgram.setMatrix(modelViewProj);
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

                m_sphere.bind();
                m_sphere.draw(GLES20.GL_TRIANGLES);
                m_sphere.unbind();

                model.translate(1.5f,0,0);
                model.scale(0.5f);
                updateTransform();
                mQuadProgram.setMatrix(modelViewProj);
                m_sphere.bind();
                m_sphere.draw(GLES20.GL_TRIANGLES);
                m_sphere.unbind();
            }

            sprites.set_test_cst(m_fTestConstant1*PI*2, 0);
            sprites.begin();
            sprites.bind_light(light_pos, shinefact);
            for(int i=0; i< NUMSHINES; i++)
            {
                sprites.draw_sprite(shines[i], shines[i], 0.01f, 0.01f);
                /*glh::vec3f pos(shines[i]);
                pos *= 0.5;
                pos[0] += 1.5;*/
                Vector3f pos = tempV3;
                pos.set(shines[i]);
                pos.scale(0.5f);
                pos.x += 1.5f;

                sprites.draw_sprite(pos, shines[i], .01f, 0.01f);
            }
            sprites.end();
//            glPopMatrix();
        }
        sprites.end_occlusion(m_debug);
        GLES.checkGLError();

        if (!m_debug)
        {
            //
            // second : Render
            //
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            //
            // various settings
            //

//            glPolygonMode(GL_FRONT_AND_BACK, b['w'] ? GL_LINE : GL_FILL);

            GLES20.glDisable(GLES20.GL_CULL_FACE);

//            if(glActiveTextureARB)
            {
//                glActiveTextureARB( GL_TEXTURE1_ARB );
//                glDisable(GL_TEXTURE_2D);
                GLES20.glActiveTexture( GLES20.GL_TEXTURE0 );
            }
//            glEnable(GL_LIGHTING);
//            glEnable(GL_LIGHT0);
//            glMaterialf(GL_FRONT_AND_BACK, GL_SHININESS, shinefact);
            mLightProgram.enable();

            model.setIdentity();
            model.m32 = -3;
            model.rotate(alpha, 0,1,0);
            model.rotate(beta, 1,0,0);

            updateTransform();

            lightParams.model.load(model);
            lightParams.modelViewProj.load(modelViewProj);
            GLES.checkGLError();

            if(m_s)
            {
                /*glColor3f(0.5f,0.0f,0.2f);
                glutSolidSphere(1,30,30);*/
                lightParams.color.set(0.5f,0.0f,0.2f, 1.0f);
                mLightProgram.setLightParams(lightParams);
                m_sphere.bind();
                m_sphere.draw(GLES20.GL_TRIANGLES);
                m_sphere.unbind();

                /*glPushMatrix();
                glTranslatef(1.5,0,0);
                glColor3f(0.0f,0.2f,0.5f);
                glutSolidSphere(0.5,30,30);
                glPopMatrix();*/
                model.translate(1.5f,0,0);
                model.scale(0.5f);
                updateTransform();
                lightParams.model.load(model);
                lightParams.modelViewProj.load(modelViewProj);
                lightParams.color.set(0.0f,0.2f,0.5f, 1.0f);
                mLightProgram.setLightParams(lightParams);
                m_sphere.bind();
                m_sphere.draw(GLES20.GL_TRIANGLES);
                m_sphere.unbind();
                GLES.checkGLError();
            }

            sprites.set_test_cst(m_fTestConstant1*PI*2, 0);
            model.setIdentity();
            model.m32 = -3;
            model.rotate(alpha, 0,1,0);
            model.rotate(beta, 1,0,0);

            updateTransform();

            sprites.begin();
            sprites.bind_light(light_pos, shinefact);
            for(int i=0; i< NUMSHINES; i++)
            {
                sprites.draw_sprite(shines[i], shines[i], .05f, 0.35f);
                /*glh::vec3f pos(shines[i]);
                pos *= 0.5;
                pos[0] += 1.5;*/
                Vector3f pos = tempV3;
                pos.set(shines[i]);
                pos.scale(0.5f);
                pos.x += 1.5f;
                sprites.draw_sprite(pos, shines[i], .05f, 0.35f);
            }
            sprites.end();

            if(m_o)
                sprites.display_occlusion_map();
            //
            //----> Finish the scene
            //
//            glPopMatrix();
        }

        GLES.checkGLError();
        if(m__)
        {
            alpha += Math.toRadians(0.6);
            beta += Math.toRadians(0.2234);
        }
    }

    @Override
    protected void reshape(int width, int height) {
        Matrix4f.perspective(60, (float)width/height, 0.1f, 100.0f, proj);
        GLES20.glViewport(0,0,width, height);
    }

    private final class CSSprite{
        String			m_msg;			///< If there are some error messages
//        PBuffer 		*m_PBuffer;		///< pbuffer for this light
        FrameBufferObject m_PBuffer;
        boolean			m_inpbuffer;

        boolean m_bNoRenderTexture = true;
        boolean binit;

//        int dl_bind_vtxprg;
//        int dl_bind_vtxprg_pbuffer;
        int texture;
        int	m_occlusionmap;
        boolean m_bManaged;
        ShinySpriteRenderProgram renderProgram;
        int sprite_buffer;
        FloatBuffer bufferData;

        CSSprite(boolean managed){
            m_bManaged = managed;
        }

	    String get_error_msg() { return m_msg;}
        void set_test_cst(float f1, float f2){}
        boolean init(){
//            data_path media;
            String errors;
            int tex;

            if(binit)
                return true;
            //
            // Resource type for PNG and vertex progs. This is OPTIONAL. No conflict with linux...
            //
            /*
            long hModule = 0;
#ifdef WIN32
            hModule = (unsigned long)GetModuleHandle("cg_ShinySprites.dll");
            set_png_module_handle(hModule);
            set_png_module_restypename("PNG");
            set_module_handle(hModule);
            set_module_restypename("NV");
#endif*/
            free();

            //
            // check for extensions : some are need
            //
            /*if (!glh_init_extensions(EXTENSIONS))
            {
                error_msg += "need this extension(s): ";
                error_msg += glh_get_unsupported_extensions();
                return false;
            }*/
            //
            // check for extensions : some are optional
            //
            /*if (!glh_init_extensions("WGL_ARB_render_texture"))
                m_bNoRenderTexture = true;
            else
                m_bNoRenderTexture = false;

            glh_init_extensions("GL_ARB_multitexture");*/
            m_bNoRenderTexture = false;

            //
            // For Cg : Use GL_ARB_vertex_program extension if supported by video card
            //
            /*if (cgGLIsProfileSupported(CG_PROFILE_ARBVP1))
                Profile = CG_PROFILE_ARBVP1;
            else if (cgGLIsProfileSupported(CG_PROFILE_VP20))
                Profile = CG_PROFILE_VP20;
            else
            {
                error_msg = "Video card does not support GL_ARB_vertex_program or GL_NV_vertex_program.\n";
                return false;
            }*/

            //
            //----> create the pbuffer :
            //
            /*if(!m_PBuffer)
            {
                if(m_bNoRenderTexture)
                    m_PBuffer = new PBuffer("rgb depth");
                else
                    m_PBuffer = new PBuffer("rgb depth texture");
                m_PBuffer->Initialize(TEX_SZ, TEX_SZ, false, true);
            }*/
            if(m_PBuffer == null){
                FrameBufferBuilder builder = new FrameBufferBuilder();
                TextureInfo texture0 = builder.createColorTexture();
                texture0.setInternalFormat(GLES20.GL_RGB);
                texture0.setMagFilter(GL11.GL_LINEAR);
                texture0.setMinFilter(GL11.GL_LINEAR);
                texture0.setSWrap(GLES20.GL_CLAMP_TO_EDGE);
                texture0.setTWrap(GLES20.GL_CLAMP_TO_EDGE);
                builder.setWidth(TEX_SZ).setHeight(TEX_SZ);
                builder.getOrCreateDepthTexture().setInternalFormat(FrameBufferObject.FBO_DepthBufferType_RENDERTARGET);
                m_PBuffer = new FrameBufferObject(builder);
            }

            //
            //----> Initialise pbuffer stuff
            //
            /*m_PBuffer->Activate();
            glClearColor(0.5, 0, 1, 1.0);
            glEnable(GL_DEPTH_TEST); // need to set it. Not by default
            m_PBuffer->Deactivate();*/
            //
            //----> Create the depth map
            //
            /*glActiveTextureARB( GL_TEXTURE1_ARB );
            m_occlusionmap.enable();
            m_occlusionmap.bind();
            m_occlusionmap.parameter(GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            m_occlusionmap.parameter(GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            m_occlusionmap.parameter(GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
            m_occlusionmap.parameter(GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
            // If no render to texture, create a map of same size than the pbuffer
            if(m_bNoRenderTexture)
            {
                glTexImage2D(m_occlusionmap.target, 0, GL_RGB, TEX_SZ, TEX_SZ, 0,
                        GL_RGB, GL_UNSIGNED_BYTE, (const void *)NULL);
            }
            glActiveTextureARB( GL_TEXTURE0_ARB );*/
            //
            // load the default texture
            //
            /*texture.bind();
            glh::array2<glh::vec3ub> img;
            read_png_rgb("shine.png", img);
            make_rgb_texture( img, false);//true );
            texture.parameter(GL_TEXTURE_MIN_FILTER, GL_LINEAR );
            texture.parameter(GL_TEXTURE_MAG_FILTER, GL_LINEAR );*/
            texture = GLES.glGenTextures();
            GLES11.glBindTexture(GLES11.GL_TEXTURE_2D, texture);

            Bitmap bitmap = Glut.loadBitmapFromAssets("textures/shine.png");
            GLUtils.texImage2D(GLES11.GL_TEXTURE_2D, 0, bitmap, 0);
            GLES11.glTexParameteri(GLES11.GL_TEXTURE_2D, GLES11.GL_TEXTURE_MAG_FILTER, GLES11.GL_LINEAR);
            GLES11.glTexParameteri(GLES11.GL_TEXTURE_2D, GLES11.GL_TEXTURE_MIN_FILTER, GLES11.GL_LINEAR);
            GLES11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GLES11.GL_CLAMP_TO_EDGE);
            GLES11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GLES11.GL_CLAMP_TO_EDGE);
            bitmap.recycle();

            //
            // Cg Prog
            //
            renderProgram = new ShinySpriteRenderProgram();

            bufferData = BufferUtils.createFloatBuffer(12 * 4);
            sprite_buffer = GLES.glGenBuffers();
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, sprite_buffer);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, bufferData.remaining() * 4, bufferData, GLES20.GL_DYNAMIC_DRAW);
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

            // Load the program. Optionaly have a look in the resources
            /*char *cgProg = read_text_file("cg_ShinySprite/cg_ShinySprite.cg", "NV", hModule);
            if (!cgProg)
            {
                printf("Unable to load cg_ShinySprite.cg, exiting...\n");
                cleanExit(0);
            }

            Context = cgCreateContext();

            Program = cgCreateProgram(Context,
                    CG_SOURCE, cgProg,
                    Profile, NULL, NULL);

            cgGLLoadProgram(Program);

            Projection = cgGetNamedParameter(Program, "Projection");
            ModelView = cgGetNamedParameter(Program, "ModelView");
            ModelViewIT = cgGetNamedParameter(Program, "ModelViewIT");

            Light = cgGetNamedParameter(Program, "Light");
            Shininess = cgGetNamedParameter(Program, "Shininess");
            Slices = cgGetNamedParameter(Program, "Slices");

            Size = cgGetNamedParameter(Program, "IN.Size");

            CSTable = cgGetNamedParameter(Program, "CSTable");

            if (!Projection || !ModelView || !ModelViewIT || !Light || !Shininess || !Slices || !Size || !CSTable)
            {
                printf("Unable to retrieve vertex program parameters, exiting...\n");
                cleanExit(0);
            }

            //
            // create the parameters-binding display-list
            //
            dl_bind_vtxprg = glGenLists(1);
            glNewList(dl_bind_vtxprg, GL_COMPILE);
            if(glActiveTextureARB)
            {
                nvparse(
                        "!!RC1.0                                           \n"
                        "{ \n"
                        "	rgb { \n"
                        "		spare0 = tex1.rgb * tex0.rgb; \n"
                        "	} \n"
                        "} \n"
                        "out.rgb = spare0.rgb;                               \n"
                        "out.a = tex0.a;                                 \n", 0
                );
            }
            else
            {
                nvparse(
                        "!!RC1.0                                           \n"
                        "out.rgb = tex0.rgb;                               \n"
                        "out.a = tex0.a;                                 \n", 0
                );
            }
            for (errors = nvparse_get_errors(); *errors; errors++)
            {
                fprintf(stderr, *errors);
            }
            glEnable(GL_REGISTER_COMBINERS_NV);
            glDisable(GL_CULL_FACE);
            glEnable(GL_BLEND);
            glBlendFunc(GL_ONE, GL_ONE);
            if(glActiveTextureARB)
                glActiveTextureARB( GL_TEXTURE0_ARB );
            glEnable(GL_TEXTURE_2D);
            texture.bind();
            nvparse(
                    "!!TS1.0                                           \n"
                    "texture_2d();                                            \n"
                    "texture_2d();                                            \n"
                    "nop();                                            \n"
                    "nop();                                     \n", 0
            );
            for (errors = nvparse_get_errors(); *errors; errors++)
            {
                fprintf(stderr, *errors);
            }
            glEnable(GL_TEXTURE_SHADER_NV);
            glDepthMask(GL_FALSE);
            glDisable(GL_DEPTH_TEST);
            glEndList();
            //
            // create the parameters-binding display-list for pbuffer version
            //
            dl_bind_vtxprg_pbuffer = glGenLists(1);
            glNewList(dl_bind_vtxprg_pbuffer, GL_COMPILE);
            nvparse(
                    "!!RC1.0                                           \n"
                    "out.rgb = col0.rgb;                               \n"
                    "out.a = col0.a;                                 \n", 0
            );
            for (errors = nvparse_get_errors(); *errors; errors++)
            {
                fprintf(stderr, *errors);
            }
            glEnable(GL_REGISTER_COMBINERS_NV);
            glDisable(GL_CULL_FACE);
            glDisable(GL_BLEND);
            if(glActiveTextureARB)
            {
                glActiveTextureARB( GL_TEXTURE1_ARB );
                glDisable(GL_TEXTURE_2D);
                glActiveTextureARB( GL_TEXTURE0_ARB );
            }
            glDisable(GL_TEXTURE_2D);
            glDisable(GL_TEXTURE_SHADER_NV);
            glDepthMask(GL_TRUE);
            glEnable(GL_DEPTH_TEST);
            glEndList();*/
            return true;
        }
        void finish(){
            renderProgram.dispose();
            renderProgram = null;
        }
        void begin(){
            renderProgram.enable();
            if(m_inpbuffer)
            {
//                glCallList(dl_bind_vtxprg_pbuffer);  TODO
                GLES20.glDisable(GLES20.GL_BLEND);
                GLES20.glEnable(GLES20.GL_DEPTH_TEST);
                GLES20.glDepthMask(true);
                GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);  // unbind the texture
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);  // unbind the texture
                renderProgram.setDisableTex(true);
            }
            else
            {
//                glCallList(dl_bind_vtxprg);  TODO

                GLES20.glEnable(GLES20.GL_BLEND);
                GLES20.glBlendFuncSeparate(GLES20.GL_SRC_COLOR, GLES20.GL_ONE, GLES20.GL_ZERO, GLES20.GL_ONE);
                GLES20.glActiveTexture( GLES20.GL_TEXTURE0 );
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
                GLES20.glEnable(GLES20.GL_DEPTH_TEST);
                GLES20.glDepthMask(false);
                renderProgram.setDisableTex(false);


                /*if(glActiveTextureARB)
                {
                    glActiveTextureARB( GL_TEXTURE1_ARB );
                    m_occlusionmap.enable();
                    m_occlusionmap.bind();
#ifdef WIN32
                    if(!m_bNoRenderTexture) m_PBuffer->Bind(WGL_FRONT_LEFT_ARB);
#endif
                }*/
                GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
                m_PBuffer.bindColorTexture(0);
            }

            /*normalMat.load(modelView);
            normalMat.m30 = normalMat.m31 = normalMat.m32 = 0;  // zeros translation
            normalMat.invert();
            normalMat.transpose();*/

            renderProgram.setProjection(proj);
            renderProgram.setModelView(modelView);
            renderProgram.setModelViewIT(modelView);
        }

        void end(){
            if(m_inpbuffer) {
                //return;
            }else{
                GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

                GLES20.glDepthMask(true);
                GLES20.glDisable(GLES20.GL_BLEND);
                GLES20.glDisable(GLES20.GL_DEPTH_TEST);
            }
        }
        void draw_sprite(	Vector3f pos, Vector3f norm,
                             float MinSz, float MaxSz){
            /*cgGLSetParameter4f(Size, MinSz, MinSz, MaxSz, MaxSz);
            glTexCoord2f(1, 1);
            glNormal3fv(&(norm[0]));
            glVertex3fv(&(pos[0]));

            cgGLSetParameter4f(Size, -MinSz, MinSz, -MaxSz, MaxSz);
            glTexCoord2f(0, 1);
            glNormal3fv(&(norm[0]));
            glVertex3fv(&(pos[0]));

            cgGLSetParameter4f(Size, -MinSz, -MinSz, -MaxSz, -MaxSz);
            glTexCoord2f(0, 0);
            glNormal3fv(&(norm[0]));
            glVertex3fv(&(pos[0]));

            cgGLSetParameter4f(Size, MinSz, -MinSz, MaxSz, -MaxSz);
            glTexCoord2f(1, 0);
            glNormal3fv(&(norm[0]));
            glVertex3fv(&(pos[0]));*/

            bufferData.put(MinSz).put(MinSz).put(MaxSz).put(MaxSz);
            bufferData.put(1).put(1);
            bufferData.put(norm.x).put(norm.y).put(norm.z);
            bufferData.put(pos.x).put(pos.y).put(pos.z);

            bufferData.put(-MinSz).put(MinSz).put(-MaxSz).put(MaxSz);
            bufferData.put(0).put(1);
            bufferData.put(norm.x).put(norm.y).put(norm.z);
            bufferData.put(pos.x).put(pos.y).put(pos.z);

            bufferData.put(-MinSz).put(-MinSz).put(-MaxSz).put(-MaxSz);
            bufferData.put(0).put(0);
            bufferData.put(norm.x).put(norm.y).put(norm.z);
            bufferData.put(pos.x).put(pos.y).put(pos.z);

            bufferData.put(MinSz).put(-MinSz).put(MaxSz).put(-MaxSz);
            bufferData.put(1).put(0);
            bufferData.put(norm.x).put(norm.y).put(norm.z);
            bufferData.put(pos.x).put(pos.y).put(pos.z);
            bufferData.flip();

            final int stride = 12 * 4;
            final int pos_offset = 9 * 4;
            final int nor_offset = 6 * 4;
            final int tex_offset = 4 * 4;
            final int size_offset = 0;

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, sprite_buffer);
            GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, bufferData.remaining() << 2, bufferData);
            GLES20.glEnableVertexAttribArray(renderProgram.getPositionAttrib());
            GLES20.glVertexAttribPointer(renderProgram.getPositionAttrib(), 3, GLES20.GL_FLOAT, false, stride, pos_offset);

            GLES20.glEnableVertexAttribArray(renderProgram.getNormalAttrib());
            GLES20.glVertexAttribPointer(renderProgram.getNormalAttrib(), 3, GLES20.GL_FLOAT, false, stride, nor_offset);

            GLES20.glEnableVertexAttribArray(renderProgram.getTexcoordAttrib());
            GLES20.glVertexAttribPointer(renderProgram.getTexcoordAttrib(), 2, GLES20.GL_FLOAT, false, stride, tex_offset);

            GLES20.glEnableVertexAttribArray(renderProgram.getSizeAttrib());
            GLES20.glVertexAttribPointer(renderProgram.getSizeAttrib(), 4, GLES20.GL_FLOAT, false, stride, size_offset);

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4);

            GLES20.glDisableVertexAttribArray(renderProgram.getPositionAttrib());
            GLES20.glDisableVertexAttribArray(renderProgram.getNormalAttrib());
            GLES20.glDisableVertexAttribArray(renderProgram.getTexcoordAttrib());
            GLES20.glDisableVertexAttribArray(renderProgram.getSizeAttrib());
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        }

        void bind_light(ReadableVector3f l, float shine){
            renderProgram.setLight(l.getX(), l.getY(), l.getZ());
            renderProgram.setShininess(shine);
        }

        void begin_occlusion(boolean bDebug){
            m_PBuffer.enableRenderToColorAndDepth(0);
            m_PBuffer.saveAndSetViewPort();
            GLES20.glClearColor(0.5f, 0, 1, 1.0f);
            GLES20.glClearDepthf(1.0f);
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);

            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT|GLES20.GL_COLOR_BUFFER_BIT);
            m_inpbuffer = true;
        }

        void end_occlusion(boolean bDebug){
            FrameBufferObject.disableRenderToColorDepth();
            m_PBuffer.restoreViewPort();
            m_inpbuffer = false;
        }

        void display_occlusion_map(){
            GLES20.glDisable(GLES20.GL_DEPTH_TEST);
            mQuadProgram.enable();
            GLES20.glViewport(0,0,getWidth()/4, getHeight()/4);
            m_PBuffer.bindColorTexture(0);
            NvShapes.drawQuad(mQuadProgram.getAttribPosition(), mQuadProgram.getAttribTexCoord());

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
            GLES20.glViewport(0,0,getWidth(), getHeight());
        }

        void free(){
            if(m_PBuffer != null){
                m_PBuffer.dispose();
            }

            if(renderProgram != null){
                renderProgram.dispose();
            }
        }
    }

    private static final class SSprite{
        Vector3f Pos = new Vector3f();
        Vector3f Normal= new Vector3f();
        Vector3f Tangent = new Vector3f();
        float MinSize, MaxSize;
    }

    private static final class ShinySpriteRenderProgram extends NvGLSLProgram{
//        uniform mat4 Projection;
//        uniform mat4 ModelView;
//        uniform mat4 ModelViewIT;
//        uniform vec3 Light;
//        uniform float Shininess;
//        uniform float Slices;
//        uniform vec2 CSTable[30];

        private int projectionIdx = -1;
        private int modelViewIdx = -1;
        private int modelViewITIdx = -1;
        private int lightIdx = -1;
        private int shininessIdx = -1;
        private int slicesIdx = -1;
        private int csTableIdx = -1;
        private int disableTexIdx = -1;

//        attribute vec4 In_Position;
//        attribute vec4 In_Size;
//        attribute vec4 In_Normal;
//        attribute vec4 In_TexCoord0;
        private int positionAttrib;
        private int sizeAttrib;
        private int normalAttrib;
        private int texcoordAttrib;
        private float[] csTable = new float[60];
        private int tableIdx;

        ShinySpriteRenderProgram(){
            setSourceFromFiles("shaders/ShinySpriteVS.vert", "shaders/ShinySpritePS.frag");

            projectionIdx = getUniformLocation("Projection");
            modelViewIdx = getUniformLocation("ModelView");
            modelViewITIdx = getUniformLocation("ModelViewIT");
            lightIdx = getUniformLocation("Light");
            shininessIdx = getUniformLocation("Shininess");
            slicesIdx = getUniformLocation("Slices");
            csTableIdx = getUniformLocation("CSTable");
            disableTexIdx = getUniformLocation("g_DisableTexture");

            positionAttrib = getAttribLocation("In_Position");
            sizeAttrib = getAttribLocation("In_Size");
            normalAttrib = getAttribLocation("In_Normal");
            texcoordAttrib = getAttribLocation("In_TexCoord0");

            table(0);    // ALWAYS 0
            table(0);    // 86
            table(0);    // 84.2
            table(0);    // 82.3
            table(0);    // 80.4
            table(0);    // 78.5
            table(60);   // 76.5
            table(64);   // 74.5
            table(55);   // 72.5
            table(0);    // 70.5
            table(0);    // 68.5
            table(0);    // 66.4
            table(0);    // 64.3
            table(20);   // 62.1
            table(0);    // 60
            table(20);   // 57.7
            table(0);    // 55.4
            table(0);    // 53.1
            table(0);    // 50.7
            table(28);   // 48.2
            table(35);   // 45.5
            table(20);   // 42.8
            table(0);    // 39.9
            table(0);    // 36.8
            table(0);    // 33.5
            table(0);    // 29.9
            table(0);    // 25.8
            table(10);   // 21
            table(0);    // 14.8
            table(0);    // 0

            enable();
            setUniform1i("g_InputTex0", 0);
            setUniform1i("g_InputTex1", 1);
            setCSTable(csTable);
            setSlices(30);
            disable();

            csTable = null;
        }

        private void table(float x){
            csTable[2 * tableIdx + 0] = (float) Math.cos(x*3.141592654f/180.0f);
            csTable[2 * tableIdx + 1] = (float) Math.sin(x*3.141592654f/180.0f);
            tableIdx++;
        }

        void setProjection(Matrix4f mat){
            if(projectionIdx >=0){
                GLES20.glUniformMatrix4fv(projectionIdx, 1, false, GLUtil.wrap(mat));
            }
        }

        void setModelView(Matrix4f mat){
            if(modelViewIdx >=0){
                GLES20.glUniformMatrix4fv(modelViewIdx, 1, false, GLUtil.wrap(mat));
            }
        }

        void setModelViewIT(Matrix4f mat){
            if(modelViewITIdx >=0){
                GLES20.glUniformMatrix4fv(modelViewITIdx, 1, false, GLUtil.wrap(mat));
            }
        }

        void setLight(float x, float y, float z){
            if(lightIdx >=0){
                GLES20.glUniform3f(lightIdx, x,y,z);
            }
        }

        void setDisableTex(boolean flag){
            if(disableTexIdx >=0){
                GLES20.glUniform1i(disableTexIdx, flag?1:0);
            }
        }

        private void setSlices(float slice){
            if(slicesIdx >=0){
                GLES20.glUniform1f(slicesIdx, slice);
            }
        }

        void setShininess(float shininess){
            if(shininessIdx >=0){
                GLES20.glUniform1f(shininessIdx, shininess);
            }
        }

        private void setCSTable(float[] table){
            if(csTableIdx >=0){
                GLES20.glUniform2fv(csTableIdx, table.length/2, table, 0);
            }
        }

        int getPositionAttrib() { return positionAttrib;}
        int getSizeAttrib() { return sizeAttrib;}
        int getNormalAttrib() { return normalAttrib;}
        int getTexcoordAttrib() { return texcoordAttrib;}
    }
}
