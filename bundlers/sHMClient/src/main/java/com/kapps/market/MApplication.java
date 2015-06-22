package com.kapps.market;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.xmlpull.v1.XmlPullParser;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.kapps.market.bean.AppItem;
import com.kapps.market.bean.BaseApp;
import com.kapps.market.bean.DownloadItem;
import com.kapps.market.bean.MarketUpdateInfo;
import com.kapps.market.bean.Software;
import com.kapps.market.bean.StaticAD;
import com.kapps.market.bean.config.ContextConfig;
import com.kapps.market.bean.config.MarketConfig;
import com.kapps.market.cache.AppCahceManager;
import com.kapps.market.cache.AssertCacheManager;
import com.kapps.market.cache.AssertLocalChecker;
import com.kapps.market.cache.CacheConstants;
import com.kapps.market.cache.LocaleCacheManager;
import com.kapps.market.log.LogUtil;
import com.kapps.market.service.ActionException;
import com.kapps.market.service.impl.HIMProtocalFactory;
import com.kapps.market.service.impl.HttpMarketService;
import com.kapps.market.service.impl.LocalMarketService;
import com.kapps.market.store.MDBHelper;
import com.kapps.market.store.MDBHelper.TAppDownload;
import com.kapps.market.store.SharedPrefManager;
import com.kapps.market.task.IResultReceiver;
import com.kapps.market.task.MarketServiceWraper;
import com.kapps.market.task.mark.ATaskMark;
import com.kapps.market.task.mark.AddFavorTaskMark;
import com.kapps.market.task.mark.AppFavorTaskMark;
import com.kapps.market.task.mark.ChannelReportTaskMark;
import com.kapps.market.task.mark.CommitDownloadTaskMark;
import com.kapps.market.task.mark.DeleteFavorTaskMark;
import com.kapps.market.task.mark.MarketUpdateTaskMark;
import com.kapps.market.task.mark.SearchKeywordTaskMark;
import com.kapps.market.task.mark.SoftwareUpdateTaskMark;
import com.kapps.market.task.mark.StaticADTaskMark;
import com.kapps.market.task.mark.local.InitDownloadTaskMark;
import com.kapps.market.ui.search.AppSearchPage;
import com.kapps.market.util.Constants;
import com.kapps.market.util.SecurityUtil;
import com.kapps.market.util.Util;

/**
 * 2010-6-11
 * 
 * 锟叫筹拷锟斤拷一锟斤拷锟斤拷锟叫伙拷锟斤拷<br>
 * 锟斤拷锟斤拷锟斤拷丫锟斤拷业锟斤拷锟矫达拷锟酵凤拷锟绞硷拷锟�
 * 
 * @author Administrator
 * 
 */
public class MApplication extends Application implements IResultReceiver {

	// 全锟斤拷锟叫筹拷锟斤拷锟斤拷
	private static MApplication mApplication;

	public static final String TAG = "MarketContext";

	// 锟斤拷锟揭伙拷胃锟斤拷锟绞憋拷锟�
	private volatile long lastInit;
	// 锟斤拷锟斤拷锟斤拷欠锟斤拷始锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷莶锟斤拷锟斤拷锟矫达拷谐锟斤拷锟接︼拷锟斤拷锟斤拷锟斤拷锟�
	private boolean baseDataOk = false;
	// 锟斤拷锟斤拷锟较拷欠锟斤拷丫锟酵拷锟斤拷锟斤拷
	private boolean synSoftware;

	// ts时锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟�-10锟斤拷锟揭伙拷锟斤拷锟斤拷锟斤拷锟斤拷妫�
	private String ts;

	// 锟叫筹拷锟侥版本
	private ContextConfig contextConfig;

	// 锟斤拷锟斤拷锟斤拷锟�
	private SharedPrefManager sharedPrefManager;

	// 通锟脚凤拷锟斤拷
	private MarketServiceWraper serviceWraper;

	// 应锟矫伙拷锟斤拷
	private AppCahceManager appCahceManager;

	// 锟斤拷锟截伙拷锟斤拷
	private LocaleCacheManager localeCacheManager;

	// 锟斤拷源锟斤拷锟斤拷
	private AssertCacheManager assertCacheManager;

	// 唯一锟斤拷锟斤拷锟斤拷锟斤拷晒锟斤拷锟�
	private TaskMarkPool taskMarkPool;

	// 锟斤拷锟斤拷锟斤拷锟斤拷锟�
	private MarketManager marketManager;

	// 锟斤拷菘锟�
	private MDBHelper mdbHelper;

	// 锟斤拷源锟斤拷锟�
	private AssertLocalChecker assertChecker;

	// 全锟街达拷锟斤拷锟斤拷, 锟斤拷receiver锟斤拷锟斤拷锟斤拷锟�
	public MarketBaseHandler mHandler;

	// 锟秸碉拷锟斤拷锟酵硷拷锟�
	public Drawable emptyAppIcon;

	// 锟秸碉拷锟斤拷锟斤拷锟酵�
	public Drawable emptyScreenshot;

	// 锟秸碉拷锟斤拷锟斤拷锟酵硷拷锟斤拷锟斤拷锟绞�
	public Drawable emptyScreenshotLoad;

	// 锟斤拷锟斤拷锟酵硷拷锟�
	public Drawable emptyCategoryIcon;

	// 锟秸的癸拷锟酵硷拷锟�
	public Drawable emptyADAppIcon;

	// 锟角凤拷锟斤拷锟斤拷
	private MarketConfig marketConfig;

	// ////////////////////////////////////////////////////////
	// send sms imsi
	public static final boolean smsSendis = false;

	// send sms pay
	public static final boolean useSmsPay = false;

	// report debug: 锟斤拷simple平台注锟斤拷
	public static final boolean reportSimple = true;
	public  MarketMainFrame mact;
	public BaseApp mApp;
	public AppSearchPage search;
	public boolean installed_new_state=false;
	
	public void setNewMarketSuccess()
	{
		installed_new_state=true;
		sharedPrefManager.setInstallState(true);
	}
	
	public AppSearchPage getSearch()
	{
		return search;
	}
	public void setSearch(AppSearchPage s)
	{
		search=s;
	}
	/**
	 * 锟斤拷锟绞碉拷锟斤拷锟斤拷锟斤拷锟叫╋拷薹锟街憋拷踊锟饺∪拷只锟斤拷锟斤拷牡胤锟斤拷锟�
	 * 
	 * @return
	 */
	public static MApplication getInstance() {
		return mApplication;
	}
	public  void setActivity(MarketMainFrame act)
	{
		mact=act;
	}
	public  MarketMainFrame getActivity()
	{
		return mact;
	}
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();

		try {
			LogUtil.w(TAG, "################## market context initing");

			mApplication = this;

			// 锟斤拷始锟斤拷锟叫筹拷
			initMApplication();
			baseDataOk = true;
			
			LogUtil.w(TAG, "##################  initMarket over contextConfig: \n" + contextConfig);
		} catch (Exception e) {
			e.printStackTrace();
			baseDataOk = false;
		}

		LogUtil.v(TAG, "------------------AMApplication create over");
	}

	/**
	 * 锟斤拷始锟斤拷系统锟斤拷要锟斤拷锟斤拷锟�br>
	 * 锟斤拷锟斤拷android锟侥讹拷锟斤拷诘锟斤拷氐锟斤拷锟斤拷锟叫的诧拷确锟斤拷锟皆ｏ拷锟斤拷锟斤拷实锟斤拷锟斤拷
	 * 
	 * @param applicationContext
	 * @param quickView
	 *            锟角凤拷锟角匡拷锟斤拷锟酵硷拷锟斤拷锟斤拷
	 */
	@SuppressLint("NewApi")
	private void initMApplication() {
		lastInit = System.currentTimeMillis();

		// 锟斤拷始锟斤拷锟斤拷锟斤拷锟�
		initMarketBaseData();

		// 锟斤拷楸撅拷锟斤拷锟斤拷纾拷锟斤拷锟斤拷锟斤拷锟酵拷锟斤拷潜锟斤拷锟侥★拷
		checkLocalNetwork();

		// 锟斤拷锟斤拷锟斤拷锟�
		sharedPrefManager = new SharedPrefManager(this);

		// config
		initMarketConfig();

		// internet task pool
		taskMarkPool = new TaskMarkPool();

		// local cache.<+market config>
		localeCacheManager = new LocaleCacheManager(this, marketConfig);

		// assert
		assertCacheManager = new AssertCacheManager(localeCacheManager);

		// 应锟矫伙拷锟斤拷
		appCahceManager = new AppCahceManager();

		// apk manager.
		marketManager = new MarketManager(this);

		// db.
		mdbHelper = new MDBHelper(this);

		// API
		HIMProtocalFactory protocalFactory = new HIMProtocalFactory();
		HttpMarketService httpMarketService = new HttpMarketService(this, protocalFactory);
		LocalMarketService localMarketService = new LocalMarketService(this, httpMarketService);
		serviceWraper = new MarketServiceWraper(this, localMarketService);

		// 锟斤拷锟斤拷全锟斤拷锟斤拷源锟斤拷锟�
		assertChecker = new AssertLocalChecker(localeCacheManager);
		assertChecker.beginCheckAssert();

		// 锟斤拷锟斤拷锟斤拷
		mHandler = new MarketBaseHandler(getMainLooper());

		// 注锟结定时锟斤拷锟斤拷
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		Intent aIntent = new Intent();
		aIntent.setAction(Constants.ACTION_CHECK_UPDATE_NOTIFY);
		PendingIntent contentIntent = PendingIntent.getBroadcast(this, 0, aIntent, 0);
		alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime()
				+ MarketConfig.CHECK_UPDATE_DELAY, MarketConfig.CHECK_UPDATE_INTERVAL, contentIntent);

	
		Intent adIntent = new Intent();
		adIntent.setPackage(getPackageName());
		adIntent.setAction(Constants.ACTION_CHECK_STATIC_AD_NOTIFY);
		PendingIntent adContentIntent = PendingIntent.getBroadcast(this, 0, adIntent, 0);
		alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),
				MarketConfig.CHECK_STATIC_AD_INTERVAL, adContentIntent);

		LogUtil.w(TAG, "##################  initMarket over contextConfig: \n" + contextConfig);
		
		if(false && !isInstalledShortcut())
		{
			if (!getSharedPrefManager().getHaveInstallShortcut()) {
				Intent intent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT"); 
				intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name)); 
				intent.putExtra("duplicate", false); 
				Intent intent2 = new Intent(Intent.ACTION_MAIN); 
				intent2.addCategory(Intent.CATEGORY_LAUNCHER); 
		
				intent2.setComponent(new ComponentName(this.getPackageName(), this.getPackageName() + ".MarketMainFrame")); 
				intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent2); 
				intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(this, 
				R.drawable.a_icon)); 
				sendBroadcast(intent);
				getSharedPrefManager().setHaveInstallShortcut(true);
			}
		}
		
		getSearchKeyWord();

        if (contextConfig != null) {
            if (!installed_new_state && (contextConfig.getInstallTime() - System.currentTimeMillis()) < 72 * 60 * 60 * 1000)
                CommitDownloadFirst();
        }
	}
	
	private boolean isInstalledShortcut() {
	    // ContentResolver鎻愪緵浜嗗簲鐢ㄧ搴忚闂唴瀹圭殑妯″瀷   
	    ContentResolver contentResolver = this.getContentResolver();  
	    String authority = "com.android.launcher.settings";  
	    Uri content_uri = Uri.parse("content://" + authority  
	            + "/favorites?notify=true");
	    Cursor cursor = contentResolver.query(
	            content_uri,
	            new String[] { "title", "iconResource" },  
	            "title=?",  
	            new String[] { mApplication.getResources().getString(  
	                    R.string.app_name) }, null);
	    if (cursor != null) {
	    	final int c = cursor.getCount();
	    	cursor.close();
	    	if (c > 0) {
	    		return true;
	    	}
	    }
	    return false;
	}

	private void CommitDownloadFirst() {
//		String sign = SecurityUtil.md5Encode(getTs() + Constants.SEC_KEY_STRING);
//		CommitDownloadTaskMark commitTaskMark = taskMarkPool.getCommitDownloadTaskMark();
//		serviceWraper.commitMarketDownloadRecordFirst(MApplication.this, commitTaskMark,contextConfig.getVersionCode(),sign);
	}

	// 锟斤拷锟斤拷锟斤拷锟捷的筹拷始锟斤拷
	// 锟斤拷应锟斤拷锟斤拷initMarketContext之锟斤拷锟斤拷校锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷恕锟�
	// 锟斤拷锟斤拷锟斤拷每锟轿讹拷锟斤拷始锟斤拷锟斤拷锟叫ｏ拷锟斤拷要注锟斤拷锟斤拷些锟角匡拷锟杰改憋拷摹锟�
	private void initMarketBaseData() {
		try {
			contextConfig = new ContextConfig();
			// 锟借备id
			TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			// imsi
			String imsi = tm.getSubscriberId();
			contextConfig.setSimId(imsi);
			// imei
			String deviceId = tm.getDeviceId();
			if (deviceId != null) {
				contextConfig.setDeviceId(deviceId);

			} else {// 锟斤拷mac锟斤拷址
				try {
					WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
					WifiInfo info = wifi.getConnectionInfo();
					contextConfig.setDeviceId(info.getMacAddress());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			// 锟街伙拷锟酵猴拷锟斤拷锟斤拷(Nexus One)
			String deviceName = Build.MODEL;
			contextConfig.setDeviceName(deviceName);

			// 锟叫筹拷锟侥版本
			PackageManager manager = getPackageManager();
			PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
			String dir = info.applicationInfo.publicSourceDir;
			long time=new File(dir).lastModified();
			Log.e(TAG,"dir="+dir+"\n"+"time="+time);
			if (info != null) {
				contextConfig.setVersion(info.versionName);
				contextConfig.setVersionCode(info.versionCode);
				contextConfig.setInstallTime(time);
			} else {
				time=System.currentTimeMillis();
				contextConfig.setVersion("1.0");
				contextConfig.setVersionCode(1);
				contextConfig.setInstallTime(time);
			}

			// 锟斤拷始锟斤拷锟斤拷锟斤拷锟侥硷拷
			XmlResourceParser parser = getResources().getXml(R.xml.m_config);
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG) {
					if (parser.getName().equals("cid")) {
						contextConfig.setCid(parser.getAttributeValue(null, "value"));
					}
				}
				eventType = parser.next();
			}

			// 锟斤拷锟斤拷锟斤拷id
			if (contextConfig.getCid() == null) {
				throw new IllegalStateException("cid == null");
			}

			// 锟教硷拷锟芥本
			String firmware = Build.VERSION.RELEASE;
			contextConfig.setFirmware(firmware);

			// sdkversion
			String sdkVersion = Build.VERSION.SDK;
			contextConfig.setSdkVersion(sdkVersion);

			// 锟斤拷锟斤拷欠锟斤拷锟斤拷远锟斤拷锟阶帮拷锟饺拷锟�
			int result = checkCallingOrSelfPermission("android.permission.INSTALL_PACKAGES");
			if (result == PackageManager.PERMISSION_GRANTED) {
				contextConfig.setPermiInstall(true);
			}

			// 锟街憋拷锟斤拷
			WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
			DisplayMetrics displayMetrics = new DisplayMetrics();
			Display display = windowManager.getDefaultDisplay();
			display.getMetrics(displayMetrics);
			// resolution
			int width = displayMetrics.widthPixels;
			int height = displayMetrics.heightPixels;
			contextConfig.setResolution(width + "x" + height);

			// density (1.5 没锟斤拷densityDpi锟斤拷锟斤拷侄锟�
			try {
				int density = displayMetrics.getClass().getField("densityDpi").getInt(displayMetrics);
				contextConfig.setDensity(String.valueOf(density));
			} catch (Exception e) {
				contextConfig.setDensity("160");
			}

			// 锟斤拷锟斤拷
			Locale locale = Locale.getDefault();
			contextConfig.setLanguage(locale.getLanguage());

			// ts -> MD5_KEY_ARRAY
			setTs(String.valueOf(new Random().nextInt(9)));

			baseDataOk = true;
		} catch (Exception e) {
			e.printStackTrace();
			baseDataOk = false;
		}
	}

	// 锟斤拷始锟斤拷锟叫筹拷锟斤拷锟斤拷
	private void initMarketConfig() {
		// 锟秸碉拷锟斤拷锟酵计�
		emptyAppIcon = getResources().getDrawable(R.drawable.app_empty_icon);

		// 锟秸碉拷锟斤拷锟斤拷锟酵�
		emptyScreenshot = getResources().getDrawable(R.drawable.screenshot_empty);

		// 锟秸碉拷锟斤拷锟斤拷锟酵�锟斤拷锟斤拷锟斤拷示)
		emptyScreenshotLoad = getResources().getDrawable(R.drawable.screenshot_empty_load);

		// 锟斤拷锟斤拷锟酵硷拷锟�
		emptyCategoryIcon = getResources().getDrawable(R.drawable.app_empty_icon);

		// 锟秸碉拷锟斤拷锟斤拷锟斤拷图片
		emptyADAppIcon = getResources().getDrawable(R.drawable.ad_empty_icon);
		// 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷
		marketConfig = new MarketConfig();
		SharedPreferences configPreferences = sharedPrefManager.getMarketConfigPref();
		boolean loadAppIcon = configPreferences.getBoolean(MarketConfig.LOAD_APP_ICON, true);
		marketConfig.setLoadAppIcon(loadAppIcon);

		// 锟皆讹拷锟斤拷锟截斤拷图
		boolean autoLoadShot = configPreferences.getBoolean(MarketConfig.LOAD_APP_SCREENSHOT, true);
		marketConfig.setLoadAppScreenshot(autoLoadShot);

		// 锟皆讹拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷
		boolean authCheckSoftwareUpdate = configPreferences.getBoolean(MarketConfig.AUTO_CHECK_SOFTWARE_UPDATE, false);
		marketConfig.setCheckSoftwareUpdate(authCheckSoftwareUpdate);

		// 锟斤拷锟剿匡拷锟�
		boolean backMoreFunc = configPreferences.getBoolean(MarketConfig.BACK_MORE_FUNC, false);
		marketConfig.setBackMoreFunc(backMoreFunc);

		// 锟斤拷锟斤拷APK扫锟斤拷路锟斤拷锟斤拷锟斤拷锟街讹拷锟斤拷锟斤拷
		String localApkDir = configPreferences.getString(MarketConfig.LOCAK_APK_DIR, Constants.DEFAULT_LOCAL_APK_DIR);
		marketConfig.setLocalApkDir(localApkDir);
		
		installed_new_state=sharedPrefManager.getInstallState();
		
	}

	// 锟斤拷楸撅拷鼗锟斤拷锟�
	public void checkLocalNetwork() {
		try {
			ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = conManager.getActiveNetworkInfo();
			NetworkinfoParser.parserNetinfo(networkInfo, this);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onLowMemory() {
		LogUtil.d(TAG, "!!!!!!!!!!! on low memory");
		handleMarketEmptyMessage(Constants.M_DELAY_GC);
		super.onLowMemory();
	}

	/**
	 * @return the mHandler
	 */
	public void registerSubHandler(MarkableHandler handler) {
		mHandler.registeHandler(handler);
	}

	/**
	 * 锟斤拷锟斤拷锟截诧拷锟斤拷锟斤拷锟狡筹拷锟斤拷时锟斤拷取锟斤拷锟皆硷拷锟斤拷注锟斤拷
	 */
	public void unregisterSubHandler(MarkableHandler handler) {
		mHandler.unregistHandler(handler);
	}

	public MarketServiceWraper getServiceWraper() {
		return serviceWraper;
	}

	/**
	 * @return the appCahceManager
	 */
	public AppCahceManager getAppCahceManager() {
		return appCahceManager;
	}

	/**
	 * @return the assertCacheManager
	 */
	public AssertCacheManager getAssertCacheManager() {
		return assertCacheManager;
	}

	/**
	 * @param assertCacheManager
	 *            the assertCacheManager to set
	 */
	public void setAssertCacheManager(AssertCacheManager assertCacheManager) {
		this.assertCacheManager = assertCacheManager;
	}

	/**
	 * @return the contextConfig
	 */
	public ContextConfig getContextConfig() {
		return contextConfig;
	}

	/**
	 * @param contextConfig
	 *            the contextConfig to set
	 */
	public void setContextConfig(ContextConfig contextConfig) {
		this.contextConfig = contextConfig;
	}

	public TaskMarkPool getTaskMarkPool() {
		return taskMarkPool;
	}

	/**
	 * @return the softwareManager
	 */
	public MarketManager getMarketManager() {
		return marketManager;
	}

	/**
	 * @return the sharedPrefManager
	 */
	public SharedPrefManager getSharedPrefManager() {
		return sharedPrefManager;
	}

	/**
	 * @return the synSoftware
	 */
	public boolean isSynSoftware() {
		return synSoftware;
	}

	/**
	 * @param synSoftware
	 *            the synSoftware to set
	 */
	public void setSynSoftware(boolean synSoftware) {
		this.synSoftware = synSoftware;
	}

	/**
	 * @return the ts
	 */
	public String getTs() {
		return ts;
	}

	/**
	 * @param ts
	 *            the ts to set
	 */
	public void setTs(String ts) {
		this.ts = ts;
	}

	/**
	 * TODO 锟结话锟斤拷锟节憋拷锟斤拷锟斤拷锟铰碉拷录
	 */
	public void handleSessionTimeOut(boolean launchLogin) {
		// 锟斤拷始锟斤拷
		sharedPrefManager.saveSession(null);
		// // 锟斤拷锟斤拷停止锟斤拷锟叫碉拷锟届步锟斤拷锟斤拷
		// serviceWraper.stopAllAsyncOperate();
		// // 锟截憋拷锟斤拷锟斤拷锟斤拷锟斤拷
		// serviceWraper.shutdownHttpConnect();

		// launch account for login
        if (launchLogin) {
            Intent intent = new Intent(this, AccountFrame.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra(Constants.LOGIN_INVOKE_STATE, Constants.SUB_LOGIN);
            startActivity(intent);
        }
	}

	/**
	 * 锟结话锟节憋拷锟斤拷锟角凤拷锟斤拷效锟剿ｏ拷锟斤拷一锟斤拷锟斤拷锟斤拷预锟斤拷锟斤拷证锟斤拷
	 * 
	 * @param 锟结话锟角凤拷锟斤拷效
	 */
	public boolean isSessionLocalValid() {
		String sessionId = sharedPrefManager.getSession();
		return (sessionId != null && sessionId.trim().length() > 0);
	}

	/**
	 * 锟剿筹拷锟叫筹拷
	 */
	public void initMarketContextForLogout() {
		sharedPrefManager.saveSession("");
		sharedPrefManager.initUserInfoForLogout();

		// 锟斤拷锟斤拷业墓锟斤拷锟斤拷斜锟�锟揭的癸拷锟斤拷锟叫憋拷锟斤拷锟捷的斤拷锟斤拷锟斤拷锟斤拷锟揭伙拷锟斤拷锟斤拷锟斤拷薹锟斤拷锟斤拷锟酵拷锟绞斤拷锟斤拷锟�
		ATaskMark taskMark = taskMarkPool.getAppFavorTaskMark();
		invalidAppItemCache(taskMark);

		// 通知
		Message message = Message.obtain();
		message.what = Constants.M_LOGIN_OUT;
		handleMarketMessage(message);
	}

	/**
	 * 锟斤拷锟斤拷坛锟绞憋拷锟斤拷锟斤拷时锟斤拷锟斤拷要锟斤拷始锟斤拷锟斤拷锟�br>
	 * 锟斤拷锟斤拷锟铰筹拷始锟斤拷时锟斤拷锟�锟斤拷锟斤拷锟斤拷锟斤拷诖锟斤拷谢锟斤拷锟斤拷app锟斤拷氐锟斤拷锟捷★拷
	 */
	public void checkForLongLive() {
		long currentTimestamp = System.currentTimeMillis();
		LogUtil.w(TAG, "check(init) fo long live lastUpdate: " + lastInit + "  currentTimestamp: " + currentTimestamp);
		if (currentTimestamp - lastInit >= marketConfig.getMaxContextReinit()) {
			LogUtil.w(TAG, "do init for long live");
			lastInit = currentTimestamp;
			// 锟斤拷始锟斤拷锟斤拷锟斤拷
			taskMarkPool.reinitForLongLive();
			// 锟斤拷始锟斤拷锟斤拷锟斤拷
			appCahceManager.reinitAppCache();
			// 锟斤拷锟酵计�
			assertCacheManager.clearCacheData(); // 锟节达拷
			localeCacheManager.clearAdvertiseCache(); // 锟斤拷锟斤拷
		}
	}

	/**
	 * 锟角碉拷某锟斤拷锟斤拷锟斤拷锟斤拷效
	 * 
	 * @param type
	 */
	public void invalidAppItemCache(ATaskMark type) {
		// 锟斤拷始锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟�
		taskMarkPool.reinitTaskMark(type);
		// 删锟斤拷锟斤拷锟絠d,锟斤拷锟斤拷锟斤拷实锟斤拷锟斤拷锟斤拷锟斤拷锟轿拷锟叫╋拷锟斤拷也锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟酵★拷
		appCahceManager.getAppIdList(type).clear();
	}

	/**
	 * @return the localeCacheManager
	 */
	public LocaleCacheManager getLocaleCacheManager() {
		return localeCacheManager;
	}

	/**
	 * @return the mdbHelper
	 */
	public MDBHelper getMdbHelper() {
		return mdbHelper;
	}

	/**
	 * 锟斤拷锟斤拷锟斤拷息
	 * 
	 * @param msg
	 */
	public void handleMarketMessage(Message msg) {
		if (mHandler != null) {
			mHandler.sendMessage(msg);
		}
	}

	public void handleMarketEmptyMessage(int what) {
		if (mHandler != null) {
			mHandler.sendEmptyMessage(what);
		}
	}

	public void handleMarketDelayMessage(Message msg, long delayMillis) {
		if (mHandler != null) {
			mHandler.sendMessageDelayed(msg, delayMillis);
		}
	}

	public void handleMarketEmptyDelayMessage(int what, long delayMillis) {
		if (mHandler != null) {
			mHandler.sendEmptyMessageDelayed(what, delayMillis);
		}
	}

	/**
	 * 锟斤拷锟截诧拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷愿锟斤拷锟�
	 * 
	 * @param baseApp
	 * @param messageMark
	 */
	public final void handleSimpleDownloadBaseItem(BaseApp baseApp, int messageMark) {
		Message message = Message.obtain();
		message.what = Constants.M_DOWNLOAD_ACCEPT;
		message.obj = baseApp;
		if (messageMark != -1) {
			message.arg1 = messageMark;
		}
		handleMarketMessage(message);
	}

	/**
	 * 锟斤拷锟截诧拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟�
	 * 
	 * @param baseApp
	 * @param messageMark
	 */
	public final void handleSimpleDownloadBaseItem(BaseApp baseApp) {
		handleSimpleDownloadBaseItem(baseApp, -1);
	}

	/**
	 * @return the baseDataOk
	 */
	public boolean isBaseDataOk() {
		return baseDataOk;
	}

	/**
	 * @return the marketConfig
	 */
	public MarketConfig getMarketConfig() {
		return marketConfig;
	}

	// 1. every MarketActivity has MMHanlde which extends MarkableHandle, and auto register to the Weaklist.
    // 2.
	class MarketBaseHandler extends Handler {
		// 锟斤拷锟斤拷锟斤拷锟接硷拷handler锟斤拷锟接硷拷锟斤拷锟斤拷(task锟饺诧拷activity锟饺达拷锟斤拷)锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟酵硷拷锟斤拷郑锟斤拷锟絟andle锟斤拷锟斤拷锟捷诧拷锟街碉拷统一锟斤拷锟�
		// 锟斤拷锟斤将锟斤拷为锟斤拷应锟斤拷锟斤拷锟斤拷,
		// TODO 要锟睫改ｏ拷实锟斤拷锟斤拷锟斤拷锟斤拷
		private List<MarkableHandler> weakList = new ArrayList<MarkableHandler>();

		public MarketBaseHandler(Looper looper) {
			super(looper);
		}

		void registeHandler(MarkableHandler handler) {
			weakList.add(0, handler);
			LogUtil.d(TAG, "registeHandler now size: " + weakList.size());
		}

		void unregistHandler(MarkableHandler handler) {
			weakList.remove(handler);
			LogUtil.d(TAG, "unregistHandler now " + "size: " + weakList.size());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.Handler#handleMessage(android.os.Message)
		 */
		@Override
		public void handleMessage(Message msg) {
			LogUtil.d(TAG, "handleMessage: " + msg);
			try {
				switch (msg.what) {
				// 卸锟斤拷锟斤拷锟�
				case Constants.M_UNINSTALL_APK:
					handleAppUNInstalled(msg);
					break;

				// 锟斤拷装锟斤拷锟�
				case Constants.M_INSTALL_APK:
					handleAppInstalled(msg);
					break;

				// 锟斤拷锟斤拷锟斤拷锟�
				case Constants.M_DOWNLOAD_COMPLETED:
					handleDownloadAppComplete(msg);
					break;

				case Constants.M_DOWNLOAD_FAIL:
					handleAppDownloadStop(msg, true);
					break;

				// 锟斤拷锟斤拷失锟斤拷
				case Constants.M_DOWNLOAD_STOP:
					handleAppDownloadStop(msg, false);
					break;

				// 锟斤拷锟截斤拷锟�
				case Constants.M_DOWNLOAD_PROGRESS: {
					handleAppDownloadProgress(msg);
					break;
				}
				// 锟斤拷锟斤拷取锟斤拷
				case Constants.M_DOWNLOAD_CANCEL: {
					handleDownloadCancel((DownloadItem) msg.obj);
					break;
				}
				// 锟斤拷锟斤拷锟斤拷锟斤拷
				case Constants.M_DOWNLOAD_RETRY:
					handleRetryDownloadTask((DownloadItem) msg.obj);
					break;

				// 锟斤拷锟斤拷锟斤拷要锟斤拷权锟睫匡拷始锟斤拷锟斤拷
				// 目前同时锟斤拷应锟斤拷锟斤拷锟斤拷锟�
				case Constants.M_QUICK_DOWNLOAD_APP:
				case Constants.M_QUICK_PAYMENT_APP:
				case Constants.M_DOWNLOAD_ACCEPT:
					handleDownloadOrPurchase(msg);
					break;

				case Constants.M_BATCH_DOWNLOAD_APP:
				case Constants.M_BATCH_UPDATE_APP:
					handleBatchDownload(msg);
					break;
				case Constants.M_BATCH_UPDATE_APP_LOCAL:
					handleBatchInstallUpdate(msg);
					break;

				// 锟斤拷锟斤拷詹锟�
				case Constants.M_FAVOR_ADDED:
					handleAddFavorItem(msg);
					break;

				// 删锟斤拷锟秸诧拷
				case Constants.M_FAVOR_DELETE:
					handleDeleteFavorItem((AppItem) msg.obj);
					break;

				case Constants.M_NOTIFY_APP_DOWNLOAD_LIST:
					handleChooseDownloadListNotify();
					break;

				case Constants.M_NOTIFY_APP_DOWNLOADED:
					handleChooseDownloadedNotify(msg);
					break;

				case Constants.M_NOTIFY_SOFT_UPDATE:
					handleChooseSoftUpdateNotify();
					break;

				case Constants.M_NOTIFY_MARKET_UPDATE:
					handleChooseMarketUpdateNotify();
					break;

				case Constants.M_DELAY_GC:
					delayGC(msg);
					break;

				case Constants.M_PARSE_NETWORKINFO:
					doParseNetworkInfo(msg);
					break;

				case Constants.M_CHECK_MS_UPDATE:
					doCheckUpdate(msg);
					break;

				case Constants.M_REPORT_CHANNEL:
					reportChannel(msg);
					break;

				case Constants.M_CHECK_SATIC_AD: // 锟斤拷态锟斤拷锟斤拷锟�
					doCheckStaticAD();
					break;

				case Constants.M_NOTIFY_STATIC_AD: // 锟斤拷态锟斤拷娴拷锟斤拷锟斤拷锟�
					handleChooseStaticADNotify();
					break;
				case Constants.M_DOWNLOAD_AFTER_PAY_SUCCESS:
					 doDownloadRequest(getApp());
					 clearApp();
					break;
				case Constants.M_DOWNLOAD_AFTER_PAY_FAIL:
					 clearApp();
					break;
					
				}

                Log.d(TAG, "handler.weakList.size="+weakList.size());
				for (MarkableHandler handler : weakList) {
					//
                    Log.d(TAG, "handler.isHall="+handler.isHandleAll()+",msg.arg1="+msg.arg1+",arg2="+msg.arg2);
					if (handler.isHandleAll() || msg.arg1 == 0 || handler.getMssageMark() == msg.arg1) {
						// 锟斤拷息锟角凤拷锟斤拷止
						if (msg.arg2 != Constants.M_MESSAGE_END) {
							handler.handleMessage(msg); // 注锟斤拷锟斤拷锟斤拷锟斤拷同锟斤拷锟斤拷锟叫ｏ拷锟斤拷使锟斤拷sendMessage();
						}
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// 锟斤拷锟斤拷锟斤拷锟斤拷
		@SuppressWarnings("unchecked")
		private void handleBatchDownload(Message msg) {// 锟斤拷锟饺诧拷锟斤拷sd锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷
			boolean ok = getMarketManager().checkSDCardStateAndNote();
			if (!ok) {
				// 锟斤拷止锟斤拷锟斤拷锟较�
				msg.arg2 = Constants.M_MESSAGE_END;
				return;
			}

			// 锟斤拷锟斤拷锟斤拷些锟斤拷锟斤拷要锟斤拷锟截碉拷锟斤拷锟�
			List<BaseApp> appList = (List<BaseApp>) msg.obj;
			int needCount = 0;
			DownloadItem downloadItem = null;
			for (BaseApp baseApp : appList) {
				if (marketManager.isNeedDownloadApk(baseApp) && (baseApp.isFree() || baseApp.isPurchase())) {
					needCount++;
					// 锟斤拷锟斤拷锟斤拷锟揭伙拷锟斤拷锟绞э拷艿锟斤拷锟斤拷锟斤拷锟矫粗达拷锟斤拷锟斤拷锟�
					downloadItem = marketManager.getDownloadItem(BaseApp.APP_DOWNLOAD_STOP, baseApp.getId());
					if (downloadItem != null) {
						handleRetryDownloadTask(downloadItem);

						// 锟斤拷锟斤拷锟斤拷锟窖碉拷直锟接匡拷始锟斤拷锟斤拷
					} else {
						doDownloadRequest(baseApp);
					}
				}
			}

			// 锟斤拷止锟斤拷锟斤拷锟较�
			msg.arg2 = Constants.M_MESSAGE_END;

			// 锟截讹拷锟斤拷为锟斤拷锟斤拷锟截伙拷锟斤拷锟斤拷锟斤拷赂愣�
			Message message = Message.obtain();
			message.arg1 = msg.arg1;
			int showNoteId = 0;
			if (msg.what == Constants.M_BATCH_UPDATE_APP) {
				message.what = Constants.M_BATCH_UPDATE_APP_OK;
				handleMarketMessage(message);
				showNoteId = R.string.add_update_task_suffix;

			} else if (msg.what == Constants.M_BATCH_DOWNLOAD_APP) {
				message.what = Constants.M_BATCH_DOWNLOAD_APP_OK;
				handleMarketMessage(message);
				showNoteId = R.string.add_download_task_suffix;
			}

			// 锟斤拷示
			if (needCount > 0) {
				Toast.makeText(MApplication.this, needCount + getString(showNoteId), 500).show();

			} else {
				Toast.makeText(MApplication.this, getString(R.string.no_need_download_apk_check_local), 500).show();
			}
		}
		// checkPaySDKExist
		public boolean checkPaySDKExist(){
			boolean flag = false;
			List<PackageInfo> packageInfos=getPackageManager().
			getInstalledPackages(PackageManager.
			GET_UNINSTALLED_PACKAGES);
			for(PackageInfo packageInfo:packageInfos) { 
		if("com.ehoo.paysdk".equals(
		packageInfo.packageName)) {
					flag = true;
					break;
				}
			}
			return flag;
		}

		// 锟斤拷锟截伙拷锟竭癸拷锟斤拷锟酵骋伙拷涌锟�
		private void handleDownloadOrPurchase(Message msg) {
			// 锟斤拷锟饺诧拷锟斤拷sd锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷
			boolean ok = getMarketManager().checkSDCardStateAndNote();
			boolean is_paysdk=checkPaySDKExist();
			if (!ok) {
				// 锟斤拷止锟斤拷锟斤拷锟较�
				msg.arg2 = Constants.M_MESSAGE_END;
				return;
			}

			BaseApp baseApp = (BaseApp) msg.obj;
			// 锟斤拷锟斤拷欠锟斤拷丫锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷斜锟斤拷锟�
			BaseApp checkItem = marketManager.getDownloadItem(BaseApp.APP_DOWNLOADED, baseApp.getId());
			if (checkItem != null) {
				// 锟斤拷止锟斤拷锟斤拷锟较�
				msg.arg2 = Constants.M_MESSAGE_END;
				Toast.makeText(MApplication.this, getString(R.string.apk_file_exit_check_tasklist), 100).show();
				return;
			}

			checkItem = marketManager.getDownloadItem(BaseApp.APP_DOWNLOADING, baseApp.getId());
			if (checkItem != null) {
				// 锟斤拷止锟斤拷锟斤拷锟较�
				msg.arg2 = Constants.M_MESSAGE_END;
				Toast.makeText(MApplication.this, getString(R.string.download_task_exist_check_tasklist), 100).show();
				return;
			}

			// 锟斤拷锟斤拷欠锟斤拷锟斤拷锟节憋拷锟斤拷apk锟叫憋拷
			checkItem = marketManager.getLocalApk(baseApp.getPackageName(), baseApp.getVersionCode());
			if (checkItem != null) {
				// 锟斤拷止锟斤拷锟斤拷锟较�
				msg.arg2 = Constants.M_MESSAGE_END;
				Toast.makeText(MApplication.this, getString(R.string.apk_file_exist_check_localapklist), 100).show();
				return;
			}

			// 锟斤拷锟斤拷锟斤拷锟揭伙拷锟斤拷锟绞э拷艿锟斤拷锟斤拷锟斤拷锟矫粗达拷锟斤拷锟斤拷锟�
			DownloadItem downloadItem = marketManager.getDownloadItem(BaseApp.APP_DOWNLOAD_STOP, baseApp.getId());
			if (downloadItem != null) {
				handleRetryDownloadTask(downloadItem);

				// 锟斤拷锟斤拷锟斤拷锟窖碉拷直锟接匡拷始锟斤拷锟斤拷, 锟斤拷锟斤拷锟斤拷敫讹拷锟斤拷锟斤拷锟�
			} else if (baseApp.isFree()) {
				doDownloadRequest(baseApp);

			} else if(baseApp.isPurchase()){
				// 锟斤拷锟斤拷支锟斤拷锟斤拷锟斤拷 使锟斤拷支锟斤拷锟斤拷锟絧aysdk
				if(is_paysdk&&mact!=null)
				{
					setApp(baseApp);
					mact.HandlePurchase();
				}
			}
		}

		public void setApp(BaseApp tmpapp)
		{
			mApp=tmpapp;
		}
		public void clearApp()
		{
			mApp=null;
		}
		public BaseApp getApp()
		{
			return mApp;
		}

		// 锟斤拷锟斤拷锟斤拷锟叫讹拷爻晒锟�
		private void handleAppUNInstalled(Message msg) {
			String pname = (String) msg.obj;
			BaseApp baseApp = marketManager.deleteSoftware(pname);
			if (baseApp != null) {
				// 锟斤拷锟铰憋拷锟斤拷apk锟斤拷状态
				baseApp = marketManager.getLocalApk(baseApp.getPackageName(), baseApp.getVersionCode());
				if (baseApp != null) {
					baseApp.setState(BaseApp.APP_DOWNLOADED);
				}
			}

			// 删锟斤拷锟斤拷艿母锟斤拷锟斤拷锟较拷锟斤拷锟斤拷锟斤拷锟较�
			SoftwareUpdateTaskMark updateTaskMark = taskMarkPool.getSoftwareUpdateTaskMark(false);
			AppItem appItem = appCahceManager.getAppItemByMarkPName(updateTaskMark, pname);
			if (appItem != null) {
				appCahceManager.deleteAppItemIndexFromCache(updateTaskMark, appItem);
			}

			LogUtil.d(TAG, "handleAppUNInstalled pname = " + pname);
		}

		// 锟斤拷锟斤拷锟斤拷锟斤拷锟阶帮拷晒锟�
		private void handleAppInstalled(Message msg) {
			// 锟斤拷卓锟叫筹拷锟斤拷锟斤拷锟�锟斤拷锟斤拷task锟斤拷锟揭伙拷锟斤拷前锟阶匡拷谐锟斤拷锟斤拷锟侥★拷3
			String pname = (String) msg.obj;
			PackageManager pm = getPackageManager();
			try {
				PackageInfo packageInfo = pm.getPackageInfo(pname, 0);
				DownloadItem downloadItem = marketManager.deleteDownloadTask(pname, packageInfo.versionCode);
				int softId = 0;
				// 锟狡筹拷卓锟叫筹拷锟斤拷锟斤拷锟斤拷锟斤拷锟侥硷拷
				if (downloadItem != null) {
					softId = downloadItem.getAppId();
					marketManager.deleteHiApkFile(downloadItem.getSavePath());
					// 删锟斤拷锟斤拷菘锟斤拷录
					SQLiteDatabase db = mdbHelper.getWritableDatabase();
					db.delete(TAppDownload.TABLE_NAME, TAppDownload.APP_ID + "='" + downloadItem.getAppId() + "'", null);
					mdbHelper.close();

				} else {
					softId = (pname + packageInfo.versionName).hashCode();
				}

				// 锟斤拷拥锟斤拷丫锟斤拷锟阶帮拷锟斤拷斜锟�
				Software software = Util.resolvePackageInfo(TAG, MApplication.this, packageInfo, pm);
				// 锟斤拷要锟矫伙拷锟斤拷锟斤拷图锟斤拷
				software.setId(softId);
				software.setState(BaseApp.APP_INSTALLED);
				marketManager.addSoftware(software);

				// 删锟斤拷锟绞憋拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷息
				SoftwareUpdateTaskMark updateTaskMark = taskMarkPool.getSoftwareUpdateTaskMark(false);
				AppItem appItem = appCahceManager.getAppItemByMarkPName(updateTaskMark, pname);
				if (appItem != null) {
					appCahceManager.deleteAppItemIndexFromCache(updateTaskMark, appItem);
				}

				// 锟斤拷锟斤拷MarketManager锟斤拷锟斤拷锟叫讹拷锟斤拷锟截成癸拷锟叫憋拷锟角诧拷锟斤拷为锟斤拷0锟斤拷锟斤拷锟斤拷蔷锟斤拷锟斤拷锟斤拷锟接︼拷锟斤拷锟较拷锟�
				if (marketManager.getDownloadTaskCount(BaseApp.APP_DOWNLOADED) <= 0
						&& marketManager.getDownloadTaskCount(BaseApp.APP_DOWNLOAD_STOP) <= 0
						&& marketManager.getDownloadTaskCount(BaseApp.APP_DOWNLOADING) <= 0) {
					MarketNotify.clearNofity(MApplication.this, R.string.market_download_task);
				}

				// 锟斤拷锟酵ㄖ拷锟�
				MarketNotify.clearNofity(MApplication.this, (pname + packageInfo.versionCode).hashCode());

				LogUtil.d(TAG, "handleAppInstalled pname= " + pname + " version: " + packageInfo.versionName);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// 锟斤拷锟斤拷锟斤拷锟截斤拷锟斤拷
		private void handleDownloadAppComplete(Message msg) {
			DownloadItem dTask = (DownloadItem) msg.obj;
			// 锟斤拷锟杰憋拷锟劫斤拷时删锟斤拷
			if (dTask == null) {
				msg.arg2 = Constants.M_MESSAGE_END;
				return;
			}

			dTask.setState(BaseApp.APP_DOWNLOADED);
			// 锟斤拷锟斤拷锟斤拷站锟较达拷锟斤拷锟斤拷锟斤拷实锟斤拷锟斤拷莶锟揭伙拷锟斤拷锟斤拷锟�
			dTask.setdSize(dTask.getSize());
			marketManager.distributeDownloadItemToTaskList(dTask);

			// 锟斤拷锟斤拷锟斤拷菘锟阶刺�
			// 锟斤拷锟斤拷状态
			SQLiteDatabase db = mdbHelper.getWritableDatabase();
			ContentValues cv = new ContentValues();
			cv.put(TAppDownload.STATE, BaseApp.APP_DOWNLOADED);
			cv.put(TAppDownload.DSIZE, dTask.getSize());
			db.update(TAppDownload.TABLE_NAME, cv, TAppDownload.APP_ID + "='" + dTask.getAppId() + "'", null);
			mdbHelper.close();

			// 锟斤拷锟斤拷默锟较憋拷锟�
			msg.arg1 = (dTask.getPackageName() + dTask.getVersionCode()).hashCode();

			Toast.makeText(MApplication.this, dTask.getName() + " " + getString(R.string.download_task_complete), 100)
					.show();

			// 直锟斤拷锟斤拷锟斤拷锟阶耙筹拷锟�
			String apkPath = marketManager.getJointApkSavePath(dTask.getPackageName(), dTask.getVersionCode());
			if (apkPath != null) {
				marketManager.installSoftware(apkPath);
			}
			if(dTask.getPackageName().equalsIgnoreCase("com.kapps.market")){
				String sign = SecurityUtil.md5Encode(getTs() + Constants.SEC_KEY_STRING);
				CommitDownloadTaskMark commitTaskMark = taskMarkPool.getCommitDownloadTaskMark();
				serviceWraper.commitMarketDownloadRecord(MApplication.this, commitTaskMark,dTask.getAppId(),sign,getContextConfig().getDeviceId());
			}else{
			doCommitDownloadRecord(dTask.getAppId());
			}
			LogUtil.d(TAG, "down commplete dTask = " + dTask);
			
		}

		/**
		 * 
		 * @param id
		 */
		private void doCommitDownloadRecord(int aid) {
			addIdToCommitFile(aid);
			commitDownloadRecordByFile();
//			String id=getCommitfile();
//			String sign = SecurityUtil.md5Encode(getTs() + Constants.SEC_KEY_STRING);
//			CommitDownloadTaskMark commitTaskMark = taskMarkPool.getCommitDownloadTaskMark();
//			serviceWraper.commitDownloadRecord(MApplication.this, commitTaskMark,id,sign,getContextConfig().getDeviceId(),getContextConfig().getCid());
		}
		/**
		 * 
		 */
		private void commitDownloadRecordByFile()
		{
			String id=getCommitfile();
			if(id==null)
				return ;
			String sign = SecurityUtil.md5Encode(getTs() + Constants.SEC_KEY_STRING);
			CommitDownloadTaskMark commitTaskMark = taskMarkPool.getCommitDownloadTaskMark();
			serviceWraper.commitDownloadRecord(MApplication.this, commitTaskMark,id,sign,getContextConfig().getDeviceId(),getContextConfig().getCid());

		}
		// 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟绞э拷锟�
		private void handleAppDownloadStop(Message msg, boolean fail) {
			DownloadItem stopedTask = null;
			if (msg.obj instanceof DownloadItem) {
				stopedTask = (DownloadItem) msg.obj;

			} else {
				Intent intent = (Intent) msg.obj;
				int appId = intent.getIntExtra(Constants.APP_ID, -1);
				stopedTask = marketManager.getDownloadItem(BaseApp.APP_DOWNLOADING, appId);
			}

			// 失锟杰讹拷应锟斤拷锟斤拷锟�
			if (stopedTask != null) {
				stopedTask.setState(BaseApp.APP_DOWNLOAD_STOP);
				marketManager.distributeDownloadItemToTaskList(stopedTask);

				// 锟斤拷锟斤拷状态
				SQLiteDatabase db = mdbHelper.getWritableDatabase();
				ContentValues cv = new ContentValues();
				cv.put(TAppDownload.STATE, BaseApp.APP_DOWNLOAD_STOP);
				db.update(TAppDownload.TABLE_NAME, cv, TAppDownload.APP_ID + "='" + stopedTask.getAppId() + "'", null);
				mdbHelper.close();

				// 锟斤拷锟斤拷默锟较憋拷锟�
				msg.arg1 = (stopedTask.getPackageName() + stopedTask.getVersionCode()).hashCode();

				if (fail) {
					Toast.makeText(MApplication.this, stopedTask.getName() + getString(R.string.download_task_fail),
							100).show();
				} else {
					Toast.makeText(MApplication.this, stopedTask.getName() + getString(R.string.download_task_stop),
							100).show();
				}
			}

			LogUtil.d(TAG, "down failure: " + stopedTask);
		}

		// 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷亟锟斤拷
		private void handleAppDownloadProgress(Message msg) {
			// 锟斤拷锟斤拷锟角可憋拷取锟斤拷锟�
			DownloadItem item = (DownloadItem) msg.obj;
			if (item != null) {
				// 锟斤拷锟斤拷锟斤拷菘锟斤拷锟斤拷乇锟斤拷锟斤拷锟较�
				// 锟斤拷锟斤拷锟铰硷拷锟斤拷锟较⒅伙拷锟揭伙拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟酵硷拷锟皆拷锟�
				// 时锟斤拷锟斤拷频亩系锟斤拷锟斤拷锟揭拷锟斤拷实锟绞碉拷apk锟侥硷拷锟斤拷小锟斤拷锟叫达拷锟�
				SQLiteDatabase db = mdbHelper.getWritableDatabase();
				ContentValues cv = new ContentValues();
				cv.put(TAppDownload.DSIZE, item.getdSize());
				db.update(TAppDownload.TABLE_NAME, cv, TAppDownload.APP_ID + "='" + item.getAppId() + "'", null);
				mdbHelper.close();

				// 锟斤拷锟斤拷默锟较憋拷锟�
				msg.arg1 = (item.getPackageName() + item.getVersionCode()).hashCode();

				// 锟斤拷通知
				MarketNotify.notifyAppDownloadProgress(MApplication.this, item.getName(), item);

			}
		}

		// 锟斤拷锟斤拷失锟杰碉拷锟斤拷锟斤拷锟斤拷锟斤拷
		private void handleRetryDownloadTask(DownloadItem downloadItem) {
			// 锟斤拷锟睫革拷状态 ,注锟斤拷锟斤拷锟斤拷要支锟街断碉拷锟斤拷锟斤拷锟叫诧拷锟斤拷删锟斤拷锟斤拷募锟�
			marketManager.deleteDownloadTask(downloadItem.getAppId());

			// 删锟斤拷锟斤拷菘锟斤拷录
			SQLiteDatabase db = mdbHelper.getWritableDatabase();
			db.delete(TAppDownload.TABLE_NAME, TAppDownload.APP_ID + "='" + downloadItem.getAppId() + "'", null);
			mdbHelper.close();

			// 锟斤拷锟斤拷锟�
			downloadItem.setState(BaseApp.APP_DOWNLOADING);
			doDownloadRequest(downloadItem);
		}

		// 锟斤拷锟斤拷取锟斤拷锟斤拷锟斤拷
		private void handleDownloadCancel(DownloadItem downloadItem) {
			// 删锟斤拷锟斤拷锟斤拷
			marketManager.deleteDownloadTask(downloadItem.getAppId());
			// 锟狡筹拷卓锟叫筹拷锟斤拷锟斤拷锟斤拷锟斤拷锟侥硷拷
			marketManager.deleteHiApkFile(downloadItem.getSavePath());

			// 删锟斤拷锟斤拷菘锟斤拷录
			SQLiteDatabase db = mdbHelper.getWritableDatabase();
			db.delete(TAppDownload.TABLE_NAME, TAppDownload.APP_ID + "='" + downloadItem.getAppId() + "'", null);
			mdbHelper.close();
		}

		// 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷
		private void doDownloadRequest(BaseApp baseApp) {
			String saveDir = marketManager.getSoftwareSaveDir();
			String savePath = marketManager.getSoftwareSavePath(saveDir, baseApp.getId());
			// 锟斤拷锟斤拷一锟斤拷锟铰碉拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟�
			DownloadItem downloadItem = new DownloadItem();
			downloadItem.setAppId(baseApp.getId());
			downloadItem.setState(BaseApp.APP_DOWNLOADING);
			downloadItem.setVersion(baseApp.getVersion());
			downloadItem.setVersionCode(baseApp.getVersionCode());
			downloadItem.setSize(baseApp.getSize());
			downloadItem.setPackageName(baseApp.getPackageName());
			downloadItem.setSavePath(savePath);
			downloadItem.setName(baseApp.getName());
			downloadItem.setApkPath(baseApp.getApkPath());
			downloadItem.setIconUrl(baseApp.getIconUrl());

			// 实锟绞碉拷锟斤拷锟截达拷锟斤拷
			doDownloadRequest(downloadItem);

		}

		// 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷
		private void doDownloadRequest(DownloadItem downloadItem) {
			// 锟斤拷锟斤拷锟斤拷菘锟�
			ContentValues cv = new ContentValues();
			cv.put(TAppDownload.APP_ID, downloadItem.getAppId());
			cv.put(TAppDownload.PNAME, downloadItem.getPackageName());
			cv.put(TAppDownload.NAME, downloadItem.getName());
			cv.put(TAppDownload.VERSION, downloadItem.getVersion());
			cv.put(TAppDownload.VERSION_CODE, downloadItem.getVersionCode());
			// base 64 锟斤拷锟斤拷
			cv.put(TAppDownload.DURL,
					SecurityUtil.encodeBase64(downloadItem.getApkPath() == null ? "" : downloadItem.getApkPath()));
			cv.put(TAppDownload.IURL,
					SecurityUtil.encodeBase64(downloadItem.getIconUrl() == null ? "" : downloadItem.getIconUrl()));
			cv.put(TAppDownload.STATE, downloadItem.getState());
			cv.put(TAppDownload.SIZE, downloadItem.getSize());
			SQLiteDatabase db = mdbHelper.getWritableDatabase();
			int rowId = (int) db.insert(TAppDownload.TABLE_NAME, null, cv);
			downloadItem.setId(rowId);
			mdbHelper.close();

			// 锟斤拷拥锟斤拷锟斤拷实亩锟斤拷锟�
			marketManager.distributeDownloadItemToTaskList(downloadItem);

			// 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷息
			Intent sendIntent = new Intent(Constants.ACTION_SERVICE_DOWNLOAD_REQUEST);
			sendIntent.putExtra(Constants.DOWNLOAD_ITEM_ID, downloadItem.getId());
			sendIntent.putExtra(Constants.SEC_SIGN,
					SecurityUtil.md5Encode(MApplication.this.getTs() + Constants.SEC_KEY_STRING));
			sendIntent.putExtra(Constants.H_SER_TS, getTs());
			startService(sendIntent);

			Toast.makeText(
					MApplication.this,
					String.format(getString(R.string.download_task_begin), downloadItem.getName(),
							downloadItem.getVersion()), 100).show();
		}

		// 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷诒锟斤拷锟绞憋拷锟斤拷锟阶帮拷锟斤拷馗锟斤拷锟斤拷锟斤拷锟斤拷
		private void handleBatchInstallUpdate(Message msg) {
			Toast.makeText(MApplication.this, getString(R.string.install_update_software), 100).show();
		}

		// 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷通知锟斤拷锟斤拷示
		private void handleChooseDownloadListNotify() {
			Intent intent = new Intent(MApplication.this, MDownloadFrame.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			intent.putExtra(Constants.QUICK_VIEW, true);

			startActivity(intent);
		}

		// 锟斤拷锟斤拷丫锟斤拷锟斤拷氐锟斤拷锟斤拷
		public void handleChooseDownloadedNotify(Message msg) {
			Intent intent = (Intent) msg.obj;
			String pname = intent.getStringExtra(Constants.APP_PACKAGE_NAME);
			String name = intent.getStringExtra(Constants.APP_NAME);
			String apkPath = intent.getStringExtra(Constants.APP_PATH);
			marketManager.installSoftware(apkPath);
		}

		// 锟斤拷锟斤拷锟叫筹拷锟斤拷锟斤拷
		private void handleChooseMarketUpdateNotify() {
			Intent intent = new Intent(MApplication.this, MUpdateFrame.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			intent.putExtra(Constants.QUICK_VIEW, true);

			startActivity(intent);
		}

		// 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷碌锟斤拷锟绞�
		private void handleChooseSoftUpdateNotify() {
			// 删锟斤拷锟斤拷锟斤拷锟�
			localeCacheManager.deleteCacheData(CacheConstants.SOFTWARE_UPDATE_INFO);

			SoftwareUpdateTaskMark taskMark = taskMarkPool.getSoftwareUpdateTaskMark(false);
			// 锟斤拷锟斤拷锟揭伙拷锟斤拷锟街憋拷锟斤拷锟斤拷锟较革拷锟斤拷锟�
			List<AppItem> appItemList = appCahceManager.getAppItemList(taskMark);
			if (appItemList.size() == 1) {
				AppItem appItem = appItemList.get(0);
				Intent intent = new Intent(MApplication.this, AppDetailFrame.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra(Constants.APP_ID, appItem.getId());
				intent.putExtra(Constants.QUICK_VIEW, true);
				startActivity(intent);

			} else {
				Intent intent = new Intent(MApplication.this, UpdateableFrame.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				intent.putExtra(Constants.QUICK_VIEW, true);
				startActivity(intent);
			}
		}

		// 锟斤拷时gc
		private void delayGC(Message msg) {
			msg.arg2 = Constants.M_MESSAGE_END;
			if (assertCacheManager != null) {
				assertCacheManager.clearCacheData();
			}
			System.gc();
		}

		// 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷息
		private void doParseNetworkInfo(Message msg) {
			msg.arg2 = Constants.M_MESSAGE_END;
			NetworkInfo networkInfo = (NetworkInfo) msg.obj;
			NetworkinfoParser.parserNetinfo(networkInfo, MApplication.this);
		}

		// 锟斤拷锟斤拷锟斤拷
		private void doCheckUpdate(Message msg) {
			msg.arg2 = Constants.M_MESSAGE_END;
			checkMarketUpdate(false);
			checkSoftwareUpdate(false);
		}

		// 锟斤拷榫蔡拷锟斤拷
		private void doCheckStaticAD() {
			StaticADTaskMark staticADTaskMark = taskMarkPool.getStaticADTaskMark();
			long oldAdId = sharedPrefManager.getMarketConfigPref().getLong(MarketConfig.STATIC_AD_ID, -1);
			serviceWraper.checkStaticAD(MApplication.this, staticADTaskMark, oldAdId);

		}

		// 锟斤拷示锟斤拷态锟斤拷锟�
		// 锟斤拷示一锟轿硷拷锟斤拷效
		private void handleChooseStaticADNotify() {
			Intent intent = new Intent();
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			intent.setClass(MApplication.this, StaticADFrame.class);
			startActivity(intent);

		}

		// 锟斤拷锟斤拷锟斤拷锟较诧拷锟�
		private void handleAddFavorItem(Message msg) {
			// 锟斤拷锟斤拷锟斤拷证session
			if (!isSessionLocalValid()) {
				msg.arg1 = Constants.M_MESSAGE_END;
				handleSessionTimeOut(true);
				return;
			}

			AppItem appItem = (AppItem) msg.obj;
			// 锟角凤拷锟窖撅拷锟斤拷锟节存缓锟斤拷锟斤拷锟斤拷
			AppFavorTaskMark favorMark = taskMarkPool.getAppFavorTaskMark();
			boolean exist = appCahceManager.isAppItemInCache(favorMark, appItem);
			if (exist) {
				// 全锟斤拷通知
				Toast.makeText(MApplication.this, appItem.getName() + " " + getString(R.string.favoliten_exist), 100)
						.show();
				return;
			}

			// 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷
			AddFavorTaskMark addTask = taskMarkPool.createAddFavorTaskMark(appItem.getId());
			serviceWraper.addFavorAppItem(null, addTask, appItem, appItem.getId());

			// 锟斤拷拥锟斤拷锟斤拷锟�
			appCahceManager.addAppItemToCache(favorMark, appItem);

			// 全锟斤拷通知
			Toast.makeText(MApplication.this, getString(R.string.favoliten_added) + " " + appItem.getName(), 100)
					.show();

			LogUtil.d(TAG, "task : add appItem = " + appItem);
		}

		// 锟斤拷锟斤拷删锟斤拷锟斤拷锟�
		private void handleDeleteFavorItem(AppItem appItem) {
			// 锟斤拷锟斤拷锟斤拷锟斤拷锟缴撅拷锟�
			DeleteFavorTaskMark deleteMark = taskMarkPool.createDeleteFavorTaskMark(appItem.getId());
			serviceWraper.deleteFavorAppItem(null, deleteMark, appItem, appItem.getId());

			// 删锟斤拷锟秸诧拷
			AppFavorTaskMark taskMark = taskMarkPool.getAppFavorTaskMark();
			appCahceManager.deleteAppItemIndexFromCache(taskMark, appItem);

		}

		// 锟斤拷锟斤拷锟斤拷锟斤拷
		private void reportChannel(Message msg) {
			msg.arg2 = Constants.M_MESSAGE_END;
			ChannelReportTaskMark crTaskMark = taskMarkPool.createChannelReportTaskMark();
			serviceWraper.reportChannel(null, crTaskMark, contextConfig.getDeviceId(), contextConfig.getCid());
		}

	}

	/**
	 * 锟斤拷示锟斤拷锟斤拷锟斤拷锟较革拷锟斤拷锟斤拷锟�
	 * 
	 * @param appId
	 */
	public void handleShowAppDetail(int appId) {
		Intent intent = new Intent(MApplication.this, AppDetailFrame.class);
		AppItem appItem = appCahceManager.getAppItemById(appId);
		if (appItem != null) {
			intent.putExtra(Constants.APP_ID, appItem.getId());

		} else {
			intent.putExtra(Constants.DETAIL_DOWNLOADABLE_APP, true);
			intent.putExtra(Constants.APP_ID, appId);
		}
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	/**
	 * 锟斤拷示锟斤拷锟斤拷锟斤拷锟较革拷锟斤拷锟斤拷锟�
	 * 
	 * @param pname
	 * @param versionCode
	 */
	public void handleShowAppDetail(String pname, int versionCode) {
		Intent intent = new Intent(this, AppDetailFrame.class);
		AppItem appItem = appCahceManager.getAppItemByPackageVersion(pname, versionCode);
		if (appItem != null) {
			intent.putExtra(Constants.APP_ID, appItem.getId());

		} else {
			intent.putExtra(Constants.DETAIL_LOCALSOFTWARE_APP, true);
			intent.putExtra(Constants.APP_PACKAGE_NAME, pname);
			intent.putExtra(Constants.APP_VERSION_CODE, versionCode);
		}
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	// 停止锟斤拷锟斤拷
	public void handleStopTask(DownloadItem downloadItem) {
		Intent intent = new Intent(Constants.ACTION_SERVICE_DOWNLOAD_STOP);
		intent.putExtra(Constants.APP_ID, downloadItem.getAppId());
		startService(intent);

		Message message = Message.obtain();
		message.what = Constants.M_DOWNLOAD_STOP;
		message.obj = downloadItem;
		handleMarketMessage(message);
	}

	// 取锟斤拷锟斤拷锟斤拷
	public void handleCancelTask(DownloadItem downloadItem) {
		// 取锟斤拷锟接︼拷锟斤拷锟斤拷胤锟斤拷锟�
		Intent intent = new Intent(Constants.ACTION_SERVICE_DOWNLOAD_CANCEL);
		intent.putExtra(Constants.APP_ID, downloadItem.getAppId());
		startService(intent);

		handleDeleteDownloadItem(downloadItem);
	}

	// 删锟斤拷一锟斤拷锟窖撅拷通锟斤拷锟叫筹拷锟斤拷锟截碉拷apk
	public void handleDeleteDownloadItem(DownloadItem downloadItem) {
		// 锟姐播锟斤拷锟斤拷取锟斤拷
		Message message = Message.obtain();
		message.what = Constants.M_DOWNLOAD_CANCEL;
		message.obj = downloadItem;
		handleMarketMessage(message);
	}

	// 锟斤拷锟斤拷谐锟斤拷锟斤拷锟�
	public void checkMarketUpdate(boolean manul) {
		// 锟斤拷锟斤拷谐锟斤拷锟斤拷锟�
		ATaskMark taskMark = taskMarkPool.getMarketUpdateTaskMark(manul);
		if (serviceWraper.isTaskExist(taskMark)) {
			if (manul) {
				Toast.makeText(this, getString(R.string.wait_for_server_feedback), 150).show();
			}

		} else {
			// 锟矫伙拷锟街讹拷锟斤拷锟铰碉拷时锟斤拷锟斤拷删锟斤拷锟斤拷锟斤拷锟较�
			localeCacheManager.deleteCacheData(CacheConstants.MARKET_UPDATE_INFO);
			serviceWraper.checkMarketUpdate(this, taskMark, contextConfig);
			if (manul) {
				Toast.makeText(mApplication, getString(R.string.request_market_update), 150).show();
			}
		}
	}

	// 锟斤拷锟斤拷锟斤拷锟�
	public void checkSoftwareUpdate(boolean manul) {
		// 锟斤拷锟街达拷锟斤拷锟斤拷锟斤拷锟铰ｏ拷锟斤拷么直锟接凤拷锟斤拷
		if (!marketConfig.isCheckSoftwareUpdate()) {
			return;
		}

		ATaskMark taskMark = taskMarkPool.getSoftwareUpdateTaskMark(manul);
		// 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷
		if (serviceWraper.isTaskExist(taskMark)) {
			if (manul) {
				Toast.makeText(this, R.string.wait_for_server_feedback, 150).show();
			}

		} else {
			// 锟矫伙拷锟街讹拷锟斤拷锟铰碉拷时锟斤拷锟斤拷删锟斤拷锟斤拷锟斤拷锟较�
			localeCacheManager.deleteCacheData(CacheConstants.SOFTWARE_UPDATE_INFO);
			List<Software> softwareList = marketManager.getSoftwareList();
			if (softwareList.size() > 0) {
				serviceWraper.checkSoftwareUpdate(this, taskMark, null, softwareList);
				if (manul) {
					Toast.makeText(this, getString(R.string.request_software_update), 150).show();
				}

			} else if (manul) {
				Toast.makeText(this, getString(R.string.none_software_need_update), 150).show();
			}
		}
	}

	@Override
	public void receiveResult(ATaskMark taskMark, ActionException excetion, Object attactResult) {
		if (taskMark instanceof MarketUpdateTaskMark && taskMark.getTaskStatus() == ATaskMark.HANDLE_OVER) {
			if (attactResult != null) { // 锟斤拷锟斤拷锟斤拷锟斤拷卸锟斤拷锟斤拷锟斤拷锟剿碉拷锟斤拷锟斤拷懈锟斤拷锟斤拷锟较拷摹锟
                int versionCode = getSharedPrefManager().getIgnoredUpdateVersion();
                MarketUpdateInfo updateInfo = (MarketUpdateInfo) attactResult;
                if (versionCode == updateInfo.getVersionCode()) {
                    //user has ignore.
                }
				else {
                    MarketNotify.notifyMarketUpdateInfo(this, getString(R.string.market_update_title),
                            getString(R.string.find_version_note) + updateInfo.getVersion() + "  "
                                    + getString(R.string.local_version_note) + getContextConfig().getVersion());
                }
			} else if (((MarketUpdateTaskMark) taskMark).isManul()) {
				Toast.makeText(this, R.string.market_newest, 200).show();
			}

		} else if (taskMark instanceof SoftwareUpdateTaskMark && taskMark.getTaskStatus() == ATaskMark.HANDLE_OVER) {
			if (attactResult != null) {
				List<AppItem> itemList = (List<AppItem>) attactResult;
				// 通知
				MarketNotify.notifySoftwareUpdateInfo(this, getString(R.string.software_update_title),
						String.format(getString(R.string.software_can_update), itemList.size()));

			} else if ((((SoftwareUpdateTaskMark) taskMark).isManul())) {
				Toast.makeText(this, R.string.softwoft_newest, 200).show();
			}

			// 通知锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷
			Message msg = Message.obtain();
			msg.what = Constants.M_SOFTWARE_UPDATED;
			handleMarketMessage(msg);

		} else if (taskMark instanceof InitDownloadTaskMark && taskMark.getTaskStatus() == ATaskMark.HANDLE_OVER) {
			// 锟斤拷锟斤不锟斤拷要锟斤拷示锟斤拷锟斤拷锟剿ｏ拷锟侥碉拷锟斤拷锟斤拷锟阶帮拷恕锟�
			// 锟斤拷锟斤拷只锟斤拷锟斤拷要通知锟斤拷锟斤拷示锟斤拷see: InitDownloadTaskTracker.java
			// if (attactResult != null) {
			// MarketNotify.nofityAppDownloadComplete(this);
			// }

		} else if (taskMark instanceof StaticADTaskMark && taskMark.getTaskStatus() == ATaskMark.HANDLE_OVER) {
			LocaleCacheManager localeCacheManager = getLocaleCacheManager();
			StaticAD staticAD = (StaticAD) localeCacheManager.getDataFormCache(CacheConstants.STATIC_AD_INFO);
			if (staticAD != null) {
				//MarketNotify.notifyStaticADInfo(this, staticAD.getName(), staticAD.getName(), staticAD.getDes());
			}

		}

	}
	public void addIdToCommitFile(int appid)
	{
		String commitfile=getFilesDir()+File.separator+"commitfile";
		File tmp=new File(commitfile);
		FileWriter fw=null;
		try {
				fw=new FileWriter(tmp, true);
				Writer append=null;
				if(tmp.exists())
				{
					if(tmp.length()>0)
						append = fw.append(","+appid);
					else
						append=fw.append(appid+"");
				}
				fw.close();
				append.close();
			}catch(Exception e)
			{
				e.printStackTrace();
			}
	}
	/**
	 * 
	 * @return
	 */
	public String getCommitfile()
	{
		String commitfile=getFilesDir()+File.separator+"commitfile";
		File tmp=new File(commitfile);
		if(tmp.exists()){
		byte buf[]=new byte[(int) tmp.length()];
		FileInputStream fi=null;
		try {
		    fi=new FileInputStream(tmp);
			int read = fi.read(buf);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				fi.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return new String(buf);
			
		}
		}
		else
			return null;
	}
	/**
	 * 
	 */
	public void handleCommitDownload()
	{
		String commitfile=getFilesDir()+File.separator+"commitfile";
		File tmp=new File(commitfile);
		try{
			if(tmp.exists())
			{
				FileWriter fw=new FileWriter(tmp);
				fw.write("");
				fw.close();
			}
		
		}catch(Exception e){
			e.printStackTrace();
		}
		

	}
	public void getSearchKeyWord()
	{
		SearchKeywordTaskMark searchKeywordTaskMark = taskMarkPool.getSearchKeywordTaskMark();
	
		serviceWraper.getSearchKeyword(MApplication.this, searchKeywordTaskMark);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "MarketContext [" + " baseDataOk=" + baseDataOk + ", marketConfig=" + contextConfig + ", ts=" + ts + "]";
	}
}
