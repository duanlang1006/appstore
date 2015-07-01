package com.applite.homepage;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

import com.applite.bean.HomePageApkData;
import com.applite.bean.HomePageBean;
import com.applite.bean.HomePageDataBean;
import com.applite.bean.HomePageTopic;
import com.applite.bean.HomePageTypeBean;
import com.applite.bean.SubjectData;
import com.applite.common.AppliteUtils;
import com.applite.common.Constant;
import com.applite.common.LogUtils;
import com.applite.data.ListArrayAdapter;
import com.applite.utils.SPUtils;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplListener;
import com.mit.impl.ImplStatusTag;

import net.tsz.afinal.FinalBitmap;
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
* Created by hxd on 15-6-9.
*/
public class HomePageListFragment extends ListFragment {
    private final static String TAG = "homepage_ListFragment";
    private final static int MSG_LOAD_DATA = 0;

    private Activity mActivity;
    private Context mContext;
    private SubjectData mData;

    private int mTable = 0;
    private View mListFooterView;
    private ListArrayAdapter mListAdapter = null;
    private FinalHttp mFinalHttp;
    int mCurCheckPosition = 0;
    private List<HomePageBean> mHomePageData = new ArrayList<HomePageBean>();
    private List<HomePageTypeBean> mHomePageMainType = new ArrayList<HomePageTypeBean>();
    private int mPageGood = 0;
    private int mPageOder = 0;
    private int mPageMainType = 0;
    private ImplListener mImplListener = new HomePageImplListener();
    private HomePageListListener mListAdapterListener = new HomePageListListener();
    private SlideShowView.OnSlideViewClickListener mSlideViewListener = new SlideShowView.OnSlideViewClickListener(){
        @Override
        public void onClick(View v, int position) {
            LogUtils.d(TAG,"OnSlideViewClickListener,"+position);
        }
    };
    private AbsListView.OnScrollListener mOnScrollListener = new AbsListView.OnScrollListener() {
        private boolean isLastRow = false;
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (isLastRow && scrollState == this.SCROLL_STATE_IDLE) {
                LogUtils.i(TAG, "拉到最底部");
                mListFooterView.setVisibility(view.VISIBLE);
//                mSearchPostPage = mSearchPostPage + 1;
                mHandler.sendEmptyMessage(MSG_LOAD_DATA);
                isLastRow = false;
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                             int totalItemCount) {
            //判断是否滚到最后一行
            if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount > 0) {
                LogUtils.i(TAG, "滚到最后一行");
                isLastRow = true;
            }
        }
    };
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_LOAD_DATA:
                    switch (mTable) {
                        case 0 :
                            listPost("goods", mData.getHomePageApkData().size()/10);
                            break;
                        case 1 :
                            listPost("order", mData.getHomePageApkData().size()/10);
                            break;
                        case 2 :
                            listPost("maintype", mData.getHomePageApkData().size()/10);
                            break;
                    }
                    LogUtils.i(TAG, "加载更多数据");
                    break;
                default:
                    break;
            }
        }

        ;
    };
    public HomePageListFragment(SubjectData data) {
        this.mData = data;
        this.mTable = mTable;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.i(TAG, "ListFragment.onCreate() ");
    }

    @Override
    public void onAttach(Activity activity) {
        LogUtils.i(TAG, "onAttach ");
        super.onAttach(activity);
        mActivity = activity;
        ImplAgent.registerImplListener(mImplListener);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        LogUtils.i(TAG, "ListFragment.onViewCreated() ");
        super.onViewCreated(view, savedInstanceState);
        Context context = mActivity;
        LayoutInflater mInflater = LayoutInflater.from(context);
        try {
            context = BundleContextFactory.getInstance().getBundleContext().getBundleContext();
            if (null != context) {
                mInflater = LayoutInflater.from(context);
                mInflater = mInflater.cloneInContext(context);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        SlideShowView slideView = new SlideShowView(context);
        getListView().addHeaderView(slideView);
        slideView.setOnViewClickListener(mSlideViewListener);
        slideView.setImageUrls(
                new String[]{"http://192.168.1.104/test_pic/q1.jpg",
                "http://192.168.1.104/test_pic/q2.jpg",
                        "http://192.168.1.104/test_pic/q1.jpg",
                        "http://192.168.1.104/test_pic/q2.jpg"});
        mListFooterView = mInflater.inflate(R.layout.load, null);
        getListView().addFooterView(mListFooterView);

        getListView().setOnScrollListener(mOnScrollListener);
        mListAdapter = new ListArrayAdapter(mActivity,
                R.layout.fragment_list,
                mData,
                mListAdapterListener);
        setListAdapter(mListAdapter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LogUtils.i(TAG, "onActivityCreated ");
        //PullToRefreshListView listView = new PullToRefreshListView(getActivity());

        if (savedInstanceState != null) {
            // Restore last state for checked position.
            mCurCheckPosition = savedInstanceState.getInt("curChoice", 0);
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        LogUtils.i(TAG, "onSaveInstanceState ");
        outState.putInt("curChoice", mCurCheckPosition);
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtils.i(TAG, "onResume ");
    }

    @Override
    public void onStart() {
        LogUtils.i(TAG, "onStart ");
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        LogUtils.i(TAG, "onStop");
    }
    @Override
    public void onDetach(){
        super.onDetach();
        LogUtils.i(TAG, "onDetach ");
        ImplAgent.unregisterImplListener(mImplListener);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        //ImplAgent.unregisterImplListener(mImplListener);
        LogUtils.i(TAG, "onDestroy");
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        LogUtils.i(TAG, "onListItemClick, "+position);
        super.onListItemClick(l, v, position, id);
        if (2 == this.mTable) {
            //homePageFragment.postMainType(mActivity);
            FragmentManager fm;
            FragmentTransaction ftx;
            HomePageFragment hp = new HomePageFragment();
            hp.setMainType(true);
            fm = ((FragmentActivity)mActivity).getSupportFragmentManager();
            ftx = fm.beginTransaction();
            ftx.replace(hp.getNode(), hp);
            ftx.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ftx.commit();
            LogUtils.i(TAG, "onListItemClick.onCreate() ");
        }else{
            HomePageApkData bean = mData.getHomePageApkData().get(position);
            try {
                BundleContext bundleContext = BundleContextFactory.getInstance().getBundleContext();
                OSGIServiceAgent<ApkplugOSGIService> agent = new OSGIServiceAgent<ApkplugOSGIService>(
                        bundleContext, ApkplugOSGIService.class,
                        "(serviceName="+ Constant.OSGI_SERVICE_HOST_OPT+")", //服务查询条件
                        OSGIServiceAgent.real_time);   //每次都重新查询
                agent.getService().ApkplugOSGIService(bundleContext,
                        Constant.OSGI_SERVICE_DM_FRAGMENT,
                        0, Constant.OSGI_SERVICE_DETAIL_FRAGMENT,bean.getPackageName(),bean.getName(),bean.getIconUrl());
            } catch (Exception e) {
                // T
                e.printStackTrace();
            }
        }
        LogUtils.i(TAG, "onListItemClick()  l : " + l + " ; v : " + v +
                " ; id : " + id + " ; position : " + position);
    }


    /**
     * 上拉加载网络请求
     */
    public void listPost(final String mTabType ,final int mPage) {
        //Message msg = new Message();
        if(null == mFinalHttp) {
            mFinalHttp = new FinalHttp();
        }
        LogUtils.e(TAG, "listPost  mPage : " + mPage);
        AjaxParams params = new AjaxParams();
        params.put("appkey", AppliteUtils.getMitMetaDataValue(mActivity, Constant.META_DATA_MIT));
        params.put("packagename", "com.android.applite1.0");
        //params.put("packagename",Utils.getPackgeName(this));
        params.put("app", "applite");
        params.put("type", "hptab");
        params.put("page", String.valueOf(mPage));
        params.put("tabtype", mTabType);
        params.put("pullonloading", "pullonloading");
        mFinalHttp.post(Constant.URL, params, new AjaxCallBack<Object>() {
            @Override
            public void onSuccess(Object o) {
                super.onSuccess(o);
                Message msg = new Message();
                String reuslt = (String) o;
                LogUtils.i(TAG, "HomePage网络请求成功， reuslt:" + reuslt);
                setData(reuslt, mActivity, mTabType);

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
    public void setData(final String data, final Activity mActivity, final String mType) {
        HomePageBean hpBeanData = null;
        try {
            JSONObject obj = new JSONObject(data);
            LogUtils.i(TAG, "setData JSONObject data， obj : " + obj);
            int app_key = obj.getInt("app_key");
            //goods_data
            String dataStr =null;
            switch (mType){
                case "goods" : dataStr = obj.getString("goods_data");
                    break;
                case "order" : dataStr = obj.getString("order_data");
                    break;
                case "maintype" : dataStr =obj.getString("maintype_data");
                      setDateMainType(obj);
            }

            JSONArray mJson = new JSONArray(dataStr);
            //LogUtils.i(TAG, "setData JSONObject data，json : " + json);
            LogUtils.i( TAG," mJson.length() : " + mJson.length());
            LogUtils.i( TAG," mJson : " + mJson);
            for (int i = 0; i < mJson.length(); i++) {
                JSONObject object = new JSONObject(mJson.get(i).toString());
                hpBeanData = new HomePageBean();
                hpBeanData.setId(1 + (Integer) SPUtils.get(mActivity, SPUtils.HOMEPAGE_POSITION, 0));
                hpBeanData.setPackagename(object.getString("packageName"));
                hpBeanData.setName(object.getString("name"));
                hpBeanData.setImgurl(object.getString("iconUrl"));
                hpBeanData.setUrl(object.getString("rDownloadUrl"));
                hpBeanData.setApkSize(object.getString("apkSize"));
                hpBeanData.setRating(object.getString("rating"));
                hpBeanData.setBrief(object.getString("brief"));
                hpBeanData.setBoxLabel(object.getString("boxLabel"));
                hpBeanData.setCategoryMain(object.getString("categorymain"));
                hpBeanData.setCategorySub(object.getString("categorysub"));
                hpBeanData.setDownloadTimes(object.getString("downloadTimes"));
                hpBeanData.setVersionName(object.getString("versionName"));
                try {
                    hpBeanData.setmVersionCode(object.getInt("versionCode"));
                }catch (Exception e){
                    hpBeanData.setmVersionCode(0);
                    e.printStackTrace();
                }
                mHomePageData.add(hpBeanData);
                SPUtils.put(mActivity, SPUtils.HOMEPAGE_POSITION,
                        (Integer) SPUtils.get(mActivity, SPUtils.HOMEPAGE_POSITION, 0) + 1);
            }
            LogUtils.i( TAG," mJson : " + mJson);
            if (null == mListAdapter){
                mListAdapter = new ListArrayAdapter(mActivity,
                        R.layout.fragment_list,
                        mData,
                        mListAdapterListener);
                getListView().setAdapter(mListAdapter);
            }else{
                mListAdapter.notifyDataSetChanged();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            LogUtils.e(TAG, " HomePageJSON解析异常");
        }
    }

    public void setDateMainType(JSONObject mObj) throws JSONException {

        HomePageTypeBean hpBeanMainType = null;
        //MainType_data

        String mMainType_data = mObj.getString("maintype_data");
        JSONArray mMainType_json = new JSONArray(mMainType_data);
        LogUtils.i(TAG, " mMainType_json.length() : " + mMainType_json.length());
        LogUtils.i(TAG, " mMainType_json : " + mMainType_json);
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
    }

    private HomePageApkData findBeanByKey(String key){
        HomePageApkData bean = null;
        for (int i = 0;i<mData.getHomePageApkData().size();i++){
            if (key.equals(mData.getHomePageApkData().get(i).getPackageName())){
                bean = mData.getHomePageApkData().get(i);
                break;
            }
        }
        return bean;
    }

    class HomePageListListener implements ListArrayAdapter.ListAdapterListener {
        @Override
        public void onDownloadButtonClicked(ImplStatusTag tag) {
            HomePageApkData bean = findBeanByKey(tag.getKey());
            if (null == bean){
                return;
            }
            switch(tag.getAction()){
                case ImplStatusTag.ACTION_DOWNLOAD:
                    ImplAgent.downloadPackage(mActivity,
                            bean.getPackageName(),
                            bean.getRDownloadUrl(),
                            Constant.extenStorageDirPath,
                            bean.getName() + ".apk",
                            3,
                            false,
                            bean.getName(),
                            "",
                            true,
                            bean.getIconUrl(),
                            "",
                            bean.getPackageName());
                    break;
                case ImplStatusTag.ACTION_OPEN:
                    try {
                        mActivity.startActivity(tag.getIntent());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    class HomePageImplListener implements ImplListener{
        private final String TAG = "impl_homepage";
        private Runnable mRefreshListRunnable = new Runnable(){
            @Override
            public void run() {
                mListAdapter.notifyDataSetInvalidated();
            }
        };

        @Override
        public void onDownloadComplete(boolean b, ImplAgent.DownloadCompleteRsp downloadCompleteRsp) {
            LogUtils.i(TAG,  "onDownloadComplete key="+downloadCompleteRsp.key);
            HomePageApkData bean = findBeanByKey(downloadCompleteRsp.key);
            if (null != bean){
                LogUtils.i(TAG,  "onDownloadComplete name="+bean.getName()+",status="+bean.getStatus());
                if (bean.getStatus() <= Constant.STATUS_FAILED) {
                    bean.setStatus(downloadCompleteRsp.status);
                }
                bean.setLocalUri(downloadCompleteRsp.localPath);
                mActivity.runOnUiThread(mRefreshListRunnable);
            }
            LogUtils.i(TAG,  "onDownloadComplete end");
        }

        @Override
        public void onDownloadUpdate(boolean b, ImplAgent.DownloadUpdateRsp downloadUpdateRsp) {
            LogUtils.i(TAG,  "onDownloadUpdate  key="+downloadUpdateRsp.key);
            HomePageApkData bean = findBeanByKey(downloadUpdateRsp.key);
            if (null != bean){
                LogUtils.i(TAG,  "onDownloadUpdate name="+bean.getName()+",status="+bean.getStatus());
                if (bean.getStatus() <= Constant.STATUS_FAILED) {
                    int OriginalStatus = bean.getStatus();
                    bean.setStatus(downloadUpdateRsp.status);
                    bean.setCurrentBytes(downloadUpdateRsp.progress);
                    bean.setTotalBytes(100);
                    if (OriginalStatus != bean.getStatus()) {
                        mActivity.runOnUiThread(mRefreshListRunnable);
                    }
                }
            }
        }

        @Override
        public void onPackageAdded(boolean b, ImplAgent.PackageAddedRsp packageAddedRsp) {
            LogUtils.i(TAG,  "onPackageAdded key="+packageAddedRsp.key);
            HomePageApkData bean = findBeanByKey(packageAddedRsp.key);
            if (null != bean){
                bean.setStatus(Constant.STATUS_INSTALLED);
                LogUtils.i(TAG,  "onPackageAdded name="+bean.getName()+",status="+bean.getStatus());
                mActivity.runOnUiThread(mRefreshListRunnable);
            }
        }

        @Override
        public void onPackageRemoved(boolean b, ImplAgent.PackageRemovedRsp packageRemovedRsp) {
            HomePageApkData bean = findBeanByKey(packageRemovedRsp.key);
            if (null != bean){
                bean.setStatus(Constant.STATUS_INIT);
                LogUtils.i(TAG,  "onPackageRemoved key="+packageRemovedRsp.key+",name="+bean.getName()+",status="+bean.getStatus());
                mActivity.runOnUiThread(mRefreshListRunnable);
            }
        }

        @Override
        public void onPackageChanged(boolean b, ImplAgent.PackageChangedRsp packageChangedRsp) {
            HomePageApkData bean = findBeanByKey(packageChangedRsp.key);
            if (null != bean){
                bean.setStatus(Constant.STATUS_INSTALLED);
                LogUtils.i(TAG,  "onPackageChanged key="+packageChangedRsp.key+",name="+bean.getName()+",status="+bean.getStatus());
                mActivity.runOnUiThread(mRefreshListRunnable);
            }
        }

        @Override
        public void onSystemInstallResult(boolean b, ImplAgent.SystemInstallResultRsp systemInstallResultRsp) {
            LogUtils.i(TAG,  "onSystemInstallResult key="+systemInstallResultRsp.key);
            HomePageApkData bean = findBeanByKey(systemInstallResultRsp.key);
            if (null != bean){
                if (systemInstallResultRsp.result == Constant.INSTALL_SUCCEEDED) {
                    bean.setStatus(Constant.STATUS_INSTALLED);
                }else{
                    bean.setStatus(Constant.STATUS_INSTALL_FAILED);
                }
                LogUtils.i(TAG,  "onSystemInstallResult name="+bean.getName()+",status="+bean.getStatus());
                mActivity.runOnUiThread(mRefreshListRunnable);
            }
        }

        @Override
        public void onSystemDeleteResult(boolean b, ImplAgent.SystemDeleteResultRsp systemDeleteResultRsp) {
            HomePageApkData bean = findBeanByKey(systemDeleteResultRsp.key);
            if (null != bean){
                if (systemDeleteResultRsp.result == Constant.DELETE_SUCCEEDED) {
                    bean.setStatus(Constant.STATUS_INIT);
                }
                LogUtils.i(TAG,  "onSystemDeleteResult key="+systemDeleteResultRsp.key+",name="+bean.getName()+",status="+bean.getStatus());
                mActivity.runOnUiThread(mRefreshListRunnable);
            }
        }

        @Override
        public void onFinish(boolean b, ImplAgent.ImplResponse implResponse) {
            LogUtils.i(TAG,  "onFinish implResponse.action="+implResponse.action);
        }
    }

}

