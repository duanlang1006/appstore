package com.mit.mitupdatesdk;

import java.util.Map;

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
import com.umeng.analytics.MobclickAgent;

import android.content.Context;
import android.text.TextUtils;

public class MitMobclickAgent {

    private static final String TAG = "MitMobclickAgent";
    private static final String UMengMobclickClass = "com.umeng.analytics.MobclickAgent";
    private static boolean bool = true;
    /**
     * POST请求前数据统计的值
     */
    private static Map<String, Integer> mSPMapPostAgo;
    /**
     * POST请求成功数据统计的值
     */
    private static Map<String, Integer> mSPMapPostAfter;

    /**
     * 数据统计
     *
     * @param context
     * @param eventId
     */
    public static void onEvent(Context context, String eventId) {
        if (isClass()) {
            MobclickAgent.onEvent(context, eventId);// 友盟统计
        }
        if (SPUtils.contains(context, eventId, SPUtils.MOBCLICK_FILE_NAME)) {
            int value = (Integer) SPUtils.get(context, eventId, 0, SPUtils.MOBCLICK_FILE_NAME) + 1;
            SPUtils.put(context, eventId, value, SPUtils.MOBCLICK_FILE_NAME);
        } else {
            SPUtils.put(context, eventId, 1, SPUtils.MOBCLICK_FILE_NAME);
        }
        if (bool) {
            if (System.currentTimeMillis() > Long.parseLong(SPUtils.get(context, SPUtils.MOBCLICK_TIME, 0L,
                    SPUtils.NEXT_TIME_FILE_NAME).toString())
                    && !TextUtils.isEmpty(getAllStr(context))) {
                post(context);
            }
            bool = false;
        }
    }

    /**
     * 请求网络
     *
     * @param context
     */
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
        params.put("data_info", getAllStr(context));
        params.put("appkey", Utils.getMitMetaDataValue(context));
        params.put("type", "data2");
        params.put("app_version", Utils.getVersionName(context));
        mSPMapPostAgo = getAllMap(context);

        mFinalHttp.post(url, params, new AjaxCallBack<Object>() {
            @Override
            public void onSuccess(Object t) {
                super.onSuccess(t);
                String result = (String) t;
                LogUtils.i(TAG, "数据统计请求成功：result = " + result);
                resolve(context, result);
            }

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                super.onFailure(t, errorNo, strMsg);
                LogUtils.e(TAG, "数据统计请求失败:" + strMsg);
            }
        });
    }

    /**
     * 解析返回的JSON
     *
     * @param context
     * @param data
     */
    private static void resolve(Context context, String data) {
        try {
            JSONObject object = new JSONObject(data);
            String url = object.getString("url");
            int appkey = object.getInt("app_key");
            String inner_version = object.getString("inner_version");
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
            int state = obj.getInt("status");
            long next = obj.getInt("next") * 60 * 60 * 1000 + System.currentTimeMillis();
            if (state == 1) {
                mSPMapPostAfter = getAllMap(context);
                if (Utils.MapToJsonStr(mSPMapPostAgo).equals(Utils.MapToJsonStr(mSPMapPostAfter))) {
                    SPUtils.clear(context, SPUtils.MOBCLICK_FILE_NAME);
                } else {
                    clearSameKeySPData(context);
                }
                SPUtils.put(context, SPUtils.MOBCLICK_TIME, next, SPUtils.NEXT_TIME_FILE_NAME);

            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e(TAG, "数据统计返回值解析失败");
        }
    }

    /**
     * 得到数据统计里面的所有键值对 转成String
     *
     * @param context
     * @return String
     */
    @SuppressWarnings("unchecked")
    private static String getAllStr(Context context) {
        Map<String, Integer> map = (Map<String, Integer>) SPUtils.getAll(context, SPUtils.MOBCLICK_FILE_NAME);
        return Utils.MapToJsonStr(map);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void getAllJsonStr(Context context) {
        Map<String, Integer> map = (Map<String, Integer>) SPUtils.getAll(context, SPUtils.MOBCLICK_FILE_NAME);
        JSONObject data_info = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        try {
            for (Map.Entry entry : map.entrySet()) {
                String key = (String) entry.getKey();
                int value = (Integer) entry.getValue();

                JSONObject data = new JSONObject();
                data.put("key", key);
                data.put("value", value);
                jsonArray.put(data);
            }
            data_info.put("date_info", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        LogUtils.i(TAG, data_info.toString());
    }

    /**
     * 得到数据统计里面的所有键值对
     *
     * @param context
     * @return Map
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Integer> getAllMap(Context context) {
        return (Map<String, Integer>) SPUtils.getAll(context, SPUtils.MOBCLICK_FILE_NAME);
    }

    /**
     * 把当前的Value值减去已经post上去的值 然后设置到SharedPreferences里面去
     *
     * @param context
     */
    @SuppressWarnings("rawtypes")
    private static void clearSameKeySPData(Context context) {
        for (Map.Entry entry : mSPMapPostAgo.entrySet()) {
            String mSPMapPostAgoKey = (String) entry.getKey();
            int mSPMapPostAgoValue = (Integer) entry.getValue();
            int spData = mSPMapPostAfter.get(mSPMapPostAgoKey) - mSPMapPostAgoValue;
            SPUtils.put(context, mSPMapPostAgoKey, spData, SPUtils.MOBCLICK_FILE_NAME);
        }
    }

    /**
     * 判断友盟更新SDK是否存在
     *
     * @return
     */
    private static boolean isClass() {
        try {
            Class.forName(UMengMobclickClass);
            LogUtils.i(TAG, "友盟数据统计SDK存在");
            return true;
        } catch (ClassNotFoundException e) {
            LogUtils.e(TAG, "友盟数据统计SDK不存在");
            return false;
        }
    }

}
