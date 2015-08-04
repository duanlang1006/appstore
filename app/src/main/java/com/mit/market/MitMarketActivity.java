package com.mit.market;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.view.Window;

import com.applite.common.AppliteUtils;
import com.applite.common.Constant;
import com.applite.common.IconCache;
import com.applite.common.LogUtils;
import com.mit.main.GuideFragment;
import com.mit.mitupdatesdk.MitMobclickAgent;
import com.mit.mitupdatesdk.MitUpdateAgent;
import com.applite.android.R;
import com.osgi.extra.OSGIServiceHost;
import com.umeng.analytics.MobclickAgent;

import org.apkplug.app.FrameworkInstance;
import org.osgi.framework.BundleContext;
import java.lang.reflect.Field;
import java.lang.reflect.Method;


public class MitMarketActivity extends ApkPluginActivity implements OSGIServiceHost{
    private static final String TAG = "applite_MitMarketActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtils.d(TAG,"onCreate:"+savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mit_market);
        setOverflowShowingAlways();

        long current = System.currentTimeMillis();
        MobclickAgent.openActivityDurationTrack(false);//禁止默认的页面统计方式
        MobclickAgent.updateOnlineConfig(this);
        MitUpdateAgent.setDebug(true);
        MitUpdateAgent.update(this);
        LogUtils.d(TAG,"onCreate take "+(System.currentTimeMillis()-current)+" ms");

        FragmentManager fgm = getSupportFragmentManager();
        Fragment fg = fgm.findFragmentByTag(Constant.OSGI_SERVICE_LOGO_FRAGMENT);
        if (null == fg ){
            Intent intent = getIntent();
            if (null != intent && Constant.UPDATE_FRAGMENT_NOT.equals(intent.getStringExtra("update"))){
                fg = GuideFragment.newInstance(Constant.OSGI_SERVICE_UPDATE_FRAGMENT,null,
                        AppliteUtils.putFgParams(new Bundle(),null,"replace",false));
            }else {
                fg = GuideFragment.newInstance(Constant.OSGI_SERVICE_MAIN_FRAGMENT,null,
                        AppliteUtils.putFgParams(new Bundle(),null,"replace",false));
            }
            fgm.beginTransaction()
                    .replace(R.id.container,fg,Constant.OSGI_SERVICE_LOGO_FRAGMENT)
                    .commit();
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
        setIntent(intent);

        FragmentManager fgm = getSupportFragmentManager();
        if (null != intent && Constant.UPDATE_FRAGMENT_NOT.equals(intent.getStringExtra("update"))){
            Fragment fg = GuideFragment.newInstance(Constant.OSGI_SERVICE_UPDATE_FRAGMENT,null,
                    AppliteUtils.putFgParams(new Bundle(),null,"replace",false));
            fgm.beginTransaction()
                    .replace(R.id.container,fg,Constant.OSGI_SERVICE_LOGO_FRAGMENT)
                    .commit();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        LogUtils.d(TAG,"onSaveInstanceState");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        IconCache.getInstance(this).flush();
    }


    @Override
    public void initPlugins(final OnInitFinishedListener listener) {
        new Thread(){
            @Override
            public void run() {
                installBundles();
                initBundleList();
                startAllBundles();
                ListenerBundleEvent();
                if (null != listener){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.onInitFinished();
                            checkUpdate();
                        }
                    });
                }
            }
        }.start();
    }

    @Override
    public BundleContext getSystemBundleContext() {
        FrameworkInstance frame = AppLiteApplication.getFrame(this);
        return (null == frame)?null:frame.getSystemBundleContext();
    }

    @Override
    public void notify(BundleContext bundleContext, Bundle params) {
        if (null != params){
            int number = params.getInt("number");
            UpdateNotification.getInstance().showNot(MitMarketActivity.this, String.valueOf(number));
        }
    }

    @Override
    public void jumpto(BundleContext bundleContext, String whichService, String whichFragment, Bundle params) {
        Fragment fragment = newFragment(bundleContext, whichService, whichFragment, params);
        String fromTag = params.getString("fromTag");
        String operate = params.getString("operate");
        boolean addToBackStack = params.getBoolean("addToBackStack");

        FragmentManager fgm = getSupportFragmentManager();
        FragmentTransaction ft = fgm.beginTransaction();
        if ("add".equals(operate)){
            Fragment fromFragment = fgm.findFragmentByTag(fromTag);
            if (null != fromFragment){
                ft.hide(fromFragment);
            }
            ft.add(R.id.container, fragment, whichService);
        }else if ("replace".equals(operate)){
            ft.replace(R.id.container, fragment, whichService);
        }
        if (addToBackStack) {
            ft.addToBackStack(null);
        }
        ft.commit();
    }

    @Override
    public Fragment newFragment(BundleContext bundleContext, String whichService, String whichFragment, Bundle params) {
        return ApkPluginFragment.newInstance(whichService,whichFragment,params);
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
