package com.mit.applite.main;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.applite.common.AppliteUtils;
import com.applite.common.BitmapHelper;
import com.applite.common.Constant;
import com.applite.common.LogUtils;
import com.applite.view.ProgressButton;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.mit.applite.utils.DetailUtils;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplInfo;
import com.mit.impl.ImplChangeCallback;
import com.osgi.extra.OSGIBaseFragment;
import com.osgi.extra.OSGIServiceHost;
import com.umeng.analytics.MobclickAgent;

import net.tsz.afinal.FinalBitmap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DetailFragment extends OSGIBaseFragment implements View.OnClickListener {

    private static final String TAG = "DetailFragment";
    private Activity mActivity;
    private View rootView;
    private String mApkName;
    private TextView mName1View;
    private TextView mApkSizeAndCompanyView;
    private TextView mApkContentView;
    private RatingBar mXingView;
    private ImageView mApkImgView;
    private String mViewPagerUrlList[] = null;
    private ViewGroup container;
    private LinearLayout mImgLl;
    private String mPackageName;
    private String mName;
    private String mImgUrl;
    private ProgressButton mProgressButton;
    private int mVersionCode;
    private RelativeLayout no_network;
    private Button refreshButton;
    private ImplAgent implAgent;
    private ImplChangeCallback implCallback;
    private BitmapUtils mBitmapUtil;
    private String mDownloadUrl;
    private LinearLayout mDataLayout;
    private LinearLayout mLoadLayout;
    private ImageView mLoadView;
    private Animation LoadingAnimation;
    private FinalBitmap mFinalBitmap;
    private LinearLayout mOpenIntroduceLayout;
    private LinearLayout mOpenUpdateLogLayout;
    private TextView mUpdateLogView;
    private String mDescription;
    private String mUpdateLog;

    public static OSGIBaseFragment newInstance(Fragment fg, Bundle params) {
        return new DetailFragment(fg, params);
    }

//    public static OSGIBaseFragment newInstance(OSGIServiceHost host,String packageName,String name,String imgUrl){
//        Fragment fg = null;
//        if (null != host){
//            Bundle b = new Bundle();
//            b.putString("packageName",packageName);
//            b.putString("name",name);
//            b.putString("imgUrl",imgUrl);
//            fg = host.newFragment(
//                    BundleContextFactory.getInstance().getBundleContext(),
//                    Constant.OSGI_SERVICE_DETAIL_FRAGMENT,DetailFragment.class.getName(),b);
//        }
//        return fg;
//    }

    private DetailFragment(Fragment mFragment, Bundle params) {
        super(mFragment, params);
        if (null != params) {
            mPackageName = params.getString("packageName");
            mApkName = params.getString("name");
            mImgUrl = params.getString("imgUrl");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        implAgent = ImplAgent.getInstance(mActivity.getApplicationContext());
        implCallback = new DetailImplCallback();
        LogUtils.i(TAG, "mApkName:" + mApkName + "------mPackageName:" + mPackageName + "------mImgUrl:" + mImgUrl);
        initActionBar();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBitmapUtil = BitmapHelper.getBitmapUtils(mActivity.getApplicationContext());
        mFinalBitmap = FinalBitmap.create(mActivity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.container = container;

        rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        initView();
        if (!TextUtils.isEmpty(mPackageName))
            post(mPackageName);

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
        //加载中控件
        mLoadLayout = (LinearLayout) rootView.findViewById(R.id.detail_loading_layout);
        mLoadView = (ImageView) rootView.findViewById(R.id.detail_loading_img);
        //旋转动画
        LoadingAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.loading);
        LinearInterpolator lin = new LinearInterpolator();
        LoadingAnimation.setInterpolator(lin);
        mLoadView.startAnimation(LoadingAnimation);

        //下载按钮
        LinearLayout mDownloadLayout = (LinearLayout) rootView.findViewById(R.id.detail_download_layout);
        mProgressButton = ProgressButton.inflate(mActivity, container, false);
        mDownloadLayout.addView(mProgressButton);

        mDataLayout = (LinearLayout) rootView.findViewById(R.id.detail_data_layout);
        mApkImgView = (ImageView) rootView.findViewById(R.id.detail_apkimg);
        mName1View = (TextView) rootView.findViewById(R.id.detail_name1);
        mApkSizeAndCompanyView = (TextView) rootView.findViewById(R.id.detail_apksize_and_company);
        mApkContentView = (TextView) rootView.findViewById(R.id.detail_content);
        mXingView = (RatingBar) rootView.findViewById(R.id.detail_xing);
        mImgLl = (LinearLayout) rootView.findViewById(R.id.detail_viewpager_img_ll);
        no_network = (RelativeLayout) rootView.findViewById(R.id.no_network);
        refreshButton = (Button) rootView.findViewById(R.id.refresh_btn);

        mOpenIntroduceLayout = (LinearLayout) rootView.findViewById(R.id.detail_open_introduce_content_layout);
        mOpenUpdateLogLayout = (LinearLayout) rootView.findViewById(R.id.detail_open_update_log_layout);
        mUpdateLogView = (TextView) rootView.findViewById(R.id.detail_update_log);

        mName1View.setText(mApkName);

        mBitmapUtil.configDefaultLoadingImage(mActivity.getResources().getDrawable(R.drawable.apk_icon_defailt_img));
        mBitmapUtil.configDefaultLoadFailedImage(mActivity.getResources().getDrawable(R.drawable.apk_icon_defailt_img));
        mBitmapUtil.display(mApkImgView, mImgUrl);

        mProgressButton.setOnProgressButtonClickListener(new ProgressButton.OnProgressButtonClickListener() {
            @Override
            public void onClickListener() {
                if (!TextUtils.isEmpty(mPackageName)) {
                    mProgressButton.setBackgroundColor(mActivity.getResources().getColor(R.color.progress_background));
                    ImplInfo implinfo = (ImplInfo) mProgressButton.getTag();
                    if (null != implinfo) {
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
                                mActivity.startActivity(implAgent.getActionIntent(implinfo));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        });
        mOpenIntroduceLayout.setOnClickListener(this);
        mOpenUpdateLogLayout.setOnClickListener(this);
        refreshButton.setOnClickListener(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main_detail,menu);
        MenuItem item = menu.findItem(R.id.action_search);
        if (null != item){
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.action_search == item.getItemId()){
            DetailUtils.launchSearchFragment((OSGIServiceHost)mActivity);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.refresh_btn) {
            no_network.setVisibility(View.GONE);
            post(mPackageName);
        }else if (v.getId() == R.id.detail_open_introduce_content_layout) {
            mOpenIntroduceLayout.setVisibility(View.GONE);
            mApkContentView.setText(mDescription);
        }else if (v.getId() == R.id.detail_open_update_log_layout) {
            mOpenUpdateLogLayout.setVisibility(View.GONE);
            mUpdateLogView.setText(mUpdateLog);
        }
    }

    /**
     * 应用详情网络请求
     *
     * @param mPackageName
     */
    private void post(String mPackageName) {
        RequestParams params = new RequestParams();
        params.addBodyParameter("appkey", AppliteUtils.getMitMetaDataValue(mActivity, Constant.META_DATA_MIT));
        params.addBodyParameter("packagename", mActivity.getPackageName());
        params.addBodyParameter("type", "detail");
        params.addBodyParameter("name", mPackageName);
        HttpUtils mHttpUtils = new HttpUtils();
        mHttpUtils.send(HttpRequest.HttpMethod.POST, Constant.URL, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                LogUtils.i(TAG, "应用详情网络请求成功，result:" + responseInfo.result);
                setData(responseInfo.result);
            }

            @Override
            public void onFailure(HttpException e, String s) {
                LogUtils.e(TAG, "应用详情网络请求失败:" + s);
                // 这里设置没有网络时的图片
                mLoadLayout.setVisibility(View.GONE);
                mDataLayout.setVisibility(View.GONE);
                no_network.setVisibility(View.VISIBLE);
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
                    mDescription = obj.getString("description");
                    mViewPagerUrl = obj.getString("screenshotsUrl");
                    String developer = obj.getString("developer");
                    mUpdateLog = obj.getString("updateInfo");

                    mName1View.setText(mName);
                    mXingView.setRating(Float.parseFloat(xing) / 2.0f);
                    mBitmapUtil.display(mApkImgView, mImgUrl);
                    mApkSizeAndCompanyView.setText(AppliteUtils.bytes2kb(size) + " | " + developer);

                    LogUtils.i(TAG, "应用介绍：" + mDescription);
                    if (mDescription.length() > 150) {
                        mApkContentView.setText(mDescription.substring(0, 150) + "...");
                    } else {
                        mApkContentView.setText(mDescription);
                        mOpenIntroduceLayout.setVisibility(View.GONE);
                    }
                    LogUtils.i(TAG, "更新日志：" + mDescription);
                    if (mUpdateLog.length() > 150) {
                        mUpdateLogView.setText(mUpdateLog.substring(0, 150) + "...");
                    } else {
                        mUpdateLogView.setText(mUpdateLog);
                        mOpenUpdateLogLayout.setVisibility(View.GONE);
                    }
                }
                mViewPagerUrlList = mViewPagerUrl.split(",");
                setPreViewImg();
            }

            ImplInfo implinfo = implAgent.getImplInfo(mPackageName, mPackageName, mVersionCode);
            if (null != implinfo) {
                implAgent.setImplCallback(implCallback, implinfo);
                mProgressButton.setText(implAgent.getActionText(implinfo));
                mProgressButton.setProgress(implAgent.getProgress(implinfo));
                if (mProgressButton.getProgress() == 0) {
                    mProgressButton.setBackgroundColor(mActivity.getResources().getColor(R.color.progress_foreground));
                } else {
                    mProgressButton.setBackgroundColor(mActivity.getResources().getColor(R.color.progress_background));
                }
                mProgressButton.setTag(implinfo);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "应用详情JSON解析失败");
        }

    }

//    /**
//     * 设置应用介绍的图片
//     */
//    private void setPreViewImg() {
//        BitmapUtils bitmapUtils = new BitmapUtils(mActivity);
//        for (int i = 0; i < mViewPagerUrlList.length; i++) {
//            LogUtils.i(TAG, "应用图片URL地址：" + mViewPagerUrlList[i]);
//            final View child = mInflater.inflate(R.layout.item_detail_viewpager_img, container, false);
//            final ImageView img = (ImageView) child.findViewById(R.id.item_viewpager_img);
//            mImgLl.addView(child);
//            bitmapUtils.display(img, mViewPagerUrlList[i], new BitmapLoadCallBack<ImageView>() {
//                @Override
//                public void onLoadCompleted(ImageView imageView, String s, Bitmap bitmap, BitmapDisplayConfig bitmapDisplayConfig, BitmapLoadFrom bitmapLoadFrom) {
//                    imageView.setBackground(new BitmapDrawable(bitmap));
//                }
//
//                @Override
//                public void onLoadFailed(ImageView imageView, String s, Drawable drawable) {
//                    imageView.setBackground(mContext.getResources().getDrawable(R.drawable.detail_default_img));
//                }
//            });
//        }
//        mLoadLayout.setVisibility(View.GONE);
//        mDataLayout.setVisibility(View.VISIBLE);
//    }

    /**
     * 设置应用介绍的图片
     */
    private void setPreViewImg() {
        // 下面添加参数，显示读出时内容和读取失败时的内容
        for (int i = 0; i < mViewPagerUrlList.length; i++) {
            LogUtils.i(TAG, "应用图片URL地址：" + mViewPagerUrlList[i]);
            final View child = mActivity.getLayoutInflater().inflate(R.layout.item_detail_viewpager_img, container, false);
            final ImageView img = (ImageView) child.findViewById(R.id.item_viewpager_img);
            mImgLl.addView(child);
            mFinalBitmap.display(img, mViewPagerUrlList[i]);
        }
        mLoadLayout.setVisibility(View.GONE);
        mDataLayout.setVisibility(View.VISIBLE);
    }

    class DetailImplCallback implements ImplChangeCallback {
        @Override
        public void onChange(ImplInfo implInfo) {
            refresh(implInfo);
        }

        private void refresh(ImplInfo info) {
            LogUtils.d(TAG, "refresh" + implAgent.getActionText(info) + "," + info.getStatus());
            mProgressButton.setText(implAgent.getActionText(info));
            mProgressButton.setProgress(implAgent.getProgress(info));
        }
    }

}