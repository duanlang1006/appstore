package com.mit.impl;

import android.content.Context;
import android.content.Intent;

import com.android.dsc.downloads.DownloadManager;

public interface ImplInterface {
    final static String TAG = "ApplicationInfo";
    final static boolean DEBUG = true;
    public final static String IMPL_ACTION_QUERY ="impl.action.QUERY";
    public final static String IMPL_ACTION_DOWNLOAD ="impl.action.DOWNLOAD";
    public final static String IMPL_ACTION_DOWNLOAD_TOGGLE ="impl.action.DOWNLOAD.TOGGLE";
    public final static String IMPL_ACTION_FAKE_DOWNLOAD ="impl.action.FAKE_DOWNLOAD";
//    public final static String IMPL_ACTION_DOWNLOAD_UPDATE ="impl.action.DOWNLOAD_UPDATE";
    public final static String IMPL_ACTION_DOWNLOAD_DELETE ="impl.action.DOWNLOAD_DELETE";
    public final static String IMPL_ACTION_DOWNLOAD_COMPLETE = DownloadManager.ACTION_DOWNLOAD_COMPLETE;

    public final static String IMPL_ACTION_INSTALL_PACKAGE = "impl.action.PACKAGE_INSTALL";
    public final static String IMPL_ACTION_DELETE_PACKAGE = "impl.action.PACKAGE_DELETE";
    public final static String IMPL_ACTION_PACKAGE_ADDED = Intent.ACTION_PACKAGE_ADDED;
    public final static String IMPL_ACTION_PACKAGE_REMOVED = Intent.ACTION_PACKAGE_REMOVED;
    public final static String IMPL_ACTION_PACKAGE_CHANGED = Intent.ACTION_PACKAGE_CHANGED;
    public final static String IMPL_ACTION_SYSTEM_INSTALL_RESULT = "com.installer.system.install.result";
    public final static String IMPL_ACTION_SYSTEM_DELETE_RESULT = "com.installer.system.delete.result";


    public boolean request(ImplAgent.ImplRequest cmd);
    public void cancel(ImplAgent.ImplRequest cmd);
}
