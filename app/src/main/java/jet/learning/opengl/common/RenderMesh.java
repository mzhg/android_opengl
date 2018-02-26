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

import com.nvidia.developer.opengl.utils.NvDisposeable;

/**
 * Created by mazhen'gui on 2017/11/13.
 */

public interface RenderMesh extends NvDisposeable{

    void initlize(MeshParams params);

    void draw();

    public static class MeshParams{
        public int posAttribLoc;
        public int norAttribLoc;
        public int texAttribLoc;
        public int tanAttribLoc = -1;  // tangent attribution is diabled default.
    }
}
