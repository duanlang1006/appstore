package com.applite.homepage;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.applite.bean.HomePageApkData;
import com.applite.bean.HomePageDataBean;
import com.applite.bean.SpecialTopicData;
import com.applite.bean.SubjectData;
import com.applite.common.AppliteUtils;
import com.applite.common.Constant;
import com.applite.common.LogUtils;
import com.applite.data.ListArrayAdapter;
import com.google.gson.Gson;
import com.mit.mitupdatesdk.MitMobclickAgent;
import com.osgi.extra.OSGIBaseFragment;
import com.osgi.extra.OSGIServiceHost;
import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import java.util.ArrayList;
import java.util.List;

/**
* Created by hxd on 15-6-9.
*/
public class HomePageListFragment extends OSGIBaseFragment implements AbsListView.OnItemClickListener{
    private final static String TAG = "homepage_ListFragment";
    private final static int MSG_LOAD_DATA = 0;

    private FinalHttp mFinalHttp = new FinalHttp();
    private Gson gson = new Gson();

    private SubjectData mData;
    private ListView mListView;
    private View mMoreView;
    private TextView mMoreTextView;
    private TextView mEndTextView;
    private View mEndView;
    private SlideShowView mTopicView;
    private ListArrayAdapter mListAdapter = null;
    private boolean showBack = false;
    private MySlideViewListener mSlideViewListener = new MySlideViewListener();
    private MyScrollListener mOnScrollListener = new MyScrollListener();

    private boolean isend;
    private boolean sendhttpreq = true;
    private String whichPage;
    private int mPosition;

    private static final String TOPICFRAGMENT = "TopicListFragment";
    private static final String HOMEPAGELISTFRAGMENT = "HomepageListFragment";

    public static Bundle newBundle(String s_key, String s_name, int step, String s_datatype){
        Bundle bundle = new Bundle();
        bundle.putString("key",s_key);
        bundle.putString("name", s_name);
        bundle.putInt("step", step);
        bundle.putString("datatype", s_datatype);
        return bundle;
    }

    public static Bundle newBundle(SubjectData data,int position,boolean showBack){
        Bundle b = new Bundle();
        b.putParcelable("subject_data", data);
        b.putBoolean("show_home", showBack);
        b.putInt("position", position);
        return b;
    }

    public HomePageListFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.i(TAG, "ListFragment.onCreate() ");
        if (showBack && null != mData.getS_key()) {
            whichPage = TOPICFRAGMENT+"_"+mData.getS_key();
            MitMobclickAgent.onEvent(mActivity, whichPage+"_onCreate");
        }else{
            whichPage = HOMEPAGELISTFRAGMENT + this.mPosition;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        LogUtils.i(TAG, "onAttach ");
        super.onAttach(activity);
        mActivity = activity;
        mInflater = LayoutInflater.from(mActivity);
        Bundle params = getArguments();
        if (null != params) {
            this.mData = params.getParcelable("subject_data");
            this.showBack = params.getBoolean("show_home");
            this.mPosition = params.getInt("position");
            if (null == mData){
                mData = new SubjectData();
                mData.setS_key(params.getString("key"));
                mData.setS_name(params.getString("name"));
                mData.setStep(params.getInt("step"));
                mData.setS_datatype(params.getString("datatype"));
                mData.setData(new ArrayList<HomePageApkData>());
                mData.setSpecialtopic_data(null);
                showBack = true;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LogUtils.i(TAG, "ListFragment.onCreateView() ");
        Context context = mActivity;
        View rootView = inflater.inflate(R.layout.fragment_homepage_list, container, false);

        // Set the adapter
        mListView = (ListView) rootView.findViewById(android.R.id.list);

        if (null == mData || null == mData.getData() || mData.getData().size() == 0){
            httpRequest();
        }

        setTopicView(context);
        setMoreView(inflater);

        mListView.setOnItemClickListener(this);
        mListView.setOnScrollListener(mOnScrollListener);
        mListAdapter = new ListArrayAdapter(mActivity,mData);
        mListView.setAdapter(mListAdapter);
        initActionBar();
        return rootView;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        LogUtils.i(TAG, "onItemClick");
        ListArrayAdapter.ViewHolder viewHolder = (ListArrayAdapter.ViewHolder)view.getTag();
        if (null == viewHolder){
            return;
        }
        HomePageApkData itemData = viewHolder.getItemData();
        if (null == itemData){
            return;
        }
        if (viewHolder.getLayoutStr().equals("fragment_categorylist")) {
            ((OSGIServiceHost)mActivity).jumptoHomepage(itemData.getKey(),itemData.getName(),true);
        }else if (viewHolder.getLayoutStr().equals("fragment_apklist")){
            ((OSGIServiceHost)mActivity).jumptoDetail(itemData.getPackageName(),itemData.getName(),itemData.getIconUrl(),true);
        }
        MitMobclickAgent.onEvent(mActivity, "onItemClick" + viewHolder.getItemPosition());
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        LogUtils.i(TAG, "HomePageListFragment onHiddenChanged  hidden = " + hidden);
//        if (!hidden){
//            initActionBar();
//            if (null != mTopicView){
//                mTopicView.startPlay();
//            }
//        }else{
//            if (null != mTopicView){
//                mTopicView.stopPlay();
//            }
//        }
    }

    public void play(boolean flag) {
        LogUtils.i(TAG, "play = " + flag + " mTopicView = " + mTopicView);
        if (flag){
            if (null != mTopicView){
                mTopicView.startPlay();
            }
        }else{
            if (null != mTopicView){
                mTopicView.stopPlay();
            }
        }
    }

    @Override
    public void onResume() {
        LogUtils.i(TAG, "onResume");
        super.onResume();
        if (null != whichPage && !TextUtils.isEmpty(whichPage)) {
            MitMobclickAgent.onPageStart(whichPage); //统计页面
        }
    }

    @Override
    public void onPause() {
        LogUtils.i(TAG, "onPause");
        super.onPause();
        if (null != whichPage && !TextUtils.isEmpty(whichPage)) {
            MitMobclickAgent.onPageEnd(whichPage);
        }
    }

    @Override
    public void onDestroy() {
        LogUtils.i(TAG, "onDestroy");
        super.onDestroy();
        if (showBack && null != mData.getS_key()) {
            MitMobclickAgent.onEvent(mActivity, whichPage + "_onDestroy");
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem item1 = menu.findItem(R.id.action_dm);
        if (null != item1) {
            item1.setVisible(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            ((OSGIServiceHost) mActivity).jumptoSearch(null, true, mInfo, null);
            return true;
        } else if (item.getItemId() == R.id.action_dm) {
            ((OSGIServiceHost) mActivity).jumptoDownloadManager(true);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String mInfo;
    private ViewGroup customView;
    private LayoutInflater mInflater;
    private EditText mEtView;
    private ImageView mSearchView;

    private void initActionBar(){
        LogUtils.i(TAG, "initActionBar");
        try {

            if(null == customView){
                customView = (ViewGroup) mInflater.inflate(R.layout.actionbar_searchbar, null);
                mEtView = (EditText) customView.findViewById(R.id.search_et);
                mEtView.setFocusable(false);
                mEtView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View paramView) {
                        ((OSGIServiceHost) mActivity).jumptoSearch(null, true, mInfo, null);
                    }
                });
                mSearchView = (ImageView) customView.findViewById(R.id.search_icon);
                mSearchView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View paramView) {
                        String mKeyWord = mEtView.getHint().toString();
                        ((OSGIServiceHost) mActivity).jumptoSearch(null, true, mInfo, mKeyWord);
                    }
                });
            }

            if (showBack) {
                ActionBar actionBar = ((ActionBarActivity) mActivity).getSupportActionBar();
                actionBar.setDisplayShowCustomEnabled(true);
                actionBar.setDisplayShowTitleEnabled(false);
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setCustomView(customView);
//                actionBar.setTitle(mData.getS_name());
                actionBar.removeAllTabs();
                if(!TextUtils.isEmpty(mData.getS_name())){
                    actionBar.addTab(actionBar.newTab().setTabListener(mBarTabListener));
                    ActionBar.Tab t = actionBar.getTabAt(0);
                    t.setCustomView(R.layout.actionbar_tab);
                    TextView title = (TextView)t.getCustomView().findViewById(R.id.tab_title);
                    title.setText(mData.getS_name());
                }
                actionBar.show();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private final ActionBar.TabListener mBarTabListener = new ActionBar.TabListener(){
        private final static String TAG = "homepage_ListFragment_mBarTabListener";

        @Override
        public void onTabReselected(ActionBar.Tab arg0, FragmentTransaction arg1) {
            LogUtils.i(TAG, "onTabReselected arg0.getPosition()= "+arg0.getPosition());
        }

        @Override
        public void onTabSelected(ActionBar.Tab arg0, FragmentTransaction arg1) {
            LogUtils.i(TAG, "onTabSelected arg0.getPosition()= "+arg0.getPosition());
        }

        @Override
        public void onTabUnselected(ActionBar.Tab arg0, FragmentTransaction arg1) {
            LogUtils.i(TAG, "onTabUnselected arg0.getPosition()= "+arg0.getPosition());
        }
    };

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
        mMoreTextView = (TextView) mMoreView.findViewById(R.id.loadmore_text);
        mEndTextView = (TextView) mMoreView.findViewById(R.id.loadend_text);
        mEndTextView.setVisibility(View.GONE);
        mListView.addFooterView(mMoreView);
    }

    private void removeMoreView(){
        if (null != mMoreView){
            mListView.removeFooterView(mMoreView);
        }
    }

    private void setEndView(LayoutInflater inflater){
        if (null != mEndView){
            mListView.removeFooterView(mEndView);
        }
        mEndView = inflater.inflate(R.layout.notify_end, null);
        mListView.addFooterView(mEndView);
        mListView.invalidate();
    }

    private void removeEndView(){
        if (null != mEndView){
            mListView.removeFooterView(mEndView);
        }
    }

    private void httpRequest() {
        int page = 0;
        if (null != mData.getData()) {
            page = mData.getData().size() / mData.getStep();
            if (mData.getData().size()% mData.getStep() != 0){
                page ++;
            }
        }
        LogUtils.i(TAG, mData+"");
        LogUtils.d(TAG, "httpRequest  mPage : " + page);
        AjaxParams params = new AjaxParams();
        params.put("appkey", AppliteUtils.getMitMetaDataValue(mActivity, Constant.META_DATA_MIT));
        params.put("packagename", "com.android.applite1.0");
        params.put("protocol_version", Constant.PROTOCOL_VERSION);
        params.put("app", "applite");
        params.put("type", "hptab");
        params.put("page", String.valueOf(page));
        params.put("tabtype", mData.getS_key());
        mFinalHttp.post(Constant.URL, params, new AjaxCallBack<Object>() {
            @Override
            public void onSuccess(Object o) {
                super.onSuccess(o);
                LogUtils.i(TAG, "HomePageList网络请求成功，" + (String) o);
                HomePageDataBean pageData = gson.fromJson((String) o, HomePageDataBean.class);
                if (null != pageData && null != pageData.getSubjectData()) {
                    for (int i = 0; i < pageData.getSubjectData().size(); i++) {
                        SubjectData subject = pageData.getSubjectData().get(i);
                        if (subject.getS_key().equals(mData.getS_key())) {
                            mData.setS_datatype(subject.getS_datatype());
                            mData.setStep(subject.getStep());
                            if (null != subject.getData()) {
                                mData.getData().addAll(subject.getData());
                            }
                            break;
                        }
                    }
                }
                sendhttpreq = true;

                mListAdapter.notifyDataSetChanged();
                mMoreView.setVisibility(View.GONE);
                //mMoreTextView.setVisibility(View.GONE);

                if((pageData.getSubjectData().get(0).getS_key().equals("maintype"))||(pageData.getSubjectData().get(0).getS_key().equals("maintype_m_game"))){
                    removeMoreView();
                }

                if (pageData.getSubjectData().get(0).getData().isEmpty()) {
                    isend = true;
                    mMoreView.setVisibility(View.GONE);
                } else {
                    isend = false;
                    removeEndView();
                }
                sendhttpreq = true;

                mListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                super.onFailure(t, errorNo, strMsg);
                LogUtils.e(TAG, "HomePage网络请求失败:" + strMsg);
                sendhttpreq = true;
                mMoreView.setVisibility(View.GONE);
                //mMoreTextView.setVisibility(View.GONE);

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

    class MyScrollListener implements AbsListView.OnScrollListener{
        private boolean isLastRow = false;
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (isLastRow && scrollState == this.SCROLL_STATE_IDLE) {
                LogUtils.i(TAG, "拉到最底部");
                if(!isend){
                    mMoreView.setVisibility(view.VISIBLE);
                    mEndTextView.setVisibility(View.GONE);
                    //mMoreTextView.setVisibility(View.VISIBLE);
                }else{
                    mMoreView.setVisibility(view.VISIBLE);
                    mMoreTextView.setVisibility(View.GONE);
                    mEndTextView.setVisibility(View.VISIBLE);
                }
                if(sendhttpreq){
                    httpRequest();
                    sendhttpreq = false;
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
        public void onClick(View v, int position){
            SpecialTopicData topicData = mData.getSpecialtopic_data().get(position);
            LogUtils.i(TAG, "topicData = " + topicData+" mTopicView = "+mTopicView);
//            mTopicView.stopPlay();
            if(topicData.getT_skiptype() == 1){
                MitMobclickAgent.onEvent(mActivity, "onSlideViewClick_" + topicData.getTt_packageName());
                ((OSGIServiceHost)mActivity).jumptoDetail(topicData.getTt_packageName(), topicData.getTt_name(), topicData.getTt_iconUrl(), true);
            }else{
                MitMobclickAgent.onEvent(mActivity, "onSlideViewClick_" + topicData.t_key);
                ((OSGIServiceHost)mActivity).jumptoTopic(topicData.t_key,topicData.t_info,mData.getStep(),mData.getS_datatype(),true);
            }
        }
    }
}
