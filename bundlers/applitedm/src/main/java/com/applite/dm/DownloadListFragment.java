package com.applite.dm;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.applite.common.Constant;
import com.applite.common.LogUtils;
import com.applite.dm.utils.HostUtils;
import com.lidroid.xutils.DbUtils;
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
import java.util.Arrays;
import java.util.List;
import java.util.Observer;

public class DownloadListFragment extends OSGIBaseFragment implements ListView.OnItemClickListener {
    private ListView mListview;
    private DownloadAdapter mAdapter;
    private Integer mStatusFlags;
    private DbUtils db;
    private boolean flag_showCheckBox = false;
    private Button btnDelete = null;
    private Button btnNone = null;
    private Button btnShare = null;
    private boolean[] status = null;
    private Cursor cursor = null;
//    private static int len = -1;//这里长度待定

    public static Bundle newBundle(int flag) {
        Bundle b = new Bundle();
        b.putInt("statusFilter", flag);
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
        btnDelete = (Button) view.findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(btn_clickLis);
        btnNone = (Button) view.findViewById(R.id.btnNone);
        btnShare = (Button) view.findViewById(R.id.btnShare);
        btnShare.setOnClickListener(btn_clickLis);
        //这里添加一个 从adapter拿到getOunt的方法 赋给len
////        len = mAdapter.getlen();
//        try {
////            len = mAdapter.getlen() + 1;
////            len = cursor.getCount() + 1;
//            len = cursor.getColumnCount() + 1;
//            Toast.makeText(mActivity.getApplicationContext(), "!!!!", Toast.LENGTH_SHORT).show();
////            len = cursor.getCount() + 1;
//        } catch (Exception e) {
//            Toast.makeText(mActivity.getApplicationContext(), "????", Toast.LENGTH_SHORT).show();
        int len = 40;
//        }

        status = new boolean[len];
        Arrays.fill(status, false);//全部填充为false(chechbox不选中)
        setAdapter();

        //我在这里添加长按删除的代码
        mListview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (!flag_showCheckBox) {
                    if (btnDelete.getVisibility() != View.VISIBLE) {
                        flag_showCheckBox = true;
                        btnDelete.setVisibility(View.VISIBLE);
                        btnNone.setVisibility(View.INVISIBLE);
                        btnShare.setVisibility(View.VISIBLE);
                        mAdapter.resetFlag(flag_showCheckBox);
                    }
                    status[i] = !status[i];
//                    Toast.makeText(mActivity.getApplicationContext(), status[i] + "", Toast.LENGTH_SHORT).show();
                    mAdapter.resetStatus(status);
                    mAdapter.notifyDataSetChanged();
                }
                return false;
            }
        });
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        db = ImplDbHelper.getDbUtils(mActivity.getApplicationContext());
        Bundle params = getArguments();
        if (null != params) {
            mStatusFlags = params.getInt("statusFilter");
        }
        ImplLog.d(DownloadPagerFragment.TAG, "onAttach," + this);
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("DownloadListFragment"); //统计页面
        flag_showCheckBox = false;
    }

//    @overdide
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            if (flag_showCheckBox) {
//                Toast.makeText(mActivity.getApplicationContext(), "我取消了操作", Toast.LENGTH_SHORT).show();
//                flag_showCheckBox = false;
//                mAdapter.notifyDataSetChanged();
//                btnDelete.setVisibility(View.INVISIBLE);
//                Arrays.fill(status, false);
//                return false;
//            }
//        }
//        return mActivity.onKeyDown(keyCode, event);
//    }

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
        DownloadAdapter.ViewHolder vh = (DownloadAdapter.ViewHolder) view.getTag();
        if (flag_showCheckBox) {
            mListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    if (flag_showCheckBox) {
                        status[i] = !status[i];
//                        Toast.makeText(mActivity.getApplicationContext(), status[i] + "", Toast.LENGTH_SHORT).show();
                        mAdapter.resetStatus(status);
                        mAdapter.notifyDataSetChanged();
                    }
                }
            });
        } else {
            if (null != vh) {
                HostUtils.launchDetail((OSGIServiceHost) mActivity,
                        vh.implInfo.getPackageName(),
                        vh.implInfo.getTitle(),
                        vh.implInfo.getIconUrl());
            }
        }
    }

    private View.OnClickListener btn_clickLis = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            btnDelete.setVisibility(View.GONE);
            btnNone.setVisibility(View.GONE);
            btnShare.setVisibility(View.GONE);
            if (view.getId() == R.id.btnShare) {
                flag_showCheckBox = false;
                mAdapter.resetFlag(flag_showCheckBox);
                Arrays.fill(status, false);//删除后将status复位
//                Toast.makeText(mActivity.getApplicationContext(), "分享功能尚未实现，敬请期待", Toast.LENGTH_SHORT).show();
            } else if (view.getId() == R.id.btnDelete) {
                deleteItem();
            }


        }
    };

    private void deleteItem() {
        View childView;
        DownloadAdapter.ViewHolder vh = null;
        ImplAgent implAgent;
        String key;
        String packageName;
        int versionCode;
        cursor.moveToFirst();
        int count = 0;
        for (int i = 0; i < cursor.getCount(); i++) {
            if (status[i]) {
                //这里删除
                childView = mListview.getChildAt(i);
                //但是删除一个的话会正常删除
                vh = (DownloadAdapter.ViewHolder) childView.getTag();
//                ImplAgent.getInstance(mActivity.getApplicationContext()).remove(vh.implInfo);
                implAgent = ImplAgent.getInstance(mActivity.getApplicationContext());
//                LogUtils.d("dm_list", "count" + cursor.getColumnCount());
                key = cursor.getString(cursor.getColumnIndex("key"));
                packageName = cursor.getString(cursor.getColumnIndex("packageName"));
                versionCode = cursor.getInt(cursor.getColumnIndex("versionCode"));
                vh.initView(implAgent.getImplInfo(key, packageName, versionCode));
                implAgent.remove(vh.implInfo);
                count++;
//                childView.setVisibility(View.GONE);
//                mAdapter.notifyDataSetChanged();
            }
            cursor.moveToNext();
        }
        flag_showCheckBox = false;
        mAdapter.resetFlag(flag_showCheckBox);
        Arrays.fill(status, false);//删除后将status复位
        setAdapter();//这里
        Toast.makeText(mActivity.getApplicationContext(), "成功删除了" + count + "条下载", Toast.LENGTH_SHORT).show();
//        mAdapter.notifyDataSetChanged();//并通知适配器
    }

    private void setAdapter() {
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
        LogUtils.d("applitedm", sql);

        try {
            cursor = db.execQuery(sql);
        } catch (DbException e) {
            e.printStackTrace();
        }
//        if (null == mAdapter){
        if (null != cursor) {
            mAdapter = new DownloadAdapter(mActivity, cursor, mStatusFlags, flag_showCheckBox, status);
            mListview.setAdapter(mAdapter);
        }
//        }else{
//            mAdapter.changeCursor(cursor);
//            mAdapter.notifyDataSetChanged();
//        }
    }
}
