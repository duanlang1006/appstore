package com.applite.common;

import android.app.DownloadManager;

public class Constant {
    public static final String URL = "http://www.fuli365.net/app_interface/app_main_interface.php";

    public static final String OSGI_SERVICE_LOGO_FRAGMENT = "osgi.service.logo.fragment";
    public static final String OSGI_SERVICE_MAIN_FRAGMENT = "osgi.service.main.fragment";
    public static final String OSGI_SERVICE_TOPIC_FRAGMENT = "osgi.service.topic.fragment";
    public static final String OSGI_SERVICE_SETTING_FRAGMENT = "osgi.service.setting.fragment";
    public static final String OSGI_SERVICE_SEARCH_FRAGMENT = "osgi.service.search.fragment";
    public static final String OSGI_SERVICE_DM_FRAGMENT = "osgi.service.dm.fragment";
    public static final String OSGI_SERVICE_DM_LIST_FRAGMENT = "osgi.service.dmlist.fragment";
    public static final String OSGI_SERVICE_UPGRADE_FRAGMENT = "osgi.service.upgrade.fragment";
//    public static final String OSGI_SERVICE_HOST_OPT = "osgi.service.host.opt";
    public static final String OSGI_SERVICE_DETAIL_FRAGMENT = "osgi.service.detail.fragment";
    public static final String OSGI_SERVICE_UPDATE_FRAGMENT = "osgi.service.update.fragment";

    public static final String OSGI_SERVICE_IMPL_LISTENER = "osgi.service.impl.listener";


    public static final String CONFIG_BUNDLES_INFO = "bundles_info";
    public static final String KEY_BUNDLES = "bundles";


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

    public static final String META_DATA_MIT = "MIT_APPKEY";
    public static final String extenStorageDirPath = ".android/";//下载的APK保存文件夹
    public static final String PATH = "applite/";//下载的LOGO图片和插件保存的文件夹

    public static final int INSTALLED = 0; // 表示已经安装，且跟现在这个apk文件是一个版本
    public static final int UNINSTALLED = 1; // 表示未安装
    public static final int INSTALLED_UPDATE = 2; // 表示已经安装，版本比现在这个版本要低，可以点击按钮更新

    public static final String UPDATE_FRAGMENT_NOT = "show_update_fragment";
    public static final String LOGO_IMG_NAME = "logo.jpg";

}
