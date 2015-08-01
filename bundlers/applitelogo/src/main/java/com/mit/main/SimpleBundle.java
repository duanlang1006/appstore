package com.mit.main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.applite.common.Constant;
import com.applite.common.LogUtils;
import com.osgi.extra.OSGIBaseFragment;
import com.osgi.extra.OSGIServiceClient;
import com.osgi.extra.OSGIServiceHost;
import org.apkplug.Bundle.OSGIServiceAgent;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.lang.reflect.Constructor;
import java.util.Dictionary;
import java.util.Hashtable;

public class SimpleBundle implements BundleActivator{
    static final String TAG = "logo_simplebundle";
    private ServiceRegistration mServiceReg = null;

    public void start(BundleContext mcontext){
        BundleContextFactory.getInstance().setBundleContext(mcontext);
        Log.d(TAG, "simplebundle start");

        Dictionary<String,Object> properties =new Hashtable<String,Object>();
        properties.put("serviceName", Constant.OSGI_SERVICE_LOGO_FRAGMENT);
        OSGIServiceClient service = new LogoOSGIServiceImpl();
        //注册一个服务给Host调用
        mServiceReg = mcontext.registerService(
                OSGIServiceClient.class.getName(),
                service,
                properties);
    }
   
    public void stop(BundleContext mcontext){
        Log.d(TAG,"simplebundle stop");
        if (null != mServiceReg){
            mServiceReg.unregister();
        }
    }


    public class LogoOSGIServiceImpl extends OSGIServiceClient {
        @Override
        public void launchOSGIFragment(String servicename, Bundle params) {
            LogUtils.d(SimpleBundle.TAG, "ApkplugOSGIService,recv service:" + servicename);
            if (Constant.OSGI_SERVICE_LOGO_FRAGMENT.equals(servicename)){
                try {
                    BundleContext bundleContext = BundleContextFactory.getInstance().getBundleContext();
                    OSGIServiceHost host = new OSGIServiceAgent<OSGIServiceHost>(
                            bundleContext,
                            OSGIServiceHost.class,
                            "(serviceName="+Constant.OSGI_SERVICE_HOST_OPT+")", //服务查询条件
                            OSGIServiceAgent.real_time).getService();   //每次都重新查询
                    Fragment fg = GuideFragment.newInstance(host, params);
                    FragmentManager fgm = host.getFragmentManager();
                    fgm.beginTransaction()
                            .add(host.getNode(), fg, Constant.OSGI_SERVICE_LOGO_FRAGMENT)
                            .commitAllowingStateLoss();
                } catch (Exception e) {
                    // TODO 自动生成的 catch 块
                    e.printStackTrace();
                }
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
    }
}
