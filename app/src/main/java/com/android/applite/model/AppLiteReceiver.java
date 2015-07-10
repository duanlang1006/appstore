package com.android.applite.model;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import com.applite.util.AppliteConfig;
import com.mit.market.MyIntentService;


public class AppLiteReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
	    if(Intent.ACTION_TIME_CHANGED.equals(intent.getAction())){
	        AppLiteModel.getInstance(context).onReceive(context, intent);
	    }
	    if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
	        AppliteConfig.initNetwork(context);
        }else if ("com.dataservice.broadcast".equals(intent.getAction())){
            AppLiteModel.getInstance(context).onReceive(context,intent);
        }else{
            MyIntentService.startByOriginIntent(context,intent);
        }
    }
}
