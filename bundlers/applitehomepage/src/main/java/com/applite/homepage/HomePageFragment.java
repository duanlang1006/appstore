package com.applite.homepage;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.applite.bean.HomePageDataBean;
import com.applite.bean.SubjectData;
import com.applite.common.AppliteUtils;
import com.applite.common.Constant;
import com.applite.common.LogUtils;
import com.applite.common.PagerSlidingTabStrip;
import com.google.gson.Gson;
import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;
import org.apkplug.Bundle.ApkplugOSGIService;
import org.apkplug.Bundle.OSGIServiceAgent;
import org.osgi.framework.BundleContext;

import java.util.List;

/**
 * Created by yuzhimin on 6/17/15.
 */
public class HomePageFragment extends Fragment implements View.OnClickListener{
    private final String TAG = "homepage_PagerFragment";
    private Activity mActivity;
    private LayoutInflater mInflater;
    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private PagerSlidingTabStrip mPagerSlidingTabStrip;

    private Gson mGson;
    private FinalHttp mFinalHttp;
    private List<SubjectData> mPageData;

    private String mCategory;   //null：首页   非null:分类
    private String mTitle;

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
        mActivity = activity;
        LogUtils.d(TAG, "onAttach ");
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
        mInflater = inflater;
        try {
            Context context = BundleContextFactory.getInstance().getBundleContext().getBundleContext();
            if (null != context) {
                mInflater = LayoutInflater.from(context);
                mInflater = mInflater.cloneInContext(context);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        ViewGroup rootView = (ViewGroup)mInflater.inflate(R.layout.fragment_homepage_main, container, false);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());
        mViewPager = (ViewPager) rootView.findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        initActionBar();
        if (null == mPageData){
            mViewPager.setVisibility(View.GONE);
        }else{
            mViewPager.setVisibility(View.VISIBLE);
        }
        mPagerSlidingTabStrip.setViewPager(mViewPager);

        httpRequest();
        LogUtils.d(TAG, "onCreateView");
        return rootView;
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        LogUtils.i(TAG, "onDestroyView");
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.action_personal:
//                launchDownloadManagerFragment();
                launchUpgradeFragment();
                break;
            case R.id.action_search:
                launchSearchFragment();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                ((FragmentActivity)mActivity).getSupportFragmentManager().popBackStack();
                break;
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
            mPagerSlidingTabStrip = PagerSlidingTabStrip.inflate(mActivity,null,false);
            customView.addView(mPagerSlidingTabStrip,1);
            ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            if (null == mCategory) {
                personal.setVisibility(View.VISIBLE);
                actionBar.setDisplayHomeAsUpEnabled(false);
                actionBar.setDisplayShowTitleEnabled(false);
            }else{
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setTitle(mTitle);
                personal.setVisibility(View.GONE);
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
            actionBar.setCustomView(customView);
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
        private static final String TAG = "SectionsPagerAdapter";
        private int mChildCount = 0;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
        }

        @Override
        public Fragment getItem(int position) {
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

