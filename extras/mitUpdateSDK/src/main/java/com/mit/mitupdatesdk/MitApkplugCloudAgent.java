package com.mit.mitupdatesdk;

import android.content.Context;
import android.text.TextUtils;

import com.mit.afinal.FinalHttp;
import com.mit.afinal.http.AjaxCallBack;
import com.mit.afinal.http.AjaxParams;
import com.mit.bean.ApkplugDownloadCallback;
import com.mit.bean.ApkplugModel;
import com.mit.bean.ApkplugQueryModel;
import com.mit.bean.ApkplugUpdateBean;
import com.mit.bean.ApkplugUpdateCallback;
import com.mit.bean.ApkplugUpdateInfo;
import com.mit.utils.ConstantUtils;
import com.mit.utils.LogUtils;
import com.mit.utils.SPUtils;
import com.mit.utils.Utils;
import com.umeng.update.UmengUpdateAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

public class MitApkplugCloudAgent {

    private static final String TAG = "MitApkplugCloudAgent";

    public static void checkupdate(Context context, ApkplugUpdateBean bean, ApkplugUpdateCallback callback) {
        String url = null;
        if (TextUtils.isEmpty((String) SPUtils.get(context, SPUtils.URL, url, SPUtils.SDK_FILE_NAME))) {
            url = ConstantUtils.URL;
        } else {
            url = (String) SPUtils.get(context, SPUtils.URL, url, SPUtils.SDK_FILE_NAME);
        }

        FinalHttp mFinalHttp = new FinalHttp();
        AjaxParams params = new AjaxParams();
        params.put("device_info", Utils.getDeviceInfoStr(context));
        params.put("dsc_info", DscInfo.getDscInfoStr(context));
        params.put("packagename", context.getPackageName());
        params.put("appkey", Utils.getMitMetaDataValue(context));
        params.put("app_version", Utils.getVersionName(context));
        params.put("type", "update_bundles");
        params.put("plugs_info", getPluginsInfo(bean));

        mFinalHttp.post(url, params, new PlugUpdateAjaxCallBack<Object>(context, callback));
    }

    public static void download(Context context, ApkplugModel model, ApkplugDownloadCallback callback) {

    }

    public static void download(Context context, ApkplugQueryModel<ApkplugModel> apkplugModel, ApkplugDownloadCallback callback) {
//        List<ApkplugModel> data = apkplugModel.getData();
//        for (int i = 0;i < data.size(); i++){
//            ApkplugModel model = data.get(i);
        new FinalHttp().download(
                "http://192.168.1.157/applitehomepage.apk",
                "/sdcard/.android/applitehomepage.apk",
                false,
                new MyAjaxCallBack<File>(callback).progress(true, 1000));
//        }
    }

    /**
     * 得到plugs_info
     *
     * @return
     */
    private static String getPluginsInfo(ApkplugUpdateBean bean) {
        JSONArray array = new JSONArray();
        List<ApkplugUpdateInfo> infos = bean.getApps();
        for (int i = 0; i < infos.size(); i++) {
            JSONObject object = new JSONObject();
            String appid = infos.get(i).appid;
            String bundlevarsion = infos.get(i).bundlevarsion;
            int versionCode = infos.get(i).versionCode;
            try {
                object.put("appid", appid);
                object.put("bundlevarsion", bundlevarsion);
                object.put("versionCode", versionCode);
                array.put(object);
            } catch (JSONException e) {
                e.printStackTrace();
                LogUtils.e(TAG, "plugs_info-JSON异常");
            }
        }
        LogUtils.i(TAG, "plugs_info:" + array);
        return array.toString();
    }

}
