package com.mit.mitupdatesdk;

import android.content.Context;
import android.text.TextUtils;

import com.mit.afinal.http.AjaxCallBack;
import com.mit.bean.ApkplugModel;
import com.mit.bean.ApkplugQueryModel;
import com.mit.bean.ApkplugUpdateCallback;
import com.mit.utils.LogUtils;
import com.mit.utils.SPUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LSY on 15-6-3.
 */
public class PlugUpdateAjaxCallBack<T> extends AjaxCallBack<Object> {

    private static final String TAG = "MitApkplugCloudAgent";
    private static ApkplugQueryModel<ApkplugModel> queryModel;
    private static Context context;
    private static ApkplugUpdateCallback callback;

    public PlugUpdateAjaxCallBack(Context context, ApkplugUpdateCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    @Override
    public void onSuccess(Object o) {
        super.onSuccess(o);
        String reuslt = (String) o;
        LogUtils.i(TAG, "插件更新请求成功，reuslt:" + reuslt);
        resolve(context, reuslt);
    }

    @Override
    public void onFailure(Throwable t, int errorNo, String strMsg) {
        super.onFailure(t, errorNo, strMsg);
        LogUtils.e(TAG, "插件更新请求失败，strMsg:" + strMsg);
        if (callback != null) {
            callback.onFailure(errorNo, strMsg);
        }
    }

    /**
     * 解析返回的JSON
     *
     * @param context
     * @param reuslt
     */
    private static void resolve(Context context, String reuslt) {
        try {
            JSONObject object = new JSONObject(reuslt);
            int app_key = object.getInt("app_key");
            String url = object.getString("url");
            String inner_version = object.getString("innerversion");
            String bundles = object.getString("Bundles");

            SPUtils.put(context, SPUtils.URL, url, SPUtils.SDK_FILE_NAME);

            JSONArray array = new JSONArray(inner_version);
            String s = "";
            for (int i = 0; i < array.length(); i++) {
                if (i == 0) {
                    s = array.getString(i);
                } else {
                    s = s + "|" + array.getString(i);
                }
            }
            LogUtils.i(TAG, "保存在SPUtils里面的inner_version值:" + s);
            SPUtils.put(context, SPUtils.INNER_VERSION, s, SPUtils.SDK_FILE_NAME);

            queryModel = new ApkplugQueryModel<ApkplugModel>();
            List<ApkplugModel> list = new ArrayList<ApkplugModel>();

            LogUtils.d(TAG,bundles);
            JSONObject obj = new JSONObject(bundles);
            String data = obj.getString("data");
            if (!TextUtils.isEmpty(data)) {
                JSONArray json = new JSONArray(data);
                for (int i = 0; i < json.length(); i++) {
                    ApkplugModel model = new ApkplugModel();
                    JSONObject jsonObject = new JSONObject(json.get(i).toString());
                    model.setAppid(jsonObject.getString("appid"));
                    model.setAppname(jsonObject.getString("apkname"));
                    model.setPackageName(jsonObject.getString("packageName"));
                    model.setVersionCode(jsonObject.getInt("versionCode"));
                    model.setVersionName(jsonObject.getString("versionName"));
                    model.setBundlename(jsonObject.getString("BundleName"));
                    model.setBundlevarsion(jsonObject.getString("BundleVersion"));
                    model.setSymbolicName(jsonObject.getString("BundleSymbolicName"));
                    model.setMd5(jsonObject.getString("md5"));
                    model.setMinSdkVersion(jsonObject.getInt("minSdkVersion"));
                    model.setSdkVersion(jsonObject.getInt("sdkVersion"));
                    model.setTargetSdkVersion(jsonObject.getInt("targetSdkVersion"));
                    model.setType(jsonObject.getInt("type"));
                    model.setInfo(jsonObject.getString("info"));
                    model.setPlugurl(jsonObject.getString("plugurl"));
                    model.setSize(jsonObject.getLong("size"));
                    list.add(model);
                }
                queryModel.setTotalRows(obj.getInt("totalRows"));
                queryModel.setPage(obj.getInt("page"));
                queryModel.setTotlepage(obj.getInt("totalPage"));
                queryModel.setData(list);
            }

            if (callback != null) {
                callback.onSuccess(ApkplugUpdateCallback.success, queryModel);
            }

        } catch (Exception e) {
            LogUtils.e(TAG, "插件更新JSON解析失败");
            e.printStackTrace();

            if (callback != null) {
                callback.onSuccess(ApkplugUpdateCallback.msg_exp_fail, queryModel);
            }
        }
    }

}
