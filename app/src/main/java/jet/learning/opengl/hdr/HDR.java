////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2014, NVIDIA CORPORATION. All rights reserved.
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
package jet.learning.opengl.hdr;

import com.nvidia.developer.opengl.app.NvSampleApp;

/**
 * Created by mazhen'gui on 2017/3/13.
 */

public final class HDR extends NvSampleApp {
    private static final int POS_BIND = 0;
    private static final int NOR_BIND = 1;
    private static final int TEX_BIND = 2;

    private HDRScene mScene;

    @Override
    protected void initBeforeGL() {
        mScene = new HDRScene(this);
    }

    @Override
    protected void initRendering() {
        mScene.onCreate();;
    }

    @Override
    protected void onResume() {
        super.onResume();

        mScene.startHeadTracking();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mScene.stopHeadTracking();
    }

    @Override
    public void initUI() {
        mScene.initUI();
    }

    @Override
    protected void draw() {
        mScene.onDraw();
    }

    @Override
    protected void reshape(int width, int height) {
        mScene.onResize(width, height);
    }

}
