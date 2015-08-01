package com.osgi.extra;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import org.osgi.framework.BundleContext;

/**
 * Created by hxd on 15-7-30.
 */
public abstract class OSGIServiceHost {
    public abstract void notify(BundleContext bundleContext, Bundle params);
    public abstract void jumpto(BundleContext bundleContext, String whichService, String whichFragment, Bundle params);
    public abstract Fragment newFragment(BundleContext bundleContext,String whichService, String whichFragment, Bundle param);
    public abstract FragmentManager getFragmentManager();
    public abstract int getNode();
}
