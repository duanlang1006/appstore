package com.mit.market;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.view.Window;

import com.applite.common.Constant;
import com.mit.bean.ApkplugModel;
import com.mit.bean.ApkplugQueryModel;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplListener;
import com.mit.mitupdatesdk.MitApkplugCloudAgent;
import com.mit.mitupdatesdk.MitUpdateAgent;
import com.applite.android.R;
import org.apkplug.Bundle.ApkplugOSGIService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


public class MitMarketActivity extends ApkPluginActivity {
    private static final String TAG = "applite_MitMarketActivity";
    private ApkplugOSGIService mOptService = new ApkplugOSGIService(){
        @Override
        public Object ApkplugOSGIService(BundleContext bundleContext, String from, int type, Object... objects) {
            switch(type){
                case 0:
                    launchFragment(R.id.container,objects);
                    break;
                case 1:
                    MitApkplugCloudAgent.download(MitMarketActivity.this,new ApkplugQueryModel<ApkplugModel>(),new MyApkplugDownloadCallback());
                    break;
            }
            return null;
        }
    };
    private ServiceRegistration mOptReg;
//    private ImplListener mImplListener = new MitImplListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mit_market);
        setOverflowShowingAlways();
        mOptReg = registerOSGIService(Constant.OSGI_SERVICE_HOST_OPT,mOptService);
//        ImplAgent.registerImplListener(mImplListener);

        MitUpdateAgent.update(this);
        if (savedInstanceState == null) {
            launchFragment(R.id.container,Constant.OSGI_SERVICE_MAIN_FRAGMENT);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_mit_market, menu);
//        MenuItem searchItem=menu.findItem(R.id.action_search);
//        final SearchView searchView=(SearchView)MenuItemCompat.getActionView(searchItem);
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
//            @Override
//            public boolean onQueryTextSubmit(String arg0)	{
//                launchFragment(R.id.container,Constant.OSGI_SERVICE_SEARCH_FRAGMENT);
//                searchView.onActionViewCollapsed();
//                return true;
//            }
//            @Override
//            public boolean onQueryTextChange(String arg0)	{
//                return false;
//            }
//        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (featureId == Window.FEATURE_ACTION_BAR && menu != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try {
                    Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (Exception e) {
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterOSGIService(mOptReg);
//        ImplAgent.unregisterImplListener(mImplListener);
    }

    private void setOverflowShowingAlways() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            menuKeyField.setAccessible(true);
            menuKeyField.setBoolean(config, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    class MitImplListener implements ImplListener {
//        private final String TAG = "impl_activity";
//        @Override
//        public void onDownloadComplete(boolean b, ImplAgent.DownloadCompleteRsp downloadCompleteRsp) {
//            ImplLog.d(TAG, "onDownloadComplete key=" + downloadCompleteRsp.key);
//        }
//
//        @Override
//        public void onDownloadUpdate(boolean b, ImplAgent.DownloadUpdateRsp downloadUpdateRsp) {
//            ImplLog.d(TAG,  "onDownloadUpdate  key="+downloadUpdateRsp.key);
//        }
//
//        @Override
//        public void onPackageAdded(boolean b, ImplAgent.PackageAddedRsp packageAddedRsp) {
//            ImplLog.d(TAG,  "onPackageAdded key="+packageAddedRsp.key);
//        }
//
//        @Override
//        public void onPackageRemoved(boolean b, ImplAgent.PackageRemovedRsp packageRemovedRsp) {
//            ImplLog.d(TAG,  "onSystemDeleteResult key="+packageRemovedRsp.key);
//        }
//
//        @Override
//        public void onPackageChanged(boolean b, ImplAgent.PackageChangedRsp packageChangedRsp) {
//            ImplLog.d(TAG,  "onSystemDeleteResult key="+packageChangedRsp.key);
//        }
//
//        @Override
//        public void onSystemInstallResult(boolean b, ImplAgent.SystemInstallResultRsp systemInstallResultRsp) {
//            ImplLog.d(TAG,  "onSystemDeleteResult key="+systemInstallResultRsp.key+",result="+systemInstallResultRsp.result);
//        }
//
//        @Override
//        public void onSystemDeleteResult(boolean b, ImplAgent.SystemDeleteResultRsp systemDeleteResultRsp) {
//            ImplLog.d(TAG,  "onSystemDeleteResult key="+systemDeleteResultRsp.key+",result="+systemDeleteResultRsp.result);
//        }
//
//        @Override
//        public void onFinish(boolean b, ImplAgent.ImplResponse implResponse) {
//            ImplLog.d(TAG,  "onFinish implResponse.action="+implResponse.action);
//        }
//    }
}
