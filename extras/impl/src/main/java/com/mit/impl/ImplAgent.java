package com.mit.impl;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.format.Formatter;
import android.util.Log;

import com.android.dsc.downloads.DownloadManager;
import com.applite.common.Constant;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.util.MimeTypeUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

/**
 * Created by hxd on 15-6-10.
 */
public class ImplAgent {
    private final static String TAG = "impl_agent";

    private final static String IMPL_ACTION_DOWNLOAD_COMPLETE = DownloadManager.ACTION_DOWNLOAD_COMPLETE;
    private final static String IMPL_ACTION_PACKAGE_ADDED = Intent.ACTION_PACKAGE_ADDED;
    private final static String IMPL_ACTION_PACKAGE_REMOVED = Intent.ACTION_PACKAGE_REMOVED;
    private final static String IMPL_ACTION_PACKAGE_CHANGED = Intent.ACTION_PACKAGE_CHANGED;
    private final static String IMPL_ACTION_SYSTEM_INSTALL_RESULT = "com.installer.system.install.result";
    private final static String IMPL_ACTION_SYSTEM_DELETE_RESULT = "com.installer.system.delete.result";

    private static final HandlerThread sWorkerThread = new HandlerThread("impl-worker");
    static {
        sWorkerThread.start();
    }
    static final Handler mWorkHandler = new Handler(sWorkerThread.getLooper());
    private static ImplAgent mInstance = null;
    private static synchronized void initInstance(Context context){
        if (null == mInstance ){
            mInstance = new ImplAgent(context);
        }
    }

    public static ImplAgent getInstance(Context appcontext){
        if (null == mInstance){
            initInstance(appcontext);
        }
        return mInstance;
    }

    private Context mContext;
    private List<ImplInfo> mImplList;
    private DbUtils db;
    private ImplAgentCallback mImplCallback;
    private WeakHashMap<ImplListener,ImplInfo> mWeakCallbackMap;

    private ImplAgent(Context context) {
        mContext = context;
        db = ImplDbHelper.getDbUtils(mContext.getApplicationContext());
        try {
            mImplList = db.findAll(Selector.from(ImplInfo.class));
        } catch (DbException e) {
            LogUtils.e(e.getMessage(), e);
        }
        if (mImplList == null) {
            mImplList = new ArrayList<ImplInfo>();
        }
        mImplCallback = new ImplAgentCallback();
        mWeakCallbackMap = new WeakHashMap<>();
    }

    public ImplInfo getImplInfo(String key,String packageName,int versionCode){
        ImplInfo implInfo = null;
        for (int i =0;i < mImplList.size();i++){
            if (mImplList.get(i).getKey().equals(key)){
                implInfo = mImplList.get(i);
                break;
            }
        }
        if (null == implInfo){
            implInfo = new ImplInfo();
            implInfo.setKey(key);
            mImplList.add(implInfo);
        }
        implInfo.setPackageName(packageName)
                .setVersionCode(versionCode);
        ImplDownload.getInstance(mContext).fillImplInfo(implInfo);
        ImplPackageManager.getInstance(mContext).fillImplInfo(implInfo);
        if (implInfo.getDownloadId()>0){
            try {
                db.saveOrUpdate(implInfo);
            } catch (DbException e) {
                e.printStackTrace();
            }
        }
        return implInfo;
    }

    public boolean onReceive(final Context context,final Intent intent){
        mWorkHandler.post(new Runnable(){
            @Override
            public void run() {
                String action = intent.getAction();
                Log.d(TAG,"onReceive,"+action);
                ImplInfo implInfo = null;
                if (IMPL_ACTION_PACKAGE_ADDED.equals(action)){
                    String packageName = intent.getData().getSchemeSpecificPart();
                    implInfo = findImplInfoByPackageName(packageName);
                    if (null != implInfo) {
                        ImplPackageManager.getInstance(mContext).onPackageAdded(implInfo, mImplCallback);
                    }
                }else if (IMPL_ACTION_PACKAGE_CHANGED.equals(action)){
                    String packageName = intent.getData().getSchemeSpecificPart();
                    implInfo = findImplInfoByPackageName(packageName);
                    if (null != implInfo) {
                        ImplPackageManager.getInstance(mContext).onPackageChanged(implInfo, mImplCallback);
                    }
                }else if (IMPL_ACTION_PACKAGE_REMOVED.equals(action)){
                    String packageName = intent.getData().getSchemeSpecificPart();
                    implInfo = findImplInfoByPackageName(packageName);
                    if (null != implInfo) {
                        ImplPackageManager.getInstance(mContext).onPackageRemoved(implInfo, mImplCallback);
                    }
                }else if (IMPL_ACTION_SYSTEM_INSTALL_RESULT.equals(action)){
                    String packageName = intent.getStringExtra("name");
                    int result = intent.getIntExtra("result",0);
                    implInfo = findImplInfoByPackageName(packageName);
                    if (null != implInfo) {
                        ImplPackageManager.getInstance(mContext).onSystemInstallResult(implInfo, result, mImplCallback);
                    }
                }else if (IMPL_ACTION_SYSTEM_DELETE_RESULT.equals(action)){
                    String packageName = intent.getStringExtra("name");
                    int result = intent.getIntExtra("result",0);
                    implInfo = findImplInfoByPackageName(packageName);
                    if (null != implInfo) {
                        ImplPackageManager.getInstance(mContext).onSystemDeleteResult(implInfo, result, mImplCallback);
                    }
                }else if (IMPL_ACTION_DOWNLOAD_COMPLETE.equals(action)){
//                    ImplDownload.getInstance(mContext).onDownloadComplete(intent);
                }
            }
        });
        return true;
    }

    /**
     *
     * @param implInfo
     * @param publicDir 下载存储路径
     * @param filename  保存的文件名
     */
    public void newDownload(ImplInfo implInfo,
                               String publicDir,
                               String filename,
                               boolean autoLauncher,
                               ImplListener appCallback){
        if (null == implInfo){
            return;
        }
        mWeakCallbackMap.put(appCallback,implInfo);
        implInfo.setMimeType(MimeTypeUtils.getMimeType(filename));
        implInfo.setAutoLaunch(autoLauncher);
        ImplDownload.getInstance(mContext).addDownload(implInfo,publicDir,filename,mImplCallback);
    }

    public void pauseDownload(ImplInfo implInfo){
        if (null == implInfo){
            return;
        }
        ImplDownload.getInstance(mContext).pause(implInfo);
    }

    public void resumeDownload(ImplInfo implInfo,ImplListener appCallback){
        if (null == implInfo){
            return;
        }
        mWeakCallbackMap.put(appCallback,implInfo);
        ImplDownload.getInstance(mContext).resume(implInfo, mImplCallback);
    }

    public void remove(ImplInfo implInfo){
        if (null == implInfo){
            return;
        }
        try {
            db.delete(implInfo);
        } catch (DbException e) {
            e.printStackTrace();
        }
        mImplList.remove(implInfo);
        ImplDownload.getInstance(mContext).remove(implInfo);
    }

    public void install(ImplInfo implInfo,boolean silent,ImplListener appCallback){
        mWeakCallbackMap.put(appCallback,implInfo);
        ImplPackageManager.getInstance(mContext).install(implInfo,silent,mImplCallback);
    }

    public void uninstall(ImplInfo implInfo,boolean silent,ImplListener appCallback){
        mWeakCallbackMap.put(appCallback,implInfo);
        ImplPackageManager.getInstance(mContext).uninstall(implInfo, silent, mImplCallback);
    }

    public int getProgress(ImplInfo implInfo){
        return ImplDownload.getInstance(mContext).getProgress(implInfo);
    }

    public void setImplCallback(ImplListener appCallback,ImplInfo implInfo){
        mWeakCallbackMap.put(appCallback,implInfo);
    }

    public int getAction(ImplInfo implInfo) {
        int action = ImplInfo.ACTION_DOWNLOAD;
        switch (implInfo.getStatus()) {
            case Constant.STATUS_INIT:
            case Constant.STATUS_PENDING:
            case Constant.STATUS_RUNNING:
            case Constant.STATUS_FAILED:
                action = ImplInfo.ACTION_DOWNLOAD;
                break;

            case Constant.STATUS_SUCCESSFUL:
                String localPath = implInfo.getLocalPath();
                String mimeType = implInfo.getMimeType();
                action = ImplInfo.ACTION_OPEN;
                //下载的是apk
                if ("application/vnd.android.package-archive".equals(mimeType)) {
                    PackageInfo archivePkg = mContext.getPackageManager()
                            .getPackageArchiveInfo(localPath, PackageManager.GET_ACTIVITIES);
                    if (null != archivePkg){
                        action = ImplInfo.ACTION_OPEN;
                    }else{//下载apk解析错误
                        action = ImplInfo.ACTION_DOWNLOAD;
                    }
                }
                break;

            case Constant.STATUS_INSTALLED:
                try {
                    PackageInfo installPkg = mContext.getPackageManager().getPackageInfo(implInfo.getPackageName(), PackageManager.GET_ACTIVITIES);
                    action = ImplInfo.ACTION_OPEN;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    action = ImplInfo.ACTION_DOWNLOAD;
                }
                break;

            case Constant.STATUS_UPGRADE:
                action = ImplInfo.ACTION_DOWNLOAD;
                break;

            case Constant.STATUS_PRIVATE_INSTALLING:
            case Constant.STATUS_NORMAL_INSTALLING:
                action = ImplInfo.ACTION_INSTALL;
                break;

            case Constant.STATUS_PACKAGE_INVALID:
            case Constant.STATUS_INSTALL_FAILED:
                action = ImplInfo.ACTION_DOWNLOAD;
                break;
        }
        return action;
    }

    public String getActionText(ImplInfo implInfo) {
        Resources mResources = mContext.getResources();
        String actionText = "" ;
        switch (implInfo.getStatus()) {
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
                actionText = mResources.getString(R.string.action_pause);
                break;

            case Constant.STATUS_FAILED:
                actionText = mResources.getString(R.string.action_retry);
                break;

            case Constant.STATUS_SUCCESSFUL:
                String localPath = implInfo.getLocalPath();
                String mimeType = implInfo.getMimeType();
                actionText = mResources.getString(R.string.action_open);
                //下载的是apk
                if ("application/vnd.android.package-archive".equals(mimeType)) {
                    PackageInfo archivePkg = mContext.getPackageManager()
                            .getPackageArchiveInfo(localPath, PackageManager.GET_ACTIVITIES);
                    if (null != archivePkg){
                        Intent intent = getLaunchDownloadIntent(mContext,archivePkg.packageName);
                        if (null == intent){
                            actionText = mResources.getString(R.string.action_open);
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
                    PackageInfo installPkg = mContext.getPackageManager().getPackageInfo(implInfo.getPackageName(), PackageManager.GET_ACTIVITIES);
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

    public String getStatusText(ImplInfo implInfo) {
        Resources mResources = mContext.getResources();
        String statusText = "" ;
        switch (implInfo.getStatus()) {
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
                statusText = mResources.getString(R.string.download_status_paused);
                break;

            case Constant.STATUS_FAILED:
                statusText = mResources.getString(R.string.download_status_error);
                break;

            case Constant.STATUS_SUCCESSFUL:
                String localPath = implInfo.getLocalPath();
                String mimeType = implInfo.getMimeType();
                statusText = mResources.getString(R.string.download_status_success);
                //下载的是apk
                if ("application/vnd.android.package-archive".equals(mimeType)) {
                    PackageInfo archivePkg = mContext.getPackageManager()
                            .getPackageArchiveInfo(localPath, PackageManager.GET_ACTIVITIES);
                    if (null == archivePkg){
                        statusText = mResources.getString(R.string.download_status_invalid_package);
                    }
                }
                break;

            case Constant.STATUS_INSTALLED:
                try {
                    mContext.getPackageManager().getPackageInfo(implInfo.getPackageName(), PackageManager.GET_ACTIVITIES);
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

    public String getDescText(ImplInfo implInfo) {
        Resources mResources = mContext.getResources();
        String descText = "" ;
        ImplDownload implDownload = ImplDownload.getInstance(mContext);
        switch (implInfo.getStatus()) {
            case Constant.STATUS_INIT:
            case Constant.STATUS_PENDING:
            case Constant.STATUS_RUNNING:
            case Constant.STATUS_PAUSED:
            case Constant.STATUS_FAILED:
                descText = getSizeText(mContext,implDownload.getCurrentBytes(implInfo),implDownload.getTotalBytes(implInfo));
                break;

            case Constant.STATUS_SUCCESSFUL:
                String localPath = implInfo.getLocalPath();
                String mimeType = implInfo.getMimeType();
                descText = Formatter.formatFileSize(mContext, implDownload.getTotalBytes(implInfo));
                //下载的是apk
                if ("application/vnd.android.package-archive".equals(mimeType)) {
                    PackageInfo archivePkg = mContext.getPackageManager()
                            .getPackageArchiveInfo(localPath, PackageManager.GET_ACTIVITIES);
                    if (null != archivePkg){
                        descText = (String.format(mResources.getString(R.string.apk_version), archivePkg.versionName));
                    }
                }
                break;

            case Constant.STATUS_INSTALLED:
                try {
                    PackageInfo installPkg = mContext.getPackageManager().getPackageInfo(implInfo.getPackageName(), PackageManager.GET_ACTIVITIES);
                    descText = (String.format(mResources.getString(R.string.apk_version), installPkg.versionName));
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    descText = Formatter.formatFileSize(mContext, implDownload.getTotalBytes(implInfo));
                }
                break;

            case Constant.STATUS_UPGRADE:
                descText = mResources.getString(R.string.install_status_upgrade);
                break;

            case Constant.STATUS_PRIVATE_INSTALLING:
            case Constant.STATUS_NORMAL_INSTALLING:
            case Constant.STATUS_PACKAGE_INVALID:
            case Constant.STATUS_INSTALL_FAILED:
                descText = getSizeText(mContext,implDownload.getCurrentBytes(implInfo),implDownload.getTotalBytes(implInfo));
                break;
        }
        return descText;
    }

    public Intent getActionIntent(ImplInfo implInfo) {
        Intent actionIntent = null;
        switch (implInfo.getStatus()) {
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
                String localPath = implInfo.getLocalPath();
                String mimeType = implInfo.getMimeType();
                //下载的是apk
                if ("application/vnd.android.package-archive".equals(mimeType)) {
                    PackageInfo archivePkg = mContext.getPackageManager()
                            .getPackageArchiveInfo(localPath, PackageManager.GET_ACTIVITIES);
                    if (null != archivePkg){
                        actionIntent = getLaunchDownloadIntent(mContext,archivePkg.packageName);
                    }else{//下载apk解析错误
                        actionIntent = null;
                    }
                }
                if(null == actionIntent){
                    actionIntent = getOpenDownloadIntent(localPath,mimeType);
                }
                break;

            case Constant.STATUS_INSTALLED:
                try {
                    PackageInfo installPkg = mContext.getPackageManager().getPackageInfo(implInfo.getPackageName(), PackageManager.GET_ACTIVITIES);
                    actionIntent = getLaunchDownloadIntent(mContext,implInfo.getPackageName());
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



    private ImplInfo findImplInfoByPackageName(String packageName){
        ImplInfo implInfo = null;
        for (int i =0;i < mImplList.size();i++){
            if (mImplList.get(i).getPackageName().equals(packageName)){
                implInfo = mImplList.get(i);
                break;
            }
        }
        return implInfo;
    }


    private class ImplAgentCallback extends ImplListener{
        @Override
        public void onStart(ImplInfo info) {
            super.onStart(info);
            try {
                db.saveOrUpdate(info);
            } catch (DbException e) {
                e.printStackTrace();
            }
            for (ImplListener callback: mWeakCallbackMap.keySet()){
                ImplInfo i = mWeakCallbackMap.get(callback);
                if (i == info){
                    callback.onStart(info);
                }
            }
        }

        @Override
        public void onCancelled(ImplInfo info) {
            super.onCancelled(info);
            try {
                db.saveOrUpdate(info);
            } catch (DbException e) {
                e.printStackTrace();
            }
            for (ImplListener callback: mWeakCallbackMap.keySet()) {
                ImplInfo i = mWeakCallbackMap.get(callback);
                if (i == info){
                    callback.onCancelled(info);
                }
            }
        }

        @Override
        public void onLoading(ImplInfo info, long total, long current, boolean isUploading) {
            super.onLoading(info, total, current, isUploading);
            try {
                db.saveOrUpdate(info);
            } catch (DbException e) {
                e.printStackTrace();
            }
            for (ImplListener callback: mWeakCallbackMap.keySet()) {
                ImplInfo i = mWeakCallbackMap.get(callback);
                if (i == info){
                    callback.onLoading(info, total, current, isUploading);
                }
            }
        }

        @Override
        public void onSuccess(ImplInfo info, File file) {
            super.onSuccess(info, file);
            try {
                db.saveOrUpdate(info);
            } catch (DbException e) {
                e.printStackTrace();
            }

            if (info.isAutoLaunch()) {
                //安装
                ImplPackageManager.getInstance(mContext).install(info, true, this);
            }

            for (ImplListener callback: mWeakCallbackMap.keySet()) {
                ImplInfo i = mWeakCallbackMap.get(callback);
                if (i == info){
                    callback.onSuccess(info, file);
                }
            }
        }

        @Override
        public void onFailure(ImplInfo info, Throwable t, String msg) {
            super.onFailure(info, t, msg);
            try {
                db.saveOrUpdate(info);
            } catch (DbException e) {
                e.printStackTrace();
            }
            for (ImplListener callback: mWeakCallbackMap.keySet()) {
                ImplInfo i = mWeakCallbackMap.get(callback);
                if (i == info){
                    callback.onFailure(info, t, msg);
                }
            }
        }

        @Override
        public void onInstallSuccess(ImplInfo info) {
            super.onInstallSuccess(info);
            try {
                db.saveOrUpdate(info);
            } catch (DbException e) {
                e.printStackTrace();
            }
            for (ImplListener callback: mWeakCallbackMap.keySet()) {
                ImplInfo i = mWeakCallbackMap.get(callback);
                if (i == info){
                    callback.onInstallSuccess(info);
                }
            }
        }

        @Override
        public void onInstalling(ImplInfo info) {
            super.onInstalling(info);
            try {
                db.saveOrUpdate(info);
            } catch (DbException e) {
                e.printStackTrace();
            }
            for (ImplListener callback: mWeakCallbackMap.keySet()) {
                ImplInfo i = mWeakCallbackMap.get(callback);
                if (i == info){
                    callback.onInstalling(info);
                }
            }
        }

        @Override
        public void onInstallFailure(ImplInfo info, int errorCode) {
            super.onInstallFailure(info, errorCode);
            try {
                db.saveOrUpdate(info);
            } catch (DbException e) {
                e.printStackTrace();
            }
            for (ImplListener callback: mWeakCallbackMap.keySet()) {
                ImplInfo i = mWeakCallbackMap.get(callback);
                if (i == info){
                    callback.onInstallFailure(info,errorCode);
                }
            }
        }

        @Override
        public void onUninstallSuccess(ImplInfo info) {
            super.onUninstallSuccess(info);
            try {
                db.saveOrUpdate(info);
            } catch (DbException e) {
                e.printStackTrace();
            }
            for (ImplListener callback: mWeakCallbackMap.keySet()) {
                ImplInfo i = mWeakCallbackMap.get(callback);
                if (i == info){
                    callback.onUninstallSuccess(info);
                }
            }
        }

        @Override
        public void onUninstalling(ImplInfo info) {
            super.onUninstalling(info);
            try {
                db.saveOrUpdate(info);
            } catch (DbException e) {
                e.printStackTrace();
            }
            for (ImplListener callback: mWeakCallbackMap.keySet()) {
                ImplInfo i = mWeakCallbackMap.get(callback);
                if (i == info){
                    callback.onUninstalling(info);
                }
            }
        }

        @Override
        public void onUninstallFailure(ImplInfo info, int errorCode) {
            super.onUninstallFailure(info, errorCode);
            try {
                db.saveOrUpdate(info);
            } catch (DbException e) {
                e.printStackTrace();
            }
            for (ImplListener callback: mWeakCallbackMap.keySet()) {
                ImplInfo i = mWeakCallbackMap.get(callback);
                if (i == info){
                    callback.onUninstallFailure(info,errorCode);
                }
            }
        }
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

    public static Intent getOpenDownloadIntent(String localPath,String mediaType) {
        Uri localUri = Uri.fromFile(new File(localPath));
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


}
