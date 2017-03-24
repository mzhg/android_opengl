package jet.learning.android.examples;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.WindowManager;

import com.nvidia.developer.opengl.utils.NvUtils;

public class GuideView2 extends RenderView{

	private static final float FRAME_START_Y = 0.3F;
	private static final float FRAME_END_Y = 0.55F;
	
	private static final float CONTENT_START_Y = 0.4F;
	private static final float CONTENT_END_Y = 0.55F;
	AnimaImage img_frame;
	AnimaImage img_content;
	
	long mStartTime;
	float mAnimationTime = 0.7f;
	
	float mFrameStartX;
	float mContentStartX;
	
	// Global variables
	final Rect text_bound = new Rect();
	final Paint paint = new Paint();
	
	float text1Size = 60;
	float text2Size = 40;
	float titlePositionY = 50;
	
	public GuideView2(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		init(context);
	}

	public GuideView2(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		init(context);
	}

	public GuideView2(Context context) {
		super(context);
		
		init(context);
	}
	
	void init(Context content){
		WindowManager windowManager = (WindowManager) content.getSystemService(Context.WINDOW_SERVICE);
		@SuppressWarnings("deprecation")
		float screenWidth = windowManager.getDefaultDisplay().getWidth();
		
		Bitmap frame = GuideResource.view2_frame;
		mFrameStartX = -frame.getWidth()/ screenWidth * 0.5f;
		img_frame = new AnimaImage(frame, mFrameStartX, FRAME_START_Y);
		
		Bitmap conte = GuideResource.view2_content;
		mContentStartX = conte.getWidth()/screenWidth * 0.5f + 1.0f;
		img_content = new AnimaImage(conte, mContentStartX, CONTENT_START_Y);
	}

	@Override
	protected void onRender(Canvas canvas) {
		final float elpsedTime = (SystemClock.elapsedRealtime() - mStartTime)/1000f;
		final int viewWidth = getMeasuredWidth();
	    final int viewHeight = getMeasuredHeight();
	    
	    GuideResource.drawBackground(canvas, 1, viewWidth, viewHeight);
	    
	    if(!isInRenderState())return;
	    
	    String text1 = "我的政府";
	    String text2 = "政府服务，尽在家中";
	    GuideResource.drawTitle(canvas, text1, text2, viewWidth);
	    
	    float fx,fy;
	    float cx, cy;
	    if(elpsedTime < mAnimationTime){
			float interp = elpsedTime/mAnimationTime;
			fx = NvUtils.lerp(mFrameStartX, 0.5f, interp);
			fy = NvUtils.lerp(FRAME_START_Y, FRAME_END_Y, interp);
			cx = NvUtils.lerp(mContentStartX, 0.5f, interp);
			cy = NvUtils.lerp(CONTENT_START_Y, CONTENT_END_Y, interp);
	    }else{
	    	fx = 0.5f;
	    	fy = FRAME_END_Y;
	    	cx = 0.5f;
	    	cy = CONTENT_END_Y;
	    }
	    
	    img_content.setPosition(cx, cy);
		img_content.draw(canvas, elpsedTime, viewWidth, viewHeight);
		
		img_frame.setPosition(fx, fy);
		img_frame.draw(canvas, elpsedTime, viewWidth, viewHeight);
		
		if(elpsedTime > mAnimationTime){
			stopRender();
		}
	}

	@Override
	public void startRender() {
		super.startRender();
		
		mStartTime = SystemClock.elapsedRealtime();
	}
	
	/** 设置主标题字体的大小 */
	public void setText1Size(float textSize){
		text1Size = textSize;
	}
	
	/** 设置副标题字体的带下 */
	public void setText2Size(float textSize){
		text2Size = textSize;
	}
	
	/** 设置标题的Y坐标位置 */
	public void setTitlePosition(float y){
		titlePositionY = y;
	}
	
}
