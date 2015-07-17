//package com.mit.impl;
//
//import android.content.Context;
//import android.database.Cursor;
//import android.database.sqlite.SQLiteDatabase;
//import android.net.Uri;
//import android.util.SparseArray;
//import com.android.dsc.downloads.DownloadManager;
//import com.applite.common.Constant;
//
//import net.tsz.afinal.FinalHttp;
//import net.tsz.afinal.http.AjaxCallBack;
//import net.tsz.afinal.http.HttpHandler;
//import java.io.File;
//import java.lang.reflect.Method;
//import java.util.List;
//
//public class ImplDownloadSimple extends AbstractImpl{
//    private static final String TAG = "impl_downloadsimple";
//    private SparseArray<Method> mCmdList = new SparseArray<Method>();
//    private boolean inited = false;
//    private FinalHttp finalHttp ;
//    private SparseArray<HttpHandler<File>> mHttpHandlerList = new SparseArray<HttpHandler<File>>();
//    private static ImplDownloadSimple mInstance = null;
//    private static synchronized void initInstance(){
//        if (null == mInstance ){
//            mInstance = new ImplDownloadSimple();
//        }
//    }
//
//    public static ImplDownloadSimple getInstance(){
//        if (null == mInstance){
//            initInstance();
//        }
//        return mInstance;
//    }
//
//    private ImplDownloadSimple() {
//        try {
//            Class<?> cls = this.getClass();
//            mCmdList.append(IMPL_ACTION_QUERY.hashCode(),
//                    cls.getDeclaredMethod("handleQueryReq",ImplAgent.ImplRequest.class));
//            mCmdList.append(IMPL_ACTION_DOWNLOAD.hashCode(),
//                    cls.getDeclaredMethod("handleDownloadReq",ImplAgent.ImplRequest.class));
//            mCmdList.append(IMPL_ACTION_DOWNLOAD_TOGGLE.hashCode(),
//                    cls.getDeclaredMethod("handleDownloadToggle",ImplAgent.ImplRequest.class));
//            mCmdList.append(IMPL_ACTION_DOWNLOAD_COMPLETE.hashCode(),
//                    cls.getDeclaredMethod("handleDownloadComplete",ImplAgent.ImplRequest.class));
//            mCmdList.append(IMPL_ACTION_DOWNLOAD_DELETE.hashCode(),
//                    cls.getDeclaredMethod("handleDownloadDelete",ImplAgent.ImplRequest.class));
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//    }
//
//    private void init(Context context) {
////        Map<String,String> map = (Map<String,String>)sp.getAll();
////        for (String key : map.keySet()){
////            String json = map.get(key);
////            if (null != json && json.length()>0) {
////                ImplInfo info = gson.fromJson(json,ImplInfo.class);
////                if (null != info){
////                    mDlRunnable.add(context,info);
////                }
////            }
////        }
//        finalHttp = new FinalHttp();
//        finalHttp.configTimeout(30*1000);
//        finalHttp.configUserAgent("Android Impl");
//        inited = true;
//    }
//
//    @Override
//    public boolean request(ImplAgent.ImplRequest cmd) {
//        super.request(cmd);
//        if (!inited){
//            init(cmd.context);
//        }
//
//        final Method method = mCmdList.get(cmd.action.hashCode());
//        if (null == method){
//            return false;
//        }
//        boolean result = true;
//        try {
//            result = (boolean)method.invoke(this,cmd);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return result;
//    }
//
//    @Override
//    public void cancel(ImplAgent.ImplRequest cmd) {
//        super.cancel(cmd);
//    }
//
//
//    private boolean handleQueryReq(ImplAgent.ImplRequest cmd){
//        ImplAgent.DownloadUpdateReq implCmd = (ImplAgent.DownloadUpdateReq)cmd;
//        ImplLog.d(TAG,"handleQueryReq,"+implCmd.keys);
//        List<ImplInfo> infoList = ImplConfig.findInfoByKeyBatch(databaseHelper,implCmd.keys);
//
//        return true;
//    }
//
//    private boolean handleDownloadReq(ImplAgent.ImplRequest cmd) {
//        // TODO Auto-generated method stub
//        ImplAgent.DownloadPackageReq implCmd = (ImplAgent.DownloadPackageReq)cmd;
//        HttpHandler httpHandler = mHttpHandlerList.get(implCmd.key.hashCode());
//        if (null != httpHandler && !httpHandler.isStop()){
//            httpHandler.stop();
//        }else{
//
//            ImplInfo info = new ImplInfo(implCmd.key,
//                    implCmd.url, 0,
//                    implCmd.packageName,
//                    implCmd.iconDir,implCmd.iconUrl,
//                    implCmd.title,implCmd.desc);
//            save(info);
//            httpHandler = finalHttp.download(implCmd.url,
//                    "/sdcard/"+implCmd.publicDir + implCmd.filename,
//                    true,
//                    new HttpCallback<File>(implCmd.context,info));
//            mHttpHandlerList.put(implCmd.key.hashCode(), httpHandler);
//
//        }
//        return true;
//    }
//
//    private boolean handleDownloadToggle(ImplAgent.ImplRequest cmd){
//        ImplAgent.ToggleDownloadReq implCmd = (ImplAgent.ToggleDownloadReq)cmd;
//        ImplInfo info = ImplConfig.findInfoByKey(databaseHelper,implCmd.key);
//        if (null != info){
//            HttpHandler httpHandler = mHttpHandlerList.get(implCmd.key.hashCode());
//            if (null != httpHandler && !httpHandler.isStop()){
//                httpHandler.stop();
//                info.setStatus(Constant.STATUS_PENDING);
//                save(info);
//                mHttpHandlerList.put(implCmd.key.hashCode(),null);
//            }else{
//                httpHandler = finalHttp.download(info.getDownloadUrl(),
//                        "/sdcard/.android/" + info.getTitle()+".apk",
//                        true,
//                        new HttpCallback<File>(implCmd.context,info));
//                mHttpHandlerList.put(implCmd.key.hashCode(), httpHandler);
//            }
//        }
//        return true;
//    }
//
//    private boolean handleDownloadDelete(ImplAgent.ImplRequest cmd){
//        ImplAgent.DeleteDownloadReq implCmd = (ImplAgent.DeleteDownloadReq)cmd;
//        long id = 0;
//        ImplInfo info = ImplConfig.findInfoByKey(databaseHelper,implCmd.key);
//        if (null != info){
//            id = info.getDownloadId();
//        }
//        if (id == 0){
//            return false;
//        }
//        ImplLog.d(TAG, "handleDownloadDelete," + id + "," + info.getKey() + "," + info.getTitle());
//        remove(implCmd.key);
//        ImplAgent.notify(true,
//                new ImplAgent.DeleteDownloadRsp(cmd.context,info.getKey(),true));
//        return true;
//    }
//
//
//
//    class HttpCallback<T> extends AjaxCallBack<T> {
//        private Context mContext;
//        private ImplInfo mInfo;
//
//        HttpCallback(Context context,ImplInfo info) {
//            this.mContext = context;
//            mInfo = info;
//        }
//
//        @Override
//        public boolean isProgress() {
//            return super.isProgress();
//        }
//
//        @Override
//        public AjaxCallBack<T> progress(boolean progress, int rate) {
//            return super.progress(progress, rate);
//        }
//
//        @Override
//        public int getRate() {
//            return super.getRate();
//        }
//
//        @Override
//        public void onStart() {
//            super.onStart();
//            mInfo.setStatus(Constant.STATUS_PENDING);
//            save(mInfo);
//            ImplAgent.notify(true,
//                    new ImplAgent.DownloadUpdateRsp(mContext,mInfo.getKey(), Constant.STATUS_PENDING, 0));
//        }
//
//        @Override
//        public void onLoading(long count, long current) {
//            super.onLoading(count, current);
//            mInfo.setStatus(Constant.STATUS_RUNNING);
//            mInfo.setCurrentBytes(current);
//            mInfo.setTotalBytes(count);
//            mInfo.setProgress((count>0)? (int) (current / count) :0);
//            save(mInfo);
//            ImplAgent.notify(true,
//                    new ImplAgent.DownloadUpdateRsp(mContext,mInfo.getKey(), mInfo.getStatus(), mInfo.getProgress()));
//        }
//
//        @Override
//        public void onSuccess(T t) {
//            super.onSuccess(t);
//            File file = (File)t;
//            mInfo.setStatus(Constant.STATUS_SUCCESSFUL);
//            mInfo.setProgress(100);
//            mInfo.setLocalPath(Uri.fromFile(file).toString());
//            save(mInfo);
//            ImplAgent.notify(true,
//                    new ImplAgent.DownloadCompleteRsp(mContext,mInfo.getKey(), mInfo.getStatus(),mInfo.getLocalPath(),mInfo.getPackageName()));
//        }
//
//        @Override
//        public void onFailure(Throwable t, int errorNo, String strMsg) {
//            super.onFailure(t, errorNo, strMsg);
//            ImplLog.d(TAG,"onFailure,"+strMsg);
//            mInfo.setStatus(Constant.STATUS_FAILED);
//            save(mInfo);
//            ImplAgent.notify(true,
//                    new ImplAgent.DownloadUpdateRsp(mContext,mInfo.getKey(), Constant.STATUS_FAILED, 0));
//        }
//    }
//}
