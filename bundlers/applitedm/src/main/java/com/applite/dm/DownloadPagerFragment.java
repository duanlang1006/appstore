package com.applite.dm;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.applite.common.Constant;
import com.applite.common.PagerSlidingTabStrip;


public class DownloadPagerFragment extends android.support.v4.app.Fragment {
    final static String TAG = "applite_dm";
    private ViewPager mViewPager;
    private PagerSlidingTabStrip mPagerSlidingTabStrip;
    private Activity mActivity;


    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        hasOptionsMenu();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LayoutInflater mInflater = inflater;
        try {
            Context context = BundleContextFactory.getInstance().getBundleContext().getBundleContext();
            if (null != context) {
                mInflater = LayoutInflater.from(context);
                mInflater = mInflater.cloneInContext(context);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        View rootView = mInflater.inflate(R.layout.fragment_download_pager, container, false);
        mViewPager = (ViewPager) rootView.findViewById(R.id.pager);
        mViewPager.setAdapter(new SectionsPagerAdapter(this.getFragmentManager()));
//        mPagerSlidingTabStrip = (PagerSlidingTabStrip)inflater.inflate(R.layout.pager_sliding_tab,container,false);
        mPagerSlidingTabStrip = PagerSlidingTabStrip.inflate(mActivity,container,false);
        mPagerSlidingTabStrip.setViewPager(mViewPager);
//        mPagerSlidingTabStrip.setOnPageChangeListener(mPageChangeListener);
        initActionBar(mPagerSlidingTabStrip);
        return rootView;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                ((FragmentActivity)mActivity).getSupportFragmentManager().popBackStack();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initActionBar(View customView){
        try {
            ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(customView);
            actionBar.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
        int[] tabs = new int[2];

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            tabs[0] = R.string.dm_downloading;
            tabs[1] = R.string.dm_downloaded;
        }
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

        }
        @Override
        public Fragment getItem(int position) {
            Fragment fm = new DownloadListFragment();
            Bundle b = new Bundle();
            switch(tabs[position]){
                case R.string.dm_downloaded:
                    b.putInt("statusFilter",Constant.STATUS_SUCCESSFUL
                           /* | Constant.STATUS_INSTALLED
                            | Constant.STATUS_INSTALL_FAILED
                            | Constant.STATUS_PRIVATE_INSTALLING*/);
                    break;
                case R.string.dm_downloading:
                    b.putInt("statusFilter",Constant.STATUS_PENDING
                            | Constant.STATUS_RUNNING
                            | Constant.STATUS_PAUSED
                            | Constant.STATUS_FAILED);
                    break;
            }
            fm.setArguments(b);
            return fm;
        }

        @Override
        public int getCount() {
            return tabs.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Resources res = getResources();
            try {
                res = BundleContextFactory.getInstance().getBundleContext().getBundleContext().getResources();
            }catch (Exception e){
                e.printStackTrace();
            }
            return res.getString(tabs[position]);
        }
    }
}