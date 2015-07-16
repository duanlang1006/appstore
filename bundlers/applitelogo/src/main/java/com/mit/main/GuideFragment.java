package com.mit.main;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.PointF;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.Toast;

import com.applite.common.AppliteUtils;
import com.applite.common.Constant;
import com.applite.common.LogUtils;
import com.mit.bean.GuideBean;
import com.mit.impl.ImplAgent;
import com.mit.utils.GuideUtils;
import com.mit.utils.GuideSPUtils;
import com.umeng.analytics.MobclickAgent;

import net.tsz.afinal.FinalBitmap;
import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;
import net.tsz.afinal.http.HttpHandler;

import org.apkplug.Bundle.ApkplugOSGIService;
import org.apkplug.Bundle.OSGIServiceAgent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class GuideFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "GuideFragment";
    private FrameLayout mFLayout;
    private int mFLayoutWidth;
    private int mFLayoutHeight;
    private RelativeLayout mRLayout;
    private Button mInstallView;
    private Button mToHomeView;
    private List<GuideBean> mGuideContents = new ArrayList<GuideBean>();
    private FinalBitmap mFinalBitmap;
    private FinalHttp mFinalHttp;
    private ImageView mLogoIV;

    private Activity mActivity;
    private View rootView;
    private LayoutInflater mInflater;
    private ViewGroup container;
    private int[] mX;//APK的X坐标数组

    private int[] mY;//APK的Y坐标数组
    private boolean[] mApkMovePath = new boolean[10];//APK移动方向数组  true右  false左
    private int POST_ALL_APK = -1;
    private List<View> mApkList = new ArrayList<View>();//存放APK的list
    private boolean mShuttingdown = false;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            }
        }
    };
    private Runnable mThread = new Runnable() {//延时线程
        public void run() {
            toHome();
        }
    };

    public GuideFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
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
            ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
            actionBar.hide();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null == mInflater) {
            mInflater = inflater;
        }
        this.container = container;

        mFinalBitmap = FinalBitmap.create(mActivity);
        mFinalHttp = new FinalHttp();
        if ((Boolean) GuideSPUtils.get(mActivity, GuideSPUtils.ISGUIDE, true)) {
            rootView = mInflater.inflate(R.layout.fragment_guide, container, false);
            initView();
            getResolution();
            post(1, 10, POST_ALL_APK);
            GuideSPUtils.put(mActivity, GuideSPUtils.ISGUIDE, false);
        } else {
            rootView = mInflater.inflate(R.layout.fragment_logo, container, false);
            logoInitView();
            mHandler.postDelayed(mThread,
                    (long) GuideSPUtils.get(mActivity, GuideSPUtils.LOGO_SHOW_TIME, 3000L));
            if (System.currentTimeMillis() / 1000 >
                    (Long) GuideSPUtils.get(mActivity, GuideSPUtils.LOGO_NEXT_TIME, 0L)) {
                logoPost();
            }
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

        setAppViewXY(W);
    }

    /**
     * 保存10个控件的位置到数组里面
     */
    private void setAppViewXY(int w) {
        mX = new int[10];
        mX[0] = (mFLayoutWidth * 6 / 10) * w / mFLayoutWidth;
        mX[1] = (mFLayoutWidth * 7 / 10) * w / mFLayoutWidth;
        mX[2] = (mFLayoutWidth * 3 / 10 - 20) * w / mFLayoutWidth;
        mX[3] = (mFLayoutWidth * 2 / 10 - 20) * w / mFLayoutWidth;
        mX[4] = (mFLayoutWidth * 6 / 10) * w / mFLayoutWidth;
        mX[5] = (mFLayoutWidth * 7 / 10) * w / mFLayoutWidth;
        mX[6] = (mFLayoutWidth * 2 / 10) * w / mFLayoutWidth;
        mX[7] = (mFLayoutWidth * 3 / 10 - 30) * w / mFLayoutWidth;
        mX[8] = (mFLayoutWidth * 6 / 10) * w / mFLayoutWidth;
        mX[9] = (mFLayoutWidth * 5 / 10 + 40) * w / mFLayoutWidth;
        mY = new int[10];
        mY[0] = (mFLayoutHeight * 2 / 20 - 20) * w / mFLayoutWidth;
        mY[1] = (mFLayoutHeight * 3 / 20 + 40) * w / mFLayoutWidth;
        mY[2] = (mFLayoutHeight * 5 / 20 + 40) * w / mFLayoutWidth;
        mY[3] = (mFLayoutHeight * 7 / 20) * w / mFLayoutWidth;
        mY[4] = (mFLayoutHeight * 9 / 20 + 40) * w / mFLayoutWidth;
        mY[5] = (mFLayoutHeight * 11 / 20 - 30) * w / mFLayoutWidth;
        mY[6] = (mFLayoutHeight * 12 / 20 - 30) * w / mFLayoutWidth;
        mY[7] = (mFLayoutHeight * 14 / 20 - 30) * w / mFLayoutWidth;
        mY[8] = (mFLayoutHeight * 16 / 20 - 20) * w / mFLayoutWidth;
        mY[9] = (mFLayoutHeight * 18 / 20 - 40) * w / mFLayoutWidth;
    }

    /**
     * 首页指导网络请求
     *
     * @param position 第几条开始
     * @param number   给几条
     */
    private void post(int position, int number, final int apkPsition) {
        annalPostApkNumber(apkPsition);

        AjaxParams params = new AjaxParams();
        params.put("appkey", AppliteUtils.getMitMetaDataValue(mActivity, Constant.META_DATA_MIT));
        params.put("packagename", mActivity.getPackageName());
        params.put("app", "applite");
        params.put("type", "guide");
        params.put("sort", "gift");
        params.put("position", position + "");
        params.put("number", number + "");
        mFinalHttp.post(Constant.URL, params, new AjaxCallBack<Object>() {
            @Override
            public void onSuccess(Object o) {
                super.onSuccess(o);
                if (mShuttingdown) {
                    return;
                }
                String reuslt = (String) o;
                LogUtils.i(TAG, "首页指导网络请求成功，reuslt:" + reuslt);
                setData(reuslt, apkPsition);
            }

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                super.onFailure(t, errorNo, strMsg);
                if (mShuttingdown) {
                    return;
                }
                LogUtils.e(TAG, "首页指导网络请求失败:" + strMsg);
                deleteNoReturn(apkPsition, 0);
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
            GuideSPUtils.put(mActivity, GuideSPUtils.GUIDE_POSITION,
                    (Integer) GuideSPUtils.get(mActivity, GuideSPUtils.GUIDE_POSITION, 0) + 10);
        } else {
            GuideSPUtils.put(mActivity, GuideSPUtils.GUIDE_POSITION,
                    (Integer) GuideSPUtils.get(mActivity, GuideSPUtils.GUIDE_POSITION, 0) + 1);
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
            GuideSPUtils.put(mActivity, GuideSPUtils.GUIDE_POSITION,
                    (Integer) GuideSPUtils.get(mActivity, GuideSPUtils.GUIDE_POSITION, 0)
                            - (10 - number));
        } else if (number == 0 && apkPsition != POST_ALL_APK) {//请求1个，但是返回数据等于零
            GuideSPUtils.put(mActivity, GuideSPUtils.GUIDE_POSITION,
                    (Integer) GuideSPUtils.get(mActivity, GuideSPUtils.GUIDE_POSITION, 0) - 1);
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

        //判断应用名有没有括号，有的话去掉
//        int subscript = bean.getName().lastIndexOf("(");
//        if (subscript != -1) {
//            String result = bean.getName().substring(0, bean.getName().lastIndexOf("("));
//            mAppTV.setText(result);
//        } else {
        mAppTV.setText(bean.getName());
//        }

        mFinalBitmap.display(mAppIV, bean.getImgurl());

        child.setTag(apkPsition);
        mApkList.add(child);

        mLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGuideContents.remove(child.getTag());
                mApkList.remove(child);
                paowuxianAnimator(child, mApkMovePath[apkPsition]);

                post((int) GuideSPUtils.get(mActivity, GuideSPUtils.GUIDE_POSITION, 0) + 1, 1, apkPsition);

                int mApkType = AppliteUtils.isAppInstalled(mActivity, bean.getPackagename(), bean.getmVersionCode());
                LogUtils.i(TAG, "mApkType:" + mApkType);
                switch (mApkType) {
                    case Constant.UNINSTALLED:
                        requestDownload(bean);
                        break;
                    case Constant.INSTALLED:
                        Toast.makeText(mActivity, "该应用您已经安装过了！", Toast.LENGTH_SHORT).show();
                        break;
                    case Constant.INSTALLED_UPDATE:
                        Toast.makeText(mActivity, "版本更新", Toast.LENGTH_SHORT).show();
                        requestDownload(bean);
                        break;
                }
            }
        });

        mRLayout.addView(child);
        AppliteUtils.setLayout(child, mX[apkPsition], mY[apkPsition]);
        appearAnimator(child);

        int i = (child.getRight() - child.getLeft()) / 2;
        if ((child.getLeft() + i) < mFLayoutWidth / 2) {
            mApkMovePath[apkPsition] = true;
        } else {
            mApkMovePath[apkPsition] = false;
        }
    }

    /**
     * 下载APK
     *
     * @param bean
     */
    private void requestDownload(GuideBean bean) {
        ImplAgent.downloadPackage(mActivity,
                bean.getPackagename(),
                bean.getUrl(),
                Constant.extenStorageDirPath,
                bean.getName() + ".apk",
                3,
                false,
                bean.getName(),
                "",
                true,
                bean.getImgurl(),
                "",
                bean.getPackagename());
    }

    /**
     * 去首页
     */
    private void toHome() {
        try {
            BundleContext bundleContext = BundleContextFactory.getInstance().getBundleContext();
            OSGIServiceAgent<ApkplugOSGIService> agent = new OSGIServiceAgent<ApkplugOSGIService>(
                    bundleContext, ApkplugOSGIService.class,
                    "(serviceName=osgi.service.host.opt)", //服务查询条件
                    OSGIServiceAgent.real_time);   //每次都重新查询
            agent.getService().ApkplugOSGIService(bundleContext, SimpleBundle.OSGI_SERVICE_LOGO_FRAGMENT, 0, Constant.OSGI_SERVICE_MAIN_FRAGMENT);
        } catch (Exception e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.guide_tohome:
                toHome();
                break;
            case R.id.guide_install:
                installAllApp();
                post((int) GuideSPUtils.get(mActivity, GuideSPUtils.GUIDE_POSITION, 0) + 1, 10, POST_ALL_APK);
                break;
            default:
                break;
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
                requestDownload(bean);
            }
            mGuideContents.clear();
            mApkList.clear();
        }
    }

    /**
     * APP出来时的放大动画
     *
     * @param view
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void appearAnimator(final View view) {
        ObjectAnimator anim1 = ObjectAnimator.ofFloat(view, "scaleX", 0.2f, 1f);
        ObjectAnimator anim2 = ObjectAnimator.ofFloat(view, "scaleY", 0.2f, 1f);
        AnimatorSet animSet = new AnimatorSet();
        animSet.setDuration(2000);
        animSet.setInterpolator(new LinearInterpolator());
        // 两个动画同时执行
        animSet.playTogether(anim1, anim2);
        animSet.start();
        animSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
//                view.setClickable(false);// 动画执行时不可以点击
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
//                view.setClickable(true);// 动画结束后才可以点击
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
        HttpHandler mHttpHandler = mFinalHttp.download(url, //这里是下载的路径
                AppliteUtils.getAppDir(name), //这是保存到本地的路径
                true,//true:断点续传 false:不断点续传（全新下载）
                new AjaxCallBack<File>() {

                    @Override
                    public void onLoading(long count, long current) {
                        LogUtils.i(TAG, "下载进度：" + current + "/" + count);
                    }

                    @Override
                    public void onSuccess(File t) {
                        LogUtils.i(TAG, name + "下载成功");
                        LogUtils.i(TAG, "Utils.getAppDir(name):" + AppliteUtils.getAppDir(name));
                        GuideSPUtils.put(mActivity, GuideSPUtils.LOGO_IMG_URL, AppliteUtils.getAppDir(name));
                    }

                    @Override
                    public void onFailure(Throwable t, int errorNo, String strMsg) {
                        super.onFailure(t, errorNo, strMsg);
                        LogUtils.e(TAG, name + "下载失败，strMsg：" + strMsg);
                    }
                });
    }

    /**
     * 初始化LOGO页面的View
     */
    private void logoInitView() {
        mLogoIV = (ImageView) rootView.findViewById(R.id.logo_iv);
        if (System.currentTimeMillis() / 1000 >= (Long) GuideSPUtils.get(mActivity, GuideSPUtils.LOGO_START_SHOW_TIME, 0L) &&
                System.currentTimeMillis() / 1000 <= (Long) GuideSPUtils.get(mActivity, GuideSPUtils.LOGO_END_SHOW_TIME, 0L)) {
            if (!TextUtils.isEmpty((String) GuideSPUtils.get(mActivity, GuideSPUtils.LOGO_IMG_URL, ""))) {
                mLogoIV.setImageBitmap(AppliteUtils.getLoacalBitmap(
                        (String) GuideSPUtils.get(mActivity, GuideSPUtils.LOGO_IMG_URL, "")));
            }
        }
    }

    /**
     * LOGO页面的网络请求
     */
    private void logoPost() {
        AjaxParams params = new AjaxParams();
        params.put("appkey", AppliteUtils.getMitMetaDataValue(mActivity, Constant.META_DATA_MIT));
        params.put("packagename", mActivity.getPackageName());
        params.put("app", "applite");
        params.put("type", "logo");
        mFinalHttp.post(Constant.URL, params, new AjaxCallBack<Object>() {
            @Override
            public void onSuccess(Object o) {
                super.onSuccess(o);
                if (mShuttingdown) {
                    return;
                }
                String reuslt = (String) o;
                LogUtils.i(TAG, "LOGO网络请求成功，reuslt：" + reuslt);
                try {
                    JSONObject obj = new JSONObject(reuslt);
                    int app_key = obj.getInt("app_key");
                    long NextTime = obj.getLong("nexttime");
                    long ShowTime = obj.getInt("i_staytime") * 1000;
                    String BigImgUrl = obj.getString("i_biglogourl");
                    String SmallImgUrl = obj.getString("i_smalllogourl");
                    long StartTime = obj.getLong("limit_starttime");
                    long EndTime = obj.getLong("limit_endtime");
                    GuideSPUtils.put(mActivity, GuideSPUtils.LOGO_NEXT_TIME, NextTime);
                    GuideSPUtils.put(mActivity, GuideSPUtils.LOGO_SHOW_TIME, ShowTime);
                    GuideSPUtils.put(mActivity, GuideSPUtils.LOGO_START_SHOW_TIME, StartTime);
                    GuideSPUtils.put(mActivity, GuideSPUtils.LOGO_END_SHOW_TIME, EndTime);
                    if (!TextUtils.isEmpty(BigImgUrl)) {
                        download("logo.jpg", BigImgUrl);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    LogUtils.e(TAG, "LOGO,JSON解析异常");
                }
            }

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                super.onFailure(t, errorNo, strMsg);
                if (mShuttingdown) {
                    return;
                }
                LogUtils.e(TAG, "LOGO网络请求失败：" + strMsg);
            }
        });
    }

}
