package com.mit.impl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;


public class ImplReceiver extends BroadcastReceiver {
    private static final String TAG = "AppLiteReceiver";
    public static final String KEY_NETWORK = "impl_network";

	@Override
	public void onReceive(Context context, Intent intent) {
	    if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
	        initNetwork(context);
        }
        ImplAgent.getInstance(context.getApplicationContext()).onReceive(context,intent);
    }

    public static boolean setNetwork(Context context,String network){
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(KEY_NETWORK, network);
        return editor.commit();
    }

    public static String getNetwork(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(KEY_NETWORK, "none");
    }

    public static void initNetwork(Context context){
        ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        try{
            NetworkInfo netInfo = manager.getActiveNetworkInfo();
            setNetwork(context, "none");
            switch(netInfo.getType()){
                case ConnectivityManager.TYPE_WIFI:
                    setNetwork(context, "wifi");
                    break;
                case ConnectivityManager.TYPE_MOBILE:
                    setNetwork(context, "mobile");
                    break;
            }
        }catch(Exception e ){
            e.printStackTrace();
        }
    }
}
