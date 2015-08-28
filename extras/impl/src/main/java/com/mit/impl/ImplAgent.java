package com.mit.impl;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
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
    private Map<String,ImplInfo> mPendingImplMap;
    private DbUtils db;
    private ImplAgentCallback mImplCallback;
    private final Map<ImplInfo, List<WeakReference<ImplChangeCallback>>> mWeakCallbackMap;
    private UpdateObserverRunnable mUpdateTask;

    private ImplAgent(Context context) {
        long current = System.currentTimeMillis();
        mContext = context;
        mDownloader = ImplDownload.getInstance(mContext);
        mInstaller = ImplPackageManager.getInstance(mContext);
        mImplCallback = new ImplAgentCallback();
        mWeakCallbackMap = Collections.synchronizedMap(new HashMap());
        mImplList = new ArrayList<ImplInfo>();
        mPendingImplMap = new HashMap<String,ImplInfo>();
        mInited = new AtomicBoolean(false);

        ImplReceiver.initNetwork(mContext);
        //重新载始下载
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                db = ImplDbHelper.getDbUtils(mContext.getApplicationContext());
                try {
                    mImplList = db.findAll(Selector.from(ImplInfo.class));
                } catch (DbException e) {
                    LogUtils.e(e.getMessage(), e);
                }
                if (null == mImplList){
                    mImplList = new ArrayList<ImplInfo>();
                }
                for (int i = 0;i< mImplList.size();i++){
                    ImplInfo implInfo = mImplList.get(i);
                    mDownloader.fillImplInfo(implInfo);
                    mInstaller.fillImplInfo(implInfo);
                }

                mDownloader.kickDownload(mImplList,mImplCallback);
                mDownloader.onNetworkChanged(mImplList,mImplCallback);
                mUpdateTask = new UpdateObserverRunnable();
                mInited.set(true);
            }
        });
        ImplLog.d(TAG,"ImplAgent Constructor take " + (System.currentTimeMillis() - current) + "ms");
    }

    private void mergePendingList(){
        if (false == mInited.get()){
            return ;
        }

        if (mPendingImplMap.size() == 0){
            return;
        }

        ImplInfo implInfo = null;
        for (int i = 0; i < mImplList.size(); i++ ){
            implInfo = mImplList.get(i);
            if (mPendingImplMap.containsKey(implInfo.getKey())){
                mPendingImplMap.remove(implInfo.getKey());
            }
        }

        if (mPendingImplMap.size() > 0) {
            Iterator<ImplInfo> it = mPendingImplMap.values().iterator();
            while(it.hasNext()){
                implInfo = it.next();
                mImplList.add(implInfo);
                ImplLog.d(TAG,"mergeImplList,"+implInfo.getTitle());
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
        if (true == mInited.get()) {
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
        }else{
            implInfo = mPendingImplMap.get(key);
            if (null == implInfo){
                implInfo = new ImplInfo();
                implInfo.setKey(key);
                mPendingImplMap.put(key,implInfo);
            }
        }
        implInfo.setPackageName(packageName).setVersionCode(versionCode);
        mDownloader.fillImplInfo(implInfo);
        mInstaller.fillImplInfo(implInfo);
        ImplLog.d(TAG,"getImplInfo,"+implInfo.getKey()+","+implInfo.getTitle()+","+implInfo.getStatus()+","+implInfo.getDownloadId());
        return implInfo;
    }

    public boolean onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "onReceive," + action);
        ImplInfo implInfo = null;
        if (IMPL_ACTION_PACKAGE_ADDED.equals(action)) {
            String packageName = intent.getData().getSchemeSpecificPart();
            implInfo = findImplInfoByPackageName(packageName);
            if (null != implInfo) {
                mInstaller.onPackageAdded(implInfo, mImplCallback);
            }
        } else if (IMPL_ACTION_PACKAGE_CHANGED.equals(action)) {
            String packageName = intent.getData().getSchemeSpecificPart();
            implInfo = findImplInfoByPackageName(packageName);
            if (null != implInfo) {
                mInstaller.onPackageChanged(implInfo, mImplCallback);
            }
        } else if (IMPL_ACTION_PACKAGE_REMOVED.equals(action)) {
            String packageName = intent.getData().getSchemeSpecificPart();
            implInfo = findImplInfoByPackageName(packageName);
            if (null != implInfo) {
                mInstaller.onPackageRemoved(implInfo, mImplCallback);
            }
        } else if (IMPL_ACTION_SYSTEM_INSTALL_RESULT.equals(action)) {
            String packageName = intent.getStringExtra("name");
            int result = intent.getIntExtra("result", 0);
            implInfo = findImplInfoByPackageName(packageName);
            if (null != implInfo) {
                mInstaller.onSystemInstallResult(implInfo, result, mImplCallback);
            }
        } else if (IMPL_ACTION_SYSTEM_DELETE_RESULT.equals(action)) {
            String packageName = intent.getStringExtra("name");
            int result = intent.getIntExtra("result", 0);
            implInfo = findImplInfoByPackageName(packageName);
            if (null != implInfo) {
                mInstaller.onSystemDeleteResult(implInfo, result, mImplCallback);
            }
        } else if (IMPL_ACTION_DOWNLOAD_COMPLETE.equals(action)) {
//                    mDownloader.onDownloadComplete(intent);
        }else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)){
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
        ImplLog.d(TAG,"newDownload,"+title+","+implInfo.getStatus());
        setImplCallback(appCallback, implInfo);
        if (null != downloadUrl) {
            implInfo.setDownloadUrl(downloadUrl);
        }
        if (null != title){
            implInfo.setTitle(title);
        }else if (null == implInfo.getTitle()){
            int index = fullname.lastIndexOf(File.separator);
            if (index >= 0) {
                implInfo.setTitle(fullname.substring(index));
            }else{
                implInfo.setTitle(fullname);
            }
        }
        if (null != iconUrl){
            implInfo.setIconUrl(iconUrl);
        }
        implInfo.setAutoLaunch(autoLauncher);
        mDownloader.addDownload(implInfo, fullname, md5, mImplCallback);
        MitMobclickAgent.onEvent(mContext, "impl_DownloadActionAdd");
    }

    public void pauseDownload(ImplInfo implInfo) {
        if (null == implInfo) {
            return;
        }
        ImplLog.d(TAG,"pauseDownload,"+implInfo.getTitle()+","+implInfo.getStatus());
        MitMobclickAgent.onEvent(mContext, "impl_DownloadActionPause");
        mDownloader.pause(implInfo,mImplCallback);
    }

    public void pauseAll() {
        ImplLog.d(TAG,"pauseAll");
        MitMobclickAgent.onEvent(mContext, "impl_DownloadActionPauseAll");
        mDownloader.pauseAll(mImplList,mImplCallback);
    }

    public void resumeDownload(ImplInfo implInfo, ImplChangeCallback appCallback) {
        if (null == implInfo) {
            return;
        }
        ImplLog.d(TAG,"resumeDownload,"+implInfo.getTitle()+","+implInfo.getStatus());
        MitMobclickAgent.onEvent(mContext, "impl_DownloadActionResume");
        setImplCallback(appCallback, implInfo);
        mDownloader.resume(implInfo, mImplCallback);
    }

    public void resumeAll() {
        ImplLog.d(TAG,"resumeAll");
        MitMobclickAgent.onEvent(mContext, "impl_DownloadActionResumeAll");
        mDownloader.resumeAll(mImplList, mImplCallback);
    }

    public void remove(ImplInfo implInfo) {
        if (null == implInfo) {
            return;
        }
        ImplLog.d(TAG, "remove," + implInfo.getTitle() + "," + implInfo.getStatus());
        MitMobclickAgent.onEvent(mContext, "impl_DownloadActionRemove");
        mImplList.remove(implInfo);
        notifyObserverUpdate("remove");

        mDownloader.remove(implInfo);
        try {
            db.delete(implInfo);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    public void install(ImplInfo implInfo, boolean silent, ImplChangeCallback appCallback) {
        MitMobclickAgent.onEvent(mContext, "impl_InstallerActionInstall");
        setImplCallback(appCallback, implInfo);
        mInstaller.install(implInfo, silent, mImplCallback);
    }

    public void uninstall(ImplInfo implInfo, boolean silent, ImplChangeCallback appCallback) {
        MitMobclickAgent.onEvent(mContext, "impl_InstallerActionUninstall");
        setImplCallback(appCallback, implInfo);
        mInstaller.uninstall(implInfo, silent, mImplCallback);
    }

    public List<ImplInfo> getDownloadInfoList(int statusFlag) {
        List<ImplInfo> list = new ArrayList<ImplInfo>();
        for (ImplInfo info : mImplList) {
            if ((info.getStatus() & statusFlag) != 0 && info.getDownloadId() > 0) {
                list.add(info);
            }
        }
        return list;
    }

    public int getImplInfoCount(int statusFlag) {
        int count = 0;
        for (ImplInfo info : mImplList) {
            if ((info.getStatus() & statusFlag) != 0 && info.getDownloadId() > 0) {
                count++;
            }
        }
        return count;
    }

    private boolean findCallback(List<WeakReference<ImplChangeCallback>> list,ImplChangeCallback callback){
        boolean ret = false;
        if (null == list || list.size() == 0){
            return ret;
        }
        for (int i = 0; i < list.size(); i++) {
            ImplChangeCallback changeCallback = list.get(i).get();
            if (changeCallback == callback){
                ret = true;
                break;
            }
        }
        return ret;
    }
    public void setImplCallback(ImplChangeCallback appCallback, ImplInfo implInfo) {
        if (null != appCallback && null != implInfo) {
            synchronized (mWeakCallbackMap) {
                List<WeakReference<ImplChangeCallback>> list = mWeakCallbackMap.get(implInfo);
                if (null == list){
                    list = new ArrayList<WeakReference<ImplChangeCallback>>();
                    mWeakCallbackMap.put(implInfo,list);
                }
                if (!findCallback(list,appCallback)) {
                    list.add(new WeakReference<ImplChangeCallback>(appCallback));
                }
            }
        }
    }

    private ImplInfo findImplInfoByPackageName(String packageName) {
        ImplInfo implInfo = null;
        for (int i = 0; i < mImplList.size(); i++) {
            if (mImplList.get(i).getPackageName().equals(packageName)) {
                implInfo = mImplList.get(i);
                break;
            }
        }
        return implInfo;
    }

    private void notifyObserverUpdate(String event){
        if (Looper.getMainLooper() == Looper.myLooper()) {
            mUpdateTask.run();
        }else{
            mMainHandler.post(mUpdateTask);
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
            callbackImpl(info);
            saveImplInfo(info);
            notifyObserverUpdate("onEnqued");
            ImplLog.d(TAG, info.getTitle() + ",onEnqued,"+info.getDownloadId());
        }

        @Override
        public void onPending(ImplInfo info) {
            super.onPending(info);
            MitMobclickAgent.onEvent(mContext, "impl_DownloadPending");
            callbackImpl(info);
            saveImplInfo(info);
            ImplLog.d(TAG, info.getTitle() + ",onPending,"+info.getDownloadId());
        }

        @Override
        public void onStart(ImplInfo info) {
            super.onStart(info);
            MitMobclickAgent.onEvent(mContext, "impl_DownloadStart");
            callbackImpl(info);
            saveImplInfo(info);
            ImplLog.d(TAG, info.getTitle() + ",onStart,"+info.getDownloadId());
        }

        @Override
        public void onCancelled(ImplInfo info) {
            super.onCancelled(info);
            MitMobclickAgent.onEvent(mContext, "impl_DownloadPaused");
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
            callbackImpl(info);
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
            callbackImpl(info);
            remove(info);       //应用卸载掉，同时删除下载记录
            ImplLog.d(TAG, info.getTitle() + ",onUninstallSuccess");
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

        private void saveImplInfo(ImplInfo implInfo){
            try {
                db.saveOrUpdate(implInfo);
            } catch (DbException e) {
                e.printStackTrace();
            }
        }

        private void callbackImpl(ImplInfo info) {
            synchronized (mWeakCallbackMap) {
                //回调
                List<WeakReference<ImplChangeCallback>> list = mWeakCallbackMap.get(info);
                if (null != list){
                    Iterator it = list.iterator();
                    while(it.hasNext()){
                        WeakReference<ImplChangeCallback> weakref = (WeakReference<ImplChangeCallback>)it.next();
                        ImplChangeCallback callback = weakref.get();
                        if (null != callback) {
                            if (Looper.myLooper() != Looper.getMainLooper()) {
                                mMainHandler.post(new CallbackRunnable(info, callback));
                            }else{
                                callback.onChange(info);
                            }
                        }else{
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
    private class CallbackRunnable implements Runnable{
        private ImplInfo implInfo ;
        private ImplChangeCallback callback;

        private CallbackRunnable(ImplInfo implInfo, ImplChangeCallback callback) {
            this.implInfo = implInfo;
            this.callback = callback;
        }

        @Override
        public void run() {
            if (null != callback ){
                callback.onChange(implInfo);
//                  ImplLog.d(TAG, "onChanged," + info.getTitle()+","+callback);
            }
        }
    }

    private class UpdateObserverRunnable implements Runnable{
        @Override
        public void run() {
            setChanged();
            notifyObservers();
        }
    }
}
