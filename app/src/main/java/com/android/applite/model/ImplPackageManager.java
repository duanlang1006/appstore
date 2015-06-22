package com.android.applite.model;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.util.Log;
import android.util.SparseArray;

import java.io.File;

public class ImplPackageManager implements ImplInterface {
    private SparseArray<String> mCmdList = new SparseArray<String>();
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
        mCmdList.append(ACTION_PACKAGE_INSTALL_REQ.hashCode(), ACTION_PACKAGE_INSTALL_REQ);
        mCmdList.append(ACTION_PACKAGE_DELETE_REQ.hashCode(), ACTION_PACKAGE_DELETE_REQ);
        mCmdList.append(ACTION_PACKAGE_ADDED.hashCode(), ACTION_PACKAGE_ADDED);
        mCmdList.append(ACTION_PACKAGE_CHANGED.hashCode(), ACTION_PACKAGE_CHANGED);
        mCmdList.append(ACTION_PACKAGE_REMOVED.hashCode(), ACTION_PACKAGE_REMOVED);
        mCmdList.append("com.installer.system.install.result".hashCode(), "com.installer.system.install.result");
        mCmdList.append("com.installer.system.delete.result".hashCode(), "com.installer.system.delete.result");
    }

    @Override
    public boolean request(Context context,Intent cmd, ImplListener listener) {
        // TODO Auto-generated method stub
        final String action = mCmdList.get(cmd.getAction().hashCode());
        if (DEBUG){
            Log.d(TAG,"ImplPackageManager,request("+action+")");
        }
        if (null == action){
            return false;
        }
        
        if (ACTION_PACKAGE_INSTALL_REQ.equals(action)){
            return handlePackageInstallReq(context, cmd, listener);
        }else if (ACTION_PACKAGE_DELETE_REQ.equals(action)){
            return handlePackageDeleteReq(context, cmd, listener);
        }else if (ACTION_PACKAGE_ADDED.equals(action)){
            return handlePackageAdded(context, cmd, listener);
        }else if (ACTION_PACKAGE_CHANGED.equals(action)){
            return handlePackageChanged(context, cmd, listener);
        }else if (ACTION_PACKAGE_REMOVED.equals(action)){
            return handlePackageRemoved(context, cmd, listener);
        }else if ("com.installer.system.install.result".equals(action)){
            return handleSystemInstallResult(context, cmd, listener);
        }else if ("com.installer.system.delete.result".equals(action)){
            return handleSystemDeleteResult(context, cmd, listener);
        }
        return false;
    }

    @Override
    public void abort(Context context,Intent cmd,  ImplListener listener) {
        // TODO Auto-generated method stub

    }
    
    public boolean handlePackageInstallReq(Context context,Intent cmd,  ImplListener listener) {
        // TODO Auto-generated method stub
        Intent result = new Intent(ACTION_IMPL_RESULT);
        boolean success = true;
        
        String apkPath = cmd.getStringExtra("apk");
        boolean silent = cmd.getBooleanExtra("silent", false);
        String packageName = cmd.getStringExtra("package");
        final PackageManager pm = context.getPackageManager();
        try{
            PackageInfo info = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
            if (null != info&& info.packageName.equals(packageName)){
                if (!silent){
                    result.putExtra("silent",false);
                    installImplNormal(context,apkPath);
                }else{
                    try {
                        pm.getPackageInfo("com.android.installer", 0);
                        installImplPrivate(context,apkPath);
                        result.putExtra("silent",true);
                    } catch (NameNotFoundException e) {
                        try{
                            pm.getPackageInfo("com.android.dbservices", 0);
                            installImplPrivate(context,apkPath);
                            result.putExtra("silent",true);
                        }catch(Exception e1){
                            e1.printStackTrace();
                            installImplNormal(context,apkPath);
                            result.putExtra("silent",false);
                        }
                    }
                }
            }
        }catch(Exception e){
            success = false;
            e.printStackTrace();
        }
        if (null != listener){
            listener.onFinish(success,cmd.getAction(), result);
        }
        return true;
    }
        
    private void installImplPrivate(final Context context ,final String fileName){
        Intent intent = new Intent();
        intent.setAction("com.installer.system");
        intent.putExtra("name", fileName);
        intent.putExtra("nameTag", "APK_PATH_NAME.tag");
        context.sendBroadcast(intent);
    }
    
    private void installImplNormal(final Context context ,final String fileName) {
        Uri path = Uri.parse(fileName);
        if (path.getScheme() == null) {
            path = Uri.fromFile(new File(fileName));
        }
        Intent activityIntent = new Intent(Intent.ACTION_VIEW);
        activityIntent.setDataAndType(path, "application/vnd.android.package-archive");
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(activityIntent);
        } catch (ActivityNotFoundException ex) {
        
        }
    }
    
    boolean handlePackageDeleteReq(Context context,Intent cmd,  ImplListener listener) {
        // TODO Auto-generated method stub
        String packageN = cmd.getStringExtra("package"); 
        Intent intent = new Intent(Intent.ACTION_DELETE, Uri.parse("package:" + packageN));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        
        Intent result = new Intent(ACTION_IMPL_RESULT);
        if (null != listener){
            listener.onFinish(true, cmd.getAction(), result);
        }
        return true;
    }
    
    boolean handlePackageAdded(Context context,Intent cmd,  ImplListener listener) {
        // TODO Auto-generated method stub
        Intent result = new Intent(ACTION_IMPL_RESULT);
        if (null != listener){
            listener.onFinish(true, cmd.getAction(), result);
        }
        return true;
    }

    boolean handlePackageChanged(Context context,Intent cmd,  ImplListener listener) {
        // TODO Auto-generated method stub
        Intent result = new Intent(ACTION_IMPL_RESULT);
        if (null != listener){
            listener.onFinish(true, cmd.getAction(), result);
        }
        return true;
    }
    
    boolean handlePackageRemoved(Context context,Intent cmd,  ImplListener listener) {
        // TODO Auto-generated method stub
        Intent result = new Intent(ACTION_IMPL_RESULT);
        if (null != listener){
            listener.onFinish(true, cmd.getAction(), result);
        }
        return true;
    }

    boolean handleSystemInstallResult(Context context,Intent cmd,  ImplListener listener) {
        // TODO Auto-generated method stub
        String packageName = cmd.getStringExtra("name");
        int returnCode = cmd.getIntExtra("result",0);
        if (DEBUG){
            Log.d(TAG,packageName+","+returnCode);
        }
        Intent result = new Intent(ACTION_IMPL_RESULT);
        if (null != listener){
            listener.onFinish(returnCode == 1, cmd.getAction(), result);
        }
        return true;
    }
    
    boolean handleSystemDeleteResult(Context context,Intent cmd,  ImplListener listener) {
        // TODO Auto-generated method stub
        String packageName = cmd.getStringExtra("name");
        int returnCode = cmd.getIntExtra("result",0);
        Intent result = new Intent(ACTION_IMPL_RESULT);
        if (null != listener && returnCode == 1){
            listener.onFinish(true, cmd.getAction(), result);
        }
        return true;
    }
}
