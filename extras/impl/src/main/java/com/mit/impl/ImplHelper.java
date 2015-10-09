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
public class ImplHelper {
    private final static String TAG = "impl_helper";
//    /**
//     * Notification构造器
//     */
//    private static NotificationCompat.Builder mBuilder;
//    /**
//     * Notification的ID
//     */
//    private static int notifyId_base = 100;
//    /**
//     * Notification管理
//     */
//    public static NotificationManager mNotificationManager;

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
                               ImplChangeCallback callback) {
        ImplInfo.ImplRes res = implInfo.getImplRes();
        if (ImplInfo.ACTION_DOWNLOAD == res.getAction()) {
            ImplAgent implAgent = ImplAgent.getInstance(context.getApplicationContext());
            switch (implInfo.getStatus()) {
                case ImplInfo.STATUS_PENDING:
                    break;
                case ImplInfo.STATUS_RUNNING:
                    implAgent.pauseDownload(implInfo);
                    break;
                case ImplInfo.STATUS_PAUSED:
                    if (ImplInfo.CAUSE_PAUSED_BY_NETWORK == implInfo.getCause()) {
                        Toast.makeText(context, R.string.network_disable_toast, Toast.LENGTH_SHORT).show();
                    } else {
                        implAgent.resumeDownload(implInfo, callback);
                    }
//                    showDownloadNotify(context, ImplInfo.STATUS_PENDING | ImplInfo.STATUS_RUNNING | ImplInfo.STATUS_PAUSED
//                            | ImplInfo.STATUS_FAILED | ImplInfo.STATUS_PACKAGE_INVALID);
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
//                    showDownloadNotify(context, ImplInfo.STATUS_PENDING | ImplInfo.STATUS_RUNNING | ImplInfo.STATUS_PAUSED
//                            | ImplInfo.STATUS_FAILED | ImplInfo.STATUS_PACKAGE_INVALID);
                    break;
            }
        } else {
            startActivity(context, res);
        }
    }

//    private static void showDownloadNotify(Context context, int position) {
//        initNotify(context);
//        ImplAgent mImplAgent = ImplAgent.getInstance(context.getApplicationContext());
////        if (R.string.downloading == position) {
//        showIntentActivityNotify(context, mImplAgent.getImplInfoCount(position) + 1, notifyId_base + 1);
//        //这里是显示 点击返回的提示
////        } else {
////            showIntentActivityNotify(context, mImplAgent.getImplInfoCount(position) + 1, notifyId_base + 2);
////        }
//
//    }

//    private static void initNotify(Context context) {
//        mBuilder = new NotificationCompat.Builder(context);
//        mBuilder.setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示
//                .setPriority(Notification.PRIORITY_DEFAULT)//设置该通知优先级
////				.setAutoCancel(true)//设置这个标志当用户单击面板就可以让通知将自动取消
//                .setOngoing(false)//ture，设置他为一个正在进行的通知。
//                .setSmallIcon(R.drawable.ic_launcher);
//        mNotificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
//    }

//    /**
//     * 显示通知栏点击跳转到指定Activity
//     */
//    public static void showIntentActivityNotify(Context context, int count, int notify) {
//        // Notification.FLAG_ONGOING_EVENT --设置常驻 Flag;
//        // Notification.FLAG_AUTO_CANCEL 通知栏上点击此通知后自动清除此通知
////		notification.flags = Notification.FLAG_AUTO_CANCEL; //在通知栏上点击此通知后自动清除此通知
//        String temp = null;
//        if (notifyId_base + 1 == notify) {
//            temp = "您有" + count + "个应用正在下载";
//        } else {
//            temp = "您有" + count + "个应用已下载完成";
//        }
//        mBuilder.setAutoCancel(true)//点击后让通知将消失
//                .setContentTitle(temp)
//                .setContentText("点击查看");
////
//        Intent clickIntent = new Intent(context, ClickReceiver.class); //点击 Intent
//        clickIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        clickIntent.putExtra("notify", notify);
//        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        mBuilder.setContentIntent(pendingIntent);
//        mNotificationManager.notify(notifyId_base + 1, mBuilder.build());
////        ((OSGIServiceHost) context).jumptoDownloadManager(true);
//
//    }

//    /**
//     * 显示通知栏点击打开Apk
//     */
//    public void showIntentApkNotify() {
//        LogUtils.d("wanghc", "我执行了showIntentApkNotify");
//        // Notification.FLAG_ONGOING_EVENT --设置常驻 Flag;Notification.FLAG_AUTO_CANCEL 通知栏上点击此通知后自动清除此通知
////		notification.flags = Notification.FLAG_AUTO_CANCEL; //在通知栏上点击此通知后自动清除此通知
//        mBuilder.setAutoCancel(true)//点击后让通知将消失
//                .setContentTitle("您有" + mImplList.size() + "个应用下载完成")
//                .setContentText("点击安装");
////                .setTicker("下载完成！");
//        //我们这里需要做的是打开一个安装包
//        Intent apkIntent = new Intent();
//        apkIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        apkIntent.setAction(android.content.Intent.ACTION_VIEW);
//        //注意：这里的这个APK是放在assets文件夹下，获取路径不能直接读取的，要通过COYP出去在读或者直接读取自己本地的PATH，这边只是做一个跳转APK，实际打不开的
//        String apk_path = "file:///android_asset/cs.apk";
////		Uri uri = Uri.parse(apk_path);
//        Uri uri = Uri.fromFile(new File(apk_path));
//        apkIntent.setDataAndType(uri, "application/vnd.android.package-archive");
//        // context.startActivity(intent);
//        PendingIntent contextIntent = PendingIntent.getActivity(mActivity, 0, apkIntent, 0);
//        mBuilder.setContentIntent(contextIntent);
//        mNotificationManager.notify(notifyId1, mBuilder.build());
//    }

    public static boolean startActivity(Context context, ImplInfo.ImplRes implRes) {
        boolean ret = true;
        Intent intent = implRes.getActionIntent();
        switch (implRes.getAction()) {
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

    public static void downloadImpl(Context context,
                                    ImplInfo implInfo,
                                    String downloadUrl,
                                    String name,
                                    String iconUrl,
                                    String fullname,
                                    String md5,
                                    ImplChangeCallback callback) {
        if (ImplInfo.ACTION_DOWNLOAD == implInfo.getImplRes().getAction()) {
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
                                  ImplChangeCallback callback) {
        if (ImplInfo.ACTION_DOWNLOAD == implInfo.getImplRes().getAction()) {
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

    public static boolean packageInstalled(Context context, String packageName) {
        boolean ret = false;
        PackageManager pm = context.getPackageManager();
        try {
            pm.getApplicationInfo(packageName, 0);
            ret = true;
        } catch (Exception e) {
        }
        return ret;
    }


    static void fillImplRes(Context context, ImplInfo implInfo) {
        ImplDownload implDownload = ImplDownload.getInstance(context.getApplicationContext());
        Resources mResources = context.getResources();
        if (null == implInfo) {
            return;
        }
        ImplInfo.ImplRes implRes = implInfo.getImplRes();
        String localPath = implInfo.getLocalPath();
        if (null == localPath || TextUtils.isEmpty(localPath)) {
            localPath = implInfo.getFileSavePath();
        }
        String mimeType = (null == localPath || TextUtils.isEmpty(localPath)) ? "" : MimeTypeUtils.getMimeType(localPath);

        switch (implInfo.getStatus()) {
            case ImplInfo.STATUS_INIT:
                implRes.setAction(ImplInfo.ACTION_DOWNLOAD);
                implRes.setActionText(mResources.getString(R.string.action_install));
                implRes.setStatusText("");
                implRes.setDescText(getSizeText(context, implInfo.getCurrent(), implInfo.getTotal()));
                break;
            case ImplInfo.STATUS_PENDING:
                implRes.setAction(ImplInfo.ACTION_DOWNLOAD);
                implRes.setActionText(mResources.getString(R.string.action_waiting));
                implRes.setStatusText(mResources.getString(R.string.download_status_waiting));
                implRes.setDescText(getSizeText(context, implInfo.getCurrent(), implInfo.getTotal()));
                break;
            case ImplInfo.STATUS_RUNNING:
                implRes.setAction(ImplInfo.ACTION_DOWNLOAD);
                implRes.setActionText(mResources.getString(R.string.action_pause));
                implRes.setStatusText(mResources.getString(R.string.download_status_running));
                implRes.setDescText(getSizeText(context, implInfo.getCurrent(), implInfo.getTotal()));
                break;
            case ImplInfo.STATUS_PAUSED:
                implRes.setAction(ImplInfo.ACTION_DOWNLOAD);
                implRes.setActionText(mResources.getString(R.string.action_resume));
                switch (implInfo.getCause()) {
                    case ImplInfo.CAUSE_PAUSED_BY_NETWORK:
                        implRes.setStatusText(mResources.getString(R.string.download_status_waiting_network));
                        break;
                    case ImplInfo.CAUSE_PAUSED_BY_OVERSIZE:
                        implRes.setStatusText(mResources.getString(R.string.download_status_waiting_wlan));
                        break;
                    case ImplInfo.CAUSE_PAUSED_BY_APP:
                    default:
                        implRes.setStatusText(mResources.getString(R.string.download_status_paused));
                        break;
                }
                implRes.setDescText(getSizeText(context, implInfo.getCurrent(), implInfo.getTotal()));
                break;
            case ImplInfo.STATUS_FAILED:
                implRes.setAction(ImplInfo.ACTION_DOWNLOAD);
                implRes.setActionText(mResources.getString(R.string.action_retry));
                switch (implInfo.getCause()) {
                    case ImplInfo.CAUSE_FAILED_BY_SPACE_NOT_ENOUGH:
                        implRes.setStatusText(mResources.getString(R.string.download_status_insufficient_space));
                        break;
                    default:
                        implRes.setStatusText(mResources.getString(R.string.download_status_error));
                        break;
                }
                implRes.setDescText(getSizeText(context, implInfo.getCurrent(), implInfo.getTotal()));
                break;

            case ImplInfo.STATUS_SUCCESSFUL:
                implRes.setAction(ImplInfo.ACTION_OPEN);
                implRes.setActionText(mResources.getString(R.string.action_open));
                implRes.setStatusText(mResources.getString(R.string.download_status_success));
                implRes.setDescText(Formatter.formatFileSize(context, implInfo.getTotal()));
                //下载的是apk
                if (null != localPath
                        && "application/vnd.android.package-archive".equals(mimeType)) {
                    PackageInfo archivePkg = context.getPackageManager()
                            .getPackageArchiveInfo(localPath, PackageManager.GET_ACTIVITIES);
                    if (null != archivePkg) {
                        implRes.setAction(ImplInfo.ACTION_OPEN);
                        implRes.setActionIntent(getLaunchDownloadIntent(context, archivePkg.packageName));
                        if (null == implRes.getActionIntent()) {
                            implRes.setActionText(mResources.getString(R.string.action_open));
                        } else {
                            implRes.setActionText(mResources.getString(R.string.action_open));
                        }
                        implRes.setDescText((String.format(mResources.getString(R.string.apk_version), archivePkg.versionName)));
                        implRes.setActionIntent(getLaunchDownloadIntent(context, archivePkg.packageName));
                    } else {//下载apk解析错误
                        implRes.setAction(ImplInfo.ACTION_DOWNLOAD);
                        implRes.setActionText(mResources.getString(R.string.action_retry));
                        implRes.setStatusText(mResources.getString(R.string.download_status_invalid_package));
                    }
                }
                implRes.setDescText(implRes.getDescText() + "|" + millis2FormatString("yy-MM-dd", implInfo.getLastMod()));
                if (null == implRes.getActionIntent()) {
                    implRes.setActionIntent(getOpenDownloadIntent(localPath, mimeType));
                }
                break;

            case ImplInfo.STATUS_INSTALLED:
                try {
                    PackageInfo installed = context.getPackageManager()
                            .getPackageInfo(implInfo.getPackageName(), PackageManager.GET_ACTIVITIES);
                    implRes.setDescText((String.format(mResources.getString(R.string.apk_version), installed.versionName)));
                    if (implInfo.getVersionCode() <= installed.versionCode) {
                        //安装版本比目标版本新，需要打开
                        implRes.setAction(ImplInfo.ACTION_OPEN);
                        implRes.setActionText(mResources.getString(R.string.action_open));
                        implRes.setStatusText(mResources.getString(R.string.install_status_success));
                        implRes.setActionIntent(getLaunchDownloadIntent(context, implInfo.getPackageName()));
                    } else {
                        //目标版本比较新
                        if (null == localPath || TextUtils.isEmpty(localPath)) {
                            //但是apk文件不存在，需要下载
                            implRes.setAction(ImplInfo.ACTION_DOWNLOAD);
                            implRes.setActionText(mResources.getString(R.string.action_upgrade));
                        } else {
                            implRes.setAction(ImplInfo.ACTION_DOWNLOAD);
                            implRes.setActionText(mResources.getString(R.string.action_upgrade));
                            PackageInfo archivePkg = context.getPackageManager()
                                    .getPackageArchiveInfo(localPath, PackageManager.GET_ACTIVITIES);
                            if (null != archivePkg) {
                                if (archivePkg.versionCode <= installed.versionCode) {
                                    //apk文件存在，但是apk的版本较旧，需要下载
                                    implRes.setAction(ImplInfo.ACTION_DOWNLOAD);
                                    implRes.setActionText(mResources.getString(R.string.action_upgrade));
                                } else {
                                    //否则需要打开安装
                                    implRes.setAction(ImplInfo.ACTION_OPEN);
                                    implRes.setActionText(mResources.getString(R.string.action_open));
                                    implRes.setActionIntent(getOpenDownloadIntent(localPath, mimeType));
                                }
                            } else {
                                implRes.setActionText(mResources.getString(R.string.action_upgrade));
                                implRes.setStatusText(mResources.getString(R.string.install_status_success));
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    implRes.setAction(ImplInfo.ACTION_DOWNLOAD);
                    implRes.setActionText(mResources.getString(R.string.action_retry));
                    implRes.setStatusText(mResources.getString(R.string.download_status_invalid_package));
                    implRes.setDescText(Formatter.formatFileSize(context, implInfo.getTotal()));
                }
                implRes.setDescText(implRes.getDescText() + "|" + millis2FormatString("yy-MM-dd", implInfo.getLastMod()));
                break;


            case ImplInfo.STATUS_PRIVATE_INSTALLING:
            case ImplInfo.STATUS_NORMAL_INSTALLING:
                implRes.setAction(ImplInfo.ACTION_INSTALL);
                implRes.setActionText(mResources.getString(R.string.action_open));
                implRes.setStatusText(mResources.getString(R.string.install_status_installing));
                implRes.setDescText(Formatter.formatFileSize(context, implInfo.getTotal()));
                implRes.setDescText(implRes.getDescText() + "|" + millis2FormatString("yy-MM-dd", implInfo.getLastMod()));

                //下载的是apk
                if ("application/vnd.android.package-archive".equals(mimeType) && null != localPath) {
                    PackageInfo archivePkg = context.getPackageManager()
                            .getPackageArchiveInfo(localPath, PackageManager.GET_ACTIVITIES);
                    if (null != archivePkg) {
                        implRes.setActionIntent(getLaunchDownloadIntent(context, archivePkg.packageName));
                    }
                }
                if (null == implRes.getActionIntent()) {
                    implRes.setActionIntent(getOpenDownloadIntent(localPath, mimeType));
                }
                break;

            case ImplInfo.STATUS_PACKAGE_INVALID:
                implRes.setAction(ImplInfo.ACTION_DOWNLOAD);
                implRes.setActionText(mResources.getString(R.string.action_retry));
                implRes.setStatusText(mResources.getString(R.string.download_status_invalid_package));
                implRes.setDescText(getSizeText(context, implInfo.getCurrent(), implInfo.getTotal()));
//                implRes.setDescText(implRes.getDescText() + "|" + millis2FormatString("yy-MM-dd", implInfo.getLastMod()));
                break;

            case ImplInfo.STATUS_INSTALL_FAILED:
                implRes.setAction(ImplInfo.ACTION_DOWNLOAD);
                implRes.setActionText(mResources.getString(R.string.action_retry));
                implRes.setStatusText(mResources.getString(R.string.install_status_failed));
                implRes.setDescText(Formatter.formatFileSize(context, implInfo.getTotal()));
                implRes.setDescText(implRes.getDescText() + "|" + millis2FormatString("yy-MM-dd", implInfo.getLastMod()));
                break;
        }
    }

}
