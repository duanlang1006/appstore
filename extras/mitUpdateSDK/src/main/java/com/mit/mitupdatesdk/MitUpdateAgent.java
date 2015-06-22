package com.mit.mitupdatesdk;

import com.mit.afinal.FinalHttp;
import com.mit.afinal.http.AjaxCallBack;
import com.mit.afinal.http.AjaxParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mit.utils.ConstantUtils;
import com.mit.utils.LogUtils;
import com.mit.utils.SPUtils;
import com.mit.utils.Utils;
import com.umeng.update.UmengUpdateAgent;

import android.content.Context;
import android.text.TextUtils;

public class MitUpdateAgent {

    private static final String TAG = "MitUpdateAgent";
    private static final String UMengUpdateClass = "com.umeng.update.UmengUpdateAgent";

    public static void update(Context context) {
        if (isClass()) {
            if (System.currentTimeMillis() / 1000 >
                    Long.parseLong(SPUtils.get(context, SPUtils.UPDATE_TIME, 0L,
                    SPUtils.NEXT_TIME_FILE_NAME).toString())) {
                post(context);
            }
        }
    }

    private static void post(final Context context) {
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
        params.put("type", "update2");
        mFinalHttp.post(url, params, new AjaxCallBack<Object>() {
            @Override
            public void onSuccess(Object t) {
                String result = (String) t;
                LogUtils.i(TAG, "更新请求成功：result = " + result);
                resolve(context, result);
            }

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                super.onFailure(t, errorNo, strMsg);
                LogUtils.e(TAG, "更新请求失败:" + strMsg);
            }
        });
    }

    /**
     * MitUpdate数据解析
     *
     * @param context
     * @param data
     */
    private static void resolve(Context context, String data) {
        try {
            JSONObject object = new JSONObject(data);
            String url = object.getString("url");
            String inner_version = object.getString("inner_version");
            int app_key = object.getInt("app_key");
            String info = object.getString("info");

            SPUtils.put(context, SPUtils.URL, url, SPUtils.SDK_FILE_NAME);

            JSONArray array = new JSONArray(inner_version);
            String s = null;
            for (int i = 0; i < array.length(); i++) {
                if (i == 0) {
                    s = array.getString(i);
                } else {
                    s = s + "|" + array.getString(i);
                }
            }
            LogUtils.i(TAG, "保存在SPUtils里面的inner_version值:" + s);
            SPUtils.put(context, SPUtils.INNER_VERSION, s, SPUtils.SDK_FILE_NAME);

            JSONObject obj = new JSONObject(info);
            String mVersion = obj.getString("version");
            int mAllow = obj.getInt("allow");
            long mTime = obj.getLong("next");
            SPUtils.put(context, SPUtils.UPDATE_TIME, mTime, SPUtils.NEXT_TIME_FILE_NAME);
            if (mAllow == 1) {
                UmengUpdateAgent.update(context);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "MitUpdate返回值解析失败");
        }
    }

    /**
     * 判断友盟更新SDK是否存在
     *
     * @return
     */
    private static boolean isClass() {
        try {
            Class.forName(UMengUpdateClass);
            LogUtils.i(TAG, "友盟更新SDK存在");
            return true;
        } catch (ClassNotFoundException e) {
            LogUtils.e(TAG, "友盟更新SDK不存在");
            return false;
        }
    }

    /**
     * 设置Debug模式 默认false
     */
    public static void setDebug(boolean bool) {
        LogUtils.isDebug = bool;
    }

}
