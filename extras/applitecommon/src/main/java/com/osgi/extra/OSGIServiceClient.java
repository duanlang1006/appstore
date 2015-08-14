package com.osgi.extra;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import com.applite.common.Constant;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hxd on 15-7-30.
 */
public class OSGIServiceClient {
    private Map<String,String> mClientMap;
    private static OSGIServiceClient mInstance = null;

    public static OSGIServiceClient getInstance(){
        if (null ==  mInstance){
            mInstance = new OSGIServiceClient();
        }
        return mInstance;
    }

    private OSGIServiceClient(){
        mClientMap =  new HashMap<String,String>();
    }

    public OSGIBaseFragment newOSGIFragment(String whichService,String whichFragment,Bundle params){
        OSGIBaseFragment baseFragment = null;

        if (null == whichFragment || TextUtils.isEmpty(whichFragment)){
            whichFragment = mClientMap.get(whichService);
        }

        if (null != whichFragment && !TextUtils.isEmpty(whichFragment)){
            try {
                Class<?> cls = Class.forName(whichFragment);
                Constructor ct = cls.getDeclaredConstructor();
                ct.setAccessible(true);
                baseFragment = (OSGIBaseFragment)ct.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (null != baseFragment){
            baseFragment.setArguments(params);
        }
        return baseFragment;
    }

    public void register(String service,String fragment){
        mClientMap.put(service,fragment);
    }

    public void unregister(String service){
        mClientMap.remove(service);
    }
}
