package com.mit.impl;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by hxd on 15-9-9.
 */
public class ImplConfig {
    static final String IMPL_CONFIG = "impl_config";
    static void setDeleteAfterInstalled(Context context,boolean delete){
        SharedPreferences sp = context.getSharedPreferences(IMPL_CONFIG,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("DeleteAfterInstalled",delete).apply();
    }

    static boolean getDeleteAfterInstalled(Context context){
        SharedPreferences sp = context.getSharedPreferences(IMPL_CONFIG,Context.MODE_PRIVATE);
        return sp.getBoolean("DeleteAfterInstalled",false);
    }

    static void setMaxOverSize(Context context,long size){
        SharedPreferences sp = context.getSharedPreferences(IMPL_CONFIG,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong("MaxOverSize",size).apply();
    }

    static long getMaxOverSize(Context context){
        SharedPreferences sp = context.getSharedPreferences(IMPL_CONFIG,Context.MODE_PRIVATE);
        return sp.getLong("MaxOverSize",1024*1000);
    }
}
