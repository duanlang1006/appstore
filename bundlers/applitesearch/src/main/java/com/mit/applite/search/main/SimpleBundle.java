package com.mit.applite.search.main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.applite.common.AppliteUtils;
import com.applite.common.Constant;
import com.osgi.extra.OSGIBaseFragment;
import com.osgi.extra.OSGIServiceHost;
import com.osgi.extra.OSGIServiceClient;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.lang.reflect.Constructor;
import java.util.Dictionary;
import java.util.Hashtable;

public class SimpleBundle implements BundleActivator {
    static final String TAG = "search_simplebundle";
    private ServiceRegistration mServiceReg = null;

    public void start(BundleContext mcontext) {
        BundleContextFactory.getInstance().setBundleContext(mcontext);
        Log.d(TAG, "simplebundle start");

        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put("serviceName", Constant.OSGI_SERVICE_SEARCH_FRAGMENT);
        OSGIServiceClient service = new SearchOSGIServiceImpl();
        //注册一个服务给Host调用
        mServiceReg = mcontext.registerService(
                OSGIServiceClient.class.getName(),
                service,
                properties);
    }

    public void stop(BundleContext mcontext) {
        Log.d(TAG, "simplebundle stop");
        if (null != mServiceReg) {
            mServiceReg.unregister();
        }
    }

    public class SearchOSGIServiceImpl extends OSGIServiceClient {
        @Override
        public void launchOSGIFragment(String servicename, Bundle bundle) {
            OSGIServiceHost host = AppliteUtils.getHostOSGIService(BundleContextFactory.getInstance().getBundleContext());
            if (Constant.OSGI_SERVICE_SEARCH_FRAGMENT.equals(servicename) && null != host) {
                Fragment fg = SearchFragment.newInstance(host,bundle);
                FragmentManager fgm = host.getFragmentManager();
                FragmentTransaction ft = fgm.beginTransaction();
                ft.hide(fgm.findFragmentByTag(Constant.OSGI_SERVICE_MAIN_FRAGMENT));//得到首页Fragment，然后隐藏
                ft.add(host.getNode(), fg, Constant.OSGI_SERVICE_SEARCH_FRAGMENT);
                ft.addToBackStack(null);
                ft.commit();
            }
        }

        @Override
        public OSGIBaseFragment newOSGIFragment(Fragment container, String whichFragment, Bundle params) {
            OSGIBaseFragment fg = null;
            try {
                Class<?> cls = Class.forName(whichFragment);
                Constructor ct = cls.getDeclaredConstructor(Fragment.class,Bundle.class);
                ct.setAccessible(true);
                fg = (OSGIBaseFragment)ct.newInstance(container,params);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return fg;
        }
//        @Override
//        public Object ApkplugOSGIService(BundleContext bundleContext, String servicename, int node, Object... objs) {
//            Log.d(SimpleBundle.TAG, "ApkplugOSGIService,recv service:" + servicename + ",node=" + node);
//            if (Constant.OSGI_SERVICE_SEARCH_FRAGMENT.equals(servicename)) {
//                Fragment fg = new SearchFragment();
//                FragmentManager fgm = (FragmentManager) objs[0];
//                FragmentTransaction ft = fgm.beginTransaction();
//                ft.hide(fgm.findFragmentByTag(Constant.OSGI_SERVICE_MAIN_FRAGMENT));//得到首页Fragment，然后隐藏
//                ft.add(node, fg, Constant.OSGI_SERVICE_SEARCH_FRAGMENT);
////            ft.replace(node, fg, Constant.OSGI_SERVICE_SEARCH_FRAGMENT);
//                ft.addToBackStack(null);
//                ft.commit();
//            }
//            return null;
//        }
    }
}
