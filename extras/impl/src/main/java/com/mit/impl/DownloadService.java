package com.mit.impl;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.List;

/**
 * Author: wyouflf
 * Date: 13-11-10
 * Time: 上午1:04
 */
public class DownloadService extends Service {

    private String DM_NOTIFICATION = "com.mit.market.MitMarketActivity";
    private DownloadPackageListener listener;

//    private static ImplDownload DOWNLOAD_MANAGER;

//    public static ImplDownload getDownloadManager(Context appContext) {
//        if (!DownloadService.isServiceRunning(appContext)) {
//            Intent downloadSvr = new Intent(appContext,DownloadService.class);
//            appContext.startService(downloadSvr);
//        }
//        if (DownloadService.DOWNLOAD_MANAGER == null) {
//            DownloadService.DOWNLOAD_MANAGER = ImplDownload.getInstance(appContext);
//        }
//        return DOWNLOAD_MANAGER;
//    }

    public DownloadService() {
        super();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        listener = new DownloadPackageListener(this);
        ImplAgent.getInstance(getApplicationContext()).registerPackageListener(listener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
//        if (DOWNLOAD_MANAGER != null) {
//            try {
//                DOWNLOAD_MANAGER.pauseAll();
//                DOWNLOAD_MANAGER.backupDownloadInfoList();
//            } catch (DbException e) {
//                LogUtils.e(e.getMessage(), e);
//            }
//        }
        ImplAgent.getInstance(getApplicationContext()).unregisterPackageListener(listener);
        super.onDestroy();
    }

    public static boolean isServiceRunning(Context context) {
        boolean isRunning = false;

        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList
                = activityManager.getRunningServices(Integer.MAX_VALUE);

        if (serviceList == null || serviceList.size() == 0) {
            return false;
        }

        for (int i = 0; i < serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(DownloadService.class.getName())) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }

    class DownloadPackageListener extends ImplAgent.SimplePackageListener {

        private NotificationCompat.Builder mBuilder;
        private int notifyId = 101;
        private NotificationManager mNotificationManager;
        private Context mContext;
        private int downloading = ImplInfo.STATUS_PENDING | ImplInfo.STATUS_RUNNING | ImplInfo.STATUS_PAUSED
                | ImplInfo.STATUS_FAILED | ImplInfo.STATUS_PACKAGE_INVALID;
        private int downloaded = ~downloading;

        DownloadPackageListener(Context context) {
            this.mContext = context;
        }

        @Override
        public void onDownloadEnqued(ImplInfo implInfo) {
            showDownloadNotify(mContext);
        }

        @Override
        public void onDownloadSucess(ImplInfo implInfo) {
            showDownloadNotify(mContext);
        }

        @Override
        public void onDownloadPaused(ImplInfo implInfo) {
            showDownloadNotify(mContext);
        }

        @Override
        public void onDownloadResume(ImplInfo implInfo) {
            showDownloadNotify(mContext);
        }

        //        @Override
//        public void onPackageRemoved(ImplInfo implInfo) {
//            removeNotification(mContext);
//        }

        private void showDownloadNotify(Context mContext) {
            initNotify(mContext);
            int tempCount = calculate(mContext, downloading);
            if (tempCount != 0) {
                if (tempCount == calculate(mContext, ImplInfo.STATUS_PAUSED)) {
                    showIntentActivityNotify(mContext, 2);
                } else {
                    showIntentActivityNotify(mContext, 0);
                }
            } else if (calculate(mContext, downloaded) != 0) {
                showIntentActivityNotify(mContext, 1);
            } else {
                //Resume
                showIntentActivityNotify(mContext, 1);
            }
        }

//        private void removeNotification(Context mContext) {
//            if (0 == calculate(mContext, downloading) && 0 == calculate(mContext, downloaded)) {
//                mNotificationManager.cancel(notifyId);
//            }
//        }

        //显示通知栏点击跳转到指定Activity
        // 0 下载中;1 下载完成; 2 暂停
        public void showIntentActivityNotify(Context context, int status) {
            String temp = null;
            Intent clickIntent; //点击 Intent
            switch (status) {
                case 0:
                    temp = mContext.getResources().getString(R.string.notification_message_downloading,
                            calculate(mContext, ImplInfo.STATUS_RUNNING | ImplInfo.STATUS_PENDING));
                    break;
                case 1:
                    temp = mContext.getResources().getString(R.string.notification_message_downloaded);
                    break;
                case 2:
                    temp = mContext.getResources().getString(R.string.notification_message_pause);
                    status = 0;
                    break;
            }
            mBuilder.setAutoCancel(true)//点击后让通知将消失
                    .setContentTitle(temp)
                    .setContentText(mContext.getResources().getString(R.string.click_check))
                    .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_launcher));
            try {
                clickIntent = new Intent(context, Class.forName(DM_NOTIFICATION));
                clickIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                clickIntent.putExtra("notify", notifyId + "");
                clickIntent.putExtra("position", status);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(pendingIntent);
                mNotificationManager.notify(notifyId, mBuilder.build());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        private int calculate(Context context, int position) {
            return ImplAgent.getInstance(context.getApplicationContext()).getImplInfoCount(position);
        }

        private void initNotify(Context context) {
            if (null == mBuilder) {
                mBuilder = new NotificationCompat.Builder(context);
                mBuilder.setWhen(System.currentTimeMillis())
                        .setPriority(Notification.PRIORITY_DEFAULT)
                        .setOngoing(false)
                        .setSmallIcon(R.drawable.ic_launcher);
                mNotificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
            }
        }
    }
}
