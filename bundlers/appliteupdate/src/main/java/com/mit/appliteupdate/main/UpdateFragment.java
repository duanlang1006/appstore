package com.mit.appliteupdate.main;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.applite.common.AppliteUtils;
import com.applite.common.Constant;
import com.applite.common.LogUtils;
import com.mit.appliteupdate.R;
import com.mit.appliteupdate.adapter.UpdateAdapter;
import com.mit.appliteupdate.bean.DataBean;
import com.mit.appliteupdate.utils.UpdateSPUtils;
import com.mit.appliteupdate.utils.UpdateUtils;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplListener;
import com.umeng.analytics.MobclickAgent;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.apkplug.Bundle.ApkplugOSGIService;
import org.apkplug.Bundle.OSGIServiceAgent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;

import java.util.ArrayList;
import java.util.List;

public class UpdateFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "UpdateFragment";
    private LayoutInflater mInflater;
    private View rootView;
    private Activity mActivity;
    private FinalHttp mFinalHttp;
    private TextView mAllUpdateView;
    private ListView mListView;
    private List<DataBean> mDataContents = new ArrayList<DataBean>();
    private UpdateAdapter mAdapter;
    private Runnable mNotifyRunnable = new Runnable() {
        @Override
        public void run() {
            mAdapter.notifyDataSetChanged();
        }
    };
    private ImplListener mImplListener = new ImplListener() {
        @Override
        public void onDownloadComplete(boolean b, ImplAgent.DownloadCompleteRsp downloadCompleteRsp) {
            for (int i = 0; i < mDataContents.size(); i++) {
                if (downloadCompleteRsp.key.equals(mDataContents.get(i).getmPackageName())) {
                    switch (downloadCompleteRsp.status) {
                        case Constant.STATUS_SUCCESSFUL:
                            mDataContents.get(i).setmShowText(AppliteUtils.getString(mContext, R.string.download_success));
                            mActivity.runOnUiThread(mNotifyRunnable);
                            break;
                    }
                }
            }
        }

        @Override
        public void onDownloadUpdate(boolean b, ImplAgent.DownloadUpdateRsp downloadUpdateRsp) {
            for (int i = 0; i < mDataContents.size(); i++) {
                if (downloadUpdateRsp.key.equals(mDataContents.get(i).getmPackageName())) {
                    String OriginalShowText = mDataContents.get(i).getmShowText();

                    switch (downloadUpdateRsp.status) {
                        case Constant.STATUS_PENDING:
                            mDataContents.get(i).setmShowText(AppliteUtils.getString(mContext, R.string.download_pending));
                            break;
                        case Constant.STATUS_RUNNING:
                            mDataContents.get(i).setmShowText(AppliteUtils.getString(mContext, R.string.download_running));
                            break;
                        case Constant.STATUS_PAUSED:
                            mDataContents.get(i).setmShowText(AppliteUtils.getString(mContext, R.string.download_paused));
                            break;
                        case Constant.STATUS_FAILED:
                            mDataContents.get(i).setmShowText(AppliteUtils.getString(mContext, R.string.download_failed));
                            break;
                        case Constant.STATUS_NORMAL_INSTALLING:
                            break;
                    }

                    String CurrentShowText = mDataContents.get(i).getmShowText();
                    if (!OriginalShowText.equals(CurrentShowText))
                        mActivity.runOnUiThread(mNotifyRunnable);
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
            for (int i = 0; i < mDataContents.size(); i++) {
                if (systemInstallResultRsp.key.equals(mDataContents.get(i).getmPackageName())) {
                    switch (systemInstallResultRsp.result) {
                        case Constant.STATUS_PACKAGE_INVALID:
                            mDataContents.get(i).setmShowText(AppliteUtils.getString(mContext, R.string.package_invalid));
                            Toast.makeText(mActivity, AppliteUtils.getString(mContext, R.string.package_invalid),
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case Constant.STATUS_INSTALL_FAILED:
                            mDataContents.get(i).setmShowText(AppliteUtils.getString(mContext, R.string.install_failed));
                            break;
                        case Constant.STATUS_INSTALLED:
                            mDataContents.get(i).setmShowText(AppliteUtils.getString(mContext, R.string.start_up));
                            break;
                    }
                    mActivity.runOnUiThread(mNotifyRunnable);
                }
            }
        }

        @Override
        public void onSystemDeleteResult(boolean b, ImplAgent.SystemDeleteResultRsp systemDeleteResultRsp) {

        }

        @Override
        public void onFinish(boolean b, ImplAgent.ImplResponse implResponse) {
            if (implResponse instanceof ImplAgent.InstallPackageRsp) {
                for (int i = 0; i < mDataContents.size(); i++) {
                    if (((ImplAgent.InstallPackageRsp) implResponse).key.equals(mDataContents.get(i).getmPackageName())) {
                        mDataContents.get(i).setmShowText(AppliteUtils.getString(mContext, R.string.installing));
                        mActivity.runOnUiThread(mNotifyRunnable);
                    }
                }
            }
        }
    };
    private Context mContext;
    private LinearLayout mStatsLayout;
    private ImageView mStatsImgView;
    private Button mStatsButton;
    private boolean mPostStats = true;

    public UpdateFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        initActionBar();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFinalHttp = new FinalHttp();
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
//        this.container = container;

        rootView = mInflater.inflate(R.layout.fragment_update, container, false);
        initView();
        post();

        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("UpdateFragment"); //统计页面
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("UpdateFragment");
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.update_all_update:
                if (!mDataContents.isEmpty()) {
                    DataBean data = null;
                    for (int i = 0; i < mDataContents.size(); i++) {
                        data = mDataContents.get(i);
                        ImplAgent.downloadPackage(mActivity,
                                data.getmPackageName(),
                                data.getmUrl(),
                                Constant.extenStorageDirPath,
                                data.getmName() + ".apk",
                                3,
                                false,
                                data.getmName(),
                                "",
                                true,
                                data.getmImgUrl(),
                                "",
                                data.getmPackageName());
                    }
                } else {
                    Toast.makeText(mContext, AppliteUtils.getString(mContext, R.string.no_update), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.update_post_button:
                if (mPostStats)
                    post();
                break;
        }
    }

    private void initActionBar() {
        try {
            ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayShowCustomEnabled(false);
            actionBar.setTitle("更新管理");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        mAllUpdateView = (TextView) rootView.findViewById(R.id.update_all_update);
        mListView = (ListView) rootView.findViewById(R.id.update_listview);
        mStatsLayout = (LinearLayout) rootView.findViewById(R.id.update_stats);
        mStatsImgView = (ImageView) rootView.findViewById(R.id.update_stats_img);
        mStatsButton = (Button) rootView.findViewById(R.id.update_post_button);

        mStatsButton.setOnClickListener(this);
        mAllUpdateView.setOnClickListener(this);
    }

    private void post() {
        mPostStats = false;
        AjaxParams params = new AjaxParams();
        params.put("appkey", AppliteUtils.getMitMetaDataValue(mActivity, Constant.META_DATA_MIT));
        params.put("packagename", mActivity.getPackageName());
        params.put("app", "applite");
        params.put("type", "update_management");
        params.put("update_info", UpdateUtils.getAllApkData(mActivity));
        mFinalHttp.post(Constant.URL, params, new AjaxCallBack<Object>() {
            @Override
            public void onSuccess(Object o) {
                super.onSuccess(o);
                String resulit = (String) o;
                LogUtils.i(TAG, "更新请求成功，resulit：" + resulit);
                resolve(resulit);
                mPostStats = true;
            }

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                super.onFailure(t, errorNo, strMsg);
                LogUtils.i(TAG, "更新请求失败，strMsg：" + strMsg);
                setStatsLayoutVisibility(View.VISIBLE, mContext.getResources().getDrawable(R.drawable.post_failure));
                mStatsButton.setVisibility(View.VISIBLE);
                mPostStats = true;
            }
        });
    }


    /**
     * 解析返回的数据
     *
     * @param resulit
     */
    private void resolve(String resulit) {
        try {
            JSONObject object = new JSONObject(resulit);
            int app_key = object.getInt("app_key");
            String installed_update_list = object.getString("installed_update_list");
            DataBean bean = null;
            if (!TextUtils.isEmpty(installed_update_list)) {
                JSONArray array = new JSONArray(installed_update_list);
                for (int i = 0; i < array.length(); i++) {
                    bean = new DataBean();
                    JSONObject obj = new JSONObject(array.get(i).toString());
                    bean.setmName(obj.getString("name"));
                    bean.setmVersionCode(obj.getInt("versionCode"));
                    bean.setmVersionName(obj.getString("versionName"));
                    bean.setmImgUrl(obj.getString("iconUrl"));
                    bean.setmPackageName(obj.getString("packageName"));
                    bean.setmUrl(obj.getString("rDownloadUrl"));
                    bean.setmSize(obj.getLong("apkSize"));

                    bean.setmShowText(AppliteUtils.getString(mContext, R.string.install));

                    mDataContents.add(bean);
                }
                if (array.length() == 0) {
                    setStatsLayoutVisibility(View.VISIBLE, mContext.getResources().getDrawable(R.drawable.no_update));
                    mStatsButton.setVisibility(View.GONE);
                } else {
                    setStatsLayoutVisibility(View.GONE, null);
                    if (System.currentTimeMillis() >
                            (Long) UpdateSPUtils.get(mActivity, UpdateSPUtils.UPDATE_NOT_SHOW, 0L)) {
                        showUpdateNotification(array.length());
                        UpdateSPUtils.put(mActivity, UpdateSPUtils.UPDATE_NOT_SHOW, System.currentTimeMillis() + 24 * 60 * 60 * 1000);
                    }
                }
                mAdapter = new UpdateAdapter(mActivity, mDataContents);
                mListView.setAdapter(mAdapter);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            LogUtils.i(TAG, "更新管理返回的JSON解析失败");
        }
    }

    /**
     * 判断StatsView显示状态
     */
    private void setStatsLayoutVisibility(int visibility, Drawable drawable) {
        switch (visibility) {
            case View.GONE:
                mStatsLayout.setVisibility(visibility);
                mListView.setVisibility(View.VISIBLE);
                break;
            case View.VISIBLE:
                mListView.setVisibility(View.GONE);
                mStatsImgView.setBackground(drawable);
                mStatsLayout.setVisibility(visibility);
                break;
        }
    }

    /**
     * 发送更新通知
     */
    private void showUpdateNotification(int number) {
        try {
            BundleContext bundleContext = BundleContextFactory.getInstance().getBundleContext();
            OSGIServiceAgent<ApkplugOSGIService> agent = new OSGIServiceAgent<ApkplugOSGIService>(
                    bundleContext, ApkplugOSGIService.class,
                    "(serviceName=" + Constant.OSGI_SERVICE_HOST_OPT + ")", //服务查询条件
                    OSGIServiceAgent.real_time);   //每次都重新查询
            agent.getService().ApkplugOSGIService(bundleContext,
                    Constant.OSGI_SERVICE_MAIN_FRAGMENT,
                    2, number);
        } catch (Exception e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        }
    }

}
