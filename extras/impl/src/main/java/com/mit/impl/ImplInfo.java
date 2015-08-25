package com.mit.impl;

import com.applite.common.Constant;

/**
 * Created by hxd on 15-6-11.
 */
public class ImplInfo {
    public final static int ACTION_DOWNLOAD = 1;   //下载
    public final static int ACTION_INSTALL = 2;    //安装过程
    public final static int ACTION_OPEN = 3;       //打开下载文件或者运行应用程序

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
        status = Constant.STATUS_INIT;
        cause = Constant.CAUSE_NONE;
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