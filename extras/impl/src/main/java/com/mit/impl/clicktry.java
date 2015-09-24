package com.mit.impl;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

import com.applite.common.Constant;
import com.osgi.extra.OSGIServiceHost;

/**
 * Created by wanghaochen on 15-9-22.
 */
public class clicktry extends ActionBarActivity implements OSGIServiceHost{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_download_pager);

        Toast.makeText(clicktry.this, "1111", Toast.LENGTH_SHORT).show();
        try {
//            Bundle bundle = GuideFragment.newBundles(Constant.OSGI_SERVICE_DM_FRAGMENT, null, null, false, true);
//            bundle.putString("update_data", intent.getStringExtra("update_data"));
//            jumpto(Constant.OSGI_SERVICE_DM_FRAGMENT, null, null, false);

            ((OSGIServiceHost) clicktry.this).jumptoDownloadManager(true);
        } catch (ClassCastException e) {
            Toast.makeText(clicktry.this, "然而并没有跳转", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public Fragment newFragment(String s, String s1, Bundle bundle) {
        return null;
    }

    @Override
    public void jumpto(String s, String s1, Bundle bundle, boolean b) {

    }

    @Override
    public void jumptoHomepage(String s, String s1, boolean b) {

    }

    @Override
    public void jumptoDetail(String s, String s1, String s2, int i, String s3, boolean b) {

    }

    @Override
    public void jumptoDetail(String s, boolean b) {

    }

    @Override
    public void jumptoTopic(String s, String s1, int i, String s2, boolean b) {

    }

    @Override
    public void jumptoSearch(String s, boolean b, String s1, String s2, String s3) {

    }

    @Override
    public void jumptoPersonal(boolean b) {

    }

    @Override
    public void jumptoLucky(boolean b) {

    }

    @Override
    public void jumptoUpdate(boolean b) {

    }

    @Override
    public void jumptoDownloadManager(boolean b) {

    }

    @Override
    public void jumptoMylife(boolean b) {

    }

    @Override
    public void jumptoSetting(boolean b) {

    }

    @Override
    public void jumptoAbout(boolean b) {

    }

    @Override
    public void jumptoConversation() {

    }
}
