//package com.applite.homepage;
//
//import android.os.Bundle;
//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentManager;
//import android.support.v4.app.FragmentTransaction;
//import com.applite.bean.HomePageApkData;
//import com.applite.bean.SubjectData;
//import com.applite.common.AppliteUtils;
//import com.applite.common.Constant;
//import com.applite.common.LogUtils;
//import com.osgi.extra.OSGIBaseFragment;
//import com.osgi.extra.OSGIServiceHost;
//import com.osgi.extra.OSGIServiceClient;
//
//import org.apkplug.Bundle.OSGIServiceAgent;
//import org.osgi.framework.BundleActivator;
//import org.osgi.framework.BundleContext;
//import org.osgi.framework.ServiceRegistration;
//
//import java.lang.reflect.Constructor;
//import java.util.ArrayList;
//import java.util.Dictionary;
//import java.util.Hashtable;
//
//public class SimpleBundle implements BundleActivator{
//    static final String TAG = "main_simplebundle";
//    private ServiceRegistration mServiceReg = null;
//    private ServiceRegistration mServiceTopicReg = null;
//
//    public void start(BundleContext mcontext){
//        BundleContextFactory.getInstance().setBundleContext(mcontext);
//        LogUtils.d(TAG, "simplebundle start");
//
//        Dictionary<String,Object> properties =new Hashtable<String,Object>();
//        properties.put("serviceName", Constant.OSGI_SERVICE_MAIN_FRAGMENT);
//        OSGIServiceClient service = new HomePageOSGIServiceImpl();
//        //注册一个服务给Host调用
//        mServiceReg = mcontext.registerService(
//                OSGIServiceClient.class.getName(),
//                service,
//                properties);
//
//        properties.put("serviceName", Constant.OSGI_SERVICE_TOPIC_FRAGMENT);
//        //注册一个服务给Host调用
//        mServiceTopicReg = mcontext.registerService(
//                OSGIServiceClient.class.getName(),
//                service,
//                properties);
//    }
//
//    public void stop(BundleContext mcontext){
//        LogUtils.d(TAG,"simplebundle stop");
//        if (null != mServiceReg){
//            mServiceReg.unregister();
//        }
//
//        if (null != mServiceTopicReg){
//            mServiceTopicReg.unregister();
//        }
//    }
//
//
//    public class HomePageOSGIServiceImpl extends OSGIServiceClient {
//        @Override
//        public OSGIBaseFragment newOSGIFragment(Fragment container, String whichService,String whichFragment, Bundle params) {
//            OSGIBaseFragment fg = null;
//            try {
//                Class<?> cls = Class.forName(whichFragment);
//                Constructor ct = cls.getDeclaredConstructor(Fragment.class,Bundle.class);
//                ct.setAccessible(true);
//                fg = (OSGIBaseFragment)ct.newInstance(container,params);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            if (null == fg) {
//                if (Constant.OSGI_SERVICE_MAIN_FRAGMENT.equals(whichService)) {
//                    fg = HomePageFragment.newInstance(container, params);
//                }else if (Constant.OSGI_SERVICE_TOPIC_FRAGMENT.equals(whichService)){
//                    SubjectData data = new SubjectData();
//                    data.setS_key(params.getString("key"));
//                    data.setS_name(params.getString("name"));
//                    data.setStep(params.getInt("step"));
//                    data.setS_datatype(params.getString("datatype"));
//                    data.setData(new ArrayList<HomePageApkData>());
//                    data.setSpecialtopic_data(null);
//                    params = HomePageListFragment.newBundle(data,true);
//                    fg = HomePageListFragment.newInstance(container,params);
//                }
//            }
//            return fg;
//        }
//    }
//}
