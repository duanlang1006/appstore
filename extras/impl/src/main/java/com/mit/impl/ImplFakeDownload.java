//package com.mit.impl;
//
//import android.content.Context;
//import android.content.Intent;
//import android.os.Handler;
//import android.util.SparseArray;
//
//import com.android.dsc.downloads.DownloadManager;
//import java.util.ArrayList;
//
//public class ImplFakeDownload implements ImplInterface {
//    private SparseArray<String> mCmdList = new SparseArray<String>();
//    private Handler mHandler;
//    private FakeRunnable mDlRunnable;
//
//    private static ImplFakeDownload mInstance = null;
//    private static synchronized void initInstance(){
//        if (null == mInstance ){
//            mInstance = new ImplFakeDownload();
//        }
//    }
//    public static ImplFakeDownload getInstance(){
//        if (null == mInstance){
//            initInstance();
//        }
//        return mInstance;
//    }
//
//    private ImplFakeDownload() {
//        mCmdList.append(ACTION_FAKE_DOWNLOAD_REQ.hashCode(), ACTION_FAKE_DOWNLOAD_REQ);
//        mCmdList.append(ACTION_DOWNLOAD_DELETE_REQ.hashCode(), ACTION_DOWNLOAD_DELETE_REQ);
//    }
//
//    @Override
//    public boolean request(Context context,Intent cmd, ImplListener listener) {
//        // TODO Auto-generated method stub
//        if (null == mDlRunnable){
//            mDlRunnable = new FakeRunnable(context);
//        }
//        if (null == mHandler){
//            mHandler = new Handler();
//        }
//        String action =  mCmdList.get(cmd.getAction().hashCode());
//        if (null == action){
//            return false;
//        }
//        action = cmd.getAction();
//        if (ACTION_FAKE_DOWNLOAD_REQ.equals(action)){
//            return handleDownloadReq(context, cmd, listener);
//        }else if (ACTION_DOWNLOAD_DELETE_REQ.equals(action)){
//            return handleDownloadDelete(context, cmd, listener);
//        }
//        return true;
//    }
//
//    @Override
//    public void abort(Context context,Intent cmd, ImplListener listener) {
//        // TODO Auto-generated method stub
//
//    }
//
//    private boolean handleDownloadReq(Context context, Intent cmd, ImplListener listener) {
//        // TODO Auto-generated method stub
//        Intent result = new Intent(ACTION_IMPL_RESULT);
//        long id = cmd.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
//        int key = cmd.getStringExtra("key").hashCode();
//        DownloadData data = mDlRunnable.findDataByKey(key);
//        if (null != data){
//            switch(data.status){
//                case DownloadManager.STATUS_PENDING:
//                case DownloadManager.STATUS_RUNNING:
//                    mDlRunnable.pauseDownload(key);
//                    break;
//                case DownloadManager.STATUS_PAUSED:
//                    mDlRunnable.resumeDownload(key);
//                    break;
//            }
//        }else{
//            String appId = cmd.getStringExtra("appId");
//            String localPath = cmd.getStringExtra("localPath");
//            id = -Math.abs(appId.hashCode());
//            mDlRunnable.add(key, id, 0,  DownloadManager.STATUS_PENDING, localPath, listener);
//
//            result.putExtra(DownloadManager.EXTRA_DOWNLOAD_ID, id);
//            if (null != listener){
//                listener.onFinish(true, ACTION_DOWNLOAD_REQ, result);
//            }
//        }
//        return true;
//    }
//
//    private boolean handleDownloadDelete(final Context context,final Intent cmd, final ImplListener listener){
//        final long id = cmd.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
//        final int key = cmd.getStringExtra("key").hashCode();
//        if (id >= 0){
//            return false;
//        }
//        mDlRunnable.remove(key);
//        return true;
//    }
//
//    class DownloadData{
//        private int key;
//        private long id ;
//        private int progress;
//        private int status;
//        private String localPath;
//        private ImplListener listener;
//        public DownloadData(int key,long id, int progress,int status,String localPath,ImplListener listener) {
//            super();
//            this.key = key;
//            this.id = id;
//            this.listener = listener;
//        }
//    }
//
//    class FakeRunnable implements Runnable{
//        private Context mContext;
//        private SparseArray<DownloadData> mDownloadList ;
//        public FakeRunnable(Context context) {
//            super();
//            // TODO Auto-generated constructor stub
//            mContext = context;
//            mDownloadList = new SparseArray<DownloadData>();
//        }
//
//        public synchronized void add(int key,long downloadId, int progress,int status,String localPath,ImplListener listener){
//            if (downloadId <=0){
//                return;
//            }
//            DownloadData data = mDownloadList.get(key);
//            if (null == data){
//                data = new DownloadData(key,downloadId,progress,status,localPath,listener);
//                mDownloadList.put(key,data);
//                mHandler.post(this);
//            }else{
//                if (data.id != downloadId || data.listener != listener){
//                    data.id = downloadId;
//                    data.progress = progress;
//                    data.status = status;
//                    data.localPath = localPath;
//                    data.listener = listener;
//                    mHandler.post(this);
//                }
//            }
//        }
//
//        public synchronized void remove(int key){
//            mDownloadList.remove(key);
//        }
//
//        @Override
//        public synchronized void run() {
//            // TODO Auto-generated method stub
//            mHandler.removeCallbacks(this);
//            ArrayList<Integer>  removeKeyList = new ArrayList<Integer>();
//
//            int size = mDownloadList.size();
//            if (size<=0){
//                return;
//            }
//            DownloadData data = null;
//            for (int i =0;i<size;i++){
//                data = findDataByDownloadId(mDownloadList.valueAt(i).id);
//                switch(data.status){
//                    case DownloadManager.STATUS_PENDING:
//                    case DownloadManager.STATUS_RUNNING:
//                        if (!"none".equals(ImplUtils.getNetwork(mContext))){
//                            data.progress += 2;
//                        }
//                        break;
//                }
//                if (data.progress < 100){
//                    if (null != data.listener){
//                        Intent result = new Intent(ACTION_IMPL_RESULT);
//                        result.putExtra("status", data.status);
//                        result.putExtra("progress", data.progress);
//                        data.listener.onFinish(true, ACTION_DOWNLOAD_UPDATE_REQ, result);
//                    }
//                }else{
//                    data.progress = 100;
//                    data.status = DownloadManager.STATUS_SUCCESSFUL;
//                    removeKeyList.add(data.key);
//                    if (null != data.listener){
//                        Intent result = new Intent(ACTION_IMPL_RESULT);
//                        result.putExtra("localPath", data.localPath);
//                        data.listener.onFinish(true, ACTION_DOWNLOAD_COMPLETE, result);
//                    }
//                }
//            }
//            for (int key : removeKeyList){
//                mDownloadList.remove(key);
//            }
//            if (mDownloadList.size()>0){
//                mHandler.postDelayed(this, 300);
//            }
//        }
//
//        public synchronized void pauseDownload(int key){
//            DownloadData data = mDownloadList.get(key);
//            if (null != data){
//                data.status = DownloadManager.STATUS_PAUSED;
//            }
//        }
//
//        public synchronized void resumeDownload(int key){
//            DownloadData data = mDownloadList.get(key);
//            if (null != data){
//                data.status = DownloadManager.STATUS_RUNNING;
//            }
//        }
//
//        DownloadData findDataByDownloadId(long downloadId){
//            int size = mDownloadList.size();
//            for (int i =0; i < size; i++){
//                DownloadData data = mDownloadList.valueAt(i);
//                if (downloadId == data.id){
//                    return data;
//                }
//            }
//            return null;
//        }
//
//        DownloadData findDataByKey(int key){
//            return mDownloadList.get(key);
//        }
//    }
//}
