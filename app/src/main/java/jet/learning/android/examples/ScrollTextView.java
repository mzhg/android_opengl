package jet.learning.android.examples;

import java.util.ArrayList;
import java.util.List;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.TextView;

public class ScrollTextView extends TextView{

	private final List<String> items = new ArrayList<String>();
	private int current = 0;
	private boolean looping;
	private long delayMillis = 3000;  // 3 seconds.
	
	@SuppressLint("HandlerLeak")
	private final Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			current = (current + 1) % items.size();
			setText(items.get(current));
			if(looping)
				mHandler.sendEmptyMessageDelayed(0, delayMillis);
		}
	};
	
	public ScrollTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public ScrollTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ScrollTextView(Context context) {
		super(context);
	}

	public void addItem(String item) { items.add(item);}
	
	public List<String> getItems() { return items;}
	public void setDelay(long millis) { delayMillis = millis;}
	
	public void doLoop(){
		if(!looping){
			looping = true;
			setText(items.get(current));
			mHandler.sendEmptyMessageDelayed(0, delayMillis);
		}
	}
	
	public void reset() { current = 0;}
	public void stop()  { looping = false;  mHandler.removeMessages(0);}
}
