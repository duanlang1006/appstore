package com.osgi.extra;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import org.osgi.framework.BundleContext;

/**
 * Created by hxd on 15-7-30.
 */
public interface OSGIServiceHost {
    public void notify(BundleContext bundleContext, Bundle params);
    public void jumpto(BundleContext bundleContext, String whichService, String whichFragment, Bundle params);
    public Fragment newFragment(BundleContext bundleContext,String whichService, String whichFragment, Bundle param);


    public void initPlugins(OnInitFinishedListener onInitFinishedListener);
    public BundleContext getSystemBundleContext();

    public interface OnInitFinishedListener{
        public void onInitFinished();
    }
}
