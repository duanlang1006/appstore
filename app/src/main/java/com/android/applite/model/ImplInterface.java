package com.android.applite.model;

import android.content.Context;
import android.content.Intent;

import com.android.dsc.downloads.DownloadManager;

public interface ImplInterface {
    final static String TAG = "ApplicationInfo";
    final static boolean DEBUG = true;
    
    public final static String ACTION_IMPL_RESULT ="applite.intent.action.IMPL_RESULT";
    public final static String ACTION_DOWNLOAD_INIT ="applite.intent.action.DOWNLOAD_INIT";
    public final static String ACTION_DOWNLOAD_REQ ="applite.intent.action.DOWNLOAD_REQ";
    public final static String ACTION_FAKE_DOWNLOAD_REQ ="applite.intent.action.FAKE_DOWNLOAD_REQ";
    public final static String ACTION_DOWNLOAD_TOGGLE_REQ ="applite.intent.action.DOWNLOAD_TOGGLE_REQ";
    public final static String ACTION_DOWNLOAD_UPDATE_REQ ="applite.intent.action.DOWNLOAD_UPDATE_REQ";
    public final static String ACTION_DOWNLOAD_DELETE_REQ ="applite.intent.action.DOWNLOAD_DELETE_REQ";
    public final static String ACTION_DOWNLOAD_COMPLETE = DownloadManager.ACTION_DOWNLOAD_COMPLETE;
    
    
    public final static String ACTION_PACKAGE_INSTALL_REQ = "applite.intent.action.PACKAGE_INSTALL_REQ";
    public final static String ACTION_PACKAGE_DELETE_REQ = "applite.intent.action.PACKAGE_DELETE_REQ";
    public final static String ACTION_PACKAGE_ADDED = Intent.ACTION_PACKAGE_ADDED;
    public final static String ACTION_PACKAGE_REMOVED = Intent.ACTION_PACKAGE_REMOVED;
    public final static String ACTION_PACKAGE_CHANGED = Intent.ACTION_PACKAGE_CHANGED;
    
    public boolean request(Context context,Intent cmd,ImplListener listener);
    public void abort(Context context ,Intent cmd,ImplListener listener);
}
