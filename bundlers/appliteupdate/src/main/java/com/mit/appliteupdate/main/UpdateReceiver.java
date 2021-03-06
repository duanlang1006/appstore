package com.mit.appliteupdate.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;

import com.applite.bean.ApkBean;
import com.applite.common.AppliteUtils;
import com.applite.common.Constant;
import com.applite.common.LogUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mit.afinal.FinalHttp;
import com.mit.afinal.http.AjaxCallBack;
import com.mit.afinal.http.AjaxParams;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplHelper;
import com.mit.impl.ImplInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class UpdateReceiver extends BroadcastReceiver {
    private static final String TAG = "UpdateReceiver";
    //    HttpUtils mHttpUtils = new HttpUtils();
    FinalHttp mFinalHttp = new FinalHttp();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        LogUtils.d(TAG, "onReceiver,action=" + action);
        //由wifi网络下检查更新处理，这里不再处理
//        if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
//            String packageName = intent.getData().getSchemeSpecificPart();
//            if (ImplHelper.packageInstalled(context,"com.android.installer")
//                    || ImplHelper.packageInstalled(context,"com.android.dbservices")){
//                new UpdateCheckTask().execute(context,packageName);
//            }
//        }
    }

    class UpdateCheckTask extends AsyncTask<Object, Object, String> {
        private Context mContext;
        private String packageName;
        private int versionCode;
        List<PackageInfo> packageInfos = new ArrayList<PackageInfo>();

        @Override
        protected String doInBackground(Object... params) {
            mContext = (Context) params[0];
            packageName = (String) params[1];

            String md5 = null;
            try {
                PackageInfo installed = mContext.getPackageManager()
                        .getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
                ApplicationInfo appInfo = installed.applicationInfo;
                String sourceDir = appInfo.publicSourceDir;
                md5 = ImplHelper.getFileMD5(new File(sourceDir));
                versionCode = installed.versionCode;
                packageInfos.add(installed);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return md5;
        }

        @Override
        protected void onPostExecute(final String md5) {
            super.onPostExecute(md5);
            if (null == md5 || TextUtils.isEmpty(md5)) {
                return;
            }
            ImplAgent implAgent = ImplAgent.getInstance(mContext.getApplicationContext());
            final ImplInfo implInfo = implAgent.getImplInfo(packageName, packageName, versionCode);
            if (null != implInfo && md5.equals(implInfo.getMd5())) {
                return;
            }
            //网络请求
//            RequestParams params = new RequestParams();
//            params.addBodyParameter("appkey", AppliteUtils.getMitMetaDataValue(mContext, Constant.META_DATA_MIT));
//            params.addBodyParameter("packagename", mContext.getPackageName());
//            params.addBodyParameter("type", "update_management");
//            params.addBodyParameter("protocol_version", Constant.PROTOCOL_VERSION);
//            params.addBodyParameter("update_info", AppliteUtils.encodePackages(packageInfos));
//            mHttpUtils.send(HttpRequest.HttpMethod.POST, Constant.URL, params, new RequestCallBack<String>() {
//                @Override
//                public void onSuccess(ResponseInfo<String> responseInfo) {
//                    try {
//                        JSONObject object = new JSONObject(responseInfo.result);
//                        String installed_update_list = object.getString("installed_update_list");
//                        Type listType = new TypeToken<List<ApkBean>>() {
//                        }.getType();
//                        List<ApkBean> beans = new Gson().fromJson(installed_update_list, listType);
//                        if (null != beans && beans.size() > 0) {
//                            for (int i = 0; i < beans.size(); i++) {
//                                ApkBean bean = beans.get(i);
//                                LogUtils.d(TAG, md5 + "," + bean.getName() + "," + bean.getApkMd5());
//                                if (packageName.equals(bean.getPackageName()) && !md5.equals(bean.getApkMd5())) {
//                                    ImplAgent.getInstance(mContext.getApplicationContext()).newDownload(
//                                            implInfo,
//                                            bean.getrDownloadUrl(),
//                                            bean.getName(),
//                                            bean.getIconUrl(),
//                                            Environment.getExternalStorageDirectory() + File.separator + Constant.extenStorageDirPath + bean.getName() + ".apk",
//                                            bean.getApkMd5(),
//                                            true,
//                                            null);
//                                }
//                            }
//                        }
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                @Override
//                public void onFailure(HttpException e, String s) {
//                }
//
//            });

            AjaxParams params = new AjaxParams();
            params.put("appkey", AppliteUtils.getMitMetaDataValue(mContext, Constant.META_DATA_MIT));
            params.put("packagename", mContext.getPackageName());
            params.put("type", "update_management");
            params.put("protocol_version", Constant.PROTOCOL_VERSION);
            params.put("update_info", AppliteUtils.encodePackages(packageInfos));
            mFinalHttp.post(Constant.URL, params, new AjaxCallBack<String>() {
                @Override
                public void onSuccess(String responseInfo) {
                    try {
                        JSONObject object = new JSONObject(responseInfo);
                        String installed_update_list = object.getString("installed_update_list");
                        Type listType = new TypeToken<List<ApkBean>>() {
                        }.getType();
                        List<ApkBean> beans = new Gson().fromJson(installed_update_list, listType);
                        if (null != beans && beans.size() > 0) {
                            for (int i = 0; i < beans.size(); i++) {
                                ApkBean bean = beans.get(i);
                                LogUtils.d(TAG, md5 + "," + bean.getName() + "," + bean.getApkMd5());
                                if (packageName.equals(bean.getPackageName()) && !md5.equals(bean.getApkMd5())) {
                                    ImplAgent.getInstance(mContext.getApplicationContext()).newDownload(
                                            implInfo,
                                            bean.getrDownloadUrl(),
                                            bean.getName(),
                                            bean.getIconUrl(),
                                            Environment.getExternalStorageDirectory() + File.separator + Constant.extenStorageDirPath + bean.getName() + ".apk",
                                            bean.getApkMd5(),
                                            true,
                                            null);
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Throwable t, int errorNo, String strMsg) {
                    LogUtils.i(TAG, "请求失败：" + strMsg);
                }
            });


        }
    }
}
