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
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;
import com.applite.bean.HomePageBean;
import com.applite.bean.HomePageTypeBean;
import com.applite.common.AppliteUtils;
import com.applite.common.Constant;
import com.applite.common.LogUtils;
import com.applite.data.ListArrayAdapter;
import com.applite.utils.SPUtils;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplListener;
import com.mit.impl.ImplStatusTag;

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
public class HomePageListFragment extends ListFragment implements OnTouchListener,OnScrollListener {
    private final String TAG = "homepage_ListFragment";
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private ListArrayAdapter mListAdapter = null;
    private Activity mActivity;
    private List<HomePageBean> mData;
    List<HomePageTypeBean> mDataType;
    private int mTable = 0;
    float x, y, upx, upy;
    boolean mDualPane;
    private PullDownView pullDownView; //PullDown
    private ScrollOverListView listView;
    private FinalHttp mFinalHttp;
    int mCurCheckPosition = 0;
    private List<HomePageBean> mHomePageData = new ArrayList<HomePageBean>();
    private List<HomePageBean> mHomePageOrder = new ArrayList<HomePageBean>();
    private List<HomePageTypeBean> mHomePageMainType = new ArrayList<HomePageTypeBean>();
    private int mPageDood = 0;
    private int mPageOder = 0;
    private int mPageMainType = 0;
    private ImplListener mImplListener = new HomePageImplListener();
    private HomePageListListener mListAdapterListener = new HomePageListListener();

    public HomePageListFragment(List<HomePageBean> data, List<HomePageTypeBean> mDataType, int mTable, Activity activity) {
        this.mData = data;
        this.mTable = mTable;
        this.mDataType = mDataType;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.i(TAG, "ListFragment.onCreate() ");
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
            HomePageBean bean = mData.get(position);
            try {
                BundleContext bundleContext = BundleContextFactory.getInstance().getBundleContext();
                OSGIServiceAgent<ApkplugOSGIService> agent = new OSGIServiceAgent<ApkplugOSGIService>(
                        bundleContext, ApkplugOSGIService.class,
                        "(serviceName="+ Constant.OSGI_SERVICE_HOST_OPT+")", //服务查询条件
                        OSGIServiceAgent.real_time);   //每次都重新查询
                agent.getService().ApkplugOSGIService(bundleContext,
                        Constant.OSGI_SERVICE_DM_FRAGMENT,
                        0, Constant.OSGI_SERVICE_DETAIL_FRAGMENT,bean.getPackagename(),bean.getName(),bean.getImgurl());
            } catch (Exception e) {
                // TODO 自动生成的 catch 块
                e.printStackTrace();
            }
        }
        LogUtils.i(TAG, "onListItemClick()  l : " + l + " ; v : " + v +
                " ; id : " + id + " ; position : " + position);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        LogUtils.i(TAG, "onAttach ");
        mListAdapter = new ListArrayAdapter(mActivity,
                R.layout.fragment_list,
                mData,
                mDataType,
                mTable,
                mListAdapterListener);
        setListAdapter(mListAdapter);
        ImplAgent.registerImplListener(mImplListener);
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
        //outState.putAll();
    }
    public List<HomePageBean> getData() {
        return null;
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        LogUtils.i(TAG, "ListFragment.onCreateView() ");
        View rootView = mInflater.inflate(R.layout.fragment_tabl, container, false);
        //
        pullDownView = (PullDownView)rootView.findViewById(R.id.pullDownView);
        LogUtils.i(TAG, "ListFragment.onCreateView()  pullDownView : " + pullDownView);
        LogUtils.i(TAG, "ListFragment.onCreateView()  Thread.currentThread().getId() : " +
                Thread.currentThread().getId());
        if(null != pullDownView) {
            pullDownView.enableAutoFetchMore(true, 0);
            listView = pullDownView.getListView();

            pullDownView.setOnPullDownListener(new PullDownView.OnPullDownListener() {

                @Override
                public void onRefresh() {//刷新
                    getNewData(new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            //arrays.add(0, (String) msg.obj);
                            System.out.println("刷新" + (String) msg.obj);
                            //adapter.notifyDataSetChanged();
                            pullDownView.notifyDidRefresh(true);
                        }
                    });
                }

                @Override
                public void onLoadMore() {//加载更多
                    getNewData(new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            LogUtils.i(TAG, "ListFragment.onLoadMore()  Thread.currentThread().getId() : " +
                                    Thread.currentThread().getId() + " ; mHomePageData : " + mHomePageData);
                            if(null != mHomePageData) {
                                mPageDood = mListAdapter.setData(mHomePageData, mHomePageMainType, mTable);
                            }
                            mListAdapter.notifyDataSetChanged();
                            pullDownView.notifyDidLoadMore(((mHomePageData.size()) != 0 && (mHomePageData.size()==10)) ? false : true);
                            mHomePageData.clear();
                            mHomePageMainType.clear();
                            //System.out.println("加载更多");
                        }
                    });
                }
            });
            pullDownView.notifyDidDataLoad(false);
        }
        mFinalHttp = new FinalHttp();
        //TextView tv = (TextView)rootView.findViewById(R.id.section_label);
        //String text = "" + tv.getText()+getArguments().getInt(ARG_SECTION_NUMBER);
        //tv.setText(text);

        return rootView;
    }
    private void getNewData(final Handler mHandler) {
        new Thread(new Runnable() {//刷新
            @Override
            public void run() {
                try {
                    switch (mTable) {
                        case 0 : listPost("goods", mListAdapter.getCount()/10);
                                 break;
                        case 1 : listPost("order", mListAdapter.getCount()/10);
                                 break;
                        case 2 : listPost("maintype", mListAdapter.getCount()/10);
                                 break;
                    }
                    //listPost("order",1,mHandler);
                    Thread.sleep(3000);
                } catch (Exception e) {
                    Thread.interrupted();
                    e.printStackTrace();
                }

                LogUtils.i(TAG, "ListFragment.getNewString()  Thread.currentThread().getId() : " +
                        Thread.currentThread().getId());
                mHandler.obtainMessage().sendToTarget();
            }
        }).start();
    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            x = event.getX();
            y = event.getY();
            LogUtils.v(TAG, " is on touch down x = " + x + " ,y = " + y);
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            upx = event.getX();
            upy = event.getY();
            int position1 = ((ListView) v).pointToPosition((int) x, (int) y);
            int position2 = ((ListView) v).pointToPosition((int) upx, (int) upy);

            LogUtils.v(TAG, " is on touch x = " + x + " ,y = " + y);
            LogUtils.v(TAG, " is on touch upx = " + upx + " ,upy = " + upy);

            LogUtils.v(TAG, " is on touch positon1 = " + position1 + " ,position2 = " + position2);

            if (position1 == position2 && Math.abs(x - upx) > 10) {
                View view = ((ListView) v).getChildAt(position1);
                //removeListItem(view, position1);
            }
        }

        return false;
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
            String mData =null;
            switch (mType){
                case "goods" : mData = obj.getString("goods_data");
                    break;
                case "order" : mData = obj.getString("order_data");
                    break;
                case "maintype" : mData =obj.getString("maintype_data");
                      setDateMainType(obj);
            }

            JSONArray mJson = new JSONArray(mData);
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
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        LogUtils.v(TAG, "onScrollStateChanged  view = " + view + " ,scrollState = " + scrollState);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        LogUtils.v(TAG, "onScrollStateChanged  view = " + view + " ,totalItemCount = " + totalItemCount);
    }

    private HomePageBean findBeanByKey(String key){
        HomePageBean bean = null;
        for (int i = 0;i<mData.size();i++){
            if (key.equals(mData.get(i).getPackagename())){
                bean = mData.get(i);
                break;
            }
        }
        return bean;
    }

    class HomePageListListener implements ListArrayAdapter.ListAdapterListener {
        @Override
        public void onDownloadButtonClicked(ImplStatusTag tag) {
            HomePageBean bean = findBeanByKey(tag.getKey());
            if (null == bean){
                return;
            }
            switch(tag.getAction()){
                case ImplStatusTag.ACTION_DOWNLOAD:
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
            HomePageBean bean = findBeanByKey(downloadCompleteRsp.key);
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
            HomePageBean bean = findBeanByKey(downloadUpdateRsp.key);
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
            HomePageBean bean = findBeanByKey(packageAddedRsp.key);
            if (null != bean){
                bean.setStatus(Constant.STATUS_INSTALLED);
                LogUtils.i(TAG,  "onPackageAdded name="+bean.getName()+",status="+bean.getStatus());
                mActivity.runOnUiThread(mRefreshListRunnable);
            }
        }

        @Override
        public void onPackageRemoved(boolean b, ImplAgent.PackageRemovedRsp packageRemovedRsp) {
            HomePageBean bean = findBeanByKey(packageRemovedRsp.key);
            if (null != bean){
                bean.setStatus(Constant.STATUS_INIT);
                LogUtils.i(TAG,  "onPackageRemoved key="+packageRemovedRsp.key+",name="+bean.getName()+",status="+bean.getStatus());
                mActivity.runOnUiThread(mRefreshListRunnable);
            }
        }

        @Override
        public void onPackageChanged(boolean b, ImplAgent.PackageChangedRsp packageChangedRsp) {
            HomePageBean bean = findBeanByKey(packageChangedRsp.key);
            if (null != bean){
                bean.setStatus(Constant.STATUS_INSTALLED);
                LogUtils.i(TAG,  "onPackageChanged key="+packageChangedRsp.key+",name="+bean.getName()+",status="+bean.getStatus());
                mActivity.runOnUiThread(mRefreshListRunnable);
            }
        }

        @Override
        public void onSystemInstallResult(boolean b, ImplAgent.SystemInstallResultRsp systemInstallResultRsp) {
            LogUtils.i(TAG,  "onSystemInstallResult key="+systemInstallResultRsp.key);
            HomePageBean bean = findBeanByKey(systemInstallResultRsp.key);
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
            HomePageBean bean = findBeanByKey(systemDeleteResultRsp.key);
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

