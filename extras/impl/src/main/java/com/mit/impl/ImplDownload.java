package com.mit.impl;

import android.content.Context;
import android.util.SparseArray;
import com.applite.common.Constant;
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

    void addDownload(ImplInfo implInfo,String fullname,String md5,ImplListener callback){
        try {
            DownloadInfo downloadInfo = dm.getDownloadInfoById(implInfo.getDownloadId());
            if (null != downloadInfo){
                dm.removeDownload(downloadInfo);
            }
//            String fullname = Environment.getExternalStorageDirectory() + File.separator + publicDir + filename;
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
            implInfo.setUserContinue(false);
            downloadInfo = dm.addNewDownload(implInfo.getDownloadUrl(),
                    implInfo.getTitle(),
                    fullname,
                    true,
                    true,
                    new DownloadCallback<File>(implInfo,callback));
            implInfo.setDownloadId(downloadInfo.getId());
            implInfo.setStatus(Constant.STATUS_PENDING);
            implInfo.setLastMod(System.currentTimeMillis());
            downloadInfo.getHandler().getRequestCallBack().setRate(callback.getRate());
            callback.onPending(implInfo);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    void pause(ImplInfo implInfo){
        try {
            DownloadInfo downloadInfo = dm.getDownloadInfoById(implInfo.getDownloadId());
            if (null != downloadInfo) {
                implInfo.setCause(Constant.CAUSE_PAUSED_BY_APP);
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
                implInfo.setUserContinue(true);
                dm.resumeDownload(downloadInfo, new DownloadCallback<File>(implInfo, callback));
//                implInfo.setCause(Constant.CAUSE_NONE);
                downloadInfo.getHandler().getRequestCallBack().setRate(callback.getRate());
                callback.onPending(implInfo);
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    void pauseAll(List<ImplInfo> implList){
        for (ImplInfo implInfo : implList){
            DownloadInfo downloadInfo = dm.getDownloadInfoById(implInfo.getDownloadId());
            if (null != downloadInfo){
                HttpHandler handler = downloadInfo.getHandler();
                if (null != handler && !handler.isCancelled() && !handler.isPaused()
                    && !downloadInfo.getState().equals(HttpHandler.State.SUCCESS)) {
                    try {
                        implInfo.setCause(Constant.CAUSE_PAUSED_BY_APP);
                        dm.stopDownload(downloadInfo);
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
//                        implInfo.setCause(Constant.CAUSE_NONE);
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
            if (implInfo.getStatus() == Constant.STATUS_PAUSED
                    && implInfo.getCause() == Constant.CAUSE_PAUSED_BY_APP){
                continue;
            }
            if (needKick(downloadInfo)){
                resume(implInfo,implCallback);
            }
        }
    }

    boolean checkOverSize(ImplInfo implInfo){
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

            if(Constant.STATUS_PENDING == implInfo.getStatus()
                || Constant.STATUS_RUNNING == implInfo.getStatus()
                || (Constant.STATUS_PAUSED == implInfo.getStatus()
                        && Constant.CAUSE_PAUSED_BY_APP != implInfo.getCause()) ) {
                try {
                    implInfo.setStatus(Constant.STATUS_PAUSED);
                    implInfo.setCause(Constant.CAUSE_PAUSED_BY_OVERSIZE);
                    dm.stopDownload(downloadInfo);
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
            if ("none".equals(network)) {
                if (Constant.STATUS_PAUSED == implInfo.getStatus()
                        && Constant.CAUSE_PAUSED_BY_APP != implInfo.getCause()){
                    implInfo.setCause(Constant.CAUSE_PAUSED_BY_NETWORK);
                    if (null != callback){
                        callback.onCancelled(implInfo);
                    }
                }
            }else if ("wifi".equals(network)) {
                if (Constant.STATUS_PAUSED == implInfo.getStatus()
                        && implInfo.getCause() != Constant.CAUSE_PAUSED_BY_APP){
                    try {
                        dm.resumeDownload(downloadInfo, new DownloadCallback<File>(implInfo, callback));
//                        implInfo.setCause(Constant.CAUSE_NONE);
                    } catch (DbException e) {
                        e.printStackTrace();
                    }
                }
            }else if ("mobile".equals(network)) {
                if (checkOverSize(implInfo)) {
                    if (null != callback) {
                        callback.onCancelled(implInfo);
                    }
                }
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
            if (total > 0 && total != implInfo.getSize()){
                implInfo.setSize(total);
            }
            if (!checkOverSize(implInfo)) {
                implInfo.setStatus(Constant.STATUS_RUNNING);
            }

            if (null != baseCallback){
                baseCallback.onLoading(implInfo, total, current, isUploading);
            }
            ImplLog.d(TAG,implInfo.getTitle()+",onLoading,"+total+","+current);
        }

        @Override
        public void onCancelled() {
            super.onCancelled();
            implInfo.setStatus(Constant.STATUS_PAUSED);

            if (null != baseCallback){
                baseCallback.onCancelled(implInfo);
            }
            ImplLog.d(TAG,implInfo.getTitle()+",onCancelled");
        }

        @Override
        public void onStart() {
            super.onStart();
            implInfo.setStatus(Constant.STATUS_RUNNING);

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
                implInfo.setStatus(Constant.STATUS_SUCCESSFUL);
                if (null != baseCallback){
                    baseCallback.onSuccess(implInfo,file);
                }
            }else{
                implInfo.setLocalPath(null);
                implInfo.setStatus(Constant.STATUS_FAILED);
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
                implInfo.setStatus(Constant.STATUS_PAUSED);
                implInfo.setCause(Constant.CAUSE_PAUSED_BY_NETWORK);
                if (null != baseCallback){
                    baseCallback.onCancelled(implInfo);
                }
            }else {
                implInfo.setStatus(Constant.STATUS_FAILED);
                if (null != baseCallback){
                    baseCallback.onFailure(implInfo,e,s);
                }
            }

            ImplLog.d(TAG,"onFailure,"+e.getCause()+","+e.getMessage()+","+e.getExceptionCode()+","+e.getClass());
        }
    }
}
