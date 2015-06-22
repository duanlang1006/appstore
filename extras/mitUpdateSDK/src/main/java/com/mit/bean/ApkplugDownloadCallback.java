package com.mit.bean;

/**
 * Created by hxd on 15-5-29.
 */
public interface ApkplugDownloadCallback {
    int suc_install = 0;
    int fail_install = -1;
    int suc_download = 1;
    int fail_download = -2;
    int suc_url = 2;
    int fail_url = -2;

    void onDownLoadSuccess(String path);

    void onProgress(long bytesWritten, long totalSize, String speed);

    void onFailure(int cause, String errMsg);
}