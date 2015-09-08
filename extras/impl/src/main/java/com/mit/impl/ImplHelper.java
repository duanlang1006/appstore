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
    private static ImplHelperRes sHelperRes = new ImplHelperRes();

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
        ImplHelperRes res = getImplRes(context,implInfo);
        if (ImplInfo.ACTION_DOWNLOAD == res.action) {
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
            res.startActivity(context);
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
        ImplHelperRes res = getImplRes(context,implInfo);
        if (ImplInfo.ACTION_DOWNLOAD == res.action) {
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
        ImplHelperRes res = getImplRes(context,implInfo);
        if (ImplInfo.ACTION_DOWNLOAD == res.action) {
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
    }}

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

    public static boolean packageInstalled(Context context,String packageName){
        boolean ret = false;
        PackageManager pm = context.getPackageManager();
        try {
            pm.getApplicationInfo(packageName, 0);
            ret = true;
        } catch (Exception e) {
        }
        return ret;
    }


    public static ImplHelperRes getImplRes(Context context,ImplInfo implInfo){
        ImplDownload implDownload = ImplDownload.getInstance(context.getApplicationContext());
        Resources mResources = context.getResources();
        sHelperRes.reset();
        if (null == implInfo){
            return sHelperRes;
        }
        String localPath = implInfo.getLocalPath();
        if (null == localPath || TextUtils.isEmpty(localPath)){
            localPath = implInfo.getFileSavePath();
        }
        String mimeType = (null == localPath || TextUtils.isEmpty(localPath))?"":MimeTypeUtils.getMimeType(localPath);

        sHelperRes.progress = implDownload.getProgress(implInfo);
        switch (implInfo.getStatus()) {
            case ImplInfo.STATUS_INIT:
                sHelperRes.action = ImplInfo.ACTION_DOWNLOAD;
                sHelperRes.actionText = mResources.getString(R.string.action_install);
                sHelperRes.statusText = "";
                sHelperRes.descText = getSizeText(context, implInfo.getCurrent(), implInfo.getTotal());
                break;
            case ImplInfo.STATUS_PENDING:
                sHelperRes.action = ImplInfo.ACTION_DOWNLOAD;
                sHelperRes.actionText = mResources.getString(R.string.action_waiting);
                sHelperRes.statusText = mResources.getString(R.string.download_status_waiting);
                sHelperRes.descText = getSizeText(context, implInfo.getCurrent(), implInfo.getTotal());
                break;
            case ImplInfo.STATUS_RUNNING:
                sHelperRes.action = ImplInfo.ACTION_DOWNLOAD;
                sHelperRes.actionText = mResources.getString(R.string.action_pause);
                sHelperRes.statusText = mResources.getString(R.string.download_status_running);
                sHelperRes.descText = getSizeText(context, implInfo.getCurrent(), implInfo.getTotal());
                break;
            case ImplInfo.STATUS_PAUSED:
                sHelperRes.action = ImplInfo.ACTION_DOWNLOAD;
                sHelperRes.actionText = mResources.getString(R.string.action_resume);
                switch(implInfo.getCause()){
                    case ImplInfo.CAUSE_PAUSED_BY_NETWORK:
                        sHelperRes.statusText = mResources.getString(R.string.download_status_waiting_network);
                        break;
                    case ImplInfo.CAUSE_PAUSED_BY_OVERSIZE:
                        sHelperRes.statusText = mResources.getString(R.string.download_status_waiting_wlan);
                        break;
                    case ImplInfo.CAUSE_PAUSED_BY_APP:
                    default:
                        sHelperRes.statusText = mResources.getString(R.string.download_status_paused);
                        break;
                }
                sHelperRes.descText = getSizeText(context, implInfo.getCurrent(), implInfo.getTotal());
                break;
            case ImplInfo.STATUS_FAILED:
                sHelperRes.action = ImplInfo.ACTION_DOWNLOAD;
                sHelperRes.actionText = mResources.getString(R.string.action_retry);
                sHelperRes.statusText = mResources.getString(R.string.download_status_error);
                sHelperRes.descText = getSizeText(context, implInfo.getCurrent(), implInfo.getTotal());
                break;

            case ImplInfo.STATUS_SUCCESSFUL:
                sHelperRes.action = ImplInfo.ACTION_OPEN;
                sHelperRes.actionText = mResources.getString(R.string.action_open);
                sHelperRes.statusText = mResources.getString(R.string.download_status_success);
                sHelperRes.descText = Formatter.formatFileSize(context, implInfo.getTotal());
                //下载的是apk
                if (null != localPath
                        && "application/vnd.android.package-archive".equals(mimeType)) {
                    PackageInfo archivePkg = context.getPackageManager()
                            .getPackageArchiveInfo(localPath, PackageManager.GET_ACTIVITIES);
                    if (null != archivePkg) {
                        sHelperRes.action = ImplInfo.ACTION_OPEN;
                        sHelperRes.actionIntent = getLaunchDownloadIntent(context, archivePkg.packageName);
                        if (null == sHelperRes.actionIntent) {
                            sHelperRes.actionText = mResources.getString(R.string.action_open);
                        } else {
                            sHelperRes.actionText = mResources.getString(R.string.action_open);
                        }
                        sHelperRes.descText = (String.format(mResources.getString(R.string.apk_version), archivePkg.versionName));
                        sHelperRes.actionIntent = getLaunchDownloadIntent(context, archivePkg.packageName);
                    } else {//下载apk解析错误
                        sHelperRes.action = ImplInfo.ACTION_DOWNLOAD;
                        sHelperRes.actionText = mResources.getString(R.string.action_retry);
                        sHelperRes.statusText = mResources.getString(R.string.download_status_invalid_package);
                    }
                }
                sHelperRes.descText += ("|" + millis2FormatString("yy-MM-dd", implInfo.getLastMod()));
                if (null == sHelperRes.actionIntent) {
                    sHelperRes.actionIntent = getOpenDownloadIntent(localPath, mimeType);
                }
                break;

            case ImplInfo.STATUS_INSTALLED:
                try {
                    PackageInfo installed = context.getPackageManager()
                            .getPackageInfo(implInfo.getPackageName(), PackageManager.GET_ACTIVITIES);
                    sHelperRes.descText = (String.format(mResources.getString(R.string.apk_version), installed.versionName));
                    if (implInfo.getVersionCode() <= installed.versionCode ) {
                        //安装版本比目标版本新，需要打开
                        sHelperRes.action = ImplInfo.ACTION_OPEN;
                        sHelperRes.actionText = mResources.getString(R.string.action_open);
                        sHelperRes.statusText = mResources.getString(R.string.install_status_success);
                        sHelperRes.actionIntent = getLaunchDownloadIntent(context, implInfo.getPackageName());
                    }else {
                        //目标版本比较新
                        if (null == localPath || TextUtils.isEmpty(localPath)) {
                            //但是apk文件不存在，需要下载
                            sHelperRes.action = ImplInfo.ACTION_DOWNLOAD;
                            sHelperRes.actionText = mResources.getString(R.string.action_upgrade);
                        }else{
                            sHelperRes.action = ImplInfo.ACTION_DOWNLOAD;
                            sHelperRes.actionText = mResources.getString(R.string.action_upgrade);
                            PackageInfo archivePkg = context.getPackageManager()
                                    .getPackageArchiveInfo(localPath, PackageManager.GET_ACTIVITIES);
                            if (null != archivePkg) {
                                if (archivePkg.versionCode <= installed.versionCode){
                                    //apk文件存在，但是apk的版本较旧，需要下载
                                    sHelperRes.action = ImplInfo.ACTION_DOWNLOAD;
                                    sHelperRes.actionText = mResources.getString(R.string.action_upgrade);
                                }else{
                                    //否则需要打开安装
                                    sHelperRes.action = ImplInfo.ACTION_OPEN;
                                    sHelperRes.actionText = mResources.getString(R.string.action_open);
                                    sHelperRes.actionIntent = getOpenDownloadIntent(localPath,mimeType);
                                }
                            }else{
                                sHelperRes.actionText = mResources.getString(R.string.action_upgrade);
                                sHelperRes.statusText = mResources.getString(R.string.install_status_success);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    sHelperRes.action = ImplInfo.ACTION_DOWNLOAD;
                    sHelperRes.actionText = mResources.getString(R.string.action_retry);
                    sHelperRes.statusText = mResources.getString(R.string.download_status_invalid_package);
                    sHelperRes.descText = Formatter.formatFileSize(context, implInfo.getTotal());
                }
                sHelperRes.descText += ("|" + millis2FormatString("yy-MM-dd", implInfo.getLastMod()));
                break;


            case ImplInfo.STATUS_PRIVATE_INSTALLING:
            case ImplInfo.STATUS_NORMAL_INSTALLING:
                sHelperRes.action = ImplInfo.ACTION_INSTALL;
                sHelperRes.actionText = mResources.getString(R.string.action_open);
                sHelperRes.statusText = mResources.getString(R.string.install_status_installing);
                sHelperRes.descText = Formatter.formatFileSize(context, implInfo.getTotal());
                sHelperRes.descText += ("|" + millis2FormatString("yy-MM-dd", implInfo.getLastMod()));

                //下载的是apk
                if ("application/vnd.android.package-archive".equals(mimeType) && null != localPath) {
                    PackageInfo archivePkg = context.getPackageManager()
                            .getPackageArchiveInfo(localPath, PackageManager.GET_ACTIVITIES);
                    if (null != archivePkg) {
                        sHelperRes.actionIntent = getLaunchDownloadIntent(context, archivePkg.packageName);
                    }
                }
                if (null == sHelperRes.actionIntent) {
                    sHelperRes.actionIntent = getOpenDownloadIntent(localPath, mimeType);
                }
                break;

            case ImplInfo.STATUS_PACKAGE_INVALID:
                sHelperRes.action = ImplInfo.ACTION_DOWNLOAD;
                sHelperRes.actionText = mResources.getString(R.string.action_retry);
                sHelperRes.statusText = mResources.getString(R.string.download_status_invalid_package);
                sHelperRes.descText = Formatter.formatFileSize(context, implInfo.getTotal());
                sHelperRes.descText += ("|" + millis2FormatString("yy-MM-dd", implInfo.getLastMod()));
                break;

            case ImplInfo.STATUS_INSTALL_FAILED:
                sHelperRes.action = ImplInfo.ACTION_DOWNLOAD;
                sHelperRes.actionText = mResources.getString(R.string.action_retry);
                sHelperRes.statusText = mResources.getString(R.string.install_status_failed);
                sHelperRes.descText = Formatter.formatFileSize(context, implInfo.getTotal());
                sHelperRes.descText += ("|" + millis2FormatString("yy-MM-dd", implInfo.getLastMod()));
                break;
        }
        return sHelperRes;
    }

    public static class ImplHelperRes{
        private int action;
        private String actionText;
        private String statusText;
        private String descText;
        private Intent actionIntent;
        private int progress;

        public ImplHelperRes() {
            reset();
        }

        public void reset(){
            action = ImplInfo.ACTION_DOWNLOAD;
            actionText = "";
            statusText = "";
            descText = "";
            actionIntent = null;
            progress = 0;
        }

        public int getAction() {
            return action;
        }

        public String getActionText() {
            return actionText;
        }

        public String getStatusText() {
            return statusText;
        }

        public String getDescText() {
            return descText;
        }

        public int getProgress() {
            return progress;
        }

        public boolean startActivity(Context context){
            boolean ret = true;
            switch(action){
                case ImplInfo.ACTION_INSTALL:
                    MitMobclickAgent.onEvent(context, "impl_startActivity_InstallApk");
                    break;
                case ImplInfo.ACTION_OPEN:
                    MitMobclickAgent.onEvent(context, "impl_startActivity_OpenApk");
                    break;
            }
            try {
                context.startActivity(actionIntent);
            } catch (Exception e) {
                e.printStackTrace();
                ret = false;
            }
            return ret;
        }
    }
}
