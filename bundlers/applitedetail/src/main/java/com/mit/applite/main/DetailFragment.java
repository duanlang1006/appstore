package com.mit.applite.main;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
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
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.applite.common.AppliteUtils;
import com.applite.common.BitmapHelper;
import com.applite.common.Constant;
import com.applite.common.LogUtils;
import com.applite.similarview.SimilarAdapter;
import com.applite.similarview.SimilarBean;
import com.applite.similarview.SimilarView;
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
import com.mit.impl.ImplHelper;
import com.mit.impl.ImplInfo;
import com.mit.impl.ImplChangeCallback;
import com.osgi.extra.OSGIBaseFragment;
import com.osgi.extra.OSGIServiceHost;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DetailFragment extends OSGIBaseFragment implements View.OnClickListener, SimilarAdapter.SimilarAPKDetailListener {

    private static final String TAG = "DetailFragment";
    private View rootView;
    private String mApkName;
    private TextView mName1View;
    private TextView mApkSizeAndCompanyView;
    private TextView mApkContentView;
    private RatingBar mXingView;
    private ImageView mApkImgView;
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
    //    private LinearLayout mLoadLayout;
//    private ImageView mLoadView;
//    private Animation LoadingAnimation;
    private String mDescription;
    private String mUpdateLog;
    private ImageView mOpenIntroduceView;
    private int DEFAULT_MAX_LINE_COUNT = 3;//应用介绍、更新日志默认最多显示3行
    private int COLLAPSIBLE_STATE_NONE = 0;//少于3行状态
    private int COLLAPSIBLE_STATE_SHRINKUP = 1;//收缩状态
    private int COLLAPSIBLE_STATE_SPREAD = 2;//展开状态
    private int CONTENT_STATE = COLLAPSIBLE_STATE_NONE;
    private int UPDATE_LOG_STATE = COLLAPSIBLE_STATE_NONE;
    private Handler mHandler = new Handler();
    private BitmapUtils bitmapUtils;
    private List<View> mDetailImgList = new ArrayList<View>();
    private LinearLayout mHorDefaultLayout;
    private LinearLayout mTagStateLayout;
    private LinearLayout mTagLayout1;
    private TextView mTagTitleView;
    private LinearLayout mTagLayout2;
    private int mWidthPixels;
    private int mHeightPixels;

    private List<SimilarBean> mSimilarData = new ArrayList<SimilarBean>();
    private SimilarView mSimilarView;

    public static Bundle newBundle(String packageName, String name, String imgUrl) {
        Bundle b = new Bundle();
        b.putString("packageName", packageName);
        b.putString("name", name);
        b.putString("imgUrl", imgUrl);
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
        }
        LogUtils.i(TAG, "mApkName:" + mApkName + "------mPackageName:" + mPackageName + "------mImgUrl:" + mImgUrl);
        initActionBar();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBitmapUtil = BitmapHelper.getBitmapUtils(mActivity.getApplicationContext());
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        mWidthPixels = mDisplayMetrics.widthPixels;
        mHeightPixels = mDisplayMetrics.heightPixels;
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

        mTagStateLayout = (LinearLayout) rootView.findViewById(R.id.detail_state_tag_layout);
        mTagTitleView = (TextView) rootView.findViewById(R.id.detail_tag_title);
        mTagLayout1 = (LinearLayout) rootView.findViewById(R.id.detail_tag_layout1);
        mTagLayout2 = (LinearLayout) rootView.findViewById(R.id.detail_tag_layout2);

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
                        ImplHelper.onClick(mActivity,
                                implinfo,
                                mDownloadUrl,
                                mName,
                                mImgUrl,
                                Environment.getExternalStorageDirectory() + File.separator + Constant.extenStorageDirPath + mApkName + ".apk",
                                null,
                                implCallback);

//                        if (ImplInfo.ACTION_DOWNLOAD == implAgent.getAction(implinfo)) {
//                            switch (implinfo.getStatus()) {
//                                case ImplInfo.STATUS_PENDING:
//                                case ImplInfo.STATUS_RUNNING:
//                                    implAgent.pauseDownload(implinfo);
//                                    break;
//                                case ImplInfo.STATUS_PAUSED:
//                                    implAgent.resumeDownload(implinfo, implCallback);
//                                    break;
//                                default:
//                                    implAgent.newDownload(implinfo,
//                                            mDownloadUrl,
//                                            mName,
//                                            mImgUrl,
//                                            Constant.extenStorageDirPath,
//                                            mApkName + ".apk",
//                                            true,
//                                            implCallback);
//                                    break;
//                            }
//                        } else {
//                            implAgent.startActivity(implinfo);
//                        }
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
            ((OSGIServiceHost) mActivity).jumptoSearch(null, true, null, null);
            return true;
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
     * 应用详情网络请求
     *
     * @param mPackageName
     */
    private void post(String mPackageName) {
        RequestParams params = new RequestParams();
        params.addBodyParameter("appkey", AppliteUtils.getMitMetaDataValue(mActivity, Constant.META_DATA_MIT));
        params.addBodyParameter("packagename", mActivity.getPackageName());
        params.addBodyParameter("type", "detail");
        params.addBodyParameter("protocol_version", Constant.PROTOCOL_VERSION);
        params.addBodyParameter("name", mPackageName);
        HttpUtils mHttpUtils = new HttpUtils();
        mHttpUtils.send(HttpRequest.HttpMethod.POST, Constant.URL, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                LogUtils.i(TAG, "应用详情网络请求成功:" + responseInfo.result);
                setData(responseInfo.result);
            }

            @Override
            public void onFailure(HttpException e, String s) {
                LogUtils.e(TAG, "应用详情网络请求失败:" + s);
                // 这里设置没有网络时的图片
//                mLoadLayout.setVisibility(View.GONE);
//                mDataLayout.setVisibility(View.GONE);
//                no_network.setVisibility(View.VISIBLE);
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
            JSONObject object = new JSONObject(data);
            String app_key = object.getString("app_key");
            String detail_info = object.getString("detail_info");
            LogUtils.i(TAG, "应用详情网络请求成功detail_info:" + detail_info);

            String similar_info = object.getString("similar_info");
            LogUtils.i(TAG, "应用详情网络请求成功similar_info:" + similar_info);

            if (!mSimilarData.isEmpty())
                mSimilarData.clear();
            JSONArray similar_json = new JSONArray(similar_info);
            SimilarBean similarBean = null;
            if (similar_json.length() != 0 && similar_json != null) {
                for (int i = 0; i < 4; i++) {
                    similarBean = new SimilarBean();
                    JSONObject obj = new JSONObject(similar_json.get(i).toString());
                    similarBean.setName(obj.getString("name"));
                    similarBean.setPackageName(obj.getString("packageName"));
                    similarBean.setIconUrl(obj.getString("iconUrl"));
                    similarBean.setrDownloadUrl(obj.getString("rDownloadUrl"));
                    similarBean.setVersionCode(obj.getInt("versionCode"));
                    mSimilarData.add(similarBean);
                }
                mSimilarView.setData(mSimilarData, this);
            }

            String mViewPagerUrl = null;
            String mApkTag = null;
            JSONArray json = new JSONArray(detail_info);
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
                    mApkTag = obj.getString("tag");

                    mName1View.setText(mName);
                    mXingView.setRating(Float.parseFloat(xing) / 2.0f);
//                    mBitmapUtil.display(mApkImgView, mImgUrl);
                    mApkSizeAndCompanyView.setText(AppliteUtils.bytes2kb(size) + " | " + developer);
                }
//                LogUtils.i(TAG, "应用介绍：" + mDescription);
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

                //应用图片介绍
                setPreViewImg(mViewPagerUrl);

                //标签
                setApkTag(mApkTag);
            }

            ImplInfo implinfo = implAgent.getImplInfo(mPackageName, mPackageName, mVersionCode);
            if (null != implinfo) {
                ImplHelper.ImplHelperRes res = ImplHelper.getImplRes(mActivity, implinfo);
                implAgent.setImplCallback(implCallback, implinfo);
                implinfo.setDownloadUrl(mDownloadUrl).setIconUrl(mImgUrl).setTitle(mName);
                mProgressButton.setText(res.getActionText());
                mProgressButton.setProgress(res.getProgress());
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
     * 设置标签
     */
    private void setApkTag(String apkTag) {
        for (int i = 0; i < mTagLayout1.getChildCount(); i++) {
            mTagLayout1.removeView(mTagLayout1.getChildAt(1));// 删除view
        }
        for (int i = 0; i < mTagLayout2.getChildCount(); i++) {
            mTagLayout2.removeView(mTagLayout2.getChildAt(1));// 删除view
        }

        LogUtils.d(TAG, "Tag：" + apkTag);
        if (TextUtils.isEmpty(apkTag)) {
            mTagStateLayout.setVisibility(View.GONE);
        } else {
            mTagStateLayout.setVisibility(View.VISIBLE);
            String[] mApkTagList = apkTag.split(",");

            int mTagLayoutWidth = mWidthPixels - 20;
            LogUtils.d(TAG, "mTagLayoutWidth：" + mTagLayoutWidth);

            int[] mTagWidthList = new int[mApkTagList.length];//标签的宽度
            int mTagLayout1Number = 0;
            for (int i = 0; i < mApkTagList.length; i++) {
                final View child = mActivity.getLayoutInflater().inflate(R.layout.item_apk_tag_layout, container, false);
                final TextView mTagView = (TextView) child.findViewById(R.id.item_tag_tv);
                mTagView.setText(mApkTagList[i]);
                mTagView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((OSGIServiceHost) mActivity).jumptoSearch(mTagView.getText().toString(), true, null, null);
                    }
                });

                mTagWidthList[i] = AppliteUtils.getWidth(child);
                LogUtils.d(TAG, "mTagWidthList[i]：" + mTagWidthList[i]);

                int mTagLayout1Width = 0;
                for (int j = 0; j < mTagWidthList.length; j++) {
                    mTagLayout1Width += mTagWidthList[j];
                }
                LogUtils.d(TAG, "mTagLayout1Width：" + mTagLayout1Width);

                if (mTagLayout1Width <= mTagLayoutWidth) {//标签加入第一行
                    mTagLayout1.addView(child);
                    mTagLayout1Number = i;
                } else {
                    int mTagLayout2Width = 0;
                    for (int w = mTagLayout1Number + 1; w < mTagWidthList.length; w++) {
                        mTagLayout2Width += mTagWidthList[w];
                    }
                    LogUtils.d(TAG, "mTagLayout2Width：" + mTagLayout2Width);
                    if (mTagLayout2Width <= mTagLayoutWidth)
                        mTagLayout2.addView(child);
                }
            }
        }
    }

    /**
     * 设置应用介绍的图片
     */
    private void setPreViewImg(String mViewPagerUrl) {
        if (null == bitmapUtils) {
            bitmapUtils = new BitmapUtils(mActivity);
        }
        if (!mDetailImgList.isEmpty()) {
            LogUtils.i(TAG, "删除原来的详情介绍图片");
            for (int i = 0; i < mDetailImgList.size(); i++) {
                ViewGroup parent = (ViewGroup) mDetailImgList.get(i).getParent();
                if (parent != null) {
                    parent.removeView(mDetailImgList.get(i));// 删除点击了的view
                }
            }
            mDetailImgList.clear();
        }
        String[] mViewPagerUrlList = mViewPagerUrl.split(",");
        mHorDefaultLayout.setVisibility(View.GONE);
        for (int i = 0; i < mViewPagerUrlList.length; i++) {
            LogUtils.i(TAG, "应用图片URL地址：" + mViewPagerUrlList[i]);
            final View child = mActivity.getLayoutInflater().inflate(R.layout.item_detail_viewpager_img, container, false);
            final ImageView img = (ImageView) child.findViewById(R.id.item_viewpager_img);
            mImgLl.addView(child);
            mDetailImgList.add(child);
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
//        mLoadLayout.setVisibility(View.GONE);
        mDataLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void refreshDetail(SimilarBean bean) {
//        mLoadLayout.setVisibility(View.VISIBLE);
//        mApkName = bean.getmName();
//        mPackageName = bean.getmPackageName();
//        mImgUrl = bean.getmImgUrl();
//        initActionBar();
//        post(bean.getmPackageName());
        ((OSGIServiceHost) mActivity).jumptoDetail(bean.getPackageName(), bean.getName(), bean.getIconUrl(), true);
    }

    class DetailImplCallback implements ImplChangeCallback {
        @Override
        public void onChange(ImplInfo implInfo) {
            refresh(implInfo);
        }

        private void refresh(ImplInfo info) {
            ImplHelper.ImplHelperRes res = ImplHelper.getImplRes(mActivity, info);
            LogUtils.d(TAG, "refresh" + res.getActionText() + "," + info.getStatus());
            mProgressButton.setText(res.getActionText());
            mProgressButton.setProgress(res.getProgress());
        }
    }
}