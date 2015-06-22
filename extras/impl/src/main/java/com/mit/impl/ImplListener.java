package com.mit.impl;

public interface ImplListener {
    public void onDownloadComplete(boolean success,ImplAgent.DownloadCompleteRsp rsp);
    public void onDownloadUpdate(boolean success,ImplAgent.DownloadUpdateRsp rsp);
    public void onPackageAdded(boolean success,ImplAgent.PackageAddedRsp rsp);
    public void onPackageRemoved(boolean success,ImplAgent.PackageRemovedRsp rsp);
    public void onPackageChanged(boolean success,ImplAgent.PackageChangedRsp rsp);
    public void onSystemInstallResult(boolean success,ImplAgent.SystemInstallResultRsp rsp);
    public void onSystemDeleteResult(boolean success,ImplAgent.SystemDeleteResultRsp rsp);

    public void onFinish(boolean success, ImplAgent.ImplResponse rsp);
}
