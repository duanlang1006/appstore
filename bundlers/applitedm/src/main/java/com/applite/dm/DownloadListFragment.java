package com.applite.dm;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.applite.common.LogUtils;
import com.applite.dm.utils.HostUtils;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.bitmap.PauseOnScrollListener;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplInfo;
import com.mit.impl.ImplLog;
import com.osgi.extra.OSGIBaseFragment;
import com.osgi.extra.OSGIServiceHost;
import com.umeng.analytics.MobclickAgent;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class DownloadListFragment extends OSGIBaseFragment implements ListView.OnItemClickListener {
    private ListView mListview;
    private DownloadAdapter mAdapter;
    private boolean flag_showCheckBox = false;//长按删除的标志位
    private Button btnDelete = null;
    private Button btnShare = null;
    private boolean[] status = null;//这里存放checkBox的选中状态
    private Integer mStatusFlags = null;
    private ImplAgent mImplAgent;
    private List<ImplInfo> mImplList;
    private BitmapUtils mBitmapHelper;
    private Button btnCancel;
    private Button btnAllpick;
    private TextView tvShowTotal;
    private ActionBar actionBar;//声明ActionBar
    private WindowManager manager1;
    private WindowManager manager2;
    private View titleBar;//长按时覆盖ActionBar的控件
    private View bottomBar;//长按时出现在下面的布局
    private WindowManager.LayoutParams lp_top;
    private WindowManager.LayoutParams lp_bottom;
    private Animation animaBt1;
    private Animation animaBt2;


    public static final Comparator<ImplInfo> IMPL_TIMESTAMP_COMPARATOR = new Comparator<ImplInfo>() {
        public final int compare(ImplInfo a, ImplInfo b) {
            int result = 0;
            if (a.getLastMod() < b.getLastMod()) {
                result = 1;
            } else if (a.getLastMod() > b.getLastMod()) {
                result = -1;
            }
            return result;
        }
    };


    public static Bundle newBundle(int flag) {
        Bundle b = new Bundle();
        b.putInt("statusFilter", flag);
        return b;
    }

    public DownloadListFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ImplLog.d(DownloadPagerFragment.TAG, "onCreateView," + this);
        LayoutInflater mInflater = inflater;
        View view = mInflater.inflate(R.layout.fragment_download_list, container, false);
        mListview = (ListView) view.findViewById(android.R.id.list);
        mListview.setEmptyView(view.findViewById(R.id.empty));
        mListview.setOnItemClickListener(this);
        status = new boolean[mImplList.size()];
        Arrays.fill(status, false);//全部填充为false(chechbox不选中)
        titleBar = inflater.inflate(R.layout.cover_actionbar, null);//这里是添加的控件
        bottomBar = inflater.inflate(R.layout.bottom_view, null);//这里是添加的控件
        initializeView();
        //这里是长按删除
        mListview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (!flag_showCheckBox) {
                    if (titleBar.getVisibility() != View.VISIBLE) {
                        flag_showCheckBox = true;
                        mAdapter.resetFlag(flag_showCheckBox);
                        VibratorUtil.Vibrate(mActivity, 200);   //震动200ms
                        titleBar.setVisibility(View.VISIBLE);//显示titleBar
                        bottomBar.setVisibility(View.VISIBLE);//显示titleBar
                        btnShare.startAnimation(animaBt1);
                        btnDelete.startAnimation(animaBt2);
                    }
                    status[i] = !status[i];
                    setButtonStatus();
                    mAdapter.resetStatus(status);
                    mAdapter.notifyDataSetChanged();
                }
                return false;
            }
        });
        mListview.setOnScrollListener(new PauseOnScrollListener(mBitmapHelper, false, true));
        if (null != mImplList && mImplList.size() > 0) {
            mAdapter = new DownloadAdapter(mActivity, R.layout.download_list_item, mImplList, mBitmapHelper, flag_showCheckBox);
            mAdapter.sort(IMPL_TIMESTAMP_COMPARATOR);
            mListview.setAdapter(mAdapter);
        }
        return view;
    }

    private void setButtonStatus() {
        tvShowTotal.setText("选中了" + statistics() + "个安装包");
        if (-1 == judgeStatus()) {
            btnAllpick.setText("全选");
            btnShare.setEnabled(false);
            btnShare.setTextAppearance(mActivity, R.style.DownloadListEmpty);
            btnDelete.setEnabled(false);
            btnDelete.setTextAppearance(mActivity, R.style.DownloadListEmpty);
        } else {
            if (1 == judgeStatus()) {
                btnAllpick.setText("全不选");
            } else {
                btnAllpick.setText("全选");
            }
            btnShare.setEnabled(true);
            btnShare.setTextAppearance(mActivity, R.style.DownloadOptButton);
            btnDelete.setEnabled(true);
            btnDelete.setTextAppearance(mActivity, R.style.DownloadOptButton);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle params = getArguments();
        if (null != params) {
            mStatusFlags = params.getInt("statusFilter");
        }
        mImplAgent = ImplAgent.getInstance(activity.getApplicationContext());
        mImplList = mImplAgent.getDownloadInfoList(mStatusFlags);
        mBitmapHelper = new BitmapUtils(mActivity.getApplicationContext());
        ImplLog.d(DownloadPagerFragment.TAG, "onAttach," + this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
                    if (flag_showCheckBox) {
                        refresh(true);
                    }
                    return false;
                }
                return true;
            }
        });
        MobclickAgent.onPageStart("DownloadListFragment"); //统计页面
        flag_showCheckBox = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("DownloadListFragment");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ImplLog.d(DownloadPagerFragment.TAG, "onDetach," + this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ImplLog.d(DownloadPagerFragment.TAG, "onDestroyView," + this);
    }

    @Override
    public void onDestroy() {
        manager1.removeView(titleBar);
        manager2.removeView(bottomBar);
        super.onDestroy();
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListview.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DownloadAdapter.ViewHolder vh = (DownloadAdapter.ViewHolder) view.getTag();
        if (flag_showCheckBox) {
            mListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    if (flag_showCheckBox) {
                        status[i] = !status[i];
                        setButtonStatus();
                        mAdapter.resetStatus(status);
                        mAdapter.notifyDataSetChanged();
                    }
                }
            });
        } else {
            if (null != vh) {
                ((OSGIServiceHost) mActivity).jumptoDetail(
                        vh.implInfo.getPackageName(),
                        vh.implInfo.getTitle(),
                        vh.implInfo.getIconUrl(),
                        true);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.dm_action_pause_all) {
            Toast.makeText(mActivity.getApplicationContext(), "dm_action_pause_all", Toast.LENGTH_SHORT).show();
        } else if (item.getItemId() == R.id.dm_action_resume_all) {
            Toast.makeText(mActivity.getApplicationContext(), "dm_action_resume_all", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initializeView() {
        actionBar = ((ActionBarActivity) mActivity).getSupportActionBar();//得到ActionBar
        mListview.setAdapter(mAdapter);

        lp_top = new WindowManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                actionBar.getHeight(),
                WindowManager.LayoutParams.TYPE_APPLICATION,
                // 设置为无焦点状态
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, // 没有边界
                // 半透明效果
                PixelFormat.TRANSLUCENT);
        lp_top.gravity = Gravity.TOP;
        lp_top.windowAnimations = R.style.anim_view_top;
        manager1 = (WindowManager) mActivity.getSystemService(Context.WINDOW_SERVICE);
        manager1.addView(titleBar, lp_top);

        titleBar.setVisibility(View.GONE);
        btnCancel = (Button) titleBar.findViewById(R.id.select_cancel);
        btnCancel.setOnClickListener(btn_clickLis);
        tvShowTotal = (TextView) titleBar.findViewById(R.id.total);
        btnAllpick = (Button) titleBar.findViewById(R.id.select_allpick);
        btnAllpick.setOnClickListener(btn_clickLis);

        lp_bottom = new WindowManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, // 没有边界
                PixelFormat.TRANSLUCENT);
        lp_bottom.gravity = Gravity.BOTTOM;
        lp_bottom.windowAnimations = R.style.anim_view_bottom;
        manager2 = (WindowManager) mActivity.getSystemService(Context.WINDOW_SERVICE);
        manager2.addView(bottomBar, lp_bottom);

        bottomBar.setVisibility(View.GONE);
        btnDelete = (Button) bottomBar.findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(btn_clickLis);
        animaBt1 = AnimationUtils.loadAnimation(mActivity, R.anim.btn_share_in);
        btnShare = (Button) bottomBar.findViewById(R.id.btnShare);
        btnShare.setOnClickListener(btn_clickLis);
        animaBt2 = AnimationUtils.loadAnimation(mActivity, R.anim.btn_delete_in);
    }

    private View.OnClickListener btn_clickLis = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            boolean flag = false;
            if (view.getId() == R.id.btnShare) {
                flag = true;
                Toast.makeText(mActivity.getApplicationContext(), "分享功能尚未实现，敬请期待", Toast.LENGTH_SHORT).show();
            } else if (view.getId() == R.id.btnDelete) {
                flag = true;
                deleteItem();
            } else if (view.getId() == R.id.select_allpick) {//全选
                if (1 == judgeStatus()) {
                    Arrays.fill(status, false);
                } else {
                    Arrays.fill(status, true);
                }
                setButtonStatus();
                mAdapter.resetStatus(status);
                mAdapter.notifyDataSetChanged();
            } else if (view.getId() == R.id.select_cancel) {//取消
                flag = true;
                Toast.makeText(mActivity.getApplicationContext(), "我取消了操作", Toast.LENGTH_SHORT).show();
            }
            refresh(flag);
        }
    };

    private void refresh(boolean b) {
        if (b) {
            titleBar.setVisibility(View.GONE);
            bottomBar.setVisibility(View.GONE);
            flag_showCheckBox = false;//标志位复位
            mAdapter.resetFlag(flag_showCheckBox);
            Arrays.fill(status, false);//删除后将status复位
            mAdapter.resetStatus(status);
//                mAdapter.notifyDataSetChanged();
            mListview.setAdapter(mAdapter);//取消和分享不对item做改变，所以notifyDataSetChanged()不会更改listView
        }
    }

    private void deleteItem() {
        View childView;
        DownloadAdapter.ViewHolder vh;
        ImplAgent implAgent;
        int len = mImplList.size();
        for (int i = len - 1; i >= 0; i--) {
            if (status[i]) {
                childView = mListview.getChildAt(i);
                vh = (DownloadAdapter.ViewHolder) childView.getTag();
                implAgent = ImplAgent.getInstance(mActivity.getApplicationContext());
                implAgent.remove(vh.implInfo);
                mImplList.remove(i);
            }
        }
        Toast.makeText(mActivity.getApplicationContext(), "成功删除了" + statistics() + "条下载", Toast.LENGTH_SHORT).show();
    }

    private int statistics() {
        int temp = 0;
        for (int i = 0; i < status.length; i++) {
            if (status[i]) {
                temp++;
            }
        }
        return temp;
    }

    private int judgeStatus() {
        boolean first = false;
        for (int i = 0; i < status.length; i++) {
            if (0 == i) {
                first = status[0];
            } else {
                if (first != status[i]) {
                    return 0;//不全相同
                }
            }
        }
        if (first) {
            return 1;//全为真
        } else {
            return -1;//全为假
        }
    }
}