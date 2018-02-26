/*
 * Copyright 2017 mzhg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jet.learning.opengl.common;


import org.lwjgl.util.vector.Matrix4f;

import java.nio.ByteBuffer;

/**
 * Created by mazhen'gui on 2017/12/12.
 */

public class FrameData {
    public static final int MAX_INSTANCE_COUNT = 10;
    public static final int SIZE = Matrix4f.SIZE * (MAX_INSTANCE_COUNT * 2 + 3);

    public final Matrix4f viewProj = new Matrix4f();
    public final Matrix4f[] models = new Matrix4f[MAX_INSTANCE_COUNT];
    public final Matrix4f[] normalMats = new Matrix4f[MAX_INSTANCE_COUNT];
    public final Matrix4f texMat = new Matrix4f();
    public final Matrix4f lightViewProj = new Matrix4f();

    private int instanceCount;

    public FrameData(int instanceCount){
        setInstanceCount(instanceCount);
    }

    public int getInstanceCount() { return instanceCount;}
    public void setInstanceCount(int count){
        if(count < 0 || count > MAX_INSTANCE_COUNT){
            throw new IllegalArgumentException("Invalid instanceCount: " + count);
        }

        int oldValue = instanceCount;
        instanceCount = count;
        for(int i = oldValue; i < instanceCount; i++){
            if(models[i] == null){
                models[i] = new Matrix4f();
                normalMats[i] = new Matrix4f();
            }
        }
    }

    public ByteBuffer store(ByteBuffer buffer){
        viewProj.store(buffer);

        int old_pos = buffer.position();
        for(int i = 0; i < instanceCount; i++){
            models[i].store(buffer);
        }

        old_pos += MAX_INSTANCE_COUNT * Matrix4f.SIZE;
        buffer.position(old_pos);

        for(int i = 0; i < instanceCount; i++){
            normalMats[i].store(buffer);
        }

        old_pos += MAX_INSTANCE_COUNT * Matrix4f.SIZE;
        buffer.position(old_pos);

        texMat.store(buffer);
        lightViewProj.store(buffer);
        return buffer;
    }
}
