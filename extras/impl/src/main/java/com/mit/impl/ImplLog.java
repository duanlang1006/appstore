package com.mit.impl;

import android.util.Log;

/**
 * Created by hxd on 15-6-18.
 */
public class ImplLog {
    private static boolean DEBUG = true;
    public static void d(String tag,String text){
        if (DEBUG) {
            Log.d(tag, text);
        }
    }
}
