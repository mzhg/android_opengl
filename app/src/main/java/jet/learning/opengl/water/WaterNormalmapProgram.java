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

import com.nvidia.developer.opengl.utils.NvGLSLProgram;

import jet.learning.opengl.common.SimpleOpenGLProgram;

/**
 * Created by mazhen'gui on 2017/3/21.
 */

final class WaterNormalmapProgram extends SimpleOpenGLProgram {
    public void init(){
        NvGLSLProgram program = NvGLSLProgram.createFromFiles("hdr_shaders/quad_es3.vert", "water_resources/waternormalmap.frag");

        program.enable();
        program.setUniform1i("WaterHeightMap", 0);
        program.setUniform1f("ODWNMR", 1.0f / (float)COpenGLRenderer.WHMR);
        program.setUniform1f("WMSDWNMRM2", 4.0f / (float)COpenGLRenderer.WHMR);
        program.disable();

        programID = program.getProgram();
        findAttrib();
    }
}
