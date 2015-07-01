package com.applite.data;

import android.app.Activity;
import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.applite.common.LogUtils;
import com.applite.homepage.HomePageListFragment;
import com.applite.bean.HomePageBean;
import com.applite.bean.HomePageTab;
import com.applite.bean.HomePageTypeBean;
import com.applite.homepage.BundleContextFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by yuzhimin on 6/18/15.
 */
public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
    private static final String TAG = "SectionsPagerAdapter";
    private int mChildCount = 0;
    private List<HomePageBean> mHomePageGoods = new ArrayList<HomePageBean>();
    private List<HomePageBean> mHomePageOrder = new ArrayList<HomePageBean>();
    private List<HomePageTypeBean> mHomePageMainType = new ArrayList<HomePageTypeBean>();
    private List<HomePageTab> mHPTabContents = new ArrayList<HomePageTab>();
    private Activity mActivity;
    public SectionsPagerAdapter(FragmentManager fm, Activity activity) {
        super(fm);
        this.mActivity = activity;
    }
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container,position,object);
    }
    public void setHomePageTab(List<HomePageTab> mHomePageTab){
        mHPTabContents = mHomePageTab;
    }
    public void setHomePageGoods(List<HomePageBean> mHomePageData){
        mHomePageGoods = mHomePageData;
    }

    public void setHomePageOrders(List<HomePageBean> mHomePageData){
        mHomePageOrder = mHomePageData;
    }
    public void setHomePageMainType(List<HomePageTypeBean> mHomePageData){
        mHomePageMainType = mHomePageData;
    }
    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        LogUtils.d(TAG,"getItem("+position+")");
        switch(position){
            case 0:
                LogUtils.i(TAG, "Table yuzm mHomePageGoods position :" + position);
                return new HomePageListFragment(mHomePageGoods,null, position, mActivity);
            case 1:
                LogUtils.i(TAG, "Table yuzm mHomePageOrder position :" + position);
                return new HomePageListFragment(mHomePageOrder,null, position, mActivity);
            case 2:
                LogUtils.i(TAG, "Table yuzm mHomePageMainType position :" + position);
                return new HomePageListFragment(null,mHomePageMainType, position, mActivity);
            default:
                return new HomePageListFragment(mHomePageGoods, mHomePageMainType,position, mActivity);
        }
    }

    @Override
    public int getCount() {
        return mHPTabContents.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Locale l = Locale.getDefault();
        Resources res = mActivity.getResources();
        try {
            res = BundleContextFactory.getInstance().getBundleContext().getBundleContext().getResources();
        }catch (Exception e){
            e.printStackTrace();
        }
        LogUtils.i(TAG, "getPageTitle Table yuzm TabtName :" + mHPTabContents.get(position).getName());
        return mHPTabContents.get(position).getName();
    }

    @Override
    public int getItemPosition(Object object) {
        LogUtils.d(TAG,"getItemPosition("+mChildCount+")");
        if (mChildCount > 0) {
            mChildCount --;
            return POSITION_NONE;
        }
        return super.getItemPosition(object);
    }

    @Override
    public void notifyDataSetChanged() {
        mChildCount = getCount();
        super.notifyDataSetChanged();
    }
}
