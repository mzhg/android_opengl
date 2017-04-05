package jet.learning.opengl.hdr;

import android.graphics.Bitmap;
import android.opengl.GLES30;
import android.opengl.GLUtils;

import com.nvidia.developer.opengl.app.NvSampleApp;
import com.nvidia.developer.opengl.ui.NvTweakBar;
import com.nvidia.developer.opengl.ui.NvTweakEnumi;
import com.nvidia.developer.opengl.ui.NvTweakVarBase;
import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.GLUtil;
import com.nvidia.developer.opengl.utils.Glut;
import com.nvidia.developer.opengl.utils.NvAssetLoader;
import com.nvidia.developer.opengl.utils.NvLogger;
import com.nvidia.developer.opengl.utils.NvShapes;
import com.nvidia.developer.opengl.utils.NvUtils;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import javax.microedition.khronos.opengles.GL11;

import jet.learning.opengl.common.FrameBufferBuilder;
import jet.learning.opengl.common.FrameBufferObject;
import jet.learning.opengl.common.HDRImage;
import jet.learning.opengl.common.TextureInfo;

import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glEnableVertexAttribArray;

/**
 * Created by mazhen'gui on 2017/3/13.
 */

public class HDR extends NvSampleApp {
    private static final int POS_BIND = 0;
    private static final int NOR_BIND = 1;
    private static final int TEX_BIND = 2;

    // enum MATERIAL_TYPE
    private static final int MATERIAL_MAT = 0x00000001;
    private static final int MATERIAL_REFRACT = 0x00000002;
    private static final int MATERIAL_REFLECT = 0x00000003;
    private static final int MATERIAL_MATTE = 0x00000011;
    private static final int MATERIAL_ALUM = 0x00000013;
    private static final int MATERIAL_SILVER = 0x00000023;
    private static final int MATERIAL_GOLDEN = 0x00000033;
    private static final int MATERIAL_METALIC = 0x00000043;
    private static final int MATERIAL_DIAMOND = 0x00000012;
    private static final int MATERIAL_EMERALD = 0x00000022;
    private static final int MATERIAL_RUBY = 0x00000032;

    static boolean ENABLE_HDR = true;

    private static final float Z_NEAR = 0.4f;
    private static final float Z_FAR = 5000.0f;
    private static final float FOV = 90f;  // 3.14f*0.5f

    private static final String model_file[]={"venus","teapot","knot"};
    private static final String s_hdr_tex[]={"rnl_cross_mmp_s.hdr", "grace_cross_mmp_s.hdr","altar_cross_mmp_s.hdr","uffizi_cross_mmp_s.hdr"};
    private static final String s_hdr_tex_rough[]={"rnl_cross_rough_mmp_s.hdr", "grace_cross_rough_mmp_s.hdr","altar_cross_rough_mmp_s.hdr","uffizi_cross_rough_mmp_s.hdr"};
    private static final String s_hdr_tex_irrad[]={"rnl_cross_irrad_mmp_s.hdr", "grace_cross_irrad_mmp_s.hdr","altar_cross_irrad_mmp_s.hdr","uffizi_cross_irrad_mmp_s.hdr"};
    private static final String[] cube_names = {"posx", "negx","posy", "negy","posz", "negz",};
    private static final float exposureCompansation[]={3.0f,3.0f,10.0f,4.0f};

    final HDRPostProcessing hdr_post = new HDRPostProcessing();
    final HDRParameters hdr_params = new HDRParameters();

    final MTLData material[]={
            new MTLData(MATERIAL_MATTE, 1.0f, 1.0f, 1.0f, 0.0f),
            new MTLData(MATERIAL_ALUM, 1.0f, 1.0f, 1.0f, 0.5f),
            new MTLData(MATERIAL_SILVER, 1.0f, 1.0f, 1.0f, 0.9f),
            new MTLData(MATERIAL_GOLDEN, 1.0f, 0.9f, 0.4f, 0.9f),
            new MTLData(MATERIAL_METALIC,1.0f, 1.0f, 1.0f, 0.1f),
            new MTLData(MATERIAL_DIAMOND, 0.8f, 0.8f, 0.8f, 1.0f),
            new MTLData(MATERIAL_EMERALD, 0.2f, 0.8f, 0.2f, 1.0f),
            new MTLData(MATERIAL_RUBY, 0.9f, 0.1f, 0.4f, 1.0f),
    };

    // programs
    final BaseProgram[] programs = new BaseProgram[3];
    TextureBlitProgram simple_program;
    SkyProgram sky_program;

    // vao
    final int[] object_ids = new int[3];
    final int[] triangles_count = new int[3];
    private VertexBufferObject m_skybox;

    FrameBufferObject scene_buffer;
    FrameBufferBuilder fbo_builder;

    final Matrix4f projection_mat = new Matrix4f();
    final Vector3f eye_pos = new Vector3f();

    // hdr images
//	private HDRImage[] image = new HDRImage[4];
    private int[] hdr_tex = new int[4];
    //	private HDRImage[] image_rough = new HDRImage[4];
    private int[] hdr_tex_rough = new int[4];
    //	private HDRImage[] image_irrad = new HDRImage[4];
    private int[] hdr_tex_irrad = new int[4];

    int m_sceneIndex = 2;
    int m_objectIndex;
    int m_materialIndex = 5;
    boolean m_autoSpin = false;
    boolean m_drawBackground = true;
    boolean m_hdr_post = false;
    float m_expAdjust = 1.4f;

    final Matrix4f temp = new Matrix4f();
    final Matrix4f eye_mvp = new Matrix4f();

    @Override
    public void initUI() {
        @SuppressWarnings("unused")
        NvTweakVarBase var;

        NvTweakBar tweakBar = mTweakBar;
        var = tweakBar.addValue("Auto Spin", createControl("m_autoSpin"));
//        var = tweakBar.addValue("Draw Background", createControl("m_drawBackground"));
        var = tweakBar.addValue("HDR Post", createControl("m_hdr_post"));
//        var = tweakBar.addValue("Adaptive Exposure", createControl(hdr_params, "autoExposure"));
//        var = tweakBar.addValue("Exposure", createControl("m_expAdjust"), 1.0f, 5.0f);

//        tweakBar.addPadding();
        tweakBar.addPadding();
        NvTweakEnumi sceneIndex[] =
                {
                        new NvTweakEnumi( "Nature", 0 ),
                        new NvTweakEnumi( "Grace", 1 ),
                        new NvTweakEnumi( "Altar", 2 ),
                        new NvTweakEnumi( "Uffizi", 3 ),
                };
        tweakBar.addMenu("Select Scene:", createControl("m_sceneIndex"), sceneIndex, 0x22);

        tweakBar.addPadding();
        NvTweakEnumi materialIndex[] =
                {
                        new NvTweakEnumi( "Matte", 0 ),
                        new NvTweakEnumi( "Alum", 1 ),
                        new NvTweakEnumi( "Silver", 2 ),
                        new NvTweakEnumi( "Golden", 3 ),
                        new NvTweakEnumi( "Metalic", 4 ),
                        new NvTweakEnumi( "Diamond", 5 ),
                        new NvTweakEnumi( "Emerald", 6 ),
                        new NvTweakEnumi( "Ruby", 7 ),
                };
        tweakBar.addMenu("Select Material:", createControl("m_materialIndex"), materialIndex, 0x33);

        tweakBar.addPadding();
        tweakBar.addPadding();
        NvTweakEnumi objectIndex[] =
                {
                        new NvTweakEnumi( "Venus", 0 ),
                        new NvTweakEnumi( "Teapot", 1 ),
                        new NvTweakEnumi( "Knot", 2 ),
                };

        tweakBar.addEnum("Select Object:", createControl("m_objectIndex"), objectIndex, 0x55);

        tweakBar.addPadding();
        NvTweakEnumi glareType[] =
                {
                        new NvTweakEnumi( "Camera", HDRPostProcessing.CAMERA_GLARE ),
                        new NvTweakEnumi( "Filmic", HDRPostProcessing.FILMIC_GLARE ),
                };

        tweakBar.addEnum("Glare Type:", createControl(hdr_params, "glareType"), glareType, 0x44);
        tweakBar.syncValues();
    }

    @Override
    protected void initRendering() {
        programs[0] = new MatteProgram();
        programs[1] = new RefractProgram();
        programs[2] = new ReflectProgram();
        sky_program = new SkyProgram();
        simple_program = new TextureBlitProgram();
        simple_program.init();

//        NvLogger.setLevel(1);
        NvLogger.i("Init shaders done!");
        GLES.checkGLError();

/*
        Model sky_model = ModelGenerator.genCube(1000, false, false, false);
        sky_model.bindAttribIndex(0, 0);
        sky_vao = sky_model.genVAO();
        GLES.checkGLError();
        fullscreen = ModelGenerator.genRect(-1, -1, 1, 1, true).genVAO();
        */
        if(ENABLE_HDR)
            loadHdrImages();
        else
            loadLdrImages();
        loadModels();
        m_transformer.setTranslationVec(new Vector3f(0.0f, 0.0f, -202.2f));
        m_transformer.setRotationVec(new Vector3f(-0.2f, -0.3f, 0));
    }

    @Override
    protected void draw() {
        FrameBufferObject source = scene_buffer;
        m_transformer.setRotationVel(new Vector3f(0.0f, m_autoSpin ? (NvUtils.PI *0.05f) : 0.0f, 0.0f));
        scene_buffer.enableRenderToColorAndDepth(0);
        scene_buffer.saveAndSetViewPort();
        //we only need to clear depth
        GLES30.glClear(GL11.GL_DEPTH_BUFFER_BIT);
        GLES.checkGLError("draw0");

        GLES30.glDisable(GL11.GL_DEPTH_TEST);
        GLES30.glDisable(GL11.GL_CULL_FACE);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_CUBE_MAP, hdr_tex[m_sceneIndex]);
        GLES.checkGLError("draw1");
        Matrix4f mvp;
        /*
        m_transformer.getModelViewMat(temp);
        temp.invert();
        eye_pos.set(0, 0, 0);
        // Transform the eye position from camera coordinate to the world coordinate.
        Matrix4f.transformVector(temp, eye_pos, eye_pos);
        */
        eye_pos.set(m_transformer.getTranslationVec());
        eye_pos.scale(-1);

        m_transformer.getModelViewMat(temp);
        Matrix4f view_inverse = eye_mvp;
        view_inverse.load(temp);
        view_inverse.invert();

        mvp = Matrix4f.mul(projection_mat, temp, temp);
//	    	sky_program.applyEyePos(eye_pos.getX(), eye_pos.getY(), eye_pos.getZ());
        // 1. draw the sky box
        if(m_drawBackground){
            sky_program.enable();
            sky_program.applyProjMat(projection_mat);
            sky_program.applyViewMat(view_inverse);

            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, m_skybox.getVBO());
            GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, m_skybox.getIBO());

            GLES30.glEnableVertexAttribArray(sky_program.getAttribPosition());
            GLES30.glVertexAttribPointer(sky_program.getAttribPosition(), 3, GLES30.GL_FLOAT, false, 32, 0);

            GLES30.glDrawElements(GLES30.GL_TRIANGLES, 6*6, GLES30.GL_UNSIGNED_SHORT, 0);

            GLES30.glDisableVertexAttribArray(sky_program.getAttribPosition());

            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
            GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0);
        }
        // 2. draw the models.
        GLES30.glEnable(GL11.GL_DEPTH_TEST);
        GLES30.glEnable(GL11.GL_CULL_FACE);
        GLES30.glFrontFace(GL11.GL_CCW);
        GLES30.glCullFace(GL11.GL_FRONT);

        int mtlClass = material[m_materialIndex].type & 0xF;
//	    	programs[mtlClass].enable();
        BaseProgram program = null;
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_CUBE_MAP, hdr_tex[m_sceneIndex]);
        GLES.checkGLError("draw2----");
        switch (mtlClass) {
            case MATERIAL_MAT:
            default:
//                System.out.println("MATERIAL_MAT");
                GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
                GLES30.glBindTexture(GLES30.GL_TEXTURE_CUBE_MAP, hdr_tex_irrad[m_sceneIndex]);
                program = programs[0];
                break;
            case MATERIAL_REFLECT:
//                System.out.println("MATERIAL_REFLECT");
                GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
                GLES30.glBindTexture(GLES30.GL_TEXTURE_CUBE_MAP, hdr_tex_rough[m_sceneIndex]);
                program = programs[2];
                break;
            case MATERIAL_REFRACT:
//                GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
//                GLES30.glBindTexture(GLES30.GL_TEXTURE_CUBE_MAP, 0);
//                GLES30.glActiveTexture(GLES30.GL_TEXTURE2);
//                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
//	    			System.out.println("MATERIAL_REFRACT");
                program = programs[1];
                break;
        }
        GLES.checkGLError("draw2a");
        program.enable();
        program.applyMVP(mvp);
        program.applyModelView(BaseProgram.IDENTITY);
        GLES.checkGLError("draw2b");
//	    	eye_pos.set(0,0,0);
//	    	Matrix4f viewMat;
//	    	if(CUSTOME_CAMERA){
//	    		viewMat = Matrix4f.invert(camera.getView(), temp);
//	    	}else{
//	    		m_transformer.getModelViewMat(temp);
//	    		viewMat = temp;
//	    		viewMat.invert();
//	    	}
//	    	Matrix4f.transformVector(viewMat, eye_pos, eye_pos);
        program.applyEyePos(eye_pos);
        program.applyEmission(0, 0, 0);
        program.applyColor(material[m_materialIndex].r, material[m_materialIndex].g, material[m_materialIndex].b, material[m_materialIndex].a);
        GLES.checkGLError("draw2c");
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, object_ids[m_objectIndex]);
        GLES.checkGLError("draw2d");
        glEnableVertexAttribArray(program.getAttribPosition());
        glEnableVertexAttribArray(program.getAttribNormal());
        GLES.checkGLError("draw2e");
        GLES30.glVertexAttribPointer(program.getAttribPosition(), 3, GL11.GL_FLOAT, false, 32, 0);
        GLES30.glVertexAttribPointer(program.getAttribNormal(), 3, GL11.GL_FLOAT, false, 32, 12);
        GLES.checkGLError("draw2f");
//	    	GL20.glVertexAttribPointer(2, 2, GL11.GL_FLOAT, false, 32, 24);
        GLES30.glDrawArrays( GL11.GL_TRIANGLES, 0, triangles_count[m_objectIndex]);
        GLES.checkGLError("draw2g");
        glDisableVertexAttribArray(program.getAttribPosition());
        glDisableVertexAttribArray(program.getAttribNormal());
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_CUBE_MAP, 0);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_CUBE_MAP, 0);

        scene_buffer.restoreViewPort();
        GLES.checkGLError("draw3");

        GLES30.glCullFace(GLES30.GL_BACK);  // reset to the default value.
        GLES30.glDisable(GLES30.GL_CULL_FACE);
        GLES30.glDisable(GLES30.GL_BLEND);
        GLES30.glDisable(GLES30.GL_DEPTH_TEST);

        if(m_hdr_post){
            hdr_params.elpsedTime = getFrameDeltaTime();

            hdr_params.scene_texture = scene_buffer.getColorTexture(0);
            hdr_params.scene_width = getWidth();
            hdr_params.scene_height = getHeight();
            hdr_params.enableLightStreaker = true;
            hdr_params.enableLensFlare = true;
            hdr_params.enableHDR = true;
//            hdr_params.lumThreshold = 0.68f;
//            hdr_params.blendAmount = 1;

            float newExp = (float)(exposureCompansation[m_sceneIndex] * Math.log(m_expAdjust+0.0001));
            hdr_params.explosure = newExp;
            hdr_post.postprocessing(hdr_params);

        }else{
            FrameBufferObject.disableRenderToColorDepth();

            GLES30.glViewport(0, 0, getWidth(), getHeight());
            GLES30.glClear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
            GLES30.glDisable(GL11.GL_DEPTH_TEST);
            scene_buffer.bindColorTexture(0);
            simple_program.enable();
//            fullscreen.bind();
//            fullscreen.draw(GL11.GL_TRIANGLES);
//            fullscreen.unbind();
            NvShapes.drawQuad(simple_program.getAttribPosition(), simple_program.getAttribTexCoord());
        }

        GLES30.glUseProgram(0);
    }

    @Override
    protected void reshape(int width, int height) {
        GLES30.glViewport(0, 0, width, height);
        if(width == 0 && height == 0)
            return;

        GLES.checkGLError("reshape0");

        if(fbo_builder == null){
            FrameBufferBuilder builder = new FrameBufferBuilder();
            TextureInfo color0 = builder.createColorTexture();
            color0.setMinFilter(GL11.GL_LINEAR);
            color0.setMagFilter(GL11.GL_LINEAR);
            color0.setSWrap(GLES30.GL_CLAMP_TO_EDGE);
            color0.setTWrap(GLES30.GL_CLAMP_TO_EDGE);
            TextureInfo depth = builder.getOrCreateDepthTexture();
            depth.setInternalFormat(FrameBufferObject.FBO_DepthBufferType_RENDERTARGET);
            fbo_builder = builder;
        }
        if(fbo_builder.equals(width, height)){
            return;
        }

        if(scene_buffer != null)
            scene_buffer.dispose();

        fbo_builder.getColorTextures().get(0).setInternalFormat(GLES30.GL_RGBA16F);
        fbo_builder.setWidth(width).setHeight(height);
        scene_buffer = new FrameBufferObject(fbo_builder);
        hdr_params.scene_texture = scene_buffer.getColorTexture(0);
        hdr_params.scene_width = width;
        hdr_params.scene_height = height;
        hdr_params.viewport_width = width;
        hdr_params.viewport_height = height;
        System.out.println(hdr_params);

//        fbo_builder.getColorTextures().get(0).setInternalFormat(GLES30.GL_RGBA8);
//		hdr_params.dest = new FrameBufferObject(fbo_builder);

        Matrix4f.perspective(FOV * 0.5f, (float)width/(float)height, Z_NEAR, Z_FAR, projection_mat);
        GLES.checkGLError("reshape1");
    }

    void loadModels(){
        for(int i = 0; i < 3; i++){
            byte[] data = NvAssetLoader.read("hdr_shaders/" + model_file[i]);
            int bufID;
            bufID = GLES.glGenBuffers();
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, bufID);
            GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, data.length, GLUtil.wrap(data), GLES30.GL_STATIC_DRAW);
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);

            object_ids[i] = bufID;
            triangles_count[i] = data.length/32;
        }

        m_skybox = new VertexBufferObject();
        m_skybox.genVertexData(GLUtil.wrap(CubeData.verticesCube), 4*8*6*4, false);
        m_skybox.genIndexData(GLUtil.wrap(CubeData.indicesCube), 6*6*2, false);

        GLES.checkGLError();
        NvLogger.i("loadModels done!");
    }

    void loadLdrImages(){
        //load all HDRImages we need
        int i;
        for (i=0;i<4;i++) {
            hdr_tex[i] = createCubemapTexture(s_hdr_tex[i]);
        }
        for (i=0;i<4;i++) {
            hdr_tex_rough[i] = createCubemapTexture(s_hdr_tex_rough[i]);
        }
        for (i=0;i<4;i++) {
            hdr_tex_irrad[i] = createCubemapTexture(s_hdr_tex_irrad[i]);
        }

        GLES.checkGLError("Load Images");
    }

    void loadHdrImages(){
        //load all HDRImages we need
        int i;
        for (i=0;i<4;i++) {
            HDRImage image = new HDRImage();
            if (!image.loadHDRIFromFile("hdr_shaders/" + s_hdr_tex[i])) {
                NvLogger.e("Error loading image file '%s'\n", s_hdr_tex[i]);
            }
            if (!image.convertCrossToCubemap()) {
                NvLogger.e("Error converting image to cubemap\n");
            };
            hdr_tex[i] = createCubemapTexture(image, GL11.GL_RGB, true);
        }
        for (i=0;i<4;i++) {
            HDRImage image_rough = new HDRImage();
            if (!image_rough.loadHDRIFromFile("hdr_shaders/" + s_hdr_tex_rough[i])) {
                NvLogger.e("Error loading image file '%s'\n", s_hdr_tex_rough[i]);
            }
            if (!image_rough.convertCrossToCubemap()) {
                NvLogger.e("Error converting image to cubemap\n");
            };
            hdr_tex_rough[i] = createCubemapTexture(image_rough, GL11.GL_RGB, true);
        }
        for (i=0;i<4;i++) {
            HDRImage image_irrad = new HDRImage();
            if (!image_irrad.loadHDRIFromFile("hdr_shaders/" + s_hdr_tex_irrad[i])) {
                NvLogger.e("Error loading image file '%s'\n", s_hdr_tex_irrad[i]);
            }
            if (!image_irrad.convertCrossToCubemap()) {
                NvLogger.e("Error converting image to cubemap\n");
            };
            hdr_tex_irrad[i] = createCubemapTexture(image_irrad, GL11.GL_RGB, true);
        }

        GLES.checkGLError("Load Images");
    }

    static int createCubemapTexture(String filename){
        int tex = GLES.glGenTextures();
        GLES30.glBindTexture(GLES30.GL_TEXTURE_CUBE_MAP, tex);

        GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);

        GLES30.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
        GLES.checkGLError("creating ldr cube map00000000000");
        for(int i = 0; i < 6; i++){
            int dot = filename.lastIndexOf('.');
            String fullfilepath = "ldr_textures/" + filename.substring(0, dot) + '_' + cube_names[i] + ".png";
//            Log.i("HDR", "fullfilepath = " + fullfilepath);
            Bitmap bitmap = Glut.loadBitmapFromAssets(fullfilepath);
            GLUtils.texImage2D(GLES30.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, bitmap, 0);
        }
        GLES.checkGLError("creating ldr cube map");
        return tex;
    }

    static int createCubemapTexture(HDRImage img, int internalformat, boolean filtering)
    {
        int tex = GLES.glGenTextures();
        GLES30.glBindTexture(GLES30.GL_TEXTURE_CUBE_MAP, tex);

        GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MAG_FILTER, filtering ? GL11.GL_LINEAR : GL11.GL_NEAREST);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MIN_FILTER, filtering ? GL11.GL_LINEAR : GL11.GL_NEAREST);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);

        GLES30.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);

        GLES.checkGLError("creating cube map0");
        short[] out = new short[img.getWidth()*img.getHeight()*3];
        for(int i=0; i<6; i++) {
            HDRImage.fp32toFp16(img.getLevel(0, GLES30.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i), 0, out, 0, img.getWidth(), img.getHeight());
            GLES30.glTexImage2D(GLES30.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0,
                    GLES30.GL_RGB16F, img.getWidth(), img.getHeight(), 0,
                    GL11.GL_RGB, GLES30.GL_HALF_FLOAT, GLUtil.wrap(out));
        }

        GLES.checkGLError("creating cube map1");
        GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_CUBE_MAP);
        GLES.checkGLError("creating cube map2..");
        GLES30.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);
        GLES.checkGLError("creating cube map3");

        return tex;
    }
}
