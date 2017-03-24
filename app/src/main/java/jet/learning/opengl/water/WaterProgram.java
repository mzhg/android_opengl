package jet.learning.opengl.water;

import android.opengl.GLES20;

import com.nvidia.developer.opengl.utils.GLUtil;
import com.nvidia.developer.opengl.utils.NvGLSLProgram;

import org.lwjgl.util.vector.Matrix4f;

import jet.learning.opengl.common.SimpleOpenGLProgram;

/**
 * Created by mazhen'gui on 2017/3/21.
 */

public class WaterProgram extends SimpleOpenGLProgram{
    private int attribPos;
    private int mvpIndex = -1;
    private int lightPosIndex = -1;
    private int cubeMapNormalsIndex = -1;
    private int cameraPosIndex = -1;

    public void init(){
        NvGLSLProgram program = NvGLSLProgram.createFromFiles("water_resources/water.vert", "water_resources/water.frag");

        program.enable();
        program.setUniform1i("WaterHeightMap", 0);
        program.setUniform1i("WaterNormalMap", 1);
        program.setUniform1i("PoolSkyCubeMap", 2);
        program.disable();

//        uniform sampler2D WaterNormalMap;
//        uniform samplerCube PoolSkyCubeMap;

//        uniform vec3 LightPosition;
//        uniform vec3 CubeMapNormals[6];
//        uniform vec3 CameraPosition;

        attribPos = program.getAttribLocation("PosAttribute");
        mvpIndex = program.getUniformLocation("g_mvp");
        lightPosIndex = program.getUniformLocation("LightPosition");
        cubeMapNormalsIndex = program.getUniformLocation("CubeMapNormals");
        cameraPosIndex = program.getUniformLocation("CameraPosition");
        programID = program.getProgram();
    }

    public void setLightPosition(float x, float y, float z){
        GLES20.glUniform3f(lightPosIndex, x,y,z);
    }

    public void setCameraPosition(float x, float y, float z){
        GLES20.glUniform3f(cameraPosIndex, x,y,z);
    }

    public void setCubemapNormals(float[] normals){
        GLES20.glUniform3fv(cubeMapNormalsIndex, 6, normals, 0);
    }

    public void setMVP(Matrix4f mvpMat){
        GLES20.glUniformMatrix4fv(mvpIndex, 1, false, GLUtil.wrap(mvpMat));
    }

    @Override
    public final int getAttribPosition() { return attribPos;}

    @Override
    public int getAttribTexCoord() {
        return -1;
    }
}
