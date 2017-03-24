package jet.learning.android.examples;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.WindowManager;

import com.nvidia.developer.opengl.utils.NvUtils;

public class GuideView3 extends RenderView{
	
	private static final float SHOP_END_Y = 0.58F;
	private static final float SHOP_X = 0.45F;
	private static final float ICON_ANGLE = 80;

	AnimaImage img_shop;
	AnimaImage img_icon;
	
	float mIconOffsetX;
	float mIconOffsetY;
	
	float mShopStartY;
	
	long mStartTime;
	
	float mAnimaState1 = 1.0f;
	float mAnimaState2 = mAnimaState1 + 1 + 3f/4;
	float mIconPerid = 1f;
	float mLastAngle;
	
	final Matrix mat = new Matrix();
	
	public GuideView3(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		init(context);
	}

	public GuideView3(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		init(context);
	}

	public GuideView3(Context context) {
		super(context);
		
		init(context);
	}
	
	@SuppressWarnings("deprecation")
	void init(Context context){
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		
		float screenHeight = windowManager.getDefaultDisplay().getHeight();
		float screenWidth  = windowManager.getDefaultDisplay().getWidth();
		Bitmap shop = GuideResource.view3_shop;
		float shopX = SHOP_X;
		mShopStartY = 1.0f + shop.getHeight()/screenHeight * 0.5f;
		img_shop = new AnimaImage(shop, shopX, mShopStartY);
		img_shop.setRatio(1.1f);
		
		mIconOffsetX = shop.getWidth()/screenWidth * 0.45f;
		mIconOffsetY = -0.07f;
		img_icon = new AnimaImage(GuideResource.view3_card, shopX + mIconOffsetX, mShopStartY + mIconOffsetY);
		img_icon.setTime(mAnimaState1, mAnimaState2);
	}

	@Override
	protected void onRender(Canvas canvas) {
	    final int viewWidth = getMeasuredWidth();
	    final int viewHeight = getMeasuredHeight();
	    
	    GuideResource.drawBackground(canvas, 2, viewWidth, viewHeight);
	    
	    if(!isInRenderState()) return;
	    
	    String text1 = "我的生活";
	    String text2 = "足不出户，供你所需";
	    GuideResource.drawTitle(canvas, text1, text2, viewWidth);
	    
	    if(isInRenderState()){
		    final float elpsedTime = (SystemClock.elapsedRealtime() - mStartTime)/1000f;
		    if(elpsedTime < mAnimaState1){
		    	float ratio = elpsedTime/mAnimaState1;
		    	float shopY = NvUtils.lerp(mShopStartY, SHOP_END_Y, ratio);
		    	float shopX = SHOP_X;
		    	
		    	img_shop.setPosition(shopX, shopY);
		    	img_shop.draw(canvas, 0, viewWidth, viewHeight);
		    	
//		    	img_icon.setPosition(shopX + mIconOffsetX, shopY + mIconOffsetY);
//		    	img_icon.draw(canvas, 0, viewWidth, viewHeight);
		    }else {
		    	float shopY = SHOP_END_Y;
		    	float shopX = SHOP_X;
		    	
		    	img_shop.setPosition(shopX, shopY);
		    	img_shop.draw(canvas, 0, viewWidth, viewHeight);
		    	
		    	img_icon.setPosition(shopX + mIconOffsetX, shopY + mIconOffsetY);
		    	float angle;
		    	if(elpsedTime < mAnimaState2){
		    		angle = - (float) (Math.cos((elpsedTime - mAnimaState1)/mIconPerid * 2.0 * Math.PI) * ICON_ANGLE);
		    		mLastAngle = angle;
		    	}else{
		    		angle = mLastAngle;
		    		stopRender();
		    	}
		    	
		    	mat.reset();
		    	mat.setTranslate(-img_icon.getWidth() * 0.5f, 0);
		    	mat.postRotate(angle);
		    	mat.postTranslate(viewWidth * (shopX + mIconOffsetX), viewHeight * (shopY + mIconOffsetY));
		    	canvas.drawBitmap(img_icon.image, mat, null);
		    }
	    }else{
//	    	float shopY = SHOP_END_Y;
//	    	float shopX = 0.5f;
//	    	
//	    	img_shop.setPosition(shopX, shopY);
//	    	img_shop.draw(canvas, 0, viewWidth, viewHeight);
//	    	
//	    	img_icon.setPosition(shopX + mIconOffsetX, shopY + mIconOffsetY);
//	    	
//	    	mat.reset();
//	    	mat.setTranslate(img_icon.getWidth() * 0.5f, 0);
//	    	mat.postRotate(mLastAngle);
//	    	mat.postTranslate(viewWidth * (shopX + mIconOffsetX), viewHeight * (shopY + mIconOffsetY));
//	    	canvas.drawBitmap(img_icon.image, mat, null);
	    }
	}

	@Override
	public void startRender() {
		super.startRender();
		
		mStartTime = SystemClock.elapsedRealtime();
	}
}
