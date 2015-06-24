package com.mit.applite.search.utils;

import android.content.Context;

import com.mit.applite.search.R;

/**
 * Created by LSY on 15-5-22.
 */
public class SearchUtils {

    private static final String TAG = "SearchUtils";

    public static String getDownloadNumber(Context context, int number) {
        String s = null;
        if (number > 1000000) {
            s = ">100" + context.getResources().getString(R.string.wan);
        } else if (number > 500000) {
            s = ">50" + context.getResources().getString(R.string.wan);
        } else if (number > 300000) {
            s = ">30" + context.getResources().getString(R.string.wan);
        } else if (number > 200000) {
            s = ">20" + context.getResources().getString(R.string.wan);
        } else if (number > 100000) {
            s = ">10" + context.getResources().getString(R.string.wan);
        } else if (number <= 100000) {
            s = number + "";
        }
        return s;
    }

}
