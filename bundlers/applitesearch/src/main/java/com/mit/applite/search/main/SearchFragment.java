package com.mit.applite.search.main;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.applite.common.AppliteUtils;
import com.applite.common.Constant;
import com.applite.common.LogUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.mit.applite.search.adapter.HotWordAdapter;
import com.mit.applite.search.R;
import com.mit.applite.search.adapter.PreloadAdapter;
import com.mit.applite.search.adapter.SearchApkAdapter;
import com.mit.applite.search.bean.HotWordBean;
import com.mit.applite.search.bean.SearchBean;
import com.mit.applite.search.utils.KeyBoardUtils;
import com.mit.applite.search.utils.SearchUtils;
import com.mit.mitupdatesdk.MitMobclickAgent;
import com.osgi.extra.OSGIBaseFragment;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends OSGIBaseFragment implements View.OnClickListener, SearchApkAdapter.UpdateInatsllButtonText, HotWordAdapter.ClickHotWordItemPostlistener {

    private static final String TAG = "SearchFragment";
    private ImageView mBackView;
    private EditText mEtView;
    private ImageView mSearchView;
    private LinearLayout mHotWordLL;
    private ListView mListView;
    private List<SearchBean> mSearchApkContents = new ArrayList<SearchBean>();
    private SearchApkAdapter mAdapter;
    private View rootView;

    //热词相关
    private List<HotWordBean> mHotWordBeans = new ArrayList<HotWordBean>();
    private int mChangeNumbew = 1;//在线热词换一换点击的次数
    private GridView mGridView;
    private HotWordAdapter mGvAdapter;
    private int mEndPageNumber;//最后一页热词的个数
    private int mHotWordPage;//热词的页数

    private ImageView mDeleteView;
    private List<HotWordBean> mShowHotData = new ArrayList<HotWordBean>();
    private TextView mHotChangeView;

    private ListView mPreloadListView;
    private List<SearchBean> mPreloadData = new ArrayList<SearchBean>();
    private boolean isShowPreload = true;//是否显示预加载
    private int mPostPreloadNumber = 0;
    private PreloadAdapter mPreloadAdapter;
    private int mSearchPostPage = 0;
    private boolean isLastRow = false;
    private View moreView;//搜索ListView尾部布局
    private boolean ISTOEND = false;//服务器数据是否到底
    private String mSearchText = "";//当前搜索的关键字
    private boolean ISPOSTSEARCH = true;//上拉加载是否可以请求服务器
    private int mShowHotWordNumber = 9;

    private Button refresh;
    private RelativeLayout no_network;

    private int HINT_UPDATE_TIME = 2000;
    private int HINT_SHOW_NUMBER = 0;

    private Runnable mNotifyRunnable = new Runnable() {
        @Override
        public void run() {
            mAdapter.notifyDataSetChanged();
        }
    };

    private AbsListView.OnScrollListener mOnScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (isLastRow && scrollState == this.SCROLL_STATE_IDLE) {
                LogUtils.i(TAG, "拉到最底部");
                if (mListView.getFooterViewsCount() == 0)
                    mListView.addFooterView(moreView);

                if (ISTOEND) {
                    mMoreProgressBar.setVisibility(View.GONE);
                    mMoreText.setText(AppliteUtils.getString(mActivity, R.string.no_data));
//                    Toast.makeText(mActivity, "木有更多数据！", Toast.LENGTH_SHORT).show();
//                    mListView.removeFooterView(moreView); //移除底部视图
                } else {
                    mMoreProgressBar.setVisibility(View.VISIBLE);
                    mMoreText.setText(AppliteUtils.getString(mActivity, R.string.loading));
                    //加载更多数据，这里可以使用异步加载
                    if (ISPOSTSEARCH) {
                        postSearch(mEtView.getText().toString());
                        ISPOSTSEARCH = false;
                    }
                }
                LogUtils.i(TAG, "加载更多数据");

                isLastRow = false;
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                             int totalItemCount) {
            //判断是否滚到最后一行
            if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount > 0) {
                LogUtils.i(TAG, "滚到最后一行");
                isLastRow = true;
            }
        }
    };
    private TextView mMoreText;
    private ProgressBar mMoreProgressBar;
    private LayoutInflater mInflater;
    private String mEtViewText;//页面隐藏时mEtView里面的字
    private HttpUtils mHttpUtils;
    private ViewGroup customView;
    private LinearLayout mLoadingLayout;
    private ImageView mLoadingImgView;
    private Animation LoadingAnimation;
    private String[] mHint;
    private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {

        @Override
        public void run() {
            mEtView.setHint(mHint[HINT_SHOW_NUMBER]);
            mHandler.postDelayed(this, HINT_UPDATE_TIME);
            if (HINT_SHOW_NUMBER < mHint.length - 1) {
                HINT_SHOW_NUMBER = HINT_SHOW_NUMBER + 1;
            } else {
                HINT_SHOW_NUMBER = 0;
            }
        }
    };

    public SearchFragment() {
        super();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mHttpUtils = new HttpUtils();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mInflater = inflater;
        rootView = mInflater.inflate(R.layout.fragment_search, container, false);
        moreView = mInflater.inflate(R.layout.load, null);
        initView();
        if (mHotWordBeans.size() == 0)
            postHotWord();

        return rootView;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            LogUtils.i(TAG, "隐藏搜索页面");
            mEtViewText = mEtView.getText().toString();
            closeKeybord();
        } else {
            LogUtils.i(TAG, "显示搜索页面");
            if (mListView.getVisibility() == View.GONE) {
                LogUtils.i(TAG, "设置输入框焦点");
                mEtView.setFocusable(true);
                mEtView.setFocusableInTouchMode(true);
                mEtView.requestFocus();
                KeyBoardUtils.openKeybord(mEtView, mActivity);
            }
            initActionBar();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

//        getView().setFocusableInTouchMode(true);
//        getView().requestFocus();
//        getView().setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
//                    // handle back button
//                    getFragmentManager().popBackStackImmediate();
//                    return true;
//                }
//                return false;
//            }
//        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (android.R.id.home == item.getItemId()) {
            closeKeybord();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
        closeKeybord();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void initActionBar() {
        try {
            if (null == customView)
                customView = (ViewGroup) mInflater.inflate(R.layout.actionbar_search, null);
            mBackView = (ImageView) customView.findViewById(R.id.search_back);
            mBackView.setOnClickListener(this);
            mEtView = (EditText) customView.findViewById(R.id.search_et);
            mEtView.setFocusable(true);
            mEtView.setFocusableInTouchMode(true);
            mEtView.requestFocus();
            KeyBoardUtils.openKeybord(mEtView, mActivity);
            if (null != mEtViewText) {
                isShowPreload = false;
                mEtView.setText(mEtViewText);
            }
            mSearchView = (ImageView) customView.findViewById(R.id.search_search);
            mSearchView.setOnClickListener(this);
            mDeleteView = (ImageView) customView.findViewById(R.id.search_delete);
            mDeleteView.setOnClickListener(this);

            ActionBar actionBar = ((ActionBarActivity) mActivity).getSupportActionBar();
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setCustomView(customView);
            actionBar.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化控件
     */
    private void initView() {
        initActionBar();

        //加载中
        mLoadingLayout = (LinearLayout) rootView.findViewById(R.id.search_loading_layout);
        mLoadingImgView = (ImageView) rootView.findViewById(R.id.search_loading_img);
        //旋转动画
        LoadingAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.loading);
        LinearInterpolator lin = new LinearInterpolator();
        LoadingAnimation.setInterpolator(lin);
        mLoadingImgView.startAnimation(LoadingAnimation);

        //上拉加载
        mMoreText = (TextView) moreView.findViewById(R.id.loadmore_text);
        mMoreProgressBar = (ProgressBar) moreView.findViewById(R.id.load_progressbar);

        mHotWordLL = (LinearLayout) rootView.findViewById(R.id.hot_word_ll);
        mListView = (ListView) rootView.findViewById(R.id.search_listview);
        mGridView = (GridView) rootView.findViewById(R.id.search_gv);
        mHotChangeView = (TextView) rootView.findViewById(R.id.hot_word_change);
        mPreloadListView = (ListView) rootView.findViewById(R.id.search_preload_listview);

        refresh = (Button) rootView.findViewById(R.id.refresh_btn);
        no_network = (RelativeLayout) rootView.findViewById(R.id.no_network);

        mPreloadListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mPreloadListView.setVisibility(View.GONE);
                mEtViewModifyPostSearch(mPreloadData.get(position).getmName(), false);
            }
        });

        mListView.setSelected(true);
        mListView.setOnScrollListener(mOnScrollListener);

        mEtView.addTextChangedListener(mTextWatcher);

        mHotChangeView.setOnClickListener(this);
        refresh.setOnClickListener(this);
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
            ISPOSTSEARCH = true;
            if (TextUtils.isEmpty(mEtView.getText().toString())) {
                isHotWordLayoutVisibility(View.VISIBLE);
                no_network.setVisibility(View.GONE);
                mListView.setVisibility(View.GONE);
                mPreloadListView.setVisibility(View.GONE);
            } else {
                no_network.setVisibility(View.GONE);
                isHotWordLayoutVisibility(View.GONE);
                if (isShowPreload) {//是否postPreload请求
                    mListView.setVisibility(View.GONE);
                    mPostPreloadNumber = mPostPreloadNumber + 1;
                    postPreload(mEtView.getText().toString(), mPostPreloadNumber);
                }
            }
            isShowPreload = true;
        }
    };

    /**
     * 判断在线热词Layout是否隐藏
     *
     * @param i
     */
    private void isHotWordLayoutVisibility(int i) {
        if (i == View.VISIBLE) {
            if (!mHotWordBeans.isEmpty()) {
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
        if (v.getId() == R.id.search_back) {
            getFragmentManager().popBackStack();
        } else if (v.getId() == R.id.search_delete) {
            mPreloadListView.setVisibility(View.GONE);
            mListView.setVisibility(View.GONE);
            mEtView.setText(null);
            isHotWordLayoutVisibility(View.VISIBLE);
        } else if (v.getId() == R.id.search_search) {
            MitMobclickAgent.onEvent(mActivity, "clickSearch");
            no_network.setVisibility(View.GONE);
            mPreloadListView.setVisibility(View.GONE);
            if (TextUtils.isEmpty(mEtView.getText())) {
                if (TextUtils.isEmpty(mEtView.getHint())) {
                    Toast.makeText(mActivity, AppliteUtils.getString(mActivity, R.string.srarch_content_no_null),
                            Toast.LENGTH_SHORT).show();
                } else {
                    MitMobclickAgent.onEvent(mActivity, "searchHint");
                    mEtViewModifyPostSearch(mEtView.getHint().toString(), false);
                }
            } else {
                mListView.setSelection(0);
                if (mSearchText.equals(mEtView.getText().toString())) {
                    mListView.setVisibility(View.VISIBLE);
                } else {
                    mEtViewModifyPostSearch(null, true);
                }
            }
        } else if (v.getId() == R.id.hot_word_change) {
            MitMobclickAgent.onEvent(mActivity, "clickHotWordChange");
            if (mChangeNumbew >= mHotWordPage)
                mChangeNumbew = 0;
            mShowHotData.clear();
            setHotWordShowData(mChangeNumbew);
            mChangeNumbew = mChangeNumbew + 1;
        } else if (v.getId() == R.id.refresh_btn) {
            no_network.setVisibility(View.GONE);
            postSearch(mEtView.getText().toString());
        } else if (v.getId() == R.id.search_et){
            LogUtils.i("duanlang", "search_et");
        }
    }

    /**
     * 搜索框的字 改变后的搜索请求
     */
    private void mEtViewModifyPostSearch(String searchName, boolean showPreload) {
        isShowPreload = showPreload;
        if (!showPreload) {
            closeKeybord();
            mEtView.setText(searchName);
            mEtView.setSelection(searchName.length());
        }
        if (ISPOSTSEARCH) {
            ISTOEND = false;
            ISPOSTSEARCH = false;
            mSearchPostPage = 0;
            postSearch(mEtView.getText().toString());
        }
    }

    public void closeKeybord() {
        KeyBoardUtils.closeKeybord(mEtView, mActivity);
    }

    /**
     * 搜索网络请求
     *
     * @param name
     */
    public void postSearch(String name) {
        isHotWordLayoutVisibility(View.GONE);
        if (mSearchPostPage == 0)
            mLoadingLayout.setVisibility(View.VISIBLE);

        if (SearchUtils.isLetter(name)) {
            name = AppliteUtils.SplitLetter(name);
        }
        LogUtils.i(TAG, "name:" + name);
        RequestParams params = new RequestParams();
        params.addBodyParameter("appkey", AppliteUtils.getMitMetaDataValue(mActivity, Constant.META_DATA_MIT));
        params.addBodyParameter("packagename", mActivity.getPackageName());
        params.addBodyParameter("type", "search");
        params.addBodyParameter("key", name);
        params.addBodyParameter("page", mSearchPostPage + "");
        final String finalName = name;
        mHttpUtils.send(HttpRequest.HttpMethod.POST, Constant.URL, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                mLoadingLayout.setVisibility(View.GONE);
                LogUtils.i(TAG, "搜索网络请求成功，result:" + responseInfo.result);
                setSearchData(responseInfo.result);

                mSearchText = finalName;
                ISPOSTSEARCH = true;//请求结束后，才可以继续请求
                mSearchPostPage = mSearchPostPage + 1;//请求成功后，请求的页数加1
            }

            @Override
            public void onFailure(HttpException e, String s) {
                mLoadingLayout.setVisibility(View.GONE);
                if (mListView.getVisibility() == View.GONE)
                    no_network.setVisibility(View.VISIBLE);
                ISPOSTSEARCH = true;
                mListView.removeFooterView(moreView);
                if (null != mAdapter)
                    mActivity.runOnUiThread(mNotifyRunnable);

                Toast.makeText(mActivity, AppliteUtils.getString(mActivity, R.string.post_failure),
                        Toast.LENGTH_SHORT).show();
                LogUtils.e(TAG, "搜索网络请求失败:" + s);
            }
        });
    }

    /**
     * 显示搜索得到的数据
     *
     * @param data
     */
    private void setSearchData(String data) {
        if (!mSearchApkContents.isEmpty() && mSearchPostPage == 0) {
            mSearchApkContents.clear();
            if (null != mAdapter) {
                mAdapter.notifyDataSetChanged();
            }
        }
        SearchBean bean = null;
        try {
            JSONObject object = new JSONObject(data);
            int app_key = object.getInt("app_key");
            String json = object.getString("search_info");
            ISTOEND = object.getBoolean("istoend");
            if (!TextUtils.isEmpty(json)) {
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

                    mSearchApkContents.add(bean);
                }

                mPreloadListView.setVisibility(View.GONE);
                isHotWordLayoutVisibility(View.GONE);
                mListView.setVisibility(View.VISIBLE);
                mListView.removeFooterView(moreView);

                if (null == mAdapter) {
                    mAdapter = new SearchApkAdapter(mActivity, mSearchApkContents, this);
                    mListView.setAdapter(mAdapter);
                } else {
                    mAdapter.notifyDataSetChanged();
                }
                if (mSearchPostPage == 0) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mListView.setSelection(0);
                        }
                    }, 200);//ListView设置显示条目位置，需要延时一会。因为ListView没有显示之前，设置不了显示位置
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "搜索JSON解析失败");
        }
    }

    /**
     * 预加载网络请求
     *
     * @param name
     * @param number 点击搜索随便填
     */
    public void postPreload(String name, final int number) {
        if (SearchUtils.isLetter(name)) {
            name = AppliteUtils.SplitLetter(name);
        }
        LogUtils.i(TAG, "name:" + name);
        RequestParams params = new RequestParams();
        params.addBodyParameter("appkey", AppliteUtils.getMitMetaDataValue(mActivity, Constant.META_DATA_MIT));
        params.addBodyParameter("packagename", mActivity.getPackageName());
        params.addBodyParameter("type", "search");
        params.addBodyParameter("protocol_version", "1.0");
        params.addBodyParameter("key", name);
        mHttpUtils.send(HttpRequest.HttpMethod.POST, Constant.URL, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                LogUtils.i(TAG, "预加载网络请求成功，result:" + responseInfo.result);
                if (number == mPostPreloadNumber)
                    setPreloadData(responseInfo.result);
            }

            @Override
            public void onFailure(HttpException e, String s) {
                LogUtils.e(TAG, "预加载网络请求失败:" + s);
            }
        });
    }

    /**
     * 显示预加载的数据
     *
     * @param result
     */
    private void setPreloadData(String result) {
        if (!mPreloadData.isEmpty()) {
            mPreloadData.clear();
            if (null != mPreloadAdapter) {
                mPreloadAdapter.notifyDataSetChanged();
            }
        }
        SearchBean bean = null;
        try {
            JSONObject object = new JSONObject(result);
            int app_key = object.getInt("app_key");
            String json = object.getString("search_info");
            JSONArray array = new JSONArray(json);
            if (array.length() != 0) {
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = new JSONObject(array.get(i).toString());
                    bean = new SearchBean();
                    bean.setmPackageName(obj.getString("packageName"));
                    bean.setmName(obj.getString("name"));
                    bean.setmImgUrl(obj.getString("iconUrl"));
                    bean.setmApkSize(obj.getString("apkSize"));
                    bean.setmDownloadNumber(obj.getString("downloadTimes"));
                    bean.setmXing(obj.getString("rating"));
                    bean.setmVersionName(obj.getString("versionName"));
                    bean.setmVersionCode(obj.getInt("versionCode"));
                    bean.setmDownloadUrl(obj.getString("rDownloadUrl"));
                    mPreloadData.add(bean);
                }

                if (!TextUtils.isEmpty(mEtView.getText().toString())) {
                    isHotWordLayoutVisibility(View.GONE);
                    if (!mEtView.getText().toString().equals(mSearchText)) {
                        mListView.setVisibility(View.GONE);
                        mPreloadListView.setVisibility(View.VISIBLE);
                    }

                    int SHOW_ICON_NUMBER = 1 + (int) (Math.random() * 3);
                    if (null == mPreloadAdapter) {
                        mPreloadAdapter = new PreloadAdapter(mActivity, mPreloadData, SHOW_ICON_NUMBER);
                        mPreloadListView.setAdapter(mPreloadAdapter);
                    } else {
                        mPreloadAdapter.notifyDataSetChanged();
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "搜索预加载JSON解析失败");
        }
    }

    /**
     * 在线热词网络请求
     */
    private void postHotWord() {
        RequestParams params = new RequestParams();
        params.addBodyParameter("appkey", AppliteUtils.getMitMetaDataValue(mActivity, Constant.META_DATA_MIT));
        params.addBodyParameter("packagename", mActivity.getPackageName());
        params.addBodyParameter("type", "hot_word");
        mHttpUtils.send(HttpRequest.HttpMethod.POST, Constant.URL, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                LogUtils.i(TAG, "在线热词请求成功，reuslt:" + responseInfo.result);
                resolve(responseInfo.result);
            }

            @Override
            public void onFailure(HttpException e, String s) {
                LogUtils.e(TAG, "在线热词请求失败:" + s);
            }
        });
    }

    /**
     * 解析返回的在线热词JSON
     *
     * @param data
     */
    private void resolve(String data) {
        if (!mHotWordBeans.isEmpty()) {
            mHotWordBeans.clear();
            if (null != mGvAdapter) {
                mGvAdapter.notifyDataSetChanged();
            }
            mHotWordLL.setVisibility(View.GONE);
        }
        HotWordBean bean = null;
        try {
            JSONObject obj = new JSONObject(data);
            int app_key = obj.getInt("app_key");
            String hotword_info = obj.getString("hotword_info");
            String hint_info = obj.getString("searchscroll_info");
            LogUtils.i(TAG, "Hint:" + hint_info);

            JSONArray hint_json = new JSONArray(hint_info);
            mHint = new String[hint_json.length()];
            for (int i = 0; i < hint_json.length(); i++) {
                JSONObject hint_obj = new JSONObject(hint_json.get(i).toString());
                String hint = hint_obj.getString("searchscroll");
                mHint[i] = hint;
            }
            mHandler.postDelayed(mRunnable, 0);

            JSONArray hotword_json = new JSONArray(hotword_info);
            for (int i = 0; i < hotword_json.length(); i++) {
                JSONObject object = new JSONObject(hotword_json.get(i).toString());
                bean = new HotWordBean();
                bean.set_id(i);
                bean.setmName(object.getString("name"));
                bean.setmPackageName(object.getString("packageName"));
                bean.setmImgUrl(object.getString("iconUrl"));
                bean.setmType(object.getInt("ishotword"));

                bean.setmStep(object.getInt("step"));
                bean.setmDataType(object.getString("s_datatype"));
                mHotWordBeans.add(bean);
            }
            mEndPageNumber = mHotWordBeans.size() % mShowHotWordNumber;
            if (mEndPageNumber == 0) {
                mHotWordPage = mHotWordBeans.size() / mShowHotWordNumber;
            } else {
                mHotWordPage = mHotWordBeans.size() / mShowHotWordNumber + 1;
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
        if (mHotWordBeans.size() > mShowHotWordNumber) {
            if (position == mHotWordPage - 1 && mEndPageNumber != 0) {
                for (int i = mHotWordBeans.size() - mShowHotWordNumber; i < mHotWordBeans.size(); i++) {
                    mShowHotData.add(mHotWordBeans.get(i));
                }
            } else {
                for (int i = 0 + mShowHotWordNumber * position; i < mShowHotWordNumber + mShowHotWordNumber * position; i++) {
                    mShowHotData.add(mHotWordBeans.get(i));
                }
            }
        } else {
            for (int i = 0; i < mHotWordBeans.size(); i++) {
                mShowHotData.add(mHotWordBeans.get(i));
            }
        }
        if (null == mGvAdapter) {
            mGvAdapter = new HotWordAdapter(mActivity, mShowHotData, this);
            mGridView.setAdapter(mGvAdapter);
        } else {
            mGvAdapter.notifyDataSetChanged();
        }

        mPreloadListView.setVisibility(View.GONE);
        mListView.setVisibility(View.GONE);
        if (TextUtils.isEmpty(mEtView.getText().toString()))
            isHotWordLayoutVisibility(View.VISIBLE);
    }

    @Override
    public void updateText() {
        mActivity.runOnUiThread(mNotifyRunnable);
    }

    @Override
    public void clickItem(String name) {
        mEtViewModifyPostSearch(name, false);
    }
}
