package com.mit.impl;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.widget.Toast;
import com.lidroid.xutils.util.MimeTypeUtils;
import com.mit.mitupdatesdk.MitMobclickAgent;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by hxd on 15-6-10.
 */
public class ImplHelper{
    private final static String TAG = "impl_helper";

    public static int getProgress(Context context,ImplInfo implInfo) {
        return ImplDownload.getInstance(context.getApplicationContext()).getProgress(implInfo);
    }

    public static int getAction(Context context,ImplInfo implInfo) {
        int action = ImplInfo.ACTION_DOWNLOAD;
        if (null == implInfo) {
            return action;
        }

        switch (implInfo.getStatus()) {
            case ImplInfo.STATUS_INIT:
            case ImplInfo.STATUS_PENDING:
            case ImplInfo.STATUS_RUNNING:
            case ImplInfo.STATUS_FAILED:
                action = ImplInfo.ACTION_DOWNLOAD;
                break;

            case ImplInfo.STATUS_SUCCESSFUL:
                String localPath = implInfo.getLocalPath();
                if (null == localPath || TextUtils.isEmpty(localPath)) {
                    localPath = ImplDownload.getInstance(context.getApplicationContext()).getLocalPath(implInfo);
                }
                String mimeType = MimeTypeUtils.getMimeType(localPath);
                action = ImplInfo.ACTION_OPEN;
                //下载的是apk
                if ("application/vnd.android.package-archive".equals(mimeType) && null != localPath) {
                    PackageInfo archivePkg = context.getPackageManager()
                            .getPackageArchiveInfo(localPath, PackageManager.GET_ACTIVITIES);
                    if (null != archivePkg) {
                        action = ImplInfo.ACTION_OPEN;
                    } else {//下载apk解析错误
                        action = ImplInfo.ACTION_DOWNLOAD;
                    }
                }
                break;

            case ImplInfo.STATUS_INSTALLED:
                try {
                    PackageInfo installPkg = context.getPackageManager()
                            .getPackageInfo(implInfo.getPackageName(), PackageManager.GET_ACTIVITIES);
//                    if (implInfo.getVersionCode() <= installPkg.versionCode) {
                        action = ImplInfo.ACTION_OPEN;
//                    }else{
//                        action = ImplInfo.ACTION_DOWNLOAD;
//                    }
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    action = ImplInfo.ACTION_DOWNLOAD;
                }
                break;

//            case ImplInfo.STATUS_UPGRADE:
//                action = ImplInfo.ACTION_DOWNLOAD;
//                break;

            case ImplInfo.STATUS_PRIVATE_INSTALLING:
            case ImplInfo.STATUS_NORMAL_INSTALLING:
                action = ImplInfo.ACTION_INSTALL;
                break;

            case ImplInfo.STATUS_PACKAGE_INVALID:
            case ImplInfo.STATUS_INSTALL_FAILED:
                action = ImplInfo.ACTION_DOWNLOAD;
                break;
        }
        return action;
    }

    public static String getActionText(Context context,ImplInfo implInfo) {
        Resources mResources = context.getResources();
        String actionText = "";
        if (null == implInfo) {
            return actionText;
        }

        switch (implInfo.getStatus()) {
            case ImplInfo.STATUS_INIT:
                actionText = mResources.getString(R.string.action_install);
                break;

            case ImplInfo.STATUS_PENDING:
                actionText = mResources.getString(R.string.action_waiting);
                break;

            case ImplInfo.STATUS_RUNNING:
                actionText = mResources.getString(R.string.action_pause);
                break;

            case ImplInfo.STATUS_PAUSED:
                actionText = mResources.getString(R.string.action_resume);
//                switch(implInfo.getCause()){
//                    case ImplInfo.CAUSE_PAUSED_BY_APP:
//                        actionText = mResources.getString(R.string.action_resume);
//                        break;
//                    case ImplInfo.CAUSE_PAUSED_BY_NETWORK:
//                    case ImplInfo.CAUSE_PAUSED_BY_OVERSIZE:
//                    default:
//                        actionText = mResources.getString(R.string.action_pause);
//                        break;
//                }
                break;

            case ImplInfo.STATUS_FAILED:
                actionText = mResources.getString(R.string.action_retry);
                break;

            case ImplInfo.STATUS_SUCCESSFUL:
                String localPath = implInfo.getLocalPath();
                if (null == localPath || TextUtils.isEmpty(localPath)) {
                    localPath = ImplDownload.getInstance(context.getApplicationContext()).getLocalPath(implInfo);
                }
                String mimeType = MimeTypeUtils.getMimeType(localPath);
                actionText = mResources.getString(R.string.action_open);
                //下载的是apk
                if ("application/vnd.android.package-archive".equals(mimeType) && null != localPath) {
                    PackageInfo archivePkg = context.getPackageManager()
                            .getPackageArchiveInfo(localPath, PackageManager.GET_ACTIVITIES);
                    if (null != archivePkg) {
                        Intent intent = getLaunchDownloadIntent(context, archivePkg.packageName);
                        if (null == intent) {
                            actionText = mResources.getString(R.string.action_open);
                        } else {
                            actionText = mResources.getString(R.string.action_open);
                        }
                    } else {//下载apk解析错误
                        actionText = mResources.getString(R.string.action_retry);
                    }
                }
                break;

            case ImplInfo.STATUS_INSTALLED:
                try {
                    PackageInfo installPkg = context.getPackageManager()
                            .getPackageInfo(implInfo.getPackageName(), PackageManager.GET_ACTIVITIES);
//                    if (implInfo.getVersionCode() <= installPkg.versionCode) {
                        actionText = mResources.getString(R.string.action_open);
//                    } else {
//                        actionText = mResources.getString(R.string.action_upgrade);
//                    }
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    actionText = mResources.getString(R.string.action_retry);
                }
                break;

//            case ImplInfo.STATUS_UPGRADE:
//                actionText = mResources.getString(R.string.action_upgrade);
//                break;

            case ImplInfo.STATUS_PRIVATE_INSTALLING:
            case ImplInfo.STATUS_NORMAL_INSTALLING:
                actionText = mResources.getString(R.string.action_open);
                break;
            case ImplInfo.STATUS_PACKAGE_INVALID:
            case ImplInfo.STATUS_INSTALL_FAILED:
                actionText = mResources.getString(R.string.action_retry);
                break;
        }
        return actionText;
    }

    public static String getStatusText(Context context,ImplInfo implInfo) {
        Resources mResources = context.getResources();
        String statusText = "";
        if (null == implInfo) {
            return statusText;
        }
        switch (implInfo.getStatus()) {
            case ImplInfo.STATUS_INIT:
                statusText = "";
                break;

            case ImplInfo.STATUS_PENDING:
                statusText = mResources.getString(R.string.download_status_waiting);
                break;

            case ImplInfo.STATUS_RUNNING:
                statusText = mResources.getString(R.string.download_status_running);
                break;

            case ImplInfo.STATUS_PAUSED:
                switch(implInfo.getCause()){
                    case ImplInfo.CAUSE_PAUSED_BY_NETWORK:
                        statusText = mResources.getString(R.string.download_status_waiting_network);
                        break;
                    case ImplInfo.CAUSE_PAUSED_BY_OVERSIZE:
                        statusText = mResources.getString(R.string.download_status_waiting_wlan);
                        break;
                    case ImplInfo.CAUSE_PAUSED_BY_APP:
                    default:
                        statusText = mResources.getString(R.string.download_status_paused);
                        break;
                }
                break;

            case ImplInfo.STATUS_FAILED:
                statusText = mResources.getString(R.string.download_status_error);
                break;

            case ImplInfo.STATUS_SUCCESSFUL:
                String localPath = implInfo.getLocalPath();
                if (null == localPath || TextUtils.isEmpty(localPath)) {
                    localPath = ImplDownload.getInstance(context.getApplicationContext()).getLocalPath(implInfo);
                }
                String mimeType = MimeTypeUtils.getMimeType(localPath);
                statusText = mResources.getString(R.string.download_status_success);
                //下载的是apk
                if ("application/vnd.android.package-archive".equals(mimeType) && null != localPath) {
                    PackageInfo archivePkg = context.getPackageManager()
                            .getPackageArchiveInfo(localPath, PackageManager.GET_ACTIVITIES);
                    if (null == archivePkg) {
                        statusText = mResources.getString(R.string.download_status_invalid_package);
                    }
                }
                break;

            case ImplInfo.STATUS_INSTALLED:
                try {
                    context.getPackageManager()
                            .getPackageInfo(implInfo.getPackageName(), PackageManager.GET_ACTIVITIES);
                    statusText = mResources.getString(R.string.install_status_success);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    statusText = mResources.getString(R.string.download_status_invalid_package);
                }
                break;

//            case ImplInfo.STATUS_UPGRADE:
//                statusText = mResources.getString(R.string.install_status_upgrade);
//                break;

            case ImplInfo.STATUS_PRIVATE_INSTALLING:
            case ImplInfo.STATUS_NORMAL_INSTALLING:
                statusText = mResources.getString(R.string.install_status_installing);
                break;

            case ImplInfo.STATUS_PACKAGE_INVALID:
                statusText = mResources.getString(R.string.download_status_invalid_package);
                break;
            case ImplInfo.STATUS_INSTALL_FAILED:
                statusText = mResources.getString(R.string.install_status_failed);
                break;
        }
        return statusText;
    }

    public static String getDescText(Context context,ImplInfo implInfo) {
        Resources mResources = context.getResources();
        String descText = "";
        if (null == implInfo) {
            return descText;
        }
        ImplDownload implDownload = ImplDownload.getInstance(context.getApplicationContext());
        switch (implInfo.getStatus()) {
            case ImplInfo.STATUS_INIT:
            case ImplInfo.STATUS_PENDING:
            case ImplInfo.STATUS_RUNNING:
            case ImplInfo.STATUS_PAUSED:
            case ImplInfo.STATUS_FAILED:
                descText = getSizeText(context, implDownload.getCurrentBytes(implInfo), implDownload.getTotalBytes(implInfo));
                break;

            case ImplInfo.STATUS_SUCCESSFUL:
                String localPath = implInfo.getLocalPath();
                if (null == localPath || TextUtils.isEmpty(localPath)) {
                    localPath = implDownload.getLocalPath(implInfo);
                }
                String mimeType = MimeTypeUtils.getMimeType(localPath);
                descText = Formatter.formatFileSize(context, implDownload.getTotalBytes(implInfo));
                //下载的是apk
                if ("application/vnd.android.package-archive".equals(mimeType) && null != localPath) {
                    PackageInfo archivePkg = context.getPackageManager()
                            .getPackageArchiveInfo(localPath, PackageManager.GET_ACTIVITIES);
                    if (null != archivePkg) {
                        descText = (String.format(mResources.getString(R.string.apk_version), archivePkg.versionName));
                    }
                }
                descText += ("|" + millis2FormatString("yy-MM-dd", implInfo.getLastMod()));
                break;

            case ImplInfo.STATUS_INSTALLED:
                try {
                    PackageInfo installPkg = context.getPackageManager().getPackageInfo(implInfo.getPackageName(), PackageManager.GET_ACTIVITIES);
                    descText = (String.format(mResources.getString(R.string.apk_version), installPkg.versionName));
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    descText = Formatter.formatFileSize(context, implDownload.getTotalBytes(implInfo));
                }
                descText += ("|" + millis2FormatString("yy-MM-dd", implInfo.getLastMod()));
                break;

//            case ImplInfo.STATUS_UPGRADE:
//                descText = mResources.getString(R.string.install_status_upgrade);
//                descText += ("|" + millis2FormatString("yy-MM-dd",implInfo.getLastMod()));
//                break;

            case ImplInfo.STATUS_PRIVATE_INSTALLING:
            case ImplInfo.STATUS_NORMAL_INSTALLING:
            case ImplInfo.STATUS_PACKAGE_INVALID:
            case ImplInfo.STATUS_INSTALL_FAILED:
                descText = Formatter.formatFileSize(context, implDownload.getTotalBytes(implInfo));
                descText += ("|" + millis2FormatString("yy-MM-dd", implInfo.getLastMod()));
                break;
        }
        return descText;
    }

    public static boolean startActivity(Context context,ImplInfo implInfo){
        boolean ret = true;
        Intent intent = getActionIntent(context, implInfo);
        switch(getAction(context, implInfo)){
            case ImplInfo.ACTION_INSTALL:
                MitMobclickAgent.onEvent(context, "impl_startActivity_InstallApk");
                break;
            case ImplInfo.ACTION_OPEN:
                MitMobclickAgent.onEvent(context, "impl_startActivity_OpenApk");
                break;
        }
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            ret = false;
        }
        return ret;
    }

    private static Intent getActionIntent(Context context,ImplInfo implInfo) {
        Intent actionIntent = null;
        if (null == implInfo) {
            return actionIntent;
        }
        ImplDownload implDownload = ImplDownload.getInstance(context.getApplicationContext());
        switch (implInfo.getStatus()) {
            case ImplInfo.STATUS_INIT:
            case ImplInfo.STATUS_PENDING:
            case ImplInfo.STATUS_RUNNING:
            case ImplInfo.STATUS_PAUSED:
            case ImplInfo.STATUS_FAILED:
                actionIntent = null;
                break;

            case ImplInfo.STATUS_PRIVATE_INSTALLING:
            case ImplInfo.STATUS_NORMAL_INSTALLING:
            case ImplInfo.STATUS_SUCCESSFUL:
                String localPath = implInfo.getLocalPath();
                if (null == localPath || TextUtils.isEmpty(localPath)) {
                    localPath = implDownload.getLocalPath(implInfo);
                }
                String mimeType = MimeTypeUtils.getMimeType(localPath);
                //下载的是apk
                if ("application/vnd.android.package-archive".equals(mimeType) && null != localPath) {
                    PackageInfo archivePkg = context.getPackageManager()
                            .getPackageArchiveInfo(localPath, PackageManager.GET_ACTIVITIES);
                    if (null != archivePkg) {
                        actionIntent = getLaunchDownloadIntent(context, archivePkg.packageName);
                    } else {//下载apk解析错误
                        actionIntent = null;
                    }
                }
                if (null == actionIntent) {
                    actionIntent = getOpenDownloadIntent(localPath, mimeType);
                }
                break;

            case ImplInfo.STATUS_INSTALLED:
                try {
                    PackageInfo installPkg = context.getPackageManager().getPackageInfo(implInfo.getPackageName(), PackageManager.GET_ACTIVITIES);
                    actionIntent = getLaunchDownloadIntent(context, implInfo.getPackageName());
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    actionIntent = null;
                }
                break;

//            case ImplInfo.STATUS_UPGRADE:
//                actionIntent = null;
//                break;

            case ImplInfo.STATUS_PACKAGE_INVALID:
            case ImplInfo.STATUS_INSTALL_FAILED:
                actionIntent = null;
                break;
        }
        return actionIntent;
    }

    public static String getSizeText(Context context, long currentBytes, long totalBytes) {
        StringBuffer sizeText = new StringBuffer();
        if (totalBytes >= 0) {
            sizeText.append(Formatter.formatFileSize(context, currentBytes));
            sizeText.append("/");
            sizeText.append(Formatter.formatFileSize(context, totalBytes));
        }
        return sizeText.toString();
    }

    public static String millis2FormatString(String format, Long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        return sdf.format(new Date(millis));
    }

    public static Intent getOpenDownloadIntent(String localPath, String mediaType) {
        Uri localUri = Uri.fromFile(new File(localPath));
        if (null != localUri) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(localUri, mediaType);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            return intent;
        }
        return null;
    }

    public static Intent getLaunchDownloadIntent(Context context, String packageName) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (null != intent) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        }
        return intent;
    }

    public static void onClick(Context context,
                               ImplInfo implInfo,
                               String downloadUrl,
                               String name,
                               String iconUrl,
                               String fullname,
                               String md5,
                               ImplChangeCallback callback){
        if (ImplInfo.ACTION_DOWNLOAD == getAction(context,implInfo)) {
            ImplAgent implAgent = ImplAgent.getInstance(context.getApplicationContext());
            switch (implInfo.getStatus()) {
                case ImplInfo.STATUS_PENDING:
                    break;
                case ImplInfo.STATUS_RUNNING:
                    implAgent.pauseDownload(implInfo);
                    break;
                case ImplInfo.STATUS_PAUSED:
                    if (ImplInfo.CAUSE_PAUSED_BY_NETWORK == implInfo.getCause()){
                        Toast.makeText(context,R.string.network_disable_toast,Toast.LENGTH_SHORT).show();
                    }else {
                        implAgent.resumeDownload(implInfo, callback);
                    }
                    break;
                default:
                    implAgent.newDownload(implInfo,
                            downloadUrl,
                            name,
                            iconUrl,
                            fullname,
                            md5,
                            true,
                            callback);
                    break;
            }
        } else {
            startActivity(context,implInfo);
        }
    }

    public static void downloadImpl(Context context,
                                    ImplInfo implInfo,
                                    String downloadUrl,
                                    String name,
                                    String iconUrl,
                                    String fullname,
                                    String md5,
                                    ImplChangeCallback callback){
        if (ImplInfo.ACTION_DOWNLOAD == getAction(context,implInfo)) {
            ImplAgent implAgent = ImplAgent.getInstance(context.getApplicationContext());
            switch (implInfo.getStatus()) {
                case ImplInfo.STATUS_PENDING:
                case ImplInfo.STATUS_RUNNING:
                    break;
                case ImplInfo.STATUS_PAUSED:
                    implAgent.resumeDownload(implInfo, null);
                    break;
                case ImplInfo.STATUS_INSTALLED:
                case ImplInfo.STATUS_NORMAL_INSTALLING:
                case ImplInfo.STATUS_PRIVATE_INSTALLING:
                    //正在安装或已安装
//                            Toast.makeText(mActivity, "该应用您已经安装过了！", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    implAgent.newDownload(implInfo,
                            downloadUrl,
                            name,
                            iconUrl,
                            fullname,
                            md5,
                            true,
                            null);
                    break;
            }
        }
    }

    public static void updateImpl(Context context,
                                    ImplInfo implInfo,
                                    String downloadUrl,
                                    String name,
                                    String iconUrl,
                                    String fullname,
                                    String md5,
                                    ImplChangeCallback callback){
        ImplAgent implAgent = ImplAgent.getInstance(context.getApplicationContext());
        switch (implInfo.getStatus()) {
            case ImplInfo.STATUS_PENDING:
            case ImplInfo.STATUS_RUNNING:
                break;
            case ImplInfo.STATUS_PAUSED:
                implAgent.resumeDownload(implInfo, null);
                break;
            case ImplInfo.STATUS_NORMAL_INSTALLING:
            case ImplInfo.STATUS_PRIVATE_INSTALLING:
                //正在安装或已安装
//                            Toast.makeText(mActivity, "该应用您已经安装过了！", Toast.LENGTH_SHORT).show();
                break;
            case ImplInfo.STATUS_INSTALLED:
            default:
                implAgent.newDownload(implInfo,
                        downloadUrl,
                        name,
                        iconUrl,
                        fullname,
                        md5,
                        false,
                        null);
                break;
        }
    }

    public static String getFileMD5(File file) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bigInt.toString(16);
    }
}
