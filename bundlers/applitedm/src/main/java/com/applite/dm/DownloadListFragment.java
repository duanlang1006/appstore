package com.applite.dm;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.applite.common.Constant;
import com.applite.common.LogUtils;
import com.applite.dm.utils.HostUtils;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.bitmap.PauseOnScrollListener;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.db.table.Table;
import com.lidroid.xutils.exception.DbException;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplDbHelper;
import com.mit.impl.ImplInfo;
import com.mit.impl.ImplLog;
import com.osgi.extra.OSGIBaseFragment;
import com.osgi.extra.OSGIServiceHost;
import com.umeng.analytics.MobclickAgent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Observer;

public class DownloadListFragment extends OSGIBaseFragment implements ListView.OnItemClickListener{
    private ListView mListview;
    private DownloadAdapter mAdapter;
    private Integer mStatusFlags = null;
    private ImplAgent mImplAgent;
    private List<ImplInfo> mImplList;
    private BitmapUtils mBitmapHelper;
    public static final Comparator<ImplInfo> IMPL_TIMESTAMP_COMPARATOR = new Comparator<ImplInfo>() {
        public final int compare(ImplInfo a, ImplInfo b) {
            int result = 0;
            if (a.getLastMod()< b.getLastMod()){
                result = 1;
            }else if (a.getLastMod() > b.getLastMod()){
                result = -1;
            }
            return result;
        }
    };


    public static Bundle newBundle(int flag){
        Bundle b = new Bundle();
        b.putInt("statusFilter",flag);
        return b;
    }

    public DownloadListFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ImplLog.d(DownloadPagerFragment.TAG, "onCreate," + this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ImplLog.d(DownloadPagerFragment.TAG, "onCreateView," + this);
        LayoutInflater mInflater = inflater;
        View view = mInflater.inflate(R.layout.fragment_download_list, container, false);
        mListview = (ListView) view.findViewById(android.R.id.list);
        mListview.setEmptyView(view.findViewById(R.id.empty));
        mListview.setOnItemClickListener(this);
        mListview.setOnScrollListener(new PauseOnScrollListener(mBitmapHelper, false, true));

        if (null != mImplList && mImplList.size()>0) {
            mAdapter = new DownloadAdapter(mActivity,R.layout.download_list_item,mImplList,mBitmapHelper);
            mAdapter.sort(IMPL_TIMESTAMP_COMPARATOR);
            mListview.setAdapter(mAdapter);

        }
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle params = getArguments();
        if (null != params) {
            mStatusFlags = params.getInt("statusFilter");
        }
        mImplAgent = ImplAgent.getInstance(activity.getApplicationContext());
        mImplList = mImplAgent.getDownloadInfoList(mStatusFlags);
        mBitmapHelper = new BitmapUtils(mActivity.getApplicationContext());
        ImplLog.d(DownloadPagerFragment.TAG, "onAttach," + this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ImplLog.d(DownloadPagerFragment.TAG, "onDetach," + this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ImplLog.d(DownloadPagerFragment.TAG, "onDestroyView," + this);
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListview.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DownloadAdapter.ViewHolder vh = (DownloadAdapter.ViewHolder)view.getTag();
        if (null != vh){
            ((OSGIServiceHost) mActivity).jumptoDetail(
                    vh.implInfo.getPackageName(),
                    vh.implInfo.getTitle(),
                    vh.implInfo.getIconUrl(),
                    true);
        }
    }
}
