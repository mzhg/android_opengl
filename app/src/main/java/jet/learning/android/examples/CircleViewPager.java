package jet.learning.android.examples;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public class CircleViewPager extends ViewPager{

	private OnPageChangeListener listener;
	private boolean flag, igoreDoubleCase;
	private int _position;
	
	public CircleViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		init(context);
	}

	public CircleViewPager(Context context) {
		super(context);
		
		init(context);
	}
	
	@Override
	public void setCurrentItem(int item, boolean smoothScroll) {
		int count = getAdapter().getCount();
		int currentItem = getCurrentItem();
		if(count < 2){
			super.setCurrentItem(item);
		}else{
			if(item == 0){
				super.setCurrentItem(_position = (count - 2), smoothScroll);
			}else if(item == count - 1){
				super.setCurrentItem(_position = 1, smoothScroll);
			}else{
				super.setCurrentItem(_position = item, smoothScroll);
			}
		}
		
		flag = currentItem != _position;
		if(!flag && listener != null)
			listener.onPageSelected(_position);
	}
	
	@Override
	public void setCurrentItem(int item) {
		int count = getAdapter().getCount();
		int currentItem = getCurrentItem();
		Log.i("Circle", "child count = " + count + ", currentItem = " + currentItem + ", item = " + item);
		if(count < 2){
			flag = true;
			super.setCurrentItem(item);
		}else{
			if(item == 0){
				_position = count - 2;
			}else if(item == count - 1){
				_position = 1;
			}else{
				_position = item;
			}

			flag = (currentItem != _position);
			if(!flag){
				Log.i("Circle", "called from setCurrentItem");
				listener.onPageSelected(item);
			}else{
				Log.i("Circle", "flag set to true, condition is currentItem = " + currentItem + ", _position = " + _position);
				super.setCurrentItem(_position, false);
			}
		}
		
	}
	
	private void init(Context context){
		super.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				Log.i("Circle", "onPageSelected = " + position + ", flag = " + flag);
				if(!flag)
					setCurrentItem(position);
				else{
					flag = false;
					if(listener != null){
						Log.i("Circle", "called from onPageSelected");
						listener.onPageSelected(_position);
					}
				}
			}
			
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				if(listener != null)
					listener.onPageScrolled(_position, positionOffset, positionOffsetPixels);
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
				if(listener != null)
					listener.onPageScrollStateChanged(state);
			}
		});
	}
	
	@Override
	public void setAdapter(PagerAdapter arg0) {
		if(!(arg0 instanceof CirclePageAdapter)){
			throw new IllegalArgumentException("PagerAdapter is not the subclass of the CirclePageAdapter");
		}
		super.setAdapter(arg0);
	}

	@Override
	public void setOnPageChangeListener(OnPageChangeListener listener) {
		this.listener = listener;
	}
	
	public static abstract class CirclePageAdapter<T> extends PagerAdapter{
		
		final List<T> _elements = new ArrayList<T>();
		
		public final void setElements(List<T> elements, T first, T last){
			_elements.clear();
			
			if(elements != null)
				_elements.addAll(elements);
			_elements.add(0, last);
			_elements.add(first);
		}
		
		public final void setElements(List<T> elements){
			_elements.clear();
			if(elements != null)
				_elements.addAll(elements);
			
			if(elements.size() > 1){
				T first = elements.get(0);
				T last  = elements.get(elements.size() - 1);
				_elements.add(0, last);
				_elements.add(first);
			}
		}
		
		@Override
		public int getCount() { return _elements.size();}
		
		public final T getElement(int index) { return _elements.get(index);}
		
		public boolean removeElement(T e){
			if(_elements.size() < 2){
				return _elements.remove(e);
			}else{
				int index = -1;
				for(int i = 1; i < _elements.size() - 1; i++){
					if(_elements.get(i).equals(e)){
						index = i;
						break;
					}
				}
				
				if(index != -1){
					_elements.remove(index);
				}
				
				return index != -1;
			}
		}
		
		public final void clear(){
			_elements.clear();
		}
	}
	
	private final class _ViewAdapter extends PagerAdapter{

		private PagerAdapter _adapter;
		private View first, last;
		
		public _ViewAdapter(PagerAdapter adapter) {
			_adapter = adapter;
		}
		
		/**
		 * @param container
		 * @param position
		 * @param object
		 * @deprecated
		 * @see android.support.v4.view.PagerAdapter#destroyItem(android.view.View, int, java.lang.Object)
		 */
		public void destroyItem(View container, int position, Object object) {
			int count = _adapter.getCount();
			if(count < 2){
				_adapter.destroyItem(container, position, object);
				return;
			}
			
			if(position == 0){
				position = count - 1;
			}else if(position == count + 1){
				position = 0;
			}else{
				position --;
			}
			_adapter.destroyItem(container, position, object);
			
			if(count - 1 < 2){
				notifyDataSetChanged();
			}
		}

//		final int fixPosition(int position){
//			int count = _adapter.getCount();
//			if(count < 2)
//				return position;
//			
//			if(position == 0){
//				
//			}
//		}

		/**
		 * @param container
		 * @param position
		 * @param object
		 * @see android.support.v4.view.PagerAdapter#destroyItem(android.view.ViewGroup, int, java.lang.Object)
		 */
		public void destroyItem(ViewGroup container, int position, Object object) {
			int count = _adapter.getCount();
			if(count < 2){
				_adapter.destroyItem(container, position, object);
				return;
			}
			
			if(position == 0){
				position = count - 1;
			}else if(position == count + 1){
				position = 0;
			}else{
				position --;
			}
			_adapter.destroyItem(container, position, object);
			
			if(count - 1 < 2){
				notifyDataSetChanged();
			}
		}

		/**
		 * @param o
		 * @return
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object o) {
			return _adapter.equals(o);
		}



		/**
		 * @param container
		 * @deprecated
		 * @see android.support.v4.view.PagerAdapter#finishUpdate(android.view.View)
		 */
		public void finishUpdate(View container) {
			_adapter.finishUpdate(container);
		}



		/**
		 * @param container
		 * @see android.support.v4.view.PagerAdapter#finishUpdate(android.view.ViewGroup)
		 */
		public void finishUpdate(ViewGroup container) {
			_adapter.finishUpdate(container);
		}



		/**
		 * @param object
		 * @return
		 * @see android.support.v4.view.PagerAdapter#getItemPosition(java.lang.Object)
		 */
		public int getItemPosition(Object object) {
			int position = _adapter.getItemPosition(object) + 1;
//			if(position == _adapter.getCount());
			return position;
		}



		/**
		 * @param position
		 * @return
		 * @see android.support.v4.view.PagerAdapter#getPageTitle(int)
		 */
		public CharSequence getPageTitle(int position) {
			return _adapter.getPageTitle(position);
		}



		/**
		 * @param position
		 * @return
		 * @see android.support.v4.view.PagerAdapter#getPageWidth(int)
		 */
		public float getPageWidth(int position) {
			return _adapter.getPageWidth(position);
		}

		/**
		 * @return
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode() {
			return _adapter.hashCode();
		}



		/**
		 * @param container
		 * @param position
		 * @return
		 * @deprecated
		 * @see android.support.v4.view.PagerAdapter#instantiateItem(android.view.View, int)
		 */
		public Object instantiateItem(View container, int position) {
			int count = _adapter.getCount();
	        if(position == 0)
	        	position = count - 1;
	        else if(position == count + 1)
	        	position = 0;
	        else
	        	position --;
			
			return _adapter.instantiateItem(container, position);
		}



		/**
		 * @param container
		 * @param position
		 * @return
		 * @see android.support.v4.view.PagerAdapter#instantiateItem(android.view.ViewGroup, int)
		 */
		public Object instantiateItem(ViewGroup container, int position) {
			int count = _adapter.getCount();
	        if(position == 0)
	        	position = count - 1;
	        else if(position == count + 1)
	        	position = 0;
	        else
	        	position --;
			
			return _adapter.instantiateItem(container, position);
		}



		/*
		 * 
		 * @see android.support.v4.view.PagerAdapter#notifyDataSetChanged()
		 */
//		public void notifyDataSetChanged() {
//			_adapter.notifyDataSetChanged();
//		}



		/**
		 * @param observer
		 * @see android.support.v4.view.PagerAdapter#registerDataSetObserver(android.database.DataSetObserver)
		 */
		public void registerDataSetObserver(DataSetObserver observer) {
			super.registerDataSetObserver(observer);
			_adapter.registerDataSetObserver(observer);
		}



		/**
		 * @param state
		 * @param loader
		 * @see android.support.v4.view.PagerAdapter#restoreState(android.os.Parcelable, java.lang.ClassLoader)
		 */
		public void restoreState(Parcelable state, ClassLoader loader) {
			super.restoreState(state, loader);
			_adapter.restoreState(state, loader);
		}



		/**
		 * @return
		 * @see android.support.v4.view.PagerAdapter#saveState()
		 */
		public Parcelable saveState() {
			
			return _adapter.saveState();
		}



		/**
		 * @param container
		 * @param position
		 * @param object
		 * @deprecated
		 * @see android.support.v4.view.PagerAdapter#setPrimaryItem(android.view.View, int, java.lang.Object)
		 */
		public void setPrimaryItem(View container, int position, Object object) {
			_adapter.setPrimaryItem(container, position, object);
		}



		/**
		 * @param container
		 * @param position
		 * @param object
		 * @see android.support.v4.view.PagerAdapter#setPrimaryItem(android.view.ViewGroup, int, java.lang.Object)
		 */
		public void setPrimaryItem(ViewGroup container, int position,
				Object object) {
			_adapter.setPrimaryItem(container, position, object);
		}



		/**
		 * @param container
		 * @deprecated
		 * @see android.support.v4.view.PagerAdapter#startUpdate(android.view.View)
		 */
		public void startUpdate(View container) {
			_adapter.startUpdate(container);
		}



		/**
		 * @param container
		 * @see android.support.v4.view.PagerAdapter#startUpdate(android.view.ViewGroup)
		 */
		public void startUpdate(ViewGroup container) {
			_adapter.startUpdate(container);
		}



		/**
		 * @return
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return _adapter.toString();
		}



		/**
		 * @param observer
		 * @see android.support.v4.view.PagerAdapter#unregisterDataSetObserver(android.database.DataSetObserver)
		 */
		public void unregisterDataSetObserver(DataSetObserver observer) {
			_adapter.unregisterDataSetObserver(observer);
		}



		@Override
		public int getCount() {
			int count = _adapter.getCount();
			if(count < 2)
				return count;
			return count + 2;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return _adapter.isViewFromObject(arg0, arg1);
		}
		
	}
	
}
