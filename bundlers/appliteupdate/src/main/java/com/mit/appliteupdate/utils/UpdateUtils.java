package com.mit.appliteupdate.utils;

import com.applite.sharedpreferences.AppliteSPUtils;
import com.applite.similarview.SimilarBean;
import com.mit.appliteupdate.bean.ApkData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by LSY on 15-9-9.
 */
public class UpdateUtils {

    public static String listTojson(List<ApkData> mUpdateApkList, List<SimilarBean> mSimilarDataList) {
        JSONObject object = new JSONObject();
        try {
            JSONArray array = new JSONArray();
            for (int i = 0; i < mUpdateApkList.size(); i++) {
                JSONObject obj = new JSONObject();
                obj.put("packageName", mUpdateApkList.get(i).getPackageName());
                obj.put("name", mUpdateApkList.get(i).getName());
                obj.put("iconUrl", mUpdateApkList.get(i).getIconUrl());
                obj.put("versionName", mUpdateApkList.get(i).getVersionName());
                obj.put("versionCode", mUpdateApkList.get(i).getVersionCode());
                obj.put("rDownloadUrl", mUpdateApkList.get(i).getrDownloadUrl());
                obj.put("apkSize", mUpdateApkList.get(i).getApkSize());
                obj.put("apkMd5", mUpdateApkList.get(i).getApkMd5());
                obj.put("updateInfo", mUpdateApkList.get(i).getUpdateInfo());
                obj.put("updateTime", mUpdateApkList.get(i).getUpdateTime());
                array.put(obj);
            }
            JSONArray array1 = new JSONArray();
            for (int i = 0; i < mSimilarDataList.size(); i++) {
                JSONObject obj = new JSONObject();
                obj.put("packageName", mSimilarDataList.get(i).getPackageName());
                obj.put("name", mSimilarDataList.get(i).getName());
                obj.put("iconUrl", mSimilarDataList.get(i).getIconUrl());
                obj.put("versionCode", mSimilarDataList.get(i).getVersionCode());
                obj.put("rDownloadUrl", mSimilarDataList.get(i).getrDownloadUrl());
                array1.put(obj);
            }

            object.put("app_key", 1);
            object.put("installed_update_list", array);
            object.put("similar_info", array1);
            return object.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
