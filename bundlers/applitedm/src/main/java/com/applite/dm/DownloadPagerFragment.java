package com.applite.dm;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.applite.common.Constant;
import com.applite.common.PagerSlidingTabStrip;
import com.mit.impl.ImplLog;


public class DownloadPagerFragment extends android.support.v4.app.Fragment {
    final static String TAG = "applite_dm";
    private ViewPager mViewPager;
    private PagerSlidingTabStrip mPagerSlidingTabStrip;
    private Activity mActivity;
    private boolean destoryView = false;


    public void onAttach(Activity activity) {
        ImplLog.d(TAG, "onAttach,"+this);
        super.onAttach(activity);
        mActivity = activity;
        hasOptionsMenu();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ImplLog.d(TAG, "onCreateView,"+this);
        destoryView = false;
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
    public void onDetach() {
        super.onDetach();
        ImplLog.d(TAG, "onDetach,"+this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        destoryView = true;
        ImplLog.d(TAG, "onDestroyView," + this + "," + destoryView);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        for (int i = 0;i < mViewPager.getAdapter().getCount();i++){
            Fragment f = (Fragment)mViewPager.getAdapter().instantiateItem(mViewPager,i);
            if (null != f){
                ft.remove(f);
            }
        }
        ft.commit();
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
        FragmentManager mFragmentManager ;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            this.mFragmentManager = fm;
            tabs[0] = R.string.dm_downloading;
            tabs[1] = R.string.dm_downloaded;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container,position,object);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fm = new DownloadListFragment();
            Bundle b = new Bundle();
            switch(tabs[position]){
                case R.string.dm_downloaded:
                    b.putInt("statusFilter",Constant.STATUS_SUCCESSFUL
                            | Constant.STATUS_INSTALLED
                            | Constant.STATUS_INSTALL_FAILED
                            | Constant.STATUS_PRIVATE_INSTALLING);
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

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {
            super.registerDataSetObserver(observer);
            ImplLog.d(TAG, "registerDataSetObserver,"+observer+","+this);
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            super.unregisterDataSetObserver(observer);
            ImplLog.d(TAG, "unregisterDataSetObserver,"+observer+","+this);
        }

        @Override
        public void finishUpdate(ViewGroup container) {
//            if (!destoryView) {
                super.finishUpdate(container);
//            }else{
//                mFragmentManager.beginTransaction().commit();
//            }
            ImplLog.d(TAG,"finishUpdate,"+destoryView);
        }
    }
}