package com.osgi.extra;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by hxd on 15-7-30.
 */
public abstract class OSGIServiceClient {
//    public abstract void launchOSGIFragment(String service,Fragment fg,Bundle params);
    public abstract OSGIBaseFragment newOSGIFragment(Fragment container,String whichService,String whichFragment,Bundle params);

    public void onCreate(OSGIBaseFragment target,Bundle savedInstanceState){
        if (null != target){
            target.onCreate(savedInstanceState);
        }
    }
    public void onAttach(OSGIBaseFragment target,Activity activity){
        if (null != target){
            target.onAttach(activity);
        }
    }
    public void onStart(OSGIBaseFragment target){
        if (null != target){
            target.onStart();
        }
    }
    public void onResume(OSGIBaseFragment target){
        if (null != target){
            target.onResume();
        }
    }
    public void onSaveInstanceState(OSGIBaseFragment target,Bundle outState){
        if (null != target){
            target.onSaveInstanceState(outState);
        }
    }
    public void onPause(OSGIBaseFragment target){
        if (null != target){
            target.onPause();
        }
    }
    public void onStop(OSGIBaseFragment target){
        if (null != target){
            target.onStop();
        }
    }
    public void onDestroy(OSGIBaseFragment target){
        if (null != target){
            target.onDestroy();
        }
    }
    public void onDestroyView(OSGIBaseFragment target){
        if (null != target){
            target.onDestroyView();
        }
    }
    public void onDetach(OSGIBaseFragment target){
        if (null != target){
            target.onDetach();
        }
    }
    public View onCreateView(OSGIBaseFragment target,LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = null;
        if (null != target){
            view = target.onCreateView(inflater,container,savedInstanceState);
        }
        return view;
    }

    public void onHiddenChanged(OSGIBaseFragment target,boolean hidden){
        if (null != target){
            target.onHiddenChanged(hidden);
        }
    }
}
