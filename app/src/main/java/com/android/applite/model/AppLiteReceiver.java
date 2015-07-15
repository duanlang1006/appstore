package com.android.applite.model;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import com.applite.util.AppliteConfig;
import com.mit.market.MyIntentService;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.applite.android.R;
import com.mit.market.MitMarketActivity;

public class AppLiteReceiver extends BroadcastReceiver {
    private static final String TAG = "AppLiteReceiver";
    NotificationManager manager;
	@Override
	public void onReceive(Context context, Intent intent) {
	    if(Intent.ACTION_TIME_CHANGED.equals(intent.getAction())){
	        AppLiteModel.getInstance(context).onReceive(context, intent);
	    }
	    if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
	        AppliteConfig.initNetwork(context);
        }else if ("com.dataservice.broadcast".equals(intent.getAction())){
            setRemind(context,intent);
            //AppLiteModel.getInstance(context).onReceive(context,intent);
        }else{
            MyIntentService.startByOriginIntent(context,intent);
        }
    }

    private void setRemind(Context context,Intent intent){
        /*
        *字段说明
        *"SUBR,ACTION,packagename,title,desc,icon_url,intent"
        *ACTION : android.intent.action.MAIN
        *packagename : com.applite.android
        *title : title
        *desc : desc
        *icon_url :
        *intent : intent
        */
        String stringValue = intent.getStringExtra("intent");
        Log.d(TAG, "onReceive stringValue : " + stringValue);
        int mRequestCode = 1;
        String[] mString = null;
        mString = stringValue.split(",");
        for(int i=0;i<mString.length;i++){
            Log.d(TAG, "onReceive mString[" + i + "] : " + mString[i]);
        }
        String mAction = null;
        String mPackageName = null;
        String mTitle = null;
        String mDesc = null;
        String mIconUrl = null;
        String mIntent = null;
        if (null != mString) {
            mAction = mString[0];
            mPackageName = mString[1];
            mTitle = mString[2];
            mDesc = mString[3];
            mIconUrl = mString[4];
            mIntent = mString[5];
        }
        Intent mPlayIntent =null;
        mIntent = mString[5].replace("_**_",";");

        //myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Log.i(TAG, "mAction: " + mAction
                + " ; mPackageName: " + mPackageName
                + " ; mTitle: " + mTitle
                + " ; mDesc: " + mDesc
                + " ; mIconUrl: " + mIconUrl
                + " ; mIntent: " + mIntent);
        if(context.getPackageName().equals(mPackageName)) {
            manager = (NotificationManager) context.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
            ComponentName cn =null;
            if(null ==mIntent || null == mAction || null == mPackageName){
                mPlayIntent = new Intent(context, MitMarketActivity.class);
                mPlayIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                String intentstr = mPlayIntent.toUri(0).toString();
                Log.d(TAG, "mPlayIntent= " + intentstr);
            }else {
                try {
                    mPlayIntent = Intent.parseUri(mIntent, 0);
                    Log.i(TAG, "mPlayIntent : " + mPlayIntent +" ; mPlayIntent.getDataString() : "
                            + mPlayIntent.getDataString() + " ; mPlayIntent.getAction() : "
                            + mPlayIntent.getAction());
                    cn = new ComponentName(mPackageName,
                            mPlayIntent.getAction());
                    mPlayIntent.setComponent(cn);
                    mPlayIntent.setAction(mAction);
                }catch (Exception e){
                    mPlayIntent = new Intent(context, MitMarketActivity.class);
                    e.printStackTrace();
                }

            }
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    mRequestCode,
                    mPlayIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            Log.i(TAG, "pendingIntent : " + pendingIntent );
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            builder.setContentTitle(mTitle)
                   .setContentText(mDesc)
                   .setSmallIcon(R.drawable.notification_applite)
                   .setDefaults(Notification.DEFAULT_ALL)
                   .setContentIntent(pendingIntent)
                   .setAutoCancel(true);
            manager.notify(mRequestCode, builder.build());
        }

    }
}
