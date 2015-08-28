package com.mit.impl;

import android.app.DownloadManager;

/**
 * Created by hxd on 15-6-11.
 */
public class ImplInfo {
    public final static int ACTION_DOWNLOAD = 1;   //下载
    public final static int ACTION_INSTALL = 2;    //安装过程
    public final static int ACTION_OPEN = 3;       //打开下载文件或者运行应用程序


    public final static int STATUS_INIT = 0;                                    //初始状态，未安装
    public final static int STATUS_PENDING = DownloadManager.STATUS_PENDING;        //下载等待中
    public final static int STATUS_RUNNING = DownloadManager.STATUS_RUNNING;        //下载进行中
    public final static int STATUS_PAUSED = DownloadManager.STATUS_PAUSED;          //下载暂停
    public final static int STATUS_SUCCESSFUL = DownloadManager.STATUS_SUCCESSFUL;  //下载成功
    public final static int STATUS_FAILED = DownloadManager.STATUS_FAILED;      //下载失败
    public final static int STATUS_PACKAGE_INVALID = 1<<8;      //包不合法
    public final static int STATUS_PRIVATE_INSTALLING = 1<<9;   //静默安装
    public final static int STATUS_NORMAL_INSTALLING=1<<10;     //普通安装
    public final static int STATUS_INSTALLED = 1<<11;           //已安装
    public final static int STATUS_INSTALL_FAILED = 1<<12;      //安装失败
//    public final static int STATUS_UPGRADE = 1<<13;             //有更新

    public final static int INSTALL_SUCCEEDED = 1;//ApplicationManager
    public final static int DELETE_SUCCEEDED = 1; //ApplicationManager

    public final static int CAUSE_NONE = 0;                 //无原因
    public final static int CAUSE_PAUSED_BY_APP = 1;        //用户主动暂停
    public final static int CAUSE_PAUSED_BY_NETWORK = 2;    //没有网络暂停
    public final static int CAUSE_PAUSED_BY_OVERSIZE = 3;   //数据网络，超过允许下载大小暂停

    private long _id;
    private String key;
    private String packageName;
    private int status;
    private int cause;
    private long downloadId ;
    private String downloadUrl;
    private String iconUrl;
    private String title;
    private String description;
    private long lastMod;
//    private int versionCode;
    private String localPath;
    private String mimeType;
    private boolean autoLaunch;  //下载完成后自动启动安装
    private long size;
    private boolean userContinue;

    public ImplInfo() {
        key = null;
        packageName = null;
        status = STATUS_INIT;
        cause = CAUSE_NONE;
        downloadId = 0;
        downloadUrl = null;
        iconUrl = null;
        title = null;
        description = null;
        lastMod = 0;
//        versionCode = 0;
        localPath = null;
        mimeType = null;
        autoLaunch = false;
        size = 0;
        userContinue = false;
    }

    public long getId() {
        return _id;
    }

    public String getKey() {
        return key;
    }

    public int getStatus() {
        return status;
    }

    public int getCause() {
        return cause;
    }

    public String getPackageName() {
        return packageName;
    }

    public long getDownloadId() {
        return downloadId;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public long getLastMod() {
        return lastMod;
    }

//    public int getVersionCode() {
//        return versionCode;
//    }

    public String getLocalPath() {
        return localPath;
    }

//    public String getMimeType() {
//        return mimeType;
//    }

    public boolean isAutoLaunch() {
        return autoLaunch;
    }

    public long getSize() {
        return size;
    }

    public boolean isUserContinue() {
        return userContinue;
    }

    public void setId(long id) {
        this._id = id;
    }

    public ImplInfo setKey(String key) {
        this.key = key;
        return this;
    }

    public ImplInfo setStatus(int status) {
        this.status = status;
        return this;
    }

    public ImplInfo setCause(int cause) {
        this.cause = cause;
        ImplLog.d("impl_info","setCause:"+cause);
        return this;
    }

    public ImplInfo setPackageName(String packageName) {
        this.packageName = packageName;
        return this;
    }

    public ImplInfo setDownloadId(long downloadId) {
        this.downloadId = downloadId;
        return this;
    }

    public ImplInfo setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
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

    public ImplInfo setLastMod(long lastMod) {
        this.lastMod = lastMod;
        return this;
    }

//    public ImplInfo setVersionCode(int versionCode) {
//        this.versionCode = versionCode;
//        return this;
//    }

    public ImplInfo setLocalPath(String localPath) {
        this.localPath = localPath;
        return this;
    }

//    public ImplInfo setMimeType(String mimeType) {
//        this.mimeType = mimeType;
//        return this;
//    }

    public ImplInfo setAutoLaunch(boolean autoLaunch) {
        this.autoLaunch = autoLaunch;
        return this;
    }

    public ImplInfo setSize(long size) {
        this.size = size;
        return this;
    }

    public ImplInfo setUserContinue(boolean userContinue) {
        this.userContinue = userContinue;
        return this;
    }
}