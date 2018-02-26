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

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Readable;
import org.lwjgl.util.vector.Vector4f;

import java.nio.ByteBuffer;

/**
 * Created by mazhen'gui on 2017/12/22.
 */
final class RenderFrame implements Readable{
    static final int SIZE = Matrix4f.SIZE * 2 + Vector4f.SIZE;

    final Matrix4f projection = new Matrix4f();
    final Matrix4f view = new Matrix4f();
    float render_particle;
    float pointSize;

    @Override
    public RenderFrame store(ByteBuffer buf) {
        projection.store(buf);
        view.store(buf);
        buf.putFloat(render_particle);
        buf.putFloat(pointSize);
        buf.position(SIZE);
        return this;
    }

    public ByteBuffer storePortion(ByteBuffer buf){
        buf.putFloat(render_particle);
        buf.putFloat(pointSize);
        return buf;
    }
}
