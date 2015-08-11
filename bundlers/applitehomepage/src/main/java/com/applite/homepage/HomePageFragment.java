package com.applite.homepage;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.internal.view.SupportMenu;
import android.support.v4.internal.view.SupportMenuItem;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.applite.bean.ScreenBean;
import com.applite.bean.HomePageDataBean;
import com.applite.bean.SubjectData;
import com.applite.common.AppliteUtils;
import com.applite.common.Constant;
import com.applite.common.LogUtils;
import com.applite.common.PagerSlidingTabStrip;
import com.applite.utils.HomepageUtils;
import com.applite.utils.SPUtils;
import net.tsz.afinal.FinalBitmap;
import com.google.gson.Gson;
import com.mit.mitupdatesdk.MitMobclickAgent;
import com.osgi.extra.OSGIBaseFragment;
import com.osgi.extra.OSGIServiceHost;
import com.umeng.analytics.MobclickAgent;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;
import net.tsz.afinal.http.HttpHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuzhimin on 6/17/15.
 */
public class HomePageFragment extends OSGIBaseFragment implements View.OnClickListener{
    private final String TAG = "homepage_PagerFragment";
    private Activity mActivity;
    private View popView;
    private PopupWindow popupWindow;
    private List<ScreenBean> mScreenBeanList = new ArrayList<ScreenBean>();
    private FinalBitmap mFinalBitmap;
    private String mPopImgUrl;
    private boolean mPopIsClick = false;
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

    private RelativeLayout mLoadingarea;
    private View mLoadingView;
    private TextView loadingText;
    private View mOffnetView;

    private ImageView loadingView;
    Animation LoadingAnimation;

    private Button mRetrybtn;
    private View offnetImg;
    private boolean refreshflag;

    Runnable mRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            LogUtils.i(TAG, "mRefreshRunnable run");
            if(mViewPager != null) {
                if (null == mPageData) {
                    mViewPager.setVisibility(View.GONE);
                } else {
                    mViewPager.setVisibility(View.VISIBLE);
                    mSectionsPagerAdapter.notifyDataSetChanged();
                }
            mPagerSlidingTabStrip.setViewPager(mViewPager);
            }
        }
    };
    private ViewGroup rootView;
    private SubjectData mPopData = new SubjectData();


    public static OSGIBaseFragment newInstance(Fragment fg,Bundle params){
        return new HomePageFragment(fg,params);
    }

    public static Bundle newBundle(String category,String title){
        Bundle bundle = new Bundle();
        bundle.putString("category",category);
        bundle.putString("title",title);
        return bundle;
    }

    private HomePageFragment(Fragment mFragment, Bundle params) {
        super(mFragment, params);
        mGson = new Gson();
        mFinalHttp = new FinalHttp();
        mPageData = null;
        if (null != params){
            mCategory = params.getString("category");
            mTitle = params.getString("title");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.d(TAG, "onCreate savedInstanceState : " + savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        LogUtils.d(TAG, "onAttach ");
        mActivity = activity;
        mInflater = LayoutInflater.from(mActivity);
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

        boolean networkState = NetworkDetector.detect(getActivity());
        LogUtils.i(TAG, "networkState = " + networkState);

        rootView = (ViewGroup)mInflater.inflate(R.layout.fragment_homepage_main, container, false);

        mLoadingarea = (RelativeLayout)rootView.findViewById(R.id.top_parent);
        //加载中显示动画资源及文字
        mLoadingView = rootView.findViewById(R.id.loading_img);
        LoadingAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.tip);
        LinearInterpolator lin = new LinearInterpolator();
        LoadingAnimation.setInterpolator(lin);
        if (LoadingAnimation != null) {
            mLoadingView.startAnimation(LoadingAnimation);
        }
        loadingText = (TextView) rootView.findViewById(R.id.loading_text);

        //无网络连接时显示图片资源
        mOffnetView = rootView.findViewById(R.id.middle_parent);
        offnetImg = (ImageView)rootView.findViewById(R.id.off_img);
        mRetrybtn = (Button)rootView.findViewById(R.id.refresh_btn);

        mOffnetView.setVisibility(View.GONE);

        if(!networkState){
             mLoadingarea.setVisibility(View.GONE);
             //mLoadingView.setVisibility(View.GONE);
             //loadingText.setVisibility(View.GONE);
             mLoadingView.clearAnimation();
             mOffnetView.setVisibility(View.VISIBLE);

             mRetrybtn.setOnClickListener(new View.OnClickListener(){
                public void onClick(View paramView){
                    LogUtils.i(TAG, "click the retry button ");
                    httpRequest();
                    popupWindowPost();
                }
            });
        }

        if (null == mSectionsPagerAdapter){
            mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());
        }
        mViewPager = (ViewPager) rootView.findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mPagerSlidingTabStrip = PagerSlidingTabStrip.inflate(mActivity,container,false);
        initActionBar();
        if (null == mPageData){
            mViewPager.setVisibility(View.GONE);
            httpRequest();
        }else{
            mViewPager.setVisibility(View.VISIBLE);
        }
        mPagerSlidingTabStrip.setViewPager(mViewPager);

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
        }else{
            try{
                ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
                actionBar.setHomeAsUpIndicator(getActivity().getResources().getDrawable(R.drawable.action_bar_back_light));
            }catch(Exception e){
                e.printStackTrace();
            }
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
                    mPopData.setS_key(obj.getString("spt_key"));
                    mPopData.setS_name(obj.getString("pl_name"));
                    mPopData.setStep(obj.getInt("step"));
                    mPopData.setS_datatype(obj.getString("s_datatype"));
                    mPopImgUrl = obj.getString("pl_iconurl");
                    mPopStartTime = obj.getLong("pl_starttime") * 1000;
                    mPopEndTime = obj.getLong("pl_endtime") * 1000;
                }
                if (!TextUtils.isEmpty(mPopImgUrl))
                    download(mPopData.getS_name()+".jpg",mPopImgUrl);
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
            popupWindow.dismiss();
            LogUtils.i("lang", "v.getId() = "+v.getId());
            if (v.getId() == R.id.pop_img_exit) {

            }else if (v.getId() == R.id.pop_img_img) {
                HomepageUtils.toTopicFragment(((OSGIServiceHost) mActivity), mPopData.getS_key(), mPopData.getS_name(), mPopData.getStep(), mPopData.getS_datatype());
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main_homepage, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        if (null != item){
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            if (null == mCategory){
                HomepageUtils.launchPersonalFragment(((OSGIServiceHost) mActivity));
                return true;
            }
        }else if (item.getItemId() == R.id.action_search){
            HomepageUtils.launchSearchFragment((OSGIServiceHost)mActivity);
            MitMobclickAgent.onEvent(mActivity, "toSearchFragment");
            return true;
        }else if (item.getItemId() == R.id.action_dm){
            HomepageUtils.launchDmFragment((OSGIServiceHost)mActivity);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.action_personal) {
            HomepageUtils.launchPersonalFragment(((OSGIServiceHost) mActivity));
        }else if (v.getId() == R.id.action_search){
            HomepageUtils.launchSearchFragment(((OSGIServiceHost)mActivity));
            MitMobclickAgent.onEvent(mActivity, "toSearchFragment");
        }
    }

    private void initActionBar(){
        try {
//            ViewGroup customView = (ViewGroup)mInflater.inflate(R.layout.actionbar_custom,new LinearLayout(mActivity),false);
//            View personal = customView.findViewById(R.id.action_personal);
//            personal.setOnClickListener(this);
//            View search = customView.findViewById(R.id.action_search);
//            search.setOnClickListener(this);
//            //加入滑动tab管理viewPager
//            //mPagerSlidingTabStrip = PagerSlidingTabStrip.inflate(mActivity,null,false);
//
//            ViewGroup title = (ViewGroup)customView.findViewById(R.id.action_title);
//            mPagerSlidingTabStrip = PagerSlidingTabStrip.inflate(mActivity,title,false);
//            title.addView(mPagerSlidingTabStrip);
            ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();

            if (null == mCategory) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setHomeAsUpIndicator(getActivity().getResources().getDrawable(R.drawable.icon_personal_light));
                actionBar.setDisplayShowTitleEnabled(false);
            }else{
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setHomeAsUpIndicator(getActivity().getResources().getDrawable(R.drawable.action_bar_back_light));
                actionBar.setHomeButtonEnabled(true);
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setTitle(mTitle);
            }
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(mPagerSlidingTabStrip);
            actionBar.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * HomePage网络请求
     */
    private void httpRequest() {
        LogUtils.i(TAG, "httpRequest");
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
        mOffnetView.setVisibility(View.GONE);
        mLoadingarea.setVisibility(View.VISIBLE);
        if (LoadingAnimation != null) {
            mLoadingView.startAnimation(LoadingAnimation);
        }

        mFinalHttp.post(Constant.URL, params, new AjaxCallBack<Object>() {
            @Override
            public void onSuccess(Object o) {
                super.onSuccess(o);
                LogUtils.i(TAG, "获取首页数据:");
                try {
                    HomePageDataBean data = mGson.fromJson((String) o, HomePageDataBean.class);
                    if (1 == data.getAppKey()) {
                        mPageData = data.getSubjectData();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (LoadingAnimation != null) {
                    mLoadingView.startAnimation(LoadingAnimation);
                }
                mActivity.runOnUiThread(mRefreshRunnable);
            }

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                super.onFailure(t, errorNo, strMsg);
                mLoadingView.clearAnimation();
                mLoadingarea.setVisibility(View.GONE);
                mOffnetView.setVisibility(View.VISIBLE);
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
            Fragment f = (Fragment)object;
            if (f.isAdded()) {
                LogUtils.d(TAG,"destroyItem,"+position);
                super.destroyItem(container, position, object);
            }
        }

        @Override
        public Fragment getItem(int position) {
            LogUtils.d(TAG,"getItem,"+position);
            OSGIServiceHost host = (OSGIServiceHost)mActivity;
            Fragment fg = host.newFragment(Constant.OSGI_SERVICE_MAIN_FRAGMENT,
                    HomePageListFragment.class.getName(),
                    HomePageListFragment.newBundle(mPageData.get(position),false));
            return fg;
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

