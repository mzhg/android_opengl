package jet.learning.android.examples;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class ItemPicker extends RenderView implements android.view.GestureDetector.OnGestureListener{

	private static final String TAG = "ItemPicker";
	
	private static final int NONE = 0;
	private static final int SCROLL = 1;
	private static final int FLIPPING = 2;
	
	/** 要显示的内容 */
	String[] items;
	/** 可见的项目数，该值一般为3、5、7等奇数 */
	int visiableItemCount = 3;

	int itemColor = Color.BLACK;
	final Paint textPaint = new Paint();
	final Rect textBound = new Rect();

	/** 两个Item之间的垂直间距 , 文字之间的垂直间距*/
	int verticalPadding = 5;
	/** 分割线的宽度， 默认是2像素. 一般不大于{@link #verticalPadding}. */
	int dividerWidth = 3;
	/** 分割线的颜色，默认是绿色 */
	int dividerColor = Color.GREEN;
	/** 是否显示分割线 */
	boolean showDivider = true;
	/** 选中项的颜色 */
	int focusedItemColor = Color.GREEN;
	
	/** 内部私有变量 */
	int itemHeight;
	int totalHeight;
	int cursor;
	int touchDownY;
	
	/** True表示已经触摸屏幕 */
	boolean inTouched;
	
	GestureDetector gesture;
	
	/** 当前处于选中状态的 Item*/
	int focusedItem = 1;
	
	// 动画状态，目前有三种状态：None, Scroller, Filp.
	int mAnimaState;
	int mDestCursor;
	float mScrollerTime = 0.3f;
	float mFlipTime = 0.4f;
	final Accelarator accelarator = new Accelarator();
	long mAnimaStartTime;
	final Fliping fliping = new Fliping();
	
	public ItemPicker(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		initItemPicker(context);
	}

	private void initItemPicker(Context context) {
		gesture = new GestureDetector(context, this);
	}

	public ItemPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		initItemPicker(context);
	}

	public ItemPicker(Context context) {
		super(context);
		initItemPicker(context);
	}
	
	public void setRangeValues(int min, int max){
		int count = Math.abs(max - min);
		int step = max > min ? 1 : -1;
		String[] values =new String[count];
		for(int i = 0; i < count; i++){
			values[i] = Integer.toString(min + i * step);
		}
		
		setDisplayValues(values);
	}
	
	public void setVisibleItemCount(int count){
		if(visiableItemCount !=count){
			visiableItemCount = count;
			focusedItem = count/2;
			
			requestLayout();
		}
	}

	public void setDisplayValues(String[] values) {
		items = values;
		
		requestLayout();
	}
	
	public void setTextColor(int color){
		itemColor = color;
	}
	
	public void setTextSize(float textSize){
		textPaint.setTextSize(textSize);
		
		requestLayout();
		invalidate();
	}

	@Override
	protected void onRender(Canvas canvas) {
		int viewWidth = getMeasuredWidth();
		int viewHeight = getMeasuredHeight();
		
		if(mAnimaState == SCROLL){
			float elsepTime = (SystemClock.elapsedRealtime() - mAnimaStartTime)/1000f;
			if(elsepTime > mScrollerTime){
				stopRender();
				mAnimaState = NONE;
				cursor = mDestCursor;
//				Log.i(TAG, "End = " + mDestCursor);
//				clampCursor(true);
			}else{
				cursor = (int) accelarator.valueOf(elsepTime);
			}
		}else if(mAnimaState == FLIPPING){
			float elsepTime = (SystemClock.elapsedRealtime() - mAnimaStartTime)/1000f;
			cursor = (int) fliping.valueOf(elsepTime);
			if(elsepTime > mFlipTime /*|| !inRange(fliping.start, mDestCursor, cursor)*/){
				stopRender();
//				Log.i(TAG, "onFling:dest = " + mDestCursor);
				cursor = mDestCursor;
//				clampCursor(true);
				mAnimaState = NONE;
			}/*else{
				cursor = (int) fliping.valueOf(elsepTime);
				Log.i(TAG, "cursor = " + cursor);
				if(cursor > 0){
					cursor = 0;
					stopRender();
					mAnimaState = NONE;
				}
				
				if(cursor < viewHeight - totalHeight){
					cursor = viewHeight - totalHeight;
					stopRender();
					mAnimaState = NONE;
				}
			}*/
		}
		
		if(items == null || items.length == 0)
			return;
		
		if(itemHeight == 0){
			Log.w("ItemPicker", "itemHeight is zero");
			itemHeight = viewHeight/visiableItemCount;
		}
		
		int cursor = clampCursor(this.cursor);
		// -------------- draw the item ------------------ //
		// 1, find the first visible item.
	    int firstVisiableItem = -1;
	    for(int i = 0; i < items.length; i++){
	    	int position = cursor + i * itemHeight;
			if(position + itemHeight <= 0)
				continue;
			firstVisiableItem = i;
			break;
	    }
	    
		// 2, Compute All visible items according firstVisibleItem in step 1.
//	    int[] visibleItems = new int[visiableItemCount];
//	    int remaingCount = Math.min(items.length - firstVisiableItem, visiableItemCount);
//	    for(int i = 0; i < remaingCount; i++){
//	    	visibleItems[i] = i + firstVisiableItem;
//	    }
//	    for(int i = remaingCount - visiableItemCount; i < 0; i++){
//	    	visibleItems[remaingCount++] = i;
//	    }
	    int lastVisibleItem = firstVisiableItem + visiableItemCount + 1;
	    
	    
	    // 3, draw items.
	    textPaint.setColor(itemColor);
		int middleItemPosition = viewHeight/2;
		int currentPosition = cursor;
		int selectedItem = -1;
		int miniDistance = Integer.MAX_VALUE;
		int specialItem = -1;
		int specialItemPostion = 0;
		
		for(int i = firstVisiableItem; i < lastVisibleItem; i++){
			int position = currentPosition + i * itemHeight;
			int currentMiddlePosition = position + itemHeight/2;
			String item = items[i % items.length];
			int diff = Math.abs(currentMiddlePosition - middleItemPosition);
			
			// 计算alpha值
			float t = AnimaRenderView.smoothStep(0, middleItemPosition, diff);
			t = (float) Math.pow(t, 3);
			float alpha = 1.0f - t;
			textPaint.setAlpha((int)(255 * alpha));
			
			textPaint.getTextBounds(item, 0, item.length(), textBound);
			float x = (viewWidth - textBound.width())/2;
			float y = (itemHeight - textBound.height())/2 + textBound.height() + position;
			
			// Do not draw the focused item.
			if(i % items.length != focusedItem){
				canvas.drawText(item, x, y, textPaint);
			}else{
				// record the focused item information
				specialItem = i % items.length;
				specialItemPostion = position;
			}
			
			// Re-compute the selected item.
			if(diff < miniDistance){
				selectedItem = i % items.length;
				miniDistance = diff;
			}
		}
		
		// draw the focused item with focusedItemColor
		if(specialItem != -1){
			textPaint.setColor(focusedItemColor);
			textPaint.setAlpha(255);
			
			String item = items[specialItem];
			textPaint.getTextBounds(item, 0, item.length(), textBound);
			float x = (viewWidth - textBound.width())/2;
			float y = (itemHeight - textBound.height())/2 + textBound.height() + specialItemPostion;
			canvas.drawText(item, x, y, textPaint);
		}
		
	    
//		for(int i = 0; i < items.length; i++){
//			int position = currentPosition + i * itemHeight;
//			if((position < 0 && position + itemHeight <= 0) || position > viewHeight)
//				continue;
//			
//			int currentMiddlePosition = position + itemHeight/2;
//			String item = items[i];
//			int diff = Math.abs(currentMiddlePosition - middleItemPosition);
//			
//			// 计算alpha值
//			float t = AnimaRenderView.smoothStep(0, middleItemPosition, diff);
//			t = (float) Math.pow(t, 3);
//			float alpha = 1.0f - t;
//			textPaint.setAlpha((int)(255 * alpha));
//			
//			textPaint.getTextBounds(item, 0, item.length(), textBound);
//			float x = (viewWidth - textBound.width())/2;
//			float y = (itemHeight - textBound.height())/2 + textBound.height() + position;
//			canvas.drawText(item, x, y, textPaint);
//			
//			if(diff < miniDistance){
//				selectedItem = i;
//				miniDistance = diff;
//			}
//		}
		
		focusedItem = selectedItem;
		
		// --------------- Draw Items End ----------------- //
		
		// draw the divider
		if(!showDivider)
			return;
		
		textPaint.setColor(dividerColor);
		textPaint.setStyle(Style.FILL_AND_STROKE);
		textPaint.setStrokeWidth(dividerWidth);
		int uppermiddle = (viewHeight - itemHeight - dividerWidth)/2;
		canvas.drawLine(1, uppermiddle, viewWidth - 1, uppermiddle, textPaint);
		int bottommiddle = (viewHeight + itemHeight + dividerWidth)/2;
		canvas.drawLine(1, bottommiddle, viewWidth - 1, bottommiddle, textPaint);
	}
	
	public int getSelectedItemIndex() { return focusedItem;}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int w;
		int h;

		// Desired aspect ratio of the view's contents (not including padding)
//		float desiredAspect = 0.0f;

		// We are allowed to change the view's width
		boolean resizeWidth = false;

		// We are allowed to change the view's height
		boolean resizeHeight = false;

		final int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
		final int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);

		if (items == null || items.length == 0) {
			w = 0;
			h = 0;
		} else {
			Size size = measureTextSize();
			w = size.width;
			h = size.height;
			
			// We are supposed to adjust view bounds to match the aspect
            // ratio of our drawable. See if that is possible.
            resizeWidth = widthSpecMode != MeasureSpec.EXACTLY;
            resizeHeight = heightSpecMode != MeasureSpec.EXACTLY;
		}
		
		int widthSize;
		int heightSize;
		h = visiableItemCount * h + (visiableItemCount - 1) * verticalPadding;
		
		if(resizeWidth || resizeHeight){
			widthSize = resolveAdjustedSize(w, w, widthMeasureSpec);
			heightSize = resolveAdjustedSize(h, h, heightMeasureSpec);
		}else{
			w = Math.max(w, getSuggestedMinimumWidth());
            h = Math.max(h, getSuggestedMinimumHeight());

            widthSize = resolveSizeAndState(w, widthMeasureSpec, 0);
            heightSize = resolveSizeAndState(h, heightMeasureSpec, 0);
		}
		
		itemHeight = heightSize/visiableItemCount;
		totalHeight = itemHeight * items.length;
		setMeasuredDimension(widthSize, heightSize);
	}

	// Return the max size of the items.
	private Size measureTextSize() {
		int width = 0;
		int height = 0;

		for (String item : items) {
			textPaint.getTextBounds(item, 0, item.length(), textBound);
			width = Math.max(width, textBound.width());
			height = Math.max(height, textBound.height());
		}

		return new Size(width, height);
	}

	// Copied from ImageView
	private int resolveAdjustedSize(int desiredSize, int maxSize, int measureSpec) {
		int result = desiredSize;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);
		switch (specMode) {
		case MeasureSpec.UNSPECIFIED:
			/*
			 * Parent says we can be as big as we want. Just don't be larger
			 * than max size imposed on ourselves.
			 */
			result = Math.min(desiredSize, maxSize);
			break;
		case MeasureSpec.AT_MOST:
			// Parent says we can be as big as we want, up to specSize.
			// Don't be larger than specSize, and don't be larger than
			// the max size imposed on ourselves.
			result = Math.min(Math.min(desiredSize, specSize), maxSize);
			break;
		case MeasureSpec.EXACTLY:
			// No choice. Do what we are told.
			result = specSize;
			break;
		}
		return result;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		gesture.onTouchEvent(event);
		
		if(event.getAction() == MotionEvent.ACTION_CANCEL ||
			event.getAction() == MotionEvent.ACTION_UP){
			onUp(event);
		}
		
		return true;
	}
	
	private void onUp(MotionEvent event){
		if(mAnimaState == NONE){
			scrollback(event.getY() > touchDownY);
		}
	}
	
	@Override
	public boolean onDown(MotionEvent e) {
		touchDownY = (int) e.getY();
		Log.i(TAG, "onDown");
		if(mAnimaState != NONE){  // 取消动画
			stopRender();
			mAnimaState = NONE;
			
			return true;
		}
		
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		cursor -= distanceY;
		
//		int total = getMeasuredHeight() - totalHeight;
//		if(cursor > 0) cursor = 0;
//		if(cursor < total) cursor = total;
		
		invalidate();
		return true;
	}
	
	private int clampCursor(int cursor){
		if(cursor > 0){
			int n = cursor / totalHeight;
			while(cursor > 0){
				cursor -= n * totalHeight;
				n = 1;
			}
		}else{ // cursor < 0
			if(cursor <= -totalHeight){
				int n = -cursor / totalHeight;
				cursor += n * totalHeight;
			}
		}
		
		return cursor;
		
//		if(fix){
//			int mod = cursor % totalHeight;
//			if(mod != 0){
//				if(-mod < totalHeight /2){
//					cursor -= mod;
//				}else{
//					cursor -= mod;
//					cursor += totalHeight;
//				}
//				
////				clampCursor(false);
//			}
//		}
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	private static boolean inRange(float left, float right, float var){
		return (var - left) * (var - right) < 0;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		
		Log.i(TAG, "velocityY = " + velocityY);
	    if(mAnimaState == NONE){
	    	fliping.reset(cursor, velocityY, mFlipTime);
	    	float dest = fliping.valueOf(mFlipTime);
	    	
//	    	if(dest >= 0){
////	    		mDestCursor = 0;
////	    	}else if(dest < getMeasuredHeight() - totalHeight){
////	    		mDestCursor = getMeasuredHeight() - totalHeight;
//	    		
//	    		int mod = (int) (dest % itemHeight);
//	    		if(mod < itemHeight/2){
//	    			mDestCursor = (int) (dest - mod);
//	    		}else{
//	    			mDestCursor = (int) (dest - mod) + itemHeight;
//	    		}
//	    	}else{
//	    		int pos = (int) -dest;
//	    		int mod = pos % itemHeight;
//	    		if(dest < cursor)
//	    			mDestCursor = -(pos - mod);
//	    		else
//	    			mDestCursor = -(pos -mod + itemHeight);
//	    	}
	    	
//	    	Log.i(TAG, "onFling:(start, dest) = (" + cursor + ", " + dest + ")");
	    	mDestCursor = nearest((int)dest, itemHeight);
	    	fliping.fix(mDestCursor);
	    	float d = fliping.valueOf(mFlipTime);
	    	Log.i(TAG, "onFling:diff = " + (d - mDestCursor));
	    	
	    	mAnimaState = FLIPPING;
	    	startRender();
	    	mAnimaStartTime = SystemClock.elapsedRealtime();
	    }
		return false;
	}
	
	// 进入到滚动动画, 
	private void scrollback(boolean down){
		int position = Math.abs(cursor);
		int mod = position%itemHeight;
		if(mod == 0)
			return;
		
//		if(mod < itemHeight/2){
//			mDestCursor = position - mod;
//		}else{
//			mDestCursor = position - mod + itemHeight;
//		}
		
		
//		if(mDestCursor > totalHeight) mDestCursor = totalHeight;
		mDestCursor = nearest(cursor, itemHeight);
		accelarator.reset(cursor, mDestCursor, mScrollerTime);
		mAnimaState = SCROLL;
		startRender();
		
		mAnimaStartTime = SystemClock.elapsedRealtime();
	}
	
	private static int nearest(int var, int length){
		if(var > 0){
			int mod = var % length;
			if(mod < length /2){
				var -= mod;
			}else{
				var -= mod;
				var += length;
			}
		}else{
			int mod = var % length;
			if(-mod < length/2){
				var -= mod;
			}else{
				var -= mod;
				var -= length;
			}
		}
		
		return var;
	}

	private static final class Size {
		int width;
		int height;

		public Size(int width, int height) {
			this.width = width;
			this.height = height;
		}
	}
	
	private static final class Accelarator{
		float duration;  // 持续时间
		float start;     // 开始位置
		float end;       // 终止位置
		float accler;    // 加速度
		float v0;        // 初始速度
		
		void reset(float start, float end, float duration){
			this.start = start;
			this.duration = duration;
//			float s = end - start;
//			float t2 = duration * duration;
//			accler = -2 * s/t2;
//			v0 = accler * duration;
			
			Log.i(TAG, "start = " + start + ", end = " + end);
			
			v0 = (end - start)/duration;
		}
		
		float valueOf(float t){
//			return v0 * t + 0.5f * accler * t * t + start;
			if(t > duration) t =duration;
			return v0 * t + start;
		}
	}
	
	private final class Fliping{
		float v0;
		float a;
		float start;
//		float duration;
		
		void reset(float start, float v0, float duration){
			this.start = start;
			this.v0 = v0;
//			this.duration = duration;
			
			a = -v0/duration;
		}
		
		// recompute the accelerator.
		void fix(float end){
			float s = end - start;
			v0 = (float) Math.sqrt(Math.abs(-2 * a * s));
			if(s < 0) v0 = -v0;
		}
		
		float valueOf(float t){
			return start + v0 * t + 0.5f * a * t * t;
		}
	}

}
