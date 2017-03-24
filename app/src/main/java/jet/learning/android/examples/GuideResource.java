package jet.learning.android.examples;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;

import com.example.oglsamples.R;

public final class GuideResource {
	
	// Title information.
	static final float TITLE_Y = 60;  // should dp
	/** 主标题颜色 */
	static final int TITLE1_COLOR = 0xFF1EB6B7;
	/** 副标题颜色 */
	static final int TITLE2_COLOR = 0xFF80999B;
	/** 主标题文字大小 */
	static final int TITLE1_SIZE = 22;
	/** 标题之间的垂直距离 */
	static final float TITLE_PADDING = 10;
	/** 副标题文字的大小 */
	static final int TITLE2_SIZE = 16;
	
	static float density;
	
	static Bitmap view1_phone;
	static Bitmap view1_red_ball;
	static Bitmap view1_blue_ball;
	static Bitmap view1_orange_ball;
	static Bitmap view1_wave;
	
	static Bitmap view2_frame;
	static Bitmap view2_content;
	
	static Bitmap view3_shop;
	static Bitmap view3_card;
	
	static Bitmap view4_phone;
	static Bitmap view4_icon1;
	static Bitmap view4_icon2;
	static Bitmap view4_icon3;
	static Bitmap view4_icon4;
	static Bitmap view4_icon5;
	static Bitmap view4_icon6;
	
	static final Bitmap[] guide_bgs = new Bitmap[4];
	
	static Paint paint;
	static Rect text_bound;
	static Matrix matrix;
	
	// 绘制标题
	static void drawTitle(Canvas canvas,String text1, String text2, int viewWidth){
		float titleY = dip2px(TITLE_Y);
	    float titleX;
	    
	    // draw text1
	    paint.setTextSize(dip2px(TITLE1_SIZE));
	    paint.setColor(TITLE1_COLOR);
	    paint.getTextBounds(text1, 0, text1.length(), text_bound);
	    titleY += text_bound.height();
	    titleX = (viewWidth - text_bound.width())/2;
	    canvas.drawText(text1, titleX, titleY, paint);
	    
	    // draw text2
	    paint.setTextSize(dip2px(TITLE2_SIZE));
	    paint.setColor(TITLE2_COLOR);
	    paint.getTextBounds(text2, 0, text2.length(), text_bound);
	    titleY += (text_bound.height() + dip2px(TITLE_PADDING)); // 10 padding
	    titleX = (viewWidth - text_bound.width())/2;
	    canvas.drawText(text2, titleX, titleY, paint);
	}
	
	// 绘制GuideView的背景
	static void drawBackground(Canvas canvas, int index, int viewWidth, int viewHeight){
		float scale;
		Bitmap bitmap = guide_bgs[index];
//		float screenRatio = (float)viewHeight/viewWidth;
//		float bitmapRatio = (float)bitmap.getHeight()/bitmap.getWidth();
		
		matrix.reset();
		matrix.setTranslate(-bitmap.getWidth()/2, -bitmap.getHeight()/2);
		
//		if(screenRatio > bitmapRatio){// scale the image along the x-axis.
			scale = (float)viewWidth/bitmap.getWidth(); 
//		}else{ // scale the image along the y-axis.
//			scale = (float)viewHeight/bitmap.getHeight();
//		}
		
		matrix.postScale(scale, scale);
		matrix.postTranslate(viewWidth/2, viewHeight/2);
		canvas.drawBitmap(bitmap, matrix, null);
	}
	
	public static void load(Activity content){
		view1_phone = loadBitmap(content, R.drawable.view1_phone);
		view1_red_ball = loadBitmap(content, R.drawable.view1_red_ball);
		view1_blue_ball = loadBitmap(content, R.drawable.view1_blue_ball);
		view1_orange_ball = loadBitmap(content, R.drawable.view1_orange_ball);
		view1_wave = loadBitmap(content, R.drawable.view1_wave);
		
		view2_frame = loadBitmap(content, R.drawable.view2_frame);
		view2_content = loadBitmap(content, R.drawable.view2_content);
		
		view3_shop = loadBitmap(content, R.drawable.view3_shop);
		view3_card = loadBitmap(content, R.drawable.view3_billboard);
		
		view4_phone = loadBitmap(content, R.drawable.view4_phone);
		view4_icon1 = loadBitmap(content, R.drawable.view4_1);
		view4_icon2 = loadBitmap(content, R.drawable.view4_2);
		view4_icon3 = loadBitmap(content, R.drawable.view4_3);
		view4_icon4 = loadBitmap(content, R.drawable.view4_4);
		view4_icon5 = loadBitmap(content, R.drawable.view4_5);
		view4_icon6 = loadBitmap(content, R.drawable.view4_6);
		
		int[] ids = {R.drawable.guide_background1, R.drawable.guide_background2,
					 R.drawable.guide_background3, R.drawable.guide_background4};
		
		for(int i = 0; i < 4; i++){
			guide_bgs[i] = loadBitmap(content, ids[i]);
		}
		
		paint = new Paint();
		paint.setAntiAlias(true);
		text_bound = new Rect();
		matrix = new Matrix();
		
		density = content.getResources().getDisplayMetrics().density;
		GuideView1.FIRST_RENDER = false;
	}
	
	static float dip2px(float dip){
		return (int) (dip * density + 0.5f);
	}
	
	public static void release(){
		paint = null;
		text_bound = null;
		matrix = null;
		
		view1_phone.recycle();
		view1_red_ball.recycle();
		view1_blue_ball.recycle();
		view1_orange_ball.recycle();
		view1_wave.recycle();
		
		view2_frame.recycle();
		view2_content.recycle();
		
		view3_shop.recycle();
		view3_card.recycle();
		
		view4_phone.recycle();
		view4_icon1.recycle();
		view4_icon2.recycle();
		view4_icon3.recycle();
		view4_icon4.recycle();
		view4_icon5.recycle();
		view4_icon6.recycle();
	}
	
	private static Bitmap loadBitmap(Activity content, int resid){
		return BitmapFactory.decodeResource(content.getResources(), resid);
	}
}
