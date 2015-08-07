package com.applite.dm;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.internal.view.SupportMenu;
import android.support.v4.internal.view.SupportMenuItem;
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
import com.applite.common.PagerSlidingTabStrip;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplLog;
import com.osgi.extra.OSGIBaseFragment;
import com.osgi.extra.OSGIServiceHost;

import java.lang.reflect.Field;


public class DownloadPagerFragment extends OSGIBaseFragment implements View.OnClickListener {
    final static String TAG = "applite_dm";
    private ViewPager mViewPager;
    private PagerSlidingTabStrip mPagerSlidingTabStrip;
    private Activity mActivity;
    private boolean destoryView = false;
    private LayoutInflater mInflater;
    private Context mPlugContext;

    public static OSGIBaseFragment newInstance(Fragment fg,Bundle params){
        return new DownloadPagerFragment(fg,params);
    }

    private DownloadPagerFragment(Fragment mFragment, Bundle params) {
        super(mFragment, params);
    }

    public void onAttach(Activity activity) {
        ImplLog.d(TAG, "onAttach," + this);
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ImplLog.d(TAG, "onCreateView,"+this);
        destoryView = false;
        mInflater = inflater;
        try {
            mPlugContext = BundleContextFactory.getInstance().getBundleContext().getBundleContext();
            if (null != mPlugContext) {
                mInflater = LayoutInflater.from(mPlugContext);
                mInflater = mInflater.cloneInContext(mPlugContext);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        View rootView = mInflater.inflate(R.layout.fragment_download_pager, container, false);
        mViewPager = (ViewPager) rootView.findViewById(R.id.pager);
        mViewPager.setAdapter(new SectionsPagerAdapter(getChildFragmentManager()));
        mPagerSlidingTabStrip = PagerSlidingTabStrip.inflate(mActivity,container,false);
//        mPagerSlidingTabStrip.setOnPageChangeListener(mPageChangeListener);
        initActionBar(mPagerSlidingTabStrip);
        mPagerSlidingTabStrip.setViewPager(mViewPager);
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
        try {
            Context plugContext = BundleContextFactory.getInstance().getBundleContext().getBundleContext();
            Field field = inflater.getClass().getDeclaredField("mContext");
            field.setAccessible(true);
            Object orgContext = field.get(inflater);
            field.set(inflater,plugContext);
            inflater.inflate(R.menu.menu_main,menu);
            field.set(inflater,orgContext);

            field = menu.getClass().getDeclaredField("mContext");
            field.setAccessible(true);
            field.set(menu,plugContext);

            field = menu.getClass().getDeclaredField("mResources");
            field.setAccessible(true);
            field.set(menu,plugContext.getResources());

            if (menu instanceof SupportMenu){
                SupportMenuItem item = (SupportMenuItem)menu.findItem(R.id.action_search);
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
//            case R.id.action_search:
//                HostUtils.launchSearchFragment((OSGIServiceHost) getActivity());
//                return true;
            case android.R.id.home:
                if (null != getFragmentManager().getFragments() && getFragmentManager().getFragments().size() > 1) {
                    getFragmentManager().popBackStack();
                }else{
                    getActivity().finish();
                }
                return true;
            case R.id.dm_action_pause_all:
//                ImplAgent.getInstance(mActivity).pauseDownload();
                return true;
            case R.id.dm_action_resume_all:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        FragmentManager fgm = getFragmentManager();
        switch(v.getId()){
            case R.id.action_back:
                if (null != fgm.getFragments() && fgm.getFragments().size() > 0) {
                    fgm.popBackStack();
                }else{
                    getActivity().finish();
                }
                break;
            case R.id.action_more:
                break;
        }

    }

    private void initActionBar(View tabStrip){
        try {
            Resources res = mPlugContext.getResources();
            ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
//            actionBar.setBackgroundDrawable(res.getDrawable(R.drawable.action_bar_bg_light));
            actionBar.setHomeButtonEnabled(true);
            actionBar.setHomeAsUpIndicator(res.getDrawable(R.drawable.action_bar_back_light));
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
            if (null != host) {
                switch (tabs[position]) {
                    case R.string.dm_downloaded:
                        fg = host.newFragment(
                                BundleContextFactory.getInstance().getBundleContext(),
                                Constant.OSGI_SERVICE_DM_FRAGMENT,
                                DownloadListFragment.class.getName(),
                                DownloadListFragment.newBundle(Constant.STATUS_SUCCESSFUL
                                        | Constant.STATUS_INSTALLED
                                        | Constant.STATUS_INSTALL_FAILED
                                        | Constant.STATUS_PRIVATE_INSTALLING
                                        | Constant.STATUS_UPGRADE));
                        break;
                    case R.string.dm_downloading:
                        fg = host.newFragment(
                                BundleContextFactory.getInstance().getBundleContext(),
                                Constant.OSGI_SERVICE_DM_FRAGMENT,
                                DownloadListFragment.class.getName(),
                                DownloadListFragment.newBundle( Constant.STATUS_PENDING
                                        | Constant.STATUS_RUNNING
                                        | Constant.STATUS_PAUSED
                                        | Constant.STATUS_FAILED));
                        break;
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
            Resources res = getActivity().getResources();
            try {
                res = BundleContextFactory.getInstance().getBundleContext().getBundleContext().getResources();
            }catch (Exception e){
                e.printStackTrace();
            }
            return res.getString(tabs[position]);
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
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