package com.mit.appliteupdate.main;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.applite.common.AppliteUtils;
import com.applite.common.Constant;
import com.applite.common.LogUtils;
import com.applite.sharedpreferences.AppliteSPUtils;
import com.applite.similarview.SimilarAdapter;
import com.applite.similarview.SimilarBean;
import com.applite.similarview.SimilarView;
import com.google.gson.Gson;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.mit.appliteupdate.R;
import com.mit.appliteupdate.adapter.IgnoreAdapter;
import com.mit.appliteupdate.adapter.MySimilarAdapter;
import com.mit.appliteupdate.adapter.UpdateAdapter;
import com.mit.appliteupdate.bean.ApkData;
import com.mit.appliteupdate.bean.UpdateData;
import com.mit.appliteupdate.utils.UpdateUtils;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplHelper;
import com.mit.impl.ImplInfo;
import com.mit.mitupdatesdk.MitMobclickAgent;
import com.osgi.extra.OSGIBaseFragment;
import com.osgi.extra.OSGIServiceHost;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UpdateFragment extends OSGIBaseFragment implements View.OnClickListener, SimilarAdapter.SimilarAPKDetailListener, UpdateAdapter.UpdateSuccessListener,
        IgnoreAdapter.CancelIgnoreListener {

    private static final String TAG = "UpdateFragment";
    private View rootView;
    private TextView mAllUpdateView;
    private ListView mListView;
    private List<ApkData> mUpdateApkList;
    private SimilarView mSimilarView;
    private SimilarAdapter mSimilarAdapter;
    private List<SimilarBean> mSimilarDataList;
    private UpdateAdapter mAdapter;
    private Runnable mNotifyRunnable = new Runnable() {
        public void run() {
            mAdapter.notifyDataSetChanged();
        }
    };
    private RelativeLayout mStatsLayout;
    private TextView mStatsButton;
    private boolean mPostStats = true;
    private ImplAgent implAgent;
    private HttpUtils mHttpUtils;
    private LinearLayout mLoadLayout;
    private ImageView mLoadView;
    private Animation LoadingAnimation;
    private String mUpdateData;
    private Gson mGson = new Gson();
    private UninstallReceiver mReceiver;
    private LayoutInflater mInflater;
    private ViewGroup customView;
    private TextView mActionBarIgnore;
    private List<ApkData> mIgnoreList = new ArrayList<>();
    private LinearLayout mTitleLayout;
    private ListView mIgnoreListView;
    private TextView mActionBarTitle;
    private IgnoreAdapter mIgnoreAdapter;
    private ImageView mNoUpdateView;

    public UpdateFragment() {
        super();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        implAgent = ImplAgent.getInstance(mActivity.getApplicationContext());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHttpUtils = new HttpUtils();
        mReceiver = new UninstallReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addDataScheme("package");
        mActivity.registerReceiver(mReceiver, filter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mInflater = inflater;
        rootView = inflater.inflate(R.layout.fragment_update, container, false);
        mSimilarView = (SimilarView) View.inflate(mActivity, R.layout.similar_view, null);
        initActionBar();
        initView();
        mUpdateData = (String) AppliteSPUtils.get(mActivity, AppliteSPUtils.UPDATE_DATA, "");
        LogUtils.d(TAG, "onCreateView,mUpdateData:" + mUpdateData);
        if (TextUtils.isEmpty(mUpdateData)) {
            post();
        } else {
            resolve(mUpdateData);
        }
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mActivity.unregisterReceiver(mReceiver);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem item = menu.findItem(R.id.action_search);
        if (null != item) {
            item.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (android.R.id.home == item.getItemId()) {
            if (mIgnoreListView.getVisibility() == View.VISIBLE) {
                mIgnoreListView.setVisibility(View.GONE);
                mTitleLayout.setVisibility(View.VISIBLE);
                mActionBarTitle.setVisibility(View.VISIBLE);
                mListView.setVisibility(View.VISIBLE);
                if (mUpdateApkList.size() == 0)
                    mNoUpdateView.setVisibility(View.VISIBLE);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            initActionBar();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.update_all_update) {
            if (null != mUpdateApkList && !mUpdateApkList.isEmpty()) {
                MitMobclickAgent.onEvent(mActivity, "onClickButtonAllUpdate");
                ApkData data = null;
                for (int i = 0; i < mUpdateApkList.size(); i++) {
                    data = mUpdateApkList.get(i);
                    download(data);
                }
                mAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(mActivity, AppliteUtils.getString(mActivity, R.string.no_update), Toast.LENGTH_SHORT).show();
            }
        } else if (v.getId() == R.id.update_post_button) {
            if (mPostStats) {
                post();
                setStatsLayoutVisibility(View.GONE);
            }
        } else if (v.getId() == R.id.update_actionbar_ignore_tv) {
            mTitleLayout.setVisibility(View.GONE);
            mListView.setVisibility(View.GONE);
            mActionBarTitle.setVisibility(View.GONE);
            mNoUpdateView.setVisibility(View.GONE);
            mIgnoreListView.setVisibility(View.VISIBLE);
            if (null == mIgnoreAdapter) {
                mIgnoreAdapter = new IgnoreAdapter(mActivity, mIgnoreList, this);
                mIgnoreListView.setAdapter(mIgnoreAdapter);
            } else {
                mIgnoreAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * 设置ActionBar
     */
    private void initActionBar() {
        try {
            LogUtils.i(TAG, "initActionBar");
            if (null == customView) {
                customView = (ViewGroup) mInflater.inflate(R.layout.update_actionbar, null);
                mActionBarIgnore = (TextView) customView.findViewById(R.id.update_actionbar_ignore_tv);
                mActionBarTitle = (TextView) customView.findViewById(R.id.update_actionbar_title);
                mActionBarIgnore.setOnClickListener(this);
            }
            ActionBar actionBar = ((ActionBarActivity) mActivity).getSupportActionBar();
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            actionBar.setCustomView(customView);
            actionBar.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 关联控件及监听
     */
    private void initView() {
        mAllUpdateView = (TextView) rootView.findViewById(R.id.update_all_update);
        mListView = (ListView) rootView.findViewById(R.id.update_listview);
        mListView.addFooterView(mSimilarView);
        mStatsLayout = (RelativeLayout) rootView.findViewById(R.id.update_stats);
        mStatsButton = (TextView) rootView.findViewById(R.id.update_post_button);
        mTitleLayout = (LinearLayout) rootView.findViewById(R.id.update_title);
        mIgnoreListView = (ListView) rootView.findViewById(R.id.update_ignore_listview);
        mNoUpdateView = (ImageView) rootView.findViewById(R.id.update_no_update_iv);

        //加载中控件
        mLoadLayout = (LinearLayout) rootView.findViewById(R.id.update_loading_layout);
        mLoadView = (ImageView) rootView.findViewById(R.id.update_loading_img);
        //旋转动画
        LoadingAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.loading);
        LinearInterpolator lin = new LinearInterpolator();
        LoadingAnimation.setInterpolator(lin);
        mLoadView.startAnimation(LoadingAnimation);

        mStatsButton.setOnClickListener(this);
        mAllUpdateView.setOnClickListener(this);
    }

    /**
     * 设置加载中控件的显示和隐藏
     *
     * @param Visibility
     */
    private void setLoadLayoutVisibility(int Visibility) {
        switch (Visibility) {
            case View.VISIBLE:
                mLoadLayout.setVisibility(View.VISIBLE);
                mLoadView.startAnimation(LoadingAnimation);
                break;
            case View.GONE:
                mLoadLayout.setVisibility(View.GONE);
                mLoadView.clearAnimation();
                break;
        }
    }

    /**
     * 发送更新请求
     */
    private void post() {
        setLoadLayoutVisibility(View.VISIBLE);
        mPostStats = false;
        RequestParams params = new RequestParams();
        params.addBodyParameter("appkey", AppliteUtils.getMitMetaDataValue(mActivity, Constant.META_DATA_MIT));
        params.addBodyParameter("packagename", mActivity.getPackageName());
        params.addBodyParameter("type", "update_management");
        params.addBodyParameter("protocol_version", Constant.PROTOCOL_VERSION);
        params.addBodyParameter("update_info", AppliteUtils.getAllApkData(mActivity));
        mHttpUtils.send(HttpRequest.HttpMethod.POST, Constant.URL, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                setLoadLayoutVisibility(View.GONE);
                LogUtils.i(TAG, "更新请求成功，resulit：" + responseInfo.result);
                AppliteSPUtils.put(mActivity, AppliteSPUtils.UPDATE_DATA, responseInfo.result);
                resolve(responseInfo.result);
                mPostStats = true;
            }

            @Override
            public void onFailure(HttpException e, String s) {
                setLoadLayoutVisibility(View.GONE);
                LogUtils.i(TAG, "更新请求失败：" + s);
                setStatsLayoutVisibility(View.VISIBLE);
                mPostStats = true;
            }

        });
    }


    /**
     * 解析返回的数据
     *
     * @param result
     */
    private void resolve(String result) {
        if (!TextUtils.isEmpty(mUpdateData))
            setLoadLayoutVisibility(View.GONE);
        try {
            UpdateData updateData = mGson.fromJson(result, UpdateData.class);
            if (null != updateData) {
                mUpdateApkList = updateData.getInstalled_update_list();
                mSimilarDataList = updateData.getSimilar_info();
            }
            if (null == mSimilarAdapter) {
                mSimilarAdapter = new MySimilarAdapter(mActivity);
                mSimilarAdapter.setData(mSimilarDataList, this);
                mSimilarView.setAdapter(mSimilarAdapter);
            } else {
                mSimilarAdapter.setData(mSimilarDataList, this);
                mSimilarAdapter.notifyDataSetChanged();
            }

            //删除已经忽略的APK
            Iterator iter = mUpdateApkList.iterator();
            while (iter.hasNext()) {
                ApkData data = (ApkData) iter.next();
                boolean isKeyExist = AppliteSPUtils.contains(mActivity, data.getPackageName());
                if (isKeyExist) {
                    int VersionCode = (int) AppliteSPUtils.get(mActivity, data.getPackageName(), 0);
                    if (data.getVersionCode() == VersionCode) {
                        LogUtils.d(TAG, "忽略的Name：" + data.getName());
                        iter.remove();
                        mUpdateApkList.remove(iter);
                        mIgnoreList.add(data);
                    } else {
                        AppliteSPUtils.remove(mActivity, data.getPackageName());
                    }
                }
            }
            if (mIgnoreList.size() > 0) {//有忽略的才显示，不然就隐藏
                mActionBarIgnore.setText(mActivity.getResources().getString(R.string.ignore_update) + "(" + mIgnoreList.size() + ")");
                mActionBarIgnore.setVisibility(View.VISIBLE);
            } else {
                mActionBarIgnore.setVisibility(View.GONE);
            }

            if (null == mUpdateApkList || 0 == mUpdateApkList.size()) {
                mNoUpdateView.setVisibility(View.VISIBLE);
            } else {
                mNoUpdateView.setVisibility(View.GONE);
            }

            mAdapter = new UpdateAdapter(mActivity, mUpdateApkList, this);
            mListView.setAdapter(mAdapter);
            mListView.setVisibility(View.VISIBLE);

        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.i(TAG, "更新管理返回的JSON解析失败");
        }
    }

    /**
     * 判断StatsView显示状态
     */
    private void setStatsLayoutVisibility(int visibility) {
        switch (visibility) {
            case View.GONE:
                mStatsLayout.setVisibility(visibility);
                mListView.setVisibility(View.VISIBLE);
                break;
            case View.VISIBLE:
                mListView.setVisibility(View.GONE);
                mStatsLayout.setVisibility(visibility);
                break;
        }
    }

    private void download(ApkData bean) {
        ImplInfo implInfo = implAgent.getImplInfo(bean.getPackageName(), bean.getPackageName(), bean.getVersionCode());
        if (null == implInfo) {
            return;
        }
        String path = Environment.getExternalStorageDirectory() + File.separator + Constant.extenStorageDirPath + bean.getName() + ".apk";
        if (!AppliteUtils.isPackageOk(mActivity, path))
            ImplHelper.updateImpl(mActivity,
                    implInfo,
                    bean.getrDownloadUrl(),
                    bean.getName(),
                    bean.getIconUrl(),
                    path,
                    null,
                    null);
    }

    @Override
    public void refreshDetail(SimilarBean similarBean) {
        ((OSGIServiceHost) mActivity).jumptoDetail(similarBean.getPackageName(), similarBean.getName(), similarBean.getIconUrl(), true);
    }

    @Override
    public void removeDataPosition(String packageName) {
        int position = -1;
        for (int i = 0; i < mUpdateApkList.size(); i++) {
            if (packageName.equals(mUpdateApkList.get(i).getPackageName())) {
                position = i;
            }
        }
        if (position != -1) {
            mUpdateApkList.remove(position);
            AppliteSPUtils.put(mActivity, AppliteSPUtils.UPDATE_DATA, UpdateUtils.listTojson(mUpdateApkList, mSimilarDataList));
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void ignoreDataPosition(String packageName) {
        int position = -1;
        for (int i = 0; i < mUpdateApkList.size(); i++) {
            if (packageName.equals(mUpdateApkList.get(i).getPackageName())) {
                position = i;
            }
        }
        if (position != -1) {
            mIgnoreList.add(mUpdateApkList.get(position));
            mActionBarIgnore.setText(mActivity.getResources().getString(R.string.ignore_update) + "(" + mIgnoreList.size() + ")");
            mActionBarIgnore.setVisibility(View.VISIBLE);

            mUpdateApkList.remove(position);
        }
        if (mUpdateApkList.size() == 0)
            mNoUpdateView.setVisibility(View.VISIBLE);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void cancelIgnoreListener(String PackageName) {
        AppliteSPUtils.remove(mActivity, PackageName);

        int position = -1;
        for (int i = 0; i < mIgnoreList.size(); i++) {
            if (PackageName.equals(mIgnoreList.get(i).getPackageName())) {
                position = i;
            }
        }
        if (position != -1) {
            mUpdateApkList.add(mIgnoreList.get(position));

            mIgnoreList.remove(position);
            mActionBarIgnore.setText(mActivity.getResources().getString(R.string.ignore_update) + "(" + mIgnoreList.size() + ")");
        }
        mIgnoreAdapter.notifyDataSetChanged();
        mAdapter.notifyDataSetChanged();
    }

    private class UninstallReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //接收卸载广播
            String action = intent.getAction();
            if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
                int position = -1;
                String packageName = intent.getDataString();
                if (null != mUpdateApkList && !mUpdateApkList.isEmpty() && null != mAdapter) {
                    for (int i = 0; i < mUpdateApkList.size(); i++) {
                        if (packageName.equals("package:" + mUpdateApkList.get(i).getPackageName())) {
                            position = i;
                        }
                    }
                    if (position != -1) {
                        mUpdateApkList.remove(position);
                        mActivity.runOnUiThread(mNotifyRunnable);
                        LogUtils.d(TAG, "检测到卸载，mAdapter刷新");

                        AppliteSPUtils.put(mActivity, AppliteSPUtils.UPDATE_DATA, UpdateUtils.listTojson(mUpdateApkList, mSimilarDataList));
                    }
                }
            }
        }
    }
}
