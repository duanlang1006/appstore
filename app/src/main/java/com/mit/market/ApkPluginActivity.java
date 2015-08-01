package com.mit.market;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.applite.android.R;
import com.applite.common.LogUtils;
import com.mit.bean.ApkplugDownloadCallback;
import com.mit.bean.ApkplugModel;
import com.mit.bean.ApkplugQueryModel;
import com.mit.bean.ApkplugUpdateBean;
import com.mit.bean.ApkplugUpdateCallback;
import com.mit.bean.ApkplugUpdateInfo;
import com.mit.mitupdatesdk.MitApkplugCloudAgent;
import com.osgi.extra.OSGIServiceHost;
import com.osgi.extra.OSGIServiceClient;
import org.apkplug.Bundle.OSGIServiceAgent;
import org.apkplug.Bundle.installCallback;
import org.apkplug.app.FrameworkInstance;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.SynchronousBundleListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

public abstract class ApkPluginActivity extends ActionBarActivity {
    private static final String TAG = "applite_ApkPlugin";
    private List<org.osgi.framework.Bundle> bundles=null;
    private Set<String> mPluginList= new HashSet<String>();/*{"applitelogo.apk","applitehomepage.apk"};*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        installBundles();
        initBundleList();
        startAllBundles();
        ListenerBundleEvent();
        checkUpdate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAllBundles();
        bundles.clear();
    }


    private void initPluginList(){
        try {
//            SharedPreferences sp = getSharedPreferences(Constant.CONFIG_BUNDLES_INFO,MODE_PRIVATE);
            mPluginList.clear();
//            mPluginList = sp.getStringSet(Constant.KEY_BUNDLES,mPluginList);
            if (mPluginList.size()==0) {
                String[] list = getAssets().list("");
                for (String file : list) {
                    if (file.toLowerCase().contains(".apk")) {
                        mPluginList.add(file);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void installBundles(){
        try {
            initPluginList();
            FrameworkInstance frame = ((AppLiteApplication) getApplication()).getFrame();
            BundleContext context = frame.getSystemBundleContext();
            InstallBundle ib = new InstallBundle(context);
            for (final String apk : mPluginList) {
                ib.install(this, context, apk, new installCallback() {
                    @Override
                    public void callback(int status, org.osgi.framework.Bundle bundle) {
                        if (status == installCallback.stutas5 || status == installCallback.stutas7) {
                            Log.d(TAG, bundle.getName() + InstallBundle.stutasToStr(status));
                        } else {
                            Log.e(TAG, apk+","+InstallBundle.stutasToStr(status));
                        }
                    }
                }, 2, false);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 初始化显示已安装插件的UI
     */
    public void initBundleList(){
        bundles = new ArrayList<org.osgi.framework.Bundle>();
        try {
            FrameworkInstance frame = ((AppLiteApplication) getApplication()).getFrame();
            BundleContext context = frame.getSystemBundleContext();
            for (org.osgi.framework.Bundle bundle : context.getBundles()) {
                if (0 == bundle.getBundleId()){
                    continue;
                }
                bundles.add(bundle);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    void checkUpdate(){
        if (null == bundles || bundles.size() == 0){
            return;
        }
        int size = bundles.size();
        List<ApkplugUpdateInfo> updateInfoList = new ArrayList<ApkplugUpdateInfo>();
        for(int i=0;i<size;i++){
            org.osgi.framework.Bundle b = bundles.get(i);
            updateInfoList.add(new ApkplugUpdateInfo(b.getSymbolicName(),b.getVersion(),b.getPackageInfo().versionCode));
        }
        ApkplugUpdateBean bean=new ApkplugUpdateBean();
        bean.setApps(updateInfoList);
        MitApkplugCloudAgent.checkupdate(this,bean,new MyApkplugUpdateCallback());
    }

    /**
     * 监听插件安装事件，当有新插件安装或卸载时成功也更新一下
     */
    private void ListenerBundleEvent(){
        try {
            final FrameworkInstance frame = ((AppLiteApplication) getApplication()).getFrame();
            frame.getSystemBundleContext().addBundleListener(
                    new SynchronousBundleListener() {
                        public void bundleChanged(BundleEvent event) {
                            bundles.clear();
                            BundleContext context = frame.getSystemBundleContext();
                            for (int i = 0; i < context.getBundles().length; i++) {
                                bundles.add(context.getBundles()[i]);

                            }
                        }

                    });
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    protected void launchFragment(String serviceName,Bundle params){
        boolean ret = false;
        try {
            //查询插件服务
            FrameworkInstance frame= ((AppLiteApplication)getApplication()).getFrame();
            BundleContext bundleContext = frame.getSystemBundleContext();
            OSGIServiceClient service = new OSGIServiceAgent<OSGIServiceClient>(
                    bundleContext,
                    OSGIServiceClient.class,
                    "(serviceName="+serviceName+")", //服务查询条件
                    OSGIServiceAgent.real_time).getService();   //每次都重新查询
            service.launchOSGIFragment(serviceName, params);
            ret = true;
        } catch (Exception e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        }
        if (!ret){
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    protected ServiceRegistration registerOSGIService(String serviceName,OSGIServiceHost service){
        Dictionary<String,Object> properties =new Hashtable<String,Object>();
        properties.put("serviceName", serviceName);
        //注册一个服务给Host调用
        FrameworkInstance frame= ((AppLiteApplication)getApplication()).getFrame();
        return frame.getSystemBundleContext().registerService(
                OSGIServiceHost.class.getName(),
                service,
                properties);
    }

    protected void unregisterOSGIService(ServiceRegistration reg){
        if (null != reg){
            reg.unregister();
        }
    }

    private void startAllBundles(){
        try {
            for (org.osgi.framework.Bundle bundle : bundles) {
                Log.d(TAG, "start,bundle info:" + bundle.toString()/*getName() + "," + bundle.getState() + "," + bundle.getVersion() + "," + bundle.getBundleId() + "," + bundle.getPackageInfo()*/);
                bundle.start();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void stopAllBundles(){
        try {
            for (org.osgi.framework.Bundle bundle : bundles) {
                Log.d(TAG, "stop,bundle info:" + bundle.toString());
                bundle.stop();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_mit_market, container, false);
            TextView tv = (TextView)rootView.findViewById(R.id.market_error);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            return rootView;
        }
    }

    class MyApkplugUpdateCallback implements ApkplugUpdateCallback{
        @Override
        public void onSuccess(int status, final ApkplugQueryModel<ApkplugModel> apkplugModel) {
            LogUtils.d(TAG,"ApkplugUpdateCallback,status="+status);
            if (ApkplugUpdateCallback.success != status){
                return;
            }
            ApkPluginActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MitApkplugCloudAgent.download(ApkPluginActivity.this,apkplugModel,new MyApkplugDownloadCallback());
                }
            });
        }

        @Override
        public void onFailure(int i, String s) {

        }
    }

    class MyApkplugDownloadCallback implements ApkplugDownloadCallback {
        @Override
        public void onDownLoadSuccess(final String path) {
            Log.d(TAG, "MyApkplugDownloadCallback,onDownLoadSuccess("+path);
            try {
                FrameworkInstance frame= ((AppLiteApplication)getApplication()).getFrame();
                BundleContext bundleContext = frame.getSystemBundleContext();
                InstallBundle ib=new InstallBundle(bundleContext);
                ib.install(getApplicationContext(),bundleContext,path,new installCallback() {
                    @Override
                    public void callback(int status, org.osgi.framework.Bundle bundle) {
                        if(status == installCallback.stutas5||status == installCallback.stutas7){
                            Log.d(TAG, bundle.getName() + InstallBundle.stutasToStr(status));
                            new File(path).delete();
                        }else{
                            Log.e(TAG, InstallBundle.stutasToStr(status));
                        }
                    }
                });
            } catch (Exception e) {
                // TODO 自动生成的 catch 块
                e.printStackTrace();
            }
        }

        @Override
        public void onProgress(long bytesWritten, long totalSize, String speed) {
            Log.d(TAG, "MyApkplugDownloadCallback,onProgress("+bytesWritten+","+totalSize+","+speed);
        }

        @Override
        public void onFailure(int errNo, String errMsg) {
            Log.d(TAG, "MyApkplugDownloadCallback,onFailure("+errNo+","+errMsg);
        }
    }
}
