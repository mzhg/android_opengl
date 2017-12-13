package jet.learning.opengl.common;

import com.nvidia.developer.opengl.utils.NvDisposeable;

/**
 * Created by mazhen'gui on 2017/11/13.
 */

public interface RenderMesh extends NvDisposeable{

    void initlize(MeshParams params);

    void draw();

    default void update(float dt){}

    public static class MeshParams{
        public int posAttribLoc;
        public int norAttribLoc;
        public int texAttribLoc;
        public int tanAttribLoc = -1;  // tangent attribution is diabled default.
    }
}
