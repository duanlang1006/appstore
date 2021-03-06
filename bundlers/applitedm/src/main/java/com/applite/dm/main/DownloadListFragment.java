package com.applite.dm.main;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;

import com.mit.afinal.FinalHttp;
import com.mit.afinal.http.AjaxCallBack;
import com.mit.afinal.http.AjaxParams;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.applite.common.AppliteUtils;
import com.applite.common.Constant;
import com.applite.common.LogUtils;
import com.applite.common.VibratorUtil;
import com.applite.dm.adapter.DownloadAdapter;
import com.applite.dm.adapter.DownloadSimilarAdapter;
import com.applite.dm.R;
import com.applite.sharedpreferences.AppliteSPUtils;
import com.applite.similarview.SimilarAdapter;
import com.applite.similarview.SimilarBean;
import com.applite.similarview.SimilarView;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.PauseOnScrollListener;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplChangeCallback;
import com.mit.impl.ImplHelper;
import com.mit.impl.ImplInfo;
import com.mit.impl.ImplLog;
import com.osgi.extra.OSGIBaseFragment;
import com.osgi.extra.OSGIServiceHost;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class DownloadListFragment extends OSGIBaseFragment implements DownloadPagerFragment.IDownloadOperator,
        ListView.OnItemClickListener, AdapterView.OnItemLongClickListener, SimilarAdapter.SimilarAPKDetailListener,
        View.OnClickListener, Observer {
    final static String TAG = "applite_dm";
    private ListView mListview;
    private DownloadAdapter mAdapter;
    private int mStatusFlags;
    private int mTitleId;
    private ImplAgent mImplAgent;
    private List<ImplInfo> mImplList;


    private BitmapUtils mBitmapHelper;
    private SimilarView mSimilarView;
    private List<SimilarBean> mSimilarDataList;
    private SimilarAdapter mSimilarAdapter;
    //    private boolean checkBoxAnima = true;
    private int temp = 0;
    private HashMap<Long, Boolean> status = new HashMap();

    private TextView emptyView = null;

    private String COUNT_DOWNLOADING = "count downloading";
    private String COUNT_DOWNLOADED = "count downloaded";
    private String FLAG = "flag";
    private String POSITION = "position";

    private DownloadListener mDownloadListener = new DownloadListener() {
        @Override
        public boolean isShowCheckBox() {
            return (boolean) AppliteSPUtils.get(mActivity, FLAG, false);
        }

        @Override
        public int getTitleId() {
            return mTitleId;
        }

        @Override
        public boolean getStatus(long id) {
            return status.get(id);
        }

//        @Override
//        public boolean getCheckBoxAnimaStatus() {
//            return checkBoxAnima;
//        }
//
//        @Override
//        public void setCheckBoxAnimaStatus(boolean b) {
//            checkBoxAnima = b;
//        }
    };

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


    public static Bundle newBundle(int resid, int flag) {
        Bundle b = new Bundle();
        b.putInt("titleId", resid);
        b.putInt("statusFilter", flag);
        return b;
    }

    public DownloadListFragment() {
        super();
    }

    private SharedPreferences.OnSharedPreferenceChangeListener mListListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(POSITION)) {
                mAdapter.notifyDataSetChanged();
                if (R.string.dm_downloading == (int) AppliteSPUtils.get(mActivity, POSITION, 0)) {
                    if ((boolean) AppliteSPUtils.get(mActivity, FLAG, false) && View.VISIBLE == mSimilarView.getVisibility()) {
                        mSimilarView.setVisibility(View.GONE);
                        mSimilarView.setPadding(0, -mSimilarView.getHeight(), 0, 0);
                    } else if (!(boolean) AppliteSPUtils.get(mActivity, FLAG, false) && View.GONE == mSimilarView.getVisibility()) {
                        mSimilarView.setVisibility(View.VISIBLE);
                        mSimilarView.setPadding(0, 1, 0, 0);
                    }
                }
            }
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ImplAgent.getInstance(activity).addObserver(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle params = getArguments();
        if (null != params) {
            mStatusFlags = params.getInt("statusFilter");
            mTitleId = params.getInt("titleId");
        }
        mImplAgent = ImplAgent.getInstance(mActivity.getApplicationContext());
        mImplList = mImplAgent.getDownloadInfoList(mStatusFlags);
        Collections.sort(mImplList, IMPL_TIMESTAMP_COMPARATOR);
        setStatus(false);
        putCount(0);//当前页选中项目数
        mBitmapHelper = new BitmapUtils(mActivity.getApplicationContext());
        AppliteSPUtils.registerChangeListener(mActivity, mListListener);
        AppliteSPUtils.put(mActivity, FLAG, false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ImplLog.d(TAG, "onCreateView," + this);
        View view = inflater.inflate(R.layout.fragment_download_list, container, false);
        mListview = (ListView) view.findViewById(android.R.id.list);
        emptyView = (TextView) view.findViewById(R.id.emptylistview);
        setEmptyViewStatus();
        if (mTitleId == R.string.dm_downloading) {
            initSimilarView(view);
            mListview.addFooterView(mSimilarView);
        }
        mListview.setOnItemClickListener(this);
        mListview.setAdapter(mAdapter);
        mListview.setOnItemLongClickListener(this);
        mListview.setOnScrollListener(new PauseOnScrollListener(mBitmapHelper, false, true));
        if (null != mImplList) {
            mAdapter = new DownloadAdapter(mActivity, R.layout.download_list_item,
                    mImplList, mBitmapHelper, mDownloadListener);
            mListview.setAdapter(mAdapter);
        }
        return view;
    }

    private void setEmptyViewStatus() {
        if (mImplList.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ImplLog.d(DownloadListFragment.TAG, "onDetach," + this);
        ImplAgent.getInstance(mActivity).deleteObserver(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        setStatus(false);
        putCount(0);
        mAdapter.notifyDataSetChanged();
        ImplLog.d(DownloadListFragment.TAG, "onDestroyView," + this);
    }

    //当前页选中项目数
    private void putCount(int number) {
        if (mTitleId == R.string.dm_downloading) {
            AppliteSPUtils.put(mActivity, COUNT_DOWNLOADING, number);
        } else if (mTitleId == R.string.dm_downloaded) {
            AppliteSPUtils.put(mActivity, COUNT_DOWNLOADED, number);
        }
    }

    private int getCount() {
        if (mTitleId == R.string.dm_downloading) {
            return (int) AppliteSPUtils.get(mActivity, COUNT_DOWNLOADING, 0);
        } else if (mTitleId == R.string.dm_downloaded) {
            return (int) AppliteSPUtils.get(mActivity, COUNT_DOWNLOADED, 0);
        } else {
            return 0;
        }
    }

    private void initSimilarView(View view) {
        if (null == mSimilarView) {
            mSimilarView = (SimilarView) view.inflate(mActivity, R.layout.similar_view, null);
            mSimilarView.getTitleView().setText(getResources().getString(R.string.similar_title));
            mSimilarView.getChangeView().setText(getResources().getString(R.string.similar_change));
            mSimilarDataList = new ArrayList<>();
            mSimilarAdapter = new DownloadSimilarAdapter(mActivity);
            mSimilarAdapter.setData(mSimilarDataList, DownloadListFragment.this, 4);
            mSimilarView.setAdapter(mSimilarAdapter);
            post();
            mSimilarView.setVisibility(View.VISIBLE);
            mSimilarView.setPadding(0, 1, 0, 0);
        }
    }

    private void post() {
        AjaxParams params = new AjaxParams();
        params.put("appkey", AppliteUtils.getMitMetaDataValue(mActivity, Constant.META_DATA_MIT));
        params.put("packagename", mActivity.getPackageName());
        params.put("type", "update_management");
        params.put("protocol_version", Constant.PROTOCOL_VERSION);
        params.put("update_info", AppliteUtils.getAllApkData(mActivity));
        FinalHttp mFinalHttp = new FinalHttp();
        mFinalHttp.post(Constant.URL, params, new AjaxCallBack<String>() {
            @Override
            public void onSuccess(String responseInfo) {
                LogUtils.i(TAG, "更新请求成功，resulit：" + responseInfo);
                try {
                    JSONObject object = new JSONObject(responseInfo);
                    String similar_info = object.getString("similar_info");
                    if (!mSimilarDataList.isEmpty())
                        mSimilarDataList.clear();
                    JSONArray similar_json = new JSONArray(similar_info);
                    SimilarBean similarBean = null;
                    if (similar_json.length() != 0 && similar_json != null) {
                        for (int i = 0; i < similar_json.length(); i++) {
                            similarBean = new SimilarBean();
                            JSONObject obj = new JSONObject(similar_json.get(i).toString());
                            similarBean.setName(obj.getString("name"));
                            similarBean.setPackageName(obj.getString("packageName"));
                            similarBean.setIconUrl(obj.getString("iconUrl"));
                            similarBean.setrDownloadUrl(obj.getString("rDownloadUrl"));
                            similarBean.setVersionCode(obj.getInt("versionCode"));
                            mSimilarDataList.add(similarBean);
                        }
                        mSimilarAdapter.setData(mSimilarDataList, DownloadListFragment.this, 4);
                        mSimilarAdapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    //                    Toast.makeText(mActivity, "kong", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                LogUtils.i(TAG, mActivity.getPackageName() + " strMsg");
            }
        });


    }

//    /**
//     * The default content for this Fragment has a TextView that is shown when
//     * the list is empty. If you would like to change the text, call this method
//     * to supply the text it should use.
//     */
//    public void setEmptyText(CharSequence emptyText) {
//        View emptyView = mListview.getEmptyView();
//
//        if (emptyView instanceof TextView) {
//            ((TextView) emptyView).setText(emptyText);
//        }
//    }

    private void reSet() {
        if (null != mSimilarView && View.VISIBLE != mSimilarView.getVisibility()) {
            mSimilarView.setVisibility(View.VISIBLE);
            mSimilarView.setPadding(0, 1, 0, 0);
        }
        setStatus(false);
        putCount(0);//当前页选中项目数
//        checkBoxAnima = true;
        AppliteSPUtils.put(mActivity, FLAG, false);
//        AppliteSPUtils.put(mActivity, POSITION, 0);
        if (null != mAdapter) {
            mAdapter.notifyDataSetChanged();
        }
    }

    private void deleteItem(boolean deleteFile) {
        List<Long> tempList = new ArrayList<>();
        for (ImplInfo implInfo : mImplList) {
            if (status.get(implInfo.getId())) {
                tempList.add(implInfo.getId());
                status.remove(implInfo.getId());
            }
        }
        if (!tempList.isEmpty()) {
            mImplAgent.remove(tempList, deleteFile);
        }
    }

    @Override
    public void onClickDelete(boolean b) {
        deleteItem(b);
        reSet();
        if (null != mSimilarView && View.VISIBLE == mSimilarView.getVisibility()) {
            mSimilarAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClickSeleteAll() {
        setStatus(true);
        putCount(mImplList.size());
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClickDeselectAll() {
        setStatus(false);
        putCount(0);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void resetFlag() {
        reSet();
    }

    //获取本页下载项的个数
    @Override
    public int getLength() {
        //直接跳转到下载完成页会异常
        try {
            return mImplList.size();
        } catch (NullPointerException e) {
            return 0;
        }
    }

    @Override
    public void update(Observable observable, Object data) {
        if (null == mListview) {
            return;
        }
        mImplList = mImplAgent.getDownloadInfoList(mStatusFlags);
        Collections.sort(mImplList, IMPL_TIMESTAMP_COMPARATOR);

        //遍历新的mImplList
        for (ImplInfo implInfo : mImplList) {
            //有新增元素（新mImplList中存在，status中不存在）
            if (!status.containsKey(implInfo.getId())) {
                status.put(implInfo.getId(), false);
                continue;
            }
            //检查是否有移除元素（mImplList中已经移除但status中仍然存在）
            if (status.containsKey(implInfo.getId())) {
                continue;
            }
            //否则就是status中有mImplList中不存在的元素
            status.remove(implInfo.getId());
        }

        if (null == mAdapter) {
            mAdapter = new DownloadAdapter(mActivity, R.layout.download_list_item,
                    mImplList, mBitmapHelper, mDownloadListener);
            mListview.setAdapter(mAdapter);
        } else {
            mAdapter.clear();
            for (int i = 0; i < mImplList.size(); i++) {
                mAdapter.add(mImplList.get(i));
            }
            mAdapter.notifyDataSetChanged();
        }
        setEmptyViewStatus();
    }

    @Override
    public void onClick(View v) {
        if (R.id.similar_change == v.getId()) {
            initSimilarView(v);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position > mImplList.size() - 1) {
            return;
        }
        DownloadAdapter.ViewHolder vh = (DownloadAdapter.ViewHolder) view.getTag();
        if (!(boolean) AppliteSPUtils.get(mActivity, FLAG, false)) {
            if (null != vh) {
                ((OSGIServiceHost) mActivity).jumptoDetail(
                        vh.implInfo.getPackageName(),
                        vh.implInfo.getTitle(),
                        vh.implInfo.getIconUrl(),
                        vh.implInfo.getVersionCode(),
                        null,
                        true);
            }
        } else {
            status.put(vh.implInfo.getId(), !status.get(vh.implInfo.getId()));
            temp = getCount();
            putCount(status.get(vh.implInfo.getId()) ? temp + 1 : temp - 1);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (position > mImplList.size() - 1) {
            return false;
        }
        if (!(boolean) AppliteSPUtils.get(mActivity, FLAG, false)) {
            AppliteSPUtils.put(mActivity, FLAG, true);
            mImplList = mImplAgent.getDownloadInfoList(mStatusFlags);
            Collections.sort(mImplList, IMPL_TIMESTAMP_COMPARATOR);
            setStatus(false);
            putCount(0);
            if ((boolean) AppliteSPUtils.get(mActivity, FLAG, true)) {
                VibratorUtil.Vibrate(mActivity, 200);   //震动200ms
//                checkBoxAnima = false;
            }
            if (R.string.dm_downloading == mTitleId) {
                mSimilarView.setVisibility(View.GONE);
                mSimilarView.setPadding(0, -mSimilarView.getHeight(), 0, 0);
            }
//            mAdapter.notifyDataSetChanged();
        }
        return false;
    }

    private void setStatus(boolean b) {
        for (ImplInfo implInfo : mImplList) {
            status.put(implInfo.getId(), b);
        }
    }

    @Override
    public void onClickIcon(Object... params) {
        SimilarBean bean = (SimilarBean) params[0];
        ((OSGIServiceHost) mActivity).jumptoDetail(bean.getPackageName(), bean.getName(), bean.getIconUrl(), bean.getVersionCode(), null, true);
    }

    @Override
    public void onClickName(Object... params) {
        SimilarBean bean = (SimilarBean) params[0];
        ((OSGIServiceHost) mActivity).jumptoDetail(bean.getPackageName(), bean.getName(), bean.getIconUrl(), bean.getVersionCode(), null, true);
    }

    @Override
    public void onClickButton(Object... params) {
        ImplInfo implInfo = (ImplInfo) params[0];
        SimilarBean bean = (SimilarBean) params[1];
        ImplChangeCallback implChangeCallback = (ImplChangeCallback) params[2];
        ImplHelper.onClick(mActivity,
                implInfo,
                bean.getrDownloadUrl(),
                bean.getName(),
                bean.getIconUrl(),
                Environment.getExternalStorageDirectory() + File.separator + Constant.extenStorageDirPath + bean.getName() + ".apk",
                implInfo.getMd5(),
                implChangeCallback);
    }

    @Override
    public void dataLess(int dataNumber) {
    }

    //ListFragment和适配器传递数据
    public interface DownloadListener {
        boolean isShowCheckBox();

        int getTitleId();

        boolean getStatus(long id);
//
//        boolean getCheckBoxAnimaStatus();
//
//        void setCheckBoxAnimaStatus(boolean b);
    }
}