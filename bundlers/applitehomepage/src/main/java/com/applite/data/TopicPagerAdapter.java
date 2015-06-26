package com.applite.data;

import android.content.Context;
import android.graphics.Color;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.applite.homepage.BundleContextFactory;
import com.applite.homepage.R;
import com.applite.utils.HomePageUtils;

import java.util.ArrayList;

/**
 * Created by yuzhimin on 6/25/15.
 */
public class TopicPagerAdapter extends PagerAdapter{
    ImageView leftImg;
    ImageView rightImg;
    Context mContext = null;
    private static final String TAG = "TopicPagerAdapter";
    private ArrayList<ImageView> mNewsImages;  //上方viewpager的图片
    public TopicPagerAdapter(Context context){
        mNewsImages = new ArrayList<ImageView>();
        try {
            Context mContextBundle = BundleContextFactory.getInstance().getBundleContext().getBundleContext();
            if (null != context) {
                //inflater = LayoutInflater.from(mContext);
                //inflater = inflater.cloneInContext(mContext);
                mContext = mContextBundle;
            }else{
                mContext =context;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        HomePageUtils.i(TAG, "isViewFromObject yuzm mContext : " + mContext);
        leftImg = new ImageView(mContext);
        leftImg.setBackgroundColor(Color.WHITE);
        leftImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
        leftImg.setImageResource(R.drawable.item01);
        //	photoFront.setOnClickListener(new MyOnClickListener(0));

        rightImg = new ImageView(mContext);

        rightImg.setBackgroundColor(Color.WHITE);

        rightImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
        rightImg.setImageResource(R.drawable.item02);
        //videoFront.setOnClickListener(new MyOnClickListener(1));
        mNewsImages.add(leftImg);
        mNewsImages.add(rightImg);
        HomePageUtils.i(TAG, "TopicPagerAdapter yuzm mContext : " + mContext);
    }
    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {

        return arg0 == arg1;

    }

    @Override
    public int getItemPosition(Object object) {
        // TODO Auto-generated method stub
        //		return super.getItemPosition(object);
        return POSITION_NONE;
    }
    @Override
    public int getCount() {
        HomePageUtils.i(TAG, "isViewFromObject yuzm mNewsImages : " + mNewsImages);
        if (null != mNewsImages) {
            return mNewsImages.size();
        }else {
            return 0;
        }

    }


    @Override
    public Parcelable saveState() {
        return null;
    }

    @Override
    public void restoreState(Parcelable arg0, ClassLoader arg1) {
    }

    @Override
    public void startUpdate(View arg0) {
    }

    @Override
    public void finishUpdate(View arg0) {
    }

    @Override
    public void destroyItem(View container, int position, Object object) {

        ((ViewPager) container).removeView(mNewsImages.get(position));

    }

    @Override
    public Object instantiateItem(View container, int position) {

        ((ViewPager) container).addView(mNewsImages.get(position));

        return mNewsImages.get(position);

    }
}
