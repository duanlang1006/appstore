package com.mit.impl;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.util.SparseArray;

import com.applite.common.Constant;

import java.io.File;
import java.lang.reflect.Method;

public class ImplPackageManager extends AbstractImpl {
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

//    @Override
//    public void init(Context context) {
//        super.init(context);
//
//    }

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

    public boolean handlePackageInstallReq(ImplAgent.ImplRequest cmd) {
        // TODO Auto-generated method stub
        boolean success = true;
        ImplAgent.InstallPackageReq implCmd = (ImplAgent.InstallPackageReq)cmd;
        ImplInfo implInfo = ImplConfig.findInfoByPackageName(databaseHelper,implCmd.packageName);
        if (null == implInfo){
            implInfo = new ImplInfo(implCmd.key,"",0,implCmd.packageName,"","","","");
            implInfo.setLocalPath(implCmd.localPath);
            save(implInfo);
        }

        ImplLog.d(TAG,"handlePackageInstallReq,"+implInfo.getKey()+","+implInfo.getTitle());
        boolean silent = implCmd.silent;
        try{
            PackageInfo info = pm.getPackageArchiveInfo(implCmd.localPath, PackageManager.GET_ACTIVITIES);
            if (null != info&& info.packageName.equals(implCmd.packageName)){
                if (!implCmd.silent){
                    silent = false;
                    installImplNormal(implCmd.context,implCmd.localPath);
                }else{
                    try {
                        pm.getPackageInfo("com.android.installer", 0);
                        installImplPrivate(implCmd.context,implCmd.localPath);
                        silent = true;
                    } catch (NameNotFoundException e) {
                        try{
                            pm.getPackageInfo("com.android.dbservices", 0);
                            installImplPrivate(implCmd.context,implCmd.localPath);
                            silent = true;
                        }catch(Exception e1){
                            e1.printStackTrace();
                            installImplNormal(implCmd.context,implCmd.localPath);
                            silent = false;
                        }
                    }
                }
            }
        }catch(Exception e){
            success = false;
            e.printStackTrace();
        }
        ImplAgent.notify(success, new ImplAgent.InstallPackageRsp(implCmd.context,implCmd.key, silent));
        return true;
    }
        
    private void installImplPrivate(final Context context ,final String filename){
        Intent intent = new Intent();
        intent.setAction("com.installer.system");
        intent.putExtra("name", filename);
        intent.putExtra("nameTag", "APK_PATH_NAME.tag");
        context.sendBroadcast(intent);
    }
    
    private void installImplNormal(final Context context ,final String filename) {
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
    }
    
    boolean handlePackageDeleteReq(ImplAgent.ImplRequest cmd) {
        // TODO Auto-generated method stub
        ImplAgent.DeletePackageReq implCmd = (ImplAgent.DeletePackageReq)cmd;
        ImplInfo implInfo = ImplConfig.findInfoByPackageName(databaseHelper,implCmd.packageName);
        if (null == implInfo){
            implInfo = new ImplInfo(implCmd.key,"",0,implCmd.packageName,"","","","");
        }
        ImplLog.d(TAG,"handlePackageDeleteReq,"+implInfo.getKey()+","+implInfo.getTitle());
        if (implCmd.silent){
            Intent intent = new Intent("com.installer.action.delete");
            intent.putExtra("name", implCmd.packageName);
            intent.putExtra("nameTag", "APK_PATH_NAME.tag");
            intent.putExtra("silent", true);
            cmd.context.sendBroadcast(intent);
            ImplAgent.notify(true,new ImplAgent.DeletePackageRsp(implCmd.context,implCmd.key,true));
            implInfo.setStatus(Constant.STATUS_PRIVATE_INSTALLING);
        }else {
            Intent intent = new Intent(Intent.ACTION_DELETE, Uri.parse("package:" + implCmd.packageName));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            cmd.context.startActivity(intent);
            ImplAgent.notify(true, new ImplAgent.DeletePackageRsp(implCmd.context, implCmd.key, false));
//            implInfo.setStatus(Constant.STATUS_NORMAL_INSTALLING);
        }
        save(implInfo);
        return true;
    }
    
    boolean handlePackageAdded(ImplAgent.ImplRequest cmd) {
        // TODO Auto-generated method stub
        ImplAgent.PackageAddedReq implCmd = (ImplAgent.PackageAddedReq)cmd;
        String packageName = implCmd.intent.getData().getSchemeSpecificPart();
        ImplInfo implInfo = ImplConfig.findInfoByPackageName(databaseHelper,packageName);
        if (null == implInfo){
            return false;
        }
        implInfo.setStatus(Constant.STATUS_INSTALLED);
        save(implInfo);
        ImplAgent.notify(true, new ImplAgent.PackageAddedRsp(cmd.context,implInfo.getKey()));
        return true;
    }

    boolean handlePackageChanged(ImplAgent.ImplRequest cmd) {
        // TODO Auto-generated method stub
        ImplAgent.PackageChangedReq implCmd = (ImplAgent.PackageChangedReq)cmd;
        String packageName = implCmd.intent.getData().getSchemeSpecificPart();
        ImplInfo implInfo = ImplConfig.findInfoByPackageName(databaseHelper,packageName);
        if (null == implInfo){
            return false;
        }
        implInfo.setStatus(Constant.STATUS_INSTALLED);
        save(implInfo);
        ImplAgent.notify(true,new ImplAgent.PackageChangedRsp(cmd.context,implInfo.getKey()));
        return true;
    }
    
    boolean handlePackageRemoved(ImplAgent.ImplRequest cmd) {
        // TODO Auto-generated method stub
        ImplAgent.PackageRemovedReq implCmd = (ImplAgent.PackageRemovedReq)cmd;
        String packageName = implCmd.intent.getData().getSchemeSpecificPart();
        ImplInfo implInfo = ImplConfig.findInfoByPackageName(databaseHelper,packageName);
        if (null == implInfo){
            return false;
        }
        remove(implInfo.getKey());
        ImplAgent.notify(true,new ImplAgent.PackageRemovedRsp(cmd.context,implInfo.getKey()));
        return true;
    }

    boolean handleSystemInstallResult(ImplAgent.ImplRequest cmd) {
        // TODO Auto-generated method stub
        ImplAgent.SystemInstallResultReq implCmd = (ImplAgent.SystemInstallResultReq)cmd;
        String packageName = implCmd.intent.getStringExtra("name");
        int result = implCmd.intent.getIntExtra("result",0);
        ImplInfo implInfo = ImplConfig.findInfoByPackageName(databaseHelper,packageName);
        if (null == implInfo){
            return false;
        }
        if (result == Constant.INSTALL_SUCCEEDED){
            implInfo.setStatus(Constant.STATUS_INSTALLED);
        }else{
            implInfo.setStatus(Constant.STATUS_INSTALL_FAILED);
            implInfo.setReason(result);
        }
        ImplAgent.notify(true,new ImplAgent.SystemInstallResultRsp(cmd.context,implInfo.getKey(),result));
        return true;
    }
    
    boolean handleSystemDeleteResult(ImplAgent.ImplRequest cmd) {
        // TODO Auto-generated method stub
        ImplAgent.SystemDeleteResultReq implCmd = (ImplAgent.SystemDeleteResultReq)cmd;
        String packageName = implCmd.intent.getStringExtra("name");
        int result = implCmd.intent.getIntExtra("result",0);
        ImplInfo implInfo = ImplConfig.findInfoByPackageName(databaseHelper,packageName);
        if (null == implInfo){
            return false;
        }
        if (result == Constant.DELETE_SUCCEEDED){
            remove(implInfo.getKey());
        }else{
        }
        ImplAgent.notify(true,new ImplAgent.SystemDeleteResultRsp(cmd.context,implInfo.getKey(),result));
        return true;
    }
}
