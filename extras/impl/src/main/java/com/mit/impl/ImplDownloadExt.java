package com.mit.impl;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.SparseArray;
import android.webkit.MimeTypeMap;

import com.android.dsc.downloads.DownloadManager;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;

class ImplDownloadExt extends AbstractImpl{
    private static final String TAG = "impl_download";
    private SparseArray<Method> mCmdList = new SparseArray<Method>();
    private DownloadManager dm;
    private boolean inited = false;
    private static ImplDownloadExt mInstance = null;
    private static synchronized void initInstance(){
        if (null == mInstance ){
            mInstance = new ImplDownloadExt();
        }
    }

    public static ImplDownloadExt getInstance(){
        if (null == mInstance){
            initInstance();
        }
        return mInstance;
    }

    private ImplDownloadExt() {
        try {
            Class<?> cls = this.getClass();
            mCmdList.append(IMPL_ACTION_QUERY.hashCode(),
                    cls.getDeclaredMethod("handleQueryReq",ImplAgent.ImplRequest.class));
            mCmdList.append(IMPL_ACTION_DOWNLOAD.hashCode(),
                    cls.getDeclaredMethod("handleDownloadReq",ImplAgent.ImplRequest.class));
            mCmdList.append(IMPL_ACTION_DOWNLOAD_TOGGLE.hashCode(),
                    cls.getDeclaredMethod("handleDownloadToggle",ImplAgent.ImplRequest.class));
            mCmdList.append(IMPL_ACTION_DOWNLOAD_COMPLETE.hashCode(),
                    cls.getDeclaredMethod("handleDownloadComplete",ImplAgent.ImplRequest.class));
            mCmdList.append(IMPL_ACTION_DOWNLOAD_DELETE.hashCode(),
                    cls.getDeclaredMethod("handleDownloadDelete",ImplAgent.ImplRequest.class));
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void init(Context context) {
//        SQLiteDatabase db = databaseHelper.getWritableDatabase();
//        String sql = "select * from "+ImplConfig.TABLE_IMPL;
//        Cursor c = db.rawQuery(sql,null);
//        try{
//            c.moveToFirst();
//            do {
//                ImplInfo info = ImplInfo.from(context,c);
//                if (null != info){
//                    mDlRunnable.add(context,info);
//                }
//            }while(c.moveToNext());
//        }catch(Exception e){
//            e.printStackTrace();
//        }finally {
//            if (null != c){
//                c.close();
//            }
//        }
        inited = true;
    }

    @Override
    public boolean request(ImplAgent.ImplRequest cmd) {
        super.request(cmd);
        if (null == dm) {
            dm = DownloadManager.getInstance(cmd.context);
        }
        if (!inited){
            init(cmd.context);
        }

        final Method method = mCmdList.get(cmd.action.hashCode());
        if (null == method){
            return false;
        }
        boolean result = true;
        try {
            result = (boolean)method.invoke(this,cmd);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void cancel(ImplAgent.ImplRequest cmd) {
        super.cancel(cmd);
    }

    private boolean existInDownloadDb(long id){
        boolean ret = false;
        if (id >0 ){
            Cursor c = dm.query(new DownloadManager.Query().setFilterById(id));
            try{
                if (null != c && c.moveToFirst()){
                    ret = true;
                }
            }catch(Exception e){
                ret = false;
                e.printStackTrace();
            }finally{
                if (null != c){
                    c.close();
                }
            }
        }
        return ret;
    }

    private boolean handleQueryReq(ImplAgent.ImplRequest cmd){
        ImplAgent.DownloadQueryReq implCmd = (ImplAgent.DownloadQueryReq)cmd;
        ImplLog.d(TAG,"handleQueryReq,"+implCmd.keys);
        List<ImplInfo> infoList = findInfoByKeyBatch(implCmd.keys);
        for (ImplInfo info:infoList){
            ImplAgent.notify(true,info);
            syncStatus(info);
        }
        return true;
    }

    private boolean handleDownloadReq(ImplAgent.ImplRequest cmd) {
        // TODO Auto-generated method stub
        ImplAgent.DownloadPackageReq implCmd = (ImplAgent.DownloadPackageReq)cmd;
        ImplLog.d(TAG,"handleDownloadReq,"+implCmd.key);

        ImplInfo info = findInfoByKey(implCmd.key);
        if (null != info && existInDownloadDb(info.getDownloadId())){
            toggleDownload(cmd.context, info);
        }else{
            try{
                DownloadManager.Request request=new DownloadManager.Request(Uri.parse(implCmd.url));
                request.setAllowedNetworkTypes(implCmd.networkFlag);
                request.setAllowedOverRoaming(implCmd.roming);
                MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
                String mimeString = mimeTypeMap.getMimeTypeFromExtension(
                                    MimeTypeMap.getFileExtensionFromUrl(implCmd.filename));
                request.setMimeType(mimeString);
                if (implCmd.visible) {
                    request.setShowRunningNotification(true);
                    request.setVisibleInDownloadsUi(true);
                }else{
                    request.setShowRunningNotification(false);
                    request.setVisibleInDownloadsUi(false);
                }
                request.setDestinationInExternalPublicDir(implCmd.publicDir, implCmd.filename);
                request.setTitle(implCmd.title);
                request.setDescription(implCmd.desc);
                long id = dm.enqueue(request);

                info = ImplInfo.create(implCmd.context,implCmd.key,implCmd.url,implCmd.packageName);
                info.setDownloadId(id)
                        .setIconUrl(implCmd.iconUrl)
                        .setIconPath(implCmd.iconDir)
                        .setTitle(implCmd.title)
                        .setDescription(implCmd.desc);
                save(info);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        if (null != info){
            syncStatus(info);
        }
        return true;
    }

    private boolean handleDownloadComplete(ImplAgent.ImplRequest cmd) {
        ImplAgent.DownloadCompleteReq implCmd = (ImplAgent.DownloadCompleteReq)cmd;
        final long id = implCmd.intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
        if (id <= 0){
            return false;
        }
        ImplInfo info = findInfoByDownloadId(id);
        if (null == info){
            return false;
        }

        ImplLog.d(TAG,"handleDownloadComplete,"+info.getDownloadId()+","+info.getKey());
        Cursor c = dm.query(new DownloadManager.Query().setFilterById(id));
        String localUri = null;
        try{
            if(null != c && c.moveToFirst()) {
                info.setLocalPath(c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)))
                        .setStatus(c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS)))
                        .setReason(c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON)))
                        .setCurrentBytes(c.getLong(c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)))
                        .setTotalBytes(c.getLong(c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)))
                        .setMimeType(c.getString(c.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE)));
                save(info);
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if (null != c ){
                c.close();
            }
        }
        syncStatus(info);
//        ImplAgent.notify(true,info);
//        mHandler.removeMessages(info.getKey().hashCode(),info);
        return true;
    }
    
    private boolean handleDownloadDelete(ImplAgent.ImplRequest cmd){
        ImplAgent.DeleteDownloadReq implCmd = (ImplAgent.DeleteDownloadReq)cmd;
        ImplInfo info = findInfoByKey(implCmd.key);
        if (null == info || 0 == info.getDownloadId()){
            return false;
        }
        ImplLog.d(TAG,"handleDownloadDelete,"+info.getTitle());
        try{
            dm.remove(info.getDownloadId());
        }catch(Exception e){}
        mHandler.removeMessages(info.getKey().hashCode(), info);
        remove(implCmd.key);
//        ImplAgent.notify(true,info);
        return true;
    }

    private boolean handleDownloadToggle(ImplAgent.ImplRequest cmd){
        ImplAgent.ToggleDownloadReq implCmd = (ImplAgent.ToggleDownloadReq)cmd;
        long id = 0;
        ImplInfo info = findInfoByKey(implCmd.key);
        if (null != info){
            ImplLog.d(TAG,"handleDownloadToggle,"+info.getTitle());
            id = info.getDownloadId();
            if (existInDownloadDb(id)){
                toggleDownload(cmd.context,info);
            }
        }
        return true;
    }

    private void toggleDownload(Context context,ImplInfo info){
        long id = info.getDownloadId();
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(id);
        Cursor c = dm.query(query);
        try {
            boolean updateStop = false;
            if (null != c && c.moveToFirst()){
                int downloadStatus = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                switch(downloadStatus){
                    case DownloadManager.STATUS_PENDING:
                    case DownloadManager.STATUS_RUNNING:
                        dm.pauseDownload(id);
                        break;

                    case DownloadManager.STATUS_PAUSED:
                        int reason = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON));
                        if (DownloadManager.PAUSED_BY_APP == reason){
                            dm.resumeDownload(id);
                        }else if (reason == DownloadManager.PAUSED_QUEUED_FOR_WIFI){
                            //mobile network size limit,waiting for wifi network
                            //maybe toast something
                        }else {
                            dm.pauseDownload(id);
                        }
                        break;

                    case DownloadManager.STATUS_FAILED:
                        reason = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON));
                        if (DownloadManager.ERROR_FILE_ALREADY_EXISTS == reason){
                            String uri = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                            new File(Uri.parse(uri).getPath()).delete();
                        }
                        dm.restartDownload(id);
                        break;

                    case DownloadManager.STATUS_SUCCESSFUL:
                        String localUri = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                        String localPath = Uri.parse(localUri).getPath();
                        int status = DownloadManager.STATUS_SUCCESSFUL;
                        if (!new File(localPath).exists()){
                            dm.restartDownload(id);
                            status = DownloadManager.STATUS_FAILED;
                        }else{
                            updateStop = true;
                        }

                        info.setLocalPath(localUri)
                                .setStatus(status)
                                .setReason(c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON)))
                                .setCurrentBytes(c.getLong(c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)))
                                .setTotalBytes(c.getLong(c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)))
                                .setMimeType(c.getString(c.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE)));
                        ImplAgent.notify(true,info);
                        save(info);
                        break;
                }
            }

            if (!updateStop){
                if (!mHandler.hasMessages(info.getKey().hashCode(),info)){
                    mHandler.sendMessage(mHandler.obtainMessage(info.getKey().hashCode(),info));
                }
            }else{
                mHandler.removeMessages(info.getKey().hashCode(),info);
            }
        }catch(Exception e){
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    public Handler mHandler = new Handler(ImplAgent.mWorkHandler.getLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ImplInfo info = (ImplInfo)msg.obj;
            if(null != info && info.getKey().hashCode() == msg.what){
                boolean isContinue = true;
                DownloadManager.Query query = new DownloadManager.Query().setFilterById(info.getDownloadId());
                Cursor c = dm.query(query);
                try {
                    if (null != c && c.moveToFirst()){
                        int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                        switch(status){
                            case DownloadManager.STATUS_PAUSED:
                                int reason = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON));
                                if (reason != DownloadManager.PAUSED_QUEUED_FOR_WIFI
                                        && reason != DownloadManager.PAUSED_BY_APP){
                                    status = DownloadManager.STATUS_RUNNING;
                                }else{
                                    status = DownloadManager.STATUS_PAUSED;
                                }
                                break;
                            case DownloadManager.STATUS_SUCCESSFUL:
                            case DownloadManager.STATUS_FAILED:
                                isContinue = false;
                                break;
                        }
                        if (info.getStatus()<=DownloadManager.STATUS_FAILED) {
                            info.setStatus(status);
                        }
                        info.setLocalPath(c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)))
                                .setTotalBytes(c.getInt(c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)))
                                .setCurrentBytes(c.getInt(c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)))
                                .setReason(c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON)))
                                .setMimeType(c.getString(c.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE)));
                        save(info);
                        ImplAgent.notify(true,info);
                    }
                }catch(Exception e){
                    e.printStackTrace();
                    isContinue = false;
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }

                if (isContinue){
                    this.sendMessageDelayed(this.obtainMessage(info.getKey().hashCode(), info), 500);
                }
            }
        }
    };

    private void syncStatus(ImplInfo info){
        if (!mHandler.hasMessages(info.getKey().hashCode(),info)) {
            mHandler.sendMessage(mHandler.obtainMessage(info.getKey().hashCode(),info));
        }
    }
}
