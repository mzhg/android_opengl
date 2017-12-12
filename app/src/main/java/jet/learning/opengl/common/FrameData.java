package jet.learning.opengl.common;


import org.lwjgl.util.vector.Matrix4f;

import java.nio.ByteBuffer;

/**
 * Created by mazhen'gui on 2017/12/12.
 */

public class FrameData {
    public static final int MAX_INSTANCE_COUNT = 10;
    public static final int SIZE = Matrix4f.SIZE * (MAX_INSTANCE_COUNT * 2 + 1);

    public final Matrix4f viewProj = new Matrix4f();
    public final Matrix4f[] models = new Matrix4f[MAX_INSTANCE_COUNT];
    public final Matrix4f[] normalMats = new Matrix4f[MAX_INSTANCE_COUNT];

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

        return buffer;
    }
}
