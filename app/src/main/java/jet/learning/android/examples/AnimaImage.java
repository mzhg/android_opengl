package jet.learning.android.examples;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;

public class AnimaImage {
	
	static final Rect rect_src = new Rect();
	static final Rect rect_dst = new Rect();

	Bitmap image;
	final PointF center = new PointF();
	float startTime;
	float endTime;
	float ratio = 1.0f;  // real_width/bitmap_width.
	
	public AnimaImage(Bitmap image, float centerX, float centerY) {
		this.image = image;
		center.set(centerX, centerY);
	}
	
	public void setTime(float start, float end){
		startTime = start;
		endTime   = end;
	}
	
	public void setRatio(float ratio){this.ratio = ratio;}
	
	public void setPosition(float x, float y){ center.set(x, y); }
	
	public void draw(Canvas g, float currentTime, int viewWidth, int viewHeight){
		int left = (int) (viewWidth * center.x - image.getWidth() * 0.5f * ratio);
		int right = (int) (viewWidth * center.x + image.getWidth() * 0.5f * ratio);
		int top  = (int) (viewHeight * center.y - image.getHeight() * 0.5f * ratio);
		int bottom  = (int) (viewHeight * center.y + image.getHeight() * 0.5f * ratio);
		rect_dst.set(left, top, right, bottom);
		rect_src.set(0, 0, (int)(image.getWidth() * ratio), (int)(image.getHeight() * ratio));
		
		g.drawBitmap(image, rect_src,  rect_dst, null);
	}
	
	public boolean inTime(float time){
		return time >= startTime && time <= endTime;
	}
	
	public float getWidth() { return image.getWidth() * ratio;}
	public float getHeight(){ return image.getHeight() * ratio;}
}
