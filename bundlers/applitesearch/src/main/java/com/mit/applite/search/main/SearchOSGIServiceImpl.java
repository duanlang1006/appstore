package com.mit.applite.search.main;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.applite.common.Constant;

import org.apkplug.Bundle.ApkplugOSGIService;
import org.osgi.framework.BundleContext;

/**
 * Created by LSY on 15-6-3.
 */
public class SearchOSGIServiceImpl implements ApkplugOSGIService {
    @Override
    public Object ApkplugOSGIService(BundleContext bundleContext, String servicename, int node, Object... objs) {
        Log.d(SimpleBundle.TAG, "ApkplugOSGIService,recv service:" + servicename + ",node=" + node);
        if (Constant.OSGI_SERVICE_SEARCH_FRAGMENT.equals(servicename)) {
            Fragment fg = new SearchFragment();
            FragmentManager fgm = (FragmentManager) objs[0];
            fgm.beginTransaction().add(node, fg,Constant.OSGI_SERVICE_SEARCH_FRAGMENT).commit();
        }
        return null;
    }
}
