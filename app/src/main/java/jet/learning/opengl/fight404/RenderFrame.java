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
