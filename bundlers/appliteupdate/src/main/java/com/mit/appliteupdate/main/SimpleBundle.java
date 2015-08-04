package com.mit.appliteupdate.main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.applite.common.AppliteUtils;
import com.applite.common.Constant;
import com.applite.common.LogUtils;
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
    static final String TAG = "update_simplebundle";
    private ServiceRegistration mServiceReg = null;

    public void start(BundleContext mcontext) {
        BundleContextFactory.getInstance().setBundleContext(mcontext);
        Log.d(TAG, "simplebundle start");

        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put("serviceName", Constant.OSGI_SERVICE_UPDATE_FRAGMENT);
        OSGIServiceClient service = new UpdateOSGIServiceImpl();
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


    public class UpdateOSGIServiceImpl extends OSGIServiceClient {
        @Override
        public OSGIBaseFragment newOSGIFragment(Fragment container, String whichService, String whichFragment, Bundle params) {
            OSGIBaseFragment baseFragment = null;
            try {
                Class<?> cls = Class.forName(whichFragment);
                Constructor ct = cls.getDeclaredConstructor(Fragment.class,Bundle.class);
                ct.setAccessible(true);
                baseFragment = (OSGIBaseFragment)ct.newInstance(container,params);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (null == baseFragment){
                if (Constant.OSGI_SERVICE_UPDATE_FRAGMENT.equals(whichService)){
                    baseFragment = UpdateFragment.newInstance(container,params);
                }
            }

            return baseFragment;
        }
    }
}
