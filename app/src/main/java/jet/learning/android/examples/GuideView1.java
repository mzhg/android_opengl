package jet.learning.android.examples;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.WindowManager;

import com.nvidia.developer.opengl.utils.NvUtils;

public class GuideView1 extends RenderView{
	
	static boolean FIRST_RENDER = false;
	
	private static final float PHONE_END_Y = 0.7f;
	private static final float ANIMA_START = 0.2F;
	
	private static final float period_count = 1f;
	private static final float WAVE_PERIOD = 1F;
	
	// Global variables
	final Paint paint = new Paint();
	
	float text1Size = 60;
	float text2Size = 40;
	float titlePositionY = 50;
	
	AnimaImage img_phone;
	AnimaImage img_red_ball;
	AnimaImage img_blue_ball;
	AnimaImage img_orange_ball;
	AnimaImage img_wave;
	
	long mStartTime;  // 开始时间
	
	float phone_start_y;
	
	public GuideView1(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		initView();
	}

	public GuideView1(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		initView();
	}

	public GuideView1(Context context) {
		super(context);
		
		initView();
	}
	
	void initView(){
		Context content =  getContext();
		WindowManager windowManager = (WindowManager) content.getSystemService(Context.WINDOW_SERVICE);
		@SuppressWarnings("deprecation")
		float screenHeight = windowManager.getDefaultDisplay().getHeight();
		Bitmap view_phone = GuideResource.view1_phone;
		phone_start_y = 1.0f + view_phone.getHeight()/screenHeight * 0.5f;
		img_phone = new AnimaImage(view_phone, 0.5f, phone_start_y);
		img_phone.setTime(ANIMA_START, 0.7f);
		img_red_ball = new BallImage(GuideResource.view1_red_ball, 0.24f, 0.4f);
		img_red_ball.setTime(1f, 2.0f);
		
		img_blue_ball = new BallImage(GuideResource.view1_blue_ball, 0.5f, 0.33f);
		img_blue_ball.setTime(1.3f, 2.3f);
		
		img_orange_ball = new BallImage(GuideResource.view1_orange_ball, 0.75f, 0.39f);
		img_orange_ball.setTime(1.6f, 2.6f);
		
		img_wave = new WaveImage();
		img_wave.setTime(0.8f, 0);
		
		paint.setAntiAlias(true);
	}

	@Override
	protected void onRender(Canvas canvas) {
	    final float elpsedTime = (SystemClock.elapsedRealtime() - mStartTime)/1000f;
	    final int viewWidth = getMeasuredWidth();
	    final int viewHeight = getMeasuredHeight();
	    
	    if(!FIRST_RENDER && !isInRenderState()){
	    	startRender(100);
	    	FIRST_RENDER = true;
	    	return;
	    }
	    
	    GuideResource.drawBackground(canvas, 0, viewWidth, viewHeight);
	    
	    if(!isInRenderState()) return;
	    
	    String text1 = "我的声音";
	    String text2 = "随时随地，有感而发";
	    GuideResource.drawTitle(canvas, text1, text2, viewWidth);
	    
	    if(elpsedTime < ANIMA_START){
			return;
		}
		
		// draw the phone;
	    float x, y;
	    boolean drawBall = true;
	    if(elpsedTime < img_phone.endTime){
//			float lerp = NvUtils.lerp(img_phone.startTime, img_phone.endTime, elpsedTime - img_phone.startTime);
			float lerp = (elpsedTime - img_phone.startTime)/(img_phone.endTime - img_phone.startTime);
			x = 0.5f;
			y = NvUtils.lerp(phone_start_y, PHONE_END_Y, lerp);
			drawBall = false;
	    }else{
	    	x = 0.5f;
	    	y = PHONE_END_Y;
	    }
	    
	    img_phone.setPosition(x, y);
		img_phone.draw(canvas, elpsedTime, viewWidth, viewHeight);
		
		if(!drawBall)
			return;
		
		// draw the balls
		img_red_ball.draw(canvas, elpsedTime, viewWidth, viewHeight);
		img_blue_ball.draw(canvas, elpsedTime, viewWidth, viewHeight);
		img_orange_ball.draw(canvas, elpsedTime, viewWidth, viewHeight);
		
		if(!isInRenderState())
			return;
		
		// draw the wave
		img_wave.draw(canvas, elpsedTime, viewWidth, viewHeight);
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
	
	@Override
	public void startRender() {
		super.startRender();
		
		mStartTime = SystemClock.elapsedRealtime();
	}
	
	@Override
	public void startRender(long millon) {
		super.startRender(millon);
		
		mStartTime = SystemClock.elapsedRealtime() + millon;
	}
	
	private final class BallImage extends AnimaImage{

		
		public BallImage(Bitmap image, float centerX, float centerY) {
			super(image, centerX, centerY);
		}
		
		@Override
		public void draw(Canvas g, float currentTime, int viewWidth, int viewHeight) {
			
			float imageScale;
			if(currentTime < startTime){
				return; // no drawing
			} else if(currentTime > endTime){
				imageScale = 1.0f;
			}else{
//				float period_time = (endTime - startTime)/period_count;
//				float elpsed_time = (currentTime - startTime) % period_time;
				
				float ratio = (currentTime - startTime)/(endTime - startTime);
				// y = 0.3 * sin(x) + 1.0
				imageScale = (float) (0.15 * Math.sin(ratio * Math.PI * 2 * period_count) + 1.0);
//				float interploate = 0.7f * NvUtils.lerp(0, period_time, elpsed_time);
//				imageScale = 1.0f - interploate;
			}
			
			float halfWidth = getWidth() * 0.5f * imageScale;
			float halfHeight = getHeight() * 0.5f * imageScale;
			
			rect_dst.left = (int) (viewWidth * center.x - halfWidth);
			rect_dst.top  = (int) (viewHeight * center.y - halfHeight);
			rect_dst.right = (int) (viewWidth * center.x + halfWidth);
			rect_dst.bottom = (int) (viewHeight * center.y + halfHeight);
			
			rect_src.left = 0;
			rect_src.top = 0;
			rect_src.right = (int) getWidth();
			rect_src.bottom = (int) getHeight();
			
			g.drawBitmap(image, rect_src, rect_dst, null);
		}
	}
	
	/** 波纹动画 */
	private final class WaveImage extends AnimaImage{
		public WaveImage() {
			super(GuideResource.view1_wave, 0.5f, 0.5f);
		}
		
		@Override
		public void draw(Canvas g, float currentTime, int viewWidth, int viewHeight) {
			float imageAlpha;
			
			if(currentTime < startTime)
				return;
			
//			if(!inTime(currentTime)){
//				imageAlpha = 1.0f;
//			}else{
				float elpsed_time = (currentTime - startTime) % WAVE_PERIOD;
				imageAlpha = 1.0f - AnimaRenderView.smoothStep(0, WAVE_PERIOD, elpsed_time);
//			}
			
			float halfWidth = image.getWidth() * 0.5f;
			float halfHeight = image.getHeight() * 0.5f;
			
			rect_dst.left = (int) (viewWidth * center.x - halfWidth);
			rect_dst.top  = (int) (viewHeight * center.y - halfHeight);
			rect_dst.right = (int) (viewWidth * center.x + halfWidth);
			rect_dst.bottom = (int) (viewHeight * center.y + halfHeight);
			
			rect_src.left = 0;
			rect_src.top = 0;
			rect_src.right = image.getWidth();
			rect_src.bottom = image.getHeight();
			
			paint.setAlpha((int)(255 * imageAlpha));
			g.drawBitmap(image, rect_src, rect_dst, paint);
			
			// reset the paint for other drawing
			paint.setAlpha(255);
		}
	}
}
