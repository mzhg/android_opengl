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
package jet.learning.opengl.water;

import com.nvidia.developer.opengl.app.NvPointerEvent;
import com.nvidia.developer.opengl.app.NvSampleApp;
import com.nvidia.developer.opengl.ui.NvTweakEnumi;

import org.lwjgl.util.vector.Vector3f;

/**
 * Created by mazhen'gui on 2017/3/21.
 */

public class WaterDemo extends NvSampleApp {

    private final float[] dropValues = {
        4.0f/256, 4.0f/128, 4.0f/64, 4.0f/32, 4.0f/16
    };
    private int dropIndex;
    COpenGLRenderer openGLRenderer;

    @Override
    public void initUI() {
        if(openGLRenderer == null)
            openGLRenderer = new COpenGLRenderer();
        NvTweakEnumi sceneIndex[] =
                {
                        new NvTweakEnumi( "DropRadius: 4/256", 0 ),
                        new NvTweakEnumi( "DropRadius: 4/128", 1 ),
                        new NvTweakEnumi( "DropRadius: 4/64", 2 ),
                        new NvTweakEnumi( "DropRadius: 4/32", 3 ),
                        new NvTweakEnumi( "DropRadius: 4/16", 4 ),
                };
        mTweakBar.addEnum("Select Scene:", createControl("dropIndex"), sceneIndex, 0x22);
    }

    @Override
    protected void initRendering() {
        m_transformer.setTranslationVec(new Vector3f(0.0f, 0.0f, 10.0f));
        m_transformer.setRotationVec(new Vector3f(-0.2f, -0.3f, 0));
        if(openGLRenderer == null)
            openGLRenderer = new COpenGLRenderer();
        openGLRenderer.Init();
    }

    @Override
    protected void draw() {
        openGLRenderer.dropRadius = dropValues[dropIndex];
        openGLRenderer.Render(m_transformer, getFrameDeltaTime());
    }

    @Override
    protected void reshape(int width, int height) {
        openGLRenderer.Resize(width, height);
    }

    @Override
    public boolean handlePointerInput(int device, int action, int modifiers, int count, NvPointerEvent[] points) {
        boolean added = false;
        for(int i = 0; i < count; i++){
            added |= openGLRenderer.AddDropByMouseClick((int)points[i].m_x, (int)points[i].m_y);
        }

        return added;
    }

}
