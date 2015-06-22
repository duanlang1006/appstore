package com.android.applite.plugin;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.applite.model.AppLiteModel;
import com.android.applite.model.IAppInfo;
import com.android.applite.model.IModelCallback;
import com.android.applite.model.UpdateHelper;
import com.android.applite.view.AppsCustomizePagedView;
import com.android.applite.view.AppsOnlinePagedView;
import com.android.applite.view.GalleryFlow;
import com.android.applite.view.Panel;
import com.android.dsc.service.IDscInterface;
import com.applite.android.R;
import com.applite.util.AppLiteSpUtils;
import com.applite.util.AppliteConfig;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;

public class AppLitePlugin extends Activity implements IModelCallback,
		OnClickListener/*, OnItemSelectedListener*/ {
	private static final boolean DEBUG = false;
	private static final String TAG = "AppLitePlugin";
	private static final String UPDATE_APP_URL = "http://www.yangmarket.com/applite/applite_main_interface.php";
	private LayoutInflater mInflater;
	private AppsOnlinePagedView mAppsOnlineView;
	private AppsCustomizePagedView mAppsOfflineView;
	private TextView mTitle;
	private TextView mTitle1;
	private TextView mVersion;
	//private Button mMore;
	private Button mBack;
	private TextView mSize;
//	private ImageView mImageDesc;
	private Animation mShowAnimation, mHideAnimation;
	private Button mDownload;
	private AppLiteModel model;
	private TextView mTextDesc;
	private ImageView mAppImage;
	private RelativeLayout mRelativeDesc;
//	private LinearLayout mLinearDescImage;
	private LayoutParams lp;
	private Panel mPanel;
	private LinearLayout mLinearMain;
	private static LinearLayout mLinearLayout;
	private LinearLayout mLayout;
	public static boolean mDetailFlag = false;
	private String packageName;
	private String details;
	private boolean flag=true;
	private ProgressDialog mDialog;
	private GalleryFlow mGallery;
	private int count=0;
	private IAppInfo saveInfo;
	private FrameLayout mFrameLayout;
	private boolean mShuttingdown = false;
	private IAppLiteOperator mOperator = new IAppLiteOperator() {
		@Override
		public void onRemoveAppClick(IAppInfo info) {
		    info.removeApp();
			mDetailFlag = false;
			MobclickAgent.onEvent(AppLitePlugin.this,"Uninstall");//友盟计数事件
		};

		@Override
		public void startActivitySafely(Intent intent, Object tag) {
			// TODO Auto-generated method stub
			try {
				MobclickAgent.onEvent(AppLitePlugin.this,"RunNumber");//友盟计数事件
				final IAppInfo appInfo = (IAppInfo) tag;
				if (!mAppsOfflineView.getRemoveMode()) {
				    appInfo.launchApp();
//					AppLitePlugin.this.startActivity(intent);
//					model.appRunning(appInfo);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onOnlineAppClick(IAppInfo appInfo) {
			// TODO Auto-generated method stub
			mLinearLayout.setVisibility(LinearLayout.GONE);
		/*	count++;
			if (count==1) {
				saveInfo=appInfo;
			}else if (count>=2) {
				if (saveInfo==appInfo) {
					handleAfterDownload();
					count = 0;
					return;
				}else {
					saveInfo=appInfo;
				}
			}
			detailDescription(appInfo);*/
			 handleItemDownload(appInfo);
		}

		@Override
		public void onOfflineAppClick(IAppInfo info) {
			if (!mAppsOfflineView.getRemoveMode()) {
				handleItemDownload(info);
			}
		};

		@Override
		public void onAppOnLongClick() {
			mAppsOfflineView.setRemoveMode(true);
		}

		@Override
		public void onShowIndication(IAppInfo info) {
			// TODO Auto-generated method stub
			if (IAppInfo.AppOnline == info.getItemType() || IAppInfo.AppMore == info.getItemType()) {
				info.setShown();
			}
		}
	};
	private PackageInfo info;
	private String versionName;
	private boolean mIsPullNetwork = false;
	private static final int PULL_NETWORK = 1;
	IDscInterface mDsc = null;
	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder service) {
			mDsc = IDscInterface.Stub.asInterface(service);
			try {
			    if (DEBUG)
			        Log.d(TAG,"uuid:"+mDsc.getDbserviceUUID()+",userid:"+mDsc.getUserId());
			    String uuid = mDsc.getDbserviceUUID();
			    String userid = mDsc.getUserId();
			    AppliteConfig.setUUID(AppLitePlugin.this,uuid);
			    AppliteConfig.setUserId(AppLitePlugin.this,userid);
				PullNetwork(uuid ,userid);
			} catch (RemoteException e) {
			}
		}
	
		public void onServiceDisconnected(ComponentName className) {
			mDsc = null;
		}
	};
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case PULL_NETWORK:
				UpdateHelper helper = new UpdateHelper();
				final String uuid = "applite_" + helper.getDeviceUUID(AppLitePlugin.this , null);
				new Handler().postDelayed(new Runnable() {// 5秒后自动请求网络
							@Override
							public void run() {
								PullNetwork(uuid ,null);
							}
						}, 5000);
				break;
			}
		}

	};
	
	/**
	 * Bitmap旋转９０度
	 */
	private Bitmap onRotaBitmap(Bitmap bm) {
		Matrix m = new Matrix();
		m.setRotate(90, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
		float targetX, targetY;
		targetX = bm.getHeight();
		targetY = 0;

		final float[] values = new float[9];
		m.getValues(values);

		float x1 = values[Matrix.MTRANS_X];
		float y1 = values[Matrix.MTRANS_Y];

		m.postTranslate(targetX - x1, targetY - y1);

		Bitmap bm1 = Bitmap.createBitmap(bm.getHeight(), bm.getWidth(),	Bitmap.Config.ARGB_8888);
		Paint paint = new Paint();
		Canvas canvas = new Canvas(bm1);
		canvas.drawBitmap(bm, m, paint);

		return bm1;
	}

	private void detailDescription(IAppInfo appInfo) {
		mFrameLayout.setBackgroundResource(R.color.app_bg);
//		int index=mGalleryAdapter.data.indexOf(appInfo);
//		mGallery.setSelection(index);
		mAppsOnlineView.Selection(appInfo);
//		Bitmap mBitmap = appInfo.getDetailIcon(true);
		mBack.setVisibility(Button.VISIBLE);
		mAppsOfflineView.setVisibility(View.GONE);
		mAppImage.setImageBitmap(appInfo.getIcon());
		mTitle.setText(appInfo.getTitle());
		mTitle1.setText(appInfo.getTitle());
		if (!appInfo.getVersionName().trim().equals("")) {
			mVersion.setText(appInfo.getVersionName());
		}
		BigDecimal b = new BigDecimal((float) appInfo.getSize() / 1024 / 1024);
		float f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
		mSize.setText(f1 + "M");
		mTextDesc.setText("\t\t" + appInfo.getDetailText());
		mRelativeDesc.setVisibility(RelativeLayout.VISIBLE);
		mLayout.setVisibility(LinearLayout.GONE);
		mDetailFlag = true;
//		if (mBitmap != model.getIconCache().getDetailIcon() && mBitmap.getWidth() > mBitmap.getHeight()) {
//			mImageDesc.setImageBitmap(onRotaBitmap(mBitmap));
//		}else {
//		mImageDesc.setImageBitmap(mBitmap);
//		}
		if (appInfo.getItemType() == IAppInfo.AppInstalled||appInfo.getItemType() == IAppInfo.AppMore) {
			mDownload.setText(R.string.start_up);
		} else {
			mDownload.setText(R.string.download_start);
		}
		mRelativeDesc.setTag(appInfo);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (DEBUG)
			Log.d(TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		  if(VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
	            Window window = getWindow();
	            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
	                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
	            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
	                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
	                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
	            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
	            window.setStatusBarColor(Color.TRANSPARENT);
	            window.setNavigationBarColor(Color.TRANSPARENT);
	        }
		int screenHeight = dm.heightPixels;
		int screenWight = dm.widthPixels;
		mInflater = getLayoutInflater();
		setupViews();
		setViewSize(screenHeight, screenWight);
		model = AppLiteModel.getInstance(this);
		model.initialize(this);
		processNewIntent(getIntent());
		mPanel.setmAppsCustomizePagedView(mAppsOfflineView);
		mPanel.setmLayout(mLinearLayout);
		MobclickAgent.updateOnlineConfig(this);
		
		getVersion();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mShuttingdown = true;
	}
/*	intent.setPackage("com.android.dsc.service");*/
	protected void onStart() {
		super.onStart();
		Intent intent = new Intent("com.android.dsc.service.DscService.bind");
		intent.setPackage("com.android.dbservices");
		bindService(intent, mConnection,
				Context.BIND_AUTO_CREATE);
		mHandler.sendEmptyMessageDelayed(PULL_NETWORK, 1000);
	}
	
	protected void onStop() {
		super.onStop();
		if (null != mDsc) {
			unbindService(mConnection);
		}
	};
	
	private void PullNetwork(String uuid ,String userid){
		if(!mIsPullNetwork){
			//判断请求更新的时间是否到了
			if(System.currentTimeMillis() > AppLiteSpUtils.getUpdateAppTime(this)){
				upDateApp(uuid ,userid);
			}
			//判断发送统计数据的时间是否到了
			if(System.currentTimeMillis() > AppLiteSpUtils.getUpdateDataTime(this)){
				pullData(uuid);
			}
			mIsPullNetwork = true;
		}
	}
	
	/**
	 * 把点击数据传送给服务器
	 */
	private void pullData(String uuid) {
		//下次请求的时间
		AppLiteSpUtils.setUpdateDataTime(this, (System.currentTimeMillis() + 24 * 60 * 60 * 1000));
		
		JSONObject data = new JSONObject();
		try {
			data.put("uuid", uuid);
			data.put("swver", versionName);
			data.put("ylzx", AppLiteSpUtils.getDataYLZX(this));
			data.put("zjbb", AppLiteSpUtils.getDataZJBB(this));
			data.put("carry_on", AppLiteSpUtils.getDataCarryOn(this));
			data.put("download", AppLiteSpUtils.getDataDownload(this));
			data.put("download_success", AppLiteSpUtils.getDataDownloadSuccess(this));
			data.put("install_success", AppLiteSpUtils.getDataInstallSuccess(this));
			data.put("run_number", AppLiteSpUtils.getDataRunNumber(this));
			data.put("time_out", AppLiteSpUtils.getDataTimeOut(this));
			data.put("uninstall", AppLiteSpUtils.getDataUninstall(this));
		} catch (Exception e) {
			if(DEBUG){
				Log.e(TAG, "JSON异常");
			}
			e.printStackTrace();
		}
		
		FinalHttp finalHttp = new FinalHttp();
		AjaxParams params = new AjaxParams();
		params.put("type", "action");
		params.put("data", data.toString());
		finalHttp.post(UPDATE_APP_URL, params, new AjaxCallBack<Object>(){
			@Override
			public void onSuccess(Object t) {
				super.onSuccess(t);
				String result = (String) t;
				if(DEBUG){
					Log.e(TAG, "pullDataResult:"+result);
				}
				try {
					JSONObject Json = new JSONObject(result);
					JSONArray JsonList = Json.getJSONArray("action_info");
					String mStatus = JsonList.getJSONObject(0).getString("status");
					if("1".equals(mStatus)){
						//数据发送成功后重置为0
						AppLiteSpUtils.setDataCarryOn(AppLitePlugin.this, 0);
						AppLiteSpUtils.setDataDownload(AppLitePlugin.this, 0);
						AppLiteSpUtils.setDataDownloadSuccess(AppLitePlugin.this, 0);
						AppLiteSpUtils.setDataInstallSuccess(AppLitePlugin.this, 0);
						AppLiteSpUtils.setDataRunNumber(AppLitePlugin.this, 0);
						AppLiteSpUtils.setDataTimeOut(AppLitePlugin.this, 0);
						AppLiteSpUtils.setDataUninstall(AppLitePlugin.this, 0);
						AppLiteSpUtils.setDataYLZX(AppLitePlugin.this, 0);
						AppLiteSpUtils.setDataZJBB(AppLitePlugin.this, 0);
					}

				} catch (JSONException e) {
					if(DEBUG){
						Log.e(TAG, "pullData---JSON解析错误");
					}
					e.printStackTrace();
				}
			}
			
			@Override
			public void onFailure(Throwable t, int errorNo, String strMsg) {
				super.onFailure(t, errorNo, strMsg);
			}
		});
	}
	
	/**
	 * 检查是否更新
	 */
	private void upDateApp(String uuid ,String userid) {
		FinalHttp mFinalHttp = new FinalHttp();
		AjaxParams params = new AjaxParams();
		try {
			params.put("uuid", uuid);
			params.put("userid", userid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		params.put("swver", versionName);
		params.put("type", "swver");
		mFinalHttp.post(UPDATE_APP_URL, params, new AjaxCallBack<Object>() {
			@Override
			public void onSuccess(Object t) {
				super.onSuccess(t);
				if(mShuttingdown){
					return;
				}
				String result = (String) t;
				if(DEBUG){
					Log.e(TAG, "upDateAppResult:"+result);
				}
				try {
					JSONObject Json = new JSONObject(result);
					JSONObject object = Json.getJSONObject("swver_info");
					String mVersion = object.getString("swver");
					boolean mAllow = object.getBoolean("allow");
					long mTime= object.getLong("next");
					boolean mStatus = object.getBoolean("status");
					Log.e(TAG, mVersion+mAllow+mTime+mStatus);
					if(mAllow){
						UmengUpdateAgent.update(AppLitePlugin.this);
					}
					//设置下次更新的时间
					AppLiteSpUtils.setUpdateAppTime(AppLitePlugin.this, mTime);
				} catch (JSONException e) {
					if(DEBUG){
						Log.e(TAG, "upDateApp---JSON解析错误");
					}
					e.printStackTrace();
				}
			}
			
			@Override
			public void onFailure(Throwable t, int errorNo, String strMsg) {
				super.onFailure(t, errorNo, strMsg);
			}
		});
	}
	
	/**
	 * 得到和设置当前APK的版本号
	 */
	private void getVersion() {
		try {
			info = this.getPackageManager().getPackageInfo(
					this.getPackageName(), 0);
			// 当前应用的版本名称
			versionName = info.versionName;
			// 当前版本的版本号
			// versionCode = info.versionCode;
		} catch (Exception e) {

		}
	}

	private void setViewSize(int screenHeight, int screenWight) {
		// lp=mAppsOnlineView.getLayoutParams();
		// lp.height=screenHeight*10/50;
		// mAppsOnlineView.setLayoutParams(lp);
		float dimen = getResources().getDimension(R.dimen.app_onlineview_size);
		int height = BitmapFactory.decodeResource(getResources(),R.drawable.app_hub_game_handle_up_n).getHeight();
		lp = mLinearLayout.getLayoutParams();
		lp.height = (int) (dimen + height);
		mLinearLayout.setLayoutParams(lp);
//		lp = mLinearDescImage.getLayoutParams();
//		lp.height = (screenHeight / 2);
//		lp.width = screenWight * 3 / 5;
//		mLinearDescImage.setLayoutParams(lp);
	}

	private void processNewIntent(Intent intent) {
	    
	    boolean hidden = AppliteConfig.getRecommendHidden(this);
        if(hidden){
            mFrameLayout.setVisibility(FrameLayout.GONE);
        }else{
            mFrameLayout.setVisibility(FrameLayout.VISIBLE);
        }
	    
		int savedCategory = AppliteConfig.getProject(this);
		int project = intent.getIntExtra(AppliteConfig.KEY_PROJECT,IAppInfo.CatgoryNone);
		AppliteConfig.setProject(this,project);
		if (null == intent	|| savedCategory == project) {
			model.startLoader(this, false);
			if (DEBUG)
				Log.d(TAG, "model.startLoader(),model.isAllAppsLoaded()="+ model.isAllAppsLoaded());
		} else {
			model.forceReload();
			mAppsOnlineView.forceInit();
			mAppsOfflineView.forceInit();
			if (DEBUG)
				Log.d(TAG, "model.forceReload(),model.isAllAppsLoaded()="+model.isAllAppsLoaded());
		}
		if (AppliteConfig.getProject(this) == IAppInfo.CatgoryZjbb) {
			mTitle.setText(R.string.app_name_hotapp);
			mLinearMain.setBackgroundResource(R.drawable.zjb);
		} else if (AppliteConfig.getProject(this) == IAppInfo.CatgoryYlzx) {
			mTitle.setText(R.string.app_name_enter);
			mLinearMain.setBackgroundResource(R.drawable.ylz);
		}
		if (mRelativeDesc.getVisibility() == RelativeLayout.VISIBLE) {
			mRelativeDesc.setVisibility(RelativeLayout.GONE);
			mBack.setVisibility(Button.GONE);
			mAppsOfflineView.setVisibility(View.VISIBLE);
			mFrameLayout.setBackgroundResource(0);
		}
		mLayout.setVisibility(LinearLayout.VISIBLE);
		mLinearLayout.setVisibility(LinearLayout.VISIBLE);
		if (mPanel.getContent().getVisibility() == View.GONE) {
			mPanel.setContent();
		}
		if (mAppsOfflineView.getRemoveMode()) {
			mAppsOfflineView.setRemoveMode(false);
		}
		if (!model.isAllAppsLoaded()) {
			ViewGroup appsCustomizeContentParent = (ViewGroup) mAppsOnlineView.getParent();
			mInflater.inflate(R.layout.apps_customize_progressbar,appsCustomizeContentParent);
		}
		packageName = getPakageNameFromData(getIntent().getData());
		details=getDetailsFromData(getIntent().getData());
		mAppsOfflineView.setLinearLayout(mLinearLayout);
		mAppsOnlineView.setmGallery(mGallery);
	}
	private String getPakageNameFromData(Uri data) {
		String pakageName = null;
		if (data != null) {
			String dat=data+"";
			pakageName = dat.substring(dat.lastIndexOf("=") + 1);
		}
		return pakageName;
	}
	private String getDetailsFromData(Uri data){
		String details = null;
		if (data != null) {
			String dat=data+"";
			details = dat.substring(dat.lastIndexOf("/") + 1);
		}
		return details;
	}
	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		if (DEBUG)
			Log.d(TAG,"onNewIntent(),project=" + intent.getStringExtra(AppliteConfig.KEY_PROJECT));
		super.onNewIntent(intent);
		getWindow().setBackgroundDrawable(null);
		processNewIntent(intent);
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		if (DEBUG)
			Log.d(TAG, "onResume");
		super.onResume();
		mAppsOnlineView.onResume();
		mAppsOfflineView.onResume();
		if (AppliteConfig.getProject(this) != IAppInfo.CatgoryNone) {
			model.updateOnlineApp();
		}
		MobclickAgent.onResume(this);
		
//		DisplayMetrics dm = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(dm);
//        Log.d("pixel","屏幕宽/高:"+dm.widthPixels+"/"+dm.heightPixels
//                +",密度:"+getResources().getDisplayMetrics().density+"/"+dm.density
//                +",dip:"+(dm.widthPixels/dm.density));
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPause(this);
		if (mDialog!=null) {
			mDialog.cancel();
		}
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK && mAppsOfflineView.getRemoveMode() 
		            && mRelativeDesc.getVisibility()!= RelativeLayout.VISIBLE) {
			mAppsOfflineView.setRemoveMode(false);
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_BACK && mRelativeDesc.getVisibility() == RelativeLayout.VISIBLE) {
			if (mAppsOfflineView.mNumAppsPages == 1) {
				mLinearLayout.setVisibility(LinearLayout.VISIBLE);
			} else {
				mLinearLayout.setVisibility(LinearLayout.GONE);
			}
			mRelativeDesc.setVisibility(RelativeLayout.GONE);
			mLayout.setVisibility(LinearLayout.VISIBLE);
			mBack.setVisibility(Button.GONE);
			mFrameLayout.setBackgroundResource(0);
			if (AppliteConfig.getProject(this) == IAppInfo.CatgoryZjbb) {
				mTitle.setText(R.string.app_name_hotapp);
			} else if (AppliteConfig.getProject(this) == IAppInfo.CatgoryYlzx) {
				mTitle.setText(R.string.app_name_enter);
			}
			if (packageName!=null) {
				mTitle.setText("");
			}
			mAppsOfflineView.setVisibility(View.VISIBLE);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean setLoadOnResume() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void startRefresh() {
		// TODO Auto-generated method stub
		mAppsOnlineView.post(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (mAppsOnlineView != null) {
					mAppsOnlineView.onStartRefresh();
				}
			}
		});
	}

	@Override
	public void refreshing() {
		// TODO Auto-generated method stub
		mAppsOnlineView.post(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (mAppsOnlineView != null) {
					mAppsOnlineView.onRefreshing();
				}
			}
		});
	}

	@Override
	public void finishRefresh() {
		// TODO Auto-generated method stub
		mAppsOnlineView.post(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (mAppsOnlineView != null) {
					mAppsOnlineView.onFinishRefresh();
				}
			}
		});
	}

	@Override
	public void bindAll(final boolean recommend, final ArrayList<IAppInfo> apps) {
		// TODO Auto-generated method stub
		if (DEBUG) 
			Log.d(TAG,"bindAll" + (recommend ? ",online" : ",offline") + apps.size()+ apps);
		// final long t = DEBUG?SystemClock.uptimeMillis():0;
		View progressBar = findViewById(R.id.apps_customize_progress_bar);
		if (progressBar != null) {
			((ViewGroup) progressBar.getParent()).removeView(progressBar);
		}
		if (recommend) {
			mAppsOnlineView.setApps(apps);
//			mGalleryAdapter=new GalleryAdapter(apps);
//			mGallery.setAdapter(mGalleryAdapter);
		} else {
			mAppsOfflineView.setApps(apps);
		}
		if (packageName != null) {
			IAppInfo info = model.getInfoByPackageName(packageName);
			if (info == null) {
				//1.从服务端获取json数据
				if (flag) {
					mDialog = new ProgressDialog(AppLitePlugin.this);
					mDialog.setMessage(getString(R.string.getdata));
					mDialog.show();
					model.addMarketApp(details, packageName);
					flag=false;
				}
			} else {
				if (mDialog!=null) {
					mDialog.cancel();
				}
				if (info.getItemType() != IAppInfo.AppOffline) {
					detailDescription(info);
				} else {
					Toast.makeText(this, info.getTitle() + getString(R.string.hasdownload),Toast.LENGTH_SHORT).show();
				}
			}
		}
		// if (DEBUG&false){
		// Log.d(TAG,"bindAll,cost:"+(SystemClock.uptimeMillis()-t)+" ms");
		// }
	}
	@Override
	public void bindAdded(final boolean recommend,
			final ArrayList<IAppInfo> apps) {
		// TODO Auto-generated method stub
		if (DEBUG) 
			Log.e(TAG,"bindAdded" + (recommend ? ",online" : ",offline") + apps.size() + apps);
		// final long t = DEBUG?SystemClock.uptimeMillis():0;
		if (apps.size() < 1) {
			return;
		}
		if (recommend) {
			if (null != mAppsOnlineView) {
				mAppsOnlineView.addApps(apps);
//				mGalleryAdapter.notifyDataSetChanged();
			}
		} else {
			if (null != mAppsOfflineView) {
				mAppsOfflineView.addApps(apps);
			}
		}
		// if (DEBUG & false){
		// Log.d(TAG,"bindAdded,cost:"+(SystemClock.uptimeMillis()-t)+" ms");
		// }
	}

	@Override
	public void bindUpdated(final boolean recommend, ArrayList<IAppInfo> apps) {
		if (DEBUG) 
			Log.v(TAG,"bindUpdated"+(recommend?",online":",offline")+apps.size()+","+apps);
		// final long t = DEBUG?SystemClock.uptimeMillis():0;
		if (apps.size() < 1) {
			return;
		}
		if (mRelativeDesc.getVisibility() == RelativeLayout.VISIBLE) {
//			Bitmap mBitmap = ((IAppInfo) mRelativeDesc.getTag()).getDetailIcon(false);
			Bitmap appBitmap=((IAppInfo) mRelativeDesc.getTag()).getIcon();
//			mImageDesc.setImageBitmap(mBitmap);
			mAppImage.setImageBitmap(appBitmap);
//			mGalleryAdapter.notifyDataSetChanged();
		}
		if (recommend) {
			if (null != mAppsOnlineView) {
				mAppsOnlineView.updateApps(apps);
			}
		} else {
			if (null != mAppsOfflineView) {
				mAppsOfflineView.updateApps(apps);
			}
		}
		// if (DEBUG & false){
		// Log.d(TAG,"bindUpdated,cost:"+(SystemClock.uptimeMillis()-t)+" ms");
		// }
	}

	@Override
	public void bindRemoved(final boolean recommend, ArrayList<IAppInfo> apps,
			boolean permanent) {
		if (DEBUG) 
			Log.i(TAG,"bindRemoved" + (recommend ? ",online" : ",offline")+ apps.size() + apps);
		// final long t = DEBUG?SystemClock.uptimeMillis():0;
		if (apps.size() < 1) {
			return;
		}
		if (recommend) {
			if (null != mAppsOnlineView) {
				mAppsOnlineView.removeApps(apps);
			}
		} else {
			if (null != mAppsOfflineView) {
				mAppsOfflineView.removeApps(apps);
			}
		}
		// if (DEBUG & false){
		// Log.d(TAG,"bindRemoved,cost:"+(SystemClock.uptimeMillis()-t)+" ms");
		// }
	}

	@Override
	public void downloadStatusFailed(final String str, final long downloadId) {
//		new AlertDialog.Builder(this)
//				.setTitle(R.string.dialog_title_not_available)
//				.setMessage(str)
//				.setNegativeButton(android.R.string.cancel, null)
//				.setPositiveButton(R.string.retry_download,
//						getRestartClickHandler(downloadId)).show();
	}
	@Override
	public void getDataFailed() {
		// TODO Auto-generated method stub
		if (mDialog!=null) {
			mDialog.cancel();
		}
	}
	
	@Override
	public void wakeDetail(IAppInfo info) {
		// TODO Auto-generated method stub
		detailDescription(info);
	}
	private void setupViews() {
		mTitle = (TextView) findViewById(R.id.app_hub_title);
		mTitle1 = (TextView) findViewById(R.id.text_app_title);
		mVersion = (TextView) findViewById(R.id.text_app_version);
		mSize = (TextView) findViewById(R.id.text_app_size);
		mDownload = (Button) findViewById(R.id.button_download_item);
		mBack = (Button) findViewById(R.id.back);
		//mMore = (Button) findViewById(R.id.show_or_more);
//		mImageDesc = (ImageView) findViewById(R.id.image_app_desc);
//		mLinearDescImage = (LinearLayout) findViewById(R.id.linear_image_desc);
		mLinearMain = (LinearLayout) findViewById(R.id.linear_main);
		mAppImage = (ImageView) findViewById(R.id.app_image_desc);
		mRelativeDesc = (RelativeLayout) findViewById(R.id.desc_item);
		mTextDesc = (TextView) findViewById(R.id.text_app_desc);
		mPanel = (Panel) findViewById(R.id.bottomPanel);
		mLayout = (LinearLayout) findViewById(R.id.linear_bottom);
		mLinearLayout = (LinearLayout) findViewById(R.id.linear_panel);
		//initAnimation();
		mAppsOnlineView = (AppsOnlinePagedView) findViewById(R.id.apps_customize_pane_content);
		mAppsOfflineView = (AppsCustomizePagedView) findViewById(R.id.install_apps_customize_pane_content);
		mGallery=(GalleryFlow) findViewById(R.id.gallery_flow);
//		mGallery.setSpacing(-20);
		mFrameLayout=(FrameLayout) findViewById(R.id.frame_panel);
		if (null != mAppsOnlineView) {
			mAppsOnlineView.setup(mOperator);
		}
		if (mAppsOfflineView != null) {
			mAppsOfflineView.setup(mOperator);
		}
		mDownload.setOnClickListener(this);
		//mMore.setOnClickListener(this);
		mBack.setOnClickListener(this);
		mAppsOnlineView.Callback(this);
//		mGallery.setOnItemSelectedListener(this);
//		mAppsOnlineView.setHeaderView((RefreshView) findViewById(R.id.paged_view_header));
//		mAppsOnlineView.setFooterView((RefreshView) findViewById(R.id.paged_view_footer));
	}

	private void initAnimation() {
		// 从自已-1倍的位置移到自己原来的位置
		mShowAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
				0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
				Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF,
				0.0f);
		mHideAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
				0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
				-1.0f);
		mShowAnimation.setDuration(1000);
		mHideAnimation.setDuration(1000);

	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.back:
			mFrameLayout.setBackgroundResource(0);
			mRelativeDesc.setVisibility(RelativeLayout.GONE);
			mLayout.setVisibility(LinearLayout.VISIBLE);
			if (mAppsOfflineView.mNumAppsPages == 1) {
				mLinearLayout.setVisibility(LinearLayout.VISIBLE);
			} else {
				mLinearLayout.setVisibility(LinearLayout.GONE);
			}
			mBack.setVisibility(Button.GONE);
			if (AppliteConfig.getProject(this) == IAppInfo.CatgoryZjbb) {
				mTitle.setText(R.string.app_name_hotapp);
			} else if (AppliteConfig.getProject(this) == IAppInfo.CatgoryYlzx) {
				mTitle.setText(R.string.app_name_enter);
			}
			if (packageName!=null) {
				mTitle.setText("");
			}
			mAppsOfflineView.setVisibility(View.VISIBLE);
			MobclickAgent.onEvent(this,"ExitDetails");//友盟计数事件
			break;
		/*case R.id.show_or_more:
			try {
				Intent intent = model.getMoreIntent();
				startActivity(intent);
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;*/
		case R.id.button_download_item:
			handleAfterDownload();
			MobclickAgent.onEvent(AppLitePlugin.this,"Download");//友盟计数事件
			break;
		default:
			break;
		}
	}

	private void handleAfterDownload() {
		mFrameLayout.setBackgroundResource(0);
		mRelativeDesc.setVisibility(RelativeLayout.GONE);
		mLayout.setVisibility(LinearLayout.VISIBLE);
		mDetailFlag = false;
		mBack.setVisibility(Button.GONE);
		if (AppliteConfig.getProject(this) == IAppInfo.CatgoryZjbb) {
			mTitle.setText(R.string.app_name_hotapp);
		} else if (AppliteConfig.getProject(this) == IAppInfo.CatgoryYlzx) {
			mTitle.setText(R.string.app_name_enter);
		}
		if (packageName!=null) {
			mTitle.setText("");
		}
		mAppsOfflineView.setVisibility(View.VISIBLE);
		if (mAppsOfflineView.getRemoveMode()) {
			mAppsOfflineView.setRemoveMode(false);
		}
		IAppInfo appInfo = (IAppInfo)mRelativeDesc.getTag();
		if (appInfo.getItemType()==IAppInfo.AppInstalled
				||appInfo.getItemType()==IAppInfo.AppMore) {
		    appInfo.launchApp();
//			AppLitePlugin.this.startActivity(((IAppInfo)mRelativeDesc.getTag()).getIntent());
//			model.appRunning((IAppInfo)mRelativeDesc.getTag());
		}else {
			handleItemDownload((IAppInfo) mRelativeDesc.getTag());
		}
	}
	
	private DialogInterface.OnClickListener getRestartClickHandler(
			final long downloadId) {
		return new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
//				model.downloadRestart(downloadId);
			}
		};
	}

	private void handleItemDownload(IAppInfo info) {
		String state = Environment.getExternalStorageState();
		if (!Environment.MEDIA_MOUNTED.equals(state) || !Environment.getExternalStorageDirectory().canWrite()) {
			Toast.makeText(this, R.string.download_TF_msg, Toast.LENGTH_SHORT).show();
			return;
		}
		if (AppliteConfig.getNetwork(this).equals("none")) {
			Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
		}
		info.downloadApp();
//		model.downloadApp(info);
		// long downloadId = info.getDownloadId();
		// if (downloadId > 0){
		// DownloadManager.Query query = new DownloadManager.Query();
		// query.setFilterById(downloadId);
		// Cursor c = null;
		// try {
		// c = DownloadManager.getInstance(this).query(query);
		// if (c != null && c.moveToFirst()) {
		// int downloadStatus =
		// c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
		// switch(downloadStatus){
		// case DownloadManager.STATUS_PENDING:
		// case DownloadManager.STATUS_RUNNING:
		// model.downloadPause(downloadId);
		// break;
		// case DownloadManager.STATUS_PAUSED:
		// model.downloadResume(isPausedForWifi(c),downloadId);
		// break;
		// case DownloadManager.STATUS_FAILED:
		// int reason =
		// c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON));
		// if (DownloadManager.ERROR_FILE_ALREADY_EXISTS == reason){
		// model.downloadApp(info);
		// }else{
		// }
		// break;
		// case DownloadManager.STATUS_SUCCESSFUL:
		// model.downloadSuccessful(info);
		// break;
		// }
		// }
		// } finally {
		// if (c != null) {
		// c.close();
		// }
		// }
		// }else{
		// model.downloadApp(info);
		// }
	}
//	private class GalleryAdapter extends BaseAdapter{
//		ArrayList<IAppInfo> data;
//		
//		public GalleryAdapter(ArrayList<IAppInfo> data) {
//			super();
//			this.data = data;
//		}
//
//		@Override
//		public int getCount() {
//			// TODO Auto-generated method stub
//			return data.size();
//		}
//
//		@Override
//		public Object getItem(int position) {
//			// TODO Auto-generated method stub
//			return position;
//		}
//
//		@Override
//		public long getItemId(int position) {
//			// TODO Auto-generated method stub
//			return position;
//		}
//
//		@Override
//		public View getView(int position, View convertView, ViewGroup parent) {
//			// TODO Auto-generated method stub
//			ImageView mImageView=new ImageView(AppLitePlugin.this);
//			BitmapDrawable drawable=new BitmapDrawable(AppLitePlugin.this.getResources(), data.get(position).getDetailIcon(true));
//			drawable.setAntiAlias(true);
//			mImageView.setImageDrawable(drawable);
//			// imageView.setImageDrawable(mBitmaps.get(position));
//			mImageView.setScaleType(ScaleType.FIT_XY);
//			return mImageView;
//		}
//		
//	}
//	@Override
//	public void onItemSelected(AdapterView<?> parent, View view, int position,
//			long id) {
//		// TODO Auto-generated method stub
//		int mCellCountx=mAppsOnlineView.getCellCountX();
//		detailDescription(mGalleryAdapter.data.get(position));
//		if (position%mCellCountx==0||position%mCellCountx==mCellCountx-1) {
//			mAppsOnlineView.snapToPage(position/mCellCountx);
//		}
//	}
//	
//	@Override
//	public void onNothingSelected(AdapterView<?> parent) {
//		// TODO Auto-generated method stub
//	}
}