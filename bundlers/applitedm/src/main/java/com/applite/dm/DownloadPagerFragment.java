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
import com.applite.common.DefaultValue;
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


public class DownloadPagerFragment extends OSGIBaseFragment implements View.OnClickListener, Observer {
    final static String TAG = "applite_dm";
    private ViewPager mViewPager;
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
    //    private Button btnShare = null;
    //    private Animation animaBt1;
    private Animation animaBtDel;
    private String temp = null;

    private String COUNT = "count";
    private String FLAG = "flag";
    private String LENGTH = "length";
    private String STATUS = "status";
    private String DELETEBTNPRESSED = "deleteBtnPressed";


    public DownloadPagerFragment() {
        super();
    }

    private SharedPreferences.OnSharedPreferenceChangeListener mPagerListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if ((boolean) AppliteSPUtils.get(mActivity, FLAG, DefaultValue.defaultBoolean)) {//长按Item显示进入删除状态
                show();
            } else {
                hide();
            }
            setButtonStatus();
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
        mViewPager.setAdapter(new SectionsPagerAdapter(getChildFragmentManager()));
        mPagerSlidingTabStrip = PagerSlidingTabStrip.inflate(mActivity, container, false);
//        mPagerSlidingTabStrip.setOnPageChangeListener(mPageChangeListener);
        initActionBar(mPagerSlidingTabStrip);
        mPagerSlidingTabStrip.setViewPager(mViewPager);
        ImplAgent.getInstance(mActivity).addObserver(this);

        titleBar = inflater.inflate(R.layout.cover_actionbar, null);//这里是添加的控件
        initializeView(rootView);
        layout_button = (LinearLayout) rootView.findViewById(R.id.layout_button);
//        btnShare = (Button) view.findViewById(R.id.btnShare);
//        animaBt1 = AnimationUtils.loadAnimation(mActivity, R.anim.btn_share_in);
        btnDelete = (Button) rootView.findViewById(R.id.btnDelete);
        animaBtDel = AnimationUtils.loadAnimation(mActivity, R.anim.btn_delete_in);
//        btnShare.setOnClickListener(this);
        btnDelete.setOnClickListener(this);
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
        AppliteSPUtils.registerChangeListener(mActivity, mPagerListener);
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

        }
        //        if (v.getId() == R.id.btnShare) {//分享(已不可点)
//            flag = true;
//            Toast.makeText(mActivity.getApplicationContext(), "分享功能尚未实现，敬请期待", Toast.LENGTH_SHORT).show();
//        } else
        else if (v.getId() == R.id.btnDelete) {//删除
            AppliteSPUtils.put(mActivity, DELETEBTNPRESSED, true);
//            deleteItem();
            //通知listFragment删除应用
        } else if (v.getId() == R.id.select_allpick) {//全选
            LogUtils.d("wanghc","LENGTH____"+(int) AppliteSPUtils.get(mActivity, LENGTH, DefaultValue.defaultInt));
            if ((int) AppliteSPUtils.get(mActivity, LENGTH, DefaultValue.defaultInt) == (int) AppliteSPUtils.get(mActivity, COUNT, DefaultValue.defaultInt)) {
//                Arrays.fill(status, false);
                AppliteSPUtils.put(mActivity, STATUS, 0);//全选按钮状态
                AppliteSPUtils.put(mActivity, COUNT, 0);//选中的个数
            } else {
//                Arrays.fill(status, true);
//                checkedCount = mImplList.size();
                AppliteSPUtils.put(mActivity, STATUS, 1);
                AppliteSPUtils.put(mActivity, COUNT, (int) AppliteSPUtils.get(mActivity, LENGTH, DefaultValue.defaultInt));
            }
            setButtonStatus();
        } else if (v.getId() == R.id.select_cancel) {//取消
            hide();
            Toast.makeText(mActivity.getApplicationContext(), R.string.cancel_operator, Toast.LENGTH_SHORT).show();
        }

    }

    private void show() {
        if (titleBar.getVisibility() != View.VISIBLE) {
//                flagShowCheckBox = true;
//                    AppliteSPUtils.put(mActivity, FLAG, true);
            titleBar.setVisibility(View.VISIBLE);//显示titleBar
            layout_button.setVisibility(View.VISIBLE);//底部的布局及按钮
//                btnShare.setVisibility(View.VISIBLE);
//                btnShare.startAnimation(animaBt1);
            btnDelete.setVisibility(View.VISIBLE);
            btnDelete.startAnimation(animaBtDel);
        }
    }

    private void hide() {
        titleBar.setVisibility(View.GONE);
//            btnShare.setVisibility(View.GONE);
        btnDelete.setVisibility(View.GONE);
        layout_button.setVisibility(View.GONE);
        AppliteSPUtils.put(mActivity, FLAG, false);
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
//            btnShare = (Button) view.findViewById(R.id.btnShare);
//            animaBt1 = AnimationUtils.loadAnimation(mActivity, R.anim.btn_share_in);
            btnDelete = (Button) view.findViewById(R.id.btnDelete);
            animaBtDel = AnimationUtils.loadAnimation(mActivity, R.anim.btn_delete_in);
        }
        btnCancel.setOnClickListener(this);
        btnAllpick.setOnClickListener(this);
//        btnShare.setOnClickListener(this);
        btnDelete.setOnClickListener(this);
    }

    private void setButtonStatus() {

        temp = mActivity.getResources().getString(R.string.choose_message1) + AppliteSPUtils.get(mActivity, COUNT, DefaultValue.defaultInt) +
                mActivity.getResources().getString(R.string.choose_message2);
        tvShowTotal.setText(temp);
        temp = null;
        if (0 == (int) AppliteSPUtils.get(mActivity, COUNT, DefaultValue.defaultInt)) {
            btnAllpick.setText(R.string.allpick_btn);
            btnDelete.setEnabled(false);
            btnDelete.setTextAppearance(mActivity, R.style.DownloadListEmpty0);
        } else if ((int) AppliteSPUtils.get(mActivity, LENGTH, DefaultValue.defaultInt) == (int) AppliteSPUtils.get(mActivity, COUNT, DefaultValue.defaultInt)) {
            btnAllpick.setText(R.string.nonepick_btn);
            btnDelete.setEnabled(true);
            btnDelete.setTextAppearance(mActivity, R.style.DownloadOptButton);
        } else {
            btnAllpick.setText(R.string.allpick_btn);
            btnDelete.setEnabled(true);
            btnDelete.setTextAppearance(mActivity, R.style.DownloadOptButton);
        }
    }

//    @Override
//    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//        if (!(boolean) AppliteSPUtils.get(mActivity, FLAG, DefaultValue.defaultBoolean)) {
//            if (titleBar.getVisibility() != View.VISIBLE) {
////                flagShowCheckBox = true;
//                AppliteSPUtils.put(mActivity, FLAG, true);
//                titleBar.setVisibility(View.VISIBLE);//显示titleBar
//                layout_button.setVisibility(View.VISIBLE);//底部的布局及按钮
////                btnShare.setVisibility(View.VISIBLE);
////                btnShare.startAnimation(animaBt1);
//                btnDelete.setVisibility(View.VISIBLE);
//                btnDelete.startAnimation(animaBtDel);
//            }
//        }
//        return false;
//    }


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
            Fragment fg = null;
            OSGIServiceHost host = (OSGIServiceHost) mActivity;
            int downloadFlag = ImplInfo.STATUS_PENDING | ImplInfo.STATUS_RUNNING | ImplInfo.STATUS_PAUSED
                    | ImplInfo.STATUS_FAILED | ImplInfo.STATUS_PACKAGE_INVALID;
            if (null != host) {
                if (R.string.dm_downloaded == tabs[position]) {
                    fg = host.newFragment(
                            Constant.OSGI_SERVICE_DM_FRAGMENT,
                            DownloadListFragment.class.getName(),
                            DownloadListFragment.newBundle(R.string.dm_downloaded, ~downloadFlag));
                } else if (R.string.dm_downloading == tabs[position]) {
                    fg = host.newFragment(
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
}