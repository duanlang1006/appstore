package com.mit.appliteupdate.main;

import android.util.Log;

import com.applite.common.Constant;

import org.apkplug.Bundle.ApkplugOSGIService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.Dictionary;
import java.util.Hashtable;

public class SimpleBundle implements BundleActivator {
    static final String TAG = "update_simplebundle";
    private ServiceRegistration mServiceReg = null;

    public void start(BundleContext mcontext) {
        BundleContextFactory.getInstance().setBundleContext(mcontext);
        Log.d(TAG, "simplebundle start");

        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put("serviceName", Constant.OSGI_SERVICE_UPDATE_FRAGMENT);
        ApkplugOSGIService service = new UpdateOSGIServiceImpl();
        //注册一个服务给Host调用
        mServiceReg = mcontext.registerService(
                ApkplugOSGIService.class.getName(),
                service,
                properties);
    }

    public void stop(BundleContext mcontext) {
        Log.d(TAG, "simplebundle stop");
        if (null != mServiceReg) {
            mServiceReg.unregister();
        }
    }
}
