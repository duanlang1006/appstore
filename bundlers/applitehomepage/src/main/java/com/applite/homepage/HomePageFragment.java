package com.applite.homepage;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.applite.bean.HomePageDataBean;
import com.applite.common.AppliteUtils;
import com.applite.common.Constant;
import com.applite.common.LogUtils;
import com.applite.common.PagerSlidingTabStrip;
import com.applite.data.SectionsPagerAdapter;

import com.google.gson.Gson;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.apkplug.Bundle.ApkplugOSGIService;
import org.apkplug.Bundle.OSGIServiceAgent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuzhimin on 6/17/15.
 */
public class HomePageFragment extends Fragment implements View.OnClickListener{
    private final String TAG = "homepage_PagerFragment";
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private PagerSlidingTabStrip mPagerSlidingTabStrip;
    private Activity mActivity;
    private FinalHttp mFinalHttp;

    Gson mGson;
    HomePageDataBean mHomePageDataBean = null;
    private int mTableType = 0;
    private boolean isMainType = false;
    private static int mNode;

    private final ViewPager.OnPageChangeListener mPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int i, float v, int i2) {
        }

        @Override
        public void onPageSelected(int i) {
        }

        @Override
        public void onPageScrollStateChanged(int i) {
        }
    };

    public HomePageFragment() {
        setHasOptionsMenu(true);
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

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;

        LogUtils.d(TAG, "onAttach activity : " + activity+","+this.getId()+","+mNode);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LogUtils.d(TAG, "onCreate savedInstanceState : " + savedInstanceState);
        //getArguments();
    }
    @Override
    public void onStart(){
        super.onStart();

        LogUtils.i(TAG, "onStart");
    }
    @Override
    public void onPause(){
        super.onPause();

        LogUtils.i(TAG, "onPause");
    }
    @Override
    public void onResume(){
        super.onResume();

        LogUtils.i(TAG, "onResume");
    }
    @Override
    public void onStop(){
        super.onStop();

        LogUtils.i(TAG, "onStop");
    }
    @Override
    public void onDestroy(){
        super.onDestroy();

        LogUtils.i(TAG, "onDestroy");
    }
    @Override
    public void onDetach(){
        super.onDetach();

        LogUtils.i(TAG, "onDetach");
    }
    @Override
    public void onDestroyView(){
        super.onDestroyView();

        LogUtils.i(TAG, "onDestroyView");
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LayoutInflater mInflater = inflater;
        try {
            Context context = BundleContextFactory.getInstance().getBundleContext().getBundleContext();
            if (null != context) {
                mInflater = LayoutInflater.from(context);
                mInflater = mInflater.cloneInContext(context);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        mFinalHttp = new FinalHttp();

        post();

        ViewGroup rootView = (ViewGroup)mInflater.inflate(R.layout.fragment_activity_page, container, false);
        mSectionsPagerAdapter = new SectionsPagerAdapter(this.getFragmentManager(),mHomePageDataBean,mActivity);
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) rootView.findViewById(R.id.pager);

        mViewPager.setAdapter(mSectionsPagerAdapter);

        //加入滑动tab管理viewPager
        mPagerSlidingTabStrip = PagerSlidingTabStrip.inflate(mActivity,container,false);
        rootView.addView(mPagerSlidingTabStrip,0);
        mPagerSlidingTabStrip.setViewPager(mViewPager);
        mPagerSlidingTabStrip.setOnPageChangeListener(mPageChangeListener);
        View customView = mInflater.inflate(R.layout.custom_actionbar_main,container,false);
        customView.findViewById(R.id.action_dm).setOnClickListener(this);
        customView.findViewById(R.id.action_search).setOnClickListener(this);
        customView.findViewById(R.id.action_upgrade).setOnClickListener(this);
        initActionBar(customView);
        if (null == mHomePageDataBean){
            mViewPager.setVisibility(View.GONE);
        }else{
            mViewPager.setVisibility(View.VISIBLE);
        }

        LogUtils.d(TAG, "onCreateView");
        return rootView;
    }

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

    /****
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

        params.put("appkey", AppliteUtils.getMitMetaDataValue(mActivity, Constant.META_DATA_MIT));
        params.put("packagename", "com.android.applite1.0");
        params.put("type", "hpmaintype");
        params.put("categorytype", "m_game");
        mMainFinalHttp.post(Constant.URL, params, new AjaxCallBack<Object>() {
            @Override
            public void onSuccess(Object o) {
                super.onSuccess(o);
                Message msg = new Message();
                String reuslt = (String) o;
                LogUtils.i(TAG, "HomePage网络请求成功，yuzm reuslt:" + reuslt);
                setData(reuslt,mActivity);
                msg.what = 1;
                mHandler.sendMessage(msg);
                mPagerSlidingTabStrip.setViewPager(mViewPager);
                mPagerSlidingTabStrip.setOnPageChangeListener(mPageChangeListener);
            }

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                super.onFailure(t, errorNo, strMsg);
                LogUtils.e(TAG, "HomePage网络请求失败:" + strMsg);
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
        params.put("appkey", AppliteUtils.getMitMetaDataValue(mActivity, Constant.META_DATA_MIT));
        params.put("packagename", "com.android.applite1.0");
//        params.put("packagename", mActivity.getPackageName());
        params.put("app", "applite");
        params.put("type", "homepage");
        params.put("tabTitle", "tabtitle");
        mFinalHttp.post(Constant.URL, params, new AjaxCallBack<Object>() {
            @Override
            public void onSuccess(Object o) {
                super.onSuccess(o);
                Message msg = new Message();
                String reuslt = (String) o;
                LogUtils.i(TAG, "HomePage网络请求成功，yuzm reuslt:" + reuslt);
                setData(reuslt,mActivity);
                msg.what = 0;
                mHandler.sendMessage(msg);

            }

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                super.onFailure(t, errorNo, strMsg);
                LogUtils.e(TAG, "HomePage网络请求失败:" + strMsg);
            }
        });
    }
    /**
     * 设置数据
     *
     * @param data
     */
    public void setData(final String data,final Activity mActivity) {

        mGson = new Gson();
        mHomePageDataBean = mGson.fromJson(data, HomePageDataBean.class);
        LogUtils.i(TAG, "setData Gson data，mHomePageDataBean : " + mHomePageDataBean);
        LogUtils.i(TAG, "setData Gson data，mHomePageDataBean.getSubjectData.getSubjectData() : "
                + mHomePageDataBean.getSubjectData());
        //mHomePageDataBean.getSubjectData().get(0).;

        try {
            JSONObject obj = new JSONObject(data);
            LogUtils.i(TAG, "setData JSONObject data，obj : " + obj);
        } catch (JSONException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "HomePageJSON解析异常");
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
                        LogUtils.i(TAG, "ListFragment.onCreateView()Thread.currentThread().getId() : " +
                                Thread.currentThread().getId());
                        LogUtils.e(TAG, "Handler->handleMessage Gson mHomePageDataBean : " + mHomePageDataBean);
                        if (null == mHomePageDataBean){
                            mViewPager.setVisibility(View.GONE);
                        }else{
                            LogUtils.e(TAG, "Handler->handleMessage");
                            mSectionsPagerAdapter.setHomePageDataBean(mHomePageDataBean);
                            mPagerSlidingTabStrip.setViewPager(mViewPager);
                            mPagerSlidingTabStrip.setOnPageChangeListener(mPageChangeListener);
                            mViewPager.setVisibility(View.VISIBLE);
                            mSectionsPagerAdapter.notifyDataSetChanged();

                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                case 1:
                    LogUtils.e(TAG, "Handler->handleMessage : " + " ; mViewPager : " + mViewPager +
                            " ; mSectionsPagerAdapter : " + mSectionsPagerAdapter);
                    try {
                        if (null == mHomePageDataBean){
                            mViewPager.setVisibility(View.GONE);
                        }else{
                            LogUtils.e(TAG, "Handler->handleMessage");
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

