package com.mit.applite.main;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.applite.bean.ApkBean;
import com.applite.common.AppliteUtils;
import com.applite.common.BitmapHelper;
import com.applite.common.Constant;
import com.applite.common.LogUtils;
import com.applite.sharedpreferences.AppliteSPUtils;
import com.applite.similarview.SimilarAdapter;
import com.applite.similarview.SimilarBean;
import com.applite.similarview.SimilarView;
import com.applite.view.FlowLayout;
import com.applite.view.ProgressButton;
import com.google.gson.Gson;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.callback.BitmapLoadCallBack;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;
import com.mit.afinal.FinalHttp;
import com.mit.afinal.http.AjaxCallBack;
import com.mit.afinal.http.AjaxParams;
import com.mit.applite.bean.DetailData;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplChangeCallback;
import com.mit.impl.ImplHelper;
import com.mit.impl.ImplInfo;
import com.osgi.extra.OSGIBaseFragment;
import com.osgi.extra.OSGIServiceHost;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DetailFragment extends OSGIBaseFragment implements View.OnClickListener, SimilarAdapter.SimilarAPKDetailListener {

    private static final String TAG = "DetailFragment";
    private View rootView;
    private TextView mName1View;
    private TextView mApkSizeAndCompanyView;
    private TextView mApkContentView;
    private RatingBar mXingView;
    private ImageView mApkImgView;
    private ViewGroup container;
    private LinearLayout mImgLl;

    //APK信息
    private List<ApkBean> mApkDatas = new ArrayList<ApkBean>();
    private String mApkName;
    private String mPackageName;
    private String mImgUrl;
    private String mViewPagerUrl;
    private String mApkTag;
    private int mVersionCode;
    private String mRating;
    private long mApkSize;
    private String mDescription;
    private String mDownloadUrl;
    private String mDeveloper;

    private String mBoxlabelValue;
    private ProgressButton mProgressButton;
    private RelativeLayout no_network;
    private Button refreshButton;
    private ImplAgent implAgent;
    private ImplChangeCallback implCallback;
    private BitmapUtils mBitmapUtil;
    private LinearLayout mDataLayout;
    //    private LinearLayout mLoadLayout;
//    private ImageView mLoadView;
//    private Animation LoadingAnimation;
    private ImageView mOpenIntroduceView;
    private int DEFAULT_MAX_LINE_COUNT = 3;//应用介绍、更新日志默认最多显示3行
    private int COLLAPSIBLE_STATE_NONE = 0;//少于3行状态
    private int COLLAPSIBLE_STATE_SHRINKUP = 1;//收缩状态
    private int COLLAPSIBLE_STATE_SPREAD = 2;//展开状态
    private int CONTENT_STATE = COLLAPSIBLE_STATE_NONE;
    private Handler mHandler = new Handler();
    private LinearLayout mHorDefaultLayout;
    private LinearLayout mTagStateLayout;

    private List<SimilarBean> mSimilarData = new ArrayList<SimilarBean>();
    private SimilarView mSimilarView;
    private SimilarAdapter mSimilarAdapter;
    private ImplInfo mImplInfo;
    private FlowLayout mFlowLayout;

    private int points;
    private boolean luckyflag = false;
    private String mPostReturnData;

    public static Bundle newBundle(String packageName, String name, String imgUrl, int versionCode, String boxlabelvalue) {
        Bundle b = new Bundle();
        b.putString("packageName", packageName);
        b.putString("name", name);
        b.putString("imgUrl", imgUrl);
        b.putInt("versionCode", versionCode);
        b.putString("boxlabelvalue", boxlabelvalue);
        return b;
    }

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
            mVersionCode = params.getInt("versionCode");
            mBoxlabelValue = params.getString("boxlabelvalue");
        }
        LogUtils.i(TAG, "mApkName:" + mApkName + "------mPackageName:" + mPackageName + "------mImgUrl:" + mImgUrl + "------mVersionCode:" + mVersionCode);
        LogUtils.i(TAG, "mBoxlabelValue:" + mBoxlabelValue);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBitmapUtil = BitmapHelper.getBitmapUtils(mActivity.getApplicationContext());

        if (!TextUtils.isEmpty(mBoxlabelValue)) {
            luckyflag = true;
            points = Integer.parseInt(mBoxlabelValue);
        } else
            luckyflag = false;
        LogUtils.d(TAG, "points = " + points + " luckyflag = " + luckyflag);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.container = container;
        rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        initView();
        initActionBar();

        setProgressButtonState();
        if (null == mPostReturnData) {
            post(mPackageName);
        } else {
            setData(mPostReturnData);
        }
        return rootView;
    }

    public void onResume() {
        super.onResume();
        setProgressButtonState();
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
                    if (!isHomeExist()) {
                        ((OSGIServiceHost) mActivity).jumpto(Constant.OSGI_SERVICE_MAIN_FRAGMENT, null, null, false);
                        return true;
                    }
                }
                return false;
            }
        });
    }

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
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
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
//        mLoadLayout = (LinearLayout) rootView.findViewById(R.id.detail_loading_layout);
//        mLoadView = (ImageView) rootView.findViewById(R.id.detail_loading_img);
//        //旋转动画
//        LoadingAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.loading);
//        LinearInterpolator lin = new LinearInterpolator();
//        LoadingAnimation.setInterpolator(lin);
//        mLoadView.startAnimation(LoadingAnimation);

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

        mHorDefaultLayout = (LinearLayout) rootView.findViewById(R.id.detail_hor_default_layout);

        mSimilarView = (SimilarView) rootView.findViewById(R.id.similar_view);
        mFlowLayout = (FlowLayout) rootView.findViewById(R.id.detail_flowlayout);
//        mSimilarView.setVisibility(View.INVISIBLE);

        mTagStateLayout = (LinearLayout) rootView.findViewById(R.id.detail_state_tag_layout);

        mName1View.setText(mApkName);

        mBitmapUtil.configDefaultLoadingImage(mActivity.getResources().getDrawable(R.drawable.apk_icon_defailt_img));
        mBitmapUtil.configDefaultLoadFailedImage(mActivity.getResources().getDrawable(R.drawable.apk_icon_defailt_img));
        if (AppliteUtils.isLoadNetworkBitmap(mActivity))
            mBitmapUtil.display(mApkImgView, mImgUrl);

        mProgressButton.setOnProgressButtonClickListener(new ProgressButton.OnProgressButtonClickListener() {
            @Override
            public void onClickListener() {
                if (!TextUtils.isEmpty(mPackageName)) {
                    mProgressButton.setBackgroundColor(mActivity.getResources().getColor(R.color.progress_background));
                    ImplInfo implinfo = (ImplInfo) mProgressButton.getTag();

                    if (implinfo.getStatus() == ImplInfo.STATUS_PRIVATE_INSTALLING) {
                        LogUtils.d(TAG, "正在静默安装");
//                        mProgressButton.setEnabled(false);
                        mProgressButton.setText("正在安装");
                        mProgressButton.setBackgroundColor(mActivity.getResources().getColor(R.color.progress_background));
                        return;
                    }

                    if (null != implinfo) {
                        ImplHelper.onClick(mActivity,
                                implinfo,
                                mDownloadUrl,
                                mApkName,
                                mImgUrl,
                                Environment.getExternalStorageDirectory() + File.separator + Constant.extenStorageDirPath + mApkName + ".apk",
                                null,
                                implCallback);
                    }
                }
            }
        });
        mOpenIntroduceView.setOnClickListener(this);
        mApkContentView.setOnClickListener(this);
        refreshButton.setOnClickListener(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        LogUtils.d(TAG, "onCreateOptionsMenu");
        MenuItem item = menu.findItem(R.id.action_search);
        if (null != item) {
            return;
        }

//        inflater.inflate(R.menu.menu_main_detail, menu);
//        item = menu.findItem(R.id.action_search);
//        if (null != item) {
//            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
//        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.action_search == item.getItemId()) {
            ((OSGIServiceHost) mActivity).jumptoSearch(null, true, null, null, null);
            return true;
        }
        if (android.R.id.home == item.getItemId()) {
            if (!isHomeExist()) {
                ((OSGIServiceHost) mActivity).jumpto(Constant.OSGI_SERVICE_MAIN_FRAGMENT, null, null, false);
                return false;
            }

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyOptionsMenu() {
        LogUtils.d(TAG, "onDestroyOptionsMenu");
        super.onDestroyOptionsMenu();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.refresh_btn) {
            no_network.setVisibility(View.GONE);
            mDataLayout.setVisibility(View.VISIBLE);
//            mLoadLayout.setVisibility(View.VISIBLE);
            post(mPackageName);
        } else if (v.getId() == R.id.detail_content || v.getId() == R.id.detail_open_introduce_content) {
            if (CONTENT_STATE == COLLAPSIBLE_STATE_SHRINKUP) {
                LogUtils.i(TAG, "应用介绍展开");
                mApkContentView.setMaxLines(Integer.MAX_VALUE);
                CONTENT_STATE = COLLAPSIBLE_STATE_SPREAD;
                mOpenIntroduceView.setImageBitmap(BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.desc_less));
            } else if (CONTENT_STATE == COLLAPSIBLE_STATE_SPREAD) {
                LogUtils.i(TAG, "应用介绍收缩");
                mApkContentView.setMaxLines(DEFAULT_MAX_LINE_COUNT);
                CONTENT_STATE = COLLAPSIBLE_STATE_SHRINKUP;
                mOpenIntroduceView.setImageBitmap(BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.desc_more));
            }
        }
    }

    /**
     * 设置ProgressButton的状态
     */
    private void setProgressButtonState() {
        if (mVersionCode != 0) {
            mImplInfo = implAgent.getImplInfo(mPackageName, mPackageName, mVersionCode);
            if (null != mImplInfo) {
                ImplInfo.ImplRes res = mImplInfo.getImplRes();
                implAgent.bindImplCallback(implCallback, mImplInfo);
                mProgressButton.setText(res.getActionText());
                mProgressButton.setProgress(mImplInfo.getProgress());
                if (mProgressButton.getProgress() == 0) {
                    mProgressButton.setBackgroundColor(mActivity.getResources().getColor(R.color.progress_foreground));
                } else {
                    mProgressButton.setBackgroundColor(mActivity.getResources().getColor(R.color.progress_background));
                }

                if (mImplInfo.getStatus() == ImplInfo.STATUS_PRIVATE_INSTALLING) {
                    mProgressButton.setText(res.getStatusText());
                    mProgressButton.setBackgroundColor(mActivity.getResources().getColor(R.color.progress_background));
                }

                mProgressButton.setTag(mImplInfo);
            }
        }
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
     * 应用详情网络请求
     *
     * @param mPackageName
     */
    private void post(String mPackageName) {
//        RequestParams params = new RequestParams();
//        params.addBodyParameter("appkey", AppliteUtils.getMitMetaDataValue(mActivity, Constant.META_DATA_MIT));
//        params.addBodyParameter("packagename", mActivity.getPackageName());
//        params.addBodyParameter("type", "detail");
//        params.addBodyParameter("protocol_version", Constant.PROTOCOL_VERSION);
//        params.addBodyParameter("name", mPackageName);
//        HttpUtils mHttpUtils = new HttpUtils();
//        mHttpUtils.send(HttpRequest.HttpMethod.POST, Constant.URL, params, new RequestCallBack<String>() {
//            @Override
//            public void onSuccess(ResponseInfo<String> responseInfo) {
//                LogUtils.i(TAG, "应用详情网络请求成功:" + responseInfo.result);
//                setData(responseInfo.result);
//            }
//
//            @Override
//            public void onFailure(HttpException e, String s) {
//                LogUtils.e(TAG, "应用详情网络请求失败:" + s);
//                // 这里设置没有网络时的图片
////                mLoadLayout.setVisibility(View.GONE);
////                mDataLayout.setVisibility(View.GONE);
////                no_network.setVisibility(View.VISIBLE);
//            }
//        });

        AjaxParams params = new AjaxParams();
        params.put("appkey", AppliteUtils.getMitMetaDataValue(mActivity, Constant.META_DATA_MIT));
        params.put("packagename", mActivity.getPackageName());
        params.put("type", "detail");
        params.put("protocol_version", Constant.PROTOCOL_VERSION);
        params.put("name", mPackageName);
        FinalHttp mFinalHttp = new FinalHttp();
        mFinalHttp.post(Constant.URL, params, new AjaxCallBack<String>() {
            @Override
            public void onSuccess(String responseInfo) {
                LogUtils.i(TAG, "应用详情网络请求成功:" + responseInfo);
                mPostReturnData = responseInfo;
                setData(responseInfo);
            }

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                LogUtils.e(TAG, "应用详情网络请求失败:" + strMsg);
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
            mSimilarData.clear();
            mApkDatas.clear();
            if (null != mSimilarAdapter)
                mSimilarAdapter.notifyDataSetChanged();

            Gson gson = new Gson();
            DetailData detailData = gson.fromJson(data, DetailData.class);
            if (null != detailData) {
                int app_key = detailData.getApp_key();
                mSimilarData = detailData.getSimilar_info();
                LogUtils.i(TAG, "应用详情similar_info:" + mSimilarData);
                if (null == mSimilarAdapter)
                    mSimilarAdapter = new MySimilarAdapter(mActivity);
                mSimilarAdapter.setData(mSimilarData, this, 4);
                mSimilarView.setAdapter(mSimilarAdapter);

                mApkDatas = detailData.getDetail_info();
                LogUtils.i(TAG, "应用详情detail_info:" + mApkDatas);
                ApkBean bean = mApkDatas.get(0);
                mPackageName = bean.getPackageName();
                mApkName = bean.getName();
                mImgUrl = bean.getIconUrl();
                mVersionCode = bean.getVersionCode();
                mRating = bean.getRating();
                mDownloadUrl = bean.getrDownloadUrl();
                mApkSize = bean.getApkSize();
                mDescription = bean.getDescription();
                mViewPagerUrl = bean.getScreenshotsUrl();
                mApkTag = bean.getTag();
                mDeveloper = bean.getDeveloper();
                String mVersionName = bean.getVersionName();
                String mDownloadNumber = bean.getDownloadTimes();
                String mUpdateLog = bean.getUpdateInfo();

                Bitmap bmp = BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.ratingbar_star_small_off_light);
                mXingView.setMinimumHeight(bmp.getHeight());
                mXingView.setRating(Float.parseFloat(mRating) / 2.0f);
                mApkSizeAndCompanyView.setText(AppliteUtils.bytes2kb(mApkSize) + " | " + mDeveloper);
                if (TextUtils.isEmpty(mDescription)) {
                    mApkContentView.setText(mActivity.getResources().getText(R.string.no_app_detail));
                } else {
                    mApkContentView.setText(mDescription);
                }
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
                setPreViewImg(mViewPagerUrl);
                setApkTag(mApkTag);
                if (null == mImplInfo)
                    setProgressButtonState();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置标签
     */
    private void setApkTag(String apkTag) {
        LogUtils.d(TAG, "Tag：" + apkTag);
        if (TextUtils.isEmpty(apkTag)) {
            mTagStateLayout.setVisibility(View.GONE);
        } else {
            mTagStateLayout.setVisibility(View.VISIBLE);
            String[] mApkTagList = apkTag.split(",");
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);//定义一个LayoutParams
            layoutParams.setMargins(0, 0, 0, 10);
            for (int i = 0; i < mApkTagList.length; i++) {
                final View child = mActivity.getLayoutInflater().inflate(R.layout.item_apk_tag_layout, container, false);
                final TextView mTagView = (TextView) child.findViewById(R.id.item_tag_tv);
                child.setLayoutParams(layoutParams);
                mTagView.setText(mApkTagList[i]);
                mTagView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((OSGIServiceHost) mActivity).jumptoSearch(mTagView.getText().toString(), true, null, null, null);
                    }
                });
                mFlowLayout.addView(child);
            }
        }
    }

    /**
     * 设置应用介绍的图片
     */
    private void setPreViewImg(String mViewPagerUrl) {
        final Matrix matrix = new Matrix();
        matrix.reset();
        matrix.setRotate(90);

        String[] mViewPagerUrlList = mViewPagerUrl.split(",");
        mHorDefaultLayout.setVisibility(View.GONE);
        BitmapUtils bitmapUtils = new BitmapUtils(mActivity);
        for (int i = 0; i < mViewPagerUrlList.length; i++) {
            LogUtils.i(TAG, "应用图片URL地址：" + mViewPagerUrlList[i]);
            final View child = mActivity.getLayoutInflater().inflate(R.layout.item_detail_viewpager_img, container, false);
            final ImageView img = (ImageView) child.findViewById(R.id.item_viewpager_img);
            mImgLl.addView(child);
            if (AppliteUtils.isLoadNetworkBitmap(mActivity)) {
                bitmapUtils.display(img, mViewPagerUrlList[i], new BitmapLoadCallBack<ImageView>() {
                    @Override
                    public void onLoadCompleted(ImageView imageView, String s, Bitmap bitmap, BitmapDisplayConfig bitmapDisplayConfig, BitmapLoadFrom bitmapLoadFrom) {
                        imageView.setDrawingCacheEnabled(false);
                        if (bitmap.getWidth() > bitmap.getHeight()) {
                            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                            imageView.setImageBitmap(bitmap);
//                            imageView.setImageBitmap(DetailUtils.rotateBitmap(bitmap, 90));
                        } else {
                            imageView.setImageBitmap(bitmap);
//                            imageView.setImageBitmap(DetailUtils.rotateBitmap(bitmap, 0));
                        }
                    }

                    @Override
                    public void onLoadFailed(ImageView imageView, String s, Drawable drawable) {
                        imageView.setImageResource(R.drawable.detail_default_img);
                    }
                });
            }
        }
//        mLoadLayout.setVisibility(View.GONE);
        mDataLayout.setVisibility(View.VISIBLE);
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
        LogUtils.d(TAG, "onClickButton");
        ImplInfo implInfo = (ImplInfo) params[0];
        SimilarBean bean = (SimilarBean) params[1];
        ImplChangeCallback implChangeCallback = (ImplChangeCallback) params[2];
        ImplHelper.onClick(mActivity,
                implInfo,
                bean.getrDownloadUrl(),
                bean.getName(),
                bean.getIconUrl(),
                Environment.getExternalStorageDirectory() + File.separator + Constant.extenStorageDirPath + bean.getName() + ".apk",
                null,
                implChangeCallback);
    }

    @Override
    public void dataLess(int i) {

    }

    class DetailImplCallback implements ImplChangeCallback {
        @Override
        public void onChange(ImplInfo implInfo) {
            refresh(implInfo);
        }

        public void refresh(ImplInfo info) {
            ImplInfo.ImplRes res = info.getImplRes();
            LogUtils.d(TAG, res.getActionText() + "," + info.getStatus() + "," + res.getStatusText());

            mProgressButton.setText(res.getActionText());
            mProgressButton.setProgress(info.getProgress());

            if (info.getStatus() == ImplInfo.STATUS_PRIVATE_INSTALLING) {
                LogUtils.d(TAG, "正在静默安装");
                mProgressButton.setText(res.getStatusText());
                mProgressButton.setBackgroundColor(mActivity.getResources().getColor(R.color.progress_background));
            }

            if ((info.getStatus() == info.STATUS_INSTALLED) && luckyflag) {
                luckyflag = false;
                int mLuckyPonints = (int) AppliteSPUtils.get(mActivity, AppliteSPUtils.LUCKY_POINTS, 0);
                mLuckyPonints += points;
                AppliteSPUtils.put(mActivity, AppliteSPUtils.LUCKY_POINTS, mLuckyPonints);
                Toast toast = Toast.makeText(mActivity, "成功下载安装有奖应用, 获得奖励" + points + "积分", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                mProgressButton.setBackgroundColor(mActivity.getResources().getColor(R.color.progress_foreground));
            }
        }
    }
}