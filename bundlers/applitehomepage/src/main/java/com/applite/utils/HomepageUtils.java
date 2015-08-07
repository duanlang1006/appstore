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

    public static void toHomePageCategory(OSGIServiceHost host,String category, String name) {
        BundleContext bundleContext = BundleContextFactory.getInstance().getBundleContext();
        if (null != host){
            Bundle b = HomePageFragment.newBundle(category, name);
            AppliteUtils.putFgParams(b,Constant.OSGI_SERVICE_MAIN_FRAGMENT,"add",true);
            host.jumpto(bundleContext,Constant.OSGI_SERVICE_MAIN_FRAGMENT,HomePageFragment.class.getName(),b);
        }
    }

    /**
     * 去专题页面
     */
    public static void toTopicFragment(OSGIServiceHost host,String s_key, String s_name, int step, String s_datatype) {
        BundleContext bundleContext = BundleContextFactory.getInstance().getBundleContext();
        if (null != host){
            SubjectData data = new SubjectData();
            data.setS_key(s_key);
            data.setS_name(s_name);
            data.setStep(step);
            data.setS_datatype(s_datatype);
            data.setData(new ArrayList<HomePageApkData>());
            data.setSpecialtopic_data(null);
            Bundle params = HomePageListFragment.newBundle(data,true);
            AppliteUtils.putFgParams(params,Constant.OSGI_SERVICE_TOPIC_FRAGMENT,"add",true);
            host.jumpto(bundleContext,Constant.OSGI_SERVICE_TOPIC_FRAGMENT,HomePageListFragment.class.getName(),params);
        }
    }

    /**
     * 升级
     */
    public static void launchUpgradeFragment(OSGIServiceHost host) {
        BundleContext bundleContext = BundleContextFactory.getInstance().getBundleContext();
        if (null != host){
            host.jumpto(bundleContext,Constant.OSGI_SERVICE_UPDATE_FRAGMENT,null,
                    AppliteUtils.putFgParams(new Bundle(),Constant.OSGI_SERVICE_MAIN_FRAGMENT,"add",true));
        }
    }

    /****
     * 下载管理
     */
    public static void launchDownloadManagerFragment(OSGIServiceHost host) {
        BundleContext bundleContext = BundleContextFactory.getInstance().getBundleContext();
        if (null != host){
            host.jumpto(bundleContext,Constant.OSGI_SERVICE_DM_FRAGMENT,null,
                    AppliteUtils.putFgParams(new Bundle(),Constant.OSGI_SERVICE_MAIN_FRAGMENT,"add",true));
        }
    }

    public static void launchDetail(OSGIServiceHost host,String packageName,String name,String imgUrl){
        BundleContext bundleContext = BundleContextFactory.getInstance().getBundleContext();
        if (null != host){
            Bundle b = new Bundle();
            b.putString("packageName",packageName);
            b.putString("name",name);
            b.putString("imgUrl",imgUrl);
            AppliteUtils.putFgParams(b,Constant.OSGI_SERVICE_MAIN_FRAGMENT,"add",true);
            host.jumpto(bundleContext,Constant.OSGI_SERVICE_DETAIL_FRAGMENT,null,b);
        }
    }

    /****
     * 搜索
     */
    public static void launchSearchFragment(OSGIServiceHost host) {
        BundleContext bundleContext = BundleContextFactory.getInstance().getBundleContext();
        if (null != host){
            host.jumpto(bundleContext,Constant.OSGI_SERVICE_SEARCH_FRAGMENT,null,
                    AppliteUtils.putFgParams(new Bundle(),Constant.OSGI_SERVICE_MAIN_FRAGMENT,"add",true));
        }
    }

    /***
     * 进入个人中心
     */
    public static void launchPersonalFragment(OSGIServiceHost host) {
        BundleContext bundleContext = BundleContextFactory.getInstance().getBundleContext();
        if (null != host){
            host.jumpto(bundleContext,Constant.OSGI_SERVICE_MAIN_FRAGMENT,PersonalFragment.class.getName(),
                    AppliteUtils.putFgParams(new Bundle(),Constant.OSGI_SERVICE_MAIN_FRAGMENT,"add",true));
        }
    }
}
