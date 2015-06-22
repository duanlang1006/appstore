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
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplConfig;
import com.mit.impl.ImplDatabaseHelper;
import com.mit.impl.ImplInfo;
import com.mit.impl.ImplListener;

import java.util.ArrayList;
import java.util.List;

public class DownloadListFragment extends ListFragment implements ListView.OnItemClickListener{
    private Activity mActivity;
    private ListView mListview;
    private DownloadAdapter mAdapter;
    private Integer mStatusFlags = null;
    private DownloadItemListener mDownloadSelectListener = new DownloadItemListener();
    private ImplDatabaseHelper databaseHelper;
    private ImplListener mImplListener = new ImplListener() {
        @Override
        public void onDownloadComplete(boolean b, ImplAgent.DownloadCompleteRsp downloadCompleteRsp) {
            if (null != mAdapter){
                mAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onDownloadUpdate(boolean success, ImplAgent.DownloadUpdateRsp downloadUpdateRsp) {
            if (null != mAdapter){
                mAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onPackageAdded(boolean b, ImplAgent.PackageAddedRsp packageAddedRsp) {
            if (null != mAdapter){
                mAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onPackageRemoved(boolean b, ImplAgent.PackageRemovedRsp packageRemovedRsp) {
            if (null != mAdapter){
                mAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onPackageChanged(boolean b, ImplAgent.PackageChangedRsp packageChangedRsp) {
            if (null != mAdapter){
                mAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onSystemInstallResult(boolean b, ImplAgent.SystemInstallResultRsp systemInstallResultRsp) {
            if (null != mAdapter){
                mAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onSystemDeleteResult(boolean b, ImplAgent.SystemDeleteResultRsp systemDeleteResultRsp) {
            if (null != mAdapter){
                mAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onFinish(boolean b, ImplAgent.ImplResponse implResponse) {
            if (null != mAdapter){
                mAdapter.notifyDataSetChanged();
            }
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DownloadListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = getArguments();
        mStatusFlags = b.getInt("statusFilter");
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
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ImplAgent.unregisterImplListener(mImplListener);
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
        public void onDownloadButtonClicked(DownloadItem.DownloadItemTag tag) {
            ImplInfo info = ImplConfig.findInfoByKey(databaseHelper,tag.key);
            if (null == info){
                return;
            }
            switch(tag.operate){
                case DownloadItem.DownloadItemTag.OP_DOWNLOAD:
                    if (null != info){
                        ImplAgent.downloadToggle(mActivity,info.getKey());
                    }
                    break;
                case DownloadItem.DownloadItemTag.OP_LAUNCH:
                case DownloadItem.DownloadItemTag.OP_OPEN:
                    if (tag.operate == DownloadItem.DownloadItemTag.OP_OPEN
                            && "application/vnd.android.package-archive".equals(tag.mediaType)){
                        if (null != info) {
                            ImplAgent.requestPackageInstall(mActivity, info.getKey(),tag.localUri.getPath(),tag.packageName,true);
                        }
                    }else {
                        if (null != tag.intent) {
                            try {
                                mActivity.startActivity(tag.intent);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    break;
            }
        }

        @Override
        public void onDeleteButtonClicked(DownloadItem.DownloadItemTag tag) {
            ImplInfo info = ImplConfig.findInfoByKey(databaseHelper,tag.key);
            if (null == info){
                return;
            }
            if (null != info){
                ImplAgent.requestDownloadDelete(mActivity,info.getKey());
            }
        }

        @Override
        public void onDetailButtonClicked(DownloadItem.DownloadItemTag tag) {

        }
    }
}
