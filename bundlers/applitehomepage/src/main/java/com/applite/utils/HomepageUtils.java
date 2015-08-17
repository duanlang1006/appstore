package com.applite.utils;

import android.os.Bundle;
import com.applite.common.AppliteUtils;
import com.applite.common.Constant;
import com.applite.homepage.HomePageFragment;
import com.applite.homepage.HomePageListFragment;
import com.applite.homepage.PersonalFragment;
import com.osgi.extra.OSGIServiceHost;

/**
 * Created by LSY on 15-7-15.
 */
public class HomepageUtils {

//    public static void toHomePageCategory(OSGIServiceHost host,String category, String name) {
//        if (null != host){
//            host.jumpto(Constant.OSGI_SERVICE_MAIN_FRAGMENT+"#"+category,
//                    HomePageFragment.class.getName(),
//                    HomePageFragment.newBundle(category, name),true);
//        }
//    }
//
//    /**
//     * 去专题页面
//     */
//    public static void toTopicFragment(OSGIServiceHost host,String s_key, String s_name, int step, String s_datatype) {
//        if (null != host){
//            host.jumpto(Constant.OSGI_SERVICE_TOPIC_FRAGMENT+"#"+s_key,
//                    null,
//                    HomePageListFragment.newBundle(s_key,s_name,step,s_datatype),
//                    true);
//        }
//    }
//
//    /**
//     * 升级
//     */
//    public static void launchUpgradeFragment(OSGIServiceHost host) {
//        if (null != host){
//            host.jumpto(Constant.OSGI_SERVICE_UPDATE_FRAGMENT,null,null,true);
//        }
//    }
//
//    /****
//     * 下载管理
//     */
//    public static void launchDownloadManagerFragment(OSGIServiceHost host) {
//        if (null != host){
//            host.jumpto(Constant.OSGI_SERVICE_DM_FRAGMENT,null,null,true);
//        }
//    }
//
//    /****
//     * 我的一天
//     */
//    public static void launchLogoManagerFragment(OSGIServiceHost host) {
//        if (null != host){
//            Bundle b = new Bundle();
//            b.putString("service", Constant.OSGI_SERVICE_MAIN_FRAGMENT);
//            b.putString("fragment", null);
//            b.putBundle("params", null);
//            host.jumpto(Constant.OSGI_SERVICE_LOGO_FRAGMENT,null,b,true);
//        }
//    }
//
//    /****
//     * 详情
//     */
//    public static void launchDetail(OSGIServiceHost host,String packageName,String name,String imgUrl){
//        if (null != host){
//            Bundle b = new Bundle();
//            b.putString("packageName",packageName);
//            b.putString("name",name);
//            b.putString("imgUrl",imgUrl);
//            host.jumpto(Constant.OSGI_SERVICE_DETAIL_FRAGMENT,null,b,true);
//        }
//    }
//
//    /****
//     * 搜索
//     */
//    public static void launchSearchFragment(OSGIServiceHost host) {
//        if (null != host){
//            host.jumpto(Constant.OSGI_SERVICE_SEARCH_FRAGMENT,null,
//                    null,true);
//        }
//    }
//
//    /***
//     * 进入个人中心
//     */
//    public static void launchPersonalFragment(OSGIServiceHost host) {
//        if (null != host){
//            host.jumpto(Constant.OSGI_SERVICE_MAIN_FRAGMENT,PersonalFragment.class.getName(),
//                    null,true);
//        }
//    }
//
//    public static void launchDmFragment(OSGIServiceHost host) {
//        if (null != host){
//            host.jumpto(Constant.OSGI_SERVICE_DM_FRAGMENT,null,
//                    null,true);
//        }
//    }
}
