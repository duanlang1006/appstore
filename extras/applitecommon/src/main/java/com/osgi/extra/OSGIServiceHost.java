package com.osgi.extra;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by hxd on 15-7-30.
 */
public interface OSGIServiceHost {
    //    public void notify(Bundle params);
    public Fragment newFragment(String targetService, String targetFragment, Bundle param);

    public void jumpto(String whichService, String whichFragment, Bundle params, boolean addToBackstack);

    public void jumptoHomepage(String category, String name, boolean addToBackstack);

    public void jumptoDetail(String packageName, String name, String imgUrl, int versionCode, String boxlabelvalue, boolean addToBackstack);

    public void jumptoDetail(String httpUrl, boolean addToBackstack);

    public void jumptoTopic(String key, String name, int step, String datatype, boolean addToBackstack);

    public void jumptoSearch(String detailTag, boolean addToBackstack, String info, String keyword, String hintword);

    public void jumptoPersonal(boolean addToBackstack);

    public void jumptoLucky(boolean addToBackstack);

    public void jumptoUpdate(boolean addToBackstack);

    public void jumptoDownloadManager(boolean addToBackstack);

    public void jumptoMylife(boolean addToBackstack);

    public void jumptoSetting(boolean addToBackstack);

    public void jumptoAbout(boolean addToBackstack);

    public void jumptoDownloadSizeLimit(boolean addToBackstack);

    public void jumptoConversation();
}
