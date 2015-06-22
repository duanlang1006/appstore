package com.android.applite.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;


public abstract class PagedViewWithHeaderAndFooter extends PagedView {
    private static final String TAG ="PagedViewWithHeaderAndFooter";
    
    private final static int REFRESH_PULL = 0;          //下拉不超过一个图标位置状态
    private final static int REFRESH_PULL_OVER = 1;     //下拉超过了一个图标位置状态
    private final static int REFRESH_RELEASE = 2;       //松手刷新状态
    private final static int REFRESH_DOING = 3;         //刷新中
    private final static int REFRESH_NONE = 4;          //刷新完成
    
    private int mRefreshState = REFRESH_NONE;
    private RefreshView mHeaderView;
    private int mHeaderViewWidth;
    private RefreshView mFooterView;
    private int mFooterViewWidth;
    
    private int mTouchAction;
    

    public PagedViewWithHeaderAndFooter(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    private void measureView(View child) {
        ViewGroup.LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        int childHeightSpec,childWidthSpec;
        int lpWidth = p.width;
        if (lpWidth > 0){
            childWidthSpec = MeasureSpec.makeMeasureSpec(lpWidth,
                    MeasureSpec.EXACTLY);
        } else {
            childWidthSpec = MeasureSpec.makeMeasureSpec(0,
                    MeasureSpec.UNSPECIFIED);
        }
        
        int lpHeight = p.height;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight,
                    MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0,
                    MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }
    
    public void setHeaderView(RefreshView header){
        if (null == header){
            return;
        }
        mHeaderView = header;
        mHeaderView.setup();
        measureView(mHeaderView);
        mHeaderViewWidth = mHeaderView.getMeasuredWidth();
        mHeaderView.setPadding(-mHeaderViewWidth,0,0,0);
    }
    
    public void setFooterView(RefreshView footer){
        if (null == footer){
            return;
        }
        mFooterView = footer;
        mFooterView.setup();
        measureView(mFooterView);
        mFooterViewWidth = mFooterView.getMeasuredWidth();
        mFooterView.setPadding(0,0,-mFooterViewWidth,0); 
        
    }

    @Override
    public void scrollToImpl(int x, int y) {
        // TODO Auto-generated method stub
//        Log.d(TAG, "scrollToImpl("+x+","+y+")");
        int min =0;
        int max = mMaxScrollX;
        if (null != mHeaderView){
            min = min-(int)(mHeaderViewWidth*1.25);
        }
        if (null != mFooterView){
            max = max+(int)(mFooterViewWidth*1.25);
        }
        int deltaX = x - mUnboundedScrollX;
        
        mUnboundedScrollX = x;
        mOverScrollX = x;
        if (x < 0) {
            if (MotionEvent.ACTION_MOVE == mTouchAction){
                switch(mRefreshState){
                case REFRESH_NONE:
                    if (null != mHeaderView){
                        mRefreshState = REFRESH_PULL;
                        mHeaderView.refreshPull();
                    }
                    break;
                case REFRESH_PULL:
                    if (null != mHeaderView){
                        if (x < (0-mHeaderViewWidth)){
                            mRefreshState = REFRESH_PULL_OVER;
                            mHeaderView.refreshPullOver();
                        }else{
                            mRefreshState = REFRESH_PULL;
                        }
                    }
                    break;
                case REFRESH_PULL_OVER:
                    if (null != mHeaderView){
                        if (x < (0-mHeaderViewWidth)){
                            mRefreshState = REFRESH_PULL_OVER;
                        }else{
                            mRefreshState = REFRESH_PULL;
                            mHeaderView.refreshPull();
                        }
                    }
                    break;
                }
//                Log.d(TAG,"state="+mRefreshState);
            }

            if (x < min){
                super.scrollTo(min, y);
                if (null != mHeaderView){
                    mHeaderView.setPadding(-mHeaderViewWidth-min,0,0,0);
                }
            }else{
                super.scrollTo(x,y);
                if (null != mHeaderView){
                    mHeaderView.setPadding(-mHeaderViewWidth-x,0,0,0);
                }
            }
        } else if (x > mMaxScrollX) {
            if (MotionEvent.ACTION_MOVE == mTouchAction){
                switch(mRefreshState){
                case REFRESH_NONE:
                    if (null != mFooterView){
                        mRefreshState = REFRESH_PULL;
                        mFooterView.refreshPull();
                    }
                    break;
                case REFRESH_PULL:
                    if (null != mFooterView){
                        if (x > (mMaxScrollX+mFooterViewWidth)){
                            mRefreshState = REFRESH_PULL_OVER;
                            mFooterView.refreshPullOver();
                        }else{
                            mRefreshState = REFRESH_PULL;
                        }
                    }
                    break;
                case REFRESH_PULL_OVER:
                    if (null != mFooterView){
                        if (x > (mMaxScrollX+mFooterViewWidth)){
                            mRefreshState = REFRESH_PULL_OVER;
                        }else{
                            mRefreshState = REFRESH_PULL;
                            mFooterView.refreshPull();
                        }
                    }
                    break;
                }
//                Log.d(TAG,"state="+mRefreshState);
            }
            
            if (x>max){
                super.scrollTo(max, y);
                if (null != mFooterView){
//                    Log.d(TAG, "mFooterView.setPadding("+(-mFooterViewWidth-(max-mMaxScrollX))+")");
                    mFooterView.setPadding(0,0,-mFooterViewWidth+(max-mMaxScrollX),0);
                }
            }else{
                super.scrollTo(x, y);
                if (null != mFooterView){
//                  Log.d(TAG, "mFooterView.setPadding("+(-mFooterViewWidth-(x-mMaxScrollX))+")");
                    mFooterView.setPadding(0,0,-mFooterViewWidth+(x-mMaxScrollX),0);
                }
            }
        } else {
            super.scrollTo(x, y);
            if (null != mFooterView && mFooterView.getPaddingRight()>(-mFooterViewWidth)){
                mFooterView.setPadding(0, 0, -mFooterViewWidth, 0);
            }
            
            if (null != mHeaderView && mHeaderView.getPaddingLeft()>(-mHeaderViewWidth)){
                mHeaderView.setPadding(-mHeaderViewWidth, 0, 0, 0);
            }
        }

        mTouchX = x;
        mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
    }

    @Override
    protected void onTouchActionDown(MotionEvent ev) {
        // TODO Auto-generated method stub
        mTouchAction = MotionEvent.ACTION_DOWN;
        if (REFRESH_RELEASE == mRefreshState || REFRESH_DOING == mRefreshState){
            return;
        }
        super.onTouchActionDown(ev);
    }

    @Override
    protected void onTouchActionMove(MotionEvent ev) {
        // TODO Auto-generated method stub
        mTouchAction = MotionEvent.ACTION_MOVE;
        if (REFRESH_RELEASE == mRefreshState || REFRESH_DOING == mRefreshState){
            return;
        }
        super.onTouchActionMove(ev);
    }

    @Override
    protected void onTouchActionUp(MotionEvent ev) {
        // TODO Auto-generated method stub
        mTouchAction = MotionEvent.ACTION_UP;
        
        switch(mRefreshState){
        case REFRESH_PULL:
            mRefreshState = REFRESH_NONE;
            break;
        case REFRESH_PULL_OVER:
            mRefreshState = REFRESH_RELEASE;
            break;
        case REFRESH_NONE:
        default:
            break;
        }
         super.onTouchActionUp(ev);
    }
    
//    @Override
//    protected void snapToDestination() {
//        // TODO Auto-generated method stub
//        if (REFRESH_RELEASE == mRefreshState || REFRESH_DOING == mRefreshState){
//            return;
//        }
//        super.snapToDestination();
//    }
//
//    @Override
//    protected void snapToPageWithVelocity(int whichPage, int velocity) {
//        // TODO Auto-generated method stub
//        if (REFRESH_RELEASE == mRefreshState || REFRESH_DOING == mRefreshState){
//            return;
//        }
//        super.snapToPageWithVelocity(whichPage, velocity);
//    }

    public void onStartRefresh(){

    }
    
    public void onRefreshing(){
        mRefreshState = REFRESH_DOING;
        if (null != mHeaderView){
            mHeaderView.refreshing();
        }
        if (null != mFooterView){
            mFooterView.refreshing();
        }
    }

    public void onFinishRefresh(){
        mRefreshState = REFRESH_NONE;
        if (null != mHeaderView){
            mHeaderView.refreshReset();
        }
        if (null != mFooterView){
            mFooterView.refreshReset();
        }
        snapToDestination();
        Log.d(TAG,"onFinishRefresh");
    }

    @Override
    protected void onPageBeginMoving() {
        // TODO Auto-generated method stub
        super.onPageBeginMoving();
    }

    @Override
    protected void onPageEndMoving() {
        // TODO Auto-generated method stub
        super.onPageEndMoving();
        if (REFRESH_RELEASE == mRefreshState){
            startRefresh();
        }
        mRefreshState = REFRESH_NONE;         
    }

    protected abstract void startRefresh();
}
