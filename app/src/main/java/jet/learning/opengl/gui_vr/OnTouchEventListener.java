package jet.learning.opengl.gui_vr;

/**
 * Created by mazhen'gui on 2017/4/15.
 */

public interface OnTouchEventListener {
    void onEnter(BoundingBox box);
    void onLeval(BoundingBox box);
    void onWatching(BoundingBox box, float elpsedTime);
}
