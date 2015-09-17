package com.applite.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

public class AppliteConfig {
	//keys
	public static final String KEY_NETWORK = "network";
	public static final String KEY_BDUSER_ID = "bduser_id";

    public static boolean setNetwork(Context context,String network){
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(KEY_NETWORK, network);
        return editor.commit();
    }

    public static String getNetwork(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(KEY_NETWORK, "none");
    }

    public static String getUUID(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString("UUID", "");
    }

    public static boolean setUUID(Context context ,String uuid){
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString("UUID", uuid);
        return editor.commit();
    }

    public static String getUserId(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString("userid", "");
    }

    public static boolean setUserId(Context context ,String userid){
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString("userid", userid);
        return editor.commit();
    }

	public static void initNetwork(Context context){
        ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        try{
            NetworkInfo netInfo = manager.getActiveNetworkInfo();
            AppliteConfig.setNetwork(context, "none");
            switch(netInfo.getType()){
                case ConnectivityManager.TYPE_WIFI:
                    AppliteConfig.setNetwork(context, Constant.WIFI);
                    break;
                case ConnectivityManager.TYPE_MOBILE:
                    AppliteConfig.setNetwork(context, Constant.MOBILE);
                    break;
            }
        }catch(Exception e ){
            e.printStackTrace();
        }
    }
}
