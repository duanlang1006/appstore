package com.mit.appliteupdate.main;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.applite.common.Constant;
import com.applite.common.LogUtils;

import org.apkplug.Bundle.ApkplugOSGIService;
import org.osgi.framework.BundleContext;

/**
 * Created by LSY on 15-6-3.
 */
public class UpdateOSGIServiceImpl implements ApkplugOSGIService {
    @Override
    public Object ApkplugOSGIService(BundleContext bundleContext, String servicename, int node, Object... objs) {
        Log.d(SimpleBundle.TAG, "ApkplugOSGIService,recv service:" + servicename + ",node=" + node);
        if (Constant.OSGI_SERVICE_UPDATE_FRAGMENT.equals(servicename)) {
            FragmentManager fgm = (FragmentManager) objs[0];
            Fragment fg = new UpdateFragment();
            FragmentTransaction ft = fgm.beginTransaction();
            if (null == fgm.findFragmentByTag(Constant.OSGI_SERVICE_UPDATE_FRAGMENT)) {
                if (null != fgm.findFragmentByTag(Constant.OSGI_SERVICE_MAIN_FRAGMENT)) {
                    ft.hide(fgm.findFragmentByTag(Constant.OSGI_SERVICE_MAIN_FRAGMENT));//得到首页Fragment，然后隐藏
                    ft.add(node, fg, Constant.OSGI_SERVICE_UPDATE_FRAGMENT);
                } else {
                    ft.replace(node, fg, Constant.OSGI_SERVICE_UPDATE_FRAGMENT);
                }
                ft.addToBackStack(null);
                ft.commit();
            }
        }
        return null;
    }
}