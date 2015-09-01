package com.mit.market.network;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.text.TextUtils;

import com.applite.common.AppliteUtils;
import com.applite.common.Constant;
import com.applite.common.LogUtils;
import com.applite.sharedpreferences.AppliteSPUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.mit.appliteupdate.bean.ApkData;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplHelper;
import com.mit.impl.ImplInfo;
import com.mit.market.UpdateNotification;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.File;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NetworkReceiver extends BroadcastReceiver {

    private static final String TAG = "NetworkReceiver";
    private static final String START_ALARM_INTENT = "app.start.update.alarm.intent";
    private Context mContext;
    private ImplAgent implAgent;
    private List<ApkData> mDataContents = new ArrayList<ApkData>();
    private HttpUtils mHttpUtils;

    public NetworkReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        //判断wifi是打开还是关闭
        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) { //此处无实际作用，只是看开关是否开启
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
            switch (wifiState) {
                case WifiManager.WIFI_STATE_DISABLED:
                    LogUtils.d(TAG, "系统关闭wifi");
                    break;
                case WifiManager.WIFI_STATE_DISABLING:
                    break;
                case WifiManager.WIFI_STATE_ENABLED:
                    LogUtils.d(TAG, "系统开启wifi");
                    break;
            }
        }

        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (NetworkInfo.State.CONNECTED == info.getState()) {//连接状态
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                //获取当前wifi名称
                LogUtils.d(TAG, "连接到WIFI:" + wifiInfo.getSSID());

                LogUtils.d(TAG, "零流量更新状态:" + AppliteSPUtils.get(mContext, AppliteSPUtils.WIFI_UPDATE_SWITCH, true));
                LogUtils.d(TAG, "当前时间:" + System.currentTimeMillis());
                LogUtils.d(TAG, "下次请求时间:" + AppliteSPUtils.get(mContext, AppliteSPUtils.UPDATE_NOT_SHOW, 0L));
                if ((boolean) AppliteSPUtils.get(mContext, AppliteSPUtils.WIFI_UPDATE_SWITCH, true))
                    if (System.currentTimeMillis() > (long) AppliteSPUtils.get(mContext, AppliteSPUtils.UPDATE_NOT_SHOW, 0L))
                        post();
            } else {
                LogUtils.d(TAG, "无网络连接");
            }
        }

        if (START_ALARM_INTENT.equals(intent.getAction())) {
            LogUtils.d(TAG, "接收定时闹钟通知");
            AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
            Intent updateTimeIntent = new Intent(START_ALARM_INTENT);
            updateTimeIntent.setClass(mContext, NetworkReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, updateTimeIntent, 0);
            am.cancel(pendingIntent);
            if (isWifi(mContext))
                post();
        }

    }

    /**
     * 判断是否是wifi连接
     */
    private boolean isWifi(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null)
            return false;
        return cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI;

    }

    private void post() {
        if (null == mHttpUtils)
            mHttpUtils = new HttpUtils();
        RequestParams params = new RequestParams();
        params.addBodyParameter("appkey", AppliteUtils.getMitMetaDataValue(mContext, Constant.META_DATA_MIT));
        params.addBodyParameter("packagename", mContext.getPackageName());
        params.addBodyParameter("type", "update_management");
        params.addBodyParameter("protocol_version", "1.0");
        params.addBodyParameter("update_info", AppliteUtils.getAllApkData(mContext));
        mHttpUtils.send(HttpRequest.HttpMethod.POST, Constant.URL, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                LogUtils.d(TAG, "更新请求成功，resulit：" + responseInfo.result);
                resolve(responseInfo.result);
            }

            @Override
            public void onFailure(HttpException e, String s) {
                LogUtils.d(TAG, "更新请求失败：" + s);
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
            long next_update_notify_times = object.getLong("next_update_notify_times") * 1000;
            String wify_update_start = object.getString("installed_wify_automatic_update_start");
            String wify_update_end = object.getString("installed_wify_automatic_update_end");
            String installed_update_list = object.getString("installed_update_list");
            ApkData bean = null;
            if (!TextUtils.isEmpty(installed_update_list)) {
                JSONArray array = new JSONArray(installed_update_list);
                for (int i = 0; i < array.length(); i++) {
                    bean = new ApkData();
                    JSONObject obj = new JSONObject(array.get(i).toString());
                    bean.setName(obj.getString("name"));
                    bean.setVersionCode(obj.getInt("versionCode"));
                    bean.setVersionName(obj.getString("versionName"));
                    bean.setIconUrl(obj.getString("iconUrl"));
                    bean.setPackageName(obj.getString("packageName"));
                    bean.setrDownloadUrl(obj.getString("rDownloadUrl"));
                    bean.setApkSize(obj.getLong("apkSize"));
                    bean.setApkMd5(obj.getString("apkMd5"));
                    mDataContents.add(bean);
                }
                if (0 == array.length() || null == array) {
                    JSONArray recommendedArray = new JSONArray();
                    UpdateNotification.getInstance().showNot(mContext, recommendedArray);
                } else {
                    UpdateNotification.getInstance().showNot(mContext, array.length() + "", array);
                    AppliteSPUtils.put(mContext, AppliteSPUtils.UPDATE_NOT_SHOW, System.currentTimeMillis() + next_update_notify_times);

                    SimpleDateFormat sDateFormat = new SimpleDateFormat("hh:mm:ss");
                    String date = sDateFormat.format(new Date());
                    int time = Integer.parseInt(date.substring(0, 2)) * 60 * 60 + Integer.parseInt(date.substring(3, 5)) * 60 + Integer.parseInt(date.substring(6, 8));
                    int time_start = Integer.parseInt(wify_update_start.substring(0, 2)) * 60 * 60 + Integer.parseInt(wify_update_start.substring(3, 5)) * 60 + Integer.parseInt(wify_update_start.substring(6, 8));
                    int time_end = Integer.parseInt(wify_update_end.substring(0, 2)) * 60 * 60 + Integer.parseInt(wify_update_end.substring(3, 5)) * 60 + Integer.parseInt(wify_update_end.substring(6, 8));
                    LogUtils.d(TAG, "当前时间：" + time + "--------自动更新时段：" + wify_update_start + "--" + wify_update_end);
                    if (time_start > time_end)//如果开始时间大于结束时间，那么结束时间就是第二天的时间
                        time_end = time_end + 24 * 60 * 60 * 1000;

                    if (time > time_start && time < time_end) {//当前时间正好在闲时
                        downloadAll();
                    } else if (time < time_start) {
                        long startTime = time_start - time + System.currentTimeMillis();
                        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
                        Intent updateTimeIntent = new Intent(START_ALARM_INTENT);
                        updateTimeIntent.setClass(mContext, NetworkReceiver.class);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, updateTimeIntent, 0);
                        am.set(AlarmManager.RTC, startTime, pendingIntent);
                    }

                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            LogUtils.d(TAG, TAG + "返回的JSON解析失败");
        }
    }

    /**
     * 下载所有需要更新的APK更新包
     */

    private void downloadAll() {
        ApkData data = null;
        for (int i = 0; i < mDataContents.size(); i++) {
            data = mDataContents.get(i);
            download(data);
        }
    }

    private void download(ApkData bean) {
        if (null == implAgent)
            implAgent = ImplAgent.getInstance(mContext.getApplicationContext());
        ImplInfo implInfo = implAgent.getImplInfo(bean.getPackageName(), bean.getPackageName(), bean.getVersionCode());
        if (null == implInfo) {
            return;
        }
        ImplHelper.updateImpl(mContext,
                implInfo,
                bean.getrDownloadUrl(),
                bean.getName(),
                bean.getIconUrl(),
                Environment.getExternalStorageDirectory() + File.separator + Constant.extenStorageDirPath + bean.getName() + ".apk",
                null,
                null);

//        implInfo.setTitle(bean.getmName()).setDownloadUrl(bean.getmUrl()).setIconUrl(bean.getmImgUrl());
//        if (ImplInfo.ACTION_DOWNLOAD == implAgent.getAction(implInfo)) {
//            switch (implInfo.getStatus()) {
//                case ImplInfo.STATUS_PENDING:
//                case ImplInfo.STATUS_RUNNING:
//                    break;
//                case ImplInfo.STATUS_PAUSED:
//                    implAgent.resumeDownload(implInfo, null);
//                    break;
//                case ImplInfo.STATUS_INSTALLED:
//                case ImplInfo.STATUS_NORMAL_INSTALLING:
//                case ImplInfo.STATUS_PRIVATE_INSTALLING:
//                    //正在安装或已安装
////                            Toast.makeText(mActivity, "该应用您已经安装过了！", Toast.LENGTH_SHORT).show();
//                    break;
//                default:
//                    implAgent.newDownload(implInfo,
//                            bean.getmUrl(),
//                            bean.getmName(),
//                            bean.getmImgUrl(),
//                            Constant.extenStorageDirPath,
//                            bean.getmName() + ".apk",
//                            true,
//                            null);
//                    break;
//            }
//        }
    }

}
