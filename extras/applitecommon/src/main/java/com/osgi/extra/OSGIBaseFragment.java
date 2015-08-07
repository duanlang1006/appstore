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

/**
 * Created by hxd on 15-7-30.
 */
public class OSGIBaseFragment{
    Fragment mContainerFragment;
    Activity mActivity;

    public OSGIBaseFragment(Fragment mFragment,Bundle params) {
        this.mContainerFragment = mFragment;
    }

    public FragmentManager getFragmentManager() {
        FragmentManager fm = null;
        if(null != mContainerFragment){
            fm = mContainerFragment.getFragmentManager();
        }
        return fm;
    }

    public FragmentManager getChildFragmentManager() {
        FragmentManager fm = null;
        if(null != mContainerFragment){
            fm = mContainerFragment.getChildFragmentManager();
        }
        return fm;
    }

    public int getId(){
        return mContainerFragment.getId();
    }

    public Activity getActivity(){
        return mActivity;
    }

    public void onCreate(Bundle savedInstanceState){

    }

    public void onAttach(Activity activity){
        mActivity = activity;
    }
    public void onStart(){

    }
    public void onResume(){

    }
    public void onSaveInstanceState(Bundle outState){

    }
    public void onPause(){

    }
    public void onStop(){

    }
    public void onDestroy(){

    }
    public void onDestroyView(){

    }
    public void onDetach(){

    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        return null;
    }
    public void onHiddenChanged(boolean hidden){

    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }
}
