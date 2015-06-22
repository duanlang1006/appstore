package com.kapps.market;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kapps.market.bean.AppBadness;
import com.kapps.market.bean.AppItem;
import com.kapps.market.bean.MImageType;
import com.kapps.market.log.LogUtil;
import com.kapps.market.service.ActionException;
import com.kapps.market.task.IResultReceiver;
import com.kapps.market.task.mark.ATaskMark;
import com.kapps.market.task.mark.AppDetailTaskMark;
import com.kapps.market.task.mark.AppImageTaskMark;
import com.kapps.market.task.mark.AppSummaryTaskMark;
import com.kapps.market.ui.CommonView;
import com.kapps.market.ui.RatingView;
import com.kapps.market.ui.detail.AppBadnessView;
import com.kapps.market.ui.detail.AppDetailMainView;
import com.kapps.market.ui.detail.AppOtherVersionView;
import com.kapps.market.ui.detail.AppPermssionView;
import com.kapps.market.util.Constants;
import com.kapps.market.util.Util;

/**
 * 2010-6-21 应用的明细 <br>
 * 合理设置后它会完成概要和明显的获取<br>
 * 查看软件明细： key： 软件包名<br>
 * http://mgy.com/details?id=" + key<br>
 * shmarket://details?id=key<br>
 * 
 * @author admin
 */
public class AppDetailFrame extends TabableActivity implements IResultReceiver {

	public static final String TAG = "AppDetailFrame";

	// 软件介绍
	public static final int DETAIL_MAIN_VIEW = 1;
	// 问题举报
	public static final int DETAIL_BADNESS_VIEW = 2;
	// 进度条
	public static final int PROGRESS_VIEW = 3;
	// 权限视图
	public static final int DETAIL_PERMISSION_VIEW = 4;
	// 其他版本
	public static final int APP_OTHER_VERSION_VIEW = 5;

	// 当前处理的软件
	private AppItem appItem;

	// 是否当前处于作者应用模式(查看作者的应用)
	private boolean isAuthorMode;
	private boolean isHistoryAppMode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.m_detail_page);
		contentFrame = (FrameLayout)findViewById(R.id.contentFrame);

		// 主子页面
		registerView(DETAIL_MAIN_VIEW);
		// 先进度
		showChoosedView(PROGRESS_VIEW);

		Uri data = getIntent().getData();
		if (data != null) {
			initForExtractInvoke(data);

		} else {
			initForInsideInvoke(savedInstanceState);
		}
	}

	// 市场uri型调用
	private void initForExtractInvoke(Uri uri) {
		String id = uri.getQueryParameter("id");
		LogUtil.d(TAG, "initForExtractInvoke id: " + id);
		if (id == null) {
			Toast.makeText(this, getString(R.string.cant_find_app_detail), 200).show();
			finish();

		} else {
			String vCodeStr = uri.getQueryParameter("vcode");
			if (vCodeStr == null) {
				doWaitSoftwareDetail(id, -1);
			} else {
				doWaitSoftwareDetail(id, Integer.parseInt(vCodeStr));
			}
		}
	}

	// 市场非uri型调用
	private void initForInsideInvoke(Bundle savedInstanceState) {
		// 是否是显示作者的其他应用。
		isAuthorMode = getIntent().getBooleanExtra(Constants.DETAIL_AUTHOR_APP, false);
		isHistoryAppMode = getIntent().getBooleanExtra(Constants.DETAIL_HISTORY_APP, false);

		// 软件id
		int id = getIntent().getIntExtra(Constants.APP_ID, -9999);
		if (id != 9999) {
			appItem = marketContext.getAppCahceManager().getAppItemById(id);
		}

		// 内存过低造成
		if (appItem == null && savedInstanceState != null) {
			id = savedInstanceState.getInt(Constants.APP_ID);
			appItem = marketContext.getAppCahceManager().getAppItemById(id);
		}

		// 如果是一个本地已经安装的软件，那么等待获取软件信息的结束

		if (appItem != null) {
			doInitDetailFrame(appItem);

		} else {
			boolean isSoftwareDetail = getIntent().getBooleanExtra(Constants.DETAIL_LOCALSOFTWARE_APP, false);
			// 来自已安装软件
			if (isSoftwareDetail) {
				String pname = getIntent().getStringExtra(Constants.APP_PACKAGE_NAME);
				int versionCode = getIntent().getIntExtra(Constants.APP_VERSION_CODE, 0);
				doWaitSoftwareDetail(pname, versionCode);

			} else {
				boolean isPurchaseDetail = getIntent().getBooleanExtra(Constants.DETAIL_DOWNLOADABLE_APP, false);
				// 来自购买软件
				if (isPurchaseDetail) {
					int appId = getIntent().getIntExtra(Constants.APP_ID, -1);
					doWaitPurchaseDetail(appId);

				} else {
					Toast.makeText(this, getString(R.string.cant_find_app_detail), 200).show();
					finish();
				}
			}
		}
	}

	// 初始化明细
	private void doInitDetailFrame(AppItem app) {
		// 消息表示
		setHandleMessageMark((app.getPackageName() + app.getVersionCode()).hashCode());

		// 首先显示固定部分
		initFixView();

		// 检查是否已经提取过明细数据了
		if (appItem.getAppDetail() != null) {
			// 首先显示软件介绍
			showChoosedView(DETAIL_MAIN_VIEW);

		} else {
			serviceWraper.getAppDetailById(this,
					marketContext.getTaskMarkPool().createAppDetailTaskMark(appItem.getId()), appItem.getId());
		}
	}

	// 等待软件明细的获取
	private void doWaitSoftwareDetail(String pname, int versionCode) {
		AppSummaryTaskMark appSummaryTaskMark = marketContext.getTaskMarkPool().createAppSummaryTaskMark(pname,
				versionCode);
		serviceWraper.getAppSummary(this, appSummaryTaskMark, pname, versionCode);

	}

	// 等待软件明细的获取
	private void doWaitPurchaseDetail(int appId) {
		AppSummaryTaskMark appSummaryTaskMark = marketContext.getTaskMarkPool().createAppSummaryTaskMark(appId);
		serviceWraper.getAppSummary(this, appSummaryTaskMark, appId);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ck.market.task.IResultReceiver#receiveResult(com.ck.market.
	 * task.ATaskMark, com.ck.market.service.ServiceException)
	 */
	@Override
	public void receiveResult(ATaskMark taskMark, ActionException exception, Object trackerResult) {
		// 明细
		if (taskMark instanceof AppDetailTaskMark) {
			if (taskMark.getTaskStatus() == ATaskMark.HANDLE_OVER) {
				showChoosedView(DETAIL_MAIN_VIEW);

			} else {
				Toast.makeText(this, getString(R.string.cant_find_app_detail), 200).show();
				finish();
			}

			// 概要
		} else if (taskMark instanceof AppSummaryTaskMark) {
			if (taskMark.getTaskStatus() == ATaskMark.HANDLE_OVER) {
				appItem = (AppItem) trackerResult;

				// 自动获取软件详细信息
				ATaskMark detailTaskMark = marketContext.getTaskMarkPool().createAppDetailTaskMark(appItem.getId());
				marketContext.getServiceWraper().getAppDetailById(this, detailTaskMark, appItem.getId());

				doInitDetailFrame(appItem);

			} else {
				Toast.makeText(this, getString(R.string.cant_find_app_detail), 200).show();
				finish();
			}

		} else if (taskMark instanceof AppImageTaskMark) {
			if (taskMark.getTaskStatus() == ATaskMark.HANDLE_OVER) {
				Drawable icon = marketContext.getAssertCacheManager().getAppIconFromCache(appItem.getId(), true);
				if (icon != null) {
					View convertView = findViewById(R.id.appSummaryView);
					((ImageView) convertView.findViewById(R.id.appIconView)).setImageDrawable(icon);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt(Constants.APP_ID, appItem == null ? -1 : appItem.getId());
		super.onSaveInstanceState(outState);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.kapps.market.TabableActivity#createContentView(int)
	 */
	@Override
	protected View createContentView(int viewMark) {
		switch (viewMark) {
		case DETAIL_MAIN_VIEW:
			AppDetailMainView appDetailMainView = new AppDetailMainView(this, appItem, isAuthorMode, isHistoryAppMode);
			return appDetailMainView;

		case DETAIL_BADNESS_VIEW:
			return new AppBadnessView(this, appItem);

		case PROGRESS_VIEW:
			View proView = LayoutInflater.from(this).inflate(R.layout.progressbar_view_l_r, null);
			return proView;

		case DETAIL_PERMISSION_VIEW:
			return new AppPermssionView(this, appItem);
			
		case APP_OTHER_VERSION_VIEW:
			AppOtherVersionView appOtherVersionView = new AppOtherVersionView(this, appItem);
			appOtherVersionView.initLoadleList(taskMarkPool.getHistoryAppTaskMark(appItem.getId()));
			return appOtherVersionView;

		default:
			return null;
		}
	}

	// 固定区域
	private void initFixView() {
		View convertView = findViewById(R.id.appSummaryView);

		Drawable icon = marketContext.getAssertCacheManager().getAppIconFromCache(appItem.getId(), true);
		if (icon == null) {
			// 空图标
			((ImageView) convertView.findViewById(R.id.appIconView)).setImageDrawable(marketContext.emptyAppIcon);
			AppImageTaskMark imageTaskMark = taskMarkPool.createAppImageTaskMark(appItem.getId(), appItem.getIconUrl(),
					MImageType.APP_ICON);
			serviceWraper.getAppImageResource(this, imageTaskMark, null, appItem.getId(), appItem.getIconUrl(),
					MImageType.APP_ICON);

		} else {
			((ImageView) convertView.findViewById(R.id.appIconView)).setImageDrawable(icon);
		}

		// 名字
		int commonColor = getResources().getColor(R.color.app_item_detail_name);
		ActionBar actionBar = getSupportActionBar();
		//设置标题  
        actionBar.setTitle(appItem.getName());
        if (appItem.getAuthorName().length() > 0) {
        	actionBar.setSubtitle(appItem.getAuthorName());
        }
        else {
        	
        }
		
		TextView textField = (TextView) convertView.findViewById(R.id.appVersion);
		textField.setText(appItem.getVersion());
		
		textField = (TextView) convertView.findViewById(R.id.appSize);
		textField.setText(Util.getSizeDes(appItem));
		
		// 开发者
//		textField = (TextView) convertView.findViewById(R.id.appDeveloperLabel);
//		textField.setText(appItem.getAuthorName());

		// 星星
		((RatingView) convertView.findViewById(R.id.appRatingView)).setRating(appItem.getRating());
		// 这台
		textField = (TextView) convertView.findViewById(R.id.appStateLabel);
		updateAppStateLabel(textField);
	}

	// 更新软件状态
	private void updateAppStateLabel(TextView textField) {
		if (textField == null) {
			textField = (TextView) findViewById(R.id.appStateLabel);
		}
		// 状态
		String viweing = marketContext.getMarketManager().getAppViewDescribe(appItem);
		if (viweing != null) {
			textField.setTextColor(getResources().getColor(R.color.app_state_highlight));
			textField.setText(viweing);

		} else {
			textField.setTextColor(getResources().getColor(R.color.app_item_detail_name));
			if (appItem.isFree()) {
				textField.setText(getString(R.string.free));

			} else {
				textField.setText("￥" + appItem.getPrice());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ck.market.TabableActivity#getShowViewMark(android.view.View)
	 */
	@Override
	protected int getShowViewMark(View trigger) {
		return Constants.NONE_VIEW;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.app_detail_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.shareAppButton) {
            if (appItem != null && appItem.getAppDetail() != null) {
                MarketManager.shareChooser(this, appItem.getName(), appItem.getAppDetail().getDescribeSplit());
            }
        }
        return super.onOptionsItemSelected(item);    
    }

    @Override
	public boolean onPrepareOptionsMenu(Menu menu) {
//		menu.findItem(R.id.logoutMenuItem).setVisible(false);
//		menu.findItem(R.id.closeMenuItem).setVisible(false);
		return super.onPrepareOptionsMenu(menu);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		int mark = getCurrentTabMark();
		if (keyCode == KeyEvent.KEYCODE_BACK && mark != DETAIL_MAIN_VIEW && appItem != null
				&& appItem.getAppDetail() != null) {
			showChoosedView(DETAIL_MAIN_VIEW);
			return true;

		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Constants.SCREENT_VIEW_CLOSE && getCurrentTabMark() == DETAIL_MAIN_VIEW) {
			((CommonView) getCurrentTab()).flushView(Constants.NONE_VIEW);
		}
	}

	@Override
	public void subHandleMessage(Message msg) {
		LogUtil.d(TAG, "msg: " + msg);
		switch (msg.what) {
		case Constants.M_INSTALL_APK:
		case Constants.M_UNINSTALL_APK:
		case Constants.M_DOWNLOAD_COMPLETED:
		case Constants.M_DOWNLOAD_STOP:
		case Constants.M_DOWNLOAD_FAIL:
			updateAppStateLabel(null);
			doViewChainMessage(DETAIL_MAIN_VIEW, msg);
			break;

		case Constants.M_BADNESS_Ok: {
			AppBadness appBadness = (AppBadness) msg.obj;
			// 提交, 将新的举报作为附件，以便tracker合理的对数据进行处理。
			serviceWraper.commitAppBadness(this,
					marketContext.getTaskMarkPool().createCommitBadnessTaskWraper(appBadness.getAppId()), appBadness,
					appBadness);
			showChoosedView(DETAIL_MAIN_VIEW);
		}

			break;

		case Constants.M_BADNESS_CANCEL:
		case Constants.M_PERMISSION_BACk:
			showChoosedView(DETAIL_MAIN_VIEW);
			break;

		// 下载请求, 注意这里有可能是作者列表中触发的快捷下载任务。
		case Constants.M_DOWNLOAD_ACCEPT:
			finish();
			break;

		case Constants.M_BADNESS_SHOW_VIEW:
			showChoosedView(DETAIL_BADNESS_VIEW);
			break;
			
		case Constants.M_SHOW_APP_OTHER_VERSION:
			showChoosedView(APP_OTHER_VERSION_VIEW);
			break;

		case Constants.M_PERMISSION_SHOW_VIEW:
			Log.e("M_SHOW_APP_OTHER_VERSION", "M_SHOW_APP_OTHER_VERSION");
			showChoosedView(DETAIL_PERMISSION_VIEW);
			break;

		case Constants.M_SCREEN_SHOTS_SHOW:
			showScreenShotFrame(msg);
			break;

		default:
			break;
		}
	}

	private void showScreenShotFrame(Message msg) {
		Intent intent = new Intent(AppDetailFrame.this, ScreensFrame.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		if (msg.obj != null) {
			intent.putExtra(Constants.CHOOSED_SCREEN_INDEX, Integer.parseInt(msg.obj.toString()));
		}
		intent.putExtra(Constants.APP_ID, appItem.getId());
		startActivityForResult(intent, Constants.SCREENT_VIEW_CLOSE);
	}

	private void doViewChainMessage(int viewMark, Message msg) {
		CommonView commonView = (CommonView) getViewByMarkFromCache(viewMark);
		commonView.handleChainMessage(msg);
	}

	@Override
	protected void initCustomActionbar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
	}

}
