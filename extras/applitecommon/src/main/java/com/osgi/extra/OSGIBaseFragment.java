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

import com.applite.common.LogUtils;
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
        LogUtils.d(this.getClass().getSimpleName(),"onAttach");
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LogUtils.d(this.getClass().getSimpleName(),"onCreate");
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
        LogUtils.d(this.getClass().getSimpleName(),"onResume");
        super.onResume();
        String className = this.getClass().getSimpleName();
        if (!"HomePageFragment".equals(className)
                && !"HomePageListFragment".equals(className)
                && !"GuideFragment".equals(className)) {
            MitMobclickAgent.onPageStart(className);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LogUtils.d(this.getClass().getSimpleName(),"onCreateView");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LogUtils.d(this.getClass().getSimpleName(),"onDestroyView");
    }


    @Override
    public void onPause() {
        LogUtils.d(this.getClass().getSimpleName(),"onPause");
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
        LogUtils.d(this.getClass().getSimpleName(),"onDestroy");
        super.onDestroy();
        String className = this.getClass().getSimpleName();
        if (!"HomePageFragment".equals(className)
                && !"HomePageListFragment".equals(className)
                && !"GuideFragment".equals(className)) {
            MitMobclickAgent.onEvent(mActivity, className + "_onDestroy");
        }
    }

    @Override
    public void onDetach() {
        LogUtils.d(this.getClass().getSimpleName(),"onDetach");
        super.onDetach();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        LogUtils.d(this.getClass().getSimpleName(),"onHiddenChanged,hidden="+hidden);
        super.onHiddenChanged(hidden);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (android.R.id.home == item.getItemId()){
            getFragmentManager().popBackStack();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public String getTagText(){
        return this.getClass().getSimpleName();
    }
}
