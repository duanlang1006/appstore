package com.mit.impl;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;

import com.lidroid.xutils.db.annotation.Table;
import com.lidroid.xutils.db.annotation.Transient;
import com.lidroid.xutils.http.HttpHandler;
import com.mit.mitupdatesdk.MitMobclickAgent;

import java.io.File;

/**
 * Created by hxd on 15-6-11.
 */
@Table(name=ImplDbHelper.TABLE_IMPLINFO)
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

    private long _id;               //数据库绑定自增ID
    private String key;             //唯一主键
    private String packageName;     //包名
    private int status;             //impl状态
    private int cause;              //impl原因
    private String downloadUrl;     //下载地址
    private String iconUrl;         //图标地址
    private String title;           //title文本
    private String description;     //描述
    private long lastMod;           //最后下载时间
    private int versionCode;        //应用对应的versionCode
    private boolean autoLaunch;     //下载完成后自动启动安装
    private boolean userContinue;   //用户确认继续
    private String md5;             //下载文件对应的MD5码
    private String localPath;       //下载完成后最终路径

    @Transient
    private HttpHandler<File> handler;      //下载handler
    private HttpHandler.State state;        //下载状态
    private String fileSavePath;            //文件保存路径
    private long current;                   //当前下载的字节数
    private long total;                     //文件的总字节数
    private boolean autoResume;             //断点续传
    private boolean autoRename;             //自动重命名

    @Transient
    private ImplRes implRes;                //从implinfo生成的资源

    public ImplInfo() {
        key = null;
        packageName = null;
        status = STATUS_INIT;
        cause = CAUSE_NONE;
        downloadUrl = null;
        iconUrl = null;
        title = null;
        description = null;
        lastMod = 0;
        versionCode = 0;
        autoLaunch = false;
        userContinue = false;
        md5 = null;
        implRes = new ImplRes();
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

//    public long getDownloadId() {
//        return downloadId;
//    }

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

    public int getVersionCode() {
        return versionCode;
    }

    public String getLocalPath() {
        return localPath;
    }

//    public String getMimeType() {
//        return mimeType;
//    }

    public boolean isAutoLaunch() {
        return autoLaunch;
    }

//    public long getSize() {
//        return size;
//    }

    public boolean isUserContinue() {
        return userContinue;
    }

    public String getMd5() {
        return md5;
    }

    public HttpHandler<File> getHandler() {
        return handler;
    }

    public HttpHandler.State getState() {
        return state;
    }

    public String getFileSavePath() {
        return fileSavePath;
    }

    public long getCurrent() {
        return current;
    }

    public long getTotal() {
        return total;
    }

    public boolean isAutoResume() {
        return autoResume;
    }

    public boolean isAutoRename() {
        return autoRename;
    }

    public int getProgress(){
        int progress = 0;
        if (total > 0){
            progress = (int)(current*100/total);
        }
        return progress;
    }

    public ImplRes getImplRes() {
        return implRes;
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
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
        return this;
    }

    public ImplInfo setPackageName(String packageName) {
        this.packageName = packageName;
        return this;
    }

//    public ImplInfo setDownloadId(long downloadId) {
//        this.downloadId = downloadId;
//        return this;
//    }

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

    public ImplInfo setVersionCode(int versionCode) {
        this.versionCode = versionCode;
        return this;
    }

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

//    public ImplInfo setSize(long size) {
//        this.size = size;
//        return this;
//    }

    public ImplInfo setUserContinue(boolean userContinue) {
        this.userContinue = userContinue;
        return this;
    }

    public ImplInfo setMd5(String md5) {
        this.md5 = md5;
        return this;
    }

    public void setHandler(HttpHandler<File> handler) {
        this.handler = handler;
    }

    public void setState(HttpHandler.State state) {
        this.state = state;
    }

    public void setFileSavePath(String fileSavePath) {
        this.fileSavePath = fileSavePath;
    }

    public void setCurrent(long progress) {
        this.current = progress;
    }

    public void setTotal(long fileLength) {
        this.total = fileLength;
    }

    public void setAutoResume(boolean autoResume) {
        this.autoResume = autoResume;
    }

    public void setAutoRename(boolean autoRename) {
        this.autoRename = autoRename;
    }

    @Override
    public String toString() {
        return "ImplInfo:"+title+","+state+","+status;
    }

    public void initImplRes(Context context){
        if (implRes.inited){
            return;
        }
        ImplHelper.fillImplRes(context,this);
    }

    public void updateImplRes(Context context){
        ImplHelper.fillImplRes(context,this);
    }

    public class ImplRes{
        private boolean inited;
        private int action;
        private String actionText;
        private String statusText;
        private String descText;
        private Intent actionIntent;

        public ImplRes() {
            reset();
        }

        public void reset(){
            action = ImplInfo.ACTION_DOWNLOAD;
            actionText = "";
            statusText = "";
            descText = "";
            actionIntent = null;
            inited = false;
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

        Intent getActionIntent() {
            return actionIntent;
        }

        public void setAction(int action) {
            this.action = action;
        }

        public void setActionText(String actionText) {
            this.actionText = actionText;
        }

        public void setStatusText(String statusText) {
            this.statusText = statusText;
        }

        public void setDescText(String descText) {
            this.descText = descText;
        }

        public void setActionIntent(Intent actionIntent) {
            this.actionIntent = actionIntent;
        }
    }
}