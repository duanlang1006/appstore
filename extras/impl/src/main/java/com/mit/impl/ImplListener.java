package com.mit.impl;
import java.io.File;

abstract class ImplListener {
    private int rate;

    protected ImplListener() {
        rate = 1000;
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

    public void onEnqued(ImplInfo info){};
    public void onPending(ImplInfo info){};
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
