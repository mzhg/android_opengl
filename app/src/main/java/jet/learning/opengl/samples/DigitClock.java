package jet.learning.opengl.samples;

import android.opengl.GLES20;

import com.nvidia.developer.opengl.app.NvSampleApp;

import jet.learning.opengl.common.DigitClockRenderer;

/**
 * Created by mazhen'gui on 2017/12/18.
 */
public final class DigitClock extends NvSampleApp {

    private DigitClockRenderer mRenderer;
    private float mScale = 1.0f;

    @Override
    public void initUI() {
        mTweakBar.addValue("Digit Scale", createControl("mScale"), 0.2f, 1.0f, 0.01f);
    }

    @Override
    protected void initRendering() {
        mRenderer = new DigitClockRenderer();
        mRenderer.initlizeGL();
    }

    @Override
    protected void draw() {
        GLES20.glClearColor(0,0,0,0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        int width = (int) (getWidth() * 4/5 * mScale);
        int height = (int) (getHeight() * 4/5 * mScale);

        int centerX = getWidth()/2;
        int centerY = getHeight()/2;

        int x,y;

        float ratioHW = (float)height/width;
        if(ratioHW >= DigitClockRenderer.RATIO_HW){
            height = (int) (width * ratioHW + 0.5f);
            x = centerX - width/2;
            y = centerY - height/2;
        }else{
            width = (int) (width * DigitClockRenderer.RATIO_HW + 0.5f);
            x = centerX - width/2;
            y = centerY - height/2;
        }

        mRenderer.draw(x,y, width, height);
    }

    @Override
    protected void reshape(int width, int height) {
        GLES20.glViewport(0,0,width, height);
        mRenderer.reshape(width, height);
    }
}
