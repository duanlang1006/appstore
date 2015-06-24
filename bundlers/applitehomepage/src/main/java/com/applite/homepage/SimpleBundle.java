package com.applite.homepage;

import android.util.Log;

import org.apkplug.Bundle.ApkplugOSGIService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.Dictionary;
import java.util.Hashtable;

public class SimpleBundle implements BundleActivator{
    static final String TAG = "main_simplebundle";
    static final String OSGI_SERVICE_LOGO_FRAGMENT = "osgi.service.main.fragment";
    private ServiceRegistration mServiceReg = null;

    public void start(BundleContext mcontext){
        BundleContextFactory.getInstance().setBundleContext(mcontext);
        Log.d(TAG, "simplebundle start");

        Dictionary<String,Object> properties =new Hashtable<String,Object>();
        properties.put("serviceName", OSGI_SERVICE_LOGO_FRAGMENT);
        ApkplugOSGIService service = new HomePageOSGIServiceImpl();
        //注册一个服务给Host调用
        mServiceReg = mcontext.registerService(
                ApkplugOSGIService.class.getName(),
                service,
                properties);
    }
   
    public void stop(BundleContext mcontext){
        Log.d(TAG,"simplebundle stop");
        if (null != mServiceReg){
            mServiceReg.unregister();
        }
    }
}
