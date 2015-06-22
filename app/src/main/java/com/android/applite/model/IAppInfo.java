package com.android.applite.model;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.android.dsc.downloads.DownloadManager;

import java.util.Observer;

public interface IAppInfo {
    public final static String extenStorageDirPath = ".android/";
    public final static int AppOnline = 0;            //未下载
    public final static int AppOffline = 1;            //已下载，未安装
    public final static int AppInstalled = 2;        //显示在offline区
    public final static int AppUpgrade = 3;         //显示在offline区
    public final static int AppMore = 4;               //更多，显示在online
    
    public final static int CatgoryNone = 0;        //未分类
    public final static int CatgoryYlzx = 1;           //娱乐中心
    public final static int CatgoryZjbb = 2;          //装机必备
    
    public final static int STATUS_INIT = 0;
    public final static int STATUS_PENDING = DownloadManager.STATUS_PENDING;
    public final static int STATUS_RUNNING = DownloadManager.STATUS_RUNNING;
    public final static int STATUS_PAUSED = DownloadManager.STATUS_PAUSED;
    public final static int STATUS_SUCCESSFUL = DownloadManager.STATUS_SUCCESSFUL;
    public final static int STATUS_FAILED = DownloadManager.STATUS_FAILED;
    public final static int STATUS_PACKAGE_INVALID = 1<<8;
    public final static int STATUS_PRIVATE_INSTALLING = 1<<9;
    public final static int STATUS_NORMAL_INSTALLING=1<<10;
    public final static int STATUS_INSTALLED = 1<<11;
    public final static int STATUS_INSTALL_FAILED = 1<<12;
    

    
    public final static int NETWORK_WIFI = DownloadManager.Request.NETWORK_WIFI;
    public final static int NETWORK_MOBILE = DownloadManager.Request.NETWORK_MOBILE;

    public final static String COLUMN_STATUS = DownloadManager.COLUMN_STATUS;
    public final static String COLUMN_LOCAL_URI = DownloadManager.COLUMN_LOCAL_URI;
    

	public void request(Intent cmd);
	public void abort(Intent cmd);
    
    public String getId();
    public Bitmap getIcon();
    public Intent getIntent();
    public long getSize();
    public int getItemType();
    public String getVersionName();
    public void setShown();
    public void clearDisplay();
    public ComponentName getComponentName();
    public String getDetailUrl();
    public String getDetailText();
    public String getTitle();
    public void removeApp();
    public void launchApp();
    public void downloadApp();
    public void drawIcon(Canvas canvas, int left, int top, int right, int bottom);
    
    public void registerObserver(Observer o);
    public void unregisterObserver(Observer o);
}
