package com.mit.applite.main;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import com.applite.common.AppliteUtils;
import com.applite.common.Constant;
import com.applite.common.LogUtils;
import com.mit.applite.view.ProgressButton;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplInfo;
import com.mit.impl.ImplListener;
import com.umeng.analytics.MobclickAgent;

import net.tsz.afinal.FinalBitmap;
import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class DetailFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "DetailFragment";
    private Activity mActivity;
    private View rootView;
    private String mApkName;
    private TextView mNameView;
    private TextView mName1View;
    private TextView mApkSizeAndCompanyView;
    private TextView mApkContentView;
    private ImageButton mBackView;
    private ImageButton mSearchView;
    private Button mDownloadView;
    private RatingBar mXingView;
    private String mDownloadUrl;
    private FinalBitmap mFinalBitmap;
    private ImageView mApkImgView;
    private String mViewPagerUrlList[] = null;
    private ProgressBar mProgressBar;
    private static final int APK_LOADING = 1;
    private LayoutInflater mInflater;
    private ViewGroup container;
    private LinearLayout mImgLl;
    private String mPackageName;
    private String mName;
    private String mImgUrl;
    private ProgressButton mProgressButton;
    private int mVersionCode;
    private Context mContext;
    private LinearLayout no_network;
    private Button refreshButton;
    private ImplAgent implAgent;
    private ImplListener implCallback;

    public DetailFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        Bundle bundle = this.getArguments();
        mPackageName = bundle.getString("packageName");
        mApkName = bundle.getString("name");
        mImgUrl = bundle.getString("imgUrl");
        implAgent = ImplAgent.getInstance(mActivity.getApplicationContext());
        implCallback = new DetailImplCallback();
        LogUtils.i(TAG, "mApkName:" + mApkName + "------mPackageName:" + mPackageName + "------mImgUrl:" + mImgUrl);
        initActionBar();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mInflater = inflater;
        try {
            Context context = BundleContextFactory.getInstance().getBundleContext().getBundleContext();
            mInflater = LayoutInflater.from(context);
            mInflater = mInflater.cloneInContext(context);
            mContext = context;
        } catch (Exception e) {
            e.printStackTrace();
            mContext = mActivity;
        }
        if (null == mInflater) {
            mInflater = inflater;
        }
        this.container = container;

        rootView = mInflater.inflate(R.layout.fragment_detail, container, false);
        mFinalBitmap = FinalBitmap.create(mActivity);
        initView();
        if (!TextUtils.isEmpty(mPackageName))
            post(mPackageName);

        setHasOptionsMenu(true);
        return rootView;
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("DetailFragment"); //统计页面
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("DetailFragment");
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getFragmentManager().popBackStack();
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    private void initActionBar() {
        try {
            ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayShowCustomEnabled(false);
            actionBar.setTitle(mApkName);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化控件
     */
    private void initView() {
        mProgressButton = (ProgressButton) rootView.findViewById(R.id.detail_progress_button);
        mApkImgView = (ImageView) rootView.findViewById(R.id.detail_apkimg);
        mNameView = (TextView) rootView.findViewById(R.id.detail_name);
        mName1View = (TextView) rootView.findViewById(R.id.detail_name1);
        mApkSizeAndCompanyView = (TextView) rootView.findViewById(R.id.detail_apksize_and_company);
        mApkContentView = (TextView) rootView.findViewById(R.id.detail_content);
        mBackView = (ImageButton) rootView.findViewById(R.id.detail_back);
        mSearchView = (ImageButton) rootView.findViewById(R.id.detail_search);
        mDownloadView = (Button) rootView.findViewById(R.id.detail_download);
        mXingView = (RatingBar) rootView.findViewById(R.id.detail_xing);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.detail_progress);
        mImgLl = (LinearLayout) rootView.findViewById(R.id.detail_viewpager_img_ll);
        no_network = (LinearLayout) rootView.findViewById(R.id.no_network);
        refreshButton = (Button) rootView.findViewById(R.id.refresh_btn);

        mNameView.setText(mApkName);
        mName1View.setText(mApkName);
        mFinalBitmap.display(mApkImgView, mImgUrl);

        mProgressButton.setOnProgressButtonClickListener(new ProgressButton.OnProgressButtonClickListener() {
            @Override
            public void onClickListener() {
                if (!TextUtils.isEmpty(mPackageName)) {
                    ImplInfo implinfo = (ImplInfo)mProgressButton.getTag();
                    if (null != implinfo){
                        if (ImplInfo.ACTION_DOWNLOAD == implAgent.getAction(implinfo)) {
                            switch (implinfo.getStatus()) {
                                case Constant.STATUS_PENDING:
                                case Constant.STATUS_RUNNING:
                                    implAgent.pauseDownload(implinfo);
                                    break;
                                case Constant.STATUS_PAUSED:
                                    implAgent.resumeDownload(implinfo, implCallback);
                                    break;
                                default:
                                    implAgent.newDownload(implinfo,
                                            Constant.extenStorageDirPath,
                                            mApkName + ".apk",
                                            true,
                                            implCallback);
                                    break;
                            }
                        } else {
                            try {
                                mContext.startActivity(implAgent.getActionIntent(implinfo));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        });
        mBackView.setOnClickListener(this);
        mSearchView.setOnClickListener(this);
        mDownloadView.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.detail_back:
                getFragmentManager().popBackStack();
                break;
            case R.id.detail_download:
                break;
        }
    }

    /**
     * 应用详情网络请求
     *
     * @param mPackageName
     */
    private void post(String mPackageName) {
        FinalHttp mFinalHttp = new FinalHttp();
        AjaxParams params = new AjaxParams();
        params.put("appkey", AppliteUtils.getMitMetaDataValue(mActivity, Constant.META_DATA_MIT));
        params.put("packagename", mActivity.getPackageName());
        params.put("app", "applite");
        params.put("type", "detail");
        params.put("name", mPackageName);
        mFinalHttp.post(Constant.URL, params, new AjaxCallBack<Object>() {
            @Override
            public void onSuccess(Object o) {
                super.onSuccess(o);
                String result = (String) o;
                LogUtils.i(TAG, "应用详情网络请求成功，result:" + result);
                setData(result);
            }

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                super.onFailure(t, errorNo, strMsg);
                // 这里设置没有网络时的图片
                String result = strMsg;
                LogUtils.e(TAG, "应用详情网络请求失败，strMsg:" + strMsg);
                setDate1(result);
            }
        });
    }

    /**
     * 设置应用详细数据
     *
     * @param data
     */
    private void setData(String data) {
        try {
            String mViewPagerUrl = null;
            JSONObject object = new JSONObject(data);
            String app_key = object.getString("app_key");
            String info = object.getString("detail_info");

            JSONArray json = new JSONArray(info);
            if (json.length() != 0 && json != null) {
                for (int i = 0; i < json.length(); i++) {
                    JSONObject obj = new JSONObject(json.get(i).toString());
                    mPackageName = obj.getString("packageName");
                    mName = obj.getString("name");
                    mImgUrl = obj.getString("iconUrl");
                    String mVersionName = obj.getString("versionName");
                    mVersionCode = obj.getInt("versionCode");
                    String xing = obj.getString("rating");
                    mDownloadUrl = obj.getString("rDownloadUrl");
                    long size = obj.getLong("apkSize");
                    String mDownloadNumber = obj.getString("downloadTimes");
                    String content = obj.getString("description");
                    mViewPagerUrl = obj.getString("screenshotsUrl");

                    mNameView.setText(mName);
                    mName1View.setText(mName);
                    mXingView.setRating(Float.parseFloat(xing) / 2.0f);
                    mFinalBitmap.display(mApkImgView, mImgUrl);
                    mApkSizeAndCompanyView.setText(AppliteUtils.bytes2kb(size));
                    mApkContentView.setText(content);
                }
                mViewPagerUrlList = mViewPagerUrl.split(",");
                LogUtils.i(TAG, "应用图片URL地址：" + mViewPagerUrl);
                setPreViewImg();
            }

            ImplInfo implinfo = implAgent.getImplInfo(mPackageName,mPackageName,mVersionCode);
            implAgent.setImplCallback(implCallback,implinfo);
            mProgressButton.setText(implAgent.getActionText(implinfo));
            mProgressButton.setProgress(implAgent.getProgress(implinfo));
            mProgressButton.setTag(implinfo);
        } catch (JSONException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "应用详情JSON解析失败");
        }

    }

    /**
     * 设置无网络状态时的内容，以及点击刷新进行网络链接判断
     * @param date1
     */
    private void setDate1(String date1){
        no_network.setVisibility(View.VISIBLE);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                no_network.setVisibility(View.GONE);
                post(mPackageName);
            }
        });
    }

    /**
     * 设置应用介绍的图片
     */
    private void setPreViewImg() {
        FinalBitmap fb = FinalBitmap.create(mActivity);
//        fb.configLoadingImage(R.drawable.detail_default_img);
        for (int i = 0; i < mViewPagerUrlList.length; i++) {
            final View child = mInflater.inflate(R.layout.item_detail_viewpager_img, container, false);
            final ImageView img = (ImageView) child.findViewById(R.id.item_viewpager_img);
            mImgLl.addView(child);
            fb.display(img, mViewPagerUrlList[i]);
        }
//        fb.configLoadingImage(null);
    }

    class DetailImplCallback extends ImplListener {
        DetailImplCallback() {}

        @Override
        public void onStart(ImplInfo info) {
            super.onStart(info);
            refresh(info);
        }

        @Override
        public void onCancelled(ImplInfo info) {
            super.onCancelled(info);
            refresh(info);
        }

        @Override
        public void onLoading(ImplInfo info, long total, long current, boolean isUploading) {
            super.onLoading(info, total, current, isUploading);
            refresh(info);
        }

        @Override
        public void onSuccess(ImplInfo info, File file) {
            super.onSuccess(info, file);
            refresh(info);
        }

        @Override
        public void onFailure(ImplInfo info, Throwable t, String msg) {
            super.onFailure(info, t, msg);
            refresh(info);
        }

        @Override
        public void onInstallSuccess(ImplInfo info) {
            super.onInstallSuccess(info);
            refresh(info);
        }

        @Override
        public void onInstalling(ImplInfo info) {
            super.onInstalling(info);
            refresh(info);
        }

        @Override
        public void onInstallFailure(ImplInfo info, int errorCode) {
            super.onInstallFailure(info, errorCode);
            refresh(info);
        }

        @Override
        public void onUninstallSuccess(ImplInfo info) {
            super.onUninstallSuccess(info);
            refresh(info);
        }

        @Override
        public void onUninstalling(ImplInfo info) {
            super.onUninstalling(info);
            refresh(info);
        }

        @Override
        public void onUninstallFailure(ImplInfo info, int errorCode) {
            super.onUninstallFailure(info, errorCode);
            refresh(info);
        }

        private void refresh(ImplInfo info){
            mProgressButton.setText(implAgent.getActionText(info));
            mProgressButton.setProgress(implAgent.getProgress(info));
        }
    }

}