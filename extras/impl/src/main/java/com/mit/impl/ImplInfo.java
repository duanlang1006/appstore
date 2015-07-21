package com.mit.impl;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.text.format.Formatter;

import com.android.dsc.downloads.DownloadManager;
import com.applite.common.Constant;

/**
 * Created by hxd on 15-6-11.
 */
public class ImplInfo {
    public final static int ACTION_DOWNLOAD = 1;   //下载
    public final static int ACTION_INSTALL = 2;     //安装过程
    public final static int ACTION_OPEN = 3;       //打开下载文件或者运行应用程序

    private String key;
    private String downloadUrl;
    private long downloadId ;
    private String packageName;
    private String iconPath;
    private String iconUrl;
    private int status;
    private int reason;
    private String title;
    private String description;
    private long totalBytes;
    private long currentBytes;
    private String localPath;
    private String mimeType;
    private long lastMod;

    private ImplInfo(String key,String downloadUrl,String packageName){
        this.key = key;
        this.downloadUrl = downloadUrl;
        this.packageName = packageName;
        this.status = Constant.STATUS_INIT;
        this.totalBytes = 0;
        this.currentBytes = 0;
        this.localPath = null;
        this.mimeType = null;
        this.lastMod = System.currentTimeMillis();
    }

    public static ImplInfo create(Context context,String key,String downloadUrl,String packageName,int versionCode){
        ImplInfo info = new ImplInfo(key,downloadUrl,packageName);
        try {
            PackageInfo pakageinfo = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            if (versionCode <= pakageinfo.versionCode) {
                info.setStatus(Constant.STATUS_INSTALLED);
            }else{
                info.setStatus(Constant.STATUS_UPGRADE);
            }
        } catch (PackageManager.NameNotFoundException e) {
            //e.printStackTrace();
        }
        return info;
    }

    public String getKey() {
        return key;
    }

    public long getDownloadId() {
        return downloadId;
    }

    public String getPackageName() {
        return packageName;
    }

    public int getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public long getCurrentBytes() {
        return currentBytes;
    }

    public String getLocalPath() {
        return localPath;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public ImplInfo setDownloadId(long downloadId) {
        this.downloadId = downloadId;
        return this;
    }

    public ImplInfo setIconPath(String iconPath) {
        this.iconPath = iconPath;
        return this;
    }

    public ImplInfo setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
        return this;
    }

    public ImplInfo setTitle(String title) {
        this.title = title;
        return this;
    }

    public ImplInfo setDescription(String description) {
        this.description = description;
        return this;
    }

    public ImplInfo setStatus(int status) {
        this.status = status;
        return this;
    }

    public ImplInfo setTotalBytes(long totalBytes) {
        this.totalBytes = totalBytes;
        return this;
    }

    public ImplInfo setCurrentBytes(long currentBytes) {
        this.currentBytes = currentBytes;
        return this;
    }

    public ImplInfo setLocalPath(String localPath) {
        this.localPath = localPath;
        return this;
    }

    public ImplInfo setMimeType(String mimeType) {
        this.mimeType = mimeType;
        return this;
    }

    public ImplInfo setLastMod(long lastMod) {
        this.lastMod = lastMod;
        return this;
    }

    public ImplInfo setReason(int reason) {
        this.reason = reason;
        return this;
    }

    public int getProgress(){
        int percent = 0;
        if (totalBytes > 0) {
            percent = (int) ((currentBytes * 100) / totalBytes);
        }
        return percent;
    }

    public int getAction(Context context) {
        int action = ACTION_DOWNLOAD;
        switch (getStatus()) {
            case Constant.STATUS_INIT:
            case Constant.STATUS_PENDING:
            case Constant.STATUS_RUNNING:
            case Constant.STATUS_FAILED:
                action = ACTION_DOWNLOAD;
                break;

            case Constant.STATUS_SUCCESSFUL:
                Uri localUri = (null == localPath)?null:Uri.parse(localPath);
                action = ACTION_OPEN;
                //下载的是apk
                if ("application/vnd.android.package-archive".equals(mimeType) && null != localUri) {
                    PackageInfo archivePkg = context.getPackageManager()
                            .getPackageArchiveInfo(localUri.getPath(), PackageManager.GET_ACTIVITIES);
                    if (null != archivePkg){
                        action = ACTION_OPEN;
                    }else{//下载apk解析错误
                        action = ACTION_DOWNLOAD;
                    }
                }
                break;

            case Constant.STATUS_INSTALLED:
                try {
                    PackageInfo installPkg = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
                    action = ACTION_OPEN;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    action = ACTION_DOWNLOAD;
                }
                break;

            case Constant.STATUS_UPGRADE:
                action = ACTION_DOWNLOAD;
                break;

            case Constant.STATUS_PRIVATE_INSTALLING:
            case Constant.STATUS_NORMAL_INSTALLING:
                action = ACTION_INSTALL;
                break;

            case Constant.STATUS_PACKAGE_INVALID:
            case Constant.STATUS_INSTALL_FAILED:
                action = ACTION_DOWNLOAD;
                break;
        }
        return action;
    }

    public String getActionText(Context context) {
        Resources mResources = context.getResources();
        String actionText = "" ;
        switch (getStatus()) {
            case Constant.STATUS_INIT:
                actionText = mResources.getString(R.string.action_install);
                break;

            case Constant.STATUS_PENDING:
                actionText = mResources.getString(R.string.action_waiting);
                break;

            case Constant.STATUS_RUNNING:
                actionText = mResources.getString(R.string.action_pause);
                break;

            case Constant.STATUS_PAUSED:
                switch (reason) {
                    case DownloadManager.PAUSED_QUEUED_FOR_WIFI:
                        actionText = mResources.getString(R.string.action_pause);
                        break;
                    case DownloadManager.PAUSED_BY_APP:
                        actionText = mResources.getString(R.string.action_resume);
                        break;
                    case DownloadManager.PAUSED_WAITING_FOR_NETWORK:
                        actionText = mResources.getString(R.string.action_pause);
                        break;
                    default:
                        actionText = mResources.getString(R.string.action_pause);
                        break;
                }
                break;

            case Constant.STATUS_FAILED:
                actionText = mResources.getString(R.string.action_retry);
                break;

            case Constant.STATUS_SUCCESSFUL:
                Uri localUri = (null == localPath)?null:Uri.parse(localPath);
                actionText = mResources.getString(R.string.action_open);
                //下载的是apk
                if ("application/vnd.android.package-archive".equals(mimeType) && null != localUri) {
                    PackageInfo archivePkg = context.getPackageManager()
                            .getPackageArchiveInfo(localUri.getPath(), PackageManager.GET_ACTIVITIES);
                    if (null != archivePkg){
                        Intent intent = getLaunchDownloadIntent(context,archivePkg.packageName);
                        if (null == intent){
                            actionText = mResources.getString(R.string.action_install);
                        }else{
                            actionText = mResources.getString(R.string.action_run);
                        }
                    }else{//下载apk解析错误
                        actionText = mResources.getString(R.string.action_retry);
                    }
                }
                break;

            case Constant.STATUS_INSTALLED:
                try {
                    PackageInfo installPkg = context.getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
                    actionText = mResources.getString(R.string.action_run);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    actionText = mResources.getString(R.string.action_retry);
                }
                break;

            case Constant.STATUS_UPGRADE:
                actionText = mResources.getString(R.string.action_upgrade);
                break;

            case Constant.STATUS_PRIVATE_INSTALLING:
            case Constant.STATUS_NORMAL_INSTALLING:
            case Constant.STATUS_PACKAGE_INVALID:
            case Constant.STATUS_INSTALL_FAILED:
                actionText = mResources.getString(R.string.action_open);
                break;
        }
        return actionText;
    }

    public String getStatusText(Context context) {
        Resources mResources = context.getResources();
        String statusText = "" ;
        switch (getStatus()) {
            case Constant.STATUS_INIT:
                statusText = "";
                break;

            case Constant.STATUS_PENDING:
                statusText = mResources.getString(R.string.download_status_waiting);
                break;

            case Constant.STATUS_RUNNING:
                statusText = mResources.getString(R.string.download_status_running);
                break;

            case Constant.STATUS_PAUSED:
                switch (reason) {
                    case DownloadManager.PAUSED_QUEUED_FOR_WIFI:
                        statusText = mResources.getString(R.string.download_status_queued);
                        break;
                    case DownloadManager.PAUSED_BY_APP:
                        statusText = mResources.getString(R.string.download_status_paused);
                        break;
                    case DownloadManager.PAUSED_WAITING_FOR_NETWORK:
                        statusText = mResources.getString(R.string.download_status_waiting_for_network);
                        break;
                    default:
                        statusText = mResources.getString(R.string.download_status_running);
                        break;
                }
                break;

            case Constant.STATUS_FAILED:
                if (DownloadManager.ERROR_INSUFFICIENT_SPACE == reason){
                    statusText = mResources.getString(R.string.download_status_insufficient_space);
                }else{
                    statusText = mResources.getString(R.string.download_status_error);
                }
                break;

            case Constant.STATUS_SUCCESSFUL:
                Uri localUri = (null == localPath)?null:Uri.parse(localPath);
                statusText = mResources.getString(R.string.download_status_success);
                //下载的是apk
                if ("application/vnd.android.package-archive".equals(mimeType) && null != localUri) {
                    PackageInfo archivePkg = context.getPackageManager()
                            .getPackageArchiveInfo(localUri.getPath(), PackageManager.GET_ACTIVITIES);
                    if (null != archivePkg){
                    }else{//下载apk解析错误
                        statusText = mResources.getString(R.string.download_status_invalid_package);
                    }
                }
                break;

            case Constant.STATUS_INSTALLED:
                try {
                    PackageInfo installPkg = context.getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
                    statusText = mResources.getString(R.string.install_status_success);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    statusText = mResources.getString(R.string.download_status_invalid_package);
                }
                break;

            case Constant.STATUS_UPGRADE:
                statusText = mResources.getString(R.string.install_status_upgrade);
                break;

            case Constant.STATUS_PRIVATE_INSTALLING:
            case Constant.STATUS_NORMAL_INSTALLING:
                statusText = mResources.getString(R.string.install_status_installing);
                break;

            case Constant.STATUS_PACKAGE_INVALID:
                statusText = mResources.getString(R.string.download_status_invalid_package);
                break;
            case Constant.STATUS_INSTALL_FAILED:
                statusText = mResources.getString(R.string.install_status_failed);
                break;
        }
        return statusText;
    }

    public String getDescText(Context context) {
        Resources mResources = context.getResources();
        String descText = "" ;
        switch (getStatus()) {
            case Constant.STATUS_INIT:
            case Constant.STATUS_PENDING:
            case Constant.STATUS_RUNNING:
            case Constant.STATUS_PAUSED:
            case Constant.STATUS_FAILED:
                descText = getSizeText(context,getCurrentBytes(),getTotalBytes());
                break;

            case Constant.STATUS_SUCCESSFUL:
                Uri localUri = (null == localPath)?null:Uri.parse(localPath);
                descText = getSizeText(context, getCurrentBytes(), getTotalBytes());
                //下载的是apk
                if ("application/vnd.android.package-archive".equals(mimeType) && null != localUri) {
                    PackageInfo archivePkg = context.getPackageManager()
                            .getPackageArchiveInfo(localUri.getPath(), PackageManager.GET_ACTIVITIES);
                    if (null != archivePkg){
                        descText = (String.format(mResources.getString(R.string.apk_version), archivePkg.versionName));
                    }
                }
                break;

            case Constant.STATUS_INSTALLED:
                try {
                    PackageInfo installPkg = context.getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
                    descText = (String.format(mResources.getString(R.string.apk_version), installPkg.versionName));
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    descText = getSizeText(context,getCurrentBytes(),getTotalBytes());
                }
                break;

            case Constant.STATUS_UPGRADE:
                descText = mResources.getString(R.string.install_status_upgrade);
                break;

            case Constant.STATUS_PRIVATE_INSTALLING:
            case Constant.STATUS_NORMAL_INSTALLING:
            case Constant.STATUS_PACKAGE_INVALID:
            case Constant.STATUS_INSTALL_FAILED:
                descText = getSizeText(context,getCurrentBytes(),getTotalBytes());
                break;
        }
        return descText;
    }

    public Intent getActionIntent(Context context) {
        Intent actionIntent = null;
        switch (getStatus()) {
            case Constant.STATUS_INIT:
            case Constant.STATUS_PENDING:
            case Constant.STATUS_RUNNING:
            case Constant.STATUS_PAUSED:
            case Constant.STATUS_FAILED:
                actionIntent = null;
                break;

            case Constant.STATUS_PRIVATE_INSTALLING:
            case Constant.STATUS_NORMAL_INSTALLING:
            case Constant.STATUS_SUCCESSFUL:
                Uri localUri = (null == localPath)?null:Uri.parse(localPath);
                //下载的是apk
                if ("application/vnd.android.package-archive".equals(mimeType) && null != localUri) {
                    PackageInfo archivePkg = context.getPackageManager()
                            .getPackageArchiveInfo(localUri.getPath(), PackageManager.GET_ACTIVITIES);
                    if (null != archivePkg){
                        actionIntent = getLaunchDownloadIntent(context,archivePkg.packageName);
                    }else{//下载apk解析错误
                        actionIntent = null;
                    }
                }
                if(null == actionIntent){
                    actionIntent = getOpenDownloadIntent(localUri,mimeType);
                }
                break;

            case Constant.STATUS_INSTALLED:
                try {
                    PackageInfo installPkg = context.getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
                    actionIntent = getLaunchDownloadIntent(context,getPackageName());
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    actionIntent = null;
                }
                break;

            case Constant.STATUS_UPGRADE:
                actionIntent = null;
                break;

            case Constant.STATUS_PACKAGE_INVALID:
            case Constant.STATUS_INSTALL_FAILED:
                actionIntent = null;
                break;
        }
        return actionIntent;
    }

    public ContentValues getContentValues(){
        ContentValues values = new ContentValues();
        values.put(ImplConfig.COLUMN_KEY,key);
        values.put(ImplConfig.COLUMN_DOWNLOADURL,downloadUrl);
        values.put(ImplConfig.COLUMN_DOWNLOADID,downloadId);
        values.put(ImplConfig.COLUMN_PACKAGENAME,packageName);
        values.put(ImplConfig.COLUMN_ICON_PATH,iconPath);
        values.put(ImplConfig.COLUMN_ICON_URL,iconUrl);
        values.put(ImplConfig.COLUMN_TITLE,title);
        values.put(ImplConfig.COLUMN_DESCRIPTION,description);
        values.put(ImplConfig.COLUMN_TOTAL_BYTES,totalBytes);
        values.put(ImplConfig.COLUMN_CURRENT_BYTES,currentBytes);
        values.put(ImplConfig.COLUMN_LOCALURI, localPath);
        values.put(ImplConfig.COLUMN_MIMETYPE,mimeType);
        values.put(ImplConfig.COLUMN_STATUS,status);
        values.put(ImplConfig.COLUMN_REASON,reason);
        values.put(ImplConfig.COLUMN_LAST_MODIFIED_TIMESTAMP,lastMod);
        return values;
    }

    public static ImplInfo from(Cursor c){
        String key = c.getString(c.getColumnIndex(ImplConfig.COLUMN_KEY));
        String downloadUrl = c.getString(c.getColumnIndex(ImplConfig.COLUMN_DOWNLOADURL));
        long downloadId = c.getLong(c.getColumnIndex(ImplConfig.COLUMN_DOWNLOADID));
        String packageName = c.getString(c.getColumnIndex(ImplConfig.COLUMN_PACKAGENAME));
        String iconPath = c.getString(c.getColumnIndex(ImplConfig.COLUMN_ICON_PATH));
        String iconUrl = c.getString(c.getColumnIndex(ImplConfig.COLUMN_ICON_URL));
        String title = c.getString(c.getColumnIndex(ImplConfig.COLUMN_TITLE));
        String description = c.getString(c.getColumnIndex(ImplConfig.COLUMN_DESCRIPTION));
        long totalBytes = c.getLong(c.getColumnIndex(ImplConfig.COLUMN_TOTAL_BYTES));
        long currentBytes = c.getLong(c.getColumnIndex(ImplConfig.COLUMN_CURRENT_BYTES));
        String localUri = c.getString(c.getColumnIndex(ImplConfig.COLUMN_LOCALURI));
        String mimeType = c.getString(c.getColumnIndex(ImplConfig.COLUMN_MIMETYPE));
        int status = c.getInt(c.getColumnIndex(ImplConfig.COLUMN_STATUS));
        int reason = c.getInt(c.getColumnIndex(ImplConfig.COLUMN_REASON));
        long lastMod = c.getLong(c.getColumnIndex(ImplConfig.COLUMN_LAST_MODIFIED_TIMESTAMP));
        ImplInfo info = new ImplInfo(key,downloadUrl,packageName)
                .setDownloadId(downloadId)
                .setIconPath(iconPath)
                .setIconUrl(iconUrl)
                .setTitle(title)
                .setDescription(description)
                .setTotalBytes(totalBytes)
                .setCurrentBytes(currentBytes)
                .setLocalPath(localUri)
                .setMimeType(mimeType)
                .setStatus(status)
                .setLastMod(lastMod)
                .setReason(reason);
        return info;
    }

    public static String getSizeText(Context context,long currentBytes,long totalBytes) {
        StringBuffer sizeText = new StringBuffer();
        if (totalBytes >= 0) {
            sizeText.append(Formatter.formatFileSize(context, currentBytes));
            sizeText.append("/");
            sizeText.append(Formatter.formatFileSize(context, totalBytes));
        }
        return sizeText.toString();
    }

    public static Intent getOpenDownloadIntent(Uri localUri,String mediaType) {
        if (null != localUri) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(localUri, mediaType);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            return intent;
        }
        return null;
    }

    public static Intent getLaunchDownloadIntent(Context context ,String packageName) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (null != intent) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        }
        return intent;
    }

    private void init(){
        //判断是否已安装
//        if (null != getLaunchDownloadIntent(context, packageName) ){
//            status = Constant.STATUS_INSTALLED;
//        }else{
//            status = Constant.STATUS_INIT;
//        }
//
//        this.totalBytes = 0;
//        this.currentBytes = 0;
//        this.localPath = null;
//        this.mimeType = null;
//        this.lastMod = System.currentTimeMillis();
    }
}