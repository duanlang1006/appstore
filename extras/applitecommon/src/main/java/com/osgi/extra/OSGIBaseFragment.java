package com.osgi.extra;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mit.mitupdatesdk.MitMobclickAgent;

/**
 * Created by hxd on 15-7-30.
 */
public class OSGIBaseFragment extends Fragment{
    public Activity mActivity;

    public OSGIBaseFragment() {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        String className = this.getClass().getSimpleName();
        if (!"HomePageFragment".equals(className)
                && !"HomePageListFragment".equals(className)
                && !"GuideFragment".equals(className)) {
            MitMobclickAgent.onEvent(mActivity, className + "_onCreate");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        String className = this.getClass().getSimpleName();
        if (!"HomePageFragment".equals(className)
                && !"HomePageListFragment".equals(className)
                && !"GuideFragment".equals(className)) {
            MitMobclickAgent.onPageStart(className);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        String className = this.getClass().getSimpleName();
        if (!"HomePageFragment".equals(className)
                && !"HomePageListFragment".equals(className)
                && !"GuideFragment".equals(className)) {
            MitMobclickAgent.onPageEnd(className);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        String className = this.getClass().getSimpleName();
        if (!"HomePageFragment".equals(className)
                && !"HomePageListFragment".equals(className)
                && !"GuideFragment".equals(className)) {
            MitMobclickAgent.onEvent(mActivity, className + "_onDestroy");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (android.R.id.home == item.getItemId()){
            if (!getFragmentManager().popBackStackImmediate()){
                mActivity.finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public String getTagText(){
        return this.getClass().getSimpleName();
    }
}
