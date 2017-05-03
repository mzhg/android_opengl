package jet.learning.opengl.gui_vr;

import android.opengl.GLES11;
import android.opengl.GLES20;

import com.google.vr.sdk.base.Eye;
import com.nvidia.developer.opengl.utils.GLES;
import com.nvidia.developer.opengl.utils.GLUtil;

import org.lwjgl.util.vector.Matrix4f;

import jet.learning.opengl.common.SimpleOpenGLProgram;

/**
 * Created by mazhen'gui on 2017/4/15.
 */

final class UIRenderer implements OnTouchEventListener{

    private SpatialNormalProgram m_SpatialNormalProgram;
    private int m_RectVBO;
    private SpatialUI[] m_UIs;
    private UIDesc[] m_UIDescs;
    private SpatialLayout m_Layout;
    private Background m_Background;

    private Matrix4f m_Proj = new Matrix4f();
    private Matrix4f m_MVP  = new Matrix4f();
    private GUIDemo m_Context;

    private RaycastSelector m_RaycastSelector;
    private SpatialUI m_WatchedUI;

    public UIRenderer(GUIDemo context){
        m_Context = context;

        m_RaycastSelector = new RaycastSelector();
        m_RaycastSelector.setTouchEventListener(this);
    }

    public void setUIDescs(UIDesc[] descs){
        m_UIDescs = descs;
    }

    public void setLayout(SpatialLayout layout){m_Layout = layout;}

    public void initlize(){
        m_SpatialNormalProgram = new SpatialNormalProgram();

        m_RectVBO = GLES.glGenBuffers();
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, m_RectVBO);
        // only the vertex data
        float[] rectVBOData = {
                  -1, -1, 0, 1,
                  +1, -1, 1, 1,
                  -1, +1, 0, 0,
                  +1, +1, 1, 0,
        };
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, rectVBOData.length * 4, GLUtil.wrap(rectVBOData), GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        // create views
        m_UIs = new SpatialUI[m_UIDescs.length];
        int i = 0;
        final int COLS = 3;
        Transform transform = new Transform();

        for(UIDesc desc : m_UIDescs){
            m_UIs[i] = new SpatialUI(desc.textureName, this);
            int x = i % COLS;
            int y = i / COLS;

            m_Layout.getChildTransform(x, y, 0, transform);

            m_UIs[i].setTransform(transform);
            m_RaycastSelector.add(m_UIs[i].getBoundingBox());
            i++;
        }

        m_Background = new Background(this);
    }

    public void update(float dt){
        for(SpatialUI ui : m_UIs){
            ui.update(dt);
        }

        m_RaycastSelector.update(m_Context.getHeadViewMatrix(), dt);
    }

    public SpatialNormalProgram getSpatialNormalProgram() {return m_SpatialNormalProgram;}
    public void bindQuadVBO(SimpleOpenGLProgram program){
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, m_RectVBO);
        GLES20.glEnableVertexAttribArray(program.getAttribPosition());
        GLES20.glVertexAttribPointer(program.getAttribPosition(), 2, GLES20.GL_FLOAT, false, 16, 0);
        GLES20.glEnableVertexAttribArray(program.getAttribTexCoord());
        GLES20.glVertexAttribPointer(program.getAttribTexCoord(), 2, GLES20.GL_FLOAT, false, 16, 8);
    }

    public void unbindQuadVBO(SimpleOpenGLProgram program){
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glDisableVertexAttribArray(program.getAttribPosition());
        GLES20.glDisableVertexAttribArray(program.getAttribTexCoord());
    }

    public void draw(Eye eye){
        // update the matrix first
        Matrix4f viewMat = m_Context.getViewMatrix(eye);
        Matrix4f.mul(m_Proj, viewMat, m_MVP);

        GLES20.glClearColor(0,0,0,0);
        GLES20.glClearDepthf(1.0f);
        GLES20.glClear(GLES11.GL_COLOR_BUFFER_BIT|GLES11.GL_DEPTH_BUFFER_BIT);
        GLES20.glEnable(GLES11.GL_DEPTH_FUNC);

        // draw the background first
        m_Background.draw();

        // draw the views
        for(SpatialUI ui: m_UIs){
            ui.draw();
        }
    }

    public void onResize(int width, int height){
        Matrix4f.perspective(60, (float)width/height, 0.1f, 100.0f, m_Proj);
    }

    public Matrix4f getMVP() {return m_MVP;}

    @Override
    public void onEnter(BoundingBox box) {
        SpatialUI ui = (SpatialUI)box.getUserData();
        ui.setState(UIState.WATCHED);
    }

    @Override
    public void onLeval(BoundingBox box) {
//        Log.i("UIRenderer", "onLeval");

        SpatialUI ui = (SpatialUI)box.getUserData();
        ui.setState(UIState.NORMAL);
    }

    @Override
    public void onWatching(BoundingBox box, float elpsedTime) {
//        Log.i("UIRenderer", "onWatching: time = " + elpsedTime);

        m_WatchedUI = (SpatialUI)box.getUserData();
    }
}
