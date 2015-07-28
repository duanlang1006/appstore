package com.mit.impl;
import java.io.File;

public abstract class ImplListener {
//    public void onDownloadComplete(boolean success,ImplAgent.DownloadCompleteRsp rsp);
//    public void onDownloadUpdate(boolean success,ImplAgent.DownloadUpdateRsp rsp);
//    public void onPackageAdded(boolean success,ImplAgent.PackageAddedRsp rsp);
//    public void onPackageRemoved(boolean success,ImplAgent.PackageRemovedRsp rsp);
//    public void onPackageChanged(boolean success,ImplAgent.PackageChangedRsp rsp);
//    public void onSystemInstallResult(boolean success,ImplAgent.SystemInstallResultRsp rsp);
//    public void onSystemDeleteResult(boolean success,ImplAgent.SystemDeleteResultRsp rsp);
//    public void onFinish(boolean success, ImplAgent.ImplResponse rsp);

    private int rate;

    protected ImplListener() {
        rate = 500;
    }

    protected ImplListener(int rate) {
        if (rate < 250){
            rate = 250;
        }
        this.rate = rate;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public void onStart(ImplInfo info){};
    public void onCancelled(ImplInfo info){};
    public void onLoading(ImplInfo info,long total, long current, boolean isUploading){};
    public void onSuccess(ImplInfo info,File file){};
    public void onFailure(ImplInfo info,Throwable t,String msg){};

    public void onInstallSuccess(ImplInfo info){}
    public void onInstalling(ImplInfo info){}
    public void onInstallFailure(ImplInfo info,int errorCode){}
    public void onUninstallSuccess(ImplInfo info){}
    public void onUninstalling(ImplInfo info){}
    public void onUninstallFailure(ImplInfo info,int errorCode){}

//    public void onUpdate(boolean success, ImplInfo info);
}
