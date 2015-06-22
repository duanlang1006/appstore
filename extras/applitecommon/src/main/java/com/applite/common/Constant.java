package com.applite.common;

import android.app.DownloadManager;

public class Constant {
    public static final String URL = "http://192.168.1.104/app_interface/app_main_interface.php";

    public static final String OSGI_SERVICE_LOGO_FRAGMENT = "osgi.service.logo.fragment";
    public static final String OSGI_SERVICE_MAIN_FRAGMENT = "osgi.service.main.fragment";
    public static final String OSGI_SERVICE_SETTING_FRAGMENT = "osgi.service.setting.fragment";
    public static final String OSGI_SERVICE_SEARCH_FRAGMENT = "osgi.service.search.fragment";
    public static final String OSGI_SERVICE_DM_FRAGMENT = "osgi.service.dm.fragment";
    public static final String OSGI_SERVICE_UPGRADE_FRAGMENT = "osgi.service.upgrade.fragment";
    public static final String OSGI_SERVICE_HOST_OPT = "osgi.service.host.opt";
    public static final String OSGI_SERVICE_DETAIL_FRAGMENT = "osgi.service.detail.fragment";


    public static final String CONFIG_BUNDLES_INFO = "bundles_info";
    public static final String KEY_BUNDLES = "bundles";


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

    public final static int INSTALL_SUCCEEDED = 1;//ApplicationManager
    public final static int DELETE_SUCCEEDED = 1; //ApplicationManager
}
