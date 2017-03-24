package jet.learning.android.examples;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class CircleViewPager2 extends ViewPager{
	
	private CircleViewPagerAdapter pagerAdapter;
	private OnPageChangeListener listener;
	
	public CircleViewPager2(Context context) {
		super(context);
		init();
	}

	public CircleViewPager2(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	private void init(){
		super.setOnPageChangeListener(new OnPageChangeListener(){
			@Override
			public void onPageScrollStateChanged(int state) {
				if(listener != null){
					listener.onPageScrollStateChanged(state);
				}
			}

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				if(listener != null){
					listener.onPageScrolled(fixPosition(position), positionOffset, positionOffsetPixels);
				}
			}

			@Override
			public void onPageSelected(int position) {
				if(listener != null){
					listener.onPageSelected(fixPosition(position));
				}
			}
		});
	}
	
	int fixPosition(int position){
		if(pagerAdapter != null){
			return position % pagerAdapter.adapter.getCount(); 
		}else{
			return position;
		}
	}
	
	@Override
	public void setCurrentItem(int item) {
		if(pagerAdapter != null){
			_setCurrentItem(pagerAdapter.startValue + item);
		}else
			_setCurrentItem(item);
	}
	
	@Override
	public void setCurrentItem(int item, boolean smoothScroll) {
		if(pagerAdapter != null){
			_setCurrentItem(pagerAdapter.startValue + item, smoothScroll);
		}else
			_setCurrentItem(item, smoothScroll);
	}
	
	@Override
	public int getCurrentItem() {	return fixPosition(super.getCurrentItem());}
	
	@Override
	public void setAdapter(PagerAdapter arg0) {
		CircleViewPagerAdapter oldAdapter = pagerAdapter;
		if(oldAdapter != null){
			oldAdapter.clear();
		}
		
		pagerAdapter = new CircleViewPagerAdapter(arg0);
		super.setAdapter(pagerAdapter);
		setCurrentItem(0);
	}
	
	@Override
	public PagerAdapter getAdapter() {
		if(pagerAdapter != null){
			return pagerAdapter.adapter;
		}
		
		return null;
	}
	
	@Override
	public void setOnPageChangeListener(OnPageChangeListener listener) {
		this.listener = listener;
	}
	
	void _setCurrentItem(int item){ super.setCurrentItem(item);} 
	void _setCurrentItem(int item, boolean smooth){ super.setCurrentItem(item, smooth);} 

	private final class CircleViewPagerAdapter extends PagerAdapter{

		PagerAdapter adapter;
		
		final DataSetObserver observer = new DataSetObserver() {
			@Override
			public void onChanged() { reset();}
		};
		
		int startValue;
		int elementCount;
		
		public CircleViewPagerAdapter(PagerAdapter adapter) {
			this.adapter = adapter;
			adapter.registerDataSetObserver(observer);
			
			reset();
		}
		
		void clear(){ adapter.unregisterDataSetObserver(observer);}
		void reset(){
			int count = adapter.getCount();
			if(count < 0){
				throw new IllegalArgumentException("The proxy.getCount() < 0.");
			}
			
			if(count > 0){
				int mod = Integer.MAX_VALUE % count;
				elementCount = Integer.MAX_VALUE - mod;
				int middle = Integer.MAX_VALUE/2;
				startValue = middle - middle % count;
			}else{ // count == 0
				startValue = 0;
				elementCount = 0;
			}
			
			if(getAdapter() == this){
				_setCurrentItem(startValue);
			}
		}

		@Override
		public int getCount() { return elementCount;}
		
		public void destroyItem(View container, int position, Object object) {
			adapter.destroyItem(container, position % adapter.getCount(), object);
		}

		public void destroyItem(ViewGroup container, int position, Object object) {
			adapter.destroyItem(container, position % adapter.getCount(), object);
		}

		public boolean equals(Object o) {
			return adapter.equals(o);
		}

		public void finishUpdate(View container) {
			adapter.finishUpdate(container);
		}

		
		public void finishUpdate(ViewGroup container) {
			adapter.finishUpdate(container);
		}

		
		public int getItemPosition(Object object) {
			return adapter.getItemPosition(object);
		}

		/**
		 * @param position
		 * @return
		 * @see android.support.v4.view.PagerAdapter#getPageTitle(int)
		 */
		public CharSequence getPageTitle(int position) {
			return adapter.getPageTitle(position % adapter.getCount());
		}

		/**
		 * @param position
		 * @return
		 * @see android.support.v4.view.PagerAdapter#getPageWidth(int)
		 */
		public float getPageWidth(int position) {
			return adapter.getPageWidth(position % adapter.getCount());
		}

		/**
		 * @param container
		 * @param position
		 * @return
		 * @deprecated
		 * @see android.support.v4.view.PagerAdapter#instantiateItem(android.view.View, int)
		 */
		public Object instantiateItem(View container, int position) {
			return adapter.instantiateItem(container, position % adapter.getCount());
		}

		/**
		 * @param container
		 * @param position
		 * @return
		 * @see android.support.v4.view.PagerAdapter#instantiateItem(android.view.ViewGroup, int)
		 */
		public Object instantiateItem(ViewGroup container, int position) {
			return adapter.instantiateItem(container, position % adapter.getCount());
		}

		/**
		 * @param state
		 * @param loader
		 * @see android.support.v4.view.PagerAdapter#restoreState(android.os.Parcelable, java.lang.ClassLoader)
		 */
		public void restoreState(Parcelable state, ClassLoader loader) {
			adapter.restoreState(state, loader);
		}

		/**
		 * @return
		 * @see android.support.v4.view.PagerAdapter#saveState()
		 */
		public Parcelable saveState() {
			return adapter.saveState();
		}

		/**
		 * @param container
		 * @param position
		 * @param object
		 * @deprecated
		 * @see android.support.v4.view.PagerAdapter#setPrimaryItem(android.view.View, int, java.lang.Object)
		 */
		public void setPrimaryItem(View container, int position, Object object) {
			adapter.setPrimaryItem(container, position % adapter.getCount(), object);
		}

		/**
		 * @param container
		 * @param position
		 * @param object
		 * @see android.support.v4.view.PagerAdapter#setPrimaryItem(android.view.ViewGroup, int, java.lang.Object)
		 */
		public void setPrimaryItem(ViewGroup container, int position,Object object) {
			adapter.setPrimaryItem(container, position % adapter.getCount(), object);
		}

		/**
		 * @param container
		 * @deprecated
		 * @see android.support.v4.view.PagerAdapter#startUpdate(android.view.View)
		 */
		public void startUpdate(View container) {
			adapter.startUpdate(container);
		}

		/**
		 * @param container
		 * @see android.support.v4.view.PagerAdapter#startUpdate(android.view.ViewGroup)
		 */
		public void startUpdate(ViewGroup container) {
			adapter.startUpdate(container);
		}

		/**
		 * @return
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return adapter.toString();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return adapter.isViewFromObject(arg0, arg1);
		}
		
	}
}
