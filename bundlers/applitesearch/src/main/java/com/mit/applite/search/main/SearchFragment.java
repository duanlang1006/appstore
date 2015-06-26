package com.mit.applite.search.main;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.applite.common.AppliteUtils;
import com.applite.common.Constant;
import com.applite.common.LogUtils;
import com.mit.applite.search.adapter.HotWordAdapter;
import com.mit.applite.search.R;
import com.mit.applite.search.adapter.PreloadAdapter;
import com.mit.applite.search.adapter.SearchApkAdapter;
import com.mit.applite.search.bean.HotWordBean;
import com.mit.applite.search.bean.SearchBean;
import com.mit.applite.search.utils.KeyBoardUtils;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplListener;

import net.tsz.afinal.FinalBitmap;
import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "SearchFragment";
    private ImageButton mBackView;
    private EditText mEtView;
    private ImageButton mSearchView;
    private FinalHttp mFinalHttp;
    private LinearLayout mHotWordLL;
    private ImageView mNoNetworkIV;
    private ListView mListView;
    private List<SearchBean> mSearchApkContents = new ArrayList<SearchBean>();
    private SearchApkAdapter mAdapter;
    private Activity mActivity;
    private View rootView;
    //    private SearchFragmentClickListener mListener;
    private List<HotWordBean> mHotWordBeans = new ArrayList<HotWordBean>();
    private FinalBitmap mFinalBitmap;
    private int mChangeNumbew = 0;//在线热词换一换点击的次数
    private GridView mGridView;
    private HotWordAdapter mGvAdapter;
    private ImageView mDeleteView;
    private List<HotWordBean> mShowHotData = new ArrayList<HotWordBean>();
    private TextView mHotChangeView;

    private ListView mPreloadListView;
    private static final int POST_CLICK_SEARCH = 0;
    private static final int POST_PRELOAD_SEARCH = 1;
    private List<String> mPreloadData = new ArrayList<String>();
    private boolean isClickPreloadItem = false;
    private int mPostPreloadNumber = 0;
    private PreloadAdapter mPreloadAdapter;

    private ImplListener mImplListener = new ImplListener() {
        @Override
        public void onDownloadComplete(boolean b, ImplAgent.DownloadCompleteRsp downloadCompleteRsp) {
            for (int i = 0; i < mSearchApkContents.size(); i++) {
                if (downloadCompleteRsp.key.equals(mSearchApkContents.get(i).getmPackageName())) {
                    switch (downloadCompleteRsp.status) {
                        case Constant.STATUS_SUCCESSFUL:
                            mSearchApkContents.get(i).setmShowButtonText(AppliteUtils.getString(mContext, R.string.download_success));
                            mAdapter.notifyDataSetChanged();
                            break;
                    }
                }
            }
        }

        @Override
        public void onDownloadUpdate(boolean b, ImplAgent.DownloadUpdateRsp downloadUpdateRsp) {
            for (int i = 0; i < mSearchApkContents.size(); i++) {
                if (downloadUpdateRsp.key.equals(mSearchApkContents.get(i).getmPackageName())) {
                    String OriginalShowText = mSearchApkContents.get(i).getmShowButtonText();

                    switch (downloadUpdateRsp.status) {
                        case Constant.STATUS_PENDING:
                            mSearchApkContents.get(i).setmShowButtonText(AppliteUtils.getString(mContext, R.string.download_pending));
                            break;
                        case Constant.STATUS_RUNNING:
                            mSearchApkContents.get(i).setmShowButtonText(AppliteUtils.getString(mContext, R.string.download_running));
                            break;
                        case Constant.STATUS_PAUSED:
                            mSearchApkContents.get(i).setmShowButtonText(AppliteUtils.getString(mContext, R.string.download_paused));
                            break;
                        case Constant.STATUS_FAILED:
                            mSearchApkContents.get(i).setmShowButtonText(AppliteUtils.getString(mContext, R.string.download_failed));
                            break;
                        case Constant.STATUS_NORMAL_INSTALLING:
                            break;
                    }

                    String CurrentShowText = mSearchApkContents.get(i).getmShowButtonText();
                    if (!OriginalShowText.equals(CurrentShowText))
                        mAdapter.notifyDataSetChanged();
                    LogUtils.i(TAG, OriginalShowText + "-------" + CurrentShowText);
                }
            }
        }

        @Override
        public void onPackageAdded(boolean b, ImplAgent.PackageAddedRsp packageAddedRsp) {

        }

        @Override
        public void onPackageRemoved(boolean b, ImplAgent.PackageRemovedRsp packageRemovedRsp) {

        }

        @Override
        public void onPackageChanged(boolean b, ImplAgent.PackageChangedRsp packageChangedRsp) {

        }

        @Override
        public void onSystemInstallResult(boolean b, ImplAgent.SystemInstallResultRsp systemInstallResultRsp) {
            for (int i = 0; i < mSearchApkContents.size(); i++) {
                if (systemInstallResultRsp.key.equals(mSearchApkContents.get(i).getmPackageName())) {
                    switch (systemInstallResultRsp.result) {
                        case Constant.STATUS_PACKAGE_INVALID:
                            mSearchApkContents.get(i).setmShowButtonText(AppliteUtils.getString(mContext, R.string.package_invalid));
                            Toast.makeText(mActivity, AppliteUtils.getString(mContext, R.string.package_invalid),
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case Constant.STATUS_INSTALL_FAILED:
                            mSearchApkContents.get(i).setmShowButtonText(AppliteUtils.getString(mContext, R.string.install_failed));
                            break;
                        case Constant.STATUS_INSTALLED:
                            mSearchApkContents.get(i).setmShowButtonText(AppliteUtils.getString(mContext, R.string.start_up));
                            break;
                    }
                    mAdapter.notifyDataSetChanged();
                }
            }
        }

        @Override
        public void onSystemDeleteResult(boolean b, ImplAgent.SystemDeleteResultRsp systemDeleteResultRsp) {

        }

        @Override
        public void onFinish(boolean b, ImplAgent.ImplResponse implResponse) {
            if (implResponse instanceof ImplAgent.InstallPackageRsp) {
                for (int i = 0; i < mSearchApkContents.size(); i++) {
                    if (((ImplAgent.InstallPackageRsp) implResponse).key.equals(mSearchApkContents.get(i).getmPackageName())) {
                        mSearchApkContents.get(i).setmShowButtonText(AppliteUtils.getString(mContext, R.string.installing));
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
    };
    private Context mContext;
    private int mSearchPostPage = 0;
    private boolean isLastRow = false;
    private AbsListView.OnScrollListener mOnScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (isLastRow && scrollState == this.SCROLL_STATE_IDLE) {
                LogUtils.i(TAG, "拉到最底部");
                moreView.setVisibility(view.VISIBLE);
                mSearchPostPage = mSearchPostPage + 1;
                mHandler.sendEmptyMessage(0);
                isLastRow = false;
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                             int totalItemCount) {
//            lastItem = firstVisibleItem + visibleItemCount - 1;
            //判断是否滚到最后一行
            if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount > 0) {
                LogUtils.i(TAG, "滚到最后一行");
                isLastRow = true;
            }
        }
    };
    private View moreView;

    private boolean isToEnd = false;
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 0:
                    if (isToEnd) {
                        Toast.makeText(mActivity, "木有更多数据！", Toast.LENGTH_SHORT).show();
                        mListView.removeFooterView(moreView); //移除底部视图
                    } else {
                        //加载更多数据，这里可以使用异步加载
                        postSearch(mEtView.getText().toString(), POST_CLICK_SEARCH, 0);
                    }
                    LogUtils.i(TAG, "加载更多数据");
                    break;
                default:
                    break;
            }
        }

        ;
    };

    public SearchFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        mFinalHttp = new FinalHttp();
        mFinalBitmap = FinalBitmap.create(activity);
//        try {
//            mListener = (SearchFragmentClickListener) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString() +
//                    " must implement SearchFragmentClickListener");
//        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImplAgent.registerImplListener(mImplListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LayoutInflater mInflater = inflater;
        try {
            Context context = BundleContextFactory.getInstance().getBundleContext().getBundleContext();
            mInflater = LayoutInflater.from(context);
            mInflater = mInflater.cloneInContext(context);
            ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
            actionBar.hide();
            mContext = context;
        } catch (Exception e) {
            e.printStackTrace();
            mContext = mActivity;
        }
        if (null == mInflater) {
            mInflater = inflater;
        }

        rootView = mInflater.inflate(R.layout.fragment_search, container, false);
        moreView = mInflater.inflate(R.layout.load, null);
        initView();
        if (mHotWordBeans.size() == 0)
            postHotWord();
        return rootView;
    }

    @Override
    public void onStop() {
        super.onStop();
        KeyBoardUtils.closeKeybord(mEtView, mActivity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ImplAgent.unregisterImplListener(mImplListener);
    }

    /**
     * 初始化控件
     */
    private void initView() {
        mHotWordLL = (LinearLayout) rootView.findViewById(R.id.hot_word_ll);
        mNoNetworkIV = (ImageView) rootView.findViewById(R.id.hot_word_no_network);
        mBackView = (ImageButton) rootView.findViewById(R.id.search_back);
        mEtView = (EditText) rootView.findViewById(R.id.search_et);
        mSearchView = (ImageButton) rootView.findViewById(R.id.search_search);
        mListView = (ListView) rootView.findViewById(R.id.search_listview);
        mGridView = (GridView) rootView.findViewById(R.id.search_gv);
        mDeleteView = (ImageView) rootView.findViewById(R.id.search_delete);
        mHotChangeView = (TextView) rootView.findViewById(R.id.hot_word_change);
        mPreloadListView = (ListView) rootView.findViewById(R.id.search_preload_listview);
        mPreloadListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                isClickPreloadItem = true;
                mEtView.setText(mPreloadData.get(position));
                mPreloadListView.setVisibility(View.GONE);
                postSearch(mPreloadData.get(position), POST_CLICK_SEARCH, 0);
            }
        });

        mListView.addFooterView(moreView);
        mListView.setOnScrollListener(mOnScrollListener);

        mEtView.addTextChangedListener(mTextWatcher);
        mEtView.setFocusable(true);
        mEtView.setFocusableInTouchMode(true);
        mEtView.requestFocus();
        mEtView.requestFocusFromTouch();
        KeyBoardUtils.openKeybord(mEtView, mActivity);

        mHotChangeView.setOnClickListener(this);
        mDeleteView.setOnClickListener(this);
        mBackView.setOnClickListener(this);
        mSearchView.setOnClickListener(this);
    }

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            LogUtils.i(TAG, "输入文本之前的状态");
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            LogUtils.i(TAG, "输入文字中的状态，count是一次性输入字符数");
        }

        @Override
        public void afterTextChanged(Editable s) {
            LogUtils.i(TAG, "输入文字后的状态");
            mSearchPostPage = 0;
            if (TextUtils.isEmpty(mEtView.getText().toString())) {//空则显示在线热词
                mListView.setVisibility(View.GONE);
                isHotWordLayoutVisibility(View.VISIBLE);
            } else {
                isHotWordLayoutVisibility(View.GONE);
                mListView.setVisibility(View.GONE);
                if (!isClickPreloadItem) {
                    mPostPreloadNumber = mPostPreloadNumber + 1;
                    postSearch(mEtView.getText().toString(), POST_PRELOAD_SEARCH, mPostPreloadNumber);
                }
            }
            isClickPreloadItem = false;
        }
    };

    /**
     * 判断在线热词Layout是否隐藏
     *
     * @param i
     */
    private void isHotWordLayoutVisibility(int i) {
        if (i == View.VISIBLE) {
            if (mHotWordBeans.size() != 0) {
                if (mListView.getVisibility() == View.GONE && mPreloadListView.getVisibility() == View.GONE)
                    mHotWordLL.setVisibility(View.VISIBLE);
            } else {
                mHotWordLL.setVisibility(View.GONE);
                postHotWord();
            }
        } else if (i == View.GONE) {
            mHotWordLL.setVisibility(View.GONE);
        } else if (i == View.INVISIBLE) {
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search_back:
//                mListener.onBack();
                break;
            case R.id.search_delete:
                mPreloadListView.setVisibility(View.GONE);
                mListView.setVisibility(View.GONE);
                mEtView.setText(null);
                break;
            case R.id.search_search:
                isToEnd = false;
                mPreloadListView.setVisibility(View.GONE);
                if (TextUtils.isEmpty(mEtView.getText().toString())) {
                    Toast.makeText(mActivity, AppliteUtils.getString(mContext, R.string.srarch_content_no_null),
                            Toast.LENGTH_SHORT).show();
                } else {
                    KeyBoardUtils.closeKeybord(mEtView, mActivity);
                    if (null != mSearchApkContents) {
                        mSearchApkContents.clear();
                    }
                    postSearch(mEtView.getText().toString(), POST_CLICK_SEARCH, 0);
                    isHotWordLayoutVisibility(View.GONE);
                }
                break;
            case R.id.hot_word_change:
                mChangeNumbew = mChangeNumbew + 1;
                mShowHotData.clear();
                if (mHotWordBeans.size() / 8 <= mChangeNumbew)
                    mChangeNumbew = 0;
                setHotWordShowData(mChangeNumbew);
                break;
        }
    }

    /**
     * 搜索网络请求
     *
     * @param name
     * @param type
     * @param number 点击搜索随便填
     */
    public void postSearch(String name, final int type, final int number) {
        AjaxParams params = new AjaxParams();
        params.put("appkey", AppliteUtils.getMitMetaDataValue(mActivity, Constant.META_DATA_MIT));
        params.put("packagename", mActivity.getPackageName());
        params.put("app", "applite");
        params.put("type", "search");
        params.put("key", name);
        if (type == POST_CLICK_SEARCH) {
            params.put("page", mSearchPostPage + "");
        }
//        params.put("position", position + "");
        mFinalHttp.post(Constant.URL, params, new AjaxCallBack<Object>() {
            @Override
            public void onSuccess(Object o) {
                super.onSuccess(o);
                String result = (String) o;
                LogUtils.i(TAG, "搜索网络请求成功，result:" + result);
                if (type == POST_CLICK_SEARCH) {
                    setSearchData(result);
                } else if (type == POST_PRELOAD_SEARCH) {
                    if (number == mPostPreloadNumber)
                        setPreloadData(result);
                }
            }

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                super.onFailure(t, errorNo, strMsg);
                LogUtils.e(TAG, "搜索网络请求失败，strMsg:" + strMsg);
            }
        });
    }

    /**
     * 显示预加载的数据
     *
     * @param result
     */
    private void setPreloadData(String result) {
        if (!mPreloadData.isEmpty())
            mPreloadData.clear();
        try {
            JSONObject object = new JSONObject(result);
            int app_key = object.getInt("app_key");
            String json = object.getString("search_info");
            JSONArray array = new JSONArray(json);
            if (array.length() == 0) {

            } else {
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = new JSONObject(array.get(i).toString());
                    String name = obj.getString("name");
                    mPreloadData.add(name);
                }
                if (mListView.getVisibility() == View.GONE) {
                    mPreloadListView.setVisibility(View.VISIBLE);
                }
                if (null == mPreloadAdapter) {
                    mPreloadAdapter = new PreloadAdapter(mActivity, mPreloadData);
                    mPreloadListView.setAdapter(mPreloadAdapter);
                } else {
                    mPreloadAdapter.notifyDataSetChanged();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "搜索预加载JSON解析失败");
        }
    }

    /**
     * 显示搜索得到的数据
     *
     * @param data
     */
    private void setSearchData(String data) {
        if (!mSearchApkContents.isEmpty() && mSearchPostPage == 0)
            mSearchApkContents.clear();
        SearchBean bean = null;
        try {
            JSONObject object = new JSONObject(data);
            int app_key = object.getInt("app_key");
            String json = object.getString("search_info");
            isToEnd = object.getBoolean("istoend");
            if (TextUtils.isEmpty(json)) {

            } else {
                JSONArray array = new JSONArray(json);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = new JSONObject(array.get(i).toString());
                    bean = new SearchBean();
//                    bean.set_id(i);
                    bean.setmPackageName(obj.getString("packageName"));
                    bean.setmName(obj.getString("name"));
                    bean.setmImgUrl(obj.getString("iconUrl"));
                    bean.setmApkSize(obj.getString("apkSize"));
                    bean.setmDownloadNumber(obj.getString("downloadTimes"));
                    bean.setmXing(obj.getString("rating"));
                    bean.setmVersionName(obj.getString("versionName"));
                    bean.setmVersionCode(obj.getInt("versionCode"));
                    bean.setmDownloadUrl(obj.getString("rDownloadUrl"));

                    bean.setmShowButtonText(AppliteUtils.getString(mContext, R.string.install));
                    mSearchApkContents.add(bean);
                    ImplAgent.queryDownload(mActivity, bean.getmPackageName());
                }
                mPreloadListView.setVisibility(View.GONE);
                mListView.setVisibility(View.VISIBLE);
                moreView.setVisibility(View.GONE);
                if (null == mAdapter) {
                    mAdapter = new SearchApkAdapter(mActivity, mSearchApkContents);
                    mListView.setAdapter(mAdapter);
                } else {
                    mAdapter.notifyDataSetChanged();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "搜索JSON解析失败");
        }
    }

    /**
     * 在线热词网络请求
     */
    private void postHotWord() {
        AjaxParams params = new AjaxParams();
        params.put("appkey", AppliteUtils.getMitMetaDataValue(mActivity, Constant.META_DATA_MIT));
        params.put("packagename", mActivity.getPackageName());
        params.put("app", "applite");
        params.put("type", "hot_word");
        mFinalHttp.post(Constant.URL, params, new AjaxCallBack<Object>() {
            @Override
            public void onSuccess(Object o) {
                super.onSuccess(o);
                String reuslt = (String) o;
                LogUtils.i(TAG, "在线热词请求成功，reuslt:" + reuslt);
                resolve(reuslt);
            }

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                super.onFailure(t, errorNo, strMsg);
                LogUtils.e(TAG, "在线热词请求失败，strMsg:" + strMsg);
            }
        });
    }

    /**
     * 解析返回的在线热词JSON
     *
     * @param data
     */
    private void resolve(String data) {
        if (!mHotWordBeans.isEmpty())
            mHotWordBeans.clear();
        HotWordBean bean = null;
        try {
            JSONObject obj = new JSONObject(data);
            int app_key = obj.getInt("app_key");
            String s = obj.getString("hotword_info");
            JSONArray json = new JSONArray(s);
            for (int i = 0; i < json.length(); i++) {
                JSONObject object = new JSONObject(json.get(i).toString());
                bean = new HotWordBean();
                bean.set_id(i);
                bean.setmName(object.getString("name"));
                bean.setmPackageName(object.getString("packageName"));
                bean.setmImgUrl(object.getString("iconUrl"));
//                bean.setmType(object.getString(""));
                bean.setmType(1 + "");
                mHotWordBeans.add(bean);
            }
            setHotWordShowData(0);
            LogUtils.i(TAG, "在线热词返回的数量=" + mHotWordBeans.size());
        } catch (JSONException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "在线热词JSON解析失败");
        }
    }

    /**
     * 设置在线热词显示的数据
     */
    private void setHotWordShowData(int position) {
        if (mHotWordBeans.size() > 8) {
            for (int i = 0 + 8 * position; i < 8 + 8 * position; i++) {
                mShowHotData.add(mHotWordBeans.get(i));
            }
        } else {
            for (int i = 0; i < mHotWordBeans.size(); i++) {
                mShowHotData.add(mHotWordBeans.get(i));
            }
        }
        mGvAdapter = new HotWordAdapter(mActivity, mShowHotData);
        mGridView.setAdapter(mGvAdapter);
        isHotWordLayoutVisibility(View.VISIBLE);
    }

}
