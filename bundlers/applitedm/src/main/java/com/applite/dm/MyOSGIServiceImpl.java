package com.applite.dm;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.applite.common.Constant;

import org.apkplug.Bundle.ApkplugOSGIService;
import org.osgi.framework.BundleContext;

public class MyOSGIServiceImpl implements ApkplugOSGIService {
	@Override
	public Object ApkplugOSGIService(BundleContext arg0, String servicename, int node,Object... objs) {
        if (Constant.OSGI_SERVICE_DM_FRAGMENT.equals(servicename)){
            Fragment fg = new DownloadPagerFragment();
            FragmentManager fgm = (FragmentManager)objs[0];
            FragmentTransaction ft = fgm.beginTransaction();
            ft.replace(node, fg, Constant.OSGI_SERVICE_DM_FRAGMENT);
            ft.addToBackStack(null);
            ft.commit();
        }
		return null;
	}

}
