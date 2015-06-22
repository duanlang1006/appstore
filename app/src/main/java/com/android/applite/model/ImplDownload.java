package com.android.applite.model;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;
import android.webkit.MimeTypeMap;

import com.android.dsc.downloads.DownloadManager;
import com.android.dsc.downloads.DownloadManager.Request;
import com.applite.util.AppliteUtilities;

import java.io.File;

public class ImplDownload implements ImplInterface {
    private SparseArray<String> mCmdList = new SparseArray<String>();
    private RealRunnable mDlRunnable;
    private DownloadManager dm;
    private Handler mHandler;

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
        mCmdList.append(ACTION_DOWNLOAD_INIT.hashCode(), ACTION_DOWNLOAD_INIT);
        mCmdList.append(ACTION_DOWNLOAD_REQ.hashCode(), ACTION_DOWNLOAD_REQ);
        mCmdList.append(ACTION_DOWNLOAD_COMPLETE.hashCode(), ACTION_DOWNLOAD_COMPLETE);
        mCmdList.append(ACTION_DOWNLOAD_DELETE_REQ.hashCode(), ACTION_DOWNLOAD_DELETE_REQ);
//        mCmdList.append(ACTION_DOWNLOAD_TOGGLE_REQ.hashCode(), ACTION_DOWNLOAD_TOGGLE_REQ);
//        mCmdList.append(ACTION_DOWNLOAD_UPDATE_REQ.hashCode(), ACTION_DOWNLOAD_UPDATE_REQ);
        mDlRunnable = new RealRunnable();
    }

    @Override
    public boolean request(final Context context,final Intent cmd, final ImplListener listener) {
        // TODO Auto-generated method stub
        if (null == dm ){
            dm = DownloadManager.getInstance(context);
        }
        if (null == mHandler){
            mHandler = AppLiteModel.getInstance(context).getWorkHandler();
        }

        final String action = mCmdList.get(cmd.getAction().hashCode());
        if (null == action){
            return false;
        }
        // TODO Auto-generated method stub
        if (ACTION_DOWNLOAD_INIT.equals(action)){
            return handleDownloadInit(context,cmd,listener);
        }if (ACTION_DOWNLOAD_REQ.equals(action)){
            return handleDownloadReq(context,cmd,listener);
        }else if (ACTION_DOWNLOAD_COMPLETE.equals(action)){
            return handleDownloadComplete(context,cmd,listener);
        }else if (ACTION_DOWNLOAD_DELETE_REQ.equals(action)){
            return handleDownloadDelete(context,cmd,listener);
        }
        return true;
    }

    @Override
    public void abort(Context context,Intent cmd, ImplListener listener) {
        // TODO Auto-generated method stub

    }
    
    private boolean handleDownloadInit(Context context,Intent cmd, ImplListener listener) {
        int  key = cmd.getStringExtra("key").hashCode();
        long id = cmd.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
        if (id <= 0){
            return false;
        }
        if (existInDb(context,id)){
            mDlRunnable.add(key,id,listener);
        }
        return true;
    }
    
    private boolean existInDb(Context context,long id){
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
    
    private boolean handleDownloadReq(final Context context,final Intent cmd, final ImplListener listener) {
        // TODO Auto-generated method stub
        Intent result = new Intent(ACTION_IMPL_RESULT);
        boolean success = true;
        int key = cmd.getStringExtra("key").hashCode();
        long id = cmd.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
        String url = cmd.getStringExtra("url");
        String dir = cmd.getStringExtra("publicDir");
        int dataDownload = cmd.getIntExtra("dataFlag", 0);
        boolean roaming = cmd.getBooleanExtra("roming", false);
        String title = cmd.getStringExtra("title");
        String desc = cmd.getStringExtra("desc");
        String appFileName = AppliteUtilities.getFilenameFromUrl(url);
        if (existInDb(context, id)){
            mDlRunnable.add(key,id, listener);
            mDlRunnable.toggleDownload(id);
        }else{
            try{
                Request request=new Request(Uri.parse(url));
                request.setAllowedNetworkTypes(
                        (0 != dataDownload)?(Request.NETWORK_WIFI|Request.NETWORK_MOBILE):Request.NETWORK_WIFI);
                request.setAllowedOverRoaming(roaming);
                MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
                String mimeString = mimeTypeMap.getMimeTypeFromExtension(
                                                                    MimeTypeMap.getFileExtensionFromUrl(appFileName));  
                request.setMimeType(mimeString); 
                request.setShowRunningNotification(true);  
                request.setVisibleInDownloadsUi(true);
                request.setDestinationInExternalPublicDir(dir, appFileName);  
                request.setTitle(title);
                request.setDescription(desc);
                id = dm.enqueue(request);
                mDlRunnable.add(key,id,listener);
                result.putExtra(DownloadManager.EXTRA_DOWNLOAD_ID, id);
                if (null != listener){
                    listener.onFinish(true,ACTION_DOWNLOAD_REQ, result);
                }
            }catch(Exception e){
                
            }
        }
        return true;
    }

    private boolean handleDownloadComplete(final Context context,final Intent cmd, final ImplListener listener) {
        // TODO Auto-generated method stub
        final long id = cmd.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
        if (id <= 0){
            return false;
        }
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(id);
        Cursor c = dm.query(query);
        String localPath = null;
        int progress = 0;
        int status = 0;
        try{
            if(null != c && c.moveToFirst()) {
                status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                switch(status){
                    case DownloadManager.STATUS_SUCCESSFUL:
                        String localUri = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                        localPath = Uri.parse(localUri).getPath();
                        progress = 100;
                        break;
                    case DownloadManager.STATUS_FAILED:
                        progress = 0;
                        break;
                }
            }else{
                status = DownloadManager.STATUS_FAILED;
                progress = 0;
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if (null != c ){
                c.close();
            }
        }
        
        if (null != listener){
            Intent result = new Intent(ACTION_IMPL_RESULT);
            result.putExtra("progress",progress);
            result.putExtra("status",progress);
            result.putExtra("localPath", localPath);
            listener.onFinish(true,cmd.getAction(),result);
        }
        return true;
    }
    
    private boolean handleDownloadDelete(final Context context,final Intent cmd, final ImplListener listener){
        final long id = cmd.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
        final int key = cmd.getStringExtra("key").hashCode();
        if (id <= 0){
            return false;
        }
        try{
            dm.remove(id);
        }catch(Exception e){}
        mDlRunnable.remove(key);
        return true;
    }
    
    class DownloadData{
        private int key;
        private long id ;
        private ImplListener listener;
        public DownloadData(int key,long id, ImplListener listener) {
            super();
            this.key = key;
            this.id = id;
            this.listener = listener;
        }
    }
    
    class RealRunnable implements Runnable{
        private SparseArray<DownloadData> mDownloadList ;

        public RealRunnable() {
            super();
            // TODO Auto-generated constructor stub
            mDownloadList = new SparseArray<DownloadData>();
        }
        
        public synchronized void add(int key,long downloadId ,ImplListener listener){
            if (downloadId <=0){
                return;
            }
            DownloadData data = mDownloadList.get(key);
            if (null == data){
                data = new DownloadData(key,downloadId,listener);
                mDownloadList.put(key,data);
                mHandler.post(this);
            }else{
                if (data.id != downloadId || data.listener != listener){
                    data.id = downloadId;
                    data.listener = listener;
                    mHandler.post(this);
                }
            }
        }
        
        public synchronized void remove(int key){
            mDownloadList.remove(key);
        }
        
        @Override
        public synchronized void run() {
            // TODO Auto-generated method stub
            mHandler.removeCallbacks(this);

            int size = mDownloadList.size();
            if (size<=0){
                return;
            }
            long[] ids = new long[size];
            for (int i =0;i<size;i++){
                ids[i] = mDownloadList.valueAt(i).id;
            }
            DownloadManager.Query query = new DownloadManager.Query().setFilterById(ids);
            Cursor c = dm.query(query);
            try {
                if (null != c && c.moveToFirst()){
                    DownloadData data = null;
                    do{
                        long id = c.getLong(c.getColumnIndex(DownloadManager.COLUMN_ID));
                        data = findDataByDownloadId(id);
                        if (null == data){
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
//                        if (DEBUG){
//                            Log.d(TAG,"title="+title+",status="+status+",progress="+progress);
//                        }
                        switch(status){
                            case DownloadManager.STATUS_PENDING:
                            case DownloadManager.STATUS_RUNNING:
                                if (null != data.listener){
                                    Intent result = new Intent(ACTION_IMPL_RESULT);
                                    result.putExtra("status", status);
                                    result.putExtra("progress", progress);
                                    data.listener.onFinish(true, ACTION_DOWNLOAD_UPDATE_REQ, result);
                                }
                                break;
                            case DownloadManager.STATUS_PAUSED:
                                int reason = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON));
//                                if (DEBUG){
//                                    Log.d(TAG,"title="+title+",status="+status+",progress="+progress+",reason="+reason);
//                                }
                                if (reason != DownloadManager.PAUSED_QUEUED_FOR_WIFI 
                                        && reason != DownloadManager.PAUSED_BY_APP){
                                    status = DownloadManager.STATUS_RUNNING;
                                }else{
                                    status = DownloadManager.STATUS_PAUSED;
                                }
                                if (null != data.listener){
                                    Intent result = new Intent(ACTION_IMPL_RESULT);
                                    result.putExtra("status", status);
                                    result.putExtra("progress", progress);
                                    data.listener.onFinish(true, ACTION_DOWNLOAD_UPDATE_REQ, result);
                                }
                                break;
                            case DownloadManager.STATUS_SUCCESSFUL:
                                String localUri = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                                String localPath = Uri.parse(localUri).getPath();
                                if (new File(localPath).exists()){
                                    Intent result = new Intent(ACTION_IMPL_RESULT);
                                    result.putExtra("localPath", localPath);
                                    if (null != data.listener){
                                        data.listener.onFinish(true,ACTION_DOWNLOAD_COMPLETE,result);
                                    }
                                }
                                mDownloadList.remove(data.key);
                                break;
                            case DownloadManager.STATUS_FAILED:
                                if (null != data.listener){
                                    Intent result = new Intent(ACTION_IMPL_RESULT);
                                    result.putExtra("status", status);
                                    result.putExtra("progress", progress);
                                    data.listener.onFinish(true, ACTION_DOWNLOAD_UPDATE_REQ, result);
                                }
                                mDownloadList.remove(data.key);
                                break;
                        }
                    }while(c.moveToNext());
                }else{
                    for (int i =0;i<size;i++){
                        mDownloadList.removeAt(i);
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
            } finally {
                if (c != null) {
                    c.close();
                }
            }
            if (mDownloadList.size()>0){
                mHandler.postDelayed(this, 3000);
            }
        }

        public synchronized void toggleDownload(long id){
            DownloadData data = findDataByDownloadId(id);
            if (null == data){
                return;
            }
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
                            if (new File(localPath).exists()){
                                Intent result = new Intent(ACTION_IMPL_RESULT);
                                result.putExtra("localPath", localPath);
                                if (null != data.listener){
                                    data.listener.onFinish(true,ACTION_DOWNLOAD_COMPLETE,result);
                                }
                            }else{
                                dm.restartDownload(id);
                                int status = DownloadManager.STATUS_FAILED;
                                int progress = 0;
                                Intent result = new Intent(ACTION_IMPL_RESULT);
                                result.putExtra("status", status);
                                result.putExtra("progress", progress);
                                if (null != data.listener){                
                                    data.listener.onFinish(true,ACTION_DOWNLOAD_UPDATE_REQ,result);
                                }
                            }
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

        private DownloadData findDataByDownloadId(long downloadId){
            int size = mDownloadList.size();
            for (int i =0; i < size; i++){
                DownloadData data = mDownloadList.valueAt(i);
                if (downloadId == data.id){
                    return data;
                }
            }
            return null;
        }
    }
}
