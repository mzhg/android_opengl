package pro.android.ui;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.oglsamples.R;

public class ViewPagingActivity extends Activity {
	private static final String TAG = "ViewPager";
	
	private static int NUMBER_IMAGES = 4;
	private ViewPagerAdapter viewPagerAdapter;
	private ViewPager viewPager;
	private int[] space = {R.drawable.stars480, R.drawable.plasma480, R.drawable.plasma720, R.drawable.plasma1080};
	private static final String[] pageTitle = {"stars480.png", "galaxy480.png", "galaxy720.png", "galaxy1080.png"};
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_viewpaging);
		viewPagerAdapter = new ViewPagerAdapter();
		viewPager = (ViewPager) findViewById(R.id.planetViewPager);
		viewPager.setAdapter(viewPagerAdapter);
		
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			public void onPageSelected(int arg0) {
				Log.i(TAG, "onPageSelected: " + arg0);
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
		
		final PagerTabStrip pagerTitle = (PagerTabStrip) findViewById(R.id.pagerTabStrip);
		pagerTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
		pagerTitle.setTextColor(Color.CYAN);
		pagerTitle.setNonPrimaryAlpha(0.64f);
		pagerTitle.setTextSpacing(4);
		pagerTitle.setBackgroundColor(Color.DKGRAY);
		pagerTitle.setTabIndicatorColor(Color.YELLOW);
		pagerTitle.setBackgroundResource(R.drawable.pagetaboval);
		pagerTitle.setPadding(0, 10, 0, 0);
	}
	private class ViewPagerAdapter extends PagerAdapter {
		@Override
		public int getCount() {
			return NUMBER_IMAGES;
		}
		@Override
		public Object instantiateItem(ViewGroup imageArray, int galaxy) {
			ImageView spaceView = new ImageView(ViewPagingActivity.this);
			spaceView.setImageResource(space[galaxy]);
			((ViewPager) imageArray).addView(spaceView, 0);
			return spaceView;
		}
		@Override
		public void destroyItem(ViewGroup imageArray, int galaxy, Object spaceView) {
			((ViewPager) imageArray).removeView((ImageView) spaceView);
		}
		@Override
		public boolean isViewFromObject(View spaceView, Object galaxy) {
			return spaceView == ((ImageView) galaxy);
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