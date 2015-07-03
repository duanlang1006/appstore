package com.applite.homepage;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.applite.bean.HomePageTypeBean;
import com.applite.bean.ScreenBean;
import com.applite.common.AppliteUtils;
import com.applite.common.Constant;
import com.applite.common.LogUtils;
import com.applite.common.PagerSlidingTabStrip;
import com.applite.data.SectionsPagerAdapter;
import com.applite.utils.SPUtils;
import com.applite.bean.HomePageBean;
import com.applite.bean.HomePageTab;
import com.applite.utils.HomePageUtils;
import com.applite.utils.Utils;

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

/**
 * Created by yuzhimin on 6/17/15.
 */
public class HomePageFragment extends Fragment implements View.OnClickListener{
    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;
    PagerSlidingTabStrip mPagerSlidingTabStrip;

    private final String TAG = "HomePageFragment";
    private Activity mActivity;
    private FinalHttp mFinalHttp;
    private List<HomePageBean> mHomePageGoods = new ArrayList<HomePageBean>();
    private List<HomePageBean> mHomePageOrder = new ArrayList<HomePageBean>();
    private List<HomePageTypeBean> mHomePageMainType = new ArrayList<HomePageTypeBean>();

    private List<HomePageTab> mHPTabContents = new ArrayList<HomePageTab>();
    private int mTableType = 0;
    private boolean isMainType = false;
    private static int mNode;
    private ViewGroup rootView;
    private View popView;
    private PopupWindow popupWindow;
    private LayoutInflater mInflater;

    private List<ScreenBean> mScreenBeanList = new ArrayList<ScreenBean>();
    private FinalBitmap mFinalBitmap;
    private String mPopImgUrl;
    private boolean mPopIsClick = false;
    private String mPopImgName;
    private long mPopStartTime;
    private long mPopEndTime;

    public HomePageFragment(Context activity) {
        //mActivity = activity;
    }
    int mNum;

    /**
     * Create a new instance of HomePageFragment, providing "num"
     * as an argument.
     */
    static HomePageFragment newInstance(int num , Context context) {
        HomePageFragment f = new HomePageFragment(context);
        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("num", num);
        f.setArguments(args);
        return f;
    }
    public void setMainType(Boolean b){
        isMainType =b;
    }
    public void saveNode(int mNode){
        HomePageFragment.mNode = mNode;
    }
    public int getNode(){
        return mNode;
    }
    public Boolean getMainType(){
        return isMainType;
    }

    public int getShownIndex() {
        return getArguments().getInt("index", 0);

    }
    private final ViewPager.OnPageChangeListener mPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int i, float v, int i2) {
            mTableType = i;
            HomePageUtils.d(TAG, "onPageScrolled mTableType : " + mTableType);
        }

        @Override
        public void onPageSelected(int i) {
            mTableType = i;
            HomePageUtils.d(TAG,"onPageSelected  yuzm mTableType : "+mTableType);
        }

        @Override
        public void onPageScrollStateChanged(int i) {
            HomePageUtils.d(TAG, "onPageScrollStateChanged yuzm mTableType : "+mTableType);
        }
    };

    public HomePageFragment() {
        setHasOptionsMenu(true);
    }
    public HomePageFragment(boolean isMainType) {
        this.isMainType = isMainType;
    }
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        HomePageUtils.d(TAG, "onAttach yuzm activity : " + activity);
        mActivity = activity;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        HomePageUtils.d(TAG, "onCreate yuzm savedInstanceState : " + savedInstanceState);
        super.onCreate(savedInstanceState);
        //getArguments();
        HomePageUtils.i(TAG, "onCreate yuzm");
        mFinalBitmap = FinalBitmap.create(mActivity);
    }
    @Override
    public void onStart(){
        super.onStart();
        HomePageUtils.i(TAG, "onStart yuzm");
    }
    @Override
    public void onPause(){
        super.onPause();
        HomePageUtils.i(TAG, "onStart yuzm");
    }
    @Override
    public void onResume(){
        super.onResume();
        HomePageUtils.i(TAG, "onResume yuzm");

    }
    @Override
    public void onStop(){
        super.onStop();
        HomePageUtils.i(TAG, "onStop yuzm");
    }
    @Override
    public void onDestroy(){
        super.onDestroyView();
        HomePageUtils.i(TAG, "onDestroy yuzm");
    }
    @Override
    public void onDetach(){
        super.onDetach();
        HomePageUtils.i(TAG, "onDetach yuzm");
    }
    @Override
    public void onDestroyView(){
        super.onDestroyView();
        HomePageUtils.i(TAG, "onDestroyView yuzm");
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mInflater = inflater;
        try {
            Context context = BundleContextFactory.getInstance().getBundleContext().getBundleContext();
            HomePageUtils.d(TAG, "bundle context=" + context);
            if (null != context) {
                mInflater = LayoutInflater.from(context);
                mInflater = mInflater.cloneInContext(context);
                HomePageUtils.d(TAG,"bundle mInflater="+mInflater);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        HomePageUtils.d(TAG, "onCreateView yuzm  mInflater : " + mInflater);
        mFinalHttp = new FinalHttp();
        if(!getMainType()) {
            post();
        }else {
            postMainType();
        }
        rootView = (ViewGroup)mInflater.inflate(R.layout.fragment_activity_page, container, false);
        mSectionsPagerAdapter = new SectionsPagerAdapter(this.getFragmentManager(),mActivity);
        // Set up the ViewPager with the sections adapter.
        mSectionsPagerAdapter.setHomePageTab(mHPTabContents);
        mSectionsPagerAdapter.setHomePageGoods(mHomePageGoods);
        mSectionsPagerAdapter.setHomePageOrders(mHomePageOrder);
        mSectionsPagerAdapter.setHomePageMainType(mHomePageMainType);
        mViewPager = (ViewPager) rootView.findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        //加入滑动tab管理viewPager
        mPagerSlidingTabStrip = PagerSlidingTabStrip.inflate(mActivity,container,false);
        rootView.addView(mPagerSlidingTabStrip,0);
        mPagerSlidingTabStrip.setViewPager(mViewPager);
        mPagerSlidingTabStrip.setOnPageChangeListener(mPageChangeListener);

        //actionbar custom view
        View customView = mInflater.inflate(R.layout.custom_actionbar_main,container,false);
        customView.findViewById(R.id.action_dm).setOnClickListener(this);
        customView.findViewById(R.id.action_search).setOnClickListener(this);
        customView.findViewById(R.id.action_upgrade).setOnClickListener(this);
        initActionBar(customView);

        if (mHPTabContents.size() == 0){
            mViewPager.setVisibility(View.GONE);
        }else{
            mViewPager.setVisibility(View.VISIBLE);
        }

        popupWindowPost();
        return rootView;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden){
            ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
            actionBar.show();
        }
    }

    /**
     * 插屏请求
     */
    private void popupWindowPost(){
        LogUtils.i(TAG, "插屏网络请求");
        AjaxParams params = new AjaxParams();
        params.put("appkey", AppliteUtils.getMitMetaDataValue(mActivity, Constant.META_DATA_MIT));
        params.put("packagename", mActivity.getPackageName());
        params.put("app", "applite");
        params.put("type","screen");
        mFinalHttp.post(Constant.URL,params,new AjaxCallBack<Object>() {
            @Override
            public void onSuccess(Object o) {
                super.onSuccess(o);
                String result = (String) o;
                setData(result);
                LogUtils.i(TAG, "插屏网络请求成功，result:" + result);
            }

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                super.onFailure(t, errorNo, strMsg);
                LogUtils.e(TAG, "插屏网络请求失败，strMsg:" + strMsg);
            }
        });
    }

    /**
     * 解析插屏返回数据
     *
     * @param result
     */
    private void setData(String result) {
        try {
            JSONObject object = new JSONObject(result);
            String app_key = object.getString("app_key");
            String info = object.getString("plaque_info");
            if (!TextUtils.isEmpty(info)) {
                JSONArray array = new JSONArray(info);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = new JSONObject(array.get(i).toString());
                    mPopImgName = obj.getString("pl_name");
                    String spt_key = obj.getString("spt_key");
                    mPopImgUrl = obj.getString("pl_iconurl");
                    mPopStartTime = obj.getLong("pl_starttime") * 1000;
                    mPopEndTime = obj.getLong("pl_endtime") * 1000;
                }
                download(mPopImgName+".jpg",mPopImgUrl);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 下载文件
     */
    private void download(final String name , String url) {
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
                        SPUtils.put(mActivity, SPUtils.POP_IMG_SAVE_PATH, AppliteUtils.getAppDir(name));
//                        if (System.currentTimeMillis() > mPopStartTime && System.currentTimeMillis() < mPopEndTime){
//                            if(SPUtils.get(mActivity,SPUtils.POP_IMGURL,"").equals(mPopImgUrl)){
//                                if (!(boolean)SPUtils.get(mActivity,SPUtils.POP_IMGURL_ISCLICK,false))
//                                    initPopuWindow();
//                            }else {
                                initPopuWindow();
//                            }
//                        }
//                        SPUtils.put(mActivity,SPUtils.POP_IMGURL,mPopImgUrl);
                    }

                    @Override
                    public void onFailure(Throwable t, int errorNo, String strMsg) {
                        super.onFailure(t, errorNo, strMsg);
                        LogUtils.e(TAG, name + "下载失败，strMsg：" + strMsg);
                    }
                });
    }

    private View.OnClickListener mScreenClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SPUtils.put(mActivity,SPUtils.POP_IMGURL_ISCLICK,mPopIsClick);
            switch (v.getId()){
                case R.id.pop_img_exit:
                    popupWindow.dismiss();
                    break;
                case R.id.pop_img_img:

                    break;
            }
        }
    };

    /**
     * 实例化PopuWindow
     */
    public void initPopuWindow() {
        if (popupWindow == null) {
            popView = mInflater.inflate(R.layout.popupwindow_img,null);
            ImageView mExitView = (ImageView) popView.findViewById(R.id.pop_img_exit);
            ImageView mImgView = (ImageView) popView.findViewById(R.id.pop_img_img);
            mExitView.setOnClickListener(mScreenClickListener);
            mImgView.setOnClickListener(mScreenClickListener);
            mImgView.setImageBitmap(AppliteUtils.getLoacalBitmap(
                    (String) SPUtils.get(mActivity, SPUtils.POP_IMG_SAVE_PATH, "")));

            popupWindow = new PopupWindow(popView,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        ColorDrawable cd = new ColorDrawable(0x000000);
        popupWindow.setBackgroundDrawable(cd);
        // 产生背景变暗效果
        WindowManager.LayoutParams lp = mActivity.getWindow().getAttributes();
        lp.alpha = 0.4f;
        mActivity.getWindow().setAttributes(lp);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(rootView.findViewById(R.id.homepage_content), Gravity.CENTER
                | Gravity.CENTER_HORIZONTAL, 0, 0);

        popupWindow.update();
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            // 在dismiss中恢复透明度
            public void onDismiss() {
                WindowManager.LayoutParams lp = mActivity.getWindow()
                        .getAttributes();
                lp.alpha = 1f;
                mActivity.getWindow().setAttributes(lp);
            }
        });
    }

//    private void initViewPop() {
//        ImageView mExitView = (ImageView) rootView.findViewById(R.id.pop_exit);
//        GridView mGridView = (GridView) rootView.findViewById(R.id.pop_gv);
//        TextView mTextView = (TextView) rootView.findViewById(R.id.pop_text);
//        Button mButton = (Button) rootView.findViewById(R.id.pop_button);
//    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.action_dm:
                launchDownloadManagerFragment();
                break;
            case R.id.action_search:
                launchSearchFragment();
                break;
            case R.id.action_upgrade:
                launchUpgradeFragment();
                break;
        }
    }

    private void initActionBar(View customView){
        try {
            ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
            Log.d(TAG, "initActionBar,customView=" + customView);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setTitle("测试");
            actionBar.setCustomView(customView);
//            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
//            actionBar.addTab(actionBar.newTab()
//                    .setText("home")
//                    .setTag(0)
//                    .setTabListener(mBarTabListener));
//            actionBar.addTab(actionBar.newTab()
//                    .setText("game")
//                    .setTag(1)
//                    .setTabListener(mBarTabListener));
            actionBar.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /****
     * 下载管理
     */
    private void launchDownloadManagerFragment() {
        try {
            BundleContext bundleContext = BundleContextFactory.getInstance().getBundleContext();
            OSGIServiceAgent<ApkplugOSGIService> agent = new OSGIServiceAgent<ApkplugOSGIService>(
                    bundleContext, ApkplugOSGIService.class,
                    "(serviceName="+ Constant.OSGI_SERVICE_HOST_OPT+")", //服务查询条件
                    OSGIServiceAgent.real_time);   //每次都重新查询
            agent.getService().ApkplugOSGIService(bundleContext,
                    Constant.OSGI_SERVICE_MAIN_FRAGMENT,
                    0, Constant.OSGI_SERVICE_DM_FRAGMENT);
        } catch (Exception e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        }
    }

    /****
     * 搜索
     */
    private void launchSearchFragment() {
        try {
            BundleContext bundleContext = BundleContextFactory.getInstance().getBundleContext();
            OSGIServiceAgent<ApkplugOSGIService> agent = new OSGIServiceAgent<ApkplugOSGIService>(
                    bundleContext, ApkplugOSGIService.class,
                    "(serviceName="+ Constant.OSGI_SERVICE_HOST_OPT+")", //服务查询条件
                    OSGIServiceAgent.real_time);   //每次都重新查询
            agent.getService().ApkplugOSGIService(bundleContext,
                    Constant.OSGI_SERVICE_MAIN_FRAGMENT,
                    0, Constant.OSGI_SERVICE_SEARCH_FRAGMENT);
        } catch (Exception e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        }
    }

    /**
     * 升级
     */
    private void launchUpgradeFragment() {
        try {
            BundleContext bundleContext = BundleContextFactory.getInstance().getBundleContext();
            OSGIServiceAgent<ApkplugOSGIService> agent = new OSGIServiceAgent<ApkplugOSGIService>(
                    bundleContext, ApkplugOSGIService.class,
                    "(serviceName="+Constant.OSGI_SERVICE_HOST_OPT+")", //服务查询条件
                    OSGIServiceAgent.real_time);   //每次都重新查询
            agent.getService().ApkplugOSGIService(bundleContext,
                    Constant.OSGI_SERVICE_MAIN_FRAGMENT,
                    0, Constant.OSGI_SERVICE_UPDATE_FRAGMENT);
        } catch (Exception e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        }
    }

    public void postMainType(){
        AjaxParams params = new AjaxParams();

        FinalHttp   mMainFinalHttp = new FinalHttp();

        params.put("appkey", Utils.getMitMetaDataValue(mActivity, Utils.META_DATA_MIT));
        params.put("packagename", "com.android.applite1.0");
        params.put("type", "hpmaintype");
        params.put("categorytype", "m_game");
        mMainFinalHttp.post(Utils.URL, params, new AjaxCallBack<Object>() {
            @Override
            public void onSuccess(Object o) {
                super.onSuccess(o);
                Message msg = new Message();
                String reuslt = (String) o;
                HomePageUtils.i(TAG, "HomePage网络请求成功，yuzm reuslt:" + reuslt);
                setData(reuslt,mActivity);
                msg.what = 1;
                mHandler.sendMessage(msg);
                mPagerSlidingTabStrip.setViewPager(mViewPager);
                mPagerSlidingTabStrip.setOnPageChangeListener(mPageChangeListener);
            }

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                super.onFailure(t, errorNo, strMsg);
                HomePageUtils.e(TAG, "HomePage网络请求失败:" + strMsg);
            }
        });
    }
    /**
     * HomePage网络请求
     */
    public void post() {
        //Message msg = new Message();
        if(null == mFinalHttp) {
            mFinalHttp = new FinalHttp();
        }
        AjaxParams params = new AjaxParams();
        params.put("appkey", Utils.getMitMetaDataValue(mActivity, Utils.META_DATA_MIT));
        params.put("packagename", "com.android.applite1.0");
        //params.put("packagename",Utils.getPackgeName(this));
        params.put("app", "applite");
        params.put("type", "homepage");
        params.put("tabTitle", "tabtitle");
        mFinalHttp.post(Utils.URL, params, new AjaxCallBack<Object>() {
            @Override
            public void onSuccess(Object o) {
                super.onSuccess(o);
                Message msg = new Message();
                String reuslt = (String) o;
                HomePageUtils.i(TAG, "HomePage网络请求成功，yuzm reuslt:" + reuslt);
                setData(reuslt,mActivity);
                msg.what = 0;
                mHandler.sendMessage(msg);
                mPagerSlidingTabStrip.setViewPager(mViewPager);
                mPagerSlidingTabStrip.setOnPageChangeListener(mPageChangeListener);
            }

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                super.onFailure(t, errorNo, strMsg);
                HomePageUtils.e(TAG, "HomePage网络请求失败:" + strMsg);
            }
        });
    }
    /**
     * 设置数据
     *
     * @param data
     */
    public void setData(final String data,final Activity mActivity) {
        HomePageBean hpBeanGoods = null;
        HomePageBean hpBeanOrders = null;
        HomePageTypeBean hpBeanMainType = null;
        HomePageTab hpTab = null;
        try {
            JSONObject obj = new JSONObject(data);
            HomePageUtils.i(TAG, "setData JSONObject data，yuzm obj : " + obj);
            int app_key = obj.getInt("app_key");
            String tabStr = obj.getString("subject_data");
            HomePageUtils.i(TAG, " setData JSONObject data，yuzm tabStr : " + tabStr);
            JSONArray subjectData = new JSONArray(tabStr);
            HomePageUtils.i(TAG, "setData JSONObject data,yuzm subjectData : " + subjectData);
            mHPTabContents.clear();
            for (int j = 0; j < subjectData.length(); j++) {
                JSONObject object = new JSONObject(subjectData.get(j).toString());
                //HomePageUtils.i(TAG, "setData JSONObject data，S_name : " + object.getString("s_name"));
                hpTab = new HomePageTab();
                hpTab.setId(1 + (Integer) SPUtils.get(mActivity, SPUtils.HOMEPAGE_POSITION, 0));
                hpTab.setName(object.getString("s_name"));
                hpTab.setKey(object.getString("s_key"));
                //HomePageUtils.i(TAG, "setData JSONObject data，hpTab.getName() : " + hpTab.getName());
                mHPTabContents.add(hpTab);
                SPUtils.put(mActivity, SPUtils.HOMEPAGE_POSITION,
                        (Integer) SPUtils.get(mActivity, SPUtils.HOMEPAGE_POSITION, 0) + 1);
            }
            //goods_data
            String goods_data = obj.getString("goods_data");
            JSONArray goods_json = new JSONArray(goods_data);
            //HomePageUtils.i(TAG, "setData JSONObject data，json : " + json);
            HomePageUtils.i( TAG,"yuzm goods_json.length() : " + goods_json.length());
            HomePageUtils.i( TAG,"yuzm goods_json : " + goods_json);
            for (int i = 0; i < goods_json.length(); i++) {
                JSONObject object = new JSONObject(goods_json.get(i).toString());
                hpBeanGoods = new HomePageBean();
                hpBeanGoods.setId(1 + (Integer) SPUtils.get(mActivity, SPUtils.HOMEPAGE_POSITION, 0));
                hpBeanGoods.setPackagename(object.getString("packageName"));
                hpBeanGoods.setName(object.getString("name"));
                hpBeanGoods.setImgurl(object.getString("iconUrl"));
                hpBeanGoods.setUrl(object.getString("rDownloadUrl"));
                hpBeanGoods.setApkSize(object.getString("apkSize"));
                hpBeanGoods.setRating(object.getString("rating"));
                hpBeanGoods.setBrief(object.getString("brief"));
                hpBeanGoods.setBoxLabel(object.getString("boxLabel"));
                hpBeanGoods.setCategoryMain(object.getString("categorymain"));
                hpBeanGoods.setCategorySub(object.getString("categorysub"));
                hpBeanGoods.setDownloadTimes(object.getString("downloadTimes"));
                hpBeanGoods.setVersionName(object.getString("versionName"));
                hpBeanGoods.setmVersionCode(object.getInt("versionCode"));
                hpBeanGoods.setStatus(Utils.isAppInstalled(mActivity, hpBeanGoods.getPackagename(), hpBeanGoods.getmVersionCode()));
                mHomePageGoods.add(hpBeanGoods);
                SPUtils.put(mActivity, SPUtils.HOMEPAGE_POSITION,
                        (Integer) SPUtils.get(mActivity, SPUtils.HOMEPAGE_POSITION, 0) + 1);
            }
            //order_data
            String order_data = obj.getString("order_data");
            JSONArray orderJson = new JSONArray(order_data);
            //HomePageUtils.i( TAG," orderJson.length() : " + orderJson.length());
            HomePageUtils.i( TAG,"yuzm orderJson : " + orderJson);
            //HomePageUtils.i(TAG, "setData JSONObject data，json : " + json);
            HomePageUtils.i(TAG,"yuzm orderJson.length() : " + orderJson.length());
            for (int i = 0; i < orderJson.length(); i++) {
                JSONObject object = new JSONObject(orderJson.get(i).toString());
                hpBeanOrders = new HomePageBean();
                hpBeanOrders.setId(1 + (Integer) SPUtils.get(mActivity, SPUtils.HOMEPAGE_POSITION, 0));
                hpBeanOrders.setPackagename(object.getString("packageName"));
                hpBeanOrders.setName(object.getString("name"));
                hpBeanOrders.setImgurl(object.getString("iconUrl"));
                hpBeanOrders.setUrl(object.getString("rDownloadUrl"));
                hpBeanOrders.setApkSize(object.getString("apkSize"));
                hpBeanOrders.setRating(object.getString("rating"));
                hpBeanOrders.setBrief(object.getString("brief"));
                hpBeanOrders.setBoxLabel(object.getString("boxLabel"));
                hpBeanOrders.setCategoryMain(object.getString("categorymain"));
                hpBeanOrders.setCategorySub(object.getString("categorysub"));
                hpBeanOrders.setDownloadTimes(object.getString("downloadTimes"));
                hpBeanOrders.setVersionName(object.getString("versionName"));
                hpBeanOrders.setmVersionCode(object.getInt("versionCode"));
                hpBeanOrders.setStatus(Utils.isAppInstalled(mActivity, hpBeanOrders.getPackagename(), hpBeanOrders.getmVersionCode()));
                mHomePageOrder.add(hpBeanOrders);
                SPUtils.put(mActivity, SPUtils.HOMEPAGE_POSITION,
                        (Integer) SPUtils.get(mActivity, SPUtils.HOMEPAGE_POSITION, 0) + 1);
            }
            //MainType_data
            String mMainType_data = obj.getString("maintype_data");
            JSONArray mMainType_json = new JSONArray(mMainType_data);
            HomePageUtils.i(TAG, "yuzm mMainType_json.length() : " + mMainType_json.length());
            HomePageUtils.i(TAG, "yuzm mMainType_json : " + mMainType_json);
            for (int i = 0; i < mMainType_json.length(); i++) {
                JSONObject object = new JSONObject(mMainType_json.get(i).toString());
                hpBeanMainType = new HomePageTypeBean();
                hpBeanMainType.setId(1 + (Integer) SPUtils.get(mActivity, SPUtils.HOMEPAGE_POSITION, 0));
                hpBeanMainType.setM_Name(object.getString("m_name"));
                hpBeanMainType.setM_IconUrl(object.getString("m_iconurl"));
                hpBeanMainType.setM_key("m_key");
                mHomePageMainType.add(hpBeanMainType);
                SPUtils.put(mActivity, SPUtils.HOMEPAGE_POSITION,
                        (Integer) SPUtils.get(mActivity, SPUtils.HOMEPAGE_POSITION, 0) + 1);
            }
            //addAllAppView(mGuideContents);
        } catch (JSONException e) {
            e.printStackTrace();
            HomePageUtils.e(TAG, "HomePageJSON解析异常");
        }
    }
    /**
     * 异步回调回来并处理数据
     */
    public Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    try {
                        if (mHPTabContents.size() == 0){
                            mViewPager.setVisibility(View.GONE);
                        }else{
                            HomePageUtils.e(TAG, "Handler->handleMessage yuzm");
                            mViewPager.setVisibility(View.VISIBLE);
                            mSectionsPagerAdapter.notifyDataSetChanged();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                case 1:
                    HomePageUtils.e(TAG, "Handler->handleMessage yuzm mHPTabContents : "
                            + mHPTabContents + " ; mViewPager : " + mViewPager +
                            " ; mSectionsPagerAdapter : " + mSectionsPagerAdapter);
                    try {
                        if (mHPTabContents.size() == 0){
                            mViewPager.setVisibility(View.GONE);
                        }else{
                            HomePageUtils.e(TAG, "Handler->handleMessage yuzm");
                            mViewPager.setVisibility(View.VISIBLE);
                            mSectionsPagerAdapter.notifyDataSetChanged();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                case 2:

                    break;
                case 3:

                    break;
                default:
                    break;
            }
        }
    };
}

