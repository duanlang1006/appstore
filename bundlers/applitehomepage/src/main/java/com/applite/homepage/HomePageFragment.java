package com.applite.homepage;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.applite.bean.HomePageDataBean;
import com.applite.bean.PopupWindowBean;
import com.applite.bean.ScreenBean;
import com.applite.bean.SubjectData;
import com.applite.common.AppliteUtils;
import com.applite.common.Constant;
import com.applite.common.DefaultValue;
import com.applite.common.LogUtils;
import com.applite.common.PagerSlidingTabStrip;
import com.applite.utils.SPUtils;
import com.applite.view.ScreenView;
import com.google.gson.Gson;
import com.lidroid.xutils.HttpUtils;
import com.mit.mitupdatesdk.MitMobclickAgent;
import com.mit.mitupdatesdk.MitUpdateAgent;
import com.osgi.extra.OSGIBaseFragment;
import com.osgi.extra.OSGIServiceHost;

import net.tsz.afinal.FinalBitmap;
import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;
import net.tsz.afinal.http.HttpHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class HomePageFragment extends OSGIBaseFragment implements View.OnClickListener {
    private final String TAG = "homepage_PagerFragment";
    private List<ScreenBean> mScreenBeanList = new ArrayList<ScreenBean>();
    private FinalBitmap mFinalBitmap;
    private boolean mPopIsClick = false;

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
    private String whichPage;

    private HttpUtils mHttpUtils;

    private boolean homeflag = true;

    private ViewPager.OnPageChangeListener mPageChangeListener = new ViewPager.OnPageChangeListener() {
        private int prePosition = -1;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (prePosition != position) {
//                LogUtils.d(TAG, "onPageScrolled : " + position);
                prePosition = position;
                MitMobclickAgent.onEvent(mActivity, HomePageListFragment.class.getSimpleName() + position + "_onScrolled");
            }
        }

        @Override
        public void onPageSelected(int position) {
//            LogUtils.d(TAG, "onPageSelected : " + position);
            ActionBar actionBar = ((ActionBarActivity) mActivity).getSupportActionBar();
            if (actionBar.getNavigationMode() == ActionBar.NAVIGATION_MODE_TABS) {
                actionBar.setSelectedNavigationItem(position);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
//            LogUtils.d(TAG, "onPageScrollStateChanged : " + state);
        }
    };

    Runnable mRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            LogUtils.i(TAG, "mRefreshRunnable run");
            if (mViewPager != null) {
                if (null == mPageData) {
                    mViewPager.setVisibility(View.GONE);
                } else {
                    mViewPager.setVisibility(View.VISIBLE);
                    mSectionsPagerAdapter.notifyDataSetChanged();
                }
//                mPagerSlidingTabStrip.setViewPager(mViewPager);
//                mPagerSlidingTabStrip.setOnPageChangeListener(mPageChangeListener);
            }
        }
    };
    private ViewGroup rootView;
    private PopupWindowBean mPopData = new PopupWindowBean();
    private String mPopType;
    private ScreenView mScreenView;

    public static Bundle newBundle(String category, String title) {
        Bundle bundle = new Bundle();
        bundle.putString("category", category);
        bundle.putString("title", title);
        return bundle;
    }

    public HomePageFragment() {
        super();
        mGson = new Gson();
        mFinalHttp = new FinalHttp();
        mPageData = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        LogUtils.d(TAG, "onCreate savedInstanceState : " + savedInstanceState);
        if (null == mCategory) {
            whichPage = this.getClass().getSimpleName();
        } else {
            whichPage = this.getClass().getSimpleName() + "_" + mCategory;
        }
        MitUpdateAgent.update(mActivity);
        MitMobclickAgent.onEvent(mActivity, whichPage + "_onCreate");
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        LogUtils.d(TAG, "onAttach ");
        mHttpUtils = new HttpUtils();
        mInflater = LayoutInflater.from(mActivity);
        Bundle params = getArguments();
        if (null != params) {
            mCategory = params.getString("category");
            mTitle = params.getString("title");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        LogUtils.i(TAG, "onDetach");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.i(TAG, "onDestroy");
        MitMobclickAgent.onEvent(mActivity, whichPage + "_onDestroy");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LogUtils.d(TAG, "onCreateView");

        boolean networkState = NetworkDetector.detect(mActivity);

        rootView = (ViewGroup) mInflater.inflate(R.layout.fragment_homepage_main, container, false);

        mLoadingarea = (RelativeLayout) rootView.findViewById(R.id.top_parent);
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
        offnetImg = (ImageView) rootView.findViewById(R.id.off_img);
        mRetrybtn = (Button) rootView.findViewById(R.id.refresh_btn);
        mRetrybtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View paramView) {
                LogUtils.i(TAG, "click the retry button ");
                httpRequest();
                postSearchHint();
            }
        });

        mOffnetView.setVisibility(View.GONE);

        if (!networkState) {
            mLoadingarea.setVisibility(View.GONE);
            //mLoadingView.setVisibility(View.GONE);
            //loadingText.setVisibility(View.GONE);
            mLoadingView.clearAnimation();
            mOffnetView.setVisibility(View.VISIBLE);
        }

        if (null == mSectionsPagerAdapter) {
            mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());
        }
        mViewPager = (ViewPager) rootView.findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mPagerSlidingTabStrip = PagerSlidingTabStrip.inflate(mActivity, container, false);
//        initActionBar();
        if (null == mPageData) {
            mViewPager.setVisibility(View.GONE);
            httpRequest();
        } else {
            mViewPager.setVisibility(View.VISIBLE);
        }
        initActionBar();
//        mPagerSlidingTabStrip.setViewPager(mViewPager);
//        mPagerSlidingTabStrip.setOnPageChangeListener(mPageChangeListener);
        popupWindowPost();

        if (null != mHint && mHint.length != 0) {
            startConvenientSearch();
        } else {
            postSearchHint();
        }

        return rootView;
    }

    @Override
    public void onResume() {
        LogUtils.i(TAG, "onResume");
        super.onResume();
        MitMobclickAgent.onPageStart(whichPage); //统计页面
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
                    // handle back button

                    getFragmentManager().popBackStackImmediate();

                    if (!homeflag) {
                        homeflag = true;
                        return true;
                    } else {
                        return false;
                    }
                }
                return false;
            }
        });
    }

    @Override
    public void onPause() {
        LogUtils.i(TAG, "onPause");
        super.onPause();
        MitMobclickAgent.onPageEnd(whichPage);
    }

    @Override
    public void onStop() {
        LogUtils.i(TAG, "onStop");
        super.onStop();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            LogUtils.i(TAG, "重新显示ActionBar");
            initActionBar();
            if (mViewPager != null) {
                mViewPager.setCurrentItem(mTabSelect);
                mSectionsPagerAdapter.notifyDataSetChanged();
            }
        } else {
            LogUtils.i(TAG, "隐藏ActionBar");
            try {
                ActionBar actionBar = ((ActionBarActivity) mActivity).getSupportActionBar();
                actionBar.setHomeAsUpIndicator(mActivity.getResources().getDrawable(R.drawable.action_bar_back_light));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 插屏请求
     */
    private void popupWindowPost() {
        LogUtils.i(TAG, "插屏网络请求");
        AjaxParams params = new AjaxParams();
        params.put("appkey", AppliteUtils.getMitMetaDataValue(mActivity, Constant.META_DATA_MIT));
        params.put("packagename", mActivity.getPackageName());
        params.put("app", "applite");
        params.put("type", "screen");
        params.put("protocol_version", Constant.PROTOCOL_VERSION);
        mFinalHttp.post(Constant.URL, params, new AjaxCallBack<Object>() {
            @Override
            public void onSuccess(Object o) {
                super.onSuccess(o);
                LogUtils.i(TAG, "插屏网络请求成功，result:" + o);
//                LogUtils.i(TAG, "System.currentTimeMillis():" + System.currentTimeMillis());
                if (null != o) {
                    String result = (String) o;
                    setData(result);
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
            mPopType = object.getString("use_info");
            String plaque_info = object.getString("plaque_info");
            String detail_info = object.getString("detail_info");
//            LogUtils.i(TAG, "plaque_info:" + plaque_info);
//            LogUtils.i(TAG, "detail_info:" + detail_info);

            JSONArray plaque_array = new JSONArray(plaque_info);
            if (plaque_array.length() != 0) {
                for (int i = 0; i < plaque_array.length(); i++) {
                    JSONObject obj = new JSONObject(plaque_array.get(i).toString());
                    mPopData.setS_key(obj.getString("spt_key"));
                    mPopData.setS_name(obj.getString("pl_name"));
                    mPopData.setStep(obj.getInt("step"));
                    mPopData.setS_datatype(obj.getString("s_datatype"));
                    mPopData.setmPopImgUrl(obj.getString("pl_iconurl"));
                    mPopData.setmPopStartTime(obj.getLong("pl_starttime") * 1000);
                    mPopData.setmPopEndTime(obj.getLong("pl_endtime") * 1000);
                    mPopData.setmPopImgName(obj.getString("pl_iconurl_img"));
                }
            }
            JSONArray detail_array = new JSONArray(detail_info);
            if (detail_array.length() != 0) {
                for (int i = 0; i < detail_array.length(); i++) {
                    JSONObject obj = new JSONObject(detail_array.get(i).toString());
                    mPopData.setmPackageName(obj.getString("packageName"));
                    mPopData.setmName(obj.getString("name"));
                    mPopData.setmIconUrl(obj.getString("iconUrl"));
                    mPopData.setmPopImgUrl(obj.getString("pl_iconurl"));
                    mPopData.setmPopStartTime(obj.getLong("pl_starttime") * 1000);
                    mPopData.setmPopEndTime(obj.getLong("pl_endtime") * 1000);
                    mPopData.setmPopImgName(obj.getString("pl_iconurl_img"));
                }
            }
            download(mPopData.getmPopImgName(), mPopData.getmPopImgUrl());
        } catch (JSONException e) {
            e.printStackTrace();
            LogUtils.i(TAG, "插屏JSONException");
        }
    }

    /**
     * 下载文件
     */
    private void download(final String name, String url) {
        HttpHandler mHttpHandler = mFinalHttp.download(url, //这里是下载的路径
                AppliteUtils.getAppDir(name), //这是保存到本地的路径
                DefaultValue.defaultValueHTTPSuport,//true:断点续传 false:不断点续传（全新下载）
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
                        if (System.currentTimeMillis() > mPopData.getmPopStartTime() && System.currentTimeMillis() < mPopData.getmPopEndTime()) {
//                            if(SPUtils.get(mActivity,SPUtils.POP_IMGURL,"").equals(mPopImgUrl)){
//                                if (!(boolean)SPUtils.get(mActivity,SPUtils.POP_IMGURL_ISCLICK,false))
//                                    initPopuWindow();
//                            }else {
//                            initPopuWindow();
                            //实例化PopupWindow
                            mScreenView = new ScreenView(mActivity, mScreenClickListener);
                            mScreenView.showAtLocation(rootView.findViewById(R.id.homepage_content), Gravity.CENTER, 0, 0);
                            mScreenView.setImageBitmap(AppliteUtils.getAppDir(name));
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
            SPUtils.put(mActivity, SPUtils.POP_IMGURL_ISCLICK, mPopIsClick);
            mScreenView.dismiss();
            LogUtils.i("lang", "v.getId() = " + v.getId());
            if (v.getId() == R.id.pop_img_exit) {

            } else if (v.getId() == R.id.pop_img_img) {
                if ("plaque_info".equals(mPopType)) {
                    ((OSGIServiceHost) mActivity).jumptoTopic(mPopData.getS_key(),
                            mPopData.getS_name(),
                            mPopData.getStep(),
                            mPopData.getS_datatype(),
                            true);
                } else if ("detail_info".equals(mPopType)) {
                    ((OSGIServiceHost) mActivity).jumptoDetail(mPopData.getmPackageName(),
                            mPopData.getmName(), mPopData.getmIconUrl(), 0, null, true);
                }
            }
        }
    };

    public void onDestroyView() {
        super.onDestroyView();
        LogUtils.i(TAG, "onDestroyView");
        mViewPager = null;
        mPagerSlidingTabStrip = null;
        stopConvenientSearch();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem item = menu.findItem(R.id.action_search);
        if (null != item) {
            item.setVisible(false);
        }
        inflater.inflate(R.menu.menu_main_homepage, menu);
        MenuItem item1 = menu.findItem(R.id.action_dm);
        if (null != item1) {
            item1.setVisible(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (null == mCategory) {
                ((OSGIServiceHost) mActivity).jumptoPersonal(true);
                return true;
            }
        } else if (item.getItemId() == R.id.action_search) {
            ((OSGIServiceHost) mActivity).jumptoSearch(null, true, mInfo, null, null);
            return true;
        } else if (item.getItemId() == R.id.action_dm) {
            ((OSGIServiceHost) mActivity).jumptoDownloadManager(true);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.action_personal) {
            ((OSGIServiceHost) mActivity).jumptoPersonal(true);
        } else if (v.getId() == R.id.action_search) {
            ((OSGIServiceHost) mActivity).jumptoSearch(null, true, mInfo, null, null);
        } else if (v.getId() == R.id.action_dm) {
            ((OSGIServiceHost) mActivity).jumptoDownloadManager(true);
        }
    }

    private String mInfo;
    private ViewGroup customView;
    private RelativeLayout mSearchbarView;
    private RelativeLayout mHideSearchbarView;
    private EditText mEtView;
    private ImageView mSearchView;
    private ImageView mSearchView1;
    private TextView mSubTitle;
    //    private String mEtViewText;
    private String[] mHint;
    private String[] mHint_PackageName;
    private String[] mHint_Name;
    private String[] mHint_IconUrl;

    private int HINT_UPDATE_TIME = 3000;
    private int HINT_SHOW_NUMBER = 0;
    private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {

        @Override
        public void run() {
            if (null != mHint && mHint.length != 0) {
                mEtView.setHint(mHint[HINT_SHOW_NUMBER]);
                mHandler.postDelayed(this, HINT_UPDATE_TIME);
                if (HINT_SHOW_NUMBER < mHint.length - 1) {
                    HINT_SHOW_NUMBER = HINT_SHOW_NUMBER + 1;
                } else {
                    HINT_SHOW_NUMBER = 0;
                }
            }
        }
    };

    private void startConvenientSearch() {
        LogUtils.i(TAG, "startConvenientSearch");
        mHandler.postDelayed(mRunnable, 0);
    }

    private void stopConvenientSearch() {
        LogUtils.i(TAG, "stopConvenientSearch");
        if (null != mRunnable)
            mHandler.removeCallbacks(mRunnable);
    }

    private void setSearchBar() {
        if (null == customView) {
            customView = (ViewGroup) mInflater.inflate(R.layout.actionbar_searchbar, null);
            mSearchbarView = (RelativeLayout) customView.findViewById(R.id.search_bar);

            mEtView = (EditText) customView.findViewById(R.id.search_et);
            mEtView.setFocusable(false);
            mEtView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View paramView) {
                    String mHintword = null;
                    if (!TextUtils.isEmpty(mEtView.getHint())) {
                        mHintword = mEtView.getHint().toString();
                        LogUtils.i(TAG, "mHintword = " + mHintword);
                    }
                    ((OSGIServiceHost) mActivity).jumptoSearch(null, true, mInfo, null, mHintword);
                }
            });
            mSearchView = (ImageView) customView.findViewById(R.id.search_icon);
            mSearchView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View paramView) {
                    String mKeyWord = null;
                    if (!TextUtils.isEmpty(mEtView.getHint())) {
                        mKeyWord = mEtView.getHint().toString();
                        LogUtils.i(TAG, "mKeyWord = " + mKeyWord);
                    }
                    int i = getHintNum(mKeyWord);
                    if (i != -1) {
                        ((OSGIServiceHost) mActivity).jumptoDetail(mHint_PackageName[i], mHint_Name[i], mHint_IconUrl[i], 0, null, true);
                    } else {
                        ((OSGIServiceHost) mActivity).jumptoSearch(null, true, mInfo, mKeyWord, null);
                    }
                }
            });

//            mSearchbarView.setVisibility(View.VISIBLE);

            mHideSearchbarView = (RelativeLayout) customView.findViewById(R.id.hide_search_bar);
            mSubTitle = (TextView) customView.findViewById(R.id.game_title);
            mSearchView1 = (ImageView) customView.findViewById(R.id.search_icon1);
            mSearchView1.setOnClickListener(new View.OnClickListener() {
                public void onClick(View paramView) {
                    ((OSGIServiceHost) mActivity).jumptoSearch(null, true, mInfo, null, null);
                }
            });
        }
    }

    private String actionbartitle;
    private Boolean removetab = false;

    private void refreshActionbar() {
        if (null != customView) {
            if (null != actionbartitle) {
                mSubTitle.setText(actionbartitle);
                mSearchbarView.setVisibility(View.GONE);
                mHideSearchbarView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void removeActionTabbar() {
        if (null != customView) {
            if (null != actionbartitle) {
                mSubTitle.setText(actionbartitle);
                mSearchbarView.setVisibility(View.GONE);
                mHideSearchbarView.setVisibility(View.VISIBLE);
            }
            removetab = true;
        }
    }

    private int getHintNum(String str) {
        int i;
        if (null == str) {
            return -1;
        }
        for (i = 0; i < mHint.length; i++) {
            if (mHint_Name[i].equals(str)) {
                return i;
            }
        }
        return -1;
    }

    private void initActionBar() {
        try {
            LogUtils.i(TAG, "initActionBar");

            ActionBar actionBar = ((ActionBarActivity) mActivity).getSupportActionBar();

            if (isHidden()) {
                return;
            }

            setSearchBar();

            if (null == mCategory) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setHomeAsUpIndicator(mActivity.getResources().getDrawable(R.drawable.icon_personal_light));
                actionBar.setDisplayShowTitleEnabled(false);
            } else {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setHomeAsUpIndicator(mActivity.getResources().getDrawable(R.drawable.action_bar_back_light));
                actionBar.setHomeButtonEnabled(true);
                actionBar.setDisplayShowTitleEnabled(false);
//                actionBar.setTitle(mTitle);
            }
            actionBar.setDisplayShowCustomEnabled(true);
            //actionBar.setCustomView(mPagerSlidingTabStrip);
            actionBar.setCustomView(customView);
            if (removetab) {
                removetab = false;
                actionBar.removeAllTabs();
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                actionBar.show();
            } else {
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
                actionBar.removeAllTabs();

                if (mPageData != null && mPageData.size() > 0) {
                    for (int i = 0; i < mPageData.size(); i++) {
//                    LogUtils.i(TAG, "actionBar.addTab getPageTitle(i) : " + mSectionsPagerAdapter.getPageTitle(i));
                        actionBar.addTab(actionBar.newTab().setTabListener(mBarTabListener));
                        ActionBar.Tab t = actionBar.getTabAt(i);
                        t.setCustomView(R.layout.actionbar_tab);
                        TextView title = (TextView) t.getCustomView().findViewById(R.id.tab_title);
                        title.setText(mSectionsPagerAdapter.getPageTitle(i));
                    }
                }

                actionBar.show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int mTabSelect;
    private final ActionBar.TabListener mBarTabListener = new ActionBar.TabListener() {

        @Override
        public void onTabReselected(ActionBar.Tab arg0, FragmentTransaction arg1) {
        }

        @Override
        public void onTabSelected(ActionBar.Tab arg0, FragmentTransaction arg1) {
            if (mViewPager != null)
                mViewPager.setCurrentItem(arg0.getPosition());
        }

        @Override
        public void onTabUnselected(ActionBar.Tab arg0, FragmentTransaction arg1) {
            mTabSelect = arg0.getPosition();
        }
    };

    private void postSearchHint() {
        LogUtils.i(TAG, "postSearchHint");
//        RequestParams params = new RequestParams();
//        params.addBodyParameter("appkey", AppliteUtils.getMitMetaDataValue(mActivity, Constant.META_DATA_MIT));
//        params.addBodyParameter("packagename", mActivity.getPackageName());
//        params.addBodyParameter("type", "hot_word");
//        params.addBodyParameter("protocol_version", Constant.PROTOCOL_VERSION);
//        mHttpUtils.send(HttpRequest.HttpMethod.POST, Constant.URL, params, new RequestCallBack<String>() {
//            @Override
//            public void onSuccess(ResponseInfo<String> responseInfo) {
//                mInfo = responseInfo.result;
//                setSearchData(responseInfo.result);
//                startConvenientSearch();
//            }
//
//            @Override
//            public void onFailure(HttpException e, String s) {
//                LogUtils.e(TAG, "快捷搜索热词请求失败:" + s);
//            }
//        });

        com.mit.afinal.http.AjaxParams params = new com.mit.afinal.http.AjaxParams();
        params.put("appkey", AppliteUtils.getMitMetaDataValue(mActivity, Constant.META_DATA_MIT));
        params.put("packagename", mActivity.getPackageName());
        params.put("type", "hot_word");
        params.put("protocol_version", Constant.PROTOCOL_VERSION);
        com.mit.afinal.FinalHttp mFinalHttp = new com.mit.afinal.FinalHttp();
        mFinalHttp.post(Constant.URL, params, new com.mit.afinal.http.AjaxCallBack<String>() {
            @Override
            public void onSuccess(String responseInfo) {
                mInfo = responseInfo;
                setSearchData(responseInfo);
                startConvenientSearch();
            }

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                LogUtils.e(TAG, "快捷搜索热词请求失败:" + strMsg);
            }
        });

    }

    private void setSearchData(String result) {
        try {
            JSONObject object = new JSONObject(result);
            int app_key = object.getInt("app_key");

            //LogUtils.i(TAG, "result:" + result);
            String hint_info = object.getString("searchscroll_info");
//            LogUtils.i(TAG, "hint_info:" + hint_info);

            JSONArray hint_json = new JSONArray(hint_info);
            mHint = new String[hint_json.length()];
            mHint_PackageName = new String[hint_json.length()];
            mHint_Name = new String[hint_json.length()];
            mHint_IconUrl = new String[hint_json.length()];
            for (int i = 0; i < hint_json.length(); i++) {
                JSONObject hint_obj = new JSONObject(hint_json.get(i).toString());
                String hint = hint_obj.getString("searchscroll");
                String packagename = hint_obj.getString("packageName");
                String name = hint_obj.getString("name");
                String iconurl = hint_obj.getString("iconUrl");
                mHint[i] = hint;
                mHint_PackageName[i] = packagename;
                mHint_Name[i] = name;
                mHint_IconUrl[i] = iconurl;
//                LogUtils.e(TAG, "mHint_PackageName[" + i + "]:" + mHint_PackageName[i] + "  mHint_Name[" + i + "]:" + mHint_Name[i] + "  mHint_IconUrl[" + i + "]:" + mHint_IconUrl[i]);
            }
            mEtView.setHint(mHint[0]);
        } catch (JSONException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "搜索预加载JSON解析失败");
        }
    }

    /**
     * HomePage网络请求
     */
    private void httpRequest() {
        LogUtils.i(TAG, "httpRequest");
        AjaxParams params = new AjaxParams();
        params.put("appkey", AppliteUtils.getMitMetaDataValue(mActivity, Constant.META_DATA_MIT));
        params.put("packagename", mActivity.getPackageName());
        params.put("app", "applite");
        params.put("protocol_version", Constant.PROTOCOL_VERSION);
        if (null == mCategory) {
            params.put("type", "homepage");
            params.put("tabTitle", "tabtitle");
        } else {
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
//                    LogUtils.i(TAG, "获取首页数据:" + data);
                    if (1 == data.getAppKey()) {
                        mPageData = data.getSubjectData();
//                        LogUtils.i(TAG, "获取首页数据  mPageData: " + mPageData);
                        if (!mPageData.get(0).getS_key().equals("goods")) {
                            homeflag = false;
                            LogUtils.i(TAG, "首页分类  goods homeflag = " + homeflag);
                        }

                        String S_key = mPageData.get(0).getS_key();
                        if (S_key.equals("goods")) {
                            //首页
                            mSearchbarView.setVisibility(View.VISIBLE);
                        } else {
                            //一级分类列表判断
                            if (mPageData.get(0).getS_key().equals("goods_m_game")) {
                                //游戏类
                                actionbartitle = getString(R.string.gametitle);
                                refreshActionbar();
                            } else {
                                actionbartitle = mPageData.get(0).getData().get(0).getCategorysub();
                                S_key = S_key.substring(0, 5);
                                if (S_key.equals("goods")) {
                                    //软件类
                                    refreshActionbar();
                                } else {
                                    //游戏分类
                                    removeActionTabbar();
                                }
                            }
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (LoadingAnimation != null) {
                    mLoadingView.startAnimation(LoadingAnimation);
                }
                initActionBar();
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

    class SectionsPagerAdapter extends FragmentPagerAdapter {
        private static final String TAG = "homepage_adapter";
        private int mChildCount = 0;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            Fragment f = (Fragment) object;
            if (f.isAdded()) {
                LogUtils.d(TAG, "destroyItem," + position);
                super.destroyItem(container, position, object);
            }
        }

        @Override
        public Fragment getItem(int position) {
            LogUtils.d(TAG, "getItem," + position);
            OSGIServiceHost host = (OSGIServiceHost) mActivity;
            Fragment fg = host.newFragment(Constant.OSGI_SERVICE_MAIN_FRAGMENT,
                    HomePageListFragment.class.getName(),
                    HomePageListFragment.newBundle(mPageData.get(position), position, false));
            return fg;
        }

        @Override
        public int getCount() {
            return (null != mPageData) ? mPageData.size() : 0;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            LogUtils.d(TAG, "getPageTitle :" + position);
            if (null != mPageData)
                return mPageData.get(position).getS_name();
            else
                return "";
        }

        @Override
        public void notifyDataSetChanged() {
            LogUtils.d(TAG, "notifyDataSetChanged");
            mChildCount = getCount();
            super.notifyDataSetChanged();
        }

        @Override
        public int getItemPosition(Object object) {
            LogUtils.d(TAG, "getItemPosition :" + object);
            if (mChildCount > 0) {
                mChildCount--;
                return POSITION_NONE;
            }
            return super.getItemPosition(object);
        }
    }
}

