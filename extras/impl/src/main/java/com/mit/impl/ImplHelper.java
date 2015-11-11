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
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by hxd on 15-6-10.
 */
public class ImplHelper {
//    private final static String TAG = "impl_helper";

    public static String getSizeText(Context context, long currentBytes, long totalBytes) {
        StringBuffer sizeText = new StringBuffer();
        if (totalBytes > 0) {
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
        fillImplRes(context, implInfo);
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
            startActivity(context, res);
        }
    }

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

        byte[] b = digest.digest();
        return byteToHexString(b);

//        BigInteger bigInt = new BigInteger(1, digest.digest());
//        String md5 = bigInt.toString(16);
//        if (md5.length() < 32) {
//            md5 = "0" + md5;
//        }
//        return md5;
    }

    static char hexdigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
    private static String byteToHexString(byte[] tmp) {
        String s;
        // 用字节表示就是 16 个字节
        char str[] = new char[16 * 2]; // 每个字节用 16 进制表示的话，使用两个字符，
        // 所以表示成 16 进制需要 32 个字符
        int k = 0; // 表示转换结果中对应的字符位置+

        for (int i = 0; i < 16; i++) { // 从第一个字节开始，对 MD5 的每一个字节
            // 转换成 16 进制字符的转换
            byte byte0 = tmp[i]; // 取第 i 个字节
            str[k++] = hexdigits[byte0 >>> 4 & 0xf]; // 取字节中高 4 位的数字转换,
            // >>> 为逻辑右移，将符号位一起右移
            str[k++] = hexdigits[byte0 & 0xf]; // 取字节中低 4 位的数字转换
        }
        s = new String(str); // 换后的结果转换为字符串
        return s;
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
                implRes.setStatusText(mResources.getString(R.string.download_status_waiting));
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
                            implRes.setActionText(mResources.getString(R.string.action_install));
                        } else {
                            implRes.setActionText(mResources.getString(R.string.action_install));
                        }
                        implRes.setDescText((String.format(mResources.getString(R.string.apk_version), archivePkg.versionName)));
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
                        //已安装版本比目标版本新，需要打开
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
                                    //否则需要升级安装
//                                    implRes.setAction(ImplInfo.ACTION_OPEN);
//                                    implRes.setActionText(mResources.getString(R.string.action_open));
                                    implRes.setAction(ImplInfo.ACTION_INSTALL);
                                    implRes.setActionText(mResources.getString(R.string.action_install));
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
                implRes.setActionText(mResources.getString(R.string.action_install));
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
