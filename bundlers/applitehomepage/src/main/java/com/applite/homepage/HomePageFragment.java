package com.applite.homepage;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import com.applite.bean.ScreenBean;
import com.applite.bean.HomePageDataBean;
import com.applite.bean.SubjectData;
import com.applite.common.AppliteUtils;
import com.applite.common.Constant;
import com.applite.common.LogUtils;
import com.applite.common.PagerSlidingTabStrip;
import com.applite.utils.SPUtils;
import net.tsz.afinal.FinalBitmap;
import com.google.gson.Gson;
import com.mit.mitupdatesdk.MitMobclickAgent;
import com.umeng.analytics.MobclickAgent;

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
    private final String TAG = "homepage_PagerFragment";
    private Activity mActivity;
    private View popView;
    private PopupWindow popupWindow;
    private List<ScreenBean> mScreenBeanList = new ArrayList<ScreenBean>();
    private FinalBitmap mFinalBitmap;
    private String mPopImgUrl;
    private boolean mPopIsClick = false;
    private String mPopImgName;
    private long mPopStartTime;
    private long mPopEndTime;

    private LayoutInflater mInflater;
    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private PagerSlidingTabStrip mPagerSlidingTabStrip;

    private Gson mGson;
    private FinalHttp mFinalHttp;
    private List<SubjectData> mPageData;

    private String mCategory;   //null：首页   非null:分类
    private String mTitle;

    private View mRetrybtn;

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


    Runnable mRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            if (null == mPageData){
                mViewPager.setVisibility(View.GONE);
            }else{
                mViewPager.setVisibility(View.VISIBLE);
                mSectionsPagerAdapter.notifyDataSetChanged();
            }
            mPagerSlidingTabStrip.setViewPager(mViewPager);
//            mPagerSlidingTabStrip.setOnPageChangeListener(mPageChangeListener);
        }
    };
    private ViewGroup rootView;

    public HomePageFragment() {
        mGson = new Gson();
        mFinalHttp = new FinalHttp();
        mPageData = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.d(TAG, "onCreate savedInstanceState : " + savedInstanceState);
        mCategory = null;
        Bundle b = getArguments();
        if (null != b){
            mCategory = b.getString("param1");
            mTitle = b.getString("param2");
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        LogUtils.d(TAG, "onAttach ");
        mActivity = activity;
        mInflater = LayoutInflater.from(mActivity);
        try {
            Context context = BundleContextFactory.getInstance().getBundleContext().getBundleContext();
            if (null != context) {
                mInflater = LayoutInflater.from(context);
                mInflater = mInflater.cloneInContext(context);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

//        initActionBar();
    }

    @Override
    public void onDetach(){
        super.onDetach();
        LogUtils.i(TAG, "onDetach");
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        LogUtils.i(TAG, "onDestroy");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LogUtils.d(TAG, "onCreateView");
        rootView = (ViewGroup)mInflater.inflate(R.layout.fragment_homepage_main, container, false);
        if (null == mSectionsPagerAdapter){
            mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());
        }
        mViewPager = (ViewPager) rootView.findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        initActionBar();
        if (null == mPageData){
            mViewPager.setVisibility(View.GONE);
            httpRequest();
        }else{
            mViewPager.setVisibility(View.VISIBLE);
        }
        mPagerSlidingTabStrip.setViewPager(mViewPager);

        boolean networkState = NetworkDetector.detect(getActivity());
        LogUtils.i(TAG, "networkState = " + networkState);
        if(networkState == false){
            ViewGroup offnetView = (ViewGroup)mInflater.inflate(R.layout.off_net_custom, container, false);
            mRetrybtn = offnetView.findViewById(R.id.retry_btn);
            mRetrybtn.setOnClickListener(new View.OnClickListener(){
                public void onClick(View paramView){
                    LogUtils.i(TAG, "click the retry button ");
                    httpRequest();
                    popupWindowPost();
                }
            });
            return offnetView;
        }

        popupWindowPost();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("HomePageFragment"); //统计页面
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("HomePageFragment");
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden){
            LogUtils.i(TAG, "重新显示ActionBar");
            initActionBar();
            mActivity.runOnUiThread(mRefreshRunnable);
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
                if (null != o){
                    String result = (String) o;
                    setData(result);
                    LogUtils.i(TAG, "插屏网络请求成功，result:" + result);
                }
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
                if (!TextUtils.isEmpty(mPopImgUrl))
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
                        if (System.currentTimeMillis() > mPopStartTime && System.currentTimeMillis() < mPopEndTime){
//                            if(SPUtils.get(mActivity,SPUtils.POP_IMGURL,"").equals(mPopImgUrl)){
//                                if (!(boolean)SPUtils.get(mActivity,SPUtils.POP_IMGURL_ISCLICK,false))
//                                    initPopuWindow();
//                            }else {
                                initPopuWindow();
//                            }
                        }
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

    public void onDestroyView(){
        super.onDestroyView();
        LogUtils.i(TAG, "onDestroyView");
        mViewPager = null;
        mPagerSlidingTabStrip = null;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.action_personal:
                launchPersonalFragment();
                break;
            case R.id.action_search:
                launchSearchFragment();
                MitMobclickAgent.onEvent(mActivity, "toSearchFragment");
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                getFragmentManager().popBackStack();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initActionBar(){
        try {
            ViewGroup customView = (ViewGroup)mInflater.inflate(R.layout.actionbar_custom,null);
            View personal = customView.findViewById(R.id.action_personal);
            personal.setOnClickListener(this);
            View search = customView.findViewById(R.id.action_search);
            search.setOnClickListener(this);
            //加入滑动tab管理viewPager
            //mPagerSlidingTabStrip = PagerSlidingTabStrip.inflate(mActivity,null,false);

            ViewGroup title = (ViewGroup)customView.findViewById(R.id.action_title);
            mPagerSlidingTabStrip = PagerSlidingTabStrip.inflate(mActivity,title,false);
            title.addView(mPagerSlidingTabStrip);
            ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            if (null == mCategory) {
                personal.setVisibility(View.VISIBLE);
                title.setVisibility(View.VISIBLE);
                actionBar.setDisplayHomeAsUpEnabled(false);
                actionBar.setDisplayShowTitleEnabled(false);
            }else{
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setTitle(mTitle);
                personal.setVisibility(View.GONE);
                title.setVisibility(View.GONE);
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
            actionBar.setCustomView(customView);
            actionBar.show();
        }catch (Exception e){
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

    /***
     * 进入个人中心
     */
    private void launchPersonalFragment() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(getId(),new PersonalFragment(),"personal");
//        ft.hide(fm.findFragmentByTag(Constant.OSGI_SERVICE_MAIN_FRAGMENT));//得到首页Fragment，然后隐藏
//        ft.add(getId(),new PersonalFragment(),"personal");
        ft.addToBackStack(null);
        ft.commit();
    }

    /**
     * HomePage网络请求
     */
    private void httpRequest() {
        AjaxParams params = new AjaxParams();
        params.put("appkey", AppliteUtils.getMitMetaDataValue(mActivity, Constant.META_DATA_MIT));
        params.put("packagename", "com.android.applite1.0");
//        params.put("packagename", mActivity.getPackageName());
        params.put("app", "applite");
        if (null == mCategory) {
            params.put("type", "homepage");
            params.put("tabTitle", "tabtitle");
        }else{
            params.put("type", "hpmaintype");
            params.put("categorytype", mCategory);
        }
        mFinalHttp.post(Constant.URL, params, new AjaxCallBack<Object>() {
            @Override
            public void onSuccess(Object o) {
                super.onSuccess(o);
                LogUtils.d(TAG,"首页数据："+o.toString());
                try {
                    HomePageDataBean data = mGson.fromJson((String) o, HomePageDataBean.class);
                    if (1 == data.getAppKey()){
                        mPageData = data.getSubjectData();
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }

                mActivity.runOnUiThread(mRefreshRunnable);
            }

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                super.onFailure(t, errorNo, strMsg);
                LogUtils.e(TAG, "HomePage网络请求失败:" + strMsg);
            }
        });
    }

    class SectionsPagerAdapter extends FragmentStatePagerAdapter {
        private static final String TAG = "homepage_adapter";
        private int mChildCount = 0;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            LogUtils.d(TAG,"destroyItem,"+position);
            super.destroyItem(container, position, object);
        }

        @Override
        public Fragment getItem(int position) {
            LogUtils.d(TAG,"getItem,"+position);
            return new HomePageListFragment(mPageData.get(position));
        }

        @Override
        public int getCount() {
            return (null != mPageData)?mPageData.size():0;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (null != mPageData)
                return mPageData.get(position).getS_name();
            else
                return "";
        }

        @Override
        public void notifyDataSetChanged () {
            mChildCount = getCount();
            super.notifyDataSetChanged();
        }

        @Override
        public int getItemPosition(Object object) {
            if (mChildCount > 0){
                mChildCount -- ;
                return POSITION_NONE;
            }
            return super.getItemPosition(object);
        }
    }
}

