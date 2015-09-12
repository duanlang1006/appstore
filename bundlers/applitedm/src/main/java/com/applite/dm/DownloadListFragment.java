package com.applite.dm;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.applite.common.AppliteUtils;
import com.applite.common.Constant;
import com.applite.common.LogUtils;
import com.applite.common.VibratorUtil;
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

public class DownloadListFragment extends OSGIBaseFragment implements ListView.OnItemClickListener
        , AdapterView.OnItemLongClickListener, View.OnClickListener, SimilarAdapter.SimilarAPKDetailListener {
    final static String TAG = "applite_dm";
    private ListView mListview;
    private DownloadAdapter mAdapter;
    private boolean flagShowCheckBox = false;//长按删除的标志位
    private boolean[] status = null;//这里存放checkBox的选中状态
    private int checkedCount = 0;
    private Integer mStatusFlags = null;
    private int mTitleId;
    private ImplAgent mImplAgent;
    private List<ImplInfo> mImplList;
    private BitmapUtils mBitmapHelper;
    private Button btnCancel;
    private Button btnAllpick;
    private TextView tvShowTotal;
    private WindowManager manager1;
    private View titleBar;//长按时覆盖ActionBar的控件
    private SimilarView mSimilarView;
    private Animation animaSimilarViewOut;
    private List<SimilarBean> mSimilarDataList;
    private SimilarAdapter mSimilarAdapter;
    private HttpUtils mHttpUtils;
    private LinearLayout ll;//盛放两个按钮的布局
    private Button btnDelete = null;
    private Button btnShare = null;
    private WindowManager.LayoutParams lp_top;
    private Animation animaBt1;
    private Animation animaBt2;
    private boolean checkBoxAnima = true;
    private String temp = null;
    private ImplAgent implAgent;
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
        mListview.setOnItemClickListener(this);
        status = new boolean[mImplList.size()];
        Arrays.fill(status, false);//全部填充为false(chechbox不选中)
        checkedCount = 0;
        titleBar = inflater.inflate(R.layout.cover_actionbar, null);//这里是添加的控件
        initializeView(view);
        //这里是长按删除
        mListview.setOnItemLongClickListener(this);
        mListview.setOnScrollListener(new PauseOnScrollListener(mBitmapHelper, false, true));
        if (null != mImplList && mImplList.size() > 0) {
            mAdapter = new DownloadAdapter(mActivity, R.layout.download_list_item,
                    mImplList, mBitmapHelper, mDownloadListener);
            mAdapter.sort(IMPL_TIMESTAMP_COMPARATOR);
            mListview.setAdapter(mAdapter);
        }

        mSimilarView = (SimilarView) view.findViewById(R.id.similar_view_dm);
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
                LogUtils.d("wanghc", "更新请求成功，resulit：" + responseInfo.result);
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
                        mSimilarView.setAdapter(mSimilarAdapter);
                    }
                } catch (JSONException e) {
                    Toast.makeText(mActivity, "kong", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(HttpException e, String s) {
                LogUtils.d("wanghc", mActivity.getPackageName() + "");
                LogUtils.i(TAG, mActivity.getPackageName() + "");
            }

        });
        mSimilarView.setVisibility(View.VISIBLE);
        animaSimilarViewOut = AnimationUtils.loadAnimation(mActivity, R.anim.bottom_out);
        ll = (LinearLayout) view.findViewById(R.id.layout_button);
        btnShare = (Button) view.findViewById(R.id.btnShare);
        animaBt1 = AnimationUtils.loadAnimation(mActivity, R.anim.btn_share_in);
        btnDelete = (Button) view.findViewById(R.id.btnDelete);
        animaBt2 = AnimationUtils.loadAnimation(mActivity, R.anim.btn_delete_in);
        btnShare.setOnClickListener(this);
        btnDelete.setOnClickListener(this);

        return view;
    }

    private void setButtonStatus() {
        temp = mActivity.getResources().getString(R.string.choose_message1) + checkedCount +
                mActivity.getResources().getString(R.string.choose_message2);
        tvShowTotal.setText(temp);
        temp = null;
        if (0 == checkedCount) {
            btnAllpick.setText(R.string.allpick_btn);
            btnDelete.setEnabled(false);
            btnDelete.setTextAppearance(mActivity, R.style.DownloadListEmpty);
        } else if (status.length == checkedCount) {
            btnAllpick.setText(R.string.nonepick_btn);
            btnDelete.setEnabled(true);
            btnDelete.setTextAppearance(mActivity, R.style.DownloadOptButton);
        } else {
            btnAllpick.setText(R.string.allpick_btn);
            btnDelete.setEnabled(true);
            btnDelete.setTextAppearance(mActivity, R.style.DownloadOptButton);
        }
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
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
                    if (flagShowCheckBox) {
                        refresh(true);
                        return true;
                    }
                    return false;
                }
                return true;
            }
        });
        flagShowCheckBox = false;
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
        if (manager1 != null) {
            manager1.removeView(titleBar);
        }
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
                        true);
            }
        } else {
            status[position] = !status[position];
            checkedCount = (status[position] == false) ? checkedCount - 1 : checkedCount + 1;
            setButtonStatus();
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (!flagShowCheckBox) {
            if (titleBar.getVisibility() != View.VISIBLE) {
                flagShowCheckBox = true;
                VibratorUtil.Vibrate(mActivity, 200);   //震动200ms
                titleBar.setVisibility(View.VISIBLE);//显示titleBar
                mSimilarView.setVisibility(View.GONE);
                mSimilarView.startAnimation(animaSimilarViewOut);
                ll.setVisibility(View.VISIBLE);//底部的布局及按钮
                btnShare.setVisibility(View.VISIBLE);
                btnShare.startAnimation(animaBt1);
                btnDelete.setVisibility(View.VISIBLE);
                btnDelete.startAnimation(animaBt2);
            }
            mAdapter.notifyDataSetChanged();
        }
        return false;
    }


    @Override
    public void onClick(View v) {
        boolean flag = false;
        if (v.getId() == R.id.btnShare) {//分享(已不可点)
            flag = true;
            Toast.makeText(mActivity.getApplicationContext(), "分享功能尚未实现，敬请期待", Toast.LENGTH_SHORT).show();
        } else if (v.getId() == R.id.btnDelete) {//删除
            flag = true;
            deleteItem();
        } else if (v.getId() == R.id.select_allpick) {//全选
            if (status.length == checkedCount) {
                Arrays.fill(status, false);
                checkedCount = 0;
            } else {
                Arrays.fill(status, true);
//                checkedCount = mImplList.size();
                checkedCount = status.length;
            }
            setButtonStatus();
            mAdapter.notifyDataSetChanged();
        } else if (v.getId() == R.id.select_cancel) {//取消
            flag = true;
            Toast.makeText(mActivity.getApplicationContext(), R.string.cancel_operator, Toast.LENGTH_SHORT).show();
        }
        refresh(flag);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void initializeView(View view) {
        if (null == lp_top) {
            lp_top = new WindowManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    mActivity.getResources().getDimensionPixelSize(R.dimen.abc_action_bar_default_height_material),
                    WindowManager.LayoutParams.TYPE_APPLICATION,
                    // 设置为无焦点状态
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, // 没有边界
                    // 半透明效果
                    PixelFormat.TRANSLUCENT);
            lp_top.gravity = Gravity.TOP;
            lp_top.windowAnimations = R.style.anim_view_top;
            manager1 = (WindowManager) mActivity.getSystemService(Context.WINDOW_SERVICE);
            manager1.addView(titleBar, lp_top);

            titleBar.setVisibility(View.GONE);
            btnCancel = (Button) titleBar.findViewById(R.id.select_cancel);
            tvShowTotal = (TextView) titleBar.findViewById(R.id.total);
            btnAllpick = (Button) titleBar.findViewById(R.id.select_allpick);

            ll = (LinearLayout) view.findViewById(R.id.layout_button);
            btnShare = (Button) view.findViewById(R.id.btnShare);
            animaBt1 = AnimationUtils.loadAnimation(mActivity, R.anim.btn_share_in);
            btnDelete = (Button) view.findViewById(R.id.btnDelete);
            animaBt2 = AnimationUtils.loadAnimation(mActivity, R.anim.btn_delete_in);
        }
        btnCancel.setOnClickListener(this);
        btnAllpick.setOnClickListener(this);
        btnShare.setOnClickListener(this);
        btnDelete.setOnClickListener(this);
        mListview.setAdapter(mAdapter);

    }


    private void refresh(boolean b) {
        if (b) {
            titleBar.setVisibility(View.GONE);
            btnShare.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
            ll.setVisibility(View.GONE);
            mSimilarView.setVisibility(View.VISIBLE);
            flagShowCheckBox = false;//标志位复位
            Arrays.fill(status, false);//删除后将status复位
            mAdapter.notifyDataSetChanged();
            checkBoxAnima = true;
            checkedCount = 0;
        }
    }

    private void deleteItem() {
        for (int i = mImplList.size() - 1; i >= 0; i--) {
            if (status[i]) {
                implAgent.remove(mImplList.get(i));
            }
        }
        Toast.makeText(mActivity.getApplicationContext(), mActivity.getResources().getString(R.string.delete_message1) + checkedCount
                        + mActivity.getResources().getString(R.string.delete_message2),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void refreshDetail(SimilarBean similarBean) {
        ((OSGIServiceHost) mActivity).jumptoDetail(similarBean.getPackageName(), similarBean.getName(), similarBean.getIconUrl(), true);
    }
}