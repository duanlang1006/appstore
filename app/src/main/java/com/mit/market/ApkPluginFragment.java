package com.mit.market;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.applite.common.LogUtils;
import com.osgi.extra.OSGIServiceClient;
import com.osgi.extra.OSGIBaseFragment;

import org.apkplug.Bundle.OSGIServiceAgent;
import org.apkplug.app.FrameworkInstance;
import org.osgi.framework.BundleContext;

/**
 * Created by yuzhimin on 6/17/15.
 */
public class ApkPluginFragment extends Fragment{
    private final String TAG = "apkplugin_Fragment";
    private Activity mActivity;
    private String mTargetService;
    private String mWhichFragment;
    private Bundle mParams;
    private OSGIServiceClient mPluginService;
    private OSGIBaseFragment mPluginFragment;


    public static Fragment newInstance(String tag,String which,Bundle params){
        Fragment fg = new ApkPluginFragment();
        Bundle b = new Bundle();
        b.putString("tag",tag);
        b.putString("which",which);
        if (null != params){
            b.putBundle("bundle", params);
        }
        fg.setArguments(b);
        return fg;
    }


    public ApkPluginFragment() {
        mTargetService = null;
        mWhichFragment = null;
        mParams = null;
        mPluginService = null;
        mPluginFragment = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.d(TAG, "onCreate ,mPluginService:"+mPluginService);
        setHasOptionsMenu(true);

        if (null != mPluginService){
            mPluginService.onCreate(mPluginFragment,savedInstanceState);
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
            mTargetService = arguments.getString("tag");
            mWhichFragment = arguments.getString("which");
            mParams = arguments.getBundle("bundle");
        }
        LogUtils.d(TAG, "onAttach ,mTargetService:"+mTargetService+",mWhichFragment="+mWhichFragment);
        try {
            FrameworkInstance frame= ((AppLiteApplication)mActivity.getApplication()).getFrame();
            BundleContext bundleContext = frame.getSystemBundleContext();
            OSGIServiceAgent<OSGIServiceClient> agent = new OSGIServiceAgent<OSGIServiceClient>(
                    bundleContext, OSGIServiceClient.class,
                    "(serviceName="+mTargetService+")", //服务查询条件
                    OSGIServiceAgent.real_time);   //每次都重新查询
            mPluginService = agent.getService();
        } catch (Exception e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        }

        if (null != mPluginService){
            mPluginFragment = mPluginService.newOSGIFragment(this,mWhichFragment,mParams);
            mPluginService.onAttach(mPluginFragment,activity);
        }else{
            //wrong
        }
        LogUtils.d(TAG, "onAttach ,mPluginService:"+mPluginService+",mWhichFragment="+mWhichFragment);
    }

    @Override
    public void onStart() {
        LogUtils.d(TAG, "onStart ,mPluginService:"+mPluginService);
        super.onStart();
        if (null != mPluginService){
            mPluginService.onStart(mPluginFragment);
        }else{
            //wrong
        }
    }

    @Override
    public void onResume() {
        LogUtils.d(TAG, "onResume ,mPluginService:"+mPluginService);
        super.onResume();
        if (null != mPluginService){
            mPluginService.onResume(mPluginFragment);
        }else{
            //wrong
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        LogUtils.d(TAG, "onSaveInstanceState ,mPluginService:"+mPluginService);
        super.onSaveInstanceState(outState);
        if (null != mPluginService){
            mPluginService.onSaveInstanceState(mPluginFragment,outState);
        }else{
            //wrong
        }
    }

    @Override
    public void onPause() {
        LogUtils.d(TAG, "onPause ,mPluginService:"+mPluginService);
        super.onPause();
        if (null != mPluginService){
            mPluginService.onPause(mPluginFragment);
        }else{
            //wrong
        }
    }

    @Override
    public void onStop() {
        LogUtils.d(TAG, "onStop ,mPluginService:"+mPluginService);
        super.onStop();
        if (null != mPluginService){
            mPluginService.onStop(mPluginFragment);
        }else{
            //wrong
        }
    }

    @Override
    public void onDestroy() {
        LogUtils.d(TAG, "onDestroy ,mPluginService:"+mPluginService);
        super.onDestroy();
        if (null != mPluginService){
            mPluginService.onDestroy(mPluginFragment);
        }else{
            //wrong
        }
    }

    @Override
    public void onDestroyView() {
        LogUtils.d(TAG, "onDestroyView ,mPluginService:"+mPluginService);
        super.onDestroyView();
        if (null != mPluginService){
            mPluginService.onDestroyView(mPluginFragment);
        }else{
            //wrong
        }
    }

    @Override
    public void onDetach() {
        LogUtils.d(TAG, "onDetach ,mPluginService:"+mPluginService);
        super.onDetach();
        if (null != mPluginService){
            mPluginService.onDetach(mPluginFragment);
        }else{
            //wrong
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LogUtils.d(TAG, "onCreateView ,mPluginService:"+mPluginService);
        if (null != mPluginService){
            return mPluginService.onCreateView(mPluginFragment,inflater, container, savedInstanceState);
        }else{
            //wrong
            return super.onCreateView(inflater, container, savedInstanceState);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        LogUtils.d(TAG, "onHiddenChanged: "+hidden);
        super.onHiddenChanged(hidden);
        if (null != mPluginService){
            mPluginService.onHiddenChanged(mPluginFragment,hidden);
        }else{
            //wrong
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                getFragmentManager().popBackStack();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

