package com.mit.applite.search.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.mit.applite.search.R;
import com.mit.applite.search.view.ProgressButton;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by LSY on 15-5-22.
 */
public class Utils {

    public static final String URL = "http://192.168.1.104/app_interface/app_main_interface.php";
    private static final String TAG = "Utils";
    public static final String META_DATA_MIT = "MIT_APPKEY";
    public static final String extenStorageDirPath = ".android/";
    public static final int INSTALLED = 0; // 表示已经安装，且跟现在这个apk文件是一个版本
    public static final int UNINSTALLED = 1; // 表示未安装
    public static final int INSTALLED_UPDATE = 2; // 表示已经安装，版本比现在这个版本要低，可以点击按钮更新

    /**
     * 得到AndroidManifest.xml里面MetaData的Value值
     *
     * @param context
     * @return
     */
    public static String getMitMetaDataValue(Context context, String name) {
        ApplicationInfo appInfo;
        String MetaDataValue = null;
        try {
            appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);
            MetaDataValue = appInfo.metaData.getString(name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null != MetaDataValue) {
            LogUtils.i(TAG, MetaDataValue);
        }
        return MetaDataValue;
    }

    /**
     * byte(字节)根据长度转成kb(千字节)和mb(兆字节)
     *
     * @param bytes
     * @return
     */
    public static String bytes2kb(long bytes) {
        BigDecimal filesize = new BigDecimal(bytes);
        BigDecimal megabyte = new BigDecimal(1024 * 1024);
        float returnValue = filesize.divide(megabyte, 2, BigDecimal.ROUND_UP)
                .floatValue();
        if (returnValue > 1)
            return (returnValue + "MB");
        BigDecimal kilobyte = new BigDecimal(1024);
        returnValue = filesize.divide(kilobyte, 2, BigDecimal.ROUND_UP)
                .floatValue();
        return (returnValue + "KB");
    }

    public static String getDownloadNumber(Context context, int number) {
        String s = null;
        if (number > 1000000) {
            s = ">100" + context.getResources().getString(R.string.wan);
        } else if (number > 500000) {
            s = ">50" + context.getResources().getString(R.string.wan);
        } else if (number > 300000) {
            s = ">30" + context.getResources().getString(R.string.wan);
        } else if (number > 200000) {
            s = ">20" + context.getResources().getString(R.string.wan);
        } else if (number > 100000) {
            s = ">10" + context.getResources().getString(R.string.wan);
        } else if (number <= 100000) {
            s = number + "";
        }
        return s;
    }

    /**
     * 判断该应用在手机中的安装情况
     *
     * @param context
     * @param packageName 要判断应用的包名
     * @param versionCode 要判断应用的版本号
     */
    public static int doType(Context context, String packageName, int versionCode) {
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> pakageinfos = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
        for (PackageInfo pi : pakageinfos) {
            String pi_packageName = pi.packageName;
            int pi_versionCode = pi.versionCode;
            //如果这个包名在系统已经安装过的应用中存在
            if (packageName.endsWith(pi_packageName)) {
                if (versionCode == pi_versionCode) {
                    LogUtils.i(TAG, "已经安装，不用更新，可以卸载该应用");
                    return INSTALLED;
                } else if (versionCode > pi_versionCode) {
                    LogUtils.i(TAG, "已经安装，有更新");
                    return INSTALLED_UPDATE;
                }
            }
        }
        LogUtils.i(TAG, "未安装该应用，可以安装");
        return UNINSTALLED;
    }

    /**
     * 判断文件是否存在
     *
     * @param path
     * @return
     */
    public static boolean fileIsExists(String path) {
        try {
            File f = new File(path);
            if (!f.exists()) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 判断该应用在手机中的安装情况
     *
     * @param context
     * @param packageName 要判断应用的包名
     * @param versionCode 要判断应用的版本名称
     */
    public static int isAppInstalled(Context context, String packageName, int versionCode) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo pakageinfo = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            if (versionCode == pakageinfo.versionCode) {
                LogUtils.i(TAG, "已经安装，不用更新，可以卸载该应用");
                return INSTALLED;
            } else if (versionCode > pakageinfo.versionCode) {
                LogUtils.i(TAG, "已经安装，有更新");
                return INSTALLED_UPDATE;
            }
        } catch (PackageManager.NameNotFoundException e) {
            LogUtils.i(TAG, "未安装该应用，可以安装");
            return UNINSTALLED;
        }
        return UNINSTALLED;
    }

    /**
     * 已安装包 直接启动
     *
     * @param context
     * @param packageName
     */
    public static void startApp(Context context, String packageName) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 如果按钮的字是 安装、更新或继续 则设为暂停    是暂停则设为继续
     *
     * @param context
     * @param view
     */
    public static void setDownloadViewText(Context context, ProgressButton view) {
        if (view.getText().toString().equals(context.getResources().getString(R.string.install))
                || view.getText().toString().equals(context.getResources().getString(R.string.update))
                || view.getText().toString().equals(context.getResources().getString(R.string.keep_on))) {
            view.setText(context.getResources().getString(R.string.pause));
        } else if (view.getText().toString().equals(context.getResources().getString(R.string.pause))) {
            view.setText(context.getResources().getString(R.string.keep_on));
        }
    }

    /**
     * 设置文字
     *
     * @param mContext
     * @param id
     */
    public static String getString(Context mContext, int id) {
        return mContext.getResources().getString(id);
    }

}
