package com.mit.impl;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.util.SparseArray;

import com.android.dsc.downloads.DownloadManager;
import com.applite.common.Constant;
import com.applite.common.LogUtils;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;

public class ImplPackageManager extends AbstractImpl {
    private static final String TAG = "impl_package";
    private SparseArray<Method> mCmdList = new SparseArray<Method>();
    private PackageManager pm ;

    private static ImplPackageManager mInstance = null;
    private static synchronized void initInstance(){
        if (null == mInstance ){
            mInstance = new ImplPackageManager();
        }
    }
    
    public static ImplPackageManager getInstance(){
        if (null == mInstance){
            initInstance();
        }
        return mInstance;
    }

    private ImplPackageManager() {
        try {
            Class<?> cls = this.getClass();
            mCmdList.append(IMPL_ACTION_QUERY.hashCode(),
                    cls.getDeclaredMethod("handleQueryReq",ImplAgent.ImplRequest.class));
            mCmdList.append(IMPL_ACTION_INSTALL_PACKAGE.hashCode(),
                    cls.getDeclaredMethod("handlePackageInstallReq", ImplAgent.ImplRequest.class));
            mCmdList.append(IMPL_ACTION_DELETE_PACKAGE.hashCode(),
                    cls.getDeclaredMethod("handlePackageDeleteReq", ImplAgent.ImplRequest.class));
            mCmdList.append(IMPL_ACTION_PACKAGE_ADDED.hashCode(),
                    cls.getDeclaredMethod("handlePackageAdded", ImplAgent.ImplRequest.class));
            mCmdList.append(IMPL_ACTION_PACKAGE_CHANGED.hashCode(),
                    cls.getDeclaredMethod("handlePackageChanged", ImplAgent.ImplRequest.class));
            mCmdList.append(IMPL_ACTION_PACKAGE_REMOVED.hashCode(),
                    cls.getDeclaredMethod("handlePackageRemoved", ImplAgent.ImplRequest.class));
            mCmdList.append(IMPL_ACTION_SYSTEM_INSTALL_RESULT.hashCode(),
                    cls.getDeclaredMethod("handleSystemInstallResult", ImplAgent.ImplRequest.class));
            mCmdList.append(IMPL_ACTION_SYSTEM_DELETE_RESULT.hashCode(),
                    cls.getDeclaredMethod("handleSystemDeleteResult", ImplAgent.ImplRequest.class));
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean request(ImplAgent.ImplRequest cmd) {
        super.request(cmd);
        if (null == pm){
            pm = cmd.context.getPackageManager();
        }

        final Method method = mCmdList.get(cmd.action.hashCode());
        if (null == method){
            return false;
        }
        boolean result = true;
        try {
            result = (boolean)method.invoke(this,cmd);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void cancel(ImplAgent.ImplRequest cmd) {
        // TODO Auto-generated method stub

    }

    private boolean handleQueryReq(ImplAgent.ImplRequest cmd){
        ImplAgent.DownloadQueryReq implCmd = (ImplAgent.DownloadQueryReq)cmd;
        ImplLog.d(TAG,"handleQueryReq,"+implCmd.keys);
        List<ImplInfo> infoList = findInfoByKeyBatch(implCmd.keys);
        for (ImplInfo info:infoList){
            if (info.getStatus() >= Constant.STATUS_PACKAGE_INVALID) {
                ImplAgent.notify(true,info);
            }
        }
        return true;
    }

    public boolean handlePackageInstallReq(ImplAgent.ImplRequest cmd) {
        // TODO Auto-generated method stub
        boolean success = true;
        ImplAgent.InstallPackageReq implCmd = (ImplAgent.InstallPackageReq)cmd;
        ImplInfo implInfo = findInfoByPackageName(implCmd.packageName);
        if (null == implInfo){
            implInfo = ImplInfo.create(implCmd.context,implCmd.key,"",implCmd.packageName,0);
            implInfo.setLocalPath(implCmd.localPath);
        }

        ImplLog.d(TAG,"handlePackageInstallReq,"+implInfo.getKey()+","+implInfo.getTitle());
        PackageInfo info = pm.getPackageArchiveInfo(implCmd.localPath, PackageManager.GET_ACTIVITIES);
        if (null != info && info.packageName.equals(implCmd.packageName)){
            try {
                pm.getPackageInfo("com.android.installer", 0);
                installImpl(implCmd.context, implInfo, implCmd.localPath, implCmd.silent);
            } catch (NameNotFoundException e) {
                try{
                    pm.getPackageInfo("com.android.dbservices", 0);
                    installImpl(implCmd.context,implInfo,implCmd.localPath,implCmd.silent);
                }catch(Exception e1){
                    e1.printStackTrace();
                    installImpl(implCmd.context, implInfo, implCmd.localPath, false);
                }
            }
        }else{
            //希望安装的apk和下载的apk包名不一致,或者下载的apk不合法
            implInfo.setStatus(Constant.STATUS_PACKAGE_INVALID);
        }
        save(implInfo);
        ImplAgent.notify(success,implInfo);
        return true;
    }
        
    private void installImpl(final Context context,final ImplInfo implInfo, final String filename, boolean silent) {
        if(silent){
            Intent intent = new Intent();
            intent.setAction("com.installer.system");
            intent.putExtra("name", filename);
            intent.putExtra("nameTag", "APK_PATH_NAME.tag");
            context.sendBroadcast(intent);
            implInfo.setStatus(Constant.STATUS_PRIVATE_INSTALLING);
        }else {
            Uri path = Uri.parse(filename);
            if (path.getScheme() == null) {
                path = Uri.fromFile(new File(filename));
            }
            Intent activityIntent = new Intent(Intent.ACTION_VIEW);
            activityIntent.setDataAndType(path, "application/vnd.android.package-archive");
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                context.startActivity(activityIntent);
            } catch (ActivityNotFoundException ex) {

            }
            implInfo.setStatus(Constant.STATUS_NORMAL_INSTALLING);
        }
    }
    
    boolean handlePackageDeleteReq(ImplAgent.ImplRequest cmd) {
        // TODO Auto-generated method stub
        ImplAgent.DeletePackageReq implCmd = (ImplAgent.DeletePackageReq)cmd;
        ImplInfo implInfo = findInfoByPackageName(implCmd.packageName);
        if (null == implInfo){
            implInfo = ImplInfo.create(implCmd.context,implCmd.key,"",implCmd.packageName,0);
        }
        ImplLog.d(TAG,"handlePackageDeleteReq,"+implInfo.getKey()+","+implInfo.getTitle());
        try{
            pm.getPackageInfo("com.android.dbservices", 0);
            Intent intent = new Intent("com.installer.action.delete");
            intent.putExtra("name", implCmd.packageName);
            intent.putExtra("nameTag", "APK_PATH_NAME.tag");
            intent.putExtra("silent", implCmd.silent);
            cmd.context.sendBroadcast(intent);
        }catch(Exception e1){
            e1.printStackTrace();
            Intent intent = new Intent(Intent.ACTION_DELETE, Uri.parse("package:" + implCmd.packageName));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            cmd.context.startActivity(intent);
        }
//        ImplAgent.notify(true,implInfo);
        return true;
    }
    
    boolean handlePackageAdded(ImplAgent.ImplRequest cmd) {
        // TODO Auto-generated method stub
        ImplAgent.PackageAddedReq implCmd = (ImplAgent.PackageAddedReq)cmd;
        String packageName = implCmd.intent.getData().getSchemeSpecificPart();
        ImplInfo implInfo = findInfoByPackageName(packageName);
        if (null == implInfo){
            return false;
        }
        try {
//            DownloadManager.getInstance(cmd.context).remove(implInfo.getDownloadId());
            new File(Uri.parse(implInfo.getLocalPath()).getPath()).delete();
        }catch(Exception e){
            e.printStackTrace();
        }
        implInfo.setStatus(Constant.STATUS_INSTALLED);
        implInfo.setLocalPath(null);
        save(implInfo);
        ImplAgent.notify(true,implInfo);
        return true;
    }

    boolean handlePackageChanged(ImplAgent.ImplRequest cmd) {
        // TODO Auto-generated method stub
        ImplAgent.PackageChangedReq implCmd = (ImplAgent.PackageChangedReq)cmd;
        String packageName = implCmd.intent.getData().getSchemeSpecificPart();
        ImplInfo implInfo = findInfoByPackageName(packageName);
        if (null == implInfo){
            return false;
        }
        implInfo.setStatus(Constant.STATUS_INSTALLED);
        save(implInfo);
        ImplAgent.notify(true,implInfo);
        return true;
    }
    
    boolean handlePackageRemoved(ImplAgent.ImplRequest cmd) {
        // TODO Auto-generated method stub
        ImplAgent.PackageRemovedReq implCmd = (ImplAgent.PackageRemovedReq)cmd;
        String packageName = implCmd.intent.getData().getSchemeSpecificPart();
        ImplInfo implInfo = findInfoByPackageName(packageName);
        if (null == implInfo){
            return false;
        }
        implInfo.setStatus(Constant.STATUS_INIT);
        save(implInfo);
//        remove(implInfo.getKey());
        ImplAgent.notify(true,implInfo);
        return true;
    }

    boolean handleSystemInstallResult(ImplAgent.ImplRequest cmd) {
        // TODO Auto-generated method stub
        ImplAgent.SystemInstallResultReq implCmd = (ImplAgent.SystemInstallResultReq)cmd;
        String packageName = implCmd.intent.getStringExtra("name");
        int result = implCmd.intent.getIntExtra("result",0);
        ImplInfo implInfo = findInfoByPackageName(packageName);
        if (null == implInfo){
            return false;
        }
        if (result == Constant.INSTALL_SUCCEEDED){
            implInfo.setStatus(Constant.STATUS_INSTALLED);
        }else{
            implInfo.setStatus(Constant.STATUS_INSTALL_FAILED);
            implInfo.setReason(result);
        }
        save(implInfo);
        ImplAgent.notify(true,implInfo);
        return true;
    }
    
    boolean handleSystemDeleteResult(ImplAgent.ImplRequest cmd) {
        // TODO Auto-generated method stub
        ImplAgent.SystemDeleteResultReq implCmd = (ImplAgent.SystemDeleteResultReq)cmd;
        String packageName = implCmd.intent.getStringExtra("name");
        int result = implCmd.intent.getIntExtra("result",0);
        ImplInfo implInfo = findInfoByPackageName(packageName);
        if (null == implInfo){
            return false;
        }
        if (result == Constant.DELETE_SUCCEEDED){
            implInfo.setStatus(Constant.STATUS_INIT);
        }
        ImplAgent.notify(true,implInfo);
        return true;
    }
}
