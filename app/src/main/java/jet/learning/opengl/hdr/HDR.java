package jet.learning.opengl.hdr;

import com.nvidia.developer.opengl.app.NvSampleApp;

/**
 * Created by mazhen'gui on 2017/3/13.
 */

public final class HDR extends NvSampleApp {
    private static final int POS_BIND = 0;
    private static final int NOR_BIND = 1;
    private static final int TEX_BIND = 2;

    private HDRScene mScene;

    @Override
    protected void initBeforeGL() {
        mScene = new HDRScene(this);
    }

    @Override
    protected void initRendering() {
        mScene.onCreate();;
    }

    @Override
    protected void onResume() {
        super.onResume();

        mScene.startHeadTracking();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mScene.stopHeadTracking();
    }

    @Override
    public void initUI() {
        mScene.initUI();
    }

    @Override
    protected void draw() {
        mScene.onDraw();
    }

    @Override
    protected void reshape(int width, int height) {
        mScene.onResize(width, height);
    }

}
