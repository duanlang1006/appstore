package com.applite.dm.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.KeyEvent;
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
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.applite.common.Constant;
import com.applite.common.LogUtils;
import com.applite.common.PagerSlidingTabStrip;
import com.applite.dm.R;
import com.applite.sharedpreferences.AppliteSPUtils;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplInfo;
import com.mit.impl.ImplLog;
import com.osgi.extra.OSGIBaseFragment;
import com.osgi.extra.OSGIServiceHost;

public class DownloadPagerFragment extends OSGIBaseFragment implements View.OnClickListener, ViewPager.OnPageChangeListener {
    final static String TAG = "applite_dm";
    private ViewPager mViewPager;

    private SectionsPagerAdapter mViewPagerAdapter;
    private PagerSlidingTabStrip mPagerSlidingTabStrip;
    private WindowManager.LayoutParams layoutTop;

    private WindowManager topviewManager;
    private View titleBar;//长按时覆盖ActionBar的控件
    private Button btnCancel;
    private Button btnAllpick;
    private TextView tvShowTotal;
    private LinearLayout layout_button;//盛放两个按钮的布局

    private Button btnDelete = null;
    private Animation animaBtDel;
    private CheckBox checkBox;
    private LinearLayout all_checkbox;
    private AlertDialog dialog;
    private boolean flagDeleteFile = false;
    private View layoutCustomDialog = null;

    private String OSGI_SERVICE_DM_LIST_FRAGMENT = "osgi.service.dmlist.fragment";

    private String COUNT_DOWNLOADING = "count downloading";
    private String COUNT_DOWNLOADED = "count downloaded";
    private String FLAG = "flag";
    private String POSITION = "position";

    private int prePosition = 0;
    private IDownloadOperator operator = null;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ImplLog.d(TAG, "onCreateView," + this);
        View rootView = inflater.inflate(R.layout.fragment_download_pager, container, false);
        mViewPager = (ViewPager) rootView.findViewById(R.id.pager);
        mViewPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());
        mViewPager.setAdapter(mViewPagerAdapter);
        mPagerSlidingTabStrip = PagerSlidingTabStrip.inflate(mActivity, container, false);
        initActionBar(mPagerSlidingTabStrip);
        mPagerSlidingTabStrip.setViewPager(mViewPager);
        mPagerSlidingTabStrip.setOnPageChangeListener(this);
        titleBar = inflater.inflate(R.layout.cover_actionbar, null);//这里是添加的顶端控件
        layoutCustomDialog = inflater.inflate(R.layout.custom_dialog, (ViewGroup) rootView.findViewById(R.id.mydialog));
        initializeView(rootView);
        AppliteSPUtils.registerChangeListener(mActivity, mPagerListener);
        AppliteSPUtils.put(mActivity, COUNT_DOWNLOADING, 0);
        AppliteSPUtils.put(mActivity, COUNT_DOWNLOADED, 0);
        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ImplLog.d(TAG, "onDetach," + this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (1 == (int) AppliteSPUtils.get(mActivity, POSITION, 0)) {
            mViewPager.setCurrentItem(1);
            AppliteSPUtils.put(mActivity, POSITION, R.string.dm_downloaded);
            prePosition = 1;
        }
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
                    if ((boolean) AppliteSPUtils.get(mActivity, FLAG, false)) {
                        pressedCancel();
                        return true;
                    } else if (!isHomeExist()) {
                        ((OSGIServiceHost) mActivity).jumpto(Constant.OSGI_SERVICE_MAIN_FRAGMENT, null, null, false);
                        return true;
                    }
                } else if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_MENU) {
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (null != topviewManager) {
            topviewManager.removeView(titleBar);
            layoutTop = null;
            topviewManager = null;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main_dm, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        if (null != item) {
            item.setVisible(false);
        }
        if (prePosition == 1) {
            menu.findItem(R.id.dm_action_pause_all).setEnabled(false);
            menu.findItem(R.id.dm_action_resume_all).setEnabled(false);
        } else {
            operator = (IDownloadOperator) mViewPagerAdapter.instantiateItem(mViewPager, prePosition);
            if (null != operator) {
                try {
                    if (0 == operator.getLength()) {
                        menu.findItem(R.id.dm_action_pause_all).setEnabled(false);
                        menu.findItem(R.id.dm_action_resume_all).setEnabled(false);
                    } else {
                        menu.findItem(R.id.dm_action_pause_all).setEnabled(true);
                        menu.findItem(R.id.dm_action_resume_all).setEnabled(true);
                    }
                } catch (Exception e) {
                    menu.findItem(R.id.dm_action_pause_all).setEnabled(false);
                    menu.findItem(R.id.dm_action_resume_all).setEnabled(false);
                }
            }
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
        } else if (android.R.id.home == item.getItemId()) {
            if (!isHomeExist()) {
                ((OSGIServiceHost) mActivity).jumpto(Constant.OSGI_SERVICE_MAIN_FRAGMENT, null, null, false);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            initActionBar(mPagerSlidingTabStrip);
        }
    }

    @Override
    public void onClick(View v) {
//        FragmentManager fgm = getFragmentManager();
//        if (v.getId() == R.id.action_back) {
//            if (null != fgm.getFragments() && fgm.getFragments().size() > 0) {
//                if (!isHomeExist()) {
//                    ((OSGIServiceHost) mActivity).jumpto(Constant.OSGI_SERVICE_MAIN_FRAGMENT, null, null, false);
//                    return;
//                }
//                fgm.popBackStack();
//            } else {
//                mActivity.finish();
//            }
//        } else
        if (v.getId() == R.id.btnDelete) {//删除
            if (null == dialog) {
                initDialog();
            }
            checkBox.setChecked(false);
            dialog.show();
        } else if (v.getId() == R.id.select_allpick) {//全选/全不选
            operator = (IDownloadOperator) mViewPagerAdapter.instantiateItem(mViewPager, prePosition);
            if (null != operator) {
                if (operator.getLength() == getCount()) {
                    operator.onClickDeselectAll();
                } else {
                    operator.onClickSeleteAll();
                }
            }
        } else if (v.getId() == R.id.select_cancel) {//取消
            pressedCancel();
        } else if (v.getId() == R.id.all_checkbox) {//删除对话框的checkbox
            checkBox.setChecked(!checkBox.isChecked());
        }
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {
    }

    @Override
    public void onPageSelected(int i) {
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
    public void onPageScrollStateChanged(int i) {
    }

    /**
     * 判断首页是否存在
     *
     * @return
     */

    private boolean isHomeExist() {
        if (null == getFragmentManager().findFragmentByTag(Constant.OSGI_SERVICE_MAIN_FRAGMENT)) {
            LogUtils.d(TAG, "首页不存在");
            return false;
        }
        LogUtils.d(TAG, "首页存在");
        return true;
    }

    private void initDialog() {
        dialog = new AlertDialog.Builder(mActivity).setView(layoutCustomDialog)
                .setPositiveButton(getResources().getString(R.string.ensure), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (checkBox.isChecked()) {
                            flagDeleteFile = true;
                        } else {
                            flagDeleteFile = false;
                        }
                        int totalDelete = ((int) AppliteSPUtils.get(mActivity, COUNT_DOWNLOADING, 0) + (int) AppliteSPUtils.get(mActivity, COUNT_DOWNLOADED, 0));
                        for (int i = 0; i < mViewPagerAdapter.getCount(); i++) {
                            operator = (IDownloadOperator) mViewPagerAdapter.instantiateItem(mViewPager, i);
                            if (null != operator) {
                                operator.onClickDelete(flagDeleteFile);
                            }
                        }
                        operator = (IDownloadOperator) mViewPagerAdapter.instantiateItem(mViewPager, prePosition);
                        if (0 == (int) AppliteSPUtils.get(mActivity, COUNT_DOWNLOADING, 0) && 0 == (int) AppliteSPUtils.get(mActivity, COUNT_DOWNLOADED, 0)) {
                            hide();
                            operator.resetFlag();
                        }
                        Toast.makeText(mActivity, mActivity.getResources().getString(R.string.delete_message, totalDelete), Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton(getResources().getString(R.string.cancel_btn), null).create();
        dialog.setCanceledOnTouchOutside(false);
    }

    private void pressedCancel() {
        hide();
        for (int i = 0; i < mViewPagerAdapter.getCount(); i++) {
            operator = (IDownloadOperator) mViewPagerAdapter.instantiateItem(mViewPager, i);
            if (null != operator) {
                operator.resetFlag();
            }
        }
        Toast.makeText(mActivity.getApplicationContext(), R.string.cancel_operator, Toast.LENGTH_SHORT).show();
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

    private void initActionBar(View tabStrip) {
        try {
            ActionBar actionBar = ((ActionBarActivity) mActivity).getSupportActionBar();
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
        }
    }

    private void initializeView(View view) {
//        if (null == layoutTop) {
        layoutTop = new WindowManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                mActivity.getResources().getDimensionPixelSize(R.dimen.abc_action_bar_default_height_material),
                WindowManager.LayoutParams.TYPE_APPLICATION,
                // 设置为无焦点状态
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, // 没有边界
                // 半透明效果
                PixelFormat.TRANSLUCENT);
        layoutTop.gravity = Gravity.TOP;
        layoutTop.windowAnimations = R.style.anim_view_top;
        topviewManager = (WindowManager) mActivity.getSystemService(Context.WINDOW_SERVICE);
        topviewManager.addView(titleBar, layoutTop);

        titleBar.setVisibility(View.GONE);
        initItem(view);
//        }
//        btnCancel.setOnClickListener(this);
//        btnAllpick.setOnClickListener(this);
//        btnDelete.setOnClickListener(this);

    }

    private void initItem(View view) {
        btnCancel = (Button) titleBar.findViewById(R.id.select_cancel);
        tvShowTotal = (TextView) titleBar.findViewById(R.id.total);
        btnAllpick = (Button) titleBar.findViewById(R.id.select_allpick);

        layout_button = (LinearLayout) view.findViewById(R.id.layout_button);
        btnDelete = (Button) view.findViewById(R.id.btnDelete);
        animaBtDel = AnimationUtils.loadAnimation(mActivity, R.anim.btn_delete_in);

        checkBox = (CheckBox) layoutCustomDialog.findViewById(R.id.checkbox);
        all_checkbox = (LinearLayout) layoutCustomDialog.findViewById(R.id.all_checkbox);

        btnCancel.setOnClickListener(this);
        btnAllpick.setOnClickListener(this);
        btnDelete.setOnClickListener(this);
        all_checkbox.setOnClickListener(this);
    }

    private void setButtonStatus() {
        //textView
        tvShowTotal.setText(mActivity.getResources().getString(R.string.choose_message,
                ((int) AppliteSPUtils.get(mActivity, COUNT_DOWNLOADED, 0) + (int) AppliteSPUtils.get(mActivity, COUNT_DOWNLOADING, 0))));
        //全选按钮
        operator = (IDownloadOperator) mViewPagerAdapter.instantiateItem(mViewPager, prePosition);
        if (0 == operator.getLength() && 0 == getCount()) {
            btnAllpick.setFocusable(false);
            btnAllpick.setEnabled(false);
            btnAllpick.setText(R.string.allpick_btn);
        } else {
            btnAllpick.setFocusable(true);
            btnAllpick.setEnabled(true);
            if (operator.getLength() == getCount()) {
                btnAllpick.setText(R.string.nonepick_btn);
            } else {//其他状态
                btnAllpick.setText(R.string.allpick_btn);
            }
        }
        //删除按钮
        if (0 == (int) AppliteSPUtils.get(mActivity, COUNT_DOWNLOADING, 0) + (int) AppliteSPUtils.get(mActivity, COUNT_DOWNLOADED, 0)) {
            btnDelete.setFocusable(false);
            btnDelete.setEnabled(false);
            btnDelete.setTextAppearance(mActivity, R.style.DownloadListEmpty0);
        } else {
            btnDelete.setFocusable(true);
            btnDelete.setEnabled(true);
            btnDelete.setTextAppearance(mActivity, R.style.DownloadOptButton);
        }

    }

    //当前选中项目数
    private int getCount() {
        if (0 == prePosition) {
            return (int) AppliteSPUtils.get(mActivity, COUNT_DOWNLOADING, 0);
        } else if (1 == prePosition) {
            return (int) AppliteSPUtils.get(mActivity, COUNT_DOWNLOADED, 0);
        } else {
            return 0;
        }
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
                            OSGI_SERVICE_DM_LIST_FRAGMENT,
                            DownloadListFragment.class.getName(),
                            DownloadListFragment.newBundle(R.string.dm_downloaded, ~downloadFlag));
                } else if (R.string.dm_downloading == tabs[position]) {
                    fg = (DownloadListFragment) host.newFragment(
                            OSGI_SERVICE_DM_LIST_FRAGMENT,
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
        void onClickDelete(boolean b);

        void onClickSeleteAll();

        void onClickDeselectAll();

        void resetFlag();

        int getLength();

    }
}