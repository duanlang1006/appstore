package com.applite.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.applite.homepage.BundleContextFactory;

/**
 * Created by yuzhimin on 6/25/15.
 */
public class TopicViewPager extends ViewPager {

	int mLastMotionY;
	int mLastMotionX;
	
	public TopicViewPager(Context context) {

        super(context);
        try {
            Context mContext = BundleContextFactory.getInstance().getBundleContext().getBundleContext();
            context =mContext;

        }catch (Exception e){
            e.printStackTrace();
        }

	}
	
	public TopicViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        try {
            Context mContext = BundleContextFactory.getInstance().getBundleContext().getBundleContext();
            context =mContext;

        }catch (Exception e){
            e.printStackTrace();
        }
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		getParent().requestDisallowInterceptTouchEvent(true); //只需这句话，让父类不拦截触摸事件就可以了。
        return super.dispatchTouchEvent(ev);
	}
}
