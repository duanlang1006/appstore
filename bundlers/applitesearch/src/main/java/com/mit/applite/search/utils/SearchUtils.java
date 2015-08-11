package com.mit.applite.search.utils;

import android.content.Context;
import android.os.Bundle;

import com.applite.common.AppliteUtils;
import com.applite.common.Constant;
import com.applite.common.LogUtils;
import com.mit.applite.search.R;
import com.osgi.extra.OSGIServiceHost;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by LSY on 15-5-22.
 */
public class SearchUtils {

    private static final String TAG = "SearchUtils";
    private static Pattern mPattern;
    private static Matcher mMatcher;

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

    /**
     * 判断字符串是不是由字母组成
     *
     * @param s
     * @return
     */
    public static boolean isLetter(String s) {
        mPattern = Pattern.compile("[a-zA-Z]+");
        mMatcher = mPattern.matcher(s);
        LogUtils.i(TAG, "字符串是不是由字母组成:" + mMatcher.matches());
        return mMatcher.matches();
    }

    /**
     * 去详情页面
     */
    public static void toDetailFragment(OSGIServiceHost host, String packageName, String name, String imgUrl) {
        if (null != host) {
            Bundle b = new Bundle();
            b.putString("packageName", packageName);
            b.putString("name", name);
            b.putString("imgUrl", imgUrl);
            AppliteUtils.putFgParams(b, Constant.OSGI_SERVICE_SEARCH_FRAGMENT, "add", true);
            host.jumpto( Constant.OSGI_SERVICE_DETAIL_FRAGMENT, null, b);
        }
    }

    /**
     * 去主题页面
     */
    public static void toTopicFragment(OSGIServiceHost host, String key, String name, int step, String datatype) {
        if (null != host) {
            Bundle b = new Bundle();
            b.putString("key", key);
            b.putString("name", name);
            b.putInt("step", step);
            b.putString("datatype", datatype);
            AppliteUtils.putFgParams(b, Constant.OSGI_SERVICE_SEARCH_FRAGMENT, "add", true);
            host.jumpto(Constant.OSGI_SERVICE_TOPIC_FRAGMENT, null, b);
        }
    }

}
