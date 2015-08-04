package com.mit.applite.main;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplInfo;
import com.mit.impl.ImplChangeCallback;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DetailFragment extends Fragment implements View.OnClickListener {

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
    private ImplChangeCallback implCallback;
    private BitmapUtils mBitmapUtil;
    private String mDownloadUrl;
    private LinearLayout mDataLayout;
    private LinearLayout mLoadLayout;
    private ImageView mLoadView;
    private Animation LoadingAnimation;

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
        mBitmapUtil = BitmapHelper.getBitmapUtils(mActivity.getApplicationContext());
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
        System.gc();
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
        //加载中控件
        mLoadLayout = (LinearLayout) rootView.findViewById(R.id.detail_loading_layout);
        mLoadView = (ImageView) rootView.findViewById(R.id.detail_loading_img);
        //旋转动画
        LoadingAnimation = AnimationUtils.loadAnimation(mContext, R.anim.loading);
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
        no_network = (LinearLayout) rootView.findViewById(R.id.no_network);
        refreshButton = (Button) rootView.findViewById(R.id.refresh_btn);

        mName1View.setText(mApkName);

        mBitmapUtil.configDefaultLoadingImage(mContext.getResources().getDrawable(R.drawable.apk_icon_defailt_img));
        mBitmapUtil.configDefaultLoadFailedImage(mContext.getResources().getDrawable(R.drawable.apk_icon_defailt_img));
        mBitmapUtil.display(mApkImgView, mImgUrl);

        mProgressButton.setOnProgressButtonClickListener(new ProgressButton.OnProgressButtonClickListener() {
            @Override
            public void onClickListener() {
                if (!TextUtils.isEmpty(mPackageName)) {
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
                                mContext.startActivity(implAgent.getActionIntent(implinfo));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        });
        refreshButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.refresh_btn:
                no_network.setVisibility(View.GONE);
                post(mPackageName);
                break;
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
                    String content = obj.getString("description");
                    mViewPagerUrl = obj.getString("screenshotsUrl");
                    String developer = obj.getString("developer");

                    mName1View.setText(mName);
                    mXingView.setRating(Float.parseFloat(xing) / 2.0f);
                    mBitmapUtil.display(mApkImgView, mImgUrl);
                    mApkSizeAndCompanyView.setText(AppliteUtils.bytes2kb(size) + " | " + developer);
                    mApkContentView.setText(content);
                    LogUtils.i(TAG, "应用介绍：" + content);
                }
                mViewPagerUrlList = mViewPagerUrl.split(",");
                setPreViewImg();
            }

            ImplInfo implinfo = implAgent.getImplInfo(mPackageName, mPackageName, mVersionCode);
            if (null != implinfo) {
                implAgent.setImplCallback(implCallback, implinfo);
                mProgressButton.setText(implAgent.getActionText(implinfo));
                mProgressButton.setProgress(implAgent.getProgress(implinfo));
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
        // 下面添加参数，显示读出时内容和读取失败时的内容
        bitmapUtils.configDefaultLoadingImage(mContext.getResources().getDrawable(R.drawable.detail_default_img));
        bitmapUtils.configDefaultLoadFailedImage(mContext.getResources().getDrawable(R.drawable.detail_default_img));
        for (int i = 0; i < mViewPagerUrlList.length; i++) {
            LogUtils.i(TAG, "应用图片URL地址：" + mViewPagerUrlList[i]);
            final View child = mInflater.inflate(R.layout.item_detail_viewpager_img, container, false);
            final ImageView img = (ImageView) child.findViewById(R.id.item_viewpager_img);
            mImgLl.addView(child);
            bitmapUtils.display(img, mViewPagerUrlList[i]);
        }
        mLoadLayout.setVisibility(View.GONE);
        mDataLayout.setVisibility(View.VISIBLE);
    }

//    /**
//     * 设置应用介绍的图片
//     */
//    private void setPreViewImg() {
//        Drawable mDefaultImg = mContext.getResources().getDrawable(R.drawable.detail_default_img);
//        // 创建默认的ImageLoader配置参数
//        ImageLoaderConfiguration configuration = ImageLoaderConfiguration
//                .createDefault(getActivity());
//        ImageLoader.getInstance().init(configuration);
//        DisplayImageOptions options = new DisplayImageOptions.Builder()
//                .cacheInMemory(true) // 设置下载的图片是否缓存在内存中
//                .cacheOnDisk(true) // 设置下载的图片是否缓存在SD卡中
//                .showImageOnLoading(mDefaultImg) //设置图片在下载期间显示的图片
//                .showImageForEmptyUri(mDefaultImg)//设置图片Uri为空或是错误的时候显示的图片
//                .showImageOnFail(mDefaultImg)  //设置图片加载/解码过程中错误时候显示的图片
//                .considerExifParams(true)  //是否考虑JPEG图像EXIF参数（旋转，翻转）
//                .bitmapConfig(Bitmap.Config.RGB_565)//设置图片的解码类型
//                .build(); // 创建配置过得DisplayImageOption对象
//        for (int i = 0; i < mViewPagerUrlList.length; i++) {
//            LogUtils.i(TAG, "应用图片URL地址：" + mViewPagerUrlList[i]);
//            final View child = mInflater.inflate(R.layout.item_detail_viewpager_img, container, false);
//            final ImageView img = (ImageView) child.findViewById(R.id.item_viewpager_img);
//            mImgLl.addView(child);
//            ImageLoader.getInstance().displayImage(mViewPagerUrlList[i], img, options);
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