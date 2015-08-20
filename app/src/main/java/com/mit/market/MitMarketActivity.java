package com.mit.market;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.view.Window;
import android.widget.Toast;

import com.applite.common.Constant;
import com.applite.common.IconCache;
import com.applite.common.LogUtils;
import com.applite.dm.DownloadPagerFragment;
import com.applite.homepage.HomePageFragment;
import com.applite.homepage.HomePageListFragment;
import com.applite.homepage.PersonalFragment;
import com.mit.applite.main.DetailFragment;
import com.mit.applite.search.main.SearchFragment;
import com.mit.appliteupdate.main.UpdateFragment;
import com.applite.sharedpreferences.AppliteSPUtils;
import com.mit.main.GuideFragment;
import com.mit.mitupdatesdk.MitMobclickAgent;
import com.mit.mitupdatesdk.MitUpdateAgent;
import com.applite.android.R;
import com.osgi.extra.OSGIBaseFragment;
import com.osgi.extra.OSGIServiceClient;
import com.osgi.extra.OSGIServiceHost;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MitMarketActivity extends ActionBarActivity implements OSGIServiceHost{
    private static final String TAG = "applite_MitMarketActivity";

    private boolean personal_flag = false;
    Toast toast;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtils.d(TAG,"onCreate:"+savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mit_market);

        setOverflowShowingAlways();

        long current = System.currentTimeMillis();
        MitMobclickAgent.openActivityDurationTrack(false);//禁止默认的页面统计方式
        MitMobclickAgent.updateOnlineConfig(this);
        MitUpdateAgent.setDebug(true);
        MitUpdateAgent.update(this);
        LogUtils.d(TAG,"onCreate take "+(System.currentTimeMillis()-current)+" ms");

        registerClients();

        FragmentManager fgm = getSupportFragmentManager();
        Fragment fg = fgm.findFragmentById(R.id.container);
        if (null == fg ){
            Intent intent = getIntent();
            if (null != intent && Constant.UPDATE_FRAGMENT_NOT.equals(intent.getStringExtra("update"))){
                jumpto(Constant.OSGI_SERVICE_LOGO_FRAGMENT,null,
                        GuideFragment.newBundles(Constant.OSGI_SERVICE_UPDATE_FRAGMENT,null,null,false),false);
            }else {
                jumpto(Constant.OSGI_SERVICE_LOGO_FRAGMENT,null,
                        GuideFragment.newBundles(Constant.OSGI_SERVICE_MAIN_FRAGMENT,null,null,false),false);
            }
//            fgm.beginTransaction()
//                    .replace(R.id.container,fg,Constant.OSGI_SERVICE_LOGO_FRAGMENT)
//                    .commit();
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
        MitMobclickAgent.onResume(this);       //统计时长
    }

    public void onPause() {
        super.onPause();
        MitMobclickAgent.onPause(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_mit_market, menu);
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
            jumpto(Constant.OSGI_SERVICE_LOGO_FRAGMENT,null,
                    GuideFragment.newBundles(Constant.OSGI_SERVICE_UPDATE_FRAGMENT,null,null,false),
                    false);
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
        unregisterClients();
        IconCache.getInstance(this).flush();
    }

//    @Override
//    public void notify(Bundle params) {
//        if (null != params){
//            int number = params.getInt("number");
//            UpdateNotification.getInstance().showNot(MitMarketActivity.this, String.valueOf(number));
//        }
//    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK) { //按下的如果是BACK，同时没有重复
            //do something here
            exit();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private long clickTime = 0; //记录第一次点击的时间

    private void exit() {
        if ((System.currentTimeMillis() - clickTime) > 2000) {
            Toast.makeText(getApplicationContext(), "再按一次退出应用商店",Toast.LENGTH_SHORT).show();
            clickTime = System.currentTimeMillis();
        } else {
            this.finish();
        }
    }

    @Override
    public void jumpto(String targetService,
                       String targetFragment,
                       Bundle params,
                       boolean addToBackStack) {
        FragmentManager fgmgr = getSupportFragmentManager();
        FragmentTransaction ft = fgmgr.beginTransaction();

        boolean result = fgmgr.popBackStackImmediate(targetService,FragmentManager.POP_BACK_STACK_INCLUSIVE);
        LogUtils.d(TAG, "popBackStackImmediate("+targetService+") is "+result);

        OSGIBaseFragment newFragment = null;
//        if (Constant.OSGI_SERVICE_LOGO_FRAGMENT == targetService){
////            newFragment = (OSGIBaseFragment)fgmgr.findFragmentByTag("GuideFragment");
//            AppliteSPUtils.put(getApplicationContext(), AppliteSPUtils.ISGUIDE, true);
//        }

        newFragment = (OSGIBaseFragment) newFragment(targetService, targetFragment, params);
//        ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        Fragment current = fgmgr.findFragmentById(R.id.container);
        if (null != current) {
            if (!addToBackStack){
                ft.remove(current);
            }else{
                ft.hide(current);
            }
        }
        if (!newFragment.isAdded()) {
            ft.add(R.id.container, newFragment);
        }else{
            ft.show(newFragment);
        }
        if (addToBackStack) {
            ft.addToBackStack(targetService);
        }
        ft.commit();
    }

    @Override
    public Fragment newFragment(String whichService, String whichFragment, Bundle params) {
        return OSGIServiceClient.getInstance().newOSGIFragment(whichService,whichFragment,params);
    }

    @Override
    public void jumptoHomepage(String category, String name,boolean addToBackstack) {
        jumpto(Constant.OSGI_SERVICE_MAIN_FRAGMENT + "#" + category,
                HomePageFragment.class.getName(),
                HomePageFragment.newBundle(category, name), addToBackstack);
    }

    @Override
    public void jumptoDetail(String packageName,String name,String imgUrl,boolean addToBackstack) {
        jumpto(Constant.OSGI_SERVICE_DETAIL_FRAGMENT,
                DetailFragment.class.getName(),
                DetailFragment.newBundle(packageName,name,imgUrl),
                true);
    }

    @Override
    public void jumptoDetail(String httpUrl,boolean addToBackstack) {
        //TODO
    }

    @Override
    public void jumptoTopic(String key,String name,int step,String datatype,boolean addToBackstack) {
        jumpto(Constant.OSGI_SERVICE_TOPIC_FRAGMENT+"#"+key,
                HomePageListFragment.class.getName(),
                HomePageListFragment.newBundle(key, name, step, datatype),
                addToBackstack);
    }

    @Override
    public void jumptoSearch(boolean addToBackstack) {
        jumpto(Constant.OSGI_SERVICE_SEARCH_FRAGMENT,
                SearchFragment.class.getName(),
                null,addToBackstack);
    }

    @Override
    public void jumptoPersonal(boolean addToBackstack) {
        jumpto(Constant.OSGI_SERVICE_MAIN_FRAGMENT,
                PersonalFragment.class.getName(),
                null,addToBackstack);
    }

    @Override
    public void jumptoUpdate(boolean addToBackstack) {
        jumpto(Constant.OSGI_SERVICE_UPDATE_FRAGMENT,
                UpdateFragment.class.getName(),
                null,addToBackstack);
    }

    @Override
    public void jumptoDownloadManager(boolean addToBackstack) {
        jumpto(Constant.OSGI_SERVICE_DM_FRAGMENT,
                DownloadPagerFragment.class.getName(),
                null,true);
    }

    @Override
    public void jumptoMylife(boolean addToBackstack) {
        jumpto(Constant.OSGI_SERVICE_LOGO_FRAGMENT,
                GuideFragment.class.getName(),
                GuideFragment.newBundles(null,null,null,true),
                addToBackstack);
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

    private void registerClients(){
        OSGIServiceClient.getInstance().register(Constant.OSGI_SERVICE_MAIN_FRAGMENT, "com.applite.homepage.HomePageFragment");
        OSGIServiceClient.getInstance().register(Constant.OSGI_SERVICE_TOPIC_FRAGMENT, "com.applite.homepage.HomePageListFragment");
        OSGIServiceClient.getInstance().register(Constant.OSGI_SERVICE_DETAIL_FRAGMENT, "com.mit.applite.main.DetailFragment");
        OSGIServiceClient.getInstance().register(Constant.OSGI_SERVICE_SEARCH_FRAGMENT, "com.mit.applite.search.main.SearchFragment");
        OSGIServiceClient.getInstance().register(Constant.OSGI_SERVICE_UPDATE_FRAGMENT, "com.mit.appliteupdate.main.UpdateFragment");
        OSGIServiceClient.getInstance().register(Constant.OSGI_SERVICE_DM_FRAGMENT, "com.applite.dm.DownloadPagerFragment");
        OSGIServiceClient.getInstance().register(Constant.OSGI_SERVICE_LOGO_FRAGMENT, "com.mit.main.GuideFragment");
    }

    private void unregisterClients(){
        OSGIServiceClient.getInstance().unregister(Constant.OSGI_SERVICE_MAIN_FRAGMENT);
        OSGIServiceClient.getInstance().unregister(Constant.OSGI_SERVICE_TOPIC_FRAGMENT);
        OSGIServiceClient.getInstance().unregister(Constant.OSGI_SERVICE_DETAIL_FRAGMENT);
        OSGIServiceClient.getInstance().unregister(Constant.OSGI_SERVICE_SEARCH_FRAGMENT);
        OSGIServiceClient.getInstance().unregister(Constant.OSGI_SERVICE_UPDATE_FRAGMENT);
        OSGIServiceClient.getInstance().unregister(Constant.OSGI_SERVICE_DM_FRAGMENT);
        OSGIServiceClient.getInstance().unregister(Constant.OSGI_SERVICE_LOGO_FRAGMENT);
    }
}
