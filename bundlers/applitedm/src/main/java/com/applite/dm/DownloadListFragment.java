package com.applite.dm;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.applite.common.Constant;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.db.table.Table;
import com.lidroid.xutils.exception.DbException;
import com.mit.impl.ImplDbHelper;
import com.mit.impl.ImplInfo;
import com.mit.impl.ImplLog;
import com.umeng.analytics.MobclickAgent;
import java.util.ArrayList;
import java.util.List;

public class DownloadListFragment extends ListFragment implements ListView.OnItemClickListener{
    private Activity mActivity;
    private ListView mListview;
    private DownloadAdapter mAdapter;
    private Integer mStatusFlags = null;
    private DbUtils db;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DownloadListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ImplLog.d(DownloadPagerFragment.TAG, "onCreate," + this);
        super.onCreate(savedInstanceState);
        Bundle b = getArguments();
        mStatusFlags = b.getInt("statusFilter");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ImplLog.d(DownloadPagerFragment.TAG, "onCreateView," + this);
        LayoutInflater mInflater = inflater;
        try {
            Context context = BundleContextFactory.getInstance().getBundleContext().getBundleContext();
            if (null != context) {
                mInflater = LayoutInflater.from(context);
                mInflater = mInflater.cloneInContext(context);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        View view = mInflater.inflate(R.layout.fragment_download_list, container, false);
        mListview = (ListView) view.findViewById(android.R.id.list);
        mListview.setEmptyView(view.findViewById(R.id.empty));
//        mListview.setOnItemClickListener(this);
        setAdapter();
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        db = ImplDbHelper.getDbUtils(mActivity.getApplicationContext());
        ImplLog.d(DownloadPagerFragment.TAG, "onAttach," + this);
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("DownloadListFragment"); //统计页面
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("DownloadListFragment");
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
    }

    private void setAdapter(){
        Table table = Table.get(db, ImplInfo.class);
        WhereBuilder wb = WhereBuilder.b();
        if (mStatusFlags != null) {
            List<String> parts = new ArrayList<String>();
            if ((mStatusFlags & Constant.STATUS_PENDING) != 0) {
                wb.or("status", "=", Constant.STATUS_PENDING);
            }
            if ((mStatusFlags & Constant.STATUS_RUNNING) != 0) {
                wb.or("status", "=", Constant.STATUS_RUNNING);
            }
            if ((mStatusFlags & Constant.STATUS_PAUSED) != 0) {
                wb.or("status", "=", Constant.STATUS_PAUSED);
            }
            if ((mStatusFlags & Constant.STATUS_SUCCESSFUL) != 0) {
                wb.or("status", "=", Constant.STATUS_SUCCESSFUL);
            }
            if ((mStatusFlags & Constant.STATUS_FAILED) != 0) {
                wb.or("status", "=", Constant.STATUS_FAILED);
            }
            if ((mStatusFlags & Constant.STATUS_PACKAGE_INVALID) != 0) {
                wb.or("status", "=", Constant.STATUS_PACKAGE_INVALID);
            }
            if ((mStatusFlags & Constant.STATUS_PRIVATE_INSTALLING) != 0) {
                wb.or("status", "=", Constant.STATUS_PRIVATE_INSTALLING);
            }
            if ((mStatusFlags & Constant.STATUS_NORMAL_INSTALLING) != 0) {
                wb.or("status", "=", Constant.STATUS_NORMAL_INSTALLING);
            }
            if ((mStatusFlags & Constant.STATUS_INSTALLED) != 0) {
                wb.or("status", "=", Constant.STATUS_INSTALLED);
            }
            if ((mStatusFlags & Constant.STATUS_INSTALL_FAILED) != 0) {
                wb.or("status", "=", Constant.STATUS_INSTALL_FAILED);
            }
            if ((mStatusFlags & Constant.STATUS_UPGRADE) != 0) {
                wb.or("status", "=", Constant.STATUS_UPGRADE);
            }
        }
        Selector selector = Selector.from(ImplInfo.class).where(wb);
        String sql = selector.toString();
        Cursor cursor = null;
        try {
            cursor = db.execQuery(sql);
        } catch (DbException e) {
            e.printStackTrace();
        }
        if (null == mAdapter){
            if (null != cursor) {
                mAdapter = new DownloadAdapter(mActivity, cursor);
                mListview.setAdapter(mAdapter);
            }
        }else{
            mAdapter.changeCursor(cursor);
        }
    }
}
