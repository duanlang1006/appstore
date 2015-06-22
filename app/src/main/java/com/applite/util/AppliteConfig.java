package com.applite.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import com.android.applite.model.IAppInfo;
import com.android.applite.model.UpdateDataModel;

public class AppliteConfig {
	//keys
	public static final String KEY_NETWORK = "network";
	public static final String KEY_UPDATE_URL = "update_url";
	public static final String KEY_UPDATE_INTERVAL = "update_interval";
	public static final String KEY_UPDATE_LAST_TIME = "update_lasttime";
	public static final String KEY_BDUSER_ID = "bduser_id";
	public static final String KEY_PROJECT = "project";
	public static final String KYE_HIDDEN = "hidden";
	public static final String KYE_DATA_TIMESTAMP = "data_timestamp";
	
    public static boolean setNetwork(Context context,String network){
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(KEY_NETWORK, network);
        return editor.commit();
    }
    
    public static String getNetwork(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(KEY_NETWORK, "none");
    }
	
    public static boolean setUpdateMillis(Context context,int category){
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putLong(KEY_UPDATE_LAST_TIME+"_"+category, System.currentTimeMillis());
        return editor.commit();
    }

    public static boolean setUpdateServer(Context context,String url){
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(KEY_UPDATE_URL, url);
        return editor.commit();
    }
	
    public static String getUpdateServer(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
    	return sp.getString(KEY_UPDATE_URL, UpdateDataModel.getDefaultData(context).getUpdate_url());
    }
	
    public static long getUpdateMillis(Context context,int category) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        long value = sp.getLong(KEY_UPDATE_LAST_TIME+"_"+category, 0);
        return value;
    }
    
    public static boolean setProject(Context context,int project){
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putInt(KEY_PROJECT, project);
        return editor.commit();
    }
    
    public static int getProject(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return  sp.getInt(KEY_PROJECT, IAppInfo.CatgoryNone);
    }
	    
	public static boolean setRecommendHidden(Context context,boolean hidden){
	    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
	    editor.putBoolean(KYE_HIDDEN, hidden);
        return editor.commit();
	}
	
	public static boolean getRecommendHidden(Context context){
	    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(KYE_HIDDEN, UpdateDataModel.getDefaultData(context).getRecommend_hide()==1);
	}
	
	public static int getUpdateInterval(Context context){
	    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(KEY_UPDATE_INTERVAL, UpdateDataModel.getDefaultData(context).getUpdate_interval());
	}
	
	public static boolean setUpdateInterval(Context context,int hour){
	    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putInt(KEY_UPDATE_INTERVAL, hour);
        return editor.commit();
	}
	
	public static long getDataTimestamp(Context context){
	    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getLong(KYE_DATA_TIMESTAMP, UpdateDataModel.getDefaultData(context).getData_timestamp());
	}
	
	public static boolean setDataTimestamp(Context context ,long stamp){
	    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putLong(KYE_DATA_TIMESTAMP, stamp);
        return editor.commit();
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
                    AppliteConfig.setNetwork(context, "wifi");
                    break;
                case ConnectivityManager.TYPE_MOBILE:
                    AppliteConfig.setNetwork(context, "mobile");
                    break;
            }
        }catch(Exception e ){
            e.printStackTrace();
        }
    }
}
