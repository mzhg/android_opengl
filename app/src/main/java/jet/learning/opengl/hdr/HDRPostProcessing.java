package jet.learning.opengl.hdr;

import android.opengl.GLES20;
import android.opengl.GLES30;
import android.util.Log;

import com.nvidia.developer.opengl.utils.Dimension;
import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.GLUtil;
import com.nvidia.developer.opengl.utils.NvAssetLoader;
import com.nvidia.developer.opengl.utils.NvShapes;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

import javax.microedition.khronos.opengles.GL11;

import jet.learning.opengl.common.FrameBufferBuilder;
import jet.learning.opengl.common.FrameBufferObject;
import jet.learning.opengl.common.SimpleOpenGLProgram;
import jet.learning.opengl.common.TextureInfo;

/**
 * Created by mazhen'gui on 2017/3/16.
 */

class HDRPostProcessing {

    public static final int FILMIC_GLARE = 0;
    public static final int CAMERA_GLARE = 1;

    private static final int POS_BIND = 0;
    private static final int TEX_BIND = 1;

    public static final int POST_PROCESSING_DEFAULT_SIZE = 1024;

    private static final float cameraMixCoeff[]={1.2f, 0.8f, 0.1f, 0.0f};
    private static final float filmicMixCoeff[]={0.6f, 0.55f, 0.08f, 0.0f};
//	static final float exposureCompansation[]={3.0f,3.0f,10.0f,4.0f};

    private static final int LEVEL_0 = 0;
    private static final int LEVEL_1 = 1;
    private static final int LEVEL_2 = 2;
    private static final int LEVEL_3 = 3;
    //	private static final int LEVEL_4 = 4;
//	private static final int LEVEL_5 = 5;
    private static final int LEVEL_TOTAL = 4;

    private static final int BLURH4 = 0;

    private HDRGaussionBlurProgram[] blurs = new HDRGaussionBlurProgram[8];
    private Downsample4xProgram downSample4x;
    private TextureBlitProgram downSample;
    private CalculateLuminance calLuminance;
    private CalculateAdaptedLum calAdapted;
    private ExtractHLProgram extractHL;
    private StarStreakProgram starStreak;
    private GhostImageProgram ghostImage;
    private GlareComposeProgram glareCompose;
    private StarStreakComposeProgram starCompose;
    private TonemapProgram tonemap;

    private int postProcessingWidth = 1024;
    private int postProcessingHeight = 1024;

    FrameBufferObject[]	compose_buffer = new FrameBufferObject[LEVEL_TOTAL];
    FrameBufferObject[]	blur_bufferA   = new FrameBufferObject[LEVEL_TOTAL];
    FrameBufferObject[]	blur_bufferB   = new FrameBufferObject[LEVEL_TOTAL];
    FrameBufferObject[]	streak_bufferA = new FrameBufferObject[4];
    FrameBufferObject	streak_bufferB;
    FrameBufferObject	streak_bufferFinal;
    FrameBufferObject	ghost1st_buffer;
    FrameBufferObject	ghost2nd_buffer;
    FrameBufferObject	glare_buffer;
    FrameBufferObject[]	exp_buffer     = new FrameBufferObject[2];		//exposure info buffer
    int   fullscreenVAO;

    FrameBufferObject m_lumCurrent;
    FrameBufferObject[] m_lum = new FrameBufferObject[2];

    private int view_width, view_height;
    private final Dimension starSize = new Dimension();
    private ColorModulation colorModulation;
    private boolean initilized;
    private int m_lensMask;

    private HDRParameters params;
    private final float[] black = new float[4];

    public HDRPostProcessing() {
    }

    void init(){
        Log.e("HDR init", "begin.....");
        int size = POST_PROCESSING_DEFAULT_SIZE;
        init(size, size);
        colorModulation = new ColorModulation();

        // load image data.
        byte[] image_data = NvAssetLoader.read("hdr_shaders/star.data");
        //load mask texture for ghost image generation
        m_lensMask = GLES.glGenTextures();
        GLES30.glBindTexture(GL11.GL_TEXTURE_2D, m_lensMask);
        int c = GL11.GL_UNSIGNED_BYTE;
        GLES30.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GLES30.GL_RGBA8, 128, 128, 0, GLES30.GL_RGBA, GL11.GL_UNSIGNED_BYTE, GLUtil.wrap(image_data));
        GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    void init(int width, int height){
        postProcessingWidth = width;
        postProcessingHeight = height;

//		fullscreenVAO = ModelGenerator.genRect(-1, -1, 1, 1, true).genVAO(); TODO
//        Model rect = ModelGenerator.genRect(-1, -1, 1, 1, true);
//        rect.bindAttribIndex(0, POS_BIND);
//        rect.bindAttribIndex(1, TEX_BIND);
//        fullscreenVAO = rect.genVAO();

        initFBOs();
        initShaders();
    }

    public void dispose(){
        if(!initilized)
            return;

//        fullscreenVAO.dispose();
        if(fullscreenVAO!=0){
//            GLES.gldeleteV
        }

        downSample4x.dispose();
        calLuminance.dispose();
        calAdapted.dispose();
        extractHL.dispose();
        starStreak.dispose();
        ghostImage.dispose();
        glareCompose.dispose();
        starCompose.dispose();
        tonemap.dispose();
        downSample.dispose();
        for(int i = 0; i < blurs.length; i++)
            blurs[i].dispose();

        IntBuffer texIds = GLUtil.getCachedIntBuffer(30 + 20 + 3);
        texIds.position(27);
        IntBuffer fboIds = texIds.slice();
        texIds.position(0).limit(27);

        for(int i = 0; i < LEVEL_TOTAL; i++){
            fboIds.put(compose_buffer[i].getFBO());
            texIds.put(compose_buffer[i].getColorTexture(0));

            fboIds.put(blur_bufferA[i].getFBO());
            texIds.put(blur_bufferA[i].getColorTexture(0));

            fboIds.put(blur_bufferB[i].getFBO());
            texIds.put(blur_bufferB[i].getColorTexture(0));

            fboIds.put(streak_bufferA[i].getFBO());
            texIds.put(streak_bufferA[i].getColorTexture(0));

            if(i < exp_buffer.length){
                fboIds.put(exp_buffer[i].getFBO());
                texIds.put(exp_buffer[i].getColorTexture(0));
            }
        }

        fboIds.put(streak_bufferB.getFBO());
        texIds.put(streak_bufferB.getColorTexture(0));

        fboIds.put(streak_bufferFinal.getFBO());
        texIds.put(streak_bufferFinal.getColorTexture(0));

        fboIds.put(ghost1st_buffer.getFBO());
        texIds.put(ghost1st_buffer.getColorTexture(0));

        fboIds.put(ghost2nd_buffer.getFBO());
        texIds.put(ghost2nd_buffer.getColorTexture(0));

        fboIds.put(glare_buffer.getFBO());
        texIds.put(glare_buffer.getColorTexture(0));

        texIds.put(m_lumCurrent.getColorTexture(0));
        texIds.put(m_lum[0].getColorTexture(0));
        texIds.put(m_lum[1].getColorTexture(0));
        fboIds.put(m_lumCurrent.getFBO());
        fboIds.put(m_lum[0].getFBO());
        fboIds.put(m_lum[1].getFBO());

        texIds.put(m_lensMask);

        int tex_count = texIds.position();
        int fbo_count = fboIds.position();
        texIds.flip();
        fboIds.flip();

        GLES30.glDeleteFramebuffers(fbo_count, fboIds);
        GLES30.glDeleteTextures(tex_count, texIds);

        initilized = false;
    }

    private void initShaders(){
        downSample4x = new Downsample4xProgram();  downSample4x.init();
        Log.e("HDR Init Shader", "Downsample4xProgram init done!");
        downSample = new TextureBlitProgram();  downSample.init();
        Log.e("HDR Init Shader", "TextureBlitProgram init done!");

        calLuminance = new CalculateLuminance();  calLuminance.init();
        Log.e("HDR Init Shader", "CalculateLuminance init done!");
        calAdapted   = new CalculateAdaptedLum();  calAdapted.init();
        Log.e("HDR Init Shader", "CalculateAdaptedLum init done!");

        extractHL = new ExtractHLProgram();   extractHL.init();
        Log.e("HDR Init Shader", "ExtractHLProgram init done!");
        starStreak = new StarStreakProgram(); starStreak.init();
        Log.e("HDR Init Shader", "StarStreakProgram init done!");
        ghostImage = new GhostImageProgram(); ghostImage.init();
        Log.e("HDR Init Shader", "GhostImageProgram init done!");
        glareCompose = new GlareComposeProgram();  glareCompose.init();
        Log.e("HDR Init Shader", "GlareComposeProgram init done!");
        starCompose = new StarStreakComposeProgram();  starCompose.init();
        Log.e("HDR Init Shader", "StarStreakComposeProgram init done!");
        tonemap = new TonemapProgram(); tonemap.init(!params.enableHDR);
        Log.e("HDR Init Shader", "TonemapProgram init done!");

        //start from quarter size
        int w = (int)(postProcessingWidth/4 * (float)params.viewport_width/params.viewport_height);
        int h = (int)(postProcessingHeight/4);
        float[] s = HDRGaussionBlurProgram.std_weights;
        int kernelSize =7;
        for(int i = 0;i < 4; i++){
            float weight = s[i];
            blurs[2 * i + 0] = new HDRGaussionBlurProgram(w, h, false, weight);
            blurs[2 * i + 1] = new HDRGaussionBlurProgram(w, h, true , weight);
            w /=2;
            h /=2;
        }
    }

    private void initFBOs(){
        Log.e("HDR InitFBOs", "begin.....");
        FrameBufferBuilder builder = new FrameBufferBuilder();
        TextureInfo texture0 = builder.createColorTexture();
        texture0.setInternalFormat(params.enableHDR ? GLES30.GL_RGB16F: GLES30.GL_RGB8);
        texture0.setMagFilter(GL11.GL_LINEAR);
        texture0.setMinFilter(GL11.GL_LINEAR);
        texture0.setSWrap(GLES30.GL_CLAMP_TO_EDGE);
        texture0.setTWrap(GLES30.GL_CLAMP_TO_EDGE);

        // Create glare_buffer.
        builder.setWidth(postProcessingWidth/2).setHeight(postProcessingHeight/2);
        glare_buffer = new FrameBufferObject(builder);
        Log.e("HDR InitFBOs", "Create glare_buffer done!");

        int w = postProcessingWidth/4;
        int h = postProcessingHeight/4;
        //buffer pyramid for wide range gaussian blur
        for(int i=0; i<LEVEL_TOTAL; i++) {
            builder.setWidth(w).setHeight(h);
            compose_buffer[i] = new FrameBufferObject(builder);
            blur_bufferA[i] = new FrameBufferObject(builder);
            blur_bufferB[i] = new FrameBufferObject(builder);
            w /= 2;
            h /= 2;
        }
        Log.e("HDR InitFBOs", "Create compose_buffer done!");
        //get resolution for star streak rendering
        getBufferPyramidSize(params.starGenLevel, starSize);
        builder.setWidth(starSize.getWidth()).setHeight(starSize.getHeight());

        if(params.enableLightStreaker) {
            //4 directions
            for (int i = 0; i < 4; i++) {
                streak_bufferA[i] = new FrameBufferObject(builder);
            }
            streak_bufferB = new FrameBufferObject(builder);
            Log.e("HDR InitFBOs", "Create streak_bufferB done!");
            //star streak composition renderbuffer
            streak_bufferFinal = new FrameBufferObject(builder);
        }

        builder.setWidth(postProcessingWidth/2);
        builder.setHeight(postProcessingHeight/2);

        if(params.enableLensFlare) {
            //renderbuffer for ghost imtage
            ghost1st_buffer = new FrameBufferObject(builder);
            ghost2nd_buffer = new FrameBufferObject(builder);
        }

        if(params.enableHDR) {
            w = postProcessingWidth / 16;
            h = postProcessingHeight / 16;
            for (int i = 0; i < 2; i++) {
                builder.setWidth(w).setHeight(h);
                //2 buffers for downsampling to get thumbnail image
                exp_buffer[i] = new FrameBufferObject(builder);
//    		exp_buffer[i].init(w, h, RenderTexture.RGBA16F, RenderTexture.NoDepth);
                w /= 4;
                h /= 4;
            }

            builder.setWidth(1).setHeight(1);

            m_lumCurrent = new FrameBufferObject(builder);

            builder.setWidth(1).setHeight(1);
            for (int i = 0; i < 2; i++) {
                m_lum[i] = new FrameBufferObject(builder);
            }
            Log.e("HDR InitFBOs", "Create lum done!");
        }
    }

    private static int createTexture2D(){
        int textureid = GLES.glGenTextures();
        GLES30.glBindTexture(GL11.GL_TEXTURE_2D, textureid);
        GLES30.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GLES30.GL_R16F, 1, 1, 0, GLES30.GL_RED, GLES30.GL_HALF_FLOAT, (FloatBuffer)null);
        GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GLES30.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GLES30.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        return textureid;
    }

    private void activateFB(FrameBufferObject fbo){
        fbo.enableRenderToColorAndDepth(0);
        GLES30.glClearBufferfv(GLES30.GL_COLOR, 0, black, 0);
        int w = fbo.getWidth();
        int h = fbo.getHeight();

        if(view_height != h || view_width != w){
            GLES30.glViewport(0, 0, w, h);
            view_width = w;
            view_height = h;
        }
    }

    private void getBufferPyramidSize(int level, Dimension size){
        int width = postProcessingWidth/4;
        int height = postProcessingHeight/4;
        int lvl = level;
        while (lvl>LEVEL_0) {
            width /= 2;
            height /= 2;
            lvl--;
        }
//    	*w = width;
//    	*h = height;
        size.setSize(width, height);
    }

    private static void checkParameters(HDRParameters params){
        if(params.scene_texture == 0)
            throw new IllegalArgumentException("scene_texture is 0");

//		if(params.scene_width <= 0 || params.scene_height <= 0){
//			throw new IllegalArgumentException("invalid scene_texture dimension");
//		}
    }

    boolean debug_mode = false;
    int count = 0;

    void saveImage(int textureID){
        if(!debug_mode) return;

//        try {
//            TextureUtils.saveTextureAsText(GL11.GL_TEXTURE_2D, textureID, 0, "E:/compare_img/dst" + count + ".txt");
//            count++;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    /** Invoke the hdr post processing. The method will disable the CULL_FACE and the BLEND. */
    public void postprocessing(HDRParameters params){
        if(params.scene_width == 0 || params.scene_height == 0)
            return;
        this.params = params;
        if(!initilized){
            init();
            initilized = true;
        }

        view_width = view_height = -1;
        checkParameters(params);

//        fullscreenVAO.bind();
        downsample4x(params.scene_texture, params.scene_width, params.scene_height, blur_bufferA[0]);
//		saveImage(blur_bufferA[0].getColorTexture(0));

        if(params.enableHDR && params.autoExposure){
            downsample4x(blur_bufferA[0], exp_buffer[0]);
//			saveImage(exp_buffer[0].getColorTexture(0));
            downsample4x(exp_buffer[0], exp_buffer[1]);
            saveImage(exp_buffer[1].getColorTexture(0));
            calculateLuminance(params.elpsedTime);
//			saveImage(m_lum[1]);
//            GLError.checkError();
        }

        //Extract high light area for further processing.
        extractHL(blur_bufferA[LEVEL_0], compose_buffer[LEVEL_0]);
//    	saveImage(compose_buffer[0].getColorTexture(0));
//        GLError.checkError();
        //Gaussian blur on pyramid buffers.
        blur(compose_buffer[LEVEL_0],  blur_bufferA[LEVEL_0], blur_bufferB[LEVEL_0], BLURH4);
//    	saveImage(blur_bufferA[0].getColorTexture(0));

//        GLError.checkError();
        for (int i=LEVEL_1;i<LEVEL_TOTAL;i++) {
            downsample(compose_buffer[i-1], compose_buffer[i]);
//    		saveImage(compose_buffer[i].getColorTexture(0));
//            GLError.checkError();
            int ii = BLURH4 + i * 2;
            blur(compose_buffer[i], blur_bufferA[i], blur_bufferB[i], ii/*(BLURH4+i*2) > BLURH12 ? BLURH12 : (BLURH4+i*2)*/);
//    		saveImage(blur_bufferA[i].getColorTexture(0));
//            GLError.checkError();
        }
        //Generate streaks in 4 directions. Generate ghost image in 2 passes.
        if (params.glareType==FILMIC_GLARE) {
            if(params.enableLightStreaker) {
                genHorizontalGlare(0);
                genHorizontalGlare(1);
            }

            if(params.enableLensFlare) {
                genGhostImage(colorModulation.filmic_ghost_modulation1st, colorModulation.filmic_ghost_modulation2nd);
            }
        } else if (params.glareType==CAMERA_GLARE) {
            if(params.enableLightStreaker) {
                float ratio = (float) params.viewport_width / params.viewport_height;
                genStarStreak(0, ratio);
                genStarStreak(1, ratio);
                genStarStreak(2, ratio);
                genStarStreak(3, ratio);
            }

            if(params.enableLensFlare) {
                genGhostImage(colorModulation.camera_ghost_modulation1st, colorModulation.camera_ghost_modulation2nd);
            }
        }

        //Final glare composition.
        composeEffect();
//        GLError.checkError();

        //tonemapping to RGB888
        toneMappingPass();
//        GLError.checkError();

        GLES30.glActiveTexture(GLES30.GL_TEXTURE3);
        GLES30.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE2);
        GLES30.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
        GLES30.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        //exchange lunimance texture for next frame
        FrameBufferObject temp = m_lum[0];
        m_lum[0] = m_lum[1];
        m_lum[1] = temp;

//        fullscreenVAO.unbind();
        if(params.dest != null){
            FrameBufferObject.disableRenderToColorDepth();
//            GLError.checkError();
            GLES30.glViewport(0, 0, params.viewport_width, params.viewport_height);
        }
//        GLError.checkError();
//    	GL11.glFlush();
        this.params = null;
    }

    /*
     * read from float texture, apply tone mapping, render to regular RGB888 display
     */
    void toneMappingPass()
    {
        if(params.dest != null){
            activateFB(params.dest);
            GLES30.glClear(GL11.GL_DEPTH_BUFFER_BIT);
        }else{
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
            GLES30.glViewport(0, 0, params.viewport_width, params.viewport_height);
            GLES30.glClear(GL11.GL_DEPTH_BUFFER_BIT);
        }

//    	GL20.glUseProgram(m_shaders[TONEMAPPING].pid);
        tonemap.enable();

        saveImage(glare_buffer.getColorTexture(0));
//        saveImage(m_lum[1]);
        GLES30.glActiveTexture(/*tonemap.getSceneTexUnit()*/ GLES20.GL_TEXTURE0);
//        saveImage(params.scene_texture);
        GLES30.glBindTexture(GL11.GL_TEXTURE_2D, params.scene_texture);
//        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GLES30.glActiveTexture(/*tonemap.getBlurTexUnit()*/GLES20.GL_TEXTURE1);
        glare_buffer.bindColorTexture(0);
        GLES30.glActiveTexture(/*tonemap.getLumTexUnit()*/GLES20.GL_TEXTURE2);
        if(m_lum[1] != null)
            m_lum[1].bindColorTexture(0);
        else
            GLES30.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        //adaptive exposure adjustment in log space

//    	float newExp = (float)(exposureCompansation[m_sceneIndex] * Math.log(m_expAdjust+0.0001));

//    	GL20.glUniform1f(m_shaders[TONEMAPPING].auiLocation[0], m_blendAmount);
//    	GL20.glUniform1f(m_shaders[TONEMAPPING].auiLocation[1], newExp);
//    	GL20.glUniform1f(m_shaders[TONEMAPPING].auiLocation[2], m_gamma);
        tonemap.applyBlurAmout(params.blendAmount);
        tonemap.applyExposure(params.explosure);
        tonemap.applyGamma(params.gamma);

        drawAxisAlignedQuad(tonemap);
    }

    void composeEffect()
    {
        ////////compose gaussian blur buffers///////////////
        activateFB(compose_buffer[LEVEL_0]);
//    	GL20.glUseProgram(m_shaders[GAUSCOMP].pid);
        glareCompose.enable();

        GLES30.glActiveTexture(glareCompose.getSampler1Unit());
        blur_bufferA[LEVEL_0].bindColorTexture(0);
        GLES30.glActiveTexture(glareCompose.getSampler2Unit());
        blur_bufferA[LEVEL_1].bindColorTexture(0);
        GLES30.glActiveTexture(glareCompose.getSampler3Unit());
        blur_bufferA[LEVEL_2].bindColorTexture(0);
        GLES30.glActiveTexture(glareCompose.getSampler4Unit());
        blur_bufferA[LEVEL_3].bindColorTexture(0);

//    	float coeff[4]={0.3, 0.3, 0.25, 0.20};

//    	GL20.glUniform4f(m_shaders[GAUSCOMP].auiLocation[0], 0.3f, 0.3f, 0.25f, 0.20f);
        glareCompose.applyMixCoeff(0.3f, 0.3f, 0.25f, 0.20f);
        drawAxisAlignedQuad(glareCompose);

        ////////compose star streak from 4 directions///////////////
        if(params.enableLightStreaker) {
            activateFB(streak_bufferFinal);
            starCompose.enable();
            GLES30.glActiveTexture(starCompose.getSampler1Unit());
            streak_bufferA[0].bindColorTexture(0);
            GLES30.glActiveTexture(starCompose.getSampler2Unit());
            streak_bufferA[1].bindColorTexture(0);

            if (params.glareType == CAMERA_GLARE) {
                GLES30.glActiveTexture(starCompose.getSampler3Unit());
                streak_bufferA[2].bindColorTexture(0);
                GLES30.glActiveTexture(starCompose.getSampler4Unit());
                streak_bufferA[3].bindColorTexture(0);
            } else if (params.glareType == FILMIC_GLARE) {
                GLES30.glActiveTexture(starCompose.getSampler3Unit());
                GLES30.glBindTexture(GL11.GL_TEXTURE_2D, 0);
                GLES30.glActiveTexture(starCompose.getSampler4Unit());
                GLES30.glBindTexture(GL11.GL_TEXTURE_2D, 0);
            }

            drawAxisAlignedQuad(starCompose);
        }

        ////////////////final glare composition/////////////
        activateFB(glare_buffer);
        glareCompose.enable();

        GLES30.glActiveTexture(glareCompose.getSampler1Unit());
        compose_buffer[LEVEL_0].bindColorTexture(0);
        GLES30.glActiveTexture(glareCompose.getSampler2Unit());
        if(params.enableLightStreaker)
            streak_bufferFinal.bindColorTexture(0);
        else
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
        GLES30.glActiveTexture(glareCompose.getSampler3Unit());
        if(params.enableLensFlare)
            ghost2nd_buffer.bindColorTexture(0);
        else
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE3);
        GLES30.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        float[] mixCoeff = cameraMixCoeff;
        if (params.glareType == FILMIC_GLARE) mixCoeff = filmicMixCoeff;
        else if (params.glareType == CAMERA_GLARE) mixCoeff = cameraMixCoeff;

        glareCompose.applyMixCoeff(mixCoeff[0], mixCoeff[1], mixCoeff[2], mixCoeff[3]);
        drawAxisAlignedQuad(glareCompose);
    }

    void genStarStreak(final int dir, float ratio)
    {
        final float delta = 0.9f;
        int n,s,w,h;
//    	float[] step = new float[2];
        float step0 = 0, step1 = 0;
        float stride = 1.0f;
        Dimension size = starSize;
        w = size.getWidth();
        h = size.getHeight();
        float m_aspectRatio = ratio;
        switch (dir) {
            case 0:
                step1 = (delta)/w*m_aspectRatio;
                step0 = (delta)/w;
                break;
            case 1:
                step1 = (delta)/w*m_aspectRatio;
                step0 = -(delta)/w;
                break;
            case 2:
                step1 = -(delta)/w*m_aspectRatio;
                step0 = (delta)/w;
                break;
            case 3:
                step1 = -(delta)/w*m_aspectRatio;
                step0 = -(delta)/w;
                break;
            default:
                break;
        }

//    	GL20.glUseProgram(m_shaders[STARSTREAK].pid);
        starStreak.enable();

//    	final float  DEC = 0.9f;

        //3 passes to generate 64 pixel blur in each direction
        //1st pass
//    	float[] star_modulation1st = ColorModulation.star_modulation1st;
//    	n=1;
//    	for (s=0; s<4; s+=1) {
//    		colorCoeff[s*4] = star_modulation1st[s*4] * pow((DEC), pow((4),n-1)*s);
//    		colorCoeff[s*4+1] = star_modulation1st[s*4+1] * pow((DEC), pow((4),n-1)*s);
//    		colorCoeff[s*4+2] = star_modulation1st[s*4+2] * pow((DEC), pow((4),n-1)*s);
//    		colorCoeff[s*4+3] = star_modulation1st[s*4+3] * pow((DEC), pow((4),n-1)*s);
//    	}
//    	streak_bufferA[dir].enableRenderToColorAndDepth(0);
//    	GL11.glViewport(0, 0, streak_bufferA[dir].width, streak_bufferA[dir].height);
        activateFB(streak_bufferA[dir]);
        GLES30.glActiveTexture(/*starStreak.getTextureUnit()*/ GLES20.GL_TEXTURE0 );
        compose_buffer[LEVEL_0].bindColorTexture(0);

//    	GL20.glUniform2f(m_shaders[STARSTREAK].auiLocation[0], step[0], step[1]);
//    	GL20.glUniform1f(m_shaders[STARSTREAK].auiLocation[1], stride);
//    	GL20.glUniform4(m_shaders[STARSTREAK].auiLocation[2], wrap16(colorCoeff));
        starStreak.applyStepSize(step0, step1);
        starStreak.applyStride(stride);
        starStreak.applyColorCoffs(colorModulation.hori_passes[ColorModulation.STAR0]);
        if(count == -1){
            System.out.println("STAR0 = " + Arrays.toString(colorModulation.hori_passes[ColorModulation.STAR0]));
        }
        drawAxisAlignedQuad(starStreak);

        // 2nd pass
//    	float[] star_modulation2nd = ColorModulation.star_modulation2nd;
//    	n=2;
//    	for (s=0; s<4; s+=1) {
//    		colorCoeff[s*4] = star_modulation2nd[s*4] * pow((DEC), pow((4),n-1)*s);
//    		colorCoeff[s*4+1] = star_modulation2nd[s*4+1] * pow((DEC), pow((4),n-1)*s);
//    		colorCoeff[s*4+2] = star_modulation2nd[s*4+2] * pow((DEC), pow((4),n-1)*s);
//    		colorCoeff[s*4+3] = star_modulation2nd[s*4+3] * pow((DEC), pow((4),n-1)*s);
//    	}
        stride = 4;
//    	streak_bufferB[dir].enableRenderToColorAndDepth(0);
        activateFB(streak_bufferB);
//    	GL13.glActiveTexture(GL13.GL_TEXTURE0);
        streak_bufferA[dir].bindColorTexture(0);
//        GL20.glUniform2f(m_shaders[STARSTREAK].auiLocation[0], step[0], step[1]);
//        GL20.glUniform1f(m_shaders[STARSTREAK].auiLocation[1], stride);
//        GL20.glUniform4(m_shaders[STARSTREAK].auiLocation[2], wrap16(colorCoeff));
//    	drawAxisAlignedQuad(-1, -1, 1, 1);
        starStreak.applyStride(stride);
        starStreak.applyColorCoffs(colorModulation.hori_passes[ColorModulation.STAR1]);
        drawAxisAlignedQuad(starStreak);
        if(count == -1){
            System.out.println("STAR1 = " + Arrays.toString(colorModulation.hori_passes[ColorModulation.STAR1]));
        }

        // 3rd pass
//    	final float[] star_modulation3rd = ColorModulation.star_modulation3rd;
//    	n=3;
//    	for (s=0; s<4; s+=1) {
//    		colorCoeff[s*4] =  star_modulation3rd[s*4] * pow((DEC), pow((4),n-1)*s);
//    		colorCoeff[s*4+1] = star_modulation3rd[s*4+1] * pow((DEC), pow((4),n-1)*s);
//    		colorCoeff[s*4+2] = star_modulation3rd[s*4+2] * pow((DEC), pow((4),n-1)*s);
//    		colorCoeff[s*4+3] = star_modulation3rd[s*4+3] * pow((DEC), pow((4),n-1)*s);
//    	}
        stride = 16;
//    	streak_bufferA[dir].enableRenderToColorAndDepth(0);
        activateFB(streak_bufferA[dir]);
//    	GL13.glActiveTexture(GL13.GL_TEXTURE0);
        streak_bufferB.bindColorTexture(0);
//        GL20.glUniform2f(m_shaders[STARSTREAK].auiLocation[0], step[0], step[1]);
//        GL20.glUniform1f(m_shaders[STARSTREAK].auiLocation[1], stride);
//        GL20.glUniform4(m_shaders[STARSTREAK].auiLocation[2], wrap16(colorCoeff));
//    	drawAxisAlignedQuad(-1, -1, 1, 1);
        starStreak.applyStride(stride);
        starStreak.applyColorCoffs(colorModulation.hori_passes[ColorModulation.STAR2]);
        drawAxisAlignedQuad(starStreak);
        if(count == -1){
            System.out.println("STAR2 = " + Arrays.toString(colorModulation.hori_passes[ColorModulation.STAR2]));
        }
    }

    void genHorizontalGlare(int dir)
    {
        final float delta  = 0.9f;
        int n,s,w,h;
        float step0, step1;
        float stride = 1.0f;
        Dimension size = starSize;
        w = size.getWidth();
        h = size.getHeight();

        if (dir==0) {
            step0 = (delta)/w;
        }
        else {
            step0 = -(delta)/w;
        }
        step1 = 0;

//    	GL20.glUseProgram(m_shaders[STARSTREAK].pid);
        starStreak.enable();

//    	#undef DEC
//    	#define DEC 0.96
//    	final float DEC = 0.96f;
//    	float colorCoeff[16];

        //4 passes to generate 256 pixel blur in each direction
        //1st pass
//    	float[] hori_modulation1st = ColorModulation.hori_modulation1st;
//    	n=1;
//    	for (s=0; s<4; s+=1) {
//    		colorCoeff[s*4] = hori_modulation1st[s*4] * pow((DEC), pow((4),n-1)*s);
//    		colorCoeff[s*4+1] = hori_modulation1st[s*4+1] * pow((DEC), pow((4),n-1)*s);
//    		colorCoeff[s*4+2] = hori_modulation1st[s*4+2] * pow((DEC), pow((4),n-1)*s);
//    		colorCoeff[s*4+3] = hori_modulation1st[s*4+3] * pow((DEC), pow((4),n-1)*s);
//    	}
//    	streak_bufferB[dir].enableRenderToColorAndDepth(0);
//    	GL11.glViewport(0, 0, streak_bufferB[dir].width, streak_bufferB[dir].height);
        activateFB(streak_bufferB);
        GLES30.glActiveTexture(/*starStreak.getTextureUnit()*/GLES20.GL_TEXTURE0);
        compose_buffer[LEVEL_0].bindColorTexture(0);

//    	GL20.glUniform2f(m_shaders[STARSTREAK].auiLocation[0], step[0], step[1]);
//    	GL20.glUniform1f(m_shaders[STARSTREAK].auiLocation[1], stride);
//    	GL20.glUniform4(m_shaders[STARSTREAK].auiLocation[2], wrap16(colorCoeff));
        starStreak.applyColorCoffs(colorModulation.hori_passes[ColorModulation.HORI0]);
        starStreak.applyStepSize(step0, step1);
        starStreak.applyStride(stride);
        drawAxisAlignedQuad(starStreak);

        // 2nd pass
//    	n=2;
//    	for (s=0; s<4; s+=1) {
//    		colorCoeff[s*4] = hori_modulation1st[s*4] * pow((DEC), pow((4),n-1)*s);
//    		colorCoeff[s*4+1] = hori_modulation1st[s*4+1] * pow((DEC), pow((4),n-1)*s);
//    		colorCoeff[s*4+2] = hori_modulation1st[s*4+2] * pow((DEC), pow((4),n-1)*s);
//    		colorCoeff[s*4+3] = hori_modulation1st[s*4+3] * pow((DEC), pow((4),n-1)*s);
//    	}
        stride = 4;
//    	streak_bufferA[dir].activateFB();
//    	GL13.glActiveTexture(GL13.GL_TEXTURE0);
//        streak_bufferB[dir].bind();
//    	streak_bufferA[dir].enableRenderToColorAndDepth(0);
        activateFB(streak_bufferA[dir]);
        streak_bufferB.bindColorTexture(0);

//        GL20.glUniform2f(m_shaders[STARSTREAK].auiLocation[0], step[0], step[1]);
//        GL20.glUniform1f(m_shaders[STARSTREAK].auiLocation[1], stride);
//        GL20.glUniform4(m_shaders[STARSTREAK].auiLocation[2], wrap16(colorCoeff));
//    	drawAxisAlignedQuad(-1, -1, 1, 1);
        starStreak.applyColorCoffs(colorModulation.hori_passes[ColorModulation.HORI1]);
//        starStreak.applyStepSize(step[0], step[1]);
        starStreak.applyStride(stride);
        drawAxisAlignedQuad(starStreak);

        // 3rd pass
//    	float[] hori_modulation2nd = ColorModulation.hori_modulation2nd;
//    	n=3;
//    	for (s=0; s<4; s+=1) {
//    		colorCoeff[s*4] =  hori_modulation2nd[s*4] * pow((DEC), pow((4),n-1)*s);
//    		colorCoeff[s*4+1] = hori_modulation2nd[s*4+1] * pow((DEC), pow((4),n-1)*s);
//    		colorCoeff[s*4+2] = hori_modulation2nd[s*4+2] * pow((DEC), pow((4),n-1)*s);
//    		colorCoeff[s*4+3] = hori_modulation2nd[s*4+3] * pow((DEC), pow((4),n-1)*s);
//    	}
        stride = 16;
//    	streak_bufferB[dir].activateFB();
//    	GL13.glActiveTexture(GL13.GL_TEXTURE0);
//        streak_bufferA[dir].bind();
//    	streak_bufferB[dir].enableRenderToColorAndDepth(0);
        activateFB(streak_bufferB);
        streak_bufferA[dir].bindColorTexture(0);

//        GL20.glUniform2f(m_shaders[STARSTREAK].auiLocation[0], step[0], step[1]);
//        GL20.glUniform1f(m_shaders[STARSTREAK].auiLocation[1], stride);
//        GL20.glUniform4(m_shaders[STARSTREAK].auiLocation[2], wrap16(colorCoeff));
//    	drawAxisAlignedQuad(-1, -1, 1, 1);
        starStreak.applyColorCoffs(colorModulation.hori_passes[ColorModulation.HORI2]);
//      starStreak.applyStepSize(step[0], step[1]);
        starStreak.applyStride(stride);
        drawAxisAlignedQuad(starStreak);

        // 4rd pass
//    	float[] hori_modulation3rd = ColorModulation.hori_modulation3rd;
//    	n=4;
//    	for (s=0; s<4; s+=1) {
//    		colorCoeff[s*4] =  hori_modulation3rd[s*4] * pow((DEC), pow((4),n-1)*s);
//    		colorCoeff[s*4+1] = hori_modulation3rd[s*4+1] * pow((DEC), pow((4),n-1)*s);
//    		colorCoeff[s*4+2] = hori_modulation3rd[s*4+2] * pow((DEC), pow((4),n-1)*s);
//    		colorCoeff[s*4+3] = hori_modulation3rd[s*4+3] * pow((DEC), pow((4),n-1)*s);
//    	}
        stride = 64;
//    	streak_bufferA[dir].activateFB();
//    	GL13.glActiveTexture(GL13.GL_TEXTURE0);
//        streak_bufferB[dir].bind();
//        GL20.glUniform2f(m_shaders[STARSTREAK].auiLocation[0], step[0], step[1]);
//        GL20.glUniform1f(m_shaders[STARSTREAK].auiLocation[1], stride);
//        GL20.glUniform4(m_shaders[STARSTREAK].auiLocation[2], wrap16(colorCoeff));
//    	drawAxisAlignedQuad(-1, -1, 1, 1);
//    	streak_bufferA[dir].enableRenderToColorAndDepth(0);
        activateFB(streak_bufferA[dir]);
        streak_bufferB.bindColorTexture(0);
        starStreak.applyColorCoffs(colorModulation.hori_passes[ColorModulation.HORI3]);
        starStreak.applyStride(stride);
        drawAxisAlignedQuad(starStreak);
    }

    void genGhostImage(float[] ghost_modulation1st, float[] ghost_modulation2nd)
    {
//    	GL20.glUseProgram(m_shaders[GHOSTIMAGE].pid);
        ghostImage.enable();

//    	ghost1st_buffer.enableRenderToColorAndDepth(0);
//    	GL11.glViewport(0, 0, ghost1st_buffer.width, ghost1st_buffer.height);
        activateFB(ghost1st_buffer);

        GLES30.glActiveTexture(ghostImage.getSampler1Unit());
        blur_bufferA[LEVEL_0].bindColorTexture(0);
        GLES30.glActiveTexture(ghostImage.getSampler2Unit());
        blur_bufferA[LEVEL_1].bindColorTexture(0);
        GLES30.glActiveTexture(ghostImage.getSampler3Unit());
        blur_bufferA[LEVEL_1].bindColorTexture(0);
        GLES30.glActiveTexture(ghostImage.getSampler4Unit());
        GLES30.glBindTexture(GL11.GL_TEXTURE_2D, m_lensMask);

//    	GL20.glUniform4f(m_shaders[GHOSTIMAGE].auiLocation[0], -4.0f, 3.0f, -2.0f, 0.3f);
//    	GL20.glUniform4(m_shaders[GHOSTIMAGE].auiLocation[1], wrap16(ghost_modulation1st));
        ghostImage.applyScalar(-4.0f, 3.0f, -2.0f, 0.3f);
        ghostImage.applyColorCoffs(ghost_modulation1st);
        drawAxisAlignedQuad(ghostImage);

        activateFB(ghost2nd_buffer);
        GLES30.glActiveTexture(ghostImage.getSampler1Unit());
        ghost1st_buffer.bindColorTexture(0);
        GLES30.glActiveTexture(ghostImage.getSampler2Unit());
        ghost1st_buffer.bindColorTexture(0);
        GLES30.glActiveTexture(ghostImage.getSampler3Unit());
        blur_bufferA[LEVEL_1].bindColorTexture(0);
        GLES30.glActiveTexture(ghostImage.getSampler4Unit());
        GLES30.glBindTexture(GL11.GL_TEXTURE_2D, m_lensMask);

//        GL20.glUniform4f(m_shaders[GHOSTIMAGE].auiLocation[0], 3.6f, 2.0f, 0.9f, -0.55f);
//        GL20.glUniform4(m_shaders[GHOSTIMAGE].auiLocation[1], wrap16(ghost_modulation2nd));
        ghostImage.applyScalar(3.6f, 2.0f, 0.9f, -0.55f);
        ghostImage.applyColorCoffs(ghost_modulation2nd);
        drawAxisAlignedQuad(ghostImage);
    }

    // downsample image 2x in each dimension
    void downsample(FrameBufferObject src, FrameBufferObject dest)
    {
//        dest.enableRenderToColorAndDepth(0);
//        GL11.glViewport(0, 0, dest.width, dest.height);
        activateFB(dest);

//    	GL20.glUseProgram(m_shaders[DOWNSAMPLE].pid);
//    	GL11.glDisable(GL11.GL_CULL_FACE);
//    	GL11.glDisable(GL11.GL_BLEND);
        downSample.enable();

        GLES30.glActiveTexture(/*downSample.getTextureUnit()*/ GLES20.GL_TEXTURE0);
        src.bindColorTexture(0);
        drawAxisAlignedQuad(downSample);
//        src.release();

    }

    void blur(FrameBufferObject src, FrameBufferObject dest, FrameBufferObject temp, int blurWidth)
    {
        run_pass(blurWidth, src, temp);
        run_pass(blurWidth+1, temp, dest);
    }

    void run_pass(int prog, FrameBufferObject src, FrameBufferObject dest)
    {
//        dest.enableRenderToColorAndDepth(0);
//        GL11.glViewport(0, 0, dest.width, dest.height);
        activateFB(dest);

//    	GL20.glUseProgram(m_shaders[prog].pid);

//    	GL11.glDisable(GL11.GL_CULL_FACE);
//    	GL11.glDisable(GL11.GL_BLEND);
        blurs[prog].enable();

        GLES30.glActiveTexture(/*blurs[prog].getTextureUnit()*/ GLES30.GL_TEXTURE0);
        src.bindColorTexture(0);

        drawAxisAlignedQuad(blurs[prog]);

//        src.release();
    }

    void extractHL(FrameBufferObject source, FrameBufferObject dest){
//		dest.enableRenderToColorAndDepth(0);
//		GL11.glViewport(0, 0, dest.width, dest.height);
        activateFB(dest);

//    	GL20.glUseProgram(m_shaders[EXTRACTHL].pid);
//    	GL11.glDisable(GL11.GL_CULL_FACE);
//    	GL11.glDisable(GL11.GL_BLEND);

//    	GL20.glUniform1f(m_shaders[EXTRACTHL].auiLocation[0], m_lumThreshold);
//    	GL20.glUniform1f(m_shaders[EXTRACTHL].auiLocation[1], m_lumScaler);
        extractHL.enable();
        extractHL.applyScalar(params.lumScaler);
        extractHL.applyThreshold(params.lumThreshold);

        GLES30.glActiveTexture(/*extractHL.getTextureUnit()*/GLES30.GL_TEXTURE0);
        source.bindColorTexture(0);
        drawAxisAlignedQuad(extractHL);
        GLES30.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    void drawAxisAlignedQuad(SimpleOpenGLProgram program) {
//		fullscreenVAO.bind();
//        fullscreenVAO.draw(GL11.GL_TRIANGLES);
//		fullscreenVAO.unbind();

        NvShapes.drawQuad(program.getAttribPosition(), program.getAttribTexCoord());
    }

    int lum_count;
    float max_lum;
    void calculateLuminance(float elpsedTime){
//		GL20.glUseProgram( m_shaders[CALCLUMINANCE].pid );
//		ByteBuffer p = TextureUtils.getTextureData(GL11.GL_TEXTURE_2D, m_lumCurrent, 0, true);
//    	short _s = p.getShort();
//    	System.out.println("scene_lum: " + HDRImage.convertHFloatToFloat(_s));
        calLuminance.enable();
//        GLES31.glBindImageTexture(0, exp_buffer[1].getColorTexture(0), 0, false, 0, GL15.GL_READ_ONLY, GL30.GL_RGBA16F);
//        GL42.glBindImageTexture(1, m_lumCurrent, 0, false, 0, GL15.GL_WRITE_ONLY, GL30.GL_RGBA16F);
//        GL43.glDispatchCompute(1, 1, 1);
        activateFB(m_lumCurrent);
        GLES30.glActiveTexture(/*extractHL.getTextureUnit()*/GLES30.GL_TEXTURE0);
        exp_buffer[1].bindColorTexture(0);
        drawAxisAlignedQuad(calLuminance);

//    	GL42.glMemoryBarrier(GL42.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);

//    	if(lum_count % 20 == 0){
//    		try {
//    			TextureUtils.saveTextureAsText(GL11.GL_TEXTURE_2D, exp_buffer[1].getColorTexture(0), 0, "E:/compare_img/lum" + (lum_count/20) + ".txt");
//    			count++;
//    		} catch (IOException e) {
//    			e.printStackTrace();
//    		}
//    	}
//    	lum_count++;

//    	GL20.glUseProgram( m_shaders[CALCADAPTEDLUM].pid );
        calAdapted.enable();
//        GL42.glBindImageTexture(0, m_lumCurrent, 0, false, 0, GL15.GL_READ_ONLY, GL30.GL_RGBA16F);
//        GL42.glBindImageTexture(1, m_lum[0], 0, false, 0, GL15.GL_READ_ONLY, GL30.GL_RGBA16F);
//        GL42.glBindImageTexture(2, m_lum[1], 0, false, 0, GL15.GL_WRITE_ONLY, GL30.GL_RGBA16F);
//    	GL20.glUniform1f(m_shaders[CALCADAPTEDLUM].auiLocation[0], );
        calAdapted.applyElapsedTime(elpsedTime);
//        GL43.glDispatchCompute(1, 1, 1);
//        GL42.glMemoryBarrier(GL42.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);
//        saveImage(m_lum[1]);
        activateFB(m_lum[1]);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        m_lumCurrent.bindColorTexture(0);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
        m_lum[0].bindColorTexture(0);
        drawAxisAlignedQuad(calAdapted);

//    	ByteBuffer pixles = TextureUtils.getTextureData(GL11.GL_TEXTURE_2D, m_lum[1], 0, true);
//    	short s = pixles.getShort();
//    	float f = HDRImage.convertHFloatToFloat(s);
//    	max_lum = Math.max(max_lum, f);
//    	System.out.println("lum1: " + f + ", max: " + max_lum);
//    	pixles = TextureUtils.getTextureData(GL11.GL_TEXTURE_2D, m_lumCurrent, 0, true);
//    	s = pixles.getShort();
//    	System.out.println("scene_lum: " + HDRImage.convertHFloatToFloat(s));
    }

    void downsample4x(FrameBufferObject src, FrameBufferObject dest){
        downsample4x(src.getColorTexture(0), src.getWidth(), src.getHeight(), dest);
    }

    // downsample image 4x in each dimension
    void downsample4x(int src, int width, int height, FrameBufferObject dest)
    {
//        dest.enableRenderToColorAndDepth(0);
//        GL11.glViewport(0, 0, dest.width, dest.height);
        activateFB(dest);

//    	GL20.glUseProgram(m_shaders[DOWNSAMPLE4X].pid);
        float twoPixelX = 2.0f/width;
        float twoPixelY = 2.0f/height;

//    	GL20.glUniform2f(m_shaders[DOWNSAMPLE4X].auiLocation[0], twoPixelX, twoPixelY);

        downSample4x.enable();
        downSample4x.setTwoTexelSize(twoPixelX, twoPixelY);

        GLES30.glActiveTexture(/*downSample4x.getTextureUnit()*/GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GL11.GL_TEXTURE_2D, src);
        drawAxisAlignedQuad(downSample4x);
//        fullscreenVAO.draw(GL11.GL_TRIANGLES);
        GLES30.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }
}
