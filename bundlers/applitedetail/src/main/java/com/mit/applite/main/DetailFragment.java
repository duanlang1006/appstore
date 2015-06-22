package com.mit.applite.main;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
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

import com.applite.common.Constant;
import com.mit.applite.utils.LogUtils;
import com.mit.applite.utils.Utils;
import com.mit.applite.view.ProgressButton;
import com.mit.impl.ImplAgent;
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
    private int mApkType;
    private ProgressButton mProgressButton;
    private int mVersionCode;
    private Context mContext;
    private ImplListener mImplListener = new ImplListener() {
        @Override
        public void onDownloadComplete(boolean b, ImplAgent.DownloadCompleteRsp downloadCompleteRsp) {
        }

        @Override
        public void onDownloadUpdate(boolean b, ImplAgent.DownloadUpdateRsp downloadUpdateRsp) {
            switch (downloadUpdateRsp.status) {
                case Constant.STATUS_PENDING:
                    mProgressButton.setText(Utils.getString(mContext, R.string.download_pending));
                    break;
                case Constant.STATUS_RUNNING:
                    mProgressButton.setText(Utils.getString(mContext, R.string.download_running));
                    break;
                case Constant.STATUS_PAUSED:
                    mProgressButton.setText(Utils.getString(mContext, R.string.download_paused));
                    break;
                case Constant.STATUS_FAILED:
                    mProgressButton.setText(Utils.getString(mContext, R.string.download_failed));
                    break;
                case Constant.STATUS_SUCCESSFUL:
                    mProgressButton.setText(Utils.getString(mContext, R.string.download_success));
                    break;
                case Constant.STATUS_PACKAGE_INVALID:
//                    mProgressButton.setText(Utils.getString(mContext, R.string.package_invalid));
                    Toast.makeText(mContext, Utils.getString(mContext, R.string.package_invalid),
                            Toast.LENGTH_SHORT).show();
                    break;
                case Constant.STATUS_PRIVATE_INSTALLING:
                    mProgressButton.setText(Utils.getString(mContext, R.string.installing));
                    break;
                case Constant.STATUS_NORMAL_INSTALLING:
                    break;
                case Constant.STATUS_INSTALLED:
                    mProgressButton.setText(Utils.getString(mContext, R.string.start_up));
                    break;
                case Constant.STATUS_INSTALL_FAILED:
                    mProgressButton.setText(Utils.getString(mContext, R.string.install_failed));
                    break;
            }
            if (downloadUpdateRsp.key.equals(mPackageName)) {
//              mProgressBar.setProgress(downloadUpdateRsp.progress);
                mProgressButton.setProgress(downloadUpdateRsp.progress);
                if (downloadUpdateRsp.progress >= 100) {
//                  mProgressBar.setVisibility(View.INVISIBLE);
//                  mDownloadView.setText(getResources().getString(R.string.open));
//                    mProgressButton.setText(mContext.getResources().getString(R.string.open));
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
        }

        @Override
        public void onSystemDeleteResult(boolean b, ImplAgent.SystemDeleteResultRsp systemDeleteResultRsp) {
        }

        @Override
        public void onFinish(boolean b, ImplAgent.ImplResponse implResponse) {
        }
    };

    public DetailFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        Bundle bundle = this.getArguments();
        mApkName = bundle.getString("name");
        LogUtils.i(TAG, "mApkName:" + mApkName);
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
        this.container = container;

        rootView = mInflater.inflate(R.layout.fragment_detail, container, false);
        mFinalBitmap = FinalBitmap.create(mActivity);
        initView();
        if (!TextUtils.isEmpty(mApkName))
            post(mApkName);
        return rootView;
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

        mProgressButton.setOnProgressButtonClickListener(new ProgressButton.OnProgressButtonClickListener() {
            @Override
            public void onClickListener() {
                if (!TextUtils.isEmpty(mPackageName)) {
                    if (mApkType == Utils.INSTALLED) {
                        Utils.startApp(mActivity, mPackageName);
                    } else {
//                        mProgressBar.setVisibility(View.VISIBLE);
//                        Utils.setDownloadViewText(mContext, mProgressButton);
                        requestDownload();
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
     * @param name
     */
    private void post(String name) {
        FinalHttp mFinalHttp = new FinalHttp();
        AjaxParams params = new AjaxParams();
        params.put("appkey", Utils.getMitMetaDataValue(mActivity, Utils.META_DATA_MIT));
        params.put("packagename", mActivity.getPackageName());
        params.put("app", "applite");
        params.put("type", "detail");
        params.put("name", name);
        mFinalHttp.post(Utils.URL, params, new AjaxCallBack<Object>() {
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
                    String content = obj.getString("brief");
                    mViewPagerUrl = obj.getString("screenshotsUrl");

                    mNameView.setText(mName);
                    mName1View.setText(mName);
                    mXingView.setRating(Float.parseFloat(xing) / 2.0f);
                    mFinalBitmap.display(mApkImgView, mImgUrl);
                    mApkSizeAndCompanyView.setText(Utils.bytes2kb(size));
                    mApkContentView.setText(content);
                }
                mViewPagerUrlList = mViewPagerUrl.split(",");
                LogUtils.i(TAG, "应用图片URL地址：" + mViewPagerUrl);
                setPreViewImg();
            }

            ImplAgent.queryDownload(mActivity, mPackageName);

            //判断应用是否安装
            mApkType = Utils.isAppInstalled(mActivity, mPackageName, mVersionCode);
            if (mApkType == Utils.INSTALLED) {
                mProgressButton.setText(mContext.getResources().getString(R.string.open));
            } else if (mApkType == Utils.INSTALLED_UPDATE) {
                mProgressButton.setText(mContext.getResources().getString(R.string.update));
            } else if (mApkType == Utils.UNINSTALLED) {
                mProgressButton.setText(mContext.getResources().getString(R.string.install));
            }
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
        fb.configLoadingImage(R.drawable.detail_default_img);
        for (int i = 0; i < mViewPagerUrlList.length; i++) {
            final View child = mInflater.inflate(R.layout.item_detail_viewpager_img, container, false);
            final ImageView img = (ImageView) child.findViewById(R.id.item_viewpager_img);
            mImgLl.addView(child);
            fb.display(img, mViewPagerUrlList[i]);
        }
        fb.configLoadingImage(null);
    }

    /**
     * 下载APK
     */
    private void requestDownload() {
        ImplAgent.downloadPackage(mActivity,
                mPackageName,
                mDownloadUrl,
                Utils.extenStorageDirPath,
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