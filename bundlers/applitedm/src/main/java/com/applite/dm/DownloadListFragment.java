package com.applite.dm;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.applite.common.AppliteUtils;
import com.applite.common.Constant;
import com.applite.common.DefaultValue;
import com.applite.common.LogUtils;
import com.applite.common.VibratorUtil;
import com.applite.sharedpreferences.AppliteSPUtils;
import com.applite.similarview.SimilarAdapter;
import com.applite.similarview.SimilarBean;
import com.applite.similarview.SimilarView;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.bitmap.PauseOnScrollListener;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplInfo;
import com.mit.impl.ImplLog;
import com.osgi.extra.OSGIBaseFragment;
import com.osgi.extra.OSGIServiceHost;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class DownloadListFragment extends OSGIBaseFragment implements ListView.OnItemClickListener,
        AdapterView.OnItemLongClickListener, SimilarAdapter.SimilarAPKDetailListener {
    final static String TAG = "applite_dm";
    private ListView mListview;
    private DownloadAdapter mAdapter;
    private boolean flagShowCheckBox = false;//长按删除的标志位/
    private boolean[] status = null;//这里存放checkBox的选中状态/length
    private int checkedCount = 0;///
    private int mStatusFlags;
    private int mTitleId;
    private ImplAgent mImplAgent;
    private List<ImplInfo> mImplList;
    private BitmapUtils mBitmapHelper;

    private SimilarView mSimilarView;
    private List<SimilarBean> mSimilarDataList;
    private SimilarAdapter mSimilarAdapter;
    private HttpUtils mHttpUtils;

    private boolean checkBoxAnima = true;

    private ImplAgent implAgent;

    private String COUNT = "count";
    private String FLAG = "flag";
    private String LENGTH = "length";
    private String STATUS = "status";
    private String DELETE_BTN_PRESSED = "deleteBtnPressed";

    private DownloadListener mDownloadListener = new DownloadListener() {
        @Override
        public boolean getFlag1() {
            return flagShowCheckBox;
        }

        @Override
        public boolean getStatus(int position) {
            if (position < 0 || position >= status.length) {
                return false;
            }
            return status[position];
        }

        @Override
        public int getTitleId() {
            return mTitleId;
        }

        @Override
        public boolean getFlag2() {
            return checkBoxAnima;
        }

        @Override
        public void setFlag2(boolean b) {
            checkBoxAnima = b;
        }
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
            if (!(boolean) AppliteSPUtils.get(mActivity, FLAG, DefaultValue.defaultBoolean)) {//由删除状态返回显示状态
                refresh();
            }
            if (-1 != (int) AppliteSPUtils.get(mActivity, STATUS, DefaultValue.defaultInt)) {//全选按钮状态
                switch ((int) AppliteSPUtils.get(mActivity, STATUS, DefaultValue.defaultInt)) {
                    case 1:
                        Arrays.fill(status, true);//全选
                        break;
                    case 0:
                        Arrays.fill(status, false);//全不选
                        break;
                    default:
                        break;
                }
                AppliteSPUtils.put(mActivity, STATUS, -1);
                mAdapter.notifyDataSetChanged();
            }
            if ((boolean) AppliteSPUtils.get(mActivity, DELETE_BTN_PRESSED, false)) {//删除按钮按下
                deleteItem();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        implAgent = ImplAgent.getInstance(mActivity.getApplicationContext());
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ImplLog.d(TAG, "onCreateView," + this);
        LayoutInflater mInflater = inflater;
        View view = mInflater.inflate(R.layout.fragment_download_list, container, false);
        mListview = (ListView) view.findViewById(android.R.id.list);
        mListview.setEmptyView(view.findViewById(R.id.empty));
        initSimilarView(view);
        if (mTitleId == R.string.dm_downloaded) {
            mListview.addFooterView(mSimilarView);
        }
        mListview.setOnItemClickListener(this);
        status = new boolean[mImplList.size()];
        Arrays.fill(status, false);//全部填充为false(chechbox不选中)
        checkedCount = 0;
        AppliteSPUtils.put(mActivity, COUNT, checkedCount);
        mListview.setAdapter(mAdapter);

        //这里是长按删除
        mListview.setOnItemLongClickListener(this);
        mListview.setOnScrollListener(new PauseOnScrollListener(mBitmapHelper, false, true));
        if (null != mImplList && mImplList.size() > 0) {
            mAdapter = new DownloadAdapter(mActivity, R.layout.download_list_item,
                    mImplList, mBitmapHelper, mDownloadListener);
            mAdapter.sort(IMPL_TIMESTAMP_COMPARATOR);
            mListview.setAdapter(mAdapter);
            AppliteSPUtils.put(mActivity, LENGTH, status.length);
            AppliteSPUtils.put(mActivity, FLAG, flagShowCheckBox);
        }
        return view;
    }

    private void initSimilarView(View view) {
        mSimilarView = (SimilarView) view.inflate(mActivity, R.layout.similar_view, null);
        TextView t = (TextView) mSimilarView.findViewById(R.id.similar_title);
//        t.setText("大家还下载了");
        t.setVisibility(View.GONE);
        mSimilarDataList = new ArrayList<>();
        mHttpUtils = new HttpUtils();
        RequestParams params = new RequestParams();
        params.addBodyParameter("appkey", AppliteUtils.getMitMetaDataValue(mActivity, Constant.META_DATA_MIT));
        params.addBodyParameter("packagename", mActivity.getPackageName());
        params.addBodyParameter("type", "update_management");
        params.addBodyParameter("protocol_version", Constant.PROTOCOL_VERSION);
        params.addBodyParameter("update_info", AppliteUtils.getAllApkData(mActivity));
        mHttpUtils.send(HttpRequest.HttpMethod.POST, Constant.URL, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                LogUtils.i(TAG, "更新请求成功，resulit：" + responseInfo.result);
                try {
                    JSONObject object = new JSONObject(responseInfo.result);
                    String similar_info = object.getString("similar_info");
                    if (!mSimilarDataList.isEmpty())
                        mSimilarDataList.clear();
                    JSONArray similar_json = new JSONArray(similar_info);
                    SimilarBean similarBean = null;
                    if (similar_json.length() != 0 && similar_json != null) {
                        for (int i = 0; i < 4; i++) {
                            similarBean = new SimilarBean();
                            JSONObject obj = new JSONObject(similar_json.get(i).toString());
                            similarBean.setName(obj.getString("name"));
                            similarBean.setPackageName(obj.getString("packageName"));
                            similarBean.setIconUrl(obj.getString("iconUrl"));
                            similarBean.setrDownloadUrl(obj.getString("rDownloadUrl"));
                            similarBean.setVersionCode(obj.getInt("versionCode"));
                            mSimilarDataList.add(similarBean);
                        }
                        if (null == mSimilarAdapter) {
                            mSimilarAdapter = new DownloadSimilarAdapter(mActivity);
                            mSimilarAdapter.setData(mSimilarDataList, DownloadListFragment.this);
                            mSimilarView.setAdapter(mSimilarAdapter);
                        } else {
                            mSimilarAdapter.setData(mSimilarDataList, DownloadListFragment.this);
                            mSimilarAdapter.notifyDataSetChanged();
                        }
                    }
                } catch (JSONException e) {
                    Toast.makeText(mActivity, "kong", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(HttpException e, String s) {
                LogUtils.i(TAG, mActivity.getPackageName() + "");
            }

        });
        mSimilarView.setVisibility(View.VISIBLE);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle params = getArguments();
        if (null != params) {
            mStatusFlags = params.getInt("statusFilter");
            mTitleId = params.getInt("titleId");
        }
        mImplAgent = ImplAgent.getInstance(activity.getApplicationContext());
        mImplList = mImplAgent.getDownloadInfoList(mStatusFlags);
        mBitmapHelper = new BitmapUtils(mActivity.getApplicationContext());
        ImplLog.d(DownloadListFragment.TAG, "onAttach," + this + "," + mImplList.size());
    }

    @Override
    public void onResume() {
        super.onResume();
        AppliteSPUtils.registerChangeListener(mActivity, mListListener);
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
                    if (flagShowCheckBox) {
                        refresh();
                        return true;
                    }
                    return false;
                }
                return true;
            }
        });
        flagShowCheckBox = false;
        AppliteSPUtils.put(mActivity, FLAG, flagShowCheckBox);

    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ImplLog.d(DownloadListFragment.TAG, "onDetach," + this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ImplLog.d(DownloadListFragment.TAG, "onDestroyView," + this);
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
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
        if (!flagShowCheckBox) {
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
            status[position] = !status[position];
            checkedCount = (status[position] == false) ? checkedCount - 1 : checkedCount + 1;
            AppliteSPUtils.put(mActivity, COUNT, checkedCount);
            //这里要改变外面fragment中删除按钮和全选按钮状态
            //！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (!flagShowCheckBox) {
            if (!flagShowCheckBox) {
                flagShowCheckBox = true;
                AppliteSPUtils.put(mActivity, FLAG, flagShowCheckBox);
                VibratorUtil.Vibrate(mActivity, 200);   //震动200ms
                mSimilarView.setVisibility(View.GONE);
                mSimilarView.setPadding(0, -mSimilarView.getHeight(), 0, 0);
            }
            mAdapter.notifyDataSetChanged();
        }
        return false;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }


    private void refresh() {
        mSimilarView.setVisibility(View.VISIBLE);
        mSimilarView.setPadding(0, 0, 0, 0);
        flagShowCheckBox = false;//标志位复位
        AppliteSPUtils.put(mActivity, FLAG, flagShowCheckBox);
        Arrays.fill(status, false);//status数组复位
        mAdapter.notifyDataSetChanged();
        checkBoxAnima = true;
        checkedCount = 0;
        AppliteSPUtils.put(mActivity, COUNT, checkedCount);
        AppliteSPUtils.put(mActivity, DELETE_BTN_PRESSED, false);
    }

    private void deleteItem() {
        for (int i = mImplList.size() - 1; i >= 0; i--) {
            LogUtils.d("wanghc", i + "___" + status[i] + "");
            if (status[i]) {
                implAgent.remove(mImplList.get(i));
            }
        }
        Toast.makeText(mActivity.getApplicationContext(), mActivity.getResources().getString(R.string.delete_message1) + checkedCount
                        + mActivity.getResources().getString(R.string.delete_message2),
                Toast.LENGTH_LONG).show();
        refresh();
    }

    @Override
    public void refreshDetail(SimilarBean similarBean) {
        ((OSGIServiceHost) mActivity).jumptoDetail(similarBean.getPackageName(), similarBean.getName(), similarBean.getIconUrl(),similarBean.getVersionCode(), null,true);
    }
}