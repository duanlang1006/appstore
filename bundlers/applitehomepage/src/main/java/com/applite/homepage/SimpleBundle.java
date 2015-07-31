package com.applite.homepage;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import com.applite.bean.HomePageApkData;
import com.applite.bean.SubjectData;
import com.applite.common.AppliteUtils;
import com.applite.common.Constant;
import com.applite.common.LogUtils;
import com.osgi.extra.OSGIBaseFragment;
import com.osgi.extra.OSGIServiceHost;
import com.osgi.extra.OSGIServiceClient;

import org.apkplug.Bundle.OSGIServiceAgent;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
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
        OSGIServiceClient service = new HomePageOSGIServiceImpl();
        //注册一个服务给Host调用
        mServiceReg = mcontext.registerService(
                OSGIServiceClient.class.getName(),
                service,
                properties);

        properties.put("serviceName", Constant.OSGI_SERVICE_TOPIC_FRAGMENT);
        //注册一个服务给Host调用
        mServiceTopicReg = mcontext.registerService(
                OSGIServiceClient.class.getName(),
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


    public class HomePageOSGIServiceImpl extends OSGIServiceClient {
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

        @Override
        public void launchOSGIFragment(String servicename, Bundle params) {
            BundleContext bundleContext = BundleContextFactory.getInstance().getBundleContext();
            OSGIServiceHost host = AppliteUtils.getHostOSGIService(bundleContext);

            if (null != host && Constant.OSGI_SERVICE_MAIN_FRAGMENT.equals(servicename)){
                Fragment fg = HomePageFragment.newInstance(host,params);
                FragmentManager fgm = host.getFragmentManager();
                FragmentTransaction ft = fgm.beginTransaction();
                ft.replace(host.getNode(), fg, Constant.OSGI_SERVICE_MAIN_FRAGMENT);
                if (null != params) {
                    ft.addToBackStack(null);
                }
                ft.commitAllowingStateLoss();
            }else if (null != host && Constant.OSGI_SERVICE_TOPIC_FRAGMENT.equals(servicename)){
                SubjectData data = new SubjectData();
                data.setS_key(params.getString("key"));
                data.setS_name(params.getString("name"));
                data.setStep(params.getInt("step"));
                data.setS_datatype(params.getString("datatype"));
                data.setData(new ArrayList<HomePageApkData>());
                data.setSpecialtopic_data(null);

                Fragment fg = HomePageListFragment.newInstance(host,data,true);
                FragmentManager fgm = host.getFragmentManager();
                FragmentTransaction ft = fgm.beginTransaction();
                if(null != fgm.findFragmentByTag(Constant.OSGI_SERVICE_SEARCH_FRAGMENT)){
                    ft.hide(fgm.findFragmentByTag(Constant.OSGI_SERVICE_SEARCH_FRAGMENT));
                    ft.add(host.getNode(), fg,Constant.OSGI_SERVICE_TOPIC_FRAGMENT);
                }else {
                    ft.replace(host.getNode(), fg,Constant.OSGI_SERVICE_TOPIC_FRAGMENT);
                }
                ft.replace(host.getNode(), fg, Constant.OSGI_SERVICE_TOPIC_FRAGMENT);
                ft.addToBackStack(null);
                ft.commitAllowingStateLoss();
            }
        }

//        @Override
//        public Object ApkplugOSGIService(BundleContext arg0, String servicename, int node,Object... objs) {
//            LogUtils.d(SimpleBundle.TAG, "ApkplugOSGIService,recv service:" + servicename + ",node=" + node);
//            Bundle b = null;
//            if (Constant.OSGI_SERVICE_MAIN_FRAGMENT.equals(servicename)){
//                Fragment fg = new HomePageFragment();
//                for (int i = 1;i < objs.length;i++){
//                    if (null == b){
//                        b = new Bundle();
//                    }
//                    b.putString("param"+i,(String)objs[i]);
//                }
//                if (null != b){
//                    fg.setArguments(b);
//                }
//                FragmentManager fgm = (FragmentManager)objs[0];
//                FragmentTransaction ft = fgm.beginTransaction();
//                if (null == b) {
//                    ft.replace(node, fg, Constant.OSGI_SERVICE_MAIN_FRAGMENT).commitAllowingStateLoss();
//                }else{
//                    ft.replace(node, fg, Constant.OSGI_SERVICE_MAIN_FRAGMENT);
//                    ft.addToBackStack(null);
//                    ft.commitAllowingStateLoss();
//                }
//            }else if (Constant.OSGI_SERVICE_TOPIC_FRAGMENT.equals(servicename)){
//                SubjectData data = new SubjectData();
//                data.setS_key((String)objs[1]);
//                data.setS_name((String)objs[2]);
//                data.setStep((int)objs[3]);
//                data.setS_datatype((String)objs[4]);
//                data.setData(new ArrayList<HomePageApkData>());
//                data.setSpecialtopic_data(null);
//
//                Fragment fg = new HomePageListFragment(data);
//                b = new Bundle();
//                b.putString("entry","topic");
//                fg.setArguments(b);
//                FragmentManager fgm = (FragmentManager)objs[0];
//                FragmentTransaction ft = fgm.beginTransaction();
//                if(null != fgm.findFragmentByTag(Constant.OSGI_SERVICE_SEARCH_FRAGMENT)){
//                    ft.hide(fgm.findFragmentByTag(Constant.OSGI_SERVICE_SEARCH_FRAGMENT));
//                    ft.add(node, fg,Constant.OSGI_SERVICE_TOPIC_FRAGMENT);
//                }else {
//                    ft.replace(node, fg,Constant.OSGI_SERVICE_TOPIC_FRAGMENT);
//                }
//                ft.addToBackStack(null);
//                ft.commitAllowingStateLoss();
//            }
//            return null;
//        }
    }
}
