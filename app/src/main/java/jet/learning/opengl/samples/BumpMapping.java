package jet.learning.opengl.samples;

import android.graphics.Bitmap;
import android.opengl.GLES11;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import com.nvidia.developer.opengl.app.NvSampleApp;
import com.nvidia.developer.opengl.utils.BufferUtils;
import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.GLUtil;
import com.nvidia.developer.opengl.utils.Glut;
import com.nvidia.developer.opengl.utils.NvGLSLProgram;
import com.nvidia.developer.opengl.utils.NvUtils;
import com.nvidia.developer.opengl.utils.Pixels;
import com.nvidia.developer.opengl.utils.Pixels3;
import com.nvidia.developer.opengl.utils.Pixels4;
import com.nvidia.developer.opengl.utils.VectorUtil;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3b;
import org.lwjgl.util.vector.Vector3f;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL11;

import jet.learning.opengl.shapes.Model;

/**
 * This entry demonstrates tangent space bump mapping using a GLSL vertex and fragment shader.
 * The vertex shader transforms the light and half-angle vectors into tangent space and the
 * fragment shader uses the normal fetched from a normal map to do per-pixel bump mapping on
 * a sphere.<p>
 *
 * The entry also demonstrates a bump mapping technique called parallax bump mapping where the
 * height map is used to offset the texture coordinates used to fetch from the diffuse and
 * normal maps to produce the illusion of more depth in the bumps.<p>
 *
 * Created by mazhen'gui on 2017/10/13.
 */

public final class BumpMapping extends NvSampleApp {
    private static final String TAG = "BumpMapping";

    private static final int DECAL_MAP_UNIT = 0;
    private static final int HEIGHT_MAP_UNIT = 1;
    private static final int NORMAL_MAP_UNIT = 2;
    private BumpRenderProgram bumpRenderProgram;

    // texture ids
    private int decal;
    private int heightmap;
    private int normalmap;

    private int sphereVBO;
    private int sphereIBO;
    private int sphereIndiceCount;

    private final Matrix4f viewMat = new Matrix4f();
    private final Matrix4f viewInvMat = new Matrix4f();
    private final Matrix4f projMat = new Matrix4f();
    private float light_rotation;
    private boolean parallaxMapping = false;

    @Override
    public void initUI() {
        mTweakBar.addValue("ParallaxMapping", createControl("parallaxMapping"));
    }

    @Override
    protected void initRendering() {
        bumpRenderProgram = new BumpRenderProgram();
        GLES.checkGLError();
        decal = GLES.glGenTextures();
        GLES11.glBindTexture(GLES11.GL_TEXTURE_2D, decal);

        Bitmap bitmap = Glut.loadBitmapFromAssets("textures/earth.png");
        GLUtils.texImage2D(GLES11.GL_TEXTURE_2D, 0, bitmap, 0);
        GLES11.glTexParameteri(GLES11.GL_TEXTURE_2D, GLES11.GL_TEXTURE_MAG_FILTER, GLES11.GL_LINEAR);
        GLES11.glTexParameteri(GLES11.GL_TEXTURE_2D, GLES11.GL_TEXTURE_MIN_FILTER, GLES11.GL_LINEAR);
        GLES11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GLES11.GL_CLAMP_TO_EDGE);
        GLES11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GLES11.GL_CLAMP_TO_EDGE);
        bitmap.recycle();
        GLES.checkGLError();

        Pixels bump_img = Glut.loadImageFromAssets("textures/earth_bump.png");
        // create height map texture
        heightmap = GLES.glGenTextures();
        GLES11.glBindTexture(GL11.GL_TEXTURE_2D, heightmap);
        GLES11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GLES11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GLES11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GLES11.GL_CLAMP_TO_EDGE);
        GLES11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GLES11.GL_CLAMP_TO_EDGE);
        bump_img.uploadTexture2D(GL11.GL_TEXTURE_2D, GL11.GL_RGBA, GL11.GL_RGBA, true);
        GLES.checkGLError();

        // create normal map from heightmap
        Pixels3 img3 = bumpmap_to_normalmap((Pixels4)bump_img, new Vector3f(1.0f,1.0f, 0.2f));

        normalmap = GLES.glGenTextures();
        GLES11.glBindTexture(GL11.GL_TEXTURE_2D, normalmap);
        GLES11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GLES11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GLES11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GLES11.GL_CLAMP_TO_EDGE);
        GLES11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GLES11.GL_CLAMP_TO_EDGE);
        img3.uploadTexture2D(GL11.GL_TEXTURE_2D, true);
        GLES.checkGLError();

        generateSphereModels(50,60);
        GLES.checkGLError();

        m_transformer.setTranslationVec(new Vector3f(0.0f, 0.0f, -2));
//        m_transformer.setRotationVec(new Vector3f(-0.2f, -0.3f, 0));
    }

    @Override
    protected void draw() {
        GLES20.glViewport(0,0,getWidth(), getHeight());
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glClearColor(0.29f, 0.29f, 0.29f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT|GLES20.GL_DEPTH_BUFFER_BIT);

        m_transformer.getModelViewMat(viewMat);
        viewInvMat.setIdentity();
        viewInvMat.rotate(light_rotation, Vector3f.Z_AXIS);

        light_rotation += Math.toRadians(1.f);

        Vector3f l = new Vector3f(1,1,3);  //
        l.normalise();
        Matrix4f.transformVector(viewInvMat, l, l);

        viewMat.transpose(viewInvMat);
        VectorUtil.transformVector3( l,viewInvMat, l);

        Vector3f eye = new Vector3f(0,0,1);
        Matrix4f.transformVector(viewInvMat, eye, eye);
        Vector3f h = Vector3f.add(l, eye, null);
        h.normalise();

        Matrix4f.mul(projMat, viewMat, viewInvMat);

        bumpRenderProgram.enable();
        bumpRenderProgram.setHalfAngle(h.x,h.y,h.z);
        bumpRenderProgram.setLight(l.x, l.y, l.z);
        bumpRenderProgram.setModelView(viewMat);
        bumpRenderProgram.setModelViewProjection(viewInvMat);
        bumpRenderProgram.setparallaxMapping(parallaxMapping);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + DECAL_MAP_UNIT);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, decal);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + HEIGHT_MAP_UNIT);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, heightmap);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + NORMAL_MAP_UNIT);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, normalmap);

        final int stride = (3 + 9 + 2) * 4;
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, sphereVBO);
        GLES20.glVertexAttribPointer(bumpRenderProgram.getPositionAttrib(), 3, GLES20.GL_FLOAT, false, stride, 0);
        GLES20.glEnableVertexAttribArray(bumpRenderProgram.getPositionAttrib());

        GLES20.glVertexAttribPointer(bumpRenderProgram.getTangentBasisAttrib()+0, 3, GLES20.GL_FLOAT, false, stride, 3 * 4);
        GLES20.glVertexAttribPointer(bumpRenderProgram.getTangentBasisAttrib()+1, 3, GLES20.GL_FLOAT, false, stride, 6 * 4);
        GLES20.glVertexAttribPointer(bumpRenderProgram.getTangentBasisAttrib()+2, 3, GLES20.GL_FLOAT, false, stride, 9 * 4);
        GLES20.glEnableVertexAttribArray(bumpRenderProgram.getTangentBasisAttrib()+0);
        GLES20.glEnableVertexAttribArray(bumpRenderProgram.getTangentBasisAttrib()+1);
        GLES20.glEnableVertexAttribArray(bumpRenderProgram.getTangentBasisAttrib()+2);

        GLES20.glVertexAttribPointer(bumpRenderProgram.getTexcoordAttrib(), 2, GLES20.GL_FLOAT, false, stride, 12 * 4);
        GLES20.glEnableVertexAttribArray(bumpRenderProgram.getTexcoordAttrib());

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, sphereIBO);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, sphereIndiceCount, GLES20.GL_UNSIGNED_SHORT, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glDisableVertexAttribArray(bumpRenderProgram.getPositionAttrib());
        GLES20.glDisableVertexAttribArray(bumpRenderProgram.getTangentBasisAttrib()+0);
        GLES20.glDisableVertexAttribArray(bumpRenderProgram.getTangentBasisAttrib()+1);
        GLES20.glDisableVertexAttribArray(bumpRenderProgram.getTangentBasisAttrib()+2);
        GLES20.glDisableVertexAttribArray(bumpRenderProgram.getTexcoordAttrib());
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + DECAL_MAP_UNIT);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + HEIGHT_MAP_UNIT);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + NORMAL_MAP_UNIT);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES.checkGLError();
    }

    @Override
    protected void reshape(int width, int height) {
        Matrix4f.perspective(60, (float)width/height, 0.1f, 100.0f, projMat);
    }

    private static Pixels3 bumpmap_to_normalmap(Pixels4 src, Vector3f scale) {
        int w = src.getWidth();
        int h = src.getHeight();
        if (scale.x == 0.f || scale.y == 0.f || scale.z == 0.f) {
            float a = (float) w / (float) h;
            if (a < 1.f) {
                scale.x = 1.f;
                scale.y = 1.f / a;
            } else {
                scale.x = a;
                scale.y = 1.f;
            }
            scale.z = 1.f;
        }

        Pixels3 dst = new Pixels3(w, h);

        for (int i = 1; i < w - 1; i++) {
            for (int j = 1; j < h - 1; j++) {
                Vector3f dfdi = new Vector3f(2.f, 0.f, ((float) (src.get(i + 1, j).x
                        & 0xFF - src.get(i - 1, j).x & 0xFF)) / 255.f);
                Vector3f dfdj = new Vector3f(0.f, 2.f, ((float) (src.get(i, j + 1).x
                        & 0xFF - src.get(i, j - 1).x & 0xFF)) / 255.f);
                // vec3f n = dfdi.cross(dfdj);
                Vector3f n = Vector3f.cross(dfdi, dfdj, null);
                modulate(n, scale);
                n.normalise();
                // dst(i,j) = range_compress(n);
                dst.set(i, j, range_compress(n));
            }
        }

        // microsoft non-ansi c++ scoping concession
        {
            // cheesy boundary cop-out
            for (int i = 0; i < w; i++) {
                // dst(i,0) = dst(i,1);
                dst.assgin(i, 0, i, 1);
                // dst(i,h-1) = dst(i,h-2);
                dst.assgin(i, h - 1, i, h - 2);
            }
            for (int j = 0; j < h; j++) {
                // dst(0,j) = dst(1,j);
                dst.assgin(0, j, 1, j);
                // dst(w-1,j) = dst(w-2,j);
                dst.assgin(w - 1, j, w - 2, j);
            }
        }
        dst.setInternalFormat(GLES20.GL_RGB);
        return dst;
    }

    private void generateSphereModels(int x_steps, int y_steps){
        float s_step = 1.0f/(x_steps - 1);
        float t_step = 1.0f/(y_steps - 1);
        int vCount = x_steps * y_steps;

        FloatBuffer vertexData = BufferUtils.createFloatBuffer(vCount * (3 + 9 + 2));
        final Vector3f position = new Vector3f();
        final Vector3f normal = new Vector3f();
        final Vector3f tangent = new Vector3f();
        final Vector3f binormal = new Vector3f();
        final Vector2f texCoord = new Vector2f();

        for(int i = 0; i < x_steps; i++){
            final float x = i * s_step;
            float phi = NvUtils.PI*x - NvUtils.PI/2;
            for(int j = 0; j < y_steps; j++){
                final float y = j * t_step;

                float theta = (NvUtils.PI * 2.0f * y);
//                float phi   = (NvUtils.PI * y);

                sphere_binormal(theta, phi, binormal);
                sphere_position(theta, phi, position);
                sphere_normal(theta, phi, normal);
                sphere_tangent(theta, phi, tangent);
                texCoord.set(y,x);

                position.store(vertexData);
                tangent.store(vertexData);
                binormal.store(vertexData);
                normal.store(vertexData);
                texCoord.store(vertexData);
            }
        }
        vertexData.flip();

        sphereVBO = GLES.glGenBuffers();
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, sphereVBO);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexData.remaining() * 4, vertexData, GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);


        ShortBuffer indiceData = genIndices(x_steps, y_steps);
        sphereIBO = GLES.glGenBuffers();
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, sphereIBO);
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indiceData.remaining() * 2, indiceData, GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        sphereIndiceCount = indiceData.remaining();
    }

    private static ShortBuffer genIndices(int width, int height){
        int count =  (width - 1) * (height - 1) * 6;
        /*AttribIntArray indices = new AttribIntArray(1, count);
        indices.resize(count);
        int[] data = indices.getArray();
        int index = 0;*/
        ShortBuffer data = GLUtil.getCachedShortBuffer(count);
        for(int x = 0; x < width - 1; x++){
            for(int y = 0; y < height - 1; y++){
                int i0 = x * height + y;
                int i1 = (x + 1) * height + y;
                int i2 = i1 + 1;
                int i3 = i0 + 1;

                data.put((short)i0).put((short)i1).put((short)i2);
                data.put((short)i0).put((short)i2).put((short)i3);
            }
        }
        data.flip();
        return data;
    }

    private static final void sphere_position(float theta, float phi, Vector3f p)
    {
        p.x = (float) (Math.cos(phi) * Math.cos(theta));
        p.y = (float) Math.sin(phi);
        p.z = (float) (- Math.cos(phi) * Math.sin(theta));
    }

    private static final void sphere_tangent(float theta, float phi, Vector3f t)
    {
        t.x = (float) - Math.sin(theta);
        t.y =   0;
        t.z = (float) - Math.cos(theta);
    }

    private static final void sphere_binormal(float theta, float phi, Vector3f b)
    {
        b.x = (float) (- Math.sin(phi) *Math. cos(theta));
        b.y = (float) Math.cos(phi);
        b.z = (float) (Math.sin(phi) * Math.sin(theta));
    }

    private static final void sphere_normal(float theta, float phi, Vector3f n)
    {
        sphere_position(theta, phi, n);
    }

    private static void modulate(Vector3f lhs, final Vector3f rhs) {
        lhs.x *= rhs.x;
        lhs.y *= rhs.y;
        lhs.z *= rhs.z;
    }

    private static Vector3b range_compress(Vector3f n) {
        Vector3b v = new Vector3b();
        v.x = range_compress(n.x);
        v.y = range_compress(n.y);
        v.z = range_compress(n.z);
        return v;
    }

    private static byte range_compress(float f) {
        return (byte) ((f + 1.0f) * 127.5f);
    }

    private static final class BumpRenderProgram extends NvGLSLProgram{
//        uniform vec3 light;
//        uniform vec3 halfAngle;
//        uniform mat4 modelViewI;
//        uniform mat4 modelViewProjection;

//        uniform sampler2D decalMap;
//        uniform sampler2D heightMap;
//        uniform sampler2D normalMap;
//
//        uniform bool parallaxMapping;

        private int light_index;
        private int halfAngle_index;
        private int modelViewI_index;
        private int modelViewProjection_index;
        private int parallaxMapping_index;

        private int positionAttrib;
        private int tangentBasisAttrib;
        private int texcoordAttrib;

        BumpRenderProgram(){
            setLinkeTask((int programID)->{
                GLES20.glBindAttribLocation(programID, Model.TYPE_VERTEX, "position");
                GLES20.glBindAttribLocation(programID, Model.TYPE_NORMAL, "position");
                GLES20.glBindAttribLocation(programID, Model.TYPE_VERTEX, "position");
            });
            setSourceFromFiles("shaders/bump_mapping_vertex.glsl", "shaders/bump_mapping_fragment.glsl");

            enable();
            setUniform1i("decalMap", DECAL_MAP_UNIT);
            setUniform1i("heightMap", HEIGHT_MAP_UNIT);
            setUniform1i("normalMap", NORMAL_MAP_UNIT);
            disable();

            light_index = getUniformLocation("light");
            halfAngle_index = getUniformLocation("halfAngle");
            modelViewI_index = getUniformLocation("modelViewI");
            modelViewProjection_index = getUniformLocation("modelViewProjection");
            parallaxMapping_index = getUniformLocation("parallaxMapping");

            // get attribute locations
            positionAttrib = getAttribLocation("position");
            assert(positionAttrib >= 0);
            tangentBasisAttrib = getAttribLocation("tangentBasis");
            assert(tangentBasisAttrib >= 0);
            texcoordAttrib = getAttribLocation("texcoord");
            assert(texcoordAttrib >= 0);

            Log.i(TAG, "positionAttrib = " + positionAttrib);
            Log.i(TAG, "tangentBasisAttrib = " + tangentBasisAttrib);
            Log.i(TAG, "texcoordAttrib = " + texcoordAttrib);
        }

        int getPositionAttrib(){return positionAttrib;}
        int getTangentBasisAttrib() { return tangentBasisAttrib;}
        int getTexcoordAttrib() { return texcoordAttrib;}
        void setLight(float x, float y, float z) {if(light_index >=0) GLES20.glUniform3f(light_index, x,y,z);}
        void setHalfAngle(float x, float y, float z) {if(halfAngle_index>=0) GLES20.glUniform3f(halfAngle_index,x,y,z);}
        void setModelView(Matrix4f mat) {if(modelViewI_index >=0) GLES20.glUniformMatrix4fv(modelViewI_index, 1, false, GLUtil.wrap(mat));}
        void setModelViewProjection(Matrix4f mat) {if(modelViewProjection_index >=0) GLES20.glUniformMatrix4fv(modelViewProjection_index, 1, false, GLUtil.wrap(mat));}
        void setparallaxMapping(boolean flag) {if(parallaxMapping_index >=0) GLES20.glUniform1i(parallaxMapping_index, flag ? 1:0);}
    }
}
