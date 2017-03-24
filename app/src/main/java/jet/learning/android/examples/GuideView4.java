package jet.learning.android.examples;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.nvidia.developer.opengl.utils.NvUtils;

public class GuideView4 extends RenderView{
	
	private static final float PHONE_END_Y = 0.55F;
	private static final float ICON_TIME = 0.7F;
	private static final float ICON_DELAY = 0.2F;

	AnimaImage img_phone;
	AnimaImage img_icon1;
	AnimaImage img_icon2;
	AnimaImage img_icon3;
	AnimaImage img_icon4;
	AnimaImage img_icon5;
	AnimaImage img_icon6;
	
	float mPhoneStartY;
	float mAnimationState1 = 0.5f;
	
	final AnimaImage[] img_icons = new AnimaImage[6];
	
	long mStartTime;
	
	final String button_text = "进入应用";
	
	// Matrix used for scale icon.
	final Matrix matrix = new Matrix();
	final RectF button_bound = new RectF(0.3f, 0.85f, 0.7f, 0.92f);
	final RectF button_rect = new RectF(0.35f, 0.7f, 0.65f, 0.8f);
	final Rect  text_bound = new Rect();
	boolean buttonTouched;
	OnButtonClickedListener listener;
	final Paint button_paint = new Paint();
	
	int button_background = Color.BLUE;
	int button_text_color = Color.BLACK;
	int button_text_size  = 20;  // dp
	
	public GuideView4(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		init(context);
	}

	public GuideView4(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		init(context);
	}

	public GuideView4(Context context) {
		super(context);
		
		init(context);
	}
	
	void init(Context context){
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		@SuppressWarnings("deprecation")
		float screenHeight = windowManager.getDefaultDisplay().getHeight();
		
		Bitmap phone = GuideResource.view4_phone;
		mPhoneStartY = 1.0f + phone.getHeight()/screenHeight * 0.5f;
		img_phone = new AnimaImage(phone, 0.33f, mPhoneStartY);
		img_phone.setRatio(1.1f);
		
		img_icon1 = new IconImage(GuideResource.view4_icon1, 0.31f, 0.53f);
		img_icon2 = new IconImage(GuideResource.view4_icon2, 0.27f, 0.6f);
		img_icon3 = new IconImage(GuideResource.view4_icon3, 0.31f, 0.68f);
		img_icon4 = new IconImage(GuideResource.view4_icon4, 0.5f, 0.74f);
		img_icon5 = new IconImage(GuideResource.view4_icon5, 0.73f, 0.68f);
		img_icon6 = new IconImage(GuideResource.view4_icon6, 0.82f, 0.53f);
		
		img_icons[0] = img_icon1;
		img_icons[1] = img_icon2;
		img_icons[2] = img_icon3;
		img_icons[3] = img_icon4;
		img_icons[4] = img_icon5;
		img_icons[5] = img_icon6;
		
		for(int i = 0; i < 6; i++){
			float startTime = mAnimationState1 + i * ICON_DELAY;
			float endTime = startTime + ICON_TIME;
			img_icons[i].setTime(startTime, endTime);
		}
		
		button_paint.setAntiAlias(true);
		button_paint.setTextSize(GuideResource.dip2px(button_text_size));
		button_paint.getTextBounds(button_text, 0, button_text.length(), text_bound);
	}
	
	public void setOnButtonClickedListener(OnButtonClickedListener listener){
		this.listener = listener;
	}

	@Override
	protected void onRender(Canvas canvas) {
		final float elpsedTime = (SystemClock.elapsedRealtime() - mStartTime)/1000f;
		final int viewWidth = getMeasuredWidth();
	    final int viewHeight = getMeasuredHeight();
	    
	    GuideResource.drawBackground(canvas, 3, viewWidth, viewHeight);
	    
	    if(!isInRenderState()) return;
	    
	    String text1 = "我的支付";
	    String text2 = "生活账单，一触即付";
	    GuideResource.drawTitle(canvas, text1, text2, viewWidth);
	    
	    if(elpsedTime < mAnimationState1){
	    	float ratio = elpsedTime/mAnimationState1;
	    	float phoneY = NvUtils.lerp(mPhoneStartY, PHONE_END_Y, ratio);
	    	img_phone.setPosition(0.45f, phoneY);
	    	img_phone.draw(canvas, 0, viewWidth, viewHeight);
	    }else{
	    	img_phone.setPosition(0.45f, PHONE_END_Y);
	    	img_phone.draw(canvas, 0, viewWidth, viewHeight);
	    	
	    	for(int i = 0; i < 6; i++){
	    		img_icons[i].draw(canvas, elpsedTime, viewWidth, viewHeight);
	    	}
	    }
	    
	    // 绘制按钮
	    // 1, 绘制背景色
	    button_rect.left = button_bound.left * viewWidth;
	    button_rect.right = button_bound.right * viewWidth;
	    button_rect.bottom = button_bound.bottom * viewHeight;
	    button_rect.top = button_bound.top * viewHeight;
	    button_paint.setColor(button_background);
	    canvas.drawRoundRect(button_rect, 20, 20, button_paint);
	    
	    // 2, 绘制文字
	    float text_center_x = button_bound.centerX() * viewWidth;
	    float text_center_y = button_bound.centerY() * viewHeight;
	    int text_x = (int) (text_center_x - text_bound.width()/2);
	    int text_y = (int) (text_center_y + text_bound.height()/2);
	    button_paint.setColor(button_text_color);
	    canvas.drawText(button_text, text_x, text_y, button_paint);
	    
	    if(elpsedTime > img_icon6.endTime){
	    	stopRender();  // stop animation
	    }
	}
	
	@Override
	public void startRender() {
		super.startRender();
		
		mStartTime = SystemClock.elapsedRealtime();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(event.getAction() == MotionEvent.ACTION_DOWN){
			float width = getMeasuredWidth();
			float height = getMeasuredHeight();
			
			float x = event.getX()/width;
			float y = event.getY()/height;
			
			if(button_bound.contains(x, y)){
				buttonTouched = true;
			}
		}else if(event.getAction() == MotionEvent.ACTION_UP){
			if(buttonTouched){
				buttonTouched = false;
				
				if(listener != null)
					listener.onClicked();
				return true;
			}
		}else if(event.getAction() == MotionEvent.ACTION_CANCEL){
			if(buttonTouched){
				buttonTouched = false;
				return true;
			}
		}
		
		return buttonTouched || super.onTouchEvent(event);
	}
	
	public interface OnButtonClickedListener{
		void onClicked();
	}

	private final class IconImage extends AnimaImage{

		public IconImage(Bitmap image, float centerX, float centerY) {
			super(image, centerX, centerY);
		}
		
		@Override
		public void draw(Canvas g, float currentTime, int viewWidth, int viewHeight) {
			if(inTime(currentTime)){
				float ratio = (currentTime - startTime)/(endTime - startTime);
				// y = 0.5 * sin(x) + 1.0
				float scale = (float) (0.15 * Math.sin(ratio * Math.PI * 2) + 1.0);
				matrix.reset();
				matrix.setTranslate(-image.getWidth()/2, -image.getHeight()/2); // center the image to origin.
				matrix.postScale(scale, scale);
				matrix.postTranslate(center.x * viewWidth, center.y * viewHeight);
				g.drawBitmap(image, matrix, null);
			}else if(currentTime > endTime){
				super.draw(g, currentTime, viewWidth, viewHeight);
			}
		}
	}
	
}
