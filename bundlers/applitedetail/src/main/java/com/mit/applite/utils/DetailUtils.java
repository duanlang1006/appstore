package com.mit.applite.utils;

import android.os.Bundle;

import com.applite.common.AppliteUtils;
import com.applite.common.Constant;
import com.osgi.extra.OSGIServiceHost;

/**
 * Created by LSY on 15-5-22.
 */
public class DetailUtils {

    private static final String TAG = "DetailUtils";
    /****
     * 搜索
     */
    public static void launchSearchFragment(OSGIServiceHost host) {
        if (null != host){
            host.jumpto(Constant.OSGI_SERVICE_SEARCH_FRAGMENT,null,
                    AppliteUtils.putFgParams(new Bundle(), Constant.OSGI_SERVICE_MAIN_FRAGMENT, "add", true));
        }
    }
}
