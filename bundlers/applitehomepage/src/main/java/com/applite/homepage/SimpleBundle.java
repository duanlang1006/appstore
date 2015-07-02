package com.applite.homepage;

import android.util.Log;

import com.applite.common.Constant;
import com.applite.common.LogUtils;

import org.apkplug.Bundle.ApkplugOSGIService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.Dictionary;
import java.util.Hashtable;

public class SimpleBundle implements BundleActivator{
    static final String TAG = "main_simplebundle";
    private ServiceRegistration mServiceReg = null;
    private ServiceRegistration mServiceTopicReg = null;

    public void start(BundleContext mcontext){
        BundleContextFactory.getInstance().setBundleContext(mcontext);
        LogUtils.d(TAG, "simplebundle start");

        Dictionary<String,Object> properties =new Hashtable<String,Object>();
        properties.put("serviceName", Constant.OSGI_SERVICE_MAIN_FRAGMENT);
        ApkplugOSGIService service = new HomePageOSGIServiceImpl();
        //注册一个服务给Host调用
        mServiceReg = mcontext.registerService(
                ApkplugOSGIService.class.getName(),
                service,
                properties);

        properties.put("serviceName", Constant.OSGI_SERVICE_TOPIC_FRAGMENT);
        //注册一个服务给Host调用
        mServiceTopicReg = mcontext.registerService(
                ApkplugOSGIService.class.getName(),
                service,
                properties);
    }
   
    public void stop(BundleContext mcontext){
        LogUtils.d(TAG,"simplebundle stop");
        if (null != mServiceReg){
            mServiceReg.unregister();
        }

        if (null != mServiceTopicReg){
            mServiceTopicReg.unregister();
        }
    }
}
