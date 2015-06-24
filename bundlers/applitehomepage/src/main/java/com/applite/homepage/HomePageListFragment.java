package com.applite.homepage;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
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
import com.applite.common.Constant;
import com.applite.data.ListArrayAdapter;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplListener;
import com.applite.utils.HomePageUtils;

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
    private Context mContext;
    private List<HomePageBean> mData;
    List<HomePageTypeBean> mDataType;
    private int mTable = 0;
    float x, y, upx, upy;
    boolean mDualPane;
    private PullDownView pullDownView; //PullDown
    private ScrollOverListView listView;
    int mCurCheckPosition = 0;
    private ImplListener mImplListener = new HomePageImplListener();
    public HomePageListFragment(List<HomePageBean> data, List<HomePageTypeBean> mDataType, int mTable, Activity activity) {
        this.mData = data;
        this.mTable = mTable;
        this.mDataType = mDataType;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HomePageUtils.i(TAG, "ListFragment.onCreate() yuzm");
    }

    @Override
    public void onResume() {
        super.onResume();
        HomePageUtils.i(TAG, "onResume yuzm");
    }

    @Override
    public void onStart() {
        HomePageUtils.i(TAG, "onStart yuzm");
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        HomePageUtils.i(TAG, "onStop");
    }
    @Override
    public void onDetach(){
        super.onDetach();
        HomePageUtils.i(TAG, "onDetach yuzm");
        ImplAgent.unregisterImplListener(mImplListener);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        //ImplAgent.unregisterImplListener(mImplListener);
        HomePageUtils.i(TAG, "onDestroy");
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        //HomePageUtils.i(TAG, "onListItemClick.onCreate() yuzm");
        super.onListItemClick(l, v, position, id);
        if (2 == this.mTable) {
            //homePageFragment.postMainType(mActivity);
            FragmentManager fm;
            FragmentTransaction ftx;
            try {
                mContext =mActivity;
            }catch (Exception e){
                e.printStackTrace();
            }
            try {
                Context context = BundleContextFactory.getInstance().getBundleContext().getBundleContext();
                BundleContextFactory.getInstance().getBundleContext().getAndroidContext();
                if (null != context) {
                    mContext = context;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            HomePageFragment hp = HomePageFragment.newInstance(position, mContext);
            hp.setMainType(true);
            fm = ((FragmentActivity)mActivity).getSupportFragmentManager();
            ftx = fm.beginTransaction();
            //ftx.hide()
            ftx.replace(hp.getNode(), hp);
            //ftx.addToBackStack(null);
            ftx.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ftx.commit();
            HomePageUtils.i(TAG, "onListItemClick.onCreate() yuzm");

        }
        HomePageUtils.i(TAG, "onListItemClick() yuzm l : " + l + " ; v : " + v +
                " ; id : " + id + " ; position : " + position);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        HomePageUtils.i(TAG, "onAttach yuzm");
        mListAdapter = new ListArrayAdapter(mActivity, R.layout.fragment_list, mData, mDataType, mTable);
        setListAdapter(mListAdapter);
        ImplAgent.registerImplListener(mImplListener);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        HomePageUtils.i(TAG, "onActivityCreated yuzm");
        //PullToRefreshListView listView = new PullToRefreshListView(getActivity());

        if (savedInstanceState != null) {
            // Restore last state for checked position.
            mCurCheckPosition = savedInstanceState.getInt("curChoice", 0);
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        HomePageUtils.i(TAG, "onSaveInstanceState yuzm");
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
        HomePageUtils.i(TAG, "ListFragment.onCreateView() yuzm");
        View rootView = mInflater.inflate(R.layout.fragment_tabl, container, false);
        /*View mView = mInflater.inflate(R.layout.fragment_list, container, false);
        pullDownView = (PullDownView)mView.findViewById(R.id.pullDownView);
        HomePageUtils.i(TAG, "ListFragment.onCreateView() yuzm pullDownView : " + pullDownView);
        if(null != pullDownView) {
            pullDownView.enableAutoFetchMore(true, 0);
            listView = pullDownView.getListView();

            pullDownView.setOnPullDownListener(new PullDownView.OnPullDownListener() {

                @Override
                public void onRefresh() {//刷新
                    getNewString(new Handler() {
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
                    getNewString(new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            //arrays.add((String) msg.obj);
                            //adapter.notifyDataSetChanged();
                            pullDownView.notifyDidLoadMore(true);
                            System.out.println("加载更多");
                        }
                    });
                }
            });
        }*/
        //TextView tv = (TextView)rootView.findViewById(R.id.section_label);
        //String text = "" + tv.getText()+getArguments().getInt(ARG_SECTION_NUMBER);
        //tv.setText(text);

        return rootView;
    }
    private void getNewString(final Handler handler) {
        new Thread(new Runnable() {//刷新
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (Exception e) {
                    Thread.interrupted();
                    e.printStackTrace();
                }
                handler.obtainMessage(0, "New Text " + System.currentTimeMillis()).sendToTarget();
            }
        }).start();
    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            x = event.getX();
            y = event.getY();
            HomePageUtils.v(TAG, "yuzm is on touch down x = " + x + " ,y = " + y);
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            upx = event.getX();
            upy = event.getY();
            int position1 = ((ListView) v).pointToPosition((int) x, (int) y);
            int position2 = ((ListView) v).pointToPosition((int) upx, (int) upy);

            HomePageUtils.v(TAG, "yuzm is on touch x = " + x + " ,y = " + y);
            HomePageUtils.v(TAG, "yuzm is on touch upx = " + upx + " ,upy = " + upy);

            HomePageUtils.v(TAG, "yuzm is on touch positon1 = " + position1 + " ,position2 = " + position2);

            if (position1 == position2 && Math.abs(x - upx) > 10) {
                View view = ((ListView) v).getChildAt(position1);
                //removeListItem(view, position1);
            }
        }

        return false;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        HomePageUtils.v(TAG, "onScrollStateChanged yuzm view = " + view + " ,scrollState = " + scrollState);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        HomePageUtils.v(TAG, "onScrollStateChanged yuzm view = " + view + " ,totalItemCount = " + totalItemCount);
    }

    class HomePageImplListener implements ImplListener{
        private final String TAG = "impl_homepage";
        private Runnable mRefreshListRunnable = new Runnable(){
            @Override
            public void run() {
                mListAdapter.notifyDataSetInvalidated();
            }
        };
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

        @Override
        public void onDownloadComplete(boolean b, ImplAgent.DownloadCompleteRsp downloadCompleteRsp) {
            HomePageUtils.i(TAG,  "onDownloadComplete key="+downloadCompleteRsp.key);
            HomePageBean bean = findBeanByKey(downloadCompleteRsp.key);
            if (null != bean){
                HomePageUtils.i(TAG,  "onDownloadComplete name="+bean.getName()+",status="+bean.getStatus());
                if (bean.getStatus() <= Constant.STATUS_FAILED) {
                    bean.setStatus(downloadCompleteRsp.status);
                    mActivity.runOnUiThread(mRefreshListRunnable);
                }
            }
            HomePageUtils.i(TAG,  "onDownloadComplete end");
        }

        @Override
        public void onDownloadUpdate(boolean b, ImplAgent.DownloadUpdateRsp downloadUpdateRsp) {
            HomePageUtils.i(TAG,  "onDownloadUpdate  key="+downloadUpdateRsp.key);
            HomePageBean bean = findBeanByKey(downloadUpdateRsp.key);
            if (null != bean){
                HomePageUtils.i(TAG,  "onDownloadUpdate name="+bean.getName()+",status="+bean.getStatus());
                if (bean.getStatus() <= Constant.STATUS_FAILED) {
                    int OriginalStatus = bean.getStatus();
                    bean.setStatus(downloadUpdateRsp.status);
                    if (OriginalStatus != bean.getStatus()) {
                        mActivity.runOnUiThread(mRefreshListRunnable);
                    }
                }
            }
        }

        @Override
        public void onPackageAdded(boolean b, ImplAgent.PackageAddedRsp packageAddedRsp) {
            HomePageUtils.i(TAG,  "onPackageAdded key="+packageAddedRsp.key);
            HomePageBean bean = findBeanByKey(packageAddedRsp.key);
            if (null != bean){
                bean.setStatus(Constant.STATUS_INSTALLED);
                HomePageUtils.i(TAG,  "onPackageAdded name="+bean.getName()+",status="+bean.getStatus());
                mActivity.runOnUiThread(mRefreshListRunnable);
            }
        }

        @Override
        public void onPackageRemoved(boolean b, ImplAgent.PackageRemovedRsp packageRemovedRsp) {
            HomePageBean bean = findBeanByKey(packageRemovedRsp.key);
            if (null != bean){
                bean.setStatus(Constant.STATUS_INIT);
                HomePageUtils.i(TAG,  "onPackageRemoved key="+packageRemovedRsp.key+",name="+bean.getName()+",status="+bean.getStatus());
                mActivity.runOnUiThread(mRefreshListRunnable);
            }
        }

        @Override
        public void onPackageChanged(boolean b, ImplAgent.PackageChangedRsp packageChangedRsp) {
            HomePageBean bean = findBeanByKey(packageChangedRsp.key);
            if (null != bean){
                bean.setStatus(Constant.STATUS_INSTALLED);
                HomePageUtils.i(TAG,  "onPackageChanged key="+packageChangedRsp.key+",name="+bean.getName()+",status="+bean.getStatus());
                mActivity.runOnUiThread(mRefreshListRunnable);
            }
        }

        @Override
        public void onSystemInstallResult(boolean b, ImplAgent.SystemInstallResultRsp systemInstallResultRsp) {
            HomePageUtils.i(TAG,  "onSystemInstallResult key="+systemInstallResultRsp.key);
            HomePageBean bean = findBeanByKey(systemInstallResultRsp.key);
            if (null != bean){
                if (systemInstallResultRsp.result == Constant.INSTALL_SUCCEEDED) {
                    bean.setStatus(Constant.STATUS_INSTALLED);
                }else{
                    bean.setStatus(Constant.STATUS_INSTALL_FAILED);
                }
                HomePageUtils.i(TAG,  "onSystemInstallResult name="+bean.getName()+",status="+bean.getStatus());
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
                HomePageUtils.i(TAG,  "onSystemDeleteResult key="+systemDeleteResultRsp.key+",name="+bean.getName()+",status="+bean.getStatus());
                mActivity.runOnUiThread(mRefreshListRunnable);
            }
        }

        @Override
        public void onFinish(boolean b, ImplAgent.ImplResponse implResponse) {
            HomePageUtils.i(TAG,  "onFinish implResponse.action="+implResponse.action);
        }
    }

}

