package com.mit.market;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RemoteViews;

import com.applite.android.R;
import com.applite.common.BitmapHelper;
import com.applite.common.Constant;
import com.applite.common.LogUtils;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.callback.BitmapLoadCallBack;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;
import com.mit.market.network.Info;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * Created by LSY on 15-7-3.
 */
public class UpdateNotification {

    private static final String TAG = "UpdateNotification";
    private static UpdateNotification mInstance = null;
    private static final int NOTIFICATION_ID = 1;
    private NotificationManager mManager;
    public static final String UPDATE_NOT = "update_not";
    private List<String> mUrlList = new ArrayList<>();
    private BitmapUtils mBitmapUtils;
    private ImageView image1;

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
        Intent mIntent = new Intent(context, MitMarketActivity.class);
        mIntent.putExtra("update", Constant.UPDATE_FRAGMENT_NOT);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, UUID.randomUUID().hashCode(), mIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification();
        notification.icon = R.drawable.ic_launcher;
        notification.contentView = new RemoteViews(context.getPackageName(), R.layout.notification);
        notification.contentView.setImageViewResource(R.id.notification_large_icon, R.drawable.ic_launcher);
        notification.contentView.setTextViewText(R.id.notification_update, "您有" + number + "个应用可更新！");
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        mManager.notify(NOTIFICATION_ID, notification);
    }

    public void showNot(Context context, String number, JSONArray array) {
        LogUtils.i(TAG, array.toString());
        mBitmapUtils = BitmapHelper.getBitmapUtils(context.getApplicationContext());
        if (null == mManager) {
            mManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        Intent mIntent = new Intent(context, MitMarketActivity.class);
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mIntent.putExtra("update", Constant.UPDATE_FRAGMENT_NOT);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, UUID.randomUUID().hashCode(), mIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        final Notification notification = new Notification();
        notification.icon = R.drawable.ic_launcher;
        notification.contentView = new RemoteViews(context.getPackageName(), R.layout.notification);
        notification.contentView.setImageViewResource(R.id.notification_large_icon, R.drawable.ic_launcher);
        notification.contentView.setTextViewText(R.id.notification_update, "您有" + number + "个应用可更新！");
        try {
            if (array.length() < 6) {
                for (int i = 0; i < array.length(); i++) {
                    LogUtils.i(TAG, array.toString());
                    JSONObject obj = new JSONObject(array.get(i).toString());
                    String packageName = obj.getString("packageName");
                    LogUtils.i(TAG, packageName);
                    final int l = i;
                    LogUtils.i(TAG, "l=" + l);
                    if (0 == l) {
                        notification.contentView.setImageViewBitmap(R.id.not_img_tu1, getBitmap(new Info(context).getAppIcon(packageName)));
                    } else if (1 == l) {
                        notification.contentView.setImageViewBitmap(R.id.not_img_tu2, getBitmap(new Info(context).getAppIcon(packageName)));
                    } else if (2 == l) {
                        notification.contentView.setImageViewBitmap(R.id.not_img_tu3, getBitmap(new Info(context).getAppIcon(packageName)));
                    } else if (3 == l) {
                        notification.contentView.setImageViewBitmap(R.id.not_img_tu4, getBitmap(new Info(context).getAppIcon(packageName)));
                    } else if (4 == l) {
                        notification.contentView.setImageViewBitmap(R.id.not_img_tu5, getBitmap(new Info(context).getAppIcon(packageName)));
                    }
                }
            } else {
                for (int i = 0; i < 5; i++) {
                    LogUtils.i(TAG, array.toString());
                    JSONObject obj = new JSONObject(array.get(i).toString());
                    String packageName = obj.getString("packageName");
                    LogUtils.i(TAG, packageName);
                    final int l = i;
                    LogUtils.i(TAG, "l=" + l);
                    if (0 == l) {
                        notification.contentView.setImageViewBitmap(R.id.not_img_tu1, getBitmap(new Info(context).getAppIcon(packageName)));
                    } else if (1 == l) {
                        notification.contentView.setImageViewBitmap(R.id.not_img_tu2, getBitmap(new Info(context).getAppIcon(packageName)));
                    } else if (2 == l) {
                        notification.contentView.setImageViewBitmap(R.id.not_img_tu3, getBitmap(new Info(context).getAppIcon(packageName)));
                    } else if (3 == l) {
                        notification.contentView.setImageViewBitmap(R.id.not_img_tu4, getBitmap(new Info(context).getAppIcon(packageName)));
                    } else if (4 == l) {
                        notification.contentView.setImageViewBitmap(R.id.not_img_tu5, getBitmap(new Info(context).getAppIcon(packageName)));
                    }
                }
                notification.contentView.setTextViewText(R.id.not_text_etc, "…");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        notification.contentIntent = pendingIntent;
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        mManager.notify(NOTIFICATION_ID, notification);
    }


    /*
     *Drawable 转 Bitmap
     */
    private Bitmap getBitmap(Drawable db) {
        BitmapDrawable bd = (BitmapDrawable) db;
        return bd.getBitmap();
    }
}
