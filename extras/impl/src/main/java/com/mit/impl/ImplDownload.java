package com.mit.impl;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.SparseArray;
import android.webkit.MimeTypeMap;
import com.android.dsc.downloads.DownloadManager;
import com.applite.common.Constant;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class ImplDownload extends AbstractImpl{
    private static final String TAG = "impl_download";
    private SparseArray<Method> mCmdList = new SparseArray<Method>();
    private RefreshTask refreshTask;
    private DownloadManager dm;
    private boolean inited = false;
    private static ImplDownload mInstance = null;
    private static synchronized void initInstance(){
        if (null == mInstance ){
            mInstance = new ImplDownload();
        }
    }

    public static ImplDownload getInstance(){
        if (null == mInstance){
            initInstance();
        }
        return mInstance;
    }

    private ImplDownload() {
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
//                    refreshTask.add(context,info);
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
        if (null == refreshTask) {
            refreshTask = new RefreshTask();
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
        ImplLog.d(TAG,"handleQueryReq,"+implCmd.keys[0]);
        ImplInfo info = null;
        for (String key : implCmd.keys) {
            info = refreshTask.getCache(key.hashCode());
            if (null == info){
                info = findInfoByKey(key);
                if (null != info && info.getStatus() < Constant.STATUS_PACKAGE_INVALID) {
                    refreshTask.addCache(info);
                    ImplAgent.notify(true, info);
                }
            }
        }
        return false;
    }

    private boolean handleDownloadReq(ImplAgent.ImplRequest cmd) {
        // TODO Auto-generated method stub
        ImplAgent.DownloadPackageReq implCmd = (ImplAgent.DownloadPackageReq)cmd;
        ImplLog.d(TAG,"handleDownloadReq,"+implCmd.key+","+implCmd.title);

        ImplInfo info = findInfoByKey(implCmd.key);
        if (null != info && existInDownloadDb(info.getDownloadId())){
            refreshTask.addCache(info);
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

                info = ImplInfo.create(implCmd.context,implCmd.key,implCmd.url,implCmd.packageName,implCmd.versionCode);
                info.setDownloadId(id)
                        .setIconUrl(implCmd.iconUrl)
                        .setIconPath(implCmd.iconDir)
                        .setTitle(implCmd.title)
                        .setDescription(implCmd.desc);
                save(info);

                refreshTask.addCache(info);
            }catch(Exception e){
                e.printStackTrace();
            }
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
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(id);
        Cursor c = dm.query(query);
        String localUri = null;
        try{
            if(null != c && c.moveToFirst()) {
                info.setLocalPath(c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)))
                        .setStatus(c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS)))
                        .setReason(c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON)))
                        .setCurrentBytes(c.getLong(c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)))
                        .setTotalBytes(c.getLong(c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)))
                        .setMimeType(c.getString(c.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE)));
                update(info);
                refreshTask.addCache(info);
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if (null != c ){
                c.close();
            }
        }
        return true;
    }

    private boolean handleDownloadDelete(ImplAgent.ImplRequest cmd){
        ImplAgent.DeleteDownloadReq implCmd = (ImplAgent.DeleteDownloadReq)cmd;
        long id = 0;
        ImplInfo info = findInfoByKey(implCmd.key);
        if (null == info){
            return false;
        }
        ImplLog.d(TAG,"handleDownloadDelete,"+id+","+info.getKey()+","+info.getTitle());
        try{
            if (info.getDownloadId()>0) {
                dm.remove(id);
            }
            new File(Uri.parse(info.getLocalPath()).getPath()).delete();
        }catch(Exception e){}
        refreshTask.removeCache(implCmd.key.hashCode());
        remove(implCmd.key);
        ImplAgent.notify(true,info);
        return true;
    }

    private boolean handleDownloadToggle(ImplAgent.ImplRequest cmd){
        ImplAgent.ToggleDownloadReq implCmd = (ImplAgent.ToggleDownloadReq)cmd;
        ImplInfo info = findInfoByKey(implCmd.key);
        if (null != info /*&& existInDownloadDb(info.getDownloadId())*/){
            ImplLog.d(TAG,"handleDownloadToggle,"+info.getKey()+","+info.getTitle());
            refreshTask.addCache(info);
            toggleDownload(cmd.context,info);
        }
        return true;
    }

    private void toggleDownload(Context context,ImplInfo info){
        long id = info.getDownloadId();
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(id);
        Cursor c = dm.query(query);
        try {
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
                        }
                        info.setLocalPath(localUri)
                                .setStatus(status)
                                .setReason(c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON)))
                                .setCurrentBytes(c.getLong(c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)))
                                .setTotalBytes(c.getLong(c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)))
                                .setMimeType(c.getString(c.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE)));
                        save(info);
                        refreshTask.addCache(info);
                        break;
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    class RefreshTask implements Runnable{
        private SparseArray<ImplInfo> mRefreshCache;

        public RefreshTask() {
            mRefreshCache = new SparseArray<ImplInfo>();
        }

        public synchronized void addCache(ImplInfo info){
            if (null == info || info.getDownloadId() <=0){
                return;
            }

            ImplInfo cacheInfo = mRefreshCache.get(info.getKey().hashCode());
            if (null == cacheInfo){
                mRefreshCache.put(info.getKey().hashCode(), info);
                ImplAgent.mWorkHandler.removeCallbacks(this);
                ImplAgent.mWorkHandler.postDelayed(this, 50);
            }else{
                if (cacheInfo.getDownloadId() != info.getDownloadId() ){
                    mRefreshCache.put(info.getKey().hashCode(), info);
                    ImplAgent.mWorkHandler.removeCallbacks(this);
                    ImplAgent.mWorkHandler.postDelayed(this, 50);
                }
            }
        }

        public synchronized void removeCache(int key){
            mRefreshCache.remove(key);
        }

        public synchronized ImplInfo getCache(int key){
            return mRefreshCache.get(key);
        }

        @Override
        public synchronized void run() {
            ImplAgent.mWorkHandler.removeCallbacks(this);
            if (mRefreshCache.size()<=0){
                return;
            }
            ImplInfo cachedInfo = null;
            long[] ids = new long[mRefreshCache.size()];
            Set<Long> removedIds = new HashSet<Long>();
            for (int i =0;i<mRefreshCache.size();i++){
                ids[i] = mRefreshCache.valueAt(i).getDownloadId();
                removedIds.add(ids[i]);
            }
            DownloadManager.Query query = new DownloadManager.Query().setFilterById(ids);
            Cursor c = dm.query(query);
            try {
                if (null != c && c.moveToFirst()){
                    do{
                        long id = c.getLong(c.getColumnIndex(DownloadManager.COLUMN_ID));
                        removedIds.remove(id);
                        cachedInfo = findInfoByDownloadId(id);
                        if (null == cachedInfo){
                            continue;
                        }
                        int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                        switch(status){
                            case DownloadManager.STATUS_PENDING:
                            case DownloadManager.STATUS_RUNNING:
                                break;
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
                                mRefreshCache.remove(cachedInfo.getKey().hashCode());
                                break;
                        }
                        if (cachedInfo.getStatus()<=DownloadManager.STATUS_FAILED) {
                            cachedInfo.setStatus(status);
                        }
                        cachedInfo.setLocalPath(c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)))
                                .setTotalBytes(c.getInt(c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)))
                                .setCurrentBytes(c.getInt(c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)))
                                .setReason(c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON)))
                                .setMimeType(c.getString(c.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE)));
                        update(cachedInfo);
                        ImplAgent.notify(true,cachedInfo);
                    }while(c.moveToNext());
                }
            }catch(Exception e){
                e.printStackTrace();
            } finally {
                if (c != null) {
                    c.close();
                }
            }

            for (long id : removedIds){
                cachedInfo = findInfoByDownloadId(id);
                if (null != cachedInfo){
                    mRefreshCache.remove(cachedInfo.getKey().hashCode());
                }
            }
            removedIds.clear();

            if (mRefreshCache.size()>0){
                ImplAgent.mWorkHandler.postDelayed(this, 500);
            }
        }


        private ImplInfo findInfoByDownloadId(long downloadId){
            int size = mRefreshCache.size();
            ImplInfo info = null;
            for (int i =0; i < size; i++){
                info = mRefreshCache.valueAt(i);
                if (downloadId == info.getDownloadId()){
                    return info;
                }
            }
            return null;
        }
    }
}
