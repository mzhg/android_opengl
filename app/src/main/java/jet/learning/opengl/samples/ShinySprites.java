package jet.learning.opengl.samples;

import android.graphics.Bitmap;
import android.opengl.GLES11;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.nvidia.developer.opengl.app.NvSampleApp;
import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.GLUtil;
import com.nvidia.developer.opengl.utils.Glut;
import com.nvidia.developer.opengl.utils.NvGLSLProgram;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import javax.microedition.khronos.opengles.GL11;

import jet.learning.opengl.common.FrameBufferBuilder;
import jet.learning.opengl.common.FrameBufferObject;
import jet.learning.opengl.common.TextureInfo;

/**
 * Created by mazhen'gui on 2017/10/19.
 */

public class ShinySprites extends NvSampleApp {
    private static final int TEX_SZ = 256;

    private static final class CSSprite{
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
            }
            else
            {
//                glCallList(dl_bind_vtxprg);  TODO
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

            renderProgram.setProjection(Matrix4f.IDENTITY);
            renderProgram.setModelView(Matrix4f.IDENTITY);
            renderProgram.setModelViewIT(Matrix4f.IDENTITY);
        }

        void end(){
            if(m_inpbuffer) {
                //return;
            }else{
                GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
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
        }

        void bind_light(Vector3f l, float shine){
            renderProgram.setLight(l.x, l.y, l.z);
            renderProgram.setShininess(shine);
        }

        void begin_occlusion(boolean bDebug){

        }

        void end_occlusion(boolean bDebug){

        }

        void begin_render(){

        }
        void end_render(){

        }

        void display_occlusion_map(){

        }

        void free(){

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

        void setSlices(float slice){
            if(slicesIdx >=0){
                GLES20.glUniform1f(slicesIdx, slice);
            }
        }

        void setShininess(float shininess){
            if(shininessIdx >=0){
                GLES20.glUniform1f(shininessIdx, shininess);
            }
        }

        void setCSTable(float[] table){
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
