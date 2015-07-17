//package com.mit.impl;
//
//import android.content.Context;
//import android.content.Intent;
//import android.content.pm.PackageInfo;
//import android.content.pm.PackageManager;
//import android.content.res.Resources;
//import android.net.Uri;
//import android.text.format.Formatter;
//import com.android.dsc.downloads.DownloadManager;
//import com.applite.common.Constant;
//
///**
// * Created by hxd on 15-6-25.
// */
//public class ImplStatusTag {
//    public final static int ACTION_DOWNLOAD = 1;   //下载
//    public final static int ACTION_INSTALL = 2;     //安装过程
//    public final static int ACTION_OPEN = 3;       //打开下载文件或者运行应用程序
//
//    private ImplInfo info;
//    private int action;           //动作类型
//    private String actionText;    //动作字符串
//    private String statusText;    //状态字符串
//    private String descText;      //详情字符串
//    private Intent actionIntent;  //动作的intent
//
//    public ImplStatusTag(Context context,ImplInfo info) {
//        this.info = info;
//        Resources mResources = context.getResources();
//        //判断是否已安装
//        if (null != getLaunchDownloadIntent(context, info.getPackageName()) ){
//            info.setStatus(Constant.STATUS_INSTALLED);
//        }
//
//        switch (info.getStatus()) {
//            case Constant.STATUS_INIT:
//                action = ImplStatusTag.ACTION_DOWNLOAD;
//                actionText = mResources.getString(R.string.action_install);
//                statusText = "";
//                descText = getSizeText(context,info.getCurrentBytes(),info.getTotalBytes());
//                actionIntent = null;
//                break;
//
//            case Constant.STATUS_PENDING:
//                action = ImplStatusTag.ACTION_DOWNLOAD;
//                actionText = mResources.getString(R.string.action_waiting);
//                statusText = mResources.getString(R.string.download_status_waiting);
//                descText = getSizeText(context,info.getCurrentBytes(),info.getTotalBytes());
//                actionIntent = null;
//                break;
//
//            case Constant.STATUS_RUNNING:
//                action = ImplStatusTag.ACTION_DOWNLOAD;
//                actionText = mResources.getString(R.string.action_pause);
//                statusText = mResources.getString(R.string.download_status_running);
//                descText = getSizeText(context,info.getCurrentBytes(),info.getTotalBytes());
//                actionIntent = null;
//                break;
//
//            case Constant.STATUS_PAUSED:
//                action = ImplStatusTag.ACTION_DOWNLOAD;
//                switch (info.getReason()) {
//                    case DownloadManager.PAUSED_QUEUED_FOR_WIFI:
//                        actionText = mResources.getString(R.string.action_pause);
//                        statusText = mResources.getString(R.string.download_status_queued);
//                        break;
//                    case DownloadManager.PAUSED_BY_APP:
//                        actionText = mResources.getString(R.string.action_resume);
//                        statusText = mResources.getString(R.string.download_status_paused);
//                        break;
//                    case DownloadManager.PAUSED_WAITING_FOR_NETWORK:
//                        actionText = mResources.getString(R.string.action_pause);
//                        statusText = mResources.getString(R.string.download_status_waiting_for_network);
//                        break;
//                    default:
//                        actionText = mResources.getString(R.string.action_pause);
//                        statusText = mResources.getString(R.string.download_status_running);
//                        break;
//                }
//                descText = getSizeText(context,info.getCurrentBytes(),info.getTotalBytes());
//                actionIntent = null;
//                break;
//
//            case Constant.STATUS_FAILED:
//                action = ImplStatusTag.ACTION_DOWNLOAD;
//                actionText = mResources.getString(R.string.action_retry);
//                if (DownloadManager.ERROR_INSUFFICIENT_SPACE == info.getReason()){
//                    statusText = mResources.getString(R.string.download_status_insufficient_space);
//                }else{
//                    statusText = mResources.getString(R.string.download_status_error);
//                }
//                descText = getSizeText(context,info.getCurrentBytes(),info.getTotalBytes());
//                actionIntent = null;
//                break;
//
//            case Constant.STATUS_SUCCESSFUL:
//                Uri localUri = (null == info.getLocalPath())?null:Uri.parse(info.getLocalPath());
//                action = ImplStatusTag.ACTION_OPEN;
//                actionText = mResources.getString(R.string.action_open);
//                statusText = mResources.getString(R.string.download_status_success);
//                descText = getSizeText(context,info.getCurrentBytes(),info.getTotalBytes());
//                actionIntent = getOpenDownloadIntent(localUri,info.getMimeType());
//                //下载的是apk
//                if ("application/vnd.android.package-archive".equals(info.getMimeType()) && null != localUri) {
//                    PackageInfo archivePkg = context.getPackageManager()
//                            .getPackageArchiveInfo(localUri.getPath(), PackageManager.GET_ACTIVITIES);
//                    if (null != archivePkg){
//                        action = ImplStatusTag.ACTION_OPEN;
//                        descText = (String.format(mResources.getString(R.string.apk_version), archivePkg.versionName));
//                        actionIntent = getLaunchDownloadIntent(context,archivePkg.packageName);
//                        if (null == actionIntent){
//                            actionText = mResources.getString(R.string.action_install);
//                        }else{
//                            actionText = mResources.getString(R.string.action_run);
//                        }
//                    }else{//下载apk解析错误
//                        action = ImplStatusTag.ACTION_DOWNLOAD;
//                        info.setStatus(Constant.STATUS_FAILED);
//                        statusText = mResources.getString(R.string.download_status_invalid_package);
//                        actionText = mResources.getString(R.string.action_retry);
//                        descText = getSizeText(context,info.getCurrentBytes(),info.getTotalBytes());
//                        actionIntent = null;
//                    }
//                }
//                break;
//
//            case Constant.STATUS_INSTALLED:
//                try {
//                    PackageInfo installPkg = context.getPackageManager().getPackageInfo(info.getPackageName(), PackageManager.GET_ACTIVITIES);
//                    action = ImplStatusTag.ACTION_OPEN;
//                    statusText = mResources.getString(R.string.install_status_success);
//                    actionText = mResources.getString(R.string.action_run);
//                    descText = (String.format(mResources.getString(R.string.apk_version), installPkg.versionName));
//                    actionIntent = getLaunchDownloadIntent(context,info.getPackageName());
//                } catch (PackageManager.NameNotFoundException e) {
//                    e.printStackTrace();
//                    action = ImplStatusTag.ACTION_DOWNLOAD;
//                    statusText = mResources.getString(R.string.download_status_invalid_package);
//                    actionText = mResources.getString(R.string.action_retry);
//                    descText = getSizeText(context,info.getCurrentBytes(),info.getTotalBytes());
//                    actionIntent = null;
//                }
//                break;
//
//            case Constant.STATUS_PRIVATE_INSTALLING:
//                action = ImplStatusTag.ACTION_INSTALL;
//                statusText = mResources.getString(R.string.install_status_installing);
//                actionText = mResources.getString(R.string.action_open);
//                descText = getSizeText(context,info.getCurrentBytes(),info.getTotalBytes());
//                actionIntent = null;
//                break;
//
//            case Constant.STATUS_NORMAL_INSTALLING:
//                action = ImplStatusTag.ACTION_INSTALL;
//                statusText = mResources.getString(R.string.install_status_installing);
//                actionText = mResources.getString(R.string.action_open);
//                descText = getSizeText(context,info.getCurrentBytes(),info.getTotalBytes());
//                actionIntent = null;
//                break;
//
//            case Constant.STATUS_PACKAGE_INVALID:
//                action = ImplStatusTag.ACTION_DOWNLOAD;
//                statusText = mResources.getString(R.string.download_status_invalid_package);
//                actionText = mResources.getString(R.string.action_open);
//                descText = getSizeText(context,info.getCurrentBytes(),info.getTotalBytes());
//                actionIntent = null;
//                break;
//            case Constant.STATUS_INSTALL_FAILED:
//                action = ImplStatusTag.ACTION_DOWNLOAD;
//                statusText = mResources.getString(R.string.install_status_failed);
//                actionText = mResources.getString(R.string.action_open);
//                descText = getSizeText(context,info.getCurrentBytes(),info.getTotalBytes());
//                actionIntent = null;
//                break;
//        }
//    }
//
//    public int getStatus() {
//        return info.getStatus();
//    }
//
//    public int getAction() {
//        return action;
//    }
//
//    public String getActionText() {
//        return actionText;
//    }
//
//    public String getStatusText() {
//        return statusText;
//    }
//
//    public String getDescText() {
//        return descText;
//    }
//
//    public Intent getActionIntent() {
//        return actionIntent;
//    }
//
//    //    public static ImplStatusTag generateTag(Context context,ImplInfo info){
////        ImplStatusTag tag = new ImplStatusTag(context,info);
////        return tag;
////    }
//
////    public static ImplStatusTag generateTag(Context context ,
////                       String key,
////                       String packageName,
////                       String title,
////                       String iconUrl,
////                       int status,
////                       int reason,
////                       long currentBytes,
////                       long totalBytes,
////                       Uri localUri,
////                       String mediaType){
////        ImplLog.d("impl_status","status="+status);
////        Resources mResources = context.getResources();
//////        String sizeText = getSizeText(context,currentBytes,totalBytes);
//////        int percent = getProgress(currentBytes,totalBytes);
////        ImplStatusTag tag = new ImplStatusTag(context,key,packageName);
////        Intent intent = getLaunchDownloadIntent(context, packageName);
////        if (intent != null ){
////            status = Constant.STATUS_INSTALLED;
////        }
////
////        switch (status) {
////            case Constant.STATUS_INIT:
////                tag.setAction(ImplStatusTag.ACTION_DOWNLOAD);
////                tag.setActionText(mResources.getString(R.string.action_install));
////                tag.setStatusText("");
////                tag.setDescText(sizeText);
////                break;
////            case Constant.STATUS_FAILED:
////                tag.setAction(ImplStatusTag.ACTION_DOWNLOAD);
////                tag.setActionText(mResources.getString(R.string.action_retry));
////                if (DownloadManager.ERROR_INSUFFICIENT_SPACE == reason){
////                    tag.setStatusText(mResources.getString(R.string.download_status_insufficient_space));
////                }else{
////                    tag.setStatusText(mResources.getString(R.string.download_status_error));
////                }
////                tag.setDescText(sizeText);
////                break;
////
////            case Constant.STATUS_SUCCESSFUL:
////                tag.setAction(ImplStatusTag.ACTION_OPEN);
////                tag.setActionText(mResources.getString(R.string.action_open));
////                tag.setStatusText(mResources.getString(R.string.download_status_success));
////                tag.setDescText(sizeText);
////                tag.setIntent(getOpenDownloadIntent(localUri,mediaType));
////                ImplLog.d("impl_status",localUri+","+mediaType);
////                //下载的是apk
////                if ("application/vnd.android.package-archive".equals(mediaType) && null != localUri) {
////                    PackageInfo archivePkg = context.getPackageManager().getPackageArchiveInfo(
////                            localUri.getPath(), PackageManager.GET_ACTIVITIES);
////                    if (null != archivePkg){
////                        tag.setAction(ImplStatusTag.ACTION_OPEN);
////                        tag.setDescText(String.format(mResources.getString(R.string.apk_version), archivePkg.versionName));
////                        intent = getLaunchDownloadIntent(context,archivePkg.packageName);
////                        if (null == intent){
////                            tag.setActionText(mResources.getString(R.string.action_install));
////                        }else{
////                            tag.setIntent(intent);
////                            tag.setActionText(mResources.getString(R.string.action_run));
////                        }
////                    }else{//下载apk解析错误
////                        tag.setAction(ImplStatusTag.ACTION_DOWNLOAD);
////                        tag.setStatusText(mResources.getString(R.string.download_status_invalid_package));
////                        tag.setActionText(mResources.getString(R.string.action_retry));
////                        tag.setDescText(sizeText);
////                        tag.setIntent(null);
////                    }
////                }
////                break;
////
////            case Constant.STATUS_PENDING:
////            case Constant.STATUS_RUNNING:
////                tag.setAction(ImplStatusTag.ACTION_DOWNLOAD);
////                tag.setStatusText(mResources.getString(R.string.download_status_running));
////                tag.setActionText(mResources.getString(R.string.action_pause));
////                tag.setDescText(sizeText);
////                tag.setIntent(null);
////                break;
////
////            case Constant.STATUS_PAUSED:
////                tag.setAction(ImplStatusTag.ACTION_DOWNLOAD);
////                switch (reason) {
////                    case DownloadManager.PAUSED_QUEUED_FOR_WIFI:
////                        tag.setStatusText(mResources.getString(R.string.download_status_queued));
////                        tag.setActionText(mResources.getString(R.string.action_pause));
////                        break;
////                    case DownloadManager.PAUSED_BY_APP:
////                        tag.setStatusText(mResources.getString(R.string.download_status_paused));
////                        tag.setActionText(mResources.getString(R.string.action_resume));
////                        break;
////                    case DownloadManager.PAUSED_WAITING_FOR_NETWORK:
////                        tag.setStatusText(mResources.getString(R.string.download_status_waiting_for_network));
////                        tag.setActionText(mResources.getString(R.string.action_pause));
////                        break;
////                    default:
////                        tag.setStatusText(mResources.getString(R.string.download_status_running));
////                        tag.setActionText(mResources.getString(R.string.action_pause));
////                        break;
////                }
////                tag.setDescText(sizeText);
////                tag.setIntent(null);
////                break;
////            case Constant.STATUS_INSTALLED:
////                try {
////                    PackageInfo installPkg = context.getPackageManager().getPackageInfo(tag.getPackageName(), PackageManager.GET_ACTIVITIES);
////                    tag.setAction(ImplStatusTag.ACTION_OPEN);
////                    tag.setStatusText(mResources.getString(R.string.install_status_success));
////                    tag.setActionText(mResources.getString(R.string.action_run));
////                    tag.setIntent(getLaunchDownloadIntent(context,tag.getPackageName()));
////                    tag.setDescText(String.format(mResources.getString(R.string.apk_version), installPkg.versionName));
////                } catch (PackageManager.NameNotFoundException e) {
////                    e.printStackTrace();
////                    tag.setAction(ImplStatusTag.ACTION_DOWNLOAD);
////                    tag.setStatusText(mResources.getString(R.string.download_status_invalid_package));
////                    tag.setActionText(mResources.getString(R.string.action_retry));
////                    tag.setDescText(sizeText);
////                    tag.setIntent(null);
////                }
////                break;
////            case Constant.STATUS_PRIVATE_INSTALLING:
////                tag.setAction(ImplStatusTag.ACTION_INSTALL);
////                tag.setStatusText(mResources.getString(R.string.install_status_installing));
////                tag.setActionText(mResources.getString(R.string.action_open));
////                tag.setDescText(sizeText);
////                tag.setIntent(null);
////                break;
////            case Constant.STATUS_NORMAL_INSTALLING:
////                tag.setAction(ImplStatusTag.ACTION_INSTALL);
////                tag.setStatusText(mResources.getString(R.string.install_status_installing));
////                tag.setActionText(mResources.getString(R.string.action_open));
////                tag.setDescText(sizeText);
////                tag.setIntent(null);
////                break;
////            case Constant.STATUS_PACKAGE_INVALID:
////                tag.setAction(ImplStatusTag.ACTION_DOWNLOAD);
////                tag.setStatusText(mResources.getString(R.string.download_status_invalid_package));
////                tag.setActionText(mResources.getString(R.string.action_open));
////                tag.setDescText(sizeText);
////                tag.setIntent(null);
////                break;
////            case Constant.STATUS_INSTALL_FAILED:
////                tag.setAction(ImplStatusTag.ACTION_DOWNLOAD);
////                tag.setStatusText(mResources.getString(R.string.install_status_failed));
////                tag.setActionText(mResources.getString(R.string.action_open));
////                tag.setDescText(sizeText);
////                tag.setIntent(null);
////                break;
////        }
////        return tag;
////    }
//
//    public static String getSizeText(Context context,long currentBytes,long totalBytes) {
//        StringBuffer sizeText = new StringBuffer();
//        if (totalBytes >= 0) {
//            sizeText.append(Formatter.formatFileSize(context, currentBytes));
//            sizeText.append("/");
//            sizeText.append(Formatter.formatFileSize(context, totalBytes));
//        }
//        return sizeText.toString();
//    }
//
//    public static int getProgress(long currentBytes,long totalBytes){
//        int percent = 0;
//        if (totalBytes > 0) {
//            percent = (int) ((currentBytes * 100) / totalBytes);
//        }
//        return percent;
//    }
//
//    public static Intent getOpenDownloadIntent(Uri localUri,String mediaType) {
//        if (null != localUri) {
//            Intent intent = new Intent(Intent.ACTION_VIEW);
//            intent.setDataAndType(localUri, mediaType);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            return intent;
//        }
//        return null;
//    }
//
//    public static Intent getLaunchDownloadIntent(Context context ,String packageName) {
//        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
//        if (null != intent) {
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
//        }
//        return intent;
//    }
//}
