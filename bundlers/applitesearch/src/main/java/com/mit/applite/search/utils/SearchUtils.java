package com.mit.applite.search.utils;

import android.content.Context;
import android.os.Bundle;

import com.applite.common.AppliteUtils;
import com.applite.common.Constant;
import com.mit.applite.search.R;
import com.mit.applite.search.main.BundleContextFactory;
import com.osgi.extra.OSGIServiceHost;
import org.osgi.framework.BundleContext;

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

    /**
     * 去详情页面
     */
    public static void toDetailFragment(String packageName, String name, String imgUrl) {
        BundleContext bundleContext = BundleContextFactory.getInstance().getBundleContext();
        OSGIServiceHost host = AppliteUtils.getHostOSGIService(bundleContext);
        if (null != host){
            Bundle b = new Bundle();
            b.putString("packageName",packageName);
            b.putString("name",name);
            b.putString("imgUrl",imgUrl);
            b.putString("from",Constant.OSGI_SERVICE_SEARCH_FRAGMENT);
            host.jumpto(bundleContext, Constant.OSGI_SERVICE_DETAIL_FRAGMENT, b);
        }
    }

    /**
     * 去主题页面
     */
    public static void toTopicFragment(String key, String name, int step, String datatype) {
        BundleContext bundleContext = BundleContextFactory.getInstance().getBundleContext();
        OSGIServiceHost host = AppliteUtils.getHostOSGIService(bundleContext);
        if (null != host){
            Bundle b = new Bundle();
            b.putString("key",key);
            b.putString("name",name);
            b.putInt("step",step);
            b.putString("datatype",datatype);
            b.putString("from",Constant.OSGI_SERVICE_SEARCH_FRAGMENT);
            host.jumpto(bundleContext, Constant.OSGI_SERVICE_TOPIC_FRAGMENT, b);
        }
    }

}
