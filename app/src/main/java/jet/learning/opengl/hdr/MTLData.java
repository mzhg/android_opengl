package jet.learning.opengl.hdr;

/**
 * Created by mazhen'gui on 2017/3/16.
 */

final class MTLData {
    int type;
    float r,g,b,a;

    MTLData(int type, float r, float g, float b, float a) {
        this.type = type;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }
}
