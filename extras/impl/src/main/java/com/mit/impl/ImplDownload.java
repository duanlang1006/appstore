package com.mit.impl;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.SparseArray;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.db.converter.ColumnConverter;
import com.lidroid.xutils.db.converter.ColumnConverterFactory;
import com.lidroid.xutils.db.sqlite.ColumnDbType;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImplDownload {
    private static final String TAG = "impl_download";
    private SparseArray<Method> mCmdList = new SparseArray<Method>();
    private int maxDownloadThread;
    //    private DownloadManager dm;
    private boolean inited = false;
    private Context mContext;
    private final static ExecutorService EXECUTOR = Executors.newCachedThreadPool();


    private static ImplDownload mInstance = null;

    private static synchronized void initInstance(Context context) {
        if (null == mInstance) {
            mInstance = new ImplDownload(context);
        }
    }

    static ImplDownload getInstance(Context context) {
        if (null == mInstance) {
            initInstance(context);
        }
        return mInstance;
    }

    private ImplDownload(Context context) {
        mContext = context;
        ColumnConverterFactory.registerColumnConverter(HttpHandler.State.class, new HttpHandlerStateConverter());
//        if (null == dm) {
//            dm = DownloadService.getDownloadManager(mContext.getApplicationContext());
//        }
        if (!DownloadService.isServiceRunning(mContext)) {
            Intent downloadSvr = new Intent(mContext, DownloadService.class);
            mContext.startService(downloadSvr);
        }
        inited = true;
        if (maxDownloadThread == 0)
            maxDownloadThread = ImplConfig.getDownloadThreadNum(context);
        ImplLog.d(TAG, "maxDownloadThread = " + maxDownloadThread);
    }

    void fillImplInfo(ImplInfo implInfo) {
//        if (null == implInfo){
//            return;
//        }
//        DownloadInfo downloadInfo = dm.getDownloadInfoById(implInfo.getDownloadId());
//        if (null != downloadInfo){
//            switch(downloadInfo.getState()){
//                case WAITING:
//                    implInfo.setStatus(ImplInfo.STATUS_PENDING);
//                    break;
//                case STARTED:
//                    implInfo.setStatus(ImplInfo.STATUS_RUNNING);
//                    break;
//                case LOADING:
//                    implInfo.setStatus(ImplInfo.STATUS_RUNNING);
//                    break;
//                case CANCELLED:
//                    implInfo.setStatus(ImplInfo.STATUS_PAUSED);
//                    break;
//                case SUCCESS:
//                    implInfo.setStatus(ImplInfo.STATUS_SUCCESSFUL);
//                    break;
//                case FAILURE:
//                    if (implInfo.getStatus() == ImplInfo.STATUS_PAUSED
//                            && implInfo.getCause() == ImplInfo.CAUSE_PAUSED_BY_NETWORK){
//                        //do nothing
//                    }else {
//                        implInfo.setStatus(ImplInfo.STATUS_FAILED);
//                    }
//                    break;
//                default:
//                    break;
//            }
//        }
    }

    private class AddDownloadAsync extends AsyncTask<Object, Object, File> {
        private String fullname;
        private String md5;
        private ImplListener callback;
        private ImplInfo implInfo;

        private AddDownloadAsync(ImplInfo implInfo, String fullname, String md5, ImplListener callback) {
            this.fullname = fullname;
            this.md5 = md5;
            this.callback = callback;
            this.implInfo = implInfo;
        }

        @Override
        protected File doInBackground(Object... params) {
            File file = new File(fullname);
            if (file.exists()
                    && null != md5 && !TextUtils.isEmpty(md5)
                    && md5.equals(ImplHelper.getFileMD5(file))) {
                return file;
            } else if (file.exists()) {
                file.delete();
            }
            return null;
        }

        @Override
        protected void onPostExecute(File file) {
            super.onPostExecute(file);
            ImplLog.d(TAG, "AddDownloadAsync::onPostExecute," + implInfo.getTitle() + "," + implInfo.getStatus());
            if (null != file) {
                implInfo.setFileSavePath(file.getAbsolutePath());
                implInfo.setLocalPath(file.getAbsolutePath());
                implInfo.setCurrent(file.length());
                implInfo.setTotal(file.length());
                implInfo.setStatus(ImplInfo.STATUS_SUCCESSFUL);
                callback.onSuccess(this.implInfo, file);
            } else {
                implInfo.setAutoRename(true);
                implInfo.setAutoResume(true);
                implInfo.setFileSavePath(fullname);
                implInfo.setLocalPath(null);
                implInfo.setCurrent(0);
                implInfo.setTotal(0);
                HttpUtils http = new HttpUtils();
//                http.configRequestThreadPoolSize(ImplConfig.getDownloadThreadNum(mContext));
                DownloadCallback downloadCallback = new DownloadCallback<File>(implInfo, callback);
                HttpHandler<File> handler = http.download(implInfo.getDownloadUrl(), fullname, true, true, downloadCallback);
                implInfo.setHandler(handler);
                implInfo.setState(handler.getState());
                downloadCallback.onEnqued(0);
            }
        }
    }

//    private class AddDownloadThread extends Thread {
//        private static final int RESULT_MD5_MATCH = 0;
//        private static final int RESULT_ADD_DOWNLOAD = 1;
//        private static final int RESULT_ADD_DOWNLOAD_EXCEPTION = 2;
//
//        private String fullname;
//        private String md5;
//        private ImplListener callback;
//        private ImplInfo implInfo;
//
//        private AddDownloadThread(ImplInfo implInfo,String fullname, String md5, ImplListener callback) {
//            this.fullname = fullname;
//            this.md5 = md5;
//            this.callback = callback;
//            this.implInfo = implInfo;
//        }
//
//        @Override
//        public void run() {
//            super.run();
//            int result = RESULT_ADD_DOWNLOAD;
//            File file = new File(fullname);
//            if (file.exists()
//                    && null != md5 && !TextUtils.isEmpty(md5)
//                    && md5.equals(ImplHelper.getFileMD5(file))){
//                result = RESULT_MD5_MATCH;
//            }else {
//                if (file.exists()){
//                    file.delete();
//                }
//                try {
//                    DownloadInfo downloadInfo = dm.getDownloadInfoById(implInfo.getDownloadId());
//                    if (null != downloadInfo) {
//                        dm.removeDownload(downloadInfo);
//                    }
//                    downloadInfo = dm.addNewDownload(implInfo.getDownloadUrl(),
//                            implInfo.getTitle(),
//                            fullname,
//                            true,
//                            true,
//                            new DownloadCallback<File>(implInfo, callback));
//                    downloadInfo.getHandler().getRequestCallBack().setRate(callback.getRate());
//                } catch (DbException e) {
//                    e.printStackTrace();
//                    result = RESULT_ADD_DOWNLOAD_EXCEPTION;
//                }
//            }
//            ImplLog.d(TAG,"run,"+implInfo.getTitle()+","+implInfo.getStatus()+","+implInfo.getDownloadId()+","+result);
//            switch(result){
//                case RESULT_ADD_DOWNLOAD:
//                    break;
//                case RESULT_ADD_DOWNLOAD_EXCEPTION:
//                    implInfo.setLocalPath(null);
//                    implInfo.setStatus(ImplInfo.STATUS_FAILED);
//                    callback.onFailure(implInfo, null, "add download exception");
//                    break;
//                case RESULT_MD5_MATCH:
//                    implInfo.setLocalPath(file.getAbsolutePath());
//                    implInfo.setStatus(ImplInfo.STATUS_SUCCESSFUL);
//                    callback.onSuccess(this.implInfo,new File(fullname));
//                    break;
//            }
//        }
//    }
//
//    private class AddDownloadTask implements Runnable{
//        private static final int RESULT_MD5_MATCH = 0;
//        private static final int RESULT_ADD_DOWNLOAD = 1;
//        private static final int RESULT_ADD_DOWNLOAD_EXCEPTION = 2;
//
//        private String fullname;
//        private String md5;
//        private ImplListener callback;
//        private ImplInfo implInfo;
//
//        private AddDownloadTask(ImplInfo implInfo,String fullname, String md5, ImplListener callback) {
//            this.fullname = fullname;
//            this.md5 = md5;
//            this.callback = callback;
//            this.implInfo = implInfo;
//        }
//
//        @Override
//        public void run() {
//            int result = RESULT_ADD_DOWNLOAD;
//            File file = new File(fullname);
//            if (file.exists()
//                    && null != md5 && !TextUtils.isEmpty(md5)
//                    && md5.equals(ImplHelper.getFileMD5(file))){
//                result = RESULT_MD5_MATCH;
//            }else {
//                if (file.exists()){
//                    file.delete();
//                }
//                try {
//                    DownloadInfo downloadInfo = dm.getDownloadInfoById(implInfo.getDownloadId());
//                    if (null != downloadInfo) {
//                        dm.removeDownload(downloadInfo);
//                    }
//                    downloadInfo = dm.addNewDownload(implInfo.getDownloadUrl(),
//                            implInfo.getTitle(),
//                            fullname,
//                            true,
//                            true,
//                            new DownloadCallback<File>(implInfo, callback));
//                    downloadInfo.getHandler().getRequestCallBack().setRate(callback.getRate());
//                } catch (DbException e) {
//                    e.printStackTrace();
//                    result = RESULT_ADD_DOWNLOAD_EXCEPTION;
//                }
//            }
//            ImplLog.d(TAG,"run,"+implInfo.getTitle()+","+implInfo.getStatus()+","+implInfo.getDownloadId()+","+result);
//            switch(result){
//                case RESULT_ADD_DOWNLOAD:
//                    break;
//                case RESULT_ADD_DOWNLOAD_EXCEPTION:
//                    implInfo.setLocalPath(null);
//                    implInfo.setStatus(ImplInfo.STATUS_FAILED);
//                    callback.onFailure(implInfo, null, "add download exception");
//                    break;
//                case RESULT_MD5_MATCH:
//                    implInfo.setLocalPath(file.getAbsolutePath());
//                    implInfo.setStatus(ImplInfo.STATUS_SUCCESSFUL);
//                    callback.onSuccess(this.implInfo,new File(fullname));
//                    break;
//            }
//        }
//    }

    void addDownload(ImplInfo implInfo, String fullname, String md5, ImplListener callback) {
        if (null == implInfo.getDownloadUrl() || null == implInfo.getFileSavePath()) {
            if (null != callback) {
                implInfo.setLocalPath(null);
                implInfo.setStatus(ImplInfo.STATUS_FAILED);
                callback.onFailure(implInfo, null, "download url or savepath is null");
            }
            return;
        }
//        ImplAgent.mWorkHandler.post(new AddDownloadTask(implInfo, fullname, md5, callback));
//        new AddDownloadThread(implInfo, fullname, md5, callback).start();
//        EXECUTOR.execute(new AddDownloadTask(implInfo, fullname, md5, callback));
//        new SignatureAsync(implInfo, fullname).execute();
        new AddDownloadAsync(implInfo, fullname, md5, callback).execute();
    }

    void pause(ImplInfo implInfo, ImplListener callback) {
        implInfo.setCause(ImplInfo.CAUSE_PAUSED_BY_APP);
        pauseImpl(implInfo, callback);
    }

    private void pauseImpl(ImplInfo implInfo, ImplListener baseCallback) {
        RequestCallBack<File> downloadCallback = null;
        HttpHandler<File> handler = implInfo.getHandler();
        if (handler != null) {
            if (!handler.isCancelled()) {
                handler.cancel();
            } else {
                downloadCallback = (null != handler.getRequestCallBack()) ? handler.getRequestCallBack() : new DownloadCallback<File>(implInfo, baseCallback);
            }
        } else {
            implInfo.setState(HttpHandler.State.CANCELLED);
            downloadCallback = new DownloadCallback<File>(implInfo, baseCallback);
        }

        if (null != downloadCallback) {
            downloadCallback.onCancelled();
        }
    }

    void resume(ImplInfo implInfo, ImplListener callback) {
        implInfo.setUserContinue(true);
        resumeImpl(implInfo, callback);
    }

    private void resumeImpl(ImplInfo implInfo, ImplListener callback) {
        if (null == implInfo.getDownloadUrl() || null == implInfo.getFileSavePath()) {
            return;
        }

        HttpUtils http = new HttpUtils();
//        http.configRequestThreadPoolSize(ImplConfig.getDownloadThreadNum(mContext));
        DownloadCallback<File> downloadCallback = new DownloadCallback<File>(implInfo, callback);
        if ((implInfo.getDownloadUrl() != null) && (implInfo.getFileSavePath() != null)) {
            HttpHandler<File> handler = http.download(
                    implInfo.getDownloadUrl(),
                    implInfo.getFileSavePath(),
                    implInfo.isAutoResume(),
                    implInfo.isAutoRename(),
                    downloadCallback);
            implInfo.setHandler(handler);
            implInfo.setState(handler.getState());
        }
        downloadCallback.onPending();
    }

    void pauseAll(List<ImplInfo> implList, ImplListener callback) {
        for (ImplInfo implInfo : implList) {
            switch (implInfo.getStatus()) {
                case ImplInfo.STATUS_PENDING:
                case ImplInfo.STATUS_RUNNING:
                    pause(implInfo, callback);
                    break;
            }

//            HttpHandler handler = implInfo.getHandler();
//            if (null != handler && !handler.isCancelled() && !handler.isPaused()
//                && !implInfo.getState().equals(HttpHandler.State.SUCCESS)) {
//                pause(implInfo,callback);
//            }
        }
    }

    void resumeAll(List<ImplInfo> implList, ImplListener callback) {
        for (ImplInfo implInfo : implList) {
            switch (implInfo.getStatus()) {
//                case ImplInfo.STATUS_PENDING:
//                case ImplInfo.STATUS_RUNNING:
                case ImplInfo.STATUS_PAUSED:
                case ImplInfo.STATUS_FAILED:
                    resume(implInfo, callback);
                    break;
            }
        }
    }

    void remove(ImplInfo implInfo) {
        HttpHandler<File> handler = implInfo.getHandler();
        if (handler != null) {
            if (!handler.isCancelled()) {
                handler.cancel();
            } else {
                final RequestCallBack callback = handler.getRequestCallBack();
                if (null != callback) {
                    callback.onCancelled();
                }
            }
        }
    }

//    void stopAllDownload() throws DbException {
//        for (DownloadInfo downloadInfo : downloadInfoList) {
//            HttpHandler<File> handler = downloadInfo.getHandler();
//            if (handler != null) {
//                if (!handler.isCancelled()) {
//                    handler.cancel();
//                }else{
//                    RequestCallBack callback = handler.getRequestCallBack();
//                    if (null != callback){
//                        callback.onCancelled();
//                    }
//                }
//            } else {
//                downloadInfo.setState(HttpHandler.State.CANCELLED);
//            }
//        }
//        db.saveOrUpdateAll(downloadInfoList);
//    }

//    void backupDownloadInfoList() throws DbException {
//        for (DownloadInfo downloadInfo : downloadInfoList) {
//            HttpHandler<File> handler = downloadInfo.getHandler();
//            if (handler != null) {
//                downloadInfo.setState(handler.getState());
//            }
//        }
//        db.saveOrUpdateAll(downloadInfoList);
//    }

    int getMaxDownloadThread() {
        return maxDownloadThread;
    }

    void setMaxDownloadThread(int maxDownloadThread) {
        this.maxDownloadThread = maxDownloadThread;
    }

    boolean needKick(ImplInfo implInfo) {
        boolean ret = false;
        if (null != implInfo && null == implInfo.getHandler()) {
            switch (implInfo.getState()) {
                case WAITING:
                case STARTED:
                case LOADING:
                    ret = true;
                    break;
            }
        }
        return ret;
    }

    void kickDownload(List<ImplInfo> implList, ImplListener implCallback) {
        ImplInfo implInfo = null;
        for (int i = 0; i < implList.size(); i++) {
            implInfo = implList.get(i);
            if (implInfo.getStatus() == ImplInfo.STATUS_PAUSED
                    && implInfo.getCause() == ImplInfo.CAUSE_PAUSED_BY_APP) {
                continue;
            }
            if (needKick(implInfo)) {
                resumeImpl(implInfo, implCallback);
            }
        }
    }

    private boolean overSizePause(ImplInfo implInfo, ImplListener callback) {
        boolean ret = false;
        if ("mobile".equals(ImplReceiver.getNetwork(mContext))
                && implInfo.getTotal() > ImplConfig.getMaxOverSize(mContext)) {
            if (implInfo.isUserContinue()) {
                return ret;
            }

            if (ImplInfo.STATUS_PENDING == implInfo.getStatus()
                    || ImplInfo.STATUS_RUNNING == implInfo.getStatus()
                    || (ImplInfo.STATUS_PAUSED == implInfo.getStatus()
                    && ImplInfo.CAUSE_PAUSED_BY_APP != implInfo.getCause())) {
                implInfo.setStatus(ImplInfo.STATUS_PAUSED);
                implInfo.setCause(ImplInfo.CAUSE_PAUSED_BY_OVERSIZE);
                pauseImpl(implInfo, callback);
                ret = true;
            }
        }
        return ret;
    }

    void onNetworkChanged(List<ImplInfo> implList, ImplListener callback) {
        String network = ImplReceiver.getNetwork(mContext);
        ImplInfo implInfo = null;
        for (int i = 0; i < implList.size(); i++) {
            implInfo = implList.get(i);
            switch (network) {
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
                        resumeImpl(implInfo, callback);
                    }
                    break;
                case "mobile":
                    overSizePause(implInfo, callback);
                    break;
            }
        }
    }

    public class DownloadCallback<File> extends RequestCallBack<File> {
        private ImplInfo implInfo;
        private ImplListener baseCallback;

        DownloadCallback(ImplInfo implInfo, ImplListener callback) {
            this.implInfo = implInfo;
            this.baseCallback = callback;
        }

        public void onEnqued(long downloadId) {
            implInfo.setStatus(ImplInfo.STATUS_PENDING);
            implInfo.setUserContinue(false);
            implInfo.setLastMod(System.currentTimeMillis());
            if (null != baseCallback) {
                baseCallback.onEnqued(implInfo);
            }
            ImplLog.d(TAG, implInfo.getTitle() + ",onEnqued," + implInfo);
        }

        public void onPending() {
            implInfo.setStatus(ImplInfo.STATUS_PENDING);
            if (null != baseCallback) {
                baseCallback.onPending(implInfo);
            }
            ImplLog.d(TAG, implInfo.getTitle() + ",onPending," + implInfo);
        }

        @Override
        public void onLoading(long total, long current, boolean isUploading) {
            super.onLoading(total, current, isUploading);

            HttpHandler<java.io.File> handler = implInfo.getHandler();
            if (handler != null) {
                implInfo.setState(handler.getState());
            }
            implInfo.setTotal(total);
            implInfo.setCurrent(current);
            if (!overSizePause(implInfo, baseCallback)) {
                implInfo.setStatus(ImplInfo.STATUS_RUNNING);
            }

            if (null != baseCallback) {
                baseCallback.onLoading(implInfo, total, current, isUploading);
            }
        }

        @Override
        public void onCancelled() {
            super.onCancelled();
            HttpHandler<java.io.File> handler = implInfo.getHandler();
            if (handler != null
                    && !handler.getState().equals(HttpHandler.State.SUCCESS)
                    && !handler.getState().equals(HttpHandler.State.FAILURE)) {
                implInfo.setState(handler.getState());
            }
            implInfo.setStatus(ImplInfo.STATUS_PAUSED);

            if (null != baseCallback) {
                baseCallback.onCancelled(implInfo);
            }
            ImplLog.d(TAG, implInfo.getTitle() + ",onCancelled," + implInfo);
        }

        @Override
        public void onStart() {
            super.onStart();
            HttpHandler<java.io.File> handler = implInfo.getHandler();
            if (handler != null) {
                implInfo.setState(handler.getState());
            }
            implInfo.setStatus(ImplInfo.STATUS_RUNNING);

            if (null != baseCallback) {
                baseCallback.onStart(implInfo);
            }
            ImplLog.d(TAG, implInfo.getTitle() + ",onStart," + implInfo);
        }

        @Override
        public void onSuccess(ResponseInfo<File> fileResponseInfo) {
            HttpHandler<java.io.File> handler = implInfo.getHandler();
            if (handler != null) {
                implInfo.setState(handler.getState());
            }
            java.io.File file = (java.io.File) fileResponseInfo.result;
            if (null != file && file.exists()) {
                if (null != implInfo.getMd5() && !TextUtils.isEmpty(implInfo.getMd5())) {
                    new MD5Async(implInfo, file.getAbsolutePath(), baseCallback, file).execute();
                } else {
                    implInfo.setLocalPath(file.getAbsolutePath());
                    implInfo.setStatus(ImplInfo.STATUS_SUCCESSFUL);
                    if (null != baseCallback) {
                        baseCallback.onSuccess(implInfo, file);
                    }
                }
            } else {
                implInfo.setLocalPath(file.getAbsolutePath());
                implInfo.setStatus(ImplInfo.STATUS_FAILED);
                if (null != baseCallback) {
                    baseCallback.onFailure(implInfo, null, "download file not exist");
                }
            }
            ImplLog.d(TAG, implInfo.getTitle() + ",onSuccess," + implInfo);
        }

        @Override
        public void onFailure(HttpException e, String s) {
            HttpHandler<java.io.File> handler = implInfo.getHandler();
            if (handler != null) {
                implInfo.setState(handler.getState());
            }
            ImplReceiver.initNetwork(mContext);
            if ("none".equals(ImplReceiver.getNetwork(mContext))) {
                implInfo.setStatus(ImplInfo.STATUS_PAUSED);
                implInfo.setCause(ImplInfo.CAUSE_PAUSED_BY_NETWORK);
                if (null != baseCallback) {
                    baseCallback.onCancelled(implInfo);
                }
            } else {
                implInfo.setStatus(ImplInfo.STATUS_FAILED);
                implInfo.setCause(ImplInfo.CAUSE_NONE);
                if (null != e && null != e.getMessage()) {
                    if (e.getMessage().contains("ENOSPC")) {
                        implInfo.setCause(ImplInfo.CAUSE_FAILED_BY_SPACE_NOT_ENOUGH);
                    }
                }
                if (null != baseCallback) {
                    baseCallback.onFailure(implInfo, e, s);
                }
            }
            ImplLog.d(TAG, "onFailure," + e.getCause() + "," + e.getMessage() + "," + e.getExceptionCode() + "," + e.getClass());
        }
    }

    private class MD5Async extends AsyncTask {
        private String fullname;
        private String md5;
        private ImplInfo implInfo;
        private ImplListener baseCallback;
        private java.io.File file;

        public MD5Async(ImplInfo implInfo, String fullname, ImplListener baseCallback, java.io.File file) {
            ImplLog.d(TAG, "MD5Async");
            this.fullname = fullname;
            this.implInfo = implInfo;
            this.baseCallback = baseCallback;
            this.file = file;
            md5 = implInfo.getMd5();
        }

        @Override
        protected Object doInBackground(Object[] params) {
            File file = new File(fullname);
            if (!md5.equals(ImplHelper.getFileMD5(file))) {
                ImplLog.d(TAG, "MD5Async return false");
                return false;
            }
            ImplLog.d(TAG, "MD5Async return true");
            return true;
        }

        @Override
        protected void onPostExecute(Object o) {
            boolean flag = (boolean) o;
            ImplLog.d(TAG, "MD5Async onPostExecute flag = " + flag);
            if (!flag) {
                implInfo.setStatus(ImplInfo.STATUS_PACKAGE_INVALID);
                implInfo.setLocalPath(fullname);
                if (null != baseCallback) {
                    baseCallback.onFailure(implInfo, null, "download file not exist");
                }
            } else {
                implInfo.setLocalPath(fullname);
                implInfo.setStatus(ImplInfo.STATUS_SUCCESSFUL);
                if (null != baseCallback) {
                    baseCallback.onSuccess(implInfo, file);
                }
            }
            super.onPostExecute(o);
        }
    }

    private class HttpHandlerStateConverter implements ColumnConverter<HttpHandler.State> {

        @Override
        public HttpHandler.State getFieldValue(Cursor cursor, int index) {
            ImplLog.d(TAG, "getFieldValue:" + HttpHandler.State.valueOf(cursor.getInt(index)));
            return HttpHandler.State.valueOf(cursor.getInt(index));
        }

        @Override
        public HttpHandler.State getFieldValue(String fieldStringValue) {
            if (fieldStringValue == null) return null;
            ImplLog.d(TAG, "getFieldValue:" + fieldStringValue);
            return HttpHandler.State.valueOf(fieldStringValue);
        }

        @Override
        public Object fieldValue2ColumnValue(HttpHandler.State fieldValue) {
            if (null == fieldValue) return null;
            return fieldValue.value();
        }

        @Override
        public ColumnDbType getColumnDbType() {
            return ColumnDbType.INTEGER;
        }
    }

//    private class SignatureAsync extends AsyncTask<Void, Void, Void> {
//        private ImplInfo implInfo;
//        private String fullname;
//
//        public SignatureAsync(ImplInfo implInfo, String fullname) {
//            this.implInfo = implInfo;
//            this.fullname = fullname;
//        }
//
//        @Override
//        protected Void doInBackground(Void... params) {
//            String apkSignature = null;
//            String packagepath = null;
//            apkSignature = getApkSignature(implInfo.getPackageName());
//            if (TextUtils.isEmpty(apkSignature)) {
//                return null;
//            }
//            packagepath = implInfo.getFileSavePath();
//            if (!TextUtils.isEmpty(packagepath)) {
//                implInfo.setSignatureEqual(isEqual(getApkSignature(packagepath), apkSignature));
//            } else {
//                implInfo.setSignatureEqual(isEqual(getApkSignature(fullname), apkSignature));
//            }
//            return null;
//        }
//
//        /**
//         * 比对签名
//         */
//        private boolean isEqual(String apkSignature, String packageSignature) {
//            if (null == apkSignature) {
//                return true;
//            }
//            if (null == packageSignature) {
//                return true;
//            }
//            if (apkSignature.equals(packageSignature)) {
//                return true;
//            }
//            return false;
//        }

//        /**
//         * 获取安装包签名
//         *
//         * @param packagePath
//         * @return
//         */
//        private String getPackageSignature(String packagePath) {
//            try {
//                PackageInfo packageInfo = mContext.getPackageManager().getPackageArchiveInfo(
//                        packagePath, PackageManager.GET_SIGNATURES);
//                return packageInfo.signatures[0].toCharsString();
//            } catch (Exception e) {
//                return null;
//            }
//        }

//        /**
//         * 获取已安装应用签名
//         *
//         * @param packageName
//         * @return
//         */
//        public String getApkSignature(String packageName) {
//            try {
//                PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(
//                        packageName, PackageManager.GET_SIGNATURES);
//                return packageInfo.signatures[0].toCharsString();
//            } catch (Exception e) {
//                return null;
//            }
//        }
//
//    }
}
