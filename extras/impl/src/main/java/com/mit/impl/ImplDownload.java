package com.mit.impl;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Pair;
import android.util.SparseArray;
import android.webkit.MimeTypeMap;
import com.android.dsc.downloads.DownloadManager;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ImplDownload extends AbstractImpl{
    private static final String TAG = "impl_download";
    private SparseArray<Method> mCmdList = new SparseArray<Method>();
    private RealRunnable mDlRunnable;
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
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        String sql = "select * from "+ImplConfig.TABLE_IMPL;
        Cursor c = db.rawQuery(sql,null);
        try{
            c.moveToFirst();
            do {
                ImplInfo info = ImplInfo.from(c);
                if (null != info){
                    mDlRunnable.add(context,info);
                }
            }while(c.moveToNext());
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            if (null != c){
                c.close();
            }
        }
//        Map<String,String> map = (Map<String,String>)sp.getAll();
//        for (String key : map.keySet()){
//            String json = map.get(key);
//            if (null != json && json.length()>0) {
//                ImplInfo info = gson.fromJson(json,ImplInfo.class);
//                if (null != info){
//                    mDlRunnable.add(context,info);
//                }
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
        if (null == mDlRunnable) {
            mDlRunnable = new RealRunnable();
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

//    private ImplInfo getImplInfo(String key){
//        ImplInfo info = null;
//        SQLiteDatabase db = databaseHelper.getWritableDatabase();
//        String sql = "select * from "+ImplConfig.TABLE_IMPL+" where "+ImplConfig.KEY+" = "+key;
//        Cursor c = db.rawQuery(sql,null);
//        try{
//            c.moveToFirst();
//            info = ImplInfo.from(c);
//            if (null != info){
//                mDlRunnable.add(context,info);
//            }
//        }catch(Exception e){
//            e.printStackTrace();
//        }finally {
//            if (null != c){
//                c.close();
//            }
//        }
//
////        String json = sp.getString(key,null);
////        if (null != json) {
////            info = (ImplInfo) gson.fromJson(json, ImplInfo.class);
////        }
//        return info;
//    }

    private boolean handleQueryReq(ImplAgent.ImplRequest cmd){
        ImplAgent.DownloadUpdateReq implCmd = (ImplAgent.DownloadUpdateReq)cmd;
        ImplLog.d(TAG,"handleQueryReq,"+implCmd.keys);
        List<ImplInfo> infoList = ImplConfig.findInfoByKeyBatch(databaseHelper,implCmd.keys);
        for (ImplInfo info:infoList){
            mDlRunnable.add(cmd.context, info);
        }

        return true;
    }

    private boolean handleDownloadReq(ImplAgent.ImplRequest cmd) {
        // TODO Auto-generated method stub
        ImplAgent.DownloadPackageReq implCmd = (ImplAgent.DownloadPackageReq)cmd;
        long id = 0;
        ImplInfo info = ImplConfig.findInfoByKey(databaseHelper,implCmd.key);
        if (null != info){
            ImplLog.d(TAG,"handleDownloadReq,"+info.getDownloadId()+","+info.getKey()+","+info.getTitle());
            id = info.getDownloadId();
        }else {
            ImplLog.d(TAG, "handleDownloadReq," + id);
        }
        if (existInDownloadDb(id)){
            mDlRunnable.add(cmd.context,info);
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
                id = dm.enqueue(request);

                info = new ImplInfo(implCmd.key,
                        implCmd.url, id,
                        implCmd.packageName,
                        implCmd.iconDir,implCmd.iconUrl,
                        implCmd.title,implCmd.desc);
                mDlRunnable.add(cmd.context,info);
                save(info);
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
        ImplInfo info = ImplConfig.findInfoByDownloadId(databaseHelper,id);
        if (null == info){
            return false;
        }

        ImplLog.d(TAG,"handleDownloadComplete,"+info.getDownloadId()+","+info.getKey());
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(id);
        Cursor c = dm.query(query);
        String localUri = null;
        int status = DownloadManager.STATUS_FAILED;;
        try{
            if(null != c && c.moveToFirst()) {
                status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                if (status == DownloadManager.STATUS_SUCCESSFUL){
                    localUri = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                }
                info.setLocalPath(localUri);
                info.setStatus(status);
                info.setCurrentBytes(c.getLong(c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)));
                info.setTotalBytes(c.getLong(c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)));
                update(info);
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if (null != c ){
                c.close();
            }
        }
        ImplAgent.notify(true,
                new ImplAgent.DownloadCompleteRsp(cmd.context,info.getKey(),status,localUri,info.getPackageName()));
        return true;
    }
    
    private boolean handleDownloadDelete(ImplAgent.ImplRequest cmd){
        ImplAgent.DeleteDownloadReq implCmd = (ImplAgent.DeleteDownloadReq)cmd;
        long id = 0;
        ImplInfo info = ImplConfig.findInfoByKey(databaseHelper,implCmd.key);
        if (null != info){
            id = info.getDownloadId();
        }
        if (id == 0){
            return false;
        }
        ImplLog.d(TAG,"handleDownloadDelete,"+id+","+info.getKey()+","+info.getTitle());
        try{
            dm.remove(id);
        }catch(Exception e){}
        mDlRunnable.remove(implCmd.key.hashCode());
        remove(implCmd.key);
        ImplAgent.notify(true,
                new ImplAgent.DeleteDownloadRsp(cmd.context,info.getKey(),true));
        return true;
    }

    private boolean handleDownloadToggle(ImplAgent.ImplRequest cmd){
        ImplAgent.ToggleDownloadReq implCmd = (ImplAgent.ToggleDownloadReq)cmd;
        long id = 0;
        ImplInfo info = ImplConfig.findInfoByKey(databaseHelper,implCmd.key);
        if (null != info){
            ImplLog.d(TAG,"handleDownloadToggle,"+info.getKey()+","+info.getTitle());
            id = info.getDownloadId();
            if (existInDownloadDb(id)){
                mDlRunnable.add(cmd.context,info);
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
                        int progress = 100;
                        if (!new File(localPath).exists()){
                            dm.restartDownload(id);
                            status = DownloadManager.STATUS_FAILED;
                            progress = 0;
                        }
                        ImplAgent.notify(true,new ImplAgent.DownloadUpdateRsp(context,info.getKey(),status,progress));
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
    
    class RealRunnable implements Runnable{
        private SparseArray<Pair<Context,ImplInfo>> mDownloadList ;

        public RealRunnable() {
            super();
            // TODO Auto-generated constructor stub
            mDownloadList = new SparseArray<Pair<Context,ImplInfo>>();
        }
        
        public synchronized void add(Context context,ImplInfo info){
            if (null == info || info.getDownloadId() <=0){
                return;
            }

            Pair<Context,ImplInfo> pair = mDownloadList.get(info.getKey().hashCode());
            if (null == pair){
                mDownloadList.put(info.getKey().hashCode(),new Pair<Context,ImplInfo>(context,info));
                ImplAgent.mWorkHandler.removeCallbacks(this);
                ImplAgent.mWorkHandler.postDelayed(this, 50);
            }else{
                if (pair.second.getDownloadId() != info.getDownloadId() ){
                    mDownloadList.put(info.getKey().hashCode(),new Pair<Context,ImplInfo>(context,info));
                    ImplAgent.mWorkHandler.removeCallbacks(this);
                    ImplAgent.mWorkHandler.postDelayed(this, 50);
                }
            }
        }
        
        public synchronized void remove(int key){
            mDownloadList.remove(key);
        }
        
        @Override
        public synchronized void run() {
            // TODO Auto-generated method stub
            ImplAgent.mWorkHandler.removeCallbacks(this);

            int size = mDownloadList.size();
            if (size<=0){
                return;
            }
            long[] ids = new long[size];
            Set<Long> removedIds = new HashSet<Long>();
            for (int i =0;i<size;i++){
                ids[i] = mDownloadList.valueAt(i).second.getDownloadId();
                removedIds.add(ids[i]);
            }
            DownloadManager.Query query = new DownloadManager.Query().setFilterById(ids);
            Cursor c = dm.query(query);
            try {
                Pair<Context,ImplInfo> pair = null;
                if (null != c && c.moveToFirst()){
                    do{
                        long id = c.getLong(c.getColumnIndex(DownloadManager.COLUMN_ID));
                        removedIds.remove(id);
                        pair = findPairByDownloadId(id);
                        if (null == pair){
                            continue;
                        }
                        String title = c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE));
                        int downloadSize = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                        int totalSize = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                        int progress = 0;
                        if (totalSize <= 0){
                            progress = 0;
                        }else {
                            progress = (downloadSize * 100 / totalSize);
                            progress = Math.min(100,Math.max(0, progress));
                        }
                        int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                        switch(status){
                            case DownloadManager.STATUS_PENDING:
                            case DownloadManager.STATUS_RUNNING:
                                ImplAgent.notify(true,
                                        new ImplAgent.DownloadUpdateRsp(pair.first,pair.second.getKey(),status,progress));
                                break;
                            case DownloadManager.STATUS_PAUSED:
                                int reason = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON));
                                if (reason != DownloadManager.PAUSED_QUEUED_FOR_WIFI
                                        && reason != DownloadManager.PAUSED_BY_APP){
                                    status = DownloadManager.STATUS_RUNNING;
                                }else{
                                    status = DownloadManager.STATUS_PAUSED;
                                }
                                ImplAgent.notify(true,
                                        new ImplAgent.DownloadUpdateRsp(pair.first,pair.second.getKey(),status,progress));
                                pair.second.setReason(reason);
                                break;
                            case DownloadManager.STATUS_SUCCESSFUL:
                                String localUri = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                                String mediaType = c.getString(c.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE));
                                pair.second.setLocalPath(localUri);
                                pair.second.setMimeType(mediaType);
//                                if (new File(localPath).exists()){
                                ImplAgent.notify(true,
                                        new ImplAgent.DownloadUpdateRsp(pair.first,pair.second.getKey(),status,progress));
//                                }
                                mDownloadList.remove(pair.second.getKey().hashCode());
                                break;
                            case DownloadManager.STATUS_FAILED:
                                reason = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON));
                                ImplAgent.notify(true,
                                        new ImplAgent.DownloadUpdateRsp(pair.first,pair.second.getKey(),status,progress));
                                mDownloadList.remove(pair.second.getKey().hashCode());
                                pair.second.setReason(reason);
                                break;
                        }
                        if (pair.second.getStatus()<=DownloadManager.STATUS_FAILED) {
                            pair.second.setStatus(status);
                        }
                        pair.second.setProgress(progress);
                        pair.second.setTotalBytes(totalSize);
                        pair.second.setCurrentBytes(downloadSize);
                        update(pair.second);
                    }while(c.moveToNext());
                }else{
                    for (int i =0;i<size;i++){
                        pair = mDownloadList.get(i);
                        ImplAgent.notify(false,
                                new ImplAgent.DownloadUpdateRsp(pair.first,pair.second.getKey(),pair.second.getStatus(),pair.second.getProgress()));
                        mDownloadList.remove(pair.second.getKey().hashCode());
                    }
                }
                for (long id : removedIds){
                    pair = findPairByDownloadId(id);
                    if (null != pair){
                        mDownloadList.remove(pair.second.getKey().hashCode());
                        ImplAgent.notify(false,
                                new ImplAgent.DownloadUpdateRsp(pair.first,pair.second.getKey(),pair.second.getStatus(),pair.second.getProgress()));
                    }
                }
                removedIds.clear();
            }catch(Exception e){
                e.printStackTrace();
            } finally {
                if (c != null) {
                    c.close();
                }
            }
            if (mDownloadList.size()>0){
                ImplAgent.mWorkHandler.postDelayed(this, 500);
            }
        }


        private Pair findPairByDownloadId(long downloadId){
            int size = mDownloadList.size();
            Pair<Context,ImplInfo> pair = null;
            for (int i =0; i < size; i++){
                pair = mDownloadList.valueAt(i);
                if (downloadId == pair.second.getDownloadId()){
                    return pair;
                }
            }
            return null;
        }
    }
}
