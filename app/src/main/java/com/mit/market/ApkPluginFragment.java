package com.mit.market;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.applite.android.R;
import com.applite.common.AppliteUtils;
import com.applite.common.LogUtils;
import com.osgi.extra.OSGIServiceClient;
import com.osgi.extra.OSGIBaseFragment;

/**
 * Created by yuzhimin on 6/17/15.
 */
public class ApkPluginFragment extends Fragment{
    private final String TAG = "apkplugin_Fragment";
    private Activity mActivity;
    private String mWhichService;
    private String mWhichFragment;
    private Bundle mParams;
    private OSGIBaseFragment mPluginFragment;


    public static ApkPluginFragment newInstance(String whichService,String whichFragment,Bundle params){
        ApkPluginFragment fg = new ApkPluginFragment();
        Bundle b = new Bundle();
        b.putString("service",whichService);
        b.putString("fragment",whichFragment);
        b.putBundle("params", params);
        fg.setArguments(b);
        return fg;
    }


    public ApkPluginFragment() {
        mWhichService = null;
        mWhichFragment = null;
        mParams = null;
        mPluginFragment = null;
    }

    public String getWhichService(){
        return getArguments().getString("service");
    }

    public String getWhichFragment(){
        return getArguments().getString("fragment");
    }

    public Bundle getParams(){
        return getArguments().getBundle("params");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.d(TAG, "onCreate ,this:"+this);
        setHasOptionsMenu(true);

        if (null != mPluginFragment){
            mPluginFragment.onCreate(savedInstanceState);
        }else{
            //wrong
        }
    }

    @Override
    public void onAttach(Activity activity) {
       super.onAttach(activity);

        mActivity = activity;
        Bundle arguments = getArguments();
        if (null != arguments){
            mWhichService = arguments.getString("service");
            mWhichFragment = arguments.getString("fragment");
            mParams = arguments.getBundle("params");
        }
        LogUtils.d(TAG, "onAttach ,this:"+this);

        mPluginFragment = OSGIServiceClient.getInstance().newOSGIFragment(this,mWhichService,mWhichFragment,mParams);
        if (null != mPluginFragment){
            mPluginFragment.onAttach(activity);
        }else{
            //wrong
            LogUtils.e(TAG,"onAttach,client = null,"+mWhichService);
        }
    }

    @Override
    public void onStart() {
        LogUtils.d(TAG, "onStart ,this:"+this);
        super.onStart();
        if (null != mPluginFragment){
            mPluginFragment.onStart();
        }else{
            //wrong
        }
    }

    @Override
    public void onResume() {
        LogUtils.d(TAG, "onResume ,this:"+this);
        super.onResume();
        if (null != mPluginFragment){
            mPluginFragment.onResume();
        }else{
            //wrong
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        LogUtils.d(TAG, "onSaveInstanceState ,this:"+this);
        super.onSaveInstanceState(outState);
        if (null != mPluginFragment){
            mPluginFragment.onSaveInstanceState(outState);
        }else{
            //wrong
        }
    }

    @Override
    public void onPause() {
        LogUtils.d(TAG, "onPause ,this:"+this);
        super.onPause();
        if (null != mPluginFragment){
            mPluginFragment.onPause();
        }else{
            //wrong
        }
    }

    @Override
    public void onStop() {
        LogUtils.d(TAG, "onStop ,this:"+this);
        super.onStop();
        if (null != mPluginFragment){
            mPluginFragment.onStop();
        }else{
            //wrong
        }
    }

    @Override
    public void onDestroy() {
        LogUtils.d(TAG, "onDestroy ,this:"+this);
        super.onDestroy();
        if (null != mPluginFragment){
            mPluginFragment.onDestroy();
        }else{
            //wrong
        }
    }

    @Override
    public void onDestroyView() {
        LogUtils.d(TAG, "onDestroyView ,this:"+this);
        super.onDestroyView();
        if (null != mPluginFragment){
            mPluginFragment.onDestroyView();
        }else{
            //wrong
        }
    }

    @Override
    public void onDetach() {
        LogUtils.d(TAG, "onDetach ,this:"+this);
        super.onDetach();
        if (null != mPluginFragment){
            mPluginFragment.onDetach();
        }else{
            //wrong
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LogUtils.d(TAG, "onCreateView ,this:"+this);
        if (null != mPluginFragment){
            return mPluginFragment.onCreateView(inflater, container, savedInstanceState);
        }else{
            //wrong
            return inflater.inflate(R.layout.fragment_mit_market,container,false);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        LogUtils.d(TAG, "onHiddenChanged: "+hidden+",this:"+this);
        super.onHiddenChanged(hidden);
        if (null != mPluginFragment){
            mPluginFragment.onHiddenChanged(hidden);
        }else{
            //wrong
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (null != mPluginFragment){
            mPluginFragment.onCreateOptionsMenu(menu,inflater);
        }else{
            //wrong
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (null != mPluginFragment){
            mPluginFragment.onPrepareOptionsMenu(menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (null != mPluginFragment){
            if (true == mPluginFragment.onOptionsItemSelected(item)){
                return true;
            }
        }
        switch(item.getItemId()){
            case android.R.id.home:
                getFragmentManager().popBackStack();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

