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
package jet.learning.opengl.fight404;

import org.lwjgl.util.vector.Readable;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import java.nio.ByteBuffer;

/**
 * Created by mazhen'gui on 2017/12/21.
 */

final class BlockData implements Readable{
    static final int SIZE = 16 * 4 + Vector4f.SIZE * 4;

    final float[] seeds = new float[16];
    final Vector3f emitPosition = new Vector3f();
    float timeAmout;

    final Vector3f gravity = new Vector3f();
    float floor_level;

    final Vector3f eye_loc = new Vector3f();
    final Vector3f lookat = new Vector3f();

    boolean allow_perlin = true;
    boolean allow_gravity = true;
    boolean allow_floor = true;
    boolean allow_nebula = true;

    @Override
    public BlockData store(ByteBuffer buf) {
        for(int i = 0; i < 16; i++){
            buf.putFloat(seeds[i]);
        }

        emitPosition.store(buf);
        buf.putFloat(timeAmout);
        gravity.store(buf);
        buf.putFloat(floor_level);
        lookat.store(buf);
        buf.putInt(0);

        buf.putInt(allow_perlin ? 1:0);
        buf.putInt(allow_gravity ? 1:0);
        buf.putInt(allow_floor ? 1:0);
        buf.putInt(allow_nebula ? 1:0);
        return this;
    }
}
