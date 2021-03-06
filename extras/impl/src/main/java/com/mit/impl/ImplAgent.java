package com.mit.impl;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.TextUtils;

import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.util.LogUtils;
import com.mit.mitupdatesdk.MitMobclickAgent;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by hxd on 15-6-10.
 */
public class ImplAgent extends Observable {
    private final static String TAG = "impl_agent";

    private final static String IMPL_ACTION_DOWNLOAD_COMPLETE = DownloadManager.ACTION_DOWNLOAD_COMPLETE;
    private final static String IMPL_ACTION_PACKAGE_ADDED = Intent.ACTION_PACKAGE_ADDED;
    private final static String IMPL_ACTION_PACKAGE_REMOVED = Intent.ACTION_PACKAGE_REMOVED;
    private final static String IMPL_ACTION_PACKAGE_CHANGED = Intent.ACTION_PACKAGE_CHANGED;
    private final static String IMPL_ACTION_SYSTEM_INSTALL_RESULT = "com.installer.system.install.result";
    private final static String IMPL_ACTION_SYSTEM_DELETE_RESULT = "com.installer.system.delete.result";

    private static final HandlerThread sWorkerThread = new HandlerThread("impl-worker");

    private final static Handler mMainHandler = new Handler();

    static {
        sWorkerThread.start();
    }

    static final Handler mWorkHandler = new Handler(sWorkerThread.getLooper());
    private static ImplAgent mInstance = null;


    private static synchronized void initInstance(Context context) {
        if (null == mInstance) {
            mInstance = new ImplAgent(context);
        }
    }

    public static ImplAgent getInstance(Context appcontext) {
        if (null == mInstance) {
            initInstance(appcontext);
        }
        return mInstance;
    }

    private Context mContext;
    private ImplDownload mDownloader;
    private ImplPackageManager mInstaller;
    private AtomicBoolean mInited;

    private List<ImplInfo> mImplList;
    private Map<String, ImplInfo> mPendingImplMap;
    private DbUtils db;
    private ImplAgentCallback mImplCallback;
    private final Map<ImplInfo, List<WeakReference<ImplChangeCallback>>> mWeakCallbackMap;
    private UpdateObserverRunnable mUpdateTask;

    private ImplAgent(final Context context) {
        long current = System.currentTimeMillis();
        mContext = context;
        mDownloader = ImplDownload.getInstance(mContext);
        mInstaller = ImplPackageManager.getInstance(mContext);
        mImplCallback = new ImplAgentCallback();
        mWeakCallbackMap = Collections.synchronizedMap(new HashMap());
        mImplList = new ArrayList<ImplInfo>();
        mPendingImplMap = new HashMap<String, ImplInfo>();
        mInited = new AtomicBoolean(false);

        ImplReceiver.initNetwork(mContext);
        //重新载始下载
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    db = ImplDbHelper.getDbUtils(mContext.getApplicationContext());
                    mImplList = db.findAll(Selector.from(ImplInfo.class));
                } catch (Exception e) {
                    LogUtils.e(e.getMessage(), e);
                }
                if (null == mImplList) {
                    mImplList = new ArrayList<ImplInfo>();
                }
                for (int i = 0; i < mImplList.size(); i++) {
                    ImplInfo implInfo = mImplList.get(i);
                    mDownloader.fillImplInfo(implInfo);
                    mInstaller.fillImplInfo(implInfo);
                    implInfo.initImplRes(mContext);
                }
                mDownloader.kickDownload(mImplList, mImplCallback);
                mDownloader.onNetworkChanged(mImplList, mImplCallback);
                mUpdateTask = new UpdateObserverRunnable();
                mInited.set(true);
            }
        });
        ImplLog.d(TAG, "ImplAgent Constructor take " + (System.currentTimeMillis() - current) + "ms");
    }

    private void mergePendingList() {
        if (false == mInited.get()) {
            return;
        }

        if (mPendingImplMap.size() == 0) {
            return;
        }

        ImplInfo implInfo = null;
        for (int i = 0; i < mImplList.size(); i++) {
            implInfo = mImplList.get(i);
            if (mPendingImplMap.containsKey(implInfo.getKey())) {
                mPendingImplMap.remove(implInfo.getKey());
            }
        }

        if (mPendingImplMap.size() > 0) {
            Iterator<ImplInfo> it = mPendingImplMap.values().iterator();
            while (it.hasNext()) {
                implInfo = it.next();
                mImplList.add(implInfo);
                ImplLog.d(TAG, "mergeImplList," + implInfo.getTitle());
            }
            notifyObserverUpdate("ImplListChanged");
        }
        mPendingImplMap.clear();
    }

    public ImplInfo getImplInfo(String key, String packageName, int versionCode) {
        if (null == key || null == packageName || TextUtils.isEmpty(key) || TextUtils.isEmpty(packageName)) {
            return null;
        }

        ImplInfo implInfo = null;
        if (mInited.get()) {
            mergePendingList();
            for (int i = 0; i < mImplList.size(); i++) {
                if (mImplList.get(i).getKey().equals(key)) {
                    implInfo = mImplList.get(i);
                    break;
                }
            }
            if (null == implInfo) {
                implInfo = new ImplInfo();
                implInfo.setKey(key);
                mImplList.add(implInfo);
            }
        } else {
            implInfo = mPendingImplMap.get(key);
            if (null == implInfo) {
                implInfo = new ImplInfo();
                implInfo.setKey(key);
                mPendingImplMap.put(key, implInfo);
            }
        }
        implInfo.setPackageName(packageName).setVersionCode(versionCode);
        mDownloader.fillImplInfo(implInfo);
        mInstaller.fillImplInfo(implInfo);
        implInfo.initImplRes(mContext);
        ImplLog.d(TAG, "getImplInfo," + implInfo.getKey() + "," + implInfo.getTitle() + "," + implInfo.getStatus());
        return implInfo;
    }

    public boolean onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        ImplLog.d(TAG, "onReceive," + action);
        if (IMPL_ACTION_PACKAGE_ADDED.equals(action)) {
            final String packageName = intent.getData().getSchemeSpecificPart();
            mWorkHandler.post(new Runnable() {
                @Override
                public void run() {
                    ImplInfo implInfo = findImplInfoByPackageName(packageName);
                    if (null != implInfo) {
                        mInstaller.onPackageAdded(implInfo, mImplCallback);
                    }
                }
            });
        } else if (IMPL_ACTION_PACKAGE_CHANGED.equals(action)) {
            final String packageName = intent.getData().getSchemeSpecificPart();
            mWorkHandler.post(new Runnable() {
                @Override
                public void run() {
                    ImplInfo implInfo = findImplInfoByPackageName(packageName);
                    if (null != implInfo) {
                        mInstaller.onPackageChanged(implInfo, mImplCallback);
                    }
                }
            });
        } else if (IMPL_ACTION_PACKAGE_REMOVED.equals(action)) {
            final String packageName = intent.getData().getSchemeSpecificPart();
            mWorkHandler.post(new Runnable() {
                @Override
                public void run() {
                    ImplInfo implInfo = findImplInfoByPackageName(packageName);
                    if (null != implInfo) {
                        mInstaller.onPackageRemoved(implInfo, mImplCallback);
                    }
                }
            });
        } else if (IMPL_ACTION_SYSTEM_INSTALL_RESULT.equals(action)) {
            final String packageName = intent.getStringExtra("name");
            final String fileName = intent.getStringExtra("filename");
            final int result = intent.getIntExtra("result", 0);
            mWorkHandler.post(new Runnable() {
                @Override
                public void run() {
                    ImplInfo implInfo = findImplInfoByPackageName(packageName);
                    if (null == implInfo) {
                        implInfo = findImplInfoByFilename(fileName);
                    }
                    if (null != implInfo) {
                        mInstaller.onSystemInstallResult(implInfo, result, mImplCallback);
                        ImplLog.d(TAG, "com.installer.system.install.result,result," + result + ",packagename:" + packageName + ",filename=" + fileName);
                    } else {
                        ImplLog.d(TAG, "com.installer.system.install.result,implInfo == null");
                        for (int i = 0; i < mImplList.size(); i++) {
                            implInfo = mImplList.get(i);
                            ImplLog.d(TAG, "implInfo.getTitle() = " + implInfo.getTitle());
                            ImplLog.d(TAG, "implInfo.getStatus() = " + implInfo.getStatus());
                            if (ImplInfo.STATUS_PRIVATE_INSTALLING != implInfo.getStatus()) {
                                continue;
                            }
                            String path = implInfo.getLocalPath();
                            if (null == path || TextUtils.isEmpty(path)) {
                                path = implInfo.getFileSavePath();
                            }
                            ImplLog.d(TAG, "path = " + path);
                            ImplLog.d(TAG, "!new File(path).exists() = " + !new File(path).exists());
                            ImplLog.d(TAG, "mContext.getPackageManager().getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES) = " + mContext.getPackageManager().getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES));


                            if (null == path || TextUtils.isEmpty(path) || !new File(path).exists()
                                    || null == mContext.getPackageManager().getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES)) {
                                mInstaller.onSystemInstallResult(implInfo, -10000, mImplCallback);
                            }
                        }
                    }
                }
            });
        } else if (IMPL_ACTION_SYSTEM_DELETE_RESULT.equals(action)) {
            final String packageName = intent.getStringExtra("name");
            final int result = intent.getIntExtra("result", 0);
            mWorkHandler.post(new Runnable() {
                @Override
                public void run() {
                    ImplInfo implInfo = findImplInfoByPackageName(packageName);
                    if (null != implInfo) {
                        mInstaller.onSystemDeleteResult(implInfo, result, mImplCallback);
                    }
                }
            });
        } else if (IMPL_ACTION_DOWNLOAD_COMPLETE.equals(action)) {
//                    mDownloader.onDownloadComplete(intent);
        } else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            mWorkHandler.post(new Runnable() {
                @Override
                public void run() {
                    mDownloader.onNetworkChanged(mImplList, mImplCallback);
                }
            });
        }
        return true;
    }

    public void newDownload(ImplInfo implInfo,
                            String downloadUrl,
                            String title,
                            String iconUrl,
                            String fullname,
                            String md5,
                            boolean autoLauncher,
                            ImplChangeCallback appCallback) {
        if (null == implInfo) {
            return;
        }
        ImplLog.d(TAG, "newDownload," + title + "," + implInfo.getStatus());
        bindImplCallback(appCallback, implInfo);
        implInfo.setDownloadUrl(downloadUrl);
        implInfo.setFileSavePath(fullname);
        if (null != title) {
            implInfo.setTitle(title);
        } else if (null == implInfo.getTitle()) {
            int index = fullname.lastIndexOf(File.separator);
            if (index >= 0) {
                implInfo.setTitle(fullname.substring(index));
            } else {
                implInfo.setTitle(fullname);
            }
        }
        implInfo.setIconUrl(iconUrl);
        implInfo.setMd5(md5);
        implInfo.setAutoLaunch(autoLauncher);
        mDownloader.addDownload(implInfo, fullname, md5, mImplCallback);
        saveImplInfo(implInfo);
        MitMobclickAgent.onEvent(mContext, "impl_DownloadActionAdd");
    }

    public void pauseDownload(ImplInfo implInfo) {
        if (null == implInfo) {
            return;
        }
        ImplLog.d(TAG, "pauseDownload," + implInfo.getTitle() + "," + implInfo.getStatus());
        MitMobclickAgent.onEvent(mContext, "impl_DownloadActionPause");
        mDownloader.pause(implInfo, mImplCallback);
//        mImplCallback.onCancelled(implInfo);
    }

    public void pauseAll() {
        ImplLog.d(TAG, "pauseAll");
        MitMobclickAgent.onEvent(mContext, "impl_DownloadActionPauseAll");
        mDownloader.pauseAll(mImplList, mImplCallback);
    }

    public void resumeDownload(ImplInfo implInfo, ImplChangeCallback appCallback) {
        if (null == implInfo) {
            return;
        }
        ImplLog.d(TAG, "resumeDownload," + implInfo.getTitle() + "," + implInfo.getStatus());
        MitMobclickAgent.onEvent(mContext, "impl_DownloadActionResume");
        bindImplCallback(appCallback, implInfo);
        mDownloader.resume(implInfo, mImplCallback);
//        mImplCallback.onStart(implInfo);
    }

    public void resumeAll() {
        ImplLog.d(TAG, "resumeAll");
        MitMobclickAgent.onEvent(mContext, "impl_DownloadActionResumeAll");
        mDownloader.resumeAll(mImplList, mImplCallback);
    }

    //    public void remove(Long... ids){
    public void remove(List<Long> ids, boolean flagDeleteFile) {
        if (null == ids || ids.isEmpty()) {
            return;
        }
        File deleteFile;
        for (Long id : ids) {
            if (id < 1) {
                continue;
            }
            ImplInfo implInfo = findImplInfoById(id);
            if (null == implInfo) {
                continue;
            }
            ImplLog.d(TAG, "remove," + implInfo.getTitle() + "," + implInfo.getStatus());
            MitMobclickAgent.onEvent(mContext, "impl_DownloadActionRemove");
            mImplList.remove(implInfo);
            mDownloader.remove(implInfo);
            if (flagDeleteFile) {
                deleteFile = new File(implInfo.getFileSavePath());
                if (deleteFile.exists() && deleteFile.isFile()) {
                    deleteFile.delete();
                }
            }
            try {
                db.delete(implInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        notifyObserverUpdate("remove");
    }

//    public void remove(ImplInfo... implInfos) {
//        if (null == implInfos || implInfos.length < 1) {
//            return;
//        }
//        File deleteFile;
//        for (ImplInfo implInfo : implInfos) {
//            ImplLog.d(TAG, "remove," + implInfo.getTitle() + "," + implInfo.getStatus());
//            MitMobclickAgent.onEvent(mContext, "impl_DownloadActionRemove");
//            mImplList.remove(implInfo);
//            mDownloader.remove(implInfo);
//            if (true) {
//                deleteFile = new File(implInfo.getFileSavePath());
//                if (deleteFile.exists() && deleteFile.isFile()) {
//                    deleteFile.delete();
//                }
//            }
//            try {
//                db.delete(implInfo);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        notifyObserverUpdate("remove");
//    }

    public void install(ImplInfo implInfo, boolean silent, ImplChangeCallback appCallback) {
        MitMobclickAgent.onEvent(mContext, "impl_InstallerActionInstall");
        bindImplCallback(appCallback, implInfo);
        mInstaller.install(implInfo, silent, mImplCallback);
    }

    public void uninstall(ImplInfo implInfo, boolean silent, ImplChangeCallback appCallback) {
        MitMobclickAgent.onEvent(mContext, "impl_InstallerActionUninstall");
        bindImplCallback(appCallback, implInfo);
        mInstaller.uninstall(implInfo, silent, mImplCallback);
    }

    public List<ImplInfo> getDownloadInfoList(int statusFlag) {
        List<ImplInfo> list = new ArrayList<ImplInfo>();
        for (ImplInfo info : mImplList) {
            if ((info.getStatus() & statusFlag) != 0 && info.getId() > 0) {
                list.add(info);
            }
        }
        return list;
    }

    public int getImplInfoCount(int statusFlag) {
        int count = 0;
        for (ImplInfo info : mImplList) {
            if ((info.getStatus() & statusFlag) != 0 && info.getId() > 0) {
                count++;
            }
        }
        return count;
    }

    private boolean findCallback(List<WeakReference<ImplChangeCallback>> list, ImplChangeCallback callback) {
        boolean ret = false;
        if (null == list || list.size() == 0) {
            return ret;
        }
        for (int i = 0; i < list.size(); i++) {
            ImplChangeCallback changeCallback = list.get(i).get();
            if (changeCallback == callback) {
                ret = true;
                break;
            }
        }
        return ret;
    }

    public void bindImplCallback(ImplChangeCallback appCallback, ImplInfo implInfo) {
        if (null != appCallback && null != implInfo) {
            synchronized (mWeakCallbackMap) {
                List<WeakReference<ImplChangeCallback>> list = mWeakCallbackMap.get(implInfo);
                if (null == list) {
                    list = new ArrayList<WeakReference<ImplChangeCallback>>();
                    mWeakCallbackMap.put(implInfo, list);
                }
                if (!findCallback(list, appCallback)) {
                    list.add(new WeakReference<ImplChangeCallback>(appCallback));
                }
            }
        }
    }

    public void configDeleteAfterInstalled(boolean delete) {
        ImplConfig.setDeleteAfterInstalled(mContext, delete);
    }

    public void configMaxOverSize(long size) {
        ImplConfig.setMaxOverSize(mContext, size);
    }

    private ImplInfo findImplInfoById(long id) {
        ImplInfo implInfo = null;
        for (int i = 0; i < mImplList.size(); i++) {
            if (mImplList.get(i).getId() == id) {
                implInfo = mImplList.get(i);
                break;
            }
        }
        return implInfo;
    }

    private ImplInfo findImplInfoByPackageName(String packageName) {
        ImplInfo implInfo = null;
        if (null == packageName) {
            return implInfo;
        }
        for (int i = 0; i < mImplList.size(); i++) {
            implInfo = mImplList.get(i);
            if (packageName.equals(implInfo.getPackageName())) {
                return implInfo;
            }
        }
        return null;
    }

    private ImplInfo findImplInfoByFilename(String fileName) {
        ImplInfo implInfo = null;
        if (null == fileName) {
            return implInfo;
        }
        for (int i = 0; i < mImplList.size(); i++) {
            implInfo = mImplList.get(i);
            if (fileName.equals(implInfo.getLocalPath())
                    || fileName.equals(implInfo.getFileSavePath())) {
                return implInfo;
            }
        }
        return null;
    }

    private void notifyObserverUpdate(String event) {
//        if (Looper.getMainLooper() == Looper.myLooper()) {
//            mUpdateTask.run();
//        } else {
        mMainHandler.removeCallbacks(mUpdateTask);
        mMainHandler.postDelayed(mUpdateTask, 10);
//        }
    }

    private void saveImplInfo(ImplInfo implInfo) {
        try {
            db.saveOrUpdate(implInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class ImplAgentCallback extends ImplListener {
        private ImplAgentCallback() {
            super();
        }

        @Override
        public void onEnqued(ImplInfo info) {
            super.onEnqued(info);
            MitMobclickAgent.onEvent(mContext, "impl_DownloadEnqued");
            for (int i = 0; i < mPackageListener.size(); i++) {
                mPackageListener.get(i).onDownloadEnqued(info);
            }
            callbackImpl(info);
            saveImplInfo(info);
            notifyObserverUpdate("onEnqued");
            ImplLog.d(TAG, info.getTitle() + ",onEnqued");
        }

        @Override
        public void onPending(ImplInfo info) {
            super.onPending(info);
            MitMobclickAgent.onEvent(mContext, "impl_DownloadPending");
            callbackImpl(info);
            saveImplInfo(info);
            ImplLog.d(TAG, info.getTitle() + ",onPending");
        }

        @Override
        public void onStart(ImplInfo info) {
            super.onStart(info);
            MitMobclickAgent.onEvent(mContext, "impl_DownloadStart");
            for (int i = 0; i < mPackageListener.size(); i++) {
                mPackageListener.get(i).onDownloadResume(info);
            }
            callbackImpl(info);
            saveImplInfo(info);
            ImplLog.d(TAG, info.getTitle() + ",onStart");
        }

        @Override
        public void onCancelled(ImplInfo info) {
            super.onCancelled(info);
            MitMobclickAgent.onEvent(mContext, "impl_DownloadPaused");
            for (int i = 0; i < mPackageListener.size(); i++) {
                mPackageListener.get(i).onDownloadPaused(info);
            }
            callbackImpl(info);
            saveImplInfo(info);
            ImplLog.d(TAG, info.getTitle() + ",onCancelled");
        }

        @Override
        public void onLoading(ImplInfo info, long total, long current, boolean isUploading) {
            super.onLoading(info, total, current, isUploading);
            callbackImpl(info);
//            ImplLog.d(TAG, info.getTitle() + ",onLoading," + ((total != 0) ? current * 100 / total : 0));
        }

        @Override
        public void onSuccess(ImplInfo info, File file) {
            super.onSuccess(info, file);
            MitMobclickAgent.onEvent(mContext, "impl_DownloadSuccess");
            if (info.isAutoLaunch()) {
                //安装
                mInstaller.install(info, true, this);
            }
            for (int i = 0; i < mPackageListener.size(); i++) {
                mPackageListener.get(i).onDownloadSucess(info);
            }
            callbackImpl(info);
            notifyObserverUpdate("onSuccess");
            saveImplInfo(info);
            ImplLog.d(TAG, info.getTitle() + ",onSuccess");
        }

        @Override
        public void onFailure(ImplInfo info, Throwable t, String msg) {
            super.onFailure(info, t, msg);
            MitMobclickAgent.onEvent(mContext, "impl_DownloadFailure");
            callbackImpl(info);
            saveImplInfo(info);
            ImplLog.d(TAG, info.getTitle() + ",onFailure," + msg);
        }

        @Override
        public void onInstallSuccess(ImplInfo info) {
            super.onInstallSuccess(info);
            MitMobclickAgent.onEvent(mContext, "impl_InstallSuccess");
            for (int i = 0; i < mPackageListener.size(); i++) {
                mPackageListener.get(i).onPackageAdded(info);
            }
            callbackImpl(info);
            notifyObserverUpdate("onInstallSuccess");
            saveImplInfo(info);
            ImplLog.d(TAG, info.getTitle() + ",onInstallSuccess");
        }

        @Override
        public void onInstalling(ImplInfo info) {
            super.onInstalling(info);
            MitMobclickAgent.onEvent(mContext, "impl_Installing");
            callbackImpl(info);
            saveImplInfo(info);
            ImplLog.d(TAG, info.getTitle() + ",onInstalling");
        }

        @Override
        public void onInstallFailure(ImplInfo info, int errorCode) {
            super.onInstallFailure(info, errorCode);
            MitMobclickAgent.onEvent(mContext, "impl_InstallFailuer");
            callbackImpl(info);
            saveImplInfo(info);
            ImplLog.d(TAG, info.getTitle() + ",onInstallFailure,errorCode=" + errorCode);
        }

        @Override
        public void onUninstallSuccess(ImplInfo info) {
            super.onUninstallSuccess(info);
            MitMobclickAgent.onEvent(mContext, "impl_UninstallSuccess");
            for (int i = 0; i < mPackageListener.size(); i++) {
                mPackageListener.get(i).onPackageRemoved(info);
            }
            callbackImpl(info);
            saveImplInfo(info);
            ImplLog.d(TAG, info.getTitle() + ",onUninstallSuccess");
            notifyObserverUpdate("uninstalled");
        }

        @Override
        public void onUninstalling(ImplInfo info) {
            super.onUninstalling(info);
            MitMobclickAgent.onEvent(mContext, "impl_Uninstalling");
            callbackImpl(info);
            saveImplInfo(info);
            ImplLog.d(TAG, info.getTitle() + ",onUninstalling");
        }

        @Override
        public void onUninstallFailure(ImplInfo info, int errorCode) {
            super.onUninstallFailure(info, errorCode);
            MitMobclickAgent.onEvent(mContext, "impl_UninstallFailure");
            callbackImpl(info);
            saveImplInfo(info);
            ImplLog.d(TAG, info.getTitle() + ",onUninstalling");
        }

        private void callbackImpl(ImplInfo info) {
            synchronized (mWeakCallbackMap) {
                //回调
                info.updateImplRes(mContext);
                List<WeakReference<ImplChangeCallback>> list = mWeakCallbackMap.get(info);
                if (null != list) {
                    Iterator it = list.iterator();
                    while (it.hasNext()) {
                        WeakReference<ImplChangeCallback> weakref = (WeakReference<ImplChangeCallback>) it.next();
                        ImplChangeCallback callback = weakref.get();
                        if (null != callback) {
                            if (Looper.myLooper() != Looper.getMainLooper()) {
                                mMainHandler.post(new CallbackRunnable(info, callback));
                            } else {
                                callback.onChange(info);
                            }
                        } else {
                            it.remove();
                            list.remove(weakref);
                        }
                    }
                }

                //清理
                Set keyset = mWeakCallbackMap.keySet();
                if (null == keyset || keyset.size() == 0) {
                    return;
                }
                Iterator it = keyset.iterator();
                while (it.hasNext()) {
                    ImplInfo implInfo = (ImplInfo) it.next();
                    list = mWeakCallbackMap.get(implInfo);
                    if (null == list || list.size() == 0) {
//                        ImplLog.d(TAG, implInfo.getTitle() + ",callback is null,will be deleted");
                        it.remove();
                        mWeakCallbackMap.remove(implInfo);
                    }
                }
//                ImplLog.d(TAG, "mWeakCallbackMap.size()=" + mWeakCallbackMap.size());
            }
        }
    }

    private class CallbackRunnable implements Runnable {
        private ImplInfo implInfo;
        private ImplChangeCallback callback;

        private CallbackRunnable(ImplInfo implInfo, ImplChangeCallback callback) {
            this.implInfo = implInfo;
            this.callback = callback;
        }

        @Override
        public void run() {
            if (null != callback) {
                callback.onChange(implInfo);
//                  ImplLog.d(TAG, "onChanged," + info.getTitle()+","+callback);
            }
        }
    }

    private class UpdateObserverRunnable implements Runnable {
        @Override
        public void run() {
            setChanged();
            notifyObservers();
        }
    }


    private final static List<SimplePackageListener> mPackageListener = new ArrayList<>();

    public void registerPackageListener(SimplePackageListener listener) {
        synchronized (mPackageListener) {
            if (!mPackageListener.contains(listener)) {
                mPackageListener.add(listener);
            }
        }
    }

    public void unregisterPackageListener(SimplePackageListener listener) {
        synchronized (mPackageListener) {
            if (mPackageListener.contains(listener)) {
                mPackageListener.remove(listener);
            }
        }
    }

    public static class SimplePackageListener {
        public void onDownloadEnqued(ImplInfo implInfo) {
        }

        public void onDownloadSucess(ImplInfo implInfo) {
        }

        public void onPackageAdded(ImplInfo implInfo) {
        }

        public void onPackageRemoved(ImplInfo implInfo) {
        }

        public void onPackageChanged(ImplInfo implInfo) {
        }

        public void onSystemInstallResult(ImplInfo implInfo, int result) {
        }

        public void onSystemRemoveResult(ImplInfo implInfo, int result) {
        }

        public void onDownloadPaused(ImplInfo implInfo) {
        }

        public void onDownloadResume(ImplInfo implInfo) {

        }
    }
}
