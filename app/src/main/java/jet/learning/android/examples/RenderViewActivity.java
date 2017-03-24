package jet.learning.android.examples;

import com.example.oglsamples.R;

import android.app.Activity;
import android.os.Bundle;

public class RenderViewActivity extends Activity{

	RenderView view;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.render_view_layout);
		
		view = (RenderView) findViewById(R.id.render_view);
		
		ItemPicker picker = (ItemPicker)view;
		picker.setTextSize(60);
		picker.setVisibleItemCount(5);
		
		((ItemPicker)view).setRangeValues(1, 10);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
//		view.startRender();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
//		view.stopRender();
	}
}
