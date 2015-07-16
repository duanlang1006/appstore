package com.applite.homepage;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import com.applite.bean.HomePageApkData;
import com.applite.bean.HomePageDataBean;
import com.applite.bean.SpecialTopicData;
import com.applite.bean.SubjectData;
import com.applite.common.AppliteUtils;
import com.applite.common.Constant;
import com.applite.common.LogUtils;
import com.applite.data.ListArrayAdapter;
import com.google.gson.Gson;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplInfo;
import com.mit.impl.ImplListener;
import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;
import org.apkplug.Bundle.ApkplugOSGIService;
import org.apkplug.Bundle.OSGIServiceAgent;
import org.osgi.framework.BundleContext;

import java.util.ArrayList;
import java.util.List;

/**
* Created by hxd on 15-6-9.
*/
public class HomePageListFragment extends Fragment implements AbsListView.OnItemClickListener{
    private final static String TAG = "homepage_ListFragment";
    private final static int MSG_LOAD_DATA = 0;

    private FinalHttp mFinalHttp = new FinalHttp();
    private Gson gson = new Gson();

    private Activity mActivity;
    private SubjectData mData;

    private ListView mListView;
    private View mMoreView;
    private SlideShowView mTopicView;
    private ListArrayAdapter mListAdapter = null;
    private int mCurCheckPosition = 0;
    private boolean showHome = false;
    private ImplListener mImplListener = new HomePageImplListener();
    private MySlideViewListener mSlideViewListener = new MySlideViewListener();
    private MyScrollListener mOnScrollListener = new MyScrollListener();

    private boolean isend;
    public HomePageListFragment(SubjectData data) {
        this.mData = data;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.i(TAG, "ListFragment.onCreate() ");
        Bundle b = getArguments();
        if (null != b){
            showHome = true;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        LogUtils.i(TAG, "onAttach ");
        super.onAttach(activity);
        setHasOptionsMenu(true);
        mActivity = activity;
        ImplAgent.registerImplListener(mImplListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LogUtils.i(TAG, "ListFragment.onCreateView() ");
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
        View rootView = mInflater.inflate(R.layout.fragment_homepage_list, container, false);

        // Set the adapter
        mListView = (ListView) rootView.findViewById(android.R.id.list);

        if (null == mData || null == mData.getData() || mData.getData().size() == 0){
            httpRequest();
        }

        setTopicView(context);
        setMoreView(mInflater);

        mListView.setOnItemClickListener(this);
        mListView.setOnScrollListener(mOnScrollListener);
        mListAdapter = new ListArrayAdapter(mActivity,mData);
        mListView.setAdapter(mListAdapter);
        initActionBar();
        return rootView;
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                getFragmentManager().popBackStack();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ListArrayAdapter.ViewHolder viewHolder = (ListArrayAdapter.ViewHolder)view.getTag();
        if (null == viewHolder){
            return;
        }
        HomePageApkData itemData = viewHolder.getItemData();
        if (null == itemData){
            return;
        }
        if (viewHolder.getLayoutStr().equals("fragment_categorylist")) {
            try {
                BundleContext bundleContext = BundleContextFactory.getInstance().getBundleContext();
                OSGIServiceAgent<ApkplugOSGIService> agent = new OSGIServiceAgent<ApkplugOSGIService>(
                        bundleContext, ApkplugOSGIService.class,
                        "(serviceName="+ Constant.OSGI_SERVICE_HOST_OPT+")", //服务查询条件
                        OSGIServiceAgent.real_time);   //每次都重新查询
                agent.getService().ApkplugOSGIService(bundleContext,
                        Constant.OSGI_SERVICE_MAIN_FRAGMENT,
                        0, Constant.OSGI_SERVICE_MAIN_FRAGMENT,itemData.getKey(),itemData.getName());
            } catch (Exception e) {
                // TODO 自动生成的 catch 块
                e.printStackTrace();
            }
        }else if (viewHolder.getLayoutStr().equals("fragment_apklist")){
            try {
                BundleContext bundleContext = BundleContextFactory.getInstance().getBundleContext();
                OSGIServiceAgent<ApkplugOSGIService> agent = new OSGIServiceAgent<ApkplugOSGIService>(
                        bundleContext, ApkplugOSGIService.class,
                        "(serviceName="+ Constant.OSGI_SERVICE_HOST_OPT+")", //服务查询条件
                        OSGIServiceAgent.real_time);   //每次都重新查询
                agent.getService().ApkplugOSGIService(bundleContext,
                        Constant.OSGI_SERVICE_MAIN_FRAGMENT,
                        0, Constant.OSGI_SERVICE_DETAIL_FRAGMENT,
                        itemData.getPackageName(),
                        itemData.getName(),
                        itemData.getIconUrl(),
                        Constant.OSGI_SERVICE_MAIN_FRAGMENT);
            } catch (Exception e) {
                // T
                e.printStackTrace();
            }
        }
    }

    private void initActionBar(){
        try {
            if (showHome) {
                ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
                actionBar.setDisplayShowCustomEnabled(false);
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setTitle(mData.getS_name());
                actionBar.show();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void setTopicView(Context context){
        List<SpecialTopicData> topicList = (null == mData)?null:mData.getSpecialtopic_data();
        if (null != topicList && topicList.size()>0){
            String[] urls = new String[topicList.size()];
            for(int i = 0;i < topicList.size();i++){
                urls[i] = topicList.get(i).t_iconurl;
            }
            mTopicView = new SlideShowView(context);
            mTopicView.setImageUrls(urls);
            mTopicView.setOnViewClickListener(mSlideViewListener);
            mListView.addHeaderView(mTopicView);
        }
    }

    private void setMoreView(LayoutInflater inflater){
        if (null != mMoreView){
            mListView.removeFooterView(mMoreView);
        }
        mMoreView = inflater.inflate(R.layout.more, null);
        mListView.addFooterView(mMoreView);
    }

    private void httpRequest() {
        int page = 0;
        if (null != mData.getData()) {
            page = mData.getData().size() / mData.getStep();
            if (mData.getData().size()% mData.getStep() != 0){
                page ++;
            }
        }
        LogUtils.d(TAG, "httpRequest  mPage : " + page);
        AjaxParams params = new AjaxParams();
        params.put("appkey", AppliteUtils.getMitMetaDataValue(mActivity, Constant.META_DATA_MIT));
        params.put("packagename", "com.android.applite1.0");
        //params.put("packagename",Utils.getPackgeName(this));
        params.put("app", "applite");
        params.put("type", "hptab");
        params.put("page", String.valueOf(page));
        params.put("tabtype", mData.getS_key());
        mFinalHttp.post(Constant.URL, params, new AjaxCallBack<Object>() {
            @Override
            public void onSuccess(Object o) {
                super.onSuccess(o);
                LogUtils.i(TAG, "HomePageList网络请求成功，" + (String)o);
                HomePageDataBean pageData = gson.fromJson((String)o, HomePageDataBean.class);
                if (null != pageData && null != pageData.getSubjectData()){
                    for (int i = 0;i < pageData.getSubjectData().size(); i++) {
                        SubjectData subject = pageData.getSubjectData().get(i);
                        if (subject.getS_key().equals(mData.getS_key())){
                            mData.setS_datatype(subject.getS_datatype());
                            mData.setStep(subject.getStep());
                            if (null != subject.getData()) {
                                mData.getData().addAll(subject.getData());
                            }
                            break;
                        }
                    }
                }
                if(pageData.getSubjectData().get(0).getData().isEmpty()){
                    LogUtils.i(TAG, "咿呀咿呀哟");
                    isend = true;
                }else{
                    isend = false;
                }

                mListAdapter.notifyDataSetChanged();
                mMoreView.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                super.onFailure(t, errorNo, strMsg);
                LogUtils.e(TAG, "HomePage网络请求失败:" + strMsg);
                mMoreView.setVisibility(View.GONE);
            }
        });
    }

    private HomePageApkData findBeanByKey(String key){
        HomePageApkData bean = null;
        for (int i = 0;i<mData.getData().size();i++){
            if (key.equals(mData.getData().get(i).getPackageName())){
                bean = mData.getData().get(i);
                break;
            }
        }
        return bean;
    }

    class HomePageImplListener implements ImplListener{
        private final String TAG = "impl_homepage";
        private Runnable mRefreshListRunnable = new Runnable(){
            @Override
            public void run() {
                mListAdapter.notifyDataSetChanged();
            }
        };

        @Override
        public void onUpdate(boolean success, ImplInfo implInfo) {
            HomePageApkData bean = findBeanByKey(implInfo.getKey());
            if (null != bean){
                LogUtils.i(TAG,  HomePageListFragment.this+",onUpdate name="+bean.getName()+",status="+implInfo.getStatus());
                bean.setImplInfo(implInfo);
                mActivity.runOnUiThread(mRefreshListRunnable);
            }
        }
    }

    class MyScrollListener implements AbsListView.OnScrollListener{
        private boolean isLastRow = false;
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (isLastRow && scrollState == this.SCROLL_STATE_IDLE) {
                LogUtils.i(TAG, "拉到最底部");
                httpRequest();
                if(!isend){
                    mMoreView.setVisibility(view.VISIBLE);
                }
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
    }

    class MySlideViewListener implements SlideShowView.OnSlideViewClickListener{
        @Override
        public void onClick(View v, int position) {
            SpecialTopicData topicData = mData.getSpecialtopic_data().get(position);
            SubjectData data = new SubjectData();
            data.setS_key(topicData.t_key);
            data.setS_name(topicData.t_info);
            data.setStep(mData.getStep());
            data.setS_datatype(mData.getS_datatype());
            data.setData(new ArrayList<HomePageApkData>());
            data.setSpecialtopic_data(null);
            try {
                BundleContext bundleContext = BundleContextFactory.getInstance().getBundleContext();
                OSGIServiceAgent<ApkplugOSGIService> agent = new OSGIServiceAgent<ApkplugOSGIService>(
                        bundleContext, ApkplugOSGIService.class,
                        "(serviceName="+ Constant.OSGI_SERVICE_HOST_OPT+")", //服务查询条件
                        OSGIServiceAgent.real_time);   //每次都重新查询
                agent.getService().ApkplugOSGIService(bundleContext,
                        Constant.OSGI_SERVICE_DM_FRAGMENT,
                        0, Constant.OSGI_SERVICE_TOPIC_FRAGMENT,data);
            } catch (Exception e) {
                // T
                e.printStackTrace();
            }
        }
    }
}
