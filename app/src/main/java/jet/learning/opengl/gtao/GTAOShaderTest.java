package jet.learning.opengl.gtao;

import android.opengl.GLES20;
import android.opengl.GLES32;
import android.util.Log;

import com.nvidia.developer.opengl.app.NvSampleApp;
import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.Macro;
import com.nvidia.developer.opengl.utils.NvGLSLProgram;

import jet.learning.opengl.common.TextureUtils;

public class GTAOShaderTest extends NvSampleApp {

    public static final String LOG_TAG = "GTAOShaderTest";

    @Override
    protected void initRendering() {
        {
            // Testing shader compiling
            String root = "labs/GTAO/shaders/";
            NvGLSLProgram.ms_ThrowException = false;
            Log.d(LOG_TAG,"Test UE AO shaders");
            NvGLSLProgram.createProgram(root+"GTAO1_Pass1.comp", null);
            NvGLSLProgram.createProgram(root+"GTAO1_Pass2.comp", null);
        }

// init scene
        {
            String root = "Scenes/CubeScene/shaders/";
            Log.d(LOG_TAG,"Test SceneRender");
            NvGLSLProgram.createFromFiles(root + "scene.vert", root + "scene.frag");
            GLES.checkGLError(false);
        }

//        m_ApplyAO = NvGLSLProgram.createFromFiles("labs/GTAO/shaders/quad_es3.vert", "labs/GTAO/shaders/ApplyAO.frag");
        Log.d(LOG_TAG,"Test ApplyAO");
        NvGLSLProgram.createFromFiles("shaders/Quad_VS.vert", "labs/GTAO/shaders/ApplyAO.frag");

        final String shaderPath = "labs/GTAO/shaders/";

        {
            final int DepthFormat = GLES32.GL_R32F;
            final Macro[] macros = {
//                    new Macro("OUT_FORMAT", use32Floating ? "r32f" : "r16f")
                    new Macro("OUT_FORMAT", TextureUtils.getImageFormat(DepthFormat))
            };
            Log.d(LOG_TAG,"Test GenViewNormal.comp");
            NvGLSLProgram.createProgram(shaderPath + "GenViewNormal.comp", macros);
        }

        {
            final int OutFormat = GLES32.GL_R32F;
            final Macro[] macros = new Macro[]{
//                    new Macro("OUT_FORMAT", use32Floating ? "rg32f" : "rg16f"),
                    new Macro("OUT_FORMAT", TextureUtils.getImageFormat(OutFormat) ),
                    new Macro("SHADER_QUALITY", 1)
            };
            Log.d(LOG_TAG,"Test GTAOInterleave.comp");
            NvGLSLProgram.createProgram(shaderPath + "GTAOInterleave.comp", macros);
            Log.d(LOG_TAG,"Test HBAOInterleave.comp");
            NvGLSLProgram.createProgram(shaderPath + "HBAOInterleave.comp", macros);
        }

        {
            final Macro[] macros = {
                    new Macro("THREADGROUP_SIZEX", 8),
                    new Macro("THREADGROUP_SIZEY", 8),
                    new Macro("SHADER_QUALITY", 1),
                    new Macro("OUT_FORMAT", "rgba8")
            };
            Log.d(LOG_TAG,"Test GTAOMobileHorizonIntergralCS.comp");
            NvGLSLProgram.createProgram(shaderPath + "GTAOMobileHorizonIntergralCS.comp", macros);
        }
    }

    @Override
    protected void draw() {
        GLES20.glViewport(0,0, getWidth(), getHeight());
    }

}
