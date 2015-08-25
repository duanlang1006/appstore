package com.mit.market;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    private LinearLayout ll;

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

    public void showNot(Context context, String number, JSONArray array) throws JSONException {
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
        LogUtils.i(TAG, array.toString());
        View v2 = View.inflate(context, R.layout.notification, null);
        if (array.length() < 6) {
            for (int i = 0; i < array.length(); i++) {
                LogUtils.i(TAG, array.toString());
                JSONObject obj = new JSONObject(array.get(i).toString());
                String icon_url = obj.getString("iconUrl");
                LogUtils.i(TAG, icon_url);
                mUrlList.add(icon_url);
                final int l = i;
                mBitmapUtils.display(v2, mUrlList.get(i), new BitmapLoadCallBack() {
                    @Override
                    public void onLoadCompleted(View view, String s, Bitmap bitmap, BitmapDisplayConfig bitmapDisplayConfig, BitmapLoadFrom bitmapLoadFrom) {
                        if (l == 0) {
                            notification.contentView.setImageViewBitmap(R.id.not_img_tu1, bitmap);
                        } else if (l == 1) {
                            notification.contentView.setImageViewBitmap(R.id.not_img_tu2, bitmap);
                        } else if (l == 2) {
                            notification.contentView.setImageViewBitmap(R.id.not_img_tu3, bitmap);
                        } else if (l == 3) {
                            notification.contentView.setImageViewBitmap(R.id.not_img_tu4, bitmap);
                        } else if (l == 4) {
                            notification.contentView.setImageViewBitmap(R.id.not_img_tu5, bitmap);
                        }
                    }

                    @Override
                    public void onLoadFailed(View view, String s, Drawable drawable) {

                    }
                });

            }
        } else {
            for (int i = 0; i < 5; i++) {
                LogUtils.i(TAG, array.toString());
                JSONObject obj = new JSONObject(array.get(i).toString());
                String icon_url = obj.getString("iconUrl");
                LogUtils.i(TAG, icon_url);
                mUrlList.add(icon_url);

                final int l = i;
                BitmapLoadCallBack bitmapLoadCallBack = new BitmapLoadCallBack() {
                    @Override
                    public void onLoadCompleted(View view, String s, Bitmap bitmap, BitmapDisplayConfig bitmapDisplayConfig, BitmapLoadFrom bitmapLoadFrom) {
                        if (l == 0) {
                            notification.contentView.setImageViewBitmap(R.id.not_img_tu1, bitmap);
                        } else if (l == 1) {
                            notification.contentView.setImageViewBitmap(R.id.not_img_tu2, bitmap);
                        } else if (l == 2) {
                            notification.contentView.setImageViewBitmap(R.id.not_img_tu3, bitmap);
                        } else if (l == 3) {
                            notification.contentView.setImageViewBitmap(R.id.not_img_tu4, bitmap);
                        } else if (l == 4) {
                            notification.contentView.setImageViewBitmap(R.id.not_img_tu5, bitmap);
                        }
                    }

                    @Override
                    public void onLoadFailed(View view, String s, Drawable drawable) {

                    }

                };
                mBitmapUtils.display(v2, mUrlList.get(i), bitmapLoadCallBack);
            }
            notification.contentView.setTextViewText(R.id.not_text_etc, "…");
        }
        notification.contentIntent = pendingIntent;
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        mManager.notify(NOTIFICATION_ID, notification);
    }

}
