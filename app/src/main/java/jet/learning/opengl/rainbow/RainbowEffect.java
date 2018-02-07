package jet.learning.opengl.rainbow;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLUtils;

import com.nvidia.developer.opengl.utils.AttribBinder;
import com.nvidia.developer.opengl.utils.AttribBindingTask;
import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.Glut;
import com.nvidia.developer.opengl.utils.NvGLSLProgram;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.ReadableVector3f;

import javax.microedition.khronos.opengles.GL11;

/**
 * Created by mazhen'gui on 2018/2/3.
 */

final class RainbowEffect{
    static final int POSTION_LOC = 0;
    static final int TEXCOORD_LOC = 1;

    static final int  LOOKUP_MAP = 0;
    static final int  MOISTURE_MAP = 1;
    static final int  CORONA_LOOKUP_MAP = 2;

    private NvGLSLProgram   m_hTechniqueRenderRainbowQuad; // Handle to technique in the effect
    private int		        m_pRainbowLookupTextureScattering; // the lookup texture for the rainbow
    private int     		m_pCoronaLookupTexture;// the lookup texture for the corona

    //render target and texture for render to moisture texture
    private int		        m_pRenderTargetDepthBuffer;
    private int		        m_pRenderTarget;
    private int             m_pRenderTargetFBO;
    private int		        m_RenderTargetTexture_Moisture;
    private int		        m_pRenderTargetTextureSurface;//pointer to surface of texture for copy

    //place holders for begin/end moisture pass
    private int		        m_ptheRealBackBuffer;
    private int		        m_ptheRealDepthBuffer;

    //standard d3d object interface functions
    public void RestoreDeviceObjects(){
        if(m_hTechniqueRenderRainbowQuad == null){
            m_hTechniqueRenderRainbowQuad = new NvGLSLProgram();
            m_hTechniqueRenderRainbowQuad.setLinkeTask(new AttribBindingTask(new AttribBinder("In_Position", POSTION_LOC), new AttribBinder("In_TexCoord", TEXCOORD_LOC)));
            m_hTechniqueRenderRainbowQuad.setSourceFromFiles("rainbow/rainbowVS.vert", "rainbow/rainbowOnlyPS.frag");
            m_hTechniqueRenderRainbowQuad.enable();
            m_hTechniqueRenderRainbowQuad.setUniform1i("LookupMap", 0);
            m_hTechniqueRenderRainbowQuad.setUniform1i("MoistureMap", 1);
            m_hTechniqueRenderRainbowQuad.setUniform1i("CoronaLookupMap", 2);
        }

        {
            m_pRainbowLookupTextureScattering = GLES.glGenTextures();
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, m_pRainbowLookupTextureScattering);
            Bitmap image = Glut.loadBitmapFromAssets("rainbow/Rainbow_Scatter_FakeWidet.png");
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, image, 0);
            GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP_TO_EDGE);
        }

        {
            m_pCoronaLookupTexture = GLES.glGenTextures();
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, m_pCoronaLookupTexture);
            Bitmap image = Glut.loadBitmapFromAssets("rainbow/rainbow_plot_i_vs_a_diffract_0_90_1024.png");
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, image, 0);
            GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP_TO_EDGE);
        }

        //Create Render Target and Texture for moisture////////////////////////////////
	    final int DYNAMIC_TEXTURE_DIMENSIONS = 512;

        m_pRenderTargetDepthBuffer = 0;
        m_pRenderTarget = 0;

        /*if( FAILED( hr = pd3dDevice->CreateDepthStencilSurface(
                DYNAMIC_TEXTURE_DIMENSIONS,
                DYNAMIC_TEXTURE_DIMENSIONS,
                D3DFMT_D16,
                AA_TYPE,
                AA_QUALITY,
                false,
                        &m_pRenderTargetDepthBuffer,
                NULL
        )))
        {
            return hr;
        }*/

        m_pRenderTargetDepthBuffer = GLES.glGenTextures();
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, m_pRenderTargetDepthBuffer);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES30.GL_DEPTH_COMPONENT16, DYNAMIC_TEXTURE_DIMENSIONS, DYNAMIC_TEXTURE_DIMENSIONS, 0, GLES20.GL_DEPTH_COMPONENT, GLES20.GL_UNSIGNED_SHORT, null);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP_TO_EDGE);


        /*if( FAILED( hr = pd3dDevice->CreateRenderTarget(
                DYNAMIC_TEXTURE_DIMENSIONS,
                DYNAMIC_TEXTURE_DIMENSIONS,
                D3DFMT_A8R8G8B8,
                AA_TYPE, //depth stencil must have this format set as well!
                AA_QUALITY, //should really query for possible values
                false,
                        &m_pRenderTarget,
                NULL
        )))
        {
            return hr;
        }*/

        m_pRenderTarget = GLES.glGenTextures();
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, m_pRenderTarget);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, DYNAMIC_TEXTURE_DIMENSIONS, DYNAMIC_TEXTURE_DIMENSIONS, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP_TO_EDGE);

        m_pRenderTargetFBO = GLES.glGenFramebuffers();
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, m_pRenderTargetFBO);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, m_pRenderTarget, 0);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_TEXTURE_2D, m_pRenderTargetDepthBuffer, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        /*if(FAILED(hr = D3DXCreateTexture( pd3dDevice, DYNAMIC_TEXTURE_DIMENSIONS, DYNAMIC_TEXTURE_DIMENSIONS, 1, D3DUSAGE_RENDERTARGET, D3DFMT_A8R8G8B8, D3DPOOL_DEFAULT, &m_RenderTargetTexture_Moisture	)))
        {
            return hr;
        }*/

        m_RenderTargetTexture_Moisture = GLES.glGenTextures();
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, m_RenderTargetTexture_Moisture);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, DYNAMIC_TEXTURE_DIMENSIONS, DYNAMIC_TEXTURE_DIMENSIONS, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP_TO_EDGE);

//        m_RenderTargetTexture_Moisture->GetSurfaceLevel( 0 , &m_pRenderTargetTextureSurface );
        m_pRenderTargetTextureSurface = m_RenderTargetTexture_Moisture;


        //Store a copy of the real back buffer info
        /*pd3dDevice->GetBackBuffer(0,0,D3DBACKBUFFER_TYPE_MONO,&m_ptheRealBackBuffer);
        pd3dDevice->GetDepthStencilSurface(&m_ptheRealDepthBuffer);

        m_pFullScreenQuad->SetUpForRenderTargetSurface( m_ptheRealBackBuffer );*/


        /*if(m_pRainbowEffect)
        {
            m_pRainbowEffect->SetTexture( "tRainbowLookup", m_pRainbowLookupTextureScattering);
            m_pRainbowEffect->SetTexture( "tCoronaLookup", m_pCoronaLookupTexture);
            m_pRainbowEffect->SetTexture( "tMoisture", m_RenderTargetTexture_Moisture);
        }*/
    }

    public void InvalidateDeviceObjects(){
        GLES.glDeleteTextures(m_pRainbowLookupTextureScattering);
        GLES.glDeleteTextures(m_pCoronaLookupTexture);

        GLES.glDeleteTextures(m_pRenderTargetTextureSurface);
        GLES.glDeleteTextures(m_RenderTargetTexture_Moisture);
        GLES.glDeleteTextures(m_pRenderTargetDepthBuffer);
        GLES.glDeleteTextures(m_pRenderTarget);

        GLES.glDeleteTextures(m_ptheRealDepthBuffer);
        GLES.glDeleteTextures(m_ptheRealBackBuffer);
    }

    public void Create( /*LPDIRECT3DDEVICE9 pd3dDevice*/){}
    public void Destroy(){}


    /**
     sets up appropriate render target for rendering moisture to
     */
    public void BeginMoistureTextureRendering(/*LPDIRECT3DDEVICE9 pd3dDevice*/){
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, m_pRenderTargetFBO);
        m_hTechniqueRenderRainbowQuad.enable();
    }

    /**
     restores render target and updates moisture texture
     for use in RenderRainbow
     */
    public void EndMoistureTextureRendering(/*LPDIRECT3DDEVICE9 pd3dDevice*/){
        //reset the original render target and depth buffers
        /*pd3dDevice->SetRenderTarget( 0, m_ptheRealBackBuffer);
        pd3dDevice->SetDepthStencilSurface( m_ptheRealDepthBuffer);*/
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        //copy our rendertarget to a texture
//        pd3dDevice->StretchRect(m_pRenderTarget,0,m_pRenderTargetTextureSurface,0,D3DTEXF_NONE);  TODO
    }

    /**
     renders the rainbow effect
     */
    public void RenderRainbow( /*LPDIRECT3DDEVICE9 pd3dDevice*/){

    }

    /**
     set the sun's light direction in world space
     does not have to be normalized
     */
    public void SetLightDirection( ReadableVector3f lightDir){

    }

    /**
     set the droplet size of the moisture that is making the rainbow
     smaller radii droplets will cause a fogbow, larger ones will cause
     a rainbow. range: [0,1]
     */
    public void SetDropletRadius( float radius){

    }

    /**
     set the rainbow intensity
     */
    public void SetRainbowIntensity( float intensity){

    }

    /**
     set the current view matrix
     */
    public void SetViewMatrix(Matrix4f view){

    }

    /**
     set the inverse projection matrix
     */
    public void SetProjInvMatrix(Matrix4f projInv){

    }

    /**
     get access to the moisture texture,
     this is provided for visualization purposes.
     */
    public int		GetMoistureTexture(){ return m_RenderTargetTexture_Moisture;}
}
