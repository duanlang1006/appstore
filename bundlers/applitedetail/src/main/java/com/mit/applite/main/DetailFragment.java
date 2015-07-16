package com.mit.applite.main;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.applite.common.AppliteUtils;
import com.applite.common.Constant;
import com.applite.common.LogUtils;
import com.mit.applite.view.ProgressButton;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplInfo;
import com.mit.impl.ImplListener;

import net.tsz.afinal.FinalBitmap;
import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DetailFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "DetailFragment";
    private Activity mActivity;
    private View rootView;
    private String mApkName;
    private TextView mNameView;
    private TextView mName1View;
    private TextView mApkSizeAndCompanyView;
    private TextView mApkContentView;
    private ImageButton mBackView;
    private ImageButton mSearchView;
    private Button mDownloadView;
    private RatingBar mXingView;
    private String mDownloadUrl;
    private FinalBitmap mFinalBitmap;
    private ImageView mApkImgView;
    private String mViewPagerUrlList[] = null;
    private ProgressBar mProgressBar;
    private static final int APK_LOADING = 1;
    private LayoutInflater mInflater;
    private ViewGroup container;
    private LinearLayout mImgLl;
    private String mPackageName;
    private String mName;
    private String mImgUrl;
    private ProgressButton mProgressButton;
    private int mVersionCode;
    private Context mContext;
    private ImplInfo mImplInfo = null;
    private ImplListener mImplListener = new ImplListener() {
        @Override
        public void onUpdate(boolean b, ImplInfo implInfo) {
            if (implInfo.getKey().equals(mPackageName)) {
                mImplInfo = implInfo;
                mProgressButton.setText(implInfo.getActionText(mActivity));
                mProgressButton.setProgress(implInfo.getProgress());
            }
        }
    };

    public DetailFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        Bundle bundle = this.getArguments();
        mPackageName = bundle.getString("packageName");
        mApkName = bundle.getString("name");
        mImgUrl = bundle.getString("imgUrl");
        LogUtils.i(TAG, "mApkName:" + mApkName + "------mPackageName:" + mPackageName + "------mImgUrl:" + mImgUrl);
        initActionBar();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImplAgent.registerImplListener(mImplListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mInflater = inflater;
        try {
            Context context = BundleContextFactory.getInstance().getBundleContext().getBundleContext();
            mInflater = LayoutInflater.from(context);
            mInflater = mInflater.cloneInContext(context);
            mContext = context;
        } catch (Exception e) {
            e.printStackTrace();
            mContext = mActivity;
        }
        if (null == mInflater) {
            mInflater = inflater;
        }
        this.container = container;

        rootView = mInflater.inflate(R.layout.fragment_detail, container, false);
        mFinalBitmap = FinalBitmap.create(mActivity);
        initView();
        if (!TextUtils.isEmpty(mPackageName))
            post(mPackageName);

        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ImplAgent.unregisterImplListener(mImplListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getFragmentManager().popBackStack();
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    private void initActionBar() {
        try {
            ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayShowCustomEnabled(false);
            actionBar.setTitle(mApkName);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化控件
     */
    private void initView() {
        mProgressButton = (ProgressButton) rootView.findViewById(R.id.detail_progress_button);
        mApkImgView = (ImageView) rootView.findViewById(R.id.detail_apkimg);
        mNameView = (TextView) rootView.findViewById(R.id.detail_name);
        mName1View = (TextView) rootView.findViewById(R.id.detail_name1);
        mApkSizeAndCompanyView = (TextView) rootView.findViewById(R.id.detail_apksize_and_company);
        mApkContentView = (TextView) rootView.findViewById(R.id.detail_content);
        mBackView = (ImageButton) rootView.findViewById(R.id.detail_back);
        mSearchView = (ImageButton) rootView.findViewById(R.id.detail_search);
        mDownloadView = (Button) rootView.findViewById(R.id.detail_download);
        mXingView = (RatingBar) rootView.findViewById(R.id.detail_xing);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.detail_progress);
        mImgLl = (LinearLayout) rootView.findViewById(R.id.detail_viewpager_img_ll);

        mNameView.setText(mApkName);
        mName1View.setText(mApkName);
        mFinalBitmap.display(mApkImgView, mImgUrl);

        mProgressButton.setOnProgressButtonClickListener(new ProgressButton.OnProgressButtonClickListener() {
            @Override
            public void onClickListener() {
                if (!TextUtils.isEmpty(mPackageName)) {
                    if (null != mImplInfo){
                        switch(mImplInfo.getAction(mActivity)){
                            case ImplInfo.ACTION_DOWNLOAD:
                                requestDownload();
                                break;
                            default:
                                try{
                                    mActivity.startActivity(mImplInfo.getActionIntent(mActivity));
                                }catch(Exception e){
                                    e.printStackTrace();
                                }
                                break;
                        }
                    }
                }
            }
        });
        mBackView.setOnClickListener(this);
        mSearchView.setOnClickListener(this);
        mDownloadView.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.detail_back:
                getFragmentManager().popBackStack();
                break;
            case R.id.detail_download:
//                if (!TextUtils.isEmpty(mPackageName)) {
//                    if (mApkType == Utils.INSTALLED) {
//                        Utils.startApp(mActivity, mPackageName);
//                    } else {
//                        mProgressBar.setVisibility(View.VISIBLE);
//                        Utils.setDownloadViewText(mActivity, mDownloadView);
//                        requestDownload();
//                    }
//                }
                break;
        }
    }

    /**
     * 应用详情网络请求
     *
     * @param mPackageName
     */
    private void post(String mPackageName) {
        FinalHttp mFinalHttp = new FinalHttp();
        AjaxParams params = new AjaxParams();
        params.put("appkey", AppliteUtils.getMitMetaDataValue(mActivity, Constant.META_DATA_MIT));
        params.put("packagename", mActivity.getPackageName());
        params.put("app", "applite");
        params.put("type", "detail");
        params.put("name", mPackageName);
        mFinalHttp.post(Constant.URL, params, new AjaxCallBack<Object>() {
            @Override
            public void onSuccess(Object o) {
                super.onSuccess(o);
                String result = (String) o;
                LogUtils.i(TAG, "应用详情网络请求成功，result:" + result);
                setData(result);
            }

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                super.onFailure(t, errorNo, strMsg);
                LogUtils.e(TAG, "应用详情网络请求失败，strMsg:" + strMsg);
            }
        });
    }

    /**
     * 设置应用详细数据
     *
     * @param data
     */
    private void setData(String data) {
        try {
            String mViewPagerUrl = null;
            JSONObject object = new JSONObject(data);
            String app_key = object.getString("app_key");
            String info = object.getString("detail_info");

            JSONArray json = new JSONArray(info);
            if (json.length() != 0 && json != null) {
                for (int i = 0; i < json.length(); i++) {
                    JSONObject obj = new JSONObject(json.get(i).toString());
                    mPackageName = obj.getString("packageName");
                    mName = obj.getString("name");
                    mImgUrl = obj.getString("iconUrl");
                    String mVersionName = obj.getString("versionName");
                    mVersionCode = obj.getInt("versionCode");
                    String xing = obj.getString("rating");
                    mDownloadUrl = obj.getString("rDownloadUrl");
                    long size = obj.getLong("apkSize");
                    String mDownloadNumber = obj.getString("downloadTimes");
                    String content = obj.getString("description");
                    mViewPagerUrl = obj.getString("screenshotsUrl");

                    mNameView.setText(mName);
                    mName1View.setText(mName);
                    mXingView.setRating(Float.parseFloat(xing) / 2.0f);
                    mFinalBitmap.display(mApkImgView, mImgUrl);
                    mApkSizeAndCompanyView.setText(AppliteUtils.bytes2kb(size));
                    mApkContentView.setText(content);
                }
                mViewPagerUrlList = mViewPagerUrl.split(",");
                LogUtils.i(TAG, "应用图片URL地址：" + mViewPagerUrl);
                setPreViewImg();
            }

            ImplAgent.queryDownload(mActivity, mPackageName);
            if (null == mImplInfo){
                mImplInfo = ImplInfo.create(mActivity,mPackageName,mDownloadUrl,mPackageName);
            }
            mProgressButton.setText(mImplInfo.getActionText(mActivity));
            mProgressButton.setProgress(mImplInfo.getProgress());
        } catch (JSONException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "应用详情JSON解析失败");
        }

    }

    /**
     * 设置应用介绍的图片
     */
    private void setPreViewImg() {
        FinalBitmap fb = FinalBitmap.create(mActivity);
//        fb.configLoadingImage(R.drawable.detail_default_img);
        for (int i = 0; i < mViewPagerUrlList.length; i++) {
            final View child = mInflater.inflate(R.layout.item_detail_viewpager_img, container, false);
            final ImageView img = (ImageView) child.findViewById(R.id.item_viewpager_img);
            mImgLl.addView(child);
            fb.display(img, mViewPagerUrlList[i]);
        }
//        fb.configLoadingImage(null);
    }

    /**
     * 下载APK
     */
    private void requestDownload() {
        ImplAgent.downloadPackage(mActivity,
                mPackageName,
                mDownloadUrl,
                Constant.extenStorageDirPath,
                mName + ".apk",
                3,
                false,
                mName,
                "",
                true,
                mImgUrl,
                "",
                mPackageName);
    }

}