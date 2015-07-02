package com.applite.homepage;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.applite.bean.SubjectData;
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
        }else if (Constant.OSGI_SERVICE_TOPIC_FRAGMENT.equals(servicename)){
            Fragment fg = new HomePageListFragment((SubjectData)objs[1]);
            FragmentManager fgm = (FragmentManager)objs[0];
            FragmentTransaction ft = fgm.beginTransaction();
            ft.replace(node, fg,Constant.OSGI_SERVICE_TOPIC_FRAGMENT);
            ft.addToBackStack(null);
            ft.commit();
        }
		return null;
	}
}
