package com.mit.market;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.view.Window;
import android.widget.Toast;

import com.applite.common.Constant;
import com.applite.common.LogUtils;
import com.mit.bean.ApkplugModel;
import com.mit.bean.ApkplugQueryModel;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplInfo;
import com.mit.impl.ImplListener;
import com.mit.impl.ImplLog;
import com.mit.mitupdatesdk.MitApkplugCloudAgent;
import com.mit.mitupdatesdk.MitMobclickAgent;
import com.mit.mitupdatesdk.MitUpdateAgent;
import com.applite.android.R;
import com.umeng.analytics.MobclickAgent;

import org.apkplug.Bundle.ApkplugOSGIService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


public class MitMarketActivity extends ApkPluginActivity {
    private static final String TAG = "applite_MitMarketActivity";
    private ApkplugOSGIService mOptService = new ApkplugOSGIService() {
        @Override
        public Object ApkplugOSGIService(BundleContext bundleContext, String from, int type, Object... objects) {
            switch (type) {
                case 0:
                    launchFragment(R.id.container, objects);
                    break;
                case 1:
                    MitApkplugCloudAgent.download(MitMarketActivity.this, new ApkplugQueryModel<ApkplugModel>(), new MyApkplugDownloadCallback());
                    break;
                case 2:
                    UpdateNotification.getInstance().showNot(MitMarketActivity.this, objects[0].toString());
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
        mOptReg = registerOSGIService(Constant.OSGI_SERVICE_HOST_OPT, mOptService);

        MobclickAgent.openActivityDurationTrack(false);//禁止默认的页面统计方式
        MobclickAgent.updateOnlineConfig(this);
        MitUpdateAgent.setDebug(true);
        MitUpdateAgent.update(this);

        Intent mIntent = getIntent();
        if (Constant.UPDATE_FRAGMENT_NOT.equals(mIntent.getStringExtra("update"))) {
            launchFragment(R.id.container, Constant.OSGI_SERVICE_UPDATE_FRAGMENT);
        } else {
            if (savedInstanceState == null) {
                launchFragment(R.id.container, Constant.OSGI_SERVICE_LOGO_FRAGMENT);
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        MitMobclickAgent.onEvent(this, "OpenApk");
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);       //统计时长
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
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
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
//        Intent mIntent = getIntent();
        setIntent(intent);
        if (Constant.UPDATE_FRAGMENT_NOT.equals(intent.getStringExtra("update")))
            launchFragment(R.id.container, Constant.OSGI_SERVICE_UPDATE_FRAGMENT);
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
