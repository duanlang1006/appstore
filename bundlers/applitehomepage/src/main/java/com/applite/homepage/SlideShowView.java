package com.applite.homepage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

import net.tsz.afinal.FinalBitmap;

/**
 * ViewPager实现的轮播图广告自定义视图，如京东首页的广告轮播图效果；
 * 既支持自动轮播页面也支持手势滑动切换页面
 *
 *
 */

public class SlideShowView extends FrameLayout implements View.OnClickListener{
    //自动轮播启用开关
    private final static boolean isAutoPlay = true;

    //自定义轮播图的资源
    private String[] imageUrls;
    //放轮播图片的ImageView 的list
    private List<ImageView> imageViewsList;
    //放圆点的View的list
    private List<View> dotViewsList;

    private ViewPager viewPager;
    private MyPagerAdapter pagerAdapter;
    private LinearLayout dotLayout;
    //当前轮播页
    private int currentItem  = 0;
    //定时任务
    private ScheduledExecutorService scheduledExecutorService;

    private Context context;
    private LayoutInflater mInflater;
    private OnSlideViewClickListener mListener;
    private FinalBitmap mFinalBitmap;

    //Handler
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            viewPager.setCurrentItem(currentItem);
        }

    };

    public SlideShowView(Context context) {
        this(context,null);
        // TODO Auto-generated constructor stub
    }

    public SlideShowView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        // TODO Auto-generated constructor stub
    }

    public SlideShowView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        mInflater = LayoutInflater.from(context);
        mInflater = mInflater.cloneInContext(context);
        mFinalBitmap = FinalBitmap.create(this.context);

        mInflater.inflate(R.layout.slideshow, this, true);
        dotLayout = (LinearLayout)findViewById(R.id.dotLayout);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setFocusable(true);
        viewPager.setOnPageChangeListener(new MyPageChangeListener());

        this.imageViewsList = new ArrayList<ImageView>();
        this.dotViewsList = new ArrayList<View>();
    }

    @Override
    public void onClick(View v) {
        if (null != mListener){
            mListener.onClick(v,(int)v.getTag());
        }
    }

    public void setOnViewClickListener(OnSlideViewClickListener listener){
        mListener = listener;
    }

    public void setImageUrls(String[] imageUrls){
        stopPlay();
        this.imageUrls = imageUrls;
        if (null == this.imageUrls || this.imageUrls.length == 0){
            return;
        }
        this.imageViewsList.clear();
        this.dotViewsList.clear();
        this.dotLayout.removeAllViews();
        // 热点个数与图片特殊相等
        for (int i = 0; i < imageUrls.length; i++) {
            ImageView view =  new ImageView(context);
            view.setTag(i);
            view.setOnClickListener(this);
            if(i==0) {//给一个默认图
                view.setBackgroundResource(R.drawable.appmain_subject_1);
            }
            view.setScaleType(ScaleType.FIT_XY);
            imageViewsList.add(view);

            ImageView dotView =  new ImageView(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
            params.leftMargin = 4;
            params.rightMargin = 4;
            dotLayout.addView(dotView, params);
            dotViewsList.add(dotView);
        }
        if (null == pagerAdapter){
            pagerAdapter = new MyPagerAdapter();
            viewPager.setAdapter(pagerAdapter);
        }else {
            pagerAdapter.notifyDataSetChanged();
        }
        startPlay();
    }

    /**
     * 开始轮播图切换
     */
    public void startPlay(){
        if(isAutoPlay) {
            scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            scheduledExecutorService.scheduleAtFixedRate(new SlideShowTask(), 1, 4, TimeUnit.SECONDS);
        }
    }

    /**
     * 停止轮播图切换
     */
    public void stopPlay(){
        if (null != scheduledExecutorService) {
            scheduledExecutorService.shutdown();
            scheduledExecutorService = null;
        }
    }

    /**
     * 填充ViewPager的页面适配器
     *
     */
    private class MyPagerAdapter  extends PagerAdapter{
        private int childrenCount = 0;
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ImageView imageView = imageViewsList.get(position);
            mFinalBitmap.display(imageView,imageUrls[position]);
            container.addView(imageView);
            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View)object);
        }

        @Override
        public void finishUpdate(ViewGroup container) {
            super.finishUpdate(container);
        }

        @Override
        public int getCount() {
            return imageViewsList.size();
        }

        @Override
        public int getItemPosition(Object object) {
            if (childrenCount > 0 ){
                childrenCount --;
                return POSITION_NONE;
            }
            return super.getItemPosition(object);
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == o;
        }

        @Override
        public void notifyDataSetChanged() {
            childrenCount = getCount();
            super.notifyDataSetChanged();
        }
    }

    /**
     * ViewPager的监听器
     * 当ViewPager中页面的状态发生改变时调用
     *
     */
    private class MyPageChangeListener implements OnPageChangeListener{

        boolean isAutoPlay = false;

        @Override
        public void onPageScrollStateChanged(int arg0) {
            // TODO Auto-generated method stub
            switch (arg0) {
                case 1:// 手势滑动，空闲中
                    isAutoPlay = false;
                    break;
                case 2:// 界面切换中
                    isAutoPlay = true;
                    break;
                case 0:// 滑动结束，即切换完毕或者加载完毕
                    // 当前为最后一张，此时从右向左滑，则切换到第一张
                    if (viewPager.getCurrentItem() == viewPager.getAdapter().getCount() - 1 && !isAutoPlay) {
                        viewPager.setCurrentItem(0);
                    }
                    // 当前为第一张，此时从左向右滑，则切换到最后一张
                    else if (viewPager.getCurrentItem() == 0 && !isAutoPlay) {
                        viewPager.setCurrentItem(viewPager.getAdapter().getCount() - 1);
                    }
                    break;
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onPageSelected(int pos) {
            // TODO Auto-generated method stub

            currentItem = pos;
            for(int i=0;i < dotViewsList.size();i++){
                if(i == pos){
                    ((View)dotViewsList.get(pos)).setBackgroundResource(R.drawable.page_indicator_focused);
                }else {
                    ((View)dotViewsList.get(i)).setBackgroundResource(R.drawable.page_indicator_unfocused);
                }
            }
        }

    }

    /**
     *执行轮播图切换任务
     *
     */
    private class SlideShowTask implements Runnable{

        @Override
        public void run() {
            // TODO Auto-generated method stub
            synchronized (viewPager) {
                currentItem = (currentItem+1)%imageViewsList.size();
                handler.obtainMessage().sendToTarget();
            }
        }

    }

    /**
     * 销毁ImageView资源，回收内存
     *
     */
    private void destoryBitmaps() {

        for (int i = 0; i < imageViewsList.size(); i++) {
            ImageView imageView = imageViewsList.get(i);
            Drawable drawable = imageView.getDrawable();
            if (drawable != null) {
                //解除drawable对view的引用
                drawable.setCallback(null);
            }
        }
    }

    public interface OnSlideViewClickListener{
        public void onClick(View v,int position);
    }
}