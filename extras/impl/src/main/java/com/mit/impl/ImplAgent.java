package com.mit.impl;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by hxd on 15-6-10.
 */
public class ImplAgent {
    private static String TAG = "impl_agent";
    private static final HandlerThread sWorkerThread = new HandlerThread("impl-worker");
    static {
        sWorkerThread.start();
    }
    static final Handler mWorkHandler = new Handler(sWorkerThread.getLooper());
//    static ImplDatabaseHelper databaseHelper;
    private static Set<ImplListener> mListenerSet = new HashSet<ImplListener>();
    private static List<ImplInterface> sImplList = new ArrayList<ImplInterface>();
    private static ImplListener mDefaultListener= new ImplListener() {
        @Override
        public void onDownloadComplete(boolean success, DownloadCompleteRsp rsp) {
            switch(rsp.status){
                case DownloadManager.STATUS_SUCCESSFUL:
                    String localPath = null;
                    try {
                        localPath = Uri.parse(rsp.localPath).getPath();
                    }catch(Exception e){
                    }
                    if (null != localPath) {
                        ImplAgent.requestPackageInstall(rsp.context, rsp.key, localPath, rsp.packageName, true);
                    }
                    ImplLog.d(TAG,"onDownloadComplete,STATUS_SUCCESSFUL,"+rsp.key+","+rsp.localPath);
                    break;
                case DownloadManager.STATUS_FAILED:
                    ImplLog.d(TAG,"onDownloadComplete,STATUS_FAILED,"+rsp.key+","+rsp.localPath);
                    break;
            }
        }

        @Override
        public void onDownloadUpdate(boolean success, DownloadUpdateRsp rsp) {
            ImplLog.d(TAG,"onDownloadUpdate,"+rsp.key+","+rsp.status+","+rsp.progress);
        }

        @Override
        public void onPackageAdded(boolean success, PackageAddedRsp rsp) {
            ImplLog.d(TAG,"onPackageAdded,"+rsp.key);
        }

        @Override
        public void onPackageRemoved(boolean success, PackageRemovedRsp rsp) {
            ImplLog.d(TAG,"onPackageRemoved,"+rsp.key);
        }

        @Override
        public void onPackageChanged(boolean success, PackageChangedRsp rsp) {
            ImplLog.d(TAG,"onPackageChanged,"+rsp.key);
        }

        @Override
        public void onSystemInstallResult(boolean success, SystemInstallResultRsp rsp) {
            ImplLog.d(TAG,"onSystemInstallResult,"+rsp.key+","+rsp.result);
        }

        @Override
        public void onSystemDeleteResult(boolean success, SystemDeleteResultRsp rsp) {
            ImplLog.d(TAG,"onSystemDeleteResult,"+rsp.key+","+rsp.result);
        }

        @Override
        public void onFinish(boolean success, ImplResponse rsp) {
            ImplLog.d(TAG,"onFinish,"+rsp.action);
        }
    };

    static {
        sImplList.add(ImplDownload.getInstance());
//        sImplList.add(ImplFakeDownload.getInstance());
        sImplList.add(ImplPackageManager.getInstance());
        registerImplListener(mDefaultListener);
    }


//    public static void init(Context context){
//        for (ImplInterface impl:sImplList){
//            impl.init(context);
//        }
//        registerImplListener(mDefaultListener);
//    }

    public static void registerImplListener(ImplListener listener){
        mListenerSet.add(listener);
    }

    public static void unregisterImplListener(ImplListener listener){
        mListenerSet.remove(listener);
    }

    public static boolean onReceive(Context context,Intent intent){
        boolean handled = false;
        String action = intent.getAction();
        Log.d(TAG,"onReceive,"+action);
        if (ImplInterface.IMPL_ACTION_PACKAGE_ADDED.equals(action)){
            request(new PackageAddedReq(context,intent));
        }else if (ImplInterface.IMPL_ACTION_PACKAGE_CHANGED.equals(action)){
            request(new PackageChangedReq(context,intent));
        }else if (ImplInterface.IMPL_ACTION_PACKAGE_REMOVED.equals(action)){
            request(new PackageRemovedReq(context,intent));
        }else if (ImplInterface.IMPL_ACTION_SYSTEM_INSTALL_RESULT.equals(action)){
            request(new SystemInstallResultReq(context,intent));
        }else if (ImplInterface.IMPL_ACTION_SYSTEM_DELETE_RESULT.equals(action)){
            request(new SystemDeleteResultReq(context,intent));
        }else if (ImplInterface.IMPL_ACTION_DOWNLOAD_COMPLETE.equals(action)){
            request(new DownloadCompleteReq(context,intent));
        }

        return handled;
    }

    public static void queryDownload(Context context,String... keys){
        request(new DownloadUpdateReq(context,keys));
    }

    /***
     *
     * @param key        应用的唯一标识
     * @param url        应用的apk http下载地址
     * @param publicDir  应用的apk 本地存放地址
     * @param networkFlag   DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE
     * @param roming     是否允许漫游
     * @param title      下载title
     * @param desc       详情
     * @param iconUrl    应用小图标的http下载地址
     * @param iconDir    应用小图标的本地路径
     * @param visible    下载是否可见
     */
    public static void downloadPackage(Context context,
                                String key,
                                String url,
                                String publicDir,
                                String filename,
                                int networkFlag,
                                boolean roming,
                                String title,
                                String desc,
                                boolean visible,
                                String iconUrl,
                                String iconDir,
                                String pkg){
        request(new DownloadPackageReq(context,key,url,publicDir,filename,networkFlag,roming,title,desc,visible,iconUrl,iconDir,pkg));
    }

    public static void downloadToggle(Context context,String key){
        request(new ToggleDownloadReq(context,key));
    }

    public static void requestFakeDownload(Context context,String key,String localPath){
        request(new FakeDownloadReq(context,key,localPath));
    }

    public static void requestDownloadDelete(Context context,String key){
        request(new DeleteDownloadReq(context,key));
    }

    public static void requestPackageInstall(Context context,String key,String localApkPath,String packageName,boolean silent){
        if (null != localApkPath){
            request(new InstallPackageReq(context,key,localApkPath,packageName,silent));
        }
    }

    public static void requestPackageDelete(Context context,String key,String packageName,boolean silent){
        request(new DeletePackageReq(context,key,packageName,silent));
    }

    public static Handler getWorkHandler(){
        return mWorkHandler;
    }

    private static void request(ImplRequest cmd) {
        // TODO Auto-generated method stub
        for (ImplInterface impl:sImplList){
            if (impl.request(cmd)){
                break;
            }
        }
    }

    static void notify(boolean success,ImplAgent.ImplResponse rsp){
        for (ImplListener listener:mListenerSet){
            if (rsp instanceof DownloadCompleteRsp){
                listener.onDownloadComplete(success,(DownloadCompleteRsp)rsp);
            }else if (rsp instanceof DownloadUpdateRsp){
                listener.onDownloadUpdate(success,(DownloadUpdateRsp)rsp);
            }else if (rsp instanceof PackageAddedRsp){
                listener.onPackageAdded(success,(PackageAddedRsp)rsp);
            }else if (rsp instanceof PackageRemovedRsp){
                listener.onPackageRemoved(success,(PackageRemovedRsp)rsp);
            }else if (rsp instanceof PackageChangedRsp){
                listener.onPackageChanged(success,(PackageChangedRsp)rsp);
            }else if (rsp instanceof SystemInstallResultRsp){
                listener.onSystemInstallResult(success,(SystemInstallResultRsp)rsp);
            }else if (rsp instanceof SystemDeleteResultRsp){
                listener.onSystemDeleteResult(success, (SystemDeleteResultRsp) rsp);
            }else {
                listener.onFinish(success, rsp);
            }
        }
    }

//    static ImplDatabaseHelper getDatabaseHelper(Context context){
//        if (null == databaseHelper){
//            databaseHelper = new ImplDatabaseHelper(context);
//        }
//        return databaseHelper;
//    }

    ///===================================================================
    public static abstract class ImplRequest{
        Context context;
        public String action;

        protected ImplRequest(Context context, String action) {
            this.context = context;
            this.action = action;
        }
    }
    public static abstract class ImplResponse{
        Context context;
        public String action;
        protected ImplResponse(Context context, String action) {
            this.context = context;
            this.action = action;
        }
    }
    ///===================================================================

    public static class DownloadPackageReq extends ImplRequest{
        String key;
        String url;
        String publicDir;
        String filename;
        int networkFlag;
        boolean roming;
        String title;
        String desc;
        boolean visible;
        String iconUrl;
        String iconDir;
        String packageName;

        DownloadPackageReq(Context context,String key, String url, String publicDir,String filename,
                           int networkFlag, boolean roming, String title, String desc,
                           boolean visible, String iconUrl, String iconDir, String packageName) {
            super(context,ImplInterface.IMPL_ACTION_DOWNLOAD);
            this.key = key;
            this.url = url;
            this.publicDir = publicDir;
            this.filename = filename;
            this.networkFlag = networkFlag;
            this.roming = roming;
            this.title = title;
            this.desc = desc;
            this.visible = visible;
            this.iconUrl = iconUrl;
            this.iconDir = iconDir;
            this.packageName = packageName;
        }
    }

    public static class DownloadPackageRsp extends ImplResponse{
        public String key;
        DownloadPackageRsp(Context context,String key) {
            super(context,ImplInterface.IMPL_ACTION_DOWNLOAD);
            this.key = key;
        }
    }
    ///===================================================================

    public static class DownloadCompleteReq extends ImplRequest{
        Intent intent;

        DownloadCompleteReq(Context context,Intent intent) {
            super(context,ImplInterface.IMPL_ACTION_DOWNLOAD_COMPLETE);
            this.intent = intent;
        }
    }

    public static class DownloadCompleteRsp extends ImplResponse{
        public String key;
        public String localPath;  //uri
        public String packageName;
        public int status;
        DownloadCompleteRsp(Context context,String key,int status,String localPath,String packageName) {
            super(context,ImplInterface.IMPL_ACTION_DOWNLOAD_COMPLETE);
            this.key = key;
            this.localPath = localPath;
            this.status = status;
            this.packageName = packageName;
        }
    }
    ///===================================================================

    public static class FakeDownloadReq extends  ImplRequest{
        String key;
        String localPath;
        FakeDownloadReq(Context context,String key, String localPath) {
            super(context,ImplInterface.IMPL_ACTION_FAKE_DOWNLOAD);
            this.key = key;
            this.localPath = localPath;
        }
    }

    public static class FakeDownloadRsp extends ImplResponse{
        public String key;
        FakeDownloadRsp(Context context,String key) {
            super(context,ImplInterface.IMPL_ACTION_FAKE_DOWNLOAD);
            this.key = key;
        }
    }
    ///===================================================================
    public static class DeleteDownloadReq extends ImplRequest{
        String key;
        DeleteDownloadReq(Context context,String key) {
            super(context,ImplInterface.IMPL_ACTION_DOWNLOAD_DELETE);
            this.key = key;
        }
    }

    public static class DeleteDownloadRsp extends ImplResponse{
        String key;
        boolean result;
        DeleteDownloadRsp(Context context,String key,boolean result) {
            super(context,ImplInterface.IMPL_ACTION_DOWNLOAD_DELETE);
            this.key = key;
            this.result = result;
        }
    }

    ///===================================================================
    public static class ToggleDownloadReq extends ImplRequest{
        String key;

        public ToggleDownloadReq(Context context, String key) {
            super(context, ImplInterface.IMPL_ACTION_DOWNLOAD_TOGGLE);
            this.key = key;
        }
    }

    ///===================================================================
    public static class DownloadUpdateRsp extends ImplResponse{
        public String key;
        public int status;
        public int progress;

        DownloadUpdateRsp(Context context,String key, int status, int progress) {
            super(context,ImplInterface.IMPL_ACTION_DOWNLOAD_UPDATE);
            this.key = key;
            this.status = status;
            this.progress = progress;
        }
    }
    ///===================================================================

    public static class InstallPackageReq extends ImplRequest{
        String key;
        String localPath;
        String packageName;
        boolean silent;

        InstallPackageReq(Context context,String key, String localPath, String packageName, boolean silent) {
            super(context,ImplInterface.IMPL_ACTION_INSTALL_PACKAGE);
            this.key = key;
            this.localPath = localPath;
            this.packageName = packageName;
            this.silent = silent;
        }
    }

    public static class InstallPackageRsp extends ImplResponse{
        public String key;
        public boolean silent;

        InstallPackageRsp(Context context,String key, boolean silent) {
            super(context,ImplInterface.IMPL_ACTION_INSTALL_PACKAGE);
            this.key = key;
            this.silent = silent;
        }
    }
    ///===================================================================
    public static class DeletePackageReq extends ImplRequest{
        String key;
        String packageName;
        boolean silent;

        DeletePackageReq(Context context,String key, String packageName, boolean silent) {
            super(context,ImplInterface.IMPL_ACTION_DELETE_PACKAGE);
            this.key = key;
            this.packageName = packageName;
            this.silent = silent;
        }
    }

    public static class DeletePackageRsp extends ImplResponse{
        public String key;
        public boolean silent;

        DeletePackageRsp(Context context,String key, boolean silent) {
            super(context,ImplInterface.IMPL_ACTION_DELETE_PACKAGE);
            this.key = key;
            this.silent = silent;
        }
    }
    ///===================================================================
    public static class PackageAddedReq extends ImplRequest{
//        String key;
        Intent intent;
        PackageAddedReq(Context context,Intent intent) {
            super(context,ImplInterface.IMPL_ACTION_PACKAGE_ADDED);
//            this.key = key;
            this.intent = intent;
        }
    }

    public static class PackageAddedRsp extends ImplResponse{
        public String key;
        PackageAddedRsp(Context context,String key) {
            super(context,ImplInterface.IMPL_ACTION_PACKAGE_ADDED);
            this.key = key;
        }
    }
    ///===================================================================
    public static class PackageRemovedReq extends ImplRequest{
//        String key;
        Intent intent;
        PackageRemovedReq(Context context,Intent intent) {
            super(context,ImplInterface.IMPL_ACTION_PACKAGE_REMOVED);
//            this.key = key;
            this.intent = intent;
        }
    }
    public static class PackageRemovedRsp extends ImplResponse{
        public String key;
        PackageRemovedRsp(Context context,String key) {
            super(context,ImplInterface.IMPL_ACTION_PACKAGE_REMOVED);
            this.key = key;
        }
    }
    ///===================================================================
    public static class PackageChangedReq extends ImplRequest{
//        String key;
        Intent intent;
        PackageChangedReq(Context context,Intent intent) {
            super(context,ImplInterface.IMPL_ACTION_PACKAGE_CHANGED);
//            this.key = key;
            this.intent = intent;
        }
    }

    public static class PackageChangedRsp extends ImplResponse{
        public String key;
        PackageChangedRsp(Context context,String key) {
            super(context,ImplInterface.IMPL_ACTION_PACKAGE_CHANGED);
            this.key = key;
        }
    }
    ///===================================================================
    public static class SystemInstallResultReq extends ImplRequest{
//        String key;
        Intent intent;
        SystemInstallResultReq(Context context,Intent intent) {
            super(context,ImplInterface.IMPL_ACTION_SYSTEM_INSTALL_RESULT);
//            this.key = key;
            this.intent = intent;
        }
    }

    public static class SystemInstallResultRsp extends ImplResponse{
        public String key;
        public int result;
        SystemInstallResultRsp(Context context,String key,int result) {
            super(context,ImplInterface.IMPL_ACTION_SYSTEM_INSTALL_RESULT);
            this.key = key;
            this.result = result;
        }
    }
    ///===================================================================
    public static class SystemDeleteResultReq extends ImplRequest{
        Intent intent;
        SystemDeleteResultReq(Context context,Intent intent) {
            super(context,ImplInterface.IMPL_ACTION_SYSTEM_DELETE_RESULT);
            this.intent = intent;
        }
    }

    public static class SystemDeleteResultRsp extends ImplResponse{
        public String key;
        public int result;
        SystemDeleteResultRsp(Context context,String key,int result) {
            super(context,ImplInterface.IMPL_ACTION_SYSTEM_DELETE_RESULT);
            this.key = key;
            this.result = result;
        }
    }
    ///===================================================================

    public static class DownloadUpdateReq extends ImplRequest{
        String[] keys;

        public DownloadUpdateReq(Context context, String[] keys) {
            super(context, ImplInterface.IMPL_ACTION_DOWNLOAD_UPDATE);
            this.keys = keys;
        }
    }
}
