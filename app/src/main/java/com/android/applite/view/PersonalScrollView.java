package com.android.applite.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * 自定义ScrollView 
 * 
 */
public class PersonalScrollView extends ScrollView {
	private static int MAX_SCROLL = 0; //滑动的最大距离  
    private static float SCROLL_RATIO = 0.8f;// 阻尼系数  
    private float xDistance, yDistance, xLast, yLast;
	public PersonalScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	public PersonalScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	public PersonalScrollView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    	// TODO Auto-generated method stub
    	super.onSizeChanged(w, h, oldw, oldh);
    	MAX_SCROLL = h / 5 * 4;  
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
    	// TODO Auto-generated method stub
    	switch (ev.getAction()) {
        case MotionEvent.ACTION_DOWN:
            xDistance = yDistance = 0f;
            xLast = ev.getX();
            yLast = ev.getY();
            break;
        case MotionEvent.ACTION_MOVE:
            final float curX = ev.getX();
            final float curY = ev.getY();
                
            xDistance += Math.abs(curX - xLast);
            yDistance += Math.abs(curY - yLast);
            xLast = curX;
            yLast = curY;
                
            if(xDistance > yDistance){
                return false;
            }
    }
    	return super.onInterceptTouchEvent(ev);
    }
    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX,
    		int scrollY, int scrollRangeX, int scrollRangeY,
    		int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
    	// TODO Auto-generated method stub
    	int newDeltaY = deltaY;  
        int delta = (int) (deltaY * SCROLL_RATIO);  
        if ((scrollY + deltaY) == 0 || (scrollY - scrollRangeY + deltaY) == 0) {  
            newDeltaY = deltaY; // 回弹最后一次滚动，复位  
        } else {  
            newDeltaY = delta; // 增加阻尼效果  
        }  
  
        return super.overScrollBy(deltaX, newDeltaY, scrollX, scrollY,  
                scrollRangeX, scrollRangeY, maxOverScrollX, MAX_SCROLL,  
                isTouchEvent);  
    }
}
