package com.applite.dm.utils;

import android.os.Bundle;

import com.applite.common.AppliteUtils;
import com.applite.common.Constant;
import com.applite.dm.BundleContextFactory;
import com.osgi.extra.OSGIServiceHost;

import org.osgi.framework.BundleContext;


/**
 * Created by LSY on 15-7-15.
 */
public class HostUtils {
    /****
     * 搜索
     */
    public static void launchSearchFragment(OSGIServiceHost host) {
        BundleContext bundleContext = BundleContextFactory.getInstance().getBundleContext();
        if (null != host){
            host.jumpto(bundleContext,Constant.OSGI_SERVICE_SEARCH_FRAGMENT,null,
                    AppliteUtils.putFgParams(new Bundle(),Constant.OSGI_SERVICE_MAIN_FRAGMENT,"replace",true));
        }
    }

    public static void launchDetail(OSGIServiceHost host,String packageName,String title,String iconUrl){
        BundleContext bundleContext = BundleContextFactory.getInstance().getBundleContext();
        if (null != host){
            Bundle bundle = new Bundle();
            bundle.putString("packageName",packageName);
            bundle.putString("name",title);
            bundle.putString("iconUrl",iconUrl);
            AppliteUtils.putFgParams(bundle,Constant.OSGI_SERVICE_DM_FRAGMENT,"replace",true);
            host.jumpto(bundleContext,Constant.OSGI_SERVICE_DETAIL_FRAGMENT,null,bundle);
        }
    }
}
