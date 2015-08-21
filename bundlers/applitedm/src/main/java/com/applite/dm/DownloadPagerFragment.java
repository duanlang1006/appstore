package com.applite.dm;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.applite.common.Constant;
import com.applite.common.LogUtils;
import com.applite.common.PagerSlidingTabStrip;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplLog;
import com.osgi.extra.OSGIBaseFragment;
import com.osgi.extra.OSGIServiceHost;
import java.util.Observable;
import java.util.Observer;


public class DownloadPagerFragment extends OSGIBaseFragment implements View.OnClickListener,Observer {
    final static String TAG = "applite_dm";
    private ViewPager mViewPager;
    private PagerSlidingTabStrip mPagerSlidingTabStrip;
    private boolean destoryView = false;
    private LayoutInflater mInflater;

    public DownloadPagerFragment() {
        super();
    }

    public void onAttach(Activity activity) {
        ImplLog.d(TAG, "onAttach," + this);
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ImplLog.d(TAG, "onCreateView,"+this);
        destoryView = false;
        mInflater = inflater;
        View rootView = mInflater.inflate(R.layout.fragment_download_pager, container, false);
        mViewPager = (ViewPager) rootView.findViewById(R.id.pager);
        mViewPager.setAdapter(new SectionsPagerAdapter(getChildFragmentManager()));
        mPagerSlidingTabStrip = PagerSlidingTabStrip.inflate(mActivity,container,false);
//        mPagerSlidingTabStrip.setOnPageChangeListener(mPageChangeListener);
        initActionBar(mPagerSlidingTabStrip);
        mPagerSlidingTabStrip.setViewPager(mViewPager);
        ImplAgent.getInstance(mActivity).addObserver(this);
        return rootView;
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ImplLog.d(TAG, "onDetach," + this);

    }

    @Override
    public void onResume() {
        super.onResume();
//        getView().setFocusableInTouchMode(true);
//        getView().requestFocus();
//        getView().setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
//                    // handle back button
//                    getFragmentManager().popBackStackImmediate();
//                    return true;
//                }
//                return false;
//            }
//        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        destoryView = true;
        ImplLog.d(TAG, "onDestroyView," + this + "," + destoryView);
        ImplAgent.getInstance(mActivity).deleteObserver(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PagerAdapter adapter = mViewPager.getAdapter();
        if (null != adapter) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            for (int i = 0; i < mViewPager.getAdapter().getCount(); i++) {
                Fragment f = (Fragment) mViewPager.getAdapter().instantiateItem(mViewPager, i);
                if (null != f) {
                    ft.remove(f);
                }
            }
            ft.commit();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main_dm,menu);
        MenuItem item = menu.findItem(R.id.action_search);
        if (null != item){
            item.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.dm_action_pause_all) {
            ImplAgent.getInstance(mActivity.getApplicationContext()).pauseAll();
            return true;
        }else if (item.getItemId() == R.id.dm_action_resume_all){
            ImplAgent.getInstance(mActivity.getApplicationContext()).resumeAll();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden){
            initActionBar(mPagerSlidingTabStrip);
            ImplAgent.getInstance(mActivity).addObserver(this);
        }else{
            ImplAgent.getInstance(mActivity).deleteObserver(this);
        }
    }

    @Override
    public void onClick(View v) {
        FragmentManager fgm = getFragmentManager();
        if(v.getId() == R.id.action_back) {
            if (null != fgm.getFragments() && fgm.getFragments().size() > 0) {
                fgm.popBackStack();
            } else {
                mActivity.finish();
            }
        }else if (v.getId() == R.id.action_more){

        }

    }

    @Override
    public void update(Observable observable, Object data) {
        if (null == mViewPager || null == mViewPager.getAdapter()) {
            return;
        }
        LogUtils.d(TAG,"update");
        mViewPager.getAdapter().notifyDataSetChanged();
    }

    private void initActionBar(View tabStrip){
        try {
            ActionBar actionBar = ((ActionBarActivity) mActivity).getSupportActionBar();
//            actionBar.setBackgroundDrawable(res.getDrawable(R.drawable.action_bar_bg_light));
            actionBar.setDisplayUseLogoEnabled(false);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(tabStrip);
            actionBar.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
        int[] tabs = new int[2];
        int mChildCount = 0;
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
            Fragment fg = null;
            OSGIServiceHost host = (OSGIServiceHost)mActivity;
            int downloadFlag = Constant.STATUS_PENDING | Constant.STATUS_RUNNING | Constant.STATUS_PAUSED | Constant.STATUS_FAILED;
            if (null != host) {
                if (R.string.dm_downloaded == tabs[position]) {
                    fg = host.newFragment(
                            Constant.OSGI_SERVICE_DM_FRAGMENT,
                            DownloadListFragment.class.getName(),
                            DownloadListFragment.newBundle(~downloadFlag));
                }else if (R.string.dm_downloading == tabs[position]){
                    fg = host.newFragment(
                            Constant.OSGI_SERVICE_DM_FRAGMENT,
                            DownloadListFragment.class.getName(),
                            DownloadListFragment.newBundle( downloadFlag ));
                }
            }
            return fg;
        }

        @Override
        public int getCount() {
            return tabs.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Resources res = mActivity.getResources();
            try {
                res = mActivity.getResources();
            }catch (Exception e){
                e.printStackTrace();
            }
            return res.getString(tabs[position]);
        }

        @Override
        public void notifyDataSetChanged() {
            mChildCount = getCount();
            super.notifyDataSetChanged();
        }

        @Override
        public int getItemPosition(Object object) {
            if (mChildCount > 0){
                mChildCount -- ;
                return POSITION_NONE;
            }
            return super.getItemPosition(object);
        }
    }
}