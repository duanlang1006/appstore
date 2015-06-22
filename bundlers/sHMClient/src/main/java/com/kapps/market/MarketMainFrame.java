package com.kapps.market;

import java.text.DecimalFormat;

import org.androidpn.client.ServiceManager;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.TabListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

import com.kapps.market.bean.AppItem;
import com.kapps.market.bean.BaseApp;
import com.kapps.market.bean.PageInfo;
import com.kapps.market.service.ActionException;
import com.kapps.market.service.impl.HttpMarketService;
import com.kapps.market.task.IResultReceiver;
import com.kapps.market.task.mark.ATaskMark;
import com.kapps.market.task.mark.AddFavorTaskMark;
import com.kapps.market.task.mark.AppAdvertiseTaskMark;
import com.kapps.market.task.mark.DeleteFavorTaskMark;
import com.kapps.market.ui.CommonView;
import com.kapps.market.ui.TabableAppView;
import com.kapps.market.ui.app.AppCategoryPage;
import com.kapps.market.ui.app.AppDiggPage;
import com.kapps.market.ui.app.AppOfDailyCategoryPage;
import com.kapps.market.ui.app.AppOfGameCategoryPage;
import com.kapps.market.util.Constants;
import com.kapps.market.util.ResourceEnum;

/**
 * 2010-6-11 market 閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷�
 * 
 * @author admin
 * 
 */
public class MarketMainFrame extends TabableActivity implements
		OnClickListener, IResultReceiver {
	public static final String TAG = "MarketMainFrame";

	/** Called when the activity is first created. */
	public static final int MAIN_VIEW = 0;
    public static final int GAME_VIEW = 1;
	public static final int CATEGORY_VIEW = 2;
	public static final int DIGG_VIEW = 3;

	public static final int MANAGE_VIEW = 5;
	public static final int PREVIEW_VIEW = 6;

	public static final int ONLINE_VIEW=7;
	public static final int LOCAL_VIEW = 8;
	protected MApplication marketContext = MApplication.getInstance();
	
	//view pager
	private ViewPager mViewPager;
	public int VIEW_COUNT = 0;
    private long mLastBackPress = 0;
    private AppOfDailyCategoryPage dailyPage;
    private AppOfGameCategoryPage gamePage;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_frame);

		ServiceManager serviceManager = new ServiceManager(this);
		serviceManager.setNotificationIcon(R.drawable.a_notification_icon);
		serviceManager.startService();

		initPaper();
//        if (contentFrame == null) {
//            contentFrame = (android.widget.FrameLayout) findViewById(R.id.contentFrame);
//        }

        registerView(MAIN_VIEW);
        registerView(GAME_VIEW);

		initEnterMainView();
		marketContext.setActivity(this);
	}

	

	// 閿熸枻鎷峰閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹椤�
	private void initEnterMainView() {
		int loginFrameState = getIntent().getIntExtra(
				Constants.LOGIN_INVOKE_STATE, Constants.CM_LOGIN);
		if (loginFrameState == Constants.RE_LOGIN) {

			initContentFrame();

		} else
        {
            //preview_view == "loading view".
			showChoosedView(PREVIEW_VIEW);
		}
	}

	// 閿熸枻鎷峰閿熸枻鎷峰疄閿熸枻鎷烽敓鏂ゆ嫹鍥�
	private void initContentFrame() {
//		setHandleAllMessage(true);

		if (NetworkinfoParser.isNetConnect(marketContext)) {

            AppAdvertiseTaskMark taskMark = taskMarkPool.getAppAdvertiseTaskMark(ResourceEnum.AD_TYPE_EXCEL);
            if (marketContext.getAppCahceManager().getAppItemCount(taskMark) == 0) {
                PageInfo pageInfo = taskMark.getPageInfo();
                if (pageInfo != null) {
                    serviceWraper.getAppAdvertiseByType(this, taskMark,
                            taskMark.getPopType(), pageInfo.getNextPageIndex(),
                            pageInfo.getPageSize());
                }
            }

			/*
            registerView(MAIN_VIEW);
			registerView(CATEGORY_VIEW);
			registerView(DIGG_VIEW);
			*/
		} else {
			Toast.makeText(this, getString(R.string.market_in_none_network),
					800).show();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		super.onStop();
	}

    @Override
    protected void onResume() {
        super.onResume();
        flushTabViews();
    }

    private void flushTabViews() {
        View view = getViewByMarkFromCache(MAIN_VIEW);
        if (view != null) {// && view instanceof App) {
            ((TabableAppView)view).flushView(1);
        }

        view = getViewByMarkFromCache(GAME_VIEW);
        if (view != null) {// && view instanceof App) {
            ((TabableAppView)view).flushView(1);
        }
    }

    /*
	 * 閿熸枻鎷峰彥閿熻甯嫹閿熺粸鎾呮嫹閿熸枻鎷烽敓鏂ゆ嫹鑽介敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹閿熼摪纭锋嫹
	 * 
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (getCurrentTabMark() == PREVIEW_VIEW
				&& KeyEvent.KEYCODE_BACK != keyCode) {
			return super.onKeyDown(keyCode, event);
		}

		// 閿熸枻鎷疯閿熸枻鎷烽敓鏂ゆ嫹閿熷彨绛规嫹閿熸枻鎷烽敓鏂ゆ嫹澶遍敓鏂ゆ嫹閿熻纰夋嫹閿熸枻鎷烽敓锟�
		if (marketContext.isBaseDataOk() && KeyEvent.KEYCODE_BACK == keyCode) {
			int currentMark = getCurrentTabMark();
			boolean handle = false;
			if (currentMark == CATEGORY_VIEW) {
				CommonView commonView = (CommonView) getCurrentTab();
				if (commonView != null) {
					handle = commonView.rotateContentView();
				}
			}
			/*if (!handle) {
				showDialog(R.string.exit_market_confirm);
			}*/
            long current = System.currentTimeMillis();
            if (current - mLastBackPress < 3000) {
                showDialog(R.string.exit_market_confirm);
//                finish();
            }
            else {
                mLastBackPress = current;
                Toast.makeText(this, R.string.press_back_again_to_quit, Toast.LENGTH_LONG).show();
            }
			return true;

		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	public void HandlePurchase()
	{
		BaseApp app=marketContext.mApp;
		DecimalFormat df=new DecimalFormat("#.##");
		String price=df.format(app.getPrice());
	
		Intent intent = new Intent();
		intent.setAction("com.ehoo.paysdk.MAIN");
		Bundle bundle = new Bundle();
		// bundle.putString("merId", "1782");//閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷疯閿熸枻鎷烽敓鏂ゆ嫹
		bundle.putString("appKey", "1001");// 閿熸枻鎷疯閿熸枻鎷烽敓鏂ゆ嫹鏀敓鏂ゆ嫹閿熸枻鎷峰簲閿熺煫绛规嫹閿熸枻鎷烽敓鏂ゆ嫹
		bundle.putString("amount", "1.00");// 閿熸枻鎷疯鏀敓鏂ゆ嫹閿熶茎鏂ゆ嫹閿燂拷
		bundle.putString("chargePoint", "01");// 鏀敓鏂ゆ嫹閿熸枻鎷�
		intent.putExtras(bundle);
		startActivityForResult(intent, 8888);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{

		if(resultCode == 8888) {
			Bundle bundle = data.getExtras();
			String orderId = bundle.getString("orderId");
			String result = bundle.getString("resultCode");

			if(result.equals("0000"))
			{
				Toast.makeText(this, getString(R.string.paysuccess_colon)+orderId, Toast.LENGTH_SHORT).show();
				Message message = Message.obtain();
				message.what = Constants.M_DOWNLOAD_AFTER_PAY_SUCCESS;
				marketContext.handleMarketMessage(message);
			}
			else
			{
				Toast.makeText(this, getString(R.string.payfail), Toast.LENGTH_SHORT).show();
				Message message = Message.obtain();
				message.what = Constants.M_DOWNLOAD_AFTER_PAY_FAIL;
				marketContext.handleMarketMessage(message);
			}
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == R.string.exit_market_confirm) {
			Dialog dialog = new AlertDialog.Builder(this)
					.setTitle(R.string.share_confirm)
					.setMessage(R.string.share_market_confirm)
					.setPositiveButton(getString(R.string.ok),
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									MarketManager
											.shareChooser(
													MarketMainFrame.this,
													getString(R.string.app_name),
													getString(
															R.string.share_app_link,
															HttpMarketService.DOMAIN
																	+ "/download/kapps.apk/"));
									finish();
								}
							})
					.setNegativeButton(getString(R.string.quit),
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
                                    finish();
//									dialog.dismiss();
								}
							}).create();

			return dialog;

		} else {
			return super.onCreateDialog(id);
		}
	}


	@SuppressLint("NewApi")
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// 娣诲姞鑿滃崟椤�
		MenuItem findItem = menu.add(0, MENU_SEARCH, 0, "鏌ユ壘");
		MenuItem manageItem = menu.add(0, MENU_DOWN_MGR, 0, "涓嬭浇绠＄悊");
		MenuItem loginItem = menu.add(0, MENU_LOGIN, 0, "鐧诲綍");

		// 缁戝畾鍒癆ctionBar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			findItem.setIcon(R.drawable.kapps_title_search);
	        findItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
	        loginItem.setIcon(R.drawable.kapps_title_account);
	        loginItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
	        manageItem.setIcon(R.drawable.kapps_title_download);
	        manageItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);    
    } 
	
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ck.market.TabableActivity#getShowViewMark(android.view.View)
	 */
	@Override
	protected int getShowViewMark(View trigger) {
		switch (trigger.getId()) {
//		case R.id.mainPageButton:
//			return MAIN_VIEW;
//
//		case R.id.categoryButton:

//		case R.id.diggButton:
//			return DIGG_VIEW;

		default:
			return Constants.NONE_VIEW;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ck.market.TabableActivity#createContentView(int)
	 */
	@Override
	protected CommonView createContentView(int viewMark) {
		switch (viewMark) {
		case PREVIEW_VIEW:
			return new PreviewView(this);

		case MAIN_VIEW:
            dailyPage = new AppOfDailyCategoryPage(this);
            return dailyPage;

		case GAME_VIEW:
            gamePage = new AppOfGameCategoryPage(this);
            return gamePage;

		case CATEGORY_VIEW:
			return new AppCategoryPage(this);

		case DIGG_VIEW:
			return new AppDiggPage(this);

		default:
			Log.w(TAG, "unkonw module: " + "viewMark = " + viewMark);
			return null;
		}
	}

	@Override
	protected void onBeforeShowView(int viewMark) {
		int oldMark = getCurrentTabMark();
		setButtonSelected(oldMark, false);
		setButtonSelected(viewMark, true);
	}

	private void setButtonSelected(int mark, boolean selected) {
		//
	}

	@Override
	public void receiveResult(ATaskMark taskMark, ActionException exception,
			Object trackerResult) {
		// 鎵ч敓鍙垚鐧告嫹
		if (taskMark instanceof AddFavorTaskMark
				&& taskMark.getTaskStatus() == ATaskMark.HANDLE_ERROR) {
			int currentMark = getCurrentTabMark();
			if (currentMark == MANAGE_VIEW) {
				TabableAppView viewPage = (TabableAppView) getViewByMarkFromCache(MANAGE_VIEW);
				viewPage.flushView(Constants.NONE_VIEW);
			}
			Toast.makeText(this, getString(R.string.favoliten_add_fail), 200)
					.show();

		} else if (taskMark instanceof DeleteFavorTaskMark
				&& taskMark.getTaskStatus() == ATaskMark.HANDLE_ERROR) {
			int currentMark = getCurrentTabMark();
			if (currentMark == MANAGE_VIEW) {
				TabableAppView viewPage = (TabableAppView) getViewByMarkFromCache(MANAGE_VIEW);
				viewPage.flushView(Constants.NONE_VIEW);
			}
			Toast.makeText(this, getString(R.string.favoliten_del_fail), 200)
					.show();
		} else if (taskMark instanceof AppAdvertiseTaskMark) {
            Log.d("temp", "MarketMainFrame, appImageTask");//
//            flushTabViews();
            dailyPage.flushView(1);
            gamePage.flushView(1);
        }
	}

	@Override
	public void subHandleMessage(Message msg) {
		switch (msg.what) {
		// 娉ㄩ敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹閿熺粸纰夋嫹閮戦敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹閿熼樁甯嫹閿熷彨璁规嫹榧楅敓鎻亷鎷烽敓鏂ゆ嫹閿熺但askview
		// 閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷锋皭鏈ㄩ敓琛楃伆顏庢嫹閿熸枻鎷烽敓绲奜FT_MANANGE_VIEW 閿熸枻鎷�TASK_LIST_VIEW
		// 娉ㄩ敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹閿熺粸纰夋嫹閮戦敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹閿熼樁甯嫹閿熷彨璁规嫹榧楅敓鎻亷鎷烽敓鏂ゆ嫹閿熺但askview
		// 閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷锋皭鏈ㄩ敓琛楃伆顏庢嫹閿熸枻鎷烽敓绲奜FT_MANANGE_VIEW 閿熸枻鎷�TASK_LIST_VIEW
		case Constants.M_LOGIN_OUT:
		case Constants.M_UNINSTALL_APK:
		case Constants.M_INSTALL_APK:
		case Constants.M_DOWNLOAD_ACCEPT:
		case Constants.M_DOWNLOAD_PROGRESS:
		case Constants.M_DOWNLOAD_CANCEL:
		case Constants.M_DOWNLOAD_COMPLETED:
		case Constants.M_DOWNLOAD_FAIL:
		case Constants.M_DOWNLOAD_STOP:
		case Constants.M_QUICK_DOWNLOAD_APP:
		case Constants.M_QUICK_PAYMENT_APP:
		case Constants.M_DOWNLOAD_RETRY:
		case Constants.M_SOFTWARE_UPDATED:
		case Constants.M_BATCH_DOWNLOAD_APP_OK:
		case Constants.M_BATCH_BACKUP_SOFTWARE_OK:
		case Constants.M_BATCH_RECOVER_SOFTWARE_OK: {
			// 浣块敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹鏃堕敓鏂ゆ嫹濮嬮敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鍙鎷烽敓瑙掑嚖鎷蜂负null
			int currentMark = getCurrentTabMark();
			if (currentMark != PREVIEW_VIEW && currentMark != CATEGORY_VIEW) {
				CommonView viewPage = (CommonView) getViewByMarkFromCache(currentMark);
				viewPage.flushView(msg.what);
			}
		}
		break;

		case Constants.M_PREVIEW_INIT_OVER:
			initContentFrame();
			msg.arg2 = Constants.M_MESSAGE_END;
			break;

		case Constants.M_CLOSE_MAIN_FRAME:
			finish();
			msg.arg2 = Constants.M_MESSAGE_END;
			break;

		case Constants.M_FAVOR_ADDED:
			handleAddFavor(msg);
			break;

		case Constants.M_FAVOR_DELETE:
			handleDeleteFavor(msg);
			break;

		default:
			break;
		}
	}

	private void handleAddFavor(Message msg) {
		AppItem appItem = (AppItem) msg.obj;
		AddFavorTaskMark taskMark = marketContext.getTaskMarkPool()
				.createAddFavorTaskMark(appItem.getId());
		serviceWraper.forceTakeoverTask(MarketMainFrame.this, taskMark);
		int currentMark = getCurrentTabMark();
		TabableAppView viewPage = null;
		if (currentMark == MANAGE_VIEW) {
			viewPage = (TabableAppView) getViewByMarkFromCache(MANAGE_VIEW);
			viewPage.flushCurrentTabView(msg.what);
		}
	}

	private void handleDeleteFavor(Message msg) {
		AppItem appItem = (AppItem) msg.obj;
		DeleteFavorTaskMark taskMark = marketContext.getTaskMarkPool()
				.createDeleteFavorTaskMark(appItem.getId());
		serviceWraper.forceTakeoverTask(MarketMainFrame.this, taskMark);
		int currentMark = getCurrentTabMark();
		TabableAppView viewPage = null;
		if (currentMark == MANAGE_VIEW) {
			viewPage = (TabableAppView) getViewByMarkFromCache(MANAGE_VIEW);
			viewPage.flushCurrentTabView(msg.what);
		}
	}
	
	@Override
	protected void initCustomActionbar() {
		ActionBar actionBar = getSupportActionBar();
		// 璁剧疆鏍囬
        //actionBar.setTitle("ActionBarTest");  
        actionBar.setDisplayHomeAsUpEnabled(false);
//        actionBar.setDis
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
        actionBar.addTab(actionBar.newTab()
                .setText(R.string.cat_application)/*home*/
                .setTabListener(mBarTabListener));
        VIEW_COUNT++;
        /*current requirement is unknown, just comment category.*/
//        actionBar.addTab(actionBar.newTab()
//                .setText(R.string.category)
//                .setTabListener(mBarTabListener)); 
//        VIEW_COUNT++;
        actionBar.addTab(actionBar.newTab()
                .setText(R.string.cat_game)/*digg*/
                .setTabListener(mBarTabListener));
        VIEW_COUNT++;
	}

	//for view pager
	private void initPaper() {
        if (mViewPager ==null) {
            mViewPager = (ViewPager) findViewById(R.id.pager);

            mViewPager.setAdapter(new MyViewPagerAdapter());
            mViewPager.setCurrentItem(0);
            mViewPager.setOnPageChangeListener(new MyOnPageChangeListener());
        }
	}
	
	private final TabListener mBarTabListener = new ActionBar.TabListener(){  
        private final static String TAG = "MyBarTabListener";  
          
        @Override  
        public void onTabReselected(ActionBar.Tab arg0, FragmentTransaction arg1) {  
        }
        
        @Override  
        public void onTabSelected(ActionBar.Tab arg0, FragmentTransaction arg1) {  
			// 褰撴洿鏀笰ctionBar涓婄殑Tab椤垫湁鏇存敼鏃讹紝ViewPager鏄剧ず鐩稿叧鐨勯〉闈�
            if (mViewPager != null)    
            	mViewPager.setCurrentItem(arg0.getPosition());  
        }
        
        @Override  
        public void onTabUnselected(ActionBar.Tab arg0, FragmentTransaction arg1) {  
        }
    };

	//page adapter
	public class MyViewPagerAdapter extends PagerAdapter{
          
        public MyViewPagerAdapter() {
        }
  
        @Override  
        public void destroyItem(ViewGroup container, int position, Object object)   {     
            container.removeView(getViewByMarkFromCache(position));//mListViews.get(position));  
        }
  
        @Override  
        public Object instantiateItem(ViewGroup container, int position) {
             View view = getViewByMarkFromCache(position);
             container.addView(view, 0);  //TODO correct??
             return view;
        }
  
        @Override  
        public int getCount() {
            return  VIEW_COUNT;//mListViews.size();  
        }

        @Override  
        public boolean isViewFromObject(View arg0, Object arg1) {             
            return arg0==arg1;  
        }
    }

	public class MyOnPageChangeListener implements OnPageChangeListener{
        @Override
        public void onPageScrollStateChanged(int state) {  }
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {  }
        @Override
        public void onPageSelected(int position) {
        	ActionBar actionBar = getSupportActionBar();
        	actionBar.setSelectedNavigationItem(position);
            //do showChoosedView will make mCurrentTab correct
            showChoosedView(position);
			// Toast.makeText(MarketMainFrame.this, "鎮ㄩ�鎷╀簡"+
			// mViewPager.getCurrentItem()+"椤靛崱", Toast.LENGTH_SHORT).show();
        }
    }

}
