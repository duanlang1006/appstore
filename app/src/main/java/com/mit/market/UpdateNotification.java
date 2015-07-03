package com.mit.market;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.applite.android.R;


/**
 * Created by LSY on 15-7-3.
 */
public class UpdateNotification {
    private static UpdateNotification mInstance = null;
    private static final int NOTIFICATION_ID = 1;
    private NotificationManager mManager;
    public static final String UPDATE_NOT = "update_not";

    private static synchronized UpdateNotification init() {
        if (null == mInstance) {
            mInstance = new UpdateNotification();
        }
        return mInstance;
    }

    public static UpdateNotification getInstance() {
        if (null == mInstance) {
            mInstance = init();
        }
        return mInstance;
    }

    private UpdateNotification() {
    }

    /**
     * 隐藏通知栏
     *
     * @param context
     */
    public void hideNot(Context context) {
        if (null == mManager) {
            mManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        mManager.cancel(NOTIFICATION_ID);
    }

    /**
     * 显示通知栏
     *
     * @param context
     * @param number
     */
    public void showNot(Context context, String number) {
        if (null == mManager) {
            mManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        Intent mIntent = new Intent(context, context.getClass());
        mIntent.putExtra("show_fragment", "UpdteFragment");
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher) // 设置状态栏中的小图片，尺寸一般=在24×24
                .setTicker("应用商城")// 设置状态栏中的提示文字
                .setContentTitle("更新")// 下拉Title
                .setContentText("您有" + number + "个应用可更新！")// 下拉Content
                .setContentIntent(pendingIntent) // 关联PendingIntent
//                .setNumber(1) // 在TextView的右方显示的数字，可放大图片看，在最右侧。这个number同时也起到一个序列号的左右，如果多个触发多个通知（同一ID），可以指定显示哪一个。
                .getNotification();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        mManager.notify(NOTIFICATION_ID, notification);
    }
}
