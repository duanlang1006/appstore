package com.mit.appliteupdate;

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
import android.widget.ListView;
import android.widget.TextView;

import com.applite.common.Constant;
import com.mit.appliteupdate.adapter.UpdateAdapter;
import com.mit.appliteupdate.bean.DataBean;
import com.mit.appliteupdate.utils.LogUtils;
import com.mit.appliteupdate.utils.Utils;
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
    private ImplListener mImplListener = new ImplListener() {
        @Override
        public void onDownloadComplete(boolean b, ImplAgent.DownloadCompleteRsp downloadCompleteRsp) {

        }

        @Override
        public void onDownloadUpdate(boolean b, ImplAgent.DownloadUpdateRsp downloadUpdateRsp) {

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

    public UpdateFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
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
            ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
            actionBar.hide();
//            mContext = context;
        } catch (Exception e) {
            e.printStackTrace();
//            mContext = mActivity;
        }
        if (null == mInflater) {
            mInflater = inflater;
        }
//        this.container = container;

        rootView = mInflater.inflate(R.layout.fragment_update, container, false);
        initView();
        post();
        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ImplAgent.unregisterImplListener(mImplListener);
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
                                Utils.extenStorageDirPath,
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
                }
                break;
        }
    }

    private void initView() {
        mAllUpdateView = (TextView) rootView.findViewById(R.id.update_all_update);
        mListView = (ListView) rootView.findViewById(R.id.update_listview);

        mAllUpdateView.setOnClickListener(this);
    }

    private void post() {
        AjaxParams params = new AjaxParams();
        params.put("appkey", Utils.getMitMetaDataValue(mActivity, Utils.META_DATA_MIT));
        params.put("packagename", mActivity.getPackageName());
        params.put("app", "applite");
        params.put("type", "update_management");
        params.put("update_info", Utils.getAllApkData(mActivity));
        mFinalHttp.post(Utils.URL, params, new AjaxCallBack<Object>() {
            @Override
            public void onSuccess(Object o) {
                super.onSuccess(o);
                String resulit = (String) o;
                LogUtils.i(TAG, "更新请求成功，resulit：" + resulit);
                resolve(resulit);
            }

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                super.onFailure(t, errorNo, strMsg);
                LogUtils.i(TAG, "更新请求失败，strMsg：" + strMsg);
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
                    mDataContents.add(bean);
                }
            }
            mAdapter = new UpdateAdapter(mActivity, mDataContents);
            mListView.setAdapter(mAdapter);
        } catch (JSONException e) {
            e.printStackTrace();
            LogUtils.i(TAG, "更新管理返回的JSON解析失败");
        }
    }
}
