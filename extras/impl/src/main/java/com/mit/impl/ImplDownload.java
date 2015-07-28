package com.mit.impl;

import android.content.Context;
import android.os.Environment;
import android.util.SparseArray;
import com.applite.common.Constant;
import com.applite.common.LogUtils;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.mit.impl.download.DownloadInfo;
import com.mit.impl.download.DownloadManager;
import com.mit.impl.download.DownloadService;
import java.io.File;
import java.lang.reflect.Method;
import java.util.List;

class ImplDownload  {
    private static final String TAG = "impl_download";
    private SparseArray<Method> mCmdList = new SparseArray<Method>();
    private DownloadManager dm;
    private boolean inited = false;
    private Context mContext;

    private static ImplDownload mInstance = null;
    private static synchronized void initInstance(Context context){
        if (null == mInstance ){
            mInstance = new ImplDownload(context);
        }
    }

    static ImplDownload getInstance(Context context){
        if (null == mInstance){
            initInstance(context);
        }
        return mInstance;
    }

    private ImplDownload(Context context) {
        mContext = context;
        if (null == dm) {
            dm = DownloadService.getDownloadManager(mContext.getApplicationContext());
        }
        inited = true;
    }

    void fillImplInfo(ImplInfo implInfo){
        if (null == implInfo){
            return;
        }
        DownloadInfo downloadInfo = dm.getDownloadInfoById(implInfo.getDownloadId());
        if (null != downloadInfo){
            switch(downloadInfo.getState()){
                case WAITING:
                    implInfo.setStatus(Constant.STATUS_PENDING);
                    break;
                case STARTED:
                    implInfo.setStatus(Constant.STATUS_RUNNING);
                    break;
                case LOADING:
                    implInfo.setStatus(Constant.STATUS_RUNNING);
                    break;
                case CANCELLED:
                    implInfo.setStatus(Constant.STATUS_PAUSED);
                    break;
                case SUCCESS:
                    implInfo.setStatus(Constant.STATUS_SUCCESSFUL);
                    break;
                case FAILURE:
                    implInfo.setStatus(Constant.STATUS_FAILED);
                    break;
                default:
                    break;
            }
        }
    }

    void addDownload(ImplInfo implInfo,String publicDir,String filename,ImplListener callback){
        try {
            DownloadInfo downloadInfo = dm.getDownloadInfoById(implInfo.getDownloadId());
            if (null != downloadInfo){
                dm.removeDownload(downloadInfo);
            }
            String fullname = Environment.getExternalStorageDirectory() + File.separator + publicDir + filename;
            File file = new File(fullname);
            if (null != file){
                file.delete();
            }
            if (null == implInfo.getDownloadUrl()){
                if (null != callback) {
                    callback.onFailure(implInfo, null, "download url is null");
                }
                return;
            }

            downloadInfo = dm.addNewDownload(implInfo.getDownloadUrl(),
                    implInfo.getTitle(),
                    fullname,
                    true,
                    true,
                    new DownloadCallback<File>(implInfo,callback));
            implInfo.setDownloadId(downloadInfo.getId());
            downloadInfo.getHandler().getRequestCallBack().setRate(callback.getRate());
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    void pause(ImplInfo implInfo){
        try {
            DownloadInfo downloadInfo = dm.getDownloadInfoById(implInfo.getDownloadId());
            if (null != downloadInfo) {
                dm.stopDownload(downloadInfo);
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    void resume(ImplInfo implInfo,ImplListener callback){
        try {
            DownloadInfo downloadInfo = dm.getDownloadInfoById(implInfo.getDownloadId());
            if (null != downloadInfo) {
                dm.resumeDownload(downloadInfo, new DownloadCallback<File>(implInfo, callback));
                downloadInfo.getHandler().getRequestCallBack().setRate(callback.getRate());
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    boolean needKick(ImplInfo implInfo){
        boolean ret = false;
        DownloadInfo downloadInfo = dm.getDownloadInfoById(implInfo.getDownloadId());
        if (null != downloadInfo && null == downloadInfo.getHandler()) {
            switch (downloadInfo.getState()){
                case WAITING:
                case STARTED:
                case LOADING:
                    ret = true;
                    break;
            }
        }
        return ret;
    }

    void remove(ImplInfo implInfo){
        try {
            DownloadInfo downloadInfo = dm.getDownloadInfoById(implInfo.getDownloadId());
            if (null != downloadInfo) {
                dm.removeDownload(downloadInfo);
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    int getProgress(ImplInfo implInfo){
        DownloadInfo downloadInfo = dm.getDownloadInfoById(implInfo.getDownloadId());
        int progress = 0;
        if (null != downloadInfo && downloadInfo.getFileLength() > 0){
            progress = (int)(downloadInfo.getProgress()*100/downloadInfo.getFileLength());
        }
        return progress;
    }

    long getCurrentBytes(ImplInfo implInfo){
        long ret = 0;
        DownloadInfo downloadInfo = dm.getDownloadInfoById(implInfo.getDownloadId());
        if (null != downloadInfo ){
            ret = downloadInfo.getProgress();
        }
        return ret;
    }

    long getTotalBytes(ImplInfo implInfo){
        long ret = 0;
        DownloadInfo downloadInfo = dm.getDownloadInfoById(implInfo.getDownloadId());
        if (null != downloadInfo ){
            ret = downloadInfo.getFileLength();
        }
        return ret;
    }


    class DownloadCallback<File> extends RequestCallBack<File> {
        private ImplInfo implInfo;
        private ImplListener baseCallback;

        DownloadCallback(ImplInfo implInfo,ImplListener callback) {
            this.implInfo = implInfo;
            this.baseCallback = callback;
        }

        @Override
        public void onLoading(long total, long current, boolean isUploading) {
            super.onLoading(total, current, isUploading);
            implInfo.setStatus(Constant.STATUS_RUNNING);

            if (null != baseCallback){
                baseCallback.onLoading(implInfo,total,current,isUploading);
            }
            LogUtils.d(TAG,implInfo.getTitle()+",onLoading,"+total+","+current);
        }

        @Override
        public void onCancelled() {
            super.onCancelled();
            implInfo.setStatus(Constant.STATUS_PAUSED);

            if (null != baseCallback){
                baseCallback.onCancelled(implInfo);
            }
            LogUtils.d(TAG,implInfo.getTitle()+",onCancelled");
        }

        @Override
        public void onStart() {
            super.onStart();
            implInfo.setStatus(Constant.STATUS_RUNNING);

            if (null != baseCallback){
                baseCallback.onStart(implInfo);
            }
            LogUtils.d(TAG,implInfo.getTitle()+",onStart");
        }

        @Override
        public void onSuccess(ResponseInfo<File> fileResponseInfo) {
            java.io.File file = (java.io.File)fileResponseInfo.result;
            implInfo.setStatus(Constant.STATUS_SUCCESSFUL);
            if (null != file) {
                implInfo.setLocalPath(file.getPath());
            }

            if (null != baseCallback){
                baseCallback.onSuccess(implInfo,file);
            }
            LogUtils.d(TAG,implInfo.getTitle()+",onSuccess");
        }

        @Override
        public void onFailure(HttpException e, String s) {
            implInfo.setStatus(Constant.STATUS_FAILED);

            if (null != baseCallback){
                baseCallback.onFailure(implInfo,e,s);
            }
            LogUtils.d(TAG,implInfo.getTitle()+",onFailure,"+s);
        }
    }
}
