////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2017 mzhg
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
package jet.learning.opengl.es1_x;

import android.graphics.Bitmap;
import android.opengl.GLES11;
import android.opengl.GLUtils;

import com.nvidia.developer.opengl.app.GLES1SampleApp;
import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.GLUtil;
import com.nvidia.developer.opengl.utils.Glut;
import com.nvidia.developer.opengl.utils.ImmediateRenderer;

import org.lwjgl.util.vector.Matrix4f;

import javax.microedition.khronos.opengles.GL11;

import jet.learning.opengl.shapes.DrawMode;
import jet.learning.opengl.shapes.GLVAO;
import jet.learning.opengl.shapes.Model;
import jet.learning.opengl.shapes.QuadricBuilder;
import jet.learning.opengl.shapes.QuadricMesh;
import jet.learning.opengl.shapes.QuadricSphere;
import jet.learning.opengl.shapes.QuadricTorus;

/**
 * Created by mazhen'gui on 2017/4/11.
 */

public class ModelShow extends GLES1SampleApp{
    GLVAO torue;
    GLVAO sphere;

    int texture, bg;
    ImmediateRenderer immediateRenderer;

    final Matrix4f mProj = new Matrix4f();

    @Override
    protected void initRendering() {
        QuadricBuilder builder = new QuadricBuilder();
//		builder.setAutoGenTexCoord(false);
//		builder.setGenTexCoord(false);
        builder.setXSteps(50).setYSteps(50);
        builder.setDrawMode(DrawMode.FILL);
        builder.setCenterToOrigin(true);
        builder.setPostionLocation(Model.TYPE_VERTEX);
        builder.setNormalLocation(Model.TYPE_NORMAL);
        builder.setTexCoordLocation(Model.TYPE_TEXTURE0);
        builder.setFlag(false);  // For OpenGL ES 1.1

        sphere = new QuadricMesh(builder, new QuadricSphere(1)).getModel().genVAO(false);
        torue  = new QuadricMesh(builder, new QuadricTorus()).getModel().genVAO(false);

        GLES11.glEnable(GLES11.GL_LIGHTING);
        GLES11.glEnable(GLES11.GL_LIGHT0);

        float light_pos[] = { 5, 5, 25, 1 };
        float lightDiff[] = { 1, 1, 1, 1 };
        float lightSpec[] = { 0.9f, 0.9f, 0.9f, 1 };
        float shinefact = 50;
        float materialDiff[] = {0.7f, 0.8f, 0.5f, 1};

        GLES11.glEnable(GLES11.GL_LIGHTING);
        GLES11.glEnable(GLES11.GL_LIGHT0);
        GLES11.glLightfv(GLES11.GL_LIGHT0, GLES11.GL_POSITION, light_pos, 0);
        GLES11.glLightfv(GLES11.GL_LIGHT0, GLES11.GL_DIFFUSE, lightDiff, 0);
        GLES11.glLightfv(GLES11.GL_LIGHT0, GLES11.GL_SPECULAR, lightSpec, 0);
        GLES11.glMaterialfv(GLES11.GL_FRONT_AND_BACK, GLES11.GL_SPECULAR, lightSpec, 0);
        GLES11.glMaterialf(GLES11.GL_FRONT_AND_BACK, GLES11.GL_SHININESS, shinefact);
        GLES11.glMaterialfv(GLES11.GL_FRONT_AND_BACK, GLES11.GL_DIFFUSE, materialDiff, 0);
//        GLES11.glColorMaterial(GLES11.GL_FRONT_AND_BACK, GLES11.GL_DIFFUSE);
        GLES11.glEnable(GLES11.GL_COLOR_MATERIAL);

        GLES11.glEnable(GLES11.GL_DEPTH_TEST);
//        GLES11.glColor3f(0.7f, 0.8f, 0.5f);

        texture = GLES.glGenTextures();
        GLES11.glBindTexture(GLES11.GL_TEXTURE_2D, texture);
//        Pixels img = Glut.loadImage("earth.png");
//        img.uploadTexture2D();
        Bitmap bitmap = Glut.loadBitmapFromAssets("textures/earth.png");
        GLUtils.texImage2D(GLES11.GL_TEXTURE_2D, 0, bitmap, 0);
        GLES11.glTexParameteri(GLES11.GL_TEXTURE_2D, GLES11.GL_TEXTURE_MAG_FILTER, GLES11.GL_LINEAR);
        GLES11.glTexParameteri(GLES11.GL_TEXTURE_2D, GLES11.GL_TEXTURE_MIN_FILTER, GLES11.GL_LINEAR);
        GLES11.glEnable(GLES11.GL_TEXTURE_2D);
        bitmap.recycle();

        GLES11.glEnable(GLES11.GL_NORMALIZE);
        GLES11.glEnable(GLES11.GL_CULL_FACE);

        bg = GLES.glGenTextures();
        GLES11.glBindTexture(GLES11.GL_TEXTURE_2D, bg);
//        img = Glut.loadImage("Environment.jpg");
//        img.uploadTexture2D();
        bitmap = Glut.loadBitmapFromAssets("textures/Environment.jpg");
        GLUtils.texImage2D(GLES11.GL_TEXTURE_2D, 0, bitmap, 0);
        GLES11.glTexParameteri(GLES11.GL_TEXTURE_2D, GLES11.GL_TEXTURE_MAG_FILTER, GLES11.GL_LINEAR);
        GLES11.glTexParameteri(GLES11.GL_TEXTURE_2D, GLES11.GL_TEXTURE_MIN_FILTER, GLES11.GL_LINEAR);

        bitmap.recycle();

        GLES.checkGLError();
        immediateRenderer = new ImmediateRenderer(ImmediateRenderer.GLES1);
        m_transformer.setTranslation(0,0,-4);
    }

    @Override
    protected void draw() {
        GLES11.glDisable(GLES11.GL_CULL_FACE);
        GLES11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        GLES11.glViewport(0, 0, getWidth()/2, getHeight());
        GLES11.glMatrixMode(GL11.GL_MODELVIEW);
        GLES11.glLoadIdentity();
//        GLU.gluLookAt(0, 0, 4, 0, 0, 0, 0, 1, 0);
        Matrix4f mView = getViewMatrix();
        GLES11.glLoadMatrixf(GLUtil.wrap(mView));

        GLES11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
//        scale = 1.2f;
        // 这里注意一下， 缩放之后，光照效果会出错：放大时， 高光效果会变弱，感觉顶点法向量发散了；缩小时，高光效果会增强，法向量收敛了。原因是
        // scale会影响gl_NormalMatrix, gl_Normal经它转换后，长度会发生变化，变成了非单位向量。解决此问题的方法是调用
        // glEnable(GL_NORMALIZE)。 这样光照的问题就解决了。

//        GL11.glRotatef(rotX, 1, 0, 0);
//        GL11.glRotatef(rotY, 0, 1, 0);
//        GL11.glScalef(scale, scale, scale);


//		sphere.draw();
//        sphere1.draw(radius, stacks, slices);
        torue.bind();
        torue.draw(DrawMode.FILL.getGLMode());
        torue.unbind();

        GLES11.glDisable(GL11.GL_LIGHTING);
        GLES11.glLoadIdentity();
        GLES11.glMatrixMode(GL11.GL_PROJECTION);
        GLES11.glPushMatrix();
        GLES11.glLoadIdentity();
        GLES11.glOrthof(-1, 1, -1, 1, 0.1f, 1);

        GLES11.glBindTexture(GL11.GL_TEXTURE_2D, bg);

        /*GL11.glBegin(GL11.GL_QUADS);
        {
            GL11.glTexCoord2f(0, 0);
            GL11.glVertex3f(-1, -1, depth);

            GL11.glTexCoord2f(1, 0);
            GL11.glVertex3f(1, -1, depth);

            GL11.glTexCoord2f(1, 1);
            GL11.glVertex3f(1, 1, depth);

            GL11.glTexCoord2f(0, 1);
            GL11.glVertex3f(-1, 1, depth);
        }
        GL11.glEnd();*/
        immediateRenderer.begin(GLES11.GL_TRIANGLE_FAN, ImmediateRenderer.TEXTURE0);
        {
            immediateRenderer.texCoord(0,0);
            immediateRenderer.vertex(-1, -1, -0.999999f);

            immediateRenderer.texCoord(1, 0);
            immediateRenderer.vertex(1, -1, -0.999999f);

            immediateRenderer.texCoord(1, 1);
            immediateRenderer.vertex(1, 1, -0.999999f);

            immediateRenderer.texCoord(0, 1);
            immediateRenderer.vertex(-1, 1, -0.999999f);
        }
        immediateRenderer.end();

        GLES11.glPopMatrix();
        GLES11.glEnable(GL11.GL_LIGHTING);
        GLES11.glEnable(GLES11.GL_LIGHT0);

        GLES11.glMatrixMode(GL11.GL_MODELVIEW);
        GLES11.glLoadIdentity();
//        GLU.gluLookAt(0, 0, 4, 0, 0, 0, 0, 1, 0);
        GLES11.glLoadMatrixf(GLUtil.wrap(mView));
        GLES11.glBindTexture(GL11.GL_TEXTURE_2D, texture);

        GLES11.glViewport(getWidth()/2, 0, getWidth()/2, getHeight());
//        GLES11.glRotatef(rotX, 1, 0, 0);
//        GLES11.glRotatef(rotY, 0, 1, 0);

        sphere.bind();
        sphere.draw(DrawMode.FILL.getGLMode());
        sphere.unbind();

        GLES11.glViewport(0, 0, getWidth(), getHeight());
    }

    @Override
    protected void reshape(int width, int height) {
        GLES11.glMatrixMode(GLES11.GL_PROJECTION);
//        GLU.gluPerspective(50, (float)getWidth()/(2 * getHeight()), 0.1f, 10.0f);
        Matrix4f.perspective(60, (float)getWidth()/(2 * getHeight()), 0.1f, 10.0f, mProj);
        GLES11.glLoadMatrixf(GLUtil.wrap(mProj));
        GLES11.glMatrixMode(GL11.GL_MODELVIEW);

//        GLU.gluLookAt(0, 0, 4, 0, 0, 0, 0, 1, 0);
    }
}
