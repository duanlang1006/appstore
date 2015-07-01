package com.applite.data;

import android.app.Activity;
import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.applite.bean.HomePageDataBean;
import com.applite.common.LogUtils;
import com.applite.homepage.HomePageListFragment;
import com.applite.bean.HomePageBean;
import com.applite.bean.HomePageTab;
import com.applite.bean.HomePageTypeBean;
import com.applite.homepage.BundleContextFactory;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by yuzhimin on 6/18/15.
 */
public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
    private static final String TAG = "SectionsPagerAdapter";
    private int mChildCount = 0;
    private Activity mActivity;
    private HomePageDataBean mHomePageDataBean;

    public SectionsPagerAdapter(FragmentManager fm, HomePageDataBean mData, Activity activity) {
        super(fm);
        this.mHomePageDataBean = mData;
        this.mActivity = activity;
        LogUtils.i(TAG, "Gson yuzm mHomePageDataBean :"
                + mHomePageDataBean);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

    }

    public void setHomePageDataBean(HomePageDataBean mData) {
        this.mHomePageDataBean = mData;
        LogUtils.i(TAG, "Gson yuzm mHomePageDataBean :"
                + mHomePageDataBean);
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        return new HomePageListFragment(mHomePageDataBean.getSubjectData().get(position));
    }

    @Override
    public int getCount() {
        LogUtils.i(TAG, "getCount mHomePageDataBean : " + mHomePageDataBean);
        if (null != mHomePageDataBean)
            return mHomePageDataBean.getSubjectData().size();
        else
            return 0;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Locale l = Locale.getDefault();
        Resources res = mActivity.getResources();
        try {
            res = BundleContextFactory.getInstance().getBundleContext().getBundleContext().getResources();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //LogUtils.i(TAG, "getPageTitle Table yuzm TabtName :" + mHomePageDataBean.getSubjectData().get(position).getS_name());
        LogUtils.i(TAG, "getPageTitle mHomePageDataBean : " + mHomePageDataBean);
        if (null != mHomePageDataBean)
            return mHomePageDataBean.getSubjectData().get(position).getS_name();
        else
            return "";
    }
        @Override
        public void notifyDataSetChanged () {
            mChildCount = getCount();
            LogUtils.i(TAG, "getCount mChildCount : " + mChildCount);
            super.notifyDataSetChanged();
        }

}
