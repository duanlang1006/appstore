package com.mit.market;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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

import com.applite.android.R;
import com.applite.common.AppliteUtils;
import com.applite.common.Constant;
import com.applite.common.LogUtils;
import com.applite.dm.main.DownloadPagerFragment;
import com.applite.homepage.AboutFragment;
import com.applite.homepage.DownloadSizeLimit;
import com.applite.homepage.HomePageFragment;
import com.applite.homepage.HomePageListFragment;
import com.applite.homepage.LuckyFragment;
import com.applite.homepage.PersonalFragment;
import com.applite.homepage.SettingFragment;
import com.applite.sharedpreferences.AppliteSPUtils;
import com.mit.afinal.FinalHttp;
import com.mit.afinal.http.AjaxCallBack;
import com.mit.afinal.http.AjaxParams;
import com.mit.applite.main.DetailFragment;
import com.mit.applite.search.main.SearchFragment;
import com.mit.appliteupdate.main.UninstallReceiver;
import com.mit.appliteupdate.main.UpdateFragment;
import com.mit.impl.ImplAgent;
import com.mit.main.GuideFragment;
import com.mit.mitupdatesdk.MitMobclickAgent;
import com.mit.mitupdatesdk.MitUpdateAgent;
import com.osgi.extra.OSGIBaseFragment;
import com.osgi.extra.OSGIServiceClient;
import com.osgi.extra.OSGIServiceHost;
import com.umeng.fb.ConversationActivity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MitMarketActivity extends ActionBarActivity implements OSGIServiceHost {
    private static final String TAG = "applite_MitMarketActivity";

    private boolean personal_flag = false;
    private Toast toast = null;
    private UninstallReceiver mUninstallReceiver;

    private SharedPreferences.OnSharedPreferenceChangeListener mConfigListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (AppliteSPUtils.DELETE_PACKAGE.equals(key)) {
                configImpl();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtils.d(TAG, "onCreate:" + savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mit_market);

        setOverflowShowingAlways();

        long current = System.currentTimeMillis();
        MitMobclickAgent.openActivityDurationTrack(false);//禁止默认的页面统计方式
        MitMobclickAgent.updateOnlineConfig(this);
        MitUpdateAgent.setDebug(false);

        LogUtils.d(TAG, "onCreate take " + (System.currentTimeMillis() - current) + " ms");

        registerClients();

        AppliteSPUtils.registerChangeListener(this, mConfigListener);
        configImpl();

        FragmentManager fgm = getSupportFragmentManager();
        Fragment fg = fgm.findFragmentById(R.id.container);
        if (null == fg) {
            Intent intent = getIntent();
            LogUtils.i(TAG, "onCreate得到点击通知栏发过来的意图:" + "intent:" + intent +
                    "-----intent.getStringExtra:" + intent.getStringExtra("update"));
            if (null != intent && Constant.UPDATE_FRAGMENT_NOT.equals(intent.getStringExtra("update"))) {
                Bundle bundle = GuideFragment.newBundles(Constant.OSGI_SERVICE_UPDATE_FRAGMENT, null, null, false, true);
                bundle.putString("update_data", intent.getStringExtra("update_data"));
                jumpto(Constant.OSGI_SERVICE_LOGO_FRAGMENT, null, bundle, false);
            } else if (null != intent && "101".equals(intent.getStringExtra("notify"))) {
                AppliteSPUtils.put(this, "position", intent.getIntExtra("position", 0));
                jumptoDownloadManager(true);
            } else {
                jumpto(Constant.OSGI_SERVICE_LOGO_FRAGMENT, null,
                        GuideFragment.newBundles(Constant.OSGI_SERVICE_MAIN_FRAGMENT, null, null, false, false), false);
            }
//            fgm.beginTransaction()
//                    .replace(R.id.container,fg,Constant.OSGI_SERVICE_LOGO_FRAGMENT)
//                    .commit();
        }
        post();
        registerUninstallReceiver();
    }

    /**
     * 注册卸载APK监听广播
     */
    private void registerUninstallReceiver() {
        mUninstallReceiver = new UninstallReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addDataScheme("package");
        registerReceiver(mUninstallReceiver, filter);
    }

    private void post() {
//        RequestParams params = new RequestParams();
//        params.addBodyParameter("appkey", AppliteUtils.getMitMetaDataValue(this, Constant.META_DATA_MIT));
//        params.addBodyParameter("packagename", this.getPackageName());
//        params.addBodyParameter("type", "update_management");
//        params.addBodyParameter("protocol_version", Constant.PROTOCOL_VERSION);
//        params.addBodyParameter("update_info", AppliteUtils.getAllApkData(this));
//        HttpUtils mHttpUtils = new HttpUtils();
//        mHttpUtils.send(HttpRequest.HttpMethod.POST, Constant.URL, params, new RequestCallBack<String>() {
//            @Override
//            public void onSuccess(ResponseInfo<String> responseInfo) {
//                LogUtils.i(TAG, "首页更新请求成功，resulit：" + responseInfo.result);
//                AppliteSPUtils.put(MitMarketActivity.this, AppliteSPUtils.UPDATE_DATA, responseInfo.result);
//            }
//
//            @Override
//            public void onFailure(HttpException e, String s) {
//                LogUtils.i(TAG, "首页更新请求失败：" + s);
//            }
//
//        });

        AjaxParams params = new AjaxParams();
        params.put("appkey", AppliteUtils.getMitMetaDataValue(this, Constant.META_DATA_MIT));
        params.put("packagename", this.getPackageName());
        params.put("type", "update_management");
        params.put("protocol_version", Constant.PROTOCOL_VERSION);
        params.put("update_info", AppliteUtils.getAllApkData(this));
        FinalHttp mFinalHttp = new FinalHttp();
        mFinalHttp.post(Constant.URL, params, new AjaxCallBack<String>() {
            @Override
            public void onSuccess(String responseInfo) {
                LogUtils.i(TAG, "首页更新请求成功，resulit：" + responseInfo);
                AppliteSPUtils.put(MitMarketActivity.this, AppliteSPUtils.UPDATE_DATA, responseInfo);
            }

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                LogUtils.i(TAG, "首页更新请求失败：" + strMsg);
            }
        });

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
        if (R.id.action_search == id) {
            jumptoSearch(null, true, null, null, null);
            return true;
        } else if (R.id.action_dm == id) {
            jumptoDownloadManager(true);
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
        if (null != intent && Constant.UPDATE_FRAGMENT_NOT.equals(intent.getStringExtra("update"))) {
            LogUtils.i(TAG, "onNewIntent得到点击通知栏发过来的意图");
            Bundle bundle = GuideFragment.newBundles(Constant.OSGI_SERVICE_UPDATE_FRAGMENT, null, null, false, true);
            bundle.putString("update_data", intent.getStringExtra("update_data"));
            jumpto(Constant.OSGI_SERVICE_LOGO_FRAGMENT, null, bundle, false);
        } else if (null != intent && "101".equals(intent.getStringExtra("notify"))) {
            AppliteSPUtils.put(this, "position", intent.getIntExtra("position", 0));
            jumptoDownloadManager(true);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        LogUtils.d(TAG, "onSaveInstanceState");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterClients();
//        IconCache.getInstance(this).flush();

        unregisterReceiver(mUninstallReceiver);

        //置更新数据为空
        AppliteSPUtils.put(this, AppliteSPUtils.UPDATE_DATA, "");
        AppliteSPUtils.unregisterChangeListener(this, mConfigListener);
    }

//    @Override
//    public void notify(Bundle params) {
//        if (null != params){
//            int number = params.getInt("number");
//            UpdateNotification.getInstance().showNot(MitMarketActivity.this, String.valueOf(number));
//        }
//    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) { //按下的如果是BACK，同时没有重复
            //do something here
            if (!getSupportFragmentManager().popBackStackImmediate()) {
                exit();
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private long clickTime = 0; //记录第一次点击的时间

    private void exit() {
        if ((System.currentTimeMillis() - clickTime) > 2000) {
            toast = toast.makeText(getApplicationContext(), "再按一次退出应用商店", Toast.LENGTH_SHORT);
            toast.show();
            clickTime = System.currentTimeMillis();
        } else {
            if (null != toast) {
                toast.cancel();
            }
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

        boolean result = fgmgr.popBackStackImmediate(targetService, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        LogUtils.d(TAG, "popBackStackImmediate(" + targetService + ") is " + result);

        OSGIBaseFragment newFragment = null;
//        if (Constant.OSGI_SERVICE_LOGO_FRAGMENT == targetService){
////            newFragment = (OSGIBaseFragment)fgmgr.findFragmentByTag("GuideFragment");
//            AppliteSPUtils.put(getApplicationContext(), AppliteSPUtils.ISGUIDE, true);
//        }

        newFragment = (OSGIBaseFragment) newFragment(targetService, targetFragment, params);
//        ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        Fragment current = fgmgr.findFragmentById(R.id.container);
        if (null != current) {
            if (!addToBackStack) {
                ft.remove(current);
            } else {
                ft.hide(current);
            }
        }
        if (null != newFragment) {
            if (!newFragment.isAdded()) {
                ft.replace(R.id.container, newFragment, targetService);
            } else {
                ft.show(newFragment);
            }
        }
        if (addToBackStack) {
            ft.addToBackStack(targetService);
        }
//        ft.commit();      //报错java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState
        ft.commitAllowingStateLoss();
    }

    @Override
    public Fragment newFragment(String whichService, String whichFragment, Bundle params) {
        return OSGIServiceClient.getInstance().newOSGIFragment(whichService, whichFragment, params);
    }

    @Override
    public void jumptoHomepage(String category, String name, boolean addToBackstack) {
        jumpto(Constant.OSGI_SERVICE_MAIN_FRAGMENT + "#" + category,
                HomePageFragment.class.getName(),
                HomePageFragment.newBundle(category, name), addToBackstack);
    }

    @Override
    public void jumptoDetail(String packageName, String name, String imgUrl, int versionCode, String boxlabelvalue, boolean addToBackstack) {
        jumpto(Constant.OSGI_SERVICE_DETAIL_FRAGMENT,
                DetailFragment.class.getName(),
                DetailFragment.newBundle(packageName, name, imgUrl, versionCode, boxlabelvalue),
                addToBackstack);
    }

    @Override
    public void jumptoDetail(String httpUrl, boolean addToBackstack) {
        //TODO
    }

    @Override
    public void jumptoTopic(String key, String name, int step, String datatype, boolean addToBackstack) {
        jumpto(Constant.OSGI_SERVICE_TOPIC_FRAGMENT + "#" + key,
                HomePageListFragment.class.getName(),
                HomePageListFragment.newBundle(key, name, step, datatype),
                addToBackstack);
    }

    @Override
    public void jumptoSearch(String detailTag, boolean addToBackstack, String info, String keyword, String hintword) {
        jumpto(Constant.OSGI_SERVICE_SEARCH_FRAGMENT,
                SearchFragment.class.getName(),
                SearchFragment.newBundle(detailTag, info, keyword, hintword),
                addToBackstack);
    }

    @Override
    public void jumptoPersonal(boolean addToBackstack) {
        jumpto(PersonalFragment.class.getName(),
                PersonalFragment.class.getName(),
                null, addToBackstack);
    }

    @Override
    public void jumptoUpdate(boolean addToBackstack) {
        jumpto(Constant.OSGI_SERVICE_UPDATE_FRAGMENT,
                UpdateFragment.class.getName(),
                null, addToBackstack);
    }

    @Override
    public void jumptoDownloadManager(boolean addToBackstack) {
        jumpto(Constant.OSGI_SERVICE_DM_FRAGMENT,
                DownloadPagerFragment.class.getName(),
                null, addToBackstack);
    }

    @Override
    public void jumptoMylife(boolean addToBackstack) {
        jumpto(Constant.OSGI_SERVICE_LOGO_FRAGMENT,
                GuideFragment.class.getName(),
                GuideFragment.newBundles(null, null, null, true, false),
                addToBackstack);
    }

    @Override
    public void jumptoSetting(boolean addToBackstack) {
        jumpto(Constant.OSGI_SERVICE_SETTING_FRAGMENT,
                SettingFragment.class.getName(),
                null, true);
//        Intent intent = new Intent(this, SettingsActivity.class);
//        startActivity(intent);
    }

    public void jumptoLucky(boolean addToBackstack) {
        jumpto(Constant.OSGI_SERVICE_LUCKY_FRAGMENT,
                LuckyFragment.class.getName(),
                null, addToBackstack);
    }

    public void jumptoDownloadSizeLimit(boolean addToBackstack) {
        jumpto(Constant.OSGI_SERVICE_SIZELIMIT_FRAGMENT,
                DownloadSizeLimit.class.getName(),
                null, addToBackstack);
    }

    @Override
    public void jumptoAbout(boolean addToBackstack) {
        jumpto(Constant.OSGI_SERVICE_ABOUT_FRAGMENT,
                AboutFragment.class.getName(),
                null, addToBackstack);
    }

    @Override
    public void jumptoConversation() {
        com.umeng.fb.util.Res.setPackageName(R.class.getPackage().getName());
        Intent intent = new Intent(this, ConversationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void configImpl() {
        boolean delete = (boolean) AppliteSPUtils.get(MitMarketActivity.this, AppliteSPUtils.DELETE_PACKAGE, false);
        ImplAgent.getInstance(MitMarketActivity.this).configDeleteAfterInstalled(delete);
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

    private void registerClients() {
        OSGIServiceClient.getInstance().register(Constant.OSGI_SERVICE_MAIN_FRAGMENT, "com.applite.homepage.HomePageFragment");
        OSGIServiceClient.getInstance().register(Constant.OSGI_SERVICE_TOPIC_FRAGMENT, "com.applite.homepage.HomePageListFragment");
        OSGIServiceClient.getInstance().register(Constant.OSGI_SERVICE_DETAIL_FRAGMENT, "com.mit.applite.main.DetailFragment");
        OSGIServiceClient.getInstance().register(Constant.OSGI_SERVICE_SEARCH_FRAGMENT, "com.mit.applite.search.main.SearchFragment");
        OSGIServiceClient.getInstance().register(Constant.OSGI_SERVICE_UPDATE_FRAGMENT, "com.mit.appliteupdate.main.UpdateFragment");
        OSGIServiceClient.getInstance().register(Constant.OSGI_SERVICE_DM_FRAGMENT, "com.applite.dm.DownloadPagerFragment");
        OSGIServiceClient.getInstance().register(Constant.OSGI_SERVICE_LOGO_FRAGMENT, "com.mit.main.GuideFragment");
        OSGIServiceClient.getInstance().register(Constant.OSGI_SERVICE_SETTING_FRAGMENT, "com.applite.homepage.SettingFragment");
        OSGIServiceClient.getInstance().register(Constant.OSGI_SERVICE_LUCKY_FRAGMENT, "com.applite.homepage.LuckyFragment");
        OSGIServiceClient.getInstance().register(Constant.OSGI_SERVICE_ABOUT_FRAGMENT, "com.applite.homepage.AboutFragment");
        OSGIServiceClient.getInstance().register(Constant.OSGI_SERVICE_SIZELIMIT_FRAGMENT, "com.applite.homepage.sizelimit");
    }

    private void unregisterClients() {
        OSGIServiceClient.getInstance().unregister(Constant.OSGI_SERVICE_MAIN_FRAGMENT);
        OSGIServiceClient.getInstance().unregister(Constant.OSGI_SERVICE_TOPIC_FRAGMENT);
        OSGIServiceClient.getInstance().unregister(Constant.OSGI_SERVICE_DETAIL_FRAGMENT);
        OSGIServiceClient.getInstance().unregister(Constant.OSGI_SERVICE_SEARCH_FRAGMENT);
        OSGIServiceClient.getInstance().unregister(Constant.OSGI_SERVICE_UPDATE_FRAGMENT);
        OSGIServiceClient.getInstance().unregister(Constant.OSGI_SERVICE_DM_FRAGMENT);
        OSGIServiceClient.getInstance().unregister(Constant.OSGI_SERVICE_LOGO_FRAGMENT);
        OSGIServiceClient.getInstance().unregister(Constant.OSGI_SERVICE_SETTING_FRAGMENT);
        OSGIServiceClient.getInstance().unregister(Constant.OSGI_SERVICE_LUCKY_FRAGMENT);
        OSGIServiceClient.getInstance().unregister(Constant.OSGI_SERVICE_ABOUT_FRAGMENT);
    }
}
