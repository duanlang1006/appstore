package com.mit.appliteupdate.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.applite.common.LogUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by LSY on 15-5-22.
 */
public class UpdateUtils {

    private static final String TAG = "UpdateUtils";

    /**
     * 得到手机里面所有的APK信息
     *
     * @param context
     * @return
     */
    public static String getAllApkData(Context context) {
        JSONArray array = new JSONArray();
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> pakageinfos = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
        for (PackageInfo pi : pakageinfos) {
            try {
                JSONObject object = new JSONObject();
                String pi_packageName = pi.packageName;
                int pi_versionCode = pi.versionCode;
                object.put("apk_packagename", pi_packageName);
                object.put("apk_versioncode", pi_versionCode);
                array.put(object);
            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.i(TAG, "更新管理得到pakageinfos的JSON数据出错");
            }
        }
        LogUtils.i(TAG, "更新管理得到pakageinfos的JSON数据:" + array.toString());
        return array.toString();
    }

}
