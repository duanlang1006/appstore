package com.mit.appliteupdate.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.applite.bean.ApkBean;
import com.applite.common.LogUtils;
import com.applite.sharedpreferences.AppliteSPUtils;
import com.applite.similarview.SimilarBean;
import com.google.gson.Gson;
import com.mit.appliteupdate.bean.UpdateData;
import com.mit.appliteupdate.utils.UpdateUtils;

import java.util.List;

/**
 * Created by LSY on 15-10-14.
 */
public class UninstallReceiver extends BroadcastReceiver {

    private static final String TAG = "UninstallReceiver";
    private Gson mGson = new Gson();
    private List<ApkBean> mUpdateApkList;
    private List<SimilarBean> mSimilarDataList;

    @Override
    public void onReceive(Context context, Intent intent) {
        //接收卸载广播
        String action = intent.getAction();
        if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
            int position = -1;
            String packageName = intent.getDataString();
            String data = (String) AppliteSPUtils.get(context, AppliteSPUtils.UPDATE_DATA, "");
            UpdateData updateData = mGson.fromJson(data, UpdateData.class);
            if (null != updateData) {
                mUpdateApkList = updateData.getInstalled_update_list();
                mSimilarDataList = updateData.getSimilar_info();
            }
            if (null != mUpdateApkList && !mUpdateApkList.isEmpty()) {
                for (int i = 0; i < mUpdateApkList.size(); i++) {
                    if (packageName.equals("package:" + mUpdateApkList.get(i).getPackageName())) {
                        position = i;
                    }
                }
                if (position != -1) {
                    mUpdateApkList.remove(position);
                    LogUtils.d(TAG, "检测到卸载，SharedPreferences数据修改");
                    AppliteSPUtils.put(context, AppliteSPUtils.UPDATE_DATA, UpdateUtils.listTojson(mUpdateApkList, mSimilarDataList));
                }
            }
        }
    }
}
