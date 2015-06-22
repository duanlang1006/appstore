package com.mit.mitupdatesdk;

import com.mit.afinal.http.AjaxCallBack;
import com.mit.bean.ApkplugDownloadCallback;

import java.io.File;

/**
 * Created by hxd on 15-6-2.
 */
public class MyAjaxCallBack<T> extends AjaxCallBack<File> {
    ApkplugDownloadCallback downloadCallback;

    public MyAjaxCallBack(ApkplugDownloadCallback downloadCallback) {
        this.downloadCallback = downloadCallback;
    }

    @Override
    public void onSuccess(File file) {
        super.onSuccess(file);
        if (null != downloadCallback){
            downloadCallback.onDownLoadSuccess(file.getAbsolutePath());
        }
    }

    @Override
    public void onFailure(Throwable t, int errorNo, String strMsg) {
        super.onFailure(t, errorNo, strMsg);
        if (null != downloadCallback){
            downloadCallback.onFailure(errorNo, strMsg);
        }
    }

    @Override
    public void onLoading(long count, long current) {
        super.onLoading(count, current);
        if (null != downloadCallback){
            downloadCallback.onProgress(current,count,"");
        }
    }
}
