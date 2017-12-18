package jet.learning.opengl.samples;

import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLES31;
import android.opengl.GLES32;

import com.nvidia.developer.opengl.app.NvSampleApp;
import com.nvidia.developer.opengl.ui.NvTweakEnumi;
import com.nvidia.developer.opengl.utils.FieldControl;
import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.GLUtil;
import com.nvidia.developer.opengl.utils.NvAssetLoader;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import java.util.Arrays;

import javax.microedition.khronos.opengles.GL11;

/**
 * This dample demonstrates the use of approximating Catmull-clark subdivision surfaces with gregory patches.<p></p>
 * Created by mazhen'gui on 2017/12/18.
 */

public final class TessellationPatterns extends NvSampleApp {
    static final int EQUAL = 0;
    static final int FRACTIONAL_ODD = 1;
    static final int FRACTIONAL_EVENT = 2;

    static final int TRIANGLE = 1;
    static final int QUAD = 0;

    int g_tessellationMode = EQUAL;

    final int[] g_outterTess = new int[4];
    final int[] g_innerTess = new int[2];
    final int[][] mPrograms = new int[2][3];
    final int[][] mUniformLoc = new int[2][3];
    final Matrix4f f4x4WorldViewProjection = new Matrix4f(); // World * View * Projection matrix
    final Matrix4f mProjection = new Matrix4f();
    final int[] mVB = new int[2];
    final int[] mVAO = new int[2];

    @Override
    public void initUI() {
        NvTweakEnumi[] enums = {
                new NvTweakEnumi("Equal", EQUAL),
                new NvTweakEnumi("Even", FRACTIONAL_EVENT),
                new NvTweakEnumi("Odd", FRACTIONAL_ODD),
        };

        mTweakBar.addMenu("Fractional", createControl("g_tessellationMode"), enums, 0);

        String[] names = {"Left", "Bottom", "Right", "Top"};
        for(int i = 0; i < 4; i++){
            FieldControl control = new FieldControl(this, i, "g_outterTess", FieldControl.CALL_FIELD);
            mTweakBar.addValue("Outter " + names[i], control, 1, 30, 1, 0);
        }

        String[] inames = {"Vertical", "Horizental"};
        for(int i = 0; i < 2; i++){
            FieldControl control = new FieldControl(this, i, "g_innerTess", FieldControl.CALL_FIELD);
            mTweakBar.addValue("Inner " + inames[i], control, 1, 30, 1, 0);
        }
    }

    @Override
    protected void initRendering() {
        setTitle("OpenGL Tessellation Pattern");

        Arrays.fill(g_innerTess, 4);
        Arrays.fill(g_outterTess, 6);

        // build program.
        int vs = compileShaderSource(GLES20.GL_VERTEX_SHADER, NvAssetLoader.readText("shaders/tessellation_patterns.glvs"));
        int fs = compileShaderSource(GLES20.GL_FRAGMENT_SHADER, NvAssetLoader.readText("shaders/tessellation_patterns.glfs"));
        int gs = compileShaderSource(GLES32.GL_GEOMETRY_SHADER, NvAssetLoader.readText("shaders/tessellation_patterns.glgs"));

        final int[] tces = new int[2];
        tces[0] = compileShaderSource(GLES32.GL_TESS_CONTROL_SHADER, NvAssetLoader.readText("shaders/tessellation_patterns4.gltc"));
        tces[1] = compileShaderSource(GLES32.GL_TESS_CONTROL_SHADER, NvAssetLoader.readText("shaders/tessellation_patterns3.gltc"));
        final String teSource = NvAssetLoader.readText("shaders/tessellation_patterns.glte").toString();
        final String tag = "#pattern";
        final String[] types = {"quads", "triangles"};
        final String[] func = {"Quad", "Triangle"};
        final String[] modes = {"equal_spacing", "fractional_even_spacing", "fractional_odd_spacing"};
        final String pattern = "layout (%s, %s, ccw) in;\n";

        for(int i = 0; i < 2; i++){
            for(int j = 0; j < 3; j++){
                String pat = String.format(pattern, types[i], modes[j]);
                String testring = teSource.replace(tag, pat);
                int te = compileShaderSource(GLES32.GL_TESS_EVALUATION_SHADER, testring);

                int program = GLES20.glCreateProgram();
                GLES20.glAttachShader(program, vs);
                GLES20.glAttachShader(program, te);
                GLES20.glAttachShader(program, tces[i]);
                GLES20.glAttachShader(program, gs);
                GLES20.glAttachShader(program, fs);

                GLES20.glLinkProgram(program);

                checkProgramLinkStatus(program);

                GLES20.glDeleteShader(te);

                GLES20.glUseProgram(program);
                /*int funIndex = GL40.glGetSubroutineIndex(program, GL40.GL_TESS_EVALUATION_SHADER, func[i]);
                GL40.glUniformSubroutinesu(GL40.GL_TESS_EVALUATION_SHADER, GLUtil.wrap(funIndex));*/
                int funIndex = GLES20.glGetUniformLocation(program, "g_shape");
                assert funIndex>=0;
                GLES20.glUniform1i(funIndex, i == 0 ? 1 : 0);
                GLES20.glUseProgram(0);

                mPrograms[i][j] = program;
                mUniformLoc[i][j] = GLES20.glGetUniformLocation(program, "g_f4x4WorldViewProjection");
            }
        }

        GLES20.glDeleteShader(vs);
        GLES20.glDeleteShader(fs);
        GLES20.glDeleteShader(tces[0]);
        GLES20.glDeleteShader(tces[1]);
        GLES20.glDeleteShader(gs);

        float width = 20.0f;
        float offset = 24.0f;
        Vector3f[] data = new Vector3f[4];
        data[0] = new Vector3f(-width+offset,-width,0.0f);
        data[1] = new Vector3f(width+offset,-width,0.0f);
        data[2] = new Vector3f(width+offset,width,0.0f);
        data[3] = new Vector3f(-width+offset,width,0.0f);

        createBuffer(data, QUAD);

        offset = 24;
        data = new Vector3f[3];
        data[0] = new Vector3f(-width-offset,-width,0.0f);
        data[1] = new Vector3f(width-offset,-width,0.0f);
        data[2] = new Vector3f(-offset,width/1.5f,0.0f);

        createBuffer(data, TRIANGLE);

        m_transformer.setTranslationVec(new Vector3f(0.0f, 0.0f, -100.0f));
        GLES.checkGLError();
    }

    @Override
    protected void draw() {
        GLES20.glEnable(GL11.GL_DEPTH_TEST);
        GLES20.glClear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);

        // render the quad
        GLES30.glBindVertexArray(mVAO[QUAD]);
        int program = mPrograms[QUAD][g_tessellationMode];
        GLES20.glUseProgram(program);

        m_transformer.getModelViewMat(f4x4WorldViewProjection);
        // MVP = P * MV
        Matrix4f.mul(mProjection, f4x4WorldViewProjection, f4x4WorldViewProjection);
        GLES20.glUniformMatrix4fv(mUniformLoc[QUAD][g_tessellationMode], 1, false, GLUtil.wrap(f4x4WorldViewProjection));
        {
            int outterTessIdx = GLES20.glGetUniformLocation(program, "g_outterTess");
            int innerTessIdx = GLES20.glGetUniformLocation(program, "g_innerTess");

            assert (outterTessIdx>=0);
            assert (innerTessIdx>=0);

            GLES32.glUniform4iv(outterTessIdx, 1, GLUtil.wrap(g_outterTess));
            GLES32.glUniform2iv(innerTessIdx, 1, GLUtil.wrap(g_innerTess));
        }

        GLES32.glPatchParameteri(GLES32.GL_PATCH_VERTICES, 4);
        GLES20.glDrawArrays(GLES32.GL_PATCHES, 0, 4);


        // render the triangle
        GLES30.glBindVertexArray(mVAO[TRIANGLE]);
        program = mPrograms[TRIANGLE][g_tessellationMode];
        GLES20.glUseProgram(program);
        GLES20.glUniformMatrix4fv(mUniformLoc[TRIANGLE][g_tessellationMode], 1, false, GLUtil.wrap(f4x4WorldViewProjection));

        {
            int outterTessIdx = GLES20.glGetUniformLocation(program, "g_outterTess");
            int innerTessIdx = GLES20.glGetUniformLocation(program, "g_innerTess");

            assert (outterTessIdx>=0);
            assert (innerTessIdx>=0);

            GLES32.glUniform4iv(outterTessIdx, 1, GLUtil.wrap(g_outterTess));
            GLES32.glUniform2iv(innerTessIdx, 1, GLUtil.wrap(g_innerTess));
        }

        GLES32.glPatchParameteri(GLES32.GL_PATCH_VERTICES, 3);
        GLES20.glDrawArrays(GLES32.GL_PATCHES, 0, 3);
        GLES30.glBindVertexArray(0);

        GLES.checkGLError();
    }

    @Override
    protected void reshape(int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        Matrix4f.perspective(45, (float)width/height, 0.1f, 2000, mProjection);
    }

    public static final int compileShaderSource(int type, CharSequence source){
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, source.toString());

        // Compile shaders and check for any errors
        GLES20.glCompileShader(shader);

        checkShaderCompileStatus(type, shader);
        return shader;
    }

    public static final void checkShaderCompileStatus(int type, int shader){
        int success = GLES.glGetShaderi(shader, GLES20.GL_COMPILE_STATUS);
        if (success == 0) {
            String infoLog = GLES20.glGetShaderInfoLog(shader);
            GLES20.glDeleteShader(shader);

            throw new IllegalArgumentException(String.format("Shader(%s) compile error: %s\n", getShaderName(type), infoLog));
        }
    }

    private static String getShaderName(int type){
        switch (type) {
            case GLES20.GL_VERTEX_SHADER:
                return "Vertex";
            case GLES20.GL_FRAGMENT_SHADER:
                return "Fragment";
            case GLES32.GL_GEOMETRY_SHADER:
                return "Geometry";
            case GLES32.GL_TESS_CONTROL_SHADER:
                return "TessControl";
            case GLES32.GL_TESS_EVALUATION_SHADER:
                return "TessEvaluation";
            case GLES31.GL_COMPUTE_SHADER:
                return "Compute";
            default:
                return "Unkown";
        }
    }

    public static final void checkProgramLinkStatus(int program){
        int success = GLES.glGetProgrami(program, GLES20.GL_LINK_STATUS);
        if (success == 0) {
            String infoLog = GLES20.glGetProgramInfoLog(program);
            GLES20.glDeleteProgram(program);

            throw new IllegalArgumentException("program link error: " + infoLog);
        }
    }

    void createBuffer(Vector3f[] data, int shape){
        mVB[shape] = GLES.glGenBuffers();
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVB[shape]);
        GLES.glBufferData(GLES20.GL_ARRAY_BUFFER, GLUtil.wrap(data), GLES20.GL_STATIC_DRAW);

        mVAO[shape] = GLES.glGenVertexArrays();
        GLES30.glBindVertexArray(mVAO[shape]);
        {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVB[shape]);

            GLES20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
            GLES20.glEnableVertexAttribArray(0);
        }
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES30.glBindVertexArray(0);
    }
}
