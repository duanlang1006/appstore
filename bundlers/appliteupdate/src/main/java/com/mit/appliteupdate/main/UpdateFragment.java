package com.mit.appliteupdate.main;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.applite.common.AppliteUtils;
import com.applite.common.Constant;
import com.applite.common.LogUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.mit.appliteupdate.R;
import com.mit.appliteupdate.adapter.UpdateAdapter;
import com.mit.appliteupdate.bean.DataBean;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplInfo;
import com.mit.mitupdatesdk.MitMobclickAgent;
import com.osgi.extra.OSGIBaseFragment;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class UpdateFragment extends OSGIBaseFragment implements View.OnClickListener {

    private static final String TAG = "UpdateFragment";
    private View rootView;
    private TextView mAllUpdateView;
    private ListView mListView;
    private List<DataBean> mDataContents = new ArrayList<DataBean>();
    private UpdateAdapter mAdapter;
    private Runnable mNotifyRunnable = new Runnable() {
        @Override
        public void run() {
            mAdapter.notifyDataSetChanged();
        }
    };
    private RelativeLayout mStatsLayout;
    private ImageView mStatsImgView;
    private TextView mStatsTextView;
    private TextView mStatsButton;
    private boolean mPostStats = true;
    private ImplAgent implAgent;
    private HttpUtils mHttpUtils;
    private LinearLayout mLoadLayout;
    private ImageView mLoadView;
    private Animation LoadingAnimation;

    public UpdateFragment() {
        super();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        initActionBar();
        implAgent = ImplAgent.getInstance(mActivity.getApplicationContext());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHttpUtils = new HttpUtils();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_update, container, false);
        initView();
        post();

        return rootView;
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
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.update_all_update) {
            if (!mDataContents.isEmpty()) {
                MitMobclickAgent.onEvent(mActivity, "onClickButtonAllUpdate");
                DataBean data = null;
                for (int i = 0; i < mDataContents.size(); i++) {
                    data = mDataContents.get(i);
                    download(data);
                }
                mAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(mActivity, AppliteUtils.getString(mActivity, R.string.no_update), Toast.LENGTH_SHORT).show();
            }
        } else if (v.getId() == R.id.update_post_button) {
            if (mPostStats)
                post();
        }
    }

    private void initActionBar() {
        try {
            ActionBar actionBar = ((ActionBarActivity) mActivity).getSupportActionBar();
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayShowCustomEnabled(false);
            actionBar.setTitle("应用升级");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        mAllUpdateView = (TextView) rootView.findViewById(R.id.update_all_update);
        mListView = (ListView) rootView.findViewById(R.id.update_listview);
        mStatsLayout = (RelativeLayout) rootView.findViewById(R.id.update_stats);
        mStatsImgView = (ImageView) rootView.findViewById(R.id.update_stats_img);
        mStatsTextView = (TextView) rootView.findViewById(R.id.no_network_text);
        mStatsButton = (TextView) rootView.findViewById(R.id.update_post_button);

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

    private void post() {
        setLoadLayoutVisibility(View.VISIBLE);
        mPostStats = false;
        RequestParams params = new RequestParams();
        params.addBodyParameter("appkey", AppliteUtils.getMitMetaDataValue(mActivity, Constant.META_DATA_MIT));
        params.addBodyParameter("packagename", mActivity.getPackageName());
        params.addBodyParameter("type", "update_management");
        params.addBodyParameter("protocol_version", "1.0");
        params.addBodyParameter("update_info", AppliteUtils.getAllApkData(mActivity));
        mHttpUtils.send(HttpRequest.HttpMethod.POST, Constant.URL, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                setLoadLayoutVisibility(View.GONE);
                LogUtils.i(TAG, "更新请求成功，resulit：" + responseInfo.result);
                resolve(responseInfo.result);
                mPostStats = true;
            }

            @Override
            public void onFailure(HttpException e, String s) {
                setLoadLayoutVisibility(View.GONE);
                LogUtils.i(TAG, "更新请求失败：" + s);
                setStatsLayoutVisibility(View.VISIBLE, mActivity.getResources().getDrawable(R.drawable.post_failure));
                mStatsTextView.setVisibility(View.VISIBLE);
                mStatsButton.setVisibility(View.VISIBLE);
                mPostStats = true;
            }

        });
    }


    /**
     * 解析返回的数据
     *
     * @param resulit
     */
    private void resolve(String resulit) {
        try {
            JSONObject object = new JSONObject(resulit);
            int app_key = object.getInt("app_key");
            String installed_update_list = object.getString("installed_update_list");
            DataBean bean = null;
            if (!TextUtils.isEmpty(installed_update_list)) {
                JSONArray array = new JSONArray(installed_update_list);
                for (int i = 0; i < array.length(); i++) {
                    bean = new DataBean();
                    JSONObject obj = new JSONObject(array.get(i).toString());
                    bean.setmName(obj.getString("name"));
                    bean.setmVersionCode(obj.getInt("versionCode"));
                    bean.setmVersionName(obj.getString("versionName"));
                    bean.setmImgUrl(obj.getString("iconUrl"));
                    bean.setmPackageName(obj.getString("packageName"));
                    bean.setmUrl(obj.getString("rDownloadUrl"));
                    bean.setmSize(obj.getLong("apkSize"));

//                    bean.setmShowText(AppliteUtils.getString(mContext, R.string.install));

                    mDataContents.add(bean);
                }
                if (array.length() == 0) {
                    setStatsLayoutVisibility(View.VISIBLE, mActivity.getResources().getDrawable(R.drawable.no_update));
                    mStatsTextView.setVisibility(View.GONE);
                    mStatsButton.setVisibility(View.GONE);
                } else {
                    setStatsLayoutVisibility(View.GONE, null);
                }
                mAdapter = new UpdateAdapter(mActivity, mDataContents);
                mListView.setAdapter(mAdapter);
                mListView.setVisibility(View.VISIBLE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            LogUtils.i(TAG, "更新管理返回的JSON解析失败");
        }
    }

    /**
     * 判断StatsView显示状态
     */
    private void setStatsLayoutVisibility(int visibility, Drawable drawable) {
        switch (visibility) {
            case View.GONE:
                mStatsLayout.setVisibility(visibility);
                mListView.setVisibility(View.VISIBLE);
                break;
            case View.VISIBLE:
                mListView.setVisibility(View.GONE);
                mStatsImgView.setBackground(drawable);
                mStatsLayout.setVisibility(visibility);
                break;
        }
    }

    private void download(DataBean bean) {
        ImplInfo implInfo = implAgent.getImplInfo(bean.getmPackageName(), bean.getmPackageName(), bean.getmVersionCode());
        if (null == implInfo) {
            return;
        }
        implInfo.setTitle(bean.getmName()).setDownloadUrl(bean.getmUrl()).setIconUrl(bean.getmImgUrl());
        if (ImplInfo.ACTION_DOWNLOAD == implAgent.getAction(implInfo)) {
            switch (implInfo.getStatus()) {
                case Constant.STATUS_PENDING:
                case Constant.STATUS_RUNNING:
                    break;
                case Constant.STATUS_PAUSED:
                    implAgent.resumeDownload(implInfo, null);
                    break;
                case Constant.STATUS_NORMAL_INSTALLING:
                case Constant.STATUS_PRIVATE_INSTALLING:
                    //正在安装或已安装
//                            Toast.makeText(mActivity, "该应用您已经安装过了！", Toast.LENGTH_SHORT).show();
                    break;
                case Constant.STATUS_INSTALLED:
                default:
                    implAgent.newDownload(implInfo,
                            Constant.extenStorageDirPath,
                            bean.getmName() + ".apk",
                            true,
                            null);
                    break;
            }
        }
    }

}
