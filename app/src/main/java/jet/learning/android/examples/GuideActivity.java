package jet.learning.android.examples;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.oglsamples.R;

public class GuideActivity extends Activity{

	private static final String TAG = "Guide";
	
	private static final int NUM_VIEWS = 4;
	
	RenderView[] contentViews = new RenderView[NUM_VIEWS];
	String[] pageTitle = new String[4];
	ViewPager viewPager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		GuideResource.load(this);
		setContentView(R.layout.activity_guide_main);
		viewPager = (ViewPager) findViewById(R.id.planetViewPager);
		
		ViewPager.LayoutParams params = new ViewPager.LayoutParams();
		params.width = ViewPager.LayoutParams.MATCH_PARENT;
		params.height = ViewPager.LayoutParams.MATCH_PARENT;
		
		contentViews[0] = new GuideView1(this);
		contentViews[1] = new GuideView2(this);
		contentViews[2] = new GuideView3(this);
		contentViews[3] = new GuideView4(this);
		
		for(int i= 0; i < NUM_VIEWS; i++){
			contentViews[i].setLayoutParams(params);
			contentViews[i].setBackgroundColor(Color.rgb(240, 245, 248));
			pageTitle[i] = "Guide" + (i + 1);
		}
		
		((GuideView4)contentViews[3]).setOnButtonClickedListener(new GuideView4.OnButtonClickedListener() {
			@Override
			public void onClicked() {
				Toast.makeText(GuideActivity.this, "Button Clicked", Toast.LENGTH_SHORT).show();
			}
		});
		
		viewPager.setAdapter(new ViewPagerAdapter());
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			public void onPageSelected(int arg0) {
				Log.i(TAG, "onPageSelected: " + arg0);
				
				contentViews[arg0].startRender();
				int left = arg0 - 1;
				if(left >=0){
					contentViews[left].stopRender();
					contentViews[left].invalidate();
				}
				
				int right = arg0 + 1;
				if(right < NUM_VIEWS){
					contentViews[right].stopRender();
					contentViews[right].invalidate();
				}
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				Log.i(TAG, "onPageScrolled: " + arg0 + ", " + arg1 + ", " + arg2);
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				Log.i(TAG, "onPageScrollStateChanged: " + arg0);
			}
		});
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		for(int i= 0; i < NUM_VIEWS; i++){
			contentViews[i].stopRender();
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		GuideResource.release();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private class ViewPagerAdapter extends PagerAdapter {
		@Override
		public int getCount() {
			return NUM_VIEWS;
		}
		@Override
		public Object instantiateItem(ViewGroup imageArray, int galaxy) {
//			ImageView spaceView = new ImageView(GuideActivity.this);
//			spaceView.setImageResource(space[galaxy]);
			RenderView spaceView = contentViews[galaxy];
			((ViewPager) imageArray).addView(spaceView, 0);
			return spaceView;
		}
		@Override
		public void destroyItem(ViewGroup imageArray, int galaxy, Object spaceView) {
			((ViewPager) imageArray).removeView((RenderView) spaceView);
		}
		@Override
		public boolean isViewFromObject(View spaceView, Object galaxy) {
			return spaceView == galaxy;
		}
		@Override
		public CharSequence getPageTitle(int arrayPos){
			return pageTitle[arrayPos];
		}
		@Override
		public Parcelable saveState() {
			return null;
		}
		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {}
		@Override
		public void startUpdate(ViewGroup arg0) {}
		@Override
		public void finishUpdate(ViewGroup arg0) {}
	}
}
