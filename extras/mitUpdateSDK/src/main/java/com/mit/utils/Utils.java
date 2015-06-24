package com.mit.utils;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

public class Utils {

    private static final String TAG = "Utils";
    private static final String PATH = "applite/";

    /**
     * 设备信息
     */
    public static String getDeviceInfoStr(Context context) {
        JSONObject device_info = new JSONObject();
        try {
            device_info.put("name", getDeviceName());
            device_info.put("uuid", getDeviceUuid(context));
            device_info.put("imei", getImei(context));
            device_info.put("imsi", getImsi(context));
            device_info.put("telephone", getPhoneNumber(context));
            device_info.put("channel", getAppMetaString(context, "UMENG_CHANNEL"));
            device_info.put("version", getDeviceSwVersion());
            LogUtils.i(TAG, "设备信息:" + device_info);
        } catch (JSONException e) {
            LogUtils.e(TAG, "device_info-JSON异常");
            e.printStackTrace();
        }
        return device_info.toString();
    }

    public static String getDeviceUuid(Context context) {
        android.telephony.TelephonyManager tm = (android.telephony.TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String device_id = tm.getDeviceId();
        android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        String mac = wifi.getConnectionInfo().getMacAddress();
        if (null == mac) {
            mac = "00:00:00:00:00";
        }
        if (TextUtils.isEmpty(device_id)) {
            device_id = mac;
        }
        if (TextUtils.isEmpty(device_id)) {
            device_id = android.provider.Settings.Secure.getString(context.getContentResolver(),
                    android.provider.Settings.Secure.ANDROID_ID);
        }
        return new UUID(device_id.hashCode(), mac.hashCode()).toString();
    }

    public static String getDeviceName() {
        String name = Build.BRAND + "_" + Build.DEVICE + "_" + Build.DISPLAY;
        return name;
    }

    public static String getImei(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String imei = tm.getDeviceId();
        return imei + "";
    }

    public static String getImsi(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String imsi = tm.getSubscriberId();
        return imsi + "";
    }

    public static String getPhoneNumber(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String tel = tm.getLine1Number();
        return tel + "";
    }

    public static String getSdkVersion() {
        int sdkVersion = android.os.Build.VERSION.SDK_INT;
        return sdkVersion + "";
    }

    public static String getSwVersion(Context context) {
        String versionName = "";
        PackageInfo pkg;
        try {
            pkg = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionName = pkg.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionName + "";
    }

    public static String getAppMetaString(Context ctx, String key) {
        String ret = "xxxx";
        if (ctx == null || TextUtils.isEmpty(key)) {
            return ret;
        }
        try {
            PackageManager packageManager = ctx.getPackageManager();
            if (packageManager != null) {
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(ctx.getPackageName(),
                        PackageManager.GET_META_DATA);
                if (applicationInfo != null && applicationInfo.metaData != null) {
                    ret = applicationInfo.metaData.getString(key);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static String getDeviceSwVersion() {
        // return Build.VERSION.INCREMENTAL + "|" +
        // SystemProperties.get("ro.product.ly.inward.version") + "|"
        return Build.VERSION.INCREMENTAL + "|"
                + invoke("android.os.SystemProperties", "get", "ro.product.ly.inward.version") + "|"
                + android.os.Build.VERSION.SDK_INT;
    }

    /**
     * 获取应用程序名称
     */
    public static String getAppName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return context.getResources().getString(labelRes);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * [获取应用程序版本名称信息]
     *
     * @param context
     * @return 当前应用的版本名称
     */
    public static String getVersionName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Map转JSON
     *
     * @param map
     * @return
     */
    public static String MapToJsonStr(Map<String, Integer> map) {
        if (map == null || map.isEmpty()) {
            return "null";
        }
        String jsonStr = "{";
        Set<?> keySet = map.keySet();
        for (Object key : keySet) {
            jsonStr += "\"" + key + "\":\"" + map.get(key) + "\",";
        }
        jsonStr = jsonStr.substring(0, jsonStr.length() - 1);
        jsonStr += "}";
        LogUtils.i(TAG, jsonStr);
        return jsonStr;
    }

    /**
     * 得到AndroidManifest.xml里面MetaData的Value值
     *
     * @param context
     * @return
     */
    public static String getMitMetaDataValue(Context context) {
        ApplicationInfo appInfo = null;
        String MetaDataValue = null;
        try {
            appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);
            MetaDataValue = appInfo.metaData.getString(ConstantUtils.MIT_APPKEY);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null != MetaDataValue) {
            LogUtils.i(TAG, MetaDataValue);
        }
        return MetaDataValue;
    }

    /**
     * 利用反射调用@hide的方法
     *
     * @param ClassName
     * @param MethodName
     * @param s
     * @return
     */
    public static Object invoke(String ClassName, String MethodName, String s) {
        try {
            // 获取相应的类对象名称
            Class<?> classType = Class.forName(ClassName);
            // 返回本类对象
            Object invokeOperation = classType.newInstance();
            // 根据类对象名称去查找对应的方法
            Method method = classType.getMethod(MethodName, String.class);
            // 调用查找 到的方法执行此方法的处理
            Object result = method.invoke(invokeOperation, s);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 下载保存路径
     *
     * @param filename
     * @return
     */
    public static String getAppDir(String filename) {
        String path = "";
        String state = android.os.Environment.getExternalStorageState();
        if (android.os.Environment.MEDIA_MOUNTED.equals(state)
                && android.os.Environment.getExternalStorageDirectory().canWrite()) {
            path = android.os.Environment.getExternalStorageDirectory().getPath();
            if (!path.endsWith("/")) {
                path += "/";
            }
            path += PATH;
            if (!path.endsWith("/")) {
                path += "/";
            }
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        path += filename;
        return path;
    }

}
