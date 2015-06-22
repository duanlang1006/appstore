package com.mit.market;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.view.Window;

import com.applite.common.Constant;
import com.mit.bean.ApkplugModel;
import com.mit.bean.ApkplugQueryModel;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mit_market);
        setOverflowShowingAlways();
        mOptReg = registerOSGIService(Constant.OSGI_SERVICE_HOST_OPT,mOptService);

        MitUpdateAgent.update(this);
        if (savedInstanceState == null) {
            launchFragment(R.id.container,Constant.OSGI_SERVICE_DM_FRAGMENT);
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
}
