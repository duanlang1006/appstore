//package com.mit.impl.download;
//
//import android.content.Context;
//import android.database.Cursor;
//import android.os.Handler;
//import android.os.Looper;
//
//import com.lidroid.xutils.DbUtils;
//import com.lidroid.xutils.HttpUtils;
//import com.lidroid.xutils.db.converter.ColumnConverter;
//import com.lidroid.xutils.db.converter.ColumnConverterFactory;
//import com.lidroid.xutils.db.sqlite.ColumnDbType;
//import com.lidroid.xutils.db.sqlite.Selector;
//import com.lidroid.xutils.exception.DbException;
//import com.lidroid.xutils.exception.HttpException;
//import com.lidroid.xutils.http.HttpHandler;
//import com.lidroid.xutils.http.ResponseInfo;
//import com.lidroid.xutils.http.callback.RequestCallBack;
//import com.lidroid.xutils.util.LogUtils;
//import com.mit.impl.ImplDbHelper;
//import com.mit.impl.ImplDownload;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.List;
//
///**
//* Author: wyouflf
//* Date: 13-11-10
//* Time: 下午8:10
//*/
//public class DownloadManager {
//
//    private List<DownloadInfo> downloadInfoList;
//
//    private int maxDownloadThread = 3;
//
//    private Context mContext;
//    private DbUtils db;
//
//    /*package*/ DownloadManager(Context appContext) {
//        ColumnConverterFactory.registerColumnConverter(HttpHandler.State.class, new HttpHandlerStateConverter());
//        mContext = appContext;
//        db = ImplDbHelper.getDbUtils(mContext.getApplicationContext());
//        try {
//            downloadInfoList = db.findAll(Selector.from(DownloadInfo.class));
//        } catch (DbException e) {
//            LogUtils.e(e.getMessage(), e);
//        }
//        if (downloadInfoList == null) {
//            downloadInfoList = new ArrayList<DownloadInfo>();
//        }
//    }
//
//    public int getDownloadInfoListCount() {
//        return downloadInfoList.size();
//    }
//
//    public DownloadInfo getDownloadInfo(int index) {
//        return downloadInfoList.get(index);
//    }
//
//    public DownloadInfo getDownloadInfoById(long id) {
//        for (DownloadInfo info:downloadInfoList){
//            if (info.getId() == id){
//                return info;
//            }
//        }
//        return null;
//    }
//
//    public DownloadInfo getDownloadInfo(String url) {
//        for (DownloadInfo info:downloadInfoList){
//            if (info.getDownloadUrl().equals(url)){
//                return info;
//            }
//        }
//        return null;
//    }
//
//
//    public DownloadInfo addNewDownload(String url, String fileName, String target,
//                               boolean autoResume, boolean autoRename,
//                               final ImplDownload.DownloadCallback<File> callback) throws DbException {
//        final DownloadInfo downloadInfo = new DownloadInfo();
//        downloadInfo.setDownloadUrl(url);
//        downloadInfo.setAutoRename(autoRename);
//        downloadInfo.setAutoResume(autoResume);
//        downloadInfo.setFileName(fileName);
//        downloadInfo.setFileSavePath(target);
//        HttpUtils http = new HttpUtils();
//        http.configRequestThreadPoolSize(maxDownloadThread);
//        HttpHandler<File> handler = http.download(url, target, autoResume, autoRename, new ManagerCallBack(downloadInfo, callback));
//        downloadInfo.setHandler(handler);
//        downloadInfo.setState(handler.getState());
//        downloadInfoList.add(downloadInfo);
//        db.saveBindingId(downloadInfo);
//
//        if (null != callback) {
//            callback.onEnqued(downloadInfo.getId());
//        }
//        return downloadInfo;
//    }
//
//    public void resumeDownload(int index, final ImplDownload.DownloadCallback<File> callback) throws DbException {
//        final DownloadInfo downloadInfo = downloadInfoList.get(index);
//        resumeDownload(downloadInfo, callback);
//    }
//
//    public void resumeDownload(DownloadInfo downloadInfo, final ImplDownload.DownloadCallback<File> callback) throws DbException {
//        HttpUtils http = new HttpUtils();
//        http.configRequestThreadPoolSize(maxDownloadThread);
//        HttpHandler<File> handler = http.download(
//                downloadInfo.getDownloadUrl(),
//                downloadInfo.getFileSavePath(),
//                downloadInfo.isAutoResume(),
//                downloadInfo.isAutoRename(),
//                new ManagerCallBack(downloadInfo, callback));
//        downloadInfo.setHandler(handler);
//        downloadInfo.setState(handler.getState());
//        db.saveOrUpdate(downloadInfo);
//
//        if (null != callback){
//            callback.onPending();
//        }
//    }
//
//    public void removeDownload(int index) throws DbException {
//        DownloadInfo downloadInfo = downloadInfoList.get(index);
//        removeDownload(downloadInfo);
//    }
//
//    public void removeDownload(DownloadInfo downloadInfo) throws DbException {
//        HttpHandler<File> handler = downloadInfo.getHandler();
//        if (handler != null) {
//            if (!handler.isCancelled()) {
//                handler.cancel();
//            }else{
//                final RequestCallBack callback = handler.getRequestCallBack();
//                if (null != callback){
//                    callback.onCancelled();
//                }
//            }
//        }
//        downloadInfoList.remove(downloadInfo);
//        db.delete(downloadInfo);
//    }
//
//    public void stopDownload(int index,final RequestCallBack<File> baseCallback) throws DbException {
//        DownloadInfo downloadInfo = downloadInfoList.get(index);
//        stopDownload(downloadInfo,baseCallback);
//    }
//
//    public void stopDownload(DownloadInfo downloadInfo,final RequestCallBack<File> baseCallback) throws DbException {
//        HttpHandler<File> handler = downloadInfo.getHandler();
//        RequestCallBack<File> callbackImpl = null;
//        if (handler != null) {
//            if (!handler.isCancelled()) {
//                handler.cancel();
//            }else{
//                RequestCallBack callback = handler.getRequestCallBack();
//                callbackImpl = (null != callback)?callback:baseCallback;
//            }
//        } else {
//            downloadInfo.setState(HttpHandler.State.CANCELLED);
//            callbackImpl = baseCallback;
//        }
//        db.saveOrUpdate(downloadInfo);
//
//        if (null != callbackImpl) {
//            callbackImpl.onCancelled();
//        }
//    }
//
//    public void stopAllDownload() throws DbException {
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
//
//    public void backupDownloadInfoList() throws DbException {
//        for (DownloadInfo downloadInfo : downloadInfoList) {
//            HttpHandler<File> handler = downloadInfo.getHandler();
//            if (handler != null) {
//                downloadInfo.setState(handler.getState());
//            }
//        }
//        db.saveOrUpdateAll(downloadInfoList);
//    }
//
//    public int getMaxDownloadThread() {
//        return maxDownloadThread;
//    }
//
//    public void setMaxDownloadThread(int maxDownloadThread) {
//        this.maxDownloadThread = maxDownloadThread;
//    }
//
//    public class ManagerCallBack extends RequestCallBack<File> {
//        private DownloadInfo downloadInfo;
//        private RequestCallBack<File> baseCallBack;
//
//        public RequestCallBack<File> getBaseCallBack() {
//            return baseCallBack;
//        }
//
//        public void setBaseCallBack(RequestCallBack<File> baseCallBack) {
//            this.baseCallBack = baseCallBack;
//        }
//
//        private ManagerCallBack(DownloadInfo downloadInfo, RequestCallBack<File> baseCallBack) {
//            this.baseCallBack = baseCallBack;
//            this.downloadInfo = downloadInfo;
//        }
//
//        @Override
//        public Object getUserTag() {
//            if (baseCallBack == null) return null;
//            return baseCallBack.getUserTag();
//        }
//
//        @Override
//        public void setUserTag(Object userTag) {
//            if (baseCallBack == null) return;
//            baseCallBack.setUserTag(userTag);
//        }
//
//        @Override
//        public void onStart() {
//            HttpHandler<File> handler = downloadInfo.getHandler();
//            if (handler != null) {
//                downloadInfo.setState(handler.getState());
//            }
//            try {
//                db.saveOrUpdate(downloadInfo);
//            } catch (DbException e) {
//                LogUtils.e(e.getMessage(), e);
//            }
//            if (baseCallBack != null) {
//                baseCallBack.onStart();
//            }
//        }
//
//        @Override
//        public void onCancelled() {
//            HttpHandler<File> handler = downloadInfo.getHandler();
//            if (handler != null) {
//                downloadInfo.setState(handler.getState());
//            }
//            try {
//                db.saveOrUpdate(downloadInfo);
//            } catch (DbException e) {
//                LogUtils.e(e.getMessage(), e);
//            }
//            if (baseCallBack != null) {
//                baseCallBack.onCancelled();
//            }
//        }
//
//        @Override
//        public void onLoading(long total, long current, boolean isUploading) {
//            HttpHandler<File> handler = downloadInfo.getHandler();
//            if (handler != null) {
//                downloadInfo.setState(handler.getState());
//            }
//            downloadInfo.setFileLength(total);
//            downloadInfo.setProgress(current);
//            try {
//                db.saveOrUpdate(downloadInfo);
//            } catch (DbException e) {
//                LogUtils.e(e.getMessage(), e);
//            }
//            if (baseCallBack != null) {
//                baseCallBack.onLoading(total, current, isUploading);
//            }
//        }
//
//        @Override
//        public void onSuccess(ResponseInfo<File> responseInfo) {
//            HttpHandler<File> handler = downloadInfo.getHandler();
//            if (handler != null) {
//                downloadInfo.setState(handler.getState());
//            }
//            try {
//                db.saveOrUpdate(downloadInfo);
//            } catch (DbException e) {
//                LogUtils.e(e.getMessage(), e);
//            }
//            if (baseCallBack != null) {
//                baseCallBack.onSuccess(responseInfo);
//            }
//        }
//
//        @Override
//        public void onFailure(HttpException error, String msg) {
//            HttpHandler<File> handler = downloadInfo.getHandler();
//            if (handler != null) {
//                downloadInfo.setState(handler.getState());
//            }
//            try {
//                db.saveOrUpdate(downloadInfo);
//            } catch (DbException e) {
//                LogUtils.e(e.getMessage(), e);
//            }
//            if (baseCallBack != null) {
//                baseCallBack.onFailure(error, msg);
//            }
//        }
//    }
//
//    private class HttpHandlerStateConverter implements ColumnConverter<HttpHandler.State> {
//
//        @Override
//        public HttpHandler.State getFieldValue(Cursor cursor, int index) {
//            return HttpHandler.State.valueOf(cursor.getInt(index));
//        }
//
//        @Override
//        public HttpHandler.State getFieldValue(String fieldStringValue) {
//            if (fieldStringValue == null) return null;
//            return HttpHandler.State.valueOf(fieldStringValue);
//        }
//
//        @Override
//        public Object fieldValue2ColumnValue(HttpHandler.State fieldValue) {
//            return fieldValue.value();
//        }
//
//        @Override
//        public ColumnDbType getColumnDbType() {
//            return ColumnDbType.INTEGER;
//        }
//    }
//}
