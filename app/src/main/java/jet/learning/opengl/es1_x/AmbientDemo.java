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

import android.opengl.GLES11;
import android.opengl.GLES32;

import com.nvidia.developer.opengl.app.GLES1SampleApp;
import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.GLUtil;
import com.nvidia.developer.opengl.utils.ImmediateRenderer;

import org.lwjgl.util.vector.Matrix4f;

/**
 * Created by mazhen'gui on 2017/4/10.
 */

public class AmbientDemo extends GLES1SampleApp{

    private ImmediateRenderer mRenderer;

    @Override
    protected void initRendering() {
        GLES11.glEnable(GLES11.GL_DEPTH_TEST); // Hidden surface removal
        GLES11.glEnable(GLES11.GL_CULL_FACE); // Do not calculate inside of jet
        GLES11.glFrontFace(GLES11.GL_CCW); // Counter clock-wise polygons face out

        // Lighting stuff
        GLES11.glEnable(GLES11.GL_LIGHTING); // Enable lighting

        // Set light model to use ambient light specified by ambientLight
        // Light values
        // Bright white light
        float ambientLight[] = { 1.0f, 1.0f, 1.0f, 1.0f };
        GLES11.glLightModelfv(GLES11.GL_LIGHT_MODEL_AMBIENT, ambientLight, 0);

        GLES11.glEnable(GLES11.GL_COLOR_MATERIAL); // Enable Material color tracking

        // Front material ambient and diffuse colors track glColor
//        GLES11.glColorMaterial(GL_FRONT, GL_AMBIENT_AND_DIFFUSE);

        // Nice light blue
        GLES11.glClearColor(0.0f, 0.0f, 05.f, 1.0f);
        mRenderer = new ImmediateRenderer(ImmediateRenderer.GLES1);

        GLES.checkGLError("AmbientDemo::initRendering");
        m_transformer.setTranslation(0,0,-1);
    }

    @Override
    protected void reshape(int width, int height) {
        float w = getWidth();
        float h = getHeight();

        float nRange = 80.0f;

        // Reset projection matrix stack
        GLES11.glMatrixMode(GLES11.GL_PROJECTION);
        GLES11.glLoadIdentity();

        // Establish clipping volume (left, right, bottom, top, near, far)
        if (w <= h)
            GLES11.glOrthof(-nRange, nRange, -nRange * h / w, nRange * h / w, -nRange,
                    nRange);
        else
            GLES11.glOrthof(-nRange * w / h, nRange * w / h, -nRange, nRange, -nRange,
                    nRange);

        // Reset Model view matrix stack
        GLES11.glMatrixMode(GLES11.GL_MODELVIEW);
        GLES11.glLoadIdentity();

        GLES.checkGLError("AmbientDemo::reshape");
    }

    @Override
    protected void draw() {
        // Clear the window with current clearing color
        GLES11.glClear(GLES11.GL_COLOR_BUFFER_BIT | GLES11.GL_DEPTH_BUFFER_BIT);

        // Save the matrix state
        GLES11.glPushMatrix();
//        GLES11.glRotatef(xRot, 1.0f, 0.0f, 0.0f);
//        GLES11.glRotatef(yRot, 0.0f, 1.0f, 0.0f);
        Matrix4f mView = getViewMatrix();
        GLES11.glLoadMatrixf(GLUtil.wrap(mView));
        // Nose Cone /////////////////////////////
        // Bright Green
        GLES11.glColor4f(0,1,0,1);

        mRenderer.colorSize = 4;
        mRenderer.begin(GLES11.GL_TRIANGLES, ImmediateRenderer.COLOR);
        {
            mRenderer.color(0,1,0);
            mRenderer.vertex(0.0f, 0.0f, 60.0f);
            mRenderer.color(0,1,0);
            mRenderer.vertex(-15.0f, 0.0f, 30.0f);
            mRenderer.color(0,1,0);
            mRenderer.vertex(15.0f, 0.0f, 30.0f);

            mRenderer.color(0,1,0);
            mRenderer.vertex(15.0f, 0.0f, 30.0f);
            mRenderer.color(0,1,0);
            mRenderer.vertex(0.0f, 15.0f, 30.0f);
            mRenderer.color(0,1,0);
            mRenderer.vertex(0.0f, 0.0f, 60.0f);

            mRenderer.color(0,1,0);
            mRenderer.vertex(0.0f, 0.0f, 60.0f);
            mRenderer.color(0,1,0);
            mRenderer.vertex(0.0f, 15.0f, 30.0f);
            mRenderer.color(0,1,0);
            mRenderer.vertex(-15.0f, 0.0f, 30.0f);

            // Body of the Plane ////////////////////////
            // light gray
            mRenderer.color(192f / 255, 192f / 255, 192f / 255);
            mRenderer.vertex(-15.0f, 0.0f, 30.0f);
            mRenderer.color(192f / 255, 192f / 255, 192f / 255);
            mRenderer.vertex(0.0f, 15.0f, 30.0f);
            mRenderer.color(192f / 255, 192f / 255, 192f / 255);
            mRenderer.vertex(0.0f, 0.0f, -56.0f);

            mRenderer.color(192f / 255, 192f / 255, 192f / 255);
            mRenderer.vertex(0.0f, 0.0f, -56.0f);
            mRenderer.color(192f / 255, 192f / 255, 192f / 255);
            mRenderer.vertex(0.0f, 15.0f, 30.0f);
            mRenderer.color(192f / 255, 192f / 255, 192f / 255);
            mRenderer.vertex(15.0f, 0.0f, 30.0f);

            mRenderer.color(192f / 255, 192f / 255, 192f / 255);
            mRenderer.vertex(15.0f, 0.0f, 30.0f);
            mRenderer.color(192f / 255, 192f / 255, 192f / 255);
            mRenderer.vertex(-15.0f, 0.0f, 30.0f);
            mRenderer.color(192f / 255, 192f / 255, 192f / 255);
            mRenderer.vertex(0.0f, 0.0f, -56.0f);

            // /////////////////////////////////////////////
            // Left wing
            // Dark gray
            mRenderer.color(64f / 255, 64f / 255, 64f / 255);
            mRenderer.vertex(0.0f, 2.0f, 27.0f);
            mRenderer.color(64f / 255, 64f / 255, 64f / 255);
            mRenderer.vertex(-60.0f, 2.0f, -8.0f);
            mRenderer.color(64f / 255, 64f / 255, 64f / 255);
            mRenderer.vertex(60.0f, 2.0f, -8.0f);

            mRenderer.color(64f / 255, 64f / 255, 64f / 255);
            mRenderer.vertex(60.0f, 2.0f, -8.0f);
            mRenderer.color(64f / 255, 64f / 255, 64f / 255);
            mRenderer.vertex(0.0f, 7.0f, -8.0f);
            mRenderer.color(64f / 255, 64f / 255, 64f / 255);
            mRenderer.vertex(0.0f, 2.0f, 27.0f);

            mRenderer.color(64f / 255, 64f / 255, 64f / 255);
            mRenderer.vertex(60.0f, 2.0f, -8.0f);
            mRenderer.color(64f / 255, 64f / 255, 64f / 255);
            mRenderer.vertex(-60.0f, 2.0f, -8.0f);
            mRenderer.color(64f / 255, 64f / 255, 64f / 255);
            mRenderer.vertex(0.0f, 7.0f, -8.0f);

            // Other wing top section
            mRenderer.color(64f / 255, 64f / 255, 64f / 255);
            mRenderer.vertex(0.0f, 2.0f, 27.0f);
            mRenderer.color(64f / 255, 64f / 255, 64f / 255);
            mRenderer.vertex(0.0f, 7.0f, -8.0f);
            mRenderer.color(64f / 255, 64f / 255, 64f / 255);
            mRenderer.vertex(-60.0f, 2.0f, -8.0f);

            // Tail section///////////////////////////////
            // Bottom of back fin
            mRenderer.color(1, 1, 0);
            mRenderer.vertex(-30.0f, -0.50f, -57.0f);
            mRenderer.color(1, 1, 0);
            mRenderer.vertex(30.0f, -0.50f, -57.0f);
            mRenderer.color(1, 1, 0);
            mRenderer.vertex(0.0f, -0.50f, -40.0f);

            // top of left side
            mRenderer.color(1, 1, 0);
            mRenderer.vertex(0.0f, -0.5f, -40.0f);
            mRenderer.color(1, 1, 0);
            mRenderer.vertex(30.0f, -0.5f, -57.0f);
            mRenderer.color(1, 1, 0);
            mRenderer.vertex(0.0f, 4.0f, -57.0f);

            // top of right side
            mRenderer.color(1, 1, 0);
            mRenderer.vertex(0.0f, 4.0f, -57.0f);
            mRenderer.color(1, 1, 0);
            mRenderer.vertex(-30.0f, -0.5f, -57.0f);
            mRenderer.color(1, 1, 0);
            mRenderer.vertex(0.0f, -0.5f, -40.0f);

            // back of bottom of tail
            mRenderer.color(1, 1, 0);
            mRenderer.vertex(30.0f, -0.5f, -57.0f);
            mRenderer.color(1, 1, 0);
            mRenderer.vertex(-30.0f, -0.5f, -57.0f);
            mRenderer.color(1, 1, 0);
            mRenderer.vertex(0.0f, 4.0f, -57.0f);

            // Top of Tail section left
            mRenderer.color(1, 0, 0);
            mRenderer.vertex(0.0f, 0.5f, -40.0f);
            mRenderer.color(1, 0, 0);
            mRenderer.vertex(3.0f, 0.5f, -57.0f);
            mRenderer.color(1, 0, 0);
            mRenderer.vertex(0.0f, 25.0f, -65.0f);

            mRenderer.color(1, 0, 0);
            mRenderer.vertex(0.0f, 25.0f, -65.0f);
            mRenderer.color(1, 0, 0);
            mRenderer.vertex(-3.0f, 0.5f, -57.0f);
            mRenderer.color(1, 0, 0);
            mRenderer.vertex(0.0f, 0.5f, -40.0f);

            // Back of horizontal section
            mRenderer.color(1, 0, 0);
            mRenderer.vertex(3.0f, 0.5f, -57.0f);
            mRenderer.color(1, 0, 0);
            mRenderer.vertex(-3.0f, 0.5f, -57.0f);
            mRenderer.color(1, 0, 0);
            mRenderer.vertex(0.0f, 25.0f, -65.0f);
        }
        mRenderer.end();
        GLES11.glPopMatrix();

        GLES.checkGLError("AmbientDemo::draw");
    }
}
