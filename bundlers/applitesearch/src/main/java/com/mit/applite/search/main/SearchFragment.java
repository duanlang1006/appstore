package com.mit.applite.search.main;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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

import com.applite.bean.ApkBean;
import com.applite.common.AppliteUtils;
import com.applite.common.Constant;
import com.applite.common.LogUtils;
import com.google.gson.Gson;
import com.mit.afinal.FinalHttp;
import com.mit.afinal.http.AjaxCallBack;
import com.mit.afinal.http.AjaxParams;
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

public class SearchFragment extends OSGIBaseFragment implements View.OnClickListener, HotWordAdapter.ClickHotWordItemPostlistener {

    private static final String TAG = "SearchFragment";
    private static final int SHOW_VIEW_HOTWORD = 1;
    private static final int SHOW_VIEW_PRELOAD = 2;
    private static final int SHOW_VIEW_SEARCH = 3;
    private static final int SHOW_VIEW_NONETWORK = 4;
    private static final int SHOW_VIEW_LOADING_LAYOUT = 5;
    private static final int SHOW_VIEW_NULL = 6;
    private static final int SHOW_VIEW_DEFAULT = 0;

    private int SHOW_STATE = SHOW_VIEW_DEFAULT;
    private static final int CHANG_NUMBER = 1;
    private static final int SEARCH_DEFAULT_PAGE = 0;

    //View
    private View rootView;
    private ViewGroup customView;
    private ImageView mBackView;
    private ImageView mSearchView;
    private ImageView mDeleteView;
    private EditText mEtView;
    private LinearLayout mHotWordLL;
    private GridView mGridView;
    private TextView mHotChangeView;
    private ListView mPreloadListView;
    private ListView mListView;
    private Button mRefresh;
    private RelativeLayout mNoNetwork;
    private View moreView;//搜索ListView尾部布局
    private TextView mMoreText;
    private ProgressBar mMoreProgressBar;
    private LinearLayout mLoadingLayout;
    private ImageView mLoadingImgView;
    private Animation LoadingAnimation;

    //Adapter
    private HotWordAdapter mGvAdapter;
    private PreloadAdapter mPreloadAdapter;
    private SearchApkAdapter mAdapter;

    //数据
    private String mHotWordData;//热词返回数据
    private List<HotWordBean> mShowHotList = new ArrayList<HotWordBean>();
    private List<HotWordBean> mHotWordList = new ArrayList<HotWordBean>();
    private List<ApkBean> mPreloadList = new ArrayList<ApkBean>();
    private List<ApkBean> mSearchList = new ArrayList<ApkBean>();
    private String mKeyword;
    private String mHintword;

    //热词相关
    private int mChangeNumber = CHANG_NUMBER;//在线热词换一换点击的次数
    private int mEndPageNumber;//最后一页热词的个数
    private int mHotWordPage;//热词的页数
    private int mShowHotWordNumber = 9;

    //预加载相关
    private int mPostPreloadNumber = 0;
    private int PRELOAD_SHOW_ICON_NUMBER = 1;

    //搜索相关
    private int SEARCH_POST_CURRENT_PAGE = SEARCH_DEFAULT_PAGE;
    private String mSearchLastName;//上次请求关键字

    private boolean isPostPreload = true;//预加载是否请求
    private boolean isToEnd = false;//服务器数据是否到底
    private boolean isLastRow = false;
    private boolean isAllowPost = true;//是否允许请求
    private boolean isShowSearchView = true;//是否显示搜索列表
    private boolean isClickRefresh = false;//是否点击刷新

    private LayoutInflater mInflater;
    private FinalHttp mFinalHttp;
    private Gson mGson = new Gson();
    private String mDetailTag;

    public SearchFragment() {
        super();
    }

    public static Bundle newBundle(String DetailTag, String info, String keyword, String hintword) {
        Bundle b = new Bundle();
        b.putString("DetailTag", DetailTag);
        b.putString("info", info);
        b.putString("keyword", keyword);
        b.putString("hintword", hintword);
        return b;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mFinalHttp = new FinalHttp();
        Bundle params = getArguments();
        if (null != params) {
            mDetailTag = params.getString("DetailTag");
            LogUtils.i(TAG, "mDetailTag:" + mDetailTag);
            this.mHotWordData = params.getString("info");
            this.mKeyword = params.getString("keyword");
            this.mHintword = params.getString("hintword");
            LogUtils.i(TAG, "mHintword = " + mHintword + " mKeyword = " + mKeyword + " mInfo = " + mHotWordData);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MitMobclickAgent.onEvent(mActivity, "toSearchFragment");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mInflater = inflater;
        rootView = mInflater.inflate(R.layout.fragment_search, container, false);
        moreView = mInflater.inflate(R.layout.load, null);
        initView();
        if (TextUtils.isEmpty(mDetailTag)) {
            initActionBar();
            if (SHOW_STATE == SHOW_VIEW_DEFAULT) {//第一次进
                if (null == mKeyword) {//是否直接搜索
                    if (null == mHotWordData) {//在线热词是否有数据
                        postHotWord();
                    } else {
                        setHotWordData(mHotWordData);
                    }
                } else {
                    postSearch(mKeyword, SEARCH_DEFAULT_PAGE);
                }
            } else {
                setHotWordShowData(mShowHotList);
                setSearchData(mSearchList);
                setPreloadData(mPreloadList);
                if (SHOW_STATE == SHOW_VIEW_HOTWORD) {
                    setShowView(SHOW_VIEW_HOTWORD);
                } else if (SHOW_STATE == SHOW_VIEW_SEARCH) {
                    setShowView(SHOW_VIEW_SEARCH);
                } else if (SHOW_STATE == SHOW_VIEW_PRELOAD) {
                    setShowView(SHOW_VIEW_PRELOAD);
                }
            }
        } else {
            initDetailTagActionBar();
            setShowView(SHOW_VIEW_LOADING_LAYOUT);
            postDetailTag();
        }
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getfocuable();
        if (mListView.getVisibility() == View.GONE && mPreloadListView.getVisibility() == View.GONE) {
            openKeyboard();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem item = menu.findItem(R.id.action_search);
        if (null != item) {
            item.setVisible(false);
        }
        MenuItem item_dm = menu.findItem(R.id.action_dm);
        if (null != item_dm) {
            item_dm.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (android.R.id.home == item.getItemId()) {
            closeKeyboard();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
        closeKeyboard();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.search_back) {
            getFragmentManager().popBackStack();
        } else if (v.getId() == R.id.hot_word_change) {
            MitMobclickAgent.onEvent(mActivity, "clickHotWordChange");
            changeHotWord();
        } else if (v.getId() == R.id.search_delete) {
            mEtView.setText(null);
            setShowView(SHOW_VIEW_HOTWORD);
            getfocuable();
            openKeyboard();
            isShowSearchView = false;
        } else if (v.getId() == R.id.search_search) {
            MitMobclickAgent.onEvent(mActivity, "clickSearch");
            getfocuable();
            closeKeyboard();

            if (TextUtils.isEmpty(mEtView.getText())) {
                if (TextUtils.isEmpty(mEtView.getHint())) {
                    Toast.makeText(mActivity, AppliteUtils.getString(mActivity, R.string.srarch_content_no_null), Toast.LENGTH_SHORT).show();
                } else {
                    MitMobclickAgent.onEvent(mActivity, "searchHint");
                    mEtView.setText(mEtView.getHint().toString());
                    postSearch(mEtView.getHint().toString(), SEARCH_DEFAULT_PAGE);
                }
            } else {
                postSearch(mEtView.getText().toString(), SEARCH_DEFAULT_PAGE);
            }
        } else if (v.getId() == R.id.refresh_btn) {
            if (TextUtils.isEmpty(mDetailTag)) {
                isClickRefresh = true;
                postSearch(mEtView.getText().toString(), SEARCH_DEFAULT_PAGE);
            } else {
                postDetailTag();
            }
        }
    }

    /**
     * 详情标签进来的ActionBar
     */
    private void initDetailTagActionBar() {
        try {
            LogUtils.i(TAG, "initDetailTagActionBar");
            ActionBar actionBar = ((ActionBarActivity) mActivity).getSupportActionBar();
            actionBar.setDisplayShowCustomEnabled(false);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(mDetailTag);
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            actionBar.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 搜索页面的ActionBar
     */
    private void initActionBar() {
        try {
            LogUtils.i(TAG, "initActionBar");
            if (null == customView) {
                customView = (ViewGroup) mInflater.inflate(R.layout.actionbar_search, null);
                mBackView = (ImageView) customView.findViewById(R.id.search_back);
                mBackView.setOnClickListener(this);
                mEtView = (EditText) customView.findViewById(R.id.search_et);
                mEtView.addTextChangedListener(mTextWatcher);
                mEtView.setOnClickListener(this);
                mSearchView = (ImageView) customView.findViewById(R.id.search_search);
                mSearchView.setOnClickListener(this);
                mDeleteView = (ImageView) customView.findViewById(R.id.search_delete);
                mDeleteView.setOnClickListener(this);
                getfocuable();
                openKeyboard();
                if (!TextUtils.isEmpty(mHintword)) {
                    mEtView.setHint(mHintword);
                }
                if (null != mKeyword) {
                    isPostPreload = false;
                    mEtView.setText(mKeyword);
                }
            }
            ActionBar actionBar = ((ActionBarActivity) mActivity).getSupportActionBar();
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
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

        mRefresh = (Button) rootView.findViewById(R.id.refresh_btn);
        mNoNetwork = (RelativeLayout) rootView.findViewById(R.id.no_network);

        mPreloadListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mPreloadListView.setVisibility(View.GONE);
                isPostPreload = false;
                mEtView.setText(mPreloadList.get(position).getName());
                postSearch(mPreloadList.get(position).getName(), SEARCH_DEFAULT_PAGE);
            }
        });

        mListView.setSelected(true);
        mListView.setOnScrollListener(mOnScrollListener);

        mHotChangeView.setOnClickListener(this);
        mRefresh.setOnClickListener(this);
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
            if (TextUtils.isEmpty(mEtView.getText().toString())) {
                setShowView(SHOW_VIEW_HOTWORD);
            } else {
                isShowSearchView = false;
                isAllowPost = true;
                if (isPostPreload)
                    postPreload(mEtView.getText().toString(), mPostPreloadNumber);
                isPostPreload = true;
            }
        }
    };

    private AbsListView.OnScrollListener mOnScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (isLastRow && scrollState == this.SCROLL_STATE_IDLE) {
                LogUtils.i(TAG, "拉到最底部");
                if (mListView.getFooterViewsCount() == 0)
                    mListView.addFooterView(moreView);

                if (isToEnd) {
                    mMoreProgressBar.setVisibility(View.GONE);
                    mMoreText.setText(AppliteUtils.getString(mActivity, R.string.no_data));
                } else {
                    mMoreProgressBar.setVisibility(View.VISIBLE);
                    mMoreText.setText(AppliteUtils.getString(mActivity, R.string.loading));

                    //加载更多数据，这里使用异步加载
                    if (isAllowPost) {
                        if (TextUtils.isEmpty(mDetailTag)) {
                            postSearch(mEtView.getText().toString(), SEARCH_POST_CURRENT_PAGE);
                        } else {
                            postDetailTag();
                        }
                        isAllowPost = false;
                    }

                }
                LogUtils.i(TAG, "加载更多数据");
                isLastRow = false;
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            //判断是否滚到最后一行
            if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount > 0) {
                LogUtils.i(TAG, "滚到最后一行");
                isLastRow = true;
            }
        }
    };

    /**
     * 设置控件显示隐藏状态
     *
     * @param i
     */
    private void setShowView(int i) {
        switch (i) {
            case SHOW_VIEW_HOTWORD:
                setHotWordLayoutVisibility(View.VISIBLE);
                mPreloadListView.setVisibility(View.GONE);
                mListView.setVisibility(View.GONE);
                mNoNetwork.setVisibility(View.GONE);
                mLoadingLayout.setVisibility(View.GONE);
                SHOW_STATE = SHOW_VIEW_HOTWORD;
                break;
            case SHOW_VIEW_PRELOAD:
                setHotWordLayoutVisibility(View.GONE);
                mPreloadListView.setVisibility(View.VISIBLE);
                mListView.setVisibility(View.GONE);
                mNoNetwork.setVisibility(View.GONE);
                mLoadingLayout.setVisibility(View.GONE);
                SHOW_STATE = SHOW_VIEW_PRELOAD;
                break;
            case SHOW_VIEW_SEARCH:
                setHotWordLayoutVisibility(View.GONE);
                mPreloadListView.setVisibility(View.GONE);
                if (isShowSearchView)
                    mListView.setVisibility(View.VISIBLE);
                mNoNetwork.setVisibility(View.GONE);
                mLoadingLayout.setVisibility(View.GONE);
                SHOW_STATE = SHOW_VIEW_SEARCH;
                closeKeyboard();
                break;
            case SHOW_VIEW_NONETWORK:
                setHotWordLayoutVisibility(View.GONE);
                mPreloadListView.setVisibility(View.GONE);
                mListView.setVisibility(View.GONE);
                mNoNetwork.setVisibility(View.VISIBLE);
                mLoadingLayout.setVisibility(View.GONE);
                SHOW_STATE = SHOW_VIEW_NONETWORK;
                closeKeyboard();
                break;
            case SHOW_VIEW_LOADING_LAYOUT:
                setHotWordLayoutVisibility(View.GONE);
                mPreloadListView.setVisibility(View.GONE);
                mListView.setVisibility(View.GONE);
                mNoNetwork.setVisibility(View.GONE);
                mLoadingLayout.setVisibility(View.VISIBLE);
                SHOW_STATE = SHOW_VIEW_LOADING_LAYOUT;
                closeKeyboard();
                break;
            case SHOW_VIEW_NULL:
                setHotWordLayoutVisibility(View.GONE);
                mPreloadListView.setVisibility(View.GONE);
                mListView.setVisibility(View.GONE);
                mNoNetwork.setVisibility(View.GONE);
                mLoadingLayout.setVisibility(View.GONE);
                closeKeyboard();
                break;
        }
    }

    /**
     * 判断在线热词Layout是否隐藏
     *
     * @param i
     */
    private void setHotWordLayoutVisibility(int i) {
        if (i == View.VISIBLE) {
            if (!mHotWordList.isEmpty()) {
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

    private void losefocuable() {
        mEtView.setFocusable(false);
    }

    private void getfocuable() {
        if (null != mEtView) {
            mEtView.setFocusable(true);
            mEtView.setFocusableInTouchMode(true);
            mEtView.requestFocus();
        }
    }

    private void changeHotWord() {
        if (mChangeNumber >= mHotWordPage)
            mChangeNumber = 0;
        setHotWordShowData(mChangeNumber);
        mChangeNumber = mChangeNumber + 1;
    }

    private void openKeyboard() {
        if (TextUtils.isEmpty(mDetailTag))
            KeyBoardUtils.openKeyboard(mEtView, mActivity);
    }

    private void closeKeyboard() {
        if (TextUtils.isEmpty(mDetailTag))
            KeyBoardUtils.closeKeyboard(mEtView, mActivity);
    }

    /**
     * 在线热词网络请求
     */
    private void postHotWord() {
        AjaxParams params = new AjaxParams();
        params.put("appkey", AppliteUtils.getMitMetaDataValue(mActivity, Constant.META_DATA_MIT));
        params.put("packagename", mActivity.getPackageName());
        params.put("type", "hot_word");
        params.put("protocol_version", Constant.PROTOCOL_VERSION);
        mFinalHttp.post(Constant.URL, params, new AjaxCallBack<String>() {
            @Override
            public void onSuccess(String s) {
                LogUtils.i(TAG, "在线热词请求成功，reuslt:" + s);
                mHotWordData = s;
                setHotWordData(s);
            }

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                super.onFailure(t, errorNo, strMsg);
                LogUtils.e(TAG, "在线热词请求失败:" + strMsg);
            }

        });
    }

    /**
     * 解析返回的在线热词JSON
     *
     * @param data
     */
    private void setHotWordData(String data) {
        mHotWordList.clear();
        HotWordBean bean = null;
        try {
            JSONObject obj = new JSONObject(data);
            int app_key = obj.getInt("app_key");
            String hotword_info = obj.getString("hotword_info");

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
                mHotWordList.add(bean);
            }
            mEndPageNumber = mHotWordList.size() % mShowHotWordNumber;
            if (mEndPageNumber == 0) {
                mHotWordPage = mHotWordList.size() / mShowHotWordNumber;
            } else {
                mHotWordPage = mHotWordList.size() / mShowHotWordNumber + 1;
            }
            setHotWordShowData(0);
            LogUtils.i(TAG, "在线热词返回的数量:" + mHotWordList.size());
        } catch (JSONException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "在线热词JSON解析失败");
        }
    }

    /**
     * 设置在线热词显示的数据
     */
    private void setHotWordShowData(int position) {
        mShowHotList.clear();
        if (mHotWordList.size() > mShowHotWordNumber) {
            if (position == mHotWordPage - 1 && mEndPageNumber != 0) {
                for (int i = mHotWordList.size() - mShowHotWordNumber; i < mHotWordList.size(); i++) {
                    mShowHotList.add(mHotWordList.get(i));
                }
            } else {
                for (int i = 0 + mShowHotWordNumber * position; i < mShowHotWordNumber + mShowHotWordNumber * position; i++) {
                    mShowHotList.add(mHotWordList.get(i));
                }
            }
        } else {
            for (int i = 0; i < mHotWordList.size(); i++) {
                mShowHotList.add(mHotWordList.get(i));
            }
        }
        if (null == mGvAdapter)
            mGvAdapter = new HotWordAdapter(mActivity, mShowHotList, this);
        mGridView.setAdapter(mGvAdapter);
        setShowView(SHOW_VIEW_HOTWORD);
    }

    private void setHotWordShowData(List<HotWordBean> list) {
        if (null == mGvAdapter)
            mGvAdapter = new HotWordAdapter(mActivity, list, this);
        mGridView.setAdapter(mGvAdapter);
    }

    @Override
    public void clickItem(String name) {
        isPostPreload = false;
        mEtView.setText(name);
        postSearch(name, SEARCH_DEFAULT_PAGE);
    }

    /**
     * 搜索网络请求
     *
     * @param name
     */
    private void postSearch(String name, final int page) {
        isShowSearchView = true;
        if (page == 0) {
            setShowView(SHOW_VIEW_LOADING_LAYOUT);
            if (name.equals(mSearchLastName) && !isClickRefresh) {
                mListView.setSelection(0);
                setShowView(SHOW_VIEW_SEARCH);
                return;
            } else {
                SEARCH_POST_CURRENT_PAGE = 0;
            }
        }
        isClickRefresh = false;
        mSearchLastName = name;

        if (SearchUtils.isLetter(name)) {
            name = AppliteUtils.SplitLetter(name);
        }
        LogUtils.i(TAG, "name:" + name);
        AjaxParams params = new AjaxParams();
        params.put("appkey", AppliteUtils.getMitMetaDataValue(mActivity, Constant.META_DATA_MIT));
        params.put("packagename", mActivity.getPackageName());
        params.put("type", "search");
        params.put("key", name);
        params.put("key_type", "search_name");
        params.put("protocol_version", Constant.PROTOCOL_VERSION);
        params.put("page", page + "");
        mFinalHttp.post(Constant.URL, params, new AjaxCallBack<String>() {
            @Override
            public void onSuccess(String s) {
                LogUtils.i(TAG, "搜索网络请求成功，result:" + s);
                isAllowPost = true;
                mLoadingLayout.setVisibility(View.GONE);
                SEARCH_POST_CURRENT_PAGE = page + 1;//请求成功后，请求的页数加1

                if (mEtView.getText().toString().equals(mSearchLastName))
                    setSearchData(s, page);
            }

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                super.onFailure(t, errorNo, strMsg);
                LogUtils.e(TAG, "搜索网络请求失败:" + strMsg);
                isAllowPost = true;
                mLoadingLayout.setVisibility(View.GONE);
                mListView.removeFooterView(moreView);

                if (mListView.getVisibility() == View.GONE && mPreloadListView.getVisibility() == View.GONE && mHotWordLL.getVisibility() == View.GONE)
                    setShowView(SHOW_VIEW_NONETWORK);

                Toast.makeText(mActivity, AppliteUtils.getString(mActivity, R.string.post_failure), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 显示搜索得到的数据
     *
     * @param data
     */
    private void setSearchData(String data, int page) {
        if (page == 0)
            mSearchList.clear();
        try {
            SearchBean searchBean = mGson.fromJson(data, SearchBean.class);
            if (null != searchBean) {
                int app_key = searchBean.getApp_key();
                isToEnd = searchBean.getIstoend();
                List<ApkBean> contents = searchBean.getSearch_info();
                if (null != contents) {
                    mSearchList.addAll(contents);
                }
                if (null == mAdapter) {
                    mAdapter = new SearchApkAdapter(mActivity, mSearchList);
                    mListView.setAdapter(mAdapter);
                } else {
                    mAdapter.notifyDataSetChanged();
                }
                if (page == 0) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mListView.setSelection(0);
                        }
                    }, 200);//ListView设置显示条目位置，需要延时一会。因为ListView没有显示之前，设置不了显示位置
                }
                setShowView(SHOW_VIEW_SEARCH);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e(TAG, "搜索JSON解析失败");
        }
    }

    private void setSearchData(List<ApkBean> SearchList) {
        if (null == mAdapter)
            mAdapter = new SearchApkAdapter(mActivity, SearchList);
        mListView.setAdapter(mAdapter);
    }

    /**
     * 预加载网络请求
     *
     * @param name
     * @param number 点击搜索随便填
     */
    private void postPreload(String name, final int number) {
        if (SearchUtils.isLetter(name)) {
            name = AppliteUtils.SplitLetter(name);
        }
        LogUtils.i(TAG, "name:" + name);
        AjaxParams params = new AjaxParams();
        params.put("appkey", AppliteUtils.getMitMetaDataValue(mActivity, Constant.META_DATA_MIT));
        params.put("packagename", mActivity.getPackageName());
        params.put("type", "search");
        params.put("protocol_version", Constant.PROTOCOL_VERSION);
        params.put("key", name);
        params.put("key_type", "search_name");
        mFinalHttp.post(Constant.URL, params, new AjaxCallBack<String>() {
            @Override
            public void onSuccess(String s) {
                LogUtils.i(TAG, "预加载网络请求成功，result:" + s);
                if (number == mPostPreloadNumber) {
                    setPreloadData(s);
                }
            }

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                super.onFailure(t, errorNo, strMsg);
                LogUtils.e(TAG, "预加载网络请求失败:" + strMsg);
            }

        });
    }

    /**
     * 显示预加载的数据
     *
     * @param result
     */
    private void setPreloadData(String result) {
        mPreloadList.clear();
        try {
            SearchBean searchBean = mGson.fromJson(result, SearchBean.class);
            if (null != searchBean) {
                int app_key = searchBean.getApp_key();
                List<ApkBean> contents = searchBean.getSearch_info();
                if (null != contents) {
                    mPreloadList.addAll(contents);
                }

                if (!TextUtils.isEmpty(mEtView.getText().toString()) && !mEtView.getText().toString().equals(mSearchLastName)) {
                    PRELOAD_SHOW_ICON_NUMBER = 1 + (int) (Math.random() * 3);
                    if (null == mPreloadAdapter) {
                        mPreloadAdapter = new PreloadAdapter(mActivity, mPreloadList, PRELOAD_SHOW_ICON_NUMBER);
                        mPreloadListView.setAdapter(mPreloadAdapter);
                    } else {
                        mPreloadAdapter.notifyDataSetChanged();
                    }
                    setShowView(SHOW_VIEW_PRELOAD);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e(TAG, "搜索预加载JSON解析失败");
        }
    }

    private void setPreloadData(List<ApkBean> list) {
        if (null == mPreloadAdapter)
            mPreloadAdapter = new PreloadAdapter(mActivity, list, PRELOAD_SHOW_ICON_NUMBER);
        mPreloadListView.setAdapter(mPreloadAdapter);
    }

    /**
     * 详情标签请求
     */
    private void postDetailTag() {
        if (SEARCH_POST_CURRENT_PAGE == 0) {
            setShowView(SHOW_VIEW_LOADING_LAYOUT);
        }
        AjaxParams params = new AjaxParams();
        params.put("appkey", AppliteUtils.getMitMetaDataValue(mActivity, Constant.META_DATA_MIT));
        params.put("packagename", mActivity.getPackageName());
        params.put("type", "search");
        params.put("key_type", "search_tag");
        params.put("key", mDetailTag);
        params.put("page", SEARCH_POST_CURRENT_PAGE + "");
        params.put("protocol_version", Constant.PROTOCOL_VERSION);
        mFinalHttp.post(Constant.URL, params, new AjaxCallBack<String>() {
            @Override
            public void onSuccess(String s) {
                LogUtils.i(TAG, "详情点击标签请求成功，reuslt:" + s);
                isAllowPost = true;//请求结束后，才可以继续请求

                resolveDetailTagData(s);
                SEARCH_POST_CURRENT_PAGE = SEARCH_POST_CURRENT_PAGE + 1;//请求成功后，请求的页数加1
            }

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                super.onFailure(t, errorNo, strMsg);
                LogUtils.e(TAG, "详情点击标签请求请求失败:" + strMsg);
                isAllowPost = true;//请求结束后，才可以继续请求
                mLoadingLayout.setVisibility(View.GONE);

                if (mListView.getVisibility() == View.GONE)
                    setShowView(SHOW_VIEW_NONETWORK);
            }
        });
    }

    /**
     * 解析详情标签请求返回数据
     *
     * @param data
     */
    private void resolveDetailTagData(String data) {
        try {
            SearchBean searchBean = mGson.fromJson(data, SearchBean.class);
            if (null != searchBean) {
                int app_key = searchBean.getApp_key();
                isToEnd = searchBean.getIstoend();
                List<ApkBean> contents = searchBean.getSearch_info();
                if (null != contents) {
                    mSearchList.addAll(contents);
                }

                if (null == mAdapter) {
                    mAdapter = new SearchApkAdapter(mActivity, mSearchList);
                    mListView.setAdapter(mAdapter);
                } else {
                    mAdapter.notifyDataSetChanged();
                }
                setShowView(SHOW_VIEW_SEARCH);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e(TAG, "详情点击标签返回JSON解析失败");
        }
    }
}
