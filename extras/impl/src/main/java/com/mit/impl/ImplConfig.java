package com.mit.impl;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by hxd on 15-9-9.
 */
public class ImplConfig {
    static final String IMPL_CONFIG = "impl_config";
    public static void setDeleteAfterInstalled(Context context,boolean delete){
        SharedPreferences sp = context.getSharedPreferences(IMPL_CONFIG,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("DeleteAfterInstalled",delete).apply();
    }

    public static boolean getDeleteAfterInstalled(Context context){
        SharedPreferences sp = context.getSharedPreferences(IMPL_CONFIG,Context.MODE_PRIVATE);
        return sp.getBoolean("DeleteAfterInstalled",false);
    }

    public static void setMaxOverSize(Context context,long size){
        SharedPreferences sp = context.getSharedPreferences(IMPL_CONFIG,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong("MaxOverSize", size).apply();
    }

    public static long getMaxOverSize(Context context){
        SharedPreferences sp = context.getSharedPreferences(IMPL_CONFIG,Context.MODE_PRIVATE);
        return sp.getLong("MaxOverSize", 1024 * 1000);
    }

    public static void setDownloadThreadNum(Context context,int size){
        SharedPreferences sp = context.getSharedPreferences(IMPL_CONFIG,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("DownloadThreadNum", size).apply();
    }

    public static int getDownloadThreadNum(Context context){
        SharedPreferences sp = context.getSharedPreferences(IMPL_CONFIG,Context.MODE_PRIVATE);
        return sp.getInt("DownloadThreadNum",3);
    }

}
