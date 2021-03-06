package com.mit.impl;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.text.TextUtils;
import android.util.SparseArray;

import java.io.File;
import java.lang.reflect.Method;

public class ImplPackageManager {
    private static final String TAG = "impl_package";
    private SparseArray<Method> mCmdList = new SparseArray<Method>();
    private Context mContext;
    private PackageManager pm;
    private boolean inited = false;

    private static ImplPackageManager mInstance = null;

    private static synchronized void initInstance(Context appcontext) {
        if (null == mInstance) {
            mInstance = new ImplPackageManager(appcontext);
        }
    }

    public static ImplPackageManager getInstance(Context appcontext) {
        if (null == mInstance) {
            initInstance(appcontext);
        }
        return mInstance;
    }

    private ImplPackageManager(Context context) {
        mContext = context;
        if (null == pm) {
            pm = mContext.getPackageManager();
        }
        inited = true;
    }

    void fillImplInfo(ImplInfo implInfo) {
        if (null == implInfo) {
            return;
        }
        switch (implInfo.getStatus()) {
            case ImplInfo.STATUS_PENDING:
            case ImplInfo.STATUS_RUNNING:
            case ImplInfo.STATUS_PAUSED:
                break;
            default:
                try {
                    ApplicationInfo appInfo = pm.getApplicationInfo(implInfo.getPackageName(), PackageManager.GET_META_DATA);
//                    if (implInfo.getVersionCode() <= pakageinfo.versionCode) {
                    implInfo.setStatus(ImplInfo.STATUS_INSTALLED);
//                    }else{
//                        implInfo.setStatus(ImplInfo.STATUS_UPGRADE);
//                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                }
                break;
        }
    }

    void install(ImplInfo implInfo, boolean silent, ImplListener callback) {
        String path = implInfo.getLocalPath();
        if (null == path || TextUtils.isEmpty(path)) {
            path = implInfo.getFileSavePath();
        }
        if (null == path || path.length() < 1) {
            return;
        }
        PackageInfo info = pm.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
        if (null != info && info.packageName.equals(implInfo.getPackageName())) {
            try {
                pm.getApplicationInfo("com.android.installer", 0);
                installImpl(implInfo, path, silent);
            } catch (NameNotFoundException e) {
                try {
                    pm.getApplicationInfo("com.android.dbservices", 0);
                    installImpl(implInfo, path, silent);
                } catch (Exception e1) {
//                    e1.printStackTrace();
                    installImpl(implInfo, path, false);
                }
            }
            callback.onInstalling(implInfo);
        } else {
            //希望安装的apk和下载的apk包名不一致,或者下载的apk不合法
            implInfo.setStatus(ImplInfo.STATUS_PACKAGE_INVALID);
            callback.onInstallFailure(implInfo, -100000);
        }
    }

    void uninstall(ImplInfo implInfo, boolean silent, ImplListener callback) {
        try {
            pm.getApplicationInfo("com.android.dbservices", 0);
            Intent intent = new Intent("com.installer.action.delete");
            intent.putExtra("name", implInfo.getPackageName());
            intent.putExtra("nameTag", "APK_PATH_NAME.tag");
            intent.putExtra("silent", silent);
            mContext.sendBroadcast(intent);
        } catch (Exception e1) {
//            e1.printStackTrace();
            Intent intent = new Intent(Intent.ACTION_DELETE, Uri.parse("package:" + implInfo.getPackageName()));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }
        callback.onUninstalling(implInfo);
    }

    void onSystemInstallResult(ImplInfo implInfo, int result, ImplListener callback) {
        if (result == ImplInfo.INSTALL_SUCCEEDED) {
            implInfo.setStatus(ImplInfo.STATUS_INSTALLED);
            callback.onInstallSuccess(implInfo);
        } else {
            implInfo.setStatus(ImplInfo.STATUS_INSTALL_FAILED);
            callback.onInstallFailure(implInfo, result);
        }
    }

    void onSystemDeleteResult(ImplInfo implInfo, int result, ImplListener callback) {
        if (result == ImplInfo.DELETE_SUCCEEDED) {
            implInfo.setStatus(ImplInfo.STATUS_INIT);
            callback.onUninstallSuccess(implInfo);
        } else {
            callback.onUninstallFailure(implInfo, result);
        }
    }

    void onPackageAdded(ImplInfo implInfo, ImplListener callback) {
        if (ImplConfig.getDeleteAfterInstalled(mContext)) {
            try {
                String localPath = implInfo.getLocalPath();
                if (null == localPath) {
                    localPath = implInfo.getFileSavePath();
                }
                new File(localPath).delete();
                implInfo.setLocalPath(null);
                implInfo.setCurrent(0);
            } catch (Exception e) {
//            e.printStackTrace();
            }
        }
        implInfo.setStatus(ImplInfo.STATUS_INSTALLED);
        callback.onInstallSuccess(implInfo);
    }

    void onPackageChanged(ImplInfo implInfo, ImplListener callback) {
        implInfo.setStatus(ImplInfo.STATUS_INSTALLED);
        callback.onInstallSuccess(implInfo);
    }

    void onPackageRemoved(ImplInfo implInfo, ImplListener callback) {
        implInfo.setStatus(ImplInfo.STATUS_INIT);
        callback.onUninstallSuccess(implInfo);
    }

    private void installImpl(final ImplInfo implInfo, String filename, boolean silent) {
        if (!implInfo.isSignatureEqual()) {
            silent = false;
        }
        if (silent) {
            Intent intent = new Intent();
            intent.setAction("com.installer.system");
            intent.putExtra("name", filename);
            intent.putExtra("nameTag", "APK_PATH_NAME.tag");
            mContext.sendBroadcast(intent);
            implInfo.setStatus(ImplInfo.STATUS_PRIVATE_INSTALLING);
        } else {
            Uri path = Uri.fromFile(new File(filename));
            Intent activityIntent = new Intent(Intent.ACTION_VIEW);
            activityIntent.setDataAndType(path, "application/vnd.android.package-archive");
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                mContext.startActivity(activityIntent);
            } catch (ActivityNotFoundException ex) {

            }
            implInfo.setStatus(ImplInfo.STATUS_NORMAL_INSTALLING);
        }
    }
}
