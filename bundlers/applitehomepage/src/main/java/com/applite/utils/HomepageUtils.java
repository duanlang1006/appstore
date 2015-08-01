package com.applite.utils;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.applite.bean.HomePageApkData;
import com.applite.bean.SubjectData;
import com.applite.common.AppliteUtils;
import com.applite.common.Constant;
import com.applite.homepage.BundleContextFactory;
import com.applite.homepage.HomePageFragment;
import com.applite.homepage.HomePageListFragment;
import com.applite.homepage.PersonalFragment;
import com.osgi.extra.OSGIServiceHost;

import org.apkplug.Bundle.OSGIServiceAgent;
import org.osgi.framework.BundleContext;

import java.util.ArrayList;

/**
 * Created by LSY on 15-7-15.
 */
public class HomepageUtils {

    public static void toHomePageCategory(String category, String name) {
        BundleContext bundleContext = BundleContextFactory.getInstance().getBundleContext();
        OSGIServiceHost host = AppliteUtils.getHostOSGIService(bundleContext);
        if (null != host){
            Bundle b = HomePageFragment.newBundle(category, name);
            host.jumpto(bundleContext,Constant.OSGI_SERVICE_MAIN_FRAGMENT,HomePageFragment.class.getName(),b);
        }
    }

    /**
     * 去专题页面
     */
    public static void toTopicFragment(String s_key, String s_name, int step, String s_datatype) {
        BundleContext bundleContext = BundleContextFactory.getInstance().getBundleContext();
        OSGIServiceHost host = AppliteUtils.getHostOSGIService(bundleContext);
        if (null != host){
            SubjectData data = new SubjectData();
            data.setS_key(s_key);
            data.setS_name(s_name);
            data.setStep(step);
            data.setS_datatype(s_datatype);
            data.setData(new ArrayList<HomePageApkData>());
            data.setSpecialtopic_data(null);
            Bundle params = HomePageListFragment.newBundle(data,true);
            host.jumpto(bundleContext,Constant.OSGI_SERVICE_MAIN_FRAGMENT,HomePageListFragment.class.getName(),params);
        }
    }

    /**
     * 升级
     */
    public static void launchUpgradeFragment() {
        BundleContext bundleContext = BundleContextFactory.getInstance().getBundleContext();
        OSGIServiceHost host = AppliteUtils.getHostOSGIService(bundleContext);
        if (null != host){
            host.jumpto(bundleContext,Constant.OSGI_SERVICE_UPDATE_FRAGMENT,null,null);
        }
    }

    /****
     * 下载管理
     */
    public static void launchDownloadManagerFragment() {
        BundleContext bundleContext = BundleContextFactory.getInstance().getBundleContext();
        OSGIServiceHost host = AppliteUtils.getHostOSGIService(bundleContext);
        if (null != host){
            host.jumpto(bundleContext,Constant.OSGI_SERVICE_DM_FRAGMENT,null,null);
        }
    }

    public static void launchDetail(String packageName,String name,String imgUrl){
        BundleContext bundleContext = BundleContextFactory.getInstance().getBundleContext();
        OSGIServiceHost host = AppliteUtils.getHostOSGIService(bundleContext);
        if (null != host){
            Bundle b = new Bundle();
            b.putString("packageName",packageName);
            b.putString("name",name);
            b.putString("imgUrl",imgUrl);
            b.putString("from",Constant.OSGI_SERVICE_MAIN_FRAGMENT);
            host.jumpto(bundleContext,Constant.OSGI_SERVICE_DETAIL_FRAGMENT,null,b);
        }
    }

    /****
     * 搜索
     */
    public static void launchSearchFragment() {
        BundleContext bundleContext = BundleContextFactory.getInstance().getBundleContext();
        OSGIServiceHost host = AppliteUtils.getHostOSGIService(BundleContextFactory.getInstance().getBundleContext());
        if (null != host){
            host.jumpto(bundleContext,Constant.OSGI_SERVICE_SEARCH_FRAGMENT,null, null);
        }
    }

    /***
     * 进入个人中心
     */
    public static void launchPersonalFragment() {
        BundleContext bundleContext = BundleContextFactory.getInstance().getBundleContext();
        OSGIServiceHost host = AppliteUtils.getHostOSGIService(BundleContextFactory.getInstance().getBundleContext());
        if (null != host){
            host.jumpto(bundleContext,Constant.OSGI_SERVICE_MAIN_FRAGMENT,PersonalFragment.class.getName(), null);
        }
    }
}
