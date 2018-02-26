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

/**
 * Created by mazhen'gui on 2017/3/17.
 */

final class CubeData {
    private static final float TEX_COORD_MINX = 0.0f;
    private static final float TEX_COORD_MAXX = 1.0f;
    private static final float TEX_COORD_MINY = 1.0f;
    private static final float TEX_COORD_MAXY = 0.0f;
    private static final float CUBE_SCLAE = 6.0f;

    // Interleaved vertex data
    static final float verticesCube[] = {
            // Front Face
            -CUBE_SCLAE,-CUBE_SCLAE, CUBE_SCLAE,
            0.0f, 0.0f, 1.0f,
            TEX_COORD_MINX, TEX_COORD_MINY,
            CUBE_SCLAE,-CUBE_SCLAE, CUBE_SCLAE,
            0.0f, 0.0f, 1.0f,
            TEX_COORD_MAXX, TEX_COORD_MINY,
            CUBE_SCLAE, CUBE_SCLAE, CUBE_SCLAE,
            0.0f, 0.0f, 1.0f,
            TEX_COORD_MAXX, TEX_COORD_MAXY,
            -CUBE_SCLAE, CUBE_SCLAE, CUBE_SCLAE,
            0.0f, 0.0f, 1.0f,
            TEX_COORD_MINX, TEX_COORD_MAXY,

            // Back Face
            -CUBE_SCLAE,-CUBE_SCLAE,-CUBE_SCLAE,
            0.0f, 0.0f,-1.0f,
            TEX_COORD_MINX, TEX_COORD_MINY,
            CUBE_SCLAE,-CUBE_SCLAE,-CUBE_SCLAE,
            0.0f, 0.0f,-1.0f,
            TEX_COORD_MAXX, TEX_COORD_MINY,
            CUBE_SCLAE, CUBE_SCLAE,-CUBE_SCLAE,
            0.0f, 0.0f,-1.0f,
            TEX_COORD_MAXX, TEX_COORD_MAXY,
            -CUBE_SCLAE, CUBE_SCLAE,-CUBE_SCLAE,
            0.0f, 0.0f,-1.0f,
            TEX_COORD_MINX, TEX_COORD_MAXY,

            // Top Face
            -CUBE_SCLAE, CUBE_SCLAE, CUBE_SCLAE,
            0.0f, 1.0f, 0.0f,
            TEX_COORD_MINX, TEX_COORD_MINY,
            CUBE_SCLAE, CUBE_SCLAE, CUBE_SCLAE,
            0.0f, 1.0f, 0.0f,
            TEX_COORD_MAXX, TEX_COORD_MINY,
            CUBE_SCLAE, CUBE_SCLAE,-CUBE_SCLAE,
            0.0f, 1.0f, 0.0f,
            TEX_COORD_MAXX, TEX_COORD_MAXY,
            -CUBE_SCLAE, CUBE_SCLAE,-CUBE_SCLAE,
            0.0f, 1.0f, 0.0f,
            TEX_COORD_MINX, TEX_COORD_MAXY,

            // Bottom Face
            -CUBE_SCLAE,-CUBE_SCLAE, CUBE_SCLAE,
            0.0f,-1.0f, 0.0f,
            TEX_COORD_MINX, TEX_COORD_MINY,
            CUBE_SCLAE,-CUBE_SCLAE, CUBE_SCLAE,
            0.0f,-1.0f, 0.0f,
            TEX_COORD_MAXX, TEX_COORD_MINY,
            CUBE_SCLAE,-CUBE_SCLAE,-CUBE_SCLAE,
            0.0f,-1.0f, 0.0f,
            TEX_COORD_MAXX, TEX_COORD_MAXY,
            -CUBE_SCLAE,-CUBE_SCLAE,-CUBE_SCLAE,
            0.0f,-1.0f, 0.0f,
            TEX_COORD_MINX, TEX_COORD_MAXY,

            // Left Face
            -CUBE_SCLAE,-CUBE_SCLAE,-CUBE_SCLAE,
            -1.0f, 0.0f, 0.0f,
            TEX_COORD_MINX, TEX_COORD_MINY,
            -CUBE_SCLAE,-CUBE_SCLAE, CUBE_SCLAE,
            -1.0f, 0.0f, 0.0f,
            TEX_COORD_MAXX, TEX_COORD_MINY,
            -CUBE_SCLAE, CUBE_SCLAE, CUBE_SCLAE,
            -1.0f, 0.0f, 0.0f,
            TEX_COORD_MAXX, TEX_COORD_MAXY,
            -CUBE_SCLAE, CUBE_SCLAE,-CUBE_SCLAE,
            -1.0f, 0.0f, 0.0f,
            TEX_COORD_MINX, TEX_COORD_MAXY,

            // Right Face
            CUBE_SCLAE,-CUBE_SCLAE,-CUBE_SCLAE,
            1.0f, 0.0f, 0.0f,
            TEX_COORD_MINX, TEX_COORD_MINY,
            CUBE_SCLAE,-CUBE_SCLAE, CUBE_SCLAE,
            1.0f, 0.0f, 0.0f,
            TEX_COORD_MAXX, TEX_COORD_MINY,
            CUBE_SCLAE, CUBE_SCLAE, CUBE_SCLAE,
            1.0f, 0.0f, 0.0f,
            TEX_COORD_MAXX, TEX_COORD_MAXY,
            CUBE_SCLAE, CUBE_SCLAE,-CUBE_SCLAE,
            1.0f, 0.0f, 0.0f,
            TEX_COORD_MINX, TEX_COORD_MAXY
    };

    static final short indicesCube[] = {0,1,3,3,1,2,
            4,7,5,7,6,5,
            8,9,11,11,9,10,
            12,15,13,15,14,13,
            16,17,19,19,17,18,
            20,23,21,23,22,21};
}
