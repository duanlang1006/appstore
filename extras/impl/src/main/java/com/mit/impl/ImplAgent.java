package com.mit.impl;

import android.content.Context;
import android.content.Intent;
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
    private static Set<ImplListener> mListenerSet = new HashSet<ImplListener>();
    private static List<ImplInterface> sImplList = new ArrayList<ImplInterface>();

    static {
        sImplList.add(ImplDownload.getInstance());
        sImplList.add(ImplPackageManager.getInstance());
    }

    public static void registerImplListener(ImplListener listener){
        Log.d(TAG,"registerImplListener,"+listener);
        mListenerSet.add(listener);
    }

    public static void unregisterImplListener(ImplListener listener){
        Log.d(TAG,"unregisterImplListener,"+listener);
        mListenerSet.remove(listener);
    }

    public static boolean onReceive(final Context context,final Intent intent){
        mWorkHandler.post(new Runnable(){
            @Override
            public void run() {
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
            }
        });
        return true;
    }

    public static void queryDownload(Context context,String... keys){
        request(new DownloadQueryReq(context,keys));
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
                                String pkg,
                                int versionCode){
        request(new DownloadPackageReq(context,key,
                url,publicDir,filename,
                networkFlag,roming,title,desc,visible,
                iconUrl,iconDir,pkg,versionCode));
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

//    public static Handler getWorkHandler(){
//        return mWorkHandler;
//    }

    private static void request(final ImplRequest cmd) {
        // TODO Auto-generated method stub
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                for (ImplInterface impl:sImplList){
                    if (impl.request(cmd)){
                        break;
                    }
                }
            }
        });
    }

    static void notify(boolean success,ImplInfo info){
//        ImplLog.d(TAG,"mListenerSet.size="+mListenerSet.size());
        for (ImplListener listener:mListenerSet){
//            ImplLog.d(TAG,"notify statusTag,listener="+listener);
            listener.onUpdate(success, info);
        }
    }

    ///===================================================================
    public static abstract class ImplRequest{
        Context context;
        public String action;

        protected ImplRequest(Context context, String action) {
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
        int versionCode;

        DownloadPackageReq(Context context,String key, String url, String publicDir,String filename,
                           int networkFlag, boolean roming, String title, String desc,
                           boolean visible, String iconUrl, String iconDir, String packageName,int versionCode) {
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
            this.versionCode = versionCode;
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

    ///===================================================================
    public static class DeleteDownloadReq extends ImplRequest{
        String key;
        DeleteDownloadReq(Context context,String key) {
            super(context,ImplInterface.IMPL_ACTION_DOWNLOAD_DELETE);
            this.key = key;
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

    ///===================================================================
    public static class SystemDeleteResultReq extends ImplRequest{
        Intent intent;
        SystemDeleteResultReq(Context context,Intent intent) {
            super(context,ImplInterface.IMPL_ACTION_SYSTEM_DELETE_RESULT);
            this.intent = intent;
        }
    }

    ///===================================================================

    public static class DownloadQueryReq extends ImplRequest{
        String[] keys;

        public DownloadQueryReq(Context context, String[] keys) {
            super(context, ImplInterface.IMPL_ACTION_QUERY);
            this.keys = keys;
        }
    }
}
