package com.applite.dm;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
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

public class SimpleBundle implements BundleActivator{
    static final String TAG = "applite_dm";
    private ServiceRegistration mServiceReg = null;

    public void start(BundleContext mcontext){
        BundleContextFactory.getInstance().setBundleContext(mcontext);
        Log.d(TAG, "simplebundle start,"+Constant.OSGI_SERVICE_DM_FRAGMENT);

        Dictionary<String,Object> properties =new Hashtable<String,Object>();
        properties.put("serviceName", Constant.OSGI_SERVICE_DM_FRAGMENT);
        OSGIServiceClient service = new DmOSGIServiceImpl();
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

    public class DmOSGIServiceImpl extends OSGIServiceClient {
        @Override
        public void launchOSGIFragment(String servicename, Fragment fragment, Bundle bundle) {
            OSGIServiceHost host = AppliteUtils.getHostOSGIService(BundleContextFactory.getInstance().getBundleContext());
            if (null != host && Constant.OSGI_SERVICE_DM_FRAGMENT.equals(servicename)){
                FragmentManager fgm = host.getFragmentManager();
                FragmentTransaction ft = fgm.beginTransaction();
                ft.replace(host.getNode(), fragment, Constant.OSGI_SERVICE_DM_FRAGMENT);
                ft.addToBackStack(null);
                ft.commit();
            }
        }

        @Override
        public OSGIBaseFragment newOSGIFragment(Fragment container, String whichService,String whichFragment, Bundle params) {
            OSGIBaseFragment baseFragment = null;
            if (null != whichFragment && !TextUtils.isEmpty(whichFragment)){
                try {
                    Class<?> cls = Class.forName(whichFragment);
                    Constructor ct = cls.getDeclaredConstructor(Fragment.class,Bundle.class);
                    ct.setAccessible(true);
                    baseFragment = (OSGIBaseFragment)ct.newInstance(container,params);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (null == baseFragment) {
                if (Constant.OSGI_SERVICE_DETAIL_FRAGMENT.equals(whichService)) {
                    baseFragment = DownloadPagerFragment.newInstance(container, params);
                }
            }
            return baseFragment;
        }
    }
}
