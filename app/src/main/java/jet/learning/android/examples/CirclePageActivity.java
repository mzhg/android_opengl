package jet.learning.android.examples;

import java.util.ArrayList;
import java.util.List;

import jet.learning.android.examples.CircleViewPager.CirclePageAdapter;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.oglsamples.R;

public class CirclePageActivity extends Activity{
	private static final String TAG = "CirclePageActivity";

	private static final int NUM_VIEWS = 4;
	
	List<RenderView> contentViews = new ArrayList<RenderView>(NUM_VIEWS);
	String[] pageTitle = new String[4];
	CircleViewPager viewPager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		GuideResource.load(this);
		setContentView(R.layout.activity_circle_pager);
		viewPager = (CircleViewPager) findViewById(R.id.planetViewPager);
		
		ViewPager.LayoutParams params = new ViewPager.LayoutParams();
		params.width = ViewPager.LayoutParams.MATCH_PARENT;
		params.height = ViewPager.LayoutParams.MATCH_PARENT;
		
		contentViews.add(new GuideView1(this));
		contentViews.add(new GuideView2(this));
		contentViews.add(new GuideView3(this));
		contentViews.add(new GuideView4(this));
		
		for(int i= 0; i < NUM_VIEWS; i++){
			contentViews.get(i).setLayoutParams(params);
			contentViews.get(i).setBackgroundColor(Color.rgb(240, 245, 248));
			pageTitle[i] = "Guide" + (i + 1);
		}
		
		((GuideView4)contentViews.get(3)).setOnButtonClickedListener(new GuideView4.OnButtonClickedListener() {
			@Override
			public void onClicked() {
				Toast.makeText(CirclePageActivity.this, "Button Clicked", Toast.LENGTH_SHORT).show();
			}
		});
		
		final ViewPagerAdapter adapter = new ViewPagerAdapter();
		adapter.setElements(contentViews, new GuideView1(this), new GuideView4(this));
		viewPager.setAdapter(adapter);
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			public void onPageSelected(int arg0) {
				Log.i(TAG, "onPageSelected: " + arg0);
				
//				contentViews.get(arg0).startRender();
				adapter.getElement(arg0).startRender();
//				int left = arg0 - 1;
//				if(left >=0){
//					contentViews.get(left).stopRender();
//					contentViews.get(left).invalidate();
//				}
//				
//				int right = arg0 + 1;
//				if(right < NUM_VIEWS){
//					contentViews.get(right).stopRender();
//					contentViews.get(right).invalidate();
//				}
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
//				Log.i(TAG, "onPageScrolled: " + arg0 + ", " + arg1 + ", " + arg2);
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
//				Log.i(TAG, "onPageScrollStateChanged: " + arg0);
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		viewPager.setCurrentItem(1);
		contentViews.get(0).startRender();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		for(int i= 0; i < NUM_VIEWS; i++){
			contentViews.get(i).stopRender();
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
	
	private class ViewPagerAdapter extends CirclePageAdapter<RenderView> {
		
		@Override
		public Object instantiateItem(ViewGroup imageArray, int galaxy) {
//			ImageView spaceView = new ImageView(GuideActivity.this);
//			spaceView.setImageResource(space[galaxy]);
			RenderView spaceView = getElement(galaxy);
			imageArray.removeView(spaceView);
			((ViewPager) imageArray).addView(spaceView);
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
