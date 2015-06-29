package com.applite.dm;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.applite.common.Constant;
import com.mit.impl.ImplStatusTag;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplConfig;
import com.mit.impl.ImplDatabaseHelper;
import com.mit.impl.ImplInfo;
import com.mit.impl.ImplListener;
import com.mit.impl.ImplLog;

import java.util.ArrayList;
import java.util.List;

public class DownloadListFragment extends ListFragment implements ListView.OnItemClickListener{
    private Activity mActivity;
    private ListView mListview;
    private DownloadAdapter mAdapter;
    private Integer mStatusFlags = null;
    private DownloadItemListener mDownloadSelectListener = new DownloadItemListener();
    private ImplDatabaseHelper databaseHelper;
    private ImplListener mImplListener = new DownloadImplListener();

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DownloadListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ImplLog.d(DownloadPagerFragment.TAG,"onCreate,"+this);
        super.onCreate(savedInstanceState);
        Bundle b = getArguments();
        mStatusFlags = b.getInt("statusFilter");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ImplLog.d(DownloadPagerFragment.TAG,"onCreateView,"+this);
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
//        mListview.setOnItemClickListener(this);
        mListview.setEmptyView(view.findViewById(R.id.empty));
        setAdapter();
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        ImplAgent.registerImplListener(mImplListener);
        databaseHelper = new ImplDatabaseHelper(mActivity);
        ImplLog.d(DownloadPagerFragment.TAG,"onAttach,"+this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ImplAgent.unregisterImplListener(mImplListener);
        ImplLog.d(DownloadPagerFragment.TAG,"onDetach,"+this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ImplLog.d(DownloadPagerFragment.TAG,"onDestroyView,"+this);
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
        if (!(view instanceof DownloadItem)){
            return;
        }
        DownloadItem item = (DownloadItem)view;
        View extra = view.findViewById(R.id.extra_line);
        if (extra.getVisibility() == View.GONE){
            extra.setVisibility(View.VISIBLE);
        }else if (extra.getVisibility() == View.VISIBLE){
            extra.setVisibility(View.GONE);
        }
    }

    private void setAdapter(){
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        List<String> selectionParts = new ArrayList<String>();

        if (mStatusFlags != null) {
            List<String> parts = new ArrayList<String>();
            if ((mStatusFlags & Constant.STATUS_PENDING) != 0) {
                parts.add(ImplConfig.statusClause("=", Constant.STATUS_PENDING));
            }
            if ((mStatusFlags & Constant.STATUS_RUNNING) != 0) {
                parts.add(ImplConfig.statusClause("=", Constant.STATUS_RUNNING));
            }
            if ((mStatusFlags & Constant.STATUS_PAUSED) != 0) {
                parts.add(ImplConfig.statusClause("=", Constant.STATUS_PAUSED));
            }
            if ((mStatusFlags & Constant.STATUS_SUCCESSFUL) != 0) {
                parts.add(ImplConfig.statusClause("=", Constant.STATUS_SUCCESSFUL));
            }
            if ((mStatusFlags & Constant.STATUS_FAILED) != 0) {
                parts.add(ImplConfig.statusClause("=", Constant.STATUS_FAILED));
            }
            if ((mStatusFlags & Constant.STATUS_PACKAGE_INVALID) != 0) {
                parts.add(ImplConfig.statusClause("=", Constant.STATUS_PACKAGE_INVALID));
            }
            if ((mStatusFlags & Constant.STATUS_PRIVATE_INSTALLING) != 0) {
                parts.add(ImplConfig.statusClause("=", Constant.STATUS_PRIVATE_INSTALLING));
            }
            if ((mStatusFlags & Constant.STATUS_NORMAL_INSTALLING) != 0) {
                parts.add(ImplConfig.statusClause("=", Constant.STATUS_NORMAL_INSTALLING));
            }
            if ((mStatusFlags & Constant.STATUS_INSTALLED) != 0) {
                parts.add(ImplConfig.statusClause("=", Constant.STATUS_INSTALLED));
            }
            if ((mStatusFlags & Constant.STATUS_INSTALL_FAILED) != 0) {
                parts.add(ImplConfig.statusClause("=", Constant.STATUS_INSTALL_FAILED));
            }
            selectionParts.add("("+ImplConfig.joinStrings(" OR ", parts)+")");
        }
        String selection = ImplConfig.joinStrings(" AND ", selectionParts);
        String orderBy = ImplConfig.COLUMN_LAST_MODIFIED_TIMESTAMP + " DESC";

        Cursor cursor = db.query(ImplConfig.TABLE_IMPL,null,selection,null,null,null,orderBy);
        if (null == mAdapter){
            if (null != cursor) {
                mAdapter = new DownloadAdapter(mActivity, cursor, mDownloadSelectListener);
                mListview.setAdapter(mAdapter);
            }
        }else{
            mAdapter.changeCursor(cursor);
        }
    }

    private class DownloadItemListener implements DownloadItem.DownloadSelectListener{
        @Override
        public void onDownloadButtonClicked(ImplStatusTag tag) {
            ImplInfo info = ImplConfig.findInfoByKey(databaseHelper,tag.getKey());
            if (null == info){
                return;
            }
            switch(tag.getAction()){
                case ImplStatusTag.ACTION_DOWNLOAD:
                    if (null != info){
                        ImplAgent.downloadToggle(mActivity,info.getKey());
                    }
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

        @Override
        public void onDeleteButtonClicked(ImplStatusTag tag) {
            ImplInfo info = ImplConfig.findInfoByKey(databaseHelper,tag.getKey());
            if (null == info){
                return;
            }
            ImplAgent.requestDownloadDelete(mActivity,info.getKey());
        }

        @Override
        public void onDetailButtonClicked(ImplStatusTag tag) {

        }
    }

    private class DownloadImplListener implements ImplListener{
        private static final String TAG = "impl_dm";
        private Runnable mNotifyRunnable = new Runnable() {
            @Override
            public void run() {
                if (null != mAdapter){
                    Cursor c = mAdapter.getCursor();
                    if (null != c && ! c.isClosed()){
                        c.requery();
                    }
                }else {
                    setAdapter();
                }
            }
        };
        @Override
        public void onDownloadComplete(boolean b, ImplAgent.DownloadCompleteRsp downloadCompleteRsp) {
            ImplLog.d(TAG,  "onDownloadComplete key="+downloadCompleteRsp.key+","+downloadCompleteRsp.localPath);
            mActivity.runOnUiThread(mNotifyRunnable);
        }

        @Override
        public void onDownloadUpdate(boolean success, ImplAgent.DownloadUpdateRsp downloadUpdateRsp) {
            ImplLog.d(TAG,  "onDownloadUpdate key="+downloadUpdateRsp.key+","+downloadUpdateRsp.status);
            mActivity.runOnUiThread(mNotifyRunnable);
        }

        @Override
        public void onPackageAdded(boolean b, ImplAgent.PackageAddedRsp packageAddedRsp) {
            mActivity.runOnUiThread(mNotifyRunnable);
        }

        @Override
        public void onPackageRemoved(boolean b, ImplAgent.PackageRemovedRsp packageRemovedRsp) {
            mActivity.runOnUiThread(mNotifyRunnable);
        }

        @Override
        public void onPackageChanged(boolean b, ImplAgent.PackageChangedRsp packageChangedRsp) {
            mActivity.runOnUiThread(mNotifyRunnable);
        }

        @Override
        public void onSystemInstallResult(boolean b, ImplAgent.SystemInstallResultRsp systemInstallResultRsp) {
            mActivity.runOnUiThread(mNotifyRunnable);
        }

        @Override
        public void onSystemDeleteResult(boolean b, ImplAgent.SystemDeleteResultRsp systemDeleteResultRsp) {
            mActivity.runOnUiThread(mNotifyRunnable);
        }

        @Override
        public void onFinish(boolean b, ImplAgent.ImplResponse implResponse) {
            mActivity.runOnUiThread(mNotifyRunnable);
        }
    }
}
