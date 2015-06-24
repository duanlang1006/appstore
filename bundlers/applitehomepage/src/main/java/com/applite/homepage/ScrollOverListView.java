package com.applite.homepage;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

import com.applite.utils.HomePageUtils;


/**
 * <p>一个可以监听ListView是否滚动到最顶部或最底部的自定义控件</p>
 * 只能监听由触摸产生的，如果是ListView本身Flying导致的，则不能监听</br>
 * 如果加以改进，可以实现监听scroll滚动的具体位置等
 */

public class ScrollOverListView extends ListView {

	private static final String TAG = "ScrollOverListView";
	private static final boolean DEBUG = true;
	private int mLastY;
	private int mTopPosition;
	private int mBottomPosition;

	public ScrollOverListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
        HomePageUtils.d(TAG, "ScrollOverListView yuzm 3");
	}

	public ScrollOverListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
        HomePageUtils.d(TAG, "ScrollOverListView yuzm 2");
	}

	public ScrollOverListView(Context context) {
		super(context);
		init();
        HomePageUtils.d(TAG, "ScrollOverListView yuzm 1");
	}

	private void init(){
		mTopPosition = 0;
		mBottomPosition = 0;
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
        HomePageUtils.d(TAG, "onInterceptTouchEvent yuzm ");
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			if (DEBUG) HomePageUtils.d(TAG, "onInterceptTouchEvent Action down");
			mLastY = (int) ev.getRawY();
		}
        //PullToRefreshListFragment pullToRefreshListFragment;
		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		final int action = ev.getAction();
		final int y = (int) ev.getRawY();
        HomePageUtils.d(TAG, "onTouchEvent yuzm ");
		boolean isHandled = false;
		switch(action){
			case MotionEvent.ACTION_DOWN:{
				if (DEBUG) HomePageUtils.d(TAG, "onTouchEvent action down yuzm");
				mLastY = y;
				isHandled = mOnScrollOverListener.onMotionDown(ev);
				if (isHandled) {
					break;
				}
				break;
			}
			
			case MotionEvent.ACTION_MOVE:{
				if (DEBUG) HomePageUtils.d(TAG, "onTouchEvent action move yuzm");
				final int childCount = getChildCount();
				if(childCount == 0) {
					break;
				}
				
				final int itemCount = getAdapter().getCount() - mBottomPosition;
				
				final int deltaY = y - mLastY;
				if (DEBUG) Log.d(TAG, "lastY=" + mLastY +" y=" + y);
				
				final int firstTop = getChildAt(0).getTop();
				final int listPadding = getListPaddingTop();
				
				final int lastBottom = getChildAt(childCount - 1).getBottom();
				final int end = getHeight() - getPaddingBottom();
				
				final int firstVisiblePosition = getFirstVisiblePosition();
				
				isHandled = mOnScrollOverListener.onMotionMove(ev, deltaY);
				
				if(isHandled){
					break;
				}
				
				//DLog.d("firstVisiblePosition=%d firstTop=%d listPaddingTop=%d deltaY=%d", firstVisiblePosition, firstTop, listPadding, deltaY);
				if (firstVisiblePosition <= mTopPosition && firstTop >= listPadding && deltaY > 0) {
					if (DEBUG) Log.d(TAG, "action move pull down");
		            isHandled = mOnScrollOverListener.onListViewTopAndPullDown(ev, deltaY);
		            if(isHandled){
		            	break;
		            }
		        }
				
				// DLog.d("lastBottom=%d end=%d deltaY=%d", lastBottom, end, deltaY);
		        if (firstVisiblePosition + childCount >= itemCount && lastBottom <= end && deltaY < 0) {
		        	if (DEBUG) Log.d(TAG, "action move pull up");
		        	isHandled = mOnScrollOverListener.onListViewBottomAndPullUp(ev, deltaY);
		        	if(isHandled){
		        		break;
		        	}
		        }
				break;
			}
			
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:{
				if (DEBUG) HomePageUtils.d(TAG, "onTouchEvent action move pull up yuzm");
				isHandled = mOnScrollOverListener.onMotionUp(ev);
				if (isHandled) {
					break;
				}
				break;
			}
		}
		
		mLastY = y;
		if (isHandled) {
			return true;
		}
		return super.onTouchEvent(ev);
	}
	
	
	/**空的*/
	private OnScrollOverListener mOnScrollOverListener = new OnScrollOverListener(){

		@Override
		public boolean onListViewTopAndPullDown(MotionEvent event, int delta) {
			return false;
		}

		@Override
		public boolean onListViewBottomAndPullUp(MotionEvent event, int delta) {
			return false;
		}

		@Override
		public boolean onMotionDown(MotionEvent ev) {
			return false;
		}

		@Override
		public boolean onMotionMove(MotionEvent ev, int delta) {
			return false;
		}

		@Override
		public boolean onMotionUp(MotionEvent ev) {
			return false;
		}
		
	};
	
	
	
	
	
	
	
	// =============================== public method ===============================

	/**
	 * 可以自定义其中一个条目为头部，头部触发的事件将以这个为准，默认为第一个
	 * 
	 * @param index 正数第几个，必须在条目数范围之内
	 */
	public void setTopPosition(int index){
		if(index < 0)
			throw new IllegalArgumentException("Top position must > 0");
        HomePageUtils.d(TAG, "setTopPosition yuzm , index : " + index);
		mTopPosition = index;
	}
	
	/**
	 * 可以自定义其中一个条目为尾部，尾部触发的事件将以这个为准，默认为最后一个
	 * 
	 * @param index 倒数第几个，必须在条目数范围之内
	 */
	public void setBottomPosition(int index){
		if(index < 0)
			throw new IllegalArgumentException("Bottom position must > 0");
        HomePageUtils.d(TAG, "setBottomPosition yuzm , index : " + index);
		mBottomPosition = index;
	}

	/**
	 * 设置这个Listener可以监听是否到达顶端，或者是否到达低端等事件</br>
	 * 
	 * @see OnScrollOverListener
	 */
	public void setOnScrollOverListener(OnScrollOverListener onScrollOverListener){
		mOnScrollOverListener = onScrollOverListener;
        HomePageUtils.d(TAG, "setOnScrollOverListener yuzm , index : ");
	}
	
	/**
	 * 滚动监听接口</br>
	 * @see ScrollOverListView#setOnScrollOverListener(OnScrollOverListener)
	 *
	 */
	public interface OnScrollOverListener {
		
		/**
		 * 到达最顶部触发
		 * 
		 * @param delta 手指点击移动产生的偏移量
		 * @return 
		 */
		boolean onListViewTopAndPullDown(MotionEvent event, int delta);

		/**
		 * 到达最底部触发
		 * 
		 * @param delta 手指点击移动产生的偏移量
		 * @return 
		 */
		boolean onListViewBottomAndPullUp(MotionEvent event, int delta);
		
		/**
		 * 手指触摸按下触发，相当于{@link MotionEvent#ACTION_DOWN}
		 * 
		 * @return 返回true表示自己处理
		 * @see View#onTouchEvent(MotionEvent)
		 */
		boolean onMotionDown(MotionEvent ev);
		
		/**
		 * 手指触摸移动触发，相当于{@link MotionEvent#ACTION_MOVE}
		 * 
		 * @return 返回true表示自己处理
		 * @see View#onTouchEvent(MotionEvent)
		 */
		boolean onMotionMove(MotionEvent ev, int delta);
		
		/**
		 * 手指触摸后提起触发，相当于{@link MotionEvent#ACTION_UP} 
		 * 
		 * @return 返回true表示自己处理
		 * @see View#onTouchEvent(MotionEvent)
		 */
		boolean onMotionUp(MotionEvent ev);
		
	}


}
