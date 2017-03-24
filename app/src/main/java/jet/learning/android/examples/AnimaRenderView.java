package jet.learning.android.examples;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.AttributeSet;

import com.example.oglsamples.R;

public class AnimaRenderView extends RenderView{
	
	private static final int FORWARD = 0;
	private static final int TOP_WAIT = 1;
	private static final int DOWNWARD = 2;
	private static final int DOWN_WAIT = 3;
	

	int direction;
	Bitmap[] bitmaps;
	private float destY = 100;
	private float movingTime = 1;
	private float delayTime = 0.2f;
	private long startTime;
	
	private float animaDecay = 0.8f;
	private float imageStart = 0.25f;  //首个动画开始的时间
	private float imageTime = 0.3f;  // 动画在每个图片上的持续时间
	private float imageDelay = 0.02f;  // 两个图片之间的动画间隔
	private final Paint imagePaint = new Paint();
	
	private float t0 = 0f;  // 开始时间
	private float t1 = imageStart * 2 + 5 * (imageTime + imageDelay) + t0;  // 结束时间
	
	private String mText = "加载中...";
	private int mPadding = 10;
	
	private final Rect mImageRect = new Rect();
	private final Rect mTextRect  = new Rect();
	
	public AnimaRenderView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		init();
	}

	public AnimaRenderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		init();
	}

	public AnimaRenderView(Context context) {
		super(context);
		
		init();
	}
	
	void init(){
		bitmaps = new Bitmap[5];
		bitmaps[4] = BitmapFactory.decodeResource(getResources(), R.drawable.loading_logo1);
		bitmaps[3] = BitmapFactory.decodeResource(getResources(), R.drawable.loading_logo2);
		bitmaps[2] = BitmapFactory.decodeResource(getResources(), R.drawable.loading_logo3);
		bitmaps[1] = BitmapFactory.decodeResource(getResources(), R.drawable.loading_logo4);
		bitmaps[0] = BitmapFactory.decodeResource(getResources(), R.drawable.loading_logo5);
		
		direction = FORWARD;
		
		imagePaint.setAntiAlias(true);
		imagePaint.setColor(Color.GRAY);
		imagePaint.setTextSize(40);
	}
	
	public void setText(String text){
		mText = text;
	}
	
	public String getText() { return mText; }
	
	public void setTextSize(float textSize) { imagePaint.setTextSize(textSize);}
	public float getTextSize() { return imagePaint.getTextSize();}
	
	@Override
	protected void onRender(Canvas canvas) {
		final int viewWidth = getMeasuredWidth();
		final int viewHeight = getMeasuredHeight();
		
		final int standardX = (viewWidth - bitmaps[0].getWidth())/2;
		final int standardY = (viewHeight - bitmaps[0].getHeight())/2;
		
		mImageRect.left = standardX;
		mImageRect.top  = standardY;
		mImageRect.right = standardX + bitmaps[0].getWidth();
		mImageRect.bottom = standardY + bitmaps[0].getHeight();
		
		if(mText != null && mText.length() > 0){
			imagePaint.getTextBounds(mText, 0, mText.length(), mTextRect);
			mImageRect.offset(0, -(mTextRect.height() + mPadding)/2 );
			mTextRect.offset((viewWidth - mTextRect.width())/2, mImageRect.bottom + mPadding + mTextRect.height());
		}else{
			mTextRect.setEmpty();
		}
		
		if(isInRenderState()){
			final float elapsedTime = (SystemClock.elapsedRealtime() - startTime)/1000f * 0.7f;
			for(int i = 0; i < bitmaps.length; i++){
				float image_time = imageStart + i * (imageTime + imageDelay) + imageTime * 0.5f + t0;
				float theta = Math.abs(image_time - elapsedTime)/animaDecay;
				theta = smoothStep(0, 1, theta);
				if(theta >= 1){
					imagePaint.setAlpha(255);
				}else{
					theta *= (Math.PI * 0.5f);
					imagePaint.setAlpha((int)Math.max(255 * 0.2, (255 * Math.sin(theta))));
//					imagePaint.setAlpha((int)(255 * smoothStep(0, 1, (float)Math.sin(theta))));
				}
				
//				canvas.drawBitmap(bitmaps[i], standardX, standardY, imagePaint);
				canvas.drawBitmap(bitmaps[i], mImageRect.left, mImageRect.top, imagePaint);
			}
			
			if(elapsedTime >= (t1 -t0)){
				startTime = SystemClock.elapsedRealtime();
			}
		}
		
		if(mText != null && mText.length() > 0){
			imagePaint.setAlpha(255);
			canvas.drawText(mText, mTextRect.left, mTextRect.bottom, imagePaint);
		}
		
	}
	
	/**
	 * A smoothed step function. A cubic function is used to smooth the step between two thresholds.
	 * @param a the lower threshold position
	 * @param b the upper threshold position
	 * @param x the input parameter
	 * @return the output value
	 */
	public static float smoothStep(float a, float b, float x) {
		if (x < a)
			return 0;
		if (x >= b)
			return 1;
		x = (x - a) / (b - a);
		return x*x * (3 - 2*x);
	}

	protected void onRenderOld(Canvas canvas) {
		final int viewWidth = getMeasuredWidth();
		final int viewHeight = getMeasuredHeight();
		
		final int standardX = (viewWidth - bitmaps[0].getWidth())/2;
		final int standardY = (viewHeight - bitmaps[0].getHeight())/2;
		
		if(isInRenderState()){
			final float totalTime = (bitmaps.length -1) * delayTime + movingTime;
			final float speed = (standardY - destY)/movingTime;
			final float elapsedTime = (SystemClock.elapsedRealtime() - startTime)/1000f;
			if(direction == FORWARD){
				for(int i = 0; i < bitmaps.length; i++){
					float tokenTime = elapsedTime - i * delayTime;
					if(tokenTime > 0){
						float y = -speed * tokenTime + standardY;
						if(y < destY) y = destY;
						
						canvas.drawBitmap(bitmaps[i], standardX, y,null);
					}else{
						canvas.drawBitmap(bitmaps[i], standardX, standardY,null);
					}
				}
				
				if(elapsedTime > totalTime){
					direction = TOP_WAIT;
					startTime = SystemClock.elapsedRealtime();
				}
			}else if(direction == DOWNWARD){
				for(int i = 0; i < bitmaps.length; i++){
					float tokenTime = elapsedTime - (bitmaps.length - i - 1) * delayTime;
					if(tokenTime > 0){
						float y = speed * tokenTime + destY;
						if(y > standardY) y = standardY;
						
						canvas.drawBitmap(bitmaps[i], standardX, y,null);
					}else{
						canvas.drawBitmap(bitmaps[i], standardX, destY,null);
					}
				}
				
				if(elapsedTime > totalTime){
					direction = DOWN_WAIT;
					startTime = SystemClock.elapsedRealtime();
				}
			}else if(direction == TOP_WAIT){
				for(int i = 0; i < bitmaps.length; i++){
					canvas.drawBitmap(bitmaps[i], standardX, destY, null);
				}
				
				if(elapsedTime > 0.5f){
					direction = DOWNWARD;
					startTime = SystemClock.elapsedRealtime();
				}
			}else if(direction == DOWN_WAIT){
				for(int i = 0; i < bitmaps.length; i++){
					canvas.drawBitmap(bitmaps[i], standardX, standardY, null);
				}
				
				if(elapsedTime > 0.5f){
					direction = FORWARD;
					startTime = SystemClock.elapsedRealtime();
				}
			}
		}else{
			for(int i = 0; i < bitmaps.length; i++){
				canvas.drawBitmap(bitmaps[i], standardX, standardY, null);
			}
		}
		
	}
	
	@Override
	public void startRender() {
		super.startRender();
		
		startTime = SystemClock.elapsedRealtime();
		direction = FORWARD;
	}

	public float getDestY() {
		return destY;
	}

	public void setDestY(float destY) {
		this.destY = destY;
	}

	public float getMovingTime() {
		return movingTime;
	}

	public void setMovingTime(float movingTime) {
		this.movingTime = movingTime;
	}

	public float getDelayTime() {
		return delayTime;
	}

	public void setDelayTime(float delayTime) {
		this.delayTime = delayTime;
	}
	
}
