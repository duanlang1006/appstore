package com.applite.homepage;

import android.support.v4.app.Fragment;

import android.support.v4.app.FragmentManager;

import com.applite.common.Constant;
import com.applite.common.LogUtils;

import org.apkplug.Bundle.ApkplugOSGIService;
import org.osgi.framework.BundleContext;

public class HomePageOSGIServiceImpl implements ApkplugOSGIService {
	@Override
	public Object ApkplugOSGIService(BundleContext arg0, String servicename, int node,Object... objs) {
        LogUtils.d(SimpleBundle.TAG, "ApkplugOSGIService,recv service:" + servicename + ",node=" + node);

        if (Constant.OSGI_SERVICE_MAIN_FRAGMENT.equals(servicename)){
            Fragment fg = new HomePageFragment();
            ((HomePageFragment)fg).saveNode(node);
            FragmentManager fgm = (FragmentManager)objs[0];
            fgm.beginTransaction().replace(node, fg,Constant.OSGI_SERVICE_MAIN_FRAGMENT).commit();

        }
		return null;
	}
}
