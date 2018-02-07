package jet.learning.opengl.optimization;

import org.lwjgl.util.vector.Matrix4f;

/**
 * Created by mazhen'gui on 2018/2/6.
 */

final class TerrainInput {
    final Matrix4f transform = new Matrix4f();
    String heightmap;
    String colormap;
    int subdivsX;
    int subdivsY;
}
