package com.osgi.extra;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by hxd on 15-7-30.
 */
public interface OSGIServiceHost {
    public void notify(Bundle params);
    public void jumpto(String whichService, String whichFragment, Bundle params);
    public Fragment newFragment(String whichService, String whichFragment, Bundle param);
}
