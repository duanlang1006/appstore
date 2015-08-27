package com.mit.impl;

import android.content.Context;
import android.util.SparseArray;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.mit.impl.download.DownloadInfo;
import com.mit.impl.download.DownloadManager;
import com.mit.impl.download.DownloadService;
import java.io.File;
import java.lang.reflect.Method;
import java.util.List;

public class ImplDownload  {
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
                    implInfo.setStatus(ImplInfo.STATUS_PENDING);
                    break;
                case STARTED:
                    implInfo.setStatus(ImplInfo.STATUS_RUNNING);
                    break;
                case LOADING:
                    implInfo.setStatus(ImplInfo.STATUS_RUNNING);
                    break;
                case CANCELLED:
                    implInfo.setStatus(ImplInfo.STATUS_PAUSED);
                    break;
                case SUCCESS:
                    implInfo.setStatus(ImplInfo.STATUS_SUCCESSFUL);
                    break;
                case FAILURE:
                    if (implInfo.getStatus() == ImplInfo.STATUS_PAUSED
                            && implInfo.getCause() == ImplInfo.CAUSE_PAUSED_BY_NETWORK){
                        //do nothing
                    }else {
                        implInfo.setStatus(ImplInfo.STATUS_FAILED);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private class AddDownlaodTask implements Runnable{
        private static final int RESULT_MD5_MATCH = 0;
        private static final int RESULT_ADD_DOWNLOAD = 1;
        private static final int RESULT_ADD_DOWNLOAD_EXCEPTION = 2;

        private String fullname;
        private String md5;
        private ImplListener callback;
        private ImplInfo implInfo;

        private AddDownlaodTask(ImplInfo implInfo,String fullname, String md5, ImplListener callback) {
            this.fullname = fullname;
            this.md5 = md5;
            this.callback = callback;
            this.implInfo = implInfo;
        }

        @Override
        public void run() {
            int result = RESULT_ADD_DOWNLOAD;
            File file = new File(fullname);
            if (file.exists()
                    && null != md5 && !TextUtils.isEmpty(md5)
                    && md5.equals(ImplHelper.getFileMD5(file))){
                result = RESULT_MD5_MATCH;
            }else {
                if (file.exists()){
                    file.delete();
                }
                try {
                    DownloadInfo downloadInfo = dm.getDownloadInfoById(implInfo.getDownloadId());
                    if (null != downloadInfo) {
                        dm.removeDownload(downloadInfo);
                    }
                    downloadInfo = dm.addNewDownload(implInfo.getDownloadUrl(),
                            implInfo.getTitle(),
                            fullname,
                            true,
                            true,
                            new DownloadCallback<File>(implInfo, callback));
                    downloadInfo.getHandler().getRequestCallBack().setRate(callback.getRate());
                } catch (DbException e) {
                    e.printStackTrace();
                    result = RESULT_ADD_DOWNLOAD_EXCEPTION;
                }
            }
            ImplLog.d(TAG,"run,"+implInfo.getTitle()+","+implInfo.getStatus()+","+implInfo.getDownloadId()+","+result);
            switch(result){
                case RESULT_ADD_DOWNLOAD:
                    break;
                case RESULT_ADD_DOWNLOAD_EXCEPTION:
                    callback.onFailure(implInfo, null, "add download exception");
                    break;
                case RESULT_MD5_MATCH:
                    callback.onSuccess(this.implInfo,new File(fullname));
                    break;
            }
        }
    }

    void addDownload(ImplInfo implInfo,String fullname,String md5,ImplListener callback){
        if (null == implInfo.getDownloadUrl()){
            if (null != callback) {
                callback.onFailure(implInfo, null, "download url is null");
            }
            return;
        }
        ImplAgent.mWorkHandler.post(new AddDownlaodTask(implInfo,fullname,md5,callback));
    }

    void pause(ImplInfo implInfo,ImplListener callback){
        try {
            DownloadInfo downloadInfo = dm.getDownloadInfoById(implInfo.getDownloadId());
            if (null != downloadInfo) {
                implInfo.setCause(ImplInfo.CAUSE_PAUSED_BY_APP);
                dm.stopDownload(downloadInfo,new DownloadCallback<File>(implInfo, callback));
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    void resume(ImplInfo implInfo,ImplListener callback){
        try {
            DownloadInfo downloadInfo = dm.getDownloadInfoById(implInfo.getDownloadId());
            if (null != downloadInfo) {
                implInfo.setUserContinue(true);
                dm.resumeDownload(downloadInfo, new DownloadCallback<File>(implInfo, callback));
                downloadInfo.getHandler().getRequestCallBack().setRate(callback.getRate());
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    void pauseAll(List<ImplInfo> implList,ImplListener callback){
        for (ImplInfo implInfo : implList){
            DownloadInfo downloadInfo = dm.getDownloadInfoById(implInfo.getDownloadId());
            if (null != downloadInfo){
                HttpHandler handler = downloadInfo.getHandler();
                if (null != handler && !handler.isCancelled() && !handler.isPaused()
                    && !downloadInfo.getState().equals(HttpHandler.State.SUCCESS)) {
                    try {
                        implInfo.setCause(ImplInfo.CAUSE_PAUSED_BY_APP);
                        dm.stopDownload(downloadInfo,new DownloadCallback<File>(implInfo, callback));
                    } catch (DbException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    void resumeAll(List<ImplInfo> implList,ImplListener callback){
        for (ImplInfo implInfo : implList){
            DownloadInfo downloadInfo = dm.getDownloadInfoById(implInfo.getDownloadId());
            if (null != downloadInfo){
                HttpHandler handler = downloadInfo.getHandler();
                if ((null == handler ||  (handler.isPaused() || handler.isCancelled()))
                    && !downloadInfo.getState().equals(HttpHandler.State.SUCCESS)) {
                    try {
                        implInfo.setUserContinue(true);
                        dm.resumeDownload(downloadInfo, new DownloadCallback<File>(implInfo, callback));
                        downloadInfo.getHandler().getRequestCallBack().setRate(callback.getRate());
                    } catch (DbException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
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

    boolean needKick(DownloadInfo downloadInfo){
        boolean ret = false;
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

    void kickDownload(List<ImplInfo> implList,ImplListener implCallback){
        ImplInfo implInfo = null;
        for (int i = 0 ;i < implList.size();i++){
            implInfo = implList.get(i);
            DownloadInfo downloadInfo = dm.getDownloadInfoById(implInfo.getDownloadId());
            if (null == downloadInfo){
                continue;
            }
            if (implInfo.getStatus() == ImplInfo.STATUS_PAUSED
                    && implInfo.getCause() == ImplInfo.CAUSE_PAUSED_BY_APP){
                continue;
            }
            if (needKick(downloadInfo)){
                try {
                    dm.resumeDownload(downloadInfo, new DownloadCallback<File>(implInfo, implCallback));
                    downloadInfo.getHandler().getRequestCallBack().setRate(implCallback.getRate());
                } catch (DbException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean overSizePause(ImplInfo implInfo,ImplListener callback){
        boolean ret = false;
        if ("mobile".equals(ImplReceiver.getNetwork(mContext))
                && implInfo.getSize() > 1024000) {
            if (implInfo.isUserContinue()){
                return ret;
            }

            DownloadInfo downloadInfo = dm.getDownloadInfoById(implInfo.getDownloadId());
            if (null == downloadInfo){
                return ret;
            }

            if(ImplInfo.STATUS_PENDING == implInfo.getStatus()
                || ImplInfo.STATUS_RUNNING == implInfo.getStatus()
                || (ImplInfo.STATUS_PAUSED == implInfo.getStatus()
                        && ImplInfo.CAUSE_PAUSED_BY_APP != implInfo.getCause()) ) {
                try {
                    implInfo.setStatus(ImplInfo.STATUS_PAUSED);
                    implInfo.setCause(ImplInfo.CAUSE_PAUSED_BY_OVERSIZE);
                    dm.stopDownload(downloadInfo,new DownloadCallback<File>(implInfo, callback));
                    ret = true;
                } catch (DbException e) {
                    e.printStackTrace();
                }
            }
        }
        return ret;
    }

    void onNetworkChanged(List<ImplInfo> implList,ImplListener callback){
        String network = ImplReceiver.getNetwork(mContext);
        ImplInfo implInfo = null;
        for (int i = 0;i < implList.size();i++) {
            implInfo = implList.get(i);
            DownloadInfo downloadInfo = dm.getDownloadInfoById(implInfo.getDownloadId());
            if (null == downloadInfo){
                continue;
            }
            switch(network){
                case "none":
                    if (ImplInfo.STATUS_PAUSED == implInfo.getStatus()
                            && implInfo.getCause() != ImplInfo.CAUSE_PAUSED_BY_APP) {
                        implInfo.setCause(ImplInfo.CAUSE_PAUSED_BY_NETWORK);
                        if (null != callback) {
                            callback.onCancelled(implInfo);
                        }
                    }
                    break;
                case "wifi":
                    if (ImplInfo.STATUS_PAUSED == implInfo.getStatus()
                            && implInfo.getCause() != ImplInfo.CAUSE_PAUSED_BY_APP) {
                        try {
                            dm.resumeDownload(downloadInfo, new DownloadCallback<File>(implInfo, callback));
                        } catch (DbException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case "mobile":
                    overSizePause(implInfo,callback);
                    break;
            }
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

    String getLocalPath(ImplInfo implInfo){
        String ret = null;
        DownloadInfo downloadInfo = dm.getDownloadInfoById(implInfo.getDownloadId());
        if (null != downloadInfo ){
            ret = downloadInfo.getFileSavePath();
        }
        return ret;
    }

    public class DownloadCallback<File> extends RequestCallBack<File> {
        private ImplInfo implInfo;
        private ImplListener baseCallback;

        DownloadCallback(ImplInfo implInfo,ImplListener callback) {
            this.implInfo = implInfo;
            this.baseCallback = callback;
        }

        public void onEnqued(long downloadId){
            implInfo.setDownloadId(downloadId);
            implInfo.setStatus(ImplInfo.STATUS_PENDING);
            implInfo.setUserContinue(false);
            implInfo.setLastMod(System.currentTimeMillis());
            if (null != baseCallback){
                baseCallback.onEnqued(implInfo);
            }
        }

        public void onPending(){
            implInfo.setStatus(ImplInfo.STATUS_PENDING);
            if (null != baseCallback){
                baseCallback.onPending(implInfo);
            }
        }

        @Override
        public void onLoading(long total, long current, boolean isUploading) {
            super.onLoading(total, current, isUploading);
            if (total > 0 && total != implInfo.getSize()){
                implInfo.setSize(total);
            }
            if (!overSizePause(implInfo,baseCallback)) {
                implInfo.setStatus(ImplInfo.STATUS_RUNNING);
            }

            if (null != baseCallback){
                baseCallback.onLoading(implInfo, total, current, isUploading);
            }
        }

        @Override
        public void onCancelled() {
            super.onCancelled();
            implInfo.setStatus(ImplInfo.STATUS_PAUSED);

            if (null != baseCallback){
                baseCallback.onCancelled(implInfo);
            }
            ImplLog.d(TAG,implInfo.getTitle()+",onCancelled");
        }

        @Override
        public void onStart() {
            super.onStart();
            implInfo.setStatus(ImplInfo.STATUS_RUNNING);

            if (null != baseCallback){
                baseCallback.onStart(implInfo);
            }
            ImplLog.d(TAG,implInfo.getTitle()+",onStart");
        }

        @Override
        public void onSuccess(ResponseInfo<File> fileResponseInfo) {
            java.io.File file = (java.io.File)fileResponseInfo.result;
            if (null != file && file.exists()) {
                implInfo.setLocalPath(file.getAbsolutePath());
                implInfo.setStatus(ImplInfo.STATUS_SUCCESSFUL);
                if (null != baseCallback){
                    baseCallback.onSuccess(implInfo,file);
                }
            }else{
                implInfo.setLocalPath(null);
                implInfo.setStatus(ImplInfo.STATUS_FAILED);
                if (null != baseCallback){
                    baseCallback.onFailure(implInfo, null, "download file not exist");
                }
            }
            ImplLog.d(TAG,implInfo.getTitle()+",onSuccess");
        }

        @Override
        public void onFailure(HttpException e, String s) {
            ImplReceiver.initNetwork(mContext);
            if ("none".equals(ImplReceiver.getNetwork(mContext))){
                implInfo.setStatus(ImplInfo.STATUS_PAUSED);
                implInfo.setCause(ImplInfo.CAUSE_PAUSED_BY_NETWORK);
                if (null != baseCallback){
                    baseCallback.onCancelled(implInfo);
                }
            }else {
                implInfo.setStatus(ImplInfo.STATUS_FAILED);
                if (null != baseCallback){
                    baseCallback.onFailure(implInfo,e,s);
                }
            }
            ImplLog.d(TAG,"onFailure,"+e.getCause()+","+e.getMessage()+","+e.getExceptionCode()+","+e.getClass());
        }
    }
}
