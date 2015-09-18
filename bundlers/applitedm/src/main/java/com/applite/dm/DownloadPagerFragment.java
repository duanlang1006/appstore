package com.applite.dm;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.applite.common.Constant;
import com.applite.common.LogUtils;
import com.applite.common.PagerSlidingTabStrip;
import com.applite.sharedpreferences.AppliteSPUtils;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplInfo;
import com.mit.impl.ImplLog;
import com.osgi.extra.OSGIBaseFragment;
import com.osgi.extra.OSGIServiceHost;

import java.util.Observable;
import java.util.Observer;


public class DownloadPagerFragment extends OSGIBaseFragment implements View.OnClickListener, Observer, ViewPager.OnPageChangeListener {
    final static String TAG = "applite_dm";
    private ViewPager mViewPager;
    private SectionsPagerAdapter mViewPagerAdapter;
    private PagerSlidingTabStrip mPagerSlidingTabStrip;
    private boolean destoryView = false;
    private LayoutInflater mInflater;

    private WindowManager.LayoutParams lpTop;
    private WindowManager managerTop;
    private View titleBar;//长按时覆盖ActionBar的控件
    private Button btnCancel;
    private Button btnAllpick;
    private TextView tvShowTotal;

    private LinearLayout layout_button;//盛放两个按钮的布局
    private Button btnDelete = null;
    private Animation animaBtDel;
    private String temp = null;

    private String COUNT_DOWNLOADING = "count downloading";
    private String COUNT_DOWNLOADED = "count downloaded";
    private String FLAG = "flag";
    private String LENGTH = "length";
    private String POSITION = "position";
    private int prePosition = -1;

    public DownloadPagerFragment() {
        super();
    }

    private SharedPreferences.OnSharedPreferenceChangeListener mPagerListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(FLAG)) {
                if ((boolean) AppliteSPUtils.get(mActivity, FLAG, false)) {
                    show();
                } else {
                    hide();
                }
            } else if (key.equals(COUNT_DOWNLOADED) || key.equals(COUNT_DOWNLOADING)) {

                setButtonStatus();
            }
        }


    };

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
        ImplLog.d(TAG, "onCreateView," + this);
        destoryView = false;
        mInflater = inflater;
        View rootView = mInflater.inflate(R.layout.fragment_download_pager, container, false);
        mViewPager = (ViewPager) rootView.findViewById(R.id.pager);
        mViewPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());
        mViewPager.setAdapter(mViewPagerAdapter);
        mPagerSlidingTabStrip = PagerSlidingTabStrip.inflate(mActivity, container, false);
        initActionBar(mPagerSlidingTabStrip);
        mPagerSlidingTabStrip.setViewPager(mViewPager);
        mPagerSlidingTabStrip.setOnPageChangeListener(this);
        ImplAgent.getInstance(mActivity).addObserver(this);
        titleBar = inflater.inflate(R.layout.cover_actionbar, null);//这里是添加的控件
        initializeView(rootView);
        layout_button = (LinearLayout) rootView.findViewById(R.id.layout_button);
        btnDelete = (Button) rootView.findViewById(R.id.btnDelete);
        animaBtDel = AnimationUtils.loadAnimation(mActivity, R.anim.btn_delete_in);
        btnDelete.setOnClickListener(this);

        AppliteSPUtils.registerChangeListener(mActivity, mPagerListener);
        AppliteSPUtils.put(mActivity, COUNT_DOWNLOADING, 0);
        AppliteSPUtils.put(mActivity, COUNT_DOWNLOADED, 0);

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
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (null != managerTop) {
            managerTop.removeView(titleBar);
        }
        destoryView = true;
        ImplLog.d(TAG, "onDestroyView," + this + "," + destoryView);
        ImplAgent.getInstance(mActivity).deleteObserver(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        PagerAdapter adapter = mViewPager.getAdapter();
//        if (null != adapter) {
//            FragmentTransaction ft = getFragmentManager().beginTransaction();
//            for (int i = 0; i < mViewPager.getAdapter().getCount(); i++) {
//                Fragment f = (Fragment) mViewPager.getAdapter().instantiateItem(mViewPager, i);
//                if (null != f) {
//                    ft.remove(f);
//                }
//            }
//            ft.commit();
//        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main_dm, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        if (null != item) {
            item.setVisible(false);
        }
        MenuItem item_dm = menu.findItem(R.id.action_dm);
        if (null != item_dm) {
            item_dm.setVisible(false);
        }
        if (prePosition == 1) {
            menu.findItem(R.id.dm_action_pause_all).setEnabled(false);
            menu.findItem(R.id.dm_action_resume_all).setEnabled(false);
        } else {
            menu.findItem(R.id.dm_action_pause_all).setEnabled(true);
            menu.findItem(R.id.dm_action_resume_all).setEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.dm_action_pause_all) {
            ImplAgent.getInstance(mActivity.getApplicationContext()).pauseAll();
            return true;
        } else if (item.getItemId() == R.id.dm_action_resume_all) {
            ImplAgent.getInstance(mActivity.getApplicationContext()).resumeAll();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            initActionBar(mPagerSlidingTabStrip);
            ImplAgent.getInstance(mActivity).addObserver(this);
        } else {
            ImplAgent.getInstance(mActivity).deleteObserver(this);
        }
    }

    @Override
    public void onClick(View v) {
        FragmentManager fgm = getFragmentManager();
        if (v.getId() == R.id.action_back) {
            if (null != fgm.getFragments() && fgm.getFragments().size() > 0) {
                fgm.popBackStack();
            } else {
                mActivity.finish();
            }
        } else if (v.getId() == R.id.action_more) {

        } else if (v.getId() == R.id.btnDelete) {//删除
            int totalDelete = ((int) AppliteSPUtils.get(mActivity, COUNT_DOWNLOADING, 0) + (int) AppliteSPUtils.get(mActivity, COUNT_DOWNLOADED, 0));
            for (int i = 0; i < mViewPagerAdapter.getCount(); i++) {
                IDownloadOperator operator = (IDownloadOperator) mViewPagerAdapter.instantiateItem(mViewPager, i);
                if (null != operator) {
                    operator.onClickDelete();
                }
            }
            IDownloadOperator operator = (IDownloadOperator) mViewPagerAdapter.instantiateItem(mViewPager, prePosition);
            if (0 == (int) AppliteSPUtils.get(mActivity, COUNT_DOWNLOADING, 0) && 0 == (int) AppliteSPUtils.get(mActivity, COUNT_DOWNLOADED, 0)) {
                hide();
                operator.resetFlag();
            }
            Toast.makeText(mActivity.getApplicationContext(), mActivity.getResources().getString(R.string.delete_message1)
                            + totalDelete + mActivity.getResources().getString(R.string.delete_message2),
                    Toast.LENGTH_SHORT).show();
        } else if (v.getId() == R.id.select_allpick) {
            IDownloadOperator operator = (IDownloadOperator) mViewPagerAdapter.instantiateItem(mViewPager, prePosition);
            if (null != operator) {
                if ((int) AppliteSPUtils.get(mActivity, LENGTH, 0) == count()) {//全选=>全不选
                    operator.onClickDeselectAll();
                } else {
                    operator.onClickSeleteAll();
                }
            }
        } else if (v.getId() == R.id.select_cancel) {//取消
            hide();
            IDownloadOperator operator = (IDownloadOperator) mViewPagerAdapter.instantiateItem(mViewPager, prePosition);
            operator.resetFlag();

            Toast.makeText(mActivity.getApplicationContext(), R.string.cancel_operator, Toast.LENGTH_SHORT).show();
        }
    }

    private void show() {
        if (titleBar.getVisibility() != View.VISIBLE) {
            titleBar.setVisibility(View.VISIBLE);//显示titleBar
            layout_button.setVisibility(View.VISIBLE);//底部的布局及按钮
            btnDelete.setVisibility(View.VISIBLE);
            btnDelete.startAnimation(animaBtDel);
        }
    }

    private void hide() {
        titleBar.setVisibility(View.GONE);
        btnDelete.setVisibility(View.GONE);
        layout_button.setVisibility(View.GONE);
    }

    @Override
    public void update(Observable observable, Object data) {
        if (null == mViewPager || null == mViewPager.getAdapter()) {
            return;
        }
        mViewPager.getAdapter().notifyDataSetChanged();
    }

    private void initActionBar(View tabStrip) {
        try {
            ActionBar actionBar = ((ActionBarActivity) mActivity).getSupportActionBar();
//            actionBar.setBackgroundDrawable(res.getDrawable(R.drawable.action_bar_bg_light));
            actionBar.setDisplayUseLogoEnabled(false);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            actionBar.setCustomView(tabStrip);
            actionBar.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeView(View view) {
        if (null == lpTop) {
            lpTop = new WindowManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    mActivity.getResources().getDimensionPixelSize(R.dimen.abc_action_bar_default_height_material),
                    WindowManager.LayoutParams.TYPE_APPLICATION,
                    // 设置为无焦点状态
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, // 没有边界
                    // 半透明效果
                    PixelFormat.TRANSLUCENT);
            lpTop.gravity = Gravity.TOP;
            lpTop.windowAnimations = R.style.anim_view_top;
            managerTop = (WindowManager) mActivity.getSystemService(Context.WINDOW_SERVICE);
            managerTop.addView(titleBar, lpTop);

            titleBar.setVisibility(View.GONE);
            btnCancel = (Button) titleBar.findViewById(R.id.select_cancel);
            tvShowTotal = (TextView) titleBar.findViewById(R.id.total);
            btnAllpick = (Button) titleBar.findViewById(R.id.select_allpick);

            layout_button = (LinearLayout) view.findViewById(R.id.layout_button);
            btnDelete = (Button) view.findViewById(R.id.btnDelete);
            animaBtDel = AnimationUtils.loadAnimation(mActivity, R.anim.btn_delete_in);
        }
        btnCancel.setOnClickListener(this);
        btnAllpick.setOnClickListener(this);
        btnDelete.setOnClickListener(this);
    }

    private void setButtonStatus() {

        //textView
        temp = mActivity.getResources().getString(R.string.choose_message1) +
                (int) AppliteSPUtils.get(mActivity, COUNT_DOWNLOADING, 0) + " | " + (int) (AppliteSPUtils.get(mActivity, COUNT_DOWNLOADED, 0)) +
                mActivity.getResources().getString(R.string.choose_message2);
        tvShowTotal.setText(temp);

        //全选按钮
        if ((int) AppliteSPUtils.get(mActivity, LENGTH, 0) == count()) {//全不选
            btnAllpick.setText(R.string.nonepick_btn);
        } else {//其他状态
            btnAllpick.setText(R.string.allpick_btn);
        }

        //删除按钮
        if (0 == (int) AppliteSPUtils.get(mActivity, COUNT_DOWNLOADING, 0) + (int) AppliteSPUtils.get(mActivity, COUNT_DOWNLOADED, 0)) {
            btnDelete.setFocusable(false);
            btnDelete.setTextAppearance(mActivity, R.style.DownloadListEmpty0);
        } else {
            btnDelete.setFocusable(true);
            btnDelete.setTextAppearance(mActivity, R.style.DownloadOptButton);
        }

    }

    //当前选中项目数
    private int count() {
        if (0 == prePosition) {
            return (int) AppliteSPUtils.get(mActivity, COUNT_DOWNLOADING, 0);
        } else if (1 == prePosition) {
            return (int) AppliteSPUtils.get(mActivity, COUNT_DOWNLOADED, 0);
        } else {
            return 0;
        }
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {
        if (prePosition != i) {
            prePosition = i;
            if (0 == prePosition) {
                AppliteSPUtils.put(mActivity, POSITION, R.string.dm_downloading);
            } else if (1 == prePosition) {
                AppliteSPUtils.put(mActivity, POSITION, R.string.dm_downloaded);
            }
            setButtonStatus();
        }
    }

    @Override
    public void onPageSelected(int i) {
    }

    @Override
    public void onPageScrollStateChanged(int i) {
    }


    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
        int[] tabs = new int[2];
        int mChildCount = 0;
        FragmentManager mFragmentManager;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            this.mFragmentManager = fm;
            tabs[0] = R.string.dm_downloading;
            tabs[1] = R.string.dm_downloaded;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
        }

        @Override
        public Fragment getItem(int position) {
            LogUtils.d(TAG, "SectionsPagerAdapter,getItem(" + position + ")");
            DownloadListFragment fg = null;
            OSGIServiceHost host = (OSGIServiceHost) mActivity;
            int downloadFlag = ImplInfo.STATUS_PENDING | ImplInfo.STATUS_RUNNING | ImplInfo.STATUS_PAUSED
                    | ImplInfo.STATUS_FAILED | ImplInfo.STATUS_PACKAGE_INVALID;
            if (null != host) {
                if (R.string.dm_downloaded == tabs[position]) {
                    fg = (DownloadListFragment) host.newFragment(
                            Constant.OSGI_SERVICE_DM_FRAGMENT,
                            DownloadListFragment.class.getName(),
                            DownloadListFragment.newBundle(R.string.dm_downloaded, ~downloadFlag));

                } else if (R.string.dm_downloading == tabs[position]) {
                    fg = (DownloadListFragment) host.newFragment(
                            Constant.OSGI_SERVICE_DM_FRAGMENT,
                            DownloadListFragment.class.getName(),
                            DownloadListFragment.newBundle(R.string.dm_downloading, downloadFlag));
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
            } catch (Exception e) {
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
            if (mChildCount > 0) {
                mChildCount--;
                return POSITION_NONE;
            }
            return super.getItemPosition(object);
        }
    }

    public interface IDownloadOperator {
        public void onClickDelete();

        public void onClickSeleteAll();

        public void onClickDeselectAll();

        public void resetFlag();
    }
}