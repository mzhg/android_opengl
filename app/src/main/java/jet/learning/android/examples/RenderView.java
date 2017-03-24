package jet.learning.android.examples;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

public abstract class RenderView extends View{

	private boolean mInRenderState;
	private boolean mMsgConsumed;
	public RenderView(Context context) {
		super(context);
	}

	public RenderView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public RenderView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected final void onDraw(Canvas canvas) {
		onRender(canvas);
		if(mInRenderState && mMsgConsumed){
			mMsgConsumed = false;
			handler.sendEmptyMessageAtTime(0, 40);
		}
	}
	
	protected abstract void onRender(Canvas canvas);
	
	public void startRender(){
//		handler.sendEmptyMessageAtTime(1, 40);
		
		mInRenderState = true;
		handler.sendEmptyMessageAtTime(0, 40);
	}
	
	public void startRender(long millon){
//		handler.sendEmptyMessageAtTime(1, 40);
		
		mInRenderState = true;
		handler.sendEmptyMessageAtTime(0, millon);
	}
	
	public void stopRender(){
		mInRenderState = false;
	}
	
	public boolean isInRenderState() { return mInRenderState; }
	@SuppressLint("HandlerLeak")
	private final Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			mMsgConsumed = true;
			invalidate();
		}
	};

}
