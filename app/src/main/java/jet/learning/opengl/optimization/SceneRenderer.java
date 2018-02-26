////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2014, NVIDIA CORPORATION. All rights reserved.
// Copyright (c) 2018 mzhg
//
// Licensed under the Apache License, Version 2.0 (the "License"); you may not
// use this file except in compliance with the License.  You may obtain a copy
// of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
// License for the specific language governing permissions and limitations
// under the License.
////////////////////////////////////////////////////////////////////////////////
package jet.learning.opengl.optimization;

import android.opengl.GLES20;
import android.opengl.GLES30;

import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.NvCPUTimer;
import com.nvidia.developer.opengl.utils.NvDisposeable;
import com.nvidia.developer.opengl.utils.NvGLModel;
import com.nvidia.developer.opengl.utils.NvGLSLProgram;
import com.nvidia.developer.opengl.utils.NvImage;
import com.nvidia.developer.opengl.utils.NvLogger;
import com.nvidia.developer.opengl.utils.NvSimpleFBO;
import com.nvidia.developer.opengl.utils.NvUtils;
import com.nvidia.developer.opengl.utils.NvWritableFB;
import com.nvidia.developer.opengl.utils.VectorUtil;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.microedition.khronos.opengles.GL11;

/**
 * Created by mazhen'gui on 2018/2/6.
 */

final class SceneRenderer implements NvDisposeable {
    private static final int RENDER_NONE = 0;
    private static final int RENDER_ALPHA = 1;
    private static final int RENDER_SOLID = 2;
    private static final int RENDER_ALL = 3;

    private static final int GPU_TIMER_SCENE_DEPTH = 0;
    private static final int GPU_TIMER_PARTICLES = 1;
    private static final int GPU_TIMER_SCENE_COLOR = 2;
    private static final int GPU_TIMER_UPSAMPLE_PARTICLES = 3;
    private static final int GPU_TIMER_UPSAMPLE_SCENE = 4;
    private static final int GPU_TIMER_TOTAL = 5;
    private static final int GPU_TIMER_COUNT = 6;

    private static final int CPU_TIMER_SCENE_DEPTH = 0;
    private static final int CPU_TIMER_PARTICLES = 1;
    private static final int CPU_TIMER_SCENE_COLOR = 2;
    private static final int CPU_TIMER_UPSAMPLE_PARTICLES = 3;
    private static final int CPU_TIMER_UPSAMPLE_SCENE = 4;
    private static final int CPU_TIMER_TOTAL = 5;
    private static final int CPU_TIMER_COUNT = 6;

    final static int STATS_FRAMES = 60;

    final SceneInfo m_scene = new SceneInfo();
    List<MeshObj> m_models  = new ArrayList<MeshObj>();
    Terrain                        m_pTerrain;
    Map<String, NvGLModel> m_modelStorage = new HashMap<String, NvGLModel>();
    Map<String, Integer  > m_texStorage   = new HashMap<String, Integer>();

    final Params m_params = new Params();
    ParticleRenderer m_particles;
    Upsampler m_upsampler;

    final MatrixStorage m_matrixStorage = new MatrixStorage(); // for the drawScene method.
    final MatrixStorage m_mats = new MatrixStorage(); // for the drawScene method.

//    NvGPUTimer[] m_GPUTimers = new NvGPUTimer[GPU_TIMER_COUNT];
    NvCPUTimer[] m_CPUTimers = new NvCPUTimer[CPU_TIMER_COUNT];

    int m_statsCountdown;

    int m_screenWidth;
    int m_screenHeight;
    SceneFBOs m_fbos;

    OpaqueColorProgram m_opaqueColorProg;
    OpaqueDepthProgram m_opaqueSolidDepthProg;
    OpaqueDepthProgram m_opaqueAlphaDepthProg;

    public SceneRenderer(boolean isES2) {
        initTimers();

        // Call this early to give it time to multi-thread init.
        m_particles = new ParticleRenderer(isES2);

        m_texStorage.put("floor", NvImage.uploadTextureFromDDSFile("optimization/tex1.dds"));
        m_texStorage.put("white_dummy", NvImage.uploadTextureFromDDSFile("optimization/white_dummy.dds"));

        m_modelStorage.put("T34-85", loadModelFromFile("optimization/T34-85.obj", 30.0f));
        m_modelStorage.put("cow",    loadModelFromFile("optimization/cow.obj",    10.0f));

        final float treeOffset = 20;
        NvImage.upperLeftOrigin(false);
//	        m_texStorage["palm"]   = NvImage::UploadTextureFromDDSFile("images/palm.dds");
        m_texStorage.put("palm", NvImage.uploadTextureFromDDSFile("optimization/palm.dds"));
        NvImage.upperLeftOrigin(true);
//	        m_modelStorage["palm"] = loadModelFromFile("models/palm_tree.obj", 50.0f);
        m_modelStorage.put("palm", loadModelFromFile("optimization/palm_tree.obj", 50.0f));

        // create terrain
//	        nv::vec3f scale(1000.0f, 100.0, 1000.0f);
//	        nv::vec3f translate(0.0f, -45.0, 0.0f);
//	        nv::matrix4f scaleMat, translateMat;
//	        scaleMat.set_scale(scale);
//	        translateMat.set_translate(translate);

        TerrainInput input = new TerrainInput();
        input.heightmap = "optimization/terrain_heightmap.dds";
        input.colormap  = "optimization/terrain_colormap.dds";
//	        input.transform = translateMat*scaleMat;
        input.transform.translate(0, -45, 0);
        input.transform.scale(1000.0f, 100.0f, 1000.0f);
        input.subdivsX  = 128;
        input.subdivsY  = 128;
        m_pTerrain = new Terrain(input);

        createRandomObjectsOnLandScape(m_models, m_modelStorage, m_texStorage, treeOffset);
//	        std::sort(m_models.begin(), m_models.end(), ObjectSorter());
        Collections.sort(m_models);

        m_opaqueColorProg = new OpaqueColorProgram(isES2);
        m_opaqueSolidDepthProg = new OpaqueDepthProgram("optimization/unshaded_solid.frag");
        m_opaqueAlphaDepthProg = new OpaqueDepthProgram("optimization/unshaded_alpha.frag");

        m_fbos = new SceneFBOs();

        m_upsampler = new Upsampler(m_fbos);

        // Disable particle self-shadowing and render all particles in one draw call (slice).
        getParticleParams().numSlices = 1;

        m_scene.setLightVector(new Vector3f(-0.70710683f, 0.50000000f, 0.49999994f));
        m_scene.setLightDistance(6.f);
        m_scene.m_fbos = m_fbos;

        m_scene.m_lightAmbient = 0.15f;
        m_scene.m_lightDiffuse = 0.85f;

        m_statsCountdown = STATS_FRAMES;
    }

    @Override
    public void dispose() {
        m_fbos.dispose();
        m_opaqueColorProg.dispose();
        m_opaqueSolidDepthProg.dispose();
        m_opaqueAlphaDepthProg.dispose();
        m_particles.dispose();
    }

    void initTimers()
    {
        for (int i = 0; i < GPU_TIMER_COUNT; ++i)
        {
            /*if(m_GPUTimers[i] == null)
                m_GPUTimers[i] = new NvGPUTimer();
            m_GPUTimers[i].init();*/
        }

        for (int i = 0; i < CPU_TIMER_COUNT; ++i)
        {
            if(m_CPUTimers[i] == null)
                m_CPUTimers[i] = new NvCPUTimer();
            m_CPUTimers[i].init();
        }
    }

    void drawScene(NvGLSLProgram a_proc, MatrixStorage mats, int doAlpha, boolean a_renderDepth)
    {
        MatrixStorage ncMats = m_matrixStorage;
        ncMats.set(mats);

        int positionAttrHandle = a_proc.getAttribLocation("g_Position");
        int normalAttrHandle   = a_proc.getAttribLocation("g_Normal", false);
        int texCoordAttrHandle = a_proc.getAttribLocation("g_TexCoord", false);

        // these matrices cam be set once
        a_proc.setUniformMatrix4("g_ProjectionMatrix", ncMats.m_projection, false);
        a_proc.setUniform1f("g_lightAmbient", m_scene.m_lightAmbient);
        a_proc.setUniform1f("g_lightDiffuse", m_scene.m_lightDiffuse);
        a_proc.setUniform3f("g_lightDirection", m_scene.m_lightVector.x, m_scene.m_lightVector.y, m_scene.m_lightVector.z);

        // Note that we are drawing the terrain first which is a sub-optimal rendering order and a deliberate mistake.
        if (!a_renderDepth && ((doAlpha & RENDER_SOLID) != 0))
        {
//	        nv::matrix4f modelMatrix;
//	        modelMatrix.make_identity();
//	        modelMatrix.set_scale(nv::vec3f(1.0f, 1.0, 1.0f));

            ncMats.m_model.setIdentity();
//	        ncMats.m_model.scale(vec3f(1.0f, 1.0f,1.0f));
            ncMats.multiply();

            a_proc.setUniformMatrix4("g_ModelViewMatrix",           ncMats.m_modelView);
            a_proc.setUniformMatrix4("g_ModelViewProjectionMatrix", ncMats.m_modelViewProjection);
            a_proc.setUniformMatrix4("g_LightModelViewMatrix",      m_scene.m_lightView);

            a_proc.setUniform4f("g_color", 1.0f, 1.0f, 1.0f, 1.0f);

            a_proc.bindTexture2D("g_texture", 0, m_pTerrain.getColorTex());
            GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            m_pTerrain.draw(positionAttrHandle, normalAttrHandle, texCoordAttrHandle);
        }

        // render objects
        for(int i = 0; i < m_models.size(); i++)
        {
            MeshObj mesh = m_models.get(i);

            if (doAlpha == RENDER_ALL || (doAlpha == RENDER_ALPHA && mesh.m_alphaTest) || (doAlpha == RENDER_SOLID && !mesh.m_alphaTest))
            {
                ncMats.m_model.load(mesh.m_modelMatrix);
                ncMats.multiply();

                a_proc.setUniformMatrix4("g_ModelViewMatrix",           ncMats.m_modelView);
                a_proc.setUniformMatrix4("g_ModelViewProjectionMatrix", ncMats.m_modelViewProjection);
//                printMatrixLog("set modelViewProjection", ncMats.m_modelViewProjection);

                if ((doAlpha & RENDER_ALPHA) !=0) {
                    a_proc.bindTexture2D("g_texture", 0, mesh.m_texId);
                    GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
                    GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
                }

                if(!a_renderDepth)
                {
                    a_proc.setUniform4f("g_color", mesh.m_color.x, mesh.m_color.y, mesh.m_color.z, mesh.m_color.w);
                    a_proc.setUniform1f("g_lightSpecular", mesh.m_specularValue);
                }

                if (mesh.m_cullFacing)
                    GLES20.glEnable(GL11.GL_CULL_FACE);
                else
                    GLES20.glDisable(GL11.GL_CULL_FACE);

                mesh.m_pModelData.drawElements(positionAttrHandle, normalAttrHandle, texCoordAttrHandle);
            }
        }
        GLES20.glEnable(GL11.GL_CULL_FACE);
    }

    // render the opaque geometry to the depth buffer
    void renderSceneDepth(MatrixStorage mats, NvWritableFB depthFbo)
    {
        // bind the FBO and set the viewport to the FBO resolution
        depthFbo.bind();

        // depth-only pass, disable color writes
        GLES20.glColorMask(false, false, false, false);

        // enable depth testing and depth writes
        GLES20.glEnable(GL11.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GL11.GL_LESS);
        GLES20.glDepthMask(true);

        // clear depths to 1.0
        GLES20.glClearDepthf(1.0f);
        GLES20.glClear(GL11.GL_DEPTH_BUFFER_BIT);

        // draw the geometry with a dummy fragment shader
        m_opaqueSolidDepthProg.enable();
        m_opaqueSolidDepthProg.setUniforms(m_scene);
        drawScene(m_opaqueSolidDepthProg, mats, RENDER_SOLID, false);

        m_opaqueAlphaDepthProg.enable();
        m_opaqueAlphaDepthProg.setUniforms(m_scene);
        drawScene(m_opaqueAlphaDepthProg, mats, RENDER_ALPHA, false);

        // restore color writes
        GLES20.glColorMask(true, true, true, true);
        GLES.checkGLError();
    }

    void downsampleSceneDepth(MatrixStorage mats, NvSimpleFBO srcFbo, NvWritableFB dstFbo)
    {
        GLES.checkGLError();
        GLES20.glBindFramebuffer(GLES30.GL_READ_FRAMEBUFFER, srcFbo.fbo);
        GLES20.glBindFramebuffer(GLES30.GL_DRAW_FRAMEBUFFER, dstFbo.fbo);

        GLES30.glBlitFramebuffer(0, 0, srcFbo.width, srcFbo.height,
                0, 0, dstFbo.width, dstFbo.height,
                GL11.GL_DEPTH_BUFFER_BIT, GL11.GL_NEAREST);
        GLES.checkGLError();
    }

    // initialize the depth buffer to depth-test the low-res particles against
    void renderLowResSceneDepth( MatrixStorage mats)
    {
        if (m_params.useDepthPrepass /*&& glBlitFramebufferFunc*/) {
            downsampleSceneDepth(mats, m_fbos.m_sceneFbo, m_fbos.m_particleFbo);
            return;
        }

        if (getParticleParams().render)
            renderSceneDepth(mats, m_fbos.m_particleFbo);
    }

    // render the colors of the opaque geometry, receiving shadows from the particles
    void renderFullResSceneColor(MatrixStorage mats)
    {
        GLES30.glClearColor(m_params.backgroundColor.x, m_params.backgroundColor.y, m_params.backgroundColor.z, 0.0f);
        GLES30.glEnable(GL11.GL_DEPTH_TEST);
        m_fbos.m_sceneFbo.bind();

        m_opaqueColorProg.enable();
        m_opaqueColorProg.setUniforms(m_scene);

        if (m_params.useDepthPrepass)
        {
            // if we are using the depth pre-pass strategy, then re-use the full-resolution depth buffer
            // initialized earlier and perform a z-equal pass against it with depth writes disabled.

            GLES30.glDepthFunc(GL11.GL_LEQUAL);
            GLES30.glDepthMask(false);

            GLES30.glClear(GL11.GL_COLOR_BUFFER_BIT);

            drawScene(m_opaqueColorProg, mats, RENDER_ALL, false);

            GLES30.glDepthFunc(GL11.GL_LESS);
            GLES30.glDepthMask(true);
        }
        else
        {
            GLES30.glDepthFunc(GL11.GL_LESS);
            GLES30.glDepthMask(true);

            GLES30.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

            drawScene(m_opaqueColorProg, mats, RENDER_ALL, false);
        }
    }

    void renderParticles(MatrixStorage mats)
    {
        MatrixStorage matsCopy = m_matrixStorage;
        matsCopy.set(mats);
        matsCopy.m_model.setIdentity();
        matsCopy.multiply();

        NvGLSLProgram prog = m_particles.getCameraViewProgram();
        prog.enable();
        prog.setUniformMatrix4("g_LightModelViewMatrix", m_scene.m_lightView);
        m_particles.renderParticles(m_scene);
    }

    void setupMatrices(MatrixStorage matContainer)
    {
        matContainer.m_view.load(m_scene.m_eyeView);
        matContainer.m_projection.load(m_scene.m_eyeProj);
    }

    void updateFrame(float frameElapsed)
    {
        if (m_particles.getParams().render)
        {
            m_scene.calcVectors();
            m_particles.simulate(m_scene, frameElapsed);
            m_particles.updateBuffers();
        }
    }

    void renderFrame()
    {
        m_fbos.updateBuffers();

        // Update screen FBO
        m_fbos.m_backBufferFbo.fbo = GLES.glGetInteger(0x8CA6);

//	        CPU_TIMER_SCOPE(CPU_TIMER_TOTAL);
//	        GPU_TIMER_SCOPE(GPU_TIMER_TOTAL);
        m_CPUTimers[CPU_TIMER_TOTAL].start();
//        m_GPUTimers[GPU_TIMER_TOTAL].start();

        m_mats.setIdentity();
        MatrixStorage mats = m_mats;
        setupMatrices(mats);

        {
            // render a full-screen depth pass.
//	            CPU_TIMER_SCOPE(CPU_TIMER_SCENE_DEPTH);
//	            GPU_TIMER_SCOPE(GPU_TIMER_SCENE_DEPTH);
            m_CPUTimers[CPU_TIMER_SCENE_DEPTH].start();
//            m_GPUTimers[GPU_TIMER_SCENE_DEPTH].start();
            if (m_params.useDepthPrepass)
                renderSceneDepth(mats, m_fbos.m_sceneFbo);

            // render scene depth to buffer for particle to be depth tested against
            // This may just down-sample the above depth pre-pass.
            renderLowResSceneDepth(mats);
            m_CPUTimers[CPU_TIMER_SCENE_DEPTH].stop();
//            m_GPUTimers[GPU_TIMER_SCENE_DEPTH].stop();
        }

        {
            // the opaque colors need to be rendered after the particles
            //LOGI("OE renderFullResSceneColor\n");
//	            CPU_TIMER_SCOPE(CPU_TIMER_SCENE_COLOR);
//	            GPU_TIMER_SCOPE(GPU_TIMER_SCENE_COLOR);
            m_CPUTimers[CPU_TIMER_SCENE_COLOR].start();
//            m_GPUTimers[GPU_TIMER_SCENE_COLOR].start();
            renderFullResSceneColor(mats);
            m_CPUTimers[CPU_TIMER_SCENE_COLOR].stop();
//            m_GPUTimers[GPU_TIMER_SCENE_COLOR].stop();
        }

        if (m_particles.getParams().render)
        {
//	            CPU_TIMER_SCOPE(CPU_TIMER_PARTICLES);
//	            GPU_TIMER_SCOPE(GPU_TIMER_PARTICLES);
            m_CPUTimers[CPU_TIMER_PARTICLES].start();
//            m_GPUTimers[GPU_TIMER_PARTICLES].start();
            renderParticles(mats);

            if (m_particles.getParams().renderLowResolution)
            {
                // upsample the particles & composite them on top of the opaque scene colors
//	                CPU_TIMER_SCOPE(CPU_TIMER_UPSAMPLE_PARTICLES);
//	                GPU_TIMER_SCOPE(GPU_TIMER_UPSAMPLE_PARTICLES);
                m_CPUTimers[CPU_TIMER_UPSAMPLE_PARTICLES].start();
//                m_GPUTimers[GPU_TIMER_UPSAMPLE_PARTICLES].start();
                m_upsampler.upsampleParticleColors(m_fbos.m_sceneFbo);
                m_CPUTimers[CPU_TIMER_UPSAMPLE_PARTICLES].stop();
//                m_GPUTimers[GPU_TIMER_UPSAMPLE_PARTICLES].stop();
            }
            m_CPUTimers[CPU_TIMER_PARTICLES].stop();
//            m_GPUTimers[GPU_TIMER_PARTICLES].stop();
        }

        {
            // final bilinear upsampling from scene resolution to backbuffer resolution
//	            CPU_TIMER_SCOPE(CPU_TIMER_UPSAMPLE_SCENE);
//	            GPU_TIMER_SCOPE(GPU_TIMER_UPSAMPLE_SCENE);
            m_CPUTimers[CPU_TIMER_UPSAMPLE_SCENE].start();
//            m_GPUTimers[GPU_TIMER_UPSAMPLE_SCENE].start();
            m_upsampler.upsampleSceneColors(m_fbos.m_backBufferFbo);
            m_CPUTimers[CPU_TIMER_UPSAMPLE_SCENE].stop();
//            m_GPUTimers[GPU_TIMER_UPSAMPLE_SCENE].stop();
        }

        m_particles.swapBuffers();
        m_CPUTimers[CPU_TIMER_TOTAL].stop();
//        m_GPUTimers[GPU_TIMER_TOTAL].stop();
    }

    String stats()
    {
        final String TIMER_PATTERN =
//                "GPU Scene depth: %5.1f%%\n" +
                "CPU Scene depth: %5.1f%%\n" +
//                "GPU Scene color: %5.1f%%\n" +
                "CPU Scene color: %5.1f%%\n" +
//                "GPU Scene upsamp: %5.1f%%\n"+
                "CPU Scene upsamp: %5.1f%%\n"+
//                "GPU Particles: %5.1f%%\n"+
                "CPU Particles: %5.1f%%\n"+
//                "GPU Particles upsamp: %5.1f%%\n"+
                "CPU Particles upsamp: %5.1f%%\n";

        if (m_statsCountdown == 0) {
//            float meanGPUTotal = m_GPUTimers[GPU_TIMER_TOTAL].getScaledCycles() /
//                    m_GPUTimers[GPU_TIMER_TOTAL].getStartStopCycles();

            String buffer = NvUtils.sprintf(
                    TIMER_PATTERN,
                    /*computePercentage(m_GPUTimers[GPU_TIMER_SCENE_DEPTH], meanGPUTotal),*/
                    100.f * m_CPUTimers[CPU_TIMER_SCENE_DEPTH].getScaledCycles()        / m_CPUTimers[CPU_TIMER_TOTAL].getScaledCycles(),
//                    computePercentage(m_GPUTimers[GPU_TIMER_SCENE_COLOR], meanGPUTotal),
                    100.f * m_CPUTimers[CPU_TIMER_SCENE_COLOR].getScaledCycles()        / m_CPUTimers[CPU_TIMER_TOTAL].getScaledCycles(),
//                    computePercentage(m_GPUTimers[GPU_TIMER_UPSAMPLE_SCENE], meanGPUTotal),
                    100.f * m_CPUTimers[CPU_TIMER_UPSAMPLE_SCENE].getScaledCycles()     / m_CPUTimers[CPU_TIMER_TOTAL].getScaledCycles(),
//                    computePercentage(m_GPUTimers[GPU_TIMER_PARTICLES], meanGPUTotal),
                    100.f * m_CPUTimers[CPU_TIMER_PARTICLES].getScaledCycles()          / m_CPUTimers[CPU_TIMER_TOTAL].getScaledCycles(),
//                    computePercentage(m_GPUTimers[GPU_TIMER_UPSAMPLE_PARTICLES], meanGPUTotal),
                    100.f * m_CPUTimers[CPU_TIMER_UPSAMPLE_PARTICLES].getScaledCycles() / m_CPUTimers[CPU_TIMER_TOTAL].getScaledCycles());

            for (int i = 0; i < GPU_TIMER_COUNT; ++i)
            {
//                m_GPUTimers[i].reset();
            }

            for (int i = 0; i < CPU_TIMER_COUNT; ++i)
            {
                m_CPUTimers[i].reset();
            }

            m_statsCountdown = STATS_FRAMES;
            return buffer;
        } else {
            m_statsCountdown--;
            return null;
        }
    }

    void reshapeWindow(int w, int h)
    {
        m_scene.setScreenSize(w, h);
        m_fbos.setWindowSize(m_scene.m_screenWidth, m_scene.m_screenHeight);
    }

    void setEyeViewMatrix(Matrix4f viewMatrix)
    {
        m_scene.m_eyeView.load(viewMatrix);
    }

    void setProjectionMatrix(Matrix4f m)
    {
        m_scene.m_eyeProj.load(m);
    }

    void setLightDirection(Vector3f d)
    {
        m_scene.setLightVector(d);
    }

    ParticleRenderer.Params getParticleParams()
    {
        return m_particles.getParams();
    }

    Upsampler.Params getUpsamplingParams()
    {
        return m_upsampler.m_params;
    }

    SceneFBOs.Params getSceneFBOParams()
    {
        return m_fbos.m_params;
    }

    SceneRenderer.Params getSceneParams()
    {
        return m_params;
    }

    int getScreenWidth() { return m_scene.m_screenWidth; }
    int getScreenHeight() { return m_scene.m_screenHeight; }

    static final class Params{
        boolean useDepthPrepass;
        boolean renderLowResolution;
        final Vector3f backgroundColor = new Vector3f(0.5f, 0.8f, 1.f);
    }

    static NvGLModel loadModelFromFile(String pFileName, float rescale )
    {

        NvGLModel pModel = new NvGLModel();

        pModel.loadModelFromFile(pFileName);
        pModel.rescaleModel(rescale);
        pModel.initBuffers();
        int tSize = pModel.getModel().getTexCoordSize();
        int tCount = pModel.getModel().getTexCoordCount();

        System.out.printf("%s contain %d texcoords, %d size.\n", pFileName, tCount, tSize);

        return pModel;
    }

    /*
    This file contains a simple function to place objects in scene and assign materials to them.
    Nothing from this file should be reused in any projects. This was done just to quickly create the example scene.
    You'd better store this info in external xml or other scene desctiption format.
    */
    static float rnd(float a, float b) { return NvUtils.random(a, b); }

    static void createRandomObjectsOnLandScape(List<MeshObj> a_modelsArray, Map<String, NvGLModel> a_modelStorage,
                                               Map<String, Integer> a_texStorage, float treeHeight){
        boolean ADD_TREES=true, ADD_TANKS=true, ADD_COWS=true;

        a_modelsArray.clear();

        if (ADD_TREES)
        {
//	        nv::matrix4f translateMatrix;
//	        nv::matrix4f rotateMatrix;

            final int TREES_NUMBERZ = 6;
            final int TREES_NUMBERX = 6;
            final float TREES_DISTZ = 75.0f;
            final float TREES_DISTX = 75.0f;

            final float MAX_X = TREES_DISTX*TREES_NUMBERX;
            final float MAX_Z = TREES_DISTZ*TREES_NUMBERZ;

            // We place a few trees non-randomly to give nice interaction with the tank and cows.
            final int N_NON_RANDOM = 4;
            final Vector3f[] nonRandomPositions/*[N_NON_RANDOM]*/ =
                    {
                            new Vector3f( -65, 0, -30),        // For tank
                            new Vector3f(-160, 0,-120),        // For cow #0
                            new Vector3f(-105, 0, -25),        // For cow #1
                            new Vector3f( 120, 0,-225),        // For cow #2
                    };

            for(int i=0;i<TREES_NUMBERZ;i++)
            {
                for(int j=0;j<TREES_NUMBERX;j++)
                {
                    float x=0, z=0;

                    if (a_modelsArray.size() < N_NON_RANDOM)
                    {
                        x = nonRandomPositions[a_modelsArray.size()].x;
                        z = nonRandomPositions[a_modelsArray.size()].z;
                    }
                    else
                    {
                        x = rnd(-MAX_X, MAX_X);
                        z = rnd(-MAX_Z, MAX_Z);
                    }

                    // palm
                    //
//	                translateMatrix.set_translate(nv::vec3f(x, treeHeight + rnd(-20,0), z));
//	                nv::rotationY(rotateMatrix, rnd(0.0f,NV_PI));
//
//	                modelMatrix.make_identity();
//	                modelMatrix = translateMatrix*rotateMatrix; //.set_translate(nv::vec3f(x, 50.0, y));
                    MeshObj myObj = new MeshObj();
                    Matrix4f modelMatrix = myObj.m_modelMatrix;
                    modelMatrix.translate(x, treeHeight + rnd(-20,0), z);
                    modelMatrix.rotate(rnd(0.0f,NvUtils.PI), VectorUtil.UNIT_Y);

//	                myObj.m_modelMatrix   = modelMatrix;
                    myObj.m_color.set(1.0f, 1.0f, 1.0f, 1.0f);
                    myObj.m_specularValue = 0.0f;
                    myObj.m_pModelData    = a_modelStorage.get("palm");
                    myObj.m_texId         = a_texStorage.get("palm");
                    myObj.m_cullFacing    = false;
                    myObj.m_alphaTest     = true;

                    if(myObj.m_pModelData!= null)
                        a_modelsArray.add(myObj);
                    else
                        NvLogger.e("object 'palm' not found in the model storage");

                }
            }
        }

        if (ADD_TANKS)
        {
            final int TANKS_NUMBERZ = 1;
            final int TANKS_NUMBERX = 1;

            final float COWS_DISTZ = 50.0f;
            final float COWS_DISTX = 100.0f;

            for(int i=0;i<TANKS_NUMBERZ;i++)
            {
                for(int j=0;j<TANKS_NUMBERX;j++)
                {
                    MeshObj myObj = new MeshObj();
//	                modelMatrix.set_translate(nv::vec3f(COWS_DISTX*(2*j-TANKS_NUMBERX+1), 0.0, COWS_DISTZ*(2*i-TANKS_NUMBERZ+1)));
                    myObj.m_modelMatrix.translate(COWS_DISTX*(2*j-TANKS_NUMBERX+1), 0, COWS_DISTZ*(2*i-TANKS_NUMBERZ+1));
//	                myObj.m_modelMatrix   = modelMatrix;
                    myObj.m_color.set(0.75f, 0.75f, 0.75f, 1.0f);
                    myObj.m_specularValue = 1.0f;
                    myObj.m_pModelData    = a_modelStorage.get("T34-85");
                    myObj.m_texId         = a_texStorage.get("white_dummy");
                    myObj.m_alphaTest     = false;

                    if(myObj.m_pModelData!= null)
                        a_modelsArray.add(myObj);
                    else
                        NvLogger.e("object 'T34-85' not found in the model storage");
                }
            }
        }

        if (ADD_COWS)
        {
//	        nv::matrix4f translateMatrix;
//	        nv::matrix4f rotateMatrix;

            final int COWS_NUMBERZ = 2;
            final int COWS_NUMBERX = 2;

//	        const float COWS_DISTZ = 50.0f;
//	        const float COWS_DISTX = 100.0f;

            Vector3f[][] cowsPos = new Vector3f[2][2];

            cowsPos[0][0] = new Vector3f(-90, 9,-90);
            cowsPos[0][1] = new Vector3f(-35, 4, 50);
            cowsPos[1][0] = new Vector3f(150, 15,-180);
            cowsPos[1][1] = new Vector3f(50, 2, 100);

//	        modelMatrix.make_identity();

            for(int i=0;i<COWS_NUMBERZ;i++)
            {
                for(int j=0;j<COWS_NUMBERX;j++)
                {
//	                translateMatrix.set_translate(cowsPos[i][j]);
//	                nv::rotationY(rotateMatrix, rnd(0.0f,NV_PI));
//
//	                modelMatrix.make_identity();
//	                modelMatrix = translateMatrix*rotateMatrix; //.set_translate(nv::vec3f(x, 50.0, y));

                    MeshObj myObj = new MeshObj();
                    Matrix4f modelView = myObj.m_modelMatrix;
                    modelView.translate(cowsPos[i][j]);
                    modelView.rotate(rnd(0.0f,NvUtils.PI), VectorUtil.UNIT_Y);

//	                myObj.m_modelMatrix   = modelMatrix;
                    myObj.m_color.set(0.75f, 0.75f, 0.75f, 1.0f);
                    myObj.m_specularValue = 1.0f;
                    myObj.m_pModelData    = a_modelStorage.get("cow");
                    myObj.m_texId         = a_texStorage.get("white_dummy");
                    myObj.m_alphaTest     = false;

                    if(myObj.m_pModelData!= null)
                        a_modelsArray.add(myObj);
                    else
                        NvLogger.e("object 'cow' not found in the model storage");
                }
            }
        }
    }
}
