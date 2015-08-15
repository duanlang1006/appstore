package com.mit.applite.main;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
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
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.callback.BitmapLoadCallBack;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;
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
import net.tsz.afinal.bitmap.display.Displayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DetailFragment extends OSGIBaseFragment implements View.OnClickListener {

    private static final String TAG = "DetailFragment";
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
    private TextView mUpdateLogView;
    private String mDescription;
    private String mUpdateLog;
    private ImageView mOpenIntroduceView;
    private ImageView mOpenUpdateLogView;
    private int DEFAULT_MAX_LINE_COUNT = 3;//应用介绍、更新日志默认最多显示3行
    private int COLLAPSIBLE_STATE_NONE = 0;//少于3行状态
    private int COLLAPSIBLE_STATE_SHRINKUP = 1;//收缩状态
    private int COLLAPSIBLE_STATE_SPREAD = 2;//展开状态
    private int CONTENT_STATE = COLLAPSIBLE_STATE_NONE;
    private int UPDATE_LOG_STATE = COLLAPSIBLE_STATE_NONE;
    private Handler mHandler = new Handler();

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

    public DetailFragment() {
        super();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        implAgent = ImplAgent.getInstance(mActivity.getApplicationContext());
        implCallback = new DetailImplCallback();
        Bundle params = getArguments();
        if (null != params) {
            mPackageName = params.getString("packageName");
            mApkName = params.getString("name");
            mImgUrl = params.getString("imgUrl");
        }
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

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            LogUtils.i(TAG, "隐藏详情页面");
        } else {
            LogUtils.i(TAG, "显示详情页面");
            initActionBar();
        }
    }

    private void initActionBar() {
        try {
            ActionBar actionBar = ((ActionBarActivity) mActivity).getSupportActionBar();
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

        mOpenIntroduceView = (ImageView) rootView.findViewById(R.id.detail_open_introduce_content);
        mOpenUpdateLogView = (ImageView) rootView.findViewById(R.id.detail_open_update_log);
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
        mApkContentView.setOnClickListener(this);
        mUpdateLogView.setOnClickListener(this);
        refreshButton.setOnClickListener(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main_detail, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        if (null != item) {
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.action_search == item.getItemId()) {
            ((OSGIServiceHost) mActivity).jumptoSearch(true);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.refresh_btn) {
            no_network.setVisibility(View.GONE);
            post(mPackageName);
        } else if (v.getId() == R.id.detail_content) {
            if (CONTENT_STATE == COLLAPSIBLE_STATE_SHRINKUP) {
                LogUtils.i(TAG, "应用介绍展开");
                mApkContentView.setMaxLines(50);
                CONTENT_STATE = COLLAPSIBLE_STATE_SPREAD;
                mOpenIntroduceView.setImageBitmap(BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.desc_less));
            } else if (CONTENT_STATE == COLLAPSIBLE_STATE_SPREAD) {
                LogUtils.i(TAG, "应用介绍收缩");
                mApkContentView.setMaxLines(DEFAULT_MAX_LINE_COUNT);
                CONTENT_STATE = COLLAPSIBLE_STATE_SHRINKUP;
                mOpenIntroduceView.setImageBitmap(BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.desc_more));
            }
        } else if (v.getId() == R.id.detail_update_log) {
            if (UPDATE_LOG_STATE == COLLAPSIBLE_STATE_SHRINKUP) {
                LogUtils.i(TAG, "更新日志展开");
                mUpdateLogView.setMaxLines(Integer.MAX_VALUE);
                UPDATE_LOG_STATE = COLLAPSIBLE_STATE_SPREAD;
                mOpenUpdateLogView.setImageBitmap(BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.desc_less));
            } else if (UPDATE_LOG_STATE == COLLAPSIBLE_STATE_SPREAD) {
                LogUtils.i(TAG, "更新日志收缩");
                mUpdateLogView.setMaxLines(DEFAULT_MAX_LINE_COUNT);
                UPDATE_LOG_STATE = COLLAPSIBLE_STATE_SHRINKUP;
                mOpenUpdateLogView.setImageBitmap(BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.desc_more));
            }
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
                    mApkContentView.setText(mDescription);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mApkContentView.getLineCount() <= DEFAULT_MAX_LINE_COUNT) {
                                mOpenIntroduceView.setVisibility(View.GONE);
                            } else {
                                mApkContentView.setLines(DEFAULT_MAX_LINE_COUNT);
                                CONTENT_STATE = COLLAPSIBLE_STATE_SHRINKUP;
                            }
                        }
                    }, 500);

                    LogUtils.i(TAG, "更新日志：" + mDescription);
                    if (TextUtils.isEmpty(mUpdateLog)) {
                        mUpdateLogView.setText(mActivity.getResources().getText(R.string.no_update_log));
                    } else {
                        mUpdateLogView.setText(mUpdateLog);
                    }
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mUpdateLogView.getLineCount() <= DEFAULT_MAX_LINE_COUNT) {
                                mOpenUpdateLogView.setVisibility(View.GONE);
                            } else {
                                mUpdateLogView.setLines(DEFAULT_MAX_LINE_COUNT);
                                UPDATE_LOG_STATE = COLLAPSIBLE_STATE_SHRINKUP;
                            }
                        }
                    }, 500);
                }
                mViewPagerUrlList = mViewPagerUrl.split(",");
                setPreViewImg();
            }

            ImplInfo implinfo = implAgent.getImplInfo(mPackageName, mPackageName, mVersionCode);
            if (null != implinfo) {
                implAgent.setImplCallback(implCallback, implinfo);
                implinfo.setDownloadUrl(mDownloadUrl).setIconUrl(mImgUrl).setTitle(mName);
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

    /**
     * 设置应用介绍的图片
     */
    private void setPreViewImg() {
        BitmapUtils bitmapUtils = new BitmapUtils(mActivity);
        for (int i = 0; i < mViewPagerUrlList.length; i++) {
            LogUtils.i(TAG, "应用图片URL地址：" + mViewPagerUrlList[i]);
            final View child = mActivity.getLayoutInflater().inflate(R.layout.item_detail_viewpager_img, container, false);
            final ImageView img = (ImageView) child.findViewById(R.id.item_viewpager_img);
            mImgLl.addView(child);
            bitmapUtils.display(img, mViewPagerUrlList[i], new BitmapLoadCallBack<ImageView>() {
                @Override
                public void onLoadCompleted(ImageView imageView, String s, Bitmap bitmap, BitmapDisplayConfig bitmapDisplayConfig, BitmapLoadFrom bitmapLoadFrom) {
                    if (bitmap.getWidth() > bitmap.getHeight()) {
                        imageView.setImageBitmap(DetailUtils.rotateBitmap(bitmap, 90));
                    } else {
                        imageView.setImageBitmap(DetailUtils.rotateBitmap(bitmap, 0));
                    }
                }

                @Override
                public void onLoadFailed(ImageView imageView, String s, Drawable drawable) {
                    imageView.setBackground(mActivity.getResources().getDrawable(R.drawable.detail_default_img));
                }
            });
        }
        mLoadLayout.setVisibility(View.GONE);
        mDataLayout.setVisibility(View.VISIBLE);
    }

//    /**
//     * 设置应用介绍的图片
//     */
//    private void setPreViewImg() {
//        mFinalBitmap.configDisplayer(new Displayer() {
//            @Override
//            public void loadCompletedisplay(View view, Bitmap bitmap, net.tsz.afinal.bitmap.core.BitmapDisplayConfig bitmapDisplayConfig) {
//                if (bitmap.getWidth() > bitmap.getHeight()) {
//                    ((ImageView)view).setImageBitmap(DetailUtils.rotateBitmap(bitmap, 90));
//                } else {
//                    ((ImageView)view).setImageBitmap(bitmap);
//                }
//            }
//
//            @Override
//            public void loadFailDisplay(View view, Bitmap bitmap) {
//
//            }
//        });
//        for (int i = 0; i < mViewPagerUrlList.length; i++) {
//            LogUtils.i(TAG, "应用图片URL地址：" + mViewPagerUrlList[i]);
//            final View child = mInflater.inflate(R.layout.item_detail_viewpager_img, container, false);
//            final ImageView img = (ImageView) child.findViewById(R.id.item_viewpager_img);
//            mImgLl.addView(child);
//            mFinalBitmap.display(img, mViewPagerUrlList[i]);
//        }
//        mLoadLayout.setVisibility(View.GONE);
//        mDataLayout.setVisibility(View.VISIBLE);
//    }

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