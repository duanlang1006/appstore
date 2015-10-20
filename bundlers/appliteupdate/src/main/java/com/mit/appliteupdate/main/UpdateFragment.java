package com.mit.appliteupdate.main;

import android.app.Activity;
import android.content.SharedPreferences;
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

import com.applite.bean.ApkBean;
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
import com.mit.appliteupdate.bean.UpdateData;
import com.mit.appliteupdate.utils.UpdateUtils;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplChangeCallback;
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
    private TextView mUpdateItemNum;
    private TextView mUpdateItemSize;
    private TextView mAllUpdateView;
    private ListView mListView;
    private List<ApkBean> mUpdateApkList;
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
    private LayoutInflater mInflater;
    private ViewGroup customView;
    private TextView mActionBarIgnore;
    private List<ApkBean> mIgnoreList = new ArrayList<>();
    private LinearLayout mTitleLayout;
    private ListView mIgnoreListView;
    private TextView mActionBarTitle;
    private IgnoreAdapter mIgnoreAdapter;
    private ImageView mNoUpdateView;
    private SharedPreferences.OnSharedPreferenceChangeListener mSPListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            LogUtils.d(TAG, "监听到SharedPreferences值改变");
            if (AppliteSPUtils.UPDATE_DATA.equals(key))
                resolve((String) AppliteSPUtils.get(mActivity, AppliteSPUtils.UPDATE_DATA, ""));
        }
    };

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
        AppliteSPUtils.registerChangeListener(mActivity, mSPListener);
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
        LogUtils.d(TAG, "onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        LogUtils.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    public void onDestroy() {
        LogUtils.d(TAG, "onDestroy");
        super.onDestroy();
        AppliteSPUtils.unregisterChangeListener(mActivity, mSPListener);
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
            if (!isHomeExist()) {
                ((OSGIServiceHost) mActivity).jumpto(Constant.OSGI_SERVICE_MAIN_FRAGMENT, null, null, false);
                return true;
            } else {
                if (mIgnoreListView.getVisibility() == View.VISIBLE) {
                    mIgnoreListView.setVisibility(View.GONE);
                    mTitleLayout.setVisibility(View.VISIBLE);
                    mActionBarTitle.setVisibility(View.VISIBLE);
                    mListView.setVisibility(View.VISIBLE);
                    if (mUpdateApkList.size() == 0) {
                        mNoUpdateView.setVisibility(View.VISIBLE);
                        mUpdateItemNum.setText(getString(R.string.update_item));
                        mUpdateItemSize.setVisibility(View.GONE);
                    } else {
                        mNoUpdateView.setVisibility(View.GONE);
                        mUpdateItemNum.setText(getString(R.string.update_item) + "(" + mUpdateApkList.size() + ")");
                        mUpdateItemSizeNum = 0.00;
                        for (int i = 0; i < mUpdateApkList.size(); i++) {
                            String num = AppliteUtils.bytes2kb(mUpdateApkList.get(i).getApkSize());
                            Double sizenumvalue = Double.valueOf(num.substring(0, num.length() - 2));
                            mUpdateItemSizeNum += sizenumvalue;
                            mUpdateItemSizeNum = (double) (Math.round((mUpdateItemSizeNum) * 100) / 100.00);
                            LogUtils.d(TAG, "总大小：" + mUpdateItemSizeNum);
                        }
                        mUpdateItemSize.setText("总计大小：" + String.valueOf(mUpdateItemSizeNum) + "MB");
                        mUpdateItemSize.setVisibility(View.VISIBLE);
                    }
                    if (mIgnoreList.size() == 0) {
                        mActionBarIgnore.setVisibility(View.INVISIBLE);
                    }
                    return true;
                }
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
                ApkBean data = null;
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
        LogUtils.d(TAG, "initView");
        mUpdateItemNum = (TextView) rootView.findViewById(R.id.update_item_num);
        mUpdateItemSize = (TextView) rootView.findViewById(R.id.update_size);
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
        UpdateData updateData = mGson.fromJson(result, UpdateData.class);
        if (null != updateData) {
            mUpdateApkList = updateData.getInstalled_update_list();
            LogUtils.d(TAG, "mUpdateApkList：" + mUpdateApkList);
            mSimilarDataList = updateData.getSimilar_info();
            LogUtils.d(TAG, "mSimilarDataList：" + mSimilarDataList);
        }
        if (null == mSimilarAdapter) {
            mSimilarAdapter = new MySimilarAdapter(mActivity);
            mSimilarAdapter.setData(mSimilarDataList, this, 4);
            mSimilarView.setAdapter(mSimilarAdapter);
        } else {
            mSimilarAdapter.setData(mSimilarDataList, this, 4);
            mSimilarAdapter.notifyDataSetChanged();
        }

        //删除已经忽略的APK
        Iterator iter = mUpdateApkList.iterator();
        while (iter.hasNext()) {
            ApkBean data = (ApkBean) iter.next();
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
            mUpdateItemNum.setText(getString(R.string.update_item));
            mUpdateItemSize.setVisibility(View.GONE);
        } else {
            mNoUpdateView.setVisibility(View.GONE);
            mUpdateItemNum.setText(getString(R.string.update_item) + "(" + mUpdateApkList.size() + ")");
            mUpdateItemSizeNum = 0.00;
            for (int i = 0; i < mUpdateApkList.size(); i++) {
                String num = AppliteUtils.bytes2kb(mUpdateApkList.get(i).getApkSize());
                Double sizenumvalue = Double.valueOf(num.substring(0, num.length() - 2));
                mUpdateItemSizeNum += sizenumvalue;
                mUpdateItemSizeNum = (double) (Math.round((mUpdateItemSizeNum) * 100) / 100.00);
                LogUtils.d(TAG, "总大小：" + mUpdateItemSizeNum);
            }
            mUpdateItemSize.setText("总计大小：" + String.valueOf(mUpdateItemSizeNum) + "MB");
            mUpdateItemSize.setVisibility(View.VISIBLE);
        }

        mAdapter = new UpdateAdapter(mActivity, mUpdateApkList, this);
        mListView.setAdapter(mAdapter);
        mListView.setVisibility(View.VISIBLE);
    }

    private Double mUpdateItemSizeNum = 0.00;


    /**
     * 判断StatsView显示状态
     */
    private void setStatsLayoutVisibility(int visibility) {
        LogUtils.i(TAG, "setStatsLayoutVisibility  visibility:" + visibility);
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

    private void download(ApkBean bean) {
        ImplInfo implInfo = implAgent.getImplInfo(bean.getPackageName(), bean.getPackageName(), bean.getVersionCode());
        if (null == implInfo) {
            return;
        }
        String path = Environment.getExternalStorageDirectory() + File.separator + Constant.extenStorageDirPath + bean.getName() + ".apk";
        ImplHelper.updateImpl(mActivity,
                implInfo,
                bean.getrDownloadUrl(),
                bean.getName(),
                bean.getIconUrl(),
                path,
                bean.getApkMd5(),
                null);
    }

    @Override
    public void onClickIcon(Object... params) {
        SimilarBean bean = (SimilarBean) params[0];
        ((OSGIServiceHost) mActivity).jumptoDetail(bean.getPackageName(), bean.getName(), bean.getIconUrl(), bean.getVersionCode(), null, true);
    }

    @Override
    public void onClickName(Object... params) {
        SimilarBean bean = (SimilarBean) params[0];
        ((OSGIServiceHost) mActivity).jumptoDetail(bean.getPackageName(), bean.getName(), bean.getIconUrl(), bean.getVersionCode(), null, true);
    }

    @Override
    public void onClickButton(Object... params) {
        ImplInfo implInfo = (ImplInfo) params[0];
        SimilarBean bean = (SimilarBean) params[1];
        ImplChangeCallback implChangeCallback = (ImplChangeCallback) params[2];
        ImplHelper.onClick(mActivity,
                implInfo,
                bean.getrDownloadUrl(),
                bean.getName(),
                bean.getIconUrl(),
                Environment.getExternalStorageDirectory() + File.separator + Constant.extenStorageDirPath + bean.getName() + ".apk",
                implInfo.getMd5(),
                implChangeCallback);
    }

    @Override
    public void dataLess(int i) {

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

        if (mUpdateApkList.size() == 0) {
            mNoUpdateView.setVisibility(View.VISIBLE);
            mUpdateItemNum.setText(getString(R.string.update_item));
            mUpdateItemSize.setVisibility(View.GONE);
        } else {
            mUpdateItemNum.setText(getString(R.string.update_item) + "(" + mUpdateApkList.size() + ")");
            mUpdateItemSizeNum = 0.00;
            for (int i = 0; i < mUpdateApkList.size(); i++) {
                String num = AppliteUtils.bytes2kb(mUpdateApkList.get(i).getApkSize());
                Double sizenumvalue = Double.valueOf(num.substring(0, num.length() - 2));
                mUpdateItemSizeNum += sizenumvalue;
                mUpdateItemSizeNum = (double) (Math.round((mUpdateItemSizeNum) * 100) / 100.00);
                LogUtils.d(TAG, "总大小：" + mUpdateItemSizeNum);
            }
            mUpdateItemSize.setText("总计大小：" + String.valueOf(mUpdateItemSizeNum) + "MB");
            mUpdateItemSize.setVisibility(View.VISIBLE);
        }
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

}
