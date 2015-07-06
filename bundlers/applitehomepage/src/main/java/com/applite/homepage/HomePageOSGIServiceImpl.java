package com.applite.homepage;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.applite.bean.SubjectData;
import com.applite.common.Constant;
import com.applite.common.LogUtils;

import org.apkplug.Bundle.ApkplugOSGIService;
import org.osgi.framework.BundleContext;

public class HomePageOSGIServiceImpl implements ApkplugOSGIService {
    public static int node = 0;

	@Override
	public Object ApkplugOSGIService(BundleContext arg0, String servicename, int node,Object... objs) {
        LogUtils.d(SimpleBundle.TAG, "ApkplugOSGIService,recv service:" + servicename + ",node=" + node);
        Bundle b = null;
        if (Constant.OSGI_SERVICE_MAIN_FRAGMENT.equals(servicename)){
            Fragment fg = new HomePageFragment();
            for (int i = 1;i < objs.length;i++){
                if (null == b){
                    b = new Bundle();
                }
                b.putString("param"+i,(String)objs[i]);
            }
            if (null != b){
                fg.setArguments(b);;
            }
            FragmentManager fgm = (FragmentManager)objs[0];
            FragmentTransaction ft = fgm.beginTransaction();
            if (null == b) {
                ft.replace(node, fg, Constant.OSGI_SERVICE_MAIN_FRAGMENT).commit();
            }else{
                ft.replace(node, fg, Constant.OSGI_SERVICE_MAIN_FRAGMENT);
                ft.addToBackStack(null);
                ft.commit();
            }
        }else if (Constant.OSGI_SERVICE_TOPIC_FRAGMENT.equals(servicename)){
            Fragment fg = new HomePageListFragment((SubjectData)objs[1]);
            b = new Bundle();
            b.putString("entry","topic");
            fg.setArguments(b);
            FragmentManager fgm = (FragmentManager)objs[0];
            FragmentTransaction ft = fgm.beginTransaction();
            ft.replace(node, fg,Constant.OSGI_SERVICE_TOPIC_FRAGMENT);
            ft.addToBackStack(null);
            ft.commit();
        }
		return null;
	}
}
