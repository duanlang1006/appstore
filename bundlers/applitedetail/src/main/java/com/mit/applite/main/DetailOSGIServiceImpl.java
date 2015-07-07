package com.mit.applite.main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.applite.common.Constant;

import org.apkplug.Bundle.ApkplugOSGIService;
import org.osgi.framework.BundleContext;

/**
 * Created by LSY on 15-6-3.
 */
public class DetailOSGIServiceImpl implements ApkplugOSGIService {
    @Override
    public Object ApkplugOSGIService(BundleContext bundleContext, String servicename, int node, Object... objs) {
        if (Constant.OSGI_SERVICE_DETAIL_FRAGMENT.equals(servicename)) {
            Fragment fg = new DetailFragment();
            FragmentManager fgm = (FragmentManager) objs[0];
            FragmentTransaction ft = fgm.beginTransaction();
            Bundle bundle = new Bundle();
            bundle.putString("packageName", (String) objs[1]);
            bundle.putString("name", (String) objs[2]);
            bundle.putString("imgUrl", (String) objs[3]);
            fg.setArguments(bundle);
//            ft.hide(fgm.findFragmentByTag(Constant.OSGI_SERVICE_SEARCH_FRAGMENT));//得到搜索Fragment，然后隐藏
            ft.add(node, fg, Constant.OSGI_SERVICE_DETAIL_FRAGMENT);
//            ft.replace(node, fg, Constant.OSGI_SERVICE_DETAIL_FRAGMENT);
            ft.addToBackStack(null);
            ft.commit();
        }
        return null;
    }
}
