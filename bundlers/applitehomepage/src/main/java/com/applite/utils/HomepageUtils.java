package com.applite.utils;

import com.applite.common.Constant;
import com.applite.homepage.BundleContextFactory;

import org.apkplug.Bundle.ApkplugOSGIService;
import org.apkplug.Bundle.OSGIServiceAgent;
import org.osgi.framework.BundleContext;

/**
 * Created by LSY on 15-7-15.
 */
public class HomepageUtils {

    /**
     * 去专题页面
     */
    public static void toTopicFragment(String s_key, String s_name, int step, String s_datatype) {
        try {
            BundleContext bundleContext = BundleContextFactory.getInstance().getBundleContext();
            OSGIServiceAgent<ApkplugOSGIService> agent = new OSGIServiceAgent<ApkplugOSGIService>(
                    bundleContext, ApkplugOSGIService.class,
                    "(serviceName=" + Constant.OSGI_SERVICE_HOST_OPT + ")", //服务查询条件
                    OSGIServiceAgent.real_time);   //每次都重新查询
            agent.getService().ApkplugOSGIService(bundleContext,
                    Constant.OSGI_SERVICE_DM_FRAGMENT,
                    0, Constant.OSGI_SERVICE_TOPIC_FRAGMENT, s_key, s_name, step, s_datatype);
        } catch (Exception e) {
            // T
            e.printStackTrace();
        }
    }
}
