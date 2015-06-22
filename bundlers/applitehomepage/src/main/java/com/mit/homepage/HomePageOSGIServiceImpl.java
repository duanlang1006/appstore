package com.mit.homepage;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.applite.common.Constant;
import com.mit.utils.HomePageUtils;

import org.apkplug.Bundle.ApkplugOSGIService;
import org.osgi.framework.BundleContext;

public class HomePageOSGIServiceImpl implements ApkplugOSGIService {
	@Override
	public Object ApkplugOSGIService(BundleContext arg0, String servicename, int node,Object... objs) {
        HomePageUtils.d(SimpleBundle.TAG,"ApkplugOSGIService,recv service:"+servicename+",node="+node);

        if (SimpleBundle.OSGI_SERVICE_LOGO_FRAGMENT.equals(servicename)){
            Fragment fg = new HomePageFragment();
            ((HomePageFragment)fg).saveNode(node);
            FragmentManager fgm = (FragmentManager)objs[0];
            fgm.beginTransaction().replace(node, fg,Constant.OSGI_SERVICE_MAIN_FRAGMENT).commit();

        }
		return null;
	}
}
