package com.applite.dm;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.applite.dm.utils.HostUtils;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.bitmap.PauseOnScrollListener;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplInfo;
import com.mit.impl.ImplLog;
import com.osgi.extra.OSGIBaseFragment;
import com.osgi.extra.OSGIServiceHost;
import com.umeng.analytics.MobclickAgent;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class DownloadListFragment extends OSGIBaseFragment implements ListView.OnItemClickListener {
    private ListView mListview;
    private DownloadAdapter mAdapter;
    private boolean flag_showCheckBox = false;//长按删除的标志位
    private Button btnDelete = null;
    private Button btnNone = null;//占位
    private Button btnShare = null;
    private boolean[] status = null;//这里存放checkBox的选中状态
    //    private int len = -1;//标志位数组的长度
    private Integer mStatusFlags = null;
    private ImplAgent mImplAgent;
    private List<ImplInfo> mImplList;
    private BitmapUtils mBitmapHelper;
    private int mCount = 0;

    public static final Comparator<ImplInfo> IMPL_TIMESTAMP_COMPARATOR = new Comparator<ImplInfo>() {
        public final int compare(ImplInfo a, ImplInfo b) {
            int result = 0;
            if (a.getLastMod() < b.getLastMod()) {
                result = 1;
            } else if (a.getLastMod() > b.getLastMod()) {
                result = -1;
            }
            return result;
        }
    };


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
        mListview.setAdapter(mAdapter);
//        Toast.makeText(mActivity.getApplicationContext(), "长度成功获取", Toast.LENGTH_SHORT).show();
        status = new boolean[mImplList.size()];
        Arrays.fill(status, false);//全部填充为false(chechbox不选中)
        //这里是长按删除
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
        mListview.setOnScrollListener(new PauseOnScrollListener(mBitmapHelper, false, true));
        if (null != mImplList && mImplList.size() > 0) {
            mAdapter = new DownloadAdapter(mActivity, R.layout.download_list_item, mImplList, mBitmapHelper, flag_showCheckBox);
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
                ((OSGIServiceHost) mActivity).jumptoDetail(
                        vh.implInfo.getPackageName(),
                        vh.implInfo.getTitle(),
                        vh.implInfo.getIconUrl(),
                        true);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Toast.makeText(mActivity.getApplicationContext(), "????", Toast.LENGTH_SHORT).show();
        if (item.getItemId() == R.id.dm_action_pause_all) {
            Toast.makeText(mActivity.getApplicationContext(), "dm_action_pause_all", Toast.LENGTH_SHORT).show();
        } else if (item.getItemId() == R.id.dm_action_resume_all) {
            Toast.makeText(mActivity.getApplicationContext(), "dm_action_resume_all", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
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
                mAdapter.notifyDataSetChanged();
                Arrays.fill(status, false);//删除后将status复位
                Toast.makeText(mActivity.getApplicationContext(), "分享功能尚未实现，敬请期待", Toast.LENGTH_SHORT).show();
            } else if (view.getId() == R.id.btnDelete) {
                deleteItem();
            }
        }
    };

    private void deleteItem() {
        View childView;
        DownloadAdapter.ViewHolder vh;
        ImplAgent implAgent;
        int count = 0;//count仅统计删除的条数
        int len = mImplList.size();
//        for (int i = 0; i < len; i++) {
        for (int i = len - 1; i >= 0; i--) {
            if (status[i]) {
                childView = mListview.getChildAt(i);
                vh = (DownloadAdapter.ViewHolder) childView.getTag();
                implAgent = ImplAgent.getInstance(mActivity.getApplicationContext());
                implAgent.remove(vh.implInfo);
                mImplList.remove(i);
                count++;
//                childView.setVisibility(View.GONE);
            }
        }
        flag_showCheckBox = false;
        mAdapter.resetFlag(flag_showCheckBox);
        Arrays.fill(status, false);//删除后将status复位
        mAdapter.resetStatus(status);
//        mListview.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();//并通知适配器
        Toast.makeText(mActivity.getApplicationContext(), "成功删除了" + count + "条下载", Toast.LENGTH_SHORT).show();
    }
}
