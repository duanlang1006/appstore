package com.mit.main;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.applite.common.AppliteUtils;
import com.applite.common.BitmapHelper;
import com.applite.common.Constant;
import com.applite.common.LogUtils;
import com.applite.sharedpreferences.AppliteSPUtils;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.mit.bean.GuideBean;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplInfo;
import com.osgi.extra.OSGIBaseFragment;
import com.osgi.extra.OSGIServiceHost;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class GuideFragment extends OSGIBaseFragment implements View.OnClickListener {
    private static final String TAG = "GuideFragment";
    private FrameLayout mFLayout;
    private int mFLayoutWidth;
    private int mFLayoutHeight;
    private RelativeLayout mRLayout;
    private Button mInstallView;
    private Button mToHomeView;
    private List<GuideBean> mGuideContents = new ArrayList<GuideBean>();
    private ImageView mLogoIV;

    private View rootView;
    private LayoutInflater mInflater;
    private ViewGroup container;
    private float[] mX;//APK的X坐标数组
    private float[] mY;//APK的Y坐标数组
    private boolean[] mApkMovePath = {false, false, true, true, false, false, true, true, false, false};//APK移动方向数组  true右  false左
    private int POST_ALL_APK = -1;
    private List<View> mApkList = new ArrayList<View>();//存放APK的list
    private boolean mShuttingdown = false;
    private Handler mHandler = new Handler();
    private Runnable mThread = new Runnable() {//延时线程
        public void run() {
            jump();
        }
    };
    private ImplAgent implAgent;
    private BitmapUtils mBitmapUtil;
    private HttpUtils mHttpUtils;

    private String mWhichService;
    private String mWhichFragment;
    private Bundle mParams;
    private boolean misguide;
    private int FAILURE_POST_NUMBER = 0;
    private float mFLayoutWidthScale;


    public static Bundle newBundles(String targetService, String targetFragment, Bundle params, boolean isguide) {
        Bundle b = new Bundle();
        b.putString("service", targetService);
        b.putString("fragment", targetFragment);
        b.putBundle("params", params);
        b.putBoolean("isguide", isguide);
        return b;
    }

    public GuideFragment() {
        super();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        implAgent = ImplAgent.getInstance(mActivity.getApplicationContext());

        Bundle arguments = getArguments();
        if (null != arguments) {
            mWhichService = arguments.getString("service");
            mWhichFragment = arguments.getString("fragment");
            mParams = arguments.getBundle("params");
            misguide = arguments.getBoolean("isguide");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBitmapUtil = BitmapHelper.getBitmapUtils(mActivity.getApplicationContext());
        mHttpUtils = new HttpUtils();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mInflater = inflater;
        try {
            ActionBar actionBar = ((ActionBarActivity) mActivity).getSupportActionBar();
            actionBar.hide();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.container = container;

        if ((Boolean) AppliteSPUtils.get(mActivity, AppliteSPUtils.ISGUIDE, true)||misguide) {
            rootView = mInflater.inflate(R.layout.fragment_guide, container, false);
            initView();
            getResolution();
            post((int) AppliteSPUtils.get(mActivity, AppliteSPUtils.GUIDE_POSITION, 0), 10, POST_ALL_APK);
            //GuideSPUtils.put(mActivity, GuideSPUtils.ISGUIDE, false);
        } else {
            rootView = mInflater.inflate(R.layout.fragment_logo, container, false);
            logoInitView();
            if (System.currentTimeMillis() / 1000 >
                    (Long) AppliteSPUtils.get(mActivity, AppliteSPUtils.LOGO_NEXT_TIME, 0L)) {
                logoPost();
            }
        }


        if (null != mToHomeView) {
            mToHomeView.setEnabled(true);
        } else {
            long timeout = (long) AppliteSPUtils.get(mActivity, AppliteSPUtils.LOGO_SHOW_TIME, 3000L);
            mHandler.postDelayed(mThread, timeout);
        }
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("GuideFragment"); //统计页面
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("GuideFragment");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mHandler.removeCallbacks(mThread);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LogUtils.i(TAG, "onDestroyView");
        mShuttingdown = true;
        mHandler.removeCallbacks(mThread);//关闭延时线程
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * 初始化
     */
    private void initView() {
        mFLayout = (FrameLayout) rootView.findViewById(R.id.guide_fl);
        mFLayoutWidth = AppliteUtils.px2dip(mActivity, AppliteUtils.getWidth(mFLayout));
        mFLayoutHeight = AppliteUtils.px2dip(mActivity, AppliteUtils.getHeight(mFLayout));
        LogUtils.i(TAG, "mFLayoutWidth:" + mFLayoutWidth
                + "--------------mFLayoutHeight:" + mFLayoutHeight);
        mRLayout = (RelativeLayout) rootView.findViewById(R.id.guide_rl);
        mToHomeView = (Button) rootView.findViewById(R.id.guide_tohome);
        mInstallView = (Button) rootView.findViewById(R.id.guide_install);

        mToHomeView.setOnClickListener(this);
        mInstallView.setOnClickListener(this);
        if((Boolean) AppliteSPUtils.get(mActivity, "personal_flag", false)){
            AppliteSPUtils.put(mActivity, AppliteSPUtils.ISGUIDE, false);
            mToHomeView.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 获取屏幕分辨率
     */
    private void getResolution() {
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        int W = mDisplayMetrics.widthPixels;
        int H = mDisplayMetrics.heightPixels;
        LogUtils.i(TAG, "W = " + W + "-----------------H = " + H);

        mFLayoutWidthScale = (float) W / (float) mFLayoutWidth;
        LogUtils.i(TAG, "mFLayoutWidthScale:" + mFLayoutWidthScale);
        setAppViewXY();
    }

    /**
     * 保存10个控件的位置到数组里面
     */
    private void setAppViewXY() {
        mX = new float[10];
        mX[0] = (mFLayoutWidth * 6 / 10 - 10) * mFLayoutWidthScale;
        mX[1] = (mFLayoutWidth * 7 / 10 - 10) * mFLayoutWidthScale;
        mX[2] = (mFLayoutWidth * 3 / 10 - 20) * mFLayoutWidthScale;
        mX[3] = (mFLayoutWidth * 2 / 10 - 30) * mFLayoutWidthScale;
        mX[4] = (mFLayoutWidth * 6 / 10) * mFLayoutWidthScale;
        mX[5] = (mFLayoutWidth * 7 / 10) * mFLayoutWidthScale;
        mX[6] = (mFLayoutWidth * 2 / 10) * mFLayoutWidthScale;
        mX[7] = (mFLayoutWidth * 3 / 10 - 30) * mFLayoutWidthScale;
        mX[8] = (mFLayoutWidth * 6 / 10) * mFLayoutWidthScale;
        mX[9] = (mFLayoutWidth * 5 / 10 + 40) * mFLayoutWidthScale;
        mY = new float[10];
        mY[0] = (mFLayoutHeight * 2 / 20 + 20) * mFLayoutWidthScale;
        mY[1] = (mFLayoutHeight * 3 / 20 + 50) * mFLayoutWidthScale;
        mY[2] = (mFLayoutHeight * 5 / 20 + 40) * mFLayoutWidthScale;
        mY[3] = (mFLayoutHeight * 7 / 20) * mFLayoutWidthScale;
        mY[4] = (mFLayoutHeight * 9 / 20 + 40) * mFLayoutWidthScale;
        mY[5] = (mFLayoutHeight * 11 / 20 - 30) * mFLayoutWidthScale;
        mY[6] = (mFLayoutHeight * 12 / 20 - 30) * mFLayoutWidthScale;
        mY[7] = (mFLayoutHeight * 14 / 20 - 30) * mFLayoutWidthScale;
        mY[8] = (mFLayoutHeight * 16 / 20 - 20) * mFLayoutWidthScale;
        mY[9] = (mFLayoutHeight * 18 / 20 - 40) * mFLayoutWidthScale;
    }

    /**
     * 首页指导网络请求
     *
     * @param position 第几条开始
     * @param number   给几条
     */
    private void post(final int position, final int number, final int apkPsition) {
        annalPostApkNumber(apkPsition);

        RequestParams params = new RequestParams();
        params.addBodyParameter("appkey", AppliteUtils.getMitMetaDataValue(mActivity, Constant.META_DATA_MIT));
        params.addBodyParameter("packagename", mActivity.getPackageName());
        params.addBodyParameter("type", "guide");
        params.addBodyParameter("sort", "gift");
        params.addBodyParameter("position", position + "");
        params.addBodyParameter("number", number + "");
        mHttpUtils.send(HttpRequest.HttpMethod.POST, Constant.URL, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                if (mShuttingdown) {
                    return;
                }
                FAILURE_POST_NUMBER = 0;
                LogUtils.i(TAG, "首页指导网络请求成功，reuslt:" + responseInfo.result);
                setData(responseInfo.result, apkPsition);
            }

            @Override
            public void onFailure(HttpException e, String s) {
                if (mShuttingdown) {
                    return;
                }
                LogUtils.e(TAG, "首页指导网络请求失败:" + s);
                deleteNoReturn(apkPsition, 0);
                if (FAILURE_POST_NUMBER < 3) {
                    FAILURE_POST_NUMBER = FAILURE_POST_NUMBER + 1;
                    post(position, number, apkPsition);
                }
            }
        });
    }

    /**
     * 设置数据
     *
     * @param data
     */
    private void setData(String data, int apkPsition) {
        GuideBean bean = null;
        try {
            JSONObject obj = new JSONObject(data);
            int app_key = obj.getInt("app_key");
            String goods_data = obj.getString("goods_data");
            JSONArray json = new JSONArray(goods_data);

            deleteNoReturn(apkPsition, json.length());

            for (int i = 0; i < json.length(); i++) {
                JSONObject object = new JSONObject(json.get(i).toString());
                bean = new GuideBean();
//                bean.setId(1 + (Integer) SPUtils.get(mActivity, SPUtils.GUIDE_POSITION, 0));
                bean.setmVersionCode(object.getInt("versionCode"));
                bean.setPackagename(object.getString("packageName"));
                bean.setName(object.getString("name"));
                bean.setImgurl(object.getString("iconUrl"));
                bean.setUrl(object.getString("rDownloadUrl"));
                mGuideContents.add(bean);

                if (apkPsition != POST_ALL_APK) {
                    addAppView(bean, apkPsition);
                } else {
                    addAppView(bean, i);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "首页指导JSON解析异常");
        }
    }

    /**
     * 记录请求了多少个APK
     *
     * @param apkPsition
     */
    private void annalPostApkNumber(int apkPsition) {
        if (apkPsition == POST_ALL_APK) {
            AppliteSPUtils.put(mActivity, AppliteSPUtils.GUIDE_POSITION,
                    (Integer) AppliteSPUtils.get(mActivity, AppliteSPUtils.GUIDE_POSITION, 0) + 10);
        } else {
            AppliteSPUtils.put(mActivity, AppliteSPUtils.GUIDE_POSITION,
                    (Integer) AppliteSPUtils.get(mActivity, AppliteSPUtils.GUIDE_POSITION, 0) + 1);
        }
    }

    /**
     * 删除未返回的数量
     *
     * @param apkPsition
     * @param number
     */
    private void deleteNoReturn(int apkPsition, int number) {
        if (number < 10 && apkPsition == POST_ALL_APK) {//请求10个，但是返回数据小于10
            AppliteSPUtils.put(mActivity, AppliteSPUtils.GUIDE_POSITION,
                    (Integer) AppliteSPUtils.get(mActivity, AppliteSPUtils.GUIDE_POSITION, 0)
                            - (10 - number));
        } else if (number == 0 && apkPsition != POST_ALL_APK) {//请求1个，但是返回数据等于零
            AppliteSPUtils.put(mActivity, AppliteSPUtils.GUIDE_POSITION,
                    (Integer) AppliteSPUtils.get(mActivity, AppliteSPUtils.GUIDE_POSITION, 0) - 1);
        }
    }

    /**
     * 添加一个APP
     *
     * @param bean
     * @param apkPsition
     */
    private void addAppView(final GuideBean bean, final int apkPsition) {
        final View child = mInflater.inflate(R.layout.item_guide_app, container, false);
        final LinearLayout mLL = (LinearLayout) child.findViewById(R.id.guide_app_item);
        final ImageView mAppIV = (ImageView) child.findViewById(R.id.guide_app_iv);
        final TextView mAppTV = (TextView) child.findViewById(R.id.guidw_app_tv);

        mAppTV.setText(bean.getName());

        mBitmapUtil.configDefaultLoadingImage(getResources().getDrawable(R.drawable.apk_icon_defailt_img));
        mBitmapUtil.configDefaultLoadFailedImage(getResources().getDrawable(R.drawable.apk_icon_defailt_img));
        mBitmapUtil.display(mAppIV, bean.getImgurl());

        child.setTag(apkPsition);
        mApkList.add(child);

        mLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGuideContents.remove(child.getTag());
                mApkList.remove(child);
                paowuxianAnimator(child, mApkMovePath[apkPsition]);

                post((int) AppliteSPUtils.get(mActivity, AppliteSPUtils.GUIDE_POSITION, 0) + 1, 1, apkPsition);
                download(bean);
            }
        });

        mRLayout.addView(child);
        AppliteUtils.setLayout(child, (int) mX[apkPsition], (int) mY[apkPsition]);
        LogUtils.i("lang", "mX[apkPsition]: " + mX[apkPsition] + " mY[apkPsition]: " + mY[apkPsition]);
        appearAnimator(child);
    }

    /**
     * 去首页
     */
    private void jump() {
        AppliteSPUtils.put(mActivity, AppliteSPUtils.ISGUIDE, false);
        AppliteSPUtils.put(mActivity, "personal_flag", true);

        OSGIServiceHost host = (OSGIServiceHost) mActivity;
        host.jumpto(mWhichService, mWhichFragment, mParams,false);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.guide_tohome) {
            jump();
        } else if (v.getId() == R.id.guide_install) {
            installAllApp();
            post((int) AppliteSPUtils.get(mActivity, AppliteSPUtils.GUIDE_POSITION, 0) + 1, 10, POST_ALL_APK);
        }
    }

    /**
     * 所有的APK全部移除
     */
    private void installAllApp() {
        if (null != mApkList && !mApkList.isEmpty()) {
            for (int i = 0; i < mApkList.size(); i++) {
                View view = mApkList.get(i);
                paowuxianAnimator(view, mApkMovePath[(int) view.getTag()]);

                GuideBean bean = mGuideContents.get(i);
                download(bean);
            }
            mGuideContents.clear();
            mApkList.clear();
        }
    }


    private void download(GuideBean bean) {
        ImplInfo implInfo = implAgent.getImplInfo(bean.getPackagename(), bean.getPackagename(), bean.getmVersionCode());
        if (null == implInfo) {
            return;
        }
        implInfo.setTitle(bean.getName()).setDownloadUrl(bean.getUrl()).setIconUrl(bean.getImgurl());
        if (ImplInfo.ACTION_DOWNLOAD == implAgent.getAction(implInfo)) {
            switch (implInfo.getStatus()) {
                case Constant.STATUS_PENDING:
                case Constant.STATUS_RUNNING:
                    break;
                case Constant.STATUS_PAUSED:
                    implAgent.resumeDownload(implInfo, null);
                    break;
                case Constant.STATUS_INSTALLED:
                case Constant.STATUS_NORMAL_INSTALLING:
                case Constant.STATUS_PRIVATE_INSTALLING:
                    //正在安装或已安装
//                            Toast.makeText(mActivity, "该应用您已经安装过了！", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    implAgent.newDownload(implInfo,
                            Constant.extenStorageDirPath,
                            bean.getName() + ".apk",
                            true,
                            null);
                    break;
            }
        }
    }

    /**
     * APP出来时的放大动画
     *
     * @param view
     */
    private void appearAnimator(final View view) {
        ObjectAnimator anim = ObjectAnimator//
                .ofFloat(view, "lsy", 0.1F, 1.0F)//
                .setDuration(500);//
        anim.start();
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float cVal = (Float) animation.getAnimatedValue();
                view.setAlpha(cVal);
                view.setScaleX(cVal);
                view.setScaleY(cVal);
            }
        });
    }

    /**
     * APP点击后的抛物线动画
     *
     * @param view
     * @param bool true向右移动    false向左移动
     */
    private void paowuxianAnimator(final View view, final Boolean bool) {
        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setDuration(1000);
        valueAnimator.setObjectValues(new PointF(0, 0));
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setEvaluator(new TypeEvaluator<PointF>() {

            @Override
            public PointF evaluate(float fraction, PointF startValue,
                                   PointF endValue) {
                // x方向200px/s ，则y方向0.5 * 10 * t
                PointF point = new PointF();
                point.y = view.getTop() + 0.5f * 200 * (fraction * 3)
                        * (fraction * 3);
                if (bool) {
                    point.x = view.getLeft() + 100 * fraction * 3;
                    if (point.x > mFLayoutWidth / 2) {
                        point.x = mFLayoutWidth / 2;
                    }
                } else {
                    point.x = view.getLeft() - 100 * fraction * 3;
                    if (point.x < mFLayoutWidth / 2) {
                        point.x = mFLayoutWidth / 2;
                    }
                }
                return point;
            }
        });

        valueAnimator.start();
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {// 位置更新监听
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                PointF point = (PointF) animation.getAnimatedValue();
                view.setX(point.x);
                view.setY(point.y);
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {// 动画结束监听
            @Override
            public void onAnimationEnd(Animator animation) {
                ViewGroup parent = (ViewGroup) view.getParent();
                if (parent != null)
                    parent.removeView(view);// 删除点击了的view
            }
        });
    }

    /**
     * 下载文件
     */
    private void download(final String name, String url) {
        HttpHandler mHttpHandler = mHttpUtils.download(url, //这里是下载的路径
                AppliteUtils.getAppDir(name), //这是保存到本地的路径
                true,//true:断点续传 false:不断点续传（全新下载）
                new RequestCallBack<File>() {

                    @Override
                    public void onLoading(long total, long current, boolean isUploading) {
                        LogUtils.i(TAG, "下载进度：" + current + "/" + total);
                    }

                    @Override
                    public void onSuccess(ResponseInfo<File> responseInfo) {
                        LogUtils.i(TAG, name + "下载成功");
                        LogUtils.i(TAG, "Utils.getAppDir(name):" + AppliteUtils.getAppDir(name));
                        AppliteSPUtils.put(mActivity, AppliteSPUtils.LOGO_IMG_URL, AppliteUtils.getAppDir(name));
                    }

                    @Override
                    public void onFailure(HttpException e, String s) {
                        LogUtils.e(TAG, name + "下载LOGO失败：" + s);
                    }

                });
    }

    /**
     * 初始化LOGO页面的View
     */
    private void logoInitView() {
        mLogoIV = (ImageView) rootView.findViewById(R.id.logo_iv);
        if (System.currentTimeMillis() / 1000 >= (Long) AppliteSPUtils.get(mActivity, AppliteSPUtils.LOGO_START_SHOW_TIME, 0L) &&
                System.currentTimeMillis() / 1000 <= (Long) AppliteSPUtils.get(mActivity, AppliteSPUtils.LOGO_END_SHOW_TIME, 0L)) {
            if (!TextUtils.isEmpty((String) AppliteSPUtils.get(mActivity, AppliteSPUtils.LOGO_IMG_URL, ""))) {
                mLogoIV.setBackground(new BitmapDrawable(AppliteUtils.getLoacalBitmap(
                        (String) AppliteSPUtils.get(mActivity, AppliteSPUtils.LOGO_IMG_URL, ""))));
            }
        }
        //判断在线LOGO的是否存在和显示时间，如果当前时间大于显示时间则删除LOGO图片
        if (AppliteUtils.fileIsExists((String) AppliteSPUtils.get(mActivity, AppliteSPUtils.LOGO_IMG_URL, "")) &&
                System.currentTimeMillis() / 1000 > (Long) AppliteSPUtils.get(mActivity, AppliteSPUtils.LOGO_END_SHOW_TIME, 0L))
            AppliteUtils.delFile((String) AppliteSPUtils.get(mActivity, AppliteSPUtils.LOGO_IMG_URL, ""));
    }

    /**
     * LOGO页面的网络请求
     */
    private void logoPost() {
        RequestParams params = new RequestParams();
        params.addBodyParameter("appkey", AppliteUtils.getMitMetaDataValue(mActivity, Constant.META_DATA_MIT));
        params.addBodyParameter("packagename", mActivity.getPackageName());
        params.addBodyParameter("type", "logo");
        mHttpUtils.send(HttpRequest.HttpMethod.POST, Constant.URL, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                if (mShuttingdown) {
                    return;
                }
                LogUtils.i(TAG, "LOGO网络请求成功，reuslt：" + responseInfo.result);
                try {
                    JSONObject obj = new JSONObject(responseInfo.result);
                    int app_key = obj.getInt("app_key");
                    long NextTime = obj.getLong("nexttime");
                    long ShowTime = obj.getInt("i_staytime") * 1000;
                    String BigImgUrl = obj.getString("i_biglogourl");
                    String SmallImgUrl = obj.getString("i_smalllogourl");
                    long StartTime = obj.getLong("limit_starttime");
                    long EndTime = obj.getLong("limit_endtime");
                    AppliteSPUtils.put(mActivity, AppliteSPUtils.LOGO_NEXT_TIME, NextTime);
                    AppliteSPUtils.put(mActivity, AppliteSPUtils.LOGO_SHOW_TIME, ShowTime);
                    AppliteSPUtils.put(mActivity, AppliteSPUtils.LOGO_START_SHOW_TIME, StartTime);
                    AppliteSPUtils.put(mActivity, AppliteSPUtils.LOGO_END_SHOW_TIME, EndTime);
                    if (!TextUtils.isEmpty(BigImgUrl)) {
                        download(Constant.LOGO_IMG_NAME, BigImgUrl);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    LogUtils.e(TAG, "LOGO,JSON解析异常");
                }
            }

            @Override
            public void onFailure(HttpException e, String s) {
                if (mShuttingdown) {
                    return;
                }
                LogUtils.e(TAG, "LOGO网络请求失败：" + s);
            }
        });
    }

}
