package jet.learning.opengl.gui_vr;

import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.HeadTransform;
import com.nvidia.developer.opengl.app.GvrSampleApp;
import com.nvidia.developer.opengl.utils.GLES;

import org.lwjgl.util.vector.Vector3f;

/**
 * Created by mazhen'gui on 2017/4/14.
 */

public class GUIDemo extends GvrSampleApp{

    private UIRenderer m_Renderer;
    @Override
    protected void initRendering() {
        UIDesc[] image_names = {
          new UIDesc("lol.jpg"), new UIDesc("longmao.jpg"), new UIDesc("nba.jpg"),
                new UIDesc("news.jpg"), new UIDesc("records.jpg"), new UIDesc("runningman.jpg")
        };

        m_Renderer = new UIRenderer(this);
        m_Renderer.setUIDescs(image_names);
        SpatialGridLayout layout = new SpatialGridLayout(new Vector3f(-4.0f, -1.5f, -10.f), new Vector3f(4.f, 1.5f, -10.f));
        layout.setChildrenCount(6);
        layout.setCols(3);
        layout.setRows(2);
        m_Renderer.setLayout(layout);
        m_Renderer.initlize();

//        m_transformer.setTranslation(0, 0, -7);
        GLES.checkGLError();
    }

    @Override
    public void onDrawEye(Eye eye) {
        m_Renderer.draw(eye);
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {
        super.onNewFrame(headTransform);

        m_Renderer.update(getFrameDeltaTime());
    }

    @Override
    public void onRendererShutdown() {

    }

    @Override
    protected void reshape(int width, int height) {
        m_Renderer.onResize(width, height);
    }
}
