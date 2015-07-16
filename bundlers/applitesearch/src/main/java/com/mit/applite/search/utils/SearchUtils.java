package com.mit.applite.search.utils;

import android.content.Context;

import com.applite.common.Constant;
import com.mit.applite.search.R;
import com.mit.applite.search.main.BundleContextFactory;

import org.apkplug.Bundle.ApkplugOSGIService;
import org.apkplug.Bundle.OSGIServiceAgent;
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
        try {
            BundleContext bundleContext = BundleContextFactory.getInstance().getBundleContext();
            OSGIServiceAgent<ApkplugOSGIService> agent = new OSGIServiceAgent<ApkplugOSGIService>(
                    bundleContext, ApkplugOSGIService.class,
                    "(serviceName=" + Constant.OSGI_SERVICE_HOST_OPT + ")", //服务查询条件
                    OSGIServiceAgent.real_time);   //每次都重新查询
            agent.getService().ApkplugOSGIService(bundleContext,
                    Constant.OSGI_SERVICE_SEARCH_FRAGMENT,
                    0, Constant.OSGI_SERVICE_DETAIL_FRAGMENT, packageName, name, imgUrl, Constant.OSGI_SERVICE_SEARCH_FRAGMENT);
        } catch (Exception e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        }
    }

    /**
     * 去主题页面
     */
    public static void toTopicFragment(String key, String name, int step, String datatype) {
        try {
            BundleContext bundleContext = BundleContextFactory.getInstance().getBundleContext();
            OSGIServiceAgent<ApkplugOSGIService> agent = new OSGIServiceAgent<ApkplugOSGIService>(
                    bundleContext, ApkplugOSGIService.class,
                    "(serviceName=" + Constant.OSGI_SERVICE_HOST_OPT + ")", //服务查询条件
                    OSGIServiceAgent.real_time);   //每次都重新查询
            agent.getService().ApkplugOSGIService(bundleContext,
                    Constant.OSGI_SERVICE_SEARCH_FRAGMENT,
                    0, Constant.OSGI_SERVICE_TOPIC_FRAGMENT, key, name, step, datatype);
        } catch (Exception e) {
            // T
            e.printStackTrace();
        }
    }

}
