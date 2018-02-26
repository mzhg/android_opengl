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
package jet.learning.opengl.common;

import android.opengl.GLES20;

/**
 * Created by mazhen'gui on 2017/12/15.
 */
public class WaterRenderProgram extends SimpleLightProgram {
    public WaterRenderProgram() {
        super(true, null);

        int normalTex = GLES20.glGetUniformLocation(getProgram(), "g_WaterNormalMap");
        assert (normalTex >=0);
        GLES20.glUniform1i(normalTex, 1);

        int heightMapTex = GLES20.glGetUniformLocation(getProgram(), "g_WaterHeightMap");
        assert (heightMapTex >= 0);
        GLES20.glUniform1i(heightMapTex, 2);
    }

    @Override
    protected String getFragmentShaderFile(boolean uniform) {
        return "d3dcoder/WaterPS.frag";
    }

    @Override
    protected String getVertexShaderFile(boolean uniform) {
        if (uniform)
            return "d3dcoder/WaterVS.vert";
        else
            throw new UnsupportedOperationException();
    }
}
