package jet.learning.opengl.gtao;

import android.opengl.GLES30;
import android.opengl.GLES31;
import android.opengl.GLES32;

import com.nvidia.developer.opengl.utils.BufferUtils;
import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.Macro;
import com.nvidia.developer.opengl.utils.NvGLSLProgram;
import com.nvidia.developer.opengl.utils.NvUtils;

import org.lwjgl.util.vector.Vector2i;
import org.lwjgl.util.vector.Vector4f;

import java.nio.ByteBuffer;

import jet.learning.opengl.common.GLSLUtil;
import jet.learning.opengl.common.SamplerDesc;
import jet.learning.opengl.common.SamplerUtils;
import jet.learning.opengl.common.Texture2D;
import jet.learning.opengl.common.Texture2DDesc;
import jet.learning.opengl.common.TextureUtils;

public class GTAO {

    private final FViewNormalPass ViewNormalPass = new FViewNormalPass();
    private final FViewDepthPass ViewDepthPass = new FViewDepthPass();
    private final FDeinterleavePass DeinterleavePass = new FDeinterleavePass();
    private final FInterleavePass InterleavePass = new FInterleavePass();
    private final FReinterleavePass ReinterleavePass = new FReinterleavePass();
    private final FAOBlurPass AOBlurPass = new FAOBlurPass();
    private final FSpatialFilterPass SpatialFilterPass = new FSpatialFilterPass();
    private final FSpatialFilterPass MobileSpatialFilterPass = new FSpatialFilterPass();
    private final FUpsamplePass UpsamplePass = new FUpsamplePass();
    protected final FHorizonSearchIntegratePass HorizonSearchIntegratePass = new FHorizonSearchIntegratePass();
    protected final FHorizonSearchIntegratePass MobileHorizonIntegratePass = new FHorizonSearchIntegratePass();
    protected final FHorizonSearchIntegratePass MobileHBAOPass = new FHorizonSearchIntegratePass();

    protected final FGTAOShaderParameters shaderParameters = new FGTAOShaderParameters();

    private int mSamplerPoint;
    private int mSamplerLinear;
    private int mUBO;

    GTAOMethod mMethod = GTAOMethod.GTAO_Mobile;
    private int mFrameIndex;

    private ByteBuffer mBufCache;

    final boolean use32Floating = true;
    final String shaderPath = "labs/GTAO/shaders/";

    public void SetMethod(GTAOMethod method) { mMethod = method; }

    public GTAOMethod GetMethod() { return mMethod; }

    public void Create(){
        // defulat is linear
        mBufCache = BufferUtils.createByteBuffer(FGTAOShaderParameters.SIZE);
        SamplerDesc pointDesc = new SamplerDesc();
        mSamplerLinear = SamplerUtils.createSampler(pointDesc);
        pointDesc.magFilter = pointDesc.minFilter = GLES32.GL_NEAREST;

        mUBO = GLES.glGenBuffers();
        GLES30.glBindBuffer(GLES30.GL_UNIFORM_BUFFER, mUBO);
        GLES30.glBufferData(GLES30.GL_UNIFORM_BUFFER, FGTAOShaderParameters.SIZE, null, GLES30.GL_DYNAMIC_DRAW);
        GLES30.glBindBuffer(GLES30.GL_UNIFORM_BUFFER, 0);
    }

    public void RenderAO(SSAOParameters parameters) {
        parameters.GTAOQuality = 3;

        BuildAOConstantBuffer( parameters);
        switch (mMethod)
        {
            case Defualt:
            {
                AddHorizonSearchIntegratePass( parameters);
                AddGTAOSpatialFilter( parameters);
                AddGTAOUpsamplePass( parameters);
            }
                break;
            case Interleave:
            {
                AddGenerateNormalPass( parameters);
                AddConstructViewLinearDepth( parameters);
                AddDeinterleave( parameters);
                // do GTAO
                AddInterleavePass( parameters);
                AddReinterleave( parameters);
                AddBlurAO( parameters);
            }
                break;
            case InterleaveOpt:
            {
                AddGenerateNormalDepthPass(parameters);
                // do GTAO
                AddInterleavePass(parameters);
//			    AddBlurOptPass( parameters);
                AddGTAOSpatialFilterOpt(parameters);
                AddGTAOUpsamplePass(parameters);
            }
                break;
            case InterleaveHBAO:
            {
                AddGenerateNormalDepthPass(parameters);
                // do GTAO
                AddInterleavePass(parameters, true);
//			    AddBlurOptPass( parameters);
                AddGTAOSpatialFilterOpt(parameters);
                AddGTAOUpsamplePass(parameters);
            }
                break;
            case GTAO_Mobile:
            {
                AddMobileHorizonIntegratePass(parameters);
                AddGTAOMobileSpatialFilter(parameters, MobileHorizonIntegratePass.Output);
            }
            break;
            case HBAO_Mobile:
            {
                AddMobileHBAOPass(parameters);
                AddGTAOMobileSpatialFilter(parameters, MobileHBAOPass.Output);
            }
            break;
            default:
                break;
        }

        mFrameIndex++;
        GLES30.glBindBufferBase(GLES30.GL_UNIFORM_BUFFER, 0, 0);
    }

    protected void BuildAOConstantBuffer(SSAOParameters parameters){
        FGTAOShaderParameters shaderUniformData = GetGTAOShaderParameters(parameters, mFrameIndex);

        GLES30.glBindBufferBase(GLES30.GL_UNIFORM_BUFFER, 0, mUBO);
        mBufCache.clear();
        ByteBuffer buf = mBufCache;
        shaderUniformData.store(buf);
        buf.flip();
        GLES30.glBufferSubData(GLES30.GL_UNIFORM_BUFFER,0, buf.remaining(), buf);
    }

    private void SetupUniforms(NvGLSLProgram program){
        /*GLSLUtil.setMat4(program, "Proj", shaderParameters.ProjInverse);
        GLSLUtil.setFloat4(program, "ProjInfo", shaderParameters.ProjInfo);
        GLSLUtil.setFloat4(program, "BufferSizeAndInvSize", shaderParameters.BufferSizeAndInvSize);
        GLSLUtil.setFloat4(program, "GTAOParams", shaderParameters.GTAOParams);
        GLSLUtil.setFloat2(program, "DepthUnpackConsts", shaderParameters.DepthUnpackConsts);
        GLSLUtil.setFloat3(program, "ProjDia", shaderParameters.ProjDia);

        GLSLUtil.setFloat(program, "InvTanHalfFov", shaderParameters.InvTanHalfFov);
        GLSLUtil.setFloat(program, "AmbientOcclusionFadeRadius", shaderParameters.AmbientOcclusionFadeRadius);
        GLSLUtil.setFloat(program, "AmbientOcclusionFadeDistance", shaderParameters.AmbientOcclusionFadeDistance);*/
    }

    private Texture2D ReCreateTex2D(Texture2D source, int width, int height, int format){
        if (source == null || source.getWidth() != width || source.getHeight() != height)
        {
            if(source != null)
                source.dispose();

            Texture2DDesc outputDesc = new Texture2DDesc(   );
            outputDesc.arraySize = 1;
            outputDesc.format = format;
            outputDesc.width = width;
            outputDesc.height = height;
            outputDesc.mipLevels = 1;

            source = TextureUtils.createTexture2D(outputDesc, null, source);
        }

        return source;
    }

    private Texture2D ReCreateTex2DArray(Texture2D source, int width, int height, int format){
        if (source == null || source.getWidth() != width || source.getHeight() != height)
        {
            if(source != null)
                source.dispose();

            Texture2DDesc outputDesc = new Texture2DDesc(   );
            outputDesc.arraySize = 16;
            outputDesc.format = format;
            outputDesc.width = width;
            outputDesc.height = height;
            outputDesc.mipLevels = 1;

            source = TextureUtils.createTexture2D(outputDesc, null, source);
        }

        return source;
    }

    public Texture2D getNormalTex() {
        return  ViewNormalPass.Output;
    }

    public Texture2D getInterleaveDepth() {
        return  DeinterleavePass.Output;
    }

    public Texture2D getInterleaveAO(){
        return InterleavePass.Output;
    }

    public Texture2D getMobileGTAO() {
        if(mMethod == GTAOMethod.GTAO_Mobile)
            return MobileHorizonIntegratePass.Output;
        else if(mMethod == GTAOMethod.HBAO_Mobile)
            return MobileHBAOPass.Output;
        else
            return null;

    }

    // Interleave Methiod
    protected void AddGenerateNormalPass(SSAOParameters parameters){
        if (ViewNormalPass.Shader == null) {
            ViewNormalPass.Shader = NvGLSLProgram.createProgram(shaderPath + "GenViewNormal.comp", null);
        }

        assert(parameters.DownscaleFactor == 1);
		final int outputWidth = parameters.SceneWidth / parameters.DownscaleFactor;
        final int outputHeight = parameters.SceneHeight / parameters.DownscaleFactor;

        ViewNormalPass.Output = ReCreateTex2D(ViewNormalPass.Output, outputWidth, outputHeight,GLES32.GL_R11F_G11F_B10F);

        ViewNormalPass.Shader.enable();
        SetupUniforms(ViewNormalPass.Shader);
        GLES.glBindTextureUnit(0, parameters.SceneDepth);
        GLES30.glBindSampler(0, mSamplerPoint);
        GLES31.glBindImageTexture(0, ViewNormalPass.Output.getTexture(),0, false, 0, GLES32.GL_WRITE_ONLY, ViewNormalPass.Output.getFormat());
        GLES31.glDispatchCompute(NvUtils.divideAndRoundUp(outputWidth, 8), NvUtils.divideAndRoundUp(outputHeight, 8), 1);

        GLES.glBindTextureUnit(0, null);
        GLES31.glBindImageTexture(0, 0,0, false, 0, GLES32.GL_WRITE_ONLY, ViewNormalPass.Output.getFormat());
    }

    protected void AddConstructViewLinearDepth(SSAOParameters parameters){
        final boolean use32Floating = true;
        if (ViewDepthPass.Shader == null) {

            final Macro[] macros = {
                    new Macro("GenViewDepthCS", 1),
                    new Macro("OUT_FORMAT", use32Floating ? "r32f" : "r16f")
            };
            ViewDepthPass.Shader = NvGLSLProgram.createProgram(shaderPath + "GenViewDepth.comp", macros);
        }

        assert(parameters.DownscaleFactor == 1);
		final int outputWidth = parameters.SceneWidth / parameters.DownscaleFactor;
		final int outputHeight = parameters.SceneHeight / parameters.DownscaleFactor;

        ViewDepthPass.Output = ReCreateTex2D(ViewDepthPass.Output, outputWidth, outputHeight, use32Floating ? GLES32.GL_R32F : GLES32.GL_R16F);

        ViewDepthPass.Shader.enable();
        SetupUniforms( ViewDepthPass.Shader);

        GLES.glBindTextureUnit(0, parameters.SceneDepth);
        GLES32.glBindSampler(0, mSamplerPoint);
        GLES32.glBindImageTexture(0, ViewDepthPass.Output.getTexture(),0, false, 0, GLES32.GL_WRITE_ONLY, ViewNormalPass.Output.getFormat());
        GLES32.glDispatchCompute(NvUtils.divideAndRoundUp(outputWidth, 8), NvUtils.divideAndRoundUp(outputHeight, 8), 1);

        GLES.glBindTextureUnit(0, null);
        GLES32.glBindImageTexture(0, 0,0, false, 0, GLES32.GL_WRITE_ONLY, ViewNormalPass.Output.getFormat());
    }

    protected void AddDeinterleave(SSAOParameters parameters){

        if (DeinterleavePass.Shader == null) {
            final Macro[] macros = {
                    new Macro("DeinterleaveCS", 1),
                    new Macro("OUT_FORMAT", use32Floating ? "r32f" : "r16f")
            };
            DeinterleavePass.Shader = NvGLSLProgram.createProgram(shaderPath + "GenViewDepth.comp", macros);
        }

		final int outputWidth = parameters.SceneWidth / 4;
        final int outputHeight = parameters.SceneHeight / 4;

        DeinterleavePass.Output = ReCreateTex2DArray(DeinterleavePass.Output, outputWidth, outputHeight, use32Floating ? GLES32.GL_R32F : GLES32.GL_R16F);

        DeinterleavePass.Shader.enable();
        SetupUniforms(DeinterleavePass.Shader);
        GLES.glBindTextureUnit(0, ViewDepthPass.Output);
        GLES31.glBindSampler(0, mSamplerPoint);
        GLES31.glBindImageTexture(0, DeinterleavePass.Output.getTexture(),0, true, 0, GLES32.GL_WRITE_ONLY, DeinterleavePass.Output.getFormat());
        GLES31.glDispatchCompute(NvUtils.divideAndRoundUp(outputWidth, 8), NvUtils.divideAndRoundUp(outputHeight, 4), 1);

        GLES.glBindTextureUnit(0, null);
        GLES31.glBindImageTexture(0, 0,0, false, 0, GLES32.GL_WRITE_ONLY, ViewNormalPass.Output.getFormat());
    }

    protected void AddInterleavePass(SSAOParameters parameters){
        AddInterleavePass(parameters, false);
    }
    protected void AddInterleavePass(SSAOParameters parameters, boolean hbao){
        final int OutFormat = GLES32.GL_RGBA16F;
        if (InterleavePass.Shader == null || InterleavePass.IsHBAO == hbao) {
            if(InterleavePass.Shader != null)
                InterleavePass.Shader.dispose();
            final Macro[] macros = new Macro[]{
//                    new Macro("OUT_FORMAT", use32Floating ? "rg32f" : "rg16f"),
                    new Macro("OUT_FORMAT", TextureUtils.getImageFormat(OutFormat) ),
                    new Macro("SHADER_QUALITY", parameters.GTAOQuality)
            };
            if(!hbao){
                InterleavePass.Shader = NvGLSLProgram.createProgram(shaderPath + "GTAOInterleave.comp", macros);
            }else{
                InterleavePass.Shader = NvGLSLProgram.createProgram(shaderPath + "HBAOInterleave.comp", macros);
            }
            InterleavePass.IsHBAO = hbao;
        }

		final int outputWidth = parameters.SceneWidth / 4;
        final int outputHeight = parameters.SceneHeight / 4;

        InterleavePass.Output = ReCreateTex2DArray(InterleavePass.Output, outputWidth, outputHeight, OutFormat);

        InterleavePass.Shader.enable();
        SetupUniforms(InterleavePass.Shader);
        GLES.glBindTextureUnit(0, DeinterleavePass.Output);
        GLES31.glBindSampler(0, mSamplerPoint);
        GLES.glBindTextureUnit(1, ViewNormalPass.Output);
        GLES31.glBindSampler(1, mSamplerLinear);

        GLES31.glBindImageTexture(0, InterleavePass.Output.getTexture(),0, true, 0, GLES32.GL_WRITE_ONLY, InterleavePass.Output.getFormat());
        GLES31.glDispatchCompute(NvUtils.divideAndRoundUp(outputWidth, 8), NvUtils.divideAndRoundUp(outputHeight, 4), 16);

        GLES.glBindTextureUnit(0, null);
        GLES.glBindTextureUnit(0, null);
        GLES31.glBindImageTexture(0, 0,0, false, 0, GLES32.GL_WRITE_ONLY, ViewNormalPass.Output.getFormat());

        GLES31.glMemoryBarrier(GLES32.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);
        InterleavePass.Shader.printOnce();

        GLES.checkGLError();
    }

    protected void AddReinterleave(SSAOParameters parameters){
        if (ReinterleavePass.Shader == null) {
            final Macro[] macros = {
                    new Macro("ReinterleaveCS", 1),
                    new Macro("OUT_FORMAT", use32Floating ? "rg32f" : "rg16f")
            };
            ReinterleavePass.Shader = NvGLSLProgram.createProgram(shaderPath + "GenViewDepth.comp", macros);
        }

		final int outputWidth = parameters.SceneWidth;
		final int outputHeight = parameters.SceneHeight;

        ReinterleavePass.Output = ReCreateTex2D(ReinterleavePass.Output, outputWidth, outputHeight, use32Floating ? GLES32.GL_RG32F : GLES32.GL_RG16F);

        ReinterleavePass.Shader.enable();
        SetupUniforms(ReinterleavePass.Shader);
        GLES.glBindTextureUnit(0, InterleavePass.Output);
        GLES31.glBindSampler(0, mSamplerPoint);
        GLES31.glBindImageTexture(0, ReinterleavePass.Output.getTexture(),0, true, 0, GLES32.GL_WRITE_ONLY, ReinterleavePass.Output.getFormat());
        GLES31.glDispatchCompute(NvUtils.divideAndRoundUp(outputWidth, 8), NvUtils.divideAndRoundUp(outputHeight, 8), 1);

        GLES.glBindTextureUnit(0, null);
        GLES31.glBindImageTexture(0, 0,0, false, 0, GLES32.GL_WRITE_ONLY, ViewNormalPass.Output.getFormat());
    }

    protected void AddBlurAO(SSAOParameters parameters){
        if (AOBlurPass.ShaderX == null) {
            {
                final Macro[] macros =  new Macro[]{
                        new Macro("GTAOBlurXCS", 1),
                        new Macro("OUT_FORMAT", use32Floating ? "rg32f" : "rg16f")
                };
                AOBlurPass.ShaderX = NvGLSLProgram.createProgram(shaderPath + "GenViewDepth.comp", macros);
            }

            {
                final Macro[] macros = new Macro[]{
                        new Macro("GTAOBlurYCS", 1),
                        new Macro("OUT_FORMAT", use32Floating ? "r32f" : "r16f")
                };
                AOBlurPass.ShaderY = NvGLSLProgram.createProgram(shaderPath + "GenViewDepth.comp", macros);
            }
        }

		final int outputWidth = parameters.SceneWidth;
        final int outputHeight = parameters.SceneHeight;

        AOBlurPass.Output = ReCreateTex2D(AOBlurPass.Output, outputWidth, outputHeight, use32Floating?GLES32.GL_RG32F : GLES32.GL_RG16F);

        // BlurX
        {
            AOBlurPass.ShaderX.enable();
            SetupUniforms(AOBlurPass.ShaderX);
            GLES.glBindTextureUnit(0, ReinterleavePass.Output);
            GLES31.glBindSampler(0, mSamplerLinear);
            GLES31.glBindImageTexture(0, AOBlurPass.Output.getTexture(),0, false, 0, GLES32.GL_WRITE_ONLY, AOBlurPass.Output.getFormat());
            GLES31.glDispatchCompute(NvUtils.divideAndRoundUp(outputWidth, 8), NvUtils.divideAndRoundUp(outputHeight, 8), 1);

            GLES.glBindTextureUnit(0, null);
            GLES31.glBindImageTexture(0, 0,0, false, 0, GLES32.GL_WRITE_ONLY, GLES32.GL_RGBA8);
        }


        // BlurY
        {
            AOBlurPass.ShaderY.enable();
            SetupUniforms(AOBlurPass.ShaderY);
            GLES.glBindTextureUnit(0, AOBlurPass.Output);
            GLES31.glBindSampler(0, mSamplerLinear);
            GLES31.glBindImageTexture(0, parameters.ResultAO.getTexture(),0, false, 0, GLES32.GL_WRITE_ONLY, parameters.ResultAO.getFormat());
            GLES31.glDispatchCompute(NvUtils.divideAndRoundUp(outputWidth, 8), NvUtils.divideAndRoundUp(outputHeight, 8), 1);

            GLES.glBindTextureUnit(0, null);
            GLES31.glBindImageTexture(0, 0,0, false, 0, GLES32.GL_WRITE_ONLY, GLES32.GL_RGBA8);
        }
    }

    // InterleaveOpt method
    protected void AddGenerateNormalDepthPass(SSAOParameters parameters){
        final int NormalFormat = GLES32.GL_RGBA8;
        final int DepthFormat = GLES32.GL_R32F;

        if (ViewNormalPass.Shader == null)
        {
            final Macro[] macros = {
//                    new Macro("OUT_FORMAT", use32Floating ? "r32f" : "r16f")
                    new Macro("OUT_FORMAT", TextureUtils.getImageFormat(DepthFormat))
            };

            ViewNormalPass.Shader = NvGLSLProgram.createProgram(shaderPath + "GenViewNormal.comp", macros);
        }

        assert(parameters.DownscaleFactor == 1);
        {
            final int outputWidth = parameters.SceneWidth / parameters.DownscaleFactor;
            final int outputHeight = parameters.SceneHeight / parameters.DownscaleFactor;

            ViewNormalPass.Output = ReCreateTex2D(ViewNormalPass.Output, outputWidth,outputHeight, NormalFormat);
        }

        {
			final int outputWidth = parameters.SceneWidth / 4;
            final int outputHeight = parameters.SceneHeight / 4;

            DeinterleavePass.Output = ReCreateTex2DArray(DeinterleavePass.Output, outputWidth, outputHeight,DepthFormat);
        }

        ViewNormalPass.Shader.enable();
        SetupUniforms(ViewNormalPass.Shader);
        GLES.glBindTextureUnit(0, parameters.SceneDepth);
        GLES31.glBindSampler(0, mSamplerPoint);
        GLES31.glBindImageTexture(0, ViewNormalPass.Output.getTexture(),0, false, 0, GLES32.GL_WRITE_ONLY, ViewNormalPass.Output.getFormat());
        GLES31.glBindImageTexture(1, DeinterleavePass.Output.getTexture(),0, true, 0, GLES32.GL_WRITE_ONLY, DeinterleavePass.Output.getFormat());
        GLES31.glDispatchCompute(NvUtils.divideAndRoundUp(parameters.SceneWidth, 8), NvUtils.divideAndRoundUp(parameters.SceneHeight, 8), 1);

        GLES.glBindTextureUnit(0, null);
        GLES.glBindTextureUnit(1, null);
        GLES31.glBindImageTexture(0, 0,0, false, 0, GLES32.GL_WRITE_ONLY, GLES32.GL_RGBA8);
        GLES31.glBindImageTexture(1, 0,0, true, 0, GLES32.GL_WRITE_ONLY, GLES32.GL_RGBA8);

        GLES31.glMemoryBarrier(GLES32.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);

        GLES.checkGLError();

        ViewNormalPass.Shader.printOnce();
    }

    protected void AddBlurOptPass(SSAOParameters parameters){
        if (AOBlurPass.ShaderX == null)
        {
            {

                final Macro[] macros =  new Macro[]{
                        new Macro("GTAOBlurXCS_Opt", 1),
                        new Macro("OUT_FORMAT", use32Floating ? "rg32f" : "rg16f")
                };
                AOBlurPass.ShaderX = NvGLSLProgram.createProgram(shaderPath + "GenViewDepth.comp", macros);
            }

            {
                AOBlurPass.ShaderY = new NvGLSLProgram();

                final Macro[] macros = new Macro[]{
                        new Macro("GTAOBlurYCS_Opt", 1),
                        new Macro("OUT_FORMAT", use32Floating ? "r32f" : "r16f")
                };
                AOBlurPass.ShaderY = NvGLSLProgram.createProgram(shaderPath + "GenViewDepth.comp", macros);
            }
        }

		final int outputWidth = parameters.SceneWidth;
        final int outputHeight = parameters.SceneHeight;

        AOBlurPass.Output = ReCreateTex2D(AOBlurPass.Output, outputWidth, outputHeight,use32Floating ? GLES32.GL_R32F : GLES32.GL_R16F);

        // BlurX
        {
            AOBlurPass.ShaderX.enable();
            SetupUniforms(AOBlurPass.ShaderX);
            GLES.glBindTextureUnit(0, InterleavePass.Output);
            GLES31.glBindSampler(0, mSamplerLinear);
            GLES31.glBindImageTexture(0, AOBlurPass.Output.getTexture(),0, false, 0, GLES32.GL_WRITE_ONLY, AOBlurPass.Output.getFormat());
            GLES31.glDispatchCompute(NvUtils.divideAndRoundUp(outputWidth, 8), NvUtils.divideAndRoundUp(outputHeight, 8), 1);

            GLES.glBindTextureUnit(0, null);
            GLES31.glBindImageTexture(0, 0,0, false, 0, GLES32.GL_WRITE_ONLY, GLES32.GL_RGBA8);
            GLES.checkGLError();
        }


        // BlurY
        {
            AOBlurPass.ShaderY.enable();
            SetupUniforms(AOBlurPass.ShaderY);
            GLES.glBindTextureUnit(0, AOBlurPass.Output);
            GLES31.glBindSampler(0, mSamplerLinear);
            GLES31.glBindImageTexture(0, parameters.ResultAO.getTexture(),0, false, 0, GLES32.GL_WRITE_ONLY, parameters.ResultAO.getFormat());
            GLES31.glDispatchCompute(NvUtils.divideAndRoundUp(outputWidth, 8), NvUtils.divideAndRoundUp(outputHeight, 8), 1);

            GLES.glBindTextureUnit(0, null);
            GLES31.glBindImageTexture(0, 0,0, false, 0, GLES32.GL_WRITE_ONLY, GLES32.GL_RGBA8);
            GLES.checkGLError();
        }
    }

    protected void AddGTAOSpatialFilterOpt(SSAOParameters parameters){
        AddGTAOSpatialFilter(parameters, true);
    }

    // Defualt method
    protected void AddHorizonSearchIntegratePass(SSAOParameters parameters){
        int shaderQuality = NvUtils.clamp(parameters.GTAOQuality, 0, 4);
        if (HorizonSearchIntegratePass.CSShader[shaderQuality] == null) {
            final Macro[] macros = {
                    new Macro("THREADGROUP_SIZEX", 8),
                    new Macro("THREADGROUP_SIZEY", 8),
                    new Macro("SHADER_QUALITY", shaderQuality),
                    new Macro("OUT_FORMAT", use32Floating ? "r32f" : "r16f")
            };
            HorizonSearchIntegratePass.CSShader[shaderQuality] = NvGLSLProgram.createProgram(shaderPath + "GTAOHorizonSearchAndIntegrateCS.comp", macros);
        }

        final int outputWidth = parameters.SceneWidth / parameters.DownscaleFactor;
        final int outputHeight = parameters.SceneHeight / parameters.DownscaleFactor;

        HorizonSearchIntegratePass.Output = ReCreateTex2D(HorizonSearchIntegratePass.Output, outputWidth, outputHeight, use32Floating?GLES32.GL_R32F : GLES32.GL_R16F);

        HorizonSearchIntegratePass.CSShader[shaderQuality].enable();
        SetupUniforms(HorizonSearchIntegratePass.CSShader[shaderQuality]);
        GLES.glBindTextureUnit(0, parameters.SceneDepth);
        GLES31.glBindSampler(0, mSamplerLinear);
        GLES31.glBindImageTexture(0, HorizonSearchIntegratePass.Output.getTexture(),0, false, 0, GLES32.GL_WRITE_ONLY, HorizonSearchIntegratePass.Output.getFormat());
        GLES31.glDispatchCompute(NvUtils.divideAndRoundUp(outputWidth, 8), NvUtils.divideAndRoundUp(outputHeight, 8), 1);

        GLES.glBindTextureUnit(0, null);
        GLES31.glBindImageTexture(0, 0,0, false, 0, GLES32.GL_WRITE_ONLY, GLES32.GL_RGBA8);
        GLES31.glMemoryBarrier(GLES32.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);

        HorizonSearchIntegratePass.CSShader[shaderQuality].printOnce();
        GLES.checkGLError();
    }

    // Defualt method
    protected void AddMobileHorizonIntegratePass(SSAOParameters parameters){
        final int THREADGROUP_SIZEX = 16;
        final int THREADGROUP_SIZEY = 8;

        int shaderQuality = NvUtils.clamp(parameters.GTAOQuality, 0, 4);
        if (MobileHorizonIntegratePass.CSShader[shaderQuality] == null) {
            final Macro[] macros = {
                    new Macro("THREADGROUP_SIZEX", THREADGROUP_SIZEX),
                    new Macro("THREADGROUP_SIZEY", THREADGROUP_SIZEY),
                    new Macro("SHADER_QUALITY", shaderQuality),
                    new Macro("OUT_FORMAT", "rgba8")
            };
            final String shaderPath = "labs/GTAO/shaders/";
            MobileHorizonIntegratePass.CSShader[shaderQuality] = NvGLSLProgram.createProgram(shaderPath + "GTAOMobileHorizonIntergralCS.comp", macros);
        }

        final int outputWidth = parameters.SceneWidth / parameters.DownscaleFactor;
        final int outputHeight = parameters.SceneHeight / parameters.DownscaleFactor;

        MobileHorizonIntegratePass.Output = ReCreateTex2D(MobileHorizonIntegratePass.Output, outputWidth, outputHeight, GLES30.GL_RGBA8);

        MobileHorizonIntegratePass.CSShader[shaderQuality].enable();
        SetupUniforms(MobileHorizonIntegratePass.CSShader[shaderQuality]);
        GLES.glBindTextureUnit(0, parameters.SceneDepth);
        GLES31.glBindSampler(0, mSamplerPoint);
        GLES31.glBindImageTexture(0, MobileHorizonIntegratePass.Output.getTexture(),0, false, 0, GLES32.GL_WRITE_ONLY, MobileHorizonIntegratePass.Output.getFormat());
        GLES31.glDispatchCompute(NvUtils.divideAndRoundUp(outputWidth, THREADGROUP_SIZEX), NvUtils.divideAndRoundUp(outputHeight, THREADGROUP_SIZEY), 1);

        GLES.glBindTextureUnit(0, null);
        GLES31.glBindImageTexture(0, 0,0, false, 0, GLES32.GL_WRITE_ONLY, GLES32.GL_RGBA8);
        GLES31.glMemoryBarrier(GLES32.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);

        MobileHorizonIntegratePass.CSShader[shaderQuality].printOnce();
        GLES.checkGLError();
    }

    protected void AddMobileHBAOPass(SSAOParameters parameters){
        final int THREADGROUP_SIZEX = 16;
        final int THREADGROUP_SIZEY = 8;

        int shaderQuality = NvUtils.clamp(parameters.GTAOQuality, 0, 4);
        if (MobileHBAOPass.CSShader[shaderQuality] == null) {
            final Macro[] macros = {
                    new Macro("THREADGROUP_SIZEX", THREADGROUP_SIZEX),
                    new Macro("THREADGROUP_SIZEY", THREADGROUP_SIZEY),
                    new Macro("SHADER_QUALITY", shaderQuality),
                    new Macro("OUT_FORMAT", "rgba8")
            };
            MobileHBAOPass.CSShader[shaderQuality] = NvGLSLProgram.createProgram(shaderPath + "MobileHBAOCS.comp", macros);
        }

        final int outputWidth = parameters.SceneWidth / parameters.DownscaleFactor;
        final int outputHeight = parameters.SceneHeight / parameters.DownscaleFactor;

        MobileHBAOPass.Output = ReCreateTex2D(MobileHBAOPass.Output, outputWidth, outputHeight, GLES30.GL_RGBA8);

        MobileHBAOPass.CSShader[shaderQuality].enable();
        SetupUniforms(MobileHBAOPass.CSShader[shaderQuality]);
        GLES.glBindTextureUnit(0, parameters.SceneDepth);
        GLES31.glBindSampler(0, mSamplerLinear);
        GLES31.glBindImageTexture(0, MobileHBAOPass.Output.getTexture(),0, false, 0, GLES32.GL_WRITE_ONLY, MobileHBAOPass.Output.getFormat());
        GLES31.glDispatchCompute(NvUtils.divideAndRoundUp(outputWidth, THREADGROUP_SIZEX), NvUtils.divideAndRoundUp(outputHeight, THREADGROUP_SIZEY), 1);

        GLES.glBindTextureUnit(0, null);
        GLES31.glBindImageTexture(0, 0,0, false, 0, GLES32.GL_WRITE_ONLY, GLES32.GL_RGBA8);
        GLES31.glMemoryBarrier(GLES32.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);

        MobileHBAOPass.CSShader[shaderQuality].printOnce();
        GLES.checkGLError();
    }

    protected void AddGTAOMobileSpatialFilter(SSAOParameters parameters, Texture2D inputAO){
        final int THREADGROUP_SIZEX = 8;
        final int THREADGROUP_SIZEY = 8;
        int shaderQuality = NvUtils.clamp(parameters.GTAOQuality, 0, 4);

        if (MobileSpatialFilterPass.CSShader == null) {
            final Macro[] macros = {
                    new Macro("THREADGROUP_SIZEX", THREADGROUP_SIZEX),
                    new Macro("THREADGROUP_SIZEY", THREADGROUP_SIZEY),
                    new Macro("SHADER_QUALITY", shaderQuality),
            };
            MobileSpatialFilterPass.CSShader = NvGLSLProgram.createProgram(shaderPath + "GTAOMobileSpatialFilterCS.comp", macros);
        }

        final int outputWidth = parameters.SceneWidth / parameters.DownscaleFactor;
        final int outputHeight = parameters.SceneHeight / parameters.DownscaleFactor;

        MobileSpatialFilterPass.Output = ReCreateTex2D(MobileSpatialFilterPass.Output, outputWidth,outputHeight, GLES30.GL_RGBA8);

        MobileSpatialFilterPass.CSShader.enable();

        GLES.glBindTextureUnit(0, inputAO);
        GLES31.glBindSampler(0, mSamplerPoint);

        GLES31.glBindImageTexture(0, parameters.ResultAO.getTexture(),0, false, 0, GLES32.GL_WRITE_ONLY, parameters.ResultAO.getFormat());
        GLES31.glDispatchCompute(NvUtils.divideAndRoundUp(outputWidth, THREADGROUP_SIZEX), NvUtils.divideAndRoundUp(outputHeight, THREADGROUP_SIZEY), 1);

        GLES.glBindTextureUnit(0, null);
        GLES31.glBindImageTexture(0, 0,0, false, 0, GLES32.GL_WRITE_ONLY, GLES32.GL_RGBA8);

        MobileSpatialFilterPass.CSShader.printOnce();
    }

    protected void AddGTAOSpatialFilter(SSAOParameters parameters){
        AddGTAOSpatialFilter(parameters, false);
    }

    protected void AddGTAOSpatialFilter(SSAOParameters parameters, boolean useArray){
        final int AOFormat = GLES32.GL_R32F;
        if (SpatialFilterPass.CSShader == null) {
            final Macro[] macros = {
                    new Macro("OUT_FORMAT", TextureUtils.getImageFormat(AOFormat)),
                    new Macro("USE_ARRAY_TEXTURE", useArray ? 1 : 0 ),
            };

            SpatialFilterPass.CSShader = NvGLSLProgram.createProgram(shaderPath + "GTAOSpatialFilterCS.comp", macros);
        }

		final int outputWidth = parameters.SceneWidth / parameters.DownscaleFactor;
        final int outputHeight = parameters.SceneHeight / parameters.DownscaleFactor;

        SpatialFilterPass.Output = ReCreateTex2D(SpatialFilterPass.Output, outputWidth,outputHeight, use32Floating ? GLES32.GL_R32F : GLES32.GL_R16F);

        class ShaderParameters
        {
            final Vector4f GTAOSpatialFilterParams = new Vector4f();
            final Vector4f GTAOSpatialFilterWidth = new Vector4f();
            final Vector2i GTAOSpatialFilterExtents = new Vector2i();
            float AmbientOcclusionIntensity;
            float AmbientOcclusionPower;
        };

        ShaderParameters shaderUniformData = new ShaderParameters();
        shaderUniformData.GTAOSpatialFilterExtents.set(outputWidth, outputHeight);

        final Vector4f FilterWidthParamsValue = shaderUniformData.GTAOSpatialFilterWidth;
        if (parameters.GTAOFilterWidth == 3)
        {
            FilterWidthParamsValue.x = -1.0f;
            FilterWidthParamsValue.y = 1.0f;
        }
        else if (parameters.GTAOFilterWidth == 4)
        {
            FilterWidthParamsValue.x = -1.0f;
            FilterWidthParamsValue.y = 2.0f;
        }
        else
        {
            FilterWidthParamsValue.x = -2.0f;
            FilterWidthParamsValue.y = 2.0f;
        }
//         = FilterWidthParamsValue;
        shaderUniformData.GTAOSpatialFilterParams.set((float)parameters.DownscaleFactor, 0.0f, 0.0f, 0.0f);
        shaderUniformData.AmbientOcclusionIntensity = parameters.AmbientOcclusionIntensity;
        shaderUniformData.AmbientOcclusionPower = parameters.AmbientOcclusionPower;

        SpatialFilterPass.CSShader.enable();
//        UpdateConstantBuffer(pDeviceContext, mConstantBuffer, shaderUniformData);
        GLSLUtil.setFloat4(SpatialFilterPass.CSShader, "GTAOSpatialFilterParams", shaderUniformData.GTAOSpatialFilterParams);
        GLSLUtil.setFloat4(SpatialFilterPass.CSShader, "GTAOSpatialFilterWidth", shaderUniformData.GTAOSpatialFilterWidth);
        GLSLUtil.setInt2(SpatialFilterPass.CSShader, "GTAOSpatialFilterExtents", shaderUniformData.GTAOSpatialFilterExtents);

        GLSLUtil.setFloat(SpatialFilterPass.CSShader, "AmbientOcclusionIntensity", shaderUniformData.AmbientOcclusionIntensity);
        GLSLUtil.setFloat(SpatialFilterPass.CSShader, "AmbientOcclusionPower", shaderUniformData.AmbientOcclusionPower);

        Texture2D inputAO = useArray ? InterleavePass.Output : HorizonSearchIntegratePass.Output;

        GLES.glBindTextureUnit(0, inputAO);
        GLES31.glBindSampler(0, mSamplerPoint);
        GLES.glBindTextureUnit(1, parameters.SceneDepth);
        GLES31.glBindSampler(1, mSamplerPoint);
        GLES31.glBindImageTexture(0, SpatialFilterPass.Output.getTexture(),0, false, 0, GLES32.GL_WRITE_ONLY, SpatialFilterPass.Output.getFormat());
        GLES31.glDispatchCompute(NvUtils.divideAndRoundUp(outputWidth, 8), NvUtils.divideAndRoundUp(outputHeight, 8), 1);

        GLES.glBindTextureUnit(0, null);
        GLES.glBindTextureUnit(1, null);
        GLES31.glBindImageTexture(0, 0,0, false, 0, GLES32.GL_WRITE_ONLY, GLES32.GL_RGBA8);

        SpatialFilterPass.CSShader.printOnce();
    }

    protected void AddGTAOUpsamplePass(SSAOParameters parameters){
        if (UpsamplePass.CSShader == null) {
            UpsamplePass.CSShader = NvGLSLProgram.createProgram(shaderPath + "GTAOUpsampleCS.comp", null);
        }

        UpsamplePass.CSShader.enable();
        GLES.glBindTextureUnit(0, SpatialFilterPass.Output);
        GLES32.glBindSampler(0, mSamplerPoint);
        GLES32.glBindImageTexture(0, parameters.ResultAO.getTexture(),0, false, 0, GLES32.GL_WRITE_ONLY, parameters.ResultAO.getFormat());
        GLES32.glDispatchCompute(NvUtils.divideAndRoundUp(parameters.ResultAO.getWidth(), 8), NvUtils.divideAndRoundUp(parameters.ResultAO.getHeight(), 8), 1);
        GLES.glBindTextureUnit(0, null);
        GLES32.glBindImageTexture(0, 0,0, false, 0, GLES32.GL_WRITE_ONLY, GLES32.GL_RGBA8);

        UpsamplePass.CSShader.printOnce();
        GLES.checkGLError();
    }

    FGTAOShaderParameters GetGTAOShaderParameters(SSAOParameters parameters, int frame)
    {
		final int TemporalFrame = 0;

        FGTAOShaderParameters Result = shaderParameters;

		final float Rots[] = { 60.0f, 300.0f, 180.0f, 240.0f, 120.0f, 0.0f };
        final float Offsets[] = { 0.1f, 0.6f, 0.35f, 0.85f };

        float TemporalAngle = Rots[TemporalFrame % 6] * ((float)Math.PI / 360.0f);

        // Angles of rotation that are set per frame
        float SinAngle, CosAngle;
//		FMath::SinCos(&SinAngle, &CosAngle, TemporalAngle);
        SinAngle = (float)Math.sin(TemporalAngle);
        CosAngle = (float)Math.cos(TemporalAngle);

        Result.GTAOParams[0].set( CosAngle, SinAngle, Offsets[(TemporalFrame / 6) % 4] * 0.25f, Offsets[TemporalFrame % 4] );

        // Frame X = number , Y = Thickness param,
        float ThicknessBlend = parameters.ThicknessBlend;
        ThicknessBlend = NvUtils.clamp(1.0f - (ThicknessBlend * ThicknessBlend), 0.0f, 0.99f);
        Result.GTAOParams[1].set( frame, ThicknessBlend, 0.0f, 0.0f );

        // Destination buffer Size and InvSize
        int outputWidth = parameters.SceneWidth / parameters.DownscaleFactor;
        int outputHeight = parameters.SceneHeight / parameters.DownscaleFactor;
        float Fx = outputWidth;
        float Fy = outputHeight;
        Result.GTAOParams[2].set( Fx, Fy, 1.0f / Fx, 1.0f / Fy );

        // Fall Off Params
        float FallOffEnd = parameters.GTAOFalloffEnd;
        float FallOffStartRatio = NvUtils.clamp(parameters.GTAOFalloffStartRatio, 0.0f, 0.999f);
        float FallOffStart = FallOffEnd * FallOffStartRatio;
        float FallOffStartSq = FallOffStart * FallOffStart;
        float FallOffEndSq = FallOffEnd * FallOffEnd;

        float FallOffScale = 1.0f / (FallOffEndSq - FallOffStartSq);
        float FallOffBias = -FallOffStartSq * FallOffScale;

        Result.GTAOParams[3].set( FallOffStart, FallOffEnd, FallOffScale, FallOffBias );

        float TemporalBlendWeight = 0.2f; // FMath::Clamp(Settings.AmbientOcclusionTemporalBlendWeight, 0.01f, 1.0f);

        float NumAngles = NvUtils.clamp(parameters.GTAONumAngles, 1.0f, 16.0f);
        float SinDeltaAngle, CosDeltaAngle;
        float DeltaAngle = (float)Math.PI / NumAngles;
        SinDeltaAngle = (float)Math.sin(DeltaAngle);
        CosDeltaAngle = (float)Math.cos(DeltaAngle);

        Result.GTAOParams[4].set( 0.1f, NumAngles, SinDeltaAngle, CosDeltaAngle );

        Result.AmbientOcclusionFadeDistance = parameters.AmbientOcclusionFadeDistance;
        Result.AmbientOcclusionFadeRadius = parameters.AmbientOcclusionFadeRadius;
        Result.BufferSizeAndInvSize.set( (float)parameters.SceneWidth, (float)parameters.SceneHeight, 1.0f / (float)parameters.SceneWidth, 1.0f / (float)parameters.SceneHeight );

        // projection

        Result.InvTanHalfFov = parameters.Projection.m11;

        Result.ProjInfo.set(parameters.Projection.m20, parameters.Projection.m21, 1.0f/parameters.Projection.m00, 1.0f/parameters.Projection.m11);

        float depthLinearizeMul = parameters.CameraFar * parameters.CameraNear / (parameters.CameraFar - parameters.CameraNear);
        float depthLinearizeAdd = parameters.CameraFar / (parameters.CameraFar - parameters.CameraNear);
        Result.DepthUnpackConsts.set(depthLinearizeMul, depthLinearizeAdd);
        Result.DepthUnpackConsts.set(parameters.CameraFar, parameters.CameraNear);

        // TODO : no inverse used
        Result.ProjInverse.load(parameters.Projection);

        float projScale = parameters.SceneHeight * parameters.Projection.m11;

		final float radius = parameters.AmbientOcclusionSearchRadius;
        final float intensity = 1.5f;
        final float bias = 0.1f;

        // radius
        float meters2viewspace = 1.0f;
        float R = radius * meters2viewspace;
//        Result.RadiusToScreen = R * 0.5f * projScale;

        Result.ProjDia.x = parameters.Projection.m00;
        Result.ProjDia.y = parameters.Projection.m11;
        Result.ProjDia.z = parameters.Projection.m22;

        float FadeRadius = Math.max(1.0f, parameters.AmbientOcclusionFadeRadius);
        float InvFadeRadius = 1.0f / FadeRadius;
        Result.ViewRectMinX = 0;
        Result.ViewRectMinY = 0;

        Result.ViewSizeAndInvSize.x = parameters.SceneWidth / parameters.DownscaleFactor;
        Result.ViewSizeAndInvSize.y = parameters.SceneHeight / parameters.DownscaleFactor;
        Result.ViewSizeAndInvSize.z = 1.0f / Result.ViewSizeAndInvSize.x;
        Result.ViewSizeAndInvSize.w = 1.0f / Result.ViewSizeAndInvSize.y;

        Vector4f FallOffStartEndScaleBias = Result.GTAOParams[3];

        Result.WorldRadiusAdj_SinDeltaAngle_CosDeltaAngle_Thickness.set(FallOffStartEndScaleBias.y *parameters.SceneHeight * Result.ProjInverse.m00, SinDeltaAngle, CosDeltaAngle, ThicknessBlend);
        Result.FadeRadiusMulAdd_FadeDistance_AttenFactor.set(InvFadeRadius, -(parameters.AmbientOcclusionFadeDistance - FadeRadius) * InvFadeRadius,
                parameters.AmbientOcclusionFadeDistance, 2.0f / (FallOffStartEndScaleBias.y * FallOffStartEndScaleBias.y));
        Result.Power_Intensity_ScreenPixelsToSearch.x = parameters.AmbientOcclusionPower;
        Result.Power_Intensity_ScreenPixelsToSearch.y = parameters.AmbientOcclusionIntensity;

        if(mMethod == GTAOMethod.HBAO_Mobile){
            Result.WorldRadiusAdj_SinDeltaAngle_CosDeltaAngle_Thickness.w = FallOffEndSq * FallOffEndSq;
            Result.FadeRadiusMulAdd_FadeDistance_AttenFactor.x = 1.1f;
            Result.FadeRadiusMulAdd_FadeDistance_AttenFactor.w = 0.1f;
        }

        return Result;
    }

    private final class FViewNormalPass {
        NvGLSLProgram Shader = null;
        Texture2D Output;
    }

    private final class FViewDepthPass {
        NvGLSLProgram Shader;
        Texture2D Output;
    }

    private final class FDeinterleavePass {
        NvGLSLProgram Shader;
        Texture2D Output;
    }

    private final class FInterleavePass {
        NvGLSLProgram Shader = null;
        Texture2D Output;

        boolean IsHBAO;
    }

    private final class FReinterleavePass {
        NvGLSLProgram Shader;
        Texture2D Output;
    }

    private final class FAOBlurPass {
        NvGLSLProgram ShaderX = null;
        NvGLSLProgram ShaderY = null;
        Texture2D Output;
    };

    private final class FSpatialFilterPass {
        NvGLSLProgram CSShader = null;
        Texture2D Output;
    };

    private final class FUpsamplePass {
        NvGLSLProgram CSShader;
    };

    protected final class FHorizonSearchIntegratePass {
        NvGLSLProgram[] CSShader = new NvGLSLProgram[5];
        Texture2D Output;
    };
}
